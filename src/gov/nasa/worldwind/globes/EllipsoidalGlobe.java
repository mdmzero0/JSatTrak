/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Tom Gaskins
 * @version $Id: EllipsoidalGlobe.java 3682 2007-12-03 19:47:19Z tgaskins $
 */
public class EllipsoidalGlobe extends WWObjectImpl implements Globe
{
    private final double equatorialRadius;
    private final double polarRadius;
    private final double es;
    private final Vec4 center;
    private final ElevationModel elevationModel;
    private Tessellator tessellator;

    public EllipsoidalGlobe(double equatorialRadius, double polarRadius, double es, ElevationModel em)
    {
        if (em == null)
        {
            String msg = Logging.getMessage("nullValue.ElevationModelIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.equatorialRadius = equatorialRadius;
        this.polarRadius = polarRadius;
        this.es = es; // assume it's consistent with the two radii
        this.center = Vec4.ZERO;
        this.elevationModel = em;
        this.tessellator = (Tessellator) WorldWind.createConfigurationComponent(AVKey.TESSELLATOR_CLASS_NAME);
    }

    public Tessellator getTessellator()
    {
        return tessellator;
    }

    public void setTessellator(Tessellator tessellator)
    {
        this.tessellator = tessellator;
    }

    public final double getRadius()
    {
        return this.equatorialRadius;
    }

    public final double getEquatorialRadius()
    {
        return this.equatorialRadius;
    }

    public final double getPolarRadius()
    {
        return this.polarRadius;
    }

    public double getMaximumRadius()
    {
        return this.equatorialRadius;
    }

    public double getRadiusAt(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.computePointFromPosition(latitude, longitude, 0d).getLength3();
    }

    public double getRadiusAt(LatLon latLon)
    {
        if (latLon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.computePointFromPosition(latLon.getLatitude(), latLon.getLongitude(), 0d).getLength3();
    }

    public double getEccentricitySquared()
    {
        return this.es;
    }

    public final double getDiameter()
    {
        return this.equatorialRadius * 2;
    }

    public final Vec4 getCenter()
    {
        return this.center;
    }

    public double getMaxElevation()
    {
        return this.elevationModel.getMaxElevation();
    }

    public double getMinElevation()
    {
        return this.elevationModel.getMinElevation();
    }

    public double getMaxElevation(Sector sector)
    {
        return this.elevationModel.getMaxElevation(sector);
    }

    public double getMinElevation(Sector sector)
    {
        return this.elevationModel.getMinElevation(sector);
    }

    public final Extent getExtent()
    {
        return this;
    }

    public boolean intersects(Frustum frustum)
    {
        if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return frustum.intersects(this);
    }

    public Intersection[] intersect(Line line)
    {
        return this.intersect(line, this.equatorialRadius, this.polarRadius);
    }

    public Intersection[] intersect(Line line, double altitude)
    {
        return this.intersect(line, this.equatorialRadius + altitude, this.polarRadius + altitude);
    }

    private Intersection[] intersect(Line line, double equRadius, double polRadius)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Taken from Lengyel, 2Ed., Section 5.2.3, page 148.

        double m = equRadius / polRadius; // "ratio of the x semi-axis length to the y semi-axis length"
        double n = 1d;                    // "ratio of the x semi-axis length to the z semi-axis length" 
        double m2 = m * m;
        double n2 = n * n;
        double r2 = equRadius * equRadius; // nominal radius squared //equRadius * polRadius;

        double vx = line.getDirection().x;
        double vy = line.getDirection().y;
        double vz = line.getDirection().z;
        double sx = line.getOrigin().x;
        double sy = line.getOrigin().y;
        double sz = line.getOrigin().z;

        double a = vx * vx + m2 * vy * vy + n2 * vz * vz;
        double b = 2d * (sx * vx + m2 * sy * vy + n2 * sz * vz);
        double c = sx * sx + m2 * sy * sy + n2 * sz * sz - r2;

        double discriminant = discriminant(a, b, c);
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
            return new Intersection[] {new Intersection(near, false), new Intersection(far, false)};
        }
    }

    static private double discriminant(double a, double b, double c)
    {
        return b * b - 4 * a * c;
    }

    public boolean intersects(Line line)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return line.distanceTo(this.center) <= this.equatorialRadius;
    }

