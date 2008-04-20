/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Patrick Murris from dcollins BasicOrbitView
 * @version $Id$
 */
public class FlatOrbitView extends AbstractView implements OrbitView
{
    // View attributes.
    private FlatOrbitViewModel orbitViewModel;
    private Frustum frustum = null;
    // Orbit View attributes.
    private Angle latitude = null;
    private Angle longitude = null;
    private Angle lookAtLatitude = null;
    private Angle lookAtLongitude = null;
    private double altitude = -1;
    private Angle heading = null;
    private Angle pitch = null;
    private double zoom = -1;
    private Quaternion rotation = null;
    // Constants.
    private static final double COLLISION_PADDING_HEIGHT = 40;

    // ============== Viewing State ======================= //
    // ============== Viewing State ======================= //
    // ============== Viewing State ======================= //

    protected void doApply(DrawContext dc)
    {
        if (this.orbitViewModel == null)
            return;

        Vec4 eyeVec = this.orbitViewModel.getEyeVector();
        if (eyeVec == null)
            return;

        // Compute current viewing attributes.
        double nearClipDist = this.computeNearClipDistance(dc, eyeVec);
        double farClipDist = this.computeFarClipDistance(dc, eyeVec);
        this.frustum = this.createFrustum(nearClipDist, farClipDist);

        // Set current GL viewing state.
        Matrix modelViewMatrix =  this.createModelViewMatrix(this.orbitViewModel);
        Matrix projectionMatrix = this.createProjectionMatrix(nearClipDist, farClipDist);
        this.loadModelViewProjection(dc, modelViewMatrix, projectionMatrix);
    }

    public Frustum getFrustum()
    {
        return this.frustum;
    }

    private Matrix createModelViewMatrix(FlatOrbitViewModel orbitViewModel)
    {
        if (orbitViewModel == null)
            return null;

        return orbitViewModel.getTransformMatrix();
    }

    private Matrix createProjectionMatrix(double nearClipDistance, double farClipDistance)
    {
        Angle fov = this.getFieldOfView();
        if (fov == null)
            return null;

        java.awt.Rectangle viewport = this.getViewport();
        if (viewport == null)
            return null;

        // Create a standard perspective projection.
        return Matrix.fromPerspective(
            fov,
            viewport.width, viewport.height,
            nearClipDistance, farClipDistance);
    }

    private Frustum createFrustum(double nearClipDistance, double farClipDistance)
    {
        Angle fov = this.getFieldOfView();
        if (fov == null)
            return null;

        java.awt.Rectangle viewport = this.getViewport();
        if (viewport == null)
            return null;

        // Create a standard perspective frustum.
        return Frustum.fromPerspective(
            fov, viewport.width, viewport.height,
            nearClipDistance, farClipDistance);
    }

    // ============== Attribute Initialization ======================= //
    // ============== Attribute Initialization ======================= //
    // ============== Attribute Initialization ======================= //

    protected void doInitialize(DrawContext dc)
    {
        Angle initialLookAtLatitude = getInitialLatitude(this.lookAtLatitude);
        Angle initialLookAtLongitude = getInitialLongitude(this.lookAtLongitude);
        Angle initialHeading = getInitialHeading(this.heading);
        Angle initialPitch = getInitialPitch(this.pitch);
        double initialZoom = getInitialAltitude(dc, this.zoom);
//        // Save values not included in OrbitViewModel init.
//        // These may be set after OrbitViewModel init.
//        double initialZoom = this.zoom;
//        Angle initialLookAtLatitude = this.lookAtLatitude;
//        Angle initialLookAtLongitude = this.lookAtLongitude;
//        Quaternion initialRotation = this.rotation;

        this.orbitViewModel = new FlatOrbitViewModel(
            dc,
            initialLookAtLatitude, initialLookAtLongitude,
            initialHeading, initialPitch,
            initialZoom);
        this.updateAttributes(dc);

//        // Apply values set by client before initialization,
//        // but not included in OrbitViewModel init.
//        if (initialZoom >= 0)
//            this.setZoom(initialZoom);
//        if (initialLookAtLatitude != null)
//            this.setLookAtLatitude(initialLookAtLatitude);
//        if (initialLookAtLongitude != null)
//            this.setLookAtLongitude(initialLookAtLongitude);
//        if (initialRotation != null)
//            this.setRotation(initialRotation);
    }

    protected static Angle getInitialLatitude(Angle clientValue)
    {
        // Use value specified by client.
        if (clientValue != null)
            return clientValue;

        // Use value from configuration.
        Double configValue = Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE, 0d);
        if (configValue != null)
            return Angle.fromDegrees(configValue);

