/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;

/**
 * @author tag
 * @version $Id: SurfaceTile.java 2812 2007-09-12 19:16:41Z tgaskins $
 */
public interface SurfaceTile
{
    boolean bind(DrawContext dc);
    void applyInternalTransform(DrawContext dc);
    Sector getSector();
    Extent getExtent(DrawContext dc);

}
