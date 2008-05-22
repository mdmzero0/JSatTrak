// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   QuicktimeParser.java

package com.sun.media.parser.video;

import com.sun.media.parser.BasicPullParser;
import com.sun.media.util.SettableTime;
import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintStream;
import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;

public class QuicktimeParser extends BasicPullParser
{
    private class TimeAndDuration
    {

        double startTime;
        double duration;

        private TimeAndDuration()
        {
        }

    }

    private class HintVideoTrack extends VideoTrack
    {

        void doReadFrame(Buffer buffer)
        {
            boolean rtpMarkerSet = false;
            if(indexOfTrackBeingHinted < 0)
            {
                buffer.setDiscard(true);
                return;
            }
            if(super.useSampleIndex >= super.trakInfo.numberOfSamples)
            {
                buffer.setLength(0);
                buffer.setEOM(true);
                return;
            }
            int rtpOffset = 0;
            if(super.variableSampleSize)
                hintSampleSize = super.trakInfo.sampleSizeArray[super.useSampleIndex];
            int remainingHintSampleSize = hintSampleSize;
            long offset = super.trakInfo.sampleOffsetTable[super.useSampleIndex];
            if(debug1)
            {
                System.out.println("hintSampleSize is " + hintSampleSize);
                System.out.println("useSampleIndex, offset " + super.useSampleIndex + " : " + offset);
            }
            Object obj = buffer.getData();
            byte data[];
            if(obj == null || !(obj instanceof byte[]) || ((byte[])obj).length < maxPacketSize)
            {
                data = new byte[maxPacketSize];
                buffer.setData(data);
            } else
            {
                data = (byte[])obj;
            }
            try
            {
                int rtpSequenceNumber;
                int actualBytesRead;
                synchronized(seekSync)
                {
                    if(super.sampleIndex != super.useSampleIndex)
                    {
                        buffer.setDiscard(true);
                        currentPacketNumber = 0;
                        numPacketsInSample = -1;
                        offsetToStartOfPacketInfo = -1L;
                        rtpOffset = 0;
                        return;
                    }
                    if(super.cacheStream != null && super.listener != null && super.cacheStream.willReadBytesBlock(offset, hintSampleSize))
                        super.listener.readHasBlocked(this);
                    if(offsetToStartOfPacketInfo < 0L)
                    {
                        long pos = seekableStream.seek(offset);
                        if(pos == -2L)
                        {
                            buffer.setDiscard(true);
                            return;
                        }
                        numPacketsInSample = super.parser.readShort(stream);
                        if(debug)
                            System.out.println("video: num packets in sample " + numPacketsInSample);
                        if(numPacketsInSample < 1)
                        {
                            buffer.setDiscard(true);
                            return;
                        }
                        remainingHintSampleSize -= 2;
                        super.parser.readShort(stream);
                        remainingHintSampleSize -= 2;
                    } else
                    {
                        long pos = seekableStream.seek(offsetToStartOfPacketInfo);
                        if(pos == -2L)
                        {
                            buffer.setDiscard(true);
                            return;
                        }
                    }
                    int relativeTransmissionTime = super.parser.readInt(stream);
                    remainingHintSampleSize -= 4;
                    int rtpHeaderInfo = (short)super.parser.readShort(stream);
                    rtpMarkerSet = (rtpHeaderInfo & 0x80) > 0;
                    remainingHintSampleSize -= 2;
                    rtpSequenceNumber = super.parser.readShort(stream);
                    remainingHintSampleSize -= 2;
                    boolean paddingPresent = (rtpHeaderInfo & 0x2000) > 0;
                    boolean extensionHeaderPresent = (rtpHeaderInfo & 0x1000) > 0;
                    if(!paddingPresent);
                    if(!extensionHeaderPresent);
                    int flags = super.parser.readShort(stream);
                    if(debug)
                    {
                        System.out.println("rtp marker present? " + rtpMarkerSet);
                        System.out.println("rtp payload type " + (rtpHeaderInfo & 0x7f));
                        System.out.println("padding? " + paddingPresent);
                        System.out.println("extension header? " + extensionHeaderPresent);
                        System.out.println("video hint: flags is " + Integer.toHexString(flags));
                    }
                    remainingHintSampleSize -= 2;
                    int entriesInDataTable = super.parser.readShort(stream);
                    remainingHintSampleSize -= 2;
                    boolean extraInfoTLVPresent = (flags & 4) > 0;
                    if(extraInfoTLVPresent)
                    {
                        int tlvTableSize = super.parser.readInt(stream);
                        skip(stream, tlvTableSize - 4);
                        if(debug)
                        {
                            System.err.println("video: extraInfoTLVPresent: Skipped");
                            System.out.println("tlvTableSize is " + tlvTableSize);
                        }
                    }
                    if(debug)
                    {
                        System.out.println("Packet # " + currentPacketNumber);
                        System.out.println("  relativeTransmissionTime is " + relativeTransmissionTime);
                        System.out.println("$$$ relativeTransmissionTime is in timescale " + super.trakInfo.mediaTimeScale);
                        System.out.println("  rtpSequenceNumber is " + rtpSequenceNumber);
                        System.out.println("  entriesInDataTable is " + entriesInDataTable);
                    }
                    for(int j = 0; j < entriesInDataTable; j++)
                    {
                        int dataBlockSource = super.parser.readByte(stream);
                        remainingHintSampleSize--;
                        if(debug1)
                            System.out.println("    dataBlockSource is " + dataBlockSource);
                        if(dataBlockSource == 1)
                        {
                            int length = super.parser.readByte(stream);
                            remainingHintSampleSize--;
                            super.parser.readBytes(stream, data, rtpOffset, length);
                            rtpOffset += length;
                            super.parser.skip(stream, 14 - length);
                            remainingHintSampleSize -= 14;
                        } else
                        if(dataBlockSource == 2)
                        {
                            int trackRefIndex = super.parser.readByte(stream);
                            if(debug1)
                                System.out.println("     video: trackRefIndex is " + trackRefIndex);
                            if(trackRefIndex > 0)
                            {
                                System.err.println("     Currently we don't support hint tracks that refer to multiple media tracks");
                                buffer.setDiscard(true);
                                return;
                            }
                            int numBytesToCopy = super.parser.readShort(stream);
                            int sampleNumber = super.parser.readInt(stream);
                            int byteOffset = super.parser.readInt(stream);
                            int bytesPerCompresionBlock = super.parser.readShort(stream);
                            int samplesPerCompresionBlock = super.parser.readShort(stream);
                            if(debug1)
                            {
                                System.out.println("     sample Number is " + sampleNumber);
                                System.out.println("     numBytesToCopy is " + numBytesToCopy);
                                System.out.println("     byteOffset is " + byteOffset);
                                System.out.println("     bytesPerCompresionBlock is " + bytesPerCompresionBlock);
                                System.out.println("     samplesPerCompresionBlock is " + samplesPerCompresionBlock);
                            }
                            remainingHintSampleSize -= 15;
                            long saveCurrentPos = super.parser.getLocation(stream);
                            TrakList useTrakInfo;
                            if(trackRefIndex == 0)
                                useTrakInfo = sampleTrakInfo;
                            else
                                useTrakInfo = super.trakInfo;
                            long sampleOffset = useTrakInfo.sampleOffsetTable[sampleNumber - 1];
                            sampleOffset += byteOffset;
                            long pos = seekableStream.seek(sampleOffset);
                            if(pos == -2L)
                            {
                                buffer.setDiscard(true);
                                offsetToStartOfPacketInfo = -1L;
                                return;
                            }
                            if(debug1)
                                System.out.println("     read " + numBytesToCopy + " bytes from offset " + rtpOffset);
                            super.parser.readBytes(stream, data, rtpOffset, numBytesToCopy);
                            rtpOffset += numBytesToCopy;
                            pos = seekableStream.seek(saveCurrentPos);
                            if(pos == -2L)
                            {
                                buffer.setDiscard(true);
                                offsetToStartOfPacketInfo = -1L;
                                return;
                            }
                        } else
                        {
                            buffer.setDiscard(true);
                            offsetToStartOfPacketInfo = -1L;
                            return;
                        }
                    }

                    actualBytesRead = rtpOffset;
                    if(debug1)
                        System.out.println("Actual size of packet sent " + rtpOffset);
                    rtpOffset = 0;
                    offsetToStartOfPacketInfo = super.parser.getLocation(stream);
                    if(actualBytesRead == -2)
                    {
                        buffer.setDiscard(true);
                        return;
                    }
                }
                buffer.setLength(actualBytesRead);
                if(rtpMarkerSet)
                {
                    if(debug)
                        System.out.println("rtpMarkerSet: true");
                    buffer.setFlags(buffer.getFlags() | 0x800);
                } else
                {
                    if(debug)
                        System.out.println("rtpMarkerSet: false");
                    buffer.setFlags(buffer.getFlags() & 0xfffff7ff);
                }
                buffer.setSequenceNumber(rtpSequenceNumber);
                TimeAndDuration td = super.trakInfo.index2TimeAndDuration(super.useSampleIndex);
                double startTime = td.startTime;
                long timeStamp = (long)(startTime * 1000000000D);
                buffer.setTimeStamp(timeStamp);
                buffer.setDuration((long)(td.duration * 1000000000D));
            }
            catch(IOException e)
            {
                buffer.setLength(0);
                buffer.setEOM(true);
            }
            synchronized(this)
            {
                if(super.sampleIndex != super.useSampleIndex)
                {
                    currentPacketNumber = 0;
                    numPacketsInSample = -1;
                    offsetToStartOfPacketInfo = -1L;
                    rtpOffset = 0;
                } else
                {
                    currentPacketNumber++;
                    if(currentPacketNumber >= numPacketsInSample)
                    {
                        super.sampleIndex++;
                        currentPacketNumber = 0;
                        numPacketsInSample = -1;
                        offsetToStartOfPacketInfo = -1L;
                        rtpOffset = 0;
                    }
                }
            }
        }

        int hintSampleSize;
        int indexOfTrackBeingHinted;
        int maxPacketSize;
        int currentPacketNumber;
        int numPacketsInSample;
        long offsetToStartOfPacketInfo;
        TrakList sampleTrakInfo;

        HintVideoTrack(TrakList trakInfo)
        {
            super(trakInfo);
            indexOfTrackBeingHinted = super.trakInfo.indexOfTrackBeingHinted;
            currentPacketNumber = 0;
            numPacketsInSample = -1;
            offsetToStartOfPacketInfo = -1L;
            sampleTrakInfo = null;
            super.format = ((Hint)trakInfo.media).format;
            hintSampleSize = super.needBufferSize;
            maxPacketSize = trakInfo.maxPacketSize;
            if(debug1)
            {
                System.out.println("HintVideoTrack: Index of hinted track: " + trakInfo.indexOfTrackBeingHinted);
                System.out.println("HintVideoTrack: packet size is " + maxPacketSize);
            }
            if(indexOfTrackBeingHinted >= 0)
                sampleTrakInfo = trakList[indexOfTrackBeingHinted];
            else
            if(debug)
                System.out.println("sampleTrakInfo is not set " + indexOfTrackBeingHinted);
        }
    }

