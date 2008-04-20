package gov.nasa.worldwind.examples.applet;
/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.examples.StatusBar;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Earth.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.view.*;
import netscape.javascript.JSObject;

import javax.swing.*;
import java.awt.*;

/**
 * Provides a base application framework for simple WorldWind applets.
 *
 * A simple applet which runs World Wind with a StatusBar at the bottom
 * and lets javascript set some view attributes.
 *
 * @author Patrick Murris
 * @version $Id:
 */

public class WWJApplet extends JApplet
{
    private WorldWindowGLCanvas wwd;
    private StatusBar statusBar;

    public WWJApplet()
    {
    }

    public void init()
    {
        try
        {
            // Create World Window GL Canvas
            this.wwd = new WorldWindowGLCanvas();
            this.getContentPane().add(this.wwd, BorderLayout.CENTER);

            // Create the default model as described in the current worldwind properties.
            Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
            this.wwd.setModel(m);

            // Add a BMNG base layer to the model layer list, before the Blue Marble
            insertBeforeLayerName(this.wwd, new BMNGOneImage(), "Blue Marble");

            // Add the status bar
            this.statusBar = new StatusBar();
            this.getContentPane().add(statusBar, BorderLayout.PAGE_END);

            // Forward events to the status bar to provide the cursor position info.
            this.statusBar.setEventSource(this.wwd);

            // Setup a select listener for the worldmap click-and-go feature
            this.wwd.addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                    {
                        if (event.hasObjects())
                        {
                            if (event.getTopObject() instanceof WorldMapLayer)
                            {
                                // Left click on World Map : iterate view to target position
                                Position targetPos = event.getTopPickedObject().getPosition();
                                OrbitView view = (OrbitView)WWJApplet.this.wwd.getView();
                                Globe globe = WWJApplet.this.wwd.getModel().getGlobe();
                                // Use a PanToIterator
                                view.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                                        view, globe, new LatLon(targetPos.getLatitude(), targetPos.getLongitude()),
                                        Angle.ZERO, Angle.ZERO, targetPos.getElevation()));
                            }
                        }
                    }
                }
            });

            // Call javascript appletInit()
            try
            {
                JSObject win = JSObject.getWindow(this);
                win.call("appletInit", null);
            }
            catch(Exception e) {}
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        // Call javascript appletStart()
        try
        {
            JSObject win = JSObject.getWindow(this);
            win.call("appletStart", null);
        }
        catch(Exception e) {}
    }

    public void stop()
    {
        // Call javascript appletSop()
        try
        {
            JSObject win = JSObject.getWindow(this);
            win.call("appletStop", null);
        }
        catch(Exception e) {}
        
        // Shut down World Wind
        WorldWind.shutDown();
    }

    /**
     * Adds a layer to WW current layerlist, before a named layer
     * Target name can be a part of the layer name
     * @param wwd
     * @param layer
     * @param targetName
     */
    public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName)
    {
        // Insert the layer into the layer list just before the target layer.
        int targetPosition = 0;
        LayerList layers = wwd.getModel().getLayers();
        for (Layer l : layers)
        {
            if (l.getName().indexOf(targetName) != -1)
            {
                targetPosition = layers.indexOf(l);
                break;
            }
        }
        layers.add(targetPosition, layer);
    }

    // ============== Public API - Javascript ======================= //

    /**
     * Move the current view position
     * @param lat the target latitude in decimal degrees
     * @param lon the target longitude in decimal degrees
     */
    public void gotoLatLon(double lat, double lon)
    {
        this.gotoLatLon(lat, lon, Double.NaN, 0, 0);
    }

    /**
     * Move the current view position, zoom, heading and pitch
     * @param lat the target latitude in decimal degrees
     * @param lon the target longitude in decimal degrees
     * @param zoom the target eye distance in meters
     * @param heading the target heading in decimal degrees
     * @param pitch the target pitch in decimal degrees
     */
    public void gotoLatLon(double lat, double lon, double zoom, double heading, double pitch)
    {
        OrbitView view = (OrbitView)this.wwd.getView();
        Globe globe = this.wwd.getModel().getGlobe();
        //view.setLatLon(new LatLon(Angle.fromDegrees(lat), Angle.fromDegrees(lon)));
        //view.firePropertyChange(AVKey.VIEW, null, view);
        if(!Double.isNaN(lat) || !Double.isNaN(lon) || !Double.isNaN(zoom))
        {
            lat = Double.isNaN(lat) ? view.getLookAtLatitude().degrees : lat;
            lon = Double.isNaN(lon) ? view.getLookAtLongitude().degrees : lon;
            zoom = Double.isNaN(zoom) ? view.getZoom() : zoom;
            heading = Double.isNaN(heading) ? view.getHeading().degrees : heading;
            pitch = Double.isNaN(pitch) ? view.getPitch().degrees : pitch;
            view.applyStateIterator(FlyToOrbitViewStateIterator.createPanToIterator(
                    view, globe, LatLon.fromDegrees(lat, lon), Angle.fromDegrees(heading), Angle.fromDegrees(pitch), zoom));
        }
    }

    /**
     * Set the current view heading and pitch
     * @param heading the traget heading in decimal degrees
     * @param pitch the target pitch in decimal degrees
     */
    public void setHeadingAndPitch(double heading, double pitch)
    {
        OrbitView view = (OrbitView)this.wwd.getView();
        if(!Double.isNaN(heading) || !Double.isNaN(pitch))
        {
            heading = Double.isNaN(heading) ? view.getHeading().degrees : heading;
            pitch = Double.isNaN(pitch) ? view.getPitch().degrees : pitch;

            view.applyStateIterator(ScheduledOrbitViewStateIterator.createHeadingAndPitchIterator(
                view, Angle.fromDegrees(heading), Angle.fromDegrees(pitch)));
        }
    }

    /**
     * Set the current view zoom
     * @param zoom the target eye distance in meters
     */
    public void setZoom(double zoom)
    {
        OrbitView view = (OrbitView)this.wwd.getView();
        if(!Double.isNaN(zoom))
        {
            view.applyStateIterator(ScheduledOrbitViewStateIterator.createZoomIterator(
                view, zoom));
        }
    }

    /**
     * Get the WorldWindowGLCanvas
     * @return the current WorldWindowGLCanvas
     */
    public WorldWindowGLCanvas getWW()
    {
        return this.wwd;
    }

    /**
     * Get the current OrbitView
     * @return the current OrbitView
     */
    public OrbitView getOrbitView()
    {
        if(this.wwd.getView() instanceof OrbitView)
            return (OrbitView)this.wwd.getView();
        return null;
    }

}

