// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPSourceInfoCache.java

package com.sun.media.rtp;

import java.util.Hashtable;

// Referenced classes of package com.sun.media.rtp:
//            RTPSourceInfo, RTPRemoteSourceInfo, RTPLocalSourceInfo, SSRCCache

public class RTPSourceInfoCache
{

    public RTPSourceInfoCache()
    {
        cache = new Hashtable(20);
    }

    public void setMainCache(RTPSourceInfoCache main)
    {
        this.main = main;
    }

    public RTPSourceInfoCache getMainCache()
    {
        if(main == null)
            main = new RTPSourceInfoCache();
        return main;
    }

    public void setSSRCCache(SSRCCache ssrccache)
    {
        main.ssrccache = ssrccache;
    }

    public RTPSourceInfo get(String cname, boolean local)
    {
        RTPSourceInfo info = null;
        synchronized(this)
        {
            info = (RTPSourceInfo)cache.get(cname);
            if(info == null && !local)
            {
                info = new RTPRemoteSourceInfo(cname, main);
                cache.put(cname, info);
            }
            if(info == null && local)
            {
                info = new RTPLocalSourceInfo(cname, main);
                cache.put(cname, info);
            }
        }
        return info;
    }

    public void remove(String cname)
    {
        cache.remove(cname);
    }

    public Hashtable getCacheTable()
    {
        return cache;
    }

    public SSRCCache ssrccache;
    Hashtable cache;
    RTPSourceInfoCache main;
}
