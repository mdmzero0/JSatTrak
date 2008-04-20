/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

/**
 * @author dcollins
 * @version $Id: OrbitViewInterpolator.java 2366 2007-07-25 18:20:44Z dcollins $
 */
public interface OrbitViewInterpolator 
{
    double nextInterpolant(OrbitView orbitView);

    OrbitViewInterpolator coalesceWith(OrbitView orbitView, OrbitViewInterpolator interpolator);
}
