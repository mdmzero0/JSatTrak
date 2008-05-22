// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicOutputConnector.java

package com.sun.media;

import javax.media.Buffer;
import javax.media.Format;

// Referenced classes of package com.sun.media:
//            BasicConnector, CircularBuffer, OutputConnector, InputConnector, 
//            Connector, Module

public class BasicOutputConnector extends BasicConnector
    implements OutputConnector
{

    public BasicOutputConnector()
    {
        inputConnector = null;
        reset = false;
    }

    public Format connectTo(InputConnector inputConnector, Format useThisFormat)
    {
        Format format = canConnectTo(inputConnector, useThisFormat);
        this.inputConnector = inputConnector;
        inputConnector.setOutputConnector(this);
        int bufferSize = Math.max(getSize(), inputConnector.getSize());
        super.circularBuffer = new CircularBuffer(bufferSize);
        inputConnector.setCircularBuffer(super.circularBuffer);
        return null;
    }

    public Format canConnectTo(InputConnector inputConnector, Format useThisFormat)
    {
        if(getProtocol() != inputConnector.getProtocol())
            throw new RuntimeException("protocols do not match:: ");
        else
            return null;
    }

    public InputConnector getInputConnector()
    {
        return inputConnector;
    }

    public void reset()
    {
        synchronized(super.circularBuffer)
        {
            reset = true;
            super.reset();
            if(inputConnector != null)
                inputConnector.reset();
            super.circularBuffer.notifyAll();
        }
    }

    public boolean isEmptyBufferAvailable()
    {
        return super.circularBuffer.canWrite();
    }

    public Buffer getEmptyBuffer()
    {
        switch(super.protocol)
        {
        case 0: // '\0'
            if(!isEmptyBufferAvailable() && reset)
            {
                return null;
            } else
            {
                reset = false;
                return super.circularBuffer.getEmptyBuffer();
            }

        case 1: // '\001'
            Buffer buffer2;
            synchronized(super.circularBuffer)
            {
                for(reset = false; !reset && !isEmptyBufferAvailable();)
                    try
                    {
                        super.circularBuffer.wait();
                    }
                    catch(Exception e) { }

                if(reset)
                {
                    Buffer buffer1 = null;
                    return buffer1;
                }
                Buffer buffer = super.circularBuffer.getEmptyBuffer();
                super.circularBuffer.notifyAll();
                buffer2 = buffer;
            }
            return buffer2;
        }
        throw new RuntimeException();
    }

    public void writeReport()
    {
        switch(super.protocol)
        {
        case 0: // '\0'
            synchronized(super.circularBuffer)
            {
                if(reset)
                    return;
                super.circularBuffer.writeReport();
            }
            getInputConnector().getModule().connectorPushed(getInputConnector());
            return;

        case 1: // '\001'
            synchronized(super.circularBuffer)
            {
                if(reset)
                    return;
                super.circularBuffer.writeReport();
                super.circularBuffer.notifyAll();
            }
            return;
        }
        throw new RuntimeException();
    }

    protected InputConnector inputConnector;
    private boolean reset;
}
