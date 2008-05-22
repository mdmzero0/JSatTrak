// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AVIMux.java

package com.sun.media.multiplexer.video;

import com.sun.media.BasicPlugIn;
import com.sun.media.format.AviVideoFormat;
import com.sun.media.format.WavAudioFormat;
import com.sun.media.multiplexer.BasicMux;
import com.sun.media.parser.BasicPullParser;
import com.sun.media.util.ByteBuffer;
import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.*;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

public class AVIMux extends BasicMux
{

    public AVIMux()
    {
        numberOfEoms = 0;
        width = 0;
        height = 0;
        usecPerFrame = -1;
        frameRate = -1F;
        flags = 16;
        totalDataLength = 0;
        totalFrames = 0;
        totalVideoFrames = 0;
        reserved = new int[4];
        chunkList = new Vector(1);
        bbuf = new ByteBuffer(16384);
        chunkOffset = 4;
        moviOffset = 0;
        avihOffset = 0;
        hdrlSizeOffset = 0;
        totalStrlLength = 0;
        blockAlign = 1;
        samplesPerBlock = -1;
        sampleRate = 0.0D;
        audioDuration = 0.0D;
        averageBytesPerSecond = -1;
        mp3BitRate = -1;
        cumulativeInterFrameTimeVideo = 0L;
        previousTimeStampVideo = 0L;
        littleEndian = new AudioFormat(null, -1D, -1, -1, 0, -1);
        signed = new AudioFormat(null, -1D, -1, -1, 0, 1);
        unsigned = new AudioFormat(null, -1D, -1, -1, 0, 0);
        super.supportedInputs = new Format[2];
        super.supportedInputs[0] = new AudioFormat(null);
        super.supportedInputs[1] = new VideoFormat(null);
        super.supportedOutputs = new ContentDescriptor[1];
        super.supportedOutputs[0] = new FileTypeDescriptor("video.x_msvideo");
        chunkList.addElement(bbuf);
    }

    private Format[] createRGBFormats(Dimension size)
    {
        int NS = -1;
        Format rgbFormats[] = {
            new RGBFormat(size, size.width * size.height * 2, Format.byteArray, NS, 16, 31744, 992, 31, 2, size.width * 2, 1, 1), new RGBFormat(size, size.width * size.height * 3, Format.byteArray, NS, 24, 3, 2, 1, 3, size.width * 3, 1, NS), new RGBFormat(size, size.width * size.height * 4, Format.byteArray, NS, 32, 3, 2, 1, 4, size.width * 4, 1, NS)
        };
        return rgbFormats;
    }

    private Format[] createYUVFormats(Dimension size)
    {
        int NS = -1;
        Format yuvFormats[] = {
            new YUVFormat(size, size.width * size.height * 2, Format.byteArray, NS, 32, size.width * 2, size.width * 2, 1, 0, 2), new YUVFormat(size, size.width * size.height * 2, Format.byteArray, NS, 32, size.width * 2, size.width * 2, 0, 1, 3), new YUVFormat(size, (size.width * size.height * 3) / 2, Format.byteArray, NS, 2, size.width, size.width / 2, 0, size.width * size.height, (size.width * size.height * 5) / 4), new YUVFormat(size, (size.width * size.height * 3) / 2, Format.byteArray, NS, 2, size.width, size.width / 2, 0, (size.width * size.height * 5) / 4, size.width * size.height)
        };
        return yuvFormats;
    }

    public String getName()
    {
        return "AVI Multiplexer";
    }

