/*
 * EcefTimeDepRenderableLayer.java
 * 
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
 * Created 20 June 2009
 */

package name.gano.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import jsattrak.gui.JSatTrak;
import name.gano.worldwind.objects.SunTerminatorPolyLineTimeDep;
import name.gano.worldwind.objects.TimeDepRenderable;

/**
 * This object is an ECEF layer that contains objects that are time dependent, like sun sub point,
 * satellite sub point, etc.
 *
 * @author Shawn E. Gano
 */
public class EcefTimeDepRenderableLayer extends RenderableLayer
{
    private double currentMJD = 51544.5; // current modified julian date, universal time (default J2k)
    private boolean showTerminatorLine = false;
    private JSatTrak app;

    // terminator
    private SunTerminatorPolyLineTimeDep terminator;

    public EcefTimeDepRenderableLayer(double currentMJD, JSatTrak app)
    {
        super();

        this.currentMJD = currentMJD;
        this.app = app;

        terminator = new SunTerminatorPolyLineTimeDep(app.getSun());
        if(this.showTerminatorLine)
        {
            addRenderable(terminator);
        }
    }

    @Override
    public String toString()
    {
        return "ECEF Time Dep. Layer";
    }

    @Override
    public void addRenderable(Renderable renderable)
    {
        // set the current time for the object
        if (renderable instanceof TimeDepRenderable)
        {
            ((TimeDepRenderable) renderable).updateMJD(currentMJD);
        }

        // do the normal adding of the object
        super.addRenderable(renderable);
    }

    /**
     * addds an object to this layer
     * @param renderable
     */
    public void addRenderable(TimeDepRenderable renderable)
    {
        // set the current time
        renderable.updateMJD(currentMJD);

        // do the normal adding of the object
        super.addRenderable(renderable);
    }

    // SEG added -------------
    /**
     * Set the time for the ECEF time dep. layer.
     *
     * @param currentMJD  modified Julian Date (Universal Time)
     */
    public void setCurrentMJD(double currentMJD)
    {
        this.currentMJD = currentMJD;

        // set ECI angle to all OrbitModelRenderables
        for (Renderable renderable : super.getRenderables())
        {
            if (renderable instanceof TimeDepRenderable)
            {
                ((TimeDepRenderable) renderable).updateMJD(currentMJD);
            }//TimeDepRenderable
        } // for renderables

    } // setCurrentMJD

    /**
     * @return the showTerminatorLine
     */
    public boolean isShowTerminatorLine()
    {
        return showTerminatorLine;
    }

    /**
     * @param showTerminatorLine the showTerminatorLine to set
     */
    public void setShowTerminatorLine(boolean showTerminatorLine)
    {
        if(this.showTerminatorLine == showTerminatorLine)
        {
            return; // do nothing
        }

        this.showTerminatorLine = showTerminatorLine;

        if(this.showTerminatorLine)
        {
            terminator.updateMJD(currentMJD);
            addRenderable(terminator);
        }
        else
        {
            this.removeRenderable(terminator);
        }
    } // setShowTerminatorLine

    /**
     * For seting properties of the terminator line
     * @return the terminator
     */
    public SunTerminatorPolyLineTimeDep getTerminator()
    {
        return terminator;
    }
}
