// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSessionMgrList.java

package com.sun.media.rtp;

import java.util.Hashtable;
import javax.media.rtp.SessionAddress;

// Referenced classes of package com.sun.media.rtp:
//            RTPSessionMgr

public final class RTPSessionMgrList
{

    public RTPSessionMgrList()
    {
    }

    public static void addRTPSM(RTPSessionMgr sm)
    {
        SessionAddress destaddr = sm.getSessionAddress();
        RTPSessionMgr mgr = (RTPSessionMgr)list.get(destaddr);
        if(mgr == null)
            list.put(destaddr, sm);
    }

    public static RTPSessionMgr getRTPSM(SessionAddress destaddr)
    {
        RTPSessionMgr mgr = (RTPSessionMgr)list.get(destaddr);
        return mgr;
    }

    public static void removeRTPSM(RTPSessionMgr sm)
    {
        SessionAddress destaddr = sm.getSessionAddress();
        list.remove(destaddr);
    }

    private static Hashtable list = new Hashtable(5);

}
