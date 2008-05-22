// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Packetizer.java

package com.sun.media.codec.video.jpeg;

import com.sun.media.*;
import java.awt.Component;
import java.awt.Dimension;
import java.io.PrintStream;
import javax.media.*;
import javax.media.control.FrameProcessingControl;
import javax.media.format.JPEGFormat;
import javax.media.format.VideoFormat;

public class Packetizer extends BasicCodec
{

    public Packetizer()
    {
        inputFormat = null;
        outputFormat = null;
        PACKET_SIZE = 960;
        currentSeq = 0;
        copyLength = PACKET_SIZE;
        newFrame = true;
        dropFrame = false;
        minimal = false;
        offset = 0;
        frameLength = 0;
        decimation = -1;
        frame_duration = -1F;
        super.inputFormats = (new VideoFormat[] {
            new VideoFormat("jpeg")
        });
        super.outputFormats = (new VideoFormat[] {
            new VideoFormat("jpeg/rtp")
        });
        FrameProcessingControl fpc = new FrameProcessingControl() {

            public boolean setMinimalProcessing(boolean newMinimal)
            {
                minimal = newMinimal;
                return minimal;
            }

            public void setFramesBehind(float frames)
            {
                if(frames >= 1.0F)
                    dropFrame = true;
                else
                    dropFrame = false;
            }

            public Component getControlComponent()
            {
                return null;
            }

            public int getFramesDropped()
            {
                return 0;
            }

        }
;
        super.controls = new Control[1];
        super.controls[0] = fpc;
    }

    protected Format getInputFormat()
    {
        return inputFormat;
    }

    protected Format getOutputFormat()
    {
        return outputFormat;
    }

    public Format[] getSupportedOutputFormats(Format in)
    {
        if(in == null)
            return super.outputFormats;
        if(!verifyInputFormat(in))
            return new Format[0];
        Format out[] = new Format[1];
        if(((VideoFormat)in).getFrameRate() == -1F)
            out[0] = new VideoFormat("jpeg/rtp", ((VideoFormat)in).getSize(), ((VideoFormat)in).getMaxDataLength(), Format.byteArray, 15F);
        else
            out[0] = new VideoFormat("jpeg/rtp", ((VideoFormat)in).getSize(), ((VideoFormat)in).getMaxDataLength(), Format.byteArray, ((VideoFormat)in).getFrameRate());
        return out;
    }

    private boolean verifyInputFormat(Format input)
    {
        return fJPEG.matches(input);
    }

    public Format setInputFormat(Format input)
    {
        if(!verifyInputFormat(input))
            return null;
        inputFormat = (VideoFormat)input;
        float rate = inputFormat.getFrameRate();
        if(rate != -1F)
            frame_duration = 1000F / rate;
        if(super.opened)
            outputFormat = (VideoFormat)getSupportedOutputFormats(input)[0];
        return input;
    }

    public Format setOutputFormat(Format output)
    {
        if(BasicPlugIn.matches(output, super.outputFormats) == null)
        {
            return null;
        } else
        {
            outputFormat = (VideoFormat)output;
            return output;
        }
    }

    public void open()
        throws ResourceUnavailableException
    {
        if(inputFormat == null || outputFormat == null)
            throw new ResourceUnavailableException("Incorrect formats set on JPEG Packetizer");
        Dimension size = inputFormat.getSize();
        if(size != null && (size.width % 8 != 0 || size.height % 8 != 0))
        {
            Log.error("Class: " + this);
            Log.error("  can only packetize in sizes of multiple of 8 pixels.");
            throw new ResourceUnavailableException("Incorrect formats set on JPEG Packetizer");
        } else
        {
            super.open();
            return;
        }
    }

    public void reset()
    {
    }

