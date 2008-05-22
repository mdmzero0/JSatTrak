// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Report.java

package javax.media.rtp.rtcp;

import java.util.Vector;
import javax.media.rtp.Participant;

public interface Report
{

    public abstract Participant getParticipant();

    public abstract long getSSRC();

    public abstract Vector getFeedbackReports();

    public abstract Vector getSourceDescription();
}
