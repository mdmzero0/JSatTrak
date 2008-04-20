/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.*;

/**
 * @author tag
 * @version $Id: USGSUrbanAreaOrtho.java 2683 2007-08-25 06:45:31Z tgaskins $
 */
public class USGSUrbanAreaOrtho extends BasicTiledImageLayer
{
    public USGSUrbanAreaOrtho()
    {
        super(makeLevels());
        this.setMaxActiveAltitude(10e3d);
        this.setSplitScale(0.8);
//        this.setShowImageTileOutlines(true);
//        this.setDrawBoundingVolumes(true);
    }

    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/USGS Urban Area Ortho");
        params.setValue(AVKey.SERVICE, "http://worldwind25.arc.nasa.gov/tile/tile.aspx");
        params.setValue(AVKey.DATASET_NAME, "104dds");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 12);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 2);

        Angle levelZeroDelta = Angle.fromDegrees(3.2);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(levelZeroDelta, levelZeroDelta));

        params.setValue(AVKey.SECTOR, new Sector(Angle.fromDegrees(17.84), Angle.fromDegrees(71.55),
            Angle.fromDegrees(-168.67), Angle.fromDegrees(-65.15)));

        return new LevelSet(params);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.USGSUrbanAreaOrtho.Name");
    }
}
