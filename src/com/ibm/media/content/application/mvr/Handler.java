// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Handler.java

package com.ibm.media.content.application.mvr;

import com.ibm.media.ReplaceURLEvent;
import com.ibm.media.ShowDocumentEvent;
import com.ibm.media.controls.ParametersControl;
import com.ibm.media.util.PullSourceStream2InputStream;
import com.sun.media.BasicController;
import com.sun.media.BasicPlayer;
import java.applet.Applet;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import javax.media.*;
import javax.media.protocol.*;

// Referenced classes of package com.ibm.media.content.application.mvr:
//            Master

public class Handler extends BasicPlayer
{

    public static void cd(Handler handler, URL url)
    {
        handler.ch(url);
    }

    public static void ce(Handler handler, Component component)
    {
        handler.ci(component);
    }

    public static void cf(Handler handler)
    {
        handler.cg();
    }

    public static Applet b6(Handler handler)
    {
        return handler.bi;
    }

    public Handler()
    {
        bj = new ParametersControl();
        bh = false;
        bg = new AppletAdaptor();
    }

    private void cg()
    {
        sendEvent(new SizeChangeEvent(this, bg.getSize().width, bg.getSize().height, 1.0F));
    }

    private void ch(URL url)
    {
        sendEvent(new ReplaceURLEvent(this, url));
    }

    public void processEvent(ControllerEvent controllerevent)
    {
        if(controllerevent instanceof ReplaceURLEvent)
            sendEvent((ReplaceURLEvent)controllerevent);
        if(controllerevent instanceof ShowDocumentEvent)
            sendEvent((ShowDocumentEvent)controllerevent);
        else
            super.processEvent(controllerevent);
    }

    public void doClose()
    {
        super.controlComp = null;
        super.doClose();
    }

    private void ci(Component component)
    {
        ((Panel)super.controlComp).add(component);
    }

    public Component getControlPanelComponent()
    {
        if(super.controlComp == null)
            super.controlComp = new Panel(new BorderLayout());
        super.getControlPanelComponent();
        return super.controlComp;
    }

    public Component getVisualComponent()
    {
        super.getVisualComponent();
        return bg;
    }

    public void setSource(DataSource datasource)
    {
        if(!(datasource instanceof PullDataSource))
            throw new IncompatibleSourceException("Can accept only PullDataSource");
        bk = datasource;
        String s = datasource.getContentType();
        ContentDescriptor contentdescriptor = new ContentDescriptor(s);
        if(!s.equals("application.mvr") && !s.equals("application.x_unknown_content_type_mvr_auto_file") || !(datasource instanceof PullDataSource))
        {
            throw new IncompatibleSourceException();
        } else
        {
            bg.setLayout(null);
            bl = new Master((PullDataSource)datasource, bg);
            manageController(bl.b8());
            datasource.start();
            javax.media.protocol.PullSourceStream pullsourcestream = ((PullDataSource)datasource).getStreams()[0];
            bd = new PullSourceStream2InputStream(pullsourcestream);
            bh = false;
            super.setSource(datasource);
            return;
        }
    }

    public Control[] getControls()
    {
        Control acontrol[] = new Control[1];
        acontrol[0] = bj;
        return acontrol;
    }

    public void setHostingHM(Applet applet)
    {
        bi = applet;
    }

    public void setTrackDefs(byte abyte0[])
    {
        bl.ca(abyte0);
    }

    public void updateStats()
    {
    }

    public boolean videoEnabled()
    {
        return false;
    }

    public boolean audioEnabled()
    {
        return false;
    }

    public TimeBase getMasterTimeBase()
    {
        return getClock().getTimeBase();
    }

    public Master bl;
    public DataSource bk;
    public InputStream bd;
    public ParametersControl bj;
    public Applet bi;
    public boolean bh;
    public Applet bg;

    private class AppletAdaptor extends Applet
    {

