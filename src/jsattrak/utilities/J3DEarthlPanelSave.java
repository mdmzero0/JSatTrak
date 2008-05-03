/*
 * J3DEarthInternalPanelSave.java
 *=====================================================================
 * Copyright (C) 2008 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 * Created on 15 August 2007
 *
 */

package jsattrak.utilities;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.view.OrbitView;
import java.awt.Container;
import java.awt.Dimension;
import java.io.Serializable;
import java.util.Hashtable;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;

/**
 *
 * @author sgano
 */
public class J3DEarthlPanelSave implements Serializable
{
    private int xPos; // location of window
    private int yPos;
    
    private int width;
    private int height;
    
    // panel options
    private boolean viewModeECI; // view mode - ECI (true) or ECEF (false)
    private double fovDeg; // field of view  in degrees
    // terrain profiler
    private boolean showTerrainProfiler; 
    private String terrainProfileSat;
    private double terrainProfileLongSpan;
    
    // -- to get working
    
    // 3D view point
   double eyePosLat;
   double eyePosLon;
   double eyePosElv; 
   double viewCenterLat;
   double viewCenterLon;
   double viewCenterElv;
   double viewZoom; // working
   double viewPitch;
   double viewHeading;
   
   // title of frame
   String frameTitle = "";
   
    
    // layer list and if they are visible
    Hashtable<String,Boolean> layerEnabledHT;
    
    // web map services
    
    
    
    /** Creates a new instance of J2DEarthPanelSave */
    public J3DEarthlPanelSave(J3DEarthComponent panel, int x, int y, Dimension d)
    {
        xPos = x;
        yPos = y;
        
        width = d.width;
        height = d.height;
        
        //panel options
        viewModeECI = panel.isViewModeECI();
        fovDeg = panel.getWwd().getView().getFieldOfView().getDegrees();
        
        showTerrainProfiler = panel.getTerrainProfileEnabled();
        terrainProfileSat = panel.getTerrainProfileSat();
        terrainProfileLongSpan = panel.getTerrainProfileLongSpan();
        
        // gets a hashtable with all the layers names and if they are enabled - but no other properties
        // this won't allow opening of user added layers unfortunaly but not sure how to do that.
        // saving LayerList doesn't work.
        layerEnabledHT = new Hashtable<String,Boolean>();
        for(Layer l : panel.getLayerList())
        {
            layerEnabledHT.put(l.getName(), l.isEnabled());
        }
        
        // 3D view camera location and 
//        viewLat = ((OrbitView) panel.getWwd().getView()).getLatitude().getDegrees();
//        viewLon = ((OrbitView) panel.getWwd().getView()).getLongitude().getDegrees();
        // NEED TO UPDATE using v0.5
        eyePosLat = ((OrbitView) panel.getWwd().getView()).getCurrentEyePosition().getLatitude().getDegrees();
        eyePosLon = ((OrbitView) panel.getWwd().getView()).getCurrentEyePosition().getLongitude().getDegrees();
        eyePosElv = ((OrbitView) panel.getWwd().getView()).getCurrentEyePosition().getElevation();

        viewCenterLat = ((OrbitView) panel.getWwd().getView()).getCenterPosition().getLatitude().getDegrees();
        viewCenterLon = ((OrbitView) panel.getWwd().getView()).getCenterPosition().getLongitude().getDegrees();
        viewCenterElv = ((OrbitView) panel.getWwd().getView()).getCenterPosition().getElevation();
        
        viewPitch = ((OrbitView) panel.getWwd().getView()).getPitch().getDegrees();
        viewHeading = ((OrbitView) panel.getWwd().getView()).getHeading().getDegrees();
        viewZoom = ((OrbitView)panel.getWwd().getView()).getZoom();
        
       
        
          
//        viewQuat = ((OrbitView)panel.getWwd().getView()).getRotation();
//        
//        eyePos = ((OrbitView)panel.getWwd().getView()).getEyePosition();
//        
        
        // save title
       frameTitle = panel.getDialogTitle();
  
        
    } // J2DEarthPanelSave constructor
    