    public synchronized int process(Buffer inBuffer, Buffer outBuffer)
    {
        if(isEOM(inBuffer))
        {
            propagateEOM(outBuffer);
            return 0;
        }
        if(inBuffer.isDiscard())
        {
            updateOutput(outBuffer, outputFormat, 0, 0);
            outBuffer.setDiscard(true);
            return 4;
        }
        if(inBuffer.getLength() <= 0)
        {
            outBuffer.setDiscard(true);
            return 4;
        }
        byte inData[] = (byte[])inBuffer.getData();
        outBuffer.setFormat(outputFormat);
        Dimension size = inputFormat.getSize();
        int keyFrame = 0;
        if(newFrame)
        {
            if(dropFrame || minimal)
            {
                outBuffer.setDiscard(true);
                return 0;
            }
            int tempdec = peekJPEGDecimation(inData, inBuffer.getLength());
            if(tempdec >= 0)
                decimation = tempdec;
            if(inputFormat instanceof JPEGFormat)
                stripTables(inBuffer);
            frameLength = inBuffer.getLength();
            offset = 0;
            newFrame = false;
            keyFrame = 16;
        }
        if(frameLength - offset < PACKET_SIZE)
            copyLength = frameLength - offset;
        else
            copyLength = PACKET_SIZE;
        byte outData[] = (byte[])outBuffer.getData();
        if(outData == null || outData.length < copyLength + 8)
        {
            outData = new byte[copyLength + 8];
            outBuffer.setData(outData);
        }
        System.arraycopy(inData, offset + inBuffer.getOffset(), outData, 8, copyLength);
        int qfactor = (inputFormat instanceof JPEGFormat) ? ((JPEGFormat)inputFormat).getQFactor() : 80;
        decimation = (inputFormat instanceof JPEGFormat) ? ((JPEGFormat)inputFormat).getDecimation() : decimation;
        if(decimation == -1)
            decimation = 1;
        outBuffer.setLength(copyLength + 8);
        outBuffer.setOffset(0);
        outBuffer.setSequenceNumber(currentSeq++);
        outBuffer.setFormat(outputFormat);
        outData[0] = 0;
        outData[1] = (byte)(offset >> 16);
        outData[2] = (byte)(offset >> 8);
        outData[3] = (byte)offset;
        outData[4] = (byte)decimation;
        outData[5] = (byte)qfactor;
        outData[6] = (byte)(size.width / 8);
        outData[7] = (byte)(size.height / 8);
        offset += copyLength;
        outBuffer.setFlags(outBuffer.getFlags() | keyFrame);
        if(offset == frameLength)
        {
            outBuffer.setFlags(outBuffer.getFlags() | 0x800);
            newFrame = true;
            return 0;
        } else
        {
            return 2;
        }
    }

    public void finalize()
    {
        close();
    }

    public String getName()
    {
        return "JPEG Packetizer";
    }

    int peekJPEGDecimation(byte data[], int dataLen)
    {
        int i = 0;
        if((data[0] & 0xff) != 255 || data[1] == 0)
            return -1;
        while(i < dataLen - 2) 
            if((data[i] & 0xff) == 255)
            {
                i++;
                int code = data[i] & 0xff;
                i++;
                switch(code)
                {
                case 192: 
                case 193: 
                    return getDecimationFromSOF(data, i, dataLen);
                }
            } else
            {
                i++;
            }
        return -1;
    }

    private void stripTables(Buffer inb)
    {
        byte data[] = (byte[])inb.getData();
        int offset = inb.getOffset();
        int length = inb.getLength();
        for(int i = offset; i < (length + offset) - 8; i++)
        {
            if(data[i] != -1 || data[i + 1] != -38)
                continue;
            int blockSize = (data[i + 2] & 0xff) << 8 | data[i + 3] & 0xff;
            i += 2 + blockSize;
            System.arraycopy(data, i, data, 0, (length + offset) - i);
            inb.setOffset(0);
            inb.setLength((length + offset) - i);
            break;
        }

    }

    int getDecimationFromSOF(byte data[], int i, int length)
    {
        int sectionLen = (data[i++] & 0xff) << 8;
        sectionLen |= data[i++] & 0xff;
        i += 5;
        int ncomp = data[i++] & 0xff;
        if(sectionLen != ncomp * 3 + 8)
            System.err.println("Bogus SOF length");
        int id = data[i++] & 0xff;
        int deccode = data[i++] & 0xff;
        int hsf = deccode >> 4 & 0xf;
        int vsf = deccode & 0xf;
        if(vsf == 2 && hsf == 2)
            return 1;
        return vsf != 1 || hsf != 1 ? 0 : 2;
    }

    static final JPEGFormat fJPEG = new JPEGFormat();
    private VideoFormat inputFormat;
    private VideoFormat outputFormat;
    private int PACKET_SIZE;
    private int currentSeq;
    private int copyLength;
    private boolean newFrame;
    private boolean dropFrame;
    private boolean minimal;
    private int offset;
    private int frameLength;
    private static final int J_SOF = 192;
    private static final int J_SOF1 = 193;
    private int decimation;
    private static final int DEFAULT_FRAMERATE = 15;
    private float frame_duration;




}