    private class HintAudioTrack extends AudioTrack
    {

        public void readFrame(Buffer buffer)
        {
            if(buffer == null)
                return;
            if(!super.enabled)
            {
                buffer.setDiscard(true);
                return;
            }
            synchronized(this)
            {
                super.useChunkNumber = super.chunkNumber;
                super.useSampleIndex = super.sampleIndex;
            }
            buffer.setFormat(super.format);
            doReadFrame(buffer);
        }

        synchronized void setSampleIndex(int index)
        {
            super.chunkNumber = index;
            super.sampleIndex = index;
        }

        void doReadFrame(Buffer buffer)
        {
            if(debug1)
                System.out.println("audio: hint doReadFrame: " + super.useChunkNumber + " : " + super.sampleOffsetInChunk);
            boolean rtpMarkerSet = false;
            if(indexOfTrackBeingHinted < 0)
            {
                buffer.setDiscard(true);
                return;
            }
            int rtpOffset = 0;
            if(variableSampleSize)
                if(super.useSampleIndex >= super.trakInfo.sampleSizeArray.length)
                    hintSampleSize = super.trakInfo.sampleSizeArray[super.trakInfo.sampleSizeArray.length - 1];
                else
                    hintSampleSize = super.trakInfo.sampleSizeArray[super.useSampleIndex];
            int remainingHintSampleSize = hintSampleSize;
            if(debug1)
                System.out.println("hintSampleSize is " + hintSampleSize);
            Object obj = buffer.getData();
            byte data[];
            if(obj == null || !(obj instanceof byte[]) || ((byte[])obj).length < maxPacketSize)
            {
                data = new byte[maxPacketSize];
                buffer.setData(data);
            } else
            {
                data = (byte[])obj;
            }
            try
            {
                int rtpSequenceNumber;
                int actualBytesRead;
                synchronized(seekSync)
                {
                    if(super.sampleIndex != super.useSampleIndex)
                    {
                        buffer.setDiscard(true);
                        currentPacketNumber = 0;
                        numPacketsInSample = -1;
                        offsetToStartOfPacketInfo = -1L;
                        rtpOffset = 0;
                        return;
                    }
                    long offset = super.trakInfo.index2Offset(super.useChunkNumber);
                    if(debug)
                    {
                        System.out.println("audio: Calling index2Offset on hint track with arg " + super.useChunkNumber);
                        System.out.println("offset is " + offset);
                    }
                    if(offset == -2L)
                    {
                        buffer.setLength(0);
                        buffer.setEOM(true);
                        return;
                    }
                    if(super.cacheStream != null && super.listener != null && super.cacheStream.willReadBytesBlock(offset, hintSampleSize))
                        super.listener.readHasBlocked(this);
                    if(debug1)
                        System.out.println("currentPacketNumber is " + currentPacketNumber);
                    if(offsetToStartOfPacketInfo < 0L)
                    {
                        if(debug1)
                            System.out.println("NEW SEEK");
                        long pos = seekableStream.seek(offset);
                        if(pos == -2L)
                        {
                            buffer.setDiscard(true);
                            return;
                        }
                        numPacketsInSample = super.parser.readShort(stream);
                        if(debug)
                            System.out.println("num packets in sample " + numPacketsInSample);
                        if(numPacketsInSample < 1)
                        {
                            buffer.setDiscard(true);
                            return;
                        }
                        remainingHintSampleSize -= 2;
                        super.parser.readShort(stream);
                        remainingHintSampleSize -= 2;
                    } else
                    {
                        long pos = seekableStream.seek(offsetToStartOfPacketInfo);
                        if(pos == -2L)
                        {
                            buffer.setDiscard(true);
                            return;
                        }
                    }
                    int relativeTransmissionTime = super.parser.readInt(stream);
                    remainingHintSampleSize -= 4;
                    int rtpHeaderInfo = super.parser.readShort(stream);
                    if(debug)
                        System.out.println("rtpHeaderInfo is " + Integer.toHexString(rtpHeaderInfo));
                    rtpMarkerSet = (rtpHeaderInfo & 0x80) > 0;
                    remainingHintSampleSize -= 2;
                    rtpSequenceNumber = super.parser.readShort(stream);
                    remainingHintSampleSize -= 2;
                    boolean paddingPresent = (rtpHeaderInfo & 0x2000) > 0;
                    boolean extensionHeaderPresent = (rtpHeaderInfo & 0x1000) > 0;
                    if(!paddingPresent);
                    if(!extensionHeaderPresent);
                    int flags = super.parser.readShort(stream);
                    if(debug)
                    {
                        System.out.println("rtp marker present? " + rtpMarkerSet);
                        System.out.println("rtp payload type " + (rtpHeaderInfo & 0x7f));
                        System.out.println("padding? " + paddingPresent);
                        System.out.println("extension header? " + extensionHeaderPresent);
                        System.out.println("audio hint: flags is " + Integer.toHexString(flags));
                    }
                    remainingHintSampleSize -= 2;
                    int entriesInDataTable = super.parser.readShort(stream);
                    remainingHintSampleSize -= 2;
                    boolean extraInfoTLVPresent = (flags & 4) > 0;
                    if(extraInfoTLVPresent)
                    {
                        int tlvTableSize = super.parser.readInt(stream);
                        skip(stream, tlvTableSize - 4);
                        if(debug)
                        {
                            System.err.println("audio: extraInfoTLVPresent: Skipped");
                            System.out.println("tlvTableSize is " + tlvTableSize);
                        }
                    }
                    if(debug)
                    {
                        System.out.println("Packet # " + currentPacketNumber);
                        System.out.println("  relativeTransmissionTime is " + relativeTransmissionTime);
                        System.out.println("  rtpSequenceNumber is " + rtpSequenceNumber);
                        System.out.println("  entriesInDataTable is " + entriesInDataTable);
                    }
                    for(int j = 0; j < entriesInDataTable; j++)
                    {
                        int dataBlockSource = super.parser.readByte(stream);
                        remainingHintSampleSize--;
                        if(debug1)
                            System.out.println("    dataBlockSource is " + dataBlockSource);
                        if(dataBlockSource == 1)
                        {
                            int length = super.parser.readByte(stream);
                            remainingHintSampleSize--;
                            super.parser.readBytes(stream, data, rtpOffset, length);
                            rtpOffset += length;
                            super.parser.skip(stream, 14 - length);
                            remainingHintSampleSize -= 14;
                        } else
                        if(dataBlockSource == 2)
                        {
                            int trackRefIndex = super.parser.readByte(stream);
                            if(debug1)
                                System.out.println("     audio:trackRefIndex is " + trackRefIndex);
                            if(trackRefIndex > 0)
                            {
                                System.err.println("     Currently we don't support hint tracks that refer to multiple media tracks: " + trackRefIndex);
                                buffer.setDiscard(true);
                                return;
                            }
                            int numBytesToCopy = super.parser.readShort(stream);
                            int sampleNumber = super.parser.readInt(stream);
                            int byteOffset = super.parser.readInt(stream);
                            int bytesPerCompresionBlock = super.parser.readShort(stream);
                            int samplesPerCompresionBlock = super.parser.readShort(stream);
                            if(debug1)
                            {
                                System.out.println("     sample Number is " + sampleNumber);
                                System.out.println("     numBytesToCopy is " + numBytesToCopy);
                                System.out.println("     byteOffset is " + byteOffset);
                                System.out.println("     bytesPerCompresionBlock is " + bytesPerCompresionBlock);
                                System.out.println("     samplesPerCompresionBlock is " + samplesPerCompresionBlock);
                            }
                            remainingHintSampleSize -= 15;
                            long saveCurrentPos = super.parser.getLocation(stream);
                            TrakList useTrakInfo;
                            if(trackRefIndex == 0)
                            {
                                useTrakInfo = sampleTrakInfo;
                                if(debug2)
                                    System.out.println("set useTrakInfo as sampleTrakInfo");
                            } else
                            {
                                useTrakInfo = super.trakInfo;
                            }
                            if(debug1)
                            {
                                System.out.println("useTrakInfo is " + useTrakInfo);
                                System.out.println("useTrakInfo.sampleOffsetTable is " + useTrakInfo.sampleOffsetTable);
                            }
                            long sampleOffset;
                            if(useTrakInfo.sampleOffsetTable == null)
                            {
                                sampleOffset = useTrakInfo.index2Offset(sampleNumber - 1);
                                if(debug1)
                                {
                                    System.out.println("chunkOffsets size is " + useTrakInfo.chunkOffsets.length);
                                    System.out.println("sampleOffset from index2Offset " + sampleOffset);
                                }
                            } else
                            {
                                sampleOffset = useTrakInfo.sampleOffsetTable[sampleNumber - 1];
                            }
                            sampleOffset += byteOffset;
                            long pos = seekableStream.seek(sampleOffset);
                            if(pos == -2L)
                            {
                                buffer.setDiscard(true);
                                offsetToStartOfPacketInfo = -1L;
                                return;
                            }
                            if(debug1)
                                System.out.println("Audio: Seek to " + sampleOffset + " and read " + numBytesToCopy + " bytes into buffer with offset " + rtpOffset);
                            super.parser.readBytes(stream, data, rtpOffset, numBytesToCopy);
                            rtpOffset += numBytesToCopy;
                            pos = seekableStream.seek(saveCurrentPos);
                            if(pos == -2L)
                            {
                                buffer.setDiscard(true);
                                offsetToStartOfPacketInfo = -1L;
                                return;
                            }
                        } else
                        if(dataBlockSource == 0)
                        {
                            int length = super.parser.readByte(stream);
                            super.parser.skip(stream, length);
                            remainingHintSampleSize -= length;
                        } else
                        {
                            System.err.println("DISCARD: dataBlockSource " + dataBlockSource + " not supported");
                            buffer.setDiscard(true);
                            offsetToStartOfPacketInfo = -1L;
                            return;
                        }
                    }

                    actualBytesRead = rtpOffset;
                    if(debug1)
                        System.out.println("Actual size of packet sent " + rtpOffset);
                    rtpOffset = 0;
                    offsetToStartOfPacketInfo = super.parser.getLocation(stream);
                    if(actualBytesRead == -2)
                    {
                        buffer.setDiscard(true);
                        return;
                    }
                }
                buffer.setLength(actualBytesRead);
                if(rtpMarkerSet)
                {
                    if(debug)
                        System.out.println("rtpMarkerSet: true");
                    buffer.setFlags(buffer.getFlags() | 0x800);
                } else
                {
                    if(debug)
                        System.out.println("rtpMarkerSet: false");
                    buffer.setFlags(buffer.getFlags() & 0xfffff7ff);
                }
                buffer.setSequenceNumber(rtpSequenceNumber);
                double startTime = super.trakInfo.index2TimeAndDuration(super.useChunkNumber).startTime;
                long timeStamp = (long)(startTime * 1000000000D);
                buffer.setTimeStamp(timeStamp);
                buffer.setDuration(-1L);
            }
            catch(IOException e)
            {
                buffer.setLength(0);
                buffer.setEOM(true);
            }
            synchronized(this)
            {
                if(super.chunkNumber != super.useChunkNumber)
                {
                    currentPacketNumber = 0;
                    numPacketsInSample = -1;
                    offsetToStartOfPacketInfo = -1L;
                    rtpOffset = 0;
                } else
                {
                    currentPacketNumber++;
                    if(currentPacketNumber >= numPacketsInSample)
                    {
                        super.chunkNumber++;
                        currentPacketNumber = 0;
                        numPacketsInSample = -1;
                        offsetToStartOfPacketInfo = -1L;
                        rtpOffset = 0;
                    }
                }
            }
        }