    // OUTPUT function, save data from this object back to a new j2dearthpanel and internal frame
    public void copySettings2PanelAndFrame(J3DEarthComponent newPanel, Container iframe)
    {
        // set size and location
        iframe.setSize(width,height);
        iframe.setLocation(xPos,yPos);
        
        // set propties and options - like FOV and terrain
        newPanel.setViewModeECI(viewModeECI);
        newPanel.setTerrainProfileEnabled(showTerrainProfiler);
        newPanel.setTerrainProfileSat(terrainProfileSat);
        newPanel.setTerrainProfileLongSpan(terrainProfileLongSpan);
        
        
        // set FOV to WWJ model
        newPanel.getWwd().getView().setFieldOfView(Angle.fromDegrees(fovDeg));
        
        
        // set layer list enabled/disabled (using current layers in the object)
        for(Layer l : newPanel.getLayerList())
        {
            if( layerEnabledHT.containsKey(l.getName()) ) // if the saved has contains the name of this layer then set its properties
            {
                l.setEnabled( layerEnabledHT.get(l.getName())  );
            }
        }
        
        
        // Stop iterators first
        ((OrbitView)newPanel.getWwd().getView()).stopStateIterators();
        ((OrbitView)newPanel.getWwd().getView()).stopMovement();
        ((OrbitView)newPanel.getWwd().getView()).stopMovementOnCenter();// ??
        
        // set panel options
        //newPanel.setBackgroundColor(backgroundColor);
        //((OrbitView)newPanel.getWwd().getView()).setRotation(viewQuat);
//        ((OrbitView)newPanel.getWwd().getView()).setZoom(viewZoom);
//        ((OrbitView)newPanel.getWwd().getView()).setLatitude(Angle.fromDegrees(viewLat));
//        ((OrbitView)newPanel.getWwd().getView()).setLongitude(Angle.fromDegrees(viewLon));
        //eyePos
//        ((OrbitView)newPanel.getWwd().getView()).setLatLonAltitude(eyePos);
//        
//        ((OrbitView)newPanel.getWwd().getView()).setLatitude(Angle.fromDegrees(-45));
        
        // setup inital view
//        System.out.println("lat" + Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE));
//        System.out.println("lon" + Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE));
//        System.out.println("alt" + Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE));
//        System.out.println("head" + Configuration.getDoubleValue(AVKey.INITIAL_HEADING));
//        System.out.println("pitch" + Configuration.getDoubleValue(AVKey.INITIAL_PITCH));
        
        // does this work?
        Configuration.setValue(AVKey.INITIAL_LATITUDE, eyePosLat);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, eyePosLon);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, eyePosElv);
        
        Configuration.setValue(AVKey.INITIAL_PITCH, viewPitch);
        Configuration.setValue(AVKey.INITIAL_HEADING, viewHeading);
        
        //((OrbitView)newPanel.getWwd().getView()).setZoom(viewZoom); // set zoom
        
        
//        System.out.println("lat" + Configuration.getDoubleValue(AVKey.INITIAL_LATITUDE));
//        System.out.println("lon" + Configuration.getDoubleValue(AVKey.INITIAL_LONGITUDE));
//        System.out.println("alt" + Configuration.getDoubleValue(AVKey.INITIAL_ALTITUDE));
//        System.out.println("head" + Configuration.getDoubleValue(AVKey.INITIAL_HEADING));
//        System.out.println("pitch" + Configuration.getDoubleValue(AVKey.INITIAL_PITCH));
//        
//        System.out.println("Eye pos: lat: deg:" + eyePos.getLatitude().getDegrees());
//        
//        System.out.println("Eye pos: lat: deg:" + ((OrbitView)newPanel.getWwd().getView()).getLatitude().getDegrees());
        
        newPanel.getWwd().redraw();
        
        // set TITLE
        if( iframe instanceof JInternalFrame) // internal
        {
            ((JInternalFrame)iframe).setTitle(frameTitle);
        }
        else if(iframe instanceof JDialog) // external
        {
            ((JDialog)iframe).setTitle(frameTitle);
        }
        
    }
    
} //J3DEarthInternalPanelSave class
