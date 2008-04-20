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

import java.util.GregorianCalendar;

/**
 * @author tag
 * @version $Id: LandsatI3.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class LandsatI3 extends BasicTiledImageLayer
{
    public LandsatI3()
    {
        super(makeLevels());
        this.setUseTransparentTextures(true);
    }

    private static LevelSet makeLevels()
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/NASA LandSat I3");
        params.setValue(AVKey.SERVICE, "http://worldwind25.arc.nasa.gov/lstile/lstile.aspx");
        params.setValue(AVKey.DATASET_NAME, "esat_worlddds");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.NUM_LEVELS, 10);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 4);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        params.setValue(AVKey.EXPIRY_TIME, new GregorianCalendar(2007, 2, 22).getTimeInMillis());

        return new LevelSet(params);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.Earth.LandsatI3Layer.Name");
    }
}
