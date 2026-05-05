package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

// Этот вспомогательный класс нужен, чтобы вся низкоуровневая запись байтов жила в одном месте.
// Тогда код, который собирает manifest.bin и data.bin, остаётся читаемым и не превращается в набор побитовых трюков.
public class BinaryProtocolWriter {

    private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    private final DataOutputStream output = new DataOutputStream(byteStream);

    // Все многобайтовые числа записываются в BigEndian.
    // Так проще, потому что DataOutputStream уже пишет short/int/long именно в этом порядке.
    public void writeUInt8(int value) {
        try {
            output.writeByte(value & 0xFF);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write uint8", e);
        }
    }

    public void writeUInt16(int value) {
        try {
            output.writeShort(value & 0xFFFF);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write uint16", e);
        }
    }

    public void writeUInt32(long value) {
        try {
            output.writeInt((int) (value & 0xFFFFFFFFL));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write uint32", e);
        }
    }

    public void writeInt64(long value) {
        try {
            output.writeLong(value);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write int64", e);
        }
    }

    public void writeUuid(UUID value) {
        writeInt64(value.getMostSignificantBits());
        writeInt64(value.getLeastSignificantBits());
    }

    public void writeRawBytes(byte[] value) {
        try {
            output.write(value);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write raw bytes", e);
        }
    }

    public void writeByteArray(byte[] value) {
        writeUInt32(value.length);
        writeRawBytes(value);
    }

    public void writeUtf8(String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeUInt32(bytes.length);
        writeRawBytes(bytes);
    }

    public byte[] toByteArray() {
        try {
            output.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot flush binary writer", e);
        }
        return byteStream.toByteArray();
    }
}
