package gov.nasa.worldwind.pick;

import gov.nasa.worldwind.render.DrawContext;
/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author lado
 * @version $Id: Pickable Feb 4, 2007 11:46:48 PM
 */
public interface Pickable
{
    public void pick(DrawContext dc, java.awt.Point pickPoint);
}
