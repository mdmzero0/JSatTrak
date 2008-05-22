/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   ContentDescriptor.java

package javax.media.protocol;

import javax.media.Format;

public class ContentDescriptor extends Format
{

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
            return "RAW";
        if(super.encoding.equalsIgnoreCase("raw.rtp"))
            return "RAW/RTP";
        if(super.encoding.equalsIgnoreCase("audio.cdaudio"))
            return "CD Audio";
        else
            return super.encoding;
    }

    public static final String mimeTypeToPackageName(String mimeType)
    {
        if(mimeType == null)
            return null;
        mimeType = mimeType.toLowerCase();
        int len = mimeType.length();
        char nm[] = new char[len];
        mimeType.getChars(0, len, nm, 0);
        for(int i = 0; i < len; i++)
        {
            char c = nm[i];
            if(c == '/')
                nm[i] = '.';
            else
            if(c != '.' && ('A' > c || c > 'Z') && ('a' > c || c > 'z') && ('0' > c || c > '9'))
                nm[i] = '_';
        }

        return new String(nm);
    }

    public static final String RAW = "raw";
    public static final String RAW_RTP = "raw.rtp";
    public static final String MIXED = "application.mixed-data";
    public static final String CONTENT_UNKNOWN = "UnknownContent";
}
