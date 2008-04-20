/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.placename;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;

/**
 * @author Paul Collins
 * @version $Id: PlaceNameService.java 3307 2007-10-16 14:43:49Z patrickmurris $
 */
public class PlaceNameService
{
    // Data retrieval and caching attributes.
    private final String service;
    private final String dataset;
    private final String fileCachePath;
    private static final String FORMAT_SUFFIX = ".xml.gz";
    // Geospatial attributes.
    private final Sector sector;
    private final LatLon tileDelta;
    private Extent extent = null;
    private double extentVerticalExaggeration = Double.MIN_VALUE;
    // Display attributes.
    private final java.awt.Font font;
    private boolean enabled;
    private java.awt.Color color;
    private double minDisplayDistance;
    private double maxDisplayDistance;
    private int numColumns;

    private static final int MAX_ABSENT_TILE_TRIES = 2;
    private static final int MIN_ABSENT_TILE_CHECK_INTERVAL = 10000;
    private final AbsentResourceList absentTiles = new AbsentResourceList(MAX_ABSENT_TILE_TRIES,
        MIN_ABSENT_TILE_CHECK_INTERVAL);

    /**
     * @param service
     * @param dataset
     * @param fileCachePath
     * @param sector
     * @param tileDelta
     * @param font
     * @throws IllegalArgumentException if any parameter is null
     */
    public PlaceNameService(String service, String dataset, String fileCachePath, Sector sector, LatLon tileDelta,
        java.awt.Font font)
    {
        // Data retrieval and caching attributes.
        this.service = service;
        this.dataset = dataset;
        this.fileCachePath = fileCachePath;
        // Geospatial attributes.
        this.sector = sector;
        this.tileDelta = tileDelta;
        // Display attributes.
        this.font = font;
        this.enabled = true;
        this.color = java.awt.Color.white;
        this.minDisplayDistance = Double.MIN_VALUE;
        this.maxDisplayDistance = Double.MAX_VALUE;

        String message = this.validate();
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.numColumns = this.numColumnsInLevel();
    }

    /**
     * @param row
     * @param column
     * @return
     * @throws IllegalArgumentException if either <code>row</code> or <code>column</code> is less than zero
     */
    public String createFileCachePathFromTile(int row, int column)
    {
        if (row < 0 || column < 0)
        {
            String message = Logging.getMessage("PlaceNameService.RowOrColumnOutOfRange", row, column);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        StringBuilder sb = new StringBuilder(this.fileCachePath);
        sb.append(java.io.File.separator).append(this.dataset);
        sb.append(java.io.File.separator).append(row);
        sb.append(java.io.File.separator).append(row).append('_').append(column);

        if (FORMAT_SUFFIX.charAt(0) != '.')
            sb.append('.');
        sb.append(FORMAT_SUFFIX);

        String path = sb.toString();
        return path.replaceAll("[:*?<>|]", "");
    }

    private int numColumnsInLevel()
    {
        int firstCol = Tile.computeColumn(this.tileDelta.getLongitude(), sector.getMinLongitude());
        int lastCol = Tile.computeColumn(this.tileDelta.getLongitude(),
            sector.getMaxLongitude().subtract(this.tileDelta.getLongitude()));

        return lastCol - firstCol + 1;
    }

    public long getTileNumber(int row, int column)
    {
        return row * this.numColumns + column;
    }

    /**
     * @param sector
     * @return
     * @throws java.net.MalformedURLException
     * @throws IllegalArgumentException       if <code>sector</code> is null
     */
    public java.net.URL createServiceURLFromSector(Sector sector) throws java.net.MalformedURLException
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        StringBuilder sb = new StringBuilder(this.service);
        if (sb.charAt(sb.length() - 1) != '?')
            sb.append('?');
        sb.append("TypeName=").append(dataset);
        sb.append("&Request=GetFeature");
        sb.append("&Service=WFS");
        sb.append("&OUTPUTFORMAT=GML2-GZIP");
        sb.append("&BBOX=");
        sb.append(sector.getMinLongitude().getDegrees()).append(',');
        sb.append(sector.getMinLatitude().getDegrees()).append(',');
        sb.append(sector.getMaxLongitude().getDegrees()).append(',');
        sb.append(sector.getMaxLatitude().getDegrees());
        return new java.net.URL(sb.toString());
    }

    public synchronized final PlaceNameService deepCopy()
    {
        PlaceNameService copy = new PlaceNameService(this.service, this.dataset, this.fileCachePath, this.sector,
            this.tileDelta,
            this.font);
        copy.enabled = this.enabled;
        copy.color = this.color;
        copy.minDisplayDistance = this.minDisplayDistance;
        copy.maxDisplayDistance = this.maxDisplayDistance;
        return copy;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        final PlaceNameService other = (PlaceNameService) o;

        if (this.service != null ? !this.service.equals(other.service) : other.service != null)
            return false;
        if (this.dataset != null ? !this.dataset.equals(other.dataset) : other.dataset != null)
            return false;
        if (this.fileCachePath != null ? !this.fileCachePath.equals(other.fileCachePath) : other.fileCachePath != null)
            return false;
        if (this.sector != null ? !this.sector.equals(other.sector) : other.sector != null)
            return false;
        if (this.tileDelta != null ? !this.tileDelta.equals(other.tileDelta) : other.tileDelta != null)
            return false;
        if (this.font != null ? !this.font.equals(other.font) : other.font != null)
            return false;
        if (this.color != null ? !this.color.equals(other.color) : other.color != null)
            return false;
        if (this.minDisplayDistance != other.minDisplayDistance)
            return false;
        //noinspection RedundantIfStatement
        if (this.maxDisplayDistance != other.maxDisplayDistance)
            return false;

        return true;
    }

