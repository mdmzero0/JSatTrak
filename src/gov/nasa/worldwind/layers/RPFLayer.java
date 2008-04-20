/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.*;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.formats.rpf.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.retrieve.Retriever;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.OrbitView;

import javax.media.opengl.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.*;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.locks.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id$
 */
public class RPFLayer extends AbstractLayer
{
    public static final int DEFAULT_WINDOW = 8;
    public static final double DEFAULT_DRAW_FRAME_THRESHOLD = 0.75;
    public static final double DEFAULT_DRAW_ICON_THRESHOLD = 0.25;
    public static final long IMAGE_CACHE_SIZE = 8L * 1024 * 1024;
//    public static final long DEFAULT_CACHE_LO_WATER = 192L * 1024L * 1024L;
//    public static final long DEFAULT_CACHE_HI_WATER = 256L * 1024L * 1024L;
    private static final long FRAME_EXPIRY_TIME = new GregorianCalendar(2007, 9, 1).getTimeInMillis();
    private final RPFDataSeries dataSeries;
    private int latitudeWindow;
    private int longitudeWindow;
    private double drawFrameThreshold;
    private double drawIconThreshold;

//    private final MemoryCache memoryCache;
    private final String cachePathBase = "Earth/RPF/";

    public RPFLayer(RPFDataSeries dataSeries)
    {
        if (dataSeries == null)
        {
            String message = Logging.getMessage("nullValue.RPFDataSeriesIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        this.dataSeries = dataSeries;
        this.latitudeWindow = DEFAULT_WINDOW;
        this.longitudeWindow = DEFAULT_WINDOW;
        this.drawFrameThreshold = DEFAULT_DRAW_FRAME_THRESHOLD;
        this.drawIconThreshold = DEFAULT_DRAW_ICON_THRESHOLD;
        // Initialize the MemoryCache.
        if (!WorldWind.getMemoryCacheSet().containsCache(BasicSurfaceTile.class.getName()))
        {
            long size = IMAGE_CACHE_SIZE;
            MemoryCache cache = new BasicMemoryCache((long) (0.85 * size), size);
            cache.setName("RPF Frame Tiles");            
            WorldWind.getMemoryCacheSet().addCache(BasicSurfaceTile.class.getName(), cache);
        }
//        this.memoryCache = new BasicMemoryCache(DEFAULT_CACHE_LO_WATER, DEFAULT_CACHE_HI_WATER);
//        this.memoryCache.addCacheListener(new MemoryCache.CacheListener()
//        {
//            public void entryRemoved(Object key, Object clientObject)
//            {
//                if (clientObject == null || !(clientObject instanceof RPFTextureTile))
//                    return;
//                RPFTextureTile rpfTextureTile = (RPFTextureTile) clientObject;
//                disposalQueue.offer(rpfTextureTile);
//            }
//        });
    }

    public int getLatitudeWindow()
    {
        return latitudeWindow;
    }

    public void setLatitudeWindow(int latitudeWindow)
    {
        this.latitudeWindow = latitudeWindow;
    }

    public int getLongitudeWindow()
    {
        return longitudeWindow;
    }

    public void setLongitudeWindow(int longitudeWindow)
    {
        this.longitudeWindow = longitudeWindow;
    }

    public double getDrawFrameThreshold()
    {
        return drawFrameThreshold;
    }

    public void setDrawFrameThreshold(double drawFrameThreshold)
    {
        this.drawFrameThreshold = drawFrameThreshold;
    }

    public double getDrawIconThreshold()
    {
        return drawIconThreshold;
    }

    public void setDrawIconThreshold(double drawIconThreshold)
    {
        this.drawIconThreshold = drawIconThreshold;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(this.dataSeries.seriesCode);
        sb.append(": ");
        sb.append(this.dataSeries.dataSeries);
        return sb.toString();
    }

    // ============== Frame Directory ======================= //
    // ============== Frame Directory ======================= //
    // ============== Frame Directory ======================= //

    private final Map<FrameKey, FrameRecord> frameDirectory = new HashMap<FrameKey, FrameRecord>();
    private Sector sector = Sector.EMPTY_SECTOR;
    private int modCount = 0;
    private int lastModCount = 0;

    private static class FrameKey
    {
        public final String dataSeriesCode;
        public final char zoneCode;
        public final int frameNumber;
        private final int hashCode;

        public FrameKey(String dataSeriesCode, char zoneCode, int frameNumber)
        {
            this.dataSeriesCode = dataSeriesCode;
            this.zoneCode = zoneCode;
            this.frameNumber = frameNumber;
            this.hashCode = this.computeHash();
        }

        private int computeHash()
        {
            int hash;
            hash = this.dataSeriesCode.hashCode();
            hash = 31 * hash + (int) this.zoneCode;
            hash = 29 * hash + this.frameNumber;
            return hash;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || !o.getClass().equals(this.getClass()))
                return false;

            final FrameKey that = (FrameKey) o;
            return (this.dataSeriesCode.equalsIgnoreCase(that.dataSeriesCode)
                    && Character.toUpperCase(this.zoneCode) == Character.toUpperCase(that.zoneCode))
                    && (this.frameNumber == that.frameNumber);
        }

        public int hashCode()
        {
            return this.hashCode;
        }
    }

    private static class FrameRecord
    {
        private final String filePath;
        private final RPFFrameFilename frameFilename;
        private Sector sector;
        private String cacheFilePath;
        private final RPFLayer layer;
        final Lock fileLock = new ReentrantLock();

        public FrameRecord(File file, RPFLayer layer)
        {
            String fileName = file.getName().toUpperCase();
            this.filePath = file.getAbsolutePath();
            this.frameFilename = RPFFrameFilename.parseFilename(fileName);
            this.layer = layer;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || !o.getClass().equals(this.getClass()))
                return false;
            final FrameRecord that = (FrameRecord) o;
            return this.filePath.equals(that.filePath);
        }

        public String getCacheFilePath()
        {
            if (this.cacheFilePath == null)
            {
                this.cacheFilePath = this.layer.cachePathFor(this.frameFilename);
            }
            return this.cacheFilePath;
        }

        public long getExpiryTime()
        {
            return FRAME_EXPIRY_TIME;
        }

        public final String getFilePath()
        {
            return this.filePath;
        }

        public final RPFFrameFilename getRPFFrameProperties()
        {
            return this.frameFilename;
        }

        private static final Sector NULL_SECTOR = new Sector(Angle.ZERO, Angle.ZERO, Angle.ZERO, Angle.ZERO);

        public Sector getSector()
        {
            // Sectors 9 and J will return null, we workaround this temporary problem
            // by assigning a unique value to the FrameRecord sector when its sector
            // cannot be computed.
            if (this.sector == null)
            {
                Sector sector = this.layer.sectorFor(this.frameFilename);
                this.sector = (sector != null) ? sector : NULL_SECTOR;
            }

            // The unique value is translated back to "null" to avoid complications outside this code.
            return (this.sector != NULL_SECTOR) ? this.sector : null;
        }
    }

