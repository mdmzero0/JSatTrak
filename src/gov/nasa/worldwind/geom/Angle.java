/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * Represents a geometric angle. Instances of <code>Angle</code> are immutable. An <code>Angle</code> can be obtained
 * through the factory methods <code>fromDegrees</code> and <code>fromRadians</code>.
 *
 * @author Tom Gaskins
 * @version $Id: Angle.java 2566 2007-08-16 22:26:09Z tgaskins $
 */
public class Angle implements Comparable<Angle>
{
    /**
     * Represents an angle of zero degrees
     */
    public final static Angle ZERO = Angle.fromDegrees(0);

    /**
     * Represents a right angle of positive 90 degrees
     */
    public final static Angle POS90 = Angle.fromDegrees(90);

    /**
     * Represents a right angle of negative 90 degrees
     */
    public final static Angle NEG90 = Angle.fromDegrees(-90);

    /**
     * Represents an angle of positive 180 degrees
     */
    public final static Angle POS180 = Angle.fromDegrees(180);

    /**
     * Represents an angle of negative 180 degrees
     */
    public final static Angle NEG180 = Angle.fromDegrees(-180);

    /**
     * Represents an angle of positive 360 degrees
     */
    public final static Angle POS360 = Angle.fromDegrees(360);

    private final static double DEGREES_TO_RADIANS = Math.PI / 180d;
    private final static double RADIANS_TO_DEGREES = 180d / Math.PI;

    /**
     * Obtains an <code>Angle</code> from a specified number of degrees.
     *
     * @param degrees the size in degrees of the <code>Angle</code> to be obtained
     * @return a new <code>Angle</code>, whose size in degrees is given by <code>degrees</code>
     */
    public static Angle fromDegrees(double degrees)
    {
        return new Angle(degrees, DEGREES_TO_RADIANS * degrees);
    }

    /**
     * Obtains an <code>Angle</code> from a specified number of radians.
     *
     * @param radians the size in radians of the <code>Angle</code> to be obtained
     * @return a new <code>Angle</code>, whose size in radians is given by <code>radians</code>
     */
    public static Angle fromRadians(double radians)
    {
        return new Angle(RADIANS_TO_DEGREES * radians, radians);
    }

    private static final double PIOver2 = Math.PI / 2;

    public static Angle fromDegreesLatitude(double degrees)
    {
        degrees = degrees < -90 ? -90 : degrees > 90 ? 90 : degrees;
        double radians = DEGREES_TO_RADIANS * degrees;
        radians = radians < -PIOver2 ? -PIOver2 : radians > PIOver2 ? PIOver2 : radians;

        return new Angle(degrees, radians);
    }

    public static Angle fromRadiansLatitude(double radians)
    {
        radians = radians < -PIOver2 ? -PIOver2 : radians > PIOver2 ? PIOver2 : radians;
        double degrees = RADIANS_TO_DEGREES * radians;
        degrees = degrees < -90 ? -90 : degrees > 90 ? 90 : degrees;

        return new Angle(degrees, radians);
    }

    public static Angle fromDegreesLongitude(double degrees)
    {
        degrees = degrees < -180 ? -180 : degrees > 180 ? 180 : degrees;
        double radians = DEGREES_TO_RADIANS * degrees;
        radians = radians < -Math.PI ? -Math.PI : radians > Math.PI ? Math.PI : radians;

        return new Angle(degrees, radians);
    }

    public static Angle fromRadiansLongitude(double radians)
    {
        radians = radians < -Math.PI ? -Math.PI : radians > Math.PI ? Math.PI : radians;
        double degrees = RADIANS_TO_DEGREES * radians;
        degrees = degrees < -180 ? -180 : degrees > 180 ? 180 : degrees;

        return new Angle(degrees, radians);
    }

