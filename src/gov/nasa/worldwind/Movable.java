/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.Position;

/**
 * @author tag
 * @version $Id: Movable.java 3219 2007-10-07 00:08:14Z tgaskins $
 */
public interface Movable
{
    Position getReferencePosition();

    /**
     * Shift the shape over the globe's surface while maintaining its original azimuth -- its orientation relative to
     * North.
     *
     * @param position the latitude and longitude to add to the shapes reference position.
     */

    void move(Position position);

    /**
     * Move the shape over the globe's surface while maintaining its original azimuth -- its orientation relative to
     * North.
     *
     * @param position the new position of the shapes reference position.
     */
    void moveTo(Position position);
}
