// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MultiPlayerBeanBeanInfo.java

package com.ibm.media.bean.multiplayer;

import java.awt.Image;
import java.beans.*;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            JMFUtil

public class MultiPlayerBeanBeanInfo extends SimpleBeanInfo
{

    public MultiPlayerBeanBeanInfo()
    {
    }

    public PropertyDescriptor[] getPropertyDescriptors()
    {
        try
        {
            PropertyDescriptor background = new PropertyDescriptor("background", beanClass);
            PropertyDescriptor foreground = new PropertyDescriptor("foreground", beanClass);
            PropertyDescriptor font = new PropertyDescriptor("font", beanClass);
            PropertyDescriptor UrlVisible = new PropertyDescriptor("URLVisible", beanClass);
            UrlVisible.setDisplayName(JMFUtil.getBIString("MEDIA_NAME_VISIBLE"));
            UrlVisible.setBound(true);
            PropertyDescriptor PanelVisible = new PropertyDescriptor("panelVisible", beanClass);
            PanelVisible.setDisplayName(JMFUtil.getBIString("CONTROL_PANEL_VISIBLE"));
            PanelVisible.setBound(true);
            PropertyDescriptor loop = new PropertyDescriptor("looping", beanClass);
            loop.setDisplayName(JMFUtil.getBIString("LOOP"));
            loop.setBound(true);
            PropertyDescriptor sequential = new PropertyDescriptor("sequentialPlay", beanClass);
            sequential.setDisplayName(JMFUtil.getBIString("SEQUENTIAL"));
            sequential.setBound(true);
            PropertyDescriptor fixAspect = new PropertyDescriptor("fixAspectRatio", beanClass);
            fixAspect.setDisplayName(JMFUtil.getBIString("FIXASPECT"));
            fixAspect.setBound(true);
            PropertyDescriptor buttonPosition = new PropertyDescriptor("buttonPosition", beanClass);
            buttonPosition.setDisplayName(JMFUtil.getBIString("BUTTONPOSITION"));
            buttonPosition.setBound(true);
            buttonPosition.setPropertyEditorClass(com.ibm.media.bean.multiplayer.ButtonPositionEditor.class);
            PropertyDescriptor loadOnInit = new PropertyDescriptor("loadOnInit", beanClass);
            loadOnInit.setDisplayName(JMFUtil.getBIString("LOAD_ALL"));
            loadOnInit.setBound(true);
            PropertyDescriptor linkList = new PropertyDescriptor("links", beanClass);
            linkList.setBound(true);
            linkList.setPropertyEditorClass(com.ibm.media.bean.multiplayer.LinksArrayEditor.class);
            linkList.setDisplayName(JMFUtil.getBIString("RELATED_LINKS"));
            PropertyDescriptor mediaNames = new PropertyDescriptor("mediaNames", beanClass);
            mediaNames.setBound(true);
            mediaNames.setPropertyEditorClass(com.ibm.media.bean.multiplayer.MediaArrayEditor.class);
            mediaNames.setDisplayName(JMFUtil.getBIString("MEDIA_GROUP"));
            PropertyDescriptor rv[] = {
                mediaNames, linkList, UrlVisible, PanelVisible, loop, sequential, fixAspect, buttonPosition, loadOnInit, background, 
                foreground, font
            };
            return rv;
        }
        catch(IntrospectionException e)
        {
            throw new Error(e.toString());
        }
    }

    public int getDefaultPropertyIndex()
    {
        return 0;
    }

    public BeanDescriptor getBeanDescriptor()
    {
        return new BeanDescriptor(beanClass);
    }

    public EventSetDescriptor[] getEventSetDescriptors()
    {
        try
        {
            EventSetDescriptor cl = new EventSetDescriptor(beanClass, "controllerUpdate", javax.media.ControllerListener.class, "controllerUpdate");
            EventSetDescriptor pc = new EventSetDescriptor(beanClass, "propertyChange", java.beans.PropertyChangeListener.class, "propertyChange");
            cl.setDisplayName("Controller Events");
            EventSetDescriptor rv[] = {
                cl, pc
            };
            return rv;
        }
        catch(IntrospectionException e)
        {
            throw new Error(e.toString());
        }
    }

    public Image getIcon(int ic)
    {
        switch(ic)
        {
        case 1: // '\001'
            return loadImage("IconColor16.gif");

        case 2: // '\002'
            return loadImage("IconColor32.gif");

        case 3: // '\003'
            return loadImage("IconMono16.gif");

        case 4: // '\004'
            return loadImage("IconMono32.gif");
        }
        return null;
    }

    private static final Class beanClass;

    static 
    {
        beanClass = com.ibm.media.bean.multiplayer.MultiPlayerBean.class;
    }
}
