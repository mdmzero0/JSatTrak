/**
 * =====================================================================
 * Copyright (C) 2008 Shawn E. Gano
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
 */
package name.gano.worldwind.layers.Earth;

import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import javax.media.opengl.GL;

/**
 * This layer automatically rotates its collection of renderables so that their 
 * coordinates are plotted in ECI coordinates based on current time (MDJ).
 * <br><i>Note:</i> Because of the JOGL coordinate system is different than the ECI (e.g. J2000.0) coordinate system
 *       the renderables added to this collection should plot the ECI coordinates (x,y,z) as ( -x, z, y) 
 *       For example: gl.glVertex3d( -x, z , y);
 *
 * <p><b>!!Precession and Nutation are not yet accouted for.</b>
 *
 * <p>Shawn E. Gano
 * <br>Created on October 12, 2007
 *
 * <p> Based on RenderableLayer.java (3044 2007-09-26 02:08:22Z tgaskins)
 * 
 * @author Shawn E. Gano (shawn@gano.name), tag (orginal author of RenderableLayer.java)
 * @version $id$
 */
public class ECIRenderableLayer extends AbstractLayer
{
    
    private Collection<Renderable> renderables = new ArrayList<Renderable>();
    private final PickSupport pickSupport = new PickSupport();
    private final Layer delegateOwner;
    
    // ECI rotation variable
    private final double offsetRotdeg = -90.0; // jogl coordinate Greenwich to ECI x-axis offset 
    private double rotateECIdeg = 280.46061837+offsetRotdeg; // rotation in degrees (default j2k)
    private double currentMJD = 51544.5; // current modified julian date, universal time (default J2k)

    public ECIRenderableLayer()
    {
        this.delegateOwner = null;
    }
    
    // SEG added -------------
    /**
     * Creates layer with initial time
     * @param iniMJD inital Modified Julian Date
     */
    public ECIRenderableLayer(double iniMJD)
    {
        this.delegateOwner = null;
        setCurrentMJD(iniMJD);
    } // ECIRenderableLayer - constructor

    public ECIRenderableLayer(Layer delegateOwner)
    {
        this.delegateOwner = delegateOwner;
    }
    
    // SEG added -------------
    /**
     * Creates layer with delagate layer owner and initial time
     * @param delegateOwner delegate owner
     * @param iniMJD inital Modified Julian Date
     */
    public ECIRenderableLayer(Layer delegateOwner, double iniMJD)
    {
        this.delegateOwner = delegateOwner;
        setCurrentMJD(iniMJD);
    } // ECIRenderableLayer - constructor

    public void addRenderables(Iterable<Renderable> shapeIterator)
    {
        this.renderables = new ArrayList<Renderable>();

        if (shapeIterator == null)
            return;

        for (Renderable renderable : shapeIterator)
        {
            this.renderables.add(renderable);
        }
    }

    public void addRenderable(Renderable renderable)
    {
        if (renderable == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.renderables.add(renderable);
    }

    private void clearRenderables()
    {
        if (this.renderables != null && this.renderables.size() > 0)
        {
            for (Renderable renderable : this.renderables)
            {
                if (renderable instanceof Disposable)
                    ((Disposable) renderable).dispose();
            }
            this.renderables.clear();
        }
    }

    public void setRenderables(Iterable<Renderable> shapeIterator)
    {
        this.renderables = new ArrayList<Renderable>();

        this.clearRenderables();
        if (shapeIterator == null)
            this.addRenderables(shapeIterator);
    }

    public void setRenderable(final Renderable renderable)
    {
        this.clearRenderables();
        if (renderable != null)
            this.addRenderable(renderable);
    }

    public void removeRenderable(Renderable renderable)
    {
        if (renderable == null)
        {
            String msg = Logging.getMessage("nullValue.Shape");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.renderables.remove(renderable);
    }

    public Collection<Renderable> getRenderables()
    {
        return this.renderables;
    }

    public void dispose()
    {
        for (Renderable renderable : this.renderables)
        {
            if (renderable instanceof Disposable)
                ((Disposable) renderable).dispose();
        }
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        this.pickSupport.clearPickList();
        this.pickSupport.beginPicking(dc);

        for (Renderable renderable : this.renderables)
        {
            float[] inColor = new float[4];
            dc.getGL().glGetFloatv(GL.GL_CURRENT_COLOR, inColor, 0);
            Color color = dc.getUniquePickColor();
            dc.getGL().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());

            renderable.render(dc);

            dc.getGL().glColor4fv(inColor, 0);

            if (renderable instanceof Locatable)
            {
                this.pickSupport.addPickableObject(color.getRGB(), renderable,
                    ((Locatable) renderable).getPosition(), false);
            }
            else
            {
                this.pickSupport.addPickableObject(color.getRGB(), renderable);
            }
        }

        this.pickSupport.resolvePick(dc, pickPoint, this.delegateOwner != null ? this.delegateOwner : this);
        this.pickSupport.endPicking(dc);
    }

    // SEG modified -------------
    @Override
    //  This method has been modified to include rotating for ECI coordinates based on current MJD
    protected void doRender(DrawContext dc)
    {
        javax.media.opengl.GL gl = dc.getGL();
        
        gl.glPushMatrix();   // push for ECI roation
        gl.glRotated(-rotateECIdeg, 0.0, 1.0, 0.0); // rotate about Earth's spin axis (z-coordinate in J2K, y-coordinate in JOGL)
         
        for (Renderable renderable : this.renderables)
        {
            renderable.render(dc);
        }
        
        gl.glPopMatrix(); // pop matrix rotatex for ECI
    }

    public Layer getDelegateOwner()
    {
        return delegateOwner;
    }

    @Override
    public String toString()
    {
        return "ECI Layer";
    }

    // SEG added -------------
    public double getCurrentMJD()
    {
        return currentMJD;
    } // getCurrentMJD

    // SEG added -------------
    /**
     * Set the time for the ECI layer.  
     * Mean Sidereal formula taken from <i>Astronomical Algorithms</i> 2nd ed., Jean Meesus. 
     *
     * @param currentMJD  modified Julian Date (Universal Time)
     */
    public void setCurrentMJD(double currentMJD)
    {
        this.currentMJD = currentMJD;
        
        // centuries since J2000.0
        double T = (currentMJD-51544.5)/36525.0;  
        
        // now calculate the mean sidereal time at Greenwich (UT time) in degrees
        rotateECIdeg =  ( (280.46061837 + 360.98564736629*(currentMJD-51544.5)) + 0.000387933*T*T - T*T*T/38710000.0 +offsetRotdeg) % 360.0;
                
    } // setCurrentMJD

    // SEG added -------------
    public double getRotateECIdeg()
    {
        return rotateECIdeg;
    } //getRotateECIdeg
    
    
}
