/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: FlyToOrbitViewStateIterator.java 2488 2007-08-02 18:14:08Z dcollins $
 */
public class FlyToOrbitViewStateIterator extends BasicOrbitViewStateIterator
{
    protected FlyToOrbitViewStateIterator(long lengthMillis, OrbitViewAnimator animator)
    {
        super(false, new ScheduledOrbitViewInterpolator(lengthMillis), animator);
    }

    // ============== "Pan To" ======================= //
    // ============== "Pan To" ======================= //
    // ============== "Pan To" ======================= //

    private static class PanAnimator extends BasicOrbitViewAnimator
    {
        private final OrbitViewAnimator latLonAnimator;
        private final OrbitViewAnimator zoomAnimator;
        private final OrbitViewAnimator headingAnimator;
        private final OrbitViewAnimator pitchAnimator;
        private final OrbitViewAnimator beginToMidZoomAnimator, endToMidZoomAnimator;
        private final boolean useMidZoom;

        private PanAnimator(
            Globe globe,
            LatLon beginLookAtLatLon, LatLon endLookAtLatLon,
            Angle beginHeading, Angle endHeading,
            Angle beginPitch, Angle endPitch,
            double beginZoom, double endZoom)
        {
            // Latitude & Longitude.
            this.latLonAnimator = new LatLonAnimator(
                beginLookAtLatLon, endLookAtLatLon,
                OrbitViewPropertyAccessor.createLookAtLatitudeAndLongitudeAccessor());
            // Zoom.
            this.zoomAnimator = new DoubleAnimator(
                beginZoom, endZoom,
                OrbitViewPropertyAccessor.createZoomAccessor());
            // Heading.
            this.headingAnimator = new AngleAnimator(
                beginHeading, endHeading,
                OrbitViewPropertyAccessor.createHeadingAccessor());
            // Pitch.
            this.pitchAnimator = new AngleAnimator(
                beginPitch, endPitch,
                OrbitViewPropertyAccessor.createPitchAccessor());

            // Mid-zoom logic.
            double midZoom = computeMidZoom(
                globe,
                beginLookAtLatLon, endLookAtLatLon,
                beginZoom, endZoom);
            this.useMidZoom = useMidZoom(
                beginZoom, endZoom, midZoom);
            this.beginToMidZoomAnimator = new DoubleAnimator(
                beginZoom, midZoom,
                OrbitViewPropertyAccessor.createZoomAccessor());
            this.endToMidZoomAnimator = new DoubleAnimator(
                endZoom, midZoom,
                OrbitViewPropertyAccessor.createZoomAccessor());
        }

        private static double computeMidZoom(
            Globe globe,
            LatLon beginLatLon, LatLon endLatLon,
            double beginZoom, double endZoom)
        {
            // Scale factor is angular distance over 180 degrees.
            Angle sphericalDistance = LatLon.sphericalDistance(beginLatLon, endLatLon);
            double scaleFactor = angularRatio(sphericalDistance, Angle.POS180);

            // Mid-point zoom is interpolated value between minimum and maximum zoom.
            final double MIN_ZOOM = Math.min(beginZoom, endZoom);
            final double MAX_ZOOM = 3.0 * globe.getRadius();
            return mixDouble(scaleFactor, MIN_ZOOM, MAX_ZOOM);
        }

        private static boolean useMidZoom(double beginZoom, double endZoom, double midZoom)
        {
            double a = Math.abs(endZoom - beginZoom);
            double b = Math.abs(midZoom - Math.max(beginZoom, endZoom));
            return a < b;
        }

        protected void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            if (orbitView == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (stateIterator == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewStateIteratorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.nextLatLonState(interpolant, orbitView, stateIterator);
            this.nextZoomState(interpolant, orbitView, stateIterator);
            this.nextHeadingState(interpolant, orbitView, stateIterator);
            this.nextPitchState(interpolant, orbitView, stateIterator); 
        }

        private void nextLatLonState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double LATLON_START = this.useMidZoom ? 0.2 : 0.0;
            final double LATLON_STOP = this.useMidZoom ? 0.8 : 0.8;
            double latLonInterpolant = basicInterpolant(interpolant, LATLON_START, LATLON_STOP, MAX_SMOOTHING);
            this.latLonAnimator.doNextState(latLonInterpolant, orbitView, stateIterator);

        }

        private void nextHeadingState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double HEADING_START = this.useMidZoom ? 0.0 : 0.6;
            final double HEADING_STOP = 1.0;
            double headingInterpolant = basicInterpolant(interpolant, HEADING_START, HEADING_STOP, MAX_SMOOTHING);
            this.headingAnimator.doNextState(headingInterpolant, orbitView, stateIterator);
        }

