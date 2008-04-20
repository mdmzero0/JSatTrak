/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: GlobeRectangularTessellator.java 3632 2007-11-28 03:28:17Z tgaskins $
 */
public class GlobeRectangularTessellator extends RectangularTessellator
{
    private static final int DEFAULT_NUM_LAT_SUBDIVISIONS = 5; // TODO: make configurable
    private static final int DEFAULT_NUM_LON_SUBDIVISIONS = 10; // TODO: make configurable
    private static final int DEFAULT_DENSITY = 20; // TODO: make configurable

    private ArrayList<RectTile> topLevels;
    private Globe globe;
    private int density = DEFAULT_DENSITY;

    protected String getCacheId()
    {
        return GlobeRectangularTessellator.class.getName();
    }

    protected String getCacheName()
    {
        return "Terrain";
    }

    protected Iterable<RectTile> getTopLevelTiles(DrawContext dc)
    {
        if (this.topLevels != null)
            return this.topLevels;

        return this.topLevels = this.createTopLevelTiles(dc);
    }

    protected ArrayList<RectTile> createTopLevelTiles(DrawContext dc)
    {
        ArrayList<RectTile> tops =
            new ArrayList<RectTile>(DEFAULT_NUM_LAT_SUBDIVISIONS * DEFAULT_NUM_LON_SUBDIVISIONS);

        this.globe = dc.getGlobe();
        double deltaLat = 180d / DEFAULT_NUM_LAT_SUBDIVISIONS;
        double deltaLon = 360d / DEFAULT_NUM_LON_SUBDIVISIONS;
        Angle lastLat = Angle.NEG90;

        for (int row = 0; row < DEFAULT_NUM_LAT_SUBDIVISIONS; row++)
        {
            Angle lat = lastLat.addDegrees(deltaLat);
            if (lat.getDegrees() + 1d > 90d)
                lat = Angle.POS90;

            Angle lastLon = Angle.NEG180;

            for (int col = 0; col < DEFAULT_NUM_LON_SUBDIVISIONS; col++)
            {
                Angle lon = lastLon.addDegrees(deltaLon);
                if (lon.getDegrees() + 1d > 180d)
                    lon = Angle.POS180;

                Sector tileSector = new Sector(lastLat, lat, lastLon, lon);
                tops.add(this.createTile(dc, tileSector, 0));
                lastLon = lon;
            }
            lastLat = lat;
        }

        return tops;
    }

    protected RectTile createTile(DrawContext dc, Sector tileSector, int level)
    {
        Cylinder cylinder = dc.getGlobe().computeBoundingCylinder(1d, tileSector);
        double cellSize = tileSector.getDeltaLatRadians() * dc.getGlobe().getRadius() / this.density;

        return new RectTile(this, cylinder, level, this.density, tileSector, cellSize);
    }

    public int getTargetResolution(DrawContext dc, RectTile tile)
    {
        return dc.getGlobe().getElevationModel().getTargetResolution(dc, tile.sector, tile.density);
    }

    public boolean acceptResolution(DrawContext dc, RectTile tile, int resolution)
    {
        return tile.ri.resolution >= resolution;
    }

    @Override
    public RenderInfo buildVerts(DrawContext dc, RectTile tile, int resolution, boolean makeSkirts)
    {
        int density = tile.density;
        int numVertices = (density + 3) * (density + 3);
        java.nio.DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 3);

        Globe globe = dc.getGlobe();
        ElevationModel.Elevations elevations = globe.getElevationModel().getElevations(tile.sector, resolution);

        Angle latMax = tile.sector.getMaxLatitude();
        Angle dLat = tile.sector.getDeltaLat().divide(density);

        Angle lonMin = tile.sector.getMinLongitude();
        Angle lonMax = tile.sector.getMaxLongitude();
        Angle dLon = tile.sector.getDeltaLon().divide(density);

        int iv = 0;
        Angle lat = tile.sector.getMinLatitude();
        double verticalExaggeration = dc.getVerticalExaggeration();
        double exaggeratedMinElevation = makeSkirts ? globe.getMinElevation() * verticalExaggeration : 0;

        LatLon centroid = tile.sector.getCentroid();
        Vec4 refCenter = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), 0d);

        for (int j = 0; j <= density + 2; j++)
        {
            Angle lon = lonMin;
            for (int i = 0; i <= density + 2; i++)
            {
                double elevation = verticalExaggeration * elevations.getElevation(lat.radians, lon.radians);
                if (j == 0 || j >= tile.density + 2 || i == 0 || i >= tile.density + 2)
                {   // use abs to account for negative elevation.
                    elevation -= exaggeratedMinElevation >= 0 ? exaggeratedMinElevation : -exaggeratedMinElevation;
                }

                Vec4 p = globe.computePointFromPosition(lat, lon, elevation);
                verts.put(iv++, p.x - refCenter.x).put(iv++, p.y - refCenter.y).put(iv++, p.z - refCenter.z);

                if (i > density)
                    lon = lonMax;
                else if (i != 0)
                    lon = lon.add(dLon);
            }
            if (j > density)
                lat = latMax;
            else if (j != 0)
                lat = lat.add(dLat);
        }

        return new RenderInfo(density, verts, getTextureCoordinates(density), refCenter, elevations.getResolution());
    }