        int hintSampleSize;
        int indexOfTrackBeingHinted;
        int maxPacketSize;
        int currentPacketNumber;
        int numPacketsInSample;
        long offsetToStartOfPacketInfo;
        TrakList sampleTrakInfo;
        boolean variableSampleSize;

        HintAudioTrack(TrakList trakInfo, int channels, String encoding, int frameSizeInBytes, int samplesPerBlock, int sampleRate)
        {
            super(trakInfo, channels, encoding, frameSizeInBytes, samplesPerBlock, sampleRate);
            indexOfTrackBeingHinted = super.trakInfo.indexOfTrackBeingHinted;
            currentPacketNumber = 0;
            numPacketsInSample = -1;
            offsetToStartOfPacketInfo = -1L;
            variableSampleSize = true;
            super.format = ((Hint)trakInfo.media).format;
            maxPacketSize = trakInfo.maxPacketSize;
            if(indexOfTrackBeingHinted >= 0)
                sampleTrakInfo = trakList[indexOfTrackBeingHinted];
            else
            if(debug)
                System.out.println("sampleTrakInfo is not set " + indexOfTrackBeingHinted);
            if(trakInfo.sampleSize != 0)
            {
                variableSampleSize = false;
                hintSampleSize = trakInfo.sampleSize;
            }
        }
    }

    private class VideoTrack extends MediaTrack
    {

        void doReadFrame(Buffer buffer)
        {
            if(super.useSampleIndex >= super.trakInfo.numberOfSamples)
            {
                buffer.setLength(0);
                buffer.setEOM(true);
                return;
            }
            if(variableSampleSize)
            {
                if(super.useSampleIndex >= super.trakInfo.sampleSizeArray.length)
                {
                    buffer.setLength(0);
                    buffer.setEOM(true);
                    return;
                }
                needBufferSize = super.trakInfo.sampleSizeArray[super.useSampleIndex];
            }
            long offset = super.trakInfo.sampleOffsetTable[super.useSampleIndex];
            Object obj = buffer.getData();
            byte data[];
            if(obj == null || !(obj instanceof byte[]) || ((byte[])obj).length < needBufferSize)
            {
                data = new byte[needBufferSize];
                buffer.setData(data);
            } else
            {
                data = (byte[])obj;
            }
            try
            {
                int actualBytesRead;
                synchronized(seekSync)
                {
                    if(super.sampleIndex != super.useSampleIndex)
                    {
                        buffer.setDiscard(true);
                        return;
                    }
                    if(super.cacheStream != null && super.listener != null && super.cacheStream.willReadBytesBlock(offset, needBufferSize))
                        super.listener.readHasBlocked(this);
                    long pos = seekableStream.seek(offset);
                    if(pos == -2L)
                    {
                        buffer.setDiscard(true);
                        return;
                    }
                    actualBytesRead = super.parser.readBytes(stream, data, needBufferSize);
                    if(actualBytesRead == -2)
                    {
                        buffer.setDiscard(true);
                        return;
                    }
                }
                buffer.setLength(actualBytesRead);
                int syncSampleMapping[] = super.trakInfo.syncSampleMapping;
                boolean keyFrame = true;
                if(syncSampleMapping != null)
                    keyFrame = syncSampleMapping[super.useSampleIndex] == super.useSampleIndex;
                if(keyFrame)
                    buffer.setFlags(buffer.getFlags() | 0x10);
                buffer.setSequenceNumber(++super.sequenceNumber);
                TimeAndDuration td = super.trakInfo.index2TimeAndDuration(super.useSampleIndex);
                buffer.setTimeStamp((long)(td.startTime * 1000000000D));
                buffer.setDuration((long)(td.duration * 1000000000D));
            }
            catch(IOException e)
            {
                buffer.setLength(0);
                buffer.setEOM(true);
            }
            synchronized(this)
            {
                if(super.sampleIndex == super.useSampleIndex)
                    super.sampleIndex++;
            }
        }

        public int mapTimeToFrame(Time t)
        {
            double time = t.getSeconds();
            if(time < 0.0D)
                return 0x7fffffff;
            int index = super.trakInfo.time2Index(time);
            if(index < 0)
                return super.trakInfo.numberOfSamples - 1;
            else
                return index;
        }

        public Time mapFrameToTime(int frameNumber)
        {
            if(frameNumber < 0 || frameNumber >= super.trakInfo.numberOfSamples)
            {
                return Track.TIME_UNKNOWN;
            } else
            {
                double time = (float)frameNumber / ((Media) ((Video)super.trakInfo.media)).frameRate;
                return new Time(time);
            }
        }

        int needBufferSize;
        boolean variableSampleSize;

        VideoTrack(TrakList trakInfo)
        {
            super(trakInfo);
            variableSampleSize = true;
            if(trakInfo != null && trakInfo.sampleSize != 0)
            {
                variableSampleSize = false;
                needBufferSize = trakInfo.sampleSize;
            }
        }
    }

    private class AudioTrack extends MediaTrack
    {

        synchronized void setChunkNumberAndSampleOffset(int number, int offset)
        {
            super.chunkNumber = number;
            sampleOffsetInChunk = offset;
        }

        void doReadFrame(Buffer buffer)
        {
            synchronized(this)
            {
                if(sampleOffsetInChunk == -1)
                {
                    useSampleOffsetInChunk = 0;
                } else
                {
                    useSampleOffsetInChunk = sampleOffsetInChunk;
                    sampleOffsetInChunk = -1;
                }
            }
            int samples;
            long samplesPlayed;
            if(super.constantSamplesPerChunk != -1)
            {
                samples = super.constantSamplesPerChunk;
                samplesPlayed = super.constantSamplesPerChunk * super.useChunkNumber;
            } else
            if(super.useChunkNumber > 0)
            {
                samples = super.samplesPerChunk[super.useChunkNumber] - super.samplesPerChunk[super.useChunkNumber - 1];
                samplesPlayed = super.samplesPerChunk[super.useChunkNumber];
            } else
            {
                samples = super.samplesPerChunk[super.useChunkNumber];
                samplesPlayed = 0L;
            }
            int byteOffsetFromSampleOffset;
            if(samplesPerBlock > 1)
            {
                int skipBlocks = useSampleOffsetInChunk / samplesPerBlock;
                useSampleOffsetInChunk = skipBlocks * samplesPerBlock;
                byteOffsetFromSampleOffset = frameSizeInBytes * skipBlocks;
            } else
            {
                byteOffsetFromSampleOffset = useSampleOffsetInChunk * frameSizeInBytes;
            }
            samples -= useSampleOffsetInChunk;
            samplesPlayed += useSampleOffsetInChunk;
            int needBufferSize;
            if(encoding.equals("ima4"))
                needBufferSize = (samples / samplesPerBlock) * 34 * channels;
            else
            if(encoding.equals("agsm"))
                needBufferSize = (samples / 160) * samplesPerBlock;
            else
                needBufferSize = ((samples * ((AudioFormat)super.format).getSampleSizeInBits()) / 8) * channels;
            Object obj = buffer.getData();
            byte data[];
            if(obj == null || !(obj instanceof byte[]) || ((byte[])obj).length < needBufferSize)
            {
                data = new byte[needBufferSize];
                buffer.setData(data);
            } else
            {
                data = (byte[])obj;
            }
            try
            {
                int actualBytesRead;
                synchronized(seekSync)
                {
                    int offset = super.trakInfo.chunkOffsets[super.useChunkNumber];
                    if(super.sampleIndex != super.useSampleIndex)
                    {
                        buffer.setDiscard(true);
                        return;
                    }
                    if(super.cacheStream != null && super.listener != null && super.cacheStream.willReadBytesBlock(offset + byteOffsetFromSampleOffset, needBufferSize))
                        super.listener.readHasBlocked(this);
                    long pos = seekableStream.seek(offset + byteOffsetFromSampleOffset);
                    if(pos == -2L)
                    {
                        buffer.setDiscard(true);
                        return;
                    }
                    actualBytesRead = super.parser.readBytes(stream, data, needBufferSize);
                    if(actualBytesRead == -2)
                    {
                        buffer.setDiscard(true);
                        return;
                    }
                }
                buffer.setLength(actualBytesRead);
                buffer.setSequenceNumber(++super.sequenceNumber);
                if(sampleRate > 0)
                {
                    long timeStamp = (samplesPlayed * 0x3b9aca00L) / (long)sampleRate;
                    buffer.setTimeStamp(timeStamp);
                    buffer.setDuration(-1L);
                }
            }
            catch(IOException e)
            {
                buffer.setLength(0);
                buffer.setEOM(true);
            }
            synchronized(this)
            {
                if(super.chunkNumber == super.useChunkNumber)
                    super.chunkNumber++;
            }
        }

        String encoding;
        int channels;
        int sampleOffsetInChunk;
        int useSampleOffsetInChunk;
        int frameSizeInBytes;
        int samplesPerBlock;
        int sampleRate;

        AudioTrack(TrakList trakInfo, int channels, String encoding, int frameSizeInBytes, int samplesPerBlock, int sampleRate)
        {
            super(trakInfo);
            sampleOffsetInChunk = -1;
            useSampleOffsetInChunk = 0;
            this.channels = channels;
            this.encoding = encoding;
            this.frameSizeInBytes = frameSizeInBytes;
            this.samplesPerBlock = samplesPerBlock;
            this.sampleRate = sampleRate;
        }

        AudioTrack(TrakList trakInfo)
        {
            super(trakInfo);
            sampleOffsetInChunk = -1;
            useSampleOffsetInChunk = 0;
            if(trakInfo != null)
            {
                channels = ((Audio)trakInfo.media).channels;
                encoding = trakInfo.media.encoding;
                frameSizeInBytes = ((Audio)trakInfo.media).frameSizeInBits / 8;
                samplesPerBlock = ((Audio)trakInfo.media).samplesPerBlock;
                sampleRate = ((Audio)trakInfo.media).sampleRate;
            }
        }
    }