    public int addFrame(File file)
    {
        if (file == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        FrameRecord record = null;
        try
        {
            record = new FrameRecord(file, this);
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.FINE, Logging.getMessage("layers.RPFLayer.ExceptionParsingFileName", file), e);
        }

        if (record != null && this.dataSeries.seriesCode.equalsIgnoreCase(
            record.getRPFFrameProperties().getDataSeriesCode()))
        {
            this.addRecord(record);
            return 1;
        }
        return 0;
    }

    private void addRecord(FrameRecord record)
    {
        FrameKey key = keyFor(record);
        this.frameDirectory.put(key, record);
        ++this.modCount;
    }

    private String cachePathFor(RPFFrameFilename frameFilename)
    {
        StringBuilder sb = new StringBuilder(cachePathBase);
        sb.append(frameFilename.getDataSeriesCode()).append(File.separatorChar);
        sb.append(frameFilename.getZoneCode()).append(File.separatorChar);
        sb.append(frameFilename.getFilename());
        sb.append(".").append(TextureIO.DDS);
        return sb.toString();
    }

    private static FrameKey keyFor(FrameRecord record)
    {
        RPFFrameFilename frameFilename = record.getRPFFrameProperties();
        return new FrameKey(frameFilename.getDataSeriesCode(), frameFilename.getZoneCode(), frameFilename.getFrameNumber());
    }

    public int removeFrameFile(File file)
    {
        if (file == null)
            return 0;
        RPFFrameFilename filename = null;
        try
        {
            filename = RPFFrameFilename.parseFilename(file.getName().toUpperCase());
        }
        catch (IllegalArgumentException e)
        {
            Logging.logger().log(Level.FINE, Logging.getMessage("layers.RPFLayer.ExceptionParsingFileName", file), e);
        }
        return removeFrame(filename);
    }

    private int removeFrame(RPFFrameFilename filename)
    {
        if (filename == null)
            return 0;
        if (this.removeKey(new FrameKey(filename.getDataSeriesCode(), filename.getZoneCode(), filename.getFrameNumber())))
            return 1;
        return 0;
    }

    private boolean removeKey(FrameKey key)
    {
        FrameRecord value = this.frameDirectory.remove(key);
        --this.modCount;
        return value != null;
    }

    private Sector sectorFor(RPFFrameFilename filename)
    {
        if (filename == null)
            return null;

        RPFFrameTransform frameTransform = RPFFrameTransform.createFrameTransform(filename.getZoneCode(),
            this.dataSeries.rpfDataType, this.dataSeries.scaleOrGSD);
        if (frameTransform == null)
            return null;

        if (filename.getFrameNumber() < 0 || filename.getFrameNumber() > (frameTransform.getMaximumFrameNumber() - 1))
            return null;

        return frameTransform.frameExtent(filename.getFrameNumber());
    }

    private void updateSector()
    {
        Sector newSector = null;
        for (FrameRecord record : this.frameDirectory.values())
        {
            if (record.getSector() != null)
                newSector = (newSector != null) ? newSector.union(record.getSector()) : record.getSector();
        }
        this.sector = newSector;
    }

    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //
    // ============== Tile Assembly ======================= //

//    private void assembleTiles(DrawContext dc, Map<FrameKey, RPFTextureTile> cachedTiles,
//        Queue<RPFTextureTile> tilesToRender)
//    {
//        synchronized (cachedTiles)
//        {
//            for (RPFTextureTile rpfTextureTile : cachedTiles.values())
//            {
//                if (this.isSectorVisible(dc, rpfTextureTile.getSector()))
//                    tilesToRender.offer(rpfTextureTile);
//            }
//        }
//    }

    private void assembleTiles(DrawContext dc, RPFDataSeries dataSeries, Sector viewingSector,
        Queue<SurfaceTile> framesToRender, Queue<FrameRecord> framesToRequest)
    {
        Iterable<RPFZone> zonesInSector = RPFZone.zonesInSector(viewingSector);
        if (zonesInSector == null)
            return;

        for (RPFZone zone : zonesInSector)
        {
            assembleTilesForZone(dc, zone, dataSeries, viewingSector, framesToRender, framesToRequest);
        }
    }