        private void nextPitchState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double PITCH_START = 0.0;
            final double PITCH_STOP = 0.8;
            double pitchInterpolant = basicInterpolant(interpolant, PITCH_START, PITCH_STOP, MAX_SMOOTHING);
            this.pitchAnimator.doNextState(pitchInterpolant, orbitView, stateIterator);
        }

        private void nextZoomState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            if (this.useMidZoom)
            {
                final double ZOOM_START = 0.0;
                final double ZOOM_STOP = 1.0;
                double zoomInterpolant = this.zoomInterpolant(interpolant, ZOOM_START, ZOOM_STOP, MAX_SMOOTHING);
                if (interpolant <= 0.5)
                    this.beginToMidZoomAnimator.doNextState(zoomInterpolant, orbitView, stateIterator);
                else
                    this.endToMidZoomAnimator.doNextState(zoomInterpolant, orbitView, stateIterator);
            }
            else
            {
                final double ZOOM_START = 0.0;
                final double ZOOM_STOP = 1.0;
                double zoomInterpolant = basicInterpolant(interpolant, ZOOM_START, ZOOM_STOP, MAX_SMOOTHING);
                this.zoomAnimator.doNextState(zoomInterpolant, orbitView, stateIterator);
            }
        }

        private double zoomInterpolant(double interpolant, double startInterpolant, double stopInterpolant,
            int maxSmoothing)
        {
            // Map interpolant in to range [start, stop].
            double normalizedInterpolant = interpolantNormalized(interpolant, startInterpolant, stopInterpolant);

            // During first half of iteration, zoom increases from begin to mid,
            // and decreases from mid to end during second half.
            if (normalizedInterpolant <= 0.5)
            {
                normalizedInterpolant = 2.0 * normalizedInterpolant;
            }
            else
            {
                normalizedInterpolant = 1.0 - (2.0 * normalizedInterpolant - 1.0);
            }

            return interpolantSmoothed(normalizedInterpolant, maxSmoothing);
        }
    }

    // ============== "Zoom To" ======================= //
    // ============== "Zoom To" ======================= //
    // ============== "Zoom To" ======================= //
    
    private static class ZoomAnimator extends BasicOrbitViewAnimator
    {
        private final OrbitViewAnimator headingAnimator;
        private final OrbitViewAnimator pitchAnimator;
        private final OrbitViewAnimator zoomAnimator;

        private ZoomAnimator(
            Angle beginHeading, Angle endHeading,
            Angle beginPitch, Angle endPitch,
            double beginZoom, double endZoom)
        {
            // Heading.
            this.headingAnimator = new AngleAnimator(
                beginHeading, endHeading,
                OrbitViewPropertyAccessor.createHeadingAccessor());
            // Pitch.
            this.pitchAnimator = new AngleAnimator(
                beginPitch, endPitch,
                OrbitViewPropertyAccessor.createPitchAccessor());
            // Zoom.
            this.zoomAnimator = new DoubleAnimator(
                beginZoom, endZoom,
                OrbitViewPropertyAccessor.createZoomAccessor());
        }

        protected void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            if (orbitView == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (stateIterator == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewStateIteratorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.nextZoomState(interpolant, orbitView, stateIterator);
            this.nextHeadingState(interpolant, orbitView, stateIterator);
            this.nextPitchState(interpolant, orbitView, stateIterator);
        }

        private void nextHeadingState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double HEADING_START = 0.0;
            final double HEADING_STOP = 0.6;
            double headingInterpolant = basicInterpolant(interpolant, HEADING_START, HEADING_STOP, MAX_SMOOTHING);
            this.headingAnimator.doNextState(headingInterpolant, orbitView, stateIterator);
        }

        private void nextPitchState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double PITCH_START = 0.0;
            final double PITCH_STOP = 0.6;
            double pitchInterpolant = basicInterpolant(interpolant, PITCH_START, PITCH_STOP, MAX_SMOOTHING);
            this.pitchAnimator.doNextState(pitchInterpolant, orbitView, stateIterator);
        }

        private void nextZoomState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            final int MAX_SMOOTHING = 1;
            final double ZOOM_START = 0.0;
            final double ZOOM_STOP = 1.0;
            double zoomInterpolant = basicInterpolant(interpolant, ZOOM_START, ZOOM_STOP, MAX_SMOOTHING);
            this.zoomAnimator.doNextState(zoomInterpolant, orbitView, stateIterator);
        }
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    public static FlyToOrbitViewStateIterator createPanToIterator(
        OrbitView orbitView, Globe globe,
        LatLon lookAtLatLon,
        Angle heading,
        Angle pitch,
        double zoom)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (lookAtLatLon == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (heading == null || pitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle beginLookAtLatitude = orbitView.getLookAtLatitude();
        Angle beginLookAtLongitude = orbitView.getLookAtLongitude();
        Angle beginHeading = orbitView.getHeading();
        Angle beginPitch = orbitView.getPitch();
        double beginZoom = orbitView.getZoom();
        return createPanToIterator(
            globe,
            new LatLon(beginLookAtLatitude, beginLookAtLongitude), lookAtLatLon,
            beginHeading, heading,
            beginPitch, pitch,
            beginZoom, zoom);
    }

    public static FlyToOrbitViewStateIterator createPanToIterator(
        Globe globe,
        LatLon beginLookAtLatLon, LatLon endLookAtLatLon,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginLookAtLatLon == null || endLookAtLatLon == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: scale on mid-altitude?
        final long MIN_LENGTH_MILLIS = 4000;
        final long MAX_LENGTH_MILLIS = 16000;
        long lengthMillis = getScaledLengthMillis(
            beginLookAtLatLon, endLookAtLatLon,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);
        return createPanToIterator(
            globe,
            beginLookAtLatLon, endLookAtLatLon,
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom,
            lengthMillis);
    }

    public static FlyToOrbitViewStateIterator createPanToIterator(
        Globe globe,
        LatLon beginLookAtLatLon, LatLon endLookAtLatLon,
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom,
        long lengthMillis)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginLookAtLatLon == null || endLookAtLatLon == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (lengthMillis < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", lengthMillis);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewAnimator animator = new PanAnimator(
            globe,
            beginLookAtLatLon, endLookAtLatLon,
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom);
        return new FlyToOrbitViewStateIterator(lengthMillis, animator);
    }

    public static FlyToOrbitViewStateIterator createZoomToIterator(
        OrbitView orbitView,
        Angle heading, Angle pitch,
        double zoom)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (heading == null || pitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle beginHeading = orbitView.getHeading();
        Angle beginPitch = orbitView.getPitch();
        double beginZoom = orbitView.getZoom();
        return createZoomToIterator(
            beginHeading, heading,
            beginPitch, pitch,
            beginZoom, zoom);
    }

    public static FlyToOrbitViewStateIterator createZoomToIterator(
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom)
    {
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final long MIN_LENGTH_MILLIS = 1000;
        final long MAX_LENGTH_MILLIS = 8000;
        long lengthMillis = getScaledLengthMillis(
            beginZoom, endZoom,
            MIN_LENGTH_MILLIS, MAX_LENGTH_MILLIS);
        return createZoomToIterator(
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom,
            lengthMillis);
    }

    public static FlyToOrbitViewStateIterator createZoomToIterator(
        Angle beginHeading, Angle endHeading,
        Angle beginPitch, Angle endPitch,
        double beginZoom, double endZoom,
        long lengthMillis)
    {
        if (beginHeading == null || endHeading == null || beginPitch == null || endPitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (lengthMillis < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", lengthMillis);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewAnimator animator = new ZoomAnimator(
            beginHeading, endHeading,
            beginPitch, endPitch,
            beginZoom, endZoom);
        return new FlyToOrbitViewStateIterator(lengthMillis, animator);
    }

    private static long getScaledLengthMillis(
        double beginZoom, double endZoom,
        long minLengthMillis, long maxLengthMillis)
    {
        double scaleFactor = Math.abs(endZoom - beginZoom) / Math.max(endZoom, beginZoom);
        // Clamp scaleFactor to range [0, 1].
        scaleFactor = clampDouble(scaleFactor, 0.0, 1.0);
        // Iteration time is interpolated value between minumum and maximum lengths.
        return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
    }

    private static long getScaledLengthMillis(
            LatLon beginLatLon, LatLon endLatLon,
            long minLengthMillis, long maxLengthMillis)
    {
        Angle sphericalDistance = LatLon.sphericalDistance(beginLatLon, endLatLon);
        double scaleFactor = angularRatio(sphericalDistance, Angle.POS180);
        return (long) mixDouble(scaleFactor, minLengthMillis, maxLengthMillis);
    }

    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //
    // ============== Helper Functions ======================= //

    // Map amount range [startAmount, stopAmount] to [0, 1] when amount is inside range.
    private static double interpolantNormalized(double amount, double startAmount, double stopAmount)
    {
        if (amount < startAmount)
            return 0.0;
        else if (amount > stopAmount)
            return 1.0;
        return (amount - startAmount) / (stopAmount - startAmount);
    }

    private static double interpolantSmoothed(double interpolant, int smoothingIterations)
    {
        // Apply iterative hermite smoothing.
        double smoothed = interpolant;
        for (int i = 0; i < smoothingIterations; i++)
        {
            smoothed = smoothed * smoothed * (3.0 - 2.0 * smoothed);
        }
        return smoothed;
    }

    private static double basicInterpolant(double interpolant, double startInterpolant, double stopInterpolant,
        int maxSmoothing)
    {
        double normalizedInterpolant = interpolantNormalized(interpolant, startInterpolant, stopInterpolant);
        return interpolantSmoothed(normalizedInterpolant, maxSmoothing);
    }

    private static double angularRatio(Angle x, Angle y)
    {
        if (x == null || y == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double unclampedRatio = x.divide(y);
        return clampDouble(unclampedRatio, 0, 1);
    }

    private static double clampDouble(double value, double min, double max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    private static double mixDouble(double amount, double value1, double value2)
    {
        if (amount < 0)
            return value1;
        else if (amount > 1)
            return value2;
        return value1 * (1.0 - amount) + value2 * amount;
    }
}
