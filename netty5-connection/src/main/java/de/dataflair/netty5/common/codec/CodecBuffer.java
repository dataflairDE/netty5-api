package de.dataflair.netty5.common.codec;

import com.google.gson.JsonObject;
import de.dataflair.netty5.Netty5ChannelUtils;
import io.netty5.buffer.Buffer;
import io.netty5.buffer.BufferAllocator;
import io.netty5.buffer.DefaultBufferAllocators;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Represents a protocol buffer that can be used to read and write various data types.
 */
public record CodecBuffer(@NotNull Buffer origin) {
    /**
     * The BUFFER_ALLOCATOR is a private static final variable that holds the instance
     * of the BufferAllocator interface. It is initialized with the DefaultBufferAllocators.offHeapAllocator()
     * implementation.
     * <p>
     * The BufferAllocator interface is responsible for providing memory management functionalities
     * for buffer allocation. It abstracts the underlying memory allocation mechanism and provides
     * a unified API to allocate buffers, such as byte arrays, off-heap memory blocks, etc.
     * <p>
     * This constant variable ensures that a single instance of BufferAllocator is used throughout
     * the class. It is recommended to use a constant instance of the allocator to prevent
     * unnecessary memory allocation overhead.
     * <p>
     * The BUFFER_ALLOCATOR variable is declared private to restrict its visibility within the class.
     * Being declared as static, it belongs to the class itself rather than an instance of the class.
     * Finally, the final modifier ensures that the value of the variable cannot be modified once
     * it is assigned.
     * <p>
     * Example usage:
     *   // Allocate a buffer of size 1024 bytes
     *   ByteBuffer buffer = BUFFER_ALLOCATOR.allocate(1024);
     * <p>
     *   // Deallocate the buffer when it is no longer needed
     *   BUFFER_ALLOCATOR.deallocate(buffer);
     * <p>
     *   // Retrieve the maximum buffer size that can be allocated
     *   int maxBufferSize = BUFFER_ALLOCATOR.getMaxBufferSize();
     */
    private static final BufferAllocator BUFFER_ALLOCATOR = DefaultBufferAllocators.offHeapAllocator();

    /**
     * Allocates a new ProtocolBuffer object.
     *
     * @return a newly allocated ProtocolBuffer object
     */
    public static CodecBuffer allocate() {
        return new CodecBuffer(BUFFER_ALLOCATOR.allocate(0));
    }

    /**
     * Resets the buffer by skipping any remaining bytes in the origin buffer.
     *
     * If the origin buffer has readable bytes, it means that there are remaining bytes that have not been processed.
     * In such cases, this method will skip the remaining bytes and print an error message indicating the number of
     * bytes skipped.
     */
    public void resetBuffer() {
        if (origin.readableBytes() > 0) {
            System.err.println("Buffer not empty! Skipping remaining bytes: " + origin.readableBytes());
            origin.skipReadableBytes(origin.readableBytes());
        }
    }

    /**
     * Writes the given string value to the ProtocolBuffer.
     *
     * @param value the string value to be written
     * @return the ProtocolBuffer instance
     */
    public CodecBuffer writeString(@NotNull String value) {
        var bytes = value.getBytes(StandardCharsets.UTF_8);
        this.origin.writeInt(bytes.length);
        this.origin.writeBytes(bytes);
        return this;
    }

    /**
     * Reads a String from the ProtocolBuffer.
     *
     * @return The String read from the ProtocolBuffer.
     */
    public String readString() {
        return this.origin.readCharSequence(this.origin.readInt(), StandardCharsets.UTF_8).toString();
    }

    /**
     * Writes a boolean value to the ProtocolBuffer.
     *
     * @param booleanValue the boolean value to write
     * @return the ProtocolBuffer instance
     */
    public CodecBuffer writeBoolean(Boolean booleanValue) {
        this.origin.writeBoolean(booleanValue);
        return this;
    }

    /**
     * Reads a boolean value from the protocol buffer.
     *
     * @return the boolean value read from the protocol buffer
     */
    public boolean readBoolean() {
        return this.origin.readBoolean();
    }

    /**
     * Writes a unique identifier to the protocol buffer.
     *
     * @param uniqueId the unique identifier to write
     * @return the ProtocolBuffer instance
     */
    public CodecBuffer writeUniqueId(@NotNull UUID uniqueId) {
        this.origin.writeLong(uniqueId.getMostSignificantBits());
        this.origin.writeLong(uniqueId.getLeastSignificantBits());
        return this;
    }

    /**
     * Reads a unique identifier (UUID) from the protocol buffer.
     *
     * @return The read UUID.
     */
    public UUID readUniqueId() {
        return new UUID(this.origin.readLong(), this.origin.readLong());
    }

