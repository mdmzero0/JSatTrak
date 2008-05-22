package javax.media.protocol;

import javax.media.Format;

public class ContentDescriptor extends Format
{

    public static final String RAW = "raw";
    public static final String RAW_RTP = "raw.rtp";
    public static final String MIXED = "application.mixed-data";
    public static final String CONTENT_UNKNOWN = "UnknownContent";

    public String getContentType()
    {
        return getEncoding();
    }

    public ContentDescriptor(String cdName)
    {
        super(cdName);
    }

    public String toString()
    {
        if(super.encoding.equalsIgnoreCase("raw"))
        {
            return "RAW";
        }
        if(super.encoding.equalsIgnoreCase("raw.rtp"))
        {
            return "RAW/RTP";
        }
        if(super.encoding.equalsIgnoreCase("audio.cdaudio"))
        {
            return "CD Audio";
        } else
        {
            return super.encoding;
        }
    }

    public static final String mimeTypeToPackageName(String mimeType)
    {
        if(mimeType == null)
        {
            return null;
        }
        mimeType = mimeType.toLowerCase();
        int len = mimeType.length();
        char nm[] = new char[len];
        mimeType.getChars(0, len, nm, 0);
        for(int i = 0; i < len; i++)
        {
            char c = nm[i];
            if(c == '/')
            {
                nm[i] = '.';
            } else
            if(c != '.' && ('A' > c || c > 'Z') && ('a' > c || c > 'z') && ('0' > c || c > '9'))
            {
                nm[i] = '_';
            }
        }

        return new String(nm);
    }
}