    private abstract class MediaTrack
        implements Track
    {

        public void setTrackListener(TrackListener l)
        {
            listener = l;
        }

        public Format getFormat()
        {
            return format;
        }

        public void setEnabled(boolean t)
        {
            enabled = t;
        }

        public boolean isEnabled()
        {
            return enabled;
        }

        public Time getDuration()
        {
            return trakInfo.duration;
        }

        public Time getStartTime()
        {
            return new Time(0L);
        }

        synchronized void setSampleIndex(int index)
        {
            sampleIndex = index;
        }

        synchronized void setChunkNumber(int number)
        {
            chunkNumber = number;
        }

        public void readFrame(Buffer buffer)
        {
            if(buffer == null)
                return;
            if(!enabled)
            {
                buffer.setDiscard(true);
                return;
            }
            synchronized(this)
            {
                useChunkNumber = chunkNumber;
                useSampleIndex = sampleIndex;
            }
            if(useChunkNumber >= trakInfo.numberOfChunks || useChunkNumber < 0)
            {
                buffer.setEOM(true);
                return;
            } else
            {
                buffer.setFormat(format);
                doReadFrame(buffer);
                return;
            }
        }

        abstract void doReadFrame(Buffer buffer);

        public int mapTimeToFrame(Time t)
        {
            return 0x7fffffff;
        }

        public Time mapFrameToTime(int frameNumber)
        {
            return Track.TIME_UNKNOWN;
        }

        TrakList trakInfo;
        boolean enabled;
        int numBuffers;
        Format format;
        long sequenceNumber;
        int chunkNumber;
        int sampleIndex;
        int useChunkNumber;
        int useSampleIndex;
        QuicktimeParser parser;
        CachedStream cacheStream;
        int constantSamplesPerChunk;
        int samplesPerChunk[];
        protected TrackListener listener;

        MediaTrack(TrakList trakInfo)
        {
            enabled = true;
            numBuffers = 4;
            sequenceNumber = 0L;
            chunkNumber = 0;
            sampleIndex = 0;
            useChunkNumber = 0;
            useSampleIndex = 0;
            parser = QuicktimeParser.this;
            cacheStream = parser.getCacheStream();
            this.trakInfo = trakInfo;
            if(trakInfo != null)
            {
                enabled = (trakInfo.flag & 1) != 0;
                format = trakInfo.media.createFormat();
                samplesPerChunk = trakInfo.samplesPerChunk;
                constantSamplesPerChunk = trakInfo.constantSamplesPerChunk;
            }
        }
    }

    private class TrakList
    {

        void buildSamplePerChunkTable()
        {
            if(numberOfChunks <= 0)
                return;
            if(compactSamplesPerChunk.length == 1)
            {
                constantSamplesPerChunk = compactSamplesPerChunk[0];
                return;
            }
            samplesPerChunk = new int[numberOfChunks];
            int i = 1;
            int j;
            for(j = 0; j < compactSamplesChunkNum.length - 1; j++)
            {
                int numSamples = compactSamplesPerChunk[j];
                for(; i != compactSamplesChunkNum[j + 1]; i++)
                    samplesPerChunk[i - 1] = numSamples;

            }

            for(; i <= numberOfChunks; i++)
                samplesPerChunk[i - 1] = compactSamplesPerChunk[j];

        }

        void buildCumulativeSamplePerChunkTable()
        {
            if(constantSamplesPerChunk == -1)
            {
                for(int i = 1; i < numberOfChunks; i++)
                    samplesPerChunk[i] += samplesPerChunk[i - 1];

            }
        }

        void buildSampleOffsetTable()
        {
            sampleOffsetTable = new long[numberOfSamples];
            int index = 0;
            if(sampleSize != 0)
            {
                if(constantSamplesPerChunk != -1)
                {
                    for(int i = 0; i < numberOfChunks; i++)
                    {
                        long offset = chunkOffsets[i];
                        for(int j = 0; j < constantSamplesPerChunk; j++)
                            sampleOffsetTable[index++] = offset + (long)(j * sampleSize);

                    }

                } else
                {
                    for(int i = 0; i < numberOfChunks; i++)
                    {
                        long offset = chunkOffsets[i];
                        for(int j = 0; j < samplesPerChunk[i]; j++)
                            sampleOffsetTable[index++] = offset + (long)(j * sampleSize);

                    }

                }
            } else
            {
                int numSamplesInChunk = 0;
                if(constantSamplesPerChunk != -1)
                    numSamplesInChunk = constantSamplesPerChunk;
                for(int i = 0; i < numberOfChunks; i++)
                {
                    long offset = chunkOffsets[i];
                    sampleOffsetTable[index] = offset;
                    index++;
                    if(constantSamplesPerChunk == -1)
                        numSamplesInChunk = samplesPerChunk[i];
                    for(int j = 1; j < numSamplesInChunk; j++)
                    {
                        sampleOffsetTable[index] = sampleOffsetTable[index - 1] + (long)sampleSizeArray[index - 1];
                        index++;
                    }

                }

            }
        }

        boolean buildSyncTable()
        {
            if(syncSamples == null)
                return false;
            if(!trackType.equals("vide"))
                return false;
            int numEntries = syncSamples.length;
            if(numEntries == numberOfSamples)
            {
                syncSamples = null;
                return false;
            }
            syncSampleMapping = new int[numberOfSamples];
            int index = 0;
            int previous;
            if(syncSamples[0] != 1)
            {
                previous = syncSampleMapping[0] = 0;
            } else
            {
                previous = syncSampleMapping[0] = 0;
                index++;
            }
            for(; index < syncSamples.length; index++)
            {
                int next = syncSamples[index] - 1;
                syncSampleMapping[next] = next;
                int range = next - previous - 1;
                for(int j = previous + 1; j < next; j++)
                    syncSampleMapping[j] = previous;

                previous = next;
            }

            int lastSyncFrame = syncSamples[syncSamples.length - 1] - 1;
            for(index = lastSyncFrame + 1; index < numberOfSamples; index++)
                syncSampleMapping[index] = lastSyncFrame;

            return true;
        }

        int time2Index(double time)
        {
            if(time < 0.0D)
                time = 0.0D;
            int length = timeToSampleIndices.length;
            int sampleIndex;
            if(length == 0)
            {
                sampleIndex = (int)((time / mediaDuration.getSeconds()) * (double)numberOfSamples + 0.5D);
                if(sampleIndex >= numberOfSamples)
                    return -1;
                else
                    return sampleIndex;
            }
            int approxLocation = (int)((time / mediaDuration.getSeconds()) * (double)length);
            if(approxLocation == length)
                approxLocation--;
            if(approxLocation >= cumulativeDurationOfSamples.length)
                return -1;
            int foundIndex;
            if(cumulativeDurationOfSamples[approxLocation] < time)
            {
                int i;
                for(i = approxLocation + 1; i < length; i++)
                    if(cumulativeDurationOfSamples[i] >= time)
                        break;

                foundIndex = i;
            } else
            if(cumulativeDurationOfSamples[approxLocation] > time)
            {
                int i;
                for(i = approxLocation - 1; i >= 0; i--)
                    if(cumulativeDurationOfSamples[i] < time)
                        break;

                foundIndex = i + 1;
            } else
            {
                foundIndex = approxLocation;
            }
            if(foundIndex == length)
                foundIndex--;
            double delta = cumulativeDurationOfSamples[foundIndex] - time;
            int samples;
            double duration;
            if(foundIndex == 0)
            {
                sampleIndex = timeToSampleIndices[foundIndex];
                samples = sampleIndex;
                duration = cumulativeDurationOfSamples[foundIndex];
            } else
            {
                sampleIndex = timeToSampleIndices[foundIndex];
                samples = sampleIndex - timeToSampleIndices[foundIndex - 1];
                duration = cumulativeDurationOfSamples[foundIndex] - cumulativeDurationOfSamples[foundIndex - 1];
            }
            double fraction = delta / duration;
            sampleIndex = (int)((double)sampleIndex - (double)samples * fraction);
            return sampleIndex;
        }

        TimeAndDuration index2TimeAndDuration(int index)
        {
            double startTime = 0.0D;
            double duration = 0.0D;
            try
            {
                if(index < 0)
                    index = 0;
                else
                if(index >= numberOfSamples)
                    index = numberOfSamples - 1;
                int length = timeToSampleIndices.length;
                if(length == 0)
                {
                    duration = durationOfSamples;
                    startTime = duration * (double)index;
                } else
                if(startTimeOfSampleArray.length >= index)
                {
                    duration = durationOfSampleArray[index];
                    startTime = startTimeOfSampleArray[index];
                } else
                {
                    float factor = (float)length / (float)numberOfSamples;
                    int location = (int)((float)index * factor);
                    duration = 0.0D;
                    startTime = 0.0D;
                }
            }
            finally
            {
                TimeAndDuration timeandduration1;
                synchronized(timeAndDuration)
                {
                    timeAndDuration.startTime = startTime;
                    timeAndDuration.duration = duration;
                    timeandduration1 = timeAndDuration;
                }
                return timeandduration1;
            }
        }

        int index2Chunk(int index)
        {
            int chunk;
            if(constantSamplesPerChunk != -1)
            {
                chunk = index / constantSamplesPerChunk;
                return chunk;
            }
            int length = samplesPerChunk.length;
            int approxChunk = (int)((float)(index / numberOfSamples) * (float)length);
            if(approxChunk == length)
                approxChunk--;
            if(samplesPerChunk[approxChunk] < index)
            {
                int i;
                for(i = approxChunk + 1; i < length; i++)
                    if(samplesPerChunk[i] >= index)
                        break;

                chunk = i;
            } else
            if(samplesPerChunk[approxChunk] > index)
            {
                int i;
                for(i = approxChunk - 1; i >= 0; i--)
                    if(samplesPerChunk[i] < index)
                        break;

                chunk = i + 1;
            } else
            {
                chunk = approxChunk;
            }
            return chunk;
        }

        long index2Offset(int index)
        {
            int chunk = index2Chunk(index + 1);
            if(debug)
                System.out.println(" index2Chunk chunk is " + chunk);
            if(chunk >= chunkOffsets.length)
                return -2L;
            long offset = chunkOffsets[chunk];
            if(debug1)
                System.out.println("index2Offset: index, chunk, chunkOffset " + index + " : " + chunk + " : " + offset);
            int sampleNumInChunk;
            int start;
            if(constantSamplesPerChunk != -1)
            {
                sampleNumInChunk = index % constantSamplesPerChunk;
                start = chunk * constantSamplesPerChunk;
            } else
            {
                if(chunk == 0)
                    start = 0;
                else
                    start = samplesPerChunk[chunk - 1];
                sampleNumInChunk = index - start;
                if(debug1)
                {
                    System.out.println("index, start, sampleNumInChunk " + index + " : " + start + " : " + sampleNumInChunk);
                    System.out.println("sampleSize is " + sampleSize);
                }
            }
            if(debug1)
                System.out.println("sampleSize is " + sampleSize);
            if(sampleSize != 0)
            {
                offset += sampleSize * sampleNumInChunk;
            } else
            {
                for(int i = 0; i < sampleNumInChunk; i++)
                    offset += sampleSizeArray[start++];

            }
            return offset;
        }

