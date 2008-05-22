// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CircularBuffer.java

package com.sun.media;

import java.io.PrintStream;
import javax.media.Buffer;

// Referenced classes of package com.sun.media:
//            ExtBuffer

public class CircularBuffer
{

    public CircularBuffer(int n)
    {
        size = n;
        buf = new Buffer[n];
        for(int i = 0; i < n; i++)
            buf[i] = new ExtBuffer();

        reset();
    }

    public synchronized void readReport()
    {
        if(lockedFramesForReading == 0)
            error();
        lockedFramesForReading--;
        availableFramesForWriting++;
    }

    public synchronized boolean canRead()
    {
        return availableFramesForReading > 0;
    }

    public synchronized boolean lockedRead()
    {
        return lockedFramesForReading > 0;
    }

    public synchronized boolean lockedWrite()
    {
        return lockedFramesForWriting > 0;
    }

    public synchronized Buffer read()
    {
        if(availableFramesForReading == 0)
            error();
        Buffer buffer = buf[head];
        lockedFramesForReading++;
        availableFramesForReading--;
        head++;
        if(head >= size)
            head -= size;
        return buffer;
    }

    public synchronized Buffer peek()
    {
        if(availableFramesForReading == 0)
            error();
        return buf[head];
    }

    public synchronized void writeReport()
    {
        if(lockedFramesForWriting == 0)
            error();
        lockedFramesForWriting--;
        availableFramesForReading++;
    }

    public synchronized Buffer getEmptyBuffer()
    {
        if(availableFramesForWriting == 0)
            error();
        lockedFramesForWriting++;
        Buffer buffer = buf[tail];
        availableFramesForWriting--;
        tail++;
        if(tail >= size)
            tail -= size;
        return buffer;
    }

    public synchronized boolean canWrite()
    {
        return availableFramesForWriting > 0;
    }

    public void error()
    {
        throw new RuntimeException("CircularQueue failure:\n head=" + head + "\n tail=" + tail + "\n canRead=" + availableFramesForReading + "\n canWrite=" + availableFramesForWriting + "\n lockedRead=" + lockedFramesForReading + "\n lockedWrite=" + lockedFramesForWriting);
    }

    public void print()
    {
        System.err.println("CircularQueue : head=" + head + " tail=" + tail + " canRead=" + availableFramesForReading + " canWrite=" + availableFramesForWriting + " lockedRead=" + lockedFramesForReading + " lockedWrite=" + lockedFramesForWriting);
    }

    public synchronized void reset()
    {
        availableFramesForReading = 0;
        availableFramesForWriting = size;
        lockedFramesForReading = 0;
        lockedFramesForWriting = 0;
        head = 0;
        tail = 0;
    }

    private Buffer buf[];
    private int head;
    private int tail;
    private int availableFramesForReading;
    private int availableFramesForWriting;
    private int lockedFramesForReading;
    private int lockedFramesForWriting;
    private int size;
}