        // Fallback to zero.
        return Angle.ZERO;
    }

    protected static Angle getInitialLongitude(Angle clientValue)
    {
        // Use value specified by client.
        if (clientValue != null)
            return clientValue;

        // Use longitude of system time-zone as default.
        Angle il = Angle.ZERO;
        java.util.TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
        if (tz != null)
            il = Angle.fromDegrees(180.0 * tz.getOffset(System.currentTimeMillis()) / (12.0 * 3.6e6));

        // Use value from configuration, otherwise the default computed above.
        Double configValue = Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE, il.degrees);
        if (configValue != null)
            return Angle.fromDegrees(configValue);

        // Fallback to zero.
        return Angle.ZERO;
    }

    protected static Angle getInitialHeading(Angle clientValue)
    {
        // Use value specified by client.
        if (clientValue != null)
            return clientValue;

        // Use value from configuration.
        Double configValue = Configuration.getDoubleValue(AVKey.INITIAL_HEADING, 0d);
        if (configValue != null)
            return Angle.fromDegrees(configValue);

        // Fallback to zero.
        return Angle.ZERO;
    }

    protected static Angle getInitialPitch(Angle clientValue)
    {
        // Use value specified by client.
        if (clientValue != null)
            return clientValue;

        // Use value from configuration.
        Double configValue = Configuration.getDoubleValue(AVKey.INITIAL_PITCH, 0d);
        if (configValue != null)
            return Angle.fromDegrees(configValue);

        // Fallback to zero.
        return Angle.ZERO;
    }

    protected static double getInitialAltitude(DrawContext dc, double clientValue)
    {
        // Use value specified by client.
        if (clientValue >= 0.0)
            return clientValue;

        // Use globe radius with coefficient as default.
        double ia = 0;
        if (dc != null && dc.getGlobe() != null)
            ia = 3 * dc.getGlobe().getRadius();

        // Use value from configuration.
        Double configValue = Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE, ia);
        if (configValue != null)
            return configValue;

        // Fallback to zero.
        return 0;
    }

    // Override AbstractView
    // TODO: compute largest distance to flat globe 'corners' (OK)
    public double computeHorizonDistance()
    {
        Globe globe = this.getDrawContext().getGlobe();
        Vec4 eye = this.getEyePoint();
        if(eye == null)
            return this.altitude + 10e3;
        double dist = 0;
        Vec4 p;
        // Use max distance to six points around the map
        p = globe.computePointFromPosition(Angle.POS90, Angle.NEG180, 0); // NW
        dist = Math.max(dist, eye.distanceTo3(p));
        p = globe.computePointFromPosition(Angle.POS90, Angle.POS180, 0); // NE
        dist = Math.max(dist, eye.distanceTo3(p));
        p = globe.computePointFromPosition(Angle.NEG90, Angle.NEG180, 0); // SW
        dist = Math.max(dist, eye.distanceTo3(p));
        p = globe.computePointFromPosition(Angle.NEG90, Angle.POS180, 0); // SE
        dist = Math.max(dist, eye.distanceTo3(p));
        p = globe.computePointFromPosition(Angle.ZERO, Angle.POS180, 0); // E
        dist = Math.max(dist, eye.distanceTo3(p));
        p = globe.computePointFromPosition(Angle.ZERO, Angle.NEG180, 0); // W
        dist = Math.max(dist, eye.distanceTo3(p));
        return dist;
    }
    
    protected double computeHorizonDistance(DrawContext dc, double altitude)
    {
        return this.computeHorizonDistance();
    }


    // ============== Attribute Accessors ======================= //
    // ============== Attribute Accessors ======================= //
    // ============== Attribute Accessors ======================= //

    public Angle getLatitude()
    {
        return this.latitude;
    }

    public Angle getLongitude()
    {
        return this.longitude;
    }

    public double getAltitude()
    {
        return this.altitude;
    }

    public Angle getHeading()
    {
        return this.heading;
    }

    public Angle getPitch()
    {
        return this.pitch;
    }

    public double getZoom()
    {
        return this.zoom;
    }

    public Angle getLookAtLatitude()
    {
        return this.lookAtLatitude;
    }

    public Angle getLookAtLongitude()
    {
        return this.lookAtLongitude;
    }

    public Quaternion getRotation()
    {
        return this.rotation;
    }

    private boolean onModelChange(DrawContext dc, FlatOrbitViewModel orbitViewModel)
    {
        return this.onModelChange(dc, orbitViewModel, false);
    }

    private boolean onModelChange(DrawContext dc, FlatOrbitViewModel orbitViewModel, boolean preserveCenter)
    {
        if (dc == null || orbitViewModel == null)
            return false;

        // Moves the view when it collides with surface geometry.
        boolean isAboveSurface = this.ensureViewIsAboveSurface(orbitViewModel, preserveCenter);
        // Computes new values for orbital-viewing attributes.
        this.updateAttributes(dc);

        return isAboveSurface;
    }

    private void updateAttributes(DrawContext dc)
    {
        if (this.orbitViewModel == null)
            return;

        if (dc == null)
            return;

        Position eyePos = this.orbitViewModel.getEyePosition(dc);
        if (eyePos != null)
        {
            this.latitude = eyePos.getLatitude();
            this.longitude = eyePos.getLongitude();
            this.altitude = eyePos.getElevation();
        }
        else
        {
            this.latitude = null;
            this.longitude = null;
            this.altitude = 0;
        }

        Position centerPos = this.orbitViewModel.getLookAtPosition(dc);
        if (centerPos != null)
        {
            this.lookAtLatitude = centerPos.getLatitude();
            this.lookAtLongitude = centerPos.getLongitude();
        }
        else
        {
            this.lookAtLatitude = null;
            this.lookAtLongitude = null;
        }

        this.heading = this.orbitViewModel.getHeading(dc);
        this.pitch = this.orbitViewModel.getPitch(dc);
        Double modelZoom = this.orbitViewModel.getZoom(dc);
        if (modelZoom != null)
        {
            this.zoom = modelZoom;
        }
        else
        {
            this.zoom = 0;
        }
        this.rotation = this.orbitViewModel.getRotation();
    }

    public boolean setLatitude(Angle newLatitude)
    {
        if (newLatitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.latitude = newLatitude;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        newLatitude = Angle.normalizedLatitude(newLatitude);
        Angle latChange = newLatitude.subtract(this.latitude);
        this.orbitViewModel.transformLatitude(dc, latChange);
        return this.onModelChange(dc, this.orbitViewModel);
    }

    public boolean setLongitude(Angle newLongitude)
    {
        if (newLongitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.longitude = newLongitude;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        newLongitude = Angle.normalizedLongitude(newLongitude);
        Angle lonChange = newLongitude.subtract(this.longitude);
        this.orbitViewModel.transformLongitude(dc, lonChange);
        return this.onModelChange(dc, this.orbitViewModel);
    }


    public boolean setAltitude(double newAltitude)
    {
        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.altitude = newAltitude;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        double altChange = newAltitude - this.altitude;
        this.orbitViewModel.transformAltitude(dc, altChange);
        return this.onModelChange(dc, this.orbitViewModel);
    }

    public boolean setLatLon(LatLon newLatLon)
    {
        if (newLatLon == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean success = true;
        success &= this.setLatitude(newLatLon.getLatitude());
        success &= this.setLongitude(newLatLon.getLongitude());
        return success;
    }

    public boolean setLatLonAltitude(Position newPosition)
    {
        if (newPosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean success = true;
        success &= this.setLatitude(newPosition.getLatitude());
        success &= this.setLongitude(newPosition.getLongitude());
        success &= this.setAltitude(newPosition.getElevation());
        return success;
    }

    public boolean setHeading(Angle newHeading)
    {
        if (newHeading == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.heading = newHeading;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        newHeading = normalizedHeading(newHeading);
        Angle headingChange = newHeading.subtract(this.heading);
        this.orbitViewModel.transformHeading(dc, headingChange);
        return this.onModelChange(dc, this.orbitViewModel, true);
    }

    private static Angle normalizedHeading(Angle unnormalizedAngle)
    {
        if (unnormalizedAngle == null)
            return null;

        double degrees = unnormalizedAngle.degrees;
        double heading = degrees % 360;
        return Angle.fromDegrees(heading > 180 ? heading - 360 : (heading < -180 ? 360 + heading : heading));
    }

    public boolean setPitch(Angle newPitch)
    {
        if (newPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.pitch = newPitch;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        boolean isPitchInRange = isPitchInRange(newPitch);
        if (!isPitchInRange)
            newPitch = clampedPitch(newPitch);
        Angle pitchChange = newPitch.subtract(this.pitch);
        this.orbitViewModel.transformPitch(dc, pitchChange);
        return this.onModelChange(dc, this.orbitViewModel, true) && isPitchInRange;
    }

    private static boolean isPitchInRange(Angle angle)
    {
        if (angle == null)
            return false;

        double pitch = angle.degrees % 360;
        return (pitch >= 0) &&  (pitch <= 90);
    }

    private static Angle clampedPitch(Angle unclampedAngle)
    {
        if (unclampedAngle == null)
            return null;

        double degrees = unclampedAngle.degrees;
        double pitch = degrees % 360;
        return Angle.fromDegrees(pitch > 90 ? 90 : (pitch < 0 ? 0 : pitch));
    }

    public boolean setZoom(double newZoom)
    {
        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.zoom = newZoom;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        boolean isZoomInRange = isZoomInRange(newZoom);
        if (!isZoomInRange)
            newZoom = clampedZoom(newZoom);
        double zoomChange = newZoom - this.zoom;
        this.orbitViewModel.transformZoom(dc, zoomChange);
        return this.onModelChange(dc, this.orbitViewModel) && isZoomInRange;
    }

    private static boolean isZoomInRange(double value)
    {
        return value >= 1;
    }

    private static double clampedZoom(double unclampedValue)
    {
        return unclampedValue < 1 ? 1 : unclampedValue;
    }

    public boolean setLookAtLatitude(Angle newLatitude)
    {
        if (newLatitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.lookAtLatitude = newLatitude;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        boolean isLatInRange = isLatitudeInRange(newLatitude);
        if (!isLatInRange)
            newLatitude = clampedLatitude(newLatitude);
        Angle latChange = newLatitude.subtract(this.lookAtLatitude);
        this.orbitViewModel.transformLatitude(dc, latChange);
        return this.onModelChange(dc, this.orbitViewModel) && isLatInRange;
    }

    private static boolean isLatitudeInRange(Angle angle)
    {
        if (angle == null)
            return false;

        double latitude = angle.degrees;
        return (latitude >= -90) && (latitude <= 90);
    }

    private static Angle clampedLatitude(Angle unclampedAngle)
    {
        if (unclampedAngle == null)
            return null;

        double unclampedDegrees = unclampedAngle.degrees;
        double degrees = unclampedDegrees < -90 ? -90 : (unclampedDegrees > 90 ? 90 : unclampedDegrees);
        return Angle.fromDegrees(degrees);
    }

    public boolean setLookAtLongitude(Angle newLongitude)
    {
        if (newLongitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.lookAtLongitude = newLongitude;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        newLongitude = Angle.normalizedLongitude(newLongitude);
        Angle lonChange = newLongitude.subtract(this.lookAtLongitude);
        this.orbitViewModel.transformLongitude(dc, lonChange);
        return this.onModelChange(dc, this.orbitViewModel);
    }

    public boolean setLookAtLatLon(LatLon newLatLon)
    {
        if (newLatLon == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        boolean success = true;
        success &= this.setLookAtLatitude(newLatLon.getLatitude());
        success &= this.setLookAtLongitude(newLatLon.getLongitude());
        return success;
    }

    public boolean setRotation(Quaternion newRotation)
    {
        if (newRotation == null)
        {
            String message = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // OrbitViewModel has not been initialized, set value directly.
        if (this.orbitViewModel == null)
        {
            this.rotation = newRotation;
            return true;
        }

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        this.orbitViewModel.setRotation(dc, newRotation);
        return this.onModelChange(dc, this.orbitViewModel);
    }

    public Quaternion createRotationBetweenPositions(Position beginPosition, Position endPosition)
    {
        if (beginPosition == null || endPosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.orbitViewModel == null)
            return null;

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return null;

        return this.orbitViewModel.createRotationBetweenPositions(dc, beginPosition, endPosition);
    }

    public Quaternion createRotationForward(Angle amount)
    {
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.orbitViewModel == null)
            return null;

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return null;

        return this.orbitViewModel.createRotationForward(dc, amount);
    }

    public Quaternion createRotationRight(Angle amount)
    {
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.orbitViewModel == null)
            return null;

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return null;

        return this.orbitViewModel.createRotationRight(dc, amount);
    }

    // ============== Surface Collision Detection ======================= //
    // ============== Surface Collision Detection ======================= //
    // ============== Surface Collision Detection ======================= //

    private boolean ensureViewIsAboveSurface(FlatOrbitViewModel orbitViewModel, boolean preserveCenter)
    {
        if (orbitViewModel == null)
            return false;

        DrawContext dc = this.getDrawContext();
        if (dc == null)
            return false;

        // Apply collision resolution.
        double heightAboveSurface = this.computeFrustumHeightAboveSurface(dc, orbitViewModel, this.frustum);
        if (heightAboveSurface >= 0)
            return true;

        if (preserveCenter)
        {
            this.fixViewBelowSurfaceWithPitch(dc, orbitViewModel, heightAboveSurface);
        }
        else
        {
            this.fixViewBelowSurfaceWithAltitude(dc, orbitViewModel, heightAboveSurface);
        }

        return false;
    }

    private void fixViewBelowSurfaceWithAltitude(DrawContext dc, FlatOrbitViewModel orbitViewModel,
        double heightAboveSurface)
    {
        if (dc == null || orbitViewModel == null)
            return;

        int i = 0;
        final int MAX_ITERATIONS = 2;
        while ((i < MAX_ITERATIONS) && (heightAboveSurface < 0))
        {
            orbitViewModel.transformAltitude(dc, -heightAboveSurface);
            heightAboveSurface = this.computeFrustumHeightAboveSurface(dc, orbitViewModel, this.frustum);
            i++;
        }
    }

    private void fixViewBelowSurfaceWithPitch(DrawContext dc, FlatOrbitViewModel orbitViewModel,
        double heightAboveSurface)
    {
        if (dc == null || orbitViewModel == null)
            return;

        int i = 0;
        final int MAX_ITERATIONS = 4;
        while ((i < MAX_ITERATIONS) && (heightAboveSurface < 0))
        {
            this.transformPitchByHeight(dc, orbitViewModel, heightAboveSurface);
            heightAboveSurface = this.computeFrustumHeightAboveSurface(dc, orbitViewModel, this.frustum);
            i++;
        }

        if (heightAboveSurface < 0)
            this.fixViewBelowSurfaceWithAltitude(dc, orbitViewModel, heightAboveSurface);
    }

    private void transformPitchByHeight(DrawContext dc, FlatOrbitViewModel orbitViewModel, double heightAboveSurface)
    {
        if (dc == null || orbitViewModel == null)
            return;

        Vec4 eyeVec = orbitViewModel.getEyeVector();
        if (eyeVec == null)
            return;

        Vec4 centerVec = orbitViewModel.getLookAtVector(dc);
        if (centerVec == null)
            return;

        Vec4 normalVec = Vec4.UNIT_Z; // TODO: use globe normal
        double dot1 = normalVec.dot3(eyeVec.subtract3(centerVec).normalize3());
        if (dot1 >= 1)
            return;

        Vec4 newEyeVec = Vec4.fromLine3(
            dc.getGlobe().getCenter(),
            eyeVec.getLength3() - heightAboveSurface,
            eyeVec.normalize3());

        double dot2 = normalVec.dot3(newEyeVec.subtract3(centerVec).normalize3());
        if (dot2 >= 1)
            return;

        double changeRadians = Math.acos(dot2) - Math.acos(dot1);
        orbitViewModel.transformPitch(dc, Angle.fromRadians(changeRadians));
    }

    private double computeFrustumHeightAboveSurface(DrawContext dc, FlatOrbitViewModel orbitViewModel, Frustum frustum)
    {
        if (dc == null
            || orbitViewModel == null
            || frustum == null)
            return 0;

        java.awt.Rectangle viewport = this.getViewport();
        Angle fov = this.getFieldOfView();
        if ((viewport == null) || (viewport.width == 0) || (fov == null))
            return 0;

        Vec4 eyeVec = orbitViewModel.getEyeVector();
        if (eyeVec == null)
            return 0;

        Matrix matrix = orbitViewModel.getTransformMatrix();
        if (matrix == null)
            return 0;

        double nearClipDist = Math.abs(frustum.getNear().getDistance());
        double tanHalfFov = fov.tanHalfAngle();
        double clipRectX = nearClipDist * tanHalfFov;
        double clipRectY = viewport.height * clipRectX / (double) viewport.width;

        Vec4 center = new Vec4(0, 0, -nearClipDist).transformBy4(matrix.getInverse());
        Vec4 llCorner = new Vec4(-clipRectX, -clipRectY, -nearClipDist).transformBy4(matrix.getInverse());
        Vec4 lrCorner = new Vec4(clipRectX, -clipRectY, -nearClipDist).transformBy4(matrix.getInverse());

        double minHeight = this.findLowestHeightAboveSurface(dc, eyeVec, center, llCorner, lrCorner);
        return minHeight - COLLISION_PADDING_HEIGHT;
    }

    private double findLowestHeightAboveSurface(DrawContext dc, Vec4... points)
    {
        if (dc == null
            || points == null
            || points.length == 0)
            return 0;

        double minHeight = Double.MAX_VALUE;
        for (Vec4 vec4 : points)
        {
            double height = this.computeHeightAboveSurface(dc, vec4);
            if (height < minHeight)
                minHeight = height;
        }
        return minHeight;
    }
}
