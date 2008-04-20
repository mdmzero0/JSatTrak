package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.formats.gpx.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.view.*;
import org.xml.sax.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;
import javax.xml.parsers.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: SARDemoA.java 3437 2007-11-06 05:28:12Z tgaskins $
 */
public class SARDemoA
{
    private static ArrayList<String> trackFiles = new ArrayList<String>();

    static
    {
        trackFiles.add("src/gov/nasa/worldwind/examples/PipeTrackTest.gpx");
        trackFiles.add("src/gov/nasa/worldwind/examples/PipeTracks2.gpx");
        trackFiles.add("src/gov/nasa/worldwind/examples/PipeTracks3.gpx");
    }

    protected static class AppFrame extends JFrame
    {
        private ApplicationTemplate.AppPanel wwjPanel;
        private TrackManagementPanel trackPanel;
        private TerrainProfilePanel profilePanel;
        private LayerPanel layerPanel;

        public AppFrame()
        {
            Dimension canvasSize = new Dimension(800, 600);
            this.wwjPanel = new ApplicationTemplate.AppPanel(canvasSize, true);
            this.wwjPanel.setPreferredSize(canvasSize);

            // Panels
            JPanel controlPanel = new JPanel(new BorderLayout());
            JPanel upperControlPanel = new JPanel(new BorderLayout());
            JPanel lowerControlPanel = new JPanel(new BorderLayout());

            this.layerPanel = new LayerPanel(this.wwjPanel.getWwd());
            this.layerPanel.addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent)
                {
                    update();
                }
            });
            upperControlPanel.add(layerPanel, BorderLayout.CENTER);

            this.trackPanel = new TrackManagementPanel(trackFiles);
            lowerControlPanel.add(this.trackPanel, BorderLayout.CENTER);

            this.profilePanel = new TerrainProfilePanel(this.wwjPanel.getWwd());
            profilePanel.addPropertyChangeListener(new PropertyChangeListener()
            {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent)
                {
                    update();
                }
            });
            lowerControlPanel.add(profilePanel, BorderLayout.SOUTH);

            controlPanel.add(upperControlPanel, BorderLayout.CENTER);
            controlPanel.add(lowerControlPanel, BorderLayout.SOUTH);

            // Layers
            ApplicationTemplate.insertBeforeCompass(this.wwjPanel.getWwd(), profilePanel.getLayer());

            layerPanel.update(this.wwjPanel.getWwd());

            this.getContentPane().add(this.wwjPanel, BorderLayout.CENTER);
            this.getContentPane().add(controlPanel, BorderLayout.WEST);
            this.pack();

            // Center the application on the screen.
            Dimension prefSize = this.getPreferredSize();
            Dimension parentSize;
            java.awt.Point parentLocation = new java.awt.Point(0, 0);
            parentSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
            int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
            this.setLocation(x, y);
            this.setResizable(true);
        }

        private void update()
        {
            this.wwjPanel.getWwd().redraw();
        }

        private void updateView()
        {
            final Position pos = this.trackPanel.trackControlPanel.getPosition();
            if (pos == null)
                return;

//            OrbitView view = (OrbitView) this.wwjPanel.getWwd().getView();
//            ScheduledOrbitViewStateIterator vsi = ScheduledOrbitViewStateIterator.createHeadingIterator(
//                view.getHeading(), this.trackPanel.trackControlPanel.getHeading(), 1, true);

//            ScheduledOrbitViewStateIterator vsi = ScheduledOrbitViewStateIterator.createPositionHeadingIterator(
//                view.getEyePosition(), view.getEyePosition(), view.getHeading(), this.trackPanel.trackControlPanel.getHeading(), 500, true);

            javax.swing.Timer timer = new Timer(100, new ActionListener()
            {
                public void actionPerformed(ActionEvent actionEvent)
                {
                    OrbitView view = (OrbitView) wwjPanel.getWwd().getView();

//                    Position p = new Position(pos.getLatLon(), pos.getElevation());
//                    ScheduledOrbitViewStateIterator vsi = ScheduledOrbitViewStateIterator.createPositionIterator(
//                        view.getEyePosition(), p, 10, true);
//                    view.applyStateIterator(vsi);

                    Position p = new Position(pos.getLatLon(), pos.getElevation());
                    ScheduledOrbitViewStateIterator vsi = ScheduledOrbitViewStateIterator.createLatLonIterator(
                        view.getEyePosition().getLatLon(), p.getLatLon(), 500, true);
                    view.applyStateIterator(vsi);
                    ((Timer) actionEvent.getSource()).stop();
                }
            });
            timer.start();

//            view.applyStateIterator(vsi);
        }

        private class TrackManagementPanel extends JPanel
        {
            private final ArrayList<String> trackFiles;
            private TrackTraversalControlPanel trackControlPanel;

            public TrackManagementPanel(ArrayList<String> trackFiles)
            {
                super(new BorderLayout());

                this.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9),
                    new TitledBorder("Tracks")));

                this.trackFiles = trackFiles;

                String[] names = new String[trackFiles.size() + 1];
                names[0] = "Choose track";
                for (int i = 0; i < trackFiles.size(); i++)
                    names[i + 1] = "Track " + Integer.toString(i);

                JComboBox jcb = new JComboBox(names);
                jcb.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent actionEvent)
                    {
                        JComboBox cb = (JComboBox) actionEvent.getSource();
                        int id = cb.getSelectedIndex();
                        if (id == 0)
                            return;

                        --id;
                        String trackFile = TrackManagementPanel.this.trackFiles.get(id);
                        Layer layer[] = buildTracksLayer(trackFile);
                        Layer oldLayer = trackControlPanel.getTrackLayer();
                        trackControlPanel.setTrackLayer((RenderableLayer) layer[0]);
                        for (Layer l : layer)
                            ApplicationTemplate.insertAfterPlacenames(wwjPanel.getWwd(), l);

                        if (oldLayer != null)
                            oldLayer.dispose();

                        layerPanel.update(wwjPanel.getWwd());

//
//                        javax.swing.Timer timer = new Timer(3010, new ActionListener()
//                        {
//                            public void actionPerformed(ActionEvent actionEvent)
//                            {
//                                OrbitView view = (OrbitView) wwjPanel.getWwd().getView();
//                                ScheduledOrbitViewStateIterator psi =
//                                    ScheduledOrbitViewStateIterator.createPitchIterator(
//                                        view.getPitch(), Angle.POS90, 2000, true);
//                                view.applyStateIterator(psi);
//                                ((Timer) actionEvent.getSource()).stop();
//                            }
//                        });
//                        timer.start();
                    }
                });
                this.add(jcb, BorderLayout.NORTH);

                this.trackControlPanel = new TrackTraversalControlPanel();
                this.trackControlPanel.addPropertyChangeListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent)
                    {
                        if (propertyChangeEvent.getPropertyName().equals("PositionNumber"))
                        {
                            Position pos = trackPanel.trackControlPanel.getAdjustedPosition();
                            profilePanel.getLayer().setObjectPosition(pos);
                            profilePanel.getLayer().setObjectHeading(trackPanel.trackControlPanel.getHeading());
                            updateView();
                        }
                        else if (propertyChangeEvent.getPropertyName().equals("InteriorPosition"))
                        {
                            Position pos = trackPanel.trackControlPanel.getAdjustedPosition();
                            profilePanel.getLayer().setObjectPosition(pos);
                            profilePanel.getLayer().setObjectHeading(trackPanel.trackControlPanel.getHeading());
                            AppFrame.this.update();
                        }
                        else if (propertyChangeEvent.getPropertyName().equals("Layer"))
                        {
                            Position pos = trackPanel.trackControlPanel.getAdjustedPosition();
                            profilePanel.getLayer().setObjectPosition(pos);
                            profilePanel.getLayer().setObjectHeading(trackPanel.trackControlPanel.getHeading());
                            updateView();
                        }
                    }
                });
                this.add(this.trackControlPanel, BorderLayout.CENTER);

