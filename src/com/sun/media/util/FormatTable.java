// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Resource.java

package com.sun.media.util;

import javax.media.Format;

class FormatTable
{

    public FormatTable(int size)
    {
        keys = new Format[size];
        table = new Format[size][];
        hits = new int[size];
        last = 0;
    }

    Format[] get(Format input)
    {
        Format res[] = null;
        for(int i = 0; i < last; i++)
            if(res == null && keys[i].matches(input))
            {
                res = table[i];
                hits[i] = keys.length;
            } else
            {
                hits[i] = hits[i] - 1;
            }

        return res;
    }

    public void save(Format input, Format supported[])
    {
        int idx;
        if(last >= keys.length)
        {
            idx = findLeastHit();
        } else
        {
            idx = last;
            last++;
        }
        keys[idx] = input;
        table[idx] = supported;
        hits[idx] = keys.length;
    }

    public int findLeastHit()
    {
        int min = hits[0];
        int idx = 0;
        for(int i = 1; i < last; i++)
            if(hits[i] < min)
            {
                min = hits[i];
                idx = i;
            }

        return idx;
    }

    public Format keys[];
    public Format table[][];
    public int hits[];
    public int last;
}
