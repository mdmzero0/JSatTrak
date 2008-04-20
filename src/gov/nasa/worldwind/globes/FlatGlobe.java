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

/** Experimental flat globe
 * See TODOs for difference from EllipsoidalGlobe
 * @author Patrick Murris - base on EllipsoidalGlobe
 * @version $Id$
 */
public class FlatGlobe extends WWObjectImpl implements Globe
{
    public final static String PROJECTION_LAT_LON = "gov.nasa.worldwind.globes.projectionLatLon";
    public final static String PROJECTION_SINUSOIDAL = "gov.nasa.worldwind.globes.projectionSinusoidal";
    public final static String PROJECTION_MERCATOR = "gov.nasa.worldwind.globes.projectionMercator";
    public final static String PROJECTION_TEST = "gov.nasa.worldwind.globes.projectionTest";

    private final double equatorialRadius;
    private final double polarRadius;
    private final double es;
    private final Vec4 center;
    private Tessellator tessellator;

    private final ElevationModel elevationModel;
    private String projection = PROJECTION_LAT_LON;

    public FlatGlobe(double equatorialRadius, double polarRadius, double es, ElevationModel em)
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

    // TODO: Find a more accurate workaround then getRadius()
    public double getRadiusAt(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        return getRadius();
        //return this.computePointFromPosition(latitude, longitude, 0d).getLength3();
    }

