// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaDecoder.java

package com.ibm.media.codec.video.h263;

import com.ibm.media.codec.video.VideoCodec;
import com.sun.media.*;
import java.awt.Dimension;
import javax.media.*;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.ibm.media.codec.video.h263:
//            H263Decoder, ReadStream, FrameBuffer, YCbCrToRGB

public class JavaDecoder extends VideoCodec
{

    public JavaDecoder()
    {
        videoWidth = 176;
        videoHeight = 144;
        FormatSizeInitFlag = false;
        payloadLength = 4;
        super.supportedInputFormats = (new VideoFormat[] {
            new VideoFormat("h263"), new VideoFormat("h263/rtp")
        });
        super.defaultOutputFormats = (new VideoFormat[] {
            new RGBFormat()
        });
        super.PLUGIN_NAME = "H.263 Decoder";
    }

    protected Format[] getMatchingOutputFormats(Format in)
    {
        VideoFormat ivf = (VideoFormat)in;
        Dimension inSize = ivf.getSize();
        int maxDataLength = ivf.getMaxDataLength();
        if(ivf.getEncoding().equals("h263/rtp"))
            super.supportedOutputFormats = (new VideoFormat[] {
                new RGBFormat(new Dimension(videoWidth, videoHeight), videoWidth * videoHeight, int[].class, ivf.getFrameRate(), 32, 255, 65280, 0xff0000, 1, videoWidth, 0, -1)
            });
        else
            super.supportedOutputFormats = (new VideoFormat[] {
                new RGBFormat(new Dimension(inSize), inSize.width * inSize.height, int[].class, ivf.getFrameRate(), 32, 255, 65280, 0xff0000, 1, inSize.width, 0, -1)
            });
        return super.supportedOutputFormats;
    }

    public Format setInputFormat(Format format)
    {
        if(nativeAvail)
            return null;
        if(super.setInputFormat(format) != null)
        {
            reset();
            return format;
        } else
        {
            return null;
        }
    }

    public void open()
        throws ResourceUnavailableException
    {
        initDecoder();
    }

    public void close()
    {
        javaDecoder = null;
    }

    public void reset()
    {
        initDecoder();
    }

    protected void videoResized()
    {
        initDecoder();
    }

    protected void initDecoder()
    {
        javaDecoder = new H263Decoder(true);
    }

    public int process(Buffer inputBuffer, Buffer outputBuffer)
    {
        boolean rtpData = false;
        if(!checkInputBuffer(inputBuffer))
            return 1;
        if(isEOM(inputBuffer))
        {
            propagateEOM(outputBuffer);
            return 0;
        }
        VideoFormat ivf = (VideoFormat)inputBuffer.getFormat();
        int inLength = inputBuffer.getLength();
        int inMaxLength = ivf.getMaxDataLength();
        int outMaxLength = super.outputFormat.getMaxDataLength();
        int inputOffset = inputBuffer.getOffset();
        byte inData[] = (byte[])inputBuffer.getData();
        if(ivf.getEncoding().equals("h263/rtp"))
        {
            rtpData = true;
            payloadLength = getPayloadHeaderLength(inData, inputOffset);
            if(inData[inputOffset + payloadLength] == 0 && inData[inputOffset + payloadLength + 1] == 0 && (inData[inputOffset + payloadLength + 2] & 0xfc) == 128)
            {
                int s = inData[inputOffset + payloadLength + 4] >> 2 & 7;
                if(videoWidth != widths[s] || videoHeight != heights[s])
                {
                    videoWidth = widths[s];
                    videoHeight = heights[s];
                    super.outputFormat = new RGBFormat(new Dimension(videoWidth, videoHeight), videoWidth * videoHeight, int[].class, ivf.getFrameRate(), 32, 255, 65280, 0xff0000, 1, videoWidth, 0, -1);
                    outMaxLength = videoWidth * videoHeight;
                    if(FormatSizeInitFlag)
                        videoResized();
                }
                FormatSizeInitFlag = true;
            }
            if(!FormatSizeInitFlag)
                return 1;
        }
        int outData[] = validateIntArraySize(outputBuffer, outMaxLength);
        if(inLength + 8 + inputOffset > inData.length)
        {
            int newLength = inLength <= inMaxLength ? inMaxLength : inLength;
            byte tempArray[] = new byte[inputOffset + newLength + 8];
            System.arraycopy(inData, 0, tempArray, 0, inLength + inputOffset);
            inData = tempArray;
            inputBuffer.setData(tempArray);
        }
        inData[inputOffset + inLength] = 0;
        inData[inputOffset + inLength + 1] = 0;
        inData[inputOffset + inLength + 2] = -4;
        inLength += 3;
        inputBuffer.setLength(inLength);
        if(rtpData)
            inLength -= payloadLength;
        boolean ret = decodeData(inputBuffer, inLength, outputBuffer, rtpData);
        if(ret)
        {
            updateOutput(outputBuffer, super.outputFormat, outMaxLength, 0);
            return 0;
        } else
        {
            return 4;
        }
    }

