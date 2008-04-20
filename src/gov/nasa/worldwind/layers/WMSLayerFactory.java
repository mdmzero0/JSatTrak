/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.wms.*;
import org.w3c.dom.Element;

import java.net.*;

/**
 * @author tag
 * @version $Id: WMSLayerFactory.java 3601 2007-11-21 03:23:23Z tgaskins $
 */
public class WMSLayerFactory
{
    public static Layer newLayer(Capabilities caps, AVList params)
    {
        if (caps == null)
        {
            String message = Logging.getMessage("nullValue.WMSCapabilities");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (params == null)
        {
            String message = Logging.getMessage("nullValue.LayerConfigParams");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String layerNames = params.getStringValue(AVKey.LAYER_NAMES);
        if (layerNames == null || layerNames.length() == 0)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String[] names = layerNames.split(",");
        if (names == null || names.length == 0)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String name : names)
        {
            if (caps.getLayerByName(name) == null)
            {
                String message = Logging.getMessage("WMS.LayerNameMissing", name);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }

        params.setValue(AVKey.DATASET_NAME, layerNames);

        String mapRequestURIString = caps.getGetMapRequestGetURL();
        mapRequestURIString = fixGetMapString(mapRequestURIString);
        if (params.getValue(AVKey.SERVICE) == null)
            params.setValue(AVKey.SERVICE, mapRequestURIString);
        mapRequestURIString = params.getStringValue(AVKey.SERVICE);
        if (mapRequestURIString == null || mapRequestURIString.length() == 0)
        {
            Logging.logger().severe("WMS.RequestMapURLMissing");
            throw new IllegalArgumentException(Logging.getMessage("WMS.RequestMapURLMissing"));
        }

        String styleNames = params.getStringValue(AVKey.STYLE_NAMES);
        if (params.getValue(AVKey.DATA_CACHE_NAME) == null)
        {
            try
            {
                URI mapRequestURI = new URI(mapRequestURIString);
                String cacheName = WWIO.formPath(mapRequestURI.getAuthority(), mapRequestURI.getPath(), layerNames,
                    styleNames);
                params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
            }
            catch (URISyntaxException e)
            {
                String message = Logging.getMessage("WMS.RequestMapURLBad", mapRequestURIString);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }

        // Determine image format to request.
        if (params.getStringValue(AVKey.IMAGE_FORMAT) == null)
        {
            String imageFormat = chooseImageFormat(caps);
            params.setValue(AVKey.IMAGE_FORMAT, imageFormat);
        }

        if (params.getStringValue(AVKey.IMAGE_FORMAT) == null)
        {
            Logging.logger().severe("WMS.NoImageFormats");
            throw new IllegalArgumentException(Logging.getMessage("WMS.NoImageFormats"));
        }

        // Determine bounding sector.
        Sector sector = (Sector) params.getValue(AVKey.SECTOR);
        if (sector == null)
        {
            for (String name : names)
            {
                BoundingBox bb = caps.getLayerGeographicBoundingBox(caps.getLayerByName(name));
                if (bb == null)
                {
                    Logging.logger().log(java.util.logging.Level.SEVERE, "WMS.NoGeographicBoundingBoxForLayer", name);
                    continue;
                }

                sector = Sector.union(sector, Sector.fromDegrees(
                    clamp(bb.getMiny(), -90d, 90d),
                    clamp(bb.getMaxy(), -90d, 90d),
                    clamp(bb.getMinx(), -180d, 180d),
                    clamp(bb.getMaxx(), -180d, 180d)));
            }

            if (sector == null)
            {
                Logging.logger().severe("WMS.NoGeographicBoundingBox");
                throw new IllegalArgumentException(Logging.getMessage("WMS.NoGeographicBoundingBox"));
            }
            params.setValue(AVKey.SECTOR, sector);
        }

        if (params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA) == null)
        {
            Angle delta = Angle.fromDegrees(36);
            params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(delta, delta));
        }

        if (params.getValue(AVKey.TILE_WIDTH) == null)
            params.setValue(AVKey.TILE_WIDTH, 512);
        if (params.getValue(AVKey.TILE_HEIGHT) == null)
            params.setValue(AVKey.TILE_HEIGHT, 512);
        if (params.getValue(AVKey.FORMAT_SUFFIX) == null)
            params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
        if (params.getValue(AVKey.NUM_LEVELS) == null)
            params.setValue(AVKey.NUM_LEVELS, 14); // approximately 0.5 meters per pixel
        if (params.getValue(AVKey.NUM_EMPTY_LEVELS) == null)
            params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);

        // TODO: adjust for subsetable, fixedimage, etc.

        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(caps, layerNames, styleNames, params));

