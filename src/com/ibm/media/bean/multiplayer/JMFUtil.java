// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JMFUtil.java

package com.ibm.media.bean.multiplayer;

import java.awt.*;
import java.io.PrintStream;
import java.util.*;

public class JMFUtil
{

    public JMFUtil()
    {
    }

    public static String getString(String key)
    {
        if(res == null)
            setResourceBundle(null);
        return res.getString(key);
    }

    public static String getLang()
    {
        return lang;
    }

    public static void setResourceBundle(Locale l)
    {
        locale = Locale.getDefault();
        try
        {
            res = (ListResourceBundle)ResourceBundle.getBundle("com.ibm.media.bean.multiplayer.nls.JMFConst", locale);
        }
        catch(MissingResourceException m)
        {
            locale = new Locale("en", "US");
            res = (ListResourceBundle)ResourceBundle.getBundle("com.ibm.media.bean.multiplayer.nls.JMFConst", locale);
            System.out.println("Locale not supported, defaulting to english-US.");
        }
        lang = locale.getLanguage();
        ctry = locale.getCountry();
    }

    public static String getBIString(String key)
    {
        if(beanInfoRes == null)
            setBIResourceBundle(null);
        return beanInfoRes.getString(key);
    }

    public static void setBIResourceBundle(Locale l)
    {
        locale = Locale.getDefault();
        try
        {
            beanInfoRes = (ListResourceBundle)ResourceBundle.getBundle("com.ibm.media.bean.multiplayer.nls.MultiPlayerBeanInfoResources", locale);
        }
        catch(MissingResourceException m)
        {
            locale = new Locale("en", "US");
            beanInfoRes = (ListResourceBundle)ResourceBundle.getBundle("com.ibm.media.bean.multiplayer.nls.MultiPlayerBeanInfoResources", locale);
            System.out.println("Locale not supported, defaulting to english-US.");
        }
        lang = locale.getLanguage();
        ctry = locale.getCountry();
    }

    public static Locale getLocale(String l)
    {
        if(l == null)
            return Locale.getDefault();
        String local = l.trim().toUpperCase();
        if(local.compareTo("FRANCE") == 0)
            return Locale.FRANCE;
        if(local.compareTo("GERMANY") == 0)
            return Locale.GERMANY;
        if(local.compareTo("ITALY") == 0)
            return Locale.ITALY;
        if(local.compareTo("JAPAN") == 0)
            return Locale.JAPAN;
        if(local.compareTo("KOREA") == 0)
            return Locale.KOREA;
        if(local.compareTo("CHINA") == 0)
            return Locale.CHINA;
        if(local.compareTo("PRC") == 0)
            return Locale.PRC;
        if(local.compareTo("TAIWAN") == 0)
            return Locale.TAIWAN;
        if(local.compareTo("UK") == 0)
            return Locale.UK;
        if(local.compareTo("US") == 0)
            return Locale.US;
        if(local.compareTo("CANADA") == 0)
            return Locale.CANADA;
        if(local.compareTo("CANADA_FRENCH") == 0)
            return Locale.CANADA_FRENCH;
        else
            return null;
    }

    public static Panel doGridbagLayout2(Component comp[], int column, int stretch)
    {
        Panel pan = new Panel();
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        pan.setLayout(gb);
        pan.setBackground(Color.lightGray);
        pan.setForeground(Color.black);
        c.insets = new Insets(1, 0, 0, 1);
        c.anchor = 17;
        for(int i = 0; i < comp.length; i++)
        {
            c.gridwidth = 1;
            c.weighty = 0.0D;
            c.weightx = 0.0D;
            c.fill = 0;
            if((i % column - column) + 1 == 0)
                c.gridwidth = 0;
            if((i % column - stretch) + 1 == 0)
            {
                c.fill = 2;
                c.weighty = 1.0D;
                c.weightx = 1.0D;
            }
            gb.setConstraints(comp[i], c);
            pan.add(comp[i], c);
        }

        return pan;
    }

