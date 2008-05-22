// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaSoundSourceStream.java

package com.sun.media.protocol.javasound;

import com.sun.media.CircularBuffer;
import com.sun.media.util.LoopThread;
import javax.media.Buffer;
import javax.media.SystemTimeBase;
import javax.media.protocol.BufferTransferHandler;
import javax.sound.sampled.TargetDataLine;

// Referenced classes of package com.sun.media.protocol.javasound:
//            JavaSoundSourceStream

class PushThread extends LoopThread
{

    public PushThread()
    {
        systemTimeBase = new SystemTimeBase();
        seqNo = 0L;
        setName("JavaSound PushThread");
    }

    void setSourceStream(JavaSoundSourceStream ss)
    {
        sourceStream = ss;
    }

    protected boolean process()
    {
        CircularBuffer cb = sourceStream.cb;
        BufferTransferHandler transferHandler = sourceStream.transferHandler;
        Buffer buffer;
        synchronized(cb)
        {
            while(!cb.canWrite()) 
                try
                {
                    cb.wait();
                }
                catch(Exception e) { }
            buffer = cb.getEmptyBuffer();
        }
        byte data[];
        if(buffer.getData() instanceof byte[])
            data = (byte[])buffer.getData();
        else
            data = null;
        if(data == null || data.length < sourceStream.bufSize)
        {
            data = new byte[sourceStream.bufSize];
            buffer.setData(data);
        }
        int len = sourceStream.dataLine.read(data, 0, sourceStream.bufSize);
        buffer.setOffset(0);
        buffer.setLength(len);
        buffer.setFormat(sourceStream.format);
        buffer.setFlags(0x80 | 0x8000);
        buffer.setSequenceNumber(seqNo++);
        buffer.setTimeStamp(systemTimeBase.getNanoseconds());
        synchronized(cb)
        {
            cb.writeReport();
            cb.notify();
            if(transferHandler != null)
                transferHandler.transferData(sourceStream);
        }
        return true;
    }

    private JavaSoundSourceStream sourceStream;
    private SystemTimeBase systemTimeBase;
    private long seqNo;
}
