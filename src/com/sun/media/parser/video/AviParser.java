// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AviParser.java

package com.sun.media.parser.video;

import com.sun.media.format.WavAudioFormat;
import com.sun.media.parser.BasicPullParser;
import com.sun.media.vfw.BitMapInfo;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

public class AviParser extends BasicPullParser
{
    private class AVIIndexEntry
    {

        public String id;
        public int flag;
        public int chunkOffset;
        public int chunkLength;
        public int cumulativeChunkLength;

        private AVIIndexEntry()
        {
            cumulativeChunkLength = 0;
        }

    }

    private class VideoTrack extends MediaTrack
    {

        void doReadFrame(Buffer buffer1)
        {
        }

        long getTimeStamp()
        {
            return (long)super.useChunkNumber * (long)usecPerFrame * 1000L;
        }

        public int mapTimeToFrame(Time t)
        {
            if(nanoSecPerFrame <= 0L)
                return 0x7fffffff;
            if(t.getNanoseconds() < 0L)
                return 0x7fffffff;
            int chunkNumber = (int)(t.getNanoseconds() / nanoSecPerFrame);
            if(chunkNumber >= super.trakInfo.maxChunkIndex)
                return super.trakInfo.maxChunkIndex - 1;
            else
                return chunkNumber;
        }

        public Time mapFrameToTime(int frameNumber)
        {
            if(frameNumber < 0 || frameNumber >= super.trakInfo.maxChunkIndex)
            {
                return Track.TIME_UNKNOWN;
            } else
            {
                long time = (long)frameNumber * nanoSecPerFrame;
                return new Time(time);
            }
        }

        int needBufferSize;
        boolean variableSampleSize;

        VideoTrack(TrakList trakInfo)
        {
            super(trakInfo);
            variableSampleSize = true;
        }
    }

    private class AudioTrack extends MediaTrack
    {

        void doReadFrame(Buffer buffer1)
        {
        }

        long getTimeStamp()
        {
            if(avgBytesPerSec > 0)
            {
                long bytes = super.useOffsetWithinChunk;
                if(super.useChunkNumber > 0)
                    bytes += chunkInfo[super.useChunkNumber - 1].cumulativeChunkLength;
                return (long)((double)((float)bytes / (float)avgBytesPerSec) * 1000000000D);
            } else
            {
                return 0L;
            }
        }

        int channels;
        int avgBytesPerSec;
        AVIIndexEntry chunkInfo[];

        AudioTrack(TrakList trakInfo)
        {
            super(trakInfo);
            channels = ((Audio)trakInfo.media).channels;
            avgBytesPerSec = ((Audio)trakInfo.media).avgBytesPerSec;
            chunkInfo = trakInfo.chunkInfo;
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

        synchronized void setChunkNumberAndOffset(int number, int offset)
        {
            chunkNumber = number;
            offsetWithinChunk = offset;
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
                if(offsetWithinChunk == -1)
                {
                    useOffsetWithinChunk = 0;
                } else
                {
                    useOffsetWithinChunk = offsetWithinChunk;
                    offsetWithinChunk = -1;
                }
                useChunkNumber = chunkNumber;
            }
            if(useChunkNumber >= trakInfo.maxChunkIndex || useChunkNumber < 0)
            {
                buffer.setLength(0);
                buffer.setEOM(true);
                return;
            }
            buffer.setFormat(format);
            indexEntry = trakInfo.chunkInfo[useChunkNumber];
            int chunkLength = indexEntry.chunkLength;
            Object obj = buffer.getData();
            buffer.setHeader(new Integer(indexEntry.flag));
            byte data[];
            if(obj == null || !(obj instanceof byte[]) || ((byte[])obj).length < chunkLength)
            {
                data = new byte[chunkLength];
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
                    seekableStream.seek(indexEntry.chunkOffset + moviOffset + useOffsetWithinChunk);
                    actualBytesRead = parser.readBytes(stream, data, chunkLength - useOffsetWithinChunk);
                    offsetWithinChunk = 0;
                    buffer.setTimeStamp(getTimeStamp());
                }
                buffer.setLength(actualBytesRead);
                long frameDuration = -1L;
                if(trakInfo.trackType.equals("vids"))
                {
                    if(nanoSecPerFrame > 0L)
                        frameDuration = nanoSecPerFrame;
                    if(trakInfo.indexToKeyframeIndex.length == 0 || useChunkNumber == trakInfo.indexToKeyframeIndex[useChunkNumber])
                        buffer.setFlags(buffer.getFlags() | 0x10);
                }
                buffer.setDuration(frameDuration);
                buffer.setSequenceNumber(++sequenceNumber);
            }
            catch(IOException e)
            {
                buffer.setLength(0);
                buffer.setEOM(true);
            }
            synchronized(this)
            {
                if(chunkNumber == useChunkNumber)
                    chunkNumber++;
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

        abstract long getTimeStamp();

        protected TrakList trakInfo;
        private boolean enabled;
        private int numBuffers;
        private Format format;
        private long sequenceNumber;
        private int chunkNumber;
        protected int useChunkNumber;
        protected int offsetWithinChunk;
        protected int useOffsetWithinChunk;
        private AviParser parser;
        private AVIIndexEntry indexEntry;
        private Object header;
        private TrackListener listener;

        MediaTrack(TrakList trakInfo)
        {
            enabled = true;
            numBuffers = 4;
            sequenceNumber = 0L;
            chunkNumber = 0;
            useChunkNumber = 0;
            offsetWithinChunk = -1;
            useOffsetWithinChunk = 0;
            parser = AviParser.this;
            header = null;
            this.trakInfo = trakInfo;
            format = trakInfo.media.createFormat();
        }
    }