    public static void centerComponent(Panel pan, Component comp)
    {
        doDebug("centerComponent");
        GridBagLayout gb = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        pan.setLayout(gb);
        c.insets = new Insets(0, 0, 0, 0);
        c.anchor = 10;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weighty = 0.0D;
        c.weightx = 0.0D;
        c.fill = 0;
        gb.setConstraints(comp, c);
        pan.add(comp, c);
    }

    public static float aspectRatio(float width, float height, int controllerHeight)
    {
        return width / (height - (float)controllerHeight);
    }

    public static void center(Panel parent, Component comp, boolean fit, int dheight)
    {
        int pwidth = parent.getSize().width;
        int pheight = parent.getSize().height;
        comp.setBounds(parent.getInsets().left, parent.getInsets().top, pwidth - parent.getInsets().left - parent.getInsets().right, pheight - parent.getInsets().top - parent.getInsets().bottom);
    }

    public static void setCenterLocation(Frame self, Frame parent, int width, int height)
    {
        doDebug("setCenterLocation");
        Rectangle rect;
        if(parent != null)
        {
            rect = parent.getBounds();
        } else
        {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            rect = new Rectangle(new Point(0, 0), d);
        }
        self.setSize(width, height);
        self.setLocation(rect.x + (rect.width - width) / 2, rect.y + (rect.height - height) / 2);
    }

    public static void copyStringArray(String oldArray[], String newArray[])
    {
        if((oldArray == null) | (newArray == null))
            return;
        for(int i = 0; i < oldArray.length; i++)
            newArray[i] = oldArray[i];

    }

    public static void copyShortenStringArray(String oldArray[], String newArray[], int index, int size)
    {
        if((oldArray == null) | (newArray == null))
            return;
        for(int i = 0; i < newArray.length; i++)
            if(i >= index)
                newArray[i] = oldArray[i + size];
            else
                newArray[i] = oldArray[i];

    }

    public static String parseArrayIntoString(String value[])
    {
        StringBuffer property = new StringBuffer("");
        if(value != null)
        {
            for(int i = 0; i < value.length; i++)
            {
                property.append(value[i]);
                if(i != value.length - 1)
                    property.append(",");
                doDebug(property.toString());
            }

        }
        return property.toString();
    }

    public static String[] parseStringIntoArray(String value)
    {
        if(value == null)
            return null;
        if(value.length() == 0)
            return null;
        String temp = value;
        String sub = "";
        String tempArray[] = null;
        int length = 0;
        Vector strings = new Vector();
        int index = -1;
        doDebug("Value = " + value);
        index = temp.indexOf(",");
        for(length = temp.length(); index != -1; length = temp.length())
        {
            sub = temp.substring(0, index);
            strings.addElement(sub);
            if(index + 1 < length)
                temp = temp.substring(index + 1, length);
            else
            if(index + 1 == length)
            {
                temp = "";
                strings.addElement("");
            }
            index = temp.indexOf(",");
        }

        if((temp != null) & (length != 0))
            strings.addElement(temp.substring(0, length));
        tempArray = new String[strings.size()];
        for(int i = 0; i < strings.size(); i++)
            tempArray[i] = (String)strings.elementAt(i);

        return tempArray;
    }

    public static String convertString(String s)
    {
        if(s == null)
            return "";
        if(s.length() == 0)
            return s;
        int count = 0;
        int index = -1;
        StringBuffer newString = new StringBuffer(s);
        for(int i = 0; i < newString.length(); i++)
            if(newString.charAt(i) == '\\' || newString.charAt(i) == '"')
            {
                newString.insert(i, "\\");
                i++;
            }

        return newString.toString();
    }

    public static boolean msVersion()
    {
        boolean msVersion;
        try
        {
            Class sysVerMgr = Class.forName("com.ms.util.SystemVersionManager");
            msVersion = true;
        }
        catch(Throwable e)
        {
            msVersion = false;
        }
        return msVersion;
    }

    private static void doDebug(String s1)
    {
    }

    private static Locale locale;
    private static ListResourceBundle res;
    private static ListResourceBundle beanInfoRes;
    private static String lang = null;
    private static String ctry = null;

}
