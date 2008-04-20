/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

/**
 * @author Eric Dalgliesh 30/11/2006
 * @version $Id: Triangle.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class Triangle
{
    private static final double EPSILON = 0.0000001; // used in intersects method

    private final Vec4 a;
    private final Vec4 b;
    private final Vec4 c;

    public Triangle(Vec4 a, Vec4 b, Vec4 c)
    {
        if (a == null || b == null || c == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.a = a;
        this.b = b;
        this.c = c;
    }

//    private Plane getPlane()
//    {
//        Vector ab, ac;
//        ab = new Vector(this.b.subtract(this.a)).normalize();
//        ac = new Vector(this.c.subtract(this.a)).normalize();
//
//        Vector n = new Vector(new Point(ab.x(), ab.y(), ab.z(), ab.w()).cross(new Point(ac.x(), ac.y(), ac.z(), ac.w())));
//
//        return new gov.nasa.worldwind.geom.Plane(n);
//    }

//    private Point temporaryIntersectPlaneAndLine(Line line, Plane plane)
//    {
//        Vector n = line.getDirection();
//        Point v0 = Point.fromOriginAndDirection(plane.getDistance(), plane.getNormal(), Point.ZERO);
//        Point p0 = line.getPointAt(0);
//        Point p1 = line.getPointAt(1);
//
//        double r1 = n.dot(v0.subtract(p0))/n.dot(p1.subtract(p0));
//        if(r1 >= 0)
//            return line.getPointAt(r1);
//        return null;
//    }
//
//    private Triangle divide(double d)
//    {
//        d  = 1/d;
//        return new Triangle(this.a.multiply(d), this.b.multiply(d), this.c.multiply(d));
//    }

//    public Point intersect(Line line)
//    {
//        // taken from Moller and Trumbore
//        // http://www.cs.virginia.edu/~gfx/Courses/2003/ImageSynthesis/papers/Acceleration/
//        // Fast%20MinimumStorage%20RayTriangle%20Intersection.pdf
//
//        Point origin = line.getOrigin();
//        Point dir = new Point(line.getDirection());
//
//        double u, v;
//
//        // find vectors for two edges sharing Point a
//        Point edge1 = this.c.subtract(this.a);
//        Point edge2 = this.b.subtract(this.a);
//
//        // start calculating determinant
//        Point pvec = dir.cross(edge2);
//
//        // get determinant.
//        double det = edge1.dot(pvec);
//
//        if (det > -EPSILON && det < EPSILON)
//        {// If det is near zero, then ray lies on plane of triangle
//            return null;
//        }
//
//        double detInv = 1d / det;
//
//        // distance from vert0 to ray origin
//        Point tvec = origin.subtract(this.a);
//
//        // calculate u parameter and test bounds
//        u = tvec.dot(pvec) * detInv;
//        if (u < 0 || u > 1)
//        {
//            return null;
//        }
//
//        // prepare to test v parameter
//        Point qvec = tvec.cross(edge1);
//
//        //calculate v parameter and test bounds
//        v = dir.dot(qvec) * detInv;
//        if (v < 0 || u + v > 1)
//        {
//            return null;
//        }
//
//        double t = edge2.dot(qvec) * detInv;
//        return Point.fromOriginAndDirection(t, line.getDirection(), line.getOrigin());
//
////        return new Point(t, u, v);
////        return line.getPointAt(t);
//    }
}
