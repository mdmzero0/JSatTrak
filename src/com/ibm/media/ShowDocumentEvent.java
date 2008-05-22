// Decompiled by DJ v3.10.10.93 Copyright 2007 Atanas Neshkov  Date: 5/21/2008 10:01:44 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ShowDocumentEvent.java

package com.ibm.media;

import java.net.URL;
import javax.media.Controller;
import javax.media.ControllerEvent;

public class ShowDocumentEvent extends ControllerEvent
{

    public ShowDocumentEvent(Controller from, URL u, String s)
    {
        super(from);
        this.u = u;
        this.s = s;
    }

    public URL getURL()
    {
        return u;
    }

    public String getString()
    {
        return s;
    }

    private URL u;
    private String s;
}