        TiledImageLayer layer = new BasicTiledImageLayer(new LevelSet(params));
        layer.setUseTransparentTextures(true);
//        layer.setShowImageTileOutlines(true);
//        layer.setDrawTileIDs(true);

        layer.setName(makeTitle(caps, layerNames, styleNames));

        return layer;
    }

    private static double clamp(double v, double min, double max)
    {
        return v < min ? min : v > max ? max : v;
    }

    private static String fixGetMapString(String gms)
    {
        gms = gms.trim();
        int qMarkIndex = gms.indexOf("?");
        if (qMarkIndex < 0)
            gms += "?";
        else if (qMarkIndex != gms.length() - 1)
            if (gms.lastIndexOf("&") != gms.length() - 1)
                gms += "&";

        return gms;
    }

    private static String makeTitle(Capabilities caps, String layerNames, String styleNames)
    {
        String[] lNames = layerNames.split(",");
        String[] sNames = styleNames != null ? styleNames.split(",") : null;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lNames.length; i++)
        {
            if (sb.length() > 0)
                sb.append(", ");

            String layerName = lNames[i];
            Element layer = caps.getLayerByName(layerName);
            String layerTitle = caps.getLayerTitle(layer);
            sb.append(layerTitle != null ? layerTitle : layerName);

            if (sNames == null || sNames.length <= i)
                continue;

            String styleName = sNames[i];
            Element style = caps.getLayerStyleByName(layer, styleName);
            if (style == null)
                continue;

            sb.append(" : ");
            String styleTitle = caps.getStyleTitle(layer, style);
            sb.append(styleTitle != null ? styleTitle : styleName);
        }

        return sb.toString();
    }

    private static final String[] formatOrderPreference = new String[]
        {
            "image/dds", "image/png", "image/jpeg"
        };

    private static String chooseImageFormat(Capabilities caps)
    {
        String[] formats = caps.getGetMapFormats();
        if (formats == null || formats.length == 0)
            return null;

        for (String s : formatOrderPreference)
        {
            for (String f : formats)
            {
                if (f.equalsIgnoreCase(s))
                    return f;
            }
        }

        return formats[0]; // none recognized; just use the first in the caps list
    }

    private static class URLBuilder implements TileUrlBuilder
    {
        private static final String MAX_VERSION = "1.3.0";

        private final String layerNames;
        private final String styleNames;
        private final String imageFormat;
        private final String wmsVersion;
        private final String crs;
        public String URLTemplate = null;

        private URLBuilder(Capabilities caps, String layerNames, String styleNames, AVList params)
        {
            this.layerNames = layerNames;
            this.styleNames = styleNames;
            this.imageFormat = params.getStringValue(AVKey.IMAGE_FORMAT);

            String version = caps.getVersion();
            if (version == null || version.compareTo(MAX_VERSION) >= 0)
            {
                this.wmsVersion = MAX_VERSION;
                this.crs = "&crs=CRS:84";
            }
            else
            {
                this.wmsVersion = version;
                this.crs = "&srs=EPSG:4326";
            }
        }

        public URL getURL(Tile tile) throws MalformedURLException
        {
            StringBuffer sb;
            if (this.URLTemplate == null)
            {
                sb = new StringBuffer(tile.getLevel().getService());
                sb.append("service=WMS");
                sb.append("&request=GetMap");
                sb.append("&version=");
                sb.append(this.wmsVersion);
                sb.append(this.crs);
                sb.append("&layers=");
                sb.append(this.layerNames);
                sb.append("&styles=");
                sb.append(this.styleNames != null ? this.styleNames : "default");
                sb.append("&width=");
                sb.append(tile.getLevel().getTileWidth());
                sb.append("&height=");
                sb.append(tile.getLevel().getTileHeight());
                sb.append("&format=");
                sb.append(this.imageFormat);
                sb.append("&transparent=TRUE");
                sb.append("&bgcolor=0x000000");

                this.URLTemplate = sb.toString();
            }
            else
            {
                sb = new StringBuffer(this.URLTemplate);
            }

            Sector s = tile.getSector();
            sb.append("&bbox=");
            sb.append(s.getMinLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMinLatitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLongitude().getDegrees());
            sb.append(",");
            sb.append(s.getMaxLatitude().getDegrees());
            sb.append("&"); // terminate the query string

            return new java.net.URL(sb.toString().replace(" ", "%20"));
        }
    }
}

