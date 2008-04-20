/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.awt;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.*;
import gov.nasa.worldwind.ViewStateIterator;

/**
 * @author dcollins
 * @version $Id: OrbitViewInputStateIterator.java 3462 2007-11-08 19:43:38Z dcollins $
 */
class OrbitViewInputStateIterator extends BasicOrbitViewStateIterator
{
    protected OrbitViewInputStateIterator(boolean doCoalesce, double smoothing, MovingAnimator animator, double minEpsilon)
    {
        this(doCoalesce, new ConstantInterpolator(1 - smoothing), new SelfStoppingAnimator(animator, minEpsilon));
    }

    protected OrbitViewInputStateIterator(boolean doCoalesce, ConstantInterpolator interpolator,
        SelfStoppingAnimator animator)
    {
        super(doCoalesce, interpolator, animator);
    }

    protected ViewStateIterator doCoalesce(OrbitView orbitView, boolean doCoalesce, OrbitViewInterpolator interpolator,
        OrbitViewAnimator animator)
    {
        if (interpolator == null || !(interpolator instanceof ConstantInterpolator))
            return this;

        if (animator == null || !(animator instanceof SelfStoppingAnimator))
            return this;

        return new OrbitViewInputStateIterator(
            doCoalesce,
            (ConstantInterpolator) interpolator,
            (SelfStoppingAnimator) animator);
    }

    // ============== Interpolators, Animators ======================= //
    // ============== Interpolators, Animators ======================= //
    // ============== Interpolators, Animators ======================= //

    protected static class ConstantInterpolator implements OrbitViewInterpolator
    {
        private final double interpolant;

        public ConstantInterpolator(double interpolant)
        {
            if (interpolant < 0 || interpolant > 1)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", interpolant);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.interpolant = interpolant;
        }

        public double nextInterpolant(OrbitView orbitView)
        {
            if (orbitView == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return this.interpolant;
        }

        public OrbitViewInterpolator coalesceWith(OrbitView orbitView, OrbitViewInterpolator interpolator)
        {
            return this;
        }
    }

    protected static class SelfStoppingAnimator implements OrbitViewAnimator
    {
        private final MovingAnimator movingAnimator;
        private final double minEpsilon;

