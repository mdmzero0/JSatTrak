// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Resource.java

package com.sun.media.util;

import com.ms.security.PermissionID;
import com.ms.security.PolicyEngine;
import com.sun.media.JMFSecurity;
import com.sun.media.JMFSecurityManager;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media.util:
//            FormatTable, jdk12ReadFileAction, jdk12, jdk12PropertyAction

public class Resource
{

    public Resource()
    {
    }

    public static final synchronized void reset()
    {
        hash = new Hashtable();
    }

    public static final synchronized boolean set(String key, Object value)
    {
        if(key != null && value != null)
        {
            if(jmfSecurity != null && key.indexOf("secure.") == 0)
            {
                return false;
            } else
            {
                hash.put(key, value);
                return true;
            }
        } else
        {
            return false;
        }
    }

    public static final synchronized Object get(String key)
    {
        if(key != null)
            return hash.get(key);
        else
            return null;
    }

    public static final synchronized boolean remove(String key)
    {
        if(key != null && hash.containsKey(key))
        {
            hash.remove(key);
            return true;
        } else
        {
            return false;
        }
    }

    public static final synchronized void removeGroup(String keyStart)
    {
        Vector keys = new Vector();
        if(keyStart != null)
        {
            for(Enumeration e = hash.keys(); e.hasMoreElements();)
            {
                String key = (String)e.nextElement();
                if(key.startsWith(keyStart))
                    keys.addElement(key);
            }

        }
        for(int i = 0; i < keys.size(); i++)
            hash.remove(keys.elementAt(i));

    }

    public static final synchronized boolean commit()
        throws IOException
    {
        if(filename == null)
            throw new IOException("Can't find resource file");
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        int tableSize = hash.size();
        oos.writeInt(tableSize);
        oos.writeInt(200);
        for(Enumeration e = hash.keys(); e.hasMoreElements(); oos.flush())
        {
            String key = (String)e.nextElement();
            Object value = hash.get(key);
            oos.writeUTF(key);
            oos.writeObject(value);
        }

        oos.close();
        return true;
    }

    public static final synchronized void destroy()
    {
        if(filename == null)
            return;
        try
        {
            File file = new File(filename);
            file.delete();
        }
        catch(Throwable t)
        {
            filename = null;
        }
    }

    private static final synchronized InputStream findResourceFile()
    {
        String strJMF = ".jmf-resource";
        File file = null;
        InputStream ris = null;
        if(userhome == null)
            return null;
        try
        {
            filename = userhome + File.separator + strJMF;
            file = new File(filename);
            ris = getResourceStream(file);
        }
        catch(Throwable t)
        {
            filename = null;
            return null;
        }
        return ris;
    }

