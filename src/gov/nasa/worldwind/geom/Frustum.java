/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logging;

//todo: check javadoc accuracy,

/**
 * Instances of <code>Frustum</code> are immutable. </p>
 *
 * @author Tom Gaskins
 * @version $Id: Frustum.java 3301 2007-10-16 00:31:34Z dcollins $
 */
public class Frustum
{
    private final Plane left;
    private final Plane right;
    private final Plane bottom;
    private final Plane top;
    private final Plane near;
    private final Plane far;
    private final Plane[] allPlanes;

    /**
     * Create a default frustum with six <code>Plane</code>s. This defines a box of dimension (2, 2, 2) centered at the
     * origin.
     */
    public Frustum()
    {
        this(
            new Plane(1, 0, 0, 1),  // Left
            new Plane(-1, 0, 0, 1),  // Right
            new Plane(0, 1, 0, 1),  // Bottom
            new Plane(0, -1, 0, 1),  // Top
            new Plane(0, 0, -1, 1),  // Near
            new Plane(0, 0, 1, 1)); // Far
    }

    /**
     * Create a frustum from six <code>Plane</code>s, which define its boundaries. Does not except null arguments.
     *
     * @param near   the near plane
     * @param far    the far plane
     * @param left   the left side of the view frustum
     * @param right  the right side of the view frustm
     * @param top    the top of the view frustum
     * @param bottom the bottom of the view frustum
     * @throws IllegalArgumentException if any argument is null
     */
    public Frustum(Plane left, Plane right, Plane bottom, Plane top, Plane near, Plane far)
    {
        if (left == null || right == null || bottom == null || top == null || near == null || far == null)
        {
            String message = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.left = left;
        this.right = right;
        this.bottom = bottom;
        this.top = top;
        this.near = near;
        this.far = far;

        this.allPlanes = new Plane[] {this.left, this.right, this.bottom, this.top, this.near, this.far};
    }

    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        Frustum that = (Frustum) obj;
        return this.left.equals(that.left)
            && this.right.equals(that.right)
            && this.bottom.equals(that.bottom)
            && this.top.equals(that.top)
            && this.near.equals(that.near)
            && this.far.equals(that.far);
    }

    public final Plane getLeft()
    {
        return this.left;
    }

    public final Plane getRight()
    {
        return this.right;
    }

    public final Plane getBottom()
    {
        return this.bottom;
    }

    public final Plane getTop()
    {
        return this.top;
    }

    public final Plane getNear()
    {
        return this.near;
    }

    public final Plane getFar()
    {
        return this.far;
    }

    public Plane[] getAllPlanes()
    {
        return this.allPlanes;
    }

