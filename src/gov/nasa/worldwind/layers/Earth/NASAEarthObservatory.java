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

import java.net.*;

/**
 * @author tag
 * @version $Id: NASAEarthObservatory.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class NASAEarthObservatory extends BasicTiledImageLayer
{
    private final String title;

    public NASAEarthObservatory(String layerName, String styleName, String title)
    {
        super(makeLevels(layerName, styleName, new URLBuilder(layerName, styleName)));
        this.setUseTransparentTextures(false);
        this.title = title;
    }

    private static LevelSet makeLevels(String layerName, String styleName, URLBuilder urlBuilder)
    {
        AVList params = new AVListImpl();

        params.setValue(AVKey.TILE_WIDTH, 512);
        params.setValue(AVKey.TILE_HEIGHT, 512);
        params.setValue(AVKey.DATA_CACHE_NAME, "Earth/NASAEarthObservatory/" + layerName + "/" + styleName);
        params.setValue(AVKey.SERVICE, "http://neowms.sci.gsfc.nasa.gov/wms/wms");
        params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        params.setValue(AVKey.DATASET_NAME, layerName);
        params.setValue(AVKey.NUM_LEVELS, 5);
        params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
        params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
        params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        params.setValue(AVKey.TILE_URL_BUILDER, urlBuilder);

        return new LevelSet(params);
    }

    private static class URLBuilder implements TileUrlBuilder
    {
        private final String layerName;
        private final String styleName;

        private URLBuilder(String layerName, String styleName)
        {
            this.layerName = layerName;
            this.styleName = styleName;
        }
        public URL getURL(Tile tile) throws MalformedURLException
        {
            StringBuffer sb = new StringBuffer(tile.getLevel().getService());
            if (sb.lastIndexOf("?") != sb.length() - 1)
                sb.append("?");
            sb.append("request=GetMap");
            sb.append("&version=1.3.0");
            sb.append("&layers=");
            sb.append(this.layerName);
            sb.append("&crs=CRS:84");
            sb.append("&width=");
            sb.append(tile.getLevel().getTileWidth());
            sb.append("&height=");
            sb.append(tile.getLevel().getTileHeight());

            Sector s = tile.getSector();
            sb.append("&bbox=");
            sb.append(s.getMinLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMinLatitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLatitude().getDegrees());
            sb.append("&format=image/png");
            sb.append("&styles=");
            sb.append(this.styleName);
//            sb.append("&transparent=true");
//            sb.append("&bgcolor=0x000000");

            return new java.net.URL(sb.toString());
        }
    }

    @Override
    public String toString()
    {
        return "NEO " + this.title;
    }
}
