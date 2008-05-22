/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   FileTypeDescriptor.java

package javax.media.protocol;

import javax.media.Format;

// Referenced classes of package javax.media.protocol:
//            ContentDescriptor

public class FileTypeDescriptor extends ContentDescriptor
{

    public FileTypeDescriptor(String contentType)
    {
        super(contentType);
    }

    public String toString()
    {
        if(super.encoding.equalsIgnoreCase("video.quicktime"))
            return "QuickTime";
        if(super.encoding.equalsIgnoreCase("video.x_msvideo"))
            return "AVI";
        if(super.encoding.equalsIgnoreCase("video.mpeg"))
            return "MPEG Video";
        if(super.encoding.equalsIgnoreCase("video.vivo"))
            return "Vivo";
        if(super.encoding.equalsIgnoreCase("audio.basic"))
            return "Basic Audio (au)";
        if(super.encoding.equalsIgnoreCase("audio.x_wav"))
            return "WAV";
        if(super.encoding.equalsIgnoreCase("audio.x_aiff"))
            return "AIFF";
        if(super.encoding.equalsIgnoreCase("audio.midi"))
            return "MIDI";
        if(super.encoding.equalsIgnoreCase("audio.rmf"))
            return "RMF";
        if(super.encoding.equalsIgnoreCase("audio.x_gsm"))
            return "GSM";
        if(super.encoding.equalsIgnoreCase("audio.mpeg"))
            return "MPEG Audio";
        else
            return super.encoding;
    }

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
}
