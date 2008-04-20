/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.util.Gazetteer;
import gov.nasa.worldwind.examples.WMSLayerManagerFrame;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.view.*;
import gov.nasa.worldwind.geom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.text.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: GazetteerPanel.java 3612 2007-11-22 16:48:56Z tgaskins $
 */
public class GazetteerPanel extends JPanel
{
    private final WorldWindow wwd;
    private Gazetteer gazeteer = new Gazetteer();

    public GazetteerPanel(final WorldWindow wwd)
    {
        super(new BorderLayout());

        this.wwd = wwd;

        // The label
        URL imageURL = this.getClass().getResource("/images/safari-32x32.png");
        ImageIcon icon = new ImageIcon(imageURL);
        JLabel label = new JLabel(icon);
        label.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));

        // The text field
        final JTextField field = new JTextField("Where to go?");
        field.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent actionEvent)
            {
                EventQueue.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        String text = field.getText();
                        if (text != null && text.equals("show wms"))
                        {
                            WMSLayerManagerFrame wmsf = new WMSLayerManagerFrame(wwd);
                            return;
                        }
                        try
                        {
                            handleEntryAction(actionEvent);
                        }
                        catch (Gazetteer.GazetteerException e)
                        {
                            JOptionPane.showMessageDialog(GazetteerPanel.this,
                                "Location not available (" + e.getMessage() + ")",
                                "Location Not Available", JOptionPane.ERROR_MESSAGE);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            JOptionPane.showMessageDialog(GazetteerPanel.this,
                                "Error looking up \"" + (field.getText() != null ? field.getText() : "") + "\"\n"
                                    + e.getMessage(),
                                "Lookup Failure", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
            }
        });

        // Enclose entry field in an inner panel in order to control spacing/padding
        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.add(field, BorderLayout.CENTER);
        fieldPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));

        // Put everything together
        this.add(label, BorderLayout.WEST);
        this.add(fieldPanel, BorderLayout.CENTER);
    }

    private void handleEntryAction(ActionEvent actionEvent) throws IOException, ParserConfigurationException,
        XPathExpressionException, SAXException, Gazetteer.GazetteerException
    {
        String lookupString = null;

        if (actionEvent.getSource() instanceof JTextComponent)
            lookupString = ((JTextComponent) actionEvent.getSource()).getText();

        if (lookupString == null || lookupString.length() < 1)
            return;

        ArrayList<Gazetteer.Location> results = this.gazeteer.getLocations(lookupString);
        if (results == null || results.size() == 0)
            return;

        this.moveToLocation(results.get(0));
    }

    private void moveToLocation(Gazetteer.Location location)
    {
        System.out.println("Orbiting");
        OrbitView view = (OrbitView) this.wwd.getView();
        Globe globe = this.wwd.getModel().getGlobe();
        if (globe != null && view != null)
        {
            // Use a PanToIterator to iterate view to target position
            view.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                view, globe, location.getLatlon(),
                Angle.ZERO, Angle.ZERO, 25e3));
        }
    }
}
