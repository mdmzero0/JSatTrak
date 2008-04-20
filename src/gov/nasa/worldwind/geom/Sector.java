/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.util.Logging;

/**
 * <code>Sector</code> represents a rectangular reqion of latitude and longitude. The region is defined by four angles:
 * its minimum and maximum latitude, its minimum and maximum longitude. The angles are assumed to be normalized to +/-
 * 90 degrees latitude and +/- 180 degrees longitude. The minimums and maximums are relative to these ranges, e.g., -80
 * is less than 20. Behavior of the class is undefined for angles outside these ranges. Normalization is not performed
 * on the angles by this class, nor is it verifed by the class' methods. See {@link Angle} for a description of
 * specifying angles.
 * <p/>
 * <code>Sector</code> instances are immutable. </p>
 *
 * @author Tom Gaskins
 * @version $Id: Sector.java 3307 2007-10-16 14:43:49Z patrickmurris $
 * @see Angle
 */
public class Sector implements Cacheable, Comparable<Sector>
{
    /**
     * A <code>Sector</code> of latitude [-90 degrees, + 90 degrees] and longitude [-180 degrees, + 180 degrees].
     */
    public static final Sector FULL_SPHERE = new Sector(Angle.NEG90, Angle.POS90, Angle.NEG180, Angle.POS180);
    public static final Sector EMPTY_SECTOR = new Sector(Angle.ZERO, Angle.ZERO, Angle.ZERO, Angle.ZERO);

    private final Angle minLatitude;
    private final Angle maxLatitude;
    private final Angle minLongitude;
    private final Angle maxLongitude;
    private final Angle deltaLat;
    private final Angle deltaLon;

    /**
     * Creates a new <code>Sector</code> and initializes it to the specified angles. The angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude, but this method does not verify that.
     *
     * @param minLatitude  the sector's minimum latitude in degrees.
     * @param maxLatitude  the sector's maximum latitude in degrees.
     * @param minLongitude the sector's minimum longitude in degrees.
     * @param maxLongitude the sector's maximum longitude in degrees.
     * @return the new <code>Sector</code>
     */
    public static Sector fromDegrees(double minLatitude, double maxLatitude, double minLongitude,
        double maxLongitude)
    {
        return new Sector(Angle.fromDegrees(minLatitude), Angle.fromDegrees(maxLatitude), Angle.fromDegrees(
            minLongitude), Angle.fromDegrees(maxLongitude));
    }

    /**
     * Creates a new <code>Sector</code> and initializes it to the specified angles. The angles are assumed to be
     * normalized to +/- \u03c0/2 radians latitude and +/- \u03c0 radians longitude, but this method does not verify
     * that.
     *
     * @param minLatitude  the sector's minimum latitude in radians.
     * @param maxLatitude  the sector's maximum latitude in radians.
     * @param minLongitude the sector's minimum longitude in radians.
     * @param maxLongitude the sector's maximum longitude in radians.
     * @return the new <code>Sector</code>
     */
    public static Sector fromRadians(double minLatitude, double maxLatitude, double minLongitude,
        double maxLongitude)
    {
        return new Sector(Angle.fromRadians(minLatitude), Angle.fromRadians(maxLatitude), Angle.fromRadians(
            minLongitude), Angle.fromRadians(maxLongitude));
    }

    public static Sector boundingSector(java.util.Iterator<TrackPoint> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.TracksPointsIteratorNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!positions.hasNext())
            return EMPTY_SECTOR;

        TrackPoint position = positions.next();
        double minLat = position.getLatitude();
        double minLon = position.getLongitude();
        double maxLat = minLat;
        double maxLon = minLon;

