/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

/**
 * @author dcollins
 * @version $Id: OrbitViewAnimator.java 3462 2007-11-08 19:43:38Z dcollins $
 */
public interface OrbitViewAnimator 
{
    void doNextState(double interpolant, OrbitView orbitView, BasicOrbitViewStateIterator stateIterator);

    OrbitViewAnimator coalesceWith(OrbitView orbitView, OrbitViewAnimator animator);

    void setStopOnInvalidState(boolean stop);

    boolean isStopOnInvalidState();
}
