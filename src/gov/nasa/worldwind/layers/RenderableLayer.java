/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: RenderableLayer.java 3044 2007-09-26 02:08:22Z tgaskins $
 */
public class RenderableLayer extends AbstractLayer
{
    private Collection<Renderable> renderables = new ArrayList<Renderable>();
    private final PickSupport pickSupport = new PickSupport();
    private final Layer delegateOwner;

    public RenderableLayer()
    {
        this.delegateOwner = null;
    }

    public RenderableLayer(Layer delegateOwner)
    {
        this.delegateOwner = delegateOwner;
    }

    public void addRenderables(Iterable<Renderable> shapeIterator)
    {
        this.renderables = new ArrayList<Renderable>();

        if (shapeIterator == null)
            return;

        for (Renderable renderable : shapeIterator)
        {
            this.renderables.add(renderable);
        }
    }

    public void addRenderable(Renderable renderable)
    {
        if (renderable == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.renderables.add(renderable);
    }

    private void clearRenderables()
    {
        if (this.renderables != null && this.renderables.size() > 0)
        {
            for (Renderable renderable : this.renderables)
            {
                if (renderable instanceof Disposable)
                    ((Disposable) renderable).dispose();
            }
            this.renderables.clear();
        }
    }

    public void setRenderables(Iterable<Renderable> shapeIterator)
    {
        this.renderables = new ArrayList<Renderable>();

        this.clearRenderables();
        if (shapeIterator == null)
            this.addRenderables(shapeIterator);
    }

    public void setRenderable(final Renderable renderable)
    {
        this.clearRenderables();
        if (renderable != null)
            this.addRenderable(renderable);
    }

    public void removeRenderable(Renderable renderable)
    {
        if (renderable == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.renderables.remove(renderable);
    }

    public Collection<Renderable> getRenderables()
    {
        return this.renderables;
    }

    public void dispose()
    {
        for (Renderable renderable : this.renderables)
        {
            if (renderable instanceof Disposable)
                ((Disposable) renderable).dispose();
        }
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        this.pickSupport.clearPickList();
        this.pickSupport.beginPicking(dc);

        for (Renderable renderable : this.renderables)
        {
            float[] inColor = new float[4];
            dc.getGL().glGetFloatv(GL.GL_CURRENT_COLOR, inColor, 0);
            Color color = dc.getUniquePickColor();
            dc.getGL().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

            renderable.render(dc);

            dc.getGL().glColor4fv(inColor, 0);

            if (renderable instanceof Locatable)
            {
                this.pickSupport.addPickableObject(color.getRGB(), renderable,
                    ((Locatable) renderable).getPosition(), false);
            }
            else
            {
                this.pickSupport.addPickableObject(color.getRGB(), renderable);
            }
        }

        this.pickSupport.resolvePick(dc, pickPoint, this.delegateOwner != null ? this.delegateOwner : this);
        this.pickSupport.endPicking(dc);
    }

    @Override
    protected void doRender(DrawContext dc)
    {
        for (Renderable renderable : this.renderables)
        {
            renderable.render(dc);
        }
    }

    public Layer getDelegateOwner()
    {
        return delegateOwner;
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.RenderableLayer.Name");
    }
}
