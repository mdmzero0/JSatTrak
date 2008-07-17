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
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import javax.media.opengl.GL;
import name.gano.astro.MathUtils;
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
    
    // test rotation 
    public double angle = 0;
    public double xAxis = 0;
    public double yAxis = 0;
    public double zAxis = 0;

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
    protected void draw(DrawContext dc, WWModel3D_new model) 
    {
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
            
            
            // Earth fixed Inertial
            double radius = 0.6;
         gl.glLineWidth(3.0f);
            gl.glColor3d(1, 0, 0); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(radius * 3, 0, 0);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
            // Draw in axis
            gl.glColor3d(0, 1, 0); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(0, radius * 3, 0);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
            // Draw in axis
            gl.glColor3d(0, 0, 1); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(0, 0, radius * 3);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
        
            
            
            //model.setRollDeg(pos.getLongitude().degrees);
            //model.setPitchDeg(-pos.getLatitude().degrees);
            
            // attitude  -- flight dynamics way or Euler angles way? 
            //if the base of the model is parallel to the x-y plane and the up vector is in the positive z direction it would be...
            
//            // best ---
//            gl.glRotated(model.getYawDeg(), 0,0,1); // 0,0,1
//            gl.glRotated(model.getPitchDeg(), 1,0,0);
//            gl.glRotated(model.getRollDeg(), 0,1,0);
            
////            gl.glRotated(model.getRollDeg(), 0,1,0);
////            gl.glRotated(model.getPitchDeg(), 1,0,0);
////            gl.glRotated(model.getYawDeg(), 0,0,1); // 0,0,1
////              gl.glRotated(model.getYawDeg(),   0,1,0); // 0,0,1
////              gl.glRotated(model.getPitchDeg(), 0,0,1);
////              gl.glRotated(model.getRollDeg(),  0,1,0);
            
            // TEST
            //gl.glRotated(angle,yAxis,zAxis,xAxis);
            gl.glRotated(angle,-xAxis,zAxis,yAxis); // in J2K coordinate frame
            
            
            // body axis
             // Earth fixed Inertial
            double radius2 = 0.3;
         gl.glLineWidth(3.0f);
            gl.glColor3d(1, 0, 0); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(radius2 * 3, 0, 0);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
            // Draw in axis
            gl.glColor3d(0, 1, 0); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(0, radius2 * 3, 0);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
            // Draw in axis
            gl.glColor3d(0, 0, 1); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(0, 0, radius2 * 3);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
            
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
    
    public void setMainRotationAngleAxis(double[] v)
    {
        //double[] orgOrientation = new double[] {0,0,1};
        double[] orgOrientation = new double[] {0,1,0};
    
//        double[] v = new double[3];
//        v[0] = Double.parseDouble(xBox.getText());
//        v[1] = Double.parseDouble(yBox.getText());
//        v[2] = Double.parseDouble(zBox.getText());
        
        double normV = MathUtils.norm(v);
        double normOr = MathUtils.norm(orgOrientation);
        
        double[] unitOrient = new double[] {orgOrientation[0]/normOr,orgOrientation[1]/normOr,orgOrientation[2]/normOr};
        double[] unitV = new double[] {v[0]/normV,v[1]/normV,v[2]/normV};
        
        double angleRad = Math.acos(MathUtils.dot(unitOrient, unitV));
        
        double[] axis = MathUtils.cross(unitOrient, unitV);
        double normAxis = MathUtils.norm(axis);
        
        //JOptionPane.showMessageDialog(this, "Angle: " + (angleRad*180.0/Math.PI) + ",\n normAxis: " + normAxis + ",\n Axis: " + axis[0] +"," + axis[1] +"," + axis[2]);
        
        
        //System.out.println("MOD unit VEL:" + unitV[0] +"," + unitV[1] +"," + unitV[2]);
        
        // reorient model
        this.angle =     angleRad*180.0/Math.PI;
        this.xAxis = axis[0];
        this.yAxis = axis[1];
        this.zAxis = axis[2];
        
    } // setMainRotationAngleAxis
    
    
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