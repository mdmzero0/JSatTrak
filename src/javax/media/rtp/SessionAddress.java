// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
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
