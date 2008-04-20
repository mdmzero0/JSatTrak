/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.placename;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.pick.PlaceName;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.WWIcon;
import gov.nasa.worldwind.retrieve.HTTPRetriever;
import gov.nasa.worldwind.retrieve.RetrievalPostProcessor;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

/**
 * @author Paul Collins
 * @version $Id: PlaceNameLayer.java 3558 2007-11-17 08:36:45Z tgaskins $
 */
public class PlaceNameLayer extends AbstractLayer
{
    private final PlaceNameServiceSet placeNameServiceSet;
    private final List<Tile[]> tiles = new ArrayList<Tile[]>();
    private final MemoryCache memoryCache;

    /**
     * @param placeNameServiceSet the set of PlaceNameService objects that PlaceNameLayer will render.
     * @throws IllegalArgumentException if <code>placeNameServiceSet</code> is null
     */
    public PlaceNameLayer(PlaceNameServiceSet placeNameServiceSet)
    {
        if (placeNameServiceSet == null)
        {
            String message = Logging.getMessage("nullValue.PlaceNameServiceSetIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        String cacheName = Tile.class.getName();
        if (WorldWind.getMemoryCacheSet().containsCache(cacheName))
        {
            this.memoryCache = WorldWind.getMemoryCache(cacheName);
        }
        else
        {
            long size = Configuration.getLongValue(AVKey.PLACENAME_LAYER_CACHE_SIZE, 2000000L);
            this.memoryCache = new BasicMemoryCache((long) (0.85 * size), size);
            this.memoryCache.setName("Placenames");
            WorldWind.getMemoryCacheSet().addCache(cacheName, this.memoryCache);
        }

        this.placeNameServiceSet = placeNameServiceSet.deepCopy();
        for (int i = 0; i < this.placeNameServiceSet.getServiceCount(); i++)
        {
            tiles.add(i, buildTiles(this.placeNameServiceSet.getService(i)));
        }
    }

    public final PlaceNameServiceSet getPlaceNameServiceSet()
    {
        return this.placeNameServiceSet;
    }

    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //

    private static class Tile
    {
        final PlaceNameService placeNameService;
        final Sector sector;
        final int row;
        final int column;
        final int hash;
        // Computed data.
        String fileCachePath = null;
        Extent extent = null;
        double extentVerticalExaggeration = Double.MIN_VALUE;

        static int computeRow(Angle delta, Angle latitude)
        {
            if (delta == null || latitude == null)
            {
                String msg = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return (int) ((latitude.getDegrees() + 90d) / delta.getDegrees());
        }

        static int computeColumn(Angle delta, Angle longitude)
        {
            if (delta == null || longitude == null)
            {
                String msg = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return (int) ((longitude.getDegrees() + 180d) / delta.getDegrees());
        }

        static Angle computeRowLatitude(int row, Angle delta)
        {
            if (delta == null)
            {
                String msg = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return Angle.fromDegrees(-90d + delta.getDegrees() * row);
        }

        static Angle computeColumnLongitude(int column, Angle delta)
        {
            if (delta == null)
            {
                String msg = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }
            return Angle.fromDegrees(-180 + delta.getDegrees() * column);
        }

        Tile(PlaceNameService placeNameService, Sector sector, int row, int column)
        {
            this.placeNameService = placeNameService;
            this.sector = sector;
            this.row = row;
            this.column = column;
            this.hash = this.computeHash();
        }

        int computeHash()
        {
            return this.getFileCachePath() != null ? this.getFileCachePath().hashCode() : 0;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            final Tile other = (Tile) o;

            return this.getFileCachePath() != null ? !this.getFileCachePath().equals(other.getFileCachePath()) :
                other.getFileCachePath() != null;
        }

        Extent getExtent(DrawContext dc)
        {
            if (dc == null)
            {
                String message = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().fine(message);
                throw new IllegalArgumentException(message);
            }

            if (this.extent == null || this.extentVerticalExaggeration != dc.getVerticalExaggeration())
            {
                this.extentVerticalExaggeration = dc.getVerticalExaggeration();
                this.extent = dc.getGlobe().computeBoundingCylinder(this.extentVerticalExaggeration,
                    this.sector);
            }

            return extent;
        }

        String getFileCachePath()
        {
            if (this.fileCachePath == null)
                this.fileCachePath = this.placeNameService.createFileCachePathFromTile(this.row, this.column);

            return this.fileCachePath;
        }

        PlaceNameService getPlaceNameService()
        {
            return placeNameService;
        }

        java.net.URL getRequestURL() throws java.net.MalformedURLException
        {
            return this.placeNameService.createServiceURLFromSector(this.sector);
        }

        Sector getSector()
        {
            return sector;
        }

        public int hashCode()
        {
            return this.hash;
        }
    }

    private Tile[] buildTiles(PlaceNameService placeNameService)
    {
        final Sector sector = placeNameService.getSector();
        final Angle dLat = placeNameService.getTileDelta().getLatitude();
        final Angle dLon = placeNameService.getTileDelta().getLongitude();

        // Determine the row and column offset from the global tiling origin for the southwest tile corner
        int firstRow = Tile.computeRow(dLat, sector.getMinLatitude());
        int firstCol = Tile.computeColumn(dLon, sector.getMinLongitude());
        int lastRow = Tile.computeRow(dLat, sector.getMaxLatitude().subtract(dLat));
        int lastCol = Tile.computeColumn(dLon, sector.getMaxLongitude().subtract(dLon));

        int nLatTiles = lastRow - firstRow + 1;
        int nLonTiles = lastCol - firstCol + 1;

        Tile[] tiles = new Tile[nLatTiles * nLonTiles];

        Angle p1 = Tile.computeRowLatitude(firstRow, dLat);
        for (int row = firstRow; row <= lastRow; row++)
        {
            Angle p2;
            p2 = p1.add(dLat);

            Angle t1 = Tile.computeColumnLongitude(firstCol, dLon);
            for (int col = firstCol; col <= lastCol; col++)
            {
                Angle t2;
                t2 = t1.add(dLon);

                tiles[col + row * nLonTiles] = new Tile(placeNameService, new Sector(p1, p2, t1, t2), row, col);
                t1 = t2;
            }
            p1 = p2;
        }

        return tiles;
    }

    // ============== Place Name Data Structures ======================= //
    // ============== Place Name Data Structures ======================= //
    // ============== Place Name Data Structures ======================= //

    private static class PlaceNameChunk implements Cacheable
    {
        final PlaceNameService placeNameService;
        final CharBuffer textArray;
        final int[] textIndexArray;
        final double[] latlonArray;
        final int numEntries;
        final long estimatedMemorySize;

        PlaceNameChunk(PlaceNameService service, CharBuffer text, int[] textIndices,
            double[] positions, int numEntries)
        {
            this.placeNameService = service;
            this.textArray = text;
            this.textIndexArray = textIndices;
            this.latlonArray = positions;
            this.numEntries = numEntries;
            this.estimatedMemorySize = this.computeEstimatedMemorySize();
        }

        long computeEstimatedMemorySize()
        {
            long result = 0;
            result += BufferUtil.SIZEOF_SHORT * textArray.capacity();
            result += BufferUtil.SIZEOF_INT * textIndexArray.length;
            result += BufferUtil.SIZEOF_DOUBLE * latlonArray.length;
            return result;
        }

        Position getPosition(int index)
        {
            int latlonIndex = 2 * index;
            return Position.fromDegrees(latlonArray[latlonIndex], latlonArray[latlonIndex + 1], 0);
        }

        PlaceNameService getPlaceNameService()
        {
            return this.placeNameService;
        }

        CharSequence getText(int index)
        {
            int beginIndex = textIndexArray[index];
            int endIndex = (index + 1 < numEntries) ? textIndexArray[index + 1] : textArray.length();
            return this.textArray.subSequence(beginIndex, endIndex);
        }

        public long getSizeInBytes()
        {
            return this.estimatedMemorySize;
        }

        Iterator<PlaceName> createRenderIterator(final DrawContext dc)
        {
            return new Iterator<PlaceName>()
            {
                PlaceNameImpl placeNameProxy = new PlaceNameImpl();
                int index = -1;

                public boolean hasNext()
                {
                    return index < (PlaceNameChunk.this.numEntries - 1);
                }

                public PlaceName next()
                {
                    if (!hasNext())
                        throw new NoSuchElementException();
                    this.updateProxy(placeNameProxy, ++index);
                    return placeNameProxy;
                }

                public void remove()
                {
                    throw new UnsupportedOperationException();
                }

                void updateProxy(PlaceNameImpl proxy, int index)
                {
                    proxy.text = getText(index);
                    proxy.position = getPosition(index);
                    proxy.font = placeNameService.getFont();
                    proxy.color = placeNameService.getColor();
                    proxy.visible = isNameVisible(dc, placeNameService, proxy.position);
                }
            };
        }
    }

    private static class PlaceNameImpl implements PlaceName
    {
        CharSequence text;
        Position position;
        Font font;
        Color color;
        boolean visible;

        PlaceNameImpl()
        {
        }

        public CharSequence getText()
        {
            return this.text;
        }

        public void setText(CharSequence text)
        {
            throw new UnsupportedOperationException();
        }

        public Position getPosition()
        {
            return this.position;
        }

        public void setPosition(Position position)
        {
            throw new UnsupportedOperationException();
        }

        public Font getFont()
        {
            return this.font;
        }

        public void setFont(Font font)
        {
            throw new UnsupportedOperationException();
        }

        public Color getColor()
        {
            return this.color;
        }

        public void setColor(Color color)
        {
            throw new UnsupportedOperationException();
        }

        public boolean isVisible()
        {
            return this.visible;
        }

        public void setVisible(boolean visible)
        {
            throw new UnsupportedOperationException();
        }

        public WWIcon getIcon()
        {
            return null;
        }

        public void setIcon(WWIcon icon)
        {
            throw new UnsupportedOperationException();
        }
    }

    // ============== Rendering ======================= //
    // ============== Rendering ======================= //
    // ============== Rendering ======================= //

    private final PlaceNameRenderer placeNameRenderer = new PlaceNameRenderer();

    @Override
    public void dispose() // override if disposal is a supported operation
    {
        super.dispose();

        if (this.placeNameRenderer != null)
            this.placeNameRenderer.dispose();
    }

    @Override
    protected void doRender(DrawContext dc)
    {
        boolean enableDepthTest = this.isEnableDepthTest(dc);

        int serviceCount = this.placeNameServiceSet.getServiceCount();
        for (int i = 0; i < serviceCount; i++)
        {
            PlaceNameService placeNameService = this.placeNameServiceSet.getService(i);
            if (!isServiceVisible(dc, placeNameService))
                continue;

            double minDist = placeNameService.getMinDisplayDistance();
            double maxDist = placeNameService.getMaxDisplayDistance();
            double minDistSquared = minDist * minDist;
            double maxDistSquared = maxDist * maxDist;

            Tile[] tiles = this.tiles.get(i);
            for (Tile tile : tiles)
            {
                try
                {
                    drawOrRequestTile(dc, tile, minDistSquared, maxDistSquared, enableDepthTest);
                }
                catch (Exception e)
                {
                    Logging.logger().log(Level.FINE, Logging.getMessage("layers.PlaceNameLayer.ExceptionRenderingTile"),
                        e);
                }
            }
        }

        this.sendRequests();
    }

    private boolean isEnableDepthTest(DrawContext dc)
    {
        Position eyePos = dc.getView().getEyePosition();
        if (eyePos == null)
            return false;

        double altitude = eyePos.getElevation();
        return altitude < (dc.getVerticalExaggeration() * dc.getGlobe().getMaxElevation());
    }

    private void drawOrRequestTile(DrawContext dc, Tile tile, double minDisplayDistanceSquared,
        double maxDisplayDistanceSquared, boolean enableDepthTest)
    {
        if (!isTileVisible(dc, tile, minDisplayDistanceSquared, maxDisplayDistanceSquared))
            return;

        Object cacheObj = this.memoryCache.getObject(tile);
        if (cacheObj == null)
        {
            this.requestTile(this.readQueue, tile);
            return;
        }

        if (!(cacheObj instanceof PlaceNameChunk))
            return;

        PlaceNameChunk placeNameChunk = (PlaceNameChunk) cacheObj;
        Iterator<PlaceName> renderIter = placeNameChunk.createRenderIterator(dc);
        placeNameRenderer.render(dc, renderIter, enableDepthTest);
    }

    private static boolean isServiceVisible(DrawContext dc, PlaceNameService placeNameService)
    {
        if (!placeNameService.isEnabled())
            return false;
        //noinspection SimplifiableIfStatement
        if (dc.getVisibleSector() != null && !placeNameService.getSector().intersects(dc.getVisibleSector()))
            return false;

        return placeNameService.getExtent(dc).intersects(dc.getView().getFrustumInModelCoordinates());
    }

    private static boolean isTileVisible(DrawContext dc, Tile tile, double minDistanceSquared,
        double maxDistanceSquared)
    {
        if (!tile.getSector().intersects(dc.getVisibleSector()))
            return false;

        View view = dc.getView();
        Position eyePos = view.getEyePosition();
        if (eyePos == null)
            return false;

        Angle lat = clampAngle(eyePos.getLatitude(), tile.getSector().getMinLatitude(),
            tile.getSector().getMaxLatitude());
        Angle lon = clampAngle(eyePos.getLongitude(), tile.getSector().getMinLongitude(),
            tile.getSector().getMaxLongitude());
        Vec4 p = dc.getGlobe().computePointFromPosition(lat, lon, 0d);
        double distSquared = dc.getView().getEyePoint().distanceToSquared3(p);
        //noinspection RedundantIfStatement
        if (minDistanceSquared > distSquared || maxDistanceSquared < distSquared)
            return false;

        return true;
    }

    private static boolean isNameVisible(DrawContext dc, PlaceNameService service, Position namePosition)
    {
        double elevation = dc.getVerticalExaggeration() * namePosition.getElevation();
        Vec4 namePoint = dc.getGlobe().computePointFromPosition(namePosition.getLatitude(),
            namePosition.getLongitude(), elevation);
        Vec4 eyeVec = dc.getView().getEyePoint();

        double dist = eyeVec.distanceTo3(namePoint);
        return dist >= service.getMinDisplayDistance() && dist <= service.getMaxDisplayDistance();
    }

    private static Angle clampAngle(Angle a, Angle min, Angle max)
    {
        return a.compareTo(min) < 0 ? min : (a.compareTo(max) > 0 ? max : a);
    }

    // ============== Image Reading and Downloading ======================= //
    // ============== Image Reading and Downloading ======================= //
    // ============== Image Reading and Downloading ======================= //

    private static final int MAX_REQUESTS = 64;
    private final Queue<Tile> downloadQueue = new LinkedBlockingQueue<Tile>(MAX_REQUESTS);
    private final Queue<Tile> readQueue = new LinkedBlockingQueue<Tile>(MAX_REQUESTS);

    private static class RequestTask implements Runnable
    {
        final PlaceNameLayer layer;
        final Tile tile;

        RequestTask(PlaceNameLayer layer, Tile tile)
        {
            this.layer = layer;
            this.tile = tile;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            final RequestTask other = (RequestTask) o;

            // Don't include layer in comparison so that requests are shared among layers
            return !(this.tile != null ? !this.tile.equals(other.tile) : other.tile != null);
        }

        @Override
        public int hashCode()
        {
            return (this.tile != null ? this.tile.hashCode() : 0);
        }

        public void run()
        {
            synchronized (tile)
            {
                if (this.layer.memoryCache.getObject(this.tile) != null)
                    return;

                final java.net.URL tileURL = WorldWind.getDataFileCache().findFile(tile.getFileCachePath(), false);
                if (tileURL != null)
                {
                    if (this.layer.loadTile(this.tile, tileURL))
                    {
                        tile.getPlaceNameService().unmarkResourceAbsent(tile.getPlaceNameService().getTileNumber(
                            tile.row,
                            tile.column));
                        this.layer.firePropertyChange(AVKey.LAYER, null, this);
                    }
                    else
                    {
                        // Assume that something's wrong with the file and delete it.
                        WorldWind.getDataFileCache().removeFile(tileURL);
                        tile.getPlaceNameService().markResourceAbsent(tile.getPlaceNameService().getTileNumber(tile.row,
                            tile.column));
                        String message = Logging.getMessage("generic.DeletedCorruptDataFile", tileURL);
                        Logging.logger().fine(message);
                    }
                    return;
                }
            }

            this.layer.requestTile(this.layer.downloadQueue, this.tile);
        }

        public String toString()
        {
            return this.tile.toString();
        }
    }

    private static class DownloadPostProcessor implements RetrievalPostProcessor
    {
        final PlaceNameLayer layer;
        final Tile tile;

        private DownloadPostProcessor(PlaceNameLayer layer, Tile tile)
        {
            this.layer = layer;
            this.tile = tile;
        }

        public java.nio.ByteBuffer run(Retriever retriever)
        {
            if (retriever == null)
            {
                String msg = Logging.getMessage("nullValue.RetrieverIsNull");
                Logging.logger().fine(msg);
                throw new IllegalArgumentException(msg);
            }

            if (!retriever.getState().equals(Retriever.RETRIEVER_STATE_SUCCESSFUL))
                return null;

            if (!(retriever instanceof URLRetriever))
                return null;

            try
            {
                if (retriever instanceof HTTPRetriever)
                {
                    HTTPRetriever httpRetriever = (HTTPRetriever) retriever;
                    if (httpRetriever.getResponseCode() == java.net.HttpURLConnection.HTTP_NO_CONTENT)
                    {
                        // Mark tile as missing to avoid further attempts
                        tile.getPlaceNameService().markResourceAbsent(tile.getPlaceNameService().getTileNumber(tile.row,
                            tile.column));
                        return null;
                    }
                }

                URLRetriever urlRetriever = (URLRetriever) retriever;
                java.nio.ByteBuffer buffer = urlRetriever.getBuffer();

                synchronized (tile)
                {
                    final java.io.File cacheFile = WorldWind.getDataFileCache().newFile(this.tile.getFileCachePath());
                    if (cacheFile == null)
                        return null;

                    if (cacheFile.exists())
                        return buffer; // info is already here; don't need to do anything

                    if (buffer != null)
                    {
                        WWIO.saveBuffer(buffer, cacheFile);
                        this.layer.firePropertyChange(AVKey.LAYER, null, this);
                        return buffer;
                    }
                }
            }
            catch (java.io.IOException e)
            {
                Logging.logger().log(Level.FINE, Logging.getMessage(
                    "layers.PlaceNameLayer.ExceptionSavingRetrievedFile", this.tile.getFileCachePath()), e);
            }

            return null;
        }
    }

    private static class GMLPlaceNameSAXHandler extends org.xml.sax.helpers.DefaultHandler
    {
        static final String GML_FEATURE_MEMBER = "gml:featureMember";
        static final String TOPP_FULL_NAME_ND = "topp:full_name_nd";
        static final String TOPP_LATITUDE = "topp:latitude";
        static final String TOPP_LONGITUDE = "topp:longitude";
        final LinkedList<String> internedQNameStack = new LinkedList<String>();
        boolean inBeginEndPair = false;
        StringBuilder latBuffer = new StringBuilder();
        StringBuilder lonBuffer = new StringBuilder();

        StringBuilder textArray = new StringBuilder();
        int[] textIndexArray = new int[16];
        double[] latlonArray = new double[16];
        int numEntries = 0;

        GMLPlaceNameSAXHandler()
        {
        }

        PlaceNameChunk createPlaceNameChunk(PlaceNameService service)
        {
            int numChars = this.textArray.length();
            CharBuffer textBuffer = ByteBuffer.allocateDirect(numChars * Character.SIZE / 8).asCharBuffer();
            textBuffer.put(this.textArray.toString());
            textBuffer.rewind();
            return new PlaceNameChunk(service, textBuffer, this.textIndexArray, this.latlonArray, this.numEntries);
        }

        void beginEntry()
        {
            int textIndex = this.textArray.length();
            this.textIndexArray = append(this.textIndexArray, this.numEntries, textIndex);
            this.inBeginEndPair = true;
        }

        void endEntry()
        {
            double lat = this.parseDouble(this.latBuffer);
            double lon = this.parseDouble(this.lonBuffer);
            int numLatLon = 2 * this.numEntries;
            this.latlonArray = this.append(this.latlonArray, numLatLon, lat);
            numLatLon++;
            this.latlonArray = this.append(this.latlonArray, numLatLon, lon);

            this.latBuffer.delete(0, this.latBuffer.length());
            this.lonBuffer.delete(0, this.lonBuffer.length());
            this.inBeginEndPair = false;
            this.numEntries++;
        }

        double parseDouble(StringBuilder sb)
        {
            double value = 0;
            try
            {
                value = Double.parseDouble(sb.toString());
            }
            catch (NumberFormatException e)
            {
                Logging.logger().log(Level.FINE,
                    Logging.getMessage("layers.PlaceNameLayer.ExceptionAttemptingToReadFile", ""), e);
            }
            return value;
        }

        int[] append(int[] array, int index, int value)
        {
            if (index >= array.length)
                array = this.resizeArray(array);
            array[index] = value;
            return array;
        }

        int[] resizeArray(int[] oldArray)
        {
            int newSize = 2 * oldArray.length;
            int[] newArray = new int[newSize];
            System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
            return newArray;
        }

        double[] append(double[] array, int index, double value)
        {
            if (index >= array.length)
                array = this.resizeArray(array);
            array[index] = value;
            return array;
        }

        double[] resizeArray(double[] oldArray)
        {
            int newSize = 2 * oldArray.length;
            double[] newArray = new double[newSize];
            System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
            return newArray;
        }

        public void characters(char ch[], int start, int length)
        {
            if (!this.inBeginEndPair)
                return;

            // Top of QName stack is an interned string,
            // so we can use pointer comparison.
            String internedTopQName = this.internedQNameStack.getFirst();

            StringBuilder sb = null;
            if (TOPP_LATITUDE == internedTopQName)
                sb = this.latBuffer;
            else if (TOPP_LONGITUDE == internedTopQName)
                sb = this.lonBuffer;
            else if (TOPP_FULL_NAME_ND == internedTopQName)
                sb = this.textArray;

            if (sb != null)
                sb.append(ch, start, length);
        }

        public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes attributes)
        {
            // Don't validate uri, localName or attributes because they aren't used.
            // Intern the qName string so we can use pointer comparison.
            String internedQName = qName.intern();
            if (GML_FEATURE_MEMBER == internedQName)
                this.beginEntry();
            this.internedQNameStack.addFirst(internedQName);
        }

        public void endElement(String uri, String localName, String qName)
        {
            // Don't validate uri or localName because they aren't used.
            // Intern the qName string so we can use pointer comparison.
            String internedQName = qName.intern();
            if (GML_FEATURE_MEMBER == internedQName)
                this.endEntry();
            this.internedQNameStack.removeFirst();
        }
    }

    private boolean loadTile(Tile tile, java.net.URL url)
    {
        PlaceNameChunk placeNameChunk = readTile(tile, url);
        if (placeNameChunk == null)
            return false;

        this.memoryCache.add(tile, placeNameChunk);
        return true;
    }

    private static PlaceNameChunk readTile(Tile tile, java.net.URL url)
    {
        java.io.InputStream is = null;

        try
        {
            String path = url.getFile();
            path = path.replaceAll("%20", " "); // TODO: find a better way to get a path usable by FileInputStream

            java.io.FileInputStream fis = new java.io.FileInputStream(path);
            java.io.BufferedInputStream buf = new java.io.BufferedInputStream(fis);
            is = new java.util.zip.GZIPInputStream(buf);

            GMLPlaceNameSAXHandler handler = new GMLPlaceNameSAXHandler();
            javax.xml.parsers.SAXParserFactory.newInstance().newSAXParser().parse(is, handler);
            return handler.createPlaceNameChunk(tile.getPlaceNameService());
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.FINE,
                Logging.getMessage("layers.PlaceNameLayer.ExceptionAttemptingToReadFile", url.toString()), e);
        }
        finally
        {
            try
            {
                if (is != null)
                    is.close();
            }
            catch (java.io.IOException e)
            {
                Logging.logger().log(Level.FINE,
                    Logging.getMessage("layers.PlaceNameLayer.ExceptionAttemptingToReadFile", url.toString()), e);
            }
        }

        return null;
    }

    private void requestTile(Queue<Tile> queue, Tile tile)
    {
        if (tile.getPlaceNameService().isResourceAbsent(tile.getPlaceNameService().getTileNumber(
            tile.row, tile.column)))
            return;

        if (!queue.contains(tile))
            queue.offer(tile);
    }

    private void sendRequests()
    {
        Tile tile;
        // Send threaded read tasks.
        while (!WorldWind.getTaskService().isFull() && (tile = this.readQueue.poll()) != null)
        {
            WorldWind.getTaskService().addTask(new RequestTask(this, tile));
        }
        // Send retriever tasks.
        while (WorldWind.getRetrievalService().isAvailable() && (tile = this.downloadQueue.poll()) != null)
        {
            java.net.URL url;
            try
            {
                url = tile.getRequestURL();
                if (WorldWind.getNetworkStatus().isHostUnavailable(url))
                    return;
            }
            catch (java.net.MalformedURLException e)
            {
                String message = Logging.getMessage("layers.PlaceNameLayer.ExceptionAttemptingToDownloadFile", tile);
                Logging.logger().log(Level.FINE, message, e);
                return;
            }
            WorldWind.getRetrievalService().runRetriever(new HTTPRetriever(url, new DownloadPostProcessor(this, tile)));
        }
    }
}