        void buildStartTimeAndDurationTable()
        {
            if(debug2)
                System.out.println("buildStartTimeAndDurationTable");
            int length = timeToSampleIndices.length;
            if(length == 0)
                return;
            startTimeOfSampleArray = new double[numberOfSamples];
            durationOfSampleArray = new double[numberOfSamples];
            int previousSamples = 0;
            double previousDuration = 0.0D;
            double time = 0.0D;
            int index = 0;
            for(int i = 0; i < length; i++)
            {
                int numSamples = timeToSampleIndices[i];
                double duration = (cumulativeDurationOfSamples[i] - previousDuration) / (double)(numSamples - previousSamples);
                for(int j = 0; j < numSamples - previousSamples; j++)
                {
                    startTimeOfSampleArray[index] = time;
                    durationOfSampleArray[index] = duration;
                    index++;
                    time += duration;
                }

                previousSamples = numSamples;
                previousDuration = cumulativeDurationOfSamples[i];
            }

        }

        public String toString()
        {
            String info = "";
            info = info + "track id is " + id + "\n";
            info = info + "duration itrack is " + duration.getSeconds() + "\n";
            info = info + "duration of media is " + mediaDuration.getSeconds() + "\n";
            info = info + "trackType is " + trackType + "\n";
            info = info + media;
            return info;
        }

        int flag;
        int id;
        Time duration;
        int mediaTimeScale;
        Time mediaDuration;
        String trackType;
        int numberOfSamples;
        int sampleSize;
        int sampleSizeArray[];
        boolean supported;
        Media media;
        int numberOfChunks;
        int chunkOffsets[];
        int compactSamplesChunkNum[];
        int compactSamplesPerChunk[];
        int constantSamplesPerChunk;
        int samplesPerChunk[];
        double durationOfSamples;
        int timeToSampleIndices[];
        double cumulativeDurationOfSamples[];
        double startTimeOfSampleArray[];
        double durationOfSampleArray[];
        long sampleOffsetTable[];
        int syncSamples[];
        int syncSampleMapping[];
        TimeAndDuration timeAndDuration;
        int trackIdOfTrackBeingHinted;
        int indexOfTrackBeingHinted;
        int maxPacketSize;

        private TrakList()
        {
            duration = Duration.DURATION_UNKNOWN;
            mediaDuration = Duration.DURATION_UNKNOWN;
            sampleSize = 0;
            chunkOffsets = new int[0];
            compactSamplesChunkNum = new int[0];
            compactSamplesPerChunk = new int[0];
            constantSamplesPerChunk = -1;
            durationOfSamples = -1D;
            timeToSampleIndices = new int[0];
            cumulativeDurationOfSamples = new double[0];
            startTimeOfSampleArray = new double[0];
            durationOfSampleArray = new double[0];
            timeAndDuration = new TimeAndDuration();
            trackIdOfTrackBeingHinted = -1;
            indexOfTrackBeingHinted = -1;
            maxPacketSize = -1;
        }

    }

    private class Hint extends Media
    {

        Format createFormat()
        {
            return format;
        }

        Format format;

        private Hint()
        {
            format = null;
        }

    }

    private class Video extends Media
    {

        Format createFormat()
        {
            if(format != null)
                return format;
            if(super.encoding.toLowerCase().startsWith("raw"))
            {
                super.encoding = "rgb";
                if(pixelDepth == 24)
                    format = new RGBFormat(new Dimension(width, height), -1, Format.byteArray, super.frameRate, pixelDepth, 1, 2, 3, 3, width * 3, 0, 0);
                else
                if(pixelDepth == 16)
                    format = new RGBFormat(new Dimension(width, height), -1, Format.byteArray, super.frameRate, pixelDepth, 31744, 992, 31, 2, width * 2, 0, 0);
                else
                if(pixelDepth == 32)
                {
                    super.encoding = "rgb";
                    format = new RGBFormat(new Dimension(width, height), -1, Format.byteArray, super.frameRate, pixelDepth, 2, 3, 4, 4, width * 4, 0, 0);
                }
            } else
            if(super.encoding.toLowerCase().equals("8bps"))
                format = new VideoFormat(super.encoding, new Dimension(width, height), super.maxSampleSize, Format.byteArray, super.frameRate);
            else
            if(super.encoding.toLowerCase().equals("yuv2"))
                format = new YUVFormat(new Dimension(width, height), -1, Format.byteArray, super.frameRate, 96, width * 2, width * 2, 0, 1, 3);
            else
                format = new VideoFormat(super.encoding, new Dimension(width, height), super.maxSampleSize, Format.byteArray, super.frameRate);
            return format;
        }

        public String toString()
        {
            String info = "Video: " + format + "\n";
            info = info + "encoding is " + super.encoding + "\n";
            info = info + "pixelDepth is " + pixelDepth + "\n";
            return info;
        }

        int width;
        int height;
        int pixelDepth;
        int colorTableID;
        VideoFormat format;

        private Video()
        {
        }

    }

    private class Audio extends Media
    {

        public String toString()
        {
            String info = "Audio: " + format + "\n";
            info = info + "encoding is " + super.encoding + "\n";
            info = info + "Number of channels " + channels + "\n";
            info = info + "Bits per sample " + bitsPerSample + "\n";
            info = info + "sampleRate " + sampleRate + "\n";
            return info;
        }

        Format createFormat()
        {
            if(format != null)
                return format;
            String encodingString = null;
            boolean signed = true;
            boolean bigEndian = true;
            if(super.encoding.equals("ulaw") || super.encoding.equals("alaw"))
                bitsPerSample = 8;
            frameSizeInBits = channels * bitsPerSample;
            if(super.encoding.equals("ulaw"))
            {
                encodingString = "ULAW";
                signed = false;
            } else
            if(super.encoding.equals("alaw"))
            {
                encodingString = "alaw";
                signed = false;
            } else
            if(super.encoding.equals("twos"))
                encodingString = "LINEAR";
            else
            if(super.encoding.equals("ima4"))
            {
                encodingString = "ima4";
                samplesPerBlock = 64;
                frameSizeInBits = 34 * channels * 8;
            } else
            if(super.encoding.equals("raw "))
            {
                encodingString = "LINEAR";
                signed = false;
            } else
            if(super.encoding.equals("agsm"))
            {
                encodingString = "gsm";
                samplesPerBlock = 33;
                frameSizeInBits = 264;
            } else
            if(super.encoding.equals("mac3"))
                encodingString = "MAC3";
            else
            if(super.encoding.equals("mac6"))
                encodingString = "MAC6";
            else
                encodingString = super.encoding;
            format = new AudioFormat(encodingString, sampleRate, bitsPerSample, channels, bigEndian ? 1 : 0, signed ? 1 : 0, frameSizeInBits, -1D, Format.byteArray);
            return format;
        }

        int channels;
        int bitsPerSample;
        int sampleRate;
        AudioFormat format;
        int frameSizeInBits;
        int samplesPerBlock;

        private Audio()
        {
            format = null;
            samplesPerBlock = 1;
        }

    }

    private abstract class Media
    {

        abstract Format createFormat();

        String encoding;
        int maxSampleSize;
        float frameRate;

        private Media()
        {
        }

    }

    private class MovieHeader
    {

        int timeScale;
        Time duration;
        long mdatStart;
        long mdatSize;

        private MovieHeader()
        {
            duration = Duration.DURATION_UNKNOWN;
        }

    }


    public QuicktimeParser()
    {
        stream = null;
        mdatAtomPresent = false;
        moovAtomPresent = false;
        movieHeader = new MovieHeader();
        numTracks = 0;
        numSupportedTracks = 0;
        numberOfHintTracks = 0;
        trakList = new TrakList[MAX_TRACKS_SUPPORTED];
        keyFrameTrack = -1;
        mediaTime = new SettableTime(0L);
        hintAudioTrackNum = -1;
        debug = false;
        debug1 = false;
        debug2 = false;
        seekSync = new Object();
        tmpIntBufferSize = 16384;
        tmpBuffer = new byte[tmpIntBufferSize * 4];
    }

    protected boolean supports(SourceStream s[])
    {
        return super.seekable;
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        super.setSource(source);
        stream = (PullSourceStream)super.streams[0];
        seekableStream = (Seekable)super.streams[0];
    }

    private CachedStream getCacheStream()
    {
        return super.cacheStream;
    }

    public ContentDescriptor[] getSupportedInputContentDescriptors()
    {
        return supportedFormat;
    }

    public Track[] getTracks()
        throws IOException, BadHeaderException
    {
        if(tracks != null)
            return tracks;
        if(seekableStream == null)
            return new Track[0];
        if(super.cacheStream != null)
            super.cacheStream.setEnabledBuffering(false);
        readHeader();
        if(super.cacheStream != null)
            super.cacheStream.setEnabledBuffering(true);
        tracks = new Track[numSupportedTracks];
        int index = 0;
        for(int i = 0; i < numSupportedTracks; i++)
        {
            TrakList trakInfo = trakList[i];
            if(trakInfo.trackType.equals("soun"))
                tracks[i] = new AudioTrack(trakInfo);
            else
            if(trakInfo.trackType.equals("vide"))
                tracks[i] = new VideoTrack(trakInfo);
        }

        for(int i = 0; i < numSupportedTracks; i++)
        {
            TrakList trakInfo = trakList[i];
            if(trakInfo.trackType.equals("hint"))
            {
                int trackBeingHinted = trakInfo.trackIdOfTrackBeingHinted;
                for(int j = 0; j < numTracks; j++)
                {
                    if(trackBeingHinted != trakList[j].id)
                        continue;
                    trakInfo.indexOfTrackBeingHinted = j;
                    String hintedTrackType = trakList[j].trackType;
                    String encodingOfHintedTrack = trakList[trakInfo.indexOfTrackBeingHinted].media.encoding;
                    if(encodingOfHintedTrack.equals("agsm"))
                        encodingOfHintedTrack = "gsm";
                    String rtpEncoding = encodingOfHintedTrack + "/rtp";
                    if(hintedTrackType.equals("soun"))
                    {
                        Audio audio = (Audio)trakList[j].media;
                        hintAudioTrackNum = i;
                        int channels = audio.channels;
                        int frameSizeInBytes = audio.frameSizeInBits / 8;
                        int samplesPerBlock = audio.samplesPerBlock;
                        int sampleRate = audio.sampleRate;
                        ((Hint)trakInfo.media).format = new AudioFormat(rtpEncoding, sampleRate, 8, channels);
                        tracks[i] = new HintAudioTrack(trakInfo, channels, rtpEncoding, frameSizeInBytes, samplesPerBlock, sampleRate);
                    } else
                    if(hintedTrackType.equals("vide"))
                    {
                        int indexOfTrackBeingHinted = trakInfo.indexOfTrackBeingHinted;
                        TrakList sampleTrakInfo = null;
                        if(indexOfTrackBeingHinted >= 0)
                            sampleTrakInfo = trakList[indexOfTrackBeingHinted];
                        int width = 0;
                        int height = 0;
                        if(sampleTrakInfo != null)
                        {
                            Video sampleTrakVideo = (Video)sampleTrakInfo.media;
                            width = sampleTrakVideo.width;
                            height = sampleTrakVideo.height;
                        }
                        if(width > 0 && height > 0)
                            ((Hint)trakInfo.media).format = new VideoFormat(rtpEncoding, new Dimension(width, height), -1, null, -1F);
                        HintVideoTrack hintVideoTrack = new HintVideoTrack(trakInfo);
                        tracks[i] = hintVideoTrack;
                    }
                    break;
                }

            }
        }

        return tracks;
    }

