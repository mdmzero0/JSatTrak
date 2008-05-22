// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DynamicPlugIn.java

package com.sun.media.util;

import javax.media.Format;

public interface DynamicPlugIn
{

    public abstract Format[] getBaseInputFormats();

    public abstract Format[] getBaseOutputFormats();
}
