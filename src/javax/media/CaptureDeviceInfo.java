// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
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
