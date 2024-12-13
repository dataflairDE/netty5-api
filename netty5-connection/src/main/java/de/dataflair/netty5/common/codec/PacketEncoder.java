package de.dataflair.netty5.common.codec;

import de.dataflair.netty5.Netty5ClientChannel;
import de.dataflair.netty5.common.packet.Packet;
import io.netty5.buffer.Buffer;
import io.netty5.channel.ChannelHandlerContext;
import io.netty5.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public final class PacketEncoder extends MessageToByteEncoder<Packet> {

    private final Netty5ClientChannel.Identity identity;

    public PacketEncoder(Netty5ClientChannel.Identity identity) {
        this.identity = identity;
    }

    @Override
    protected Buffer allocateBuffer(ChannelHandlerContext ctx, Packet msg) {
        return ctx.bufferAllocator().allocate(this.allocateBytes(msg));
    }

    public int allocateBytes(Packet msg) {
        return Integer.BYTES +
                // class name
                msg.getClass().getName().getBytes(StandardCharsets.UTF_8).length +
                // amount of bytes in buffer
                Integer.BYTES +
                // buffer content
                msg.buffer().origin().readableBytes();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, Buffer out) {
        try {
            var origin = msg.buffer().origin();
            var buffer = new CodecBuffer(out);
            var readableBytes = origin.readableBytes();

            buffer.writeString(msg.getClass().getName());
            buffer.writeInt(readableBytes);

            origin.copyInto(0, out, out.writerOffset(), readableBytes);
            out.skipWritableBytes(readableBytes);
        } catch (Exception e) {
            System.err.println((identity != null ? "[identity: " + identity.name() + "]" : "") + "Error while decoding packet" + msg.getClass().getName());
            e.printStackTrace();
        }
    }
}