    public Format setInputFormat(Format input, int trackID)
    {
        String reason = null;
        if(input instanceof AudioFormat)
        {
            AudioFormat af = (AudioFormat)input;
            WavAudioFormat wavAudioFormat = null;
            if(input instanceof WavAudioFormat)
                wavAudioFormat = (WavAudioFormat)input;
            String encoding = af.getEncoding();
            if(encoding == null)
                return null;
            if(encoding.equalsIgnoreCase("LINEAR"))
                if(af.getSampleSizeInBits() > 8)
                {
                    if(af.getEndian() == 1)
                        return null;
                    if(af.getSigned() == 0)
                        return null;
                    if(af.getEndian() == -1 || af.getSigned() == -1)
                        input = (AudioFormat)af.intersects(signed);
                } else
                {
                    if(af.getSigned() == 1)
                        return null;
                    if(af.getEndian() == -1 || af.getSigned() == -1)
                        input = (AudioFormat)af.intersects(unsigned);
                }
            Integer formatTag = (Integer)WavAudioFormat.reverseFormatMapper.get(encoding.toLowerCase());
            if(formatTag == null || af.getEncoding().equalsIgnoreCase("truespeech") || af.getEncoding().toLowerCase().startsWith("voxware"))
            {
                reason = "Cannot handle format";
                return null;
            }
            short wFormatTag = formatTag.shortValue();
            switch(wFormatTag)
            {
            case 2: // '\002'
            case 17: // '\021'
            case 49: // '1'
                if(wavAudioFormat == null)
                {
                    reason = "A WavAudioFormat is required  to provide encoding specific information for this encoding " + wFormatTag;
                    return null;
                }
                break;
            }
        } else
        if(input instanceof VideoFormat)
        {
            VideoFormat vf = (VideoFormat)input;
            String encoding = vf.getEncoding();
            Dimension size = vf.getSize();
            if(size == null)
                size = new Dimension(320, 240);
            if(encoding == null)
                return null;
            if(encoding.equalsIgnoreCase("rgb"))
            {
                if(BasicPlugIn.matches(vf, createRGBFormats(size)) == null)
                    return null;
            } else
            if(encoding.equalsIgnoreCase("yuv"))
            {
                if(BasicPlugIn.matches(vf, createYUVFormats(size)) == null)
                    return null;
            } else
            {
                if(encoding.equalsIgnoreCase("jpeg"))
                    return null;
                if(encoding.length() > 4)
                    return null;
            }
            frameRate = vf.getFrameRate();
            if(frameRate > 0.0F)
                usecPerFrame = (int)((1.0F / frameRate) * 1000000F);
            avgFrameTime = usecPerFrame * 1000;
        } else
        {
            reason = "Can only support Audio and Video formats";
        }
        if(reason != null)
        {
            return null;
        } else
        {
            super.inputs[trackID] = input;
            return input;
        }
    }

    public int setNumTracks(int nTracks)
    {
        if(nTracks > 2)
            return 2;
        suggestedBufferSizeOffsets = new int[nTracks];
        suggestedBufferSizes = new int[nTracks];
        endOfMediaStatus = new boolean[nTracks];
        for(int i = 0; i < nTracks; i++)
        {
            suggestedBufferSizes[i] = -1;
            suggestedBufferSizeOffsets[i] = -1;
        }

        return super.setNumTracks(nTracks);
    }

    public synchronized int doProcess(Buffer buffer, int trackID)
    {
        if(buffer.isEOM())
        {
            numberOfEoms++;
            if(numberOfEoms >= super.numTracks)
                return super.doProcess(buffer, trackID);
            else
                return 0;
        }
        if(buffer.getData() == null)
            return 0;
        boolean isVideoFormat = buffer.getFormat() instanceof VideoFormat;
        if(isVideoFormat)
        {
            long timeStamp = buffer.getTimeStamp();
            if((double)(timeStamp - previousTimeStampVideo) > (double)avgFrameTime * 1.8999999999999999D)
            {
                int blankFrames = (int)((timeStamp - previousTimeStampVideo) / avgFrameTime);
                for(int i = 0; i < blankFrames; i++)
                {
                    Buffer blankBuffer = new Buffer();
                    blankBuffer.setTimeStamp(previousTimeStampVideo + (long)i * avgFrameTime);
                    blankBuffer.setFormat(buffer.getFormat());
                    blankBuffer.setDuration(avgFrameTime);
                    blankBuffer.setSequenceNumber(buffer.getSequenceNumber());
                    blankBuffer.setFlags(buffer.getFlags() & 0xffffffef);
                    int result = writeFrame(blankBuffer, trackID);
                    if(result != 0)
                        return result;
                }

            }
        }
        return writeFrame(buffer, trackID);
    }

