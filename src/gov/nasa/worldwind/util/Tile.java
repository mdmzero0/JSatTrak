/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.geom.*;

import java.util.Random;

/**
 * @author tag
 * @version $Id: Tile.java 2786 2007-09-10 17:41:00Z tgaskins $
 */
public class Tile implements Comparable<Tile>, Cacheable
{
    private final Sector sector;
    private final Level level;
    private final int row;
    private final int column;
    private final TileKey tileKey;
    private double priority = Double.MAX_VALUE; // Default is minimum priority
    // The following is late bound because it's only selectively needed and costly to create
    private String path;

    public Tile(Sector sector, Level level, int row, int column)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (level == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (row < 0)
        {
            String msg = Logging.getMessage("generic.RowIndexOutOfRange", row);
            msg += String.valueOf(row);

            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (column < 0)
        {
            String msg = Logging.getMessage("generic.ColumnIndexOutOfRange", column);
            msg += String.valueOf(row);

            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sector = sector;
        this.level = level;
        this.row = row;
        this.column = column;
        this.tileKey = new TileKey(this);
        this.path = null;
    }

    public Tile(Sector sector, Level level)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (level == null)
        {
            String msg = Logging.getMessage("nullValue.LevelIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.sector = sector;
        this.level = level;
        this.row = Tile.computeRow(sector.getDeltaLat(), sector.getMinLatitude());
        this.column = Tile.computeColumn(sector.getDeltaLon(), sector.getMinLongitude());
        this.tileKey = new TileKey(this);
        this.path = null;
    }

    public Tile(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Random random = new Random();

        this.sector = sector;
        this.level = null;
        this.row = random.nextInt();
        this.column = random.nextInt();
        this.path = null;
        this.tileKey = new TileKey(this);
    }

    public long getSizeInBytes()
    {
        // Return just an approximate size
        long size = 0;

        if (this.sector != null)
            size += this.sector.getSizeInBytes();

        if (this.path != null)
            size += this.getPath().length();

        size += 32; // to account for the references and the TileKey size

        return size;
    }

    public String getPath()
    {
        if (this.path == null)
        {
            this.path = this.level.getPath() + "/" + this.row + "/" + this.row + "_" + this.column;
            if (!this.level.isEmpty())
                path += this.level.getFormatSuffix();
        }

        return this.path;
    }

    public final Sector getSector()
    {
        return sector;
    }

    public Level getLevel()
    {
        return level;
    }

    public final int getLevelNumber()
    {
        return this.level != null ? this.level.getLevelNumber() : 0;
    }

    public final String getLevelName()
    {
        return this.level != null ? this.level.getLevelName() : "";
    }

    public final int getRow()
    {
        return row;
    }

    public final int getColumn()
    {
        return column;
    }

    public final String getCacheName()
    {
        return this.level != null ? this.level.getCacheName() : null;
    }

    public final String getFormatSuffix()
    {
        return this.level != null ? this.level.getFormatSuffix() : null;
    }

    public final TileKey getTileKey()
    {
        return this.tileKey;
    }

    public java.net.URL getResourceURL() throws java.net.MalformedURLException
    {
        return this.level != null ? this.level.getTileResourceURL(this) : null;
    }

    public String getLabel()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(this.getLevelNumber());
        sb.append("(");
        sb.append(this.getLevelName());
        sb.append(")");
        sb.append(", ").append(this.getRow());
        sb.append(", ").append(this.getColumn());

        return sb.toString();
    }

    public int compareTo(Tile tile)
    {
        if (tile == null)
        {
            String msg = Logging.getMessage("nullValue.TileIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // No need to compare Sectors or path because they are redundant with row and column
        if (tile.getLevelNumber() == this.getLevelNumber() && tile.row == this.row && tile.column == this.column)
            return 0;

        if (this.getLevelNumber() < tile.getLevelNumber()) // Lower-res levels compare lower than higher-res
            return -1;
        if (this.getLevelNumber() > tile.getLevelNumber())
            return 1;

        if (this.row < tile.row)
            return -1;
        if (this.row > tile.row)
            return 1;

        if (this.column < tile.column)
            return -1;

        return 1; // tile.column must be > this.column because equality was tested above
    }

    @Override
    public boolean equals(Object o)
    {
        // Equality based only on the tile key
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final Tile tile = (Tile) o;

        return !(tileKey != null ? !tileKey.equals(tile.tileKey) : tile.tileKey != null);
    }

    @Override
    public int hashCode()
    {
        return (tileKey != null ? tileKey.hashCode() : 0);
    }

    @Override
    public String toString()
    {
        return this.getPath();
    }

    /**
     * Computes the row index of a latitude in the global tile grid corresponding to a specified grid interval.
     *
     * @param delta    the grid interval
     * @param latitude the latitude for which to compute the row index
     * @return the row index of the row containing the specified latitude
     * @throws IllegalArgumentException if <code>delta</code> is null or non-positive, or <code>latitude</code> is null,
     *                                  greater than positive 90 degrees, or less than  negative 90 degrees
     */
    public static int computeRow(Angle delta, Angle latitude)
    {
        if (delta == null || latitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (delta.degrees <= 0d)
        {
            String message = Logging.getMessage("generic.DeltaAngleOutOfRange", delta);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (latitude.degrees < -90d || latitude.degrees > 90d)
        {
            String message = Logging.getMessage("generic.AngleOutOfRange", latitude);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (latitude.degrees == 90d)
            return (int) (180d / delta.degrees) - 1;
        else
            return (int) ((latitude.degrees + 90d) / delta.degrees);
    }

    /**
     * Computes the column index of a longitude in the global tile grid corresponding to a specified grid interval.
     *
     * @param delta     the grid interval
     * @param longitude the longitude for which to compute the column index
     * @return the column index of the column containing the specified latitude
     * @throws IllegalArgumentException if <code>delta</code> is null or non-positive, or <code>longitude</code> is
     *                                  null, greater than positive 180 degrees, or less than  negative 180 degrees
     */
    public static int computeColumn(Angle delta, Angle longitude)
    {
        if (delta == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (delta.degrees <= 0d)
        {
            String message = Logging.getMessage("generic.DeltaAngleOutOfRange", delta);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (longitude.degrees < -180d || longitude.degrees > 180d)
        {
            String message = Logging.getMessage("generic.AngleOutOfRange", longitude);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (longitude.degrees == 180d)
            return (int) (360d / delta.degrees) - 1;
        else
            return (int) ((longitude.degrees + 180d) / delta.degrees);
    }

    /**
     * Determines the minimum latitude of a row in the global tile grid corresponding to a specified grid interval.
     *
     * @param row   the row index of the row in question
     * @param delta the grid interval
     * @return the minimum latitude of the tile corresponding to the specified row
     * @throws IllegalArgumentException if the grid interval (<code>delta</code>) is null or zero or the row index is
     *                                  negative.
     */
    public static Angle computeRowLatitude(int row, Angle delta)
    {
        if (delta == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (row < 0)
        {
            String msg = Logging.getMessage("generic.RowIndexOutOfRange", row);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (delta.degrees <= 0d)
        {
            String message = Logging.getMessage("generic.DeltaAngleOutOfRange", delta);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(-90d + delta.degrees * row);
    }

    /**
     * Determines the minimum longitude of a column in the global tile grid corresponding to a specified grid interval.
     *
     * @param column the row index of the row in question
     * @param delta  the grid interval
     * @return the minimum longitude of the tile corresponding to the specified column
     * @throws IllegalArgumentException if the grid interval (<code>delta</code>) is null or zero or the column index is
     *                                  negative.
     */
    public static Angle computeColumnLongitude(int column, Angle delta)
    {
        if (delta == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (column < 0)
        {
            String msg = Logging.getMessage("generic.ColumnIndexOutOfRange", column);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (delta.degrees <= 0d)
        {
            String message = Logging.getMessage("generic.DeltaAngleOutOfRange", delta);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(-180 + delta.degrees * column);
    }

    public double getPriority()
    {
        return priority;
    }

    public void setPriority(double priority)
    {
        this.priority = priority;
    }
}
