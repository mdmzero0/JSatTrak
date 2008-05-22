// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Parser.java

package com.sun.media.sdp;

import com.sun.media.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Vector;

public class Parser
{

    public Parser()
    {
    }

    public void init()
    {
        buffer = new Vector();
    }

    public void ungetToken(String tokenStr)
    {
        byte token[] = tokenStr.getBytes();
        for(int i = 0; i < token.length; i++)
            buffer.insertElementAt(new Integer(token[token.length - i - 1]), 0);

    }

    public boolean getToken(ByteArrayInputStream bin, String tokenString)
    {
        boolean found = false;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        skipWhitespace(bin);
        if(bin.available() > 0)
        {
            int ch;
            for(ch = readChar(bin); ch != 61 && ch != 10 && ch != 13 && ch != -1; ch = readChar(bin))
                bout.write(ch);

            bout.write(ch);
        }
        String token = new String(bout.toByteArray());
        if(tokenString.equals(token))
            found = true;
        else
            ungetToken(token);
        return found;
    }

    public boolean getToken(ByteArrayInputStream bin, String tokenString, boolean mandatory)
    {
        boolean found = getToken(bin, tokenString);
        if(!found && mandatory)
            Log.warning("[SDP Parser] Token missing: " + tokenString);
        return found;
    }

    public String getLine(ByteArrayInputStream bin)
    {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        if(bin.available() > 0)
        {
            for(int ch = readChar(bin); ch != 10 && ch != 13 && ch != -1; ch = readChar(bin))
                bout.write(ch);

        }
        String line = new String(bout.toByteArray());
        return line;
    }

    private void skipWhitespace(ByteArrayInputStream bin)
    {
        int ch;
        for(ch = readChar(bin); ch == 32 || ch == 10 || ch == 13; ch = readChar(bin));
        buffer.insertElementAt(new Integer(ch), 0);
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

    private static Vector buffer;
}