    private class TrakList
    {

        int getChunkNumber(int offset)
        {
            for(int i = 0; i < maxChunkIndex; i++)
                if(offset < chunkInfo[i].cumulativeChunkLength)
                    return i;

            return maxChunkIndex;
        }

        Time duration;
        String trackType;
        String streamHandler;
        int flags;
        int priority;
        int initialFrames;
        int scale;
        int rate;
        int start;
        int length;
        int suggestedBufferSize;
        int quality;
        int sampleSize;
        Media media;
        boolean supported;
        AVIIndexEntry chunkInfo[];
        int maxChunkIndex;
        int indexToKeyframeIndex[];
        int keyFrames[];
        int numKeyFrames;
        int tmpCumulativeChunkLength;

        private TrakList()
        {
            duration = Duration.DURATION_UNKNOWN;
            supported = true;
            chunkInfo = new AVIIndexEntry[0];
            maxChunkIndex = 0;
            indexToKeyframeIndex = new int[0];
            keyFrames = new int[0];
            numKeyFrames = 0;
            tmpCumulativeChunkLength = 0;
        }

    }

    private class Video extends Media
    {

        Format createFormat()
        {
            if(format != null)
                return format;
            if(usecPerFrame != 0)
                format = bitMapInfo.createVideoFormat(Format.byteArray, (float)((1.0D / (double)usecPerFrame) * 1000000D));
            else
                format = bitMapInfo.createVideoFormat(Format.byteArray);
            return format;
        }

        public String toString()
        {
            System.out.println("size is " + size);
            System.out.println("width is " + width);
            System.out.println("height is " + height);
            System.out.println("planes is " + planes);
            System.out.println("depth is " + depth);
            System.out.println("compressor is " + compressor);
            return super.toString();
        }

        int size;
        int width;
        int height;
        int planes;
        int depth;
        String compressor;
        VideoFormat format;
        BitMapInfo bitMapInfo;

        private Video()
        {
            format = null;
            bitMapInfo = null;
        }

    }

    private class Audio extends Media
    {

        Format createFormat()
        {
            if(format != null)
                return format;
            String encodingString = (String)WavAudioFormat.formatMapper.get(new Integer(formatTag));
            if(encodingString == null)
                encodingString = "unknown";
            boolean signed;
            if(bitsPerSample > 8)
                signed = true;
            else
                signed = false;
            format = new WavAudioFormat(encodingString, sampleRate, bitsPerSample, channels, blockAlign * 8, avgBytesPerSec, 0, signed ? 1 : 0, -1F, Format.byteArray, codecSpecificHeader);
            return format;
        }