    private int writeFrame(Buffer buffer, int trackID)
    {
        boolean isVideoFormat = buffer.getFormat() instanceof VideoFormat;
        int length = buffer.getLength();
        int pad;
        if((length & 1) > 0)
            pad = 1;
        else
            pad = 0;
        String aviEncodingMagic = getAviEncodingMagic(trackID, isVideoFormat);
        bufClear();
        bufWriteBytes(aviEncodingMagic);
        bufWriteIntLittleEndian(length + pad);
        bufFlush();
        if(length > 0)
            write((byte[])buffer.getData(), buffer.getOffset(), length);
        if(pad > 0)
        {
            bufClear();
            bufWriteByte((byte)0);
            bufFlush();
        }
        totalDataLength += length + pad;
        if(length > suggestedBufferSizes[trackID])
            suggestedBufferSizes[trackID] = length;
        if(bbuf.length == 16384)
        {
            bbuf = new ByteBuffer(16384);
            chunkList.addElement(bbuf);
        }
        bbuf.writeBytes(aviEncodingMagic);
        int flag = (buffer.getFlags() & 0x10) == 0 ? 0 : 16;
        bbuf.writeIntLittleEndian(flag);
        bbuf.writeIntLittleEndian(chunkOffset);
        bbuf.writeIntLittleEndian(length);
        chunkOffset += length + pad + 8;
        if(isVideoFormat)
        {
            long timeStamp = buffer.getTimeStamp();
            if(totalVideoFrames > 0)
                cumulativeInterFrameTimeVideo += timeStamp - previousTimeStampVideo;
            previousTimeStampVideo = timeStamp;
            totalVideoFrames++;
        } else
        if(samplesPerBlock != -1)
        {
            int numBlocks = length / blockAlign;
            int numSamples = numBlocks * samplesPerBlock;
            audioDuration += (double)numSamples / sampleRate;
        } else
        if(averageBytesPerSecond > 0)
            audioDuration += (double)length / (double)averageBytesPerSecond;
        totalFrames++;
        return 0;
    }