    private void assembleTilesForZone(DrawContext dc, RPFZone zone, RPFDataSeries dataSeries, Sector sector,
        Queue<SurfaceTile> framesToRender, Queue<FrameRecord> framesToRequest)
    {
        RPFFrameTransform frameTransform =
            RPFFrameTransform.createFrameTransform(zone.zoneCode, dataSeries.rpfDataType, dataSeries.scaleOrGSD);
        if (frameTransform == null)
            return;

        Iterable<Integer> framesInSector = frameTransform.framesInSector(sector);
        if (framesInSector == null)
            return;

        for (Integer frameNumber : framesInSector)
        {
            FrameKey key = new FrameKey(this.dataSeries.seriesCode, zone.zoneCode, frameNumber);
            BasicSurfaceTile tile = this.getTile(key);
            if (tile != null && tile.isTextureInMemory(dc.getTextureCache()))
            {
                framesToRender.offer(tile);
            }
            else
            {
                FrameRecord record = this.frameDirectory.get(key);
                if (record != null && record.sector != null)
                {
                    framesToRequest.offer(record);
                }
            }
        }
    }

    private static Sector[] normalizeSector(Sector sector)
    {
        Angle minLat = clampAngle(sector.getMinLatitude(), Angle.NEG90, Angle.POS90);
        Angle maxLat = clampAngle(sector.getMaxLatitude(), Angle.NEG90, Angle.POS90);
        if (maxLat.degrees < minLat.degrees)
        {
            Angle tmp = minLat;
            minLat = maxLat;
            maxLat = tmp;
        }

        Angle minLon = Angle.normalizedLongitude(
            sector.getMinLongitude());//normalizeAngle(sector.getMinLongitude(), Angle.NEG180, Angle.POS180);
        Angle maxLon = Angle.normalizedLongitude(
            sector.getMaxLongitude());//normalizeAngle(sector.getMaxLongitude(), Angle.NEG180, Angle.POS180);
        if (maxLon.degrees < minLon.degrees)
        {
            return new Sector[] {
                new Sector(minLat, maxLat, minLon, Angle.POS180),
                new Sector(minLat, maxLat, Angle.NEG180, maxLon),
            };
        }

        return new Sector[] {new Sector(minLat, maxLat, minLon, maxLon)};
    }

    private static Sector createViewSector(RPFDataSeries dataSeries,
        Angle centerLat, Angle centerLon, int latWindowFrames, int lonWindowFrames)
    {
        RPFFrameTransform frameTransform = RPFFrameTransform.createFrameTransform(RPFZone.ZONE_1.zoneCode,
            dataSeries.rpfDataType, dataSeries.scaleOrGSD);
        if (frameTransform == null)
            return null;

        if (frameTransform.getMaximumFrameNumber() <= 0)
            return null;

        Sector frameExtent = frameTransform.frameExtent(0);
        Angle latWindow = frameExtent.getDeltaLat().multiply(latWindowFrames);
        Angle lonWindow = frameExtent.getDeltaLat().multiply(lonWindowFrames);
        Angle lat_over_2 = latWindow.divide(2.0);
        Angle lon_over_2 = lonWindow.divide(2.0);
        return new Sector(
            centerLat.subtract(lat_over_2), centerLat.add(lat_over_2),
            centerLon.subtract(lon_over_2), centerLon.add(lon_over_2));
    }

    private static Angle clampAngle(Angle angle, Angle min, Angle max)
    {
        return (angle.degrees < min.degrees) ? min : ((angle.degrees > max.degrees) ? max : angle);
    }

//    private static Angle normalizeAngle(Angle angle, Angle min, Angle max)
//    {
//        Angle range = max.subtract(min);
//        return (angle.degrees < min.degrees) ?
//            angle.add(range) : ((angle.degrees > max.degrees) ? angle.subtract(range) : angle);
//    }

    // ============== Rendering ======================= //
    // ============== Rendering ======================= //
    // ============== Rendering ======================= //

    private static class BasicSurfaceTile implements Disposable, Cacheable, SurfaceTile
    {
        final Object key;
        final Sector sector;
        private volatile TextureData textureData;
        private Extent extent = null; // bounding volume
        private double extentVerticalExaggertion = Double.MIN_VALUE; // VE used to calculate the extent

        BasicSurfaceTile(Object key, Sector sector)
        {
            this.key = key;
            this.sector = sector;
        }

        public void dispose()
        {
        }

        public final long getSizeInBytes()
        {
            long size = 12;

            if (this.textureData != null)
                size += this.textureData.getEstimatedMemorySize();

            return size;
        }

        public Sector getSector()
        {
            return this.sector;
        }

        public TextureData getTextureData()
        {
            return this.textureData;
        }

        public void setTextureData(TextureData textureData)
        {
            this.textureData = textureData;
        }

        public Texture getTexture(TextureCache tc)
        {
            if (tc == null)
            {
                String message = Logging.getMessage("nullValue.TextureCacheIsNull");
                Logging.logger().severe(message);
                throw new IllegalStateException(message);
            }

            return tc.get(this.key);
        }

        public boolean isTextureInMemory(TextureCache tc)
        {
            if (tc == null)
            {
                String message = Logging.getMessage("nullValue.TextureCacheIsNull");
                Logging.logger().severe(message);
                throw new IllegalStateException(message);
            }

            return this.getTexture(tc) != null || this.getTextureData() != null;
        }

        public void setTexture(TextureCache tc, Texture texture)
        {
            if (tc == null)
            {
                String message = Logging.getMessage("nullValue.TextureCacheIsNull");
                Logging.logger().severe(message);
                throw new IllegalStateException(message);
            }

            tc.put(this.key, texture);

            // No more need for texture data; allow garbage collector and memory cache to reclaim it.
            this.textureData = null;
            this.updateMemoryCache();
        }

        private BasicSurfaceTile getTileFromMemoryCache(Object key)
        {
            return (BasicSurfaceTile) WorldWind.getMemoryCache(BasicSurfaceTile.class.getName()).getObject(key);
        }

        private void updateMemoryCache()
        {
            if (this.getTileFromMemoryCache(this.key) != null)
                WorldWind.getMemoryCache(BasicSurfaceTile.class.getName()).add(this.key, this);
        }

