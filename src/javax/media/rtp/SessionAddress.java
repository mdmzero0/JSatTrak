/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   SessionAddress.java

package javax.media.rtp;

import java.io.Serializable;
import java.net.InetAddress;

public class SessionAddress
    implements Serializable
{

    public SessionAddress(InetAddress dataAddress, int dataPort)
    {
        m_dataAddress = dataAddress;
        m_controlAddress = dataAddress;
        m_dataPort = dataPort;
        m_controlPort = dataPort + 1;
    }

    public SessionAddress(InetAddress dataAddress, int dataPort, int timeToLive)
    {
        m_dataAddress = dataAddress;
        m_controlAddress = dataAddress;
        m_dataPort = dataPort;
        m_controlPort = dataPort + 1;
        ttl = timeToLive;
    }

    public SessionAddress(InetAddress dataAddress, int dataPort, InetAddress controlAddress, int controlPort)
    {
        m_dataAddress = dataAddress;
        m_controlAddress = controlAddress;
        m_dataPort = dataPort;
        m_controlPort = controlPort;
    }

    public SessionAddress()
    {
        this(null, -1, null, -1);
    }

    public int getTimeToLive()
    {
        return ttl;
    }

    public InetAddress getDataAddress()
    {
        return m_dataAddress;
    }

    public void setDataHostAddress(InetAddress dataAddress)
    {
        m_dataAddress = dataAddress;
    }

    public void setDataPort(int dataPort)
    {
        m_dataPort = dataPort;
    }

    public String getDataHostAddress()
    {
        return m_dataAddress.getHostAddress();
    }

    public int getDataPort()
    {
        return m_dataPort;
    }

    public InetAddress getControlAddress()
    {
        return m_controlAddress;
    }

    public void setControlHostAddress(InetAddress controlAddress)
    {
        m_controlAddress = controlAddress;
    }

    public void setControlPort(int controlPort)
    {
        m_controlPort = controlPort;
    }

    public String getControlHostAddress()
    {
        return m_controlAddress.getHostAddress();
    }

    public int getControlPort()
    {
        return m_controlPort;
    }

    public boolean equals(Object obj)
    {
        if(obj instanceof SessionAddress)
        {
            SessionAddress otheraddr = (SessionAddress)obj;
            InetAddress otherdest = otheraddr.getDataAddress();
            InetAddress othercontl = otheraddr.getControlAddress();
            int dport = otheraddr.getDataPort();
            int cport = otheraddr.getControlPort();
            if(otherdest.equals(m_dataAddress) && othercontl.equals(m_controlAddress) && dport == m_dataPort && cport == m_controlPort)
                return true;
        }
        return false;
    }

    public int hashCode()
    {
        return 1;
    }

    public String toString()
    {
        String s = "DataAddress: ";
        if(m_dataAddress != null)
            s = s + m_dataAddress.toString();
        else
            s = s + "null";
        s = s + "\nControlAddress: ";
        if(m_controlAddress != null)
            s = s + m_controlAddress.toString();
        else
            s = s + "null";
        s = s + "\nDataPort: " + m_dataPort + "\nControlPort: " + m_controlPort;
        return s;
    }

    private InetAddress m_dataAddress;
    private InetAddress m_controlAddress;
    private int m_dataPort;
    private int m_controlPort;
    private int ttl;
    public static final int ANY_PORT = -1;
}
