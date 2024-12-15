package de.dataflair.netty5.common.codec;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;

public abstract class BufferSerializer<T> {

    private final Class<T> clazz;

    protected BufferSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    public abstract void writeInBuffer(@NotNull final T object, @NotNull final CodecBuffer codecBuffer);

    public abstract T readFromBuffer(@NotNull final CodecBuffer codecBuffer);

    public boolean validate(@NotNull Object object) {
        return this.clazz.getName().equalsIgnoreCase(object.getClass().getName());
    }

}