    /**
     * Writes an integer value to the protocol buffer.
     *
     * @param value the integer value to write
     * @return the modified ProtocolBuffer object
     */
    public CodecBuffer writeInt(int value) {
        this.origin.writeInt(value);
        return this;
    }

    /**
     * Reads an integer value from the input source.
     *
     * @return the integer value read from the input source
     */
    public int readInt() {
        return this.origin.readInt();
    }

    /**
     * Writes the ordinal value of the given Enum to the ProtocolBuffer.
     *
     * @param value the Enum value to be written
     * @return the ProtocolBuffer object for method chaining
     */
    public CodecBuffer writeEnum(@NotNull Enum<?> value) {
        this.origin.writeInt(value.ordinal());
        return this;
    }

    /**
     * Reads an Enum value from the specified Class using a provided integer value.
     *
     * @param clazz The Class representing the Enum from which to read the value.
     * @return The Enum value read from the specified Class.
     * @throws NullPointerException if clazz is null.
     */
    public <T extends Enum<?>> T readEnum(@NotNull Class<T> clazz) {
        return clazz.getEnumConstants()[this.origin.readInt()];
    }

    /**
     * Writes a nullable object to the ProtocolBuffer using the provided consumer.
     *
     * @param object    the nullable object to write
     * @param consumer  the consumer to write the object with
     * @return the same ProtocolBuffer instance
     */
    public CodecBuffer writeNullable(@Nullable Object object, @NotNull Consumer<CodecBuffer> consumer) {
        this.writeBoolean(object != null);
        if (object != null) {
            consumer.accept(this);
        }
        return this;
    }

    /**
     * Reads a nullable object from the protocol buffer. If the boolean value read from the buffer is true,
     * the supplier is invoked to get the actual value, otherwise null is returned.
     *
     * @param objectClass the class of the object being read
     * @param supplier    the supplier function that provides the actual value of the object
     * @return the nullable object read from the buffer, or null if the boolean value is false
     */
    public <T> @Nullable T readNullable(@NotNull Class<T> objectClass, @NotNull Supplier<T> supplier) {
        return this.readBoolean() ? supplier.get() : null;
    }

    public CodecBuffer writeOptional(@NotNull Optional<Object> objectOptional, @NotNull Consumer<CodecBuffer> consumer) {
        this.writeBoolean(objectOptional.isPresent());
        objectOptional.ifPresent(object -> this.writeNullable(object, consumer));
        return this;
    }

    public <T> @NotNull Optional<T> readOptional(@NotNull Class<T> objectClass, @NotNull Supplier<T> supplier) {
        return this.readBoolean() ? Optional.ofNullable(this.readNullable(objectClass, supplier)) : Optional.empty();
    }

    /**
     * Writes a nullable object to the ProtocolBuffer using a consumer.
     * This method is deprecated and should not be used.
     *
     * @param object   the nullable object to be written to the ProtocolBuffer
     * @param consumer the consumer that writes the object to the ProtocolBuffer
     * @return the ProtocolBuffer
     */
    @Deprecated
    public CodecBuffer writeObject(@Nullable Object object, @NotNull Consumer<CodecBuffer> consumer) {
        return this.writeNullable(object, consumer);
    }

    /**
     * Reads an object of the specified type using the provided supplier.
     * This method is deprecated and should not be used in new code.
     *
     * @param <T>       the type of the object to read
     * @param tClass    the class object representing the type T
     * @param supplier  the supplier that creates a new instance of type T if the object is null
     * @return the object of type T, or null if it is not found or cannot be read
     */
    @Deprecated
    public <T> @Nullable T readObject(@NotNull Class<T> tClass, @NotNull Supplier<T> supplier) {
        return this.readNullable(tClass, supplier);
    }

    /**
     * Writes a ProtocolBuffer to the current buffer.
     *
     * @param buffer the ProtocolBuffer to be written
     * @return the current ProtocolBuffer instance
     */
    public CodecBuffer writeBuffer(@NotNull CodecBuffer buffer) {
        this.writeInt(buffer.origin().readableBytes());
        this.writeBytes(buffer.origin());
        return this;
    }

    /**
     * Reads a CodecBuffer from the current buffer.
     *
     * @return a newly created CodecBuffer
     */
    public CodecBuffer readBuffer() {
        var length = this.readInt(); // Length of the following buffer
        var bufferSubsequence = this.origin.readBytes(ByteBuffer.allocateDirect(length)); // Subsequence of the origin buffer
        return new CodecBuffer(bufferSubsequence);
    }

    /**
     * Writes a long value to the ProtocolBuffer.
     *
     * @param value the long value to write
     * @return the ProtocolBuffer instance
     */
    public CodecBuffer writeLong(long value) {
        this.origin.writeLong(value);
        return this;
    }

