// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RTPManager.java

package javax.media.rtp;

//import com.sun.media.Log;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Vector;
import javax.media.*;
import javax.media.format.UnsupportedFormatException;
import javax.media.protocol.DataSource;
import javax.media.rtp.rtcp.SourceDescription;

// Referenced classes of package javax.media.rtp:
//            InvalidSessionAddressException, ReceiveStreamListener, RemoteListener, SendStreamListener, 
//            SessionListener, SessionAddress, SendStream, GlobalReceptionStats, 
//            GlobalTransmissionStats, LocalParticipant, EncryptionInfo, RTPConnector

public abstract class RTPManager
    implements Controls
{

    public RTPManager()
    {
    }

    public abstract void addFormat(Format format, int i);

    public abstract void addReceiveStreamListener(ReceiveStreamListener receivestreamlistener);

    public abstract void addRemoteListener(RemoteListener remotelistener);

    public abstract void addSendStreamListener(SendStreamListener sendstreamlistener);

    public abstract void addSessionListener(SessionListener sessionlistener);

    public abstract void removeTarget(SessionAddress sessionaddress, String s)
        throws InvalidSessionAddressException;

    public abstract void removeTargets(String s);

    public abstract SendStream createSendStream(DataSource datasource, int i)
        throws UnsupportedFormatException, IOException;

    public abstract void dispose();

    public abstract Vector getActiveParticipants();

    public abstract Vector getAllParticipants();

    public abstract GlobalReceptionStats getGlobalReceptionStats();

    public abstract GlobalTransmissionStats getGlobalTransmissionStats();

    public abstract LocalParticipant getLocalParticipant();

    public abstract Vector getPassiveParticipants();

    public abstract Vector getReceiveStreams();

    public abstract Vector getRemoteParticipants();

    public abstract Vector getSendStreams();

    public abstract void initialize(SessionAddress sessionaddress)
        throws InvalidSessionAddressException, IOException;

    public abstract void initialize(SessionAddress asessionaddress[], SourceDescription asourcedescription[], double d, double d1, EncryptionInfo encryptioninfo)
        throws InvalidSessionAddressException, IOException;

    public abstract void initialize(RTPConnector rtpconnector);

    public abstract void addTarget(SessionAddress sessionaddress)
        throws InvalidSessionAddressException, IOException;

    public abstract void removeReceiveStreamListener(ReceiveStreamListener receivestreamlistener);

    public abstract void removeRemoteListener(RemoteListener remotelistener);

    public abstract void removeSendStreamListener(SendStreamListener sendstreamlistener);

    public abstract void removeSessionListener(SessionListener sessionlistener);

    public static RTPManager newInstance()
    {
        RTPManager rtpManager = null;
        for(Enumeration SessionList = getRTPManagerList().elements(); SessionList.hasMoreElements();)
        {
            String protoClassName = (String)SessionList.nextElement();
            try
            {
                Class protoClass = getClassForName(protoClassName);
                rtpManager = (RTPManager)protoClass.newInstance();
            }
            catch(ClassNotFoundException e) { }
            catch(InstantiationException e)
            {
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
            }
            catch(IllegalAccessException e)
            {
                System.out.println("illegal access.");
            }
            catch(Exception e)
            {
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
            }
            catch(Error e)
            {
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
            }
            if(rtpManager != null)
                break;
        }

        return rtpManager;
    }

    public static Vector getRTPManagerList()
    {
        String sourceName = "media.rtp.RTPSessionMgr";
        return buildClassList(getProtocolPrefixList(), sourceName);
    }

    static Class getClassForName(String className)
        throws ClassNotFoundException
    {
        try
        {
            return Class.forName(className);
        }
        catch(Exception e)
        {
            if(!checkIfJDK12())
                throw new ClassNotFoundException(e.getMessage());
        }
        catch(Error e)
        {
            if(!checkIfJDK12())
                throw e;
        }
        try
        {
            return (Class)forName3ArgsM.invoke(java.lang.Class.class, new Object[] {
                className, new Boolean(true), systemClassLoader
            });
        }
        catch(Throwable e) { }
        try
        {
            ClassLoader contextClassLoader = (ClassLoader)getContextClassLoaderM.invoke(Thread.currentThread(), null);
            return (Class)forName3ArgsM.invoke(java.lang.Class.class, new Object[] {
                className, new Boolean(true), contextClassLoader
            });
        }
        catch(Exception e)
        {
            throw new ClassNotFoundException(e.getMessage());
        }
        catch(Error e)
        {
            throw e;
        }
    }

    static Vector buildClassList(Vector prefixList, String name)
    {
        Vector classList = new Vector();
        classList.addElement(name);
        String prefixName;
        for(Enumeration prefix = prefixList.elements(); prefix.hasMoreElements(); classList.addElement(prefixName + "." + name))
            prefixName = (String)prefix.nextElement();

        return classList;
    }

    static Vector getProtocolPrefixList()
    {
        return (Vector)PackageManager.getProtocolPrefixList().clone();
    }

    private static boolean checkIfJDK12()
    {
        if(jdkInit)
            return forName3ArgsM != null;
        jdkInit = true;
        try
        {
            forName3ArgsM = (java.lang.Class.class).getMethod("forName", new Class[] {
                java.lang.String.class, Boolean.TYPE, java.lang.ClassLoader.class
            });
            getSystemClassLoaderM = (java.lang.ClassLoader.class).getMethod("getSystemClassLoader", null);
            systemClassLoader = (ClassLoader)getSystemClassLoaderM.invoke(java.lang.ClassLoader.class, null);
            getContextClassLoaderM = (java.lang.Thread.class).getMethod("getContextClassLoader", null);
            return true;
        }
        catch(Throwable t)
        {
            forName3ArgsM = null;
        }
        return false;
    }

    public abstract Object getControl(String s);

    public abstract Object[] getControls();

    private static boolean jdkInit = false;
    private static Method forName3ArgsM;
    private static Method getSystemClassLoaderM;
    private static ClassLoader systemClassLoader;
    private static Method getContextClassLoaderM;

}
