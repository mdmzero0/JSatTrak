// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   QuicktimeMux.java

package com.sun.media.multiplexer.video;

import com.sun.media.BasicPlugIn;
import com.sun.media.Log;
import com.sun.media.datasink.RandomAccess;
import com.sun.media.multiplexer.BasicMux;
import java.awt.Dimension;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.format.*;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

public class QuicktimeMux extends BasicMux
{
    private class AudioTrakInfo extends TrakInfo
    {

        AudioFormat audioFormat;
        final int IMA4_SAMPLES_PER_BLOCK = 64;
        final int GSM_SAMPLES_PER_BLOCK = 160;
        final int MAC3_SAMPLES_PER_BLOCK = 6;
        final int MAC6_SAMPLES_PER_BLOCK = 6;
        int samplesPerBlock;
        int numSamples;
        int frameSizeInBytes;
        final int MAX_SAMPLESPERCHUNK_NUMARRAYS = 1000;
        final int MAX_SAMPLESPERCHUNK_ARRAYSIZE = 1000;
        int numSamplesPerChunkArraysUsed;
        int samplesPerChunkIndex;
        int samplesPerChunkArray[][];
        int previousSamplesPerChunk;

        public AudioTrakInfo()
        {
            samplesPerBlock = 1;
            numSamples = 0;
            numSamplesPerChunkArraysUsed = 1;
            samplesPerChunkIndex = 0;
            previousSamplesPerChunk = -1;
            samplesPerChunkArray = new int[1000][];
            samplesPerChunkArray[0] = new int[1000];
            samplesPerChunkArray[0][0] = 1;
            samplesPerChunkArray[0][1] = -1;
        }
    }

    private class VideoTrakInfo extends TrakInfo
    {

        public String toString()
        {
            return super.toString() + " \n frameRate " + frameRate + " : frameDuration " + frameDuration;
        }

        VideoFormat videoFormat;
        float frameRate;
        int frameDuration;
        final int MAX_SAMPLE_SIZE_NUMARRAYS = 1000;
        final int MAX_SAMPLE_SIZE_ARRAYSIZE = 2000;
        int numSampleSizeArraysUsed;
        int sampleSizeIndex;
        int sampleSize[][];
        final int MAX_KEYFRAME_NUMARRAYS = 1000;
        final int MAX_KEYFRAME_ARRAYSIZE = 1000;
        int numKeyFrameArraysUsed;
        int keyFrameIndex;
        int keyFrames[][];
        final int MAX_TIMESTAMP_NUMARRAYS = 1000;
        final int MAX_TIMESTAMP_ARRAYSIZE = 1000;
        int numTimeStampArraysUsed;
        int timeStampIndex;
        long timeStamps[][];
        long minDuration;
        long maxDuration;
        long previousTimeStamp;

        public VideoTrakInfo()
        {
            numSampleSizeArraysUsed = 1;
            sampleSizeIndex = 0;
            numKeyFrameArraysUsed = 1;
            keyFrameIndex = 0;
            numTimeStampArraysUsed = 1;
            timeStampIndex = 0;
            minDuration = 0x7fffffffffffffffL;
            maxDuration = -1L;
            sampleSize = new int[1000][];
            sampleSize[0] = new int[2000];
            keyFrames = new int[1000][];
            keyFrames[0] = new int[1000];
            timeStamps = new long[1000][];
            timeStamps[0] = new long[1000];
        }
    }

    private class TrakInfo
    {

        public String toString()
        {
            if(!supported)
                System.out.println("No support for format " + format);
            return type + ": " + encoding + " : totalFrames " + totalFrames;
        }

        boolean initFormat;
        boolean supported;
        String type;
        String encoding;
        Format format;
        long tkhdDurationOffset;
        long mdhdDurationOffset;
        int totalFrames;
        int duration;
        boolean constantSampleSize;
        final int MAX_CHUNKOFFSETS_NUMARRAYS = 1000;
        final int MAX_CHUNKOFFSETS_ARRAYSIZE = 1000;
        int numChunkOffsetsArraysUsed;
        int chunkOffsetsIndex;
        int chunkOffsetsArray[][];
        int chunkOffsetOffset;

        public TrakInfo()
        {
            initFormat = false;
            supported = false;
            tkhdDurationOffset = -1L;
            mdhdDurationOffset = -1L;
            totalFrames = 0;
            constantSampleSize = true;
            numChunkOffsetsArraysUsed = 1;
            chunkOffsetsIndex = 0;
            chunkOffsetsArray = new int[1000][];
            chunkOffsetsArray[0] = new int[1000];
        }
    }


    public QuicktimeMux()
    {
        removeCount = 0;
        sourceConnected = false;
        sinkConnected = false;
        closed = false;
        opened = false;
        debugCounter = 0;
        dataSize = 0;
        numberOfEoms = 0;
        numberOfTracks = 0;
        numberOfSupportedTracks = 0;
        requireTwoPass = true;
        bigEndian = new AudioFormat(null, -1D, -1, -1, 1, -1);
        super.supportedInputs = new Format[2];
        super.supportedInputs[0] = new AudioFormat(null);
        super.supportedInputs[1] = new VideoFormat(null);
        super.supportedOutputs = new ContentDescriptor[1];
        super.supportedOutputs[0] = new FileTypeDescriptor("video.quicktime");
        int NS = -1;
        rgbFormats = (new Format[] {
            new RGBFormat(null, NS, Format.byteArray, NS, 16, 31744, 992, 31, 2, NS, 0, 0), new RGBFormat(null, NS, Format.byteArray, NS, 24, 1, 2, 3, 3, NS, 0, NS), new RGBFormat(null, NS, Format.byteArray, NS, 32, 2, 3, 4, 4, NS, 0, NS)
        });
        yuvFormats = (new Format[] {
            new YUVFormat(null, NS, Format.byteArray, NS, 96, NS, NS, 0, 1, 3)
        });
    }

