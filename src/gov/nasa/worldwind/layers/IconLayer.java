/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author tag
 * @version $Id$
 */
public class IconLayer extends AbstractLayer
{
    private final java.util.Collection<WWIcon> icons = new ConcurrentLinkedQueue<WWIcon>();
    private IconRenderer iconRenderer = new IconRenderer();
    private Pedestal pedestal;

    public IconLayer()
    {
    }

    public void addIcon(WWIcon icon)
    {
        if (icon == null)
        {
            String msg = Logging.getMessage("nullValue.Icon");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.icons.add(icon);
    }

    public void removeIcon(WWIcon icon)
    {
        if (icon == null)
        {
            String msg = Logging.getMessage("nullValue.Icon");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.icons.remove(icon);
    }

    public java.util.Collection<WWIcon> getIcons()
    {
        return this.icons;
    }

    public Pedestal getPedestal()
    {
        return pedestal;
    }

    public void setPedestal(Pedestal pedestal)
    {
        this.pedestal = pedestal;
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        this.iconRenderer.setPedestal(this.pedestal);
        this.iconRenderer.pick(dc, this.icons, pickPoint, this);
    }

    @Override
    protected void doRender(DrawContext dc)
    {
        this.iconRenderer.setPedestal(this.pedestal);
        this.iconRenderer.render(dc, this.icons);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.IconLayer.Name");
    }
}
