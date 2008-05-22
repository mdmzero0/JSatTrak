/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   RTPPushDataSource.java

package javax.media.rtp;

import java.io.IOException;
import java.io.PrintStream;
import javax.media.Time;
import javax.media.protocol.*;

// Referenced classes of package javax.media.rtp:
//            RTPControl, OutputDataStream

/**
 * @deprecated Class RTPPushDataSource is deprecated
 */

public class RTPPushDataSource extends PushDataSource
{

    public RTPPushDataSource()
    {
        contentType = null;
        connected = false;
        started = false;
        childsrc = null;
        rtpcontrol = null;
        Class eClass = null;
        try
        {
            eClass = Class.forName("com.sun.media.rtp.RTPControlImpl");
            rtpcontrol = (RTPControl)eClass.newInstance();
        }
        catch(Exception e)
        {
            rtpcontrol = null;
        }
    }

    public void setChild(DataSource source)
    {
        childsrc = source;
    }

    public PushSourceStream getOutputStream()
    {
        return outputstream;
    }

    public OutputDataStream getInputStream()
    {
        return inputstream;
    }

    public void setOutputStream(PushSourceStream outputstream)
    {
        this.outputstream = outputstream;
    }

    public void setInputStream(OutputDataStream inputstream)
    {
        this.inputstream = inputstream;
    }

    public String getContentType()
    {
        if(!connected)
        {
            System.err.println("Error: DataSource not connected");
            return null;
        } else
        {
            return ContentDescriptor.mimeTypeToPackageName(contentType);
        }
    }

    public void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    public void connect()
        throws IOException
    {
        connected = true;
        if(childsrc != null)
            childsrc.connect();
    }

    public void disconnect()
    {
        connected = false;
        if(childsrc != null)
            childsrc.disconnect();
    }

    protected void initCheck()
    {
    }

    public void start()
        throws IOException
    {
        if(!connected)
            return;
        started = true;
        if(childsrc != null)
            childsrc.start();
    }

    public void stop()
        throws IOException
    {
        if(!connected && !started)
            return;
        started = false;
        if(childsrc != null)
            childsrc.stop();
    }

    public boolean isStarted()
    {
        return started;
    }

    public Object[] getControls()
    {
        RTPControl controls[] = new RTPControl[1];
        controls[0] = rtpcontrol;
        return controls;
    }

    public Object getControl(String controlName)
    {
        if(controlName.equals("javax.media.rtp.RTPControl"))
            return rtpcontrol;
        else
            return null;
    }

    public Time getDuration()
    {
        return null;
    }

    public PushSourceStream[] getStreams()
    {
        PushSourceStream outstream[] = new PushSourceStream[1];
        outstream[0] = outputstream;
        return outstream;
    }

    PushSourceStream outputstream;
    OutputDataStream inputstream;
    String contentType;
    private boolean connected;
    private boolean started;
    DataSource childsrc;
    private RTPControl rtpcontrol;
}
