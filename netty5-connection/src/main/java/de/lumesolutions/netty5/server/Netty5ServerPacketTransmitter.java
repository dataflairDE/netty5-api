package de.lumesolutions.netty5.server;



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
import de.lumesolutions.netty5.TriConsumer;
import de.lumesolutions.netty5.common.packet.Netty5PacketTransmitter;
import de.lumesolutions.netty5.common.packet.Packet;
import de.lumesolutions.netty5.common.packet.QueryPacket;
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

    @Override
    public void publishPacket(@NotNull Packet packet) {
        // todo send to every connected client
    }

    @Override
    public void callActions(@NotNull Packet packet, @Nullable Netty5ClientChannel sender) {
    }
}
