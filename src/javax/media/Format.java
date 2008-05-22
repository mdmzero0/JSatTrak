package javax.media;

import java.io.Serializable;

public class Format
    implements Cloneable, Serializable
{

    public static final int NOT_SPECIFIED = -1;
    public static final int TRUE = 1;
    public static final int FALSE = 0;
    protected String encoding;
    public static final Class intArray = (new int[0]).getClass();
    public static final Class shortArray = (new short[0]).getClass();
    public static final Class byteArray = (new byte[0]).getClass();
    public static final Class formatArray = (new Format[0]).getClass();
    protected Class dataType;
    protected Class clz;
    private long encodingCode;

    public Format(String encoding)
    {
        dataType = byteArray;
        clz = getClass();
        encodingCode = 0L;
        this.encoding = encoding;
    }

    public Format(String encoding, Class dataType)
    {
        this(encoding);
        this.dataType = dataType;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public Class getDataType()
    {
        return dataType;
    }

    public boolean equals(Object format)
    {
        if(format == null || clz != ((Format)format).clz)
        {
            return false;
        } else
        {
            String otherEncoding = ((Format)format).encoding;
            Class otherType = ((Format)format).dataType;
            return dataType == otherType && (encoding == otherEncoding || encoding != null && otherEncoding != null && isSameEncoding((Format)format));
        }
    }

    public boolean matches(Format format)
    {
        if(format == null)
        {
            return false;
        } else
        {
            return (format.encoding == null || encoding == null || isSameEncoding(format)) && (format.dataType == null || dataType == null || format.dataType == dataType) && (clz.isAssignableFrom(format.clz) || format.clz.isAssignableFrom(clz));
        }
    }

    public Format intersects(Format other)
    {
        Format res;
        if(clz.isAssignableFrom(other.clz))
        {
            res = (Format)other.clone();
        } else
        if(other.clz.isAssignableFrom(clz))
        {
            res = (Format)clone();
        } else
        {
            return null;
        }
        if(res.encoding == null)
        {
            res.encoding = encoding == null ? other.encoding : encoding;
        }
        if(res.dataType == null)
        {
            res.dataType = dataType == null ? other.dataType : dataType;
        }
        return res;
    }

    public boolean isSameEncoding(Format other)
    {
        if(encoding == null || other == null || other.encoding == null)
        {
            return false;
        }
        if(encoding == other.encoding)
        {
            return true;
        }
        if(encodingCode > 0L && other.encodingCode > 0L)
        {
            return encodingCode == other.encodingCode;
        }
        if(encoding.length() > 10)
        {
            return encoding.equalsIgnoreCase(other.encoding);
        }
        if(encodingCode == 0L)
        {
            encodingCode = getEncodingCode(encoding);
        }
        if(encodingCode <= 0L)
        {
            return encoding.equalsIgnoreCase(other.encoding);
        }
        if(other.encodingCode == 0L)
        {
            return other.isSameEncoding(this);
        } else
        {
            return encodingCode == other.encodingCode;
        }
    }

    public boolean isSameEncoding(String encoding)
    {
        if(this.encoding == null || encoding == null)
        {
            return false;
        }
        if(this.encoding == encoding)
        {
            return true;
        }
        if(this.encoding.length() > 10)
        {
            return this.encoding.equalsIgnoreCase(encoding);
        }
        if(encodingCode == 0L)
        {
            encodingCode = getEncodingCode(this.encoding);
        }
        if(encodingCode < 0L)
        {
            return this.encoding.equalsIgnoreCase(encoding);
        } else
        {
            long otherEncodingCode = getEncodingCode(encoding);
            return encodingCode == otherEncodingCode;
        }
    }

    private long getEncodingCode(String enc)
    {
        byte chars[] = enc.getBytes();
        long code = 0L;
        for(int i = 0; i < enc.length(); i++)
        {
            byte b = chars[i];
            if(b > 96 && b < 123)
            {
                b -= 32;
            }
            b -= 32;
            if(b > 63)
            {
                return -1L;
            }
            code = code << 6 | (long)b;
        }

        return code;
    }

    public Format relax()
    {
        return (Format)clone();
    }

    public Object clone()
    {
        Format f = new Format(encoding);
        f.copy(this);
        return f;
    }

    protected void copy(Format f)
    {
        dataType = f.dataType;
    }

    public String toString()
    {
        return getEncoding();
    }

}
