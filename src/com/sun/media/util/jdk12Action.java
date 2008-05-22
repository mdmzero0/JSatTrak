// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12Action.java

package com.sun.media.util;

import java.lang.reflect.Constructor;

public class jdk12Action
{

    public jdk12Action()
    {
    }

    public static Constructor getCheckPermissionAction()
        throws NoSuchMethodException
    {
        return (com.sun.media.util.CheckPermissionAction.class).getConstructor(new Class[] {
            java.security.Permission.class
        });
    }
}