//
//    public RenderInfo buildVerts(DrawContext dc, RectTile tile, int resolution, boolean makeSkirts)
//    {
//        int density = tile.density;
//        int numVertices = (density + 3) * (density + 3);
//        java.nio.DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 3);
//
//        Globe globe = dc.getGlobe();
//        ElevationModel.Elevations elevations = globe.getElevationModel().getElevations(tile.sector, resolution);
//
//        double latMin = tile.sector.getMinLatitude().radians;
//        double latMax = tile.sector.getMaxLatitude().radians;
//        double dLat = (latMax - latMin) / density;
//
//        double lonMin = tile.sector.getMinLongitude().radians;
//        double lonMax = tile.sector.getMaxLongitude().radians;
//        double dLon = (lonMax - lonMin) / density;
//
//        int iv = 0;
//        double lat = latMin;
//        double verticalExaggeration = dc.getVerticalExaggeration();
//        double exaggeratedMinElevation = makeSkirts ? globe.getMinElevation() * verticalExaggeration : 0;
//        double equatorialRadius = globe.getEquatorialRadius();
//        double eccentricity = globe.getEccentricitySquared();
//
//        LatLon centroid = tile.sector.getCentroid();
//        Vec4 refCenter = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), 0d);
//
//        for (int j = 0; j <= density + 2; j++)
//        {
//            double cosLat = Math.cos(lat);
//            double sinLat = Math.sin(lat);
//            double rpm = equatorialRadius / Math.sqrt(1.0 - eccentricity * sinLat * sinLat);
//            double lon = lonMin;
//            for (int i = 0; i <= density + 2; i++)
//            {
//                double elevation = verticalExaggeration * elevations.getElevation(lat, lon);
//                if (j == 0 || j >= tile.density + 2 || i == 0 || i >= tile.density + 2)
//                {   // use abs to account for negative elevation.
//                    elevation -= exaggeratedMinElevation >= 0 ? exaggeratedMinElevation : -exaggeratedMinElevation;
//                }
//
//                double x = ((rpm + elevation) * cosLat * Math.sin(lon)) - refCenter.x;
//                double y = ((rpm * (1.0 - eccentricity) + elevation) * sinLat) - refCenter.y;
//                double z = ((rpm + elevation) * cosLat * Math.cos(lon)) - refCenter.z;
//
//                verts.put(iv++, x).put(iv++, y).put(iv++, z);
//
//                if (i > density)
//                    lon = lonMax;
//                else if (i != 0)
//                    lon += dLon;
//            }
//            if (j > density)
//                lat = latMax;
//            else if (j != 0)
//                lat += dLat;
//        }
//
//        return new RenderInfo(density, verts, getTextureCoordinates(density), refCenter,
//            elevations.getResolution());
//    }

    public GlobeRectangularTessellator.CacheKey createCacheKey(DrawContext dc, RectTile tile, int resolution)
    {
        return new CacheKey(dc.getGlobe(), tile.sector, resolution, dc.getVerticalExaggeration(), tile.density);
    }

    protected static class CacheKey extends RectangularTessellator.CacheKey
    {
        private final Globe globe;
        private final double verticalExaggeration;

        protected CacheKey(Globe globe, Sector sector, int resolution, double verticalExaggeration, int density)
        {
            super(sector, resolution, density);

            this.globe = globe;
            this.verticalExaggeration = verticalExaggeration;
        }

        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            CacheKey cacheKey = (CacheKey) o;

            if (Double.compare(cacheKey.verticalExaggeration, verticalExaggeration) != 0) return false;
            //noinspection RedundantIfStatement
            if (globe != null ? !globe.equals(cacheKey.globe) : cacheKey.globe != null) return false;

            return true;
        }

        public int hashCode()
        {
            int result = super.hashCode();
            long temp;
            result = 31 * result + (globe != null ? globe.hashCode() : 0);
            temp = verticalExaggeration != +0.0d ? Double.doubleToLongBits(verticalExaggeration) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }
    }

    protected Vec4 getSurfacePoint(RectTile tile, Angle latitude, Angle longitude, double metersOffset)
    {
        Vec4 result = super.getSurfacePoint(tile, latitude, longitude);
        if (metersOffset != 0 && result != null)
            result = applyOffset(this.globe, result, metersOffset);

        return result;
    }

    /**
     * Offsets <code>point</code> by <code>metersOffset</code> meters.
     *
     * @param globe        the <code>Globe</code> from which to offset
     * @param point        the <code>Vec4</code> to offset
     * @param metersOffset the magnitude of the offset
     * @return <code>point</code> offset along its surface normal as if it were on <code>globe</code>
     */
    private static Vec4 applyOffset(Globe globe, Vec4 point, double metersOffset)
    {
        Vec4 normal = globe.computeSurfaceNormalAtPoint(point);
        point = Vec4.fromLine3(point, metersOffset, normal);
        return point;
    }
}
