// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSessionMgr.java

package com.sun.media.rtp;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import com.sun.media.protocol.rtp.DataSource;
import com.sun.media.rtp.util.PacketFilter;
import com.sun.media.rtp.util.PacketForwarder;
import com.sun.media.rtp.util.RTPPacketSender;
import com.sun.media.rtp.util.SSRCTable;
import com.sun.media.rtp.util.UDPPacketSender;
import com.sun.media.util.jdk12;
import com.sun.media.util.jdk12InetAddressAction;
import com.sun.media.util.jdk12PropertyAction;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.media.Format;
import javax.media.MediaLocator;
import javax.media.control.BufferControl;
import javax.media.format.AudioFormat;
import javax.media.format.UnsupportedFormatException;
import javax.media.format.VideoFormat;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.EncryptionInfo;
import javax.media.rtp.GlobalReceptionStats;
import javax.media.rtp.GlobalTransmissionStats;
import javax.media.rtp.InvalidSessionAddressException;
import javax.media.rtp.LocalParticipant;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPConnector;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.RTPPushDataSource;
import javax.media.rtp.RTPSocket;
import javax.media.rtp.RTPStream;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.RemoteListener;
import javax.media.rtp.RemoteParticipant;
import javax.media.rtp.SSRCInUseException;
import javax.media.rtp.SendStream;
import javax.media.rtp.SendStreamListener;
import javax.media.rtp.SessionAddress;
import javax.media.rtp.SessionListener;
import javax.media.rtp.SessionManager;
import javax.media.rtp.SessionManagerException;
import javax.media.rtp.event.NewSendStreamEvent;
import javax.media.rtp.event.StreamClosedEvent;
import javax.media.rtp.rtcp.SourceDescription;

// Referenced classes of package com.sun.media.rtp:
//            FormatInfo, BufferControlImpl, OverallStats, OverallTransStats, 
//            StreamSynch, SSRCCache, RTCPRawReceiver, RTPRawReceiver, 
//            RTPDemultiplexer, RTCPReceiver, RTPReceiver, SSRCCacheCleaner, 
//            RTPSourceInfo, SSRCInfo, SendSSRCInfo, PassiveSSRCInfo, 
//            RTPRawSender, RTPTransmitter, RTPControlImpl, RTPMediaLocator, 
//            RTPSourceStream, RTCPRawSender, RTCPTransmitter, RTCPReporter, 
//            RTPSourceInfoCache, TrueRandom, RTPSinkStream, RTPEventHandler

