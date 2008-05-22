// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Parser.java

package com.sun.media.rtsp.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

public class Parser
{

    public Parser()
    {
        init();
    }

    public int readChar(ByteArrayInputStream bin)
    {
        int ch;
        if(buffer.size() > 0)
        {
            ch = ((Integer)buffer.elementAt(0)).intValue();
            buffer.removeElementAt(0);
        } else
        {
            ch = bin.read();
        }
        return ch;
    }

    public String getToken(ByteArrayInputStream bin)
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        skipWhitespace(bin);
        if(bin.available() > 0)
        {
            int ch;
            for(ch = readChar(bin); ch != 32 && ch != 10 && ch != 13 && ch != -1; ch = readChar(bin))
                bout.write(ch);

            ungetChar(ch);
        }
        String token = new String(bout.toByteArray());
        return token;
    }

    public void ungetChar(int ch)
    {
        buffer.insertElementAt(new Integer(ch), 0);
    }

    public String getLine(ByteArrayInputStream bin)
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        int ch;
        for(ch = readChar(bin); ch != 10 && ch != 13 && ch != -1; ch = readChar(bin))
            bout.write(ch);

        ch = readChar(bin);
        if(ch != 10)
            ungetChar(ch);
        String line = new String(bout.toByteArray());
        return line;
    }

    public String getStringToken(ByteArrayInputStream bin)
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        skipWhitespace(bin);
        for(int ch = readChar(bin); ch != 10 && ch != 13 && ch != -1; ch = readChar(bin))
            bout.write(ch);

        String token = new String(bout.toByteArray());
        return token;
    }

    public byte[] getContent(ByteArrayInputStream bin)
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        skipWhitespace(bin);
        for(int ch = readChar(bin); ch != -1; ch = readChar(bin))
            bout.write(ch);

        return bout.toByteArray();
    }

    private void skipWhitespace(ByteArrayInputStream bin)
    {
        int ch;
        for(ch = readChar(bin); ch == 32 || ch == 10 || ch == 13; ch = readChar(bin));
        ungetChar(ch);
    }

    private void init()
    {
        buffer = new Vector();
    }

    private Vector buffer;
}
