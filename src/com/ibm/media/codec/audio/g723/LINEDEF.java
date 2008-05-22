// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   G723Dec.java

package com.ibm.media.codec.audio.g723;


// Referenced classes of package com.ibm.media.codec.audio.g723:
//            SFSDEF

class LINEDEF
{

    LINEDEF()
    {
        Olp = new int[2];
    }

    private static final int SubFrames = 4;
    int Crc;
    int LspId;
    int Olp[];
    SFSDEF Sfs[] = {
        new SFSDEF(), new SFSDEF(), new SFSDEF(), new SFSDEF()
    };
}
