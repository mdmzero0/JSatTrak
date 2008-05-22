// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CPChunk.java

package com.sun.media.codec.video.cinepak;

import java.awt.Dimension;
import javax.media.Buffer;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.codec.video.cinepak:
//            CineStore, CpStrip, CodeEntry

public class CPChunk
{

    public CPChunk()
    {
        firstFlag = 1;
    }

    public CPChunk(int l[])
    {
        lookup = l;
        firstFlag = 1;
    }

    public void setLookup(int l[])
    {
        lookup = l;
    }

    public void processChunk(byte inData[], CineStore myStor, int whichStrip, int ChunkStart, Buffer outBuffer)
    {
        fBounding24 = CineStore.BOUNDING24;
        fChunkType = (inData[ChunkStart] & 0xff) * 256 + (inData[ChunkStart + 1] & 0xff);
        fChunkLen = (inData[ChunkStart + 2] & 0xff) * 256 + (inData[ChunkStart + 3] & 0xff);
        switch(fChunkType)
        {
        case 8192: 
            doCFUpdate(inData, ChunkStart + 4, myStor.StripVec[whichStrip].Detail);
            break;

        case 8704: 
            doCFUpdate(inData, ChunkStart + 4, myStor.StripVec[whichStrip].Smooth);
            break;

        case 8448: 
            doCPUpdate(inData, ChunkStart + 4, myStor.StripVec[whichStrip].Detail);
            break;

        case 8960: 
            doCPUpdate(inData, ChunkStart + 4, myStor.StripVec[whichStrip].Smooth);
            break;

        case 12288: 
            doFKUpdate(inData, ChunkStart + 4, myStor, whichStrip, outBuffer);
            break;

        case 12800: 
            doFSKUpdate(inData, ChunkStart + 4, myStor, whichStrip, outBuffer);
            break;

        case 12544: 
            doIUpdate(inData, ChunkStart + 4, myStor, whichStrip, outBuffer);
            break;

        case 9216: 
            doGFUpdate(inData, ChunkStart + 4, myStor.StripVec[whichStrip].Detail);
            break;

        case 9728: 
            doGFUpdate(inData, ChunkStart + 4, myStor.StripVec[whichStrip].Smooth);
            break;

        case 9472: 
            doGPUpdate(inData, ChunkStart + 4, myStor.StripVec[whichStrip].Detail);
            break;

        case 9984: 
            doGPUpdate(inData, ChunkStart + 4, myStor.StripVec[whichStrip].Smooth);
            break;
        }
    }

    public String getChunkType()
    {
        switch(fChunkType)
        {
        case 8704: 
            return "color full    smooth codebook update";

        case 8192: 
            return "color full    detail codebook update";

        case 8960: 
            return "color partial smooth codebook update";

        case 8448: 
            return "color partial detail codebook update";

        case 12288: 
            return "full key frame update";

        case 12800: 
            return "full smooth key frame update";

        case 12544: 
            return "interframe update";

        case 9216: 
            return "greyscale full smooth codebook update";

        case 9728: 
            return "greyscale full detail codebook update";

        case 9472: 
            return "greyscale partial smooth codebook update";

        case 9984: 
            return "greyscale partial detail codebook update";
        }
        return "WARNING******* unknown atom chunk type...*******";
    }

    public int getChunkLength()
    {
        return fChunkLen;
    }