    protected void writeHeader()
    {
        for(int i = 0; i < super.inputs.length; i++)
            if(super.inputs[i] instanceof AudioFormat)
            {
                AudioFormat af = (AudioFormat)super.inputs[i];
                WavAudioFormat wavAudioFormat = null;
                sampleRate = af.getSampleRate();
                if(af.getEncoding().equalsIgnoreCase("LINEAR"))
                    samplesPerBlock = 1;
                if(super.inputs[i] instanceof WavAudioFormat)
                {
                    wavAudioFormat = (WavAudioFormat)super.inputs[i];
                    byte codecSpecificHeader[] = wavAudioFormat.getCodecSpecificHeader();
                    if(!af.getEncoding().equalsIgnoreCase("mpeglayer3") && codecSpecificHeader != null && codecSpecificHeader.length >= 2)
                        try
                        {
                            samplesPerBlock = BasicPullParser.parseShortFromArray(codecSpecificHeader, false);
                        }
                        catch(IOException e)
                        {
                            System.err.println("Unable to parse codecSpecificHeader");
                        }
                }
            }

        bufClear();
        bufWriteBytes("RIFF");
        bufSkip(4);
        bufWriteBytes("AVI ");
        bufWriteBytes("LIST");
        hdrlSizeOffset = super.filePointer;
        bufSkip(4);
        bufWriteBytes("hdrl");
        bufWriteBytes("avih");
        bufWriteIntLittleEndian(56);
        avihOffset = super.filePointer;
        bufSkip(56);
        scaleOffsets = new int[super.numTracks];
        for(int i = 0; i < super.numTracks; i++)
        {
            Format format = super.inputs[i];
            boolean isVideo = format instanceof VideoFormat;
            bufWriteBytes("LIST");
            byte codecSpecificHeader[] = null;
            int extraByteLength = 0;
            AviVideoFormat aviVideoFormat = null;
            WavAudioFormat wavAudioFormat = null;
            int planes = 1;
            int depth = 24;
            String yuvEncoding = null;
            String encoding = format.getEncoding();
            int wFormatTag = -1;
            if(isVideo)
            {
                int bytesInBitmap = 40;
                RGBFormat rgbFormat = null;
                if(format instanceof RGBFormat)
                    rgbFormat = (RGBFormat)format;
                else
                if(format instanceof YUVFormat)
                {
                    YUVFormat yuv = (YUVFormat)format;
                    if(yuv.getYuvType() == 32 && yuv.getStrideY() == yuv.getSize().width * 2 && yuv.getOffsetY() == 0 && yuv.getOffsetU() == 1 && yuv.getOffsetV() == 3)
                        yuvEncoding = "YUY2";
                    else
                    if(yuv.getYuvType() == 32 && yuv.getStrideY() == yuv.getSize().width * 2 && yuv.getOffsetY() == 1 && yuv.getOffsetU() == 0 && yuv.getOffsetV() == 2)
                        yuvEncoding = "UYVY";
                    else
                    if(yuv.getYuvType() == 32 && yuv.getStrideY() == yuv.getSize().width * 2 && yuv.getOffsetY() == 0 && yuv.getOffsetU() == 3 && yuv.getOffsetV() == 1)
                        yuvEncoding = "YVYU";
                    else
                    if(yuv.getYuvType() == 2 && yuv.getStrideY() == yuv.getSize().width && yuv.getStrideUV() == yuv.getSize().width / 2)
                        if(yuv.getOffsetU() < yuv.getOffsetV())
                            yuvEncoding = "I420";
                        else
                            yuvEncoding = "YV12";
                }
                if(format instanceof AviVideoFormat)
                    aviVideoFormat = (AviVideoFormat)format;
                if(aviVideoFormat != null)
                {
                    planes = aviVideoFormat.getPlanes();
                    depth = aviVideoFormat.getBitsPerPixel();
                    codecSpecificHeader = aviVideoFormat.getCodecSpecificHeader();
                } else
                if(rgbFormat != null)
                    depth = rgbFormat.getBitsPerPixel();
            } else
            {
                if(format instanceof WavAudioFormat)
                {
                    wavAudioFormat = (WavAudioFormat)format;
                    codecSpecificHeader = wavAudioFormat.getCodecSpecificHeader();
                }
                if(codecSpecificHeader == null)
                {
                    Integer formatTag = (Integer)WavAudioFormat.reverseFormatMapper.get(encoding.toLowerCase());
                    if(formatTag != null)
                    {
                        wFormatTag = formatTag.shortValue();
                        if(wFormatTag == 85)
                            extraByteLength = 12;
                    }
                }
            }
            if(extraByteLength <= 0 && codecSpecificHeader != null)
                extraByteLength = codecSpecificHeader.length;
            int strlLength = 0;
            if(isVideo)
            {
                strlLength = 116 + extraByteLength;
                bufWriteIntLittleEndian(strlLength);
            } else
            {
                if(extraByteLength > 0)
                    strlLength = 92 + extraByteLength + 2;
                else
                    strlLength = 92;
                bufWriteIntLittleEndian(strlLength);
            }
            totalStrlLength += strlLength;
            bufWriteBytes("strl");
            bufWriteBytes("strh");
            bufWriteIntLittleEndian(56);
            if(isVideo)
            {
                bufWriteBytes("vids");
                if(encoding.startsWith("rgb"))
                    encoding = "DIB ";
                else
                if(yuvEncoding != null)
                    encoding = yuvEncoding;
                bufWriteBytes(encoding);
            } else
            {
                bufWriteBytes("auds");
                bufWriteIntLittleEndian(0);
            }
            bufWriteIntLittleEndian(0);
            bufWriteIntLittleEndian(0);
            bufWriteIntLittleEndian(0);
            scaleOffsets[i] = super.filePointer;
            bufWriteIntLittleEndian(1);
            bufWriteIntLittleEndian(15);
            bufWriteIntLittleEndian(0);
            bufWriteIntLittleEndian(0);
            suggestedBufferSizeOffsets[i] = super.filePointer;
            bufWriteIntLittleEndian(0);
            bufWriteIntLittleEndian(10000);
            bufWriteIntLittleEndian(0);
            bufWriteIntLittleEndian(0);
            bufWriteIntLittleEndian(0);
            bufWriteBytes("strf");
            if(isVideo)
            {
                bufWriteIntLittleEndian(40 + extraByteLength);
                bufWriteIntLittleEndian(40 + extraByteLength);
                width = ((VideoFormat)format).getSize().width;
                height = ((VideoFormat)format).getSize().height;
                bufWriteIntLittleEndian(width);
                bufWriteIntLittleEndian(height);
                bufWriteShortLittleEndian((short)planes);
                bufWriteShortLittleEndian((short)depth);
                if(encoding.startsWith("DIB"))
                    bufWriteIntLittleEndian(0);
                else
                    bufWriteBytes(encoding);
                int biSizeImage = 0;
                int biXPelsPerMeter = 0;
                int biYPelsPerMeter = 0;
                int biClrUsed = 0;
                int biClrImportant = 0;
                if(aviVideoFormat != null)
                {
                    if(aviVideoFormat.getImageSize() != -1)
                        biSizeImage = aviVideoFormat.getImageSize();
                    if(aviVideoFormat.getXPelsPerMeter() != -1)
                        biXPelsPerMeter = aviVideoFormat.getXPelsPerMeter();
                    if(aviVideoFormat.getYPelsPerMeter() != -1)
                        biYPelsPerMeter = aviVideoFormat.getYPelsPerMeter();
                    if(aviVideoFormat.getClrUsed() != -1)
                        biClrUsed = aviVideoFormat.getClrUsed();
                    if(aviVideoFormat.getClrImportant() != -1)
                        biClrImportant = aviVideoFormat.getClrImportant();
                }
                bufWriteIntLittleEndian(biSizeImage);
                bufWriteIntLittleEndian(biXPelsPerMeter);
                bufWriteIntLittleEndian(biYPelsPerMeter);
                bufWriteIntLittleEndian(biClrUsed);
                bufWriteIntLittleEndian(biClrImportant);
            } else
            {
                AudioFormat audioFormat = (AudioFormat)format;
                if(extraByteLength > 0)
                    bufWriteIntLittleEndian(16 + extraByteLength + 2);
                else
                    bufWriteIntLittleEndian(16);
                if(encoding.equals("unknown"))
                    encoding = "LINEAR";
                Integer formatTag = (Integer)WavAudioFormat.reverseFormatMapper.get(encoding.toLowerCase());
                if(formatTag != null)
                {
                    bufWriteShortLittleEndian(formatTag.shortValue());
                    short numChannels = (short)audioFormat.getChannels();
                    bufWriteShortLittleEndian(numChannels);
                    bufWriteIntLittleEndian((int)audioFormat.getSampleRate());
                    short sampleSizeInBits = (short)audioFormat.getSampleSizeInBits();
                    if(wavAudioFormat != null)
                    {
                        averageBytesPerSecond = wavAudioFormat.getAverageBytesPerSecond();
                        if(formatTag.shortValue() == 85)
                            mp3BitRate = averageBytesPerSecond * 8;
                    } else
                    if(formatTag.shortValue() == 85)
                    {
                        int frameRate = (int)audioFormat.getFrameRate();
                        if(frameRate != -1)
                        {
                            averageBytesPerSecond = frameRate;
                            mp3BitRate = averageBytesPerSecond * 8;
                        } else
                        {
                            averageBytesPerSecond = (int)audioFormat.getSampleRate() * numChannels * (sampleSizeInBits / 8);
                        }
                    } else
                    {
                        averageBytesPerSecond = (int)audioFormat.getSampleRate() * numChannels * (sampleSizeInBits / 8);
                    }
                    bufWriteIntLittleEndian(averageBytesPerSecond);
                    blockAlign = audioFormat.getFrameSizeInBits() / 8;
                    if(blockAlign < 1)
                        blockAlign = (sampleSizeInBits * numChannels) / 8;
                    if(blockAlign == 0)
                        blockAlign = 1;
                    if(mp3BitRate > 0)
                        blockAlign = 1;
                    bufWriteShortLittleEndian((short)blockAlign);
                    bufWriteShortLittleEndian(sampleSizeInBits);
                }
            }
            if(extraByteLength > 0)
                if(!isVideo)
                {
                    if(codecSpecificHeader != null)
                    {
                        bufWriteShortLittleEndian((short)codecSpecificHeader.length);
                        bufWriteBytes(codecSpecificHeader);
                    } else
                    {
                        Integer formatTag = (Integer)WavAudioFormat.reverseFormatMapper.get(encoding.toLowerCase());
                        if(formatTag != null)
                        {
                            wFormatTag = formatTag.shortValue();
                            if(wFormatTag == 85)
                            {
                                AudioFormat af = (AudioFormat)super.inputs[i];
                                int frameRate = (int)af.getFrameRate();
                                int blockSize;
                                if(frameRate > 0)
                                {
                                    float temp = (72F * (float)frameRate * 8F) / 8000F;
                                    temp = (float)((double)temp * (8000D / af.getSampleRate()));
                                    blockSize = (int)temp;
                                } else
                                {
                                    blockSize = 417;
                                }
                                bufWriteShortLittleEndian((short)12);
                                bufWriteShortLittleEndian((short)1);
                                bufWriteIntLittleEndian(2);
                                bufWriteShortLittleEndian((short)blockSize);
                                bufWriteShortLittleEndian((short)1);
                                bufWriteShortLittleEndian((short)1393);
                            }
                        }
                    }
                } else
                {
                    bufWriteBytes(codecSpecificHeader);
                }
        }

        bufWriteBytes("LIST");
        moviOffset = super.filePointer;
        bufSkip(4);
        bufWriteBytes("movi");
        bufFlush();
        seek(hdrlSizeOffset);
        int hdrlSize = totalStrlLength + 56 + 4 * (3 + 2 * super.numTracks);
        bufClear();
        bufWriteIntLittleEndian(hdrlSize);
        bufFlush();
        seek(moviOffset + 8);
    }

