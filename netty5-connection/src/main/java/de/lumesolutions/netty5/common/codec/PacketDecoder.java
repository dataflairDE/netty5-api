package de.lumesolutions.netty5.common.codec;

/*
 * MIT License
 *
 * Copyright (c) 2024 02:04 Mario Pascal K.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import de.lumesolutions.netty5.Netty5ClientChannel;
import de.lumesolutions.netty5.common.packet.Packet;
import io.netty5.buffer.Buffer;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.handler.codec.ByteToMessageDecoder;

public final class PacketDecoder extends ByteToMessageDecoder {

    private final Netty5ClientChannel.Identity identity;

    public PacketDecoder(Netty5ClientChannel.Identity identity) {
        this.identity = identity;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void decode(ChannelHandlerContext ctx, Buffer in) {
        var buffer = new CodecBuffer(in);
        var className = buffer.readString();

        try {
            var readableBytes = buffer.readInt();
            var content = new CodecBuffer(in.copy(in.readerOffset(), readableBytes, true));
            in.skipReadableBytes(readableBytes);

            var packet = (Packet) Class.forName(className).getConstructor(CodecBuffer.class).newInstance(content);
            buffer.resetBuffer();
            ctx.fireChannelRead(packet);
        } catch (Exception e) {
            System.err.println("[identity: " + identity.name() + "] Error while decoding packet " + className);
            e.printStackTrace();
        }
    }
}