    public String getName()
    {
        return "Quicktime Multiplexer";
    }

    public Format setInputFormat(Format input, int trackID)
    {
        if(trakInfoArray == null)
        {
            trakInfoArray = new TrakInfo[super.numTracks];
            endOfMediaStatus = new boolean[super.numTracks];
        }
        if(!(input instanceof VideoFormat) && !(input instanceof AudioFormat))
        {
            trakInfoArray[trackID] = new TrakInfo();
            trakInfoArray[trackID].format = input;
            trakInfoArray[trackID].supported = false;
            return input;
        }
        String encoding = input.getEncoding();
        if(input instanceof VideoFormat)
        {
            if(videoFourccMapper.get(encoding.toLowerCase()) == null)
                return null;
            if(encoding.equalsIgnoreCase("rgb") && BasicPlugIn.matches(input, rgbFormats) == null)
                return null;
            if(encoding.equalsIgnoreCase("yuv") && BasicPlugIn.matches(input, yuvFormats) == null)
                return null;
            VideoTrakInfo vti = new VideoTrakInfo();
            trakInfoArray[trackID] = vti;
            vti.supported = true;
            vti.type = "vide";
            vti.encoding = encoding;
            vti.format = input;
            vti.videoFormat = (VideoFormat)null;
        } else
        if(input instanceof AudioFormat)
        {
            if(encoding.equalsIgnoreCase("LINEAR"))
            {
                AudioFormat af = (AudioFormat)input;
                if(af.getSampleSizeInBits() > 8)
                {
                    if(af.getSigned() == 0)
                        return null;
                    if(af.getEndian() == 0)
                        return null;
                    if(af.getEndian() == -1)
                        input = af.intersects(bigEndian);
                }
            } else
            if(audioFourccMapper.get(encoding.toLowerCase()) == null)
                return null;
            AudioTrakInfo ati = new AudioTrakInfo();
            trakInfoArray[trackID] = ati;
            ati.supported = true;
            ati.type = "soun";
            ati.encoding = encoding;
            ati.format = input;
            ati.audioFormat = (AudioFormat)input;
            ati.frameSizeInBytes = ati.audioFormat.getFrameSizeInBits() / 8;
            if(ati.frameSizeInBytes <= 0)
                ati.frameSizeInBytes = (ati.audioFormat.getSampleSizeInBits() * ati.audioFormat.getChannels()) / 8;
            if(encoding.equalsIgnoreCase("ima4"))
                ati.samplesPerBlock = 64;
            else
            if(encoding.equalsIgnoreCase("gsm"))
                ati.samplesPerBlock = 160;
            else
            if(encoding.equalsIgnoreCase("MAC3"))
                ati.samplesPerBlock = 6;
            else
            if(encoding.equalsIgnoreCase("MAC6"))
                ati.samplesPerBlock = 6;
        }
        if(trakInfoArray[trackID].supported)
            numberOfSupportedTracks++;
        super.inputs[trackID] = input;
        return input;
    }