    private static final FileInputStream getResourceStream(File file)
        throws IOException
    {
        try
        {
            if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            {
                Constructor cons = jdk12ReadFileAction.cons;
                return (FileInputStream)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        file.getPath()
                    })
                });
            }
            if(!file.exists())
                return null;
            else
                return new FileInputStream(file.getPath());
        }
        catch(Throwable t)
        {
            return null;
        }
    }

    private static final synchronized boolean readResource(InputStream ris)
    {
        if(ris == null)
            return false;
        try
        {
            ObjectInputStream ois = new ObjectInputStream(ris);
            int tableSize = ois.readInt();
            int version = ois.readInt();
            if(version > 200)
                System.err.println("Version number mismatch.\nThere could be errors in reading the resource");
            hash = new Hashtable();
            for(int i = 0; i < tableSize; i++)
            {
                String key = ois.readUTF();
                boolean failed = false;
                try
                {
                    Object value = ois.readObject();
                    hash.put(key, value);
                }
                catch(ClassNotFoundException cnfe)
                {
                    failed = true;
                }
                catch(OptionalDataException ode)
                {
                    failed = true;
                }
            }

            ois.close();
            ris.close();
        }
        catch(IOException ioe)
        {
            System.err.println("IOException in readResource: " + ioe);
            return false;
        }
        catch(Throwable t)
        {
            return false;
        }
        return true;
    }

    static final void initDB()
    {
        synchronized(fmtTblSync)
        {
            audioFmtTbl = new FormatTable(AUDIO_TBL_SIZE);
            videoFmtTbl = new FormatTable(VIDEO_TBL_SIZE);
            miscFmtTbl = new FormatTable(MISC_TBL_SIZE);
            loadDB();
        }
    }

    public static final void purgeDB()
    {
        synchronized(fmtTblSync)
        {
            if(audioFmtTbl == null)
                return;
            audioFmtTbl = new FormatTable(AUDIO_TBL_SIZE);
            videoFmtTbl = new FormatTable(VIDEO_TBL_SIZE);
            miscFmtTbl = new FormatTable(MISC_TBL_SIZE);
        }
    }

    public static final Format[] getDB(Format input)
    {
        Format aformat2[];
        synchronized(fmtTblSync)
        {
            if(audioFmtTbl == null)
                initDB();
            if(input instanceof AudioFormat)
            {
                Format aformat[] = audioFmtTbl.get(input);
                return aformat;
            }
            if(input instanceof VideoFormat)
            {
                Format aformat1[] = videoFmtTbl.get(input);
                return aformat1;
            }
            aformat2 = miscFmtTbl.get(input);
        }
        return aformat2;
    }

    public static final Format[] putDB(Format input, Format supported[])
    {
        Format aformat[];
        synchronized(fmtTblSync)
        {
            Format in = input.relax();
            Format list[] = new Format[supported.length];
            for(int i = 0; i < supported.length; i++)
                list[i] = supported[i].relax();

            if(in instanceof AudioFormat)
                audioFmtTbl.save(in, list);
            else
            if(in instanceof VideoFormat)
                videoFmtTbl.save(in, list);
            else
                miscFmtTbl.save(in, list);
            needSaving = true;
            aformat = list;
        }
        return aformat;
    }

    private static final void loadDB()
    {
        synchronized(fmtTblSync)
        {
            Object key = get(AUDIO_SIZE_KEY);
            int size;
            if(key instanceof Integer)
                size = ((Integer)key).intValue();
            else
                size = 0;
            if(size > AUDIO_TBL_SIZE)
            {
                System.err.println("Resource file is corrupted");
                size = AUDIO_TBL_SIZE;
            }
            audioFmtTbl.last = size;
            for(int i = 0; i < size; i++)
            {
                key = get(AUDIO_INPUT_KEY + i);
                Object value = get(AUDIO_FORMAT_KEY + i);
                Object hit = get(AUDIO_HIT_KEY + i);
                if((key instanceof Format) && (value instanceof Format[]) && (hit instanceof Integer))
                {
                    audioFmtTbl.keys[i] = (Format)key;
                    audioFmtTbl.table[i] = (Format[])value;
                    audioFmtTbl.hits[i] = ((Integer)hit).intValue();
                    continue;
                }
                System.err.println("Resource file is corrupted");
                audioFmtTbl.last = 0;
                break;
            }

            key = get(VIDEO_SIZE_KEY);
            if(key instanceof Integer)
                size = ((Integer)key).intValue();
            else
                size = 0;
            if(size > VIDEO_TBL_SIZE)
            {
                System.err.println("Resource file is corrupted");
                size = VIDEO_TBL_SIZE;
            }
            videoFmtTbl.last = size;
            for(int i = 0; i < size; i++)
            {
                key = get(VIDEO_INPUT_KEY + i);
                Object value = get(VIDEO_FORMAT_KEY + i);
                Object hit = get(VIDEO_HIT_KEY + i);
                if((key instanceof Format) && (value instanceof Format[]) && (hit instanceof Integer))
                {
                    videoFmtTbl.keys[i] = (Format)key;
                    videoFmtTbl.table[i] = (Format[])value;
                    videoFmtTbl.hits[i] = ((Integer)hit).intValue();
                    continue;
                }
                System.err.println("Resource file is corrupted");
                videoFmtTbl.last = 0;
                break;
            }

            key = get(MISC_SIZE_KEY);
            if(key instanceof Integer)
                size = ((Integer)key).intValue();
            else
                size = 0;
            if(size > MISC_TBL_SIZE)
            {
                System.err.println("Resource file is corrupted");
                size = MISC_TBL_SIZE;
            }
            miscFmtTbl.last = size;
            for(int i = 0; i < size; i++)
            {
                key = get(MISC_INPUT_KEY + i);
                Object value = get(MISC_FORMAT_KEY + i);
                Object hit = get(MISC_HIT_KEY + i);
                if((key instanceof Format) && (value instanceof Format[]) && (hit instanceof Integer))
                {
                    miscFmtTbl.keys[i] = (Format)key;
                    miscFmtTbl.table[i] = (Format[])value;
                    miscFmtTbl.hits[i] = ((Integer)hit).intValue();
                    continue;
                }
                System.err.println("Resource file is corrupted");
                miscFmtTbl.last = 0;
                break;
            }

        }
    }

    public static final void saveDB()
    {
        synchronized(fmtTblSync)
        {
            if(!needSaving)
                return;
            reset();
            set(AUDIO_SIZE_KEY, new Integer(audioFmtTbl.last));
            for(int i = 0; i < audioFmtTbl.last; i++)
            {
                set(AUDIO_INPUT_KEY + i, audioFmtTbl.keys[i]);
                set(AUDIO_FORMAT_KEY + i, audioFmtTbl.table[i]);
                set(AUDIO_HIT_KEY + i, new Integer(audioFmtTbl.hits[i]));
            }

            set(VIDEO_SIZE_KEY, new Integer(videoFmtTbl.last));
            for(int i = 0; i < videoFmtTbl.last; i++)
            {
                set(VIDEO_INPUT_KEY + i, videoFmtTbl.keys[i]);
                set(VIDEO_FORMAT_KEY + i, videoFmtTbl.table[i]);
                set(VIDEO_HIT_KEY + i, new Integer(videoFmtTbl.hits[i]));
            }

            set(MISC_SIZE_KEY, new Integer(miscFmtTbl.last));
            for(int i = 0; i < miscFmtTbl.last; i++)
            {
                set(MISC_INPUT_KEY + i, miscFmtTbl.keys[i]);
                set(MISC_FORMAT_KEY + i, miscFmtTbl.table[i]);
                set(MISC_HIT_KEY + i, new Integer(miscFmtTbl.hits[i]));
            }

            try
            {
                commit();
            }
            catch(Throwable e) { }
            needSaving = false;
        }
    }

    private static Hashtable hash = null;
    private static String filename = null;
    private static final int versionNumber = 200;
    private static boolean securityPrivelege;
    private static JMFSecurity jmfSecurity;
    private static Method m[];
    private static Class cl[];
    private static Object args[][];
    private static final String USERHOME = "user.home";
    private static String userhome;
    static FormatTable audioFmtTbl;
    static FormatTable videoFmtTbl;
    static FormatTable miscFmtTbl;
    static Object fmtTblSync = new Object();
    static int AUDIO_TBL_SIZE = 40;
    static int VIDEO_TBL_SIZE = 20;
    static int MISC_TBL_SIZE = 10;
    static String AUDIO_SIZE_KEY = "ATS";
    static String AUDIO_INPUT_KEY = "AI.";
    static String AUDIO_FORMAT_KEY = "AF.";
    static String AUDIO_HIT_KEY = "AH.";
    static String VIDEO_SIZE_KEY = "VTS";
    static String VIDEO_INPUT_KEY = "VI.";
    static String VIDEO_FORMAT_KEY = "VF.";
    static String VIDEO_HIT_KEY = "VH.";
    static String MISC_SIZE_KEY = "MTS";
    static String MISC_INPUT_KEY = "MI.";
    static String MISC_FORMAT_KEY = "MF.";
    static String MISC_HIT_KEY = "MH.";
    static boolean needSaving = false;

    static 
    {
        securityPrivelege = false;
        jmfSecurity = null;
        m = new Method[1];
        cl = new Class[1];
        args = new Object[1][0];
        userhome = null;
        hash = new Hashtable();
        try
        {
            jmfSecurity = JMFSecurityManager.getJMFSecurity();
            securityPrivelege = true;
        }
        catch(SecurityException e) { }
        if(jmfSecurity != null)
        {
            String permission = null;
            try
            {
                if(jmfSecurity.getName().startsWith("jmf-security"))
                {
                    permission = "read property";
                    jmfSecurity.requestPermission(m, cl, args, 1);
                    m[0].invoke(cl[0], args[0]);
                    permission = "read file";
                    jmfSecurity.requestPermission(m, cl, args, 2);
                    m[0].invoke(cl[0], args[0]);
                } else
                if(jmfSecurity.getName().startsWith("internet"))
                {
                    PolicyEngine.checkPermission(PermissionID.PROPERTY);
                    PolicyEngine.checkPermission(PermissionID.FILEIO);
                    PolicyEngine.assertPermission(PermissionID.PROPERTY);
                    PolicyEngine.assertPermission(PermissionID.FILEIO);
                }
            }
            catch(Throwable e)
            {
                securityPrivelege = false;
            }
        }
        if(jmfSecurity != null && jmfSecurity.getName().startsWith("jdk12"))
            try
            {
                Constructor cons = jdk12PropertyAction.cons;
                userhome = (String)jdk12.doPrivM.invoke(jdk12.ac, new Object[] {
                    cons.newInstance(new Object[] {
                        "user.home"
                    })
                });
            }
            catch(Throwable e)
            {
                securityPrivelege = false;
            }
        else
            try
            {
                if(securityPrivelege)
                    userhome = System.getProperty("user.home");
            }
            catch(Exception e)
            {
                userhome = null;
                securityPrivelege = false;
            }
        if(userhome == null)
            securityPrivelege = false;
        InputStream is = null;
        if(securityPrivelege)
        {
            is = findResourceFile();
            if(is == null)
                securityPrivelege = false;
        }
        if(!readResource(is))
            hash = new Hashtable();
    }
}
