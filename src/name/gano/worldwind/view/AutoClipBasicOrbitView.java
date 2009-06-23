/*
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 *
 * This file is part of JSatTrak.
 *
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
*/
package name.gano.worldwind.view;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.BasicOrbitView;

/**
 * Adds better auto clipping calculations for JSatTrak applications (needing to see beyond the earth)
 * Currently only works for Earth Centered view - not model centered
 * @author Shawn E. Gano
 */
public class AutoClipBasicOrbitView extends BasicOrbitView
{
    private double refRadius = 6378137.0;

    private float autoNearClipFactor = 5.0f; // the near clip plane is set to zoomFactor/this value
    private float autoFarClipFactor = 200.0f;


    @Override
    public double getAutoNearClipDistance()
    {
        Position eyePos = getCurrentEyePosition();
        double near = computeNearDistance(eyePos);

        //near = (near - zeroZoom) / autoNearClipFactor;
        // nearClippingPlaneDist = (zoomFactor - zeroZoom) / autoNearClipFactor;

        return near/4.0;
    }

    @Override
    public double getAutoFarClipDistance()
    {
        Position eyePos = getCurrentEyePosition();
        return computeFarDistance(eyePos)*4.0; // SEG - 2x
    }

    @Override
    protected double computeNearDistance(Position eyePosition)
    {
        double near = 0;
        if (eyePosition != null && this.dc != null)
        {
            double elevation = this.viewSupport.computeElevationAboveSurface(this.dc, eyePosition);
            double tanHalfFov = getFieldOfView().tanHalfAngle();
            near = elevation / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));
        }
        return near < MINIMUM_NEAR_DISTANCE ? MINIMUM_NEAR_DISTANCE : near;
    }

    @Override
    protected double computeFarDistance(Position eyePosition)
    {
        double far = 0;
        if (eyePosition != null)
        {
            far = computeHorizonDistance(eyePosition);
        }

        return far < MINIMUM_FAR_DISTANCE ? MINIMUM_FAR_DISTANCE : far;
    }
}
