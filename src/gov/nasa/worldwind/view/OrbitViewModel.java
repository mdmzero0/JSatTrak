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
 * @author dcollins
 * @version $Id: OrbitViewModel.java 3570 2007-11-18 19:56:49Z dcollins $
 */
class OrbitViewModel
{
    private Matrix transformMatrix = null;
    private Vec4 currentLookAtPoint = null;
    private Vec4 currentPivotPoint = null;

    public OrbitViewModel(DrawContext dc)
    {
        this(dc, Angle.ZERO, Angle.ZERO, Angle.ZERO, Angle.ZERO, 0);
    }

    public OrbitViewModel(DrawContext dc,
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

    public OrbitViewModel(DrawContext dc, Matrix transformMatrix)
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
        this.clearCurrentLookAtPoint();
        this.clearCurrentPivotPoint();
    }

    public Matrix getTransformMatrix()
    {
        return this.transformMatrix;
    }

    /*private void onTransformChange(DrawContext dc, Matrix newTransformMatrix)
    {
    }*/

    private void setTransform(DrawContext dc, Matrix newTransformMatrix)
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
        /*this.onTransformChange(dc, newTransformMatrix);*/
    }

    private void transform(DrawContext dc, Matrix transformMatrix)
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
        Vec4 center = computeViewportCenterPointOnGeoid(dc, this.transformMatrix);
        Vec4 newCenter = computeViewportCenterPointOnGeoid(dc, newTransformMatrix);
        Position centerPos = dc.getGlobe().computePositionFromPoint(center);
        Position newCenterPos = dc.getGlobe().computePositionFromPoint(newCenter);
        // Abort the transform when it causes model errors.
        final double EPSILON = 1;
        if (Math.abs(newCenterPos.getLongitude().subtract(centerPos.getLongitude()).degrees) > EPSILON)
            return;

        this.transform(dc, latTransform);
        this.clearCurrentLookAtPoint();
        this.clearCurrentPivotPoint();
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
        this.clearCurrentLookAtPoint();
        this.clearCurrentPivotPoint();
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
        this.clearCurrentLookAtPoint();
        this.clearCurrentPivotPoint();
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
        this.clearCurrentLookAtPoint();
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
        Vec4 centerVec = computeViewportCenterPointOnGeoid(dc, this.transformMatrix);
        Vec4 newHeading = computeHeadingVector(newTransformMatrix, centerVec);
        Vec4 newForward = Vec4.UNIT_NEGATIVE_Z.transformBy3(newTransformMatrix.getInverse());
        // Abort the transform when the pitch goes "over" 0 degrees.
        if (newHeading.dot3(newForward) < 0)
            return;
        // Abort the transform when the pitch goes "below" 90 degrees.
        if (newForward.dot3(centerVec) >= 0)
            return;

        this.transform(dc, pitchTransform);
        this.clearCurrentLookAtPoint();
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
        this.clearCurrentLookAtPoint();
        this.clearCurrentPivotPoint();
    }

    /*private void transformRotation(DrawContext dc, Quaternion rotation)
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
    }*/

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
        initTransform = initTransform.multiply(Matrix.fromTranslation(
            0.0,
            0.0,
            0.0 - globe.getRadiusAt(lookAtLatitude, lookAtLongitude)));
        initTransform = initTransform.multiply(Matrix.fromRotationX(heading.multiply(-1)));
        initTransform = initTransform.multiply(Matrix.fromRotationZ(pitch));
        initTransform = initTransform.multiply(Matrix.fromTranslation(
            0.0,
            0.0,
            0.0 - zoom));
        initTransform = initTransform.multiply(Matrix.fromRotationX(lookAtLatitude));
        initTransform = initTransform.multiply(Matrix.fromRotationY(lookAtLongitude.multiply(-1)));

        return initTransform;
    }

    private Matrix createLatitudeTransform(DrawContext dc, Angle amount)
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

        Vec4 centerVec = this.computeLookAtPoint(dc);
        if (centerVec == null)
            return null;

        Vec4 surfaceNormal = centerVec.normalize3();
        Vec4 axis = Vec4.UNIT_Y.cross3(surfaceNormal);
        return Matrix.fromAxisAngle(amount, axis);
    }

    private Matrix createLongitudeTransform(DrawContext dc, Angle amount)
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

        Vec4 axis = Vec4.UNIT_Y;
        return Matrix.fromAxisAngle(amount.multiply(-1), axis);
    }

    private Matrix createAltitudeTransform(DrawContext dc, double amount)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 altVec = this.getEyeVector();
        if (altVec == null)
            return null;

        altVec = altVec.normalize3();
        altVec = altVec.multiply3(-amount);
        return Matrix.fromTranslation(altVec);
    }

    private Matrix createHeadingTransform(DrawContext dc, Angle amount)
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

        Vec4 pivotVec = this.computePivotPoint(dc);
        if (pivotVec == null)
            return null;

        Vec4 axis = pivotVec.normalize3();
        Matrix axisAngleTransform = Matrix.fromAxisAngle(amount, axis);
        return this.createTransformAboutPivot(axisAngleTransform, pivotVec);
    }

    private Matrix createPitchTransform(DrawContext dc, Angle amount)
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

        Vec4 pivotVec = this.computePivotPoint(dc);
        if (pivotVec == null)
            return null;

        Vec4 axis = Vec4.UNIT_X.transformBy3(this.transformMatrix.getInverse());
        Matrix axisAngleTransform = Matrix.fromAxisAngle(amount.multiply(-1), axis);
        return this.createTransformAboutPivot(axisAngleTransform, pivotVec);
    }

    private Matrix createZoomTransform(DrawContext dc, double amount)
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
        Vec4 centerVec = this.computeLookAtPoint(dc);
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

    public Vec4 computeLookAtPoint(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.currentLookAtPoint == null)
        {
            if (this.transformMatrix != null)
            {
                this.currentLookAtPoint = computeViewportCenterPointOnGeoid(dc, this.transformMatrix);
            }
        }
        return this.currentLookAtPoint;
    }

    private void clearCurrentLookAtPoint()
    {
        this.currentLookAtPoint = null;
    }

    private Vec4 computePivotPoint(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.currentPivotPoint == null)
        {
            // Attempt to get the pick point at screen center.
            if (dc.getGlobe() != null && dc.getViewportCenterPosition() != null)
            {
                Position centerPos = dc.getViewportCenterPosition();
                this.currentPivotPoint = dc.getGlobe().computePointFromPosition(centerPos);
            }
            // Fallback to ray-globe intersection.
            else if (this.transformMatrix != null)
            {
                this.currentPivotPoint = computeViewportCenterPointOnGeoid(dc, this.transformMatrix);
            }
        }

        return this.currentPivotPoint;
    }

    private void clearCurrentPivotPoint()
    {
        this.currentPivotPoint = null;
    }

    public Position getLookAtPosition(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Vec4 centerVec = this.computeLookAtPoint(dc);
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

        Vec4 centerVec = this.computeLookAtPoint(dc);
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

        Vec4 centerVec = this.computeLookAtPoint(dc);
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

        Vec4 pivotVec = this.computePivotPoint(dc);
        if (pivotVec == null)
            return null;

        return computeZoom(this.transformMatrix, pivotVec);
    }

    // ============== Attribute Computation Support ======================= //
    // ============== Attribute Computation Support ======================= //
    // ============== Attribute Computation Support ======================= //

    private static Vec4 computeViewportCenterPointOnGeoid(DrawContext dc, Matrix transformMatrix)
    {
        if (dc == null
            || dc.getGlobe() == null
            || transformMatrix == null)
            return null;

        Globe globe = dc.getGlobe();
        Vec4 eyeVec = Vec4.UNIT_W.transformBy4(transformMatrix.getInverse());
        Vec4 forwardVec = Vec4.UNIT_NEGATIVE_Z.transformBy3(transformMatrix.getInverse());
        forwardVec = forwardVec.normalize3();

        Vec4 viewportCenterPoint;

        Position centerPos = globe.getIntersectionPosition(new Line(eyeVec, forwardVec));
        if (centerPos != null)
        {
            viewportCenterPoint = globe.computePointFromPosition(centerPos);
        }
        else
        {
            viewportCenterPoint = computeHorizonPointOnGeoid(globe, transformMatrix);
        }

        return viewportCenterPoint;
    }

    private static Vec4 computeHorizonPointOnGeoid(Globe globe, Matrix transformMatrix)
    {
        if (globe == null || transformMatrix == null)
            return null;

        Vec4 globeOrigin = globe.getCenter();
        Vec4 eyeVec = Vec4.UNIT_W.transformBy4(transformMatrix.getInverse());
        Vec4 forward = Vec4.UNIT_NEGATIVE_Z.transformBy3(transformMatrix.getInverse());
        Vec4 origin_sub_eye = globeOrigin.subtract3(eyeVec);
        Position eyePos = globe.computePositionFromPoint(eyeVec);

        double r = globe.getRadiusAt(eyePos.getLatitude(), eyePos.getLongitude());
        double dSquared = origin_sub_eye.getLengthSquared3() - (r * r);
        if (dSquared < 0)
            return null;

        double d = Math.sqrt(dSquared);
        return eyeVec.add3(forward.multiply3(d));
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

        Vec4 surfaceNormal = lookAtPoint.normalize3();
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

        Vec4 surfaceNormal = surfacePoint.normalize3();
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

        Vec4 surfaceNormal = lookAtPoint.normalize3();
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
}
