// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UDPPacketReceiver.java

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
//            UDPPacket, PacketSource, Packet

public class UDPPacketReceiver
    implements PacketSource
{

    public DatagramSocket getSocket()
    {
        return sock;
    }

    public UDPPacketReceiver(DatagramSocket sock, int maxsize)
    {
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        dataBuf = new byte[1];
        this.sock = sock;
        this.maxsize = maxsize;
        try
        {
            sock.setSoTimeout(5000);
        }
        catch(SocketException e)
        {
            System.out.println("could not set timeout on socket");
        }
    }

    public UDPPacketReceiver(int localPort, String localAddress, int remotePort, String remoteAddress, int maxsize, DatagramSocket localSocket)
        throws SocketException, UnknownHostException, IOException
    {
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        dataBuf = new byte[1];
        InetAddress localInetAddr = InetAddress.getByName(localAddress);
        InetAddress remoteInetAddr = InetAddress.getByName(remoteAddress);
        if(remoteInetAddr.isMulticastAddress())
        {
            MulticastSocket sock = new MulticastSocket(remotePort);
            if(jmfSecurity != null)
                try
                {
                    if(jmfSecurity.getName().startsWith("jmf-security"))
                    {
                        jmfSecurity.requestPermission(m, cl, args, 512);
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
                    jmfSecurity.permissionFailureNotification(512);
                }
            sock.joinGroup(remoteInetAddr);
            this.sock = sock;
            this.maxsize = maxsize;
        } else
        {
            if(localSocket != null)
                this.sock = localSocket;
            else
                this.sock = new DatagramSocket(localPort, localInetAddr);
            if(remoteAddress == null);
            this.maxsize = maxsize;
        }
        try
        {
            this.sock.setSoTimeout(5000);
        }
        catch(SocketException e)
        {
            System.out.println("could not set timeout on socket");
        }
    }

    public Packet receiveFrom()
        throws IOException
    {
        DatagramPacket dp;
        int len;
        do
        {
            if(dataBuf.length < maxsize)
                dataBuf = new byte[maxsize];
            dp = new DatagramPacket(dataBuf, maxsize);
            sock.receive(dp);
            len = dp.getLength();
            if(len > maxsize >> 1)
                maxsize = len << 1;
        } while(len >= dp.getData().length);
        UDPPacket p = new UDPPacket();
        p.receiptTime = System.currentTimeMillis();
        p.data = dp.getData();
        p.offset = 0;
        p.length = len;
        p.datagrampacket = dp;
        p.localPort = sock.getLocalPort();
        p.remotePort = dp.getPort();
        p.remoteAddress = dp.getAddress();
        return p;
    }

    public void closeSource()
    {
        if(sock != null)
        {
            sock.close();
            sock = null;
        }
    }

    public String sourceString()
    {
        String s = "UDP Datagram Packet Receiver on port " + sock.getLocalPort() + "on local address " + sock.getLocalAddress();
        return s;
    }

    private DatagramSocket sock;
    private int maxsize;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    byte dataBuf[];

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
