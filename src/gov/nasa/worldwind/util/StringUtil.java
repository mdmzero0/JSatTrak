/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
// @version $Id: StringUtil.java 2422 2007-07-25 23:07:49Z tgaskins $

package gov.nasa.worldwind.util;

public class StringUtil
{
    public static final String EMPTY = "";

    public static boolean Equals(String s1, String s2)
    {
        if(null == s1 && null == s2)
            return true;
        if(null == s1 || null == s2)
            return false;
        return s1.equals(s2);
    }
}