        while (positions.hasNext())
        {
            TrackPoint p = positions.next();
            double lat = p.getLatitude();
            if (lat < minLat)
                minLat = lat;
            else if (lat > maxLat)
                maxLat = lat;

            double lon = p.getLongitude();
            if (lon < minLon)
                minLon = lon;
            else if (lon > maxLon)
                maxLon = lon;
        }

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    public static Sector boundingSectorfromLatLons(Iterable<LatLon> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double minLat = Angle.POS90.getDegrees();
        double minLon = Angle.POS180.getDegrees();
        double maxLat = Angle.NEG180.getDegrees();
        double maxLon = Angle.NEG180.getDegrees();

        for (LatLon p : positions)
        {
            double lat = p.getLatitude().getDegrees();
            if (lat < minLat)
                minLat = lat;
            if (lat > maxLat)
                maxLat = lat;

            double lon = p.getLongitude().getDegrees();
            if (lon < minLon)
                minLon = lon;
            if (lon > maxLon)
                maxLon = lon;
        }

        if (minLat == maxLat && minLon == maxLon)
            return EMPTY_SECTOR;

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    public static Sector boundingSectorfromPositions(Iterable<Position> positions)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double minLat = Angle.POS90.getDegrees();
        double minLon = Angle.POS180.getDegrees();
        double maxLat = Angle.NEG180.getDegrees();
        double maxLon = Angle.NEG180.getDegrees();

        for (Position p : positions)
        {
            double lat = p.getLatitude().getDegrees();
            if (lat < minLat)
                minLat = lat;
            if (lat > maxLat)
                maxLat = lat;

            double lon = p.getLongitude().getDegrees();
            if (lon < minLon)
                minLon = lon;
            if (lon > maxLon)
                maxLon = lon;
        }

        if (minLat == maxLat && minLon == maxLon)
            return EMPTY_SECTOR;

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    public static Sector boundingSector(Position pA, Position pB)
    {
        if (pA == null || pB == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double minLat = pA.getLatitude().degrees;
        double minLon = pA.getLongitude().degrees;
        double maxLat = pA.getLatitude().degrees;
        double maxLon = pA.getLongitude().degrees;

        if (pB.getLatitude().degrees < minLat)
            minLat = pB.getLatitude().degrees;
        else if (pB.getLatitude().degrees > maxLat)
            maxLat = pB.getLatitude().degrees;

        if (pB.getLongitude().degrees < minLon)
            minLon = pB.getLongitude().degrees;
        else if (pB.getLongitude().degrees > maxLon)
            maxLon = pB.getLongitude().degrees;

        if (minLat == maxLat && minLon == maxLon)
            return EMPTY_SECTOR;

        return Sector.fromDegrees(minLat, maxLat, minLon, maxLon);
    }

    /**
     * Creates a new <code>Sector</code> and initializes it to the specified angles. The angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude, but this method does not verify that.
     *
     * @param minLatitude  the sector's minimum latitude.
     * @param maxLatitude  the sector's maximum latitude.
     * @param minLongitude the sector's minimum longitude.
     * @param maxLongitude the sector's maximum longitude.
     * @throws IllegalArgumentException if any of the angles are null
     */
    public Sector(Angle minLatitude, Angle maxLatitude, Angle minLongitude, Angle maxLongitude)
    {
        if (minLatitude == null || maxLatitude == null || minLongitude == null || maxLongitude == null)
        {
            String message = Logging.getMessage("nullValue.InputAnglesNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minLatitude = minLatitude;
        this.maxLatitude = maxLatitude;
        this.minLongitude = minLongitude;
        this.maxLongitude = maxLongitude;
        this.deltaLat = Angle.fromDegrees(this.maxLatitude.degrees - this.minLatitude.degrees);
        this.deltaLon = Angle.fromDegrees(this.maxLongitude.degrees - this.minLongitude.degrees);
    }

    public Sector(Sector sector)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.minLatitude = new Angle(sector.getMinLatitude());
        this.maxLatitude = new Angle(sector.getMaxLatitude());
        this.minLongitude = new Angle(sector.getMinLongitude());
        this.maxLongitude = new Angle(sector.getMaxLongitude());
        this.deltaLat = Angle.fromDegrees(this.maxLatitude.degrees - this.minLatitude.degrees);
        this.deltaLon = Angle.fromDegrees(this.maxLongitude.degrees - this.minLongitude.degrees);
    }

    /**
     * Returns the sector's minimum latitude.
     *
     * @return The sector's minimum latitude.
     */
    public final Angle getMinLatitude()
    {
        return minLatitude;
    }

    /**
     * Returns the sector's minimum longitude.
     *
     * @return The sector's minimum longitude.
     */
    public final Angle getMinLongitude()
    {
        return minLongitude;
    }

    /**
     * Returns the sector's maximum latitude.
     *
     * @return The sector's maximum latitude.
     */
    public final Angle getMaxLatitude()
    {
        return maxLatitude;
    }

    /**
     * Returns the sector's maximum longitude.
     *
     * @return The sector's maximum longitude.
     */
    public final Angle getMaxLongitude()
    {
        return maxLongitude;
    }

    /**
     * Returns the angular difference between the sector's minimum and maximum latitudes: max - min
     *
     * @return The angular difference between the sector's minimum and maximum latitudes.
     */
    public final Angle getDeltaLat()
    {
        return this.deltaLat;//Angle.fromDegrees(this.maxLatitude.degrees - this.minLatitude.degrees);
    }

    public final double getDeltaLatDegrees()
    {
        return this.deltaLat.degrees;//this.maxLatitude.degrees - this.minLatitude.degrees;
    }

    public final double getDeltaLatRadians()
    {
        return this.deltaLat.radians;//this.maxLatitude.radians - this.minLatitude.radians;
    }

    /**
     * Returns the angular difference between the sector's minimum and maximum longitudes: max - min.
     *
     * @return The angular difference between the sector's minimum and maximum longitudes
     */
    public final Angle getDeltaLon()
    {
        return this.deltaLon;//Angle.fromDegrees(this.maxLongitude.degrees - this.minLongitude.degrees);
    }

    public final double getDeltaLonDegrees()
    {
        return this.deltaLon.degrees;//this.maxLongitude.degrees - this.minLongitude.degrees;
    }

    public final double getDeltaLonRadians()
    {
        return this.deltaLon.radians;//this.maxLongitude.radians - this.minLongitude.radians;
    }

    /**
     * Returns the latitude and longitude of the sector's angular center: (minimum latitude + maximum latitude) / 2,
     * (minimum longitude + maximum longitude) / 2.
     *
     * @return The latitude and longitude of the sector's angular center
     */
    public final LatLon getCentroid()
    {
        Angle la = Angle.fromDegrees(0.5 * (this.getMaxLatitude().degrees + this.getMinLatitude().degrees));
        Angle lo = Angle.fromDegrees(0.5 * (this.getMaxLongitude().degrees + this.getMinLongitude().degrees));
        return new LatLon(la, lo);
    }

    public Vec4 computeCenterPoint(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double lat = 0.5 * (this.minLatitude.degrees + this.maxLatitude.degrees);
        double lon = 0.5 * (this.minLongitude.degrees + this.maxLongitude.degrees);

        Angle cLat = Angle.fromDegrees(lat);
        Angle cLon = Angle.fromDegrees(lon);
        return globe.computePointFromPosition(cLat, cLon, globe.getElevation(cLat, cLon));
    }

    public Vec4[] computeCornerPoints(Globe globe)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        Vec4[] corners = new Vec4[4];

        Angle minLat = this.minLatitude;
        Angle maxLat = this.maxLatitude;
        Angle minLon = this.minLongitude;
        Angle maxLon = this.maxLongitude;

        corners[0] = globe.computePointFromPosition(minLat, minLon, globe.getElevation(minLat, minLon));
        corners[1] = globe.computePointFromPosition(minLat, maxLon, globe.getElevation(minLat, maxLon));
        corners[2] = globe.computePointFromPosition(maxLat, maxLon, globe.getElevation(maxLat, maxLon));
        corners[3] = globe.computePointFromPosition(maxLat, minLon, globe.getElevation(maxLat, minLon));

        return corners;
    }

    /**
     * Returns a sphere that minimally surrounds the sector at a specified vertical exaggeration.
     *
     * @param globe                the globe the sector is associated with
     * @param verticalExaggeration the vertical exaggeration to apply to the globe's elevations when computing the
     *                             sphere.
     * @param sector               the sector to return the bounding sphere for.
     * @return The minimal bounding sphere in Cartesian coordinates.
     * @throws IllegalArgumentException if <code>globe</code> or <code>sector</code> is null
     */
    static public Extent computeBoundingSphere(Globe globe, double verticalExaggeration, Sector sector)
    {
        if (globe == null)
        {
            String msg = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        LatLon center = sector.getCentroid();
        double maxHeight = globe.getMaxElevation(sector) * verticalExaggeration;
        double minHeight = globe.getMinElevation(sector) * verticalExaggeration;

        Vec4[] points = new Vec4[9];
        points[0] = globe.computePointFromPosition(center.getLatitude(), center.getLongitude(), maxHeight);
        points[1] = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMinLongitude(), maxHeight);
        points[2] = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMaxLongitude(), maxHeight);
        points[3] = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), maxHeight);
        points[4] = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), maxHeight);
        points[5] = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMinLongitude(), minHeight);
        points[6] = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMaxLongitude(), minHeight);
        points[7] = globe.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), minHeight);
        points[8] = globe.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), minHeight);

        return Sphere.createBoundingSphere(points);
    }

    public final boolean contains(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return latitude.degrees >= this.minLatitude.degrees
            && latitude.degrees <= this.maxLatitude.degrees
            && longitude.degrees >= this.minLongitude.degrees
            && longitude.degrees <= this.maxLongitude.degrees;
    }

    /**
     * Determines whether a latitude/longitude position is within the sector. The sector's angles are assumed to be
     * normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the operation is undefined if
     * they are not.
     *
     * @param latLon the position to test, with angles normalized to +/- &#960 latitude and +/- 2&#960 longitude.
     * @return <code>true</code> if the position is within the sector, <code>false</code> otherwise.
     * @throws IllegalArgumentException if <code>latlon</code> is null.
     */
    public final boolean contains(LatLon latLon)
    {
        if (latLon == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.contains(latLon.getLatitude(), latLon.getLongitude());
    }

    /**
     * Determines whether a latitude/longitude postion expressed in radians is within the sector. The sector's angles
     * are assumed to be normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the
     * operation is undefined if they are not.
     *
     * @param radiansLatitude  the latitude in radians of the position to test, normalized +/- &#960.
     * @param radiansLongitude the longitude in radians of the position to test, normalized +/- 2&#960.
     * @return <code>true</code> if the position is within the sector, <code>false</code> otherwise.
     */
    public final boolean containsRadians(double radiansLatitude, double radiansLongitude)
    {
        return radiansLatitude >= this.minLatitude.radians && radiansLatitude <= this.maxLatitude.radians
            && radiansLongitude >= this.minLongitude.radians && radiansLongitude <= this.maxLongitude.radians;
    }

    public final boolean containsDegrees(double degreesLatitude, double degreesLongitude)
    {
        return degreesLatitude >= this.minLatitude.degrees && degreesLatitude <= this.maxLatitude.degrees
            && degreesLongitude >= this.minLongitude.degrees && degreesLongitude <= this.maxLongitude.degrees;
    }

    /**
     * Determines whether this sector intersects another sector's range of latitude and longitude. The sector's angles
     * are assumed to be normalized to +/- 90 degrees latitude and +/- 180 degrees longitude. The result of the
     * operation is undefined if they are not.
     *
     * @param that the sector to test for intersection.
     * @return <code>true</code> if the sectors intersect, otherwise <code>false</code>.
     */
    public boolean intersects(Sector that)
    {
        if (that == null)
            return false;

        // Assumes normalized angles -- [-180, 180], [-90, 90] // TODO: have Angle normalize values when set
        if (that.maxLongitude.degrees < this.minLongitude.degrees)
            return false;
        if (that.minLongitude.degrees > this.maxLongitude.degrees)
            return false;
        if (that.maxLatitude.degrees < this.minLatitude.degrees)
            return false;
        //noinspection RedundantIfStatement
        if (that.minLatitude.degrees > this.maxLatitude.degrees)
            return false;

        return true;
    }

    /**
     * Returns a new sector whose angles are the extremes of the this sector and another. The new sector's minimum
     * latitude and longitude will be the minimum of the two sectors. The new sector's maximum latitude and longitude
     * will be the maximum of the two sectors. The sectors are assumed to be normalized to +/- 90 degrees latitude and
     * +/- 180 degrees longitude. The result of the operation is undefined if they are not.
     *
     * @param that the sector to join with <code>this</code>.
     * @return A new sector formed from the extremes of the two sectors, or <code>this</code> if the incoming sector is
     *         <code>null</code>.
     */
    public final Sector union(Sector that)
    {
        if (that == null)
            return this;

        Angle minLat = this.minLatitude;
        Angle maxLat = this.maxLatitude;
        Angle minLon = this.minLongitude;
        Angle maxLon = this.maxLongitude;

        if (that.minLatitude.degrees < this.minLatitude.degrees)
            minLat = that.minLatitude;
        if (that.maxLatitude.degrees > this.maxLatitude.degrees)
            maxLat = that.maxLatitude;
        if (that.minLongitude.degrees < this.minLongitude.degrees)
            minLon = that.minLongitude;
        if (that.maxLongitude.degrees > this.maxLongitude.degrees)
            maxLon = that.maxLongitude;

        return new Sector(minLat, maxLat, minLon, maxLon);
    }

    public final Sector union(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
            return this;

        Angle minLat = this.minLatitude;
        Angle maxLat = this.maxLatitude;
        Angle minLon = this.minLongitude;
        Angle maxLon = this.maxLongitude;

        if (latitude.degrees < this.minLatitude.degrees)
            minLat = latitude;
        if (latitude.degrees > this.maxLatitude.degrees)
            maxLat = latitude;
        if (longitude.degrees < this.minLongitude.degrees)
            minLon = longitude;
        if (longitude.degrees > this.maxLongitude.degrees)
            maxLon = longitude;

        return new Sector(minLat, maxLat, minLon, maxLon);
    }

    public static Sector union(Sector sectorA, Sector sectorB)
    {
        if (sectorA == null || sectorB == null)
        {
            if (sectorA == sectorB)
                return null; // TODO: throw exception

            return sectorB == null ? sectorA : sectorB;
        }

        return sectorA.union(sectorB);
    }

    public final Sector intersection(Sector that)
    {
        if (that == null)
            return this;

        Angle minLat, maxLat;
        minLat = (this.minLatitude.degrees > that.minLatitude.degrees) ? this.minLatitude : that.minLatitude;
        maxLat = (this.maxLatitude.degrees < that.maxLatitude.degrees) ? this.maxLatitude : that.maxLatitude;
        if (minLat.degrees > maxLat.degrees)
            return null;

        Angle minLon, maxLon;
        minLon = (this.minLongitude.degrees > that.minLongitude.degrees) ? this.minLongitude : that.minLongitude;
        maxLon = (this.maxLongitude.degrees < that.maxLongitude.degrees) ? this.maxLongitude : that.maxLongitude;
        if (minLon.degrees > maxLon.degrees)
            return null;

        return new Sector(minLat, maxLat, minLon, maxLon);
    }

    public final Sector intersection(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
            return this;

        if (!this.contains(latitude, longitude))
            return null;
        return new Sector(latitude, latitude, longitude, longitude);
    }

    public final Sector[] subdivide()
    {
        Angle midLat = Angle.average(this.minLatitude, this.maxLatitude);
        Angle midLon = Angle.average(this.minLongitude, this.maxLongitude);

        Sector[] sectors = new Sector[4];
        sectors[0] = new Sector(this.minLatitude, midLat, this.minLongitude, midLon);
        sectors[1] = new Sector(this.minLatitude, midLat, midLon, this.maxLongitude);
        sectors[2] = new Sector(midLat, this.maxLatitude, this.minLongitude, midLon);
        sectors[3] = new Sector(midLat, this.maxLatitude, midLon, this.maxLongitude);

        return sectors;
    }

    /**
     * Returns a string indicating the sector's angles.
     *
     * @return A string indicating the sector's angles.
     */
    @Override
    public String toString()
    {
        java.lang.StringBuffer sb = new java.lang.StringBuffer();
        sb.append("(");
        sb.append(this.minLatitude.toString());
        sb.append(", ");
        sb.append(this.minLongitude.toString());
        sb.append(")");

        sb.append(", ");

        sb.append("(");
        sb.append(this.maxLatitude.toString());
        sb.append(", ");
        sb.append(this.maxLongitude.toString());
        sb.append(")");

        return sb.toString();
    }

    /**
     * Retrieve the size of this object in bytes. This implementation returns an exact value of the object's size.
     *
     * @return the size of this object in bytes
     */
    public long getSizeInBytes()
    {
        return 4 * minLatitude.getSizeInBytes();  // 4 angles
    }

    /**
     * Compares this sector to a specified sector according to their minimum latitude, minimum longitude, maximum
     * latitude, and maximum longitude, respectively.
     *
     * @param that the <code>Sector</code> to compareTo with <code>this</code>.
     * @return -1 if this sector compares less than that specified, 0 if they're equal, and 1 if it compares greater.
     * @throws IllegalArgumentException if <code>that</code> is null
     */
    public int compareTo(Sector that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.getMinLatitude().compareTo(that.getMinLatitude()) < 0)
            return -1;

        if (this.getMinLatitude().compareTo(that.getMinLatitude()) > 0)
            return 1;

        if (this.getMinLongitude().compareTo(that.getMinLongitude()) < 0)
            return -1;

        if (this.getMinLongitude().compareTo(that.getMinLongitude()) > 0)
            return 1;

        if (this.getMaxLatitude().compareTo(that.getMaxLatitude()) < 0)
            return -1;

        if (this.getMaxLatitude().compareTo(that.getMaxLatitude()) > 0)
            return 1;

        if (this.getMaxLongitude().compareTo(that.getMaxLongitude()) < 0)
            return -1;

        if (this.getMaxLongitude().compareTo(that.getMaxLongitude()) > 0)
            return 1;

        return 0;
    }

    /**
     * Tests the equality of the sectors' angles. Sectors are equal if all of their corresponding angles are equal.
     *
     * @param o the sector to compareTo with <code>this</code>.
     * @return <code>true</code> if the four corresponding angles of each sector are equal, <code>false</code>
     *         otherwise.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final gov.nasa.worldwind.geom.Sector sector = (gov.nasa.worldwind.geom.Sector) o;

        if (!maxLatitude.equals(sector.maxLatitude))
            return false;
        if (!maxLongitude.equals(sector.maxLongitude))
            return false;
        if (!minLatitude.equals(sector.minLatitude))
            return false;
        //noinspection RedundantIfStatement
        if (!minLongitude.equals(sector.minLongitude))
            return false;

        return true;
    }

    /**
     * Computes a hash code from the sector's four angles.
     *
     * @return a hash code incorporating the sector's four angles.
     */
    @Override
    public int hashCode()
    {
        int result;
        result = minLatitude.hashCode();
        result = 29 * result + maxLatitude.hashCode();
        result = 29 * result + minLongitude.hashCode();
        result = 29 * result + maxLongitude.hashCode();
        return result;
    }
}