    private void doCFUpdate(byte ChunkArray[], int ChunkDataStart, CodeEntry codebook[])
    {
        int numberOfCodes = (((ChunkArray[ChunkDataStart - 2] & 0xff) * 256 + (ChunkArray[ChunkDataStart - 1] & 0xff)) - 4) / 6;
        for(int i = 0; i < numberOfCodes; i++)
        {
            int Y0 = ChunkArray[ChunkDataStart + i * 6] & 0xff;
            int Y1 = ChunkArray[ChunkDataStart + i * 6 + 1] & 0xff;
            int Y2 = ChunkArray[ChunkDataStart + i * 6 + 2] & 0xff;
            int Y3 = ChunkArray[ChunkDataStart + i * 6 + 3] & 0xff;
            int U = ChunkArray[ChunkDataStart + i * 6 + 4];
            int V = ChunkArray[ChunkDataStart + i * 6 + 5];
            int delR = 2 * U + 128;
            int delB = 2 * V + 128;
            int delG = (-(U / 2) - V) + 128;
            codebook[i].aRGB0 = (fBounding24[Y0 + delR] << 16) + (fBounding24[Y0 + delG] << 8) + fBounding24[Y0 + delB];
            codebook[i].aRGB1 = (fBounding24[Y1 + delR] << 16) + (fBounding24[Y1 + delG] << 8) + fBounding24[Y1 + delB];
            codebook[i].aRGB2 = (fBounding24[Y2 + delR] << 16) + (fBounding24[Y2 + delG] << 8) + fBounding24[Y2 + delB];
            codebook[i].aRGB3 = (fBounding24[Y3 + delR] << 16) + (fBounding24[Y3 + delG] << 8) + fBounding24[Y3 + delB];
        }

    }

    private void doCPUpdate(byte ChunkArray[], int ChunkDataStart, CodeEntry codebook[])
    {
        int ByteCounter = ChunkDataStart;
        int CodeCount = 0;
        for(int len = (((ChunkArray[ChunkDataStart - 2] & 0xff) * 256 + (ChunkArray[ChunkDataStart - 1] & 0xff)) - 4) + ByteCounter; ByteCounter < len && CodeCount < 256;)
        {
            int Map = ChunkArray[ByteCounter++] & 0xff;
            Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
            Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
            Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
            int Mask = 0x80000000;
            for(int i = 0; i < 32 && ByteCounter < len && CodeCount < 256; i++)
            {
                if((Mask & Map) != 0)
                {
                    int Y0 = ChunkArray[ByteCounter++] & 0xff;
                    int Y1 = ChunkArray[ByteCounter++] & 0xff;
                    int Y2 = ChunkArray[ByteCounter++] & 0xff;
                    int Y3 = ChunkArray[ByteCounter++] & 0xff;
                    int U = ChunkArray[ByteCounter++];
                    int V = ChunkArray[ByteCounter++];
                    int delR = 2 * U + 128;
                    int delB = 2 * V + 128;
                    int delG = (-(U / 2) - V) + 128;
                    codebook[CodeCount].aRGB0 = (fBounding24[Y0 + delR] << 16) + (fBounding24[Y0 + delG] << 8) + fBounding24[Y0 + delB];
                    codebook[CodeCount].aRGB1 = (fBounding24[Y1 + delR] << 16) + (fBounding24[Y1 + delG] << 8) + fBounding24[Y1 + delB];
                    codebook[CodeCount].aRGB2 = (fBounding24[Y2 + delR] << 16) + (fBounding24[Y2 + delG] << 8) + fBounding24[Y2 + delB];
                    codebook[CodeCount].aRGB3 = (fBounding24[Y3 + delR] << 16) + (fBounding24[Y3 + delG] << 8) + fBounding24[Y3 + delB];
                }
                Mask >>>= 1;
                CodeCount++;
            }

        }

    }