        public Extent getExtent(DrawContext dc)
        {
            if (dc == null)
            {
                String msg = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(msg);
                throw new IllegalArgumentException(msg);
            }

            if (this.extent == null || this.extentVerticalExaggertion != dc.getVerticalExaggeration())
            {
                this.extent =
                    dc.getGlobe().computeBoundingCylinder(dc.getVerticalExaggeration(), this.getSector());
                this.extentVerticalExaggertion = dc.getVerticalExaggeration();
            }

            return this.extent;
        }

        public Texture initializeTexture(DrawContext dc)
        {
            if (dc == null)
            {
                String message = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(message);
                throw new IllegalStateException(message);
            }

            Texture t = this.getTexture(dc.getTextureCache());
            if (t != null)
                return t;

            if (this.getTextureData() == null)
            {
                String msg = Logging.getMessage("nullValue.TextureDataIsNull");
                Logging.logger().severe(msg);
                throw new IllegalStateException(msg);
            }

            try
            {
                t = TextureIO.newTexture(this.getTextureData());
            }
            catch (Exception e)
            {
                Logging.logger().log(
                    java.util.logging.Level.SEVERE, "layers.TextureLayer.ExceptionAttemptingToReadTextureFile", e);
                return null;
            }

            this.setTexture(dc.getTextureCache(), t);
            t.bind();

            GL gl = dc.getGL();
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

            return t;
        }

        public boolean bind(DrawContext dc)
        {
            if (dc == null)
            {
                String message = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(message);
                throw new IllegalStateException(message);
            }

            Texture t = this.getTexture(dc.getTextureCache());
            if (t == null && this.getTextureData() != null)
            {
                t = this.initializeTexture(dc);
                if (t != null)
                    return true; // texture was bound during initialization.
            }

            if (t != null)
                t.bind();

            return t != null;
        }

        public void applyInternalTransform(DrawContext dc)
        {
            if (dc == null)
            {
                String message = Logging.getMessage("nullValue.DrawContextIsNull");
                Logging.logger().severe(message);
                throw new IllegalStateException(message);
            }

            // Use the tile's texture if available.
            Texture t = dc.getTextureCache().get(this.key);
            if (t == null && this.textureData != null)
                t = this.initializeTexture(dc);

            if (t != null)
            {
                TextureCoords tc = t.getImageTexCoords();

                GL gl = GLContext.getCurrent().getGL();
                gl.glMatrixMode(GL.GL_TEXTURE);
                gl.glLoadIdentity();
                gl.glScaled(tc.right() - tc.left(), tc.top() - tc.bottom(), 1);
                if (tc.right() < tc.left())
                {
                    gl.glTranslated(-1, 0, 0);
                }
                if (tc.top() < tc.bottom())
                {
                    gl.glTranslated(0, -1, 0);
                }
            }
        }
    }

//    private final BlockingQueue<Disposable> disposalQueue = new LinkedBlockingQueue<Disposable>();
    private final IconRenderer iconRenderer = new IconRenderer();
//    private SurfaceTile coverageTile;
    private final Object coverageTileKey = new Object();
    private WWIcon icon;
    private boolean drawCoverage = true;
    private boolean drawIcon = true;
//    private static Boolean haveARBNonPowerOfTwo = null;

    private static TextureData createCoverageTextureData(int width, int height,
        Sector extent, Collection<FrameRecord> coverage)
    {
        IntBuffer buffer = BufferUtil.newIntBuffer(width * height);
        Angle latWidth = extent.getMaxLatitude().subtract(extent.getMinLatitude());
        Angle lonWidth = extent.getMaxLongitude().subtract(extent.getMinLongitude());
        for (FrameRecord record : coverage)
        {
            if (record != null && record.getSector() != null)
            {
                int x0 = (int) Math.round((width - 1)
                    * record.sector.getMinLongitude().subtract(extent.getMinLongitude()).divide(lonWidth));
                int x1 = (int) Math.round((width - 1)
                    * record.sector.getMaxLongitude().subtract(extent.getMinLongitude()).divide(lonWidth));

                int y0 = (int) Math.round((height - 1)
                    * record.sector.getMinLatitude().subtract(extent.getMinLatitude()).divide(latWidth));
                int y1 = (int) Math.round((height - 1)
                    * record.sector.getMaxLatitude().subtract(extent.getMinLatitude()).divide(latWidth));

                for (int y = y0; y <= y1; y++)
                {
                    for (int x = x0; x <= x1; x++)
                    {
                        buffer.put(x + y * width, 0x4040407F);
                    }
                }
            }
        }
        buffer.rewind();
        return new TextureData(GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_INT_8_8_8_8,
            false, false, false, buffer, null);
    }

//    private static TextureData createCoverageTextureData(int width, int height,
//        Sector extent, Collection<FrameRecord> coverage)
//    {
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
//        Graphics2D g2d = image.createGraphics();
//        Color fillColor = new Color(0.5f, 0.5f, 0.5f, 0.5f);
//        for (FrameRecord record : coverage)
//            drawFrame(g2d, width, height, fillColor, extent, record);
//
//        TextureData textureData = new TextureData(GL.GL_RGBA, GL.GL_RGBA, false, image);
//        textureData.setMustFlipVertically(false);
//        return textureData;
//    }

//    private static void drawFrame(Graphics2D g2d, int gWidth, int gHeight, Color fillColor,
//        Sector graphicsExtent, FrameRecord record)
//    {
//        Angle xRange = graphicsExtent.getMaxLongitude().subtract(graphicsExtent.getMinLongitude());
//        int x0 = (int) Math.round((gWidth - 1)
//            * record.getSector().getMinLongitude().subtract(graphicsExtent.getMinLongitude()).divide(xRange));
//        int x1 = (int) Math.round((gWidth - 1)
//            * record.getSector().getMaxLongitude().subtract(graphicsExtent.getMinLongitude()).divide(xRange));
//
//        Angle yRange = graphicsExtent.getMaxLatitude().subtract(graphicsExtent.getMinLatitude());
//        int y0 = (int) Math.round((gHeight - 1)
//            * record.getSector().getMinLatitude().subtract(graphicsExtent.getMinLatitude()).divide(yRange));
//        int y1 = (int) Math.round((gHeight - 1)
//            * record.getSector().getMaxLatitude().subtract(graphicsExtent.getMinLatitude()).divide(yRange));
//
//        g2d.setColor(fillColor);
//        g2d.fillRect(x0, y0, x1 - x0, y1 - y0);
//
////        BufferedImage img = null;
////        try
////        {
////            RPFImageFile rpfImageFile = RPFImageFile.load(new File(record.filePath));
////            img = rpfImageFile.getBufferedImage();
////        }
////        catch (Exception e)
////        {
////            WorldWind.logger().logger(Level.FINE, "", e);
////        }
////        if (img == null)
////            return;
////
////
////        double sx = (x1 - x0) / (double) img.getWidth();
////        double sy = (y1 - y0) / (double) img.getHeight();
////        BufferedImageOp op = new AffineTransformOp(
////            AffineTransform.getScaleInstance(sx, sy), AffineTransformOp.TYPE_BICUBIC);
////
////        g2d.drawImage(img, op, x0, y0);
//    }