    public synchronized int doProcess(Buffer buffer, int trackID)
    {
        if(buffer.isEOM() && !endOfMediaStatus[trackID])
        {
            endOfMediaStatus[trackID] = true;
            numberOfEoms++;
            if(numberOfEoms == super.numTracks)
                return super.doProcess(buffer, trackID);
            else
                return 0;
        }
        if(!trakInfoArray[trackID].initFormat)
        {
            if(trakInfoArray[trackID] instanceof VideoTrakInfo)
            {
                VideoTrakInfo vti = (VideoTrakInfo)trakInfoArray[trackID];
                vti.videoFormat = (VideoFormat)buffer.getFormat();
                vti.frameRate = vti.videoFormat.getFrameRate();
                if(vti.frameRate > 0.0F)
                {
                    vti.frameDuration = (int)((double)((1.0F / vti.frameRate) * 60000F) + 0.5D);
                } else
                {
                    vti.frameRate = 15F;
                    vti.frameDuration = 4000;
                }
            }
            trakInfoArray[trackID].initFormat = true;
        }
        Object obj = buffer.getData();
        if(obj == null)
            return 1;
        byte data[] = (byte[])obj;
        if(data == null)
            return 1;
        int length = buffer.getLength();
        dataSize += length;
        TrakInfo trakInfo = trakInfoArray[trackID];
        write(data, 0, length);
        int chunkOffset = super.filePointer - length;
        int chunkOffsetsIndex = trakInfo.chunkOffsetsIndex++;
        int numChunkOffsetsArraysUsed = trakInfo.numChunkOffsetsArraysUsed;
        trakInfo.chunkOffsetsArray[numChunkOffsetsArraysUsed - 1][chunkOffsetsIndex] = chunkOffset;
        if(++chunkOffsetsIndex >= 1000)
        {
            trakInfo.chunkOffsetsIndex = 0;
            trakInfo.chunkOffsetsArray[numChunkOffsetsArraysUsed] = new int[1000];
            trakInfo.numChunkOffsetsArraysUsed++;
            if(++numChunkOffsetsArraysUsed >= 1000)
            {
                System.err.println("Cannot create quicktime file with more than " + 1000 * 1000 + " chunks ");
                return 1;
            }
        }
        String type = trakInfo.type;
        VideoTrakInfo vti = null;
        AudioTrakInfo ati = null;
        if(type.equals("vide"))
        {
            vti = (VideoTrakInfo)trakInfo;
            int sampleSizeIndex = vti.sampleSizeIndex++;
            int numSampleSizeArraysUsed = vti.numSampleSizeArraysUsed;
            vti.sampleSize[numSampleSizeArraysUsed - 1][sampleSizeIndex] = length;
            if(((TrakInfo) (vti)).constantSampleSize && length != vti.sampleSize[0][0])
                vti.constantSampleSize = false;
            if(vti.minDuration >= 0L)
            {
                long timeStamp = buffer.getTimeStamp();
                if(timeStamp <= -1L)
                    vti.minDuration = -1L;
                else
                if(((TrakInfo) (vti)).totalFrames > 0)
                {
                    long durationOfBufferData = timeStamp - vti.previousTimeStamp;
                    if(durationOfBufferData < vti.minDuration)
                        vti.minDuration = durationOfBufferData;
                    else
                    if(durationOfBufferData > vti.maxDuration)
                        vti.maxDuration = durationOfBufferData;
                    int timeStampIndex = vti.timeStampIndex++;
                    int numTimeStampArraysUsed = vti.numTimeStampArraysUsed;
                    vti.timeStamps[numTimeStampArraysUsed - 1][timeStampIndex] = durationOfBufferData;
                    if(++timeStampIndex >= 1000)
                    {
                        vti.timeStampIndex = 0;
                        vti.timeStamps[numTimeStampArraysUsed] = new long[1000];
                        vti.numTimeStampArraysUsed++;
                        if(++numTimeStampArraysUsed >= 1000)
                        {
                            System.err.println("Cannot create quicktime file with more than " + 1000 * 1000 + " frames ");
                            return 1;
                        }
                    }
                }
                vti.previousTimeStamp = timeStamp;
            }
            if(++sampleSizeIndex >= 2000)
            {
                vti.sampleSizeIndex = 0;
                vti.sampleSize[numSampleSizeArraysUsed] = new int[2000];
                vti.numSampleSizeArraysUsed++;
                if(++numSampleSizeArraysUsed >= 1000)
                {
                    System.err.println("Cannot create quicktime file with more than " + 1000 * 2000 + " samples ");
                    return 1;
                }
            }
            boolean keyframe = (buffer.getFlags() & 0x10) > 0;
            if(keyframe)
            {
                int keyFrameIndex = vti.keyFrameIndex++;
                int numKeyFrameArraysUsed = vti.numKeyFrameArraysUsed;
                vti.keyFrames[numKeyFrameArraysUsed - 1][keyFrameIndex] = ((TrakInfo) (vti)).totalFrames + 1;
                if(++keyFrameIndex >= 1000)
                {
                    vti.keyFrameIndex = 0;
                    vti.keyFrames[numKeyFrameArraysUsed] = new int[1000];
                    vti.numKeyFrameArraysUsed++;
                    if(++numKeyFrameArraysUsed >= 1000)
                    {
                        System.err.println("Cannot create quicktime file with more than " + 1000 * 1000 + " keyframes ");
                        return 1;
                    }
                }
            }
        } else
        {
            ati = (AudioTrakInfo)trakInfo;
            int samplesPerChunk = (length / ati.frameSizeInBytes) * ati.samplesPerBlock;
            ati.numSamples += samplesPerChunk;
            if(ati.previousSamplesPerChunk != samplesPerChunk)
            {
                int samplesPerChunkIndex = ati.samplesPerChunkIndex;
                int numSamplesPerChunkArraysUsed = ati.numSamplesPerChunkArraysUsed;
                ati.samplesPerChunkArray[numSamplesPerChunkArraysUsed - 1][samplesPerChunkIndex] = trakInfo.totalFrames + 1;
                samplesPerChunkIndex++;
                ati.samplesPerChunkArray[numSamplesPerChunkArraysUsed - 1][samplesPerChunkIndex] = samplesPerChunk;
                samplesPerChunkIndex++;
                ati.samplesPerChunkIndex = samplesPerChunkIndex;
                ati.previousSamplesPerChunk = samplesPerChunk;
                if(++samplesPerChunkIndex >= 1000)
                {
                    ati.samplesPerChunkIndex = 0;
                    ati.samplesPerChunkArray[numSamplesPerChunkArraysUsed] = new int[1000];
                    ati.numSamplesPerChunkArraysUsed++;
                    if(++numSamplesPerChunkArraysUsed >= 1000)
                    {
                        System.err.println("Cannot create quicktime file with more than " + 1000 * 1000 + " chunks ");
                        return 1;
                    }
                }
            }
        }
        trakInfo.totalFrames++;
        return 0;
    }

    protected void writeHeader()
    {
        mdatOffset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("mdat");
        bufFlush();
        dataSize = 0;
    }

    public boolean requireTwoPass()
    {
        return requireTwoPass;
    }

