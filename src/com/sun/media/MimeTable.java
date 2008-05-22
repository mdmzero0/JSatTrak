// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MimeManager.java

package com.sun.media;

import java.io.PrintStream;
import java.util.Hashtable;

// Referenced classes of package com.sun.media:
//            MimeManager

final class MimeTable extends Hashtable
{

    MimeTable()
    {
    }

    public final synchronized void clear()
    {
    }

    public final synchronized Object put(Object key, Object value)
    {
        return null;
    }

    public final synchronized Object remove(Object key)
    {
        return null;
    }

    protected final synchronized boolean doPut(String key, String value)
    {
        if(!MimeManager.defaultHashTable.containsKey(key))
        {
            super.put(key, value);
            if(MimeManager.extTable.get(value) == null)
                MimeManager.extTable.put(value, key);
            return true;
        } else
        {
            System.err.println("Cannot override default mime-table entries");
            return false;
        }
    }

    protected final synchronized boolean doRemove(String key)
    {
        if(!MimeManager.defaultHashTable.containsKey(key))
        {
            if(get(key) != null)
                MimeManager.extTable.remove(get(key));
            return super.remove(key) != null;
        } else
        {
            return false;
        }
    }
}
