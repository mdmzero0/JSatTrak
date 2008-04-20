/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: Vec4.java 3295 2007-10-15 22:44:21Z dcollins $
 */
public class Vec4
{
    public static final Vec4 INFINITY =
        new Vec4(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
    public static final Vec4 ZERO = new Vec4(0, 0, 0, 1);
    public static final Vec4 ONE = new Vec4(1, 1, 1, 1);
    public static final Vec4 UNIT_X = new Vec4(1, 0, 0, 0);
    public static final Vec4 UNIT_NEGATIVE_X = new Vec4(-1, 0, 0, 0);
    public static final Vec4 UNIT_Y = new Vec4(0, 1, 0, 0);
    public static final Vec4 UNIT_NEGATIVE_Y = new Vec4(0, -1, 0, 0);
    public static final Vec4 UNIT_Z = new Vec4(0, 0, 1, 0);
    public static final Vec4 UNIT_NEGATIVE_Z = new Vec4(0, 0, -1, 0);
    public static final Vec4 UNIT_W = new Vec4(0, 0, 0, 1);
    public static final Vec4 UNIT_NEGATIVE_W = new Vec4(0, 0, 0, -1);

    public final double x;
    public final double y;
    public final double z;
    public final double w;

    // Default W-component will be 1 to handle Matrix transformation.
    private static final double DEFAULT_W = 1.0;
    // 4 values in a 4-component vector.
    private static final int NUM_ELEMENTS = 4;
    // Cached computations.
    private int hashCode;

    public Vec4(double value)
    {
        this(value, value, value);
    }

    public Vec4(double x, double y, double z)
    {
        this(x, y, z, DEFAULT_W);
    }

    public Vec4(double x, double y, double z, double w)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public final boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || obj.getClass() != this.getClass())
            return false;

