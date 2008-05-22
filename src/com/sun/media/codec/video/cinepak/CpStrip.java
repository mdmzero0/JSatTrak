// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CpStrip.java

package com.sun.media.codec.video.cinepak;


// Referenced classes of package com.sun.media.codec.video.cinepak:
//            CodeEntry

public class CpStrip
{

    public CpStrip()
    {
        fSizeOfStrip = 0;
        fx0 = 0;
        fy0 = 0;
        fx1 = 0;
        fy1 = 0;
        fCID = 0;
        Detail = new CodeEntry[256];
        Smooth = new CodeEntry[256];
        for(int i = 0; i < 256; i++)
        {
            Detail[i] = new CodeEntry();
            Smooth[i] = new CodeEntry();
        }

    }

    private int fSizeOfStrip;
    private int fx0;
    private int fy0;
    private int fx1;
    private int fy1;
    private int fCID;
    public CodeEntry Detail[];
    public CodeEntry Smooth[];
}
