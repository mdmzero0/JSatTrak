/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;

import java.awt.*;

/**
 * @author Tom Gaskins
 * @version $Id: SectorGeometry.java 3632 2007-11-28 03:28:17Z tgaskins $
 */
public interface SectorGeometry extends Renderable, Pickable
{
    public Extent getExtent();

    public Sector getSector();

    public Vec4 getSurfacePoint(Angle latitude, Angle longitude, double metersOffset);

    void renderMultiTexture(DrawContext dc, int numTextureUnits);

    public void renderWireframe(DrawContext dc, boolean interior, boolean exterior);

    void renderBoundingVolume(DrawContext dc);

    PickedObject[] pick(DrawContext dc, java.util.List<Point> pickPoints);
}