//                JSlider segmentSlider = this.trackControlPanel.getSegmentSlider();
//                segmentSlider.addChangeListener(new ChangeListener()
//                {
//                    public void stateChanged(ChangeEvent changeEvent)
//                    {
//                        JSlider slider = trackControlPanel.getSegmentSlider();
//                        int i = slider.getValue();
//                        int min = slider.getMinimum();
//                        int max = slider.getMaximum();
//                        double t = (double) i / ((double) max - (double) min);
//                        Position p = trackControlPanel.getPositionAlongSegment(t);
//                        if (p == null)
//                            return;
//
//                        profilePanel.getLayer().setObjectPosition(p);
//                        profilePanel.getLayer().setObjectHeading(trackControlPanel.getHeading());
//                        AppFrame.this.update();
//                    }
//                });
            }

            private Layer[] buildTracksLayer(String filePath)
            {
                try
                {
                    GpxReader reader = new GpxReader();
                    reader.readFile(filePath);
                    TrackPointIteratorImpl tracks = new TrackPointIteratorImpl(reader.getTracks());
                    ArrayList<Position> positions = new ArrayList<Position>();
                    while (tracks.hasNext())
                        positions.add(tracks.next().getPosition());
                    
                    Polyline path = new Polyline(positions);
                    path.setPathType(Polyline.LINEAR);
                    path.setColor(Color.WHITE);
                    path.setLineWidth(2);
                    RenderableLayer pathLayer = new RenderableLayer();
                    pathLayer.setName("Path");
                    pathLayer.addRenderable(path);

                    Polyline groundPath = new Polyline(positions);
                    groundPath.setPathType(Polyline.LINEAR);
                    groundPath.setFollowTerrain(true);
                    groundPath.setStippleFactor(5);
                    groundPath.setStipplePattern((short) 0xAAAA);
                    groundPath.setColor(Color.GREEN);
                    RenderableLayer groundPathLayer = new RenderableLayer();
                    groundPathLayer.setName("Ground Path");
                    groundPathLayer.addRenderable(groundPath);

                    pathLayer.addRenderable(groundPath);

                    TrackMarkerLayer trackLayer = new TrackMarkerLayer(reader.getTracks());
                    trackLayer.setMarkerPixels(2);
                    trackLayer.setMaterial(Material.RED);

                    TrackMarkerLayer groundTrackLayer = new TrackMarkerLayer(reader.getTracks());
                    groundTrackLayer.setMarkerPixels(trackLayer.getMarkerPixels());
                    groundTrackLayer.setMaterial(groundTrackLayer.getMaterial());
                    groundTrackLayer.setOverrideElevation(true);

                    return new Layer[] {pathLayer, trackLayer, groundTrackLayer};
//                    return new TrackPipesLayer(tracks);
                }
                catch (ParserConfigurationException e)
                {
                    e.printStackTrace();
                }
                catch (SAXException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

                return null;
            }
        }
    }

    private static final String APP_NAME = "SAR 3D Demo";

    static
    {
        if (Configuration.isMacOS())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", APP_NAME);
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
        }
    }

    public static void main(String[] args)
    {
        try
        {
            AppFrame frame = new AppFrame();
            frame.setTitle(APP_NAME);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
