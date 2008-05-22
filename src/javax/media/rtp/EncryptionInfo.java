// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   EncryptionInfo.java

package javax.media.rtp;

import java.io.Serializable;

public class EncryptionInfo
    implements Serializable
{

    public EncryptionInfo(int type, byte key[])
    {
        this.type = type;
        this.key = key;
    }

    public int getType()
    {
        return type;
    }

    public byte[] getKey()
    {
        return key;
    }

    private byte key[];
    private int type;
    public static final int NO_ENCRYPTION = 0;
    public static final int XOR = 1;
    public static final int MD5 = 2;
    public static final int DES = 3;
    public static final int TRIPLE_DES = 4;
}