        public String toString()
        {
            System.out.println("Audio Media: " + format);
            System.out.println("Number of channels " + channels);
            System.out.println("average bytes per second " + avgBytesPerSec);
            System.out.println("sampleRate " + sampleRate);
            System.out.println("blockAlign " + blockAlign);
            System.out.println("bitsPerSample " + bitsPerSample);
            System.out.println("formatTag " + formatTag);
            return super.toString();
        }

        int formatTag;
        int channels;
        int sampleRate;
        int avgBytesPerSec;
        int blockAlign;
        int bitsPerSample;
        int samplesPerBlock;
        AudioFormat format;

        private Audio()
        {
            format = null;
        }

    }

    private abstract class Media
    {

        abstract Format createFormat();

        int maxSampleSize;

        private Media()
        {
        }

    }


    public AviParser()
    {
        stream = null;
        numSupportedTracks = 0;
        audioTrack = -1;
        videoTrack = -1;
        keyFrameTrack = -1;
        usecPerFrame = 0;
        nanoSecPerFrame = 0L;
        totalFrames = 0;
        numTracks = 0;
        moviOffset = 0;
        duration = Duration.DURATION_UNKNOWN;
        moviChunkSeen = false;
        idx1ChunkSeen = false;
        maxAudioChunkIndex = 0;
        maxVideoChunkIndex = 0;
        extraHeaderLength = 0;
        codecSpecificHeader = null;
        seekSync = new Object();
    }

    protected boolean supports(SourceStream streams[])
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
        readHeader();
        if(!moviChunkSeen)
            throw new BadHeaderException("No movi chunk");
        if(!idx1ChunkSeen)
            throw new BadHeaderException("Currently files with no idx1 chunk are not supported");
        if(numTracks <= 0)
            throw new BadHeaderException("Error parsing header");
        tracks = new Track[numTracks];
        for(int i = 0; i < tracks.length; i++)
        {
            TrakList trakInfo = trakList[i];
            if(trakInfo.trackType.equals("auds"))
                tracks[i] = new AudioTrack(trakInfo);
            else
            if(trakInfo.trackType.equals("vids"))
                tracks[i] = new VideoTrack(trakInfo);
        }

        return tracks;
    }

    private void readHeader()
        throws IOException, BadHeaderException
    {
        String magicRIFF = readString(stream);
        if(!magicRIFF.equals("RIFF"))
            throw new BadHeaderException("AVI Parser: expected string RIFF, got " + magicRIFF);
        length = readInt(stream, false);
        length += 8;
        String magicAVI = readString(stream);
        if(!magicAVI.equals("AVI "))
            throw new BadHeaderException("AVI Parser: expected string AVI, got " + magicAVI);
        int currentTrack = 0;
        while(getLocation(stream) <= (long)(length - 12)) 
        {
            String next = readString(stream);
            int subchunkLength = readInt(stream, false);
            if(next.equals("LIST"))
            {
                String subchunk = readString(stream);
                if(subchunk.equals("hdrl"))
                    parseHDRL();
                else
                if(subchunk.equals("strl"))
                {
                    parseSTRL(subchunkLength, currentTrack);
                    currentTrack++;
                } else
                if(subchunk.equals("movi"))
                    parseMOVI(subchunkLength - 4);
                else
                    skip(stream, subchunkLength - 4);
            } else
            if(next.equals("idx1"))
            {
                parseIDX1(subchunkLength);
            } else
            {
                skip(stream, subchunkLength);
                if((subchunkLength & 1) > 0)
                    skip(stream, 1);
            }
        }
        if(totalFrames != 0 && usecPerFrame != 0)
            duration = new Time((long)usecPerFrame * (long)totalFrames * 1000L);
    }

    private long getLocation()
    {
        return getLocation(stream);
    }

    private void parseHDRL()
        throws BadHeaderException
    {
        try
        {
            String next = readString(stream);
            if(!next.equals("avih"))
                throw new BadHeaderException("AVI Parser: expected string AVIH, got " + next);
            int headerLength = readInt(stream, false);
            parseAVIH(headerLength);
            trakList = new TrakList[numTracks];
        }
        catch(IOException e)
        {
            throw new BadHeaderException("IOException when parsing hdrl");
        }
    }

