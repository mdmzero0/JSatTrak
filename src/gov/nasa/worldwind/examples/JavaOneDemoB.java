package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.layers.Earth.*;

import javax.swing.*;
import java.awt.*;

public class JavaOneDemoB
{
    private Layer[] layers = new Layer[]
        {
            new BMNGSurfaceLayer(),
            new LandsatI3(),
            new USGSDigitalOrtho(),
            new USGSUrbanAreaOrtho(),
            new EarthNASAPlaceNameLayer(),
            new CompassLayer()
        };

    private static class AppFrame extends JFrame
    {
        public AppFrame(Layer[] layers)
        {
            
            Configuration.setValue(AVKey.INITIAL_LATITUDE, -45.0);
            Configuration.setValue(AVKey.INITIAL_LONGITUDE, 33.2);
            
            WorldWindowGLCanvas wwd = new WorldWindowGLCanvas();

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            wwd.setPreferredSize(new Dimension(1000, 800));
            mainPanel.add(wwd, BorderLayout.CENTER);

            StatusBar statusBar = new StatusBar();
            statusBar.setEventSource(wwd);
            mainPanel.add(statusBar, BorderLayout.PAGE_END);
            this.getContentPane().add(mainPanel, BorderLayout.CENTER);

            LayerList layerList = new LayerList();
            for (Layer layer : layers)
                layerList.add(layer);

            Model m = new BasicModel();
            m.setLayers(layerList);
            wwd.setModel(m);
            
            JPanel westContainer = new LayerPanel(wwd);
            this.getContentPane().add(westContainer, BorderLayout.WEST);
            this.pack();
            
//            ((OrbitView) wwd.getView()).setLatitude(Angle.fromDegrees(-45));
//            ((OrbitView)wwd.getView()).setZoom(7000000);
        }
    }

    public static void main(String[] args)
    {
        JavaOneDemoB demo = new JavaOneDemoB();
        AppFrame appFrame = new AppFrame(demo.layers);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        appFrame.setVisible(true);
    }
}