    public int hashCode()
    {
        int result;
        result = this.left.hashCode();
        result = 31 * result + this.right.hashCode();
        result = 19 * result + this.bottom.hashCode();
        result = 23 * result + this.top.hashCode();
        result = 17 * result + this.near.hashCode();
        result = 19 * result + this.far.hashCode();

        return result;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        sb.append("left=").append(this.near);
        sb.append(", right=").append(this.near);
        sb.append(", bottom=").append(this.near);
        sb.append(", top=").append(this.near);
        sb.append(", near=").append(this.near);
        sb.append(", far=").append(this.near);
        sb.append(")");
        return sb.toString();
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    public static Frustum fromProjectionMatrix(Matrix projectionMatrix)
    {
        //noinspection UnnecessaryLocalVariable
        Matrix m = projectionMatrix;
        if (m == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Extract the left clipping plane from the projection-matrix.
        double leftMag = Math.sqrt((m.m41 + m.m11) * (m.m41 + m.m11) + (m.m42 + m.m12) * (m.m42 + m.m12)
            + (m.m43 + m.m13) * (m.m43 + m.m13));
        Plane leftPlane = new Plane((m.m41 + m.m11) / leftMag, (m.m42 + m.m12) / leftMag, (m.m43 + m.m13) / leftMag,
            m.m44 + m.m14);
        // Extract the right clipping plane from the projection-matrix.
        double rightMag = Math.sqrt((m.m41 - m.m11) * (m.m41 - m.m11) + (m.m42 - m.m12) * (m.m42 - m.m12)
            + (m.m43 - m.m13) * (m.m43 - m.m13));
        Plane rightPlane = new Plane((m.m41 - m.m11) / rightMag, (m.m42 - m.m12) / rightMag, (m.m43 - m.m13) / rightMag,
            m.m44 - m.m14);
        // Extract the bottom clipping plane from the projection-matrix.
        double bottomMag = Math.sqrt((m.m41 + m.m21) * (m.m41 + m.m21) + (m.m42 + m.m22) * (m.m42 + m.m22)
            + (m.m43 + m.m23) * (m.m43 + m.m23));
        Plane bottomPlane = new Plane((m.m41 + m.m21) / bottomMag, (m.m42 + m.m22) / bottomMag,
            (m.m43 + m.m23) / bottomMag,
            m.m44 + m.m24);
        // Extract the top clipping plane from the projection-matrix.
        double topMag = Math.sqrt((m.m41 - m.m21) * (m.m41 - m.m21) + (m.m42 - m.m22) * (m.m42 - m.m22)
            + (m.m43 - m.m23) * (m.m43 - m.m23));
        Plane topPlane = new Plane((m.m41 - m.m21) / topMag, (m.m42 - m.m22) / topMag, (m.m43 - m.m23) / topMag,
            m.m44 - m.m24);
        // Extract the near clipping plane from the projection-matrix.
        double nearMag = Math.sqrt((m.m41 + m.m31) * (m.m41 + m.m31) + (m.m42 + m.m32) * (m.m42 + m.m32)
            + (m.m43 + m.m33) * (m.m43 + m.m33));
        Plane nearPlane = new Plane((m.m41 + m.m31) / nearMag, (m.m42 + m.m32) / nearMag, (m.m43 + m.m33) / nearMag,
            m.m44 + m.m34);
        // Extract the far clipping plane from the projection-matrix.
        double farMag = Math.sqrt((m.m41 - m.m31) * (m.m41 - m.m31) + (m.m42 - m.m32) * (m.m42 - m.m32)
            + (m.m43 - m.m33) * (m.m43 - m.m33));
        Plane farPlane = new Plane((m.m41 - m.m31) / farMag, (m.m42 - m.m32) / farMag, (m.m43 - m.m33) / farMag,
            m.m44 - m.m34);
        return new Frustum(leftPlane, rightPlane, bottomPlane, topPlane, nearPlane, farPlane);
    }

    /**
     * Creates a <code>Frustum</code> from a horizontal field-of-view, viewport aspect ratio and distance to near and
     * far depth clipping planes. The near plane must be closer than the far plane, and both planes must be a positive
     * distance away.
     *
     * @param horizontalFieldOfView horizontal field-of-view angle in the range (0, 180)
     * @param viewportWidth         the width of the viewport in screen pixels
     * @param viewportHeight        the height of the viewport in screen pixels
     * @param near                  distance to the near depth clipping plane
     * @param far                   distance to far depth clipping plane
     * @throws IllegalArgumentException if fov is not in the range (0, 180), if either near or far are negative, or near
     *                                  is greater than or equal to far
     */
    public static Frustum fromPerspective(Angle horizontalFieldOfView, int viewportWidth, int viewportHeight,
        double near, double far)
    {
        if (horizontalFieldOfView == null)
        {
            String message = Logging.getMessage("Geom.ViewFrustum.FieldOfViewIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        double fov = horizontalFieldOfView.getDegrees();
        double farMinusNear = far - near;
        String message = null;
        if (fov <= 0 || fov > 180)
            message = Logging.getMessage("Geom.ViewFrustum.FieldOfViewOutOfRange", fov);
        if (near <= 0 || farMinusNear <= 0)
            message = Logging.getMessage("Geom.ViewFrusutm.ClippingDistanceOutOfRange");
        if (message != null)
        {
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double focalLength = 1d / horizontalFieldOfView.tanHalfAngle();
        double aspect = viewportHeight / (double) viewportWidth;
        double lrLen = Math.sqrt(focalLength * focalLength + 1);
        double btLen = Math.sqrt(focalLength * focalLength + aspect * aspect);
        Plane leftPlane = new Plane(focalLength / lrLen, 0d, 0d - 1d / lrLen, 0);
        Plane rightPlane = new Plane(0d - focalLength / lrLen, 0d, 0d - 1d / lrLen, 0d);
        Plane bottomPlane = new Plane(0d, focalLength / btLen, 0d - aspect / btLen, 0d);
        Plane topPlane = new Plane(0d, 0d - focalLength / btLen, 0d - aspect / btLen, 0d);
        Plane nearPlane = new Plane(0d, 0d, 0d - 1d, 0d - near);
        Plane farPlane = new Plane(0d, 0d, 1d, far);
        return new Frustum(leftPlane, rightPlane, bottomPlane, topPlane, nearPlane, farPlane);
    }

    /**
     * Creates a <code>Frustum</code> from three sets of parallel clipping planes (a parallel projectionMatrix). In this
     * case, the near and far depth clipping planes may be a negative distance away.
     *
     * @param near   distance to the near depth clipping plane
     * @param far    distance to far depth clipping plane
     * @param width  horizontal dimension of the near clipping plane
     * @param height vertical dimension of the near clipping plane
     * @throws IllegalArgumentException if the difference of any plane set (lright - left, top - bottom, far - near) is
     *                                  less than or equal to zero.
     */
    public static Frustum fromPerspective(double width, double height, double near, double far)
    {
        double farMinusNear = far - near;
        if (farMinusNear <= 0.0 || width <= 0.0 || height <= 0.0)
        {
            String message = Logging.getMessage("Geom.ViewFrusutm.ClippingDistanceOutOfRange");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        double width_over_2 = width / 2.0;
        double height_over_2 = height / 2.0;
        Plane leftPlane = new Plane(1.0, 0.0, 0.0, width_over_2);
        Plane rightPlane = new Plane(-1.0, 0.0, 0.0, width_over_2);
        Plane bottomPlane = new Plane(0.0, 1.0, 0.0, height_over_2);
        Plane topPlane = new Plane(0.0, -1.0, 0.0, height_over_2);
        Plane nearPlane = new Plane(0.0, 0.0, -1.0, (near < 0.0) ? near : -near);
        Plane farPlane = new Plane(0.0, 0.0, 1.0, (far < 0.0) ? -far : far);
        return new Frustum(leftPlane, rightPlane, bottomPlane, topPlane, nearPlane, farPlane);
    }

    // ============== Intersection Functions ======================= //
    // ============== Intersection Functions ======================= //
    // ============== Intersection Functions ======================= //

    /**
     * @param extent
     * @return
     * @throws IllegalArgumentException if <code>extent</code> is null
     */
    public final boolean intersects(Extent extent)
    {
        if (extent == null)
        {
            String msg = Logging.getMessage("nullValue.ExtentIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        // See if the extent's bounding sphere is within or intersects the frustum.
        Vec4 c = extent.getCenter();
        double nr = -extent.getRadius();

        if (this.far.dot(c) <= nr)
            return false;
        if (this.left.dot(c) <= nr)
            return false;
        if (this.right.dot(c) <= nr)
            return false;
        if (this.top.dot(c) <= nr)
            return false;
        if (this.bottom.dot(c) <= nr)
            return false;
        //noinspection RedundantIfStatement
        if (this.near.dot(c) <= nr)
            return false;

        return true;
    }

    /**
     * @param point
     * @return
     * @throws IllegalArgumentException if <code>point</code> is null
     */
    public final boolean contains(Vec4 point)
    {
        if (point == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.far.dot(point) <= 0)
            return false;
        if (this.left.dot(point) <= 0)
            return false;
        if (this.right.dot(point) <= 0)
            return false;
        if (this.top.dot(point) <= 0)
            return false;
        if (this.bottom.dot(point) <= 0)
            return false;
        //noinspection RedundantIfStatement
        if (this.near.dot(point) <= 0)
            return false;

        return true;
    }

    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //
    // ============== Geometric Functions ======================= //

    /**
     * @param matrix
     * @return
     * @throws IllegalArgumentException if <code>matrix</code> is null
     */
    public Frustum transformBy(Matrix matrix)
    {
        ensureNonNull(matrix);
        Plane left = new Plane(this.left.getVector().transformBy4(matrix));
        Plane right = new Plane(this.right.getVector().transformBy4(matrix));
        Plane bottom = new Plane(this.bottom.getVector().transformBy4(matrix));
        Plane top = new Plane(this.top.getVector().transformBy4(matrix));
        Plane near = new Plane(this.near.getVector().transformBy4(matrix));
        Plane far = new Plane(this.far.getVector().transformBy4(matrix));
        return new Frustum(left, right, bottom, top, near, far);
    }

    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //

    private static void ensureNonNull(Matrix matrix)
    {
        if (matrix == null)
        {
            String msg = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }
    }
}
