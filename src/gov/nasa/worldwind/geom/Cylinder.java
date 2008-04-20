/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import javax.media.opengl.glu.*;

/**
 * Represents a geometric cylinder. <code>Cylinder</code>s are immutable.
 *
 * @author Tom Gaskins
 * @version $Id: Cylinder.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class Cylinder implements Extent, Renderable
{
    private final Vec4 bottomCenter; // point at center of cylinder base
    private final Vec4 topCenter; // point at center of cylinder top
    private final Vec4 axisUnitDirection; // axis as unit vector from bottomCenter to topCenter
    private final double cylinderRadius;
    private final double cylinderHeight;

    /**
     * Create a <code>Cylinder</code> from two points and a radius. Does not accept null arguments.
     *
     * @param bottomCenter   represents the centrepoint of the base disc of the <code>Cylinder</code>
     * @param topCenter      represents the centrepoint of the top disc of the <code>Cylinder</code>
     * @param cylinderRadius the radius of the <code>Cylinder</code>
     * @throws IllegalArgumentException if either the top or bottom point is null
     */
    public Cylinder(Vec4 bottomCenter, Vec4 topCenter, double cylinderRadius)
    {
        if (bottomCenter == null || topCenter == null)
        {
            String message = Logging.getMessage("nullValue.EndPointIsNull");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cylinderRadius <= 0)
        {
            String message = Logging.getMessage("Geom.Cylinder.RadiusIsZeroOrNegative", cylinderRadius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.bottomCenter = bottomCenter;
        this.topCenter = topCenter;
        this.cylinderHeight = this.bottomCenter.distanceTo3(this.topCenter);
        this.cylinderRadius = cylinderRadius;
        this.axisUnitDirection = this.topCenter.subtract3(this.bottomCenter).normalize3();
    }

    public Vec4 getAxisUnitDirection()
    {
        return axisUnitDirection;
    }

    public Vec4 getBottomCenter()
    {
        return bottomCenter;
    }

    public Vec4 getTopCenter()
    {
        return topCenter;
    }

    public double getCylinderRadius()
    {
        return cylinderRadius;
    }

    public double getCylinderHeight()
    {
        return cylinderHeight;
    }

    public String toString()
    {
        return this.cylinderRadius + ", " + this.bottomCenter.toString() + ", " + this.topCenter.toString() + ", "
            + this.axisUnitDirection.toString();
    }

    public Intersection[] intersect(Line line) // TODO: test this method
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 ld = line.getDirection();
        Vec4 lo = line.getOrigin();
        double a = ld.x * ld.x + ld.y * ld.y;
        double b = 2 * (lo.x * ld.x + lo.y * ld.y);
        double c = lo.x * lo.x + lo.y * lo.y - this.cylinderRadius * this.cylinderRadius;

        double discriminant = Cylinder.discriminant(a, b, c);
        if (discriminant < 0)
            return null;

        double discriminantRoot = Math.sqrt(discriminant);
        if (discriminant == 0)
        {
            Vec4 p = line.getPointAt((-b - discriminantRoot) / (2 * a));
            return new Intersection[] {new Intersection(p, true)};
        }
        else // (discriminant > 0)
        {
            Vec4 near = line.getPointAt((-b - discriminantRoot) / (2 * a));
            Vec4 far = line.getPointAt((-b + discriminantRoot) / (2 * a));
            boolean n = false, f = false;
            boolean nTangent = false, fTangent = false;
            if (near.z >= 0 && near.z <= this.getHeight())
            {
                n = true;
                nTangent = near.z == 0;
            }
            if (far.z >= 0 && far.z <= this.getHeight())
            {
                f = true;
                fTangent = far.z == 0;
            }

            // TODO: Test for intersection with planes at cylinder's top and bottom

            Intersection[] intersections = null;
            if (n && f)
                intersections = new Intersection[] {new Intersection(near, nTangent), new Intersection(far, fTangent)};
            else if (n)
                intersections = new Intersection[] {new Intersection(near, nTangent)};
            else if (f)
                intersections = new Intersection[] {new Intersection(far, fTangent)};

            return intersections;
        }
    }

    public boolean intersects(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 ld = line.getDirection();
        Vec4 lo = line.getOrigin();
        double a = ld.x * ld.x + ld.y * ld.y;
        double b = 2 * (lo.x * ld.x + lo.y * ld.y);
        double c = lo.x * lo.x + lo.y * lo.y - this.cylinderRadius * this.cylinderRadius;

        double discriminant = Cylinder.discriminant(a, b, c);

        return discriminant >= 0;
    }

    static private double discriminant(double a, double b, double c)
    {
        return b * b - 4 * a * c;
    }

    private double intersectsAt(Plane plane, double effectiveRadius, double parameter)
    {
        // Test the distance from the first cylinder end-point.
        double dq1 = plane.dot(this.bottomCenter);
        boolean bq1 = dq1 <= -effectiveRadius;

        // Test the distance from the possibly reduced second cylinder end-point.
        Vec4 newTop;
        if (parameter < 1)
            newTop = this.bottomCenter.add3(this.topCenter.subtract3(this.bottomCenter).multiply3(parameter));
        else
            newTop = this.topCenter;
        double dq2 = plane.dot(newTop);
        boolean bq2 = dq2 <= -effectiveRadius;

        if (bq1 && bq2) // both <= effective radius; cylinder is on negative side of plane
            return -1;

        if (bq1 == bq2) // both >= effective radius; can't draw any conclusions
            return parameter;

        // Compute and return the parameter value at which the plane intersects the cylinder's axis.
        return effectiveRadius + plane.dot(this.bottomCenter)
            / plane.getNormal().dot3(this.bottomCenter.subtract3(newTop));
    }

    private double getEffectiveRadius(Plane plane)
    {
        // Determine the effective radius of the cylinder axis relative to the plane.
        double dot = plane.getNormal().dot3(this.axisUnitDirection);
        double scale = 1d - dot * dot;
        if (scale <= 0)
            return 0;
        else
            return this.cylinderRadius * Math.sqrt(scale);
    }

    public boolean intersects(Plane plane)
    {
        if (plane == null)
        {
            String message = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        double effectiveRadius = this.getEffectiveRadius(plane);
        double intersectionPoint = this.intersectsAt(plane, effectiveRadius, 1d);
        return intersectionPoint >= 0;
    }

    public boolean intersects(Frustum frustum)
    {
        if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");

            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double intersectionPoint;

        double effectiveRadius = this.getEffectiveRadius(frustum.getNear());
        intersectionPoint = this.intersectsAt(frustum.getNear(), effectiveRadius, 1d);
        if (intersectionPoint < 0)
            return false;

        // Near and far have the same effective radius.
        intersectionPoint = this.intersectsAt(frustum.getFar(), effectiveRadius, intersectionPoint);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getLeft());
        intersectionPoint = this.intersectsAt(frustum.getLeft(), effectiveRadius, intersectionPoint);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getRight());
        intersectionPoint = this.intersectsAt(frustum.getRight(), effectiveRadius, intersectionPoint);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getTop());
        intersectionPoint = this.intersectsAt(frustum.getTop(), effectiveRadius, intersectionPoint);
        if (intersectionPoint < 0)
            return false;

        effectiveRadius = this.getEffectiveRadius(frustum.getBottom());
        intersectionPoint = this.intersectsAt(frustum.getBottom(), effectiveRadius, intersectionPoint);
        return intersectionPoint >= 0;
    }

    public Vec4 getCenter()
    {
        Vec4 b = this.bottomCenter;
        Vec4 t = this.topCenter;
        return new Vec4(
            (b.x + t.x) / 2.0,
            (b.y + t.y) / 2.0,
            (b.z + t.z) / 2.0);
    }

    public double getDiameter()
    {
        return 2 * this.getRadius();
    }

    public double getRadius()
    {
        // return the radius of the enclosing sphere
        double halfHeight = this.bottomCenter.distanceTo3(this.topCenter) / 2.0;
        return Math.sqrt(halfHeight * halfHeight + this.cylinderRadius * this.cylinderRadius);
    }

    /**
     * Obtain the height of this <code>Cylinder</code>.
     *
     * @return the distance between the bottom and top of this <code>Cylinder</code>
     */
    public final double getHeight()
    {
        return this.cylinderHeight;
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 center = this.getCenter();
        PolarPoint p = PolarPoint.fromCartesian(center);

        javax.media.opengl.GL gl = dc.getGL();

        gl.glPushAttrib(GL.GL_ENABLE_BIT | GL.GL_TRANSFORM_BIT);

        gl.glBegin(javax.media.opengl.GL.GL_LINES);
        gl.glVertex3d(this.bottomCenter.x, this.bottomCenter.y, this.bottomCenter.z);
        gl.glVertex3d(this.topCenter.x, this.topCenter.y, this.topCenter.z);
        gl.glEnd();

        gl.glEnable(javax.media.opengl.GL.GL_DEPTH_TEST);
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glTranslated(this.bottomCenter.x, this.bottomCenter.y, this.bottomCenter.z);
        dc.getGL().glRotated(p.getLongitude().getDegrees(), 0, 1, 0);
        dc.getGL().glRotated(Math.abs(p.getLatitude().getDegrees()), Math.signum(p.getLatitude().getDegrees()) * -1,
            0, 0);

        GLUquadric quadric = dc.getGLU().gluNewQuadric();
        dc.getGLU().gluQuadricDrawStyle(quadric, GLU.GLU_LINE);
        dc.getGLU().gluCylinder(quadric, this.cylinderRadius, this.cylinderRadius, this.cylinderHeight, 30, 30);
        dc.getGLU().gluDeleteQuadric(quadric);

        gl.glPopMatrix();
        gl.glPopAttrib();
    }
}