    protected void writeFooter()
    {
        writeIDX1Chunk();
        writeAVIH();
        seek(moviOffset);
        bufClear();
        bufWriteIntLittleEndian(4 + totalDataLength + totalFrames * 8);
        bufFlush();
        for(int i = 0; i < super.numTracks; i++)
        {
            int offset = suggestedBufferSizeOffsets[i];
            if(offset > 0)
            {
                seek(offset);
                bufClear();
                bufWriteIntLittleEndian(suggestedBufferSizes[i]);
                bufFlush();
            }
            seek(scaleOffsets[i]);
            if(super.inputs[i] instanceof VideoFormat)
            {
                int rateVal = 10000;
                bufClear();
                bufWriteIntLittleEndian(usecPerFrame / 100);
                bufWriteIntLittleEndian(rateVal);
                bufWriteIntLittleEndian(0);
                bufWriteIntLittleEndian(totalVideoFrames);
                bufFlush();
            } else
            {
                AudioFormat audioFormat = (AudioFormat)super.inputs[i];
                if(mp3BitRate > 0)
                {
                    bufClear();
                    bufWriteIntLittleEndian(8);
                    bufFlush();
                    bufClear();
                    bufWriteIntLittleEndian(mp3BitRate);
                    bufFlush();
                    blockAlign = 1;
                } else
                {
                    bufClear();
                    bufWriteIntLittleEndian(blockAlign);
                    bufFlush();
                    int factor = 1;
                    if(samplesPerBlock > 0)
                        factor = samplesPerBlock;
                    int rate = (int)((audioFormat.getSampleRate() / (double)factor) * (double)blockAlign);
                    bufClear();
                    bufWriteIntLittleEndian(rate);
                    bufFlush();
                }
                seek(super.filePointer + 16);
                bufClear();
                bufWriteIntLittleEndian(blockAlign);
                bufFlush();
            }
        }

        seek(4);
        bufClear();
        bufWriteIntLittleEndian(super.fileSize - 8);
        bufFlush();
    }

