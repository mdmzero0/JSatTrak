/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.LatLon;

import java.awt.*;

/**
 * @deprecated Superseded by {@link Polyline}
 * @author tag
 * @version $Id: SurfacePolyline.java 3274 2007-10-10 23:14:45Z tgaskins $
 */
public class SurfacePolyline extends SurfacePolygon
{
    public SurfacePolyline(Iterable<LatLon> positions, Color color, Color borderColor)
    {
        super(positions, color, borderColor);
        this.setDrawInterior(false);
        this.setStroke(new BasicStroke(3f));
    }

    public SurfacePolyline(Iterable<LatLon> positions)
    {
        super(positions);
        this.setDrawInterior(false);
        this.setPaint(new Color(1f, 1f, 0f, .8f));
        this.setStroke(new BasicStroke(3f));
    }
}