    public boolean intersects(Plane plane)
    {
        if (plane == null)
        {
            String msg = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double dq1 = plane.dot(this.center);
        return dq1 <= this.equatorialRadius;
    }

    public Vec4 computeSurfaceNormalAtPoint(Vec4 p)
    {
        if (p == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        p = p.subtract3(this.center);

        return new Vec4(
            p.x / (this.equatorialRadius * this.equatorialRadius),
            p.y / (this.polarRadius * this.polarRadius),
            p.z / (this.equatorialRadius * this.equatorialRadius)).normalize3();
    }

    public final ElevationModel getElevationModel()
    {
        return this.elevationModel;
    }

    public final double getElevation(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.elevationModel != null ? this.elevationModel.getElevation(latitude, longitude) : 0;
    }

    public final Double getElevationAtResolution(Angle latitude, Angle longitude, double resolution)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.elevationModel == null)
            return null;

        int target = this.elevationModel.getTargetResolution(this, resolution);
        return this.elevationModel.getElevationAtResolution(latitude, longitude, target);
    }

    public final Double getBestElevation(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.elevationModel != null ? this.elevationModel.getBestElevation(latitude, longitude) : null;
    }

    public final Vec4 computePointFromPosition(Position position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.geodeticToCartesian(position.getLatitude(), position.getLongitude(), position.getElevation());
    }