    /**
     * Obtains an <code>Angle</code> from rectangular coordinates.
     *
     * @param x the abscissa coordinate
     * @param y the ordinate coordinate
     * @return a new <code>Angle</code>, whose size is determined from <code>x</code> and <code>y</code>
     */
    public static Angle fromXY(double x, double y)
    {
        double radians = Math.atan2(y, x);
        return new Angle(RADIANS_TO_DEGREES * radians, radians);
    }

    public final double degrees;
    public final double radians;

    public Angle(Angle angle)
    {
        this.degrees = angle.degrees;
        this.radians = angle.radians;
    }
//
//    private Angle(double degrees)
//    {
//        this.degrees = degrees;
//        this.radians = DEGREES_TO_RADIANS * this.degrees;
//    }

    private Angle(double degrees, double radians)
    {
        this.degrees = degrees;
        this.radians = radians;
    }

    /**
     * Retrieves the size of this <code>Angle</code> in degrees. This method may be faster than first obtaining the
     * radians and then converting to degrees.
     *
     * @return the size of this <code>Angle</code> in degrees
     */
    public final double getDegrees()
    {
        return this.degrees;
    }

    /**
     * Retrieves the size of this <code>Angle</code> in radians. This may be useful for <code>java.lang.Math</code>
     * functions, which generally take radians as trigonometric arguments. This method may be faster that first
     * obtaining the degrees and then converting to radians.
     *
     * @return the size of this <code>Angle</code> in radians.
     */
    public final double getRadians()
    {
        return this.radians;
    }

    /**
     * Obtains the sum of these two <code>Angle</code>s. Does not accept a null argument. This method is commutative, so
     * <code>a.add(b)</code> and <code>b.add(a)</code> are equivalent. Neither this <code>Angle</code> nor
     * <code>angle</code> is changed, instead the result is returned as a new <code>Angle</code>.
     *
     * @param angle the <code>Angle</code> to add to this one.
     * @return an <code>Angle</code> whose size is the total of this <code>Angle</code>s and <code>angle</code>s size
     * @throws IllegalArgumentException if <code>angle</code> is null
     */
    public final Angle add(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(this.degrees + angle.degrees);
    }

    /**
     * Obtains the difference of these two <code>Angle</code>s. Does not accept a null argument. This method is not
     * commutative. Neither this <code>Angle</code> nor <code>angle</code> is changed, instead the result is returned as
     * a new <code>Angle</code>.
     *
     * @param angle the <code>Angle</code> to subtract from this <code>Angle</code>
     * @return a new <code>Angle</code> correpsonding to this <code>Angle</code>'s size minus <code>angle</code>'s size
     * @throws IllegalArgumentException if <code>angle</code> is null
     */
    public final Angle subtract(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(this.degrees - angle.degrees);
    }

    /**
     * Multiplies this <code>Angle</code> by <code>multiplier</code>. This <code>Angle</code> remains unchanged. The
     * result is returned as a new <code>Angle</code>.
     *
     * @param multiplier a scalar by which this <code>Angle</code> is multiplied
     * @return a new <code>Angle</code> whose size equals this <code>Angle</code>'s size multiplied by
     *         <code>multiplier</code>
     */
    public final Angle multiply(double multiplier)
    {
        return Angle.fromDegrees(this.degrees * multiplier);
    }

    /**
     * Divides this <code>Angle</code> by another angle. This <code>Angle</code> remains unchanged, instead the
     * resulting value in degrees is returned.
     *
     * @param angle the <code>Angle</code> by which to divide
     * @return this <code>Angle</code>'s degrees divided by <code>angle</code>'s degrees
     * @throws IllegalArgumentException if <code>angle</code> is null
     */
    public final double divide(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.degrees / angle.degrees;
    }

