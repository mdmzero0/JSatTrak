/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * Represents a point on the two-dimensional surface of a globe. Latitude is the degrees North and ranges between [-90,
 * 90], while longitude refers to degrees East, and ranges between (-180, 180].
 * <p/>
 * Instances of <code>LatLon</code> are immutable.
 *
 * @author Tom Gaskins
 * @version $Id: LatLon.java 3665 2007-11-29 22:01:11Z dcollins $
 */
public class LatLon
{
    public static final LatLon ZERO = new LatLon(Angle.ZERO, Angle.ZERO);

    private final Angle latitude;
    private final Angle longitude;

    /**
     * Factor method for obtaining a new <code>LatLon</code> from two angles expressed in radians.
     *
     * @param latitude  in radians
     * @param longitude in radians
     * @return a new <code>LatLon</code> from the given angles, which are expressed as radians
     */
    public static LatLon fromRadians(double latitude, double longitude)
    {
        return new LatLon(Math.toDegrees(latitude), Math.toDegrees(longitude));
    }

    /**
     * Factory method for obtaining a new <code>LatLon</code> from two angles expressed in degrees.
     *
     * @param latitude  in degrees
     * @param longitude in degrees
     * @return a new <code>LatLon</code> from the given angles, which are expressed as degrees
     */
    public static LatLon fromDegrees(double latitude, double longitude)
    {
        return new LatLon(latitude, longitude);
    }

    private LatLon(double latitude, double longitude)
    {
        this.latitude = Angle.fromDegrees(latitude);
        this.longitude = Angle.fromDegrees(longitude);
    }

    /**
     * Contructs a new  <code>LatLon</code> from two angles. Neither angle may be null.
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @throws IllegalArgumentException if <code>latitude</code> or <code>longitude</code> is null
     */
    public LatLon(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Obtains the latitude of this <code>LatLon</code>.
     *
     * @return this <code>LatLon</code>'s latitude
     */
    public final Angle getLatitude()
    {
        return this.latitude;
    }

    /**
     * Obtains the longitude of this <code>LatLon</code>.
     *
     * @return this <code>LatLon</code>'s longitude
     */
    public final Angle getLongitude()
    {
        return this.longitude;
    }

    public static LatLon interpolate(double amount, LatLon value1, LatLon value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (amount < 0)
            return value1;
        else if (amount > 1)
            return value2;

        Quaternion beginQuat = Quaternion.fromRotationYPR(value1.getLongitude(), value1.getLatitude(), Angle.ZERO);
        Quaternion endQuat = Quaternion.fromRotationYPR(value2.getLongitude(), value2.getLatitude(), Angle.ZERO);
        Quaternion quaternion = Quaternion.slerp(amount, beginQuat, endQuat);

        Angle lat = quaternion.getRotationY();
        Angle lon = quaternion.getRotationX();
        if ((lat == null) || (lon == null))
            return null;

        return new LatLon(lat, lon);
    }

    /**
     * Computes the great circle angular distance between two locations. The return value gives the distance as the
     * angle between the two positions on the pi radius circle. In radians, this angle is also the arc length of the
     * segment between the two positions on that circle. To compute a distance in meters from this value, multiply it by
     * the radius of the globe.
     *
     * @param p1 LatLon of the first location
     * @param p2 LatLon of the second location
     * @return the angular distance between the two locations. In radians, this value is the arc length of the radius pi
     *         circle.
     */
    public static Angle sphericalDistance(LatLon p1, LatLon p2)
    {
        if ((p1 == null) || (p2 == null))
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;

        if (lat1 == lat2 && lon1 == lon2)
            return Angle.ZERO;

        // Taken from "Map Projections - A Working Manual", page 30, equation 5-3a.
        // The traditional d=2*asin(a) form has been replaced with d=2*atan2(sqrt(a), sqrt(1-a))
        // to reduce rounding errors with large distances.
        double a = Math.sin((lat2 - lat1) / 2.0) * Math.sin((lat2 - lat1) / 2.0)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin((lon2 - lon1) / 2.0) * Math.sin((lon2 - lon1) / 2.0);
        double distanceRadians = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        if (Double.isNaN(distanceRadians))
            return Angle.ZERO;

        return Angle.fromRadians(distanceRadians);
    }

    public static Angle azimuth(LatLon p1, LatLon p2)
    {
        if ((p1 == null) || (p2 == null))
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat1 = p1.getLatitude().radians;
        double lon1 = p1.getLongitude().radians;
        double lat2 = p2.getLatitude().radians;
        double lon2 = p2.getLongitude().radians;

        if (lat1 == lat2 && lon1 == lon2)
            return Angle.ZERO;

        if (lon1 == lon2)
            return lat1 > lat2 ? Angle.POS180 : Angle.ZERO;

        // Taken from "Map Projections - A Working Manual", page 30, equation 5-4b.
        // The atan2() function is used in place of the traditional atan(y/x) to simplify the case when x==0.
        double y = Math.cos(lat2) * Math.sin(lon2 - lon1);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);
        double azimuthRadians = Math.atan2(y, x);

        if (Double.isNaN(azimuthRadians))
            return Angle.ZERO;

        return Angle.fromRadians(azimuthRadians);
    }

