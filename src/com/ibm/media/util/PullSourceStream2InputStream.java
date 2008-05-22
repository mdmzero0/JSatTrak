// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PullSourceStream2InputStream.java

package com.ibm.media.util;

import java.io.*;
import javax.media.protocol.PullSourceStream;
import javax.media.protocol.SourceStream;

public class PullSourceStream2InputStream extends InputStream
{

    public PullSourceStream2InputStream(PullSourceStream pss)
    {
        buffer = new byte[1];
        this.pss = pss;
    }

    public int read()
        throws IOException
    {
        if(pss.endOfStream())
        {
            System.out.println("end of stream");
            return -1;
        } else
        {
            pss.read(buffer, 0, 1);
            return buffer[0];
        }
    }

    public int read(byte b[])
        throws IOException
    {
        return pss.read(b, 0, b.length);
    }

    public int read(byte b[], int off, int len)
        throws IOException
    {
        return pss.read(b, off, len);
    }

    public long skip(long n)
        throws IOException
    {
        byte buffer[] = new byte[(int)n];
        int read = read(buffer);
        return (long)read;
    }

    public int available()
        throws IOException
    {
        System.out.println("available was called");
        return 0;
    }

    public void close()
        throws IOException
    {
    }

    public boolean markSupported()
    {
        return false;
    }

    PullSourceStream pss;
    byte buffer[];
}
