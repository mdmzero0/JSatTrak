// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   YCbCrToRGB.java

package com.ibm.media.codec.video.h263;


public class YCbCrToRGB
{

    public YCbCrToRGB()
    {
    }

    public static void convert(int Y[], int Cb[], int Cr[], int pixData[], int srcWidth, int srcHeight, int dstWidth, int dstHeight, 
            int alpha, int colorDepth)
    {
        int srcDstOffset = srcWidth - dstWidth;
        if(srcDstOffset < 0)
            return;
        int shiftedAlpha = alpha << 24;
        int dstIndex00 = 0;
        int dstIndex10 = dstWidth;
        int yIndex00 = 0;
        int yIndex10 = srcWidth;
        int cbIndex = 0;
        int crIndex = 0;
        for(int i = 0; i < dstHeight; i += 2)
        {
            for(int j = 0; j < dstWidth; j += 2)
            {
                byte y00 = (byte)Y[yIndex00++];
                byte y01 = (byte)Y[yIndex00++];
                byte y10 = (byte)Y[yIndex10++];
                byte y11 = (byte)Y[yIndex10++];
                int Yval = Ex0xY_Table[0xff & y00];
                int Cb_nCrCb_Idx = 0xff & (byte)Cb[cbIndex++];
                int Cr_nCrCb_Idx = 0xff & (byte)Cr[crIndex++];
                int partRed;
                int red = Yval + (partRed = E02xCr_Table[Cr_nCrCb_Idx]);
                int partGreen;
                int green = Yval + (partGreen = E11xCb_Table[Cb_nCrCb_Idx] + E12xCr_Table[Cr_nCrCb_Idx]);
                int partBlue;
                int blue = Yval + (partBlue = E21xCb_Table[Cb_nCrCb_Idx]);
                int tempRGB;
                pixData[dstIndex00++] = tempRGB = shiftedAlpha | clipR[red & 0x3ff] | clipG[green & 0x3ff] | clipB[blue & 0x3ff];
                pixData[dstIndex00++] = y00 == y01 ? tempRGB : shiftedAlpha | clipR[(Yval = Ex0xY_Table[y01 & 0xff]) + partRed & 0x3ff] | clipG[Yval + partGreen & 0x3ff] | clipB[Yval + partBlue & 0x3ff];
                pixData[dstIndex10++] = y00 == y10 ? tempRGB : shiftedAlpha | clipR[(Yval = Ex0xY_Table[y10 & 0xff]) + partRed & 0x3ff] | clipG[Yval + partGreen & 0x3ff] | clipB[Yval + partBlue & 0x3ff];
                pixData[dstIndex10++] = y00 == y11 ? tempRGB : shiftedAlpha | clipR[(Yval = Ex0xY_Table[y11 & 0xff]) + partRed & 0x3ff] | clipG[Yval + partGreen & 0x3ff] | clipB[Yval + partBlue & 0x3ff];
            }

            yIndex00 = yIndex10 + srcDstOffset;
            yIndex10 += srcWidth + srcDstOffset;
            dstIndex00 = dstIndex10;
            dstIndex10 += dstWidth;
            cbIndex += srcDstOffset / 2;
            crIndex += srcDstOffset / 2;
        }

    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997, 1998.";
    private static final int E02xCr_Table[];
    private static final int E11xCb_Table[];
    private static final int E12xCr_Table[];
    private static final int E21xCb_Table[];
    private static final int Ex0xY_Table[];
    private static final int clipR[];
    private static final int clipG[];
    private static final int clipB[];

    static 
    {
        E02xCr_Table = new int[256];
        E11xCb_Table = new int[256];
        E12xCr_Table = new int[256];
        E21xCb_Table = new int[256];
        Ex0xY_Table = new int[256];
        clipR = new int[1024];
        clipG = new int[1024];
        clipB = new int[1024];
        for(int i = 0; i < 256; i++)
        {
            E02xCr_Table[i] = (int)(1.5960000000000001D * (double)(i - 128));
            E11xCb_Table[i] = (int)(-0.39200000000000002D * (double)(i - 128));
            E12xCr_Table[i] = (int)(-0.81299999999999994D * (double)(i - 128));
            E21xCb_Table[i] = (int)(2.0169999999999999D * (double)(i - 128));
            Ex0xY_Table[i] = (int)(1.1639999999999999D * (double)(i - 16));
        }

        for(int i = 0; i < 1024; i++)
        {
            int clip = i >= 256 ? ((int) (i >= 512 ? 0 : 255)) : i;
            clipB[i] = clip << 16;
            clipG[i] = clip << 8;
            clipR[i] = clip;
        }

    }
}
