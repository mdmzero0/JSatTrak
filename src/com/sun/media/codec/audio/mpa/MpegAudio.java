// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MpegAudio.java

package com.sun.media.codec.audio.mpa;


// Referenced classes of package com.sun.media.codec.audio.mpa:
//            MPAHeader

public class MpegAudio
{

    public MpegAudio()
    {
    }

    public static native boolean nGetHeader(byte abyte0[], int i, MPAHeader mpaheader);

    public static native int nOpen(int ai[]);

    public static native boolean nClose(int i);

    public static native boolean nConvert(int i, byte abyte0[], int j, int k, byte abyte1[], int l, int i1, int ai[], 
            int ai1[], int j1, int ai2[]);
}
