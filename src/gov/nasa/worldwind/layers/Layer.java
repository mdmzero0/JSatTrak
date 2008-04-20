/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.render.DrawContext;

/**
 * @author Tom Gaskins
 * @version $Id: Layer.java 3578 2007-11-19 18:32:03Z tgaskins $
 */
public interface Layer extends WWObject, Disposable
{
    public boolean isEnabled();

    public void setEnabled(boolean enabled);

    String getName();

    void setName(String name);

    double getOpacity();

    void setOpacity(double opacity);

    boolean isPickEnabled();

    void setPickEnabled(boolean isPickable);

    public void render(DrawContext dc);

    public void pick(DrawContext dc, java.awt.Point pickPoint);

    boolean isAtMaxResolution();

    boolean isMultiResolution();
}
