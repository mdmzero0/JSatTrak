/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

/**
 * @author Patrick Muris from dcollins OrbitViewModel
 * @version $Id$
 */
class FlatOrbitViewModel
{
    private Matrix transformMatrix = null;

    public FlatOrbitViewModel(DrawContext dc)
    {
        this(dc, Angle.ZERO, Angle.ZERO, Angle.ZERO, Angle.ZERO, 0);
    }

    public FlatOrbitViewModel(DrawContext dc,
                          Angle lookAtLatitude, Angle lookAtLongitude,
                          Angle heading, Angle pitch,
                          double zoom)
    {
        this(dc, createInitTransform(
            dc,
            lookAtLatitude, lookAtLongitude,
            heading, pitch,
            zoom));
    }

    public FlatOrbitViewModel(DrawContext dc, Matrix transformMatrix)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (transformMatrix == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setTransform(dc, transformMatrix);
    }

    public Matrix getTransformMatrix()
    {
        return this.transformMatrix;
    }

    private void onTransformChange(DrawContext dc, Matrix newTransformMatrix)
    {
    }

    public void setTransform(DrawContext dc, Matrix newTransformMatrix)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newTransformMatrix == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.transformMatrix = newTransformMatrix;
        this.onTransformChange(dc, newTransformMatrix);
    }

    public void transform(DrawContext dc, Matrix transformMatrix)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (transformMatrix == null)
        {
            String message = Logging.getMessage("nullValue.MatrixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return;

        Matrix newTransformMatrix = this.transformMatrix.multiply(transformMatrix);
        this.setTransform(dc, newTransformMatrix);
    }

    public void transformLatitude(DrawContext dc, Angle amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix latTransform = this.createLatitudeTransform(dc, amount);
        if (latTransform == null)
            return;

        if (this.transformMatrix == null)
            return;

        // Test transform application.
        Matrix newTransformMatrix = this.transformMatrix.multiply(latTransform);
        Vec4 center = computeLookAtPoint(dc, this.transformMatrix);
        Vec4 newCenter = computeLookAtPoint(dc, newTransformMatrix);
        Position centerPos = dc.getGlobe().computePositionFromPoint(center);
        Position newCenterPos = dc.getGlobe().computePositionFromPoint(newCenter);
        // Abort the transform when it causes model errors.
        final double EPSILON = 1;
        if (Math.abs(newCenterPos.getLongitude().subtract(centerPos.getLongitude()).degrees) > EPSILON)
            return;

        this.transform(dc, latTransform);
    }

    public void transformLongitude(DrawContext dc, Angle amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix lonTransform = this.createLongitudeTransform(dc, amount);
        if (lonTransform == null)
            return;

        this.transform(dc, lonTransform);
    }

    public void transformAltitude(DrawContext dc, double amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix altTransform = this.createAltitudeTransform(dc, amount);
        if (altTransform == null)
            return;

        this.transform(dc, altTransform);
    }

    public void transformHeading(DrawContext dc, Angle amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix headingTransform = this.createHeadingTransform(dc, amount);
        if (headingTransform == null)
            return;

        this.transform(dc, headingTransform);
    }

    public void transformPitch(DrawContext dc, Angle amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix pitchTransform = this.createPitchTransform(dc, amount);
        if (pitchTransform == null)
            return;

        if (this.transformMatrix == null)
            return;

        // Test transform application.
        Matrix newTransformMatrix = this.transformMatrix.multiply(pitchTransform);
        Vec4 centerVec = computeLookAtPoint(dc, this.transformMatrix);
        Vec4 newHeading = computeHeadingVector(newTransformMatrix, centerVec);
        Vec4 newForward = Vec4.UNIT_NEGATIVE_Z.transformBy3(newTransformMatrix.getInverse());
        // Abort the transform when it causes model errors.
        if (newHeading.dot3(newForward) < 0)
            return;

        this.transform(dc, pitchTransform);
    }

    public void transformZoom(DrawContext dc, double amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Matrix zoomTransform = this.createZoomTransform(dc, amount);
        if (zoomTransform == null)
            return;

        this.transform(dc, zoomTransform);
    }

    public Quaternion getRotation()
    {
        return Quaternion.fromMatrix(this.transformMatrix);
    }

    public void setRotation(DrawContext dc, Quaternion newRotation)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (newRotation == null)
        {
            String message = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return;

        Matrix posMatrix = Matrix.fromTranslation(
                this.transformMatrix.m14,
                this.transformMatrix.m24,
                this.transformMatrix.m34);
        Matrix rotMatrix = Matrix.fromQuaternion(newRotation);
        Matrix newTransformMatrix = posMatrix.multiply(rotMatrix);
        this.setTransform(dc, newTransformMatrix);
    }

    public void transformRotation(DrawContext dc, Quaternion rotation)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (rotation == null)
        {
            String message = Logging.getMessage("nullValue.QuaternionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Quaternion transformRotation = Quaternion.fromMatrix(this.transformMatrix);
        Quaternion newTransformRotation = transformRotation.multiply(rotation);
        this.setRotation(dc, newTransformRotation);
    }

    // ============== Model Transforms ======================= //
    // ============== Model Transforms ======================= //
    // ============== Model Transforms ======================= //

    private static Matrix createInitTransform(
        DrawContext dc,
        Angle lookAtLatitude, Angle lookAtLongitude,
        Angle heading, Angle pitch,
        double zoom)
    {
        if (dc == null
            || dc.getGlobe() == null
            || lookAtLatitude == null
            || lookAtLongitude == null
            || heading == null
            || pitch == null)
            return null;

        Globe globe = dc.getGlobe();
//        Vec4 globeOrigin = globe.getCenter();
        Matrix initTransform = Matrix.IDENTITY;
        // Not sure why this isn't necessary, but when globeOrigin!=0 View is still centered on the Globe,
        // without this code.
//        initTransform = initTransform.multiply(Matrix.fromTranslation(
//            globeOrigin.x,
//            globeOrigin.y,
//            globeOrigin.z));
        // Flat sea level at zero on z, no need to move from globe center to surface
//        initTransform = initTransform.multiply(Matrix.fromTranslation(
//            0.0,
//            0.0,
//            0.0 - globe.getRadiusAt(lookAtLatitude, lookAtLongitude)));
        initTransform = initTransform.multiply(Matrix.fromRotationX(heading.multiply(-1)));
        initTransform = initTransform.multiply(Matrix.fromRotationZ(pitch));
        initTransform = initTransform.multiply(Matrix.fromTranslation(
            0.0,
            0.0,
            0.0 - zoom));
        // Use translation for lat/lon placement
        Vec4 lookAtPoint = globe.computePointFromPosition(lookAtLatitude, lookAtLongitude, 0);
        initTransform = initTransform.multiply(Matrix.fromTranslation(
                -lookAtPoint.x,  -lookAtPoint.y,  -lookAtPoint.z));

        return initTransform;
    }

    // TODO: use translation on y (OK)
    public Matrix createLatitudeTransform(DrawContext dc, Angle amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 delta = dc.getGlobe().computePointFromPosition(amount, Angle.ZERO, 0);
        return Matrix.fromTranslation(-delta.x, -delta.y, -delta.z);
    }

    // TODO: use translation on x (OK)
    public Matrix createLongitudeTransform(DrawContext dc, Angle amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 delta = dc.getGlobe().computePointFromPosition(Angle.ZERO, amount, 0);
        return Matrix.fromTranslation(-delta.x, -delta.y, -delta.z);
    }
    // TODO: use translation on z (OK)
    public Matrix createAltitudeTransform(DrawContext dc, double amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return Matrix.fromTranslation(0, 0, amount);
    }

    public Matrix createHeadingTransform(DrawContext dc, Angle amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 centerVec = this.getLookAtVector(dc);
        if (centerVec == null)
            return null;

        Vec4 axis = Vec4.UNIT_Z;    // TODO: use globe normal (OK)
        Matrix axisAngleTransform = Matrix.fromAxisAngle(amount, axis);
        return this.createTransformAboutPivot(axisAngleTransform, centerVec);
    }

    public Matrix createPitchTransform(DrawContext dc, Angle amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return null;

        Vec4 centerVec = this.getLookAtVector(dc);
        if (centerVec == null)
            return null;

        Vec4 axis = Vec4.UNIT_X.transformBy3(this.transformMatrix.getInverse());
        Matrix axisAngleTransform = Matrix.fromAxisAngle(amount.multiply(-1), axis);
        return this.createTransformAboutPivot(axisAngleTransform, centerVec);
    }

    public Matrix createZoomTransform(DrawContext dc, double amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return null;

        Vec4 zoomVec = Vec4.UNIT_NEGATIVE_Z.transformBy3(this.transformMatrix.getInverse());
        zoomVec = zoomVec.normalize3();
        zoomVec = zoomVec.multiply3(amount);
        return Matrix.fromTranslation(zoomVec);
    }

    private Matrix createTransformAboutPivot(Matrix transform, Vec4 pivot)
    {
        if (transform == null || pivot == null)
            return null;

        Matrix matrix = Matrix.IDENTITY;
        matrix = matrix.multiply(Matrix.fromTranslation(pivot.x, pivot.y, pivot.z));
        matrix = matrix.multiply(transform);
        matrix = matrix.multiply(Matrix.fromTranslation(-pivot.x, -pivot.y, -pivot.z));
        return matrix;
    }

    // ============== Rotation Transforms ======================= //
    // ============== Rotation Transforms ======================= //
    // ============== Rotation Transforms ======================= //

    // TODO: return no rotation or use globe normal (OK)
    public Quaternion createRotationBetweenPositions(DrawContext dc, Position beginPosition, Position endPosition)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginPosition == null || endPosition == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        /*
        Globe globe = dc.getGlobe();
        if (globe == null)
            return null;

        Vec4 beginPoint = globe.computePointFromPosition(beginPosition);
        Vec4 endPoint = globe.computePointFromPosition(endPosition);
        if (beginPoint == null || endPoint == null)
            return null;

        Angle angle = beginPoint.angleBetween3(endPoint);
        if (angle == null)
            return null;

        Vec4 axis = beginPoint.cross3(endPoint);
        return Quaternion.fromAxisAngle(angle, axis);
        */
        return Quaternion.IDENTITY;
    }

    public Quaternion createRotationForward(DrawContext dc, Angle amount)
    {
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return null;

        Vec4 xAxis = Vec4.UNIT_X.transformBy3(this.transformMatrix.getInverse());
        return Quaternion.fromAxisAngle(amount, xAxis);
    }

    public Quaternion createRotationRight(DrawContext dc, Angle amount)
    {
        if (amount == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return null;

        Vec4 xAxis = Vec4.UNIT_X.transformBy3(this.transformMatrix.getInverse());
        Vec4 centerVec = this.getLookAtVector(dc);
        centerVec = centerVec.normalize3();
        Vec4 yAxisNoTilt = centerVec.cross3(xAxis);
        return Quaternion.fromAxisAngle(amount.multiply(-1), yAxisNoTilt);
    }

    // ============== Viewing Attributes ======================= //
    // ============== Viewing Attributes ======================= //
    // ============== Viewing Attributes ======================= //

    public Vec4 getEyeVector()
    {
        if (this.transformMatrix == null)
            return null;

        return Vec4.UNIT_W.transformBy4(this.transformMatrix.getInverse());
    }

    public Position getEyePosition(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 eyeVec = this.getEyeVector();
        if (eyeVec == null)
            return null;

        if (dc.getGlobe() == null)
            return null;

        return dc.getGlobe().computePositionFromPoint(eyeVec);
    }

    public Vec4 getLookAtVector(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return null;

        return computeLookAtPoint(dc, this.transformMatrix);
    }

    public Position getLookAtPosition(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 centerVec = this.getLookAtVector(dc);
        if (centerVec == null)
            return null;

        if (dc.getGlobe() == null)
            return null;

        return dc.getGlobe().computePositionFromPoint(centerVec);
    }

    public Angle getHeading(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return null;

        Vec4 centerVec = this.getLookAtVector(dc);
        if (centerVec == null)
            return null;

        HeadingCoordinates headingCoordinates = computeHeadingCoordinates(centerVec);
        if (headingCoordinates == null)
            return null;

        return computeHeading(headingCoordinates, this.transformMatrix, centerVec);
    }

    public Angle getPitch(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return null;

        Vec4 centerVec = this.getLookAtVector(dc);
        if (centerVec == null)
            return null;

        return computePitch(this.transformMatrix, centerVec);
    }

    public Double getZoom(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.transformMatrix == null)
            return null;

        Vec4 centerVec = this.getLookAtVector(dc);
        if (centerVec == null)
            return null;

        return computeZoom(this.transformMatrix, centerVec);
    }

    // ============== Attribute Computation Support ======================= //
    // ============== Attribute Computation Support ======================= //
    // ============== Attribute Computation Support ======================= //

    private static Vec4 computeLookAtPoint(DrawContext dc, Matrix transformMatrix)
    {
        if (dc == null
            || dc.getGlobe() == null
            || transformMatrix == null)
            return null;

        Globe globe = dc.getGlobe();
        Vec4 eyeVec = Vec4.UNIT_W.transformBy4(transformMatrix.getInverse());
        Vec4 forwardVec = Vec4.UNIT_NEGATIVE_Z.transformBy3(transformMatrix.getInverse());
        forwardVec = forwardVec.normalize3();

        Position centerPos = globe.getIntersectionPosition(new Line(eyeVec, forwardVec));
        if (centerPos != null)
        {
            Angle centerLat = centerPos.getLatitude();
            Angle centerLon = centerPos.getLongitude();
            double centerElevation = dc.getVerticalExaggeration() * globe.getElevation(centerLat, centerLon);
            return globe.computePointFromPosition(centerLat, centerLon, centerElevation);
        }

        return computeHorizonPoint(globe, transformMatrix);
    }

    // TODO: adapt to flat globe
    private static Vec4 computeHorizonPoint(Globe globe, Matrix transformMatrix)
    {
        if (globe == null || transformMatrix == null)
            return null;

        Vec4 globeOrigin = globe.getCenter();
        Vec4 eyeVec = Vec4.UNIT_W.transformBy4(transformMatrix.getInverse());
        Vec4 forward = Vec4.UNIT_NEGATIVE_Z.transformBy3(transformMatrix.getInverse());
        Vec4 origin_sub_eye = globeOrigin.subtract3(eyeVec);

        double r = globe.getRadius();
        double dSquared = origin_sub_eye.getLengthSquared3() - (r * r);
        if (dSquared < 0)
            return null;

        double d = Math.sqrt(dSquared);
        return forward.normalize3().multiply3(d).add3(eyeVec);
    }

    private static Angle computeHeading(HeadingCoordinates headingCoords, Matrix transformMatrix, Vec4 lookAtPoint)
    {
        if (headingCoords == null
            || transformMatrix == null
            || lookAtPoint == null)
            return null;

        Vec4 headingVec = computeHeadingVector(transformMatrix, lookAtPoint);
        if (headingVec == null)
            return null;

        double dot = headingCoords.northVec.dot3(headingVec);
        // Compute the sum of magnitudes.
        double length = headingCoords.northVec.getLength3() * headingVec.getLength3();
        // Normalize the dot product, if necessary.
        if ((length != 0) && (length != 1.0))
            dot /= length;

        if (dot < -1) // Angle is positive 180.
        {
            return Angle.POS180;
        }
        else if (dot > 1) // Angle is zero.
        {
            return Angle.ZERO;
        }

        double degrees = Math.toDegrees(Math.acos(dot));
        if (Double.isNaN(degrees))
            return null;

        if (headingCoords.eastVec.dot3(headingVec) < 0)
            degrees = 360 - degrees;

        // Collapses duplicate values for North heading.
        if (degrees == 360)
            degrees = 0;

        return Angle.fromDegrees(degrees);
    }

    private static class HeadingCoordinates
    {
        public final Vec4 northVec;
        public final Vec4 eastVec;

        public HeadingCoordinates(Vec4 northVec, Vec4 eastVec)
        {
            this.northVec = northVec;
            this.eastVec = eastVec;
        }
    }

    private static HeadingCoordinates computeHeadingCoordinates(Vec4 lookAtPoint)
    {
        if (lookAtPoint == null)
            return null;

        Vec4 surfaceNormal = Vec4.UNIT_Z; // TODO: get globe normal (OK)
        Vec4 y_sub_normal = Vec4.UNIT_Y.subtract3(surfaceNormal);
        y_sub_normal = y_sub_normal.normalize3();
        Vec4 eastVec = y_sub_normal.cross3(surfaceNormal);
        Vec4 northVec = surfaceNormal.cross3(eastVec);
        eastVec = northVec.cross3(surfaceNormal);

        return new HeadingCoordinates(northVec, eastVec);
    }

    private static Vec4 computeHeadingVector(Matrix transformMatrix, Vec4 surfacePoint)
    {
        if (transformMatrix == null || surfacePoint == null)
            return null;

        Vec4 surfaceNormal = Vec4.UNIT_Z; // TODO: get globe normal  (OK)
        Vec4 forward = Vec4.UNIT_NEGATIVE_Z.transformBy3(transformMatrix.getInverse());
        Vec4 up = Vec4.UNIT_Y.transformBy3(transformMatrix.getInverse());

        final double EPSILON = 0.1;
        Vec4 heading = forward.cross3(surfaceNormal);
        if (heading.getLength3() < EPSILON)
            heading = up.cross3(surfaceNormal);
        heading = surfaceNormal.cross3(heading);
        heading = heading.normalize3();

        return heading;
    }

    private static Angle computePitch(Matrix transformMatrix, Vec4 lookAtPoint)
    {
        if (transformMatrix == null || lookAtPoint == null)
            return null;

        Vec4 surfaceNormal = Vec4.UNIT_Z; // TODO: get globe normal
        Vec4 forward = Vec4.UNIT_Z.transformBy3(transformMatrix.getInverse());
        forward = forward.normalize3();

        double dot = surfaceNormal.dot3(forward);
        // Compute the sum of magnitudes.
        double length = surfaceNormal.getLength3() * forward.getLength3();
        // Normalize the dot product, if necessary.
        if ((length != 0) && (length != 1))
            dot /= length;

        if (dot <= 0) // Angle is positive 90.
        {
            return Angle.POS90;
        }
        else if (dot >= 1) // Angle is zero.
        {
            return Angle.ZERO;
        }
        else // Angle is arc-cosine of dot product.
        {
            double radians = Math.acos(dot);
            if (!Double.isNaN(radians))
                return Angle.fromRadians(radians);
        }

        return null;
    }

    private static Double computeZoom(Matrix transformMatrix, Vec4 lookAtPoint)
    {
        if (transformMatrix == null || lookAtPoint == null)
            return null;

        Vec4 eyeVec = Vec4.UNIT_W.transformBy4(transformMatrix.getInverse());
        return lookAtPoint.subtract3(eyeVec).getLength3();
    }

//    private static class SurfaceAttributes
//    {
//        public final Vec4 center;
//        public final Position centerPos;
//        public final Vec4 north;
//        public final Vec4 east;
//
//        public SurfaceAttributes(Vec4 centerVec, Position centerPos, Vec4 headingUp, Vec4 headingRight)
//        {
//            this.center = centerVec;
//            this.centerPos = centerPos;
//            this.north = headingUp;
//            this.east = headingRight;
//        }
//    }

//    private static SurfaceAttributes computeSurfaceAttributes(DrawContext dc, Matrix transformMatrix)
//    {
//        if (dc == null || dc.getGlobe() == null)
//            return null;
//
//        if (transformMatrix == null)
//            return null;
//
//        Globe globe = dc.getGlobe();
//        Vec4 eyeVec = Vec4.UNIT_W.transformBy4(transformMatrix.getInverse());
//        Vec4 forwardVec = Vec4.UNIT_NEGATIVE_Z.transformBy3(transformMatrix.getInverse());
//        forwardVec = forwardVec.normalize3();
//
//        Vec4 centerVec;
//        Position centerPos = globe.getIntersectionPosition(new Line(eyeVec, forwardVec));
//        if (centerPos != null)
//        {
//            Angle centerLat = centerPos.getLatitude();
//            Angle centerLon = centerPos.getLongitude();
//            double centerElevation = dc.getVerticalExaggeration() * globe.getElevation(centerLat, centerLon);
//            centerVec = globe.computePointFromPosition(centerLat, centerLon, centerElevation);
//        }
//        else
//        {
//            centerVec = computeHorizonPoint(globe, transformMatrix);
//            centerPos = globe.computePositionFromPoint(centerVec);
//        }
//
//        if (centerVec == null)
//            return null;
//
//        Vec4 normalVec = centerVec.normalize3();
//
//        Vec4 y_sub_normal = Vec4.UNIT_Y.subtract3(normalVec);
//        y_sub_normal = y_sub_normal.normalize3();
//        Vec4 headingRight = y_sub_normal.cross3(normalVec);
//        Vec4 headingUp = normalVec.cross3(headingRight);
//        headingRight = headingUp.cross3(normalVec);
//
//        return new SurfaceAttributes(centerVec, centerPos, headingUp.normalize3(), headingRight.normalize3());
//    }
}