    public void dispose()
    {
        // Clear queues.
//        this.cachedTiles.clear();
        this.readQueue.clear();
//        this.downloadQueue.clear();
//        // Dispose objects that allocated GL memory.
//        if (this.coverageTile != null && this.coverageTile instanceof Disposable)
//        {
//            ((Disposable) this.coverageTile).dispose();
//            this.coverageTile = null;
//        }
//        processDisposables();
    }

    protected void doRender(DrawContext dc)
    {
//        // Gather GL extension info.
//        if (haveARBNonPowerOfTwo == null)
//            haveARBNonPowerOfTwo = dc.getGL().isExtensionAvailable("GL_ARB_texture_non_power_of_two");

//        // Process disposable queue.
//        this.processDisposables();

        // Update sector and coverage renderables when frame contents change.
        if (this.modCount != this.lastModCount)
        {
            this.lastModCount = this.modCount;
            this.updateSector();
            this.disposeCoverage(dc);
//            this.updateCoverage();
        }

        if (!this.isSectorVisible(dc, this.sector))
            return;

        Position positionOfInterest = this.computePositionOfInterest(dc);
        if (positionOfInterest == null)
            return;

        // Create viewing window.
        Sector viewingSector = createViewSector(
            this.dataSeries,
            positionOfInterest.getLatitude(), positionOfInterest.getLongitude(),
            this.latitudeWindow, this.longitudeWindow);

        // Frame requests.
        Queue<FrameRecord> requestQueue = new LinkedList<FrameRecord>();
        // Visible, in-memory tiles.
        Queue<SurfaceTile> renderQueue = new LinkedList<SurfaceTile>();
        for (Sector sector : normalizeSector(viewingSector))
        {
            this.assembleTiles(dc, this.dataSeries, sector, renderQueue, requestQueue);
        }

        // Compute the union of rendred tile sectors.
        Sector drawSector = null;
        for (SurfaceTile tile : renderQueue)
        {
            drawSector = (drawSector != null) ? drawSector.union(tile.getSector()) : tile.getSector();
        }

        // Render overview.
        this.renderCoverage(dc);

        // Render frame tiles.
        if (percentViewportOfSector(dc, viewingSector) > this.drawFrameThreshold)
        {
            this.renderFrames(dc, renderQueue);
            this.requestAllFrames(requestQueue);
        }

        // Render icon.
        if (percentViewportOfSector(dc, this.sector) < this.drawIconThreshold)
        {
            this.renderIcon(dc, drawSector);
        }

        this.sendRequests();
    }

    private Position computePositionOfInterest(DrawContext dc)
    {
        if (dc == null || dc.getView() == null)
            return null;

        if (dc.getView() instanceof OrbitView)
        {
            OrbitView orbitView = (OrbitView) dc.getView();
            return new Position(orbitView.getLookAtLatitude(), orbitView.getLookAtLongitude(), 0);
        }

        Position eyePos = dc.getView().getEyePosition();
        if (eyePos == null)
            return null;

        return new Position(eyePos.getLatitude(), eyePos.getLongitude(), 0);
    }

    public WWIcon getIcon()
    {
        return this.icon;
    }

    private static void initializeOverviewTexture(Texture texture)
    {
        texture.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        texture.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        texture.setTexParameteri(GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        texture.setTexParameteri(GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
    }

    public boolean isDrawCoverage()
    {
        return this.drawCoverage;
    }

    public boolean isDrawIcon()
    {
        return this.drawIcon;
    }

    private boolean isSectorVisible(DrawContext dc, Sector sector)
    {
        if (dc.getVisibleSector() != null && !sector.intersects(dc.getVisibleSector()))
            return false;

        Extent e = dc.getGlobe().computeBoundingCylinder(dc.getVerticalExaggeration(), sector);
        return e.intersects(dc.getView().getFrustumInModelCoordinates());
    }

    private static int pixelSizeOfSector(DrawContext dc, Sector sector)
    {
        LatLon centroid = sector.getCentroid();
        Globe globe = dc.getGlobe();
        Vec4 centroidPoint = globe.computePointFromPosition(centroid.getLatitude(), centroid.getLongitude(), 0);
        Vec4 minPoint = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), 0);
        Vec4 maxPoint = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), 0);
        double distanceToEye = centroidPoint.distanceTo3(dc.getView().getEyePoint());
        double sectorSize = minPoint.distanceTo3(maxPoint);
        double pixelSize = dc.getView().computePixelSizeAtDistance(distanceToEye);
        return (int) Math.round(sectorSize / pixelSize);
    }

    private static double percentViewportOfSector(DrawContext dc, Sector sector)
    {
        Rectangle viewport = dc.getView().getViewport();
        double pixelSize = pixelSizeOfSector(dc, sector);
        double maxDimension = Math.min(viewport.width, viewport.height);
        return pixelSize / maxDimension;
    }