    private void readHeader()
        throws IOException, BadHeaderException
    {
        while(parseAtom()) ;
        if(!moovAtomPresent)
            throw new BadHeaderException("moov atom not present");
        if(!mdatAtomPresent)
            throw new BadHeaderException("mdat atom not present");
        for(int i = 0; i < numSupportedTracks; i++)
        {
            TrakList trak = trakList[i];
            if(trak.buildSyncTable())
                keyFrameTrack = i;
            trak.buildSamplePerChunkTable();
            if(!trak.trackType.equals("soun"))
            {
                trak.buildSampleOffsetTable();
                trak.buildStartTimeAndDurationTable();
                float frameRate = (float)((double)trak.numberOfSamples / trak.duration.getSeconds());
                trak.media.frameRate = frameRate;
            }
            trak.buildCumulativeSamplePerChunkTable();
            trak.media.createFormat();
        }

    }

    public Time setPosition(Time where, int rounding)
    {
        double time = where.getSeconds();
        if(time < 0.0D)
            time = 0.0D;
        int keyT;
        if((keyT = keyFrameTrack) != -1 && tracks[keyFrameTrack].isEnabled() || (keyT = hintAudioTrackNum) != -1 && tracks[hintAudioTrackNum].isEnabled())
        {
            TrakList trakInfo = trakList[keyT];
            int index = trakInfo.time2Index(time);
            if(index < 0)
            {
                ((MediaTrack)tracks[keyT]).setSampleIndex(trakInfo.numberOfSamples + 1);
            } else
            {
                int syncIndex;
                if(keyT == keyFrameTrack)
                {
                    if(index >= trakInfo.syncSampleMapping.length)
                        index = trakInfo.syncSampleMapping.length - 1;
                    if(trakInfo.syncSampleMapping != null)
                    {
                        syncIndex = trakInfo.syncSampleMapping[index];
                        double newtime = trakInfo.index2TimeAndDuration(syncIndex).startTime;
                        time = newtime;
                    } else
                    {
                        syncIndex = index;
                    }
                } else
                {
                    syncIndex = index;
                    double newtime = trakInfo.index2TimeAndDuration(syncIndex).startTime;
                    time = newtime;
                }
                ((MediaTrack)tracks[keyT]).setSampleIndex(syncIndex);
            }
        }
        for(int i = 0; i < numSupportedTracks; i++)
            if(i != keyT && tracks[i].isEnabled())
            {
                TrakList trakInfo = trakList[i];
                int index = trakInfo.time2Index(time);
                if(trakInfo.trackType.equals("vide") || trakInfo.trackType.equals("hint") && (tracks[i] instanceof HintVideoTrack))
                {
                    if(index < 0)
                    {
                        ((MediaTrack)tracks[i]).setSampleIndex(trakInfo.numberOfSamples + 1);
                    } else
                    {
                        int syncIndex;
                        if(trakInfo.syncSampleMapping != null)
                            syncIndex = trakInfo.syncSampleMapping[index];
                        else
                            syncIndex = index;
                        ((MediaTrack)tracks[i]).setSampleIndex(syncIndex);
                    }
                } else
                if(index < 0)
                {
                    ((MediaTrack)tracks[i]).setChunkNumber(trakInfo.numberOfChunks + 1);
                } else
                {
                    ((MediaTrack)tracks[i]).setSampleIndex(index);
                    int chunkNumber = trakInfo.index2Chunk(index);
                    int sampleOffsetInChunk;
                    if(chunkNumber != 0)
                    {
                        if(trakInfo.constantSamplesPerChunk == -1)
                            sampleOffsetInChunk = index - trakInfo.samplesPerChunk[chunkNumber - 1];
                        else
                            sampleOffsetInChunk = index - chunkNumber * trakInfo.constantSamplesPerChunk;
                    } else
                    {
                        sampleOffsetInChunk = index;
                    }
                    ((AudioTrack)tracks[i]).setChunkNumberAndSampleOffset(chunkNumber, sampleOffsetInChunk);
                }
            }

        if(super.cacheStream != null)
            synchronized(this)
            {
                super.cacheStream.abortRead();
            }
        synchronized(mediaTime)
        {
            mediaTime.set(time);
        }
        return mediaTime;
    }

    public Time getMediaTime()
    {
        return null;
    }

    public Time getDuration()
    {
        return movieHeader.duration;
    }

    public String getName()
    {
        return "Parser for quicktime file format";
    }

    private boolean parseAtom()
        throws BadHeaderException
    {
        boolean readSizeField = false;
        try
        {
            int atomSize = readInt(stream);
            readSizeField = true;
            String atom = readString(stream);
            if(atomSize < 8)
                throw new BadHeaderException(atom + ": Bad Atom size " + atomSize);
            if(atom.equals("moov"))
                return parseMOOV(atomSize - 8);
            if(atom.equals("mdat"))
            {
                return parseMDAT(atomSize - 8);
            } else
            {
                skipAtom(atom + " [not implemented]", atomSize - 8);
                return true;
            }
        }
        catch(IOException e) { }
        if(!readSizeField)
            return false;
        else
            throw new BadHeaderException("Unexpected End of Media");
    }

    private void skipAtom(String atom, int size)
        throws IOException
    {
        if(debug2)
            System.out.println("skip unsupported atom " + atom);
        skip(stream, size);
    }

    private boolean parseMOOV(int moovSize)
        throws BadHeaderException
    {
        boolean trakAtomPresent = false;
        try
        {
            moovAtomPresent = true;
            long moovMax = getLocation(stream) + (long)moovSize;
            int remainingSize = moovSize;
            int atomSize = readInt(stream);
            String atom = readString(stream);
            if(atomSize < 8)
                throw new BadHeaderException(atom + ": Bad Atom size " + atomSize);
            if(!atom.equals("mvhd"))
                if(atom.equals("cmov"))
                    throw new BadHeaderException("Compressed movie headers are not supported");
                else
                    throw new BadHeaderException("Expected mvhd atom but got " + atom);
            parseMVHD(atomSize - 8);
            for(remainingSize -= atomSize; remainingSize > 0; remainingSize -= atomSize)
            {
                atomSize = readInt(stream);
                atom = readString(stream);
                if(atom.equals("trak"))
                {
                    if(trakList[numSupportedTracks] == null)
                        trakList[numSupportedTracks] = currentTrack = new TrakList();
                    if(parseTRAK(atomSize - 8))
                        numSupportedTracks++;
                    trakAtomPresent = true;
                    numTracks++;
                } else
                if(atom.equals("ctab"))
                    parseCTAB(atomSize - 8);
                else
                    skipAtom(atom + " [atom in moov: not implemented]", atomSize - 8);
            }

            if(!trakAtomPresent)
                throw new BadHeaderException("trak atom not present in trak atom container");
            else
                return !mdatAtomPresent;
        }
        catch(IOException e)
        {
            throw new BadHeaderException("IOException when parsing the header");
        }
    }

    private boolean parseMDAT(int size)
        throws BadHeaderException
    {
        try
        {
            mdatAtomPresent = true;
            movieHeader.mdatStart = getLocation(stream);
            movieHeader.mdatSize = size;
            if(!moovAtomPresent)
            {
                skip(stream, size);
                return true;
            } else
            {
                return false;
            }
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past MDAT atom");
        }
    }

    private void parseMVHD(int size)
        throws BadHeaderException
    {
        try
        {
            if(size != 100)
                throw new BadHeaderException("mvhd atom: header size is incorrect");
            skip(stream, 12);
            movieHeader.timeScale = readInt(stream);
            int duration = readInt(stream);
            movieHeader.duration = new Time((double)duration / (double)movieHeader.timeScale);
            int preferredRate = readInt(stream);
            int preferredVolume = readShort(stream);
            skip(stream, 10);
            skip(stream, 36);
            int previewTime = readInt(stream);
            int previewDuration = readInt(stream);
            int posterTime = readInt(stream);
            int selectionTime = readInt(stream);
            int selectionDuration = readInt(stream);
            int currentTime = readInt(stream);
            int nextTrackID = readInt(stream);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past MVHD atom");
        }
    }

    private boolean parseTRAK(int trakSize)
        throws BadHeaderException
    {
        boolean mdiaAtomPresent = false;
        boolean supported = false;
        try
        {
            int remainingSize = trakSize;
            int atomSize = readInt(stream);
            String atom = readString(stream);
            if(atomSize < 8)
                throw new BadHeaderException(atom + ": Bad Atom size " + atomSize);
            if(!atom.equals("tkhd"))
                throw new BadHeaderException("Expected tkhd atom but got " + atom);
            parseTKHD(atomSize - 8);
            for(remainingSize -= atomSize; remainingSize > 0; remainingSize -= atomSize)
            {
                atomSize = readInt(stream);
                atom = readString(stream);
                if(atom.equals("mdia"))
                {
                    supported = parseMDIA(atomSize - 8);
                    mdiaAtomPresent = true;
                } else
                if(atom.equals("tref"))
                    parseTREF(atomSize - 8);
                else
                    skipAtom(atom + " [atom in trak: not implemented]", atomSize - 8);
            }

        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past TRAK atom");
        }
        if(!mdiaAtomPresent)
            throw new BadHeaderException("mdia atom not present in trak atom container");
        if(supported && currentTrack.media == null)
            supported = false;
        return supported;
    }

    private void parseCTAB(int ctabSize)
        throws BadHeaderException
    {
        try
        {
            skip(stream, ctabSize);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("....");
        }
    }

    private void parseTKHD(int tkhdSize)
        throws BadHeaderException
    {
        try
        {
            if(tkhdSize != 84)
                throw new BadHeaderException("mvhd atom: header size is incorrect");
            int iVersionPlusFlag = readInt(stream);
            currentTrack.flag = iVersionPlusFlag & 0xffffff;
            skip(stream, 8);
            currentTrack.id = readInt(stream);
            skip(stream, 4);
            int duration = readInt(stream);
            currentTrack.duration = new Time((double)duration / (double)movieHeader.timeScale);
            skip(stream, tkhdSize - 4 - 8 - 4 - 4 - 4);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past TKHD atom");
        }
    }

    private boolean parseMDIA(int mdiaSize)
        throws BadHeaderException
    {
        boolean hdlrAtomPresent = false;
        boolean minfAtomPresent = false;
        try
        {
            currentTrack.trackType = null;
            int remainingSize = mdiaSize;
            int atomSize = readInt(stream);
            String atom = readString(stream);
            if(atomSize < 8)
                throw new BadHeaderException(atom + ": Bad Atom size " + atomSize);
            if(!atom.equals("mdhd"))
                throw new BadHeaderException("Expected mdhd atom but got " + atom);
            parseMDHD(atomSize - 8);
            for(remainingSize -= atomSize; remainingSize > 0; remainingSize -= atomSize)
            {
                atomSize = readInt(stream);
                atom = readString(stream);
                if(atom.equals("hdlr"))
                {
                    parseHDLR(atomSize - 8);
                    hdlrAtomPresent = true;
                } else
                if(atom.equals("minf"))
                {
                    if(currentTrack.trackType == null)
                        throw new BadHeaderException("In MDIA atom container minf atom appears before hdlr");
                    if(currentTrack.supported)
                        parseMINF(atomSize - 8);
                    else
                        skipAtom(atom + " [atom in mdia] as trackType " + currentTrack.trackType + " is not supported", atomSize - 8);
                    minfAtomPresent = true;
                } else
                {
                    skipAtom(atom + " [atom in mdia: not implemented]", atomSize - 8);
                }
            }

            if(!hdlrAtomPresent)
                throw new BadHeaderException("hdlr atom not present in mdia atom container");
            if(!minfAtomPresent)
                throw new BadHeaderException("minf atom not present in mdia atom container");
            else
                return currentTrack.supported;
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past MDIA atom");
        }
    }