    protected void writeFooter()
    {
        moovOffset = super.filePointer;
        seek((int)mdatOffset);
        bufClear();
        mdatLength = 8 + dataSize;
        bufWriteInt(mdatLength);
        bufFlush();
        seek((int)moovOffset);
        writeMOOV();
        int maxTrackDuration = -1;
        for(int i = 0; i < super.numTracks; i++)
            if(trakInfoArray[i].supported)
            {
                writeSize(trakInfoArray[i].tkhdDurationOffset, trakInfoArray[i].duration);
                if(trakInfoArray[i].type.equals("vide"))
                    writeSize(trakInfoArray[i].mdhdDurationOffset, trakInfoArray[i].duration);
                if(trakInfoArray[i].duration > maxTrackDuration)
                    maxTrackDuration = trakInfoArray[i].duration;
            }

        writeSize(mvhdDurationOffset, maxTrackDuration);
        if(requireTwoPass && super.sth != null && (super.sth instanceof RandomAccess))
        {
            RandomAccess st;
            if((st = (RandomAccess)super.sth).write(-1L, moovLength + mdatLength))
            {
                updateSTCO();
                write(null, 0, -1);
                st.write(moovOffset, moovLength);
                st.write(mdatOffset, mdatLength);
            } else
            {
                System.err.println("No space to write streamable file");
            }
            st.write(-1L, -1);
        }
    }

    private int writeMOOV()
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("moov");
        bufFlush();
        int size = 8;
        size += writeMVHD();
        for(int i = 0; i < super.numTracks; i++)
            if(trakInfoArray[i].supported)
                size += writeTRAK(i, trakInfoArray[i].type);