//    private void processDisposables()
//    {
//        Disposable disposable;
//        while ((disposable = this.disposalQueue.poll()) != null)
//        {
//            if (disposable instanceof RPFTextureTile)
//                this.evictTile(((RPFTextureTile) disposable).frameKey);
//            disposable.dispose();
//        }
//    }

    private void renderCoverage(DrawContext dc)
    {
        BasicSurfaceTile tile = (BasicSurfaceTile) WorldWind.getMemoryCache(BasicSurfaceTile.class.getName())
                    .getObject(this.coverageTileKey);
        if (tile == null || !tile.isTextureInMemory(dc.getTextureCache()))
        {
            tile = loadCoverageTile(tile);
        }

        // Render coverage tile.
        if (this.drawCoverage && tile != null)
        {
            GL gl = dc.getGL();
            int attribBits = GL.GL_ENABLE_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT;
            gl.glPushAttrib(attribBits);
            try
            {
                gl.glEnable(GL.GL_BLEND);
                gl.glEnable(GL.GL_CULL_FACE);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                gl.glCullFace(GL.GL_BACK);
                gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
                dc.getGeographicSurfaceTileRenderer().renderTile(dc, tile);
            }
            finally
            {
                gl.glPopAttrib();
            }
        }
    }

    private void renderIcon(DrawContext dc, Sector drawSector)
    {
        // Render coverage icon.
        if (this.drawIcon && this.icon != null)
        {
            LatLon centroid = (drawSector != null) ? drawSector.getCentroid() : this.sector.getCentroid();
            this.icon.setPosition(new Position(centroid, 0));
            this.iconRenderer.render(dc, this.icon, null);
        }
    }

    private void renderFrames(DrawContext dc, Queue<SurfaceTile> rpfTextureTiles)
    {
        GL gl = dc.getGL();
        int attribBits = GL.GL_ENABLE_BIT | GL.GL_POLYGON_BIT;
        gl.glPushAttrib(attribBits);
        try
        {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
            gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
            dc.getGeographicSurfaceTileRenderer().renderTiles(dc, rpfTextureTiles);
        }
        finally
        {
            gl.glPopAttrib();
        }
    }

    public void setIcon(WWIcon icon)
    {
        if (icon == null)
        {
            String message = Logging.getMessage("nullValue.Icon");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        this.icon = icon;
    }

    public void setDrawCoverage(boolean drawCoverage)
    {
        this.drawCoverage = drawCoverage;
    }

    public void setDrawIcon(boolean drawIcon)
    {
        this.drawIcon = drawIcon;
    }

    private BasicSurfaceTile loadCoverageTile(BasicSurfaceTile tile)
    {
        System.out.println("--------------- load coverage ----------------");        
        if (tile == null)
        {
            tile = new BasicSurfaceTile(this.coverageTileKey, this.sector)
            {
                @Override
                public Texture initializeTexture(DrawContext dc)
                {
                    Texture t = super.initializeTexture(dc);
                    if (t != null)
                        initializeOverviewTexture(t);
                    return t;
                }
            };
        }

        TextureData textureData = createCoverageTextureData(512, 512, this.sector, this.frameDirectory.values());        
        tile.setTextureData(textureData);

        WorldWind.getMemoryCache(BasicSurfaceTile.class.getName()).add(this.coverageTileKey, tile);
        return tile;
    }

    private void disposeCoverage(DrawContext dc)
    {
        dc.getTextureCache().remove(this.coverageTileKey);
        WorldWind.getMemoryCache(BasicSurfaceTile.class.getName()).remove(this.coverageTileKey);
    }

    // ============== Image Reading and Conversion ======================= //
    // ============== Image Reading and Conversion ======================= //
    // ============== Image Reading and Conversion ======================= //

//    private final Map<FrameKey, RPFTextureTile> cachedTiles = new HashMap<FrameKey, RPFTextureTile>();
    private final LinkedList<FrameRequest> downloadQueue = new LinkedList<FrameRequest>();
    private final LinkedList<FrameRequest> readQueue = new LinkedList<FrameRequest>();
//    private final AbsentResourceList absentResourceList = new AbsentResourceList();

    private static class FrameRequest
    {
        static final long STALE_REQUEST_LIMIT = 10000L;
        final FrameRecord frameRecord;
        long timestamp;

        public FrameRequest(FrameRecord frameRecord)
        {
            this.frameRecord = frameRecord;
            this.timestamp = System.currentTimeMillis();
        }

        public void touch()
        {
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isStale()
        {
            return (System.currentTimeMillis() - this.timestamp) > STALE_REQUEST_LIMIT;
        }
    }

    private static class RPFRetriever extends WWObjectImpl implements Retriever
    {
        private final RPFLayer layer;
        private final FrameRecord record;
        private volatile RPFImageFile rpfImageFile;
        private volatile ByteBuffer buffer;
        private volatile String state = RETRIEVER_STATE_NOT_STARTED;
        private long submitTime;
        private long beginTime;
        private long endTime;
        private int connectTimeout = -1;
        private int readTimeout = -1;
        private int staleRequestLimit = -1;

        public RPFRetriever(RPFLayer layer, FrameRecord record)
        {
            this.layer = layer;
            this.record = record;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || !o.getClass().equals(this.getClass()))
                return false;
            final RPFRetriever that = (RPFRetriever) o;
            return this.record.equals(that.record);
        }

        public long getBeginTime()
        {
            return this.beginTime;
        }

        public ByteBuffer getBuffer()
        {
            return this.buffer;
        }

        public int getContentLength()
        {
            return 0;
        }

        public int getContentLengthRead()
        {
            return 0;
        }

        public String getContentType()
        {
            return null;
        }

        public long getEndTime()
        {
            return this.endTime;
        }

        public String getName()
        {
            return this.record.getFilePath();
        }

        public String getState()
        {
            return this.state;
        }

        public long getSubmitTime()
        {
            return this.submitTime;
        }

        public int getConnectTimeout()
        {
            return connectTimeout;
        }

        public void setConnectTimeout(int connectTimeout)
        {
            this.connectTimeout = connectTimeout;
        }

        public int getReadTimeout()
        {
            return readTimeout;
        }

        public void setReadTimeout(int readTimeout)
        {
            this.readTimeout = readTimeout;
        }

        public int getStaleRequestLimit()
        {
            return this.staleRequestLimit;
        }

        public void setStaleRequestLimit(int staleRequestLimit)
        {
            this.staleRequestLimit = staleRequestLimit;
        }

        private boolean interrupted()
        {
            if (Thread.currentThread().isInterrupted())
            {
                this.setState(RETRIEVER_STATE_INTERRUPTED);
                Logging.logger().log(Level.FINER,
                    Logging.getMessage("layers.RPFLayer.DownloadInterrupted", this.record.getFilePath()));
                return true;
            }
            return false;
        }

        public void setBeginTime(long beginTime)
        {
            this.beginTime = beginTime;
        }

        public void setEndTime(long endTime)
        {
            this.endTime = endTime;
        }

        private void setState(String state)
        {
            String oldState = this.state;
            this.state = state;
            this.firePropertyChange(AVKey.RETRIEVER_STATE, oldState, this.state);
        }

        public void setSubmitTime(long submitTime)
        {
            this.submitTime = submitTime;
        }

        public Retriever call() throws Exception // TODO: This method appears never to be called
        {
            if (this.interrupted())
                return this;

            String cacheFilePath = this.record.getCacheFilePath();

            if (!this.record.fileLock.tryLock())
            {
                this.setState(RETRIEVER_STATE_SUCCESSFUL);
                return this;
            }
            try
            {
                this.setState(RETRIEVER_STATE_STARTED);

                if (!this.interrupted())
                {
                    if (isFileResident(cacheFilePath))
                    {
                        this.setState(RETRIEVER_STATE_SUCCESSFUL);
                        return this;
                    }
                }

                if (!this.interrupted())
                {
                    this.setState(RETRIEVER_STATE_CONNECTING);
                    File file = new File(this.record.getFilePath());
                    if (!file.exists())
                    {
                        String message = Logging.getMessage("generic.FileNotFound", this.record.getFilePath());
                        Logging.logger().severe(message);
                        throw new IOException(message);
                    }
                    this.rpfImageFile = RPFImageFile.load(file);
                }

                if (!this.interrupted())
                {
                    this.setState(RETRIEVER_STATE_READING);
                    File file = WorldWind.getDataFileCache().newFile(cacheFilePath);
                    if (file == null)
                    {
                        String msg = Logging.getMessage("generic.CantCreateCacheFile", cacheFilePath);
                        throw new IOException(msg);
                    }
//                    if (haveARBNonPowerOfTwo)
//                    {
                    this.buffer = this.rpfImageFile.getImageAsDdsTexture();
//                    }
//                    else
//                    {
//                        BufferedImage src = this.rpfImageFile.getBufferedImage();
//                        int dstWidth = previousPowerOfTwo(src.getWidth());
//                        int dstHeight = previousPowerOfTwo(src.getHeight());
//                        double sx = dstWidth / (double) src.getWidth();
//                        double sy = dstHeight / (double) src.getHeight();
//                        AffineTransformOp op = new AffineTransformOp(
//                            AffineTransform.getScaleInstance(sx, sy), AffineTransformOp.TYPE_BICUBIC);
//                        BufferedImage dst = new BufferedImage(dstWidth, dstHeight, src.getType());
//                        op.filter(src, dst);
//                        this.buffer = DDSConverter.convertToDxt1NoTransparency(dst);
//                    }
                    WWIO.saveBuffer(this.buffer, file);
                }

                if (!this.interrupted())
                {
                    this.setState(RETRIEVER_STATE_SUCCESSFUL);
//                    this.layer.absentResourceList.unmarkResourceAbsent(absentIdFor(this.record));
                    this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
                }
            }
            catch (Exception e)
            {
                this.setState(RETRIEVER_STATE_ERROR);
//                this.layer.absentResourceList.markResourceAbsent(absentIdFor(this.record));
                throw e;
            }
            finally
            {
                this.record.fileLock.unlock();
            }

            return this;
        }
    }

    private static class ReadTask implements Runnable
    {
        public final RPFLayer layer;
        public final FrameRecord record;

        public ReadTask(RPFLayer layer, FrameRecord record)
        {
            this.layer = layer;
            this.record = record;
        }

        private void deleteCorruptFile(URL fileURL)
        {
            WorldWind.getDataFileCache().removeFile(fileURL);
            String message = Logging.getMessage("generic.DeletedCorruptDataFile", fileURL);
            Logging.logger().finer(message);
        }

//        private void deleteOutOfDateFile(URL fileURL)
//        {
//            WorldWind.getDataFileCache().removeFile(fileURL);
//            String message = WorldWind.retrieveErrMsg("generic.DataFileExpired") + fileURL;
//            WorldWind.logger().logger(Level.FINER, message);
//        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || !o.getClass().equals(this.getClass()))
                return false;
            final ReadTask that = (ReadTask) o;
            return (this.record != null) ? this.record.equals(that.record) : (that.record == null);
        }

        public void run()
        {
            FrameKey key = keyFor(this.record);
            if (!this.layer.isTileResident(key))
            {
                this.readFrame(key, this.record);
                this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
            }
        }

        public void readFrame(FrameKey key, FrameRecord record)
        {
            String cacheFilePath = record.getCacheFilePath();
            URL dataFileURL = WorldWind.getDataFileCache().findFile(cacheFilePath, false);
            // The cached file does not exist. Issue a download and return.
            if (dataFileURL == null)
            {
                this.layer.downloadFrame(this.record);
                return;
            }

            if (!record.fileLock.tryLock())
                return;
            try
            {
//                // The file has expired. Delete it.
//                if (WWIO.isFileOutOfDate(dataFileURL, record.getExpiryTime()))
//                {
//                    this.deleteOutOfDateFile(dataFileURL);
//                    this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
//                    return;
//                }

                if (this.layer.isTileResident(key))
                    return;

                TextureData textureData = null;
                try
                {
                    textureData = TextureIO.newTextureData(dataFileURL, false, TextureIO.DDS);
                }
                catch (IOException e)
                {
                    String message = Logging.getMessage("generic.TextureIOException", cacheFilePath);
                    Logging.logger().log(Level.FINE, message, e);
                }

                // The file has expired. Delete it.
                if (textureData == null)
                {
                    this.deleteCorruptFile(dataFileURL);
//                    this.layer.absentResourceList.markResourceAbsent(absentIdFor(record));
                    return;
                }
//                else
//                {
//                    this.layer.absentResourceList.unmarkResourceAbsent(absentIdFor(record));
//                }

                System.out.println("--------------- load tile ----------------");                            
//                BasicSurfaceTile tile = new BasicSurfaceTile(key, record.sector);
//                tile.setTextureData(textureData);
                this.layer.makeTileResident(key, record, textureData);
                this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
            }
            finally
            {
                record.fileLock.unlock();
            }
        }
    }

    private void downloadFrame(FrameRecord record)
    {
//        if (this.absentResourceList.isResourceAbsent(absentIdFor(record)))
//            return;

        synchronized (this.downloadQueue)
        {
            FrameRequest frameRequest = new FrameRequest(record);
            int index = this.downloadQueue.indexOf(frameRequest);
            // Frame has not been requested, add it.
            if (index < 0)
                this.downloadQueue.addFirst(frameRequest);
                // Frame is already requested, update it's timestamp.
            else
                this.downloadQueue.get(index).touch();
        }
    }