    private void parseSTRL(int length, int currentTrack)
        throws BadHeaderException
    {
        try
        {
            if(currentTrack >= trakList.length)
                throw new BadHeaderException("inconsistent number of strl atoms");
            int subchunkLength;
            for(length -= 12; length >= 12; length -= subchunkLength + 4)
            {
                String subchunkid = readString(stream);
                subchunkLength = readInt(stream, false);
                if(subchunkid.equals("strh"))
                    parseSTRH(subchunkLength, currentTrack);
                else
                if(subchunkid.equals("strf"))
                {
                    if(trakList[currentTrack] == null)
                        throw new BadHeaderException("strf doesn't have a strh atom preceding it");
                    parseSTRF(subchunkLength, currentTrack);
                } else
                {
                    if((subchunkLength & 1) > 0)
                        subchunkLength++;
                    skip(stream, subchunkLength);
                }
            }

        }
        catch(IOException e)
        {
            throw new BadHeaderException("IOException when parsing hdrl");
        }
    }

    private void parseSTRH(int length, int currentTrack)
        throws BadHeaderException
    {
        try
        {
            if(length < 56)
                throw new BadHeaderException("strh: header length should be atleast 56 but is " + length);
            trakList[currentTrack] = new TrakList();
            trakList[currentTrack].trackType = readString(stream);
            trakList[currentTrack].streamHandler = readString(stream);
            trakList[currentTrack].flags = readInt(stream, false);
            trakList[currentTrack].priority = readInt(stream, false);
            trakList[currentTrack].initialFrames = readInt(stream, false);
            trakList[currentTrack].scale = readInt(stream, false);
            trakList[currentTrack].rate = readInt(stream, false);
            trakList[currentTrack].start = readInt(stream, false);
            trakList[currentTrack].length = readInt(stream, false);
            trakList[currentTrack].suggestedBufferSize = readInt(stream, false);
            trakList[currentTrack].quality = readInt(stream, false);
            trakList[currentTrack].sampleSize = readInt(stream, false);
            skip(stream, 8);
            if(length - 56 > 0)
                skip(stream, length - 56);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("IOException when parsing hdrl");
        }
    }

    private void parseSTRF(int length, int currentTrack)
        throws BadHeaderException
    {
        try
        {
            String trackType = trakList[currentTrack].trackType;
            if(trackType.equals("vids"))
            {
                Video video = new Video();
                video.size = readInt(stream, false);
                video.width = readInt(stream, false);
                video.height = readInt(stream, false);
                video.planes = readShort(stream, false);
                video.depth = readShort(stream, false);
                byte intArray[] = new byte[4];
                readBytes(stream, intArray, 4);
                if(intArray[0] > 32)
                    video.compressor = new String(intArray);
                else
                    switch(intArray[0])
                    {
                    case 0: // '\0'
                        video.compressor = "rgb";
                        break;

                    case 1: // '\001'
                        video.compressor = "rle8";
                        break;

                    case 2: // '\002'
                        video.compressor = "rle4";
                        break;

                    case 3: // '\003'
                        video.compressor = "rgb";
                        break;
                    }
                BitMapInfo bmi = new BitMapInfo();
                bmi.biWidth = video.width;
                bmi.biHeight = video.height;
                bmi.biPlanes = video.planes;
                bmi.biBitCount = video.depth;
                bmi.fourcc = new String(video.compressor);
                video.bitMapInfo = bmi;
                bmi.biSizeImage = readInt(stream, false);
                bmi.biXPelsPerMeter = readInt(stream, false);
                bmi.biYPelsPerMeter = readInt(stream, false);
                bmi.biClrUsed = readInt(stream, false);
                bmi.biClrImportant = readInt(stream, false);
                if(length - 40 > 0)
                {
                    bmi.extraSize = length - 40;
                    bmi.extraBytes = new byte[bmi.extraSize];
                    readBytes(stream, bmi.extraBytes, bmi.extraSize);
                }
                trakList[currentTrack].media = video;
                trakList[currentTrack].media.maxSampleSize = trakList[currentTrack].suggestedBufferSize;
                videoTrack = currentTrack;
            } else
            if(trackType.equals("auds"))
            {
                Audio audio = new Audio();
                audio.formatTag = readShort(stream, false);
                audio.channels = readShort(stream, false);
                audio.sampleRate = readInt(stream, false);
                audio.avgBytesPerSec = readInt(stream, false);
                audio.blockAlign = readShort(stream, false);
                audio.bitsPerSample = readShort(stream, false);
                int remainingFormatSize = length - 16;
                codecSpecificHeader = null;
                int extraFieldsSize = 0;
                if(remainingFormatSize >= 2)
                {
                    extraFieldsSize = readShort(stream, false);
                    remainingFormatSize -= 2;
                    if(extraFieldsSize > 0)
                    {
                        codecSpecificHeader = new byte[extraFieldsSize];
                        readBytes(stream, codecSpecificHeader, codecSpecificHeader.length);
                        remainingFormatSize -= extraFieldsSize;
                    }
                    if(audio.formatTag == 2 || audio.formatTag == 17 || audio.formatTag == 49)
                    {
                        if(extraFieldsSize < 2)
                            throw new BadHeaderException("samplesPerBlock field not available for encoding" + audio.formatTag);
                        audio.samplesPerBlock = BasicPullParser.parseShortFromArray(codecSpecificHeader, false);
                    }
                }
                if(remainingFormatSize < 0)
                    throw new BadHeaderException("Avi Parser: incorrect headersize in the STRF");
                if(remainingFormatSize > 0)
                    skip(stream, length - 16);
                trakList[currentTrack].media = audio;
                audioTrack = currentTrack;
            } else
            {
                throw new BadHeaderException("strf: unsupported stream type " + trackType);
            }
        }
        catch(IOException e)
        {
            throw new BadHeaderException("IOException when parsing hdrl");
        }
    }