        Vec4 that = (Vec4) obj;
        return (this.x == that.x)
            && (this.y == that.y)
            && (this.z == that.z)
            && (this.w == that.w);
    }

    public final int hashCode()
    {
        if (this.hashCode == 0)
        {
            int result;
            long tmp;
            tmp = Double.doubleToLongBits(this.x);
            result = (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.y);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.z);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.w);
            result = 29 * result + (int) (tmp ^ (tmp >>> 32));
            this.hashCode = result;
        }
        return this.hashCode;
    }

    public static Vec4 fromArray3(double[] compArray, int offset)
    {
        if (compArray == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if ((compArray.length - offset) < (NUM_ELEMENTS - 1))
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", compArray.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        //noinspection PointlessArithmeticExpression
        return new Vec4(
            compArray[0 + offset],
            compArray[1 + offset],
            compArray[2 + offset]);
    }

    public static Vec4 fromArray4(double[] compArray, int offset)
    {
        if (compArray == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if ((compArray.length - offset) < NUM_ELEMENTS)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", compArray.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        //noinspection PointlessArithmeticExpression
        return new Vec4(
            compArray[0 + offset],
            compArray[1 + offset],
            compArray[2 + offset],
            compArray[3 + offset]);
    }

    public final double[] toArray3(double[] compArray, int offset)
    {
        if (compArray == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if ((compArray.length - offset) < (NUM_ELEMENTS - 1))
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", compArray.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        //noinspection PointlessArithmeticExpression
        compArray[0 + offset] = this.x;
        compArray[1 + offset] = this.y;
        compArray[2 + offset] = this.z;
        return compArray;
    }

    public final double[] toArray4(double[] compArray, int offset)
    {
        if (compArray == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if ((compArray.length - offset) < NUM_ELEMENTS)
        {
            String msg = Logging.getMessage("generic.ArrayInvalidLength", compArray.length);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        //noinspection PointlessArithmeticExpression
        compArray[0 + offset] = this.x;
        compArray[1 + offset] = this.y;
        compArray[2 + offset] = this.z;
        compArray[3 + offset] = this.w;
        return compArray;
    }

    public final String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append(this.x).append(", ");
        sb.append(this.y).append(", ");
        sb.append(this.z).append(", ");
        sb.append(this.w);
        sb.append(")");
        return sb.toString();
    }

    public final double getX()
    {
        return this.x;
    }

    public final double getY()
    {
        return this.y;
    }

    public final double getZ()
    {
        return this.z;
    }

    public final double getW()
    {
        return this.w;
    }

    public final double x()
    {
        return this.x;
    }

    public final double y()
    {
        return this.y;
    }

    public final double z()
    {
        return this.z;
    }

    public final double w()
    {
        return this.w;
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    public static Vec4 fromLine3(Vec4 origin, double t, Vec4 direction)
    {
        if (origin == null || direction == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            origin.x + (direction.x * t),
            origin.y + (direction.y * t),
            origin.z + (direction.z * t));

//        return fromLine3(
//            origin.x, origin.y, origin.z,
//            direction.x, direction.y, direction.z,
//            t,
//            true);
    }
//
//    private static Vec4 fromLine3(
//        double px, double py, double pz,
//        double dx, double dy, double dz,
//        double t,
//        boolean normalize)
//    {
//        if (normalize)
//        {
//            double dLength = Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
//            if (!isZero(dLength) && (dLength != 1.0))
//            {
//                dx /= dLength;
//                dy /= dLength;
//                dz /= dLength;
//            }
//        }
//
//        return new Vec4(
//            px + (dx * t),
//            py + (dy * t),
//            pz + (dz * t));
//    }

    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //

    public final Vec4 add3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x + vec4.x,
            this.y + vec4.y,
            this.z + vec4.z,
            this.w);
    }

    public final Vec4 subtract3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x - vec4.x,
            this.y - vec4.y,
            this.z - vec4.z,
            this.w);
    }

    public final Vec4 multiply3(double value)
    {
        return new Vec4(
            this.x * value,
            this.y * value,
            this.z * value,
            this.w);
    }

    public final Vec4 multiply3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x * vec4.x,
            this.y * vec4.y,
            this.z * vec4.z,
            this.w);
    }

    public final Vec4 divide3(double value)
    {
        if (isZero(value))
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", value);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x / value,
            this.y / value,
            this.z / value,
            this.w);
    }

    public final Vec4 divide3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            this.x / vec4.x,
            this.y / vec4.y,
            this.z / vec4.z,
            this.w);
    }

    public final Vec4 getNegative3()
    {
        return new Vec4(
            0.0 - this.x,
            0.0 - this.y,
            0.0 - this.z,
            this.w);
    }

    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //

    public final double getLength3()
    {
        return Math.sqrt(this.getLengthSquared3());
    }

    public final double getLengthSquared3()
    {
        return (this.x * this.x)
             + (this.y * this.y)
             + (this.z * this.z);
    }

    public final Vec4 normalize3()
    {
        double length = this.getLength3();
        // Vector has zero length.
        if (isZero(length))
        {
            return this;
        }
        else
        {
            return new Vec4(
                this.x / length,
                this.y / length,
                this.z / length);
        }
    }

    public final double distanceTo3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return Math.sqrt(this.distanceToSquared3(vec4));
    }

    public final double distanceToSquared3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double tmp;
        double result = 0.0;
        tmp = this.x - vec4.x;
        result += tmp * tmp;
        tmp = this.y - vec4.y;
        result += tmp * tmp;
        tmp = this.z - vec4.z;
        result += tmp * tmp;
        return result;
    }

    public final double dot3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return (this.x * vec4.x) + (this.y * vec4.y) + (this.z * vec4.z);
    }

    public final double dot4(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return (this.x * vec4.x) + (this.y * vec4.y) + (this.z * vec4.z) + (this.w * vec4.w);
    }

    public final Vec4 cross3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (this.y * vec4.z) - (this.z * vec4.y),
            (this.z * vec4.x) - (this.x * vec4.z),
            (this.x * vec4.y) - (this.y * vec4.x));
    }

    public final Angle angleBetween3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double a_dot_b = this.dot3(vec4);
        // Compute the sum of magnitudes.
        double length = this.getLength3() * vec4.getLength3();
        // Normalize the dot product, if necessary.
        if (!isZero(length) && (length != 1.0))
            a_dot_b /= length;

        // Return null when dot-product is outside range [-1, 1].
        if ((a_dot_b < -1.0) || (a_dot_b > 1.0))
            return null;

        // Angle is arc-cosine of normalized dot product.
        return Angle.fromRadians(Math.acos(a_dot_b));
    }

    /**
     * Compute the angle and rotation axis required to rotate one vector to align with another.
     *
     * @param v1     The base vector.
     * @param v2     The vector to rotate into alignment with <code>v1</code>.
     * @param result A reference to an array in which to return the computed axis. May not be null.
     * @return The rotation angle.
     * @throws IllegalArgumentException if any parameter is null.
     */
    public static Angle axisAngle(Vec4 v1, Vec4 v2, Vec4[] result)
    {
        if (v1 == null || v2 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (result == null)
        {
            String msg = Logging.getMessage("nullValue.ArrayIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute rotation angle
        Vec4 u1 = v1.normalize3();
        Vec4 u0 = v2.normalize3();
        Angle angle = Angle.fromRadians(Math.acos(u0.x * u1.x + u0.y * u1.y + u0.z * u1.z));

        // Compute rotation axis
        double A = (u0.y * u1.z) - (u0.z * u1.y);
        double B = (u0.z * u1.x) - (u0.x * u1.z);
        double C = (u0.x * u1.y) - (u0.y * u1.x);
        double L = Math.sqrt(A * A + B * B + C * C);
        result[0] = new Vec4(A / L, B / L, C / L);

        return angle;
    }

    public final Vec4 projectOnto3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double dot = this.dot3(vec4);
        double length = vec4.getLength3();
        // Normalize the dot product, if necessary.
        if (!isZero(length) && (length != 1.0))
            dot /= (length * length);
        return vec4.multiply3(dot);
    }

    public final Vec4 perpendicularTo3(Vec4 vec4)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.subtract3(projectOnto3(vec4));
    }

    public final Vec4 transformBy3(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (matrix.m11 * this.x) + (matrix.m12 * this.y) + (matrix.m13 * this.z),
            (matrix.m21 * this.x) + (matrix.m22 * this.y) + (matrix.m23 * this.z),
            (matrix.m31 * this.x) + (matrix.m32 * this.y) + (matrix.m33 * this.z));
    }

    public final Vec4 transformBy3(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Quaternion tmp = new Quaternion(this.x, this.y, this.z, 0.0);
        tmp = quaternion.multiply(tmp);
        tmp = tmp.multiply(quaternion.getInverse());
        return new Vec4(tmp.x, tmp.y, tmp.z, 0.0);
    }

    public final Vec4 transformBy4(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (matrix.m11 * this.x) + (matrix.m12 * this.y) + (matrix.m13 * this.z) + (matrix.m14 * this.w),
            (matrix.m21 * this.x) + (matrix.m22 * this.y) + (matrix.m23 * this.z) + (matrix.m24 * this.w),
            (matrix.m31 * this.x) + (matrix.m32 * this.y) + (matrix.m33 * this.z) + (matrix.m34 * this.w),
            (matrix.m41 * this.x) + (matrix.m42 * this.y) + (matrix.m43 * this.z) + (matrix.m44 * this.w));
    }

    // ============== Mixing Functions ======================= //
    // ============== Mixing Functions ======================= //
    // ============== Mixing Functions ======================= //

    public static Vec4 min3(Vec4 value1, Vec4 value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (value1.x < value2.x) ? value1.x : value2.x,
            (value1.y < value2.y) ? value1.y : value2.y,
            (value1.x < value2.z) ? value1.z : value2.z);
    }

    public static Vec4 max3(Vec4 value1, Vec4 value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (value1.x > value2.x) ? value1.x : value2.x,
            (value1.y > value2.y) ? value1.y : value2.y,
            (value1.x > value2.z) ? value1.z : value2.z);
    }

    public static Vec4 clamp3(Vec4 vec4, double min, double max)
    {
        if (vec4 == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(
            (vec4.x < min) ? min : ((vec4.x > max) ? max : vec4.x),
            (vec4.y < min) ? min : ((vec4.y > max) ? max : vec4.y),
            (vec4.z < min) ? min : ((vec4.z > max) ? max : vec4.z));
    }

    public static Vec4 mix3(double amount, Vec4 value1, Vec4 value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (amount < 0.0)
            return value1;
        else if (amount > 1.0)
            return value2;

        double t1 = 1.0 - amount;
        return new Vec4(
            (value1.x * t1) + (value2.x * amount),
            (value1.y * t1) + (value2.y * amount),
            (value1.z * t1) + (value2.z * amount));
    }

    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //

    private static final Double POSITIVE_ZERO = +0.0d;

    private static final Double NEGATIVE_ZERO = -0.0d;

    private static boolean isZero(double value)
    {
        return (POSITIVE_ZERO.compareTo(value) == 0)
            || (NEGATIVE_ZERO.compareTo(value) == 0);
    }
}
