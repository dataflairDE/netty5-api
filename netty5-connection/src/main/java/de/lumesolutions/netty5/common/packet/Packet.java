package de.lumesolutions.netty5.common.packet;

import de.lumesolutions.netty5.common.codec.CodecBuffer;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Setter
public abstract class Packet {

    protected UUID queryId;
    protected CodecBuffer buffer;

    public Packet() {
        this.queryId = null;
        this.buffer = CodecBuffer.allocate();
    }

    public Packet(@NotNull CodecBuffer buffer) {
        this.queryId = null;
        this.buffer = buffer;
    }

    public Packet(@NotNull UUID queryId) {
        this.queryId = queryId;
        this.buffer = CodecBuffer.allocate();
    }

    public Packet(@NotNull UUID queryId, @NotNull CodecBuffer buffer) {
        this.queryId = queryId;
        this.buffer = buffer;
    }
}
