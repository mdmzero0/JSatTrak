// Decompiled by DJ v3.10.10.93 Copyright 2007 Atanas Neshkov  Date: 5/21/2008 10:01:44 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ReplaceURLEvent.java

package com.ibm.media;

import java.net.URL;
import javax.media.Controller;
import javax.media.ControllerEvent;

public class ReplaceURLEvent extends ControllerEvent
{

    public ReplaceURLEvent(Controller from, URL u)
    {
        super(from);
        this.u = u;
    }

    public URL getURL()
    {
        return u;
    }

    private URL u;
}