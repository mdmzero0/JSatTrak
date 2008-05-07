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
 * @author RodgersGB
 */
public class WWModel3D {
    private Position position;
    private Model model;
    
    /** Creates a new instance of WWModel3D */
    public WWModel3D(Model model, Position pos) {
        this.model = model;
        this.setPosition(pos);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Model getModel() {
        return model;
    }

}