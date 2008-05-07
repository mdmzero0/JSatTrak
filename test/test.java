/*
 * ModelLayerTest.java
 *
 * Created on February 12, 2008, 10:47 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package test;

import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.util.Random;
import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.geometry.Model;

/**
 *
 * @author RodgersGB
 */
public class test extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                Model3DLayer layer = new Model3DLayer();
                layer.setMaitainConstantSize(true);
                layer.setSize(300000);
                
                Random generator = new Random();
                Model modelOBJ = ModelFactory.createModel("test/data/penguin.obj");
                for (int i=0; i<100; i++) {
                    layer.addModel(new WWModel3D(modelOBJ,
                            new Position(Angle.fromDegrees(generator.nextInt()%80),
                                         Angle.fromDegrees(generator.nextInt()%180),
                                         500000)));
                }
                
                Model model3DS = ModelFactory.createModel("test/data/globalstar/Globalstar.3ds");
                for (int i=0; i<100; i++) {
                    layer.addModel(new WWModel3D(model3DS,
                            new Position(Angle.fromDegrees(generator.nextInt()%80),
                                         Angle.fromDegrees(generator.nextInt()%180),
                                         750000)));
                }
                
                insertBeforeCompass(this.getWwd(), layer);
                this.getLayerPanel().update(this.getWwd());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Model Layer Test", AppFrame.class);
    }
}
