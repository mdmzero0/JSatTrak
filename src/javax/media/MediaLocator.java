package javax.media;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class MediaLocator
    implements Serializable
{

    private URL url;
    private String locatorString;

    public MediaLocator(URL url)
    {
        this.url = url;
        locatorString = url.toString().trim();
    }

    public MediaLocator(String locatorString)
    {
        this.locatorString = locatorString.trim();
    }

    public URL getURL()
        throws MalformedURLException
    {
        if(url == null)
        {
            url = new URL(locatorString);
        }
        return url;
    }

    public String getProtocol()
    {
        String protocol = "";
        int colonIndex = locatorString.indexOf(':');
        if(colonIndex != -1)
        {
            protocol = locatorString.substring(0, colonIndex);
        }
        return protocol;
    }

    public String getRemainder()
    {
        String remainder = "";
        int colonIndex = locatorString.indexOf(":");
        if(colonIndex != -1)
        {
            remainder = locatorString.substring(colonIndex + 1);
        }
        return remainder;
    }

    public String toString()
    {
        return locatorString;
    }

    public String toExternalForm()
    {
        return locatorString;
    }
}
