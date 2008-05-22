// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Many2one.java

package com.ibm.media.codec.audio;

import com.sun.media.BasicCodec;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;

public class Many2one extends BasicCodec
{

    public Many2one()
    {
        counter = 0;
        flagEOM = false;
        af = new AudioFormat("LINEAR", 8000D, 16, 1, 0, 1, -1, -1D, java.lang.Byte.class);
    }

    public String getName()
    {
        return "many frames to one converter";
    }

    public Format[] getSupportedInputFormats()
    {
        Format fmt[] = new Format[1];
        fmt[0] = af;
        return fmt;
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        Format fmt[] = new Format[1];
        fmt[0] = af;
        return fmt;
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        if(flagEOM)
        {
            outputBuffer.setLength(0);
            outputBuffer.setEOM(true);
            flagEOM = false;
            return 0;
        }
        if(outputBuffer.isDiscard())
        {
            outputBuffer.setLength(0);
            outputBuffer.setOffset(0);
        }
        if(inputBuffer.isEOM())
            if(outputBuffer.getLength() > 0)
            {
                flagEOM = true;
                return 2;
            } else
            {
                outputBuffer.setLength(0);
                outputBuffer.setEOM(true);
                return 0;
            }
        if(outputBuffer.getData() == null)
            outputBuffer.setData(new byte[10000]);
        System.arraycopy(inputBuffer.getData(), inputBuffer.getOffset(), outputBuffer.getData(), outputBuffer.getLength(), inputBuffer.getLength());
        outputBuffer.setLength(outputBuffer.getLength() + inputBuffer.getLength());
        if(++counter == 5)
        {
            counter = 0;
            outputBuffer.setFormat(af);
            return 0;
        } else
        {
            return 4;
        }
    }

    int counter;
    boolean flagEOM;
    Format af;
}
