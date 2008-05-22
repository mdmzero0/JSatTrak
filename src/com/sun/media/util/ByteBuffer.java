// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ByteBuffer.java

package com.sun.media.util;


public class ByteBuffer
{

    public ByteBuffer(int size)
    {
        this.size = size;
        buffer = new byte[size];
    }

    public final void clear()
    {
        offset = 0;
        length = 0;
    }

    public final void writeBytes(String s)
    {
        byte bytes[] = s.getBytes();
        writeBytes(bytes);
    }

    public final void writeBytes(byte bytes[])
    {
        System.arraycopy(bytes, 0, buffer, offset, bytes.length);
        offset += bytes.length;
        length += bytes.length;
    }

    public final void writeInt(int value)
    {
        buffer[offset + 0] = (byte)(value >> 24 & 0xff);
        buffer[offset + 1] = (byte)(value >> 16 & 0xff);
        buffer[offset + 2] = (byte)(value >> 8 & 0xff);
        buffer[offset + 3] = (byte)(value >> 0 & 0xff);
        offset += 4;
        length += 4;
    }

    public final void writeIntLittleEndian(int value)
    {
        buffer[offset + 3] = (byte)(value >>> 24 & 0xff);
        buffer[offset + 2] = (byte)(value >>> 16 & 0xff);
        buffer[offset + 1] = (byte)(value >>> 8 & 0xff);
        buffer[offset + 0] = (byte)(value >>> 0 & 0xff);
        offset += 4;
        length += 4;
    }

    public final void writeShort(short value)
    {
        buffer[offset + 0] = (byte)(value >> 8 & 0xff);
        buffer[offset + 1] = (byte)(value >> 0 & 0xff);
        offset += 2;
        length += 2;
    }

    public final void writeShortLittleEndian(short value)
    {
        buffer[offset + 1] = (byte)(value >> 8 & 0xff);
        buffer[offset + 0] = (byte)(value >> 0 & 0xff);
        offset += 2;
        length += 2;
    }

    public final void writeByte(byte value)
    {
        buffer[offset] = value;
        offset++;
        length++;
    }

    public byte buffer[];
    public int offset;
    public int length;
    public int size;
}