    public static LatLon endPosition(LatLon p, double azimuthRadians, double pathLengthRadians)
    {
        if (p == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double lat = p.getLatitude().radians;
        double lon = p.getLongitude().radians;

        // Taken from "Map Projections - A Working Manual", page 31, equation 5-5 and 5-6.
        double endLatRadians = Math.asin(Math.sin(lat) * Math.cos(pathLengthRadians)
                + Math.cos(lat) * Math.sin(pathLengthRadians) * Math.cos(azimuthRadians));
        double endLonRadians = lon + Math.atan2(
                Math.sin(pathLengthRadians) * Math.sin(azimuthRadians),
                Math.cos(lat) * Math.cos(pathLengthRadians)
                        - Math.sin(lat) * Math.sin(pathLengthRadians) * Math.cos(azimuthRadians));

        if (Double.isNaN(endLatRadians) || Double.isNaN(endLonRadians))
            return LatLon.ZERO;

        return new LatLon(
                Angle.fromRadians(endLatRadians).normalizedLatitude(),
                Angle.fromRadians(endLonRadians).normalizedLongitude());
    }

    public LatLon add(LatLon that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.normalizedLatitude(this.latitude.add(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.add(that.longitude));

        return new LatLon(lat, lon);
    }

    public LatLon subtract(LatLon that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.normalizedLatitude(this.latitude.subtract(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.subtract(that.longitude));

        return new LatLon(lat, lon);
    }

    public LatLon add(Position that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.normalizedLatitude(this.latitude.add(that.getLatitude()));
        Angle lon = Angle.normalizedLongitude(this.longitude.add(that.getLongitude()));

        return new LatLon(lat, lon);
    }

    public LatLon subtract(Position that)
    {
        if (that == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Angle lat = Angle.normalizedLatitude(this.latitude.subtract(that.getLatitude()));
        Angle lon = Angle.normalizedLongitude(this.longitude.subtract(that.getLongitude()));

        return new LatLon(lat, lon);
    }

    public static boolean positionsCrossDateLine(Iterable<LatLon> positions)
    {
        if (positions == null)
        {
            String msg = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        LatLon pos = null;
        for (LatLon posNext : positions)
        {
            if (pos != null)
            {
                // A segment cross the line if end pos have different longitude signs
                // and are more than 180 degress longitude apart
                if (Math.signum(pos.getLongitude().degrees) != Math.signum(posNext.getLongitude().degrees))
                {
                    double delta = Math.abs(pos.getLongitude().degrees - posNext.getLongitude().degrees);
                    if (delta > 180 && delta < 360)
                        return true;
                }
            }
            pos = posNext;
        }

        return false;
    }

    public static boolean positionsCrossLongitudeBoundary(LatLon p1, LatLon p2)
    {
        if (p1 == null || p2 == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // A segment cross the line if end pos have different longitude signs
        // and are more than 180 degress longitude apart
        if (Math.signum(p1.getLongitude().degrees) != Math.signum(p2.getLongitude().degrees))
        {
            double delta = Math.abs(p1.getLongitude().degrees - p2.getLongitude().degrees);
            if (delta > 180 && delta < 360)
                return true;
        }

        return false;
    }

    @Override
    public String toString()
    {
        String las = String.format("Lat %7.4f\u00B0", this.getLatitude().getDegrees());
        String los = String.format("Lon %7.4f\u00B0", this.getLongitude().getDegrees());
        return "(" + las + ", " + los + ")";
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final gov.nasa.worldwind.geom.LatLon latLon = (gov.nasa.worldwind.geom.LatLon) o;

        if (!latitude.equals(latLon.latitude))
            return false;
        //noinspection RedundantIfStatement
        if (!longitude.equals(latLon.longitude))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        result = latitude.hashCode();
        result = 29 * result + longitude.hashCode();
        return result;
    }

    /**
     * Compute the forward azimuth between two positions
     *
     * @param p1               first position
     * @param p2               second position
     * @param equatorialRadius the equatorial radius of the globe in meters
     * @param polarRadius      the polar radius of the globe in meters
     * @return the azimuth
     */
    public Angle ellipsoidalForwardAzimuth(LatLon p1, LatLon p2, double equatorialRadius, double polarRadius)
    {
        // TODO: What if polar radius is larger than equatorial radius?
        final double F = (equatorialRadius - polarRadius) / equatorialRadius; // flattening = 1.0 / 298.257223563;
        final double R = 1.0 - F;

        if (p1 == null || p2 == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // See ellipsoidalDistance() below for algorithm info.

        double GLAT1 = p1.getLatitude().radians;
        double GLAT2 = p2.getLatitude().radians;
        double TU1 = R * Math.sin(GLAT1) / Math.cos(GLAT1);
        double TU2 = R * Math.sin(GLAT2) / Math.cos(GLAT2);
        double CU1 = 1. / Math.sqrt(TU1 * TU1 + 1.);
        double CU2 = 1. / Math.sqrt(TU2 * TU2 + 1.);
        double S = CU1 * CU2;
        double BAZ = S * TU2;
        double FAZ = BAZ * TU1;

        return Angle.fromRadians(FAZ);
    }

    // TODO: Need method to compute end position from initial position, azimuth and distance. The companion to the
    // spherical version, endPosition(), above.

    /**
     * Computes the distance between two points on an ellipsoid iteratively.
     * <p/>
     * NOTE: This method was copied from the UniData NetCDF Java library. http://www.unidata.ucar.edu/software/netcdf-java/
     * <p/>
     * Algorithm from U.S. National Geodetic Survey, FORTRAN program "inverse," subroutine "INVER1," by L. PFEIFER and
     * JOHN G. GERGEN. See http://www.ngs.noaa.gov/TOOLS/Inv_Fwd/Inv_Fwd.html
     * <p/>
     * Original documentation: SOLUTION OF THE GEODETIC INVERSE PROBLEM AFTER T.VINCENTY MODIFIED RAINSFORD'S METHOD
     * WITH HELMERT'S ELLIPTICAL TERMS EFFECTIVE IN ANY AZIMUTH AND AT ANY DISTANCE SHORT OF ANTIPODAL
     * STANDPOINT/FOREPOINT MUST NOT BE THE GEOGRAPHIC POLE
     * <p/>
     * Requires close to 1.4 E-5 seconds wall clock time per call on a 550 MHz Pentium with Linux 7.2.
     *
     * @param p1               first position
     * @param p2               second position
     * @param equatorialRadius the equatorial radius of the globe in meters
     * @param polarRadius      the polar radius of the globe in meters
     * @return distance in meters between the two points
     */
    public static double ellipsoidalDistance(LatLon p1, LatLon p2, double equatorialRadius, double polarRadius)
    {
        // TODO: I think there is a non-iterative way to calculate the distance. Find it and compare with this one.
        // TODO: What if polar radius is larger than equatorial radius?
        final double F = (equatorialRadius - polarRadius) / equatorialRadius; // flattening = 1.0 / 298.257223563;
        final double R = 1.0 - F;
        final double EPS = 0.5E-13;

        if (p1 == null || p2 == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Algorithm from National Geodetic Survey, FORTRAN program "inverse,"
        // subroutine "INVER1," by L. PFEIFER and JOHN G. GERGEN.
        // http://www.ngs.noaa.gov/TOOLS/Inv_Fwd/Inv_Fwd.html
        // Conversion to JAVA from FORTRAN was made with as few changes as possible
        // to avoid errors made while recasting form, and to facilitate any future
        // comparisons between the original code and the altered version in Java.
        // Original documentation:
        // SOLUTION OF THE GEODETIC INVERSE PROBLEM AFTER T.VINCENTY
        // MODIFIED RAINSFORD'S METHOD WITH HELMERT'S ELLIPTICAL TERMS
        // EFFECTIVE IN ANY AZIMUTH AND AT ANY DISTANCE SHORT OF ANTIPODAL
        // STANDPOINT/FOREPOINT MUST NOT BE THE GEOGRAPHIC POLE
        // A IS THE SEMI-MAJOR AXIS OF THE REFERENCE ELLIPSOID
        // F IS THE FLATTENING (NOT RECIPROCAL) OF THE REFERNECE ELLIPSOID
        // LATITUDES GLAT1 AND GLAT2
        // AND LONGITUDES GLON1 AND GLON2 ARE IN RADIANS POSITIVE NORTH AND EAST
        // FORWARD AZIMUTHS AT BOTH POINTS RETURNED IN RADIANS FROM NORTH
        //
        // Reference ellipsoid is the WGS-84 ellipsoid.
        // See http://www.colorado.edu/geography/gcraft/notes/datum/elist.html
        // FAZ is forward azimuth in radians from pt1 to pt2;
        // BAZ is backward azimuth from point 2 to 1;
        // S is distance in meters.
        //
        // Conversion to JAVA from FORTRAN was made with as few changes as possible
        // to avoid errors made while recasting form, and to facilitate any future
        // comparisons between the original code and the altered version in Java.
        //
        //IMPLICIT REAL*8 (A-H,O-Z)
        //  COMMON/CONST/PI,RAD

        double GLAT1 = p1.getLatitude().radians;
        double GLAT2 = p2.getLatitude().radians;
        double TU1 = R * Math.sin(GLAT1) / Math.cos(GLAT1);
        double TU2 = R * Math.sin(GLAT2) / Math.cos(GLAT2);
        double CU1 = 1. / Math.sqrt(TU1 * TU1 + 1.);
        double SU1 = CU1 * TU1;
        double CU2 = 1. / Math.sqrt(TU2 * TU2 + 1.);
        double S = CU1 * CU2;
        double BAZ = S * TU2;
        double FAZ = BAZ * TU1;
        double GLON1 = p1.getLongitude().radians;
        double GLON2 = p2.getLongitude().radians;
        double X = GLON2 - GLON1;
        double D, SX, CX, SY, CY, Y, SA, C2A, CZ, E, C;
        do
        {
            SX = Math.sin(X);
            CX = Math.cos(X);
            TU1 = CU2 * SX;
            TU2 = BAZ - SU1 * CU2 * CX;
            SY = Math.sqrt(TU1 * TU1 + TU2 * TU2);
            CY = S * CX + FAZ;
            Y = Math.atan2(SY, CY);
            SA = S * SX / SY;
            C2A = -SA * SA + 1.;
            CZ = FAZ + FAZ;
            if (C2A > 0.)
            {
                CZ = -CZ / C2A + CY;
            }
            E = CZ * CZ * 2. - 1.;
            C = ((-3. * C2A + 4.) * F + 4.) * C2A * F / 16.;
            D = X;
            X = ((E * CY * C + CZ) * SY * C + Y) * SA;
            X = (1. - C) * X * F + GLON2 - GLON1;
            //IF(DABS(D-X).GT.EPS) GO TO 100
        } while (Math.abs(D - X) > EPS);

        //FAZ = Math.atan2(TU1, TU2);
        //BAZ = Math.atan2(CU1 * SX, BAZ * CX - SU1 * CU2) + Math.PI;
        X = Math.sqrt((1. / R / R - 1.) * C2A + 1.) + 1.;
        X = (X - 2.) / X;
        C = 1. - X;
        C = (X * X / 4. + 1.) / C;
        D = (0.375 * X * X - 1.) * X;
        X = E * CY;
        S = 1. - E - E;
        S = ((((SY * SY * 4. - 3.) * S * CZ * D / 6. - X) * D / 4. + CZ) * SY
            * D + Y) * C * equatorialRadius * R;

        return S;
    }
}