    private void doFKUpdate(byte ChunkArray[], int ChunkDataStart, CineStore myStor, int thisStrip, Buffer outBuffer)
    {
        int outData[] = (int[])outBuffer.getData();
        VideoFormat outFmt = (VideoFormat)outBuffer.getFormat();
        int outWidth = outFmt.getSize().width;
        CpStrip theStrip = myStor.StripVec[thisStrip];
        CodeEntry detailBook[] = theStrip.Detail;
        CodeEntry smoothBook[] = theStrip.Smooth;
        int len = (((ChunkArray[ChunkDataStart - 2] & 0xff) * 256 + (ChunkArray[ChunkDataStart - 1] & 0xff)) - 4) + ChunkDataStart;
        int xdraw = myStor.ImagePosX + myStor.StripPosX;
        int ydraw = myStor.ImagePosY + myStor.StripPosY;
        int ByteCounter = ChunkDataStart;
        int CodeCount = 0;
        while(ByteCounter < len && ydraw < myStor.ImagePosY + myStor.ImageSizeY) 
        {
            int Map = ChunkArray[ByteCounter++] & 0xff;
            Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
            Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
            Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
            int Mask = 0x80000000;
            for(int i = 0; i < 32 && ByteCounter < len && ydraw < myStor.ImagePosY + myStor.ImageSizeY; i++)
            {
                if((Mask & Map) != 0)
                {
                    CodeEntry thisCode = detailBook[ChunkArray[ByteCounter++] & 0xff];
                    int color = thisCode.aRGB0;
                    int startLocation = xdraw + outWidth * ydraw;
                    int location = startLocation;
                    outData[location] = color;
                    color = thisCode.aRGB1;
                    location = startLocation + 1;
                    outData[location] = color;
                    color = thisCode.aRGB2;
                    location = startLocation + outWidth;
                    outData[location] = color;
                    color = thisCode.aRGB3;
                    location++;
                    outData[location] = color;
                    thisCode = detailBook[ChunkArray[ByteCounter++] & 0xff];
                    color = thisCode.aRGB0;
                    startLocation = xdraw + 2 + outWidth * ydraw;
                    location = startLocation;
                    outData[location] = color;
                    color = thisCode.aRGB1;
                    location = startLocation + 1;
                    outData[location] = color;
                    color = thisCode.aRGB2;
                    location = startLocation + outWidth;
                    outData[location] = color;
                    color = thisCode.aRGB3;
                    location++;
                    outData[location] = color;
                    thisCode = detailBook[ChunkArray[ByteCounter++] & 0xff];
                    color = thisCode.aRGB0;
                    startLocation = xdraw + outWidth * (ydraw + 2);
                    location = startLocation;
                    outData[location] = color;
                    color = thisCode.aRGB1;
                    location = startLocation + 1;
                    outData[location] = color;
                    color = thisCode.aRGB2;
                    location = startLocation + outWidth;
                    outData[location] = color;
                    color = thisCode.aRGB3;
                    location++;
                    outData[location] = color;
                    thisCode = detailBook[ChunkArray[ByteCounter++] & 0xff];
                    color = thisCode.aRGB0;
                    startLocation = xdraw + 2 + outWidth * (ydraw + 2);
                    location = startLocation;
                    outData[location] = color;
                    color = thisCode.aRGB1;
                    location = startLocation + 1;
                    outData[location] = color;
                    color = thisCode.aRGB2;
                    location = startLocation + outWidth;
                    outData[location] = color;
                    color = thisCode.aRGB3;
                    outData[location + 1] = color;
                } else
                {
                    CodeEntry thisCode = smoothBook[ChunkArray[ByteCounter++] & 0xff];
                    int color = thisCode.aRGB0;
                    int startLocation = xdraw + outWidth * ydraw;
                    int location = startLocation;
                    outData[location] = color;
                    outData[location + 1] = color;
                    location += outWidth;
                    outData[location] = color;
                    outData[location + 1] = color;
                    color = thisCode.aRGB1;
                    location = startLocation + 2;
                    outData[location] = color;
                    outData[location + 1] = color;
                    location += outWidth;
                    outData[location] = color;
                    outData[location + 1] = color;
                    color = thisCode.aRGB2;
                    location = startLocation += outWidth * 2;
                    outData[location] = color;
                    outData[location + 1] = color;
                    location += outWidth;
                    outData[location] = color;
                    outData[location + 1] = color;
                    color = thisCode.aRGB3;
                    location = startLocation + 2;
                    outData[location] = color;
                    outData[location + 1] = color;
                    location += outWidth;
                    outData[location] = color;
                    outData[location + 1] = color;
                }
                xdraw += 4;
                Mask >>>= 1;
                CodeCount++;
                if(xdraw > myStor.ImageSizeX - 4)
                {
                    xdraw = myStor.ImagePosX + myStor.StripPosX;
                    ydraw += 4;
                }
            }

        }
    }

