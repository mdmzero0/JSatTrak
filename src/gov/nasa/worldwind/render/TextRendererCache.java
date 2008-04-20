/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.util.Logging;
import com.sun.opengl.util.j2d.TextRenderer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * @author tag
 * @version $Id: TextRendererCache.java 3491 2007-11-13 07:04:30Z tgaskins $
 */
public class TextRendererCache implements Disposable
{
    private ConcurrentHashMap<Object, TextRenderer> renderers = new ConcurrentHashMap<Object, TextRenderer>();

    public void dispose()
    {
        for (Map.Entry<Object, TextRenderer> e : this.renderers.entrySet())
            if (e.getValue() != null)
                e.getValue().dispose();

        this.renderers.clear();
    }

    public void add(Object key, TextRenderer textRenderer)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.renderers.put(key, textRenderer);
    }

    public void remove(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.renderers.remove(key);
    }

    public TextRenderer get(Object key)
    {
        if (key == null)
        {
            String message = Logging.getMessage("nullValue.KeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.renderers.get(key);
    }
}
