// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CineStore.java

package com.sun.media.codec.video.cinepak;

import java.io.PrintStream;
import javax.media.Buffer;

// Referenced classes of package com.sun.media.codec.video.cinepak:
//            CpStrip, CPChunk, CodeEntry

public class CineStore
{

    public CineStore()
    {
        StripVec = new CpStrip[5];
        NumStrips = 0;
        ImageSizeX = 0;
        ImageSizeY = 0;
        ImagePosX = 0;
        ImagePosY = 0;
        StripPosX = 0;
        StripPosY = 0;
        StripPosX1 = 0;
        StripPosY1 = 0;
        fOurChunk = new CPChunk();
    }

    public void addStrip()
    {
        if(NumStrips < 5)
        {
            CpStrip tempStrip = new CpStrip();
            StripVec[NumStrips] = tempStrip;
            NumStrips++;
        }
    }

    public int getNumStrips()
    {
        return NumStrips;
    }

    public void DoFrame(Buffer inBuffer, Buffer outBuffer, CineStore myStor)
    {
        fCPStore = myStor;
        fInBufLen = inBuffer.getLength();
        int dataLength = ((byte[])inBuffer.getData()).length;
        int dataLengthO = ((int[])outBuffer.getData()).length;
        fInData = (byte[])inBuffer.getData();
        if((fInData[0] & 1) == 1)
            finitialCpFlags = false;
        else
            finitialCpFlags = true;
        fTopSize = (fInData[1] & 0xff) * 256 + (fInData[2] & 0xff) * 256 + (fInData[3] & 0xff);
        fXsize = (fInData[4] & 0xff) * 256 + (fInData[5] & 0xff);
        fYsize = (fInData[6] & 0xff) * 256 + (fInData[7] & 0xff);
        outBuffer.setLength(fXsize * fYsize);
        myStor.ImageSizeX = fXsize;
        myStor.ImageSizeY = fYsize;
        fNoOfStrips = (fInData[8] & 0xff) * 256 + (fInData[9] & 0xff);
        if(fNoOfStrips > 5)
            System.err.println("Cinepak data corrupted. Too many strips.");
        for(; fCPStore.getNumStrips() < fNoOfStrips; fCPStore.addStrip());
        int StripStartInBuffer = 10;
        int StripEndInBuffer = 10;
        for(int stripCount = 0; stripCount < fNoOfStrips; stripCount++)
        {
            int cid = (fInData[StripStartInBuffer] & 0xff) * 256 + (fInData[StripStartInBuffer + 1] & 0xff);
            int sizeOfStrip = (fInData[StripStartInBuffer + 2] & 0xff) * 256 + (fInData[StripStartInBuffer + 3] & 0xff);
            StripEndInBuffer = StripStartInBuffer + sizeOfStrip;
            int y0 = (fInData[StripStartInBuffer + 4] & 0xff) * 256 + (fInData[StripStartInBuffer + 5] & 0xff);
            int x0 = (fInData[StripStartInBuffer + 6] & 0xff) * 256 + (fInData[StripStartInBuffer + 7] & 0xff);
            if(stripCount == 0)
            {
                myStor.StripPosX = x0;
                myStor.StripPosY = y0;
            } else
            {
                myStor.StripPosX = x0;
                myStor.StripPosY = myStor.StripPosY + myStor.StripPosY1 + y0;
            }
            int y1 = (fInData[StripStartInBuffer + 8] & 0xff) * 256 + (fInData[StripStartInBuffer + 9] & 0xff);
            int x1 = (fInData[StripStartInBuffer + 10] & 0xff) * 256 + (fInData[StripStartInBuffer + 11] & 0xff);
            myStor.StripPosX1 = x1;
            myStor.StripPosY1 = y1;
            if(finitialCpFlags && cid > 0 && stripCount > 0)
            {
                for(int i = 0; i < 256; i++)
                {
                    myStor.StripVec[stripCount].Smooth[i] = new CodeEntry(myStor.StripVec[stripCount - 1].Smooth[i]);
                    myStor.StripVec[stripCount].Detail[i] = new CodeEntry(myStor.StripVec[stripCount - 1].Detail[i]);
                }

            }
            for(int ChunkStartInBuffer = StripStartInBuffer + 12; ChunkStartInBuffer < StripEndInBuffer; ChunkStartInBuffer = ChunkStartInBuffer + (fInData[ChunkStartInBuffer + 2] & 0xff) * 256 + (fInData[ChunkStartInBuffer + 3] & 0xff))
                fOurChunk.processChunk(fInData, fCPStore, stripCount, ChunkStartInBuffer, outBuffer);

            StripStartInBuffer = StripEndInBuffer;
        }

    }

    public static final int MAXSTRIPS = 5;
    public static final int BOUNDING24[] = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
        0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 
        2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 
        12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 
        22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 
        32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 
        42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 
        52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 
        62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 
        72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 
        82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 
        92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 
        102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 
        112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 
        122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 
        132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 
        142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 
        152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 
        162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 
        172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 
        182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 
        192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 
        202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 
        212, 213, 214, 215, 216, 217, 218, 219, 220, 221, 
        222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 
        232, 233, 234, 235, 236, 237, 238, 239, 240, 241, 
        242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 
        252, 253, 254, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 
        255, 255, 255, 255
    };
    public CpStrip StripVec[];
    public int NumStrips;
    public int ImageSizeX;
    public int ImageSizeY;
    public int ImagePosX;
    public int ImagePosY;
    public int StripPosX;
    public int StripPosY;
    public int StripPosX1;
    public int StripPosY1;
    private boolean finitialCpFlags;
    private int fTopSize;
    private int fXsize;
    private int fYsize;
    private int fNoOfStrips;
    private CineStore fCPStore;
    private byte fInData[];
    private int fInBufLen;
    private CPChunk fOurChunk;

}
