// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UDPPacketSender.java

package com.sun.media.rtp.util;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.*;

// Referenced classes of package com.sun.media.rtp.util:
//            UDPPacket, PacketConsumer, Packet

public class UDPPacketSender
    implements PacketConsumer
{

    public UDPPacketSender(DatagramSocket sock)
    {
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        this.sock = sock;
    }

    public UDPPacketSender()
        throws IOException
    {
        this(new DatagramSocket());
    }

    public UDPPacketSender(int localPort)
        throws IOException
    {
        this(new DatagramSocket(localPort));
    }

    public UDPPacketSender(InetAddress remoteAddress, int remotePort)
        throws IOException
    {
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        if(remoteAddress.isMulticastAddress())
        {
            MulticastSocket sock = new MulticastSocket();
            this.sock = sock;
        } else
        {
            this.sock = new DatagramSocket();
        }
        setRemoteAddress(remoteAddress, remotePort);
    }

    public InetAddress getLocalAddress()
    {
        return sock.getLocalAddress();
    }

    public UDPPacketSender(int localPort, InetAddress localAddress, InetAddress remoteAddress, int remotePort)
        throws IOException
    {
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        if(remoteAddress.isMulticastAddress())
        {
            MulticastSocket sock = new MulticastSocket(localPort);
            if(localAddress != null)
                sock.setInterface(localAddress);
            this.sock = sock;
        } else
        if(localAddress != null)
            try
            {
                this.sock = new DatagramSocket(localPort, localAddress);
            }
            catch(SocketException e)
            {
                System.out.println(e);
                System.out.println("localPort: " + localPort);
                System.out.println("localAddress: " + localAddress);
                throw e;
            }
        else
            this.sock = new DatagramSocket(localPort);
        setRemoteAddress(remoteAddress, remotePort);
    }

    public DatagramSocket getSocket()
    {
        return sock;
    }

    public void setRemoteAddress(InetAddress remoteAddress, int remotePort)
    {
        address = remoteAddress;
        port = remotePort;
    }

    public void setttl(int ttl)
        throws IOException
    {
        this.ttl = ttl;
        if(sock instanceof MulticastSocket)
            ((MulticastSocket)sock).setTTL((byte)this.ttl);
    }

    public int getLocalPort()
    {
        return sock.getLocalPort();
    }

    public void sendTo(Packet p)
        throws IOException
    {
        InetAddress addr = null;
        int port = 0;
        if(p instanceof UDPPacket)
        {
            UDPPacket udpp = (UDPPacket)p;
            addr = udpp.remoteAddress;
            port = udpp.remotePort;
            if(jmfSecurity != null)
            {
                String permission = null;
                try
                {
                    if(jmfSecurity.getName().startsWith("jmf-security"))
                    {
                        if(udpp.remoteAddress.isMulticastAddress())
                        {
                            permission = "multicast";
                            jmfSecurity.requestPermission(m, cl, args, 512);
                            m[0].invoke(cl[0], args[0]);
                        }
                        permission = "connect";
                        jmfSecurity.requestPermission(m, cl, args, 128);
                        m[0].invoke(cl[0], args[0]);
                    } else
                    if(jmfSecurity.getName().startsWith("internet"))
                    {
                        PolicyEngine.checkPermission(PermissionID.NETIO);
                        PolicyEngine.assertPermission(PermissionID.NETIO);
                    }
                }
                catch(Throwable e)
                {
                    if(permission.startsWith("multicast"))
                        jmfSecurity.permissionFailureNotification(512);
                    else
                        jmfSecurity.permissionFailureNotification(128);
                }
            }
        }
        if(addr == null)
        {
            throw new IllegalArgumentException("No address set");
        } else
        {
            send(p, addr, port);
            return;
        }
    }

    public void send(Packet p, InetAddress addr, int port)
        throws IOException
    {
        byte data[] = p.data;
        if(p.offset > 0)
            System.arraycopy(data, p.offset, data = new byte[p.length], 0, p.length);
        DatagramPacket dp = new DatagramPacket(data, p.length, addr, port);
        sock.send(dp);
    }

    public void closeConsumer()
    {
        if(sock != null)
        {
            sock.close();
            sock = null;
        }
    }

    public String consumerString()
    {
        String s = "UDP Datagram Packet Sender on port " + sock.getLocalPort();
        if(address != null)
            s = s + " sending to address " + address + ", port " + port + ", ttl" + ttl;
        return s;
    }

    private DatagramSocket sock;
    private InetAddress address;
    private int port;
    private int ttl;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
    }
}