    public synchronized final java.awt.Color getColor()
    {
        return this.color;
    }

    public final String getDataset()
    {
        return this.dataset;
    }

    /**
     * @param dc
     * @return
     * @throws IllegalArgumentException if <code>dc</code> is null
     */
    public final Extent getExtent(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.extent == null || this.extentVerticalExaggeration != dc.getVerticalExaggeration())
        {
            this.extentVerticalExaggeration = dc.getVerticalExaggeration();
            this.extent = dc.getGlobe().computeBoundingCylinder(this.extentVerticalExaggeration, this.sector);
        }

        return extent;
    }

    public final String getFileCachePath()
    {
        return this.fileCachePath;
    }

    public final java.awt.Font getFont()
    {
        return this.font;
    }

    public synchronized final double getMaxDisplayDistance()
    {
        return this.maxDisplayDistance;
    }

    public synchronized final double getMinDisplayDistance()
    {
        return this.minDisplayDistance;
    }

    public final LatLon getTileDelta()
    {
        return tileDelta;
    }

    public final Sector getSector()
    {
        return this.sector;
    }

    public final String getService()
    {
        return this.service;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = (service != null ? service.hashCode() : 0);
        result = 29 * result + (this.dataset != null ? this.dataset.hashCode() : 0);
        result = 29 * result + (this.fileCachePath != null ? this.fileCachePath.hashCode() : 0);
        result = 29 * result + (this.sector != null ? this.sector.hashCode() : 0);
        result = 29 * result + (this.tileDelta != null ? this.tileDelta.hashCode() : 0);
        result = 29 * result + (this.font != null ? this.font.hashCode() : 0);
        result = 29 * result + (this.color != null ? this.color.hashCode() : 0);
        result = 29 * result + ((Double) minDisplayDistance).hashCode();
        result = 29 * result + ((Double) maxDisplayDistance).hashCode();
        return result;
    }

    public synchronized final boolean isEnabled()
    {
        return this.enabled;
    }

    /**
     * @param color
     * @throws IllegalArgumentException if <code>color</code> is null
     */
    public synchronized final void setColor(java.awt.Color color)
    {
        if (color == null)
        {
            String message = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.color = color;
    }

    public synchronized final void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * @param maxDisplayDistance
     * @throws IllegalArgumentException if <code>maxDisplayDistance</code> is less than the current minimum display
     *                                  distance
     */
    public synchronized final void setMaxDisplayDistance(double maxDisplayDistance)
    {
        if (maxDisplayDistance < this.minDisplayDistance)
        {
            String message = Logging.getMessage("PlaceNameService.MaxDisplayDistanceLessThanMinDisplayDistance",
                maxDisplayDistance, this.minDisplayDistance);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.maxDisplayDistance = maxDisplayDistance;
    }

    /**
     * @param minDisplayDistance
     * @throws IllegalArgumentException if <code>minDisplayDistance</code> is less than the current maximum display
     *                                  distance
     */
    public synchronized final void setMinDisplayDistance(double minDisplayDistance)
    {
        if (minDisplayDistance > this.maxDisplayDistance)
        {
            String message = Logging.getMessage("PlaceNameService.MinDisplayDistanceGrtrThanMaxDisplayDistance",
                minDisplayDistance, this.maxDisplayDistance);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minDisplayDistance = minDisplayDistance;
    }

    public synchronized final void markResourceAbsent(long tileNumber)
    {
        this.absentTiles.markResourceAbsent(tileNumber);
    }

    public synchronized final boolean isResourceAbsent(long resourceNumber)
    {
        return this.absentTiles.isResourceAbsent(resourceNumber);
    }

    public synchronized final void unmarkResourceAbsent(long tileNumber)
    {
        this.absentTiles.unmarkResourceAbsent(tileNumber);
    }

    /**
     * Determines if this <code>PlaceNameService'</code> constructor arguments are valid.
     *
     * @return null if valid, otherwise a <code>String</code> containing a description of why it is invalid.
     */
    public final String validate()
    {
        String msg = "";
        if (this.service == null)
        {
            msg += Logging.getMessage("nullValue.ServiceIsNull") + ", ";
        }
        if (this.dataset == null)
        {
            msg += Logging.getMessage("nullValue.DataSetIsNull") + ", ";
        }
        if (this.fileCachePath == null)
        {
            msg += Logging.getMessage("nullValue.FileCachePathIsNull") + ", ";
        }
        if (this.sector == null)
        {
            msg += Logging.getMessage("nullValue.SectorIsNull") + ", ";
        }
        if (this.tileDelta == null)
        {
            msg += Logging.getMessage("nullValue.TileDeltaIsNull") + ", ";
        }
        if (this.font == null)
        {
            msg += Logging.getMessage("nullValue.FontIsNull") + ", ";
        }

        if (msg.length() == 0)
        {
            return null;
        }

        return msg;
    }
}
