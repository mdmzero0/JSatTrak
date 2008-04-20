/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * A <code>Plane</code> object represents a mathematical plane in an arbitrary cartesian co-ordinate system. A
 * <code>Plane</code> is defined by a normal vector and a distance along that vector from the origin, where the distance
 * represents the distance from the origin to the <code>Plane</code> rather than from the <code>Plane</code> to the
 * origin.
 * <p/>
 * <p/>
 * Instances of <code>Plane</code> are immutable. </p>
 *
 * @author Tom Gaskins
 * @version $Id: Plane.java 3252 2007-10-10 15:20:12Z tgaskins $
 */
public final class Plane
{

    /**
     * Represents all the information about this <code>Plane</code>. The first three values (<code>x, y, z</code>) of
     * <code>v</code> represent a normal <code>Vector</code> to the <code>Plane</code>, while the fourth
     * (<code>w</code>) represents the signed distance this <code>Plane</code> has been shifted along that normal.
     */
    private final Vec4 n;

    /**
     * Obtains a new instance of a <code>Plane</code> whose information is contained in <code>Vector</code>
     * <code>vec</code>.
     *
     * @param vec the <code>Vector</code> containing information about this <code>Plane</code>'s normal and distance
     * @throws IllegalArgumentException if passed a null or zero-length <code>Vector</code>
     */
    public Plane(Vec4 vec)
    {
        if (vec == null)
        {
            String message = Logging.getMessage("nullValue.VectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (vec.getLengthSquared3() == 0.0)
        {
            String message = Logging.getMessage("Geom.Plane.VectorIsZero");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.n = vec;
    }

    /**
     * Obtains a new <code>Plane</code> whose normal is defined by the vector (a,b,c) and whose disance from that vector
     * is d. The vector may not have zero length.
     *
     * @param a the x-parameter of the normal to this <code>Plane</code>
     * @param b the y-parameter of the normal to this <code>Plane</code>
     * @param c the z-parameter of the normal to this <code>Plane</code>
     * @param d the distance of this <code>Plane</code> from the origin along its normal.
     * @throws IllegalArgumentException if <code>0==a==b==c</code>
     */
    public Plane(double a, double b, double c, double d)
    {
        if (a == 0.0 && b == 0.0 && c == 0.0)
        {
            String message = Logging.getMessage("Geom.Plane.VectorIsZero");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.n = new Vec4(a, b, c, d);
    }

    /**
     * Retrieves a <code>Vec4</code> representing the normal to this <code>Plane</code>.
     *
     * @return a <code>Vec4</code> representing the normal to this <code>Plane</code>
     */
    public final Vec4 getNormal()
    {
        return new Vec4(this.n.x, this.n.y, this.n.z);
    }

    /**
     * Retrieves the distance from the origin to this <code>Plane</code>. Two options exist for defining distance - the
     * first represents the distance from the origin to the <code>Plane</code>, the second represents the distance from
     * the <code>Plane</code> to the origin. This function uses the first method. The outcome of this is that depending
     * on the caller's view of this method, the sign of distances may appear to be reversed.
     *
     * @return the distance between this <code>Plane</code> and the origin
     */
    public final double getDistance()
    {
        return this.n.w;
    }

    /**
     * Retrieves a vector representing the normal and distance to this <code>Plane</code>. The vector has the structure
     * (x, y, z, distance), where (x, y, z) represents the normal, and distance represents the distance from the
     * origin.
     *
     * @return a <code>Vector</code> representation of this <code>Plane</code>
     */
    public final Vec4 getVector()
    {
        return this.n;
    }

    /**
     * Calculates the dot product of this <code>Plane</code> with Vec4 <code>p</code>.
     *
     * @param p the Vec4 to dot with this <code>Plane</code>
     * @return the dot product of <code>p</code> and this <code>Plane</code>
     * @throws IllegalArgumentException if <code>p</code> is null
     */
    public final double dot(Vec4 p)
    {
        if (p == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.n.x * p.x + this.n.y * p.y + this.n.z * p.z + this.n.w * p.w;
    }

    public final Plane computeParallelPlaneAtDistance(double distance)
    {
        return new Plane(this.n.x, this.n.y, this.n.z, distance);
    }

    /**
     * Determine the point of intersection of a line with this plane.
     *
     * @param line the line to test
     * @return The point on the line at which it intersects the plane. null is returned if the line does not intersect
     *         the plane. The line's origin is returned if the line is coincident with the plane.
     */
    public Vec4 intersect(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double t = this.intersectDistance(line);

        if (Double.isNaN(t))
            return null;

        if (Double.isInfinite(t))
            return line.getOrigin();

        return line.getPointAt(t);
    }

    /**
     * Determine the parametric point of intersection of a line with this plane.
     *
     * @param line the line to test
     * @return The parametric value of the point on the line at which it intersects the plane. {@link Double#NaN} is
     *         returned if the line does not intersect the plane. {@link Double#POSITIVE_INFINITY} is returned if the
     *         line is coincident with the plane.
     */
    public double intersectDistance(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double ldotv = this.n.dot3(line.getDirection());
        if (ldotv == 0) // are line and plane perpendicular
        {
            double ldots = this.n.dot4(line.getOrigin());
            if (ldots == 0)
                return Double.POSITIVE_INFINITY; // line is coincident with the plane
            else
                return Double.NaN; // line is not coincident with the plane
        }

        return -this.n.dot4(line.getOrigin()) / ldotv; // ldots / ldotv
    }

    /**
     * Test a line segment for intersection with this plane. If it intersects, return the point of intersection.
     *
     * @param pa the first point of the line segment.
     * @param pb the second point of the line segment.
     * @return The point of intersection with the plane. Null is returned if the segment does not instersect this plane.
     *         {@link gov.nasa.worldwind.geom.Vec4#INFINITY} coincident with the plane.
     * @throws IllegalArgumentException if either input point is null.
     */
    public Vec4 intersect(Vec4 pa, Vec4 pb)
    {
        if (pa == null || pb == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Line l = Line.fromSegment(pa, pb);
        double t = this.intersectDistance(l);

        if (Double.isInfinite(t))
            return Vec4.INFINITY;

        if (Double.isNaN(t) || t < 0 || t > 1)
            return null;

        return l.getPointAt(t);
    }

    /**
     * Clip a line segment to this plane.
     *
     * @param pa the first point of the segment.
     * @param pb the second point of the segment.
     * @return An array of two points that are both on the positive side of the plane. If the direction of the line
     *         formed by the two points is positive with respect to this plane's normal vector, the first point in the
     *         array will be the intersection point on the plane, and the second point will be the original segment end
     *         point. If the direction of the line is negative with respect to this plane's normal vector, the first
     *         point in the array will be the original segment's begin point, and the second point will be the
     *         intersection point on the plane. If the segment does not intersect the plane, null is returned. If the
     *         segment is coincident with the plane, the input points are returned, in their input order.
     * @throws IllegalArgumentException if either input point is null.
     */
    public Vec4[] clip(Vec4 pa, Vec4 pb)
    {
        if (pa == null || pb == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Get the projection of the segment onto the plane.
        Line line = Line.fromSegment(pa, pb);
        double ldotv = this.n.dot3(line.getDirection());

        // Are the line and plane parallel?
        if (ldotv == 0) // line and plane are parallel and maybe coincident
        {
            double ldots = this.n.dot4(line.getOrigin());
            if (ldots == 0)
                return new Vec4[] {pa, pb}; // line is coincident with the plane
            else
                return null; // line is not coincident with the plane
        }

        // Not parallel so the line intersects. But does the segment intersect?
        double t = -this.n.dot4(line.getOrigin()) / ldotv; // ldots / ldotv
        if (t < 0 || t > 1) // segment does not intersect
            return null;

        Vec4 p = line.getPointAt(t);
        if (ldotv > 0)
            return new Vec4[] {p, pb};
        else
            return new Vec4[] {pa, p};
    }

    public double distanceTo(Vec4 p)
    {
        return this.n.dot4(p);
    }

    public int onSameSide(Vec4 pa, Vec4 pb)
    {
        double da = this.distanceTo(pa);
        double db = this.distanceTo(pb);

        if (da < 0 && db < 0)
            return -1;

        if (da > 0 && db > 0)
            return 1;

        return 0;
    }

    public int onSameSide(Vec4[] pts)
    {
        double d = this.distanceTo(pts[0]);
        int side = d < 0 ? -1 : d > 0 ? 1 : 0;
        if (side == 0)
            return 0;

        for (int i = 1; i < pts.length; i++)
        {
            d = this.distanceTo(pts[i]);
            if ((side == -1 && d < 0) || (side == 1 && d > 0))
                continue;

            return 0; // point is not on same side as the others
        }

        return side;
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final gov.nasa.worldwind.geom.Plane plane = (gov.nasa.worldwind.geom.Plane) o;

        //noinspection RedundantIfStatement
        if (!this.n.normalize3().equals(plane.n.normalize3()))
            return false;

        return true;
    }

    @Override
    public final int hashCode()
    {
        return this.n.hashCode();
    }

    @Override
    public final String toString()
    {
        return this.n.toString();
    }
}