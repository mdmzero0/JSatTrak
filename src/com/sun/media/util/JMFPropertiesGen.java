// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JMFPropertiesGen.java

package com.sun.media.util;


// Referenced classes of package com.sun.media.util:
//            RegistryGen, Registry

public class JMFPropertiesGen
{

    public JMFPropertiesGen()
    {
    }

    public static void main(String args[])
    {
        String nativeList[] = RegistryGen.nativePlugins;
        String defaultList[] = RegistryGen.defaultPlugins;
        boolean allJava = false;
        if(args.length > 0 && args[0].equalsIgnoreCase("java"))
            allJava = true;
        String mergedList[] = RegistryGen.findAllPlugInList(allJava, defaultList, nativeList);
        RegistryGen.registerPlugIns(mergedList);
        if(!allJava)
        {
            String fileSeparator = System.getProperty("file.separator");
            if(fileSeparator.equals("/"))
                Registry.set("secure.cacheDir", "/tmp");
            else
                Registry.set("secure.cacheDir", "C:" + fileSeparator + "temp");
            try
            {
                Registry.commit();
            }
            catch(Exception e) { }
        }
        System.exit(0);
    }
}
