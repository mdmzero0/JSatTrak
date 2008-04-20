/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Polyline;

import java.util.ArrayList;
import java.awt.*;

/**
 * Creates a global grid along lat/lon lines at a constant altitude above the globe.
 * @author tag
 * @version $Id: GlobalGridAboveSurface.java 3235 2007-10-09 10:19:16Z tgaskins $
 */
public class GlobalGridAboveSurface extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            RenderableLayer shapeLayer = new RenderableLayer();

            // Generate meridians
            ArrayList<Position> positions = new ArrayList<Position>(3);
            for (double lon = -180; lon < 180; lon += 10)
            {
                Angle longitude = Angle.fromDegrees(lon);
                positions.clear();
                positions.add(new Position(Angle.NEG90, longitude, 10e3));
                positions.add(new Position(Angle.ZERO, longitude, 10e3));
                positions.add(new Position(Angle.POS90, longitude, 10e3));
                Polyline polyline = new Polyline(positions);
                polyline.setFollowTerrain(false);
                polyline.setNumSubsegments(30);
                polyline.setColor(new Color(1f, 1f, 1f, 0.5f));
                shapeLayer.addRenderable(polyline);
            }

            // Generate parallels
            for (double lat = -80; lat < 90; lat += 10)
            {
                Angle latitude = Angle.fromDegrees(lat);
                positions.clear();
                positions.add(new Position(latitude, Angle.NEG180, 10e3));
                positions.add(new Position(latitude, Angle.ZERO, 10e3));
                positions.add(new Position(latitude, Angle.POS180, 10e3));
                Polyline polyline = new Polyline(positions);
                polyline.setPathType(Polyline.LINEAR);
                polyline.setFollowTerrain(false);
                polyline.setNumSubsegments(30);
                polyline.setColor(new Color(1f, 1f, 1f, 0.5f));
                shapeLayer.addRenderable(polyline);
            }

            insertBeforeCompass(this.getWwd(), shapeLayer);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Lines In Space", AppFrame.class);
    }
}