    public final Angle addDegrees(double degrees)
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Angle.fromDegrees(this.degrees + degrees);
    }

    public final Angle subtractDegrees(double degrees)
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Angle.fromDegrees(this.degrees - degrees);
    }

    /**
     * Divides this <code>Angle</code> by <code>divisor</code>. This <code>Angle</code> remains unchanged. The result is
     * returned as a new <code>Angle</code>. Behaviour is undefined if <code>divisor</code> equals zero.
     *
     * @param divisor the number to be divided by
     * @return a new <code>Angle</code> equivalent to this <code>Angle</code> divided by <code>divisor</code>
     */
    public final Angle divide(double divisor)
    {
        return Angle.fromDegrees(this.degrees / divisor);
    }

    public final Angle addRadians(double radians)
    {
        return Angle.fromRadians(this.radians + radians);
    }

    public final Angle subtractRadians(double radians)
    {
        return Angle.fromRadians(this.radians - radians);
    }

    /**
     * Computes the shortest distance between this and <code>angle</code>, as an
     * <code>Angle</code>.
     *
     * @param angle the <code>Angle</code> to measure angular distance to.
     * @return the angular distance between this and <code>value</code>.
     */
    public Angle angularDistanceTo(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double differenceDegrees = angle.subtract(this).degrees;
        if (differenceDegrees < -180)
            differenceDegrees += 360;
        else if (differenceDegrees > 180)
            differenceDegrees -= 360;

        double absAngle = Math.abs(differenceDegrees);
        return Angle.fromDegrees(absAngle);
    }

    /**
     * Obtains the sine of this <code>Angle</code>.
     *
     * @return the trigonometric sine of this <code>Angle</code>
     */
    public final double sin()
    {
        return Math.sin(this.radians);
    }

    public final double sinHalfAngle()
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Math.sin(0.5 * this.radians);
    }

    public static Angle asin(double sine)
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Angle.fromRadians(Math.asin(sine));
    }

    /**
     * Obtains the cosine of this <code>Angle</code>
     *
     * @return the trigonometric cosine of this <code>Angle</code>
     */
    public final double cos()
    {
        return Math.cos(this.radians);
    }

    public final double cosHalfAngle()
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Math.cos(0.5 * this.radians);
    }

    public static Angle acos(double cosine)
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Angle.fromRadians(Math.acos(cosine));
    }

    /**
     * Obtains the tangent of half of this <code>Angle</code>.
     *
     * @return the trigonometric tangent of half of this <code>Angle</code>
     */
    public final double tanHalfAngle()
    {
        return Math.tan(0.5 * this.radians);
    }

    public static Angle atan(double tan)
    {   //Tom: this method is not used, should we delete it? (13th Dec 06)
        return Angle.fromRadians(Math.atan(tan));
    }

    /**
     * Obtains the average of two <code>Angle</code>s. This method is commutative, so <code>midAngle(m, n)</code> and
     * <code>midAngle(n, m)</code> are equivalent.
     *
     * @param a1 the first <code>Angle</code>
     * @param a2 the second <code>Angle</code>
     * @return the average of <code>a1</code> and <code>a2</code> throws IllegalArgumentException if either angle is
     *         null
     */
    public static Angle midAngle(Angle a1, Angle a2)
    {
        if (a1 == null || a2 == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(0.5 * (a1.degrees + a2.degrees));
    }

    /**
     * Obtains the average of three <code>Angle</code>s. The order of parameters does not matter.
     *
     * @param a the first <code>Angle</code>
     * @param b the second <code>Angle</code>
     * @return the average of <code>a1</code>, <code>a2</code> and <code>a3</code>
     * @throws IllegalArgumentException if <code>a</code> or <code>b</code> is null
     */
    public static Angle average(Angle a, Angle b)
    {
        if (a == null || b == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees(0.5 * (a.degrees + b.degrees));
    }

    /**
     * Obtains the average of three <code>Angle</code>s. The order of parameters does not matter.
     *
     * @param a the first <code>Angle</code>
     * @param b the second <code>Angle</code>
     * @param c the third <code>Angle</code>
     * @return the average of <code>a1</code>, <code>a2</code> and <code>a3</code>
     * @throws IllegalArgumentException if <code>a</code>, <code>b</code> or <code>c</code> is null
     */
    public static Angle average(Angle a, Angle b, Angle c)
    {
        if (a == null || b == null || c == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Angle.fromDegrees((a.degrees + b.degrees + c.degrees) / 3);
    }

    /**
     * Linearly interpolates between two angles.
     *
     * @param amount the interpolant.
     * @param value1 the first <code>Angle</code>.
     * @param value2 the second <code>Angle</code>.
     * @return a new <code>Angle</code> between <code>value1</code> and <code>value2</code>.
     */
    public static Angle mix(double amount, Angle value1, Angle value2)
    {
        if (value1 == null || value2 == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (amount < 0)
            return value1;
        else if (amount > 1)
            return value2;

        Quaternion quat = Quaternion.slerp(
            amount,
            Quaternion.fromRotationYPR(value1, Angle.ZERO, Angle.ZERO),
            Quaternion.fromRotationYPR(value2, Angle.ZERO, Angle.ZERO));

        Angle angle = quat.getRotationX();
        if (Double.isNaN(angle.degrees))
            return null;

        return angle;
    }

    /**
     * Compares this <code>Angle</code> with <code>angle</code> for order. Returns a negative integer if this is the
     * smaller <code>Angle</code>, a positive integer if this is the larger, and zero if both <code>Angle</code>s are
     * equal.
     *
     * @param angle the <code>Angle</code> to compare against
     * @return -1 if this <code>Angle</code> is smaller, 0 if both are equal and +1 if this <code>Angle</code> is
     *         larger.
     * @throws IllegalArgumentException if <code>angle</code> is null
     */
    public final int compareTo(Angle angle)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.degrees < angle.degrees)
            return -1;

        if (this.degrees > angle.degrees)
            return 1;

        return 0;
    }

    private static double normalizedDegreesLatitude(double degrees)
    {
        double lat = degrees % 180;
        return lat > 90 ? 180 - lat : lat < -90 ? -180 - lat : lat;
    }

    private static double normalizedDegreesLongitude(double degrees)
    {
        double lon = degrees % 360;
        return lon > 180 ? lon - 360 : lon < -180 ? 360 + lon : lon;
    }

    public static Angle normalizedLatitude(Angle unnormalizedAngle)
    {
        if (unnormalizedAngle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return Angle.fromDegrees(normalizedDegreesLatitude(unnormalizedAngle.degrees));
    }

    public static Angle normalizedLongitude(Angle unnormalizedAngle)
    {
        if (unnormalizedAngle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return Angle.fromDegrees(normalizedDegreesLongitude(unnormalizedAngle.degrees));
    }

    public Angle normalizedLatitude()
    {
        return normalizedLatitude(this);
    }

    public Angle normalizedLongitude()
    {
        return normalizedLongitude(this);
    }

    public static boolean crossesLongitudeBoundary(Angle angleA, Angle angleB)
    {
        if (angleA == null || angleB == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // A segment cross the line if end pos have different longitude signs
        // and are more than 180 degress longitude apart
        return (Math.signum(angleA.degrees) != Math.signum(angleB.degrees))
            && (Math.abs(angleA.degrees - angleB.degrees) > 180);
    }

    /**
     * Obtains a <code>String</code> representation of this <code>Angle</code>.
     *
     * @return the value of this <code>Angle</code> in degrees and as a <code>String</code>
     */
    @Override
    public final String toString()
    {
        return Double.toString(this.degrees) + '\u00B0';
    }

    /**
     * Obtains the amount of memory this <code>Angle</code> consumes.
     *
     * @return the memory footprint of this <code>Angle</code> in bytes.
     */
    public long getSizeInBytes()
    {
        return Double.SIZE;
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Angle angle = (Angle) o;

        //noinspection RedundantIfStatement
        if (angle.degrees!= this.degrees)
            return false;

        return true;
    }

    public int hashCode()
    {
        long temp = degrees != +0.0d ? Double.doubleToLongBits(degrees) : 0L;
        return (int) (temp ^ (temp >>> 32));
    }
}
