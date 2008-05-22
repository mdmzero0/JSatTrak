/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   Buffer.java

package javax.media;


// Referenced classes of package javax.media:
//            Format

public class Buffer
{

    public Buffer()
    {
        timeStamp = -1L;
        duration = -1L;
        format = null;
        flags = 0;
        data = null;
        header = null;
        length = 0;
        offset = 0;
        sequenceNumber = 0x7ffffffffffffffeL;
    }

    public Format getFormat()
    {
        return format;
    }

    public void setFormat(Format format)
    {
        this.format = format;
    }

    public int getFlags()
    {
        return flags;
    }

    public void setFlags(int flags)
    {
        this.flags = flags;
    }

    public boolean isEOM()
    {
        return (flags & 1) != 0;
    }

    public void setEOM(boolean eom)
    {
        if(eom)
            flags |= 1;
        else
            flags &= -2;
    }

    public boolean isDiscard()
    {
        return (flags & 2) != 0;
    }

    public void setDiscard(boolean discard)
    {
        if(discard)
            flags |= 2;
        else
            flags &= -3;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }

    public Object getHeader()
    {
        return header;
    }

    public void setHeader(Object header)
    {
        this.header = header;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public long getDuration()
    {
        return duration;
    }

    public void setDuration(long duration)
    {
        this.duration = duration;
    }

    public void setSequenceNumber(long number)
    {
        sequenceNumber = number;
    }

    public long getSequenceNumber()
    {
        return sequenceNumber;
    }

    public void copy(Buffer buffer)
    {
        copy(buffer, false);
    }

    public void copy(Buffer buffer, boolean swapData)
    {
        if(swapData)
        {
            Object temp = data;
            data = buffer.data;
            buffer.data = temp;
        } else
        {
            data = buffer.data;
        }
        header = buffer.header;
        format = buffer.format;
        length = buffer.length;
        offset = buffer.offset;
        timeStamp = buffer.timeStamp;
        duration = buffer.duration;
        sequenceNumber = buffer.sequenceNumber;
        flags = buffer.flags;
    }

    public Object clone()
    {
        Buffer buf = new Buffer();
        Object data = getData();
        if(data != null)
            if(data instanceof byte[])
                buf.data = ((byte[])data).clone();
            else
            if(data instanceof int[])
                buf.data = ((int[])data).clone();
            else
            if(data instanceof short[])
                buf.data = ((short[])data).clone();
            else
                buf.data = data;
        if(header != null)
            if(header instanceof byte[])
                buf.header = ((byte[])header).clone();
            else
            if(header instanceof int[])
                buf.header = ((int[])header).clone();
            else
            if(header instanceof short[])
                buf.header = ((short[])header).clone();
            else
                buf.header = header;
        buf.format = format;
        buf.length = length;
        buf.offset = offset;
        buf.timeStamp = timeStamp;
        buf.duration = duration;
        buf.sequenceNumber = sequenceNumber;
        buf.flags = flags;
        return buf;
    }

    protected long timeStamp;
    protected long duration;
    protected Format format;
    protected int flags;
    protected Object data;
    protected Object header;
    protected int length;
    protected int offset;
    protected long sequenceNumber;
    public static final int FLAG_EOM = 1;
    public static final int FLAG_DISCARD = 2;
    public static final int FLAG_SILENCE = 4;
    public static final int FLAG_SID = 8;
    public static final int FLAG_KEY_FRAME = 16;
    public static final int FLAG_NO_DROP = 32;
    public static final int FLAG_NO_WAIT = 64;
    public static final int FLAG_NO_SYNC = 96;
    public static final int FLAG_SYSTEM_TIME = 128;
    public static final int FLAG_RELATIVE_TIME = 256;
    public static final int FLAG_FLUSH = 512;
    public static final int FLAG_SYSTEM_MARKER = 1024;
    public static final int FLAG_RTP_MARKER = 2048;
    public static final int FLAG_RTP_TIME = 4096;
    public static final int FLAG_BUF_OVERFLOWN = 8192;
    public static final int FLAG_BUF_UNDERFLOWN = 16384;
    public static final int FLAG_LIVE_DATA = 32768;
    public static final long TIME_UNKNOWN = -1L;
    public static final long SEQUENCE_UNKNOWN = 0x7ffffffffffffffeL;
}
