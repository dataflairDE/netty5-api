package de.lumesolutions.netty5.common.packet;

import de.lumesolutions.netty5.common.codec.CodecBuffer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Setter
public abstract class Packet {

    protected CodecBuffer buffer;

    public Packet() {
        this.buffer = CodecBuffer.allocate();
    }

    public Packet(@NotNull CodecBuffer buffer) {
        this.buffer = buffer;
    }
}
