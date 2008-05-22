// Decompiled by DJ v3.10.10.93 Copyright 2007 Atanas Neshkov  Date: 5/21/2008 10:01:44 PM
// Home Page: http://members.fortunecity.com/neshkov/dj.html  http://www.neshkov.com/dj.html - Check often for new version!
// Decompiler options: packimports(3) 
// Source File Name:   ButtonPositionEditor.java

package com.ibm.media.bean.multiplayer;

import java.beans.PropertyEditorSupport;

// Referenced classes of package com.ibm.media.bean.multiplayer:
//            JMFUtil

public class ButtonPositionEditor extends PropertyEditorSupport
{

    public ButtonPositionEditor()
    {
    }

    public String getJavaInitializationString()
    {
        String result = null;
        String propertyValue = getAsText();
        for(int i = 0; i < positionArr.length; i++)
            if(propertyValue.equals(positionArr[i]))
                return "new java.lang.String(\"" + positionArr[i] + "\")";

        return null;
    }

    public String[] getTags()
    {
        return positionArr;
    }

    String positionArr[] = {
        JMFUtil.getString("NORTH"), JMFUtil.getString("SOUTH"), JMFUtil.getString("EAST"), JMFUtil.getString("WEST"), JMFUtil.getString("NONE")
    };
}