public class RTPSessionMgr extends RTPManager
    implements SessionManager
{

    public RTPSessionMgr()
    {
        bindtome = false;
        cache = null;
        sendercount = 0;
        localDataAddress = null;
        localDataPort = 0;
        localControlAddress = null;
        localControlPort = 0;
        dataaddress = null;
        controladdress = null;
        dataport = 0;
        controlport = 0;
        rtpsource = null;
        rtcpsource = null;
        defaultSSRC = 0L;
        udpsender = null;
        rtpsender = null;
        sender = null;
        cleaner = null;
        unicast = false;
        startedparticipating = false;
        nonparticipating = false;
        nosockets = false;
        started = false;
        initialized = false;
        sessionlistener = new Vector();
        remotelistener = new Vector();
        streamlistener = new Vector();
        sendstreamlistener = new Vector();
        encryption = false;
        dslist = new SSRCTable();
        formatinfo = null;
        defaultsource = null;
        defaultstream = null;
        defaultformat = null;
        buffercontrol = null;
        defaultstats = null;
        transstats = null;
        defaultsourceid = 0;
        sendstreamlist = new Vector(1);
        rtpTransmitter = null;
        bds = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        peerlist = new Vector();
        multi_unicast = false;
        peerrtplist = new Hashtable(5);
        peerrtcplist = new Hashtable(5);
        newRtpInterface = false;
        formatinfo = new FormatInfo();
        buffercontrol = new BufferControlImpl();
        defaultstats = new OverallStats();
        transstats = new OverallTransStats();
        streamSynch = new StreamSynch();
    }

    public Object[] getControls()
    {
        Object c[] = new Object[1];
        c[0] = buffercontrol;
        return c;
    }

    public Object getControl(String controlname)
    {
        if(controlname.equals("javax.media.control.BufferControl"))
            return buffercontrol;
        else
            return null;
    }

    public int initSession(SessionAddress localAddress, long defaultSSRC, SourceDescription defaultUserDesc[], double rtcp_bw_fraction, double rtcp_sender_bw_fraction)
        throws InvalidSessionAddressException
    {
        if(initialized)
            return -1;
        if(rtcp_bw_fraction == 0.0D)
            nonparticipating = true;
        this.defaultSSRC = defaultSSRC;
        localDataAddress = localAddress.getDataAddress();
        localControlAddress = localAddress.getControlAddress();
        localDataPort = localAddress.getDataPort();
        localControlPort = localAddress.getControlPort();
        InetAddress addrlist[] = null;
        if(jmfSecurity != null)
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
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
                jmfSecurity.permissionFailureNotification(128);
            }
        InetAddress host;
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12InetAddressAction.cons;
                host = (InetAddress)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getLocalHost", null
                    })
                });
                String hostname = (String)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        host, "getHostName", null
                    })
                });
                addrlist = (InetAddress[])jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getAllByName", hostname
                    })
                });
            } else
            {
                host = InetAddress.getLocalHost();
                String hostname = host.getHostName();
                addrlist = InetAddress.getAllByName(hostname);
            }
        }
        catch(Throwable e)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        if(localDataAddress == null)
            localDataAddress = host;
        if(localControlAddress == null)
            localControlAddress = host;
        boolean dataok = false;
        boolean ctlok = false;
        for(int i = 0; i < addrlist.length && (!dataok || !ctlok); i++)
        {
            if(addrlist[i].equals(localDataAddress))
                dataok = true;
            if(addrlist[i].equals(localControlAddress))
                ctlok = true;
        }

        String s = "Does not belong to any of this hosts local interfaces";
        if(!dataok)
            throw new InvalidSessionAddressException("Local Data Address" + s);
        if(!ctlok)
            throw new InvalidSessionAddressException("Local Control Address" + s);
        cache = new SSRCCache(this);
        formatinfo.setCache(cache);
        cache.rtcp_bw_fraction = rtcp_bw_fraction;
        cache.rtcp_sender_bw_fraction = rtcp_sender_bw_fraction;
        cache.ourssrc = cache.get((int)defaultSSRC, host, 0, 2);
        cache.ourssrc.setAlive(true);
        if(!isCNAME(defaultUserDesc))
        {
            SourceDescription newUserDesc[] = setCNAME(defaultUserDesc);
            cache.ourssrc.setSourceDescription(newUserDesc);
        } else
        {
            cache.ourssrc.setSourceDescription(defaultUserDesc);
        }
        cache.ourssrc.ssrc = (int)defaultSSRC;
        cache.ourssrc.setOurs(true);
        initialized = true;
        return 0;
    }

    public int initSession(SessionAddress localAddress, SourceDescription defaultUserDesc[], double rtcp_bw_fraction, double rtcp_sender_bw_fraction)
        throws InvalidSessionAddressException
    {
        long defaultSSRC = generateSSRC();
        return initSession(localAddress, defaultSSRC, defaultUserDesc, rtcp_bw_fraction, rtcp_sender_bw_fraction);
    }

    public int startSession(SessionAddress destAddress, int mcastScope, EncryptionInfo encryptionInfo)
        throws IOException, InvalidSessionAddressException
    {
        if(started)
            return -1;
        if(mcastScope < 1)
            mcastScope = 1;
        ttl = mcastScope;
        if(ttl <= 16)
            cache.sessionbandwidth = 0x5dc00;
        else
        if(ttl <= 64)
            cache.sessionbandwidth = 0x1f400;
        else
        if(ttl <= 128)
            cache.sessionbandwidth = 16000;
        else
        if(ttl <= 192)
            cache.sessionbandwidth = 6625;
        else
            cache.sessionbandwidth = 4000;
        controlport = destAddress.getControlPort();
        dataport = destAddress.getDataPort();
        CheckRTPPorts(dataport, controlport);
        dataaddress = destAddress.getDataAddress();
        controladdress = destAddress.getControlAddress();
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "read property";
                    jmfSecurity.requestPermission(m, cl, args, 1);
                    m[0].invoke(cl[0], args[0]);
                    permission = "connect";
                    jmfSecurity.requestPermission(m, cl, args, 128);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.PROPERTY);
                    PolicyEngine.assertPermission(PermissionID.PROPERTY);
                    PolicyEngine.checkPermission(PermissionID.NETIO);
                    PolicyEngine.assertPermission(PermissionID.NETIO);
                }
            }
            catch(Throwable e)
            {
                if(permission.startsWith("read"))
                    jmfSecurity.permissionFailureNotification(1);
                else
                    jmfSecurity.permissionFailureNotification(128);
            }
        }
        CheckRTPAddress(dataaddress, controladdress);
        RTCPRawReceiver rtcpr = null;
        RTPRawReceiver rtpr = null;
        InetAddress mine = null;
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12InetAddressAction.cons;
                mine = (InetAddress)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getLocalHost", null
                    })
                });
            } else
            {
                mine = InetAddress.getLocalHost();
            }
        }
        catch(Throwable e)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        if(dataaddress.equals(mine))
            unicast = true;
        if(!dataaddress.isMulticastAddress() && !dataaddress.equals(mine))
            if(isBroadcast(dataaddress) && !Win32())
                bindtome = false;
            else
                bindtome = true;
        if(!bindtome)
            try
            {
                rtcpr = new RTCPRawReceiver(controlport, controladdress.getHostAddress(), defaultstats, streamSynch);
                if(dataaddress != null)
                    rtpr = new RTPRawReceiver(dataport, dataaddress.getHostAddress(), defaultstats);
            }
            catch(SocketException e)
            {
                throw new IOException(e.getMessage());
            }
            finally
            {
                if(dataaddress != null && rtpr == null && rtcpr != null)
                {
                    System.err.println("could not create RTCP/RTP raw receivers");
                    rtcpr.closeSource();
                }
            }
        else
            try
            {
                rtcpr = new RTCPRawReceiver(controlport, mine.getHostAddress(), defaultstats, streamSynch);
                if(dataaddress != null)
                    rtpr = new RTPRawReceiver(dataport, mine.getHostAddress(), defaultstats);
            }
            catch(SocketException e)
            {
                throw new IOException(e.getMessage());
            }
            finally
            {
                if(dataaddress != null && rtpr == null && rtcpr != null)
                {
                    System.err.println("could not create RTCP/RTP raw receivers");
                    rtcpr.closeSource();
                }
            }
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtpr, streamSynch);
        rtcpForwarder = new PacketForwarder(rtcpr, new RTCPReceiver(cache));
        if(rtpr != null)
            rtpForwarder = new PacketForwarder(rtpr, new RTPReceiver(cache, rtpDemultiplexer));
        rtcpForwarder.startPF("RTCP Forwarder for address" + controladdress.toString() + "port " + controlport);
        if(rtpForwarder != null)
            rtpForwarder.startPF("RTP Forwarder for address " + dataaddress.toString() + "port " + dataport);
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(!nonparticipating && !unicast && cache.ourssrc != null)
            cache.ourssrc.reporter = startParticipating(controlport, dataaddress.getHostAddress(), cache.ourssrc);
        started = true;
        return 0;
    }

    public int startSession(SessionAddress localDestAddress, SessionAddress localSenderAddress, SessionAddress remoteDestAddress, EncryptionInfo encryptionInfo)
        throws IOException, InvalidSessionAddressException
    {
        if(started)
            return -1;
        this.localSenderAddress = localSenderAddress;
        cache.sessionbandwidth = 0x5dc00;
        controlport = localDestAddress.getControlPort();
        dataport = localDestAddress.getDataPort();
        CheckRTPPorts(dataport, controlport);
        dataaddress = localDestAddress.getDataAddress();
        controladdress = localDestAddress.getControlAddress();
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "read property";
                    jmfSecurity.requestPermission(m, cl, args, 1);
                    m[0].invoke(cl[0], args[0]);
                    permission = "connect";
                    jmfSecurity.requestPermission(m, cl, args, 128);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.PROPERTY);
                    PolicyEngine.assertPermission(PermissionID.PROPERTY);
                    PolicyEngine.checkPermission(PermissionID.NETIO);
                    PolicyEngine.assertPermission(PermissionID.NETIO);
                }
            }
            catch(Throwable e)
            {
                if(permission.startsWith("read"))
                    jmfSecurity.permissionFailureNotification(1);
                else
                    jmfSecurity.permissionFailureNotification(128);
            }
        }
        if(dataaddress.isMulticastAddress() || controladdress.isMulticastAddress() || isBroadcast(dataaddress) || isBroadcast(controladdress))
            throw new InvalidSessionAddressException("Local Address must be UNICAST IP addresses");
        CheckRTPAddress(dataaddress, controladdress);
        RTCPRawReceiver rtcpr = null;
        RTPRawReceiver rtpr = null;
        InetAddress mine = null;
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12InetAddressAction.cons;
                mine = (InetAddress)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getLocalHost", null
                    })
                });
            } else
            {
                mine = InetAddress.getLocalHost();
            }
        }
        catch(Throwable e)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        try
        {
            rtcpr = new RTCPRawReceiver(controlport, controladdress.getHostAddress(), defaultstats, streamSynch);
            if(dataaddress != null)
                rtpr = new RTPRawReceiver(dataport, dataaddress.getHostAddress(), defaultstats);
        }
        catch(SocketException e)
        {
            throw new IOException(e.getMessage());
        }
        finally
        {
            if(dataaddress != null && rtpr == null && rtcpr != null)
            {
                System.err.println("could not create RTCP/RTP raw receivers");
                rtcpr.closeSource();
            }
        }
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtpr, streamSynch);
        rtcpForwarder = new PacketForwarder(rtcpr, new RTCPReceiver(cache));
        if(rtpr != null)
            rtpForwarder = new PacketForwarder(rtpr, new RTPReceiver(cache, rtpDemultiplexer));
        rtcpForwarder.startPF("RTCP Forwarder for address" + controladdress.toString() + "port " + controlport);
        if(rtpForwarder != null)
            rtpForwarder.startPF("RTP Forwarder for address " + dataaddress.toString() + "port " + dataport);
        controlport = remoteDestAddress.getControlPort();
        dataport = remoteDestAddress.getDataPort();
        CheckRTPPorts(dataport, controlport);
        dataaddress = remoteDestAddress.getDataAddress();
        controladdress = remoteDestAddress.getControlAddress();
        if(dataaddress.isMulticastAddress() || controladdress.isMulticastAddress() || isBroadcast(dataaddress) || isBroadcast(controladdress))
            throw new InvalidSessionAddressException("Remote Address must be UNICAST IP addresses");
        CheckRTPAddress(dataaddress, controladdress);
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(!nonparticipating && !unicast && cache.ourssrc != null)
            cache.ourssrc.reporter = startParticipating(localDestAddress, localSenderAddress, cache.ourssrc, rtcpr.socket);
        started = true;
        return 0;
    }

    public void addSessionListener(SessionListener listener)
    {
        if(!sessionlistener.contains(listener))
            sessionlistener.addElement(listener);
    }

    public void addRemoteListener(RemoteListener listener)
    {
        if(!remotelistener.contains(listener))
            remotelistener.addElement(listener);
    }

    public void addReceiveStreamListener(ReceiveStreamListener listener)
    {
        if(!streamlistener.contains(listener))
            streamlistener.addElement(listener);
    }

    public void addSendStreamListener(SendStreamListener listener)
    {
        if(!sendstreamlistener.contains(listener))
            sendstreamlistener.addElement(listener);
    }

    public void removeSessionListener(SessionListener listener)
    {
        sessionlistener.removeElement(listener);
    }

    public void removeRemoteListener(RemoteListener listener)
    {
        remotelistener.removeElement(listener);
    }

    public void removeReceiveStreamListener(ReceiveStreamListener listener)
    {
        streamlistener.removeElement(listener);
    }

    public void removeSendStreamListener(SendStreamListener sendstreamlistener1)
    {
    }

    public long getDefaultSSRC()
    {
        return defaultSSRC;
    }

    public Vector getRemoteParticipants()
    {
        Vector participantlist = new Vector();
        RTPSourceInfoCache sic = cache.getRTPSICache();
        Hashtable hash = sic.getCacheTable();
        for(Enumeration elements = hash.elements(); elements.hasMoreElements();)
        {
            Participant participant = (Participant)elements.nextElement();
            if(participant != null && (participant instanceof RemoteParticipant))
                participantlist.addElement(participant);
        }

        return participantlist;
    }

    public Vector getActiveParticipants()
    {
        Vector participantlist = new Vector();
        RTPSourceInfoCache sic = cache.getRTPSICache();
        Hashtable hash = sic.getCacheTable();
        for(Enumeration elements = hash.elements(); elements.hasMoreElements();)
        {
            Participant participant = (Participant)elements.nextElement();
            if(participant == null || !(participant instanceof LocalParticipant) || !nonparticipating)
            {
                Vector streams = participant.getStreams();
                if(streams.size() > 0)
                    participantlist.addElement(participant);
            }
        }

        return participantlist;
    }

    public Vector getPassiveParticipants()
    {
        Vector participantlist = new Vector();
        RTPSourceInfoCache sic = cache.getRTPSICache();
        Hashtable hash = sic.getCacheTable();
        for(Enumeration elements = hash.elements(); elements.hasMoreElements();)
        {
            Participant participant = (Participant)elements.nextElement();
            if(participant == null || !(participant instanceof LocalParticipant) || !nonparticipating)
            {
                Vector streams = participant.getStreams();
                if(streams.size() == 0)
                    participantlist.addElement(participant);
            }
        }

        return participantlist;
    }

    public LocalParticipant getLocalParticipant()
    {
        RTPSourceInfoCache sic = cache.getRTPSICache();
        Hashtable hash = sic.getCacheTable();
        for(Enumeration elements = hash.elements(); elements.hasMoreElements();)
        {
            Participant participant = (Participant)elements.nextElement();
            if(participant != null && !nonparticipating && (participant instanceof LocalParticipant))
                return (LocalParticipant)participant;
        }

        return null;
    }

    public Vector getAllParticipants()
    {
        Vector participantlist = new Vector();
        RTPSourceInfoCache sic = cache.getRTPSICache();
        Hashtable hash = sic.getCacheTable();
        for(Enumeration elements = hash.elements(); elements.hasMoreElements();)
        {
            Participant participant = (Participant)elements.nextElement();
            if(participant != null && (!(participant instanceof LocalParticipant) || !nonparticipating))
                participantlist.addElement(participant);
        }

        return participantlist;
    }

    public Vector getReceiveStreams()
    {
        Vector smstreamlist = new Vector();
        Vector participantlist = getAllParticipants();
        for(int i = 0; i < participantlist.size(); i++)
        {
            Participant part = (Participant)participantlist.elementAt(i);
            Vector partstreams = part.getStreams();
            for(int j = 0; j < partstreams.size(); j++)
            {
                RTPStream stream = (RTPStream)partstreams.elementAt(j);
                if(stream instanceof ReceiveStream)
                    smstreamlist.addElement(stream);
            }

        }

        smstreamlist.trimToSize();
        return smstreamlist;
    }

    public Vector getSendStreams()
    {
        return null;
    }

    public RTPStream getStream(long filterssrc)
    {
        Vector participantlist = null;
        participantlist = getAllParticipants();
        if(participantlist == null)
            return null;
        for(int i = 0; i < participantlist.size(); i++)
        {
            RTPSourceInfo si = (RTPSourceInfo)participantlist.elementAt(i);
            RTPStream stream = si.getSSRCStream(filterssrc);
            if(stream != null)
                return stream;
        }

        return null;
    }

    public int getMulticastScope()
    {
        return ttl;
    }

    public void setMulticastScope(int multicastScope)
    {
        if(multicastScope < 1)
            multicastScope = 1;
        ttl = multicastScope;
        if(ttl <= 16)
            cache.sessionbandwidth = 0x5dc00;
        else
        if(ttl <= 64)
            cache.sessionbandwidth = 0x1f400;
        else
        if(ttl <= 128)
            cache.sessionbandwidth = 16000;
        else
        if(ttl <= 192)
            cache.sessionbandwidth = 6625;
        else
            cache.sessionbandwidth = 4000;
        if(udpsender != null)
            try
            {
                udpsender.setttl(ttl);
            }
            catch(IOException e)
            {
                System.err.println("setMulticastScope Exception " + e.getMessage());
                e.printStackTrace();
            }
    }

    public void closeSession(String reason)
    {
        stopParticipating(reason, cache.ourssrc);
        if(defaultsource != null)
            defaultsource.disconnect();
        if(cache != null)
        {
            SSRCInfo s;
            for(Enumeration e = cache.cache.elements(); e.hasMoreElements(); stopParticipating(reason, s))
            {
                s = (SSRCInfo)e.nextElement();
                if(s.dstream != null)
                    s.dstream.close();
                if(s instanceof SendSSRCInfo)
                    ((SendSSRCInfo)s).close();
            }

        }
        for(int i = 0; i < sendstreamlist.size(); i++)
            removeSendStream((SendStream)sendstreamlist.elementAt(i));

        if(rtpTransmitter != null)
            rtpTransmitter.close();
        if(rtcpForwarder != null)
        {
            RTCPRawReceiver rtcpr = (RTCPRawReceiver)rtcpForwarder.getSource();
            rtcpForwarder.close();
            if(rtcpr != null)
                rtcpr.close();
        }
        if(cleaner != null)
            cleaner.stop();
        if(cache != null)
            cache.destroy();
        if(rtpForwarder != null)
        {
            RTPRawReceiver rtpr = (RTPRawReceiver)rtpForwarder.getSource();
            rtpForwarder.close();
            if(rtpr != null)
                rtpr.close();
        }
        if(multi_unicast)
            removeAllPeers();
    }

    public String generateCNAME()
    {
        return SourceDescription.generateCNAME();
    }

    public long generateSSRC()
    {
        long ssrc = TrueRandom.rand();
        return ssrc;
    }

    public SessionAddress getSessionAddress()
    {
        SessionAddress destAddress = new SessionAddress(dataaddress, dataport, controladdress, controlport);
        return destAddress;
    }

    public SessionAddress getLocalSessionAddress()
    {
        if(newRtpInterface)
        {
            return localAddress;
        } else
        {
            SessionAddress localAddr = new SessionAddress(localDataAddress, localDataPort, localControlAddress, localControlPort);
            return localAddr;
        }
    }

    public SessionAddress getLocalReceiverAddress()
    {
        return localReceiverAddress;
    }

    public GlobalReceptionStats getGlobalReceptionStats()
    {
        return defaultstats;
    }

    public GlobalTransmissionStats getGlobalTransmissionStats()
    {
        return transstats;
    }

    public void addFormat(Format info, int payload)
    {
        if(formatinfo != null)
            formatinfo.add(payload, info);
        if(info != null)
            addedList.addElement(info);
    }

    public static boolean formatSupported(Format format)
    {
        if(supportedList == null)
            supportedList = new FormatInfo();
        if(supportedList.getPayload(format) != -1)
            return true;
        for(int i = 0; i < addedList.size(); i++)
        {
            Format fmt = (Format)addedList.elementAt(i);
            if(fmt.matches(format))
                return true;
        }

        return false;
    }

    public SendStream createSendStream(int ssrc, javax.media.protocol.DataSource ds, int streamindex)
        throws UnsupportedFormatException, IOException, SSRCInUseException
    {
        SSRCInfo i = cache.lookup(ssrc);
        if(i != null)
            throw new SSRCInUseException("SSRC supplied is already in use");
        int newSSRC = ssrc;
        if(cache.rtcp_bw_fraction == 0.0D)
            throw new IOException("Initialized with zero RTP/RTCP outgoing bandwidth. Cannot create a sending stream ");
        PushBufferStream streams[] = ((PushBufferDataSource)ds).getStreams();
        PushBufferStream sendstream = streams[streamindex];
        Format fmt = sendstream.getFormat();
        int payload = formatinfo.getPayload(fmt);
        if(payload == -1)
            throw new UnsupportedFormatException("Format of Stream not supported in RTP Session Manager", fmt);
        SSRCInfo info = null;
        if(sendercount == 0)
        {
            info = new SendSSRCInfo(cache.ourssrc);
            info.ours = true;
            cache.ourssrc = info;
            cache.getMainCache().put(info.ssrc, info);
        } else
        {
            info = cache.get(newSSRC, dataaddress, dataport, 3);
            info.ours = true;
            if(!nosockets)
                info.reporter = startParticipating(controlport, controladdress.getHostAddress(), info);
            else
                info.reporter = startParticipating(rtcpsource, info);
        }
        info.payloadType = payload;
        info.sinkstream.setSSRCInfo((SendSSRCInfo)info);
        ((SendSSRCInfo)info).setFormat(fmt);
        if(fmt instanceof VideoFormat)
            info.clockrate = 0x15f90;
        else
        if(fmt instanceof AudioFormat)
            info.clockrate = (int)((AudioFormat)fmt).getSampleRate();
        else
            throw new UnsupportedFormatException("Format not supported", fmt);
        info.pds = ds;
        sendstream.setTransferHandler(info.sinkstream);
        if(multi_unicast)
            if(peerlist.size() > 0)
            {
                SessionAddress a = (SessionAddress)peerlist.firstElement();
                dataport = a.getDataPort();
                dataaddress = a.getDataAddress();
            } else
            {
                throw new IOException("At least one peer must be added");
            }
        if(rtpTransmitter == null)
        {
            if(rtpConnector != null)
                rtpTransmitter = startDataTransmission(rtpConnector);
            else
            if(nosockets)
            {
                rtpTransmitter = startDataTransmission(rtpsource);
            } else
            {
                if(newRtpInterface)
                {
                    dataport = remoteAddress.getDataPort();
                    dataaddress = remoteAddress.getDataAddress();
                }
                rtpTransmitter = startDataTransmission(dataport, dataaddress.getHostAddress());
            }
            if(rtpTransmitter == null)
                throw new IOException("Cannot create a transmitter");
        }
        info.sinkstream.setTransmitter(rtpTransmitter);
        addSendStream((SendStream)info);
        if(multi_unicast)
        {
            for(int j = 0; j < peerlist.size(); j++)
            {
                SessionAddress peerAddress = (SessionAddress)peerlist.elementAt(j);
                if(((PacketFilter) (info.sinkstream.transmitter.sender)).peerlist == null)
                    info.sinkstream.transmitter.sender.peerlist = new Vector();
                ((PacketFilter) (info.sinkstream.transmitter.sender)).peerlist.addElement(peerAddress);
                if(cache != null)
                {
                    for(Enumeration e = cache.cache.elements(); e.hasMoreElements();)
                    {
                        SSRCInfo s = (SSRCInfo)e.nextElement();
                        if(s instanceof SendSSRCInfo)
                        {
                            s.reporter.transmit.sender.control = true;
                            if(((PacketFilter) (s.reporter.transmit.sender)).peerlist == null)
                                s.reporter.transmit.sender.peerlist = new Vector();
                            ((PacketFilter) (s.reporter.transmit.sender)).peerlist.addElement(peerAddress);
                        }
                    }

                }
            }

        }
        info.sinkstream.startStream();
        NewSendStreamEvent evt = new NewSendStreamEvent(this, (SendStream)info);
        cache.eventhandler.postEvent(evt);
        return (SendStream)info;
    }

    public SSRCInfo getSSRCInfo(int ssrc)
    {
        SSRCInfo info = cache.lookup(ssrc);
        return info;
    }

    public SendStream createSendStream(javax.media.protocol.DataSource ds, int streamindex)
        throws IOException, UnsupportedFormatException
    {
        int newSSRC = 0;
        do
            newSSRC = (int)generateSSRC();
        while(cache.lookup(newSSRC) != null);
        SendStream s = null;
        try
        {
            s = createSendStream(newSSRC, ds, streamindex);
            if(newRtpInterface)
                setRemoteAddresses();
        }
        catch(SSRCInUseException e) { }
        return s;
    }

    public int startSession(int mcastScope, EncryptionInfo encryptionInfo)
        throws IOException
    {
        multi_unicast = true;
        if(mcastScope < 1)
            mcastScope = 1;
        ttl = mcastScope;
        if(ttl <= 16)
            cache.sessionbandwidth = 0x5dc00;
        else
        if(ttl <= 64)
            cache.sessionbandwidth = 0x1f400;
        else
        if(ttl <= 128)
            cache.sessionbandwidth = 16000;
        else
        if(ttl <= 192)
            cache.sessionbandwidth = 6625;
        else
            cache.sessionbandwidth = 4000;
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        return 0;
    }

    public void addPeer(SessionAddress peerAddress)
        throws IOException, InvalidSessionAddressException
    {
        for(int i = 0; i < peerlist.size(); i++)
        {
            SessionAddress a = (SessionAddress)peerlist.elementAt(i);
            if(a.equals(peerAddress))
                return;
        }

        peerlist.addElement(peerAddress);
        CheckRTPPorts(peerAddress.getDataPort(), peerAddress.getControlPort());
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "read property";
                    jmfSecurity.requestPermission(m, cl, args, 1);
                    m[0].invoke(cl[0], args[0]);
                    permission = "connect";
                    jmfSecurity.requestPermission(m, cl, args, 128);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.PROPERTY);
                    PolicyEngine.assertPermission(PermissionID.PROPERTY);
                    PolicyEngine.checkPermission(PermissionID.NETIO);
                    PolicyEngine.assertPermission(PermissionID.NETIO);
                }
            }
            catch(Throwable e)
            {
                if(permission.startsWith("read"))
                    jmfSecurity.permissionFailureNotification(1);
                else
                    jmfSecurity.permissionFailureNotification(128);
            }
        }
        RTCPRawReceiver rtcpr = null;
        RTPRawReceiver rtpr = null;
        InetAddress dataadd = peerAddress.getDataAddress();
        InetAddress controladd = peerAddress.getControlAddress();
        int datap = peerAddress.getDataPort();
        int controlp = peerAddress.getControlPort();
        CheckRTPAddress(dataadd, controladd);
        InetAddress mine = null;
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12InetAddressAction.cons;
                mine = (InetAddress)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getLocalHost", null
                    })
                });
            } else
            {
                mine = InetAddress.getLocalHost();
            }
        }
        catch(Throwable e)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + e.getMessage());
            e.printStackTrace();
        }
        if(!dataadd.isMulticastAddress() && !dataadd.equals(mine))
            if(isBroadcast(dataadd) && !Win32())
                bindtome = false;
            else
                bindtome = true;
        if(!bindtome)
            try
            {
                rtcpr = new RTCPRawReceiver(controlp, controladd.getHostAddress(), defaultstats, streamSynch);
                if(dataadd != null)
                    rtpr = new RTPRawReceiver(datap, dataadd.getHostAddress(), defaultstats);
            }
            catch(SocketException e)
            {
                throw new IOException(e.getMessage());
            }
            finally
            {
                if(dataadd != null && rtpr == null && rtcpr != null)
                {
                    System.err.println("could not create RTCP/RTP raw receivers");
                    rtcpr.closeSource();
                }
            }
        else
            try
            {
                rtcpr = new RTCPRawReceiver(controlp, mine.getHostAddress(), defaultstats, streamSynch);
                if(dataadd != null)
                    rtpr = new RTPRawReceiver(datap, mine.getHostAddress(), defaultstats);
            }
            catch(SocketException e)
            {
                throw new IOException(e.getMessage());
            }
            finally
            {
                if(dataadd != null && rtpr == null && rtcpr != null)
                {
                    System.err.println("could not create RTCP/RTP raw receivers");
                    rtcpr.closeSource();
                }
            }
        PacketForwarder rtcpf = new PacketForwarder(rtcpr, new RTCPReceiver(cache));
        PacketForwarder rtpf = null;
        if(rtpr != null)
            rtpf = new PacketForwarder(rtpr, new RTPReceiver(cache, rtpDemultiplexer));
        rtcpf.startPF("RTCP Forwarder for address" + controladd.toString() + "port " + controlp);
        if(rtpf != null)
            rtpf.startPF("RTP Forwarder for address " + dataadd.toString() + "port " + datap);
        peerrtplist.put(peerAddress, rtpf);
        peerrtcplist.put(peerAddress, rtcpf);
        if(cache.ourssrc != null)
        {
            if(cache.ourssrc.reporter == null)
            {
                controladdress = controladd;
                controlport = controlp;
                cache.ourssrc.reporter = startParticipating(controlp, dataadd.getHostAddress(), cache.ourssrc);
            }
            if(((PacketFilter) (cache.ourssrc.reporter.transmit.sender)).peerlist == null)
                cache.ourssrc.reporter.transmit.sender.peerlist = new Vector();
        }
        ((PacketFilter) (cache.ourssrc.reporter.transmit.sender)).peerlist.addElement(peerAddress);
        if(cache != null)
        {
            for(Enumeration e = cache.cache.elements(); e.hasMoreElements();)
            {
                SSRCInfo s = (SSRCInfo)e.nextElement();
                if(s instanceof SendSSRCInfo)
                {
                    s.reporter.transmit.sender.control = true;
                    if(((PacketFilter) (s.reporter.transmit.sender)).peerlist == null)
                    {
                        s.reporter.transmit.sender.peerlist = new Vector();
                        ((PacketFilter) (s.reporter.transmit.sender)).peerlist.addElement(peerAddress);
                    }
                }
            }

        }
        for(int j = 0; j < sendstreamlist.size(); j++)
        {
            SendSSRCInfo i = (SendSSRCInfo)sendstreamlist.elementAt(j);
            if(((PacketFilter) (((SSRCInfo) (i)).sinkstream.transmitter.sender)).peerlist == null)
            {
                ((SSRCInfo) (i)).sinkstream.transmitter.sender.peerlist = new Vector();
                ((PacketFilter) (((SSRCInfo) (i)).sinkstream.transmitter.sender)).peerlist.addElement(peerAddress);
            }
        }

    }

    public void removePeer(SessionAddress peerAddress)
    {
        PacketForwarder rtpf = (PacketForwarder)peerrtplist.get(peerAddress);
        PacketForwarder rtcpf = (PacketForwarder)peerrtplist.get(peerAddress);
        if(rtpf != null)
            rtpf.close();
        if(rtcpf != null)
            rtcpf.close();
        for(int i = 0; i < peerlist.size(); i++)
        {
            SessionAddress a = (SessionAddress)peerlist.elementAt(i);
            if(a.equals(peerAddress))
                peerlist.removeElementAt(i);
        }

    }

    public void removeAllPeers()
    {
        for(int i = 0; i < peerlist.size(); i++)
            removePeer((SessionAddress)peerlist.elementAt(i));

    }

    public Vector getPeers()
    {
        return peerlist;
    }

    void addSendStream(SendStream s)
    {
        sendstreamlist.addElement(s);
    }

    void removeSendStream(SendStream s)
    {
        sendstreamlist.removeElement(s);
        if(((SSRCInfo) ((SendSSRCInfo)s)).sinkstream != null)
        {
            ((SSRCInfo) ((SendSSRCInfo)s)).sinkstream.close();
            StreamClosedEvent evt = new StreamClosedEvent(this, s);
            cache.eventhandler.postEvent(evt);
            stopParticipating("Closed Stream", (SendSSRCInfo)s);
        }
        if(sendstreamlist.size() == 0 && cache.ourssrc != null)
        {
            SSRCInfo info = new PassiveSSRCInfo(getSSRCCache().ourssrc);
            cache.ourssrc = info;
            cache.getMainCache().put(info.ssrc, info);
        }
    }

    private RTPTransmitter startDataTransmission(int port, String address)
        throws IOException
    {
        RTPTransmitter transmitter = null;
        RTPRawSender sender = null;
        if(this.localDataPort == -1)
            udpsender = new UDPPacketSender(dataaddress, dataport);
        else
        if(newRtpInterface)
        {
            udpsender = new UDPPacketSender(rtpRawReceiver.socket);
        } else
        {
            int localDataPort = localSenderAddress.getDataPort();
            InetAddress localDataAddress = localSenderAddress.getDataAddress();
            udpsender = new UDPPacketSender(localDataPort, localDataAddress, dataaddress, dataport);
        }
        if(ttl != 1)
            udpsender.setttl(ttl);
        sender = new RTPRawSender(dataport, address, udpsender);
        transmitter = new RTPTransmitter(cache, sender);
        return transmitter;
    }

    private RTPTransmitter startDataTransmission(RTPPushDataSource s)
    {
        RTPRawSender sender = null;
        RTPTransmitter transmitter = null;
        rtpsender = new RTPPacketSender(s);
        sender = new RTPRawSender(rtpsender);
        transmitter = new RTPTransmitter(cache, sender);
        return transmitter;
    }

    private RTPTransmitter startDataTransmission(RTPConnector connector)
    {
        try
        {
            RTPRawSender sender = null;
            RTPTransmitter transmitter = null;
            rtpsender = new RTPPacketSender(connector);
            sender = new RTPRawSender(rtpsender);
            transmitter = new RTPTransmitter(cache, sender);
            return transmitter;
        }
        catch(IOException e)
        {
            return null;
        }
    }

    public void UpdateEncodings(javax.media.protocol.DataSource source)
    {
        RTPControlImpl control = (RTPControlImpl)source.getControl("javax.media.rtp.RTPControl");
        if(control != null && control.codeclist != null)
        {
            Integer p;
            for(Enumeration e = control.codeclist.keys(); e.hasMoreElements(); formatinfo.add(p.intValue(), (Format)control.codeclist.get(p)))
                p = (Integer)e.nextElement();

        }
    }

    private int startSession(RTPPushDataSource rtpsource, RTPPushDataSource rtcpsource, EncryptionInfo info)
    {
        if(!initialized)
            return -1;
        if(started)
            return -1;
        cache.sessionbandwidth = 0x5dc00;
        RTPRawReceiver rtpr = new RTPRawReceiver(rtpsource, defaultstats);
        RTCPRawReceiver rtcpr = new RTCPRawReceiver(rtcpsource, defaultstats, streamSynch);
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtpr, streamSynch);
        rtpForwarder = new PacketForwarder(rtpr, new RTPReceiver(cache, rtpDemultiplexer));
        if(rtpForwarder != null)
            rtpForwarder.startPF("RTP Forwarder " + rtpsource);
        rtcpForwarder = new PacketForwarder(rtcpr, new RTCPReceiver(cache));
        if(rtcpForwarder != null)
            rtcpForwarder.startPF("RTCP Forwarder " + rtpsource);
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(!nonparticipating && cache.ourssrc != null)
            cache.ourssrc.reporter = startParticipating(rtcpsource, cache.ourssrc);
        started = true;
        return 0;
    }

    public RTPSessionMgr(RTPPushDataSource netdatasource)
    {
        bindtome = false;
        cache = null;
        sendercount = 0;
        localDataAddress = null;
        localDataPort = 0;
        localControlAddress = null;
        localControlPort = 0;
        dataaddress = null;
        controladdress = null;
        dataport = 0;
        controlport = 0;
        rtpsource = null;
        rtcpsource = null;
        defaultSSRC = 0L;
        udpsender = null;
        rtpsender = null;
        sender = null;
        cleaner = null;
        unicast = false;
        startedparticipating = false;
        nonparticipating = false;
        nosockets = false;
        started = false;
        initialized = false;
        sessionlistener = new Vector();
        remotelistener = new Vector();
        streamlistener = new Vector();
        sendstreamlistener = new Vector();
        encryption = false;
        dslist = new SSRCTable();
        formatinfo = null;
        defaultsource = null;
        defaultstream = null;
        defaultformat = null;
        buffercontrol = null;
        defaultstats = null;
        transstats = null;
        defaultsourceid = 0;
        sendstreamlist = new Vector(1);
        rtpTransmitter = null;
        bds = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        peerlist = new Vector();
        multi_unicast = false;
        peerrtplist = new Hashtable(5);
        peerrtcplist = new Hashtable(5);
        newRtpInterface = false;
        nosockets = true;
        rtpsource = netdatasource;
        if(rtpsource instanceof RTPSocket)
            rtcpsource = ((RTPSocket)rtpsource).getControlChannel();
        formatinfo = new FormatInfo();
        buffercontrol = new BufferControlImpl();
        defaultstats = new OverallStats();
        transstats = new OverallTransStats();
        DataSource newsource = createNewDS(((RTPMediaLocator) (null)));
        UpdateEncodings(netdatasource);
        RTPControl contrl = (RTPControl)netdatasource.getControl("javax.media.rtp.RTPControl");
        newsource.setControl(contrl);
        initSession(setSDES(), 0.050000000000000003D, 0.25D);
        startSession(rtpsource, rtcpsource, null);
    }

    public RTPSessionMgr(DataSource configds)
        throws IOException
    {
        bindtome = false;
        cache = null;
        sendercount = 0;
        localDataAddress = null;
        localDataPort = 0;
        localControlAddress = null;
        localControlPort = 0;
        dataaddress = null;
        controladdress = null;
        dataport = 0;
        controlport = 0;
        rtpsource = null;
        rtcpsource = null;
        defaultSSRC = 0L;
        udpsender = null;
        rtpsender = null;
        sender = null;
        cleaner = null;
        unicast = false;
        startedparticipating = false;
        nonparticipating = false;
        nosockets = false;
        started = false;
        initialized = false;
        sessionlistener = new Vector();
        remotelistener = new Vector();
        streamlistener = new Vector();
        sendstreamlistener = new Vector();
        encryption = false;
        dslist = new SSRCTable();
        formatinfo = null;
        defaultsource = null;
        defaultstream = null;
        defaultformat = null;
        buffercontrol = null;
        defaultstats = null;
        transstats = null;
        defaultsourceid = 0;
        sendstreamlist = new Vector(1);
        rtpTransmitter = null;
        bds = false;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        peerlist = new Vector();
        multi_unicast = false;
        peerrtplist = new Hashtable(5);
        peerrtcplist = new Hashtable(5);
        newRtpInterface = false;
        formatinfo = new FormatInfo();
        buffercontrol = new BufferControlImpl();
        defaultstats = new OverallStats();
        transstats = new OverallTransStats();
        UpdateEncodings(configds);
        RTPMediaLocator mrl = null;
        try
        {
            mrl = new RTPMediaLocator(configds.getLocator().toString());
        }
        catch(MalformedURLException e)
        {
            throw new IOException("RTP URL is Malformed " + e.getMessage());
        }
        DataSource newsource = createNewDS(mrl);
        RTPControl contrl = (RTPControl)configds.getControl("javax.media.rtp.RTPControl");
        newsource.setControl(contrl);
        String address = mrl.getSessionAddress();
        dataport = mrl.getSessionPort();
        controlport = dataport + 1;
        ttl = mrl.getTTL();
        if(jmfSecurity != null)
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
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
                jmfSecurity.permissionFailureNotification(128);
            }
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12InetAddressAction.cons;
                dataaddress = (InetAddress)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getByName", address
                    })
                });
            } else
            {
                dataaddress = InetAddress.getByName(address);
            }
        }
        catch(Throwable e)
        {
            e.printStackTrace();
        }
        controladdress = dataaddress;
        SessionAddress localaddr = new SessionAddress();
        try
        {
            initSession(localaddr, setSDES(), 0.050000000000000003D, 0.25D);
        }
        catch(SessionManagerException e)
        {
            throw new IOException("SessionManager exception " + e.getMessage());
        }
    }

    public void addMRL(RTPMediaLocator mrl)
    {
        int ssrc = (int)mrl.getSSRC();
        if(ssrc == 0)
            return;
        DataSource reqsource = (DataSource)dslist.get(ssrc);
        if(reqsource != null)
        {
            return;
        } else
        {
            DataSource newSource = createNewDS(mrl);
            return;
        }
    }

    public boolean isDefaultDSassigned()
    {
        return bds;
    }

    public Format getFormat(int payload)
    {
        return formatinfo.get(payload);
    }

    public void setDefaultDSassigned(int ssrc)
    {
        bds = true;
        defaultsourceid = ssrc;
        dslist.put(ssrc, defaultsource);
        defaultsource.setSSRC(ssrc);
        defaultsource.setMgr(this);
    }

    public DataSource createNewDS(int ssrcid)
    {
        DataSource source = new DataSource();
        source.setContentType("raw");
        try
        {
            source.connect();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        RTPSourceStream stream = new RTPSourceStream(source);
        ((BufferControlImpl)buffercontrol).addSourceStream(stream);
        dslist.put(ssrcid, source);
        source.setSSRC(ssrcid);
        source.setMgr(this);
        return source;
    }

    public DataSource createNewDS(RTPMediaLocator mrl)
    {
        DataSource source = new DataSource();
        source.setContentType("raw");
        try
        {
            source.connect();
        }
        catch(IOException e)
        {
            System.err.println("IOException in createNewDS() " + e.getMessage());
            e.printStackTrace();
        }
        RTPSourceStream stream = new RTPSourceStream(source);
        ((BufferControlImpl)buffercontrol).addSourceStream(stream);
        if(mrl != null && (int)mrl.getSSRC() != 0)
        {
            dslist.put((int)mrl.getSSRC(), source);
            source.setSSRC((int)mrl.getSSRC());
            source.setMgr(this);
        } else
        {
            defaultsource = source;
            defaultstream = stream;
        }
        return source;
    }

    public DataSource getDataSource(RTPMediaLocator mrl)
    {
        if(mrl == null)
            return defaultsource;
        int ssrc = (int)mrl.getSSRC();
        if(ssrc == 0)
            return defaultsource;
        else
            return (DataSource)dslist.get(ssrc);
    }

    public String toString()
    {
        String s;
        if(newRtpInterface)
        {
            int controlPort = 0;
            int dataPort = 0;
            String address = "";
            if(localAddress != null)
            {
                controlPort = localAddress.getControlPort();
                dataPort = localAddress.getDataPort();
                address = localAddress.getDataHostAddress();
            }
            s = "RTPManager \n\tSSRCCache  " + cache + "\n\tDataport  " + dataPort + "\n\tControlport  " + controlPort + "\n\tAddress  " + address + "\n\tRTPForwarder  " + rtpForwarder + "\n\tRTPDemux  " + rtpDemultiplexer;
        } else
        {
            s = "RTPSession Manager  \n\tSSRCCache  " + cache + "\n\tDataport  " + dataport + "\n\tControlport  " + controlport + "\n\tAddress  " + dataaddress + "\n\tRTPForwarder  " + rtpForwarder + "\n\tRTPDEmux  " + rtpDemultiplexer;
        }
        return s;
    }

    public boolean IsNonParticipating()
    {
        return nonparticipating;
    }

    public void startSession()
        throws IOException
    {
        SessionAddress destaddress = new SessionAddress(dataaddress, dataport, controladdress, controlport);
        try
        {
            startSession(destaddress, ttl, null);
        }
        catch(SessionManagerException e)
        {
            throw new IOException("SessionManager exception " + e.getMessage());
        }
    }

    public void closeSession()
    {
        if(dslist.isEmpty() || nosockets)
            closeSession("DataSource disconnected");
    }

    public void removeDataSource(DataSource source)
    {
        if(source == defaultsource)
        {
            defaultsource = null;
            defaultstream = null;
            defaultsourceid = 0;
            bds = false;
        }
        dslist.removeObj(source);
    }

    void startRTCPReports(InetAddress remoteAddress)
    {
        if(!nonparticipating && !startedparticipating)
            try
            {
                if(cache.ourssrc != null)
                    cache.ourssrc.reporter = startParticipating(controlport, remoteAddress.getHostAddress(), cache.ourssrc);
            }
            catch(IOException e)
            {
                System.err.println("startRTCPReports " + e.getMessage());
                e.printStackTrace();
            }
    }

    boolean isUnicast()
    {
        return unicast;
    }

    public void addUnicastAddr(InetAddress destAddress)
    {
        if(sender != null)
            sender.addDestAddr(destAddress);
    }

    public boolean isSenderDefaultAddr(InetAddress destAddress)
    {
        if(sender == null)
            return false;
        InetAddress defaultaddr = sender.getRemoteAddr();
        return defaultaddr.equals(destAddress);
    }

    SSRCCache getSSRCCache()
    {
        return cache;
    }

    void setSessionBandwidth(int bw)
    {
        cache.sessionbandwidth = bw;
    }

    private String getProperty(String prop)
    {
        String value = null;
        if(jmfSecurity != null)
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    jmfSecurity.requestPermission(m, cl, args, 1);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.PROPERTY);
                    PolicyEngine.assertPermission(PermissionID.PROPERTY);
                }
            }
            catch(Throwable e)
            {
                jmfSecurity.permissionFailureNotification(1);
            }
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12PropertyAction.cons;
                value = (String)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        prop
                    })
                });
            } else
            {
                value = System.getProperty(prop);
            }
        }
        catch(Throwable e) { }
        return value;
    }

    private SourceDescription[] setSDES()
    {
        SourceDescription desclist[] = new SourceDescription[3];
        if(desclist == null)
        {
            return null;
        } else
        {
            desclist[0] = new SourceDescription(2, getProperty("user.name"), 1, false);
            desclist[1] = new SourceDescription(1, SourceDescription.generateCNAME(), 1, false);
            desclist[2] = new SourceDescription(6, "JMF RTP Player v1.0", 1, false);
            return desclist;
        }
    }

    private SourceDescription[] setCNAME(SourceDescription desclist[])
    {
        String descval = null;
        boolean cname = false;
        if(desclist == null)
        {
            desclist = new SourceDescription[1];
            descval = SourceDescription.generateCNAME();
            desclist[0] = new SourceDescription(1, descval, 1, false);
            return desclist;
        }
        for(int i = 0; i < desclist.length; i++)
        {
            int type = desclist[i].getType();
            descval = desclist[i].getDescription();
            if(type != 1 || descval != null)
                continue;
            descval = SourceDescription.generateCNAME();
            cname = true;
            break;
        }

        if(cname)
            return desclist;
        SourceDescription newdesclist[] = new SourceDescription[desclist.length + 1];
        newdesclist[0] = new SourceDescription(1, SourceDescription.generateCNAME(), 1, false);
        int curr = 1;
        for(int i = 0; i < desclist.length; i++)
        {
            newdesclist[curr] = new SourceDescription(desclist[i].getType(), desclist[i].getDescription(), 1, false);
            curr++;
        }

        return newdesclist;
    }

    private boolean isCNAME(SourceDescription desclist[])
    {
        String descval = null;
        boolean cname = false;
        if(desclist == null)
            return cname;
        for(int i = 0; i < desclist.length; i++)
            try
            {
                int type = desclist[i].getType();
                descval = desclist[i].getDescription();
                if(type == 1 && descval != null)
                    cname = true;
            }
            catch(Exception e) { }

        return cname;
    }

    private void CheckRTPPorts(int dataport, int controlport)
        throws InvalidSessionAddressException
    {
        if(dataport == 0 || dataport == -1)
            dataport = controlport - 1;
        if(controlport == 0 || controlport == -1)
            controlport = dataport + 1;
        if(dataport != 0 && dataport % 2 != 0)
            throw new InvalidSessionAddressException("Data Port must be valid and even");
        if(controlport != 0 && controlport % 2 != 1)
            throw new InvalidSessionAddressException("Control Port must be valid and odd");
        if(controlport != dataport + 1)
            throw new InvalidSessionAddressException("Control Port must be one higher than the Data Port");
        else
            return;
    }

    private void CheckRTPAddress(InetAddress dataaddress, InetAddress controladdress)
        throws InvalidSessionAddressException
    {
        if(dataaddress == null && controladdress == null)
            throw new InvalidSessionAddressException("Data and control addresses are null");
        if(controladdress == null && dataaddress != null)
            controladdress = dataaddress;
        if(dataaddress == null && controladdress != null)
            dataaddress = controladdress;
    }

    private synchronized RTCPReporter startParticipating(RTPPushDataSource dest, SSRCInfo info)
    {
        startedparticipating = true;
        rtpsender = new RTPPacketSender(dest);
        RTCPRawSender sender = new RTCPRawSender(rtpsender);
        RTCPTransmitter transmitter = new RTCPTransmitter(cache, sender);
        transmitter.setSSRCInfo(info);
        RTCPReporter reporter = new RTCPReporter(cache, transmitter);
        return reporter;
    }

    private synchronized RTCPReporter startParticipating(RTPConnector rtpConnector, SSRCInfo info)
    {
        startedparticipating = true;
        try
        {
            rtpsender = new RTPPacketSender(rtpConnector.getControlOutputStream());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        RTCPRawSender sender = new RTCPRawSender(rtpsender);
        RTCPTransmitter transmitter = new RTCPTransmitter(cache, sender);
        transmitter.setSSRCInfo(info);
        RTCPReporter reporter = new RTCPReporter(cache, transmitter);
        return reporter;
    }

    private synchronized RTCPReporter startParticipating(int port, String address, SSRCInfo info)
        throws IOException
    {
        startedparticipating = true;
        UDPPacketSender udpsender = null;
        if(localControlPort == -1)
        {
            udpsender = new UDPPacketSender(controladdress, controlport);
            localControlPort = udpsender.getLocalPort();
            localControlAddress = udpsender.getLocalAddress();
        } else
        {
            udpsender = new UDPPacketSender(localControlPort, localControlAddress, controladdress, controlport);
        }
        if(ttl != 1)
            udpsender.setttl(ttl);
        RTCPRawSender sender = new RTCPRawSender(port, address, udpsender);
        RTCPTransmitter transmitter = new RTCPTransmitter(cache, sender);
        transmitter.setSSRCInfo(info);
        RTCPReporter reporter = new RTCPReporter(cache, transmitter);
        return reporter;
    }

    private synchronized RTCPReporter startParticipating(SessionAddress localReceiverAddress, SessionAddress localSenderAddress, SSRCInfo info, DatagramSocket rtcpSocket)
        throws IOException
    {
        this.localReceiverAddress = localReceiverAddress;
        startedparticipating = true;
        UDPPacketSender udpsender = null;
        int localSenderControlPort = localSenderAddress.getControlPort();
        InetAddress localSenderControlAddress = localSenderAddress.getControlAddress();
        int localReceiverControlPort = localReceiverAddress.getControlPort();
        InetAddress localReceiverControlAddress = localReceiverAddress.getControlAddress();
        if(localSenderControlPort == -1)
            udpsender = new UDPPacketSender(localSenderControlAddress, localSenderControlPort);
        else
        if(localSenderControlPort == localReceiverControlPort)
            udpsender = new UDPPacketSender(rtcpSocket);
        else
            udpsender = new UDPPacketSender(localSenderControlPort, localSenderControlAddress, controladdress, controlport);
        if(ttl != 1)
            udpsender.setttl(ttl);
        RTCPRawSender sender = new RTCPRawSender(controlport, controladdress.getHostName(), udpsender);
        RTCPTransmitter transmitter = new RTCPTransmitter(cache, sender);
        transmitter.setSSRCInfo(info);
        RTCPReporter reporter = new RTCPReporter(cache, transmitter);
        return reporter;
    }

    private synchronized void stopParticipating(String reason, SSRCInfo info)
    {
        if(info.reporter != null)
        {
            info.reporter.close(reason);
            info.reporter = null;
        }
    }

    private int initSession(SourceDescription defaultUserDesc[], double rtcp_bw_fraction, double rtcp_sender_bw_fraction)
    {
        if(initialized)
            return -1;
        InetAddress host = null;
        if(rtcp_bw_fraction == 0.0D)
            nonparticipating = true;
        defaultSSRC = generateSSRC();
        cache = new SSRCCache(this);
        formatinfo.setCache(cache);
        cache.rtcp_bw_fraction = rtcp_bw_fraction;
        cache.rtcp_sender_bw_fraction = rtcp_sender_bw_fraction;
        if(jmfSecurity != null)
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
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
                jmfSecurity.permissionFailureNotification(128);
            }
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12InetAddressAction.cons;
                host = (InetAddress)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getLocalHost", null
                    })
                });
            } else
            {
                host = InetAddress.getLocalHost();
            }
        }
        catch(Throwable e)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
        cache.ourssrc = cache.get((int)defaultSSRC, null, 0, 2);
        cache.ourssrc.setAlive(true);
        if(!isCNAME(defaultUserDesc))
        {
            SourceDescription newUserDesc[] = setCNAME(defaultUserDesc);
            cache.ourssrc.setSourceDescription(newUserDesc);
        } else
        {
            cache.ourssrc.setSourceDescription(defaultUserDesc);
        }
        cache.ourssrc.ssrc = (int)defaultSSRC;
        cache.ourssrc.setOurs(true);
        initialized = true;
        return 0;
    }

    boolean isBroadcast(InetAddress checkaddr)
    {
        InetAddress mine = null;
        try
        {
            mine = InetAddress.getLocalHost();
            byte addr[] = mine.getAddress();
            int address = addr[3] & 0xff;
            address |= addr[2] << 8 & 0xff00;
            address |= addr[1] << 16 & 0xff0000;
            address |= addr[0] << 24 & 0xff000000;
            byte dataaddr[] = checkaddr.getAddress();
            int daddress = dataaddr[3] & 0xff;
            daddress |= dataaddr[2] << 8 & 0xff00;
            daddress |= dataaddr[1] << 16 & 0xff0000;
            daddress |= dataaddr[0] << 24 & 0xff000000;
            if((address | 0xff) == daddress)
                return true;
        }
        catch(UnknownHostException e)
        {
            System.err.println(e.getMessage());
        }
        return false;
    }

    private boolean Win32()
    {
        return System.getProperty("os.name").startsWith("Windows");
    }

    public void initialize(RTPConnector connector)
    {
        rtpConnector = connector;
        newRtpInterface = true;
        String cname = SourceDescription.generateCNAME();
        SourceDescription sourceDescription[] = {
            new SourceDescription(3, "jmf-user@sun.com", 1, false), new SourceDescription(1, cname, 1, false), new SourceDescription(6, "JMF RTP Player v2.0", 1, false)
        };
        int ssrc = (int)generateSSRC();
        ttl = 1;
        if(rtpConnector.getRTCPBandwidthFraction() == 0.0D)
            participating = false;
        else
            participating = true;
        cache = new SSRCCache(this);
        cache.sessionbandwidth = 0x5dc00;
        formatinfo.setCache(cache);
        if(rtpConnector.getRTCPBandwidthFraction() > 0.0D)
            cache.rtcp_bw_fraction = rtpConnector.getRTCPBandwidthFraction();
        else
            cache.rtcp_bw_fraction = 0.050000000000000003D;
        if(rtpConnector.getRTCPSenderBandwidthFraction() > 0.0D)
            cache.rtcp_sender_bw_fraction = rtpConnector.getRTCPSenderBandwidthFraction();
        else
            cache.rtcp_sender_bw_fraction = 0.25D;
        cache.ourssrc = cache.get(ssrc, null, 0, 2);
        cache.ourssrc.setAlive(true);
        if(!isCNAME(sourceDescription))
        {
            SourceDescription newUserDesc[] = setCNAME(sourceDescription);
            cache.ourssrc.setSourceDescription(newUserDesc);
        } else
        {
            cache.ourssrc.setSourceDescription(sourceDescription);
        }
        cache.ourssrc.ssrc = ssrc;
        cache.ourssrc.setOurs(true);
        initialized = true;
        rtpRawReceiver = new RTPRawReceiver(rtpConnector, defaultstats);
        rtcpRawReceiver = new RTCPRawReceiver(rtpConnector, defaultstats, streamSynch);
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtpRawReceiver, streamSynch);
        rtpForwarder = new PacketForwarder(rtpRawReceiver, new RTPReceiver(cache, rtpDemultiplexer));
        if(rtpForwarder != null)
            rtpForwarder.startPF("RTP Forwarder: " + rtpConnector);
        rtcpForwarder = new PacketForwarder(rtcpRawReceiver, new RTCPReceiver(cache));
        if(rtcpForwarder != null)
            rtcpForwarder.startPF("RTCP Forwarder: " + rtpConnector);
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(participating && cache.ourssrc != null)
            cache.ourssrc.reporter = startParticipating(rtpConnector, cache.ourssrc);
    }

    public void initialize(SessionAddress localAddress)
        throws InvalidSessionAddressException
    {
        String cname = SourceDescription.generateCNAME();
        SourceDescription sourceDescription[] = {
            new SourceDescription(3, "jmf-user@sun.com", 1, false), new SourceDescription(1, cname, 1, false), new SourceDescription(6, "JMF RTP Player v2.0", 1, false)
        };
        double rtcp_bw_fraction = 0.050000000000000003D;
        double rtcp_sender_bw_fraction = 0.25D;
        SessionAddress localAddresses[] = new SessionAddress[1];
        localAddresses[0] = localAddress;
        initialize(localAddresses, sourceDescription, rtcp_bw_fraction, rtcp_sender_bw_fraction, null);
    }

    public void initialize(SessionAddress localAddresses[], SourceDescription sourceDescription[], double rtcp_bw_fraction, double rtcp_sender_bw_fraction, EncryptionInfo encryptionInfo)
        throws InvalidSessionAddressException
    {
        if(initialized)
            return;
        newRtpInterface = true;
        remoteAddresses = new Vector();
        int ssrc = (int)generateSSRC();
        ttl = 1;
        if(rtcp_bw_fraction == 0.0D)
            participating = false;
        else
            participating = true;
        if(localAddresses.length == 0)
            throw new InvalidSessionAddressException("At least one local address is required!");
        localAddress = localAddresses[0];
        if(localAddress == null)
            throw new InvalidSessionAddressException("Invalid local address: null");
        InetAddress addrlist[] = null;
        if(jmfSecurity != null)
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
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
                jmfSecurity.permissionFailureNotification(128);
            }
        InetAddress host;
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12InetAddressAction.cons;
                host = (InetAddress)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getLocalHost", null
                    })
                });
                String hostname = (String)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        host, "getHostName", null
                    })
                });
                addrlist = (InetAddress[])jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getAllByName", hostname
                    })
                });
            } else
            {
                host = InetAddress.getLocalHost();
                String hostname = host.getHostName();
                addrlist = InetAddress.getAllByName(hostname);
            }
        }
        catch(Throwable e)
        {
            System.err.println("Initialize : UnknownHostExcpetion " + e.getMessage());
            e.printStackTrace();
            return;
        }
        if(localAddress.getDataAddress() == null)
            localAddress.setDataHostAddress(host);
        if(localAddress.getControlAddress() == null)
            localAddress.setControlHostAddress(host);
        if(localAddress.getDataAddress().isMulticastAddress())
        {
            if(localAddress.getControlAddress().isMulticastAddress())
                ttl = localAddress.getTimeToLive();
            else
                throw new InvalidSessionAddressException("Invalid multicast address");
        } else
        {
            boolean dataOk = false;
            boolean ctrlOk = false;
            for(int i = 0; i < addrlist.length && (!dataOk || !ctrlOk); i++)
            {
                if(addrlist[i].equals(localAddress.getDataAddress()))
                    dataOk = true;
                if(addrlist[i].equals(localAddress.getControlAddress()))
                    ctrlOk = true;
            }

            if(!dataOk)
            {
                String s = "Does not belong to any of this hosts local interfaces";
                throw new InvalidSessionAddressException("Local Data Address" + s);
            }
            if(!ctrlOk)
            {
                String s = "Does not belong to any of this hosts local interfaces";
                throw new InvalidSessionAddressException("Local Control Address" + s);
            }
            if(localAddress.getDataPort() == -1)
            {
                int dataPort = findLocalPorts();
                localAddress.setDataPort(dataPort);
                localAddress.setControlPort(dataPort + 1);
            }
            if(!localAddress.getDataAddress().isMulticastAddress())
                try
                {
                    dataSocket = new DatagramSocket(localAddress.getDataPort(), localAddress.getDataAddress());
                }
                catch(SocketException e)
                {
                    throw new InvalidSessionAddressException("Can't open local data port: " + localAddress.getDataPort());
                }
            if(!localAddress.getControlAddress().isMulticastAddress())
                try
                {
                    controlSocket = new DatagramSocket(localAddress.getControlPort(), localAddress.getControlAddress());
                }
                catch(SocketException e)
                {
                    if(dataSocket != null)
                        dataSocket.close();
                    throw new InvalidSessionAddressException("Can't open local control port: " + localAddress.getControlPort());
                }
        }
        cache = new SSRCCache(this);
        if(ttl <= 16)
            cache.sessionbandwidth = 0x5dc00;
        else
        if(ttl <= 64)
            cache.sessionbandwidth = 0x1f400;
        else
        if(ttl <= 128)
            cache.sessionbandwidth = 16000;
        else
        if(ttl <= 192)
            cache.sessionbandwidth = 6625;
        else
            cache.sessionbandwidth = 4000;
        formatinfo.setCache(cache);
        cache.rtcp_bw_fraction = rtcp_bw_fraction;
        cache.rtcp_sender_bw_fraction = rtcp_sender_bw_fraction;
        cache.ourssrc = cache.get(ssrc, host, 0, 2);
        cache.ourssrc.setAlive(true);
        if(!isCNAME(sourceDescription))
        {
            SourceDescription newUserDesc[] = setCNAME(sourceDescription);
            cache.ourssrc.setSourceDescription(newUserDesc);
        } else
        {
            cache.ourssrc.setSourceDescription(sourceDescription);
        }
        cache.ourssrc.ssrc = ssrc;
        cache.ourssrc.setOurs(true);
        initialized = true;
    }

    public void addTarget(SessionAddress remoteAddress)
        throws IOException
    {
        remoteAddresses.addElement(remoteAddress);
        if(remoteAddresses.size() > 1)
        {
            setRemoteAddresses();
            return;
        }
        this.remoteAddress = remoteAddress;
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "read property";
                    jmfSecurity.requestPermission(m, cl, args, 1);
                    m[0].invoke(cl[0], args[0]);
                    permission = "connect";
                    jmfSecurity.requestPermission(m, cl, args, 128);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.PROPERTY);
                    PolicyEngine.assertPermission(PermissionID.PROPERTY);
                    PolicyEngine.checkPermission(PermissionID.NETIO);
                    PolicyEngine.assertPermission(PermissionID.NETIO);
                }
            }
            catch(Throwable e)
            {
                if(permission.startsWith("read"))
                    jmfSecurity.permissionFailureNotification(1);
                else
                    jmfSecurity.permissionFailureNotification(128);
            }
        }
        try
        {
            rtcpRawReceiver = new RTCPRawReceiver(localAddress, remoteAddress, defaultstats, streamSynch, controlSocket);
            rtpRawReceiver = new RTPRawReceiver(localAddress, remoteAddress, defaultstats, dataSocket);
        }
        catch(SocketException e)
        {
            throw new IOException(e.getMessage());
        }
        catch(UnknownHostException e)
        {
            throw new IOException(e.getMessage());
        }
        rtpDemultiplexer = new RTPDemultiplexer(cache, rtpRawReceiver, streamSynch);
        rtcpForwarder = new PacketForwarder(rtcpRawReceiver, new RTCPReceiver(cache));
        if(rtpRawReceiver != null)
            rtpForwarder = new PacketForwarder(rtpRawReceiver, new RTPReceiver(cache, rtpDemultiplexer));
        rtcpForwarder.startPF("RTCP Forwarder for address" + remoteAddress.getControlHostAddress() + " port " + remoteAddress.getControlPort());
        if(rtpForwarder != null)
            rtpForwarder.startPF("RTP Forwarder for address " + remoteAddress.getDataHostAddress() + " port " + remoteAddress.getDataPort());
        cleaner = new SSRCCacheCleaner(cache, streamSynch);
        if(cache.ourssrc != null && participating)
            cache.ourssrc.reporter = startParticipating(rtcpRawReceiver.socket);
    }

    private synchronized RTCPReporter startParticipating(DatagramSocket rtcpSocket)
        throws IOException
    {
        UDPPacketSender udpsender = new UDPPacketSender(rtcpSocket);
        udpPacketSender = udpsender;
        if(ttl != 1)
            udpsender.setttl(ttl);
        RTCPRawSender sender = new RTCPRawSender(remoteAddress.getControlPort(), remoteAddress.getControlAddress().getHostName(), udpsender);
        rtcpTransmitter = new RTCPTransmitter(cache, sender);
        rtcpTransmitter.setSSRCInfo(cache.ourssrc);
        RTCPReporter reporter = new RTCPReporter(cache, rtcpTransmitter);
        startedparticipating = true;
        return reporter;
    }

    public void removeTargets(String reason)
    {
        if(cache != null)
            stopParticipating(reason, cache.ourssrc);
        if(remoteAddresses != null)
            remoteAddresses.removeAllElements();
        setRemoteAddresses();
    }

    public void removeTarget(SessionAddress remoteAddress, String reason)
    {
        remoteAddresses.removeElement(remoteAddress);
        setRemoteAddresses();
        if(remoteAddresses.size() == 0 && cache != null)
            stopParticipating(reason, cache.ourssrc);
    }

    private void setRemoteAddresses()
    {
        if(rtpTransmitter != null)
        {
            RTPRawSender rtpRawSender = rtpTransmitter.getSender();
            rtpRawSender.setDestAddresses(remoteAddresses);
        }
        if(rtcpTransmitter != null)
        {
            RTCPRawSender rtcpRawSender = rtcpTransmitter.getSender();
            rtcpRawSender.setDestAddresses(remoteAddresses);
        }
    }

    public void dispose()
    {
        if(rtpConnector != null)
            rtpConnector.close();
        if(defaultsource != null)
            defaultsource.disconnect();
        if(cache != null)
        {
            SSRCInfo s;
            for(Enumeration e = cache.cache.elements(); e.hasMoreElements(); stopParticipating("dispose", s))
            {
                s = (SSRCInfo)e.nextElement();
                if(s.dstream != null)
                    s.dstream.close();
                if(s instanceof SendSSRCInfo)
                    ((SendSSRCInfo)s).close();
            }

        }
        for(int i = 0; i < sendstreamlist.size(); i++)
            removeSendStream((SendStream)sendstreamlist.elementAt(i));

        if(rtpTransmitter != null)
            rtpTransmitter.close();
        if(rtcpTransmitter != null)
            rtcpTransmitter.close();
        if(rtcpForwarder != null)
        {
            RTCPRawReceiver rtcpRawReceiver = (RTCPRawReceiver)rtcpForwarder.getSource();
            rtcpForwarder.close();
            if(rtcpRawReceiver != null)
                rtcpRawReceiver.close();
        }
        if(cleaner != null)
            cleaner.stop();
        if(cache != null)
            cache.destroy();
        if(rtpForwarder != null)
        {
            RTPRawReceiver rtpRawReceiver = (RTPRawReceiver)rtpForwarder.getSource();
            rtpForwarder.close();
            if(rtpRawReceiver != null)
                rtpRawReceiver.close();
        }
    }

    public SessionAddress getRemoteSessionAddress()
    {
        return remoteAddress;
    }

    public int getSSRC()
    {
        return 0;
    }

    private int findLocalPorts()
    {
        boolean found = false;
        int port = -1;
        while(!found) 
        {
            do
            {
                double num = Math.random();
                port = (int)(num * 65535D);
                if(port % 2 != 0)
                    port++;
            } while(port < 1024 || port > 65534);
            try
            {
                DatagramSocket datagramSocket = new DatagramSocket(port);
                datagramSocket.close();
                datagramSocket = new DatagramSocket(port + 1);
                datagramSocket.close();
                found = true;
            }
            catch(SocketException e)
            {
                found = false;
            }
        }
        return port;
    }

    boolean bindtome;
    private SSRCCache cache;
    int ttl;
    int sendercount;
    InetAddress localDataAddress;
    int localDataPort;
    InetAddress localControlAddress;
    int localControlPort;
    InetAddress dataaddress;
    InetAddress controladdress;
    int dataport;
    int controlport;
    RTPPushDataSource rtpsource;
    RTPPushDataSource rtcpsource;
    long defaultSSRC;
    SessionAddress localSenderAddress;
    private SessionAddress localReceiverAddress;
    UDPPacketSender udpsender;
    RTPPacketSender rtpsender;
    RTCPRawSender sender;
    SSRCCacheCleaner cleaner;
    private boolean unicast;
    private boolean startedparticipating;
    private boolean nonparticipating;
    private boolean nosockets;
    private boolean started;
    private boolean initialized;
    protected Vector sessionlistener;
    protected Vector remotelistener;
    protected Vector streamlistener;
    protected Vector sendstreamlistener;
    private static final int GET_ALL_PARTICIPANTS = -1;
    boolean encryption;
    SSRCTable dslist;
    StreamSynch streamSynch;
    FormatInfo formatinfo;
    DataSource defaultsource;
    PushBufferStream defaultstream;
    Format defaultformat;
    BufferControl buffercontrol;
    OverallStats defaultstats;
    OverallTransStats transstats;
    int defaultsourceid;
    Vector sendstreamlist;
    RTPTransmitter rtpTransmitter;
    boolean bds;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private Method m[];
    private Class cl[];
    private Object args[][];
    Vector peerlist;
    boolean multi_unicast;
    Hashtable peerrtplist;
    Hashtable peerrtcplist;
    static FormatInfo supportedList = null;
    static Vector addedList = new Vector();
    private boolean newRtpInterface;
    private SessionAddress remoteAddress;
    private SessionAddress localAddress;
    private RTCPRawReceiver rtcpRawReceiver;
    private RTPRawReceiver rtpRawReceiver;
    private PacketForwarder rtpForwarder;
    private PacketForwarder rtcpForwarder;
    private RTPDemultiplexer rtpDemultiplexer;
    private OverallStats overallStats;
    private boolean participating;
    private UDPPacketSender udpPacketSender;
    private Vector remoteAddresses;
    private RTCPTransmitter rtcpTransmitter;
    private RTPConnector rtpConnector;
    private DatagramSocket dataSocket;
    private DatagramSocket controlSocket;
    private final int MAX_PORT = 65535;

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
