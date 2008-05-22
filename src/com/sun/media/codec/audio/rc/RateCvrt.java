// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RateCvrt.java

package com.sun.media.codec.audio.rc;

import com.sun.media.BasicCodec;
import com.sun.media.BasicPlugIn;
import com.sun.media.codec.audio.AudioCodec;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.AudioFormat;

public class RateCvrt extends AudioCodec
{

    public RateCvrt()
    {
        super.inputFormats = (new Format[] {
            new AudioFormat("LINEAR")
        });
    }

    public String getName()
    {
        return "Rate Conversion";
    }

    public Format[] getSupportedOutputFormats(Format input)
    {
        if(input == null)
            return (new Format[] {
                new AudioFormat("LINEAR")
            });
        if(input instanceof AudioFormat)
        {
            AudioFormat af = (AudioFormat)input;
            int ssize = af.getSampleSizeInBits();
            int chnl = af.getChannels();
            int endian = af.getEndian();
            int signed = af.getSigned();
            super.outputFormats = (new Format[] {
                new AudioFormat("LINEAR", 8000D, ssize, chnl, endian, signed), new AudioFormat("LINEAR", 11025D, ssize, chnl, endian, signed), new AudioFormat("LINEAR", 16000D, ssize, chnl, endian, signed), new AudioFormat("LINEAR", 22050D, ssize, chnl, endian, signed), new AudioFormat("LINEAR", 32000D, ssize, chnl, endian, signed), new AudioFormat("LINEAR", 44100D, ssize, chnl, endian, signed), new AudioFormat("LINEAR", 48000D, ssize, chnl, endian, signed)
            });
        } else
        {
            super.outputFormats = new Format[0];
        }
        return super.outputFormats;
    }

    public synchronized int process(Buffer in, Buffer out)
    {
        if(!checkInputBuffer(in))
            return 1;
        if(isEOM(in))
        {
            propagateEOM(out);
            return 0;
        }
        int inOffset = in.getOffset();
        int inLen = in.getLength();
        double inRate = ((AudioFormat)super.inputFormat).getSampleRate();
        double outRate = ((AudioFormat)super.outputFormat).getSampleRate();
        int chnl = ((AudioFormat)super.inputFormat).getChannels();
        int bsize = ((AudioFormat)super.inputFormat).getSampleSizeInBits() / 8;
        int step = 0;
        if(chnl == 2)
        {
            if(bsize == 2)
                step = 4;
            else
                step = 2;
        } else
        if(bsize == 2)
            step = 2;
        else
            step = 1;
        if(outRate == 0.0D || inRate == 0.0D)
            return 1;
        double ratio = inRate / outRate;
        int outLen = (int)(((double)(inLen - inOffset) * outRate) / inRate + 0.5D);
        switch(step)
        {
        case 2: // '\002'
            if(outLen % 2 == 1)
                outLen++;
            break;

        case 4: // '\004'
            if(outLen % 4 != 0)
                outLen = outLen / 4 + 1 << 2;
            break;
        }
        if(super.inputFormat.getDataType() == Format.byteArray)
            return doByteCvrt(in, inLen, inOffset, out, outLen, step, ratio);
        if(super.inputFormat.getDataType() == Format.shortArray)
            return doShortCvrt(in, inLen, inOffset, out, outLen, step, ratio);
        if(super.inputFormat.getDataType() == Format.intArray)
            return doIntCvrt(in, inLen, inOffset, out, outLen, step, ratio);
        else
            return 1;
    }