        moovLength = size;
        return writeSize(offset, size);
    }

    private int writeMVHD()
    {
        bufClear();
        bufWriteInt(108);
        bufWriteBytes("mvhd");
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(60000);
        mvhdDurationOffset = super.filePointer;
        bufWriteInt(0);
        bufWriteInt(0x10000);
        bufWriteShort((short)255);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteShort((short)0);
        bufFlush();
        writeMatrix();
        bufClear();
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(numberOfSupportedTracks + 1);
        bufFlush();
        return 108;
    }

    private int writeSize(long offset, int size)
    {
        long currentOffset = super.filePointer;
        seek((int)offset);
        bufClear();
        bufWriteInt(size);
        bufFlush();
        seek((int)currentOffset);
        return size;
    }

    private int writeTRAK(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("trak");
        bufFlush();
        int size = 8;
        size += writeTKHD(streamNumber, type);
        size += writeMDIA(streamNumber, type);
        return writeSize(offset, size);
    }

    private int writeTKHD(int streamNumber, String type)
    {
        int width = 0;
        int height = 0;
        int duration = 0;
        int volume = 0;
        if(type.equals("vide"))
        {
            VideoTrakInfo videoTrakInfo = (VideoTrakInfo)trakInfoArray[streamNumber];
            Dimension size = null;
            VideoFormat vf = videoTrakInfo.videoFormat;
            if(vf != null)
                size = vf.getSize();
            if(size != null)
            {
                width = size.width;
                height = size.height;
            }
            duration = ((TrakInfo) (videoTrakInfo)).duration;
        } else
        {
            AudioTrakInfo audioTrakInfo = (AudioTrakInfo)trakInfoArray[streamNumber];
            float sampleRate = (int)audioTrakInfo.audioFormat.getSampleRate();
            float epsilon = 0.01F;
            duration = (int)(((float)audioTrakInfo.numSamples / sampleRate) * 60000F + epsilon);
            audioTrakInfo.duration = duration;
            volume = 255;
        }
        bufClear();
        bufWriteInt(92);
        bufWriteBytes("tkhd");
        bufWriteInt(3);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(streamNumber + 1);
        bufWriteInt(0);
        trakInfoArray[streamNumber].tkhdDurationOffset = super.filePointer;
        bufWriteInt(duration);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteShort((short)0);
        bufWriteShort((short)0);
        bufWriteShort((short)volume);
        bufWriteShort((short)0);
        bufFlush();
        writeMatrix();
        bufClear();
        bufWriteInt(width * 0x10000);
        bufWriteInt(height * 0x10000);
        bufFlush();
        return 92;
    }

    private int writeMDIA(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("mdia");
        bufFlush();
        int size = 8;
        size += writeMDHD(streamNumber, type);
        size += writeMhlrHdlr(streamNumber, type);
        size += writeMINF(streamNumber, type);
        return writeSize(offset, size);
    }

    private void writeMatrix()
    {
        bufClear();
        bufWriteInt(0x10000);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0x10000);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(0x40000000);
        bufFlush();
    }

    private int writeMDHD(int streamNumber, String type)
    {
        int timeScale = 0;
        int duration = 0;
        if(type.equals("vide"))
        {
            timeScale = 60000;
            VideoTrakInfo videoTrakInfo = (VideoTrakInfo)trakInfoArray[streamNumber];
            duration = ((TrakInfo) (videoTrakInfo)).duration;
        } else
        {
            AudioTrakInfo audioTrakInfo = (AudioTrakInfo)trakInfoArray[streamNumber];
            timeScale = (int)audioTrakInfo.audioFormat.getSampleRate();
            duration = audioTrakInfo.numSamples;
        }
        bufClear();
        bufWriteInt(32);
        bufWriteBytes("mdhd");
        bufWriteInt(1);
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteInt(timeScale);
        trakInfoArray[streamNumber].mdhdDurationOffset = super.filePointer;
        bufWriteInt(duration);
        bufWriteShort((short)0);
        bufWriteShort((short)0);
        bufFlush();
        return 32;
    }

    private int writeMINF(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("minf");
        bufFlush();
        int size = 8;
        if(type.equals("vide"))
            size += writeVMHD(streamNumber, type);
        else
            size += writeSMHD(streamNumber, type);
        size += writeDHlrHdlr(streamNumber, type);
        size += writeDINF(streamNumber, type);
        size += writeSTBL(streamNumber, type);
        return writeSize(offset, size);
    }

    private int writeVMHD(int streamNumber, String type)
    {
        bufClear();
        bufWriteInt(20);
        bufWriteBytes("vmhd");
        bufWriteInt(1);
        bufWriteShort((short)64);
        bufWriteShort((short)-32768);
        bufWriteShort((short)-32768);
        bufWriteShort((short)-32768);
        bufFlush();
        return 20;
    }

    private int writeSMHD(int streamNumber, String type)
    {
        bufClear();
        bufWriteInt(16);
        bufWriteBytes("smhd");
        bufWriteInt(0);
        bufWriteShort((short)0);
        bufWriteShort((short)0);
        bufFlush();
        return 16;
    }

    private int writeMhlrHdlr(int streamNumber, String type)
    {
        bufClear();
        bufWriteInt(36);
        bufWriteBytes("hdlr");
        bufWriteInt(0);
        bufWriteBytes("mhlr");
        bufWriteBytes(type);
        bufWriteBytes("    ");
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteBytes("    ");
        bufFlush();
        return 36;
    }

    private int writeDHlrHdlr(int streamNumber, String type)
    {
        bufClear();
        bufWriteInt(36);
        bufWriteBytes("hdlr");
        bufWriteInt(0);
        bufWriteBytes("dhlr");
        bufWriteBytes("alis");
        bufWriteBytes("    ");
        bufWriteInt(0);
        bufWriteInt(0);
        bufWriteBytes("    ");
        bufFlush();
        return 36;
    }

    private int writeDINF(int streamNumber, String type)
    {
        bufClear();
        bufWriteInt(36);
        bufWriteBytes("dinf");
        bufWriteInt(28);
        bufWriteBytes("dref");
        bufWriteInt(0);
        bufWriteInt(1);
        bufWriteInt(12);
        bufWriteBytes("alis");
        bufWriteInt(1);
        bufFlush();
        return 36;
    }

    private int writeSTBL(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("stbl");
        bufFlush();
        int size = 8;
        size += writeSTSD(streamNumber, type);
        size += writeSTTS(streamNumber, type);
        size += writeSTSS(streamNumber, type);
        size += writeSTSC(streamNumber, type);
        size += writeSTSZ(streamNumber, type);
        size += writeSTCO(streamNumber, type);
        return writeSize(offset, size);
    }

    private int writeSTSD(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("stsd");
        int size = 8;
        bufWriteInt(0);
        bufWriteInt(1);
        bufFlush();
        size += 8;
        if(type.equals("vide"))
            size += writeVideoSampleDescription(streamNumber, type);
        else
            size += writeAudioSampleDescription(streamNumber, type);
        return writeSize(offset, size);
    }

    private int writeVideoSampleDescription(int streamNumber, String type)
    {
        VideoTrakInfo videoTrakInfo = (VideoTrakInfo)trakInfoArray[streamNumber];
        int width = videoTrakInfo.videoFormat.getSize().width;
        int height = videoTrakInfo.videoFormat.getSize().height;
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        int size = 4;
        String encoding = ((TrakInfo) (videoTrakInfo)).encoding;
        String fourcc = null;
        int bitsPerPixel;
        if(encoding.equalsIgnoreCase("rgb"))
        {
            RGBFormat rgbFormat = (RGBFormat)((TrakInfo) (videoTrakInfo)).format;
            bitsPerPixel = rgbFormat.getBitsPerPixel();
            fourcc = "raw ";
        } else
        {
            fourcc = (String)videoFourccMapper.get(encoding.toLowerCase());
            bitsPerPixel = 24;
        }
        bufWriteBytes(fourcc);
        size += 4;
        bufWriteInt(0);
        bufWriteShort((short)0);
        size += 6;
        bufWriteShort((short)1);
        size += 2;
        bufWriteShort((short)0);
        bufWriteShort((short)0);
        bufWriteBytes("appl");
        bufWriteInt(1023);
        bufWriteInt(1023);
        bufWriteShort((short)width);
        bufWriteShort((short)height);
        bufWriteInt(0x480000);
        bufWriteInt(0x480000);
        bufWriteInt(0);
        bufWriteShort((short)1);
        bufWriteBytes(fourcc);
        bufWriteBytes("                            ");
        bufWriteShort((short)bitsPerPixel);
        bufWriteShort((short)-1);
        bufFlush();
        size += 70;
        return writeSize(offset, size);
    }

    private int writeAudioSampleDescription(int streamNumber, String type)
    {
        AudioTrakInfo audioTrakInfo = (AudioTrakInfo)trakInfoArray[streamNumber];
        AudioFormat audioFormat = audioTrakInfo.audioFormat;
        int channels = audioFormat.getChannels();
        int sampleSizeInBits = audioFormat.getSampleSizeInBits();
        int sampleRate = (int)audioFormat.getSampleRate();
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        int size = 4;
        String encoding = ((TrakInfo) (audioTrakInfo)).encoding;
        String fourcc;
        if(encoding.equalsIgnoreCase("LINEAR"))
        {
            if(sampleSizeInBits == 8 && audioFormat.getSigned() == 0)
                fourcc = "raw ";
            else
                fourcc = "twos";
        } else
        {
            fourcc = (String)audioFourccMapper.get(encoding.toLowerCase());
        }
        bufWriteBytes(fourcc);
        size += 4;
        bufWriteInt(0);
        bufWriteShort((short)0);
        size += 6;
        bufWriteShort((short)1);
        size += 2;
        bufWriteShort((short)0);
        bufWriteShort((short)0);
        bufWriteInt(0);
        bufWriteShort((short)channels);
        bufWriteShort((short)sampleSizeInBits);
        bufWriteShort((short)0);
        bufWriteShort((short)0);
        bufWriteInt(sampleRate * 0x10000);
        bufFlush();
        size += 20;
        return writeSize(offset, size);
    }

    private int writeSTTS(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("stts");
        int size = 8;
        bufWriteInt(0);
        size += 4;
        if(type.equals("vide"))
        {
            VideoTrakInfo vti = (VideoTrakInfo)trakInfoArray[streamNumber];
            if(vti.minDuration <= -1L || vti.maxDuration - vti.minDuration < 0xf4240L)
            {
                bufWriteInt(1);
                size += 4;
                bufWriteInt(((TrakInfo) (vti)).totalFrames);
                bufWriteInt(vti.frameDuration);
                vti.duration = ((TrakInfo) (vti)).totalFrames * vti.frameDuration;
                size += 8;
            } else
            {
                bufWriteInt(((TrakInfo) (vti)).totalFrames);
                size += 4;
                vti.duration = 0;
                long timeStamps[][] = vti.timeStamps;
                int indexi = 0;
                int indexj = 0;
                long timeStamp = 0L;
                int numEntries = ((TrakInfo) (vti)).totalFrames - 1;
                int bytesPerLoop = 8;
                int actualBufSize = ((super.maxBufSize - 200) / bytesPerLoop) * bytesPerLoop;
                int requiredSize = numEntries * bytesPerLoop;
                int numLoops;
                int entriesPerLoop;
                if(requiredSize <= actualBufSize)
                {
                    numLoops = 1;
                    entriesPerLoop = numEntries;
                } else
                {
                    numLoops = requiredSize / actualBufSize;
                    if((float)requiredSize / (float)actualBufSize > (float)numLoops)
                        numLoops++;
                    entriesPerLoop = actualBufSize / bytesPerLoop;
                }
                for(int ii = 0; ii < numLoops; ii++)
                {
                    for(int jj = 0; jj < entriesPerLoop; jj++)
                    {
                        bufWriteInt(1);
                        timeStamp = timeStamps[indexi][indexj++];
                        int dur = (int)(0.5D + ((double)timeStamp / 1000000000D) * 60000D);
                        bufWriteInt(dur);
                        vti.duration += dur;
                        size += 8;
                        if(indexj >= 1000)
                        {
                            indexi++;
                            indexj = 0;
                        }
                    }

                    bufFlush();
                    bufClear();
                    if(ii == numLoops - 2)
                        entriesPerLoop = numEntries - (numLoops - 1) * entriesPerLoop;
                }

                if(((TrakInfo) (vti)).totalFrames > 1)
                {
                    bufWriteInt(1);
                    int dur = (int)(((double)timeStamp / 1000000000D) * 60000D);
                    bufWriteInt(dur);
                    size += 8;
                    vti.duration += dur;
                }
            }
            for(int i = 0; i < vti.numTimeStampArraysUsed; i++)
                vti.timeStamps[i] = null;

        } else
        {
            AudioTrakInfo ati = (AudioTrakInfo)trakInfoArray[streamNumber];
            bufWriteInt(1);
            size += 4;
            bufWriteInt(ati.numSamples);
            bufWriteInt(1);
            size += 8;
        }
        if(super.bufLength > 0)
            bufFlush();
        return writeSize(offset, size);
    }

    private int writeSTSS(int streamNumber, String type)
    {
        if(!type.equals("vide"))
            return 0;
        VideoTrakInfo vti = (VideoTrakInfo)trakInfoArray[streamNumber];
        int numKeyFrameArraysUsed = vti.numKeyFrameArraysUsed;
        int numKeyFrames = (numKeyFrameArraysUsed - 1) * 1000 + vti.keyFrameIndex;
        if(numKeyFrames == 0)
        {
            Log.warning("Error: There should be atleast 1 keyframe in the track. All frames are now treated as keyframes");
            return 0;
        }
        if(numKeyFrames == ((TrakInfo) (vti)).totalFrames)
        {
            for(int i = 0; i < numKeyFrameArraysUsed; i++)
                vti.keyFrames[i] = null;

            return 0;
        }
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("stss");
        int size = 8;
        bufWriteInt(0);
        size += 4;
        int keyFrames[][] = vti.keyFrames;
        bufWriteInt(numKeyFrames);
        size += 4;
        int numEntries = numKeyFrames;
        int bytesPerLoop = 4;
        int actualBufSize = ((super.maxBufSize - 200) / bytesPerLoop) * bytesPerLoop;
        int requiredSize = numEntries * bytesPerLoop;
        int numLoops;
        int entriesPerLoop;
        if(requiredSize <= actualBufSize)
        {
            numLoops = 1;
            entriesPerLoop = numEntries;
        } else
        {
            numLoops = requiredSize / actualBufSize;
            if((float)requiredSize / (float)actualBufSize > (float)numLoops)
                numLoops++;
            entriesPerLoop = actualBufSize / bytesPerLoop;
        }
        int indexi = 0;
        int indexj = 0;
        for(int ii = 0; ii < numLoops; ii++)
        {
            for(int jj = 0; jj < entriesPerLoop; jj++)
            {
                bufWriteInt(keyFrames[indexi][indexj++]);
                if(indexj >= 1000)
                {
                    indexi++;
                    indexj = 0;
                }
            }

            bufFlush();
            bufClear();
            if(ii == numLoops - 2)
                entriesPerLoop = numEntries - (numLoops - 1) * entriesPerLoop;
        }

        size += numKeyFrames * 4;
        return writeSize(offset, size);
    }

    private int writeSTSC(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("stsc");
        int size = 8;
        bufWriteInt(0);
        size += 4;
        if(type.equals("vide"))
        {
            VideoTrakInfo vti = (VideoTrakInfo)trakInfoArray[streamNumber];
            bufWriteInt(1);
            size += 4;
            bufWriteInt(1);
            bufWriteInt(1);
            bufWriteInt(1);
            size += 12;
        } else
        {
            AudioTrakInfo ati = (AudioTrakInfo)trakInfoArray[streamNumber];
            int numberOfEntries = ((ati.numSamplesPerChunkArraysUsed - 1) * 1000 + ati.samplesPerChunkIndex) / 2;
            bufWriteInt(numberOfEntries);
            size += 4;
            int numEntries = numberOfEntries;
            int bytesPerLoop = 12;
            int actualBufSize = ((super.maxBufSize - 200) / bytesPerLoop) * bytesPerLoop;
            int requiredSize = numEntries * bytesPerLoop;
            int numLoops;
            int entriesPerLoop;
            if(requiredSize <= actualBufSize)
            {
                numLoops = 1;
                entriesPerLoop = numEntries;
            } else
            {
                numLoops = requiredSize / actualBufSize;
                if((float)requiredSize / (float)actualBufSize > (float)numLoops)
                    numLoops++;
                entriesPerLoop = actualBufSize / bytesPerLoop;
            }
            int indexi = 0;
            int indexj = 0;
            int samplesPerChunkArray[][] = ati.samplesPerChunkArray;
            for(int ii = 0; ii < numLoops; ii++)
            {
                for(int jj = 0; jj < entriesPerLoop; jj++)
                {
                    bufWriteInt(samplesPerChunkArray[indexi][indexj++]);
                    bufWriteInt(samplesPerChunkArray[indexi][indexj++]);
                    bufWriteInt(1);
                    if(indexj >= 1000)
                    {
                        indexi++;
                        indexj = 0;
                    }
                }

                bufFlush();
                bufClear();
                if(ii == numLoops - 2)
                    entriesPerLoop = numEntries - (numLoops - 1) * entriesPerLoop;
            }

            size += numberOfEntries * 12;
        }
        if(super.bufLength > 0)
            bufFlush();
        return writeSize(offset, size);
    }

    private int writeSTSZ(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("stsz");
        int size = 8;
        bufWriteInt(0);
        size += 4;
        TrakInfo trakInfo = trakInfoArray[streamNumber];
        if(type.equals("soun"))
        {
            bufWriteInt(1);
            bufWriteInt(((AudioTrakInfo)trakInfo).numSamples);
            size += 8;
        } else
        if(type.equals("vide"))
        {
            VideoTrakInfo vti = (VideoTrakInfo)trakInfo;
            int numSampleSizeArraysUsed = vti.numSampleSizeArraysUsed;
            int numberOfEntries = trakInfo.totalFrames;
            if(trakInfo.constantSampleSize)
            {
                int sampleSize = vti.sampleSize[0][0];
                bufWriteInt(sampleSize);
                bufWriteInt(numberOfEntries);
                size += 8;
            } else
            {
                int sampleSize[][] = vti.sampleSize;
                bufWriteInt(0);
                bufWriteInt(numberOfEntries);
                size += 8;
                int numEntries = numberOfEntries;
                int bytesPerLoop = 4;
                int actualBufSize = ((super.maxBufSize - 200) / bytesPerLoop) * bytesPerLoop;
                int requiredSize = numEntries * bytesPerLoop;
                int numLoops;
                int entriesPerLoop;
                if(requiredSize <= actualBufSize)
                {
                    numLoops = 1;
                    entriesPerLoop = numEntries;
                } else
                {
                    numLoops = requiredSize / actualBufSize;
                    if((float)requiredSize / (float)actualBufSize > (float)numLoops)
                        numLoops++;
                    entriesPerLoop = actualBufSize / bytesPerLoop;
                }
                int indexi = 0;
                int indexj = 0;
                for(int ii = 0; ii < numLoops; ii++)
                {
                    for(int jj = 0; jj < entriesPerLoop; jj++)
                    {
                        bufWriteInt(sampleSize[indexi][indexj++]);
                        if(indexj >= 2000)
                        {
                            indexi++;
                            indexj = 0;
                        }
                    }

                    bufFlush();
                    bufClear();
                    if(ii == numLoops - 2)
                        entriesPerLoop = numEntries - (numLoops - 1) * entriesPerLoop;
                }

                size += numberOfEntries * 4;
            }
            for(int i = 0; i < numSampleSizeArraysUsed; i++)
                vti.sampleSize[i] = null;

        }
        if(super.bufLength > 0)
            bufFlush();
        return writeSize(offset, size);
    }

    private int writeSTCO(int streamNumber, String type)
    {
        long offset = super.filePointer;
        bufClear();
        bufWriteInt(0);
        bufWriteBytes("stco");
        int size = 8;
        bufWriteInt(0);
        size += 4;
        TrakInfo trakInfo = trakInfoArray[streamNumber];
        int numChunkOffsetsArraysUsed = trakInfo.numChunkOffsetsArraysUsed;
        int chunkOffsetsArray[][] = trakInfo.chunkOffsetsArray;
        bufWriteInt(trakInfo.totalFrames);
        size += 4;
        int numEntries = trakInfo.totalFrames;
        int bytesPerLoop = 4;
        int actualBufSize = ((super.maxBufSize - 200) / bytesPerLoop) * bytesPerLoop;
        int requiredSize = numEntries * bytesPerLoop;
        int numLoops;
        int entriesPerLoop;
        if(requiredSize <= actualBufSize)
        {
            numLoops = 1;
            entriesPerLoop = numEntries;
        } else
        {
            numLoops = requiredSize / actualBufSize;
            if((float)requiredSize / (float)actualBufSize > (float)numLoops)
                numLoops++;
            entriesPerLoop = actualBufSize / bytesPerLoop;
        }
        int indexi = 0;
        int indexj = 0;
        trakInfo.chunkOffsetOffset = super.filePointer;
        for(int ii = 0; ii < numLoops; ii++)
        {
            for(int jj = 0; jj < entriesPerLoop; jj++)
            {
                int off = (int)((long)chunkOffsetsArray[indexi][indexj++] - mdatOffset);
                bufWriteInt(off);
                if(indexj >= 1000)
                {
                    indexi++;
                    indexj = 0;
                }
            }

            bufFlush();
            bufClear();
            if(ii == numLoops - 2)
                entriesPerLoop = numEntries - (numLoops - 1) * entriesPerLoop;
        }

        size += trakInfo.totalFrames * 4;
        return writeSize(offset, size);
    }

    private void updateSTCO()
    {
        for(int streamNumber = 0; streamNumber < trakInfoArray.length; streamNumber++)
        {
            TrakInfo trakInfo = trakInfoArray[streamNumber];
            int numChunkOffsetsArraysUsed = trakInfo.numChunkOffsetsArraysUsed;
            int chunkOffsetsArray[][] = trakInfo.chunkOffsetsArray;
            int chunkOffsetOffset = trakInfo.chunkOffsetOffset;
            seek(chunkOffsetOffset);
            bufClear();
            int numEntries = trakInfo.totalFrames;
            int bytesPerLoop = 4;
            int actualBufSize = ((super.maxBufSize - 200) / bytesPerLoop) * bytesPerLoop;
            int requiredSize = numEntries * bytesPerLoop;
            int numLoops;
            int entriesPerLoop;
            if(requiredSize <= actualBufSize)
            {
                numLoops = 1;
                entriesPerLoop = numEntries;
            } else
            {
                numLoops = requiredSize / actualBufSize;
                if((float)requiredSize / (float)actualBufSize > (float)numLoops)
                    numLoops++;
                entriesPerLoop = actualBufSize / bytesPerLoop;
            }
            int indexi = 0;
            int indexj = 0;
            for(int ii = 0; ii < numLoops; ii++)
            {
                for(int jj = 0; jj < entriesPerLoop; jj++)
                {
                    int off = chunkOffsetsArray[indexi][indexj++] + moovLength;
                    bufWriteInt(off);
                    if(indexj >= 1000)
                    {
                        indexi++;
                        indexj = 0;
                    }
                }

                bufFlush();
                bufClear();
                if(ii == numLoops - 2)
                    entriesPerLoop = numEntries - (numLoops - 1) * entriesPerLoop;
            }

        }

    }

    private int removeCount;
    private boolean sourceConnected;
    private boolean sinkConnected;
    private boolean closed;
    private boolean opened;
    private int debugCounter;
    private Hashtable streamNumberHash;
    private TrakInfo trakInfoArray[];
    private int dataSize;
    private Format rgbFormats[];
    private Format yuvFormats[];
    private int scaleOffsets[];
    private boolean endOfMediaStatus[];
    private int numberOfEoms;
    private int numberOfTracks;
    private int numberOfSupportedTracks;
    private static final String VIDEO = "vide";
    private static final String AUDIO = "soun";
    private long mdatOffset;
    private long moovOffset;
    private int mdatLength;
    private int moovLength;
    private long mvhdDurationOffset;
    private static Hashtable audioFourccMapper;
    private static Hashtable videoFourccMapper;
    private final int movieTimeScale = 60000;
    private final int DEFAULT_FRAME_RATE = 15;
    private final int DEFAULT_FRAME_DURATION = 4000;
    private final int TRAK_ENABLED = 1;
    private final int TRAK_IN_MOVIE = 2;
    private static final int DATA_SELF_REFERENCE_FLAG = 1;
    private static final boolean ALWAYS_USE_ONE_ENTRY_FOR_STTS = false;
    private static final int EPSILON_DURATION = 0xf4240;
    private static final int MVHD_ATOM_SIZE = 100;
    private static final int TKHD_ATOM_SIZE = 84;
    private static final int MDHD_ATOM_SIZE = 24;
    private boolean requireTwoPass;
    Format bigEndian;

    static 
    {
        audioFourccMapper = new Hashtable();
        videoFourccMapper = new Hashtable();
        audioFourccMapper.put("alaw", "alaw");
        audioFourccMapper.put("ulaw", "ulaw");
        audioFourccMapper.put("ima4", "ima4");
        audioFourccMapper.put("gsm", "agsm");
        audioFourccMapper.put("MAC3", "MAC3");
        audioFourccMapper.put("MAC6", "MAC6");
        videoFourccMapper.put("rgb", "rgb");
        videoFourccMapper.put("cvid", "cvid");
        videoFourccMapper.put("jpeg", "jpeg");
        videoFourccMapper.put("h261", "h261");
        videoFourccMapper.put("h263", "h263");
        videoFourccMapper.put("iv32", "iv32");
        videoFourccMapper.put("iv41", "iv41");
        videoFourccMapper.put("iv50", "iv50");
        videoFourccMapper.put("mjpg", "mjpg");
        videoFourccMapper.put("mjpa", "mjpa");
        videoFourccMapper.put("mjpb", "mjpb");
        videoFourccMapper.put("mpeg", "mpeg");
        videoFourccMapper.put("rpza", "rpza");
        videoFourccMapper.put("yuv", "yuv2");
    }
}
