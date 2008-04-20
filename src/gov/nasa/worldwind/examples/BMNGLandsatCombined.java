/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.*;

import java.net.*;
import java.util.ArrayList;

/**
 * Illustrates how to create a single layer consisting of multiple data sets.
 * Note: The i-cubed Landsat imagery used in this example contains transparent regions, mostly over the oceans.
 * To cover that imagery, the example places a single low-resolution image below the combined layer.
 * @author tag
 * @version $Id: BMNGLandsatCombined.java 3687 2007-12-03 22:05:21Z tgaskins $
 */
public class BMNGLandsatCombined extends BasicTiledImageLayer
{
    private static final String LAYERS = "gov.nasa.worldwind.layers.Earth.StarsLayer"
        + ",gov.nasa.worldwind.layers.Earth.SkyGradientLayer"
        + ",gov.nasa.worldwind.layers.Earth.FogLayer"
        + ",gov.nasa.worldwind.layers.Earth.BMNGOneImage" // underlay the combined image
        + ",gov.nasa.worldwind.examples.BMNGLandsatCombined"
        + ",gov.nasa.worldwind.layers.Earth.EarthNASAPlaceNameLayer"
        + ",gov.nasa.worldwind.layers.Earth.WorldMapLayer"
        + ",gov.nasa.worldwind.layers.Earth.ScalebarLayer"
        + ",gov.nasa.worldwind.layers.CompassLayer";

    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, true);
        }
    }

    public static void main(String[] args)
    {
        Configuration.setValue(AVKey.LAYERS_CLASS_NAMES, LAYERS);
        ApplicationTemplate.start("World Wind Combination Layer", AppFrame.class);
    }

    public BMNGLandsatCombined()
    {
        super(makeLevels());
    }

    /**
     * Compose a single layer from Blue Marble imagery and i-cubed Landsat imagery.
     * @return the layer combining the two data sets.
     */
    private static LevelSet makeLevels()
    {
        ArrayList<Level> levels = new ArrayList<Level>();
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 14);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);

        params.setValue(AVKey.TILE_URL_BUILDER, new TileUrlBuilder()
        {
            public URL getURL(Tile tile) throws MalformedURLException
            {
                StringBuffer sb = new StringBuffer(tile.getLevel().getService());
                if (sb.lastIndexOf("?") != sb.length() - 1)
                    sb.append("?");
                sb.append("T=");
                sb.append(tile.getLevel().getDataset());
                sb.append("&L=");
                sb.append(tile.getLevel().getLevelName());
                sb.append("&X=");
                sb.append(tile.getColumn());
                sb.append("&Y=");
                sb.append(tile.getRow());

                return new URL(sb.toString());
            }
        });

        LatLon levelZeroTileDelta = (LatLon) params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA);
        for (int i = 0; i < 14; i++)
        {
            params.setValue(AVKey.LEVEL_NAME, Integer.toString(i));
            params.setValue(AVKey.LEVEL_NUMBER, i);

            Angle latDelta = levelZeroTileDelta.getLatitude().divide(Math.pow(2, i));
            Angle lonDelta = levelZeroTileDelta.getLongitude().divide(Math.pow(2, i));
            params.setValue(AVKey.TILE_DELTA, new LatLon(latDelta, lonDelta));

            if (i < 4)
            {
                params.setValue(AVKey.DATA_CACHE_NAME,
                    "Earth/BMNG/BMNG(Shaded + Bathymetry) Tiled - Version 1.1 - 5.2004");
                params.setValue(AVKey.SERVICE, "http://worldwind25.arc.nasa.gov/tile/tile.aspx");
                params.setValue(AVKey.DATASET_NAME, "bmng.topo.bathy.200405dds");
            }
            else
            {
                params.setValue(AVKey.LEVEL_NAME, Integer.toString(i - 4));
                params.setValue(AVKey.DATA_CACHE_NAME, "Earth/NASA LandSat I3");
                params.setValue(AVKey.SERVICE, "http://worldwind25.arc.nasa.gov/lstile/lstile.aspx");
                params.setValue(AVKey.DATASET_NAME, "esat_worlddds");
            }

            levels.add(new Level(params));
        }

        return new LevelSet(levels, params);
    }

    @Override
    public String toString()
    {
        return "Combined Blue Marble and i3 Landsat";
    }
}