    private void parseMDHD(int mdhdSize)
        throws BadHeaderException
    {
        try
        {
            if(mdhdSize != 24)
                throw new BadHeaderException("mdhd atom: header size is incorrect");
            skip(stream, 12);
            int timeScale = readInt(stream);
            int duration = readInt(stream);
            currentTrack.mediaDuration = new Time((double)duration / (double)timeScale);
            currentTrack.mediaTimeScale = timeScale;
            skip(stream, 4);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past MDHD atom");
        }
    }

    private void parseHDLR(int hdlrSize)
        throws BadHeaderException
    {
        try
        {
            if(hdlrSize < 24)
                throw new BadHeaderException("hdlr atom: header size is incorrect");
            skip(stream, 8);
            currentTrack.trackType = readString(stream);
            currentTrack.supported = isSupported(currentTrack.trackType);
            skip(stream, hdlrSize - 8 - 4);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past HDLR atom");
        }
    }

    private void parseTREF(int size)
        throws BadHeaderException
    {
        try
        {
            int childAtomSize = readInt(stream);
            size -= 4;
            String atom = readString(stream);
            size -= 4;
            if(atom.equalsIgnoreCase("hint"))
            {
                currentTrack.trackIdOfTrackBeingHinted = readInt(stream);
                size -= 4;
            }
            skip(stream, size);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past HDLR atom");
        }
    }

    private void parseMINF(int minfSize)
        throws BadHeaderException
    {
        boolean hdlrAtomPresent = false;
        try
        {
            int remainingSize = minfSize;
            int atomSize = readInt(stream);
            String atom = readString(stream);
            if(atomSize < 8)
                throw new BadHeaderException(atom + ": Bad Atom size " + atomSize);
            if(!atom.endsWith("hd"))
                throw new BadHeaderException("Expected media information header atom but got " + atom);
            skipAtom(atom + " [atom in minf: not implemented]", atomSize - 8);
            for(remainingSize -= atomSize; remainingSize > 0; remainingSize -= atomSize)
            {
                atomSize = readInt(stream);
                atom = readString(stream);
                if(atom.equals("hdlr"))
                {
                    skipAtom(atom + " [atom in minf: not implemented]", atomSize - 8);
                    hdlrAtomPresent = true;
                } else
                if(atom.equals("dinf"))
                    parseDINF(atomSize - 8);
                else
                if(atom.equals("stbl"))
                    parseSTBL(atomSize - 8);
                else
                    skipAtom(atom + " [atom in minf: not implemented]", atomSize - 8);
            }

            if(!hdlrAtomPresent)
                throw new BadHeaderException("hdlr atom not present in minf atom container");
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past MINF atom");
        }
    }

    private void parseDINF(int dinfSize)
        throws BadHeaderException
    {
        try
        {
            int atomSize;
            for(int remainingSize = dinfSize; remainingSize > 0; remainingSize -= atomSize)
            {
                atomSize = readInt(stream);
                String atom = readString(stream);
                if(atom.equals("dref"))
                    parseDREF(atomSize - 8);
                else
                    skipAtom(atom + " [Unknown atom in dinf]", atomSize - 8);
            }

        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past DIMF atom");
        }
    }

    private void parseDREF(int drefSize)
        throws BadHeaderException
    {
        try
        {
            skip(stream, 4);
            int numEntries = readInt(stream);
            for(int i = 0; i < numEntries; i++)
            {
                int drefEntrySize = readInt(stream);
                int type = readInt(stream);
                int versionPlusFlag = readInt(stream);
                skip(stream, drefEntrySize - 12);
                if((versionPlusFlag & 1) <= 0)
                    throw new BadHeaderException("Only self contained Quicktime movies are supported");
            }

        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past DREF atom");
        }
    }

    private void parseSTBL(int stblSize)
        throws BadHeaderException
    {
        try
        {
            int atomSize;
            for(int remainingSize = stblSize; remainingSize > 0; remainingSize -= atomSize)
            {
                atomSize = readInt(stream);
                String atom = readString(stream);
                if(atom.equals("stsd"))
                    parseSTSD(atomSize - 8);
                else
                if(atom.equals("stts"))
                    parseSTTS(atomSize - 8);
                else
                if(atom.equals("stss"))
                    parseSTSS(atomSize - 8);
                else
                if(atom.equals("stsc"))
                    parseSTSC(atomSize - 8);
                else
                if(atom.equals("stsz"))
                    parseSTSZ(atomSize - 8);
                else
                if(atom.equals("stco"))
                    parseSTCO(atomSize - 8);
                else
                if(atom.equals("stsh"))
                    skipAtom(atom + " [not implemented]", atomSize - 8);
                else
                    skipAtom(atom + " [UNKNOWN atom in stbl: ignored]", atomSize - 8);
            }

        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past STBL atom");
        }
    }

    private void parseSTSD(int stsdSize)
        throws BadHeaderException
    {
        try
        {
            if(stsdSize < 8)
                throw new BadHeaderException("stsd atom: header size is incorrect");
            skip(stream, 4);
            int numEntries = readInt(stream);
            if(numEntries <= 1);
            for(int i = 0; i < numEntries; i++)
            {
                int sampleDescriptionSize = readInt(stream);
                String encoding = readString(stream);
                if(i != 0)
                {
                    skip(stream, sampleDescriptionSize - 8);
                } else
                {
                    skip(stream, 6);
                    if(currentTrack.trackType.equals("vide"))
                        currentTrack.media = parseVideoSampleData(encoding, sampleDescriptionSize - 4 - 4 - 6);
                    else
                    if(currentTrack.trackType.equals("soun"))
                        currentTrack.media = parseAudioSampleData(encoding, sampleDescriptionSize - 4 - 4 - 6);
                    else
                    if(currentTrack.trackType.equals("hint"))
                    {
                        numberOfHintTracks++;
                        currentTrack.media = parseHintSampleData(encoding, sampleDescriptionSize - 4 - 4 - 6);
                    } else
                    {
                        skip(stream, sampleDescriptionSize - 4 - 4 - 6);
                    }
                }
            }

        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past STSD atom");
        }
    }

    private Video parseVideoSampleData(String encoding, int dataSize)
        throws IOException, BadHeaderException
    {
        skip(stream, 2);
        skip(stream, 16);
        Video video = new Video();
        video.encoding = encoding;
        video.width = readShort(stream);
        video.height = readShort(stream);
        skip(stream, 14);
        skip(stream, 32);
        video.pixelDepth = readShort(stream);
        video.colorTableID = readShort(stream);
        int colorTableSize = 0;
        if(video.colorTableID == 0)
        {
            colorTableSize = readInt(stream);
            skip(stream, colorTableSize - 4);
        }
        skip(stream, dataSize - 2 - 70 - -colorTableSize);
        return video;
    }

    private Audio parseAudioSampleData(String encoding, int dataSize)
        throws IOException, BadHeaderException
    {
        skip(stream, 2);
        skip(stream, 8);
        Audio audio = new Audio();
        audio.encoding = encoding;
        audio.channels = readShort(stream);
        audio.bitsPerSample = readShort(stream);
        skip(stream, 4);
        int sampleRate = readInt(stream);
        audio.sampleRate = currentTrack.mediaTimeScale;
        skip(stream, dataSize - 2 - 20);
        return audio;
    }

    private Hint parseHintSampleData(String encoding, int dataSize)
        throws IOException, BadHeaderException
    {
        if(!encoding.equals("rtp "))
            System.err.println("Hint track Data Format is not rtp");
        Hint hint = new Hint();
        int dataReferenceIndex = readShort(stream);
        int hintTrackVersion = readShort(stream);
        if(hintTrackVersion == 0)
        {
            System.err.println("Hint Track version #0 is not supported");
            System.err.println("Use QuickTimePro to convert it to version #1");
            currentTrack.supported = false;
            if(dataSize - 2 - 2 > 0)
                skip(stream, dataSize - 2 - 2);
            return hint;
        }
        int lastCompatibleHintTrackVersion = readShort(stream);
        int maxPacketSize = readInt(stream);
        currentTrack.maxPacketSize = maxPacketSize;
        int remaining = dataSize - 2 - 2 - 2 - 4;
        if(debug1)
        {
            System.out.println("dataReferenceIndex is " + dataReferenceIndex);
            System.out.println("hintTrackVersion is " + hintTrackVersion);
            System.out.println("lastCompatibleHintTrackVersion is " + lastCompatibleHintTrackVersion);
            System.out.println("maxPacketSize is " + maxPacketSize);
            System.out.println("remaining is " + remaining);
        }
        while(remaining > 8) 
        {
            int entryLength = readInt(stream);
            remaining -= 4;
            if(entryLength > 8)
            {
                if(debug2)
                    System.out.println("entryLength is " + entryLength);
                String dataTag = readString(stream);
                if(debug2)
                    System.out.println("dataTag is " + dataTag);
                remaining -= 4;
                if(dataTag.equals("tims"))
                {
                    int rtpTimeScale = readInt(stream);
                    remaining -= 4;
                } else
                if(dataTag.equals("tsro"))
                {
                    System.out.println("QuicktimeParser: rtp: tsro dataTag not supported");
                    int rtpTimeStampOffset = readInt(stream);
                    remaining -= 4;
                } else
                if(dataTag.equals("snro"))
                {
                    System.out.println("QuicktimeParser: rtp: snro dataTag not supported");
                    int rtpSequenceNumberOffset = readInt(stream);
                    remaining -= 4;
                } else
                if(dataTag.equals("rely"))
                {
                    System.out.println("QuicktimeParser: rtp: rely dataTag not supported");
                    int rtpReliableTransportFlag = readByte(stream);
                    remaining--;
                } else
                {
                    skip(stream, remaining);
                    remaining = 0;
                }
                continue;
            }
            skip(stream, remaining);
            remaining = 0;
            break;
        }
        if(remaining > 0)
            skip(stream, remaining);
        return hint;
    }

