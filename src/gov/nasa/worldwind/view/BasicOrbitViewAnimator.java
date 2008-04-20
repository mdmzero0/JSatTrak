/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * @author dcollins
 * @version $Id: BasicOrbitViewAnimator.java 3462 2007-11-08 19:43:38Z dcollins $
 */
public class BasicOrbitViewAnimator implements OrbitViewAnimator
{
    private boolean stopOnInvalidState = false;
    private boolean lastStateValid = true;

    protected BasicOrbitViewAnimator()
    {
    }

    public final void doNextState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
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

        this.doNextStateImpl(interpolant, orbitView, stateIterator);
        if (isStopOnInvalidState() && !isLastStateValid())
        {
            stateIterator.stop();
        }
    }

    protected void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
    {
    }

    public final OrbitViewAnimator coalesceWith(OrbitView orbitView, OrbitViewAnimator animator)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (animator == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewAnimatorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        OrbitViewAnimator newAnimator = this.coalesceWithImpl(orbitView, animator);
        if (this.isStopOnInvalidState() || animator.isStopOnInvalidState())
        {
            newAnimator.setStopOnInvalidState(true);
        }
        return newAnimator;
    }

    protected OrbitViewAnimator coalesceWithImpl(OrbitView orbitView, OrbitViewAnimator animator)
    {
        return this;
    }

    public void setStopOnInvalidState(boolean stop)
    {
        this.stopOnInvalidState = stop;
    }

    public boolean isStopOnInvalidState()
    {
        return this.stopOnInvalidState;
    }

    protected void flagLastStateInvalid()
    {
        this.lastStateValid = false;
    }

    protected boolean isLastStateValid()
    {
        return this.lastStateValid;
    }

    // ============== Implementations ======================= //
    // ============== Implementations ======================= //
    // ============== Implementations ======================= //

    public static class AngleAnimator extends BasicOrbitViewAnimator
    {
        private final Angle begin;
        private final Angle end;
        private final OrbitViewPropertyAccessor.AngleAccessor propertyAccessor;

        public AngleAnimator(
            Angle begin, Angle end,
            OrbitViewPropertyAccessor.AngleAccessor propertyAccessor)
        {
            if (begin == null || end == null)
            {
                String message = Logging.getMessage("nullValue.AngleIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (propertyAccessor == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewPropertyAccessorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.begin = begin;
            this.end = end;
            this.propertyAccessor = propertyAccessor;
        }

        public final Angle getBegin()
        {
            return this.begin;
        }

        public final Angle getEnd()
        {
            return this.end;
        }

        public final OrbitViewPropertyAccessor.AngleAccessor getPropertyAccessor()
        {
            return this.propertyAccessor;
        }

        protected final void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            Angle newValue = this.nextAngle(interpolant, orbitView);
            if (newValue == null)
                return;

            boolean success = this.propertyAccessor.setAngle(orbitView, newValue);
            if (!success)
            {
                flagLastStateInvalid();
            }
        }

        public Angle nextAngle(double interpolant, OrbitView orbitView)
        {
            return Angle.mix(
                interpolant,
                this.begin,
                this.end);
        }
    }

    public static class DoubleAnimator extends BasicOrbitViewAnimator
    {
        private final double begin;
        private final double end;
        private final OrbitViewPropertyAccessor.DoubleAccessor propertyAccessor;

        public DoubleAnimator(
            double begin, double end,
            OrbitViewPropertyAccessor.DoubleAccessor propertyAccessor)
        {
            if (propertyAccessor == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewPropertyAccessorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.begin = begin;
            this.end = end;
            this.propertyAccessor = propertyAccessor;
        }

        public final Double getBegin()
        {
            return this.begin;
        }

        public final Double getEnd()
        {
            return this.end;
        }

        public final OrbitViewPropertyAccessor.DoubleAccessor getPropertyAccessor()
        {
            return this.propertyAccessor;
        }

        protected final void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            Double newValue = this.nextDouble(interpolant, orbitView);
            if (newValue == null)
                return;

            boolean success = this.propertyAccessor.setDouble(orbitView, newValue);
            if (!success)
            {
                flagLastStateInvalid();
            }
        }

        public Double nextDouble(double interpolant, OrbitView orbitView)
        {
            return mix(
                interpolant,
                this.begin,
                this.end);
        }

        public static double mix(double amount, double value1, double value2)
        {
            if (amount < 0)
                return value1;
            else if (amount > 1)
                return value2;
            return value1 * (1.0 - amount) + value2 * amount;
        }
    }

    public static class LatLonAnimator extends BasicOrbitViewAnimator
    {
        private final LatLon begin;
        private final LatLon end;
        private final OrbitViewPropertyAccessor.LatLonAccessor propertyAccessor;

        public LatLonAnimator(
            LatLon begin,
            LatLon end,
            OrbitViewPropertyAccessor.LatLonAccessor propertyAccessor)
        {
            if (begin == null || end == null)
            {
                String message = Logging.getMessage("nullValue.LatLonIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (propertyAccessor == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewPropertyAccessorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.begin = begin;
            this.end = end;
            this.propertyAccessor = propertyAccessor;
        }

        public final LatLon getBegin()
        {
            return this.begin;
        }

        public final LatLon getEnd()
        {
            return this.end;
        }

        public final OrbitViewPropertyAccessor.LatLonAccessor getPropertyAccessor()
        {
            return this.propertyAccessor;
        }

        protected final void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            LatLon newValue = this.nextLatLon(interpolant, orbitView);
            if (newValue == null)
                return;

            boolean success = this.propertyAccessor.setLatLon(orbitView, newValue);
            if (!success)
            {
                flagLastStateInvalid();
            }
        }

        public LatLon nextLatLon(double interpolant, OrbitView orbitView)
        {
            return LatLon.interpolate(
                interpolant,
                this.begin,
                this.end);
        }
    }

    public static class QuaternionAnimator extends BasicOrbitViewAnimator
    {
        private final Quaternion begin;
        private final Quaternion end;
        private final OrbitViewPropertyAccessor.QuaternionAccessor propertyAccessor;

        public QuaternionAnimator(
            Quaternion begin, Quaternion end,
            OrbitViewPropertyAccessor.QuaternionAccessor propertyAccessor)
        {
            if (begin == null || end == null)
            {
                String message = Logging.getMessage("nullValue.QuaternionIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (propertyAccessor == null)
            {
                String message = Logging.getMessage("nullValue.OrbitViewPropertyAccessorIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.begin = begin;
            this.end = end;
            this.propertyAccessor = propertyAccessor;
        }

        public final Quaternion getBegin()
        {
            return this.begin;
        }

        public final Quaternion getEnd()
        {
            return this.end;
        }

        public final OrbitViewPropertyAccessor.QuaternionAccessor getPropertyAccessor()
        {
            return this.propertyAccessor;
        }

        protected final void doNextStateImpl(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator)
        {
            Quaternion newValue = this.nextQuaternion(interpolant, orbitView);
            if (newValue == null)
                return;

            boolean success = this.propertyAccessor.setQuaternion(orbitView, newValue);
            if (!success)
            {
                flagLastStateInvalid();
            }
        }

        public Quaternion nextQuaternion(double interpolant, OrbitView orbitView)
        {
            return Quaternion.mix(
                interpolant,
                this.begin,
                this.end);
        }
    }

    public static class CompoundAnimator extends BasicOrbitViewAnimator
    {
        private OrbitViewAnimator[] animators;

        public CompoundAnimator(OrbitViewAnimator... animators)
        {
            if (animators == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            int numAnimators = animators.length;
            this.animators = new OrbitViewAnimator[numAnimators];
            System.arraycopy(animators, 0, this.animators, 0, numAnimators);
        }

        private CompoundAnimator newInstance(OrbitViewAnimator... animators)
        {
            if (animators == null)
            {
                String message = Logging.getMessage("nullValue.ArrayIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            CompoundAnimator newCompoundAnimator;
            try
            {
                Class<? extends CompoundAnimator> cls = this.getClass();
                Class<? extends OrbitViewAnimator[]> paramCls = animators.getClass();
                Constructor<? extends CompoundAnimator> constructor = cls.getConstructor(paramCls);
                newCompoundAnimator = constructor.newInstance((Object) animators);
            }
            catch (Exception e)
            {
                String message = Logging.getMessage(""); // TODO
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            return newCompoundAnimator;
        }

        public final Iterable<OrbitViewAnimator> getAnimators()
        {
            return Arrays.asList(this.animators);
        }

        protected final void doNextStateImpl(double interpolant, OrbitView orbitView,
                                             BasicOrbitViewStateIterator stateIterator)
        {
            for (OrbitViewAnimator a : animators)
            {
                if (a != null)
                {
                    a.doNextState(interpolant, orbitView, stateIterator);
                }
            }
        }

        protected final OrbitViewAnimator coalesceWithImpl(OrbitView orbitView, OrbitViewAnimator animator)
        {
            if (!(animator instanceof CompoundAnimator))
                return this;

            // Cannot coalesce with different number of internal animators.
            CompoundAnimator that = (CompoundAnimator) animator;
            if (this.animators.length != that.animators.length)
                return this;

            int numAnimators = this.animators.length;
            OrbitViewAnimator[] newAnimators = new OrbitViewAnimator[numAnimators];
            for (int i = 0; i < numAnimators; i++)
            {
                if (this.animators[i] != null && that.animators[i] != null)
                {
                    newAnimators[i] = this.animators[i].coalesceWith(orbitView, that.animators[i]);
                }
                else if (this.animators[i] != null)
                {
                    newAnimators[i] = this.animators[i];
                }
            }

            CompoundAnimator newCompoundAnimator = this.newInstance(newAnimators);
            if (newCompoundAnimator == null)
                return this;

            return newCompoundAnimator;
        }

        public void setStopOnInvalidState(boolean stop)
        {
            super.setStopOnInvalidState(stop);
            for (OrbitViewAnimator a : animators)
            {
                if (a != null)
                {
                    a.setStopOnInvalidState(stop);
                }
            }
        }
    }
}
