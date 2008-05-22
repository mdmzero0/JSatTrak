// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DataSource.java

package com.sun.media.protocol.https;

import com.sun.media.JMFSecurityManager;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLStreamHandlerFactory;
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

    static Class _mthclass$(String x0)
    {
        try
        {
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x1)
        {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    private static SecurityManager securityManager;

    static 
    {
        securityManager = System.getSecurityManager();
        boolean netscape = false;
        boolean ie = false;
        boolean msjvm = false;
        String javaVendor = System.getProperty("java.vendor", "Sun").toLowerCase();
        if(javaVendor.indexOf("icrosoft") > 0)
            msjvm = true;
        if(securityManager != null)
        {
            netscape = securityManager.toString().indexOf("netscape") != -1;
            ie = securityManager.toString().indexOf("com.ms.security") != -1;
        }
        if(ie || msjvm)
            try
            {
                Class clsFactory = Class.forName("com.ms.net.wininet.WininetStreamHandlerFactory");
                if(clsFactory != null)
                    URL.setURLStreamHandlerFactory((URLStreamHandlerFactory)clsFactory.newInstance());
            }
            catch(Throwable t) { }
        else
        if(!netscape)
        {
            if(!JMFSecurityManager.isJDK12())
                throw new UnsatisfiedLinkError("Fatal Error: DataSource for https protocol needs JDK1.2 or higher VM");
            try
            {
                Class sslproviderC = Class.forName("com.sun.net.ssl.internal.ssl.Provider");
                Object provider = sslproviderC.newInstance();
                Class securityC = Class.forName("java.security.Security");
                Class providerC = Class.forName("java.security.Provider");
                Class systemC = Class.forName("java.lang.System");
                Method addProviderM = securityC.getMethod("addProvider", new Class[] {
                    providerC
                });
                Method setPropertyM = systemC.getMethod("setProperty", new Class[] {
                    java.lang.String.class, java.lang.String.class
                });
                if(addProviderM != null && setPropertyM != null)
                {
                    addProviderM.invoke(securityC, new Object[] {
                        provider
                    });
                    setPropertyM.invoke(systemC, new Object[] {
                        "java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol"
                    });
                }
            }
            catch(Exception e)
            {
                throw new UnsatisfiedLinkError("Fatal Error:Java Secure Socket Extension classes are not present");
            }
            catch(Error e)
            {
                throw new UnsatisfiedLinkError("Fatal Error:Java Secure Socket Extension classes are not present");
            }
        }
    }
}