    private void parseAVIH(int length)
        throws BadHeaderException
    {
        try
        {
            if(length < 56)
                throw new BadHeaderException("avih: header size is not 56");
            usecPerFrame = readInt(stream, false);
            nanoSecPerFrame = usecPerFrame * 1000;
            maxBytesPerSecond = readInt(stream, false);
            paddingGranularity = readInt(stream, false);
            flags = readInt(stream, false);
            totalFrames = readInt(stream, false);
            initialFrames = readInt(stream, false);
            numTracks = readInt(stream, false);
            suggestedBufferSize = readInt(stream, false);
            width = readInt(stream, false);
            height = readInt(stream, false);
            skip(stream, 16);
            if(length - 56 > 0)
                skip(stream, length - 56);
        }
        catch(IOException e)
        {
            throw new BadHeaderException("IOException when parsing hdrl");
        }
    }

    private void parseIDX1(int length)
        throws BadHeaderException
    {
        try
        {
            if(!moviChunkSeen)
                throw new BadHeaderException("idx1 chunk appears before movi chunk");
            int numIndices = length / 16;
            for(int i = 0; i < numTracks; i++)
            {
                if(trakList[i] == null)
                    throw new BadHeaderException("Bad file format");
                trakList[i].chunkInfo = new AVIIndexEntry[numIndices];
                if(trakList[i].trackType.equals("vids"))
                    trakList[i].keyFrames = new int[numIndices];
            }

            idx1MinimumChunkOffset = 0x7fffffff;
            for(int i = 0; i < numIndices; i++)
            {
                String id = readString(stream);
                if(id.equals("rec "))
                {
                    readInt(stream, false);
                    readInt(stream, false);
                    readInt(stream, false);
                    continue;
                }
                int streamNumber;
                try
                {
                    streamNumber = Integer.parseInt(id.substring(0, 2));
                }
                catch(NumberFormatException e)
                {
                    readInt(stream, false);
                    readInt(stream, false);
                    readInt(stream, false);
                    continue;
                }
                if(streamNumber < 0 || streamNumber >= numTracks)
                    throw new BadHeaderException("index chunk has illegal stream # " + streamNumber);
                int flag = readInt(stream, false);
                int chunkOffset = readInt(stream, false);
                int chunkLength = readInt(stream, false);
                AVIIndexEntry chunkInfo[] = trakList[streamNumber].chunkInfo;
                int index = trakList[streamNumber].maxChunkIndex;
                chunkInfo[index] = new AVIIndexEntry();
                chunkInfo[index].id = id;
                chunkInfo[index].flag = flag;
                chunkInfo[index].chunkOffset = chunkOffset;
                chunkInfo[index].chunkLength = chunkLength;
                if(trakList[streamNumber].trackType.equals("auds"))
                {
                    int c = trakList[streamNumber].tmpCumulativeChunkLength += chunkLength;
                    chunkInfo[index].cumulativeChunkLength = c;
                }
                if(trakList[streamNumber].trackType.equals("vids") && (flag & 0x10) > 0)
                {
                    int keyFrameIndex = trakList[streamNumber].numKeyFrames;
                    trakList[streamNumber].keyFrames[keyFrameIndex] = index;
                    trakList[streamNumber].numKeyFrames++;
                }
                trakList[streamNumber].maxChunkIndex++;
                if(chunkOffset < idx1MinimumChunkOffset)
                    idx1MinimumChunkOffset = chunkOffset;
            }

            for(int i = 0; i < numTracks; i++)
                if(trakList[i].trackType.equals("vids"))
                {
                    int numKeyFrames = trakList[i].numKeyFrames;
                    if(numKeyFrames > 0)
                        keyFrameTrack = i;
                    int maxChunkIndex = trakList[i].maxChunkIndex;
                    if(numKeyFrames > 0 && numKeyFrames < maxChunkIndex)
                        trakList[i].indexToKeyframeIndex = buildIndexToKeyFrameIndexTable(trakList[i].keyFrames, numKeyFrames, maxChunkIndex);
                    trakList[i].keyFrames = null;
                }

            if(idx1MinimumChunkOffset >= moviOffset)
                moviOffset = 0;
            moviOffset += 8;
        }
        catch(IOException e)
        {
            throw new BadHeaderException("IOException when parsing IDX1");
        }
        idx1ChunkSeen = true;
    }

