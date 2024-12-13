package de.dataflair.netty5.server;



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
import de.dataflair.netty5.TriConsumer;
import de.dataflair.netty5.common.packet.Netty5PacketTransmitter;
import de.dataflair.netty5.common.packet.Packet;
import de.dataflair.netty5.common.packet.QueryPacket;
import de.dataflair.netty5.common.packet.RespondPacket;
import io.netty5.channel.EventLoopGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class Netty5ServerPacketTransmitter extends Netty5PacketTransmitter {

    private final TriConsumer<QueryPacket, Class<Packet>, Consumer<Packet>> queryPacketConsumer;

    public Netty5ServerPacketTransmitter(EventLoopGroup eventExecutors,
                                         Consumer<Packet> packetConsumer,
                                         TriConsumer<QueryPacket, Class<Packet>, Consumer<Packet>> queryPacketConsumer) {
        super(eventExecutors, packetConsumer);
        this.queryPacketConsumer = queryPacketConsumer;
    }

    @Override
    public <P extends Packet> CompletableFuture<Packet> queryPacket(@NotNull QueryPacket queryPacket, Class<P> packet) {
        return null;
    }

    @Override
    public <P extends Packet> P queryPacketDirect(@NotNull QueryPacket queryPacket, Class<P> packetClass) {
        return null;
    }

    @Override
    public <P extends Packet> void queryPacket(@NotNull QueryPacket queryPacket, Class<P> packet, Consumer<P> callback) {
        // todo send to every connected client
    }

    public <Q extends QueryPacket> void callResponder(@NotNull Q query, @NotNull Netty5ClientChannel sender) {
        if (responders().containsKey(query.getClass())) {
            responders().get(query.getClass()).forEach((_, queryPacketRespondPacketFunction) -> {
                var respondPacket = queryPacketRespondPacketFunction.apply(query);
                respondPacket.queryId(query.queryId());
                respondPacket.buffer().writeUniqueId(query.queryId());
                respondPacket.writeBuffer(respondPacket.buffer());
                sender.transmitter().publishPacket(respondPacket);
            });
        } else {
            sender.transmitter().callResponder(query);
        }
    }

    @Override
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

        if (packet instanceof QueryPacket queryPacket) {
            if (sender != null) {
                this.callResponder(queryPacket, sender);
            } else {
                throw new RuntimeException("Sender cannot be null by QueryPacket to Server");
            }
        }

        this.callActions(packet, sender);

        if (listener().containsKey(packet.getClass())) {
            listener().get(packet.getClass()).forEach((_, packetConsumer) -> packetConsumer.accept(sender, packet));
        }
    }

    @Override
    public void publishPacket(@NotNull Packet packet) {
        packetConsumer().accept(packet);
    }

    @Override
    public void callActions(@NotNull Packet packet, @Nullable Netty5ClientChannel sender) {
    }
}
