/*
 * WWModel3D_new.java
 *
 * Created on February 14, 2008, 9:11 PM
 *
 * toolshed - http://forum.worldwindcentral.com/showthread.php?t=15222&page=6
 */

package name.gano.worldwind.modelloader;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import javax.media.opengl.GL;
import net.java.joglutils.model.examples.DisplayListRenderer;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.iModel3DRenderer;

/**
 *
 * @author RodgersGB, Shawn Gano
 */

public class WWModel3D_new implements Renderable
{
    private Position position;
    private Model model;
    
    private double yawDeg = 0; // in degrees
    private double pitchDeg = 0; //in degrees
    private double rollDeg = 0; // in degrees
    
    
    private boolean maitainConstantSize = true; // default true
    private double size = 1;

    // STK - model - Nadir Alignment with ECF velocity constraint
    
    /** Creates a new instance of WWModel3D_new
     * @param model
     * @param pos 
     */
    public WWModel3D_new(Model model, Position pos)
    {
        this.model = model;
        this.setPosition(pos);
    }

    public Position getPosition()
    {
        return position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    public Model getModel()
    {
        return model;
    }

    public double getYawDeg()
    {
        return yawDeg;
    }

    public void setYawDeg(double yawDeg)
    {
        this.yawDeg = yawDeg;
    }

    public double getPitchDeg()
    {
        return pitchDeg;
    }

    public void setPitchDeg(double pitchDeg)
    {
        this.pitchDeg = pitchDeg;
    }

    public double getRollDeg()
    {
        return rollDeg;
    }

    public void setRollDeg(double rollDeg)
    {
        this.rollDeg = rollDeg;
    }
    
     public boolean isConstantSize() 
    {
        return maitainConstantSize;
    }

    public void setMaitainConstantSize(boolean maitainConstantSize) {
        this.maitainConstantSize = maitainConstantSize;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }
    
    // Rendering routines so the object can render itself ------
    //===============================================================
    // old doRender
    public void render(DrawContext dc) 
    {
        try {
            beginDraw(dc);
            
            draw(dc, this);
        }
        // handle any exceptions
        catch (Exception e) {
            // handle
            e.printStackTrace();
        }
        // we must end drawing so that opengl
        // states do not leak through.
        finally {
            endDraw(dc);
        }
    }
    
    // draw this layer
    protected void draw(DrawContext dc, WWModel3D_new model) {
        GL gl = dc.getGL();
        Position pos = model.getPosition();
        Vec4 loc = dc.getGlobe().computePointFromPosition(pos);
        double localSize = this.computeSize(dc, loc);
        
        if (dc.getView().getFrustumInModelCoordinates().contains(loc)) 
        {
            dc.getView().pushReferenceCenter(dc, loc);
//            gl.glRotated(pos.getLongitude().degrees, 0,1,0);
//            gl.glRotated(-pos.getLatitude().degrees, 1,0,0);
            gl.glScaled(localSize, localSize, localSize);  /// can change the scale of the model here!!
            
            //model.setRollDeg(pos.getLongitude().degrees);
            //model.setPitchDeg(-pos.getLatitude().degrees);
            
            // attitude  -- flight dynamics way or Euler angles way? 
            //if the base of the model is parallel to the x-y plane and the up vector is in the positive z direction it would be...
            gl.glRotated(model.getYawDeg(), 0,0,1); // 0,0,1
            gl.glRotated(model.getPitchDeg(), 1,0,0);
            gl.glRotated(model.getRollDeg(), 0,1,0);
//            gl.glRotated(model.getRollDeg(), 0,1,0);
//            gl.glRotated(model.getPitchDeg(), 1,0,0);
//            gl.glRotated(model.getYawDeg(), 0,0,1); // 0,0,1
//              gl.glRotated(model.getYawDeg(),   0,1,0); // 0,0,1
//              gl.glRotated(model.getPitchDeg(), 0,0,1);
//              gl.glRotated(model.getRollDeg(),  0,1,0);
            
            
            
            // Get an instance of the display list renderer
            iModel3DRenderer renderer = DisplayListRenderer.getInstance();
            renderer.render(gl, model.getModel());
            dc.getView().popReferenceCenter(dc);
        }
    }
    
    // puts opengl in the correct state for this layer
    protected void beginDraw(DrawContext dc) {
        GL gl = dc.getGL();
        
        // SEG - MAYBE USE LIGHTING TO SIMULATE SUN ON SPACE CRAFT??
        
//        Vec4 cameraPosition = dc.getView().getEyePoint();
        
        gl.glPushAttrib(
            GL.GL_TEXTURE_BIT |
            GL.GL_COLOR_BUFFER_BIT |
            GL.GL_DEPTH_BUFFER_BIT |
            GL.GL_HINT_BIT |
            GL.GL_POLYGON_BIT |
            GL.GL_ENABLE_BIT | 
            GL.GL_CURRENT_BIT | 
            GL.GL_LIGHTING_BIT | 
            GL.GL_TRANSFORM_BIT);
        
        
        
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
    }
    
    // resets opengl state
    protected void endDraw(DrawContext dc) {
        GL gl = dc.getGL();
        
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
        gl.glPopMatrix();
        
        gl.glPopAttrib();
    }
    
    private double computeSize(DrawContext dc, Vec4 loc) {
        if (this.maitainConstantSize)
            return size;
        
        if (loc == null) {
            System.err.println("Null location when computing size of model");
            return 1;
        }
        double d = loc.distanceTo3(dc.getView().getEyePoint());
        double currentSize = 60 * dc.getView().computePixelSizeAtDistance(d);
        if (currentSize < 2)
            currentSize = 2;
        
        return currentSize;
    }
    
}