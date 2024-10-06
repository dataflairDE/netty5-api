package de.lumesolutions.netty5.common.packet;



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

import de.lumesolutions.netty5.Netty5ClientChannel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Getter
public final class PacketTransmitter {

    private final Netty5ClientChannel channel;
    private final Map<Class<? extends Packet>, Map<String, Consumer<Packet>>> listener = new ConcurrentHashMap<>();

    public PacketTransmitter(@NotNull Netty5ClientChannel channel) {
        this.channel = channel;
    }

    @SuppressWarnings("unchecked")
    public <P extends Packet> void listen(@NotNull Class<P> packetClass, @NotNull String key, @NotNull Consumer<P> callback) {
        var listeners = this.listener.getOrDefault(packetClass, new ConcurrentHashMap<>());
        listeners.put(key, (Consumer<Packet>) callback);
        this.listener.put(packetClass, listeners);
    }

    public <P extends Packet> void listen(@NotNull Class<P> packetClass, @NotNull Consumer<P> callback) {
        this.listen(packetClass, new SecureRandom()
                .ints(10, 0, 62)
                .mapToObj("ABCDEFGHIJKLMNOPQRSTUVWXY#abcdefghilkmnopΩqrstuvwxyz0123456789"::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString(), callback);
    }

    public void unregister(Class<? extends Packet> packetClass, @NotNull String key) {
        this.listener.getOrDefault(packetClass, new ConcurrentHashMap<>()).remove(key);
    }

    public void publishPacket(@NotNull Packet packet) {
        this.channel.sendPacket(packet);
    }
}
