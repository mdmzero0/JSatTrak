/*
 * SunTerminatorPolyLineTimeDep.java
 * 
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 * Created 20 June 2009
 */

package name.gano.worldwind.objects;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import java.awt.Color;
import java.util.Vector;
import javax.media.opengl.GL;
import jsattrak.gui.J2dEarthLabel2;
import name.gano.astro.bodies.Sun;

/**
 * A polyline object that is timedependant, representing the Sun's terminator
 * @author Shawn Gano
 */
public class SunTerminatorPolyLineTimeDep extends Polyline implements TimeDepRenderable
{
    private Sun sun;
    private int numPoints = 4;

    public SunTerminatorPolyLineTimeDep(Sun sun)
    {
        this.sun = sun;
        this.setColor(Color.DARK_GRAY);
        this.setAntiAliasHint(ANTIALIAS_DONT_CARE);
        this.setPathType(GREAT_CIRCLE); // because this ends up being a big circle and this saves us on points needed
        this.setClosed(true);
        this.setLineWidth(2.0);
        this.setFollowTerrain(true);

        updateMJD(numPoints);
    }

    /**
     * 
     * @param currentMJD
     */
    @Override
    public void updateMJD(double currentMJD)
    {
        // don't need to use currentMJD, just the fact the time has changed, sun object already has updated info

        // get new footprint points
        Vector<LatLon> llVec = J2dEarthLabel2.getFootPrintLatLonList(sun.getCurrentLLA()[0], sun.getCurrentLLA()[1], sun.getCurrentLLA()[2], numPoints);

        // update polyline
        this.setPositions(llVec,0.0);

    } // updateMJD

    @Override
    public void render(DrawContext dc)
    {
        javax.media.opengl.GL gl = dc.getGL();
        gl.glPushAttrib(javax.media.opengl.GL.GL_TEXTURE_BIT | javax.media.opengl.GL.GL_ENABLE_BIT | javax.media.opengl.GL.GL_CURRENT_BIT);

        // Added so that the colors wouldn't depend on sun shading
        gl.glDisable(GL.GL_TEXTURE_2D);

        super.render(dc);

        gl.glPopAttrib();

    }
}
