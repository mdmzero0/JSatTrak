// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LocalParticipant.java

package javax.media.rtp;

import javax.media.rtp.rtcp.SourceDescription;

// Referenced classes of package javax.media.rtp:
//            Participant

public interface LocalParticipant
    extends Participant
{

    public abstract void setSourceDescription(SourceDescription asourcedescription[]);
}
