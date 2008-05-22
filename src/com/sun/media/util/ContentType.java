// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ContentType.java

package com.sun.media.util;

import com.sun.media.Log;
import com.sun.media.MimeManager;

public class ContentType
{

    public ContentType()
    {
    }

    public static String getCorrectedContentType(String contentType, String fileName)
    {
        if(contentType != null)
        {
            if(contentType.startsWith("text"))
            {
                int i = fileName.lastIndexOf(".");
                if(i != -1)
                {
                    String ext = fileName.substring(i + 1).toLowerCase();
                    String type = MimeManager.getMimeType(ext);
                    if(type != null)
                        return type;
                }
                Log.error("Warning: The URL may not exist. Please check URL");
                return contentType;
            }
            if(contentType.equals("audio/wav"))
                return "audio/x-wav";
            if(contentType.equals("audio/aiff"))
                return "audio/x-aiff";
            if(contentType.equals("application/x-troff-msvideo"))
                return "video/x-msvideo";
            if(contentType.equals("video/msvideo"))
                return "video/x-msvideo";
            if(contentType.equals("video/avi"))
                return "video/x-msvideo";
            if(contentType.equals("audio/x-mpegaudio"))
                return "audio/mpeg";
        }
        String type = null;
        int i = fileName.lastIndexOf(".");
        if(i != -1)
        {
            String ext = fileName.substring(i + 1).toLowerCase();
            type = MimeManager.getMimeType(ext);
        }
        if(type != null)
            return type;
        if(contentType != null)
            return contentType;
        else
            return "content/unknown";
    }
}
