/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   SourceDescription.java

package javax.media.rtp.rtcp;


import jpg2movie.media.util.jdk12InetAddressAction;
import jpg2movie.media.util.jdk12PropertyAction;
import jpg2movie.media.util.jdk12;
import jpg2movie.media.JMFSecurity;
import jpg2movie.media.JMFSecurityManager;
import jpg2movie.media.util.*;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;

public class SourceDescription
    implements Serializable
{

    public SourceDescription(int type, String description, int frequency, boolean encrypted)
    {
        m_description = null;
        m_encrypted = false;
        m_type = type;
        m_description = description;
        m_frequency = frequency;
        m_encrypted = encrypted;
    }

    public int getType()
    {
        return m_type;
    }

    public String getDescription()
    {
        return m_description;
    }

    public void setDescription(String desc)
    {
        m_description = desc;
    }

    public int getFrequency()
    {
        return m_frequency;
    }

    public boolean getEncrypted()
    {
        return m_encrypted;
    }

    public static String generateCNAME()
    {
        String hostname = null;
        String cname = "";
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "connect";
                    jmfSecurity.requestPermission(m, cl, args, 128);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    //
                    //
                }
            }
            catch(Throwable e)
            {
                jmfSecurity.permissionFailureNotification(128);
            }
        }
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12InetAddressAction.cons;
                InetAddress host = (InetAddress)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        null, "getLocalHost", null
                    })
                });
                hostname = (String)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        host, "getHostName", null
                    })
                });
            } else
            {
                InetAddress host = InetAddress.getLocalHost();
                hostname = host.getHostName();
            }
        }
        catch(Throwable e)
        {
            System.err.println("InitSession : UnknownHostExcpetion " + e.getMessage());
        }
        cname = getProperty("user.name");
        if(cname == null)
            return hostname;
        else
            return cname + "@" + hostname;
    }

    private static String getProperty(String prop)
    {
        String value = null;
        if(jmfSecurity != null)
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    jmfSecurity.requestPermission(m, cl, args, 1);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    //
                    //
                }
            }
            catch(Throwable e)
            {
                jmfSecurity.permissionFailureNotification(1);
            }
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12PropertyAction.cons;
                value = (String)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        prop
                    })
                });
            } else
            {
                value = System.getProperty(prop);
            }
        }
        catch(Throwable e) { }
        return value;
    }

    public static final int SOURCE_DESC_CNAME = 1;
    public static final int SOURCE_DESC_NAME = 2;
    public static final int SOURCE_DESC_EMAIL = 3;
    public static final int SOURCE_DESC_PHONE = 4;
    public static final int SOURCE_DESC_LOC = 5;
    public static final int SOURCE_DESC_TOOL = 6;
    public static final int SOURCE_DESC_NOTE = 7;
    public static final int SOURCE_DESC_PRIV = 8;
    private static JMFSecurity jmfSecurity = null;
    private static boolean securityPrivelege = false;
    private static Method m[] = new Method[1];
    private static Class cl[] = new Class[1];
    private static Object args[][] = new Object[1][0];
    private int m_type;
    private String m_description;
    private int m_frequency;
    private boolean m_encrypted;

    static 
    {
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
    }
}
