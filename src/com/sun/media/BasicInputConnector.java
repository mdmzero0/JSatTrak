// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicInputConnector.java

package com.sun.media;

import javax.media.Buffer;

// Referenced classes of package com.sun.media:
//            BasicConnector, InputConnector, CircularBuffer, OutputConnector

public class BasicInputConnector extends BasicConnector
    implements InputConnector
{

    public BasicInputConnector()
    {
        outputConnector = null;
        reset = false;
    }

    public OutputConnector getOutputConnector()
    {
        return outputConnector;
    }

    public void reset()
    {
        synchronized(super.circularBuffer)
        {
            reset = true;
            super.reset();
            super.circularBuffer.notifyAll();
        }
    }

    public boolean isValidBufferAvailable()
    {
        return super.circularBuffer.canRead();
    }

    public Buffer getValidBuffer()
    {
        switch(super.protocol)
        {
        case 0: // '\0'
            Buffer buffer2;
            synchronized(super.circularBuffer)
            {
                if(!isValidBufferAvailable() && reset)
                {
                    Buffer buffer1 = null;
                    return buffer1;
                }
                reset = false;
                buffer2 = super.circularBuffer.read();
            }
            return buffer2;

        case 1: // '\001'
            Buffer buffer4;
            synchronized(super.circularBuffer)
            {
                for(reset = false; !reset && !isValidBufferAvailable();)
                    try
                    {
                        super.circularBuffer.wait();
                    }
                    catch(Exception e) { }

                if(reset)
                {
                    Buffer buffer3 = null;
                    return buffer3;
                }
                Buffer buffer = super.circularBuffer.read();
                super.circularBuffer.notifyAll();
                buffer4 = buffer;
            }
            return buffer4;
        }
        throw new RuntimeException();
    }

    public void setOutputConnector(OutputConnector outputConnector)
    {
        this.outputConnector = outputConnector;
    }

    public void readReport()
    {
        switch(super.protocol)
        {
        case 0: // '\0'
        case 1: // '\001'
            synchronized(super.circularBuffer)
            {
                if(reset)
                    return;
                super.circularBuffer.readReport();
                super.circularBuffer.notifyAll();
            }
            return;
        }
        throw new RuntimeException();
    }

    protected OutputConnector outputConnector;
    private boolean reset;
}
