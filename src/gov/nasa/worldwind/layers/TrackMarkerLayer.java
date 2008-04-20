/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.util.Logging;

import java.util.List;

/**
 * @author tag
 * @version $Id: TrackMarkerLayer.java 2817 2007-09-12 19:28:14Z tgaskins $
 */
public class TrackMarkerLayer extends TrackLayer
{
    private TrackRenderer trackRenderer = new TrackRenderer();
    private IconRenderer iconRenderer = new IconRenderer();
    private UserFacingIcon icon;

    public TrackMarkerLayer(List<Track> tracks)
    {
        super(tracks);
    }

    public void dispose()
    {
        this.trackRenderer.dispose();
    }

    public double getMarkerPixels()
    {
        return this.trackRenderer.getMarkerPixels();
    }

    public void setMarkerPixels(double markerPixels)
    {
        this.trackRenderer.setMarkerPixels(markerPixels);
    }

    public double getMinMarkerSize()
    {
        return this.trackRenderer.getMinMarkerSize();
    }

    public void setMinMarkerSize(double minMarkerSize)
    {
        this.trackRenderer.setMinMarkerSize(minMarkerSize);
    }

    public void setElevation(double markerElevation)
    {
        super.setElevation(markerElevation);
        this.trackRenderer.setElevation(markerElevation);
    }

    public void setOverrideElevation(boolean overrideElevation)
    {
        super.setOverrideElevation(overrideElevation);
        this.trackRenderer.setOverrideElevation(overrideElevation);
    }

    public Material getMaterial()
    {
        return this.trackRenderer.getMaterial();
    }

    public void setMaterial(Material material)
    {
        this.trackRenderer.setMaterial(material);
    }

    public String getIconFilePath()
    {
        return this.icon != null ? this.icon.getPath() : null;
    }

    public void setIconFilePath(String iconFilePath)
    {
        this.icon = iconFilePath != null ? new UserFacingIcon(iconFilePath, null) : null;
    }

    public void setMarkerShape(String shapeName)
    {
        this.trackRenderer.setShapeType(shapeName);
    }

    @Override
    public void setLowerLimit(int lowerLimit)
    {
        super.setLowerLimit(lowerLimit);
        this.trackRenderer.setLowerLimit(this.getLowerLimit());
    }

    @Override
    public void setUpperLimit(int upperLimit)
    {
        super.setUpperLimit(upperLimit);
        this.trackRenderer.setUpperLimit(this.getUpperLimit());
    }

    protected void doDraw(DrawContext dc, TrackPointIterator trackPoints, java.awt.Point pickPoint)
    {
        Vec4 iconPoint = this.trackRenderer.render(dc, trackPoints);

        if (iconPoint != null && this.icon != null)
        {
            if (dc.isPickingMode())
                this.iconRenderer.pick(dc, this.icon, iconPoint, pickPoint, this);
            else
                this.iconRenderer.render(dc, this.icon, iconPoint);
        }
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.TrackMarkerLayer.Name");
    }
}