        public boolean equals(Object obj)
        {
            if(obj instanceof Object[])
            {
                Object aobj[] = (Object[])obj;
                switch(((Integer)aobj[0]).intValue())
                {
                default:
                    break;

                case 0: // '\0'
                    int i = ((Integer)aobj[1]).intValue();
                    byte abyte0[] = (byte[])aobj[2];
                    aobj[1] = Toolkit.getDefaultToolkit().createImage(abyte0);
                    break;

                case 1: // '\001'
                    Handler.cd(Handler.this, (URL)aobj[2]);
                    break;

                case 2: // '\002'
                    if(Handler.b6(Handler.this) != null)
                    {
                        Handler.b6(Handler.this).equals(obj);
                        break;
                    }
                    Container container = getParent();
                    if(container == null)
                        break;
                    int j = ((Integer)aobj[1]).intValue();
                    if(j >= 99)
                    {
                        boolean flag = j == 99;
                        j = flag ? 3 : 0;
                    }
                    container.setCursor(new Cursor(j));
                    break;

                case 3: // '\003'
                    if(Handler.b6(Handler.this) != null)
                    {
                        Handler.b6(Handler.this).equals(obj);
                        break;
                    }
                    Container container1 = getParent();
                    if(container1 != null)
                        aobj[1] = new Integer(container1.getCursor().getType());
                    break;

                case 4: // '\004'
                    try
                    {
                        Handler handler = Handler.this;
                        if(handler.bk.getLocator().getURL().equals((URL)aobj[1]))
                        {
                            Handler handler1 = Handler.this;
                            if(!handler1.bh)
                            {
                                aobj[1] = bd;
                                boolean flag1 = true;
                                Handler handler2 = Handler.this;
                                handler2.bh = flag1;
                                break;
                            }
                        }
                        if(Handler.b6(Handler.this) != null)
                        {
                            Handler.b6(Handler.this).equals(obj);
                        } else
                        {
                            URL url = (URL)aobj[1];
                            aobj[1] = null;
                            URLConnection urlconnection = url.openConnection();
                            urlconnection.setUseCaches(false);
                            aobj[1] = urlconnection.getInputStream();
                        }
                    }
                    catch(MalformedURLException malformedurlexception)
                    {
                        System.out.println("EX: " + malformedurlexception);
                    }
                    catch(IOException ioexception)
                    {
                        System.out.println("EX: " + ioexception);
                    }
                    break;

                case 5: // '\005'
                    if(Handler.b6(Handler.this) != null)
                    {
                        Handler.b6(Handler.this).equals(obj);
                        break;
                    }
                    String s = (String)aobj[1];
                    aobj[1] = null;
                    if(s == null)
                        break;
                    try
                    {
                        aobj[1] = Class.forName(s);
                    }
                    catch(ClassNotFoundException classnotfoundexception)
                    {
                        System.out.println("EX: " + classnotfoundexception);
                    }
                    break;
                }
                return false;
            } else
            {
                return super.equals(obj);
            }
        }

        public URL getDocumentBase()
        {
            if(Handler.b6(Handler.this) != null)
                return Handler.b6(Handler.this).getDocumentBase();
            else
                return null;
        }

        public void showStatus(String s)
        {
            if(Handler.b6(Handler.this) != null)
                Handler.b6(Handler.this).showStatus(s);
            else
                System.out.println(s);
        }

        public URL getCodeBase()
        {
            if(Handler.b6(Handler.this) != null)
                return Handler.b6(Handler.this).getCodeBase();
            else
                return null;
        }

        public String getParameter(String s)
        {
            Handler handler = Handler.this;
            String s1 = handler.bj.get(s);
            if(s1 == null && Handler.b6(Handler.this) != null)
                return Handler.b6(Handler.this).getParameter(s);
            else
                return s1;
        }

        public synchronized Component add(Component component, int i)
        {
            if(Handler.b6(Handler.this) != null)
            {
                gui = component;
                super.add(component, i);
            } else
            {
                Handler.ce(Handler.this, component);
            }
            Handler.cf(Handler.this);
            return component;
        }

        public synchronized Component add(Component component)
        {
            playerExist = true;
            cc();
            component.setSize(parsedSize);
            super.add(component);
            Handler.cf(Handler.this);
            return component;
        }

        public Dimension size()
        {
            return parsedSize;
        }

        public void setSize(int i, int j)
        {
            if(Handler.b6(Handler.this) != null)
            {
                parsedSize = Handler.b6(Handler.this).getSize();
            } else
            {
                parsedSize = new Dimension(i, j);
                super.setSize(i, j);
            }
        }

        public void cb(int i, int j, int k, int l)
        {
            guiBounds = new Rectangle(i, j, k, l);
            cc();
            Handler.cf(Handler.this);
        }

        private synchronized void cc()
        {
            if(gui != null)
            {
                if(Handler.b6(Handler.this) != null)
                {
                    if(playerExist)
                    {
                        setBounds(0, 0, parsedSize.width, parsedSize.height);
                        gui.setBounds(guiBounds);
                    } else
                    {
                        setBounds(0, parsedSize.height - guiBounds.height, guiBounds.width, guiBounds.height);
                        gui.setBounds(0, 0, guiBounds.width, guiBounds.height);
                    }
                } else
                {
                    gui.setBounds(0, 0, guiBounds.width, guiBounds.height);
                }
            } else
            if(playerExist)
                setBounds(0, 0, parsedSize.width, parsedSize.height);
        }

        public final Handler this$0;
        public Component gui;
        public Rectangle guiBounds;
        public boolean playerExist;
        public Dimension parsedSize;

        public AppletAdaptor()
        {
            this$0 = Handler.this;
            this$0 = Handler.this;
            guiBounds = new Rectangle(0, 0, 0, 0);
            playerExist = false;
            parsedSize = new Dimension(0, 0);
        }
    }

}
