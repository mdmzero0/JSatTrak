// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Master.java

package com.ibm.media.content.application.mvr;

import hm20masterorig;
import java.applet.Applet;
import java.awt.Component;
import java.io.PrintStream;
import java.net.URL;
import javax.media.Controller;
import javax.media.MediaLocator;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullDataSource;

public class Master extends hm20masterorig
{

    public static void b6(Master master)
    {
        master.b9();
    }

    public Controller b8()
    {
        return hmca;
    }

    private void b9()
    {
        try
        {
            String s = source.getLocator().toString();
            String s1;
            if((s1 = super.mainApplet.getParameter("T1URL")) != null)
            {
                int i;
                if((i = s1.lastIndexOf("###")) != -1)
                    s1 = s1.substring(0, i);
                if(s1.lastIndexOf("http://") == -1)
                {
                    String s3 = new String("http://");
                    s1 = s3.concat(s1);
                }
            }
            String s2 = super.mainApplet.getParameter("T2URL");
            boolean flag = false;
            String s4 = super.mainApplet.getParameter("useDocumentBase");
            if(s4 != null && s4.equalsIgnoreCase("y"))
                flag = true;
            URL url = flag ? super.mainApplet.getDocumentBase() : super.mainApplet.getCodeBase();
            mvrURLFILE = s != null ? new URL(url, s) : url;
            mvrFileForMaster = mvrURLFILE.toString();
            if(s2 != null)
            {
                mvrFileForMaster = mvrFileForMaster.concat("%%%");
                mvrFileForMaster = mvrFileForMaster.concat(s2);
            }
            mvrFileForMaster = mvrFileForMaster.replace('\\', '/');
        }
        catch(Exception exception)
        {
            System.out.println("EX: " + exception);
        }
    }

    public void a8(int i, URL url, String s)
    {
        if(i == 2)
        {
            if(s == null)
                s = "_self";
            MasterContrAdaptor.b6(hmca, url, s);
        } else
        {
            super.a8(i, url, s);
        }
    }

    public void setBounds(int i, int j, int k, int l)
    {
        ((Handler.AppletAdaptor)super.mainApplet).cb(i, j, k, l);
    }

    public void a5(short word0, short word1)
    {
        super.mainApplet.setSize(word0, word1);
    }

    public void ca(byte abyte0[])
    {
        trackDefs = abyte0;
    }

    public Master(PullDataSource pulldatasource, Applet applet)
    {
        hmca = new MasterContrAdaptor();
        source = pulldatasource;
        super.mainApplet = applet;
    }

    public MasterContrAdaptor hmca;
    public PullDataSource source;
    public URL mvrURLFILE;
    public String mvrFileForMaster;
    public byte trackDefs[];
    public Object argsArray;

    private class MasterContrAdaptor extends BasicController
    {

        public static void b6(MasterContrAdaptor mastercontradaptor, URL url, String s)
        {
            mastercontradaptor.b7(url, s);
        }

        private void b7(URL url, String s)
        {
            sendEvent(new ShowDocumentEvent(this, url, s));
        }

        public void stopAtTime()
        {
        }

        public void doClose()
        {
            bf.destroy();
        }

        public void doStop()
        {
            bf.stop();
        }

        public synchronized void stop()
        {
            super.stop();
            sendEvent(new StopByRequestEvent(this, 600, 500, getTargetState(), getMediaTime()));
        }

        public void doStart()
        {
            bf.start();
        }

        public void abortPrefetch()
        {
        }

        public boolean doPrefetch()
        {
            Master.b6(bf);
            Object aobj[] = new Object[5];
            aobj[0] = ((hm20masterorig) (bf)).mainApplet;
            Master master;
            aobj[1] = (master = bf).mvrURLFILE;
            Master master1;
            aobj[2] = (master1 = bf).mvrFileForMaster;
            aobj[3] = new Long(System.currentTimeMillis());
            Master master2;
            aobj[4] = (master2 = bf).trackDefs;
            bf.equals(((Object) (aobj)));
            return true;
        }

        public void abortRealize()
        {
        }

        public boolean doRealize()
        {
            return true;
        }

        public boolean isConfigurable()
        {
            return false;
        }

        public final Master bf;

        public MasterContrAdaptor()
        {
            bf = Master.this;
            bf = Master.this;
        }
    }

}
