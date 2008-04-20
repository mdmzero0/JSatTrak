/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.ViewStateIterator;

/**
 * @author dcollins
 * @version $Id: BasicOrbitViewStateIterator.java 3459 2007-11-08 18:48:01Z dcollins $
 */
public class BasicOrbitViewStateIterator implements ViewStateIterator
{
    private final boolean doCoalesce;
    private final OrbitViewInterpolator interpolator;
    private final OrbitViewAnimator animator;
    private boolean hasNext = true;

    public BasicOrbitViewStateIterator(boolean doCoalesce, OrbitViewInterpolator interpolator, OrbitViewAnimator animator)
    {
        if (interpolator == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewStateIterator.InterpolatorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (animator == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewStateIterator.AnimatorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doCoalesce = doCoalesce;
        this.interpolator = interpolator;
        this.animator = animator;
    }

    public final boolean isCoalesce()
    {
        return this.doCoalesce;
    }

    public final void nextState(View view)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!(view instanceof OrbitView))
        {
            String message = Logging.getMessage("view.OrbitView.ViewNotAnOrbitView");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Get next interpolant, clamped it to range [0, 1]
        // to ensure we don't set values outside the desired range.
        double unclampedInterpolant = this.interpolator.nextInterpolant((OrbitView) view);
        double interpolant = clampDouble(unclampedInterpolant, 0, 1);

        // Flag iterator to stop when interpolant>=1,
        // or when last change did not succeed.
        if (interpolant >= 1)
            this.stop();

        this.doNextState(interpolant, (OrbitView) view);
        view.firePropertyChange(AVKey.VIEW, null, view);
    }

    private static double clampDouble(double value, double min, double max)
    {
        return value < min ? min : (value > max ? max : value);
    }

    protected void doNextState(double interpolant, OrbitView orbitView)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.animator.doNextState(interpolant, orbitView, this);
    }

    public final boolean hasNextState(View view)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!(view instanceof OrbitView))
        {
            String message = Logging.getMessage("view.OrbitView.ViewNotAnOrbitView");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.hasNext;
    }

    public final void stop()
    {
        this.hasNext = false;
    }

    public final ViewStateIterator coalesceWith(View view, ViewStateIterator stateIterator)
    {
        if (view == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!(view instanceof OrbitView))
        {
            String message = Logging.getMessage("view.OrbitView.ViewNotAnOrbitView");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (stateIterator == null || !(stateIterator instanceof BasicOrbitViewStateIterator))
            return this;

        if (!this.doCoalesce)
            return this;

        boolean doCoalesce = this.doCoalesce;
        OrbitViewInterpolator interpolator = this.interpolator.coalesceWith(
            (OrbitView) view, ((BasicOrbitViewStateIterator) stateIterator).interpolator);
        OrbitViewAnimator animator = this.animator.coalesceWith(
            (OrbitView) view, ((BasicOrbitViewStateIterator) stateIterator).animator);
        return this.doCoalesce((OrbitView) view, doCoalesce, interpolator, animator);
    }

    protected ViewStateIterator doCoalesce(OrbitView orbitView, boolean doCoalesce, OrbitViewInterpolator interpolator,
        OrbitViewAnimator animator)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (interpolator == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewStateIterator.InterpolatorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (animator == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewStateIterator.AnimatorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: should use reflection
        return new BasicOrbitViewStateIterator(doCoalesce, interpolator, animator);
    }
}
