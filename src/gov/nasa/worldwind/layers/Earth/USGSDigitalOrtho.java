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
 * @version $Id: USGSDigitalOrtho.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class USGSDigitalOrtho extends BasicTiledImageLayer
{
    public USGSDigitalOrtho()
    {
        super(makeLevels());
        this.setMaxActiveAltitude(7e3d);
    }

    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/USGS Digital Ortho");
        params.setValue(AVKey.SERVICE, "http://worldwind25.arc.nasa.gov/tile/tile.aspx");
        params.setValue(AVKey.DATASET_NAME, "101dds");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 10);
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
        return Logging.getMessage("layers.Earth.USGSDigitalOrtho.Name");
    }
}
