/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.pick.Pickable;

/**
 * @author tag
 * @version $Id$
 */
public interface OrderedRenderable extends Renderable, Pickable
{
    double getDistanceFromEye();
}
