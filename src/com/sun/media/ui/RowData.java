// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ColumnList.java

package com.sun.media.ui;

import java.util.Vector;

class RowData
{

    public RowData(Object arrValues[])
    {
        vectorValues = new Vector();
        if(arrValues != null)
        {
            int nCount = arrValues.length;
            for(int i = 0; i < nCount; i++)
                vectorValues.addElement(arrValues[i]);

        }
    }

    void setValue(Object value, int nColumn)
    {
        vectorValues.setElementAt(value, nColumn);
    }

    Object getValue(int nColumn)
    {
        Object value = vectorValues.elementAt(nColumn);
        return value;
    }

    private Vector vectorValues;
}
