/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.SectorGeometryList;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.tracks.TrackPoint;

import javax.media.opengl.GL;

/**
 * @author tag
 * @version $Id: TrackRenderer.java 2817 2007-09-12 19:28:16Z tgaskins $
 */
public class TrackRenderer extends LocationRenderer
{
    private double markerPixels = 8d; // TODO: these should all be configurable
    private double minMarkerSize = 3d;
    private Material material = Material.WHITE;
    private String iconFilePath;
    private LocationRenderer.Shape shape = SPHERE;

    public TrackRenderer()
    {
    }

    public double getMarkerPixels()
    {
        return markerPixels;
    }

    public void setMarkerPixels(double markerPixels)
    {
        this.markerPixels = markerPixels;
    }

    public double getMinMarkerSize()
    {
        return minMarkerSize;
    }

    public void setMinMarkerSize(double minMarkerSize)
    {
        this.minMarkerSize = minMarkerSize;
    }

    public Material getMaterial()
    {
        return material;
    }

    public void setMaterial(Material material)
    {
        if (material == null)
        {
            String msg = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // don't validate material's colors - material does that.

        this.material = material;
    }

    public String getIconFilePath()
    {
        return iconFilePath;
    }

    public void setIconFilePath(String iconFilePath)
    {
        //don't validate - a null iconFilePath cancels icon drawing
        this.iconFilePath = iconFilePath;
    }

    public void setShapeType(String shapeName)
    {
        if (shapeName.equalsIgnoreCase("Cone"))
            this.shape = CONE;
        else if (shapeName.equalsIgnoreCase("Cylinder"))
            this.shape = CYLINDER;
        else
            this.shape = SPHERE;
    }

    protected Vec4 draw(DrawContext dc, java.util.Iterator<TrackPoint> trackPositions)
    {
        if (dc.getVisibleSector() == null)
            return null;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        if (geos == null)
            return null;

        if (!this.shape.isInitialized)
            this.shape.initialize(dc);

        int index = 0;
        java.util.List<Vec4> points = new java.util.ArrayList<Vec4>();
        while (trackPositions.hasNext())
        {
            TrackPoint tp = trackPositions.next();
            if (index >= this.lowerLimit && index <= this.upperLimit)
            {
                Vec4 point = this.computeSurfacePoint(dc, tp);
                if (point != null)
                {
                    points.add(point);
                }
            }
            if (++index >= this.upperLimit)
                break;
        }

        if (points.size() < 1)
            return null;

        Vec4 firstPoint = points.get(0);
        Vec4 lastPointDrawn = firstPoint;
        this.begin(dc);
        {
            this.material.apply(dc.getGL(), GL.GL_FRONT);

            Vec4 previousDrawnPoint = null;

            double radius = this.computeMarkerRadius(dc, firstPoint);
            this.shape.render(dc, firstPoint, radius);
            for (Vec4 point : points)
            {
                if (previousDrawnPoint == null)
                {
                    previousDrawnPoint = firstPoint;
                    continue; // skip over first point
                }

                // TODO: More sophisticated separation algorithm to gain frame-to-frame consistency
                radius = this.computeMarkerRadius(dc, point);
                double separation = point.distanceTo3(previousDrawnPoint);
                double minSeparation = 4d * radius;
                if (separation > minSeparation)
                {
                    if (!dc.isPickingMode())
                        this.shape.render(dc, point, radius);

                    previousDrawnPoint = point;
                    lastPointDrawn = point;
                }
            }
        }
        this.end(dc);

        Vec4 iconPoint = points.get(points.size() - 1);
        return iconPoint != null ? iconPoint : lastPointDrawn;
    }

    private double computeMarkerRadius(DrawContext dc, Vec4 point)
    {
        double d = point.distanceTo3(dc.getView().getEyePoint());
        double radius = this.markerPixels * dc.getView().computePixelSizeAtDistance(d);
        if (radius < this.minMarkerSize)
            radius = this.minMarkerSize;

        return radius;
    }
}
