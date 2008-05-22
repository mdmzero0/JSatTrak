// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JMFI18N.java

package com.sun.media.util;

import java.io.PrintStream;
import java.util.*;

public class JMFI18N
{

    public JMFI18N()
    {
    }

    public static String getResource(String key)
    {
        Locale currentLocale = Locale.getDefault();
        if(bundle == null)
        {
            try
            {
                bundle = ResourceBundle.getBundle("com.sun.media.util.locale.JMFProps", currentLocale);
            }
            catch(MissingResourceException e)
            {
                System.out.println("Could not load Resources");
                System.exit(0);
            }
            try
            {
                bundleApps = ResourceBundle.getBundle("com.sun.media.util.locale.JMFAppProps", currentLocale);
            }
            catch(MissingResourceException me) { }
        }
        String value = "";
        try
        {
            value = (String)bundle.getObject(key);
        }
        catch(MissingResourceException e)
        {
            if(bundleApps != null)
                try
                {
                    value = (String)bundleApps.getObject(key);
                }
                catch(MissingResourceException mre)
                {
                    System.out.println("Could not find " + key);
                }
            else
                System.out.println("Could not find " + key);
        }
        return value;
    }

    public static ResourceBundle bundle = null;
    public static ResourceBundle bundleApps = null;

}