    private void writeIDX1Chunk()
    {
        bufClear();
        bufWriteBytes("idx1");
        bufWriteIntLittleEndian(totalFrames * 16);
        bufFlush();
        for(int i = 0; i < chunkList.size(); i++)
        {
            ByteBuffer bbuf = (ByteBuffer)chunkList.elementAt(i);
            write(bbuf.buffer, 0, bbuf.length);
        }

    }

    private void writeAVIH()
    {
        int audioFrames = 0;
        if(totalVideoFrames <= 0)
        {
            usecPerFrame = 1000;
            audioFrames = (int)((audioDuration * 1000000D) / (double)usecPerFrame);
        } else
        {
            int computedUsecPerFrame = (int)((double)cumulativeInterFrameTimeVideo / (1000D * (double)(totalVideoFrames - 1)));
            usecPerFrame = computedUsecPerFrame;
        }
        seek(avihOffset);
        bufClear();
        bufWriteIntLittleEndian(usecPerFrame);
        bufWriteIntLittleEndian(maxBytesPerSecond);
        bufWriteIntLittleEndian(paddingGranularity);
        bufWriteIntLittleEndian(flags);
        if(totalVideoFrames > 0)
            bufWriteIntLittleEndian(totalVideoFrames);
        else
            bufWriteIntLittleEndian(audioFrames);
        bufWriteIntLittleEndian(initialFrames);
        bufWriteIntLittleEndian(super.numTracks);
        bufWriteIntLittleEndian(0);
        bufWriteIntLittleEndian(width);
        bufWriteIntLittleEndian(height);
        bufWriteIntLittleEndian(0);
        bufWriteIntLittleEndian(0);
        bufWriteIntLittleEndian(0);
        bufWriteIntLittleEndian(0);
        bufFlush();
    }

