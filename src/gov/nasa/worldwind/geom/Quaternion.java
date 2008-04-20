/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * @author Chris Maxwell
 * @version $Id: Quaternion.java 3295 2007-10-15 22:44:21Z dcollins $
 */
public class Quaternion
{
    // Multiplicative identity quaternion.
    public static final Quaternion IDENTITY = new Quaternion(0, 0, 0, 1);

    public final double x;
    public final double y;
    public final double z;
    public final double w;

    // 4 values in a quaternion.
    private static final int NUM_ELEMENTS = 4;
    // Cached computations.
    private int hashCode;

    public Quaternion(double x, double y, double z, double w)
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

        Quaternion that = (Quaternion) obj;
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
            result = 31 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.z);
            result = 31 * result + (int) (tmp ^ (tmp >>> 32));
            tmp = Double.doubleToLongBits(this.w);
            result = 31 * result + (int) (tmp ^ (tmp >>> 32));
            this.hashCode = result;
        }
        return this.hashCode;
    }

    public static Quaternion fromArray(double[] compArray, int offset)
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
        return new Quaternion(
            compArray[0 + offset],
            compArray[1 + offset],
            compArray[2 + offset],
            compArray[3 + offset]);
    }

    public final double[] toArray(double[] compArray, int offset)
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

    public static Quaternion fromAxisAngle(Angle angle, Vec4 axis)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        if (axis == null)
        {
            String msg = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return fromAxisAngle(angle, axis.x, axis.y, axis.z, true);
    }

    public static Quaternion fromAxisAngle(Angle angle, double axisX, double axisY, double axisZ)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        return fromAxisAngle(angle, axisX, axisY, axisZ, true);
    }

    private static Quaternion fromAxisAngle(Angle angle, double axisX, double axisY, double axisZ, boolean normalize)
    {
        if (angle == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (normalize)
        {
            double length = Math.sqrt((axisX * axisX) + (axisY * axisY) + (axisZ * axisZ));
            if (!isZero(length) && (length != 1.0))
            {
                axisX /= length;
                axisY /= length;
                axisZ /= length;
            }
        }

        double s = angle.sinHalfAngle();
        double c = angle.cosHalfAngle();
        return new Quaternion(axisX * s, axisY * s, axisZ * s, c);
    }

    public static Quaternion fromMatrix(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        
        double t = 1.0 + matrix.m11 + matrix.m22 + matrix.m33;
        double x, y, z, w;
        double s;
        final double EPSILON = 0.00000001;
        if (t > EPSILON) {
			s = 2.0 * Math.sqrt(t);
			x = (matrix.m32 - matrix.m23) / s;
            y = (matrix.m13 - matrix.m31) / s;
            z = (matrix.m21 - matrix.m12) / s;
            w = s / 4.0;
        } else if ((matrix.m11 > matrix.m22) && (matrix.m11 > matrix.m33)) {
			s = 2.0 * Math.sqrt(1.0 + matrix.m11 - matrix.m22 - matrix.m33);
			x = s / 4.0;
			y = (matrix.m21 + matrix.m12) / s;
			z = (matrix.m13 + matrix.m31) / s;
			w = (matrix.m32 - matrix.m23) / s;
		} else if (matrix.m22 > matrix.m33) {
			s = 2.0 * Math.sqrt(1.0 + matrix.m22 - matrix.m11 - matrix.m33);
			x = (matrix.m21 + matrix.m12) / s;
			y = s / 4.0;
			z = (matrix.m32 + matrix.m23) / s;
			w = (matrix.m13 - matrix.m31) / s;
		} else {
			s = 2.0 * Math.sqrt(1.0 + matrix.m33 - matrix.m11 - matrix.m22);
			x = (matrix.m13 + matrix.m31) / s;
			y = (matrix.m32 + matrix.m23) / s;
			z = s / 4.0;
			w = (matrix.m21 - matrix.m12) / s;
		}
        return new Quaternion(x, y, z, w);
    }

    public static Quaternion fromRotationYPR(Angle yaw, Angle pitch, Angle roll)
    {
        if ((yaw == null) || (pitch == null) || (roll == null))
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double cy = yaw.cosHalfAngle();
        double cp = pitch.cosHalfAngle();
        double cr = roll.cosHalfAngle();
        double sy = yaw.sinHalfAngle();
        double sp = pitch.sinHalfAngle();
        double sr = roll.sinHalfAngle();

        double qw = (cy * cp * cr) + (sy * sp * sr);
        double qx = (sy * cp * cr) - (cy * sp * sr);
        double qy = (cy * sp * cr) + (sy * cp * sr);
        double qz = (cy * cp * sr) - (sy * sp * cr);

        return new Quaternion(qx, qy, qz, qw);
    }

    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //
    // ============== Arithmetic Functions ======================= //

    public final Quaternion add(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Quaternion(
            this.x + quaternion.x,
            this.y + quaternion.y,
            this.z + quaternion.z,
            this.w + quaternion.w);
    }

    public final Quaternion subtract(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Quaternion(
            this.x - quaternion.x,
            this.y - quaternion.y,
            this.z - quaternion.z,
            this.w - quaternion.w);
    }

    public final Quaternion multiplyComponents(double value)
    {
        return new Quaternion(
            this.x * value,
            this.y * value,
            this.z * value,
            this.w * value);
    }

    public final Quaternion multiply(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Quaternion(
            (this.w * quaternion.x) + (this.x * quaternion.w) + (this.y * quaternion.z) - (this.z * quaternion.y),
            (this.w * quaternion.y) + (this.y * quaternion.w) + (this.z * quaternion.x) - (this.x * quaternion.z),
            (this.w * quaternion.z) + (this.z * quaternion.w) + (this.x * quaternion.y) - (this.y * quaternion.x),
            (this.w * quaternion.w) - (this.x * quaternion.x) - (this.y * quaternion.y) - (this.z * quaternion.z));
    }

    public final Quaternion divideComponents(double value)
    {
        if (isZero(value))
        {
            String msg = Logging.getMessage("generic.ArgumentOutOfRange", value);
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Quaternion(
            this.x / value,
            this.y / value,
            this.z / value,
            this.w / value);
    }

    public final Quaternion divideComponents(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Quaternion(
            this.x / quaternion.x,
            this.y / quaternion.y,
            this.z / quaternion.z,
            this.w / quaternion.w);
    }

    public final Quaternion getConjugate()
    {
        return new Quaternion(
            0.0 - this.x,
            0.0 - this.y,
            0.0 - this.z,
            this.w);
    }

    public final Quaternion getNegative()
    {
        return new Quaternion(
            0.0 - this.x,
            0.0 - this.y,
            0.0 - this.z,
            0.0 - this.w);   
    }

    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //

    public final double getLength()
    {
        return Math.sqrt(this.getLengthSquared());
    }

    public final double getLengthSquared()
    {
        return (this.x * this.x)
             + (this.y * this.y)
             + (this.z * this.z)
             + (this.w * this.w);
    }

    public final Quaternion normalize()
    {
        double length = this.getLength();
        // Vector has zero length.
        if (isZero(length))
        {
            return this;
        }
        else
        {
            return new Quaternion(
                this.x / length,
                this.y / length,
                this.z / length,
                this.w / length);
        }
    }

    public final double dot(Quaternion quaternion)
    {
        if (quaternion == null)
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return (this.x * quaternion.x) + (this.y * quaternion.y) + (this.z * quaternion.z) + (this.w * quaternion.w);
    }

    public final Quaternion getInverse()
    {
        double length = this.getLength();
        // Vector has zero length.
        if (isZero(length))
        {
            return this;
        }
        else
        {
            return new Quaternion(
                (0.0 - this.x) / length,
                (0.0 - this.y) / length,
                (0.0 - this.z) / length,
                this.w / length);
        }
    }

    // ============== Mixing Functions ======================= //
    // ============== Mixing Functions ======================= //
    // ============== Mixing Functions ======================= //

    public static Quaternion mix(double amount, Quaternion value1, Quaternion value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (amount < 0.0)
            return value1;
        else if (amount > 1.0)
            return value2;

        double t1 = 1.0 - amount;
        return new Quaternion(
            (value1.x * t1) + (value2.x * amount),
            (value1.y * t1) + (value2.y * amount),
            (value1.z * t1) + (value2.z * amount),
            (value1.w * t1) + (value2.w * amount));
    }

    public static Quaternion slerp(double amount, Quaternion value1, Quaternion value2)
    {
        if ((value1 == null) || (value2 == null))
        {
            String msg = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (amount < 0.0)
            return value1;
        else if (amount > 1.0)
            return value2;

        double dot = value1.dot(value2);
        double x2, y2, z2, w2;
        if (dot < 0.0)
        {
            dot = 0.0 - dot;
            x2 = 0.0 - value2.x;
            y2 = 0.0 - value2.y;
            z2 = 0.0 - value2.z;
            w2 = 0.0 - value2.w;
        }
        else
        {
            x2 = value2.x;
            y2 = value2.y;
            z2 = value2.z;
            w2 = value2.w;
        }

        double t1, t2;

        final double EPSILON = 0.0001;
        if ((1.0 - dot) > EPSILON) // standard case (slerp)
        {
            double angle = Math.acos(dot);
            double sinAngle = Math.sin(angle);
            t1 = Math.sin((1.0 - amount) * angle) / sinAngle;
            t2 = Math.sin(amount * angle) / sinAngle;
        }
        else // just lerp
        {
            t1 = 1.0 - amount;
            t2 = amount;
        }

        return new Quaternion(
            (value1.x * t1) + (x2 * t2),
            (value1.y * t1) + (y2 * t2),
            (value1.z * t1) + (z2 * t2),
            (value1.w * t1) + (w2 * t2));
    }

    // ============== Accessor Functions ======================= //
    // ============== Accessor Functions ======================= //
    // ============== Accessor Functions ======================= //

    public final Angle getAngle()
    {
        double w = this.w;

        double length = this.getLength();
        if (!isZero(length) && (length != 1.0))
            w /= length;

        double radians = 2.0 * Math.acos(w);
        if (Double.isNaN(radians))
            return null;

        return Angle.fromRadians(radians);
    }

    public final Vec4 getAxis()
    {
        double x = this.x;
        double y = this.y;
        double z = this.z;

        double length = this.getLength();
        if (!isZero(length) && (length != 1.0))
        {
            x /= length;
            y /= length;
            z /= length;
        }

        double vecLength = Math.sqrt((x * x) + (y * y) + (z * z));
        if (!isZero(vecLength) && (vecLength != 1.0))
        {
            x /= vecLength;
            y /= vecLength;
            z /= vecLength;
        }

        return new Vec4(x, y, z);
    }

    public final Angle getRotationX()
    {
        double radians = Math.atan2(
            2.0 * (this.y * this.z + this.w * this.x),
            (this.w * this.w) - (this.x * this.x) - (this.y * this.y) + (this.z * this.z));
        if (Double.isNaN(radians))
            return null;

        return Angle.fromRadians(radians);
    }

    public final Angle getRotationY()
    {
        double radians = Math.asin(-2.0 * (this.x * this.z - this.w * this.y));
        if (Double.isNaN(radians))
            return null;

        return Angle.fromRadians(radians);
    }

    public final Angle getRotationZ()
    {
        double radians = Math.atan2(
            2.0 * (this.x * this.y + this.w * this.z),
            (this.w * this.w) + (this.x * this.x) - (this.y * this.y) - (this.z * this.z));
        if (Double.isNaN(radians))
            return null;

        return Angle.fromRadians(radians);
    }

    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //

    private static final Double PositiveZero = +0.0d;

    private static final Double NegativeZero = -0.0d;

    private static boolean isZero(double value)
    {
        return (PositiveZero.compareTo(value) == 0)
            || (NegativeZero.compareTo(value) == 0);
    }

//    /**
//     * @param q
//     * @return
//     * @throws IllegalArgumentException if <code>q</code> is null
//     */
//    public static double Norm2(Quaternion q)
//    {
//        if (q == null)
//        {
//            String msg = gov.nasa.worldwind.WorldWind.retrieveErrMsg("nullValue.QuaternionIsNull");
//            gov.nasa.worldwind.WorldWind.logger().logger(Level.SEVERE, msg);
//            throw new IllegalArgumentException(msg);
//        }
//        return q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
//    }
//
//    /**
//     * @param q
//     * @return
//     * @throws IllegalArgumentException if <code>q</code> is null
//     */
//    public static double Abs(Quaternion q)
//    {
//        if (q == null)
//        {
//            String msg = gov.nasa.worldwind.WorldWind.retrieveErrMsg("nullValue.QuaternionIsNull");
//            gov.nasa.worldwind.WorldWind.logger().logger(Level.SEVERE, msg);
//            throw new IllegalArgumentException(msg);
//        }
//        return Math.sqrt(Norm2(q));
//    }
//
//    /**
//     * @param a
//     * @param b
//     * @return
//     * @throws IllegalArgumentException if <code>a</code> or <code>b</code> is null
//     */
//    public static Quaternion Divide(Quaternion a, Quaternion b)
//    {
//        if (a == null || b == null)
//        {
//            String msg = gov.nasa.worldwind.WorldWind.retrieveErrMsg("nullValue.QuaternionIsNull");
//            gov.nasa.worldwind.WorldWind.logger().logger(Level.SEVERE, msg);
//            throw new IllegalArgumentException(msg);
//        }
//        return Quaternion.Multiply(a, Quaternion.Divide(b.Conjugate(), Abs(b)));
//    }


//    /*****************the below functions have not been certified to work properly ******************/
//
//    public Quaternion Ln()
//    {
//        return Ln(this);
//    }
//
//    /**
//     * @param q
//     * @return
//     * @throws IllegalArgumentException if <code>q</code> is null
//     */
//    public static Quaternion Ln(Quaternion q)
//    {
//        if (q == null)
//        {
//            String msg = gov.nasa.worldwind.WorldWind.retrieveErrMsg("nullValue.QuaternionIsNull");
//            gov.nasa.worldwind.WorldWind.logger().logger(Level.SEVERE, msg);
//            throw new IllegalArgumentException(msg);
//        }
//        double t;
//
//        double s = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
//        double om = Math.atan2(s, q.w);
//
//        if (Math.abs(s) < epsilon)
//            t = 0.0f;
//        else
//            t = om / s;
//
//        return new Quaternion(q.x * t, q.y * t, q.z * t, 0.0f);
//    }
//
//    /**
//     * @param q
//     * @return
//     * @throws IllegalArgumentException if <code>q</code> is null
//     */
//    public static Quaternion Exp(Quaternion q)
//    {
//        if (q == null)
//        {
//            String msg = gov.nasa.worldwind.WorldWind.retrieveErrMsg("nullValue.QuaternionIsNull");
//            gov.nasa.worldwind.WorldWind.logger().logger(Level.SEVERE, msg);
//            throw new IllegalArgumentException(msg);
//        }
//        double sinom;
//        double om = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
//
//        if (Math.abs(om) < epsilon)
//            sinom = 1.0;
//        else
//            sinom = Math.sin(om) / om;
//
//        return new Quaternion(q.x * sinom, q.y * sinom, q.z * sinom, Math.cos(om));
//    }
//
//    public Quaternion Exp()
//    {
//        return Ln(this);
//    }
//
//    /**
//     * @param q1
//     * @param a
//     * @param b
//     * @param c
//     * @param t
//     * @return
//     * @throws IllegalArgumentException if any argument is null
//     */
//    public static Quaternion Squad(
//        Quaternion q1,
//        Quaternion a,
//        Quaternion b,
//        Quaternion c,
//        double t)
//    {
//        if (q1 == null || a == null || b == null || c == null)
//        {
//            String msg = gov.nasa.worldwind.WorldWind.retrieveErrMsg("nullValue.QuaternionIsNull");
//            gov.nasa.worldwind.WorldWind.logger().logger(Level.SEVERE, msg);
//            throw new IllegalArgumentException(msg);
//        }
//        return Slerp(
//            Slerp(q1, c, t), Slerp(a, b, t), 2 * t * (1.0 - t));
//    }
//
//    // This needs to be accounted for before Squad() is used
//    public static Quaternion[] SquadSetup(
//        Quaternion4d q0,
//        Quaternion4d q1,
//        Quaternion4d q2,
//        Quaternion4d q3)
//    {
//        if(q0 == null || q1 == null || q2 == null || q3 == null)
//        {
//            String msg = gov.nasa.worldwind.WorldWind.retrieveErrMsg("nullValue.QuaternionIsNull");
//            gov.nasa.worldwind.WorldWind.logger().logger(Level.SEVERE, msg);
//            throw new IllegalArgumentException(msg);
//        }
//
//        q0 = q0 + q1;
//        q0.Normalize();
//
//        q2 = q2 + q1;
//        q2.Normalize();
//
//        q3 = q3 + q1;
//        q3.Normalize();
//
//        q1.Normalize();
//
//        Quaternion[] ret = new Quaternion[3];
//
//        ret[0] = q1 * Exp(-0.25 * (Ln(Exp(q1) * q2) + Ln(Exp(q1) * q0))); // outA
//        ret[1] = q2 * Exp(-0.25 * (Ln(Exp(q2) * q3) + Ln(Exp(q2) * q1))); // outB
//        ret[2] = q2;                                                      // outC
//
//    }
}
