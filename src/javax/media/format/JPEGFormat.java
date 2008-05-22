package javax.media.format;

import java.awt.Dimension;
import javax.media.Format;

// Referenced classes of package javax.media.format:
//            VideoFormat

public class JPEGFormat extends VideoFormat
{

    public static final int DEC_422 = 0;
    public static final int DEC_420 = 1;
    public static final int DEC_444 = 2;
    public static final int DEC_402 = 3;
    public static final int DEC_411 = 4;
    int qFactor;
    int decimation;

    public JPEGFormat()
    {
        super("jpeg");
        qFactor = -1;
        decimation = -1;
    }

    public JPEGFormat(Dimension size, int maxDataLength, Class dataType, float frameRate, int q, int dec)
    {
        super("jpeg", size, maxDataLength, dataType, frameRate);
        qFactor = -1;
        decimation = -1;
        qFactor = q;
        decimation = dec;
    }

    public int getQFactor()
    {
        return qFactor;
    }

    public int getDecimation()
    {
        return decimation;
    }

    public Object clone()
    {
        JPEGFormat f = new JPEGFormat(getSize(), getMaxDataLength(), getDataType(), getFrameRate(), qFactor, decimation);
        f.copy(this);
        return f;
    }

    protected void copy(Format f)
    {
        super.copy(f);
        JPEGFormat jf = (JPEGFormat)f;
        qFactor = jf.qFactor;
        decimation = jf.decimation;
    }

    public String toString()
    {
        String s = getEncoding() + " video format:";
        if(super.size != null)
        {
            s = s + " size = " + super.size.width + "x" + super.size.height;
        }
        if(super.frameRate != -1F)
        {
            s = s + " FrameRate = " + super.frameRate;
        }
        if(super.maxDataLength != -1)
        {
            s = s + " maxDataLength = " + super.maxDataLength;
        }
        if(super.dataType != null)
        {
            s = s + " dataType = " + super.dataType;
        }
        if(qFactor != -1)
        {
            s = s + " q factor = " + qFactor;
        }
        if(decimation != -1)
        {
            s = s + " decimation = " + decimation;
        }
        return s;
    }

    public boolean equals(Object format)
    {
        if(format instanceof JPEGFormat)
        {
            JPEGFormat vf = (JPEGFormat)format;
            return super.equals(format) && qFactor == vf.qFactor && decimation == vf.decimation;
        } else
        {
            return false;
        }
    }

    public boolean matches(Format format)
    {
        if(!super.matches(format))
        {
            return false;
        }
        if(!(format instanceof JPEGFormat))
        {
            return true;
        } else
        {
            JPEGFormat vf = (JPEGFormat)format;
            return (qFactor == -1 || vf.qFactor == -1 || qFactor == vf.qFactor) && (decimation == -1 || vf.decimation == -1 || decimation == vf.decimation);
        }
    }

    public Format intersects(Format format)
    {
        Format fmt;
        if((fmt = super.intersects(format)) == null)
        {
            return null;
        }
        if(!(format instanceof JPEGFormat))
        {
            return fmt;
        } else
        {
            JPEGFormat other = (JPEGFormat)format;
            JPEGFormat res = (JPEGFormat)fmt;
            res.qFactor = qFactor == -1 ? other.qFactor : qFactor;
            res.decimation = decimation == -1 ? other.decimation : decimation;
            return res;
        }
    }
}
