/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.globes.SectorGeometryList;

import java.util.List;

/**
 * @author tag
 * @version $Id: TrackLayer.java 2817 2007-09-12 19:28:14Z tgaskins $
 */
public abstract class TrackLayer extends AbstractLayer
{
    private java.util.List<Track> tracks = new java.util.ArrayList<Track>();
    private Sector boundingSector;
    private int lowerLimit;
    private int upperLimit;
    private boolean overrideElevation = false;
    private double elevation = 10d;

    public TrackLayer(List<Track> tracks)
    {
        if (tracks == null)
        {
            String msg = Logging.getMessage("nullValue.TracksIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.tracks = tracks;
        this.boundingSector = Sector.boundingSector(this.iterator());
    }

    public TrackPointIteratorImpl iterator()
    {
        return new TrackPointIteratorImpl(this.tracks);
    }

    public int getNumPoints()
    {
        return this.iterator().getNumPoints();
    }

    public List<Track> getTracks()
    {
        return tracks;
    }

    public void setTracks(List<Track> tracks)
    {
        this.tracks = tracks;
    }

    public Sector getBoundingSector()
    {
        return boundingSector;
    }

    public void setBoundingSector(Sector boundingSector)
    {
        this.boundingSector = boundingSector;
    }

    public int getLowerLimit()
    {
        return lowerLimit;
    }

    public void setLowerLimit(int lowerLimit)
    {
        this.lowerLimit = lowerLimit;
    }

    public int getUpperLimit()
    {
        return upperLimit;
    }

    public void setUpperLimit(int upperLimit)
    {
        this.upperLimit = upperLimit;
    }

    public double getElevation()
    {
        return this.elevation;
    }

    public void setElevation(double markerElevation)
    {
        this.elevation = markerElevation;
    }

    public boolean isOverrideElevation()
    {
        return this.overrideElevation;
    }

    public void setOverrideElevation(boolean overrideElevation)
    {
        this.overrideElevation = overrideElevation;
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.TrackLayer.Name");
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        this.draw(dc, pickPoint);
    }

    protected void doRender(DrawContext dc)
    {
        this.draw(dc, null);
    }

    private void draw(DrawContext dc, java.awt.Point pickPoint)
    {
        TrackPointIterator trackPoints = this.iterator();
        if (!trackPoints.hasNext())
            return;

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        if (geos == null)
            return;

        if (!dc.getVisibleSector().intersects(this.getBoundingSector()))
            return;

        this.doDraw(dc, trackPoints, pickPoint);
    }

    protected abstract void doDraw(DrawContext dc, TrackPointIterator trackPoints, java.awt.Point pickPoint);
}
