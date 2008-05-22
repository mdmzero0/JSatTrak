package javax.media.protocol;

import javax.media.Format;

// Referenced classes of package javax.media.protocol:
//            ContentDescriptor

public class FileTypeDescriptor extends ContentDescriptor
{

    public static final String QUICKTIME = "video.quicktime";
    public static final String MSVIDEO = "video.x_msvideo";
    public static final String MPEG = "video.mpeg";
    public static final String VIVO = "video.vivo";
    public static final String BASIC_AUDIO = "audio.basic";
    public static final String WAVE = "audio.x_wav";
    public static final String AIFF = "audio.x_aiff";
    public static final String MIDI = "audio.midi";
    public static final String RMF = "audio.rmf";
    public static final String GSM = "audio.x_gsm";
    public static final String MPEG_AUDIO = "audio.mpeg";

    public FileTypeDescriptor(String contentType)
    {
        super(contentType);
    }

    public String toString()
    {
        if(super.encoding.equalsIgnoreCase("video.quicktime"))
        {
            return "QuickTime";
        }
        if(super.encoding.equalsIgnoreCase("video.x_msvideo"))
        {
            return "AVI";
        }
        if(super.encoding.equalsIgnoreCase("video.mpeg"))
        {
            return "MPEG Video";
        }
        if(super.encoding.equalsIgnoreCase("video.vivo"))
        {
            return "Vivo";
        }
        if(super.encoding.equalsIgnoreCase("audio.basic"))
        {
            return "Basic Audio (au)";
        }
        if(super.encoding.equalsIgnoreCase("audio.x_wav"))
        {
            return "WAV";
        }
        if(super.encoding.equalsIgnoreCase("audio.x_aiff"))
        {
            return "AIFF";
        }
        if(super.encoding.equalsIgnoreCase("audio.midi"))
        {
            return "MIDI";
        }
        if(super.encoding.equalsIgnoreCase("audio.rmf"))
        {
            return "RMF";
        }
        if(super.encoding.equalsIgnoreCase("audio.x_gsm"))
        {
            return "GSM";
        }
        if(super.encoding.equalsIgnoreCase("audio.mpeg"))
        {
            return "MPEG Audio";
        } else
        {
            return super.encoding;
        }
    }
}