    private void parseSTTS(int sttsSize)
        throws BadHeaderException
    {
        if(debug2)
            System.out.println("parseSTTS: " + sttsSize);
        try
        {
            if(sttsSize < 8)
                throw new BadHeaderException("stts atom: header size is incorrect");
            skip(stream, 4);
            int numEntries = readInt(stream);
            if(debug2)
                System.out.println("numEntries is " + numEntries);
            int requiredSize = sttsSize - 8 - numEntries * 8;
            if(requiredSize < 0)
                throw new BadHeaderException("stts atom: inconsistent number_of_entries field");
            int totalNumSamples = 0;
            double timeScaleFactor = 1.0D / (double)currentTrack.mediaTimeScale;
            if(numEntries == 1)
            {
                totalNumSamples = readInt(stream);
                currentTrack.durationOfSamples = (double)readInt(stream) * timeScaleFactor;
            } else
            {
                int timeToSampleIndices[] = new int[numEntries];
                double durations[] = new double[numEntries];
                timeToSampleIndices[0] = readInt(stream);
                totalNumSamples += timeToSampleIndices[0];
                durations[0] = (double)readInt(stream) * timeScaleFactor * (double)timeToSampleIndices[0];
                int remaining = numEntries - 1;
                int numIntsWrittenPerLoop = 2;
                int maxEntriesPerLoop = tmpIntBufferSize / numIntsWrittenPerLoop;
                int i = 1;
                int numEntriesPerLoop;
                for(; remaining > 0; remaining -= numEntriesPerLoop)
                {
                    numEntriesPerLoop = remaining <= maxEntriesPerLoop ? remaining : maxEntriesPerLoop;
                    readBytes(stream, tmpBuffer, numEntriesPerLoop * numIntsWrittenPerLoop * 4);
                    int offset = 0;
                    for(int ii = 1; ii <= numEntriesPerLoop;)
                    {
                        timeToSampleIndices[i] = parseIntFromArray(tmpBuffer, offset, true);
                        offset += 4;
                        int value = parseIntFromArray(tmpBuffer, offset, true);
                        offset += 4;
                        durations[i] += (double)value * timeScaleFactor * (double)timeToSampleIndices[i] + durations[i - 1];
                        totalNumSamples += timeToSampleIndices[i];
                        timeToSampleIndices[i] = totalNumSamples;
                        ii++;
                        i++;
                    }

                }

                currentTrack.timeToSampleIndices = timeToSampleIndices;
                currentTrack.cumulativeDurationOfSamples = durations;
            }
            if(currentTrack.numberOfSamples == 0)
                currentTrack.numberOfSamples = totalNumSamples;
            skip(stream, requiredSize);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past STTS atom");
        }
    }

    private void parseSTSC(int stscSize)
        throws BadHeaderException
    {
        try
        {
            if(stscSize < 8)
                throw new BadHeaderException("stsc atom: header size is incorrect");
            skip(stream, 4);
            int numEntries = readInt(stream);
            int requiredSize = stscSize - 8 - numEntries * 12;
            if(requiredSize < 0)
                throw new BadHeaderException("stsc atom: inconsistent number_of_entries field");
            int compactSamplesChunkNum[] = new int[numEntries];
            int compactSamplesPerChunk[] = new int[numEntries];
            byte tmpBuf[] = new byte[numEntries * 4 * 3];
            readBytes(stream, tmpBuf, numEntries * 4 * 3);
            int offset = 0;
            for(int i = 0; i < numEntries; i++)
            {
                compactSamplesChunkNum[i] = parseIntFromArray(tmpBuf, offset, true);
                offset += 4;
                compactSamplesPerChunk[i] = parseIntFromArray(tmpBuf, offset, true);
                offset += 4;
                offset += 4;
            }

            tmpBuf = null;
            currentTrack.compactSamplesChunkNum = compactSamplesChunkNum;
            currentTrack.compactSamplesPerChunk = compactSamplesPerChunk;
            skip(stream, requiredSize);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past STSC atom");
        }
    }

    private void parseSTSZ(int stszSize)
        throws BadHeaderException
    {
        if(debug2)
            System.out.println("parseSTSZ: " + stszSize);
        try
        {
            if(stszSize < 8)
                throw new BadHeaderException("stsz atom: header size is incorrect");
            skip(stream, 4);
            currentTrack.sampleSize = readInt(stream);
            if(currentTrack.sampleSize != 0)
            {
                skip(stream, stszSize - 8);
                currentTrack.media.maxSampleSize = currentTrack.sampleSize;
                return;
            }
            if(stszSize - 8 < 4)
                throw new BadHeaderException("stsz atom: incorrect atom size");
            int numEntries = readInt(stream);
            if(currentTrack.numberOfSamples == 0)
                currentTrack.numberOfSamples = numEntries;
            int requiredSize = stszSize - 8 - 4 - numEntries * 4;
            if(requiredSize < 0)
                throw new BadHeaderException("stsz atom: inconsistent number_of_entries field");
            int sampleSizeArray[] = new int[numEntries];
            int maxSampleSize = 0x80000000;
            int remaining = numEntries;
            int numIntsWrittenPerLoop = 1;
            int maxEntriesPerLoop = tmpIntBufferSize / numIntsWrittenPerLoop;
            int i = 0;
            int numEntriesPerLoop;
            for(; remaining > 0; remaining -= numEntriesPerLoop)
            {
                numEntriesPerLoop = remaining <= maxEntriesPerLoop ? remaining : maxEntriesPerLoop;
                readBytes(stream, tmpBuffer, numEntriesPerLoop * numIntsWrittenPerLoop * 4);
                int offset = 0;
                for(int ii = 1; ii <= numEntriesPerLoop;)
                {
                    int value = parseIntFromArray(tmpBuffer, offset, true);
                    offset += 4;
                    if(value > maxSampleSize)
                        maxSampleSize = value;
                    sampleSizeArray[i] = value;
                    ii++;
                    i++;
                }

            }

            currentTrack.sampleSizeArray = sampleSizeArray;
            currentTrack.media.maxSampleSize = maxSampleSize;
            skip(stream, requiredSize);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past STSZ atom");
        }
    }

    private void parseSTCO(int stcoSize)
        throws BadHeaderException
    {
        if(debug2)
            System.out.println("rtp:parseSTCO: " + stcoSize);
        try
        {
            if(stcoSize < 8)
                throw new BadHeaderException("stco atom: header size is incorrect");
            skip(stream, 4);
            int numEntries = readInt(stream);
            currentTrack.numberOfChunks = numEntries;
            int chunkOffsets[] = new int[numEntries];
            int requiredSize = stcoSize - 8 - numEntries * 4;
            if(requiredSize < 0)
                throw new BadHeaderException("stco atom: inconsistent number_of_entries field");
            int remaining = numEntries;
            int numIntsWrittenPerLoop = 1;
            int maxEntriesPerLoop = tmpIntBufferSize / numIntsWrittenPerLoop;
            int i = 0;
            int numEntriesPerLoop;
            for(; remaining > 0; remaining -= numEntriesPerLoop)
            {
                numEntriesPerLoop = remaining <= maxEntriesPerLoop ? remaining : maxEntriesPerLoop;
                readBytes(stream, tmpBuffer, numEntriesPerLoop * numIntsWrittenPerLoop * 4);
                int offset = 0;
                for(int ii = 1; ii <= numEntriesPerLoop;)
                {
                    chunkOffsets[i] = parseIntFromArray(tmpBuffer, offset, true);
                    offset += 4;
                    ii++;
                    i++;
                }

            }

            currentTrack.chunkOffsets = chunkOffsets;
            skip(stream, requiredSize);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past STCO atom");
        }
    }

    private void parseSTSS(int stssSize)
        throws BadHeaderException
    {
        try
        {
            if(stssSize < 8)
                throw new BadHeaderException("stss atom: header size is incorrect");
            skip(stream, 4);
            int numEntries = readInt(stream);
            int requiredSize = stssSize - 8 - numEntries * 4;
            if(requiredSize < 0)
                throw new BadHeaderException("stss atom: inconsistent number_of_entries field");
            if(numEntries < 1)
            {
                skip(stream, requiredSize);
                return;
            }
            int syncSamples[] = new int[numEntries];
            int remaining = numEntries;
            int numIntsWrittenPerLoop = 1;
            int maxEntriesPerLoop = tmpIntBufferSize / numIntsWrittenPerLoop;
            int i = 0;
            int numEntriesPerLoop;
            for(; remaining > 0; remaining -= numEntriesPerLoop)
            {
                numEntriesPerLoop = remaining <= maxEntriesPerLoop ? remaining : maxEntriesPerLoop;
                readBytes(stream, tmpBuffer, numEntriesPerLoop * numIntsWrittenPerLoop * 4);
                int offset = 0;
                for(int ii = 1; ii <= numEntriesPerLoop;)
                {
                    syncSamples[i] = parseIntFromArray(tmpBuffer, offset, true);
                    offset += 4;
                    ii++;
                    i++;
                }

            }

            currentTrack.syncSamples = syncSamples;
            skip(stream, requiredSize);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("Got IOException when seeking past STSS atom");
        }
    }

    private boolean isSupported(String trackType)
    {
        return trackType.equals("vide") || trackType.equals("soun") || trackType.equals("hint");
    }

    private final boolean enableHintTrackSupport = true;
    private static ContentDescriptor supportedFormat[] = {
        new ContentDescriptor("video.quicktime")
    };
    private PullSourceStream stream;
    private Track tracks[];
    private Seekable seekableStream;
    private boolean mdatAtomPresent;
    private boolean moovAtomPresent;
    public static final int MVHD_ATOM_SIZE = 100;
    public static final int TKHD_ATOM_SIZE = 84;
    public static final int MDHD_ATOM_SIZE = 24;
    public static final int MIN_HDLR_ATOM_SIZE = 24;
    public static final int MIN_STSD_ATOM_SIZE = 8;
    public static final int MIN_STTS_ATOM_SIZE = 8;
    public static final int MIN_STSC_ATOM_SIZE = 8;
    public static final int MIN_STSZ_ATOM_SIZE = 8;
    public static final int MIN_STCO_ATOM_SIZE = 8;
    public static final int MIN_STSS_ATOM_SIZE = 8;
    public static final int MIN_VIDEO_SAMPLE_DATA_SIZE = 70;
    public static final int MIN_AUDIO_SAMPLE_DATA_SIZE = 20;
    public static final int TRACK_ENABLED = 1;
    public static final int TRACK_IN_MOVIE = 2;
    public static final int TRACK_IN_PREVIEW = 4;
    public static final int TRACK_IN_POSTER = 8;
    public static final String VIDEO = "vide";
    public static final String AUDIO = "soun";
    public static final String HINT = "hint";
    private static final int DATA_SELF_REFERENCE_FLAG = 1;
    private static final int HINT_NOP_IGNORE = 0;
    private static final int HINT_IMMEDIATE_DATA = 1;
    private static final int HINT_SAMPLE_DATA = 2;
    private static final int HINT_SAMPLE_DESCRIPTION = 3;
    private MovieHeader movieHeader;
    private int numTracks;
    private int numSupportedTracks;
    private int numberOfHintTracks;
    private static int MAX_TRACKS_SUPPORTED = 100;
    private TrakList trakList[];
    private TrakList currentTrack;
    private int keyFrameTrack;
    private SettableTime mediaTime;
    private int hintAudioTrackNum;
    private boolean debug;
    private boolean debug1;
    private boolean debug2;
    private Object seekSync;
    private int tmpIntBufferSize;
    private byte tmpBuffer[];









}
