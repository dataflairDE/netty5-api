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
import de.dataflair.netty5.client.Netty5ClientPacketTransmitter;
import de.dataflair.netty5.common.packet.Packet;
import de.dataflair.netty5.common.packet.auth.AuthPacket;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public final class Netty5ServerHandler extends SimpleChannelInboundHandler<Packet> {

    private final Map<SocketAddress, Channel> unauthenticated = new ConcurrentHashMap<>();
    private final Netty5Server server;

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Packet packet) throws Exception {
        if (packet instanceof AuthPacket authPacket) {
            var netty5Channel = new Netty5ClientChannel(authPacket.identity(), channelHandlerContext.channel(), null);
            for (var authPredicate : server.authPredicates()) {
                if (!authPredicate.test(new Netty5ClientChannel.AuthType(netty5Channel, authPacket.properties()))) {
                    return;
                }
            }
            var transmitter = new Netty5ClientPacketTransmitter(channelHandlerContext.channel().executor(), netty5Channel::sendPacket);
            netty5Channel.transmitter(transmitter);
            for (var authenticationAction : server.authenticationActions()) {
                authenticationAction.accept(netty5Channel);
            }
            server.connections().add(netty5Channel);
            unauthenticated.remove(channelHandlerContext.channel().remoteAddress());
            return;
        }

        if (server.connections()
                .stream()
                .noneMatch(netty5ClientChannel -> netty5ClientChannel.channel().equals(channelHandlerContext.channel()))) {
            System.out.println("Try to receive packet of channel which not authenticated");
            return;
        }

        var sender = server.connections()
                .stream()
                .filter(netty5ClientChannel -> netty5ClientChannel.channel().equals(channelHandlerContext.channel()))
                .findFirst()
                .orElse(null);

        if (sender == null) {
            throw new NullPointerException("No sender transmitter found.");
        }

        server.packetTransmitter().call(packet, sender);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.unauthenticated.put(ctx.channel().remoteAddress(), ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.unauthenticated.remove(ctx.channel().remoteAddress());
        var copy = new ArrayList<>(server.connections());
        for (var netty5ClientChannel : server.connections()) {
            if (netty5ClientChannel.channel().equals(ctx.channel())) {
                for (var inactiveAction : server.inactiveActions()) {
                    inactiveAction.accept(netty5ClientChannel);
                }
                copy.remove(netty5ClientChannel);
            }
        }
        server.connections(copy);
    }

    @Override
    public void channelExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause instanceof IOException)) {
            if (cause.getMessage().equalsIgnoreCase("null")) return;
            System.err.println((server.serverIdentity() != null ? "[server: " + server.serverIdentity().name() + "]" : "") + "Exception caught: " + cause.getMessage());
            cause.printStackTrace();
        }
    }
}
