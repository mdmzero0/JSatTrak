/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.*;

/**
 * @author dcollins
 * @version $Id: OrbitViewPropertyAccessor.java 3557 2007-11-17 04:10:32Z dcollins $
 */
public class OrbitViewPropertyAccessor
{
    private OrbitViewPropertyAccessor()
    {
    }

    public static interface AngleAccessor
    {
        Angle getAngle(OrbitView orbitView);

        boolean setAngle(OrbitView orbitView, Angle value);
    }

    public static interface DoubleAccessor
    {
        Double getDouble(OrbitView orbitView);

        boolean setDouble(OrbitView orbitView, Double value);
    }

    public static interface LatLonAccessor
    {
        LatLon getLatLon(OrbitView orbitView);

        boolean setLatLon(OrbitView orbitView, LatLon value);
    }

    public static interface QuaternionAccessor
    {
        Quaternion getQuaternion(OrbitView orbitView);

        boolean setQuaternion(OrbitView orbitView, Quaternion value);
    }

    public static AngleAccessor createLatitudeAccessor()
    {
        return new LatitudeAccessor();
    }

    public static AngleAccessor createLongitudeAccessor()
    {
        return new LongitudeAccessor();
    }

    public static AngleAccessor createLookAtLatitudeAccessor()
    {
        return new LookAtLatitudeAccessor();
    }

    public static AngleAccessor createLookAtLongitudeAccessor()
    {
        return new LookAtLongitudeAccessor();
    }

    public static AngleAccessor createHeadingAccessor()
    {
        return new HeadingAccessor();
    }

    public static AngleAccessor createPitchAccessor()
    {
        return new PitchAccessor();
    }

    public static DoubleAccessor createAltitudeAccessor()
    {
        return new AltitudeAccessor();
    }

    public static DoubleAccessor createZoomAccessor()
    {
        return new ZoomAccessor();
    }

    public static LatLonAccessor createLatitudeAndLongitudeAccessor()
    {
        return new LatitudeAndLongitudeAccessor();
    }

    public static LatLonAccessor createLookAtLatitudeAndLongitudeAccessor()
    {
        return new LookAtLatitudeAndLongitudeAccessor();
    }

    public static RotationAccessor createRotationAccessor()
    {
        return new RotationAccessor();
    }

    // ============== Implementation ======================= //
    // ============== Implementation ======================= //
    // ============== Implementation ======================= //

    private static class LatitudeAccessor implements AngleAccessor
    {
        public final Angle getAngle(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getLatitude();
        }

        public final boolean setAngle(OrbitView orbitView, Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setLatitude(value);
        }
    }

    private static class LongitudeAccessor implements AngleAccessor
    {
        public final Angle getAngle(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getLongitude();
        }

        public final boolean setAngle(OrbitView orbitView, Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setLongitude(value);
        }
    }

    private static class HeadingAccessor implements AngleAccessor
    {
        public final Angle getAngle(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getHeading();
        }

        public final boolean setAngle(OrbitView orbitView, Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setHeading(value);
        }
    }

    private static class PitchAccessor implements AngleAccessor
    {
        public final Angle getAngle(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getPitch();
        }

        public final boolean setAngle(OrbitView orbitView, Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setPitch(value);
        }
    }

    private static class LookAtLatitudeAccessor implements AngleAccessor
    {
        public final Angle getAngle(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getLookAtLatitude();
        }

        public final boolean setAngle(OrbitView orbitView, Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setLookAtLatitude(value);
        }
    }

    private static class LookAtLongitudeAccessor implements AngleAccessor
    {
        public final Angle getAngle(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getLookAtLongitude();
        }

        public final boolean setAngle(OrbitView orbitView, Angle value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setLookAtLongitude(value);
        }
    }

    private static class AltitudeAccessor implements DoubleAccessor
    {
        public final Double getDouble(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getAltitude();
        }

        public final boolean setDouble(OrbitView orbitView, Double value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setAltitude(value);
        }
    }

    private static class ZoomAccessor implements DoubleAccessor
    {
        public final Double getDouble(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getZoom();
        }

        public final boolean setDouble(OrbitView orbitView, Double value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setZoom(value);
        }
    }

    private static class LatitudeAndLongitudeAccessor implements LatLonAccessor
    {
        public final LatLon getLatLon(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return new LatLon(orbitView.getLatitude(), orbitView.getLongitude());
        }

        public final boolean setLatLon(OrbitView orbitView, LatLon value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setLatLon(value);
        }
    }

    private static class LookAtLatitudeAndLongitudeAccessor implements LatLonAccessor
    {
        public final LatLon getLatLon(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return new LatLon(orbitView.getLookAtLatitude(), orbitView.getLookAtLongitude());
        }

        public final boolean setLatLon(OrbitView orbitView, LatLon value)
        {
            //noinspection SimplifiableIfStatement
            if (orbitView == null || value == null)
                return false;

            return orbitView.setLookAtLatLon(value);
        }
    }

    private static class RotationAccessor implements QuaternionAccessor
    {
        public final Quaternion getQuaternion(OrbitView orbitView)
        {
            if (orbitView == null)
                return null;

            return orbitView.getRotation();
        }

        public final boolean setQuaternion(OrbitView orbitView, Quaternion value)
        {
            if (orbitView == null || value == null)
                return false;

            orbitView.setRotation(value);
            return true;
        }
    }
}
