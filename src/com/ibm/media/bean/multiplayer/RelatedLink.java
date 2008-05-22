// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RelatedLink.java

package com.ibm.media.bean.multiplayer;

import java.net.MalformedURLException;
import java.net.URL;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            MultiPlayerBean

public class RelatedLink
{

    public RelatedLink(String rl, long st, long et, MultiPlayerBean o)
        throws MalformedURLException
    {
        startTime = 0L;
        stopTime = 0L;
        uLink = null;
        owner = null;
        owner = o;
        uLink = owner.getURL(rl);
        if(uLink == null)
        {
            throw new MalformedURLException();
        } else
        {
            link = rl;
            startTime = st;
            stopTime = et;
            return;
        }
    }

    public void setLink(String l)
    {
        link = l;
        uLink = owner.getURL(l);
    }

    public String link;
    public long startTime;
    public long stopTime;
    public URL uLink;
    public String description;
    private MultiPlayerBean owner;
}
