package de.lumesolutions.netty5.client;



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
import de.lumesolutions.netty5.Netty5Component;
import de.lumesolutions.netty5.common.packet.Packet;
import de.lumesolutions.netty5.common.packet.auth.AuthPacket;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public final class Netty5ClientHandler extends SimpleChannelInboundHandler<Packet> {

    private final Netty5Client netty5Client;

    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Packet packet) throws Exception {
        netty5Client.thisChannel().transmitter().call(packet, null);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.channel().writeAndFlush(new AuthPacket(netty5Client.identity(), netty5Client.authProperty()));
        netty5Client.connectionState(Netty5Component.ConnectionState.CONNECTED);
        netty5Client.thisChannel(new Netty5ClientChannel(netty5Client.identity(), ctx.channel(), new Netty5ClientPacketTransmitter(
                netty5Client.bossGroup(),
                packet -> netty5Client.thisChannel().sendPacket(packet)
        )));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if ((!ctx.channel().isActive() || !ctx.channel().isOpen() || !ctx.channel().isWritable())) {
            netty5Client.connectionState(Netty5Component.ConnectionState.DISCONNECTED);
            ctx.channel().close();
        }
    }

    @Override
    public void channelExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (!(cause instanceof IOException)) {
            if (cause.getMessage().equalsIgnoreCase("null")) return;
            cause.printStackTrace();
        }
    }
}
