// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   BasicPullParser.java

package com.sun.media.parser;

import com.sun.media.BasicPlugIn;
import java.io.IOException;
import javax.media.*;
import javax.media.protocol.*;

public abstract class BasicPullParser extends BasicPlugIn
    implements Demultiplexer
{

    public BasicPullParser()
    {
        b = new byte[1];
        intArray = new byte[4];
        shortArray = new byte[2];
        tempBuffer = new byte[2048];
        currentLocation = 0L;
        seekable = false;
        positionable = false;
        sync = new Object();
    }

    public void setSource(DataSource source)
        throws IOException, IncompatibleSourceException
    {
        if(!(source instanceof PullDataSource))
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        streams = ((PullDataSource)source).getStreams();
        if(streams == null)
            throw new IOException("Got a null stream from the DataSource");
        if(streams.length == 0)
            throw new IOException("Got a empty stream array from the DataSource");
        this.source = source;
        streams = streams;
        positionable = streams[0] instanceof Seekable;
        seekable = positionable && ((Seekable)streams[0]).isRandomAccess();
        if(!supports(streams))
            throw new IncompatibleSourceException("DataSource not supported: " + source);
        try
        {
            cacheStream = (CachedStream)streams[0];
        }
        catch(ClassCastException e)
        {
            cacheStream = null;
        }
    }

    protected boolean supports(SourceStream streams[])
    {
        return streams[0] != null && (streams[0] instanceof PullSourceStream);
    }

    public boolean isPositionable()
    {
        return positionable;
    }

    public boolean isRandomAccess()
    {
        return seekable;
    }

    public int readBytes(PullSourceStream pss, byte array[], int numBytes)
        throws IOException
    {
        return readBytes(pss, array, 0, numBytes);
    }

    public int readBytes(PullSourceStream pss, byte array[], int offset, int numBytes)
        throws IOException
    {
        if(array == null)
            throw new NullPointerException();
        if(offset < 0 || offset > array.length || numBytes < 0 || offset + numBytes > array.length || offset + numBytes < 0)
            throw new IndexOutOfBoundsException();
        if(numBytes == 0)
            return 0;
        int remainingLength = numBytes;
        int actualRead = 0;
        for(remainingLength = numBytes; remainingLength > 0;)
        {
            actualRead = pss.read(array, offset, remainingLength);
            if(actualRead == -1)
                if(offset == 0)
                    throw new IOException("BasicPullParser: readBytes(): Reached end of stream while trying to read " + numBytes + " bytes");
                else
                    return offset;
            if(actualRead == -2)
                return -2;
            if(actualRead < 0)
                throw new IOException("BasicPullParser: readBytes() read returned " + actualRead);
            remainingLength -= actualRead;
            offset += actualRead;
            synchronized(sync)
            {
                currentLocation += actualRead;
            }
        }

        return numBytes;
    }

    public int readInt(PullSourceStream pss)
        throws IOException
    {
        return readInt(pss, true);
    }

    public int readShort(PullSourceStream pss)
        throws IOException
    {
        return readShort(pss, true);
    }

    public int readByte(PullSourceStream pss)
        throws IOException
    {
        readBytes(pss, b, 1);
        return b[0];
    }

    protected int readInt(PullSourceStream pss, boolean isBigEndian)
        throws IOException
    {
        readBytes(pss, intArray, 4);
        int result;
        if(isBigEndian)
            result = (intArray[0] & 0xff) << 24 | (intArray[1] & 0xff) << 16 | (intArray[2] & 0xff) << 8 | intArray[3] & 0xff;
        else
            result = (intArray[3] & 0xff) << 24 | (intArray[2] & 0xff) << 16 | (intArray[1] & 0xff) << 8 | intArray[0] & 0xff;
        return result;
    }

    protected int parseIntFromArray(byte array[], int offset, boolean isBigEndian)
        throws IOException
    {
        int result;
        if(isBigEndian)
            result = (array[offset + 0] & 0xff) << 24 | (array[offset + 1] & 0xff) << 16 | (array[offset + 2] & 0xff) << 8 | array[offset + 3] & 0xff;
        else
            result = (array[offset + 3] & 0xff) << 24 | (array[offset + 2] & 0xff) << 16 | (array[offset + 1] & 0xff) << 8 | array[offset + 0] & 0xff;
        return result;
    }

    protected short readShort(PullSourceStream pss, boolean isBigEndian)
        throws IOException
    {
        readBytes(pss, shortArray, 2);
        int result;
        if(isBigEndian)
            result = (shortArray[0] & 0xff) << 8 | shortArray[1] & 0xff;
        else
            result = (shortArray[1] & 0xff) << 8 | shortArray[0] & 0xff;
        return (short)result;
    }

    public static final short parseShortFromArray(byte array[], boolean isBigEndian)
        throws IOException
    {
        if(array.length < 2)
            throw new IOException("Unexpected EOF");
        int result;
        if(isBigEndian)
            result = (array[0] & 0xff) << 8 | array[1] & 0xff;
        else
            result = (array[1] & 0xff) << 8 | array[0] & 0xff;
        return (short)result;
    }

    protected String readString(PullSourceStream pss)
        throws IOException
    {
        readBytes(pss, intArray, 4);
        return new String(intArray);
    }

    public void skip(PullSourceStream pss, int numBytes)
        throws IOException
    {
        if((pss instanceof Seekable) && ((Seekable)pss).isRandomAccess())
        {
            long current = ((Seekable)pss).tell();
            long newPos = current + (long)numBytes;
            ((Seekable)pss).seek(newPos);
            if(newPos != ((Seekable)pss).tell())
                throw new IOException("Seek to " + newPos + " failed");
            synchronized(sync)
            {
                currentLocation += numBytes;
            }
            return;
        }
        int remaining;
        for(remaining = numBytes; remaining > 2048; remaining -= 2048)
        {
            int bytesRead = readBytes(pss, tempBuffer, 2048);
            if(bytesRead != 2048)
                throw new IOException("BasicPullParser: End of Media reached while trying to skip " + numBytes);
        }

        if(remaining > 0)
        {
            int bytesRead = readBytes(pss, tempBuffer, remaining);
            if(bytesRead != remaining)
                throw new IOException("BasicPullParser: End of Media reached while trying to skip " + numBytes);
        }
        synchronized(sync)
        {
            currentLocation += numBytes;
        }
    }

    public final long getLocation(PullSourceStream pss)
    {
        long l1;
        synchronized(sync)
        {
            if(pss instanceof Seekable)
            {
                long l = ((Seekable)pss).tell();
                return l;
            }
            l1 = currentLocation;
        }
        return l1;
    }

    public abstract ContentDescriptor[] getSupportedInputContentDescriptors();

    public void open()
    {
    }

    public void close()
    {
        if(source != null)
        {
            try
            {
                source.stop();
                source.disconnect();
            }
            catch(IOException e) { }
            source = null;
        }
    }

    public void start()
        throws IOException
    {
        if(source != null)
            source.start();
    }

    public void stop()
    {
        if(source != null)
            try
            {
                source.stop();
            }
            catch(IOException e) { }
    }

    public void reset()
    {
    }

    public abstract Time getDuration();

    public abstract Time getMediaTime();

    public abstract Time setPosition(Time time, int i);

    public abstract Track[] getTracks()
        throws IOException, BadHeaderException;

    protected DataSource source;
    protected SourceStream streams[];
    private Format outputFormats[];
    private byte b[];
    private byte intArray[];
    private byte shortArray[];
    private final int TEMP_BUFFER_LENGTH = 2048;
    private byte tempBuffer[];
    private long currentLocation;
    protected boolean seekable;
    protected boolean positionable;
    protected CachedStream cacheStream;
    private Object sync;
}
