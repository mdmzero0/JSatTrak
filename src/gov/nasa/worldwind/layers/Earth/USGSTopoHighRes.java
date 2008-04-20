/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;

/**
 * @author tag
 * @version $Id: USGSTopoHighRes.java 2919 2007-09-19 06:04:49Z tgaskins $
 */
public class USGSTopoHighRes extends BasicTiledImageLayer
{
    public USGSTopoHighRes()
    {
        super(makeLevels());
        this.setMaxActiveAltitude(7e6d);
        this.setSplitScale(1.2);
    }

    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/USGS Topographic Maps");
        params.setValue(AVKey.SERVICE, "http://worldwind25.arc.nasa.gov/tile/tile.aspx");
        params.setValue(AVKey.DATASET_NAME, "102dds");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 9);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 2);
        params.setValue(AVKey.INACTIVE_LEVELS, "0,1,2,3,4,5,6");

        Angle levelZeroDelta = Angle.fromDegrees(3.2);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(levelZeroDelta, levelZeroDelta));

        params.setValue(AVKey.SECTOR, new Sector(Angle.fromDegrees(17.84), Angle.fromDegrees(71.55),
            Angle.fromDegrees(-168.67), Angle.fromDegrees(-65.15)));

        return new LevelSet(params);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.USGSTopographicMaps.Name");
    }
}