//    private void evictTile(FrameKey key)
//    {
//        synchronized (this.cachedTiles)
//        {
//            // Value is assumed to be evicted from memory cache.
//            this.cachedTiles.remove(key);
//        }
//    }

    private BasicSurfaceTile getTile(FrameKey key)
    {
        return (BasicSurfaceTile) WorldWind.getMemoryCache(BasicSurfaceTile.class.getName()).getObject(key);

//        synchronized (this.cachedTiles)
//        {
//            dc.getTextureCache().get(key);
//            // Touch the value in memory cache.
//            this.memoryCache.getObject(key);
//            // Return the value in our local cache directory.
//            return this.cachedTiles.get(key);
//        }
    }

//    private static long absentIdFor(FrameRecord record)
//    {
//        RPFFrameProperties properties = record.getRPFFrameProperties();
//        return properties.zone.ordinal() + (100 * properties.frameNumber);
//    }

    private static boolean isFileResident(String fileName)
    {
        return WorldWind.getDataFileCache().findFile(fileName, false) != null;
    }

    private boolean isTileResident(FrameKey key)
    {
        BasicSurfaceTile tile = (BasicSurfaceTile) WorldWind.getMemoryCache(BasicSurfaceTile.class.getName()).getObject(key);
        return (tile != null) && (tile.getTextureData() != null);

//        synchronized (this.cachedTiles)
//        {
//            // Value may be evicted from memory cache, but still in local cache directory
//            // (not yet processsed for disposal).
//            return this.cachedTiles.get(key) != null;
//        }
    }

    private void makeTileResident(FrameKey key, FrameRecord record, TextureData textureData)
    {
        MemoryCache cache = WorldWind.getMemoryCache(BasicSurfaceTile.class.getName());
        BasicSurfaceTile tile = (BasicSurfaceTile) cache.getObject(key);
        if (tile == null)
        {
            tile = new BasicSurfaceTile(key, record.sector);
            cache.add(key, tile);
        }

        tile.setTextureData(textureData);

//        synchronized (this.memoryCache)
//        {
//            this.memoryCache.add(key, tile);
//        }
//
//        synchronized (this.cachedTiles)
//        {
//            this.cachedTiles.put(key, tile);
//        }
    }