    public final Vec4 computePointFromPosition(Angle latitude, Angle longitude, double metersElevation)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.geodeticToCartesian(latitude, longitude, metersElevation);
    }

    public final Position computePositionFromPoint(Vec4 point)
    {
        if (point == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.cartesianToGeodetic(point);
    }

    public final Position getIntersectionPosition(Line line)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Intersection[] intersections = this.intersect(line);
        if (intersections == null)
            return null;

        return this.computePositionFromPoint(intersections[0].getIntersectionPoint());
    }

    // The code below maps latitude / longitude position to globe-centered Cartesian coordinates.
    // The Y axis points to the north pole. The Z axis points to the intersection of the prime
    // meridian and the equator, in the equatorial plane. The X axis completes a right-handed
    // coordinate system, and is 90 degrees east of the Z axis and also in the equatorial plane.

    private Vec4 geodeticToCartesian(Angle latitude, Angle longitude, double metersElevation)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double cosLat = latitude.cos();
        double sinLat = latitude.sin();

        double rpm = // getRadius (in meters) of vertical in prime meridian
            this.equatorialRadius / Math.sqrt(1.0 - this.es * sinLat * sinLat);

        double x = (rpm + metersElevation) * cosLat * longitude.sin();
        double y = (rpm * (1.0 - this.es) + metersElevation) * sinLat;
        double z = (rpm + metersElevation) * cosLat * longitude.cos();

        return new Vec4(x, y, z);
    }

    private Position cartesianToGeodetic(Vec4 cart)
    {
        if (cart == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // according to
        // H. Vermeille,
        // Direct transformation from geocentric to geodetic ccordinates,
        // Journal of Geodesy (2002) 76:451-454
        double ra2 = 1 / (this.equatorialRadius * equatorialRadius);

        double X = cart.z;
        //noinspection SuspiciousNameCombination
        double Y = cart.x;
        double Z = cart.y;
        double e2 = this.es;
        double e4 = e2 * e2;

        double XXpYY = X * X + Y * Y;
        double sqrtXXpYY = Math.sqrt(XXpYY);
        double p = XXpYY * ra2;
        double q = Z * Z * (1 - e2) * ra2;
        double r = 1 / 6.0 * (p + q - e4);
        double s = e4 * p * q / (4 * r * r * r);
        double t = Math.pow(1 + s + Math.sqrt(s * (2 + s)), 1 / 3.0);
        double u = r * (1 + t + 1 / t);
        double v = Math.sqrt(u * u + e4 * q);
        double w = e2 * (u + v - q) / (2 * v);
        double k = Math.sqrt(u + v + w * w) - w;
        double D = k * sqrtXXpYY / (k + e2);
        double lon = 2 * Math.atan2(Y, X + sqrtXXpYY);
        double sqrtDDpZZ = Math.sqrt(D * D + Z * Z);
        double lat = 2 * Math.atan2(Z, D + sqrtDDpZZ);
        double elevation = (k + e2 - 1) * sqrtDDpZZ / k;

        return Position.fromRadians(lat, lon, elevation);
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EllipsoidalGlobe that = (EllipsoidalGlobe) o;

        if (Double.compare(that.equatorialRadius, equatorialRadius) != 0) return false;
        if (Double.compare(that.polarRadius, polarRadius) != 0) return false;
        if (center != null ? !center.equals(that.center) : that.center != null) return false;
        //noinspection SimplifiableIfStatement
        if (elevationModel != null ? !elevationModel.equals(that.elevationModel) : that.elevationModel != null)
            return false;
        return !(tessellator != null ? !tessellator.equals(that.tessellator) : that.tessellator != null);

    }

    public int hashCode()
    {
        int result;
        long temp;
        temp = equatorialRadius != +0.0d ? Double.doubleToLongBits(equatorialRadius) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = polarRadius != +0.0d ? Double.doubleToLongBits(polarRadius) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (center != null ? center.hashCode() : 0);
        result = 31 * result + (tessellator != null ? tessellator.hashCode() : 0);
        result = 31 * result + (elevationModel != null ? elevationModel.hashCode() : 0);
        return result;
    }

    /**
     * Returns a cylinder that minimally surrounds the sector at a specified vertical exaggeration.
     *
     * @param verticalExaggeration the vertical exaggeration to apply to the globe's elevations when computing the
     *                             cylinder.
     * @param sector               the sector to return the bounding cylinder for.
     * @return The minimal bounding cylinder in Cartesian coordinates.
     * @throws IllegalArgumentException if <code>globe</code> or <code>sector</code> is null
     */
    public Cylinder computeBoundingCylinder(double verticalExaggeration, Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // Compute the center points of the bounding cylinder's top and bottom planes.
        LatLon center = sector.getCentroid();
        double maxHeight = this.getMaxElevation(sector) * verticalExaggeration;
        double minHeight = 0; //globe.getMinElevation(sector) * verticalExaggeration;
        Vec4 centroidTop = this.computePointFromPosition(center.getLatitude(), center.getLongitude(), maxHeight);
        Vec4 lowPoint = this.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), minHeight);
        Vec4 axis = centroidTop.normalize3();
        double lowDistance = axis.dot3(lowPoint);
        Vec4 centroidBot = axis.multiply3(lowDistance);

        // Compute radius of circumscribing circle around general quadrilateral.
        Vec4 northwest = this.computePointFromPosition(sector.getMaxLatitude(), sector.getMinLongitude(), maxHeight);
        Vec4 southeast = this.computePointFromPosition(sector.getMinLatitude(), sector.getMaxLongitude(), maxHeight);
        Vec4 southwest = this.computePointFromPosition(sector.getMinLatitude(), sector.getMinLongitude(), maxHeight);
        Vec4 northeast = this.computePointFromPosition(sector.getMaxLatitude(), sector.getMaxLongitude(), maxHeight);
        double a = southwest.distanceTo3(southeast);
        double b = southeast.distanceTo3(northeast);
        double c = northeast.distanceTo3(northwest);
        double d = northwest.distanceTo3(southwest);
        double s = 0.5 * (a + b + c + d);
        double area = Math.sqrt((s - a) * (s - b) * (s - c) * (s - d));
        double radius = Math.sqrt((a * b + c * d) * (a * d + b * c) * (a * c + b * d)) / (4d * area);

        return new Cylinder(centroidBot, centroidTop, radius);
    }

    public SectorGeometryList tessellate(DrawContext dc)
    {
        if (this.tessellator == null)
        {
            this.tessellator = (Tessellator) WorldWind.createConfigurationComponent(AVKey.TESSELLATOR_CLASS_NAME);
        }

        return this.tessellator.tessellate(dc);
    }
}