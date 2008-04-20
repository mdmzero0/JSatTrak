/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.pick;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.util.HashMap;

/**
 * @author tag
 * @version $Id: PickSupport.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class PickSupport
{
    private HashMap<Integer, PickedObject> pickableObjects = new HashMap<Integer, PickedObject>();

    public void clearPickList()
    {
        this.pickableObjects.clear();
    }

    public void addPickableObject(int colorCode, Object o, Position position, boolean isTerrain)
    {
        this.pickableObjects.put(colorCode, new PickedObject(colorCode, o, position, isTerrain));
    }

    public void addPickableObject(int colorCode, Object o)
    {
        this.pickableObjects.put(colorCode, new PickedObject(colorCode, o));
    }

    public void addPickableObject(int colorCode, PickedObject po)
    {
        this.pickableObjects.put(colorCode, po);
    }

    public PickedObject getTopObject(DrawContext dc, java.awt.Point pickPoint, Layer layer)
    {
        if (this.pickableObjects.isEmpty())
            return null;

        int colorCode = this.getTopColor(dc, pickPoint);
        if (colorCode == dc.getClearColor().getRGB())
            return null;

        PickedObject pickedObject = pickableObjects.get(colorCode);
        if (pickedObject == null)
            return null;

        if (layer != null)
            pickedObject.setParentLayer(layer);

        return pickedObject;
    }

    public void resolvePick(DrawContext dc, java.awt.Point pickPoint, Layer layer)
    {
        PickedObject pickedObject = this.getTopObject(dc, pickPoint, layer);
        if (pickedObject != null)
            dc.addPickedObject(pickedObject);

        this.clearPickList();
    }

    public int getTopColor(DrawContext dc, java.awt.Point pickPoint)
    {
        GL gl = dc.getGL();

        int[] viewport = new int[4];
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);

        java.nio.ByteBuffer pixel = BufferUtil.newByteBuffer(3);
        gl.glReadPixels(pickPoint.x, viewport[3] - pickPoint.y, 1, 1,
            javax.media.opengl.GL.GL_RGB, GL.GL_UNSIGNED_BYTE, pixel);

        java.awt.Color topColor = null;
        try
        {
            topColor = new java.awt.Color(pixel.get(0) & 0xff, pixel.get(1) & 0xff, pixel.get(2) & 0xff, 0);
        }
        catch (Exception e)
        {
            Logging.logger().severe("layers.InvalidPickColorRead");
        }

        return topColor != null ? topColor.getRGB() : 0;
    }

    public void beginPicking(DrawContext dc)
    {
        javax.media.opengl.GL gl = dc.getGL();

        gl.glPushAttrib(GL.GL_ENABLE_BIT);

        gl.glDisable(GL.GL_DITHER);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_FOG);
        gl.glDisable(GL.GL_BLEND);
        gl.glDisable(GL.GL_TEXTURE_2D);
    }

    public void endPicking(DrawContext dc)
    {
        dc.getGL().glPopAttrib();
    }
}
