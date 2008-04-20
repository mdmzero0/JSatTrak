/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Position;

/**
 * @author tag
 * @version $Id$
 */
public class Pedestal extends UserFacingIcon
{
    private double spacingPixels = 2d;
    private double scale = 1d;

    public Pedestal(String iconPath, Position iconPosition)
    {
        super(iconPath, iconPosition);
    }

    public double getSpacingPixels()
    {
        return spacingPixels;
    }

    public void setSpacingPixels(double spacingPixels)
    {
        this.spacingPixels = spacingPixels;
    }

    public double getScale()
    {
        return scale;
    }

    public void setScale(double scale)
    {
        this.scale = scale;
    }
}
