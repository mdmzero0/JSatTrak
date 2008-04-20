/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.globes.SectorGeometry;
import gov.nasa.worldwind.geom.Sector;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: GeographicSurfaceTileRenderer.java 2533 2007-08-13 05:59:08Z tgaskins $
 */
public class GeographicSurfaceTileRenderer extends SurfaceTileRenderer
{
    private double sgWidth;
    private double sgHeight;
    private double sgMinWE;
    private double sgMinSN;

    protected void preComputeTransform(DrawContext dc, SectorGeometry sg)
    {
        Sector st = sg.getSector();
        this.sgWidth = st.getDeltaLonRadians();
        this.sgHeight = st.getDeltaLatRadians();
        this.sgMinWE = st.getMinLongitude().radians;
        this.sgMinSN = st.getMinLatitude().radians;
    }

    protected void computeTransform(DrawContext dc, SurfaceTile tile, Transform t)
    {
        Sector st = tile.getSector();
        double tileWidth = st.getDeltaLonRadians();
        double tileHeight = st.getDeltaLatRadians();
        double minLon = st.getMinLongitude().radians;
        double minLat = st.getMinLatitude().radians;

        t.VScale = tileHeight > 0 ? this.sgHeight / tileHeight : 1;
        t.HScale = tileWidth > 0 ? this.sgWidth / tileWidth : 1;
        t.VShift = -(minLat - this.sgMinSN) / this.sgHeight;
        t.HShift = -(minLon - this.sgMinWE) / this.sgWidth;
    }

    protected Iterable<SurfaceTile> getIntersectingTiles(DrawContext dc, SectorGeometry sg,
        Iterable<? extends SurfaceTile> tiles)
    {
        ArrayList<SurfaceTile> intersectingTiles = null;

        for (SurfaceTile tile : tiles)
        {
            if (!tile.getSector().intersects(sg.getSector()))
                continue;

            if (intersectingTiles == null)
                intersectingTiles = new ArrayList<SurfaceTile>();

            intersectingTiles.add(tile);
        }

        if (intersectingTiles == null)
            return null;

        return intersectingTiles;
    }
}