    private int doByteCvrt(Buffer in, int inLen, int inOffset, Buffer out, int outLen, int step, double ratio)
    {
        byte inData[] = (byte[])in.getData();
        byte outData[] = validateByteArraySize(out, outLen);
        int outOffset = 0;
        out.setData(outData);
        out.setFormat(super.outputFormat);
        out.setOffset(0);
        out.setLength(outLen);
        double sum = 0.0D;
        int inPtr = inOffset;
        int outPtr = outOffset;
        int inEnd = inOffset + inLen;
        if(ratio == 1.0D)
        {
            System.arraycopy(inData, inOffset, outData, outOffset, inLen);
            return 0;
        }
        if(ratio > 1.0D)
        {
            while(inPtr <= inEnd - step && outPtr <= outLen - step) 
            {
                for(int i = 0; i < step; i++)
                    outData[outPtr++] = inData[inPtr + i];

                for(sum += ratio; sum > 0.0D; sum--)
                    inPtr += step;

            }
        } else
        {
            byte d[] = new byte[step];
            for(; inPtr <= inEnd - step; inPtr += step)
            {
                for(int i = 0; i < step; i++)
                {
                    outData[outPtr++] = inData[inPtr + i];
                    d[i] = inData[inPtr + i];
                }

                while((sum += ratio) < 1.0D) 
                    if(outPtr <= outLen - step)
                    {
                        for(int i = 0; i < step; i++)
                            outData[outPtr++] = d[i];

                    }
                sum--;
            }

        }
        return 0;
    }

    private int doShortCvrt(Buffer in, int inLen, int inOffset, Buffer out, int outLen, int step, double ratio)
    {
        short inData[] = (short[])in.getData();
        short outData[] = validateShortArraySize(out, outLen);
        int outOffset = 0;
        out.setData(outData);
        out.setFormat(super.outputFormat);
        out.setOffset(0);
        out.setLength(outLen);
        double sum = 0.0D;
        int inPtr = inOffset;
        int outPtr = outOffset;
        int inEnd = inOffset + inLen;
        if(ratio == 1.0D)
        {
            System.arraycopy(inData, inOffset, outData, outOffset, inLen);
            return 0;
        }
        if(ratio > 1.0D)
        {
            while(inPtr <= inEnd - step && outPtr <= outLen - step) 
            {
                for(int i = 0; i < step; i++)
                    outData[outPtr++] = inData[inPtr + i];

                for(sum += ratio; sum > 0.0D; sum--)
                    inPtr += step;

            }
        } else
        {
            short d[] = new short[step];
            for(; inPtr <= inEnd - step; inPtr += step)
            {
                for(int i = 0; i < step; i++)
                {
                    outData[outPtr++] = inData[inPtr + i];
                    d[i] = inData[inPtr + i];
                }

                while((sum += ratio) < 1.0D) 
                    if(outPtr <= outLen - step)
                    {
                        for(int i = 0; i < step; i++)
                            outData[outPtr++] = d[i];

                    }
                sum--;
            }

        }
        return 0;
    }

    private int doIntCvrt(Buffer in, int inLen, int inOffset, Buffer out, int outLen, int step, double ratio)
    {
        int inData[] = (int[])in.getData();
        int outData[] = validateIntArraySize(out, outLen);
        int outOffset = 0;
        out.setData(outData);
        out.setFormat(super.outputFormat);
        out.setOffset(0);
        out.setLength(outLen);
        double sum = 0.0D;
        int inPtr = inOffset;
        int outPtr = outOffset;
        int inEnd = inOffset + inLen;
        if(ratio == 1.0D)
        {
            System.arraycopy(inData, inOffset, outData, outOffset, inLen);
            return 0;
        }
        if(ratio > 1.0D)
        {
            while(inPtr <= inEnd - step && outPtr <= outLen - step) 
            {
                for(int i = 0; i < step; i++)
                    outData[outPtr++] = inData[inPtr + i];

                for(sum += ratio; sum > 0.0D; sum--)
                    inPtr += step;

            }
        } else
        {
            int d[] = new int[step];
            for(; inPtr <= inEnd - step; inPtr += step)
            {
                for(int i = 0; i < step; i++)
                {
                    outData[outPtr++] = inData[inPtr + i];
                    d[i] = inData[inPtr + i];
                }

                while((sum += ratio) < 1.0D) 
                    if(outPtr <= outLen - step)
                    {
                        for(int i = 0; i < step; i++)
                            outData[outPtr++] = d[i];

                    }
                sum--;
            }

        }
        return 0;
    }
}
