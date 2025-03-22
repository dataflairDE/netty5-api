package de.dataflair.netty5.common.packet;



/*
 * Copyright 2023-2024 netty5-api contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import de.dataflair.netty5.Netty5ClientChannel;
import de.dataflair.netty5.common.codec.CodecBuffer;
import io.netty5.channel.EventLoopGroup;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
public abstract class Netty5PacketTransmitter {
    private final EventLoopGroup eventExecutors;
    private final Consumer<Packet> packetConsumer;
    private final Map<Class<? extends Packet>, Map<String, BiConsumer<Netty5ClientChannel, Packet>>> listener = new ConcurrentHashMap<>();
    private final Map<UUID, Consumer<Packet>> futureRequests = new ConcurrentHashMap<>();
    private final Map<UUID, Packet> directRequests = new HashMap<>();
    private final Map<Class<? extends RequestPacket>, Map<String, Function<RequestPacket, RespondPacket>>> responders = new ConcurrentHashMap<>();

    protected Netty5PacketTransmitter(@NotNull EventLoopGroup eventExecutors,
                                      @NotNull Consumer<Packet> packetConsumer) {
        this.eventExecutors = eventExecutors;
        this.packetConsumer = packetConsumer;
    }

    @SuppressWarnings("unchecked")
    public <P extends Packet> void listen(@NotNull Class<P> packetClass,
                                          @NotNull String key,
                                          @NotNull BiConsumer<Netty5ClientChannel, P> callback) {
        var listeners = this.listener.getOrDefault(packetClass, new ConcurrentHashMap<>());
        listeners.put(key, (BiConsumer<Netty5ClientChannel, Packet>) callback);
        this.listener.put(packetClass, listeners);
    }

    public <P extends Packet> void listen(@NotNull Class<P> packetClass,
                                          @NotNull BiConsumer<Netty5ClientChannel, P> callback) {
        this.listen(packetClass, generateRandomKey(), callback);
    }

    @SuppressWarnings("unchecked")
    public <Q extends RequestPacket> void listenQuery(@NotNull Class<Q> queryClass,
                                                    @NotNull String key,
                                                    @NotNull Function<Q, RespondPacket> callback) {
        var responders = this.responders.getOrDefault(queryClass, new ConcurrentHashMap<>());
        responders.put(key, (Function<RequestPacket, RespondPacket>) callback);
        this.responders.put(queryClass, responders);
    }

    public <Q extends RequestPacket> void listenQuery(@NotNull Class<Q> queryClass,
                                                    @NotNull Function<Q, RespondPacket> callback) {
        this.listenQuery(queryClass, generateRandomKey(), callback);
    }

    public void unregisterListener(Class<? extends Packet> packetClass,
                                   @NotNull String key) {
        this.listener.getOrDefault(packetClass, new ConcurrentHashMap<>()).remove(key);
    }

    public void unregisterResponder(Class<? extends RespondPacket> packetClass,
                                    @NotNull String key) {
        this.responders.getOrDefault(packetClass, new ConcurrentHashMap<>()).remove(key);
    }

    public void publishPacket(@NotNull Packet packet) {
        packetConsumer.accept(packet);
    }

    @SuppressWarnings("unchecked")
    public <P extends Packet> void queryPacket(@NotNull RequestPacket requestPacket, Class<P> packet, Consumer<P> callback) {
        requestPacket.queryId = UUID.randomUUID();
        requestPacket.buffer.writeUniqueId(requestPacket.queryId);
        requestPacket.writeBuffer(requestPacket.buffer);
        try {
            futureRequests.put(requestPacket.queryId, (Consumer<Packet>) callback);
        } catch (ClassCastException exception) {
            CompletableFuture.failedFuture(exception);
        }
        this.publishPacket(requestPacket);
    }

    public <P extends Packet> P queryPacketDirect(@NotNull RequestPacket requestPacket, Class<P> packetClass) {
        requestPacket.queryId = UUID.randomUUID();
        requestPacket.buffer.writeUniqueId(requestPacket.queryId);
        requestPacket.writeBuffer(requestPacket.buffer);
        directRequests.put(requestPacket.queryId, new NullSimulationPacket());
        eventExecutors.execute(() -> packetConsumer.accept(requestPacket));

        var i = 0;
        while (directRequests.get(requestPacket.queryId).getClass().equals(NullSimulationPacket.class) && i++ < 5000) {
            try {
                Thread.sleep(0, 500000);
            } catch (InterruptedException ignored) {
            }
        }

        var result = directRequests.get(requestPacket.queryId);
        directRequests.remove(requestPacket.queryId);
        if (result.getClass().equals(NullSimulationPacket.class))
            return null;
        return (P) result;
    }

    @SuppressWarnings("unchecked")
    public <P extends Packet> CompletableFuture<Packet> queryPacket(@NotNull RequestPacket requestPacket, Class<P> packet) {
        var future = new CompletableFuture<P>();
        this.queryPacket(requestPacket, packet, future::complete);
        try {
            return (CompletableFuture<Packet>) future;
        } catch (ClassCastException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    public abstract void callActions(@NotNull Packet packet, Netty5ClientChannel sender);

    public void call(@NotNull Packet packet, @Nullable Netty5ClientChannel sender) {
        if (packet instanceof RespondPacket respondPacket) {
            if (directRequests().containsKey(respondPacket.queryId())) {
                directRequests().put(respondPacket.queryId(), packet);
            }
            if (futureRequests().containsKey(respondPacket.queryId())) {
                futureRequests().get(respondPacket.queryId()).accept(packet);
                futureRequests().remove(respondPacket.queryId());
            }
        }

        if (packet instanceof RequestPacket requestPacket) {
            this.callResponder(requestPacket);
        }

        this.callActions(packet, sender);

        if (listener().containsKey(packet.getClass())) {
            listener().get(packet.getClass()).forEach((_, packetConsumer) -> packetConsumer.accept(sender, packet));
        }
    }

    private String generateRandomKey() {
        return new SecureRandom()
                .ints(10, 0, 62)
                .mapToObj("ABCDEFGHIJKLMNOPQRSTUVWXY#abcdefghilkmnopΩqrstuvwxyz0123456789"::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }

    public <Q extends QueryPacket> void callResponder(@NotNull Q query) {
        if (responders.containsKey(query.getClass())) {
            responders.get(query.getClass()).forEach((_, queryPacketRespondPacketFunction) -> {
                var respondPacket = queryPacketRespondPacketFunction.apply(query);
                respondPacket.queryId(query.queryId);
                respondPacket.buffer.writeUniqueId(query.queryId);
                respondPacket.writeBuffer(respondPacket.buffer);
                publishPacket(respondPacket);
            });
        }
    }

    public static class NullSimulationPacket extends RespondPacket {

        public NullSimulationPacket() {
        }

        public NullSimulationPacket(@NotNull CodecBuffer buffer) {
            super(buffer);
        }

        @Override
        public void writeBuffer(@NotNull CodecBuffer codecBuffer) {

        }

        @Override
        public void readBuffer(@NotNull CodecBuffer codecBuffer) {

        }
    }
}
