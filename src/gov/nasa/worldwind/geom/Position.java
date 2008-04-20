/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: Position.java 3293 2007-10-15 03:22:54Z tgaskins $
 */
public class Position
{
    private final Angle latitude;
    private final Angle longitude;
    private final double elevation;

    public static final Position ZERO = new Position(Angle.ZERO, Angle.ZERO, 0d);

    public static Position fromRadians(double latitude, double longitude, double elevation)
    {
        return new Position(Angle.fromRadians(latitude), Angle.fromRadians(longitude), elevation);
    }

    public static Position fromDegrees(double latitude, double longitude, double elevation)
    {
        return new Position(Angle.fromDegrees(latitude), Angle.fromDegrees(longitude), elevation);
    }

    public Position(Angle latitude, Angle longitude, double elevation)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public Position(LatLon latLon, double elevation)
    {
        if (latLon == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.latitude = latLon.getLatitude();
        this.longitude = latLon.getLongitude();
        this.elevation = elevation;
    }

    /**
     * Obtains the latitude of this position
     *
     * @return this position's latitude
     */
    public final Angle getLatitude()
    {
        return this.latitude;
    }

    /**
     * Obtains the longitude of this position
     *
     * @return this position's longitude
     */
    public final Angle getLongitude()
    {
        return this.longitude;
    }

    public LatLon getLatLon()
    {
        return new LatLon(this.getLatitude(), this.getLongitude());
    }

    public Position add(Position that)
    {
        Angle lat = Angle.normalizedLatitude(this.latitude.add(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.add(that.longitude));

        return new Position(lat, lon, this.elevation + that.elevation);
    }

    public Position subtract(Position that)
    {
        Angle lat = Angle.normalizedLatitude(this.latitude.subtract(that.latitude));
        Angle lon = Angle.normalizedLongitude(this.longitude.subtract(that.longitude));

        return new Position(lat, lon, this.elevation - that.elevation);
    }

    public static Position interpolate(double amount, Position value1, Position value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
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

        return new Position(lat, lon, amount * value2.getElevation() + (1 - amount) * value1.getElevation());
    }

    /**
     * Obtains the elevation of this position
     *
     * @return this position's elevation
     */
    public final double getElevation()
    {
        return this.elevation;
    }

    public String toString()
    {
        return "(" + this.latitude.toString() + ", " + this.longitude.toString() + ", " + this.elevation + ")";
    }

    public static boolean positionsCrossDateLine(Iterable<Position> positions)
    {
        if (positions == null)
        {
            String msg = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Position pos = null;
        for (Position posNext : positions)
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
}