    private void doFSKUpdate(byte ChunkArray[], int ChunkDataStart, CineStore myStor, int thisStrip, Buffer outBuffer)
    {
        int outData[] = (int[])outBuffer.getData();
        VideoFormat outFmt = (VideoFormat)outBuffer.getFormat();
        int outWidth = outFmt.getSize().width;
        CpStrip theStrip = myStor.StripVec[thisStrip];
        CodeEntry detailBook[] = theStrip.Detail;
        CodeEntry smoothBook[] = theStrip.Smooth;
        int len = (((ChunkArray[ChunkDataStart - 2] & 0xff) * 256 + (ChunkArray[ChunkDataStart - 1] & 0xff)) - 4) + ChunkDataStart;
        int xdraw = myStor.ImagePosX + myStor.StripPosX;
        int ydraw = myStor.ImagePosY + myStor.StripPosY;
        for(int ByteCounter = ChunkDataStart; ByteCounter < len;)
        {
            CodeEntry thisCode = smoothBook[ChunkArray[ByteCounter++] & 0xff];
            int color = thisCode.aRGB0;
            int startLocation = xdraw + outWidth * ydraw;
            if(startLocation >= outData.length)
                break;
            int location = startLocation;
            outData[location] = color;
            outData[location + 1] = color;
            location += outWidth;
            outData[location] = color;
            outData[location + 1] = color;
            color = thisCode.aRGB1;
            location = startLocation + 2;
            outData[location] = color;
            outData[location + 1] = color;
            location += outWidth;
            outData[location] = color;
            outData[location + 1] = color;
            color = thisCode.aRGB2;
            location = startLocation += outWidth * 2;
            outData[location] = color;
            outData[location + 1] = color;
            location += outWidth;
            outData[location] = color;
            outData[location + 1] = color;
            color = thisCode.aRGB3;
            location = startLocation + 2;
            outData[location] = color;
            outData[location + 1] = color;
            location += outWidth;
            outData[location] = color;
            outData[location + 1] = color;
            xdraw += 4;
            if(xdraw > myStor.ImageSizeX - 4)
            {
                xdraw = myStor.ImagePosX + myStor.StripPosX;
                ydraw += 4;
            }
        }

    }

