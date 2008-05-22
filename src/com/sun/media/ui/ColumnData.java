// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ColumnList.java

package com.sun.media.ui;


class ColumnData
{

    public ColumnData(String strName, int nType)
    {
        nWidth = 120;
        this.strName = strName;
        this.nType = nType;
    }

    public String toString()
    {
        return strName;
    }

    String strName;
    int nType;
    int nWidth;
}
