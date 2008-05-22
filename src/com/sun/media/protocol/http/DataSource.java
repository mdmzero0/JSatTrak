// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol.http;

import java.io.IOException;
import javax.media.protocol.SourceCloneable;

public class DataSource extends com.sun.media.protocol.DataSource
    implements SourceCloneable
{

    public DataSource()
    {
    }

    public javax.media.protocol.DataSource createClone()
    {
        DataSource ds = new DataSource();
        ds.setLocator(getLocator());
        if(super.connected)
            try
            {
                ds.connect();
            }
            catch(IOException e)
            {
                return null;
            }
        return ds;
    }
}
