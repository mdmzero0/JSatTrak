/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   CaptureDeviceInfo.java

package javax.media;

import java.io.Serializable;

// Referenced classes of package javax.media:
//            MediaLocator, Format

public class CaptureDeviceInfo
    implements Serializable
{

    public CaptureDeviceInfo(String name, MediaLocator locator, Format formats[])
    {
        this.locator = null;
        this.formats = null;
        this.name = name;
        this.locator = locator;
        this.formats = formats;
    }

    public CaptureDeviceInfo()
    {
        locator = null;
        formats = null;
    }

    public Format[] getFormats()
    {
        return formats;
    }

    public MediaLocator getLocator()
    {
        return locator;
    }

    public String getName()
    {
        return name;
    }

    public boolean equals(Object obj)
    {
        if(!(obj instanceof CaptureDeviceInfo))
        {
            return false;
        } else
        {
            CaptureDeviceInfo cdi = (CaptureDeviceInfo)obj;
            return name != null && locator != null && formats != null && name.equals(cdi.getName()) && locator.equals(cdi.getLocator()) && formats.equals(cdi.getFormats());
        }
    }

    public String toString()
    {
        String result = name + " : " + locator + "\n";
        if(formats != null)
        {
            for(int i = 0; i < formats.length; i++)
                result = result + formats[i] + "\n";

        }
        return result;
    }

    protected String name;
    protected MediaLocator locator;
    protected Format formats[];
}