        public SelfStoppingAnimator(MovingAnimator animator, double minEpsilon)
        {
            if (animator == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewStateIterator.AnimatorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (minEpsilon < 0 || minEpsilon > 1)
            {
                String message = Logging.getMessage("generic.ArgumentOutOfRange", minEpsilon);
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.movingAnimator = animator;
            this.minEpsilon = minEpsilon;
        }

        public void doNextState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
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

            boolean doStop = this.shouldStop(orbitView);
            if (doStop)
            {
                stateIterator.stop();
            }

            this.movingAnimator.doNextState(doStop ? 1 : interpolant, orbitView, stateIterator);
        }

        public OrbitViewAnimator coalesceWith(OrbitView orbitView, OrbitViewAnimator animator)
        {
            if (orbitView == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (animator == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewStateIterator.AnimatorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (!(animator instanceof SelfStoppingAnimator))
                return this;

            MovingAnimator newAnimator = (MovingAnimator) this.movingAnimator.coalesceWith(
                orbitView,
                ((SelfStoppingAnimator) animator).movingAnimator);
            return new SelfStoppingAnimator(newAnimator, this.minEpsilon);
        }

        private boolean shouldStop(OrbitView orbitView)
        {
            if (orbitView == null)
                return true;

            double difference = this.movingAnimator.computeRemainingDifference(orbitView);
            return difference < this.minEpsilon;
        }

        public void setStopOnInvalidState(boolean stop)
        {
            this.movingAnimator.setStopOnInvalidState(stop);
        }

        public boolean isStopOnInvalidState()
        {
            return this.movingAnimator.isStopOnInvalidState();
        }
    }

    protected static interface MovingAnimator extends OrbitViewAnimator
    {
        double computeRemainingDifference(OrbitView orbitView);
    }

    protected static class AngleMovingAnimator extends BasicOrbitViewAnimator.AngleAnimator implements MovingAnimator
    {
        public AngleMovingAnimator(Angle value, OrbitViewPropertyAccessor.AngleAccessor propertyAccessor)
        {
            super(value, value, propertyAccessor);
        }

        public Angle nextAngle(double interpolant, OrbitView orbitView)
        {
            Angle viewAngle = this.getPropertyAccessor().getAngle(orbitView);
            if (viewAngle == null)
                return null;

            return Angle.mix(
                interpolant,
                viewAngle,
                this.getEnd());
        }

        protected OrbitViewAnimator coalesceWithImpl(OrbitView orbitView, OrbitViewAnimator animator)
        {
            if (!(animator instanceof AngleMovingAnimator))
                return this;

            Angle viewValue = this.getPropertyAccessor().getAngle(orbitView);
            if (viewValue == null)
                return this;

            Angle prevValue = ((AngleMovingAnimator) animator).getEnd();
            Angle newValue = this.getEnd().add(prevValue).subtract(viewValue);
            return new AngleMovingAnimator(
                newValue,
                this.getPropertyAccessor());
        }

        public double computeRemainingDifference(OrbitView orbitView)
        {
            if (orbitView == null)
                return 0;

            Angle viewValue = this.getPropertyAccessor().getAngle(orbitView);
            if (viewValue == null)
                return 0;

            return Math.abs(this.getEnd().subtract(viewValue).degrees);
        }
    }

    protected static class DoubleMovingAnimator extends BasicOrbitViewAnimator.DoubleAnimator implements MovingAnimator
    {
        public DoubleMovingAnimator(Double value, OrbitViewPropertyAccessor.DoubleAccessor propertyAccessor)
        {
            super(value, value, propertyAccessor);
        }

        public Double nextDouble(double interpolant, OrbitView orbitView)
        {
            Double viewValue = this.getPropertyAccessor().getDouble(orbitView);
            if (viewValue == null)
                return null;

            return mix(
                interpolant,
                viewValue,
                this.getEnd());
        }

        protected OrbitViewAnimator coalesceWithImpl(OrbitView orbitView, OrbitViewAnimator animator)
        {
            if (!(animator instanceof DoubleMovingAnimator))
                return this;

            Double viewValue = this.getPropertyAccessor().getDouble(orbitView);
            if (viewValue == null)
                return this;

            double prevValue = ((DoubleMovingAnimator) animator).getEnd();
            double newValue = this.getEnd() + prevValue - viewValue;
            return new DoubleMovingAnimator(
                newValue,
                this.getPropertyAccessor());
        }

        public double computeRemainingDifference(OrbitView orbitView)
        {
            if (orbitView == null)
                return 0;

            Double viewValue = this.getPropertyAccessor().getDouble(orbitView);
            if (viewValue == null)
                return 0;

            return Math.abs(this.getEnd() - viewValue);
        }
    }

    protected static class LogDoubleMovingAnimator extends DoubleMovingAnimator
    {
        public LogDoubleMovingAnimator(Double value, OrbitViewPropertyAccessor.DoubleAccessor propertyAccessor)
        {
            super(value, propertyAccessor);
        }

        protected OrbitViewAnimator coalesceWithImpl(OrbitView orbitView, OrbitViewAnimator animator)
        {
            if (!(animator instanceof DoubleMovingAnimator))
                return this;

            Double viewValue = this.getPropertyAccessor().getDouble(orbitView);
            if (viewValue == null)
                return this;

            double prevValue = ((DoubleMovingAnimator) animator).getEnd();
            double newValue = computeNewLogValue(this.getEnd(), prevValue, viewValue);
            return new DoubleMovingAnimator(
                newValue,
                this.getPropertyAccessor());
        }

        private double computeNewLogValue(double thisValue, double thatValue, double viewValue)
        {
            double logThisValue = Math.log(thisValue);
            double logThatValue = Math.log(thatValue);
            double logViewValue = Math.log(viewValue);
            return Math.exp(logThisValue + logThatValue - logViewValue);
        }
    }

    protected static class LatLonMovingAnimator extends BasicOrbitViewAnimator.LatLonAnimator implements MovingAnimator
    {
        public LatLonMovingAnimator(LatLon value, OrbitViewPropertyAccessor.LatLonAccessor propertyAccessor)
        {
            super(value, value, propertyAccessor);
        }

        public LatLon nextLatLon(double interpolant, OrbitView orbitView)
        {
            LatLon viewValue = this.getPropertyAccessor().getLatLon(orbitView);
            if (viewValue == null)
                return null;

            Angle newLatitude = Angle.mix(interpolant, viewValue.getLatitude(), this.getEnd().getLatitude());
            Angle newLongitude = Angle.mix(interpolant, viewValue.getLongitude(), this.getEnd().getLongitude());
            return new LatLon(newLatitude, newLongitude);
        }

        protected OrbitViewAnimator coalesceWithImpl(OrbitView orbitView, OrbitViewAnimator animator)
        {
            if (!(animator instanceof LatLonMovingAnimator))
                return this;

            LatLon viewValue = this.getPropertyAccessor().getLatLon(orbitView);
            if (viewValue == null)
                return this;

            LatLon prevValue = ((LatLonMovingAnimator) animator).getEnd();
            LatLon newValue = new LatLon(
                this.getEnd().getLatitude().add(prevValue.getLatitude()).subtract(viewValue.getLatitude()),
                this.getEnd().getLongitude().add(prevValue.getLongitude()).subtract(viewValue.getLongitude()));
            return new LatLonMovingAnimator(
                newValue,
                this.getPropertyAccessor());
        }

        public double computeRemainingDifference(OrbitView orbitView)
        {
            if (orbitView == null)
                return 0;

            LatLon viewValue = this.getPropertyAccessor().getLatLon(orbitView);
            if (viewValue == null)
                return 0;

            Angle sphericalDistance = LatLon.sphericalDistance(this.getEnd(), viewValue);
            return sphericalDistance.degrees;
        }
    }

    protected static class QuaternionMovingAnimator extends BasicOrbitViewAnimator.QuaternionAnimator implements MovingAnimator
    {
        public QuaternionMovingAnimator(Quaternion value, OrbitViewPropertyAccessor.QuaternionAccessor propertyAccessor)
        {
            super(value, value, propertyAccessor);
        }

        public Quaternion nextQuaternion(double interpolant, OrbitView orbitView)
        {
            Quaternion viewValue = this.getPropertyAccessor().getQuaternion(orbitView);
            if (viewValue == null)
                return null;

            return Quaternion.slerp(
                    interpolant,
                    viewValue,
                    this.getEnd());
        }

        protected OrbitViewAnimator coalesceWithImpl(OrbitView orbitView, OrbitViewAnimator animator)
        {
            if (!(animator instanceof QuaternionMovingAnimator))
                return this;

            Quaternion viewValue = this.getPropertyAccessor().getQuaternion(orbitView);
            if (viewValue == null)
                return this;

            Quaternion prevValue = ((QuaternionMovingAnimator) animator).getEnd();
            Quaternion change = viewValue.getInverse().multiply(this.getEnd());
            Quaternion newValue = prevValue.multiply(change);
            return new QuaternionMovingAnimator(
                newValue,
                this.getPropertyAccessor());
        }

        public double computeRemainingDifference(OrbitView orbitView)
        {
            if (orbitView == null)
                return 0;

            Quaternion viewValue = this.getPropertyAccessor().getQuaternion(orbitView);
            if (viewValue == null)
                return 0;

            return this.getEnd().subtract(viewValue).getLength();
        }
    }

    protected static class CompoundMovingAnimator extends BasicOrbitViewAnimator.CompoundAnimator implements MovingAnimator
    {
        public CompoundMovingAnimator(OrbitViewAnimator... animators)
        {
            super(animators);
        }

        public double computeRemainingDifference(OrbitView orbitView)
        {
            if (orbitView == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            double maxDifference = -Double.MIN_VALUE;
            for (OrbitViewAnimator a : this.getAnimators())
            {
                if (a != null)
                {
                    double difference = ((MovingAnimator) a).computeRemainingDifference(orbitView);
                    if (difference > maxDifference)
                        maxDifference = difference;
                }
            }
            return maxDifference;
        }
    }

    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //
    // ============== Factory Functions ======================= //

    public static OrbitViewInputStateIterator createLatLonIterator(
        LatLon value)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final double DEFAULT_SMOOTHING = 0.4;
        boolean doCoalesce = true;
        return createLatLonIterator(
            value,
            DEFAULT_SMOOTHING,
            doCoalesce);
    }

    public static OrbitViewInputStateIterator createLatLonIterator(
        LatLon value,
        double smoothing,
        boolean doCoalesce)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if ((smoothing <= 0.0) || (smoothing > 1.0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", smoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewPropertyAccessor.LatLonAccessor propertyAccessor
            = OrbitViewPropertyAccessor.createLatitudeAndLongitudeAccessor();
        MovingAnimator animator = new LatLonMovingAnimator(value, propertyAccessor);
        animator.setStopOnInvalidState(true);
        final double DEFAULT_MIN_EPSILON = 0.000000001;
        return new OrbitViewInputStateIterator(
            doCoalesce,
            smoothing,
            animator,
            DEFAULT_MIN_EPSILON);
    }

    public static OrbitViewInputStateIterator createHeadingIterator(
        Angle value)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final double DEFAULT_SMOOTHING = 0.7;
        boolean doCoalesce = true;
        return createHeadingIterator(
            value,
            DEFAULT_SMOOTHING,
            doCoalesce);
    }

    public static OrbitViewInputStateIterator createHeadingIterator(
        Angle value,
        double smoothing,
        boolean doCoalesce)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if ((smoothing <= 0.0) || (smoothing > 1.0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", smoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewPropertyAccessor.AngleAccessor propertyAccessor = OrbitViewPropertyAccessor.createHeadingAccessor();
        MovingAnimator animator = new AngleMovingAnimator(value, propertyAccessor);
        animator.setStopOnInvalidState(true);
        final double DEFAULT_MIN_EPSILON = 0.0001;
        return new OrbitViewInputStateIterator(
            doCoalesce,
            smoothing,
            animator,
            DEFAULT_MIN_EPSILON);
    }

    public static OrbitViewInputStateIterator createPitchIterator(
        Angle value)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final double DEFAULT_SMOOTHING = 0.7;
        boolean doCoalesce = true;
        return createHeadingIterator(
            value,
            DEFAULT_SMOOTHING,
            doCoalesce);
    }

    public static OrbitViewInputStateIterator createPitchIterator(
        Angle value,
        double smoothing,
        boolean doCoalesce)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if ((smoothing <= 0.0) || (smoothing > 1.0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", smoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewPropertyAccessor.AngleAccessor propertyAccessor = OrbitViewPropertyAccessor.createPitchAccessor();
        MovingAnimator animator = new AngleMovingAnimator(value, propertyAccessor);
        animator.setStopOnInvalidState(true);
        final double DEFAULT_MIN_EPSILON = 0.0001;
        return new OrbitViewInputStateIterator(
            doCoalesce,
            smoothing,
            animator,
            DEFAULT_MIN_EPSILON);
    }

    public static OrbitViewInputStateIterator createHeadingAndPitchIterator(
        Angle heading, Angle pitch)
    {
        if (heading == null || pitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final double DEFAULT_SMOOTHING = 0.7;
        boolean doCoalesce = true;
        return createHeadingAndPitchIterator(
            heading, pitch,
            DEFAULT_SMOOTHING,
            doCoalesce);
    }

    public static OrbitViewInputStateIterator createHeadingAndPitchIterator(
        Angle heading, Angle pitch,
        double smoothing,
        boolean doCoalesce)
    {
        if (heading == null || pitch == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if ((smoothing <= 0.0) || (smoothing > 1.0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", smoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewPropertyAccessor.AngleAccessor headingPropertyAccessor = OrbitViewPropertyAccessor.createHeadingAccessor();
        OrbitViewPropertyAccessor.AngleAccessor pitchPropertyAccessor = OrbitViewPropertyAccessor.createPitchAccessor();
        MovingAnimator headingAnimator = new AngleMovingAnimator(heading, headingPropertyAccessor);
        MovingAnimator pitchAnimator = new AngleMovingAnimator(pitch, pitchPropertyAccessor);
        MovingAnimator compoundAnimator = new CompoundMovingAnimator(headingAnimator, pitchAnimator);
        compoundAnimator.setStopOnInvalidState(true);
        final double DEFAULT_MIN_EPSILON = 0.0001;
        return new OrbitViewInputStateIterator(
            doCoalesce,
            smoothing,
            compoundAnimator,
            DEFAULT_MIN_EPSILON);
    }

    public static OrbitViewInputStateIterator createZoomIterator(
        double value)
    {
        final double DEFAULT_SMOOTHING = 0.9;
        boolean doCoalesce = true;
        return createZoomIterator(
            value,
            DEFAULT_SMOOTHING,
            doCoalesce);
    }

    public static OrbitViewInputStateIterator createZoomIterator(
        double value,
        double smoothing,
        boolean doCoalesce)
    {
        if ((smoothing <= 0.0) || (smoothing > 1.0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", smoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewPropertyAccessor.DoubleAccessor propertyAccessor = OrbitViewPropertyAccessor.createZoomAccessor();
        // Zoom changes are treated as logarithmic values. This accomplishes two things:
        // (1) Zooming is slow near the globe, and fast at great distances.
        // (2) Zooming in then immediately zooming out returns the viewer to the same distance.
        MovingAnimator animator = new LogDoubleMovingAnimator(value, propertyAccessor);
        animator.setStopOnInvalidState(true);
        final double DEFAULT_MIN_EPSILON = 0.001;
        return new OrbitViewInputStateIterator(
            doCoalesce,
            smoothing,
            animator,
            DEFAULT_MIN_EPSILON);
    }

    public static OrbitViewInputStateIterator createLookAtLatLonIterator(
        LatLon value)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        final double DEFAULT_SMOOTHING = 0.4;
        boolean doCoalesce = true;
        return createLookAtLatLonIterator(
            value,
            DEFAULT_SMOOTHING,
            doCoalesce);
    }

    public static OrbitViewInputStateIterator createLookAtLatLonIterator(
        LatLon value,
        double smoothing,
        boolean doCoalesce)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if ((smoothing <= 0.0) || (smoothing > 1.0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", smoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewPropertyAccessor.LatLonAccessor propertyAccessor
            = OrbitViewPropertyAccessor.createLookAtLatitudeAndLongitudeAccessor();
        MovingAnimator animator = new LatLonMovingAnimator(value, propertyAccessor);
        animator.setStopOnInvalidState(true);
        final double DEFAULT_MIN_EPSILON = 0.000000001;
        return new OrbitViewInputStateIterator(
            doCoalesce,
            smoothing,
            animator,
            DEFAULT_MIN_EPSILON);
    }

    public static OrbitViewInputStateIterator createRotationIterator(
        Quaternion value)
    {
        final double DEFAULT_SMOOTHING = 0.4;
        boolean doCoalesce = true;
        return createRotationIterator(
            value,
            DEFAULT_SMOOTHING,
            doCoalesce);
    }

    public static OrbitViewInputStateIterator createRotationIterator(
        Quaternion value,
        double smoothing,
        boolean doCoalesce)
    {
        if ((smoothing <= 0.0) || (smoothing > 1.0))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", smoothing);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewPropertyAccessor.QuaternionAccessor propertyAccessor = OrbitViewPropertyAccessor.createRotationAccessor();
        MovingAnimator animator = new QuaternionMovingAnimator(value, propertyAccessor);
        animator.setStopOnInvalidState(true);        
        final double DEFAULT_MIN_EPSILON = 0.000000001;
        return new OrbitViewInputStateIterator(
            doCoalesce,
            smoothing,
            animator,
            DEFAULT_MIN_EPSILON);
    }
}