    boolean decodeData(Buffer inputBuffer, int inputLength, Buffer outputBuffer, boolean rtpData)
    {
        int outData[] = (int[])outputBuffer.getData();
        byte inputData[] = (byte[])inputBuffer.getData();
        if(inputLength <= 0)
            return false;
        javaDecoder.initBitstream();
        int inputOffset = inputBuffer.getOffset();
        int ret;
        if(rtpData)
        {
            ret = javaDecoder.DecodeRtpPacket(inputData, inputOffset + payloadLength, inputLength, inputData, inputOffset, inputBuffer.getTimeStamp());
            if(ret == 3)
                return false;
        } else
        {
            ret = javaDecoder.DecodePicture(inputData, inputOffset, true);
        }
        if(ret == 2)
            throw new RuntimeException("Currently this picture format is not supported!");
        if(ret == 1)
        {
            int outWidth = super.outputFormat.getSize().width;
            int outHeight = super.outputFormat.getSize().height;
            outputFrame = javaDecoder.CurrentFrame;
            YCbCrToRGB.convert(outputFrame.Y, outputFrame.Cb, outputFrame.Cr, outData, outputFrame.width, outputFrame.height, outWidth, outHeight, 255, 4);
            return true;
        } else
        {
            return false;
        }
    }

    public static int getPayloadHeaderLength(byte input[], int offset)
    {
        int l = 0;
        byte b = input[offset];
        if((b & 0x80) != 0)
        {
            if((b & 0x40) != 0)
                l = 12;
            else
                l = 8;
        } else
        {
            l = 4;
        }
        return l;
    }

    public boolean checkFormat(Format format)
    {
        if(format.getEncoding().equals("h263/rtp"))
            return true;
        else
            return super.checkFormat(format);
    }

    private static final int rMask = 255;
    private static final int gMask = 65280;
    private static final int bMask = 0xff0000;
    private static final boolean DEBUG = false;
    private H263Decoder javaDecoder;
    private FrameBuffer outputFrame;
    public static final int widths[] = {
        0, 128, 176, 352, 704, 1408, 0, 0
    };
    public static final int heights[] = {
        0, 96, 144, 288, 576, 1152, 0, 0
    };
    private int videoWidth;
    private int videoHeight;
    private boolean FormatSizeInitFlag;
    private int payloadLength;
    static boolean nativeAvail = false;

    static 
    {
        if(BasicPlugIn.plugInExists("com.sun.media.codec.video.vh263.NativeDecoder", 2))
            try
            {
                JMFSecurityManager.loadLibrary("jmutil");
                JMFSecurityManager.loadLibrary("jmvh263");
                nativeAvail = true;
            }
            catch(Throwable t) { }
    }
}