    /**
     * Reads a long value from the origin input source.
     *
     * @return the long value read from the input source.
     */
    public long readLong() {
        return this.origin.readLong();
    }

    /**
     * Writes a float value to the ProtocolBuffer.
     *
     * @param value the float value to be written
     * @return the updated ProtocolBuffer instance
     */
    public CodecBuffer writeFloat(float value) {
        this.origin.writeFloat(value);
        return this;
    }

    /**
     * Reads a float value from the origin object.
     *
     * @return The float value read from the origin object.
     */
    public float readFloat() {
        return this.origin.readFloat();
    }

    /**
     * Writes a double value to the ProtocolBuffer.
     *
     * @param value the double value to be written
     * @return the updated ProtocolBuffer instance
     */
    public CodecBuffer writeDouble(double value) {
        this.origin.writeDouble(value);
        return this;
    }

    /**
     * Reads a double value from the specified source.
     *
     * @return the double value read from the source.
     */
    public double readDouble() {
        return this.origin.readDouble();
    }

    /**
     * Reads a short value from the underlying input stream.
     *
     * @return the short value read from the input stream.
     */
    public short readShort() {
        return this.origin.readShort();
    }

    /**
     * Writes a short value to the protocol buffer.
     *
     * @param value the short value to be written
     * @return the ProtocolBuffer instance
     */
    public CodecBuffer writeShort(short value) {
        this.origin.writeShort(value);
        return this;
    }

    /**
     * Writes a single byte value to the protocol buffer.
     *
     * @param value the byte value to be written
     * @return the updated ProtocolBuffer object
     */
    public CodecBuffer writeByte(byte value) {
        this.origin.writeByte(value);
        return this;
    }

    /**
     * Reads a single byte from the input stream.
     *
     * @return the byte read from the input stream
     */
    public byte readByte() {
        return this.origin.readByte();
    }

    /**
     * Writes the given {@link Buffer} bytes to the protocol buffer.
     *
     * @param bytes the bytes to be written to the protocol buffer
     * @return the protocol buffer instance after writing the bytes
     */
    public CodecBuffer writeBytes(Buffer bytes) {
        this.origin.writeBytes(bytes);
        return this;
    }

    /**
     * Writes the given byte array to the ProtocolBuffer.
     *
     * @param bytes the byte array to be written
     * @return the updated ProtocolBuffer
     */
    public CodecBuffer writeBytes(byte[] bytes) {
        this.origin.writeBytes(bytes);
        return this;
    }

    /**
     * Writes the elements of a List to a ProtocolBuffer using a BiConsumer.
     *
     * @param list     the List containing the elements to be written
     * @param consumer a BiConsumer that accepts a ProtocolBuffer and an element from the List,
     *                 and performs the writing operation
     * @param <T>      the type of elements in the List
     * @return the updated ProtocolBuffer instance
     */
    public <T> CodecBuffer writeList(@NotNull List<T> list, @NotNull BiConsumer<CodecBuffer, T> consumer) {
        this.writeInt(list.size());
        list.forEach(o -> consumer.accept(this, o));
        return this;
    }

    /**
     * Reads a list of elements from input stream and adds them to the provided list.
     *
     * @param list     the list to populate with read elements
     * @param supplier a supplier function used to create new elements to be added to the list
     * @param <T>      the type of elements in the list
     * @return the same list with the newly read elements added to it
     */
    public <T> List<T> readList(@NotNull List<T> list, @NotNull Supplier<T> supplier) {
        var size = this.readInt();
        for (var i = 0; i < size; i++) {
            list.add(supplier.get());
        }
        return list;
    }

    /**
     * Writes a list of strings to the CodecBuffer.
     *
     * @param list the list of strings to be written
     * @return the CodecBuffer object after writing the list of strings
     */
    public CodecBuffer writeStringList(@NotNull List<String> list) {
        this.writeList(list, CodecBuffer::writeString);
        return this;
    }

    /**
     * Reads a list of strings from the given input list using `readString` method.
     *
     * @param list The input list to read from. Cannot be null.
     * @return A new list of strings read from the input list.
     */
    public List<String> readStringList(@NotNull List<String> list) {
        return this.readList(list, this::readString);
    }

    /**
     * Writes a list of integers to the CodecBuffer.
     *
     * @param list the list of integers to be written
     * @return the CodecBuffer object on which the method is called
     */
    public CodecBuffer writeIntegerList(@NotNull List<Integer> list) {
        this.writeList(list, CodecBuffer::writeInt);
        return this;
    }

    /**
     * Reads a list of integers.
     *
     * @param list the list of integers to be read
     * @return a list of integers
     */
    public List<Integer> readIntegerList(@NotNull List<Integer> list) {
        return this.readList(list, this::readInt);
    }

