/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.view.FlatOrbitView;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

/** Using the EarthFlat, FlatOrbitView and BasicRectangularTessellator
 * @author Patrick Murris
 * @version $Id$
 */
public class FlatWorld extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private FlatGlobe globe;
        private String projection;

        public AppFrame()
        {
            super(true, true, false);

            // Get a reference to the Globe
            this.globe = (FlatGlobe)this.getWwd().getModel().getGlobe();

            // Change atmosphere SkyGradientLayer for SkyColorLayer
            LayerList layers = this.getWwd().getModel().getLayers();
            for(int i = 0; i < layers.size(); i++)
            {
                if(layers.get(i) instanceof SkyGradientLayer)
                    layers.set(i, new SkyColorLayer());
            }
            this.getLayerPanel().update(this.getWwd());

            this.getWwd().getModel().setShowWireframeInterior(true);

            // Add control panel
            this.getLayerPanel().add(makeControlPanel(),  BorderLayout.SOUTH);
        }

        private JPanel makeControlPanel()
        {
            JPanel controlPanel = new JPanel(new GridLayout(0, 1, 0, 0));

            // Projection combo
            JPanel comboPanel = new JPanel(new GridLayout(0, 2, 0, 0));
            comboPanel.add(new JLabel("  Projection:"));
            final JComboBox cb1 = new JComboBox(new String[] {"Lat-Lon", "Mercator", "Sinusoidal", "Test"});
            cb1.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    String item = (String) cb1.getSelectedItem();
                    if(item.compareToIgnoreCase("Lat-Lon") == 0)
                    {
                        projection = FlatGlobe.PROJECTION_LAT_LON;
                    }
                    else if(item.compareToIgnoreCase("Mercator") == 0)
                    {
                        projection = FlatGlobe.PROJECTION_MERCATOR;
                    }
                    else if(item.compareToIgnoreCase("Sinusoidal") == 0)
                    {
                        projection = FlatGlobe.PROJECTION_SINUSOIDAL;
                    }
                    else if(item.compareToIgnoreCase("Test") == 0)
                    {
                        projection = FlatGlobe.PROJECTION_TEST;
                    }
                    update();
                }
            });
            cb1.setSelectedItem("Lat-Lon");
            comboPanel.add(cb1);

            controlPanel.add(comboPanel);
            controlPanel.setBorder(
                new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9), new TitledBorder("Flat World")));
            controlPanel.setToolTipText("Set the current projection");
            return controlPanel;
        }

        // Update worldwind
        private void update()
        {
            // Update globe projection
            this.globe.setProjection(this.projection);
            // Force a change in vertical exaggeration to rebuild geometry
            double ve = this.getWwd().getSceneController().getVerticalExaggeration();
            ve = ve % 1 == 0 ? ve + .5 : ve - (ve % 1);
            this.getWwd().getSceneController().setVerticalExaggeration(ve);
            this.getWwd().redraw();
        }
    }

    public static void main(String[] args)
    {
        // Adjust configuration values before instanciation
        Configuration.setValue(AVKey.GLOBE_CLASS_NAME, EarthFlat.class.getName());
        Configuration.setValue(AVKey.VIEW_CLASS_NAME, FlatOrbitView.class.getName());
        ApplicationTemplate.start("World Wind Flat World", AppFrame.class);
    }
}
