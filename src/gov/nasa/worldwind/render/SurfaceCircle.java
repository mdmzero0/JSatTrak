/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author tag
 * @version $Id: SurfaceCircle.java 2570 2007-08-16 22:31:33Z tgaskins $
 */
public class SurfaceCircle extends SurfaceEllipse
{
    public SurfaceCircle(Globe globe, LatLon center, double radius, int intervals)
    {
        super(globe, center, radius, radius, null, intervals, null, null);
    }

    public SurfaceCircle(Globe globe, LatLon center, double radius, int intervals, Color interiorColor,
        Color borderColor)
    {
        super(globe, center, radius, radius, null, intervals, interiorColor, borderColor);
    }

    public SurfaceCircle(Globe globe, LatLon center, double radius, int intervals, Color interiorColor,
        Color borderColor, Dimension textureSize)
    {
        super(globe, center, radius, radius, null, intervals, interiorColor, borderColor, textureSize);
    }

    public double getRadius()
    {
        return super.getMajorAxisLength();
    }

    public void setRadius(double radius)
    {
        if (radius <= 0)
        {
            String message = Logging.getMessage("Geom.RadiusInvalid", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.setAxisLengths(radius, radius);
    }
}