    // TODO: Find a more accurate workaround then getRadius()
    public double getRadiusAt(LatLon latLon)
    {
        if (latLon == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        return getRadius();
        //return this.computePointFromPosition(latLon.getLatitude(), latLon.getLongitude(), 0d).getLength3();
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

    public void setProjection(String projection)
    {
        this.projection = projection;
    }

    public String getProjection()
    {
        return this.projection;
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

    // TODO: plane/line intersection point (OK)
    // TODO: extract altitude from equRadius by subtracting this.equatorialRadius (OK)
    private Intersection[] intersect(Line line, double equRadius, double polRadius)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        // Intersection with world plane
        Plane plane = new Plane(0, 0, 1, equRadius - this.equatorialRadius);   // Flat globe plane
        Vec4 p = plane.intersect(line);
        if(p == null)
            return null;
        // Check if we are in the world boundaries
        Position pos = this.computePositionFromPoint(p);
        if (pos == null)
            return null;
        if(pos.getLatitude().degrees < -90 || pos.getLatitude().degrees > 90 ||
                pos.getLongitude().degrees < -180 || pos.getLongitude().degrees > 180)
            return null;

        return new Intersection[] {new Intersection(p, false)};
    }

    static private double discriminant(double a, double b, double c)
    {
        return b * b - 4 * a * c;
    }

    // TODO: plane/line intersection test (OK)
    public boolean intersects(Line line)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return this.intersect(line) != null;
    }

    // TODO: plane/plane intersection test (OK)
    public boolean intersects(Plane plane)  
    {
        if (plane == null)
        {
            String msg = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return !plane.getNormal().equals(Vec4.UNIT_Z);

    }

    // TODO: return constant (OK)
    public Vec4 computeSurfaceNormalAtPoint(Vec4 p)
    {
        if (p == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        return new Vec4(0, 0, 1);
    }

    public final ElevationModel getElevationModel()
    {
        return this.elevationModel;
    }

    public Double getBestElevation(Angle latitude, Angle longitude)
    {
        return this.elevationModel.getBestElevation(latitude, longitude);
    }

    // TODO: return zero if outside the lat/lon normal boundaries (OK)
    public final Double getElevationAtResolution(Angle latitude, Angle longitude, double resolution)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if(latitude.degrees < -90 || latitude.degrees > 90 || longitude.degrees < -180 || longitude.degrees > 180)
            return null;
        
        int target = this.elevationModel.getTargetResolution(this, resolution);
        return this.elevationModel.getElevationAtResolution(latitude, longitude, target);
    }

    // TODO: return zero if outside the lat/lon normal boundaries (OK)
    public final double getElevation(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if(latitude.degrees < -90 || latitude.degrees > 90 || longitude.degrees < -180 || longitude.degrees > 180)
            return 0;
        return this.elevationModel != null ? this.elevationModel.getElevation(latitude, longitude) : 0;
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

    /*
    // The code below maps latitude / longitude position to globe-centered Cartesian coordinates.
    // The Y axis points to the north pole. The Z axis points to the intersection of the prime
    // meridian and the equator, in the equatorial plane. The X axis completes a right-handed
    // coordinate system, and is 90 degrees east of the Z axis and also in the equatorial plane.

    private Vec4 geodeticToCartesianEl(Angle latitude, Angle longitude, double metersElevation)
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

        Vec4 cart = new Vec4(x, y, z);
        //System.out.println("geodeticToCartesian: " + latitude + ", " + longitude + ", " + metersElevation + " / " + cart);
        return cart;
    }

    private Position cartesianToGeodeticEl(Vec4 cart)
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

        Position pos = Position.fromRadians(lat, lon, elevation);
        //System.out.println("cartesianToGeodetic: " + cart + " / " + pos);
        return pos;
    }  */

    // The code below maps latitude / longitude position to globe-centered Cartesian coordinates.
    // The Y axis points to the north pole. The Z axis points to the intersection of the prime
    // meridian and the equator, in the equatorial plane. The X axis completes a right-handed
    // coordinate system, and is 90 degrees east of the Z axis and also in the equatorial plane.
    // TODO: Implement flat projections (OK)
    private Vec4 geodeticToCartesian(Angle latitude, Angle longitude, double metersElevation)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.LatitudeOrLongitudeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 cart = null;
        if(this.projection.compareToIgnoreCase(PROJECTION_LAT_LON) == 0)
        {
            // Lat/Lon projection - plate carré
            cart = new Vec4(this.equatorialRadius * longitude.radians,
                    this.equatorialRadius * latitude.radians,
                    metersElevation);
        }
        else if(this.projection.compareToIgnoreCase(PROJECTION_MERCATOR) == 0)
        {
            // Mercator projection
            if(latitude.degrees > 75) latitude = Angle.fromDegrees(75);
            if(latitude.degrees < -75) latitude = Angle.fromDegrees(-75);
            cart = new Vec4(this.equatorialRadius * longitude.radians,
                    this.equatorialRadius * Math.log(Math.tan(Math.PI / 4 + latitude.radians / 2)),
                    metersElevation);
        }
        else if(this.projection.compareToIgnoreCase(PROJECTION_SINUSOIDAL) == 0)
        {
            // Sinusoidal projection
            cart = new Vec4(this.equatorialRadius * longitude.radians * latitude.cos(),
                    this.equatorialRadius * latitude.radians,
                    metersElevation);
        }
        else if(this.projection.compareToIgnoreCase(PROJECTION_TEST) == 0)
        {
            // Test projection
            double r = Math.sqrt(latitude.radians * latitude.radians + longitude.radians * longitude.radians) * this.equatorialRadius;
            cart = new Vec4(this.equatorialRadius * longitude.radians * Math.pow(latitude.cos(), .3),
                    this.equatorialRadius * latitude.radians,
                    metersElevation);
        }
        //System.out.println("geodeticToCartesian: " + latitude + ", " + longitude + ", " + metersElevation + " / " + cart);
        //System.out.println(this.projection);
        return cart;
    }

    // TODO: Implement flat projections (OK)
    private Position cartesianToGeodetic(Vec4 cart)
    {
        if (cart == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Position pos = null;
        if(this.projection.compareToIgnoreCase(PROJECTION_LAT_LON) == 0)
        {
        // Lat/Lon projection - plate carré
        pos =  Position.fromRadians(
                cart.y / this.equatorialRadius,
                cart.x / this.equatorialRadius,
                cart.z);
        }
        else if(this.projection.compareToIgnoreCase(PROJECTION_MERCATOR) == 0)
        {
        // Mercator projection
        pos =  Position.fromRadians(
                Math.atan(Math.sinh(cart.y / this.equatorialRadius)) ,
                cart.x / this.equatorialRadius,
                cart.z);
        }
        else if(this.projection.compareToIgnoreCase(PROJECTION_SINUSOIDAL) == 0)
        {
        // Sinusoidal projection
        pos =  Position.fromRadians(
                cart.y / this.equatorialRadius,
                cart.x / this.equatorialRadius / Angle.fromRadians(cart.y / this.equatorialRadius).cos(),
                cart.z);
        }
        else if(this.projection.compareToIgnoreCase(PROJECTION_TEST) == 0)
        {
        // Test projection
        pos =  Position.fromRadians(
                cart.y / this.equatorialRadius,
                cart.x / this.equatorialRadius / Math.pow(Angle.fromRadians(cart.y / this.equatorialRadius).cos(), .3),
                cart.z);
        }
        //System.out.println("cartesianToGeodetic: " + cart + " / " + pos);
        return pos;
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
    // TODO: Adapt to flat world tiles (OK)
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
        Vec4 centroidBot = this.computePointFromPosition(center.getLatitude(), center.getLongitude(), minHeight);

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

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlatGlobe flatGlobe = (FlatGlobe) o;

        if (Double.compare(flatGlobe.equatorialRadius, equatorialRadius) != 0) return false;
        if (Double.compare(flatGlobe.polarRadius, polarRadius) != 0) return false;
        if (center != null ? !center.equals(flatGlobe.center) : flatGlobe.center != null) return false;
        if (elevationModel != null ? !elevationModel.equals(flatGlobe.elevationModel)
            : flatGlobe.elevationModel != null)
            return false;
        if (projection != null ? !projection.equals(flatGlobe.projection) : flatGlobe.projection != null) return false;
        if (tessellator != null ? !tessellator.equals(flatGlobe.tessellator) : flatGlobe.tessellator != null)
            return false;

        return true;
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
        result = 31 * result + (projection != null ? projection.hashCode() : 0);
        return result;
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