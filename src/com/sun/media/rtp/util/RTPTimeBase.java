// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPTimeBase.java

package com.sun.media.rtp.util;

import com.sun.media.Log;
import java.util.Vector;
import javax.media.Time;
import javax.media.TimeBase;

// Referenced classes of package com.sun.media.rtp.util:
//            RTPTimeReporter

public class RTPTimeBase
    implements TimeBase
{

    public static RTPTimeBase find(RTPTimeReporter r, String cname)
    {
        RTPTimeBase rtptimebase;
        synchronized(timeBases)
        {
            RTPTimeBase result = null;
            for(int i = 0; i < timeBases.size(); i++)
            {
                RTPTimeBase tb = (RTPTimeBase)timeBases.elementAt(i);
                if(!tb.cname.equals(cname))
                    continue;
                result = tb;
                break;
            }

            if(result == null)
            {
                Log.comment("Created RTP time base for session: " + cname + "\n");
                result = new RTPTimeBase(cname);
                timeBases.addElement(result);
            }
            if(r != null)
            {
                if(result.getMaster() == null)
                    result.setMaster(r);
                result.reporters.addElement(r);
            }
            rtptimebase = result;
        }
        return rtptimebase;
    }

    public static void remove(RTPTimeReporter r, String cname)
    {
        synchronized(timeBases)
        {
            for(int i = 0; i < timeBases.size(); i++)
            {
                RTPTimeBase tb = (RTPTimeBase)timeBases.elementAt(i);
                if(!tb.cname.equals(cname))
                    continue;
                tb.reporters.removeElement(r);
                if(tb.reporters.size() == 0)
                {
                    tb.master = null;
                    timeBases.removeElement(tb);
                } else
                {
                    synchronized(tb)
                    {
                        if(tb.master == r)
                            tb.setMaster((RTPTimeReporter)tb.reporters.elementAt(0));
                    }
                }
                break;
            }

        }
    }

    public static RTPTimeBase getMapper(String cname)
    {
        RTPTimeBase rtptimebase;
        synchronized(timeBases)
        {
            rtptimebase = find(null, cname);
        }
        return rtptimebase;
    }

    public static RTPTimeBase getMapperUpdatable(String cname)
    {
        RTPTimeBase rtptimebase1;
        synchronized(timeBases)
        {
            RTPTimeBase tb = find(null, cname);
            if(tb.offsetUpdatable)
            {
                tb.offsetUpdatable = false;
                RTPTimeBase rtptimebase = tb;
                return rtptimebase;
            }
            rtptimebase1 = null;
        }
        return rtptimebase1;
    }

    public static void returnMapperUpdatable(RTPTimeBase tb)
    {
        synchronized(timeBases)
        {
            tb.offsetUpdatable = true;
        }
    }

    RTPTimeBase(String cname)
    {
        master = null;
        reporters = new Vector();
        origin = 0L;
        offset = 0L;
        offsetUpdatable = true;
        this.cname = cname;
    }

    public Time getTime()
    {
        return new Time(getNanoseconds());
    }

    public synchronized long getNanoseconds()
    {
        return master == null ? 0L : master.getRTPTime();
    }

    public synchronized void setMaster(RTPTimeReporter r)
    {
        master = r;
    }

    public synchronized RTPTimeReporter getMaster()
    {
        return master;
    }

    public synchronized void setOrigin(long orig)
    {
        origin = orig;
    }

    public long getOrigin()
    {
        return origin;
    }

    public synchronized void setOffset(long off)
    {
        offset = off;
    }

    public long getOffset()
    {
        return offset;
    }

    static Vector timeBases = new Vector();
    static int SSRC_UNDEFINED = 0;
    String cname;
    RTPTimeReporter master;
    Vector reporters;
    long origin;
    long offset;
    boolean offsetUpdatable;

}
