package de.lumesolutions.netty5;



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

import de.lumesolutions.netty5.common.codec.PacketDecoder;
import de.lumesolutions.netty5.common.codec.PacketEncoder;
import io.netty5.channel.Channel;
import io.netty5.channel.ChannelInitializer;
import io.netty5.channel.SimpleChannelInboundHandler;
import io.netty5.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty5.handler.codec.LengthFieldPrepender;

public abstract class Netty5ChannelInitializer extends ChannelInitializer<Channel> {

    private final Netty5ClientChannel.Identity identity;

    public Netty5ChannelInitializer(Netty5ClientChannel.Identity identity) {
        this.identity = identity;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        channel.pipeline()
                .addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, Integer.BYTES, 0, Integer.BYTES))
                .addLast(new PacketDecoder(identity))
                .addLast(new LengthFieldPrepender(Integer.BYTES))
                .addLast(new PacketEncoder(identity))
                .addLast(handler());
    }

    public abstract SimpleChannelInboundHandler<?> handler();
}
