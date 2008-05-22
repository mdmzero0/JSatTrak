// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SessionManager.java

package javax.media.rtp;

import java.io.IOException;
import java.util.Vector;
import javax.media.Controls;
import javax.media.Format;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.DataSource;
import javax.media.rtp.rtcp.SourceDescription;

// Referenced classes of package javax.media.rtp:
//            InvalidSessionAddressException, SSRCInUseException, SessionAddress, EncryptionInfo, 
//            SessionListener, RemoteListener, ReceiveStreamListener, SendStreamListener, 
//            LocalParticipant, RTPStream, GlobalReceptionStats, GlobalTransmissionStats, 
//            SendStream

/**
 * @deprecated Interface SessionManager is deprecated
 */

public interface SessionManager
    extends Controls
{

    public abstract int initSession(SessionAddress sessionaddress, long l, SourceDescription asourcedescription[], double d, double d1)
        throws InvalidSessionAddressException;

    public abstract int initSession(SessionAddress sessionaddress, SourceDescription asourcedescription[], double d, double d1)
        throws InvalidSessionAddressException;

    public abstract int startSession(SessionAddress sessionaddress, int i, EncryptionInfo encryptioninfo)
        throws IOException, InvalidSessionAddressException;

    public abstract int startSession(SessionAddress sessionaddress, SessionAddress sessionaddress1, SessionAddress sessionaddress2, EncryptionInfo encryptioninfo)
        throws IOException, InvalidSessionAddressException;

    public abstract void addSessionListener(SessionListener sessionlistener);

    public abstract void addRemoteListener(RemoteListener remotelistener);

    public abstract void addReceiveStreamListener(ReceiveStreamListener receivestreamlistener);

    public abstract void addSendStreamListener(SendStreamListener sendstreamlistener);

    public abstract void removeSessionListener(SessionListener sessionlistener);

    public abstract void removeRemoteListener(RemoteListener remotelistener);

    public abstract void removeReceiveStreamListener(ReceiveStreamListener receivestreamlistener);

    public abstract void removeSendStreamListener(SendStreamListener sendstreamlistener);

    public abstract long getDefaultSSRC();

    public abstract Vector getRemoteParticipants();

    public abstract Vector getActiveParticipants();

    public abstract Vector getPassiveParticipants();

    public abstract LocalParticipant getLocalParticipant();

    public abstract Vector getAllParticipants();

    public abstract Vector getReceiveStreams();

    public abstract Vector getSendStreams();

    public abstract RTPStream getStream(long l);

    public abstract int getMulticastScope();

    public abstract void setMulticastScope(int i);

    public abstract void closeSession(String s);

    public abstract String generateCNAME();

    public abstract long generateSSRC();

    public abstract SessionAddress getSessionAddress();

    public abstract SessionAddress getLocalSessionAddress();

    public abstract GlobalReceptionStats getGlobalReceptionStats();

    public abstract GlobalTransmissionStats getGlobalTransmissionStats();

    public abstract SendStream createSendStream(int i, DataSource datasource, int j)
        throws UnsupportedFormatException, SSRCInUseException, IOException;

    public abstract SendStream createSendStream(DataSource datasource, int i)
        throws UnsupportedFormatException, IOException;

    public abstract void addFormat(Format format, int i);

    public abstract int startSession(int i, EncryptionInfo encryptioninfo)
        throws IOException;

    public abstract void addPeer(SessionAddress sessionaddress)
        throws IOException, InvalidSessionAddressException;

    public abstract void removePeer(SessionAddress sessionaddress);

    public abstract void removeAllPeers();

    public abstract Vector getPeers();

    public static final long SSRC_UNSPEC = 0L;
}