    private void doIUpdate(byte ChunkArray[], int ChunkDataStart, CineStore myStor, int thisStrip, Buffer outBuffer)
    {
        int outData[] = (int[])outBuffer.getData();
        VideoFormat outFmt = (VideoFormat)outBuffer.getFormat();
        int outWidth = outFmt.getSize().width;
        CodeEntry detailBook[] = myStor.StripVec[thisStrip].Detail;
        CodeEntry smoothBook[] = myStor.StripVec[thisStrip].Smooth;
        int len = (((ChunkArray[ChunkDataStart - 2] & 0xff) * 256 + (ChunkArray[ChunkDataStart - 1] & 0xff)) - 4) + ChunkDataStart;
        int xdraw = myStor.ImagePosX + myStor.StripPosX;
        int ydraw = myStor.ImagePosY + myStor.StripPosY;
        int ByteCounter = ChunkDataStart;
        int Map = 0;
        int Mask = 0;
        for(int FinishY = myStor.ImagePosY + myStor.StripPosY + myStor.StripPosY1; ByteCounter < len && ydraw < FinishY;)
        {
            Mask >>>= 1;
            if(Mask == 0)
            {
                Map = ChunkArray[ByteCounter++] & 0xff;
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                Mask = 0x80000000;
            }
            if((Mask & Map) != 0 && ByteCounter < len)
            {
                Mask >>>= 1;
                if(Mask == 0)
                {
                    Map = ChunkArray[ByteCounter++] & 0xff;
                    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                    Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                    Mask = 0x80000000;
                }
                if((Mask & Map) != 0)
                {
                    int startLocation = xdraw + outWidth * ydraw;
                    CodeEntry thisCode = detailBook[ChunkArray[ByteCounter++] & 0xff];
                    outData[startLocation] = thisCode.aRGB0;
                    outData[startLocation + 1] = thisCode.aRGB1;
                    outData[startLocation + outWidth] = thisCode.aRGB2;
                    outData[startLocation + outWidth + 1] = thisCode.aRGB3;
                    startLocation = xdraw + 2 + outWidth * ydraw;
                    thisCode = detailBook[ChunkArray[ByteCounter++] & 0xff];
                    outData[startLocation] = thisCode.aRGB0;
                    outData[startLocation + 1] = thisCode.aRGB1;
                    outData[startLocation + outWidth] = thisCode.aRGB2;
                    outData[startLocation + outWidth + 1] = thisCode.aRGB3;
                    thisCode = detailBook[ChunkArray[ByteCounter++] & 0xff];
                    startLocation = xdraw + outWidth * (ydraw + 2);
                    outData[startLocation] = thisCode.aRGB0;
                    outData[startLocation + 1] = thisCode.aRGB1;
                    outData[startLocation + outWidth] = thisCode.aRGB2;
                    outData[startLocation + outWidth + 1] = thisCode.aRGB3;
                    thisCode = detailBook[ChunkArray[ByteCounter++] & 0xff];
                    startLocation = xdraw + 2 + outWidth * (ydraw + 2);
                    outData[startLocation] = thisCode.aRGB0;
                    outData[startLocation + 1] = thisCode.aRGB1;
                    outData[startLocation + outWidth] = thisCode.aRGB2;
                    outData[startLocation + outWidth + 1] = thisCode.aRGB3;
                } else
                {
                    CodeEntry thisCode = smoothBook[ChunkArray[ByteCounter++] & 0xff];
                    int color = thisCode.aRGB0;
                    int startLocation = xdraw + outWidth * ydraw;
                    int location = startLocation;
                    outData[location] = color;
                    outData[location + 1] = color;
                    location += outWidth;
                    outData[location] = color;
                    outData[location + 1] = color;
                    color = thisCode.aRGB1;
                    location = startLocation + 2;
                    outData[location] = color;
                    outData[location + 1] = color;
                    location += outWidth;
                    outData[location] = color;
                    outData[location + 1] = color;
                    color = thisCode.aRGB2;
                    startLocation += outWidth * 2;
                    location = startLocation;
                    outData[location] = color;
                    outData[location + 1] = color;
                    location += outWidth;
                    outData[location] = color;
                    outData[location + 1] = color;
                    color = thisCode.aRGB3;
                    location = startLocation + 2;
                    outData[location] = color;
                    outData[location + 1] = color;
                    location += outWidth;
                    outData[location] = color;
                    outData[location + 1] = color;
                }
            }
            xdraw += 4;
            if(xdraw > myStor.ImageSizeX - 4)
            {
                xdraw = myStor.ImagePosX + myStor.StripPosX;
                ydraw += 4;
            }
        }

    }