    private void parseMOVI(int length)
        throws BadHeaderException
    {
        try
        {
            moviChunkSeen = true;
            if((flags & 0x10) > 0)
            {
                moviOffset = (int)getLocation(stream) - 4;
                skip(stream, length);
            }
        }
        catch(IOException e)
        {
            throw new BadHeaderException("IOException when parsing movi");
        }
    }

    public Time setPosition(Time where, int rounding)
    {
        int keyframeNum = -1;
        if(keyFrameTrack != -1 && tracks[keyFrameTrack].isEnabled())
        {
            TrakList trakInfo = trakList[keyFrameTrack];
            Track track = tracks[keyFrameTrack];
            int frameNum = track.mapTimeToFrame(where);
            keyframeNum = frameNum;
            if(trakInfo.indexToKeyframeIndex.length > frameNum)
                keyframeNum = trakInfo.indexToKeyframeIndex[frameNum];
            if(keyframeNum != frameNum)
                where = track.mapFrameToTime(keyframeNum);
        }
        for(int i = 0; i < numTracks; i++)
        {
            if(!tracks[i].isEnabled())
                continue;
            int chunkNumber = 0;
            int offsetWithinChunk = 0;
            try
            {
                if(i == keyFrameTrack)
                {
                    chunkNumber = keyframeNum;
                    continue;
                }
                TrakList trakInfo = trakList[i];
                if(trakInfo.trackType.equals("vids"))
                {
                    if(usecPerFrame != 0)
                    {
                        chunkNumber = (int)(where.getNanoseconds() / nanoSecPerFrame);
                        if(chunkNumber < 0)
                            chunkNumber = 0;
                        else
                        if(chunkNumber >= trakInfo.maxChunkIndex)
                            continue;
                    }
                } else
                if(trakInfo.trackType.equals("auds"))
                {
                    int bytePos = (int)(where.getSeconds() * (double)((Audio)trakInfo.media).avgBytesPerSec);
                    if(bytePos < 0)
                        bytePos = 0;
                    if(trakInfo.maxChunkIndex == 1)
                    {
                        if(bytePos >= trakInfo.chunkInfo[0].chunkLength)
                        {
                            chunkNumber = trakInfo.maxChunkIndex;
                            continue;
                        }
                        chunkNumber = 0;
                        offsetWithinChunk = bytePos;
                    } else
                    {
                        chunkNumber = trakInfo.getChunkNumber(bytePos);
                        if(chunkNumber >= trakInfo.maxChunkIndex)
                            continue;
                        int approx = trakInfo.chunkInfo[chunkNumber].cumulativeChunkLength - trakInfo.chunkInfo[chunkNumber].chunkLength;
                        offsetWithinChunk = bytePos - approx;
                    }
                    if((offsetWithinChunk & 1) > 0)
                        offsetWithinChunk--;
                    int blockAlign = ((Audio)trakInfo.media).blockAlign;
                    if(blockAlign != 0)
                        offsetWithinChunk -= offsetWithinChunk % blockAlign;
                }
                continue;
            }
            finally
            {
                ((MediaTrack)tracks[i]).setChunkNumberAndOffset(chunkNumber, offsetWithinChunk);
            }
        }

        return where;
    }

