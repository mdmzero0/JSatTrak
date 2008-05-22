// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPHeader.java

package javax.media.rtp;

import java.io.Serializable;

public class RTPHeader
    implements Serializable
{

    public RTPHeader()
    {
        extensionPresent = false;
        extensionType = -1;
        extension = null;
    }

    public RTPHeader(int marker)
    {
        extensionPresent = false;
        extensionType = -1;
        extension = null;
    }

    public RTPHeader(boolean extensionPresent, int extensionType, byte extension[])
    {
        this.extensionPresent = extensionPresent;
        this.extensionType = extensionType;
        this.extension = extension;
    }

    public boolean isExtensionPresent()
    {
        return extensionPresent;
    }

    public int getExtensionType()
    {
        return extensionType;
    }

    public byte[] getExtension()
    {
        return extension;
    }

    public void setExtensionPresent(boolean p)
    {
        extensionPresent = p;
    }

    public void setExtensionType(int t)
    {
        extensionType = t;
    }

    public void setExtension(byte e[])
    {
        extension = e;
    }

    public static final int VALUE_NOT_SET = -1;
    private boolean extensionPresent;
    private int extensionType;
    private byte extension[];
}
