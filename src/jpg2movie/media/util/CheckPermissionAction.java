// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   jdk12Action.java

package jpg2movie.media.util;

import java.security.*;

class CheckPermissionAction
    implements PrivilegedAction
{

    public CheckPermissionAction(Permission p)
    {
        permission = p;
    }

    public Object run()
    {
        AccessController.checkPermission(permission);
        return null;
    }

    private Permission permission;
}