    private String getAviEncodingMagic(int streamNumber, boolean isVideoFormat)
    {
        String encoding = super.inputs[streamNumber].getEncoding().toLowerCase();
        String magic;
        if(isVideoFormat)
        {
            if(encoding.equalsIgnoreCase("cvid"))
                magic = "id";
            else
            if(encoding.startsWith("iv32"))
                magic = "32";
            else
            if(encoding.startsWith("iv31"))
                magic = "31";
            else
            if(encoding.startsWith("iv"))
                magic = "iv";
            else
                magic = "dc";
        } else
        {
            magic = "wb";
        }
        String streamPrefix = null;
        if(streamNumber == 0)
            streamPrefix = "00";
        else
        if(streamNumber == 1)
            streamPrefix = "01";
        else
        if(streamNumber == 2)
            streamPrefix = "02";
        else
        if(streamNumber == 3)
            streamPrefix = "03";
        else
        if(streamNumber == 4)
            streamPrefix = "04";
        return streamPrefix + magic;
    }

    private int suggestedBufferSizes[];
    private int suggestedBufferSizeOffsets[];
    private int scaleOffsets[];
    private boolean endOfMediaStatus[];
    private int numberOfEoms;
    private int width;
    private int height;
    private static final int MAX_FRAMES_STORED = 20000;
    private static final int AVIH_HEADER_LENGTH = 56;
    private static final int STRH_HEADER_LENGTH = 56;
    private static final int STRF_VIDEO_HEADER_LENGTH = 40;
    private static final int STRF_AUDIO_HEADER_LENGTH = 16;
    static final String AUDIO = "auds";
    static final String VIDEO = "vids";
    static final int AVIF_HASINDEX = 16;
    static final int AVIF_MUSTUSEINDEX = 32;
    static final int AVIF_ISINTERLEAVED = 256;
    static final int AVIF_WASCAPTUREFILE = 0x10000;
    static final int AVIF_COPYRIGHTED = 0x20000;
    static final int AVIF_KEYFRAME = 16;
    private int usecPerFrame;
    private float frameRate;
    private int maxBytesPerSecond;
    private int paddingGranularity;
    private long avgFrameTime;
    private int flags;
    private int totalDataLength;
    private int totalFrames;
    private int totalVideoFrames;
    private int initialFrames;
    private int reserved[];
    private Vector chunkList;
    private final int BUF_SIZE = 16384;
    private ByteBuffer bbuf;
    private int chunkOffset;
    private int moviOffset;
    private int avihOffset;
    private int hdrlSizeOffset;
    private int totalStrlLength;
    private int blockAlign;
    private int samplesPerBlock;
    private double sampleRate;
    private double audioDuration;
    private int averageBytesPerSecond;
    private int mp3BitRate;
    private long cumulativeInterFrameTimeVideo;
    private long previousTimeStampVideo;
    static final String LISTRECORDCHUNK = "rec ";
    static final String VIDEO_MAGIC = "dc";
    static final String VIDEO_MAGIC_JPEG = "db";
    static final String VIDEO_MAGIC_IV32a = "iv";
    static final String VIDEO_MAGIC_IV32b = "32";
    static final String VIDEO_MAGIC_IV31 = "31";
    static final String VIDEO_MAGIC_CVID = "id";
    static final String AUDIO_MAGIC = "wb";
    Format littleEndian;
    Format signed;
    Format unsigned;
}
