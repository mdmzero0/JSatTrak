// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPStats.java

package com.sun.media.rtp;

import javax.media.rtp.ReceptionStats;

public class RTPStats
    implements ReceptionStats
{

    public RTPStats()
    {
        numLost = 0;
        numProc = 0;
        numMisord = 0;
        numInvalid = 0;
        numDup = 0;
        qSize = 0;
        PDUDrop = 0;
        ADUDrop = 0;
    }

    public synchronized void update(int which)
    {
        switch(which)
        {
        case 0: // '\0'
            numLost++;
            break;

        case 1: // '\001'
            numProc++;
            break;

        case 2: // '\002'
            numMisord++;
            break;

        case 3: // '\003'
            numInvalid++;
            break;

        case 4: // '\004'
            numDup++;
            break;
        }
    }

    public synchronized void update(int which, int amount)
    {
        switch(which)
        {
        case 0: // '\0'
            numLost = numLost + amount;
            break;

        case 5: // '\005'
            payload = amount;
            break;

        case 7: // '\007'
            qSize = amount;
            break;

        case 8: // '\b'
            PDUDrop = amount;
            break;

        case 9: // '\t'
            ADUDrop = amount;
            break;
        }
    }

    public synchronized void update(int which, String name)
    {
        if(which == 6)
            encodeName = name;
    }

    public int getPDUlost()
    {
        return numLost;
    }

    public int getPDUProcessed()
    {
        return numProc;
    }

    public int getPDUMisOrd()
    {
        return numMisord;
    }

    public int getPDUInvalid()
    {
        return numInvalid;
    }

    public int getPDUDuplicate()
    {
        return numDup;
    }

    public int getPayloadType()
    {
        return payload;
    }

    public String getEncodingName()
    {
        return encodeName;
    }

    public int getBufferSize()
    {
        return qSize;
    }

    public int getPDUDrop()
    {
        return PDUDrop;
    }

    public int getADUDrop()
    {
        return ADUDrop;
    }

    public String toString()
    {
        String s = "PDULost " + getPDUlost() + "\nPDUProcessed " + getPDUProcessed() + "\nPDUMisord " + getPDUMisOrd() + "\nPDUInvalid " + getPDUInvalid() + "\nPDUDuplicate " + getPDUDuplicate();
        return s;
    }

    public static final int PDULOST = 0;
    public static final int PDUPROCSD = 1;
    public static final int PDUMISORD = 2;
    public static final int PDUINVALID = 3;
    public static final int PDUDUP = 4;
    public static final int PAYLOAD = 5;
    public static final int ENCODE = 6;
    public static final int QSIZE = 7;
    public static final int PDUDROP = 8;
    public static final int ADUDROP = 9;
    private int numLost;
    private int numProc;
    private int numMisord;
    private int numInvalid;
    private int numDup;
    private int payload;
    private String encodeName;
    private int qSize;
    private int PDUDrop;
    private int ADUDrop;
}