//    private static int previousPowerOfTwo(int value)
//    {
//        int result = 1;
//        while (result < value)
//        {
//            result <<= 1;
//        }
//        return (result >> 1);
//    }

    private void requestFrame(FrameRecord record)
    {
//        if (this.absentResourceList.isResourceAbsent(absentIdFor(record)))
//            return;

        synchronized (this.readQueue)
        {
            FrameRequest frameRequest = new FrameRequest(record);
            int index = this.readQueue.indexOf(frameRequest);
            // Frame has not been requested, add it.
            if (index < 0)
                this.readQueue.addFirst(frameRequest);
                // Frame is already requested, update it's timestamp.
            else
                this.readQueue.get(index).touch();
        }
    }

    private void requestAllFrames(Collection<FrameRecord> frameRecords)
    {
        for (FrameRecord record : frameRecords)
        {
            this.requestFrame(record);
        }
    }

    private void sendRequests()
    {
        synchronized (this.readQueue)
        {
            FrameRequest request;
            while (!WorldWind.getTaskService().isFull() && (request = this.readQueue.poll()) != null)
            {
                if (!request.isStale())
                    WorldWind.getTaskService().addTask(new ReadTask(this, request.frameRecord));
            }
        }

        synchronized (this.downloadQueue)
        {
            FrameRequest request;
            while (WorldWind.getRetrievalService().isAvailable() && (request = this.downloadQueue.poll()) != null)
            {
                if (!request.isStale())
                    WorldWind.getRetrievalService().runRetriever(new RPFRetriever(this, request.frameRecord));
            }
        }
    }
}
