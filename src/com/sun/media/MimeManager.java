// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MimeManager.java

package com.sun.media;

import com.sun.media.util.Registry;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Hashtable;

// Referenced classes of package com.sun.media:
//            MimeTable

public final class MimeManager
{

    private MimeManager()
    {
    }

    public static final boolean addMimeType(String fileExtension, String mimeType)
    {
        if(additionalMimeTable == null)
            additionalMimeTable = new Hashtable();
        if(mimeTable.doPut(fileExtension, mimeType))
        {
            additionalMimeTable.put(fileExtension, mimeType);
            return true;
        } else
        {
            return false;
        }
    }

    public static final boolean removeMimeType(String fileExtension)
    {
        if(mimeTable.doRemove(fileExtension))
        {
            if(additionalMimeTable != null)
                additionalMimeTable.remove(fileExtension);
            return true;
        } else
        {
            return false;
        }
    }

    public static final String getMimeType(String fileExtension)
    {
        return (String)mimeTable.get(fileExtension);
    }

    public static final Hashtable getMimeTable()
    {
        return mimeTable;
    }

    public static final Hashtable getDefaultMimeTable()
    {
        return defaultMimeTable;
    }

    public static final String getDefaultExtension(String mimeType)
    {
        return (String)extTable.get(mimeType);
    }

    public static void commit()
    {
        Registry.set("additionalMimeTable", additionalMimeTable);
        try
        {
            Registry.commit();
        }
        catch(IOException e)
        {
            System.err.println("IOException on commit " + e.getMessage());
        }
    }

    private static Hashtable additionalMimeTable;
    protected static final Hashtable defaultHashTable;
    private static MimeTable mimeTable;
    private static MimeTable defaultMimeTable;
    protected static final Hashtable extTable = new Hashtable();

    static 
    {
        additionalMimeTable = null;
        defaultHashTable = new Hashtable();
        mimeTable = new MimeTable();
        mimeTable.doPut("mov", "video/quicktime");
        defaultHashTable.put("mov", "video/quicktime");
        mimeTable.doPut("avi", "video/x_msvideo");
        defaultHashTable.put("avi", "video/x_msvideo");
        mimeTable.doPut("mpg", "video/mpeg");
        defaultHashTable.put("mpg", "video/mpeg");
        mimeTable.doPut("mpv", "video/mpeg");
        defaultHashTable.put("mpv", "video/mpeg");
        mimeTable.doPut("viv", "video/vivo");
        defaultHashTable.put("viv", "video/vivo");
        mimeTable.doPut("au", "audio/basic");
        defaultHashTable.put("au", "audio/basic");
        mimeTable.doPut("wav", "audio/x_wav");
        defaultHashTable.put("wav", "audio/x_wav");
        mimeTable.doPut("aiff", "audio/x_aiff");
        defaultHashTable.put("aiff", "audio/x_aiff");
        mimeTable.doPut("aif", "audio/x_aiff");
        defaultHashTable.put("aif", "audio/x_aiff");
        mimeTable.doPut("mid", "audio/midi");
        defaultHashTable.put("mid", "audio/midi");
        mimeTable.doPut("midi", "audio/midi");
        defaultHashTable.put("midi", "audio/midi");
        mimeTable.doPut("rmf", "audio/rmf");
        defaultHashTable.put("rmf", "audio/rmf");
        mimeTable.doPut("gsm", "audio/x_gsm");
        defaultHashTable.put("gsm", "audio/x_gsm");
        mimeTable.doPut("mp2", "audio/mpeg");
        defaultHashTable.put("mp2", "audio/mpeg");
        mimeTable.doPut("mp3", "audio/mpeg");
        defaultHashTable.put("mp3", "audio/mpeg");
        mimeTable.doPut("mpa", "audio/mpeg");
        defaultHashTable.put("mpa", "audio/mpeg");
        mimeTable.doPut("g728", "audio/g728");
        defaultHashTable.put("g728", "audio/g728");
        mimeTable.doPut("g729", "audio/g729");
        defaultHashTable.put("g729", "audio/g729");
        mimeTable.doPut("g729a", "audio/g729a");
        defaultHashTable.put("g729a", "audio/g729a");
        mimeTable.doPut("cda", "audio/cdaudio");
        defaultHashTable.put("cda", "audio/cdaudio");
        mimeTable.doPut("mvr", "application/mvr");
        defaultHashTable.put("mvr", "application/mvr");
        mimeTable.doPut("swf", "application/x-shockwave-flash");
        defaultHashTable.put("swf", "application/x-shockwave-flash");
        mimeTable.doPut("spl", "application/futuresplash");
        defaultHashTable.put("spl", "application/futuresplash");
        mimeTable.doPut("jmx", "application/x_jmx");
        defaultHashTable.put("jmx", "application/x_jmx");
        Object t = Registry.get("additionalMimeTable");
        if(t != null && (t instanceof Hashtable))
            additionalMimeTable = (Hashtable)t;
        if(additionalMimeTable != null && !additionalMimeTable.isEmpty())
        {
            for(Enumeration e = additionalMimeTable.keys(); e.hasMoreElements();)
            {
                String ext = (String)e.nextElement();
                if(defaultHashTable.containsKey(ext))
                    additionalMimeTable.remove(ext);
                else
                    mimeTable.doPut(ext, (String)additionalMimeTable.get(ext));
            }

        }
        defaultMimeTable = (MimeTable)mimeTable.clone();
    }
}