    public Time getMediaTime()
    {
        return null;
    }

    public Time getDuration()
    {
        return duration;
    }

    public String getName()
    {
        return "Parser for avi file format";
    }

    private boolean isSupported(String trackType)
    {
        return trackType.equals("vids") || trackType.equals("auds");
    }

    private int[] buildIndexToKeyFrameIndexTable(int syncSamples[], int numKeyFrames, int numberOfSamples)
    {
        int syncSampleMapping[] = new int[numberOfSamples];
        int index = 0;
        int previous;
        if(syncSamples[0] != 0)
        {
            previous = syncSampleMapping[0] = 0;
        } else
        {
            previous = syncSampleMapping[0] = 0;
            index++;
        }
        for(; index < numKeyFrames; index++)
        {
            int next = syncSamples[index];
            for(int j = previous + 1; j < next; j++)
                syncSampleMapping[j] = previous;

            syncSampleMapping[next] = next;
            previous = next;
        }

        int lastSyncFrame = syncSamples[numKeyFrames - 1];
        for(index = lastSyncFrame + 1; index < numberOfSamples; index++)
            syncSampleMapping[index] = lastSyncFrame;

        return syncSampleMapping;
    }

    private static ContentDescriptor supportedFormat[] = {
        new ContentDescriptor("video.x_msvideo")
    };
    private PullSourceStream stream;
    private CachedStream cacheStream;
    private Track tracks[];
    private Seekable seekableStream;
    private int numSupportedTracks;
    private int length;
    private int audioTrack;
    private int videoTrack;
    private int keyFrameTrack;
    private static final int SIZE_OF_AVI_INDEX = 16;
    private static final int AVIH_HEADER_LENGTH = 56;
    private static final int STRH_HEADER_LENGTH = 56;
    private static final int STRF_VIDEO_HEADER_LENGTH = 40;
    private static final int STRF_AUDIO_HEADER_LENGTH = 16;
    static final int AVIF_HASINDEX = 16;
    static final int AVIF_MUSTUSEINDEX = 32;
    static final int AVIF_ISINTERLEAVED = 256;
    static final int AVIF_WASCAPTUREFILE = 0x10000;
    static final int AVIF_COPYRIGHTED = 0x20000;
    static final int AVIF_KEYFRAME = 16;
    static final String AUDIO = "auds";
    static final String VIDEO = "vids";
    static final String LISTRECORDCHUNK = "rec ";
    static final String VIDEO_MAGIC = "dc";
    static final String VIDEO_MAGIC_JPEG = "db";
    static final String VIDEO_MAGIC_IV32a = "iv";
    static final String VIDEO_MAGIC_IV32b = "32";
    static final String VIDEO_MAGIC_IV31 = "31";
    static final String VIDEO_MAGIC_CVID = "id";
    static final String AUDIO_MAGIC = "wb";
    private int usecPerFrame;
    private long nanoSecPerFrame;
    private int maxBytesPerSecond;
    private int paddingGranularity;
    private int flags;
    private int totalFrames;
    private int initialFrames;
    private int numTracks;
    private int suggestedBufferSize;
    private int width;
    private int height;
    private TrakList trakList[];
    private int idx1MinimumChunkOffset;
    private int moviOffset;
    private Time duration;
    private boolean moviChunkSeen;
    private boolean idx1ChunkSeen;
    private int maxAudioChunkIndex;
    private int maxVideoChunkIndex;
    private int extraHeaderLength;
    private byte codecSpecificHeader[];
    private Object seekSync;








}