    private void doGFUpdate(byte ChunkArray[], int ChunkDataStart, CodeEntry codebook[])
    {
        int ByteCounter = ChunkDataStart;
        int numberOfCodes = (((ChunkArray[ChunkDataStart - 2] & 0xff) * 256 + (ChunkArray[ChunkDataStart - 1] & 0xff)) - 4) / 4;
        if(lookup == null)
        {
            for(int i = 0; i < numberOfCodes; i++)
                if(firstFlag == 1)
                {
                    int anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB0 = (anInt << 16) + (anInt << 8) + anInt;
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB1 = (anInt << 16) + (anInt << 8) + anInt;
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB2 = (anInt << 16) + (anInt << 8) + anInt;
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB3 = (anInt << 16) + (anInt << 8) + anInt;
                } else
                {
                    int anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB0 = anInt << 16 | anInt << 8 | anInt;
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB1 = anInt << 16 | anInt << 8 | anInt;
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB2 = anInt << 16 | anInt << 8 | anInt;
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB3 = anInt << 16 | anInt << 8 | anInt;
                }

        } else
        {
            for(int i = 0; i < numberOfCodes; i++)
                if(firstFlag == 1)
                {
                    int anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB0 = lookup[anInt];
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB1 = lookup[anInt];
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB2 = lookup[anInt];
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB3 = lookup[anInt];
                } else
                {
                    int anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB0 = lookup[anInt];
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB1 = lookup[anInt];
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB2 = lookup[anInt];
                    anInt = ChunkArray[ByteCounter++] & 0xff;
                    codebook[i].aRGB3 = lookup[anInt];
                }

        }
        firstFlag = 0;
    }

    private void doGPUpdate(byte ChunkArray[], int ChunkDataStart, CodeEntry codebook[])
    {
        int ByteCounter = ChunkDataStart;
        int CodeCount = 0;
        int len = (((ChunkArray[ChunkDataStart - 2] & 0xff) * 256 + (ChunkArray[ChunkDataStart - 1] & 0xff)) - 4) + ChunkDataStart;
        if(lookup == null)
            while(ByteCounter < len && CodeCount < 256) 
            {
                int Map = ChunkArray[ByteCounter++] & 0xff;
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                int Mask = 0x80000000;
                for(int i = 0; i < 32 && ByteCounter < len && CodeCount < 256; i++)
                {
                    if((Mask & Map) != 0)
                    {
                        int anInt = ChunkArray[ByteCounter++] & 0xff;
                        codebook[CodeCount].aRGB0 = anInt << 16 | anInt << 8 | anInt;
                        anInt = ChunkArray[ByteCounter++] & 0xff;
                        codebook[CodeCount].aRGB1 = anInt << 16 | anInt << 8 | anInt;
                        anInt = ChunkArray[ByteCounter++] & 0xff;
                        codebook[CodeCount].aRGB2 = anInt << 16 | anInt << 8 | anInt;
                        anInt = ChunkArray[ByteCounter++] & 0xff;
                        codebook[CodeCount].aRGB3 = anInt << 16 | anInt << 8 | anInt;
                    }
                    Mask >>>= 1;
                    CodeCount++;
                }

            }
        else
            while(ByteCounter < len && CodeCount < 256) 
            {
                int Map = ChunkArray[ByteCounter++] & 0xff;
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                Map = Map * 256 + (ChunkArray[ByteCounter++] & 0xff);
                int Mask = 0x80000000;
                for(int i = 0; i < 32 && ByteCounter < len && CodeCount < 256; i++)
                {
                    if((Mask & Map) != 0)
                    {
                        int anInt = ChunkArray[ByteCounter++] & 0xff;
                        codebook[CodeCount].aRGB0 = lookup[anInt];
                        anInt = ChunkArray[ByteCounter++] & 0xff;
                        codebook[CodeCount].aRGB1 = lookup[anInt];
                        anInt = ChunkArray[ByteCounter++] & 0xff;
                        codebook[CodeCount].aRGB2 = lookup[anInt];
                        anInt = ChunkArray[ByteCounter++] & 0xff;
                        codebook[CodeCount].aRGB3 = lookup[anInt];
                    }
                    Mask >>>= 1;
                    CodeCount++;
                }

            }
    }

    int fChunkType;
    int fChunkLen;
    int lookup[];
    static int fBounding24[];
    static int firstFlag;
}
