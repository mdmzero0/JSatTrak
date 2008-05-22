/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
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