    /**
     * Writes a list of booleans to the CodecBuffer.
     *
     * @param list the list of booleans to write
     * @return the CodecBuffer instance
     */
    public CodecBuffer writeBooleanList(@NotNull List<Boolean> list) {
        this.writeList(list, CodecBuffer::writeBoolean);
        return this;
    }

    /**
     * Reads a list of booleans from the given list.
     *
     * @param list The list of booleans to read from. Must not be null.
     * @return A new list of booleans read from the given list.
     */
    public List<Boolean> readBooleanList(@NotNull List<Boolean> list) {
        return this.readList(list, this::readBoolean);
    }

    /**
     * Writes a list of bytes to the CodecBuffer.
     *
     * @param list the list of bytes to write
     * @return the CodecBuffer object
     */
    public CodecBuffer writeByteList(@NotNull List<Byte> list) {
        this.writeList(list, CodecBuffer::writeByte);
        return this;
    }

    /**
     * Reads a list of bytes using the provided list and the readByte function.
     *
     * @param list The list to read bytes from.
     * @return A list of bytes read from the provided list.
     */
    public List<Byte> readByteList(@NotNull List<Byte> list) {
        return this.readList(list, this::readByte);
    }

    /**
     * Writes a {@link CharSequence} to the codec buffer.
     *
     * @param charSequence the {@link CharSequence} to write to the buffer
     * @return the modified codec buffer
     */
    public CodecBuffer writeCharSequence(@NotNull CharSequence charSequence) {
        this.writeString(charSequence.toString());
        return this;
    }

    /**
     * Writes a char value to the CodecBuffer.
     *
     * @param value the char value to be written
     * @return the reference to the CodecBuffer object after writing the char value
     */
    public CodecBuffer writeChar(char value) {
        this.origin.writeChar(value);
        return this;
    }

    /**
     * Read a character from the input source.
     *
     * @return The character read from the input source.
     */
    public char readChar() {
        return this.origin.readChar();
    }

    /**
     * Writes the given map to the ProtocolBuffer, using the provided key and value consumers.
     *
     * @param map The map to be written to the ProtocolBuffer. Cannot be null.
     * @param keyConsumer A BiConsumer that defines the action to be performed on the key. Cannot be null.
     *                    The first argument of the BiConsumer should be the ProtocolBuffer, and the second
     *                    argument should be the key from the map.
     * @param valueConsumer A BiConsumer that defines the action to be performed on the value. Cannot be null.
     *                      The first argument of the BiConsumer should be the ProtocolBuffer, and the second
     *                      argument should be the value from the map.
     * @return Returns the reference to the current instance of the ProtocolBuffer.
     */
    public <K, V> CodecBuffer writeMap(
            @NotNull Map<K, V> map,
            @NotNull BiConsumer<CodecBuffer, K> keyConsumer,
            @NotNull BiConsumer<CodecBuffer, V> valueConsumer
    ) {
        this.writeInt(map.size());
        map.forEach((k, v) -> {
            keyConsumer.accept(this, k);
            valueConsumer.accept(this, v);
        });
        return this;
    }

    /**
     * Reads map elements from an input source and populates the given map with the read key-value pairs.
     *
     * @param map           the map to populate with the read key-value pairs (not null)
     * @param keySupplier   the supplier to generate new keys for the map elements (not null)
     * @param valueSupplier the supplier to generate new values for the map elements (not null)
     * @param <K>           the type of keys in the map
     * @param <V>           the type of values in the map
     * @return the populated map
     */
    public <K, V> Map<K, V> readMap(
            @NotNull Map<K, V> map,
            @NotNull Supplier<K> keySupplier,
            @NotNull Supplier<V> valueSupplier
    ) {
        var size = this.readInt();
        for (int i = 0; i < size; i++) {
            map.put(keySupplier.get(), valueSupplier.get());
        }
        return map;
    }

    public <T extends WriteReadStream> CodecBuffer writeStream(@NotNull T stream) {
        stream.writeBuffer(this);
        return this;
    }

    public <T extends WriteReadStream> T readStream(@NotNull T stream) {
        stream.readBuffer(this);
        return stream;
    }

    public CodecBuffer writeJsonObject(@NotNull JsonObject jsonObject) {
        this.writeString(jsonObject.toString());
        return this;
    }

    public JsonObject readJsonObject() {
        return Netty5ChannelUtils.JSON.fromJson(this.readString(), JsonObject.class);
    }

    public interface WriteReadStream {
        void writeBuffer(@NotNull CodecBuffer codecBuffer);

        void readBuffer(@NotNull CodecBuffer codecBuffer);
    }
}