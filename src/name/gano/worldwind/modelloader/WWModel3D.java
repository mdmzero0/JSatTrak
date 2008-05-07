/*
 * WWModel3D.java
 *
 * Created on February 14, 2008, 9:11 PM
 *
 * toolshed - http://forum.worldwindcentral.com/showthread.php?t=15222&page=6
 */

package name.gano.worldwind.modelloader;

import gov.nasa.worldwind.geom.Position;
import net.java.joglutils.model.geometry.Model;

/**
 *
 * @author RodgersGB, Shawn Gano
 */
public class WWModel3D
{
    private Position position;
    private Model model;
    
    private double yawDeg = 0; // in degrees
    private double pitchDeg = 0; //in degrees
    private double rollDeg = 0; // in degrees

    // STK - model - Nadir Alignment with ECF velocity constraint
    
    /** Creates a new instance of WWModel3D
     * @param model
     * @param pos 
     */
    public WWModel3D(Model model, Position pos)
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
}