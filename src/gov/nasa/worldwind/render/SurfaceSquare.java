/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;

/**
 * @author tag
 * @version $Id: SurfaceSquare.java 2570 2007-08-16 22:31:33Z tgaskins $
 */
public class SurfaceSquare extends SurfaceQuad
{
    public SurfaceSquare(Globe globe, LatLon center, double width)
    {
        //noinspection SuspiciousNameCombination
        super(globe, center, width, width, null, null, null);
    }

    public SurfaceSquare(Globe globe, LatLon center, double width, Color interiorColor, Color borderColor)
    {
        //noinspection SuspiciousNameCombination
        super(globe, center, width, width, null, interiorColor, borderColor);
    }

    public SurfaceSquare(Globe globe, LatLon center, double width, Color interiorColor, Color borderColor,
        Dimension textureSize)
    {
        //noinspection SuspiciousNameCombination
        super(globe, center, width, width, null, interiorColor, borderColor, textureSize);
    }

    public double getWidth()
    {
        return super.getWidth();
    }

    public void setWidth(double width)
    {
        if (width <= 0)
        {
            String message = Logging.getMessage("Geom.WidthInvalid", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        super.setSize(width, width);
    }
}