// Below is a start at code to crete a layer given a template GetMap URL.
//
//        private URLBuilder(URI URLTemplate)
//        {
//            // TODO: Arg check
//            this.URLTemplate = URLTemplate.toString();
//            this.layerNames = extractWMSParameter("layers", URLTemplate);
//            this.styleNames = extractWMSParameter("styles", URLTemplate);
//            this.imageFormat = extractWMSParameter("format", URLTemplate);
//            this.wmsVersion = extractWMSParameter("version", URLTemplate);
//
//            String c = extractWMSParameter("crs", URLTemplate);
//            this.crs = c != null ? c : extractWMSParameter("srs", URLTemplate);
//        }
//
//    public static Layer newLayer(String urlTemplate, Sector layerExtent, AVList params)
//    {
//        URI mapRequestURI;
//        try
//        {
//            mapRequestURI = new URI(urlTemplate);
//        }
//        catch (URISyntaxException e)
//        {
//            String message = Logging.getMessage("WMS.RequestMapURLBad", urlTemplate);
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        return newLayer(mapRequestURI, layerExtent, params);
//    }
//
//    public static Layer newLayer(URI mapRequestURI, Sector layerExtent, AVList params)
//    {
//        if (mapRequestURI == null)
//        {
//            String message = Logging.getMessage("nullValue.WMSCapabilities"); // TODO: proper log message
//            Logging.logger().severe(message);
//            throw new IllegalArgumentException(message);
//        }
//
//        if (params == null)
//            params = new AVListImpl();
//
//        // Determine bounding sector.
//        Sector sector = layerExtent != null ? layerExtent : (Sector) params.getValue(AVKey.SECTOR);
//        if (sector == null)
//        {
//            Logging.logger().severe("WMS.NoGeographicBoundingBox");
//            throw new IllegalArgumentException(Logging.getMessage("WMS.NoGeographicBoundingBox"));
//        }
//        params.setValue(AVKey.SECTOR, sector);
//
//        String layerNames = extractWMSParameter("layers", mapRequestURI);
//        String styleNames = extractWMSParameter("styles", mapRequestURI);
//        String width = extractWMSParameter("width", mapRequestURI);
//        params.setValue(AVKey.TILE_WIDTH, width);
//        String height = extractWMSParameter("height", mapRequestURI);
//        params.setValue(AVKey.TILE_HEIGHT, height);
//
//        String cacheName = params.getStringValue(AVKey.DATA_CACHE_NAME);
//        if (cacheName == null)
//            cacheName = WWIO.formPath(mapRequestURI.getAuthority(), mapRequestURI.getPath(), layerNames, styleNames);
//        params.setValue(AVKey.DATA_CACHE_NAME, cacheName);
//
//        if (params.getValue(AVKey.LEVEL_ZERO_TILE_DELTA) == null)
//        {
//            Angle delta = Angle.fromDegrees(36);
//            params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(delta, delta));
//        }
//
//        if (params.getValue(AVKey.FORMAT_SUFFIX) == null)
//            params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
//        if (params.getValue(AVKey.NUM_LEVELS) == null)
//            params.setValue(AVKey.NUM_LEVELS, 14); // approximately 0.5 meters per pixel
//        if (params.getValue(AVKey.NUM_EMPTY_LEVELS) == null)
//            params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
//
//        // TODO: adjust for subsetable, fixedimage, etc.
//
//        params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder(mapRequestURI));
//
//        TiledImageLayer layer = new BasicTiledImageLayer(new LevelSet(params));
//        layer.setUseTransparentTextures(true);
////        layer.setShowImageTileOutlines(true);
////        layer.setDrawTileIDs(true);
//
//        String layerTitle = params.getStringValue(AVKey.TITLE);
//        layer.setName(layerTitle != null ? layerTitle : layerNames + " : " + styleNames);
//
//        return layer;
//    }
//
//private static String extractWMSParameter(String paramName, URI uri)
//{
//    String qs = uri.getRawQuery();
//    if (qs == null)
//        return null;
//
//    String[] wmsParams = qs.split("&");
//    for (String wmsParam : wmsParams)
//    {
//        if (wmsParam.toLowerCase().startsWith(paramName))
//        {
//            String[] av = wmsParam.split("=");
//            if (av.length != 2 || av[1] == null)
//                return null;
//
//            return av[1];
//        }
//    }
//
//    return null;
//}
