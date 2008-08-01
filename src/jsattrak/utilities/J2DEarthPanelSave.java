/*
 * J2DEarthPanelSave.java
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
 * Created on August 20, 2007, 12:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jsattrak.utilities;

import jsattrak.gui.J2DEarthPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.io.Serializable;
import javax.swing.JInternalFrame;

/**
 *
 * @author sgano
 */
public class J2DEarthPanelSave implements Serializable
{
    private int xPos; // location of window
    private int yPos;
    
    private int width;
    private int height;
    
    // panel options
    private Color backgroundColor;
    //private int imageMapNum;
    private int imageScalingOption;
    private String backgroundImagePath;
    
    //private JulianDay currentTime;
    // label options
    private boolean showDateTime;
    private int xDateTimeOffset;
    private int yDateTimeOffset;
    private Color dateTimeColor;
    private boolean showLatLong;
    private double centerLat; // center latitude
    private double centerLong; // center longitude
    private double zoomFactor; // current zoom factor (>= 1.0)
    private double zoomIncrementMultiplier;
    
    // sun drawing options
    private boolean drawSun;
    private Color sunColor;//Color.BLACK;
    private int numPtsSunFootPrint;
    private float sunAlpha;
    
    // region drawing
    private Color landOutlineColor;
    private String dataFileName;
    private boolean showLandMassOutlines;
    
    /** Creates a new instance of J2DEarthPanelSave */
    public J2DEarthPanelSave(J2DEarthPanel panel, int x, int y, Dimension d)
    {
        xPos = x;
        yPos = y;
        
        width = d.width;
        height = d.height;
        
        //panel options
        backgroundColor = panel.getBackgroundColor();
        //imageMapNum = panel.getImageMapNum();
        backgroundImagePath = panel.getTwoDMap(); // map path
        imageScalingOption = panel.getImageScalingOption();
        //currentTime = ;
        // label options
        showDateTime = panel.isShowDateTime();
        xDateTimeOffset = panel.getXDateTimeOffset();
        yDateTimeOffset = panel.getYDateTimeOffset();
        dateTimeColor = panel.getDateTimeColor();
        showLatLong = panel.getShowLatLonLines();
        centerLat = panel.getCenterLat();
        centerLong = panel.getCenterLong();
        zoomFactor = panel.getZoomFactor();
        zoomIncrementMultiplier = panel.getZoomIncrementMultiplier();
        
        // darkness
        drawSun = panel.isDrawSun();
        sunColor = panel.getSunColor();//Color.BLACK;
        numPtsSunFootPrint = panel.getNumPtsSunFootPrint();
        sunAlpha = panel.getSunAlpha();
        
        // region drawing
        landOutlineColor = panel.getRegionLineColor();
        dataFileName = panel.getRegionFileName();
        showLandMassOutlines = panel.isRegionDrawOn();
        
        
    } // J2DEarthPanelSave constructor
    
    // OUTPUT function, save data from this object back to a new j2dearthpanel and internal frame
    public void copySettings2PanelAndFrame(J2DEarthPanel newPanel, JInternalFrame iframe)
    {
        // set size and location
        iframe.setSize(width,height);
        iframe.setLocation(xPos,yPos);
        
        // set panel options
        newPanel.setBackgroundColor(backgroundColor);
        //newPanel.setImageMapNum(imageMapNum); 
        newPanel.setTwoDMap(backgroundImagePath);
        newPanel.setImageScalingOption(imageScalingOption);
        
        // set label options
        newPanel.setShowDateTime(showDateTime);
        newPanel.setXDateTimeOffset(xDateTimeOffset);
        newPanel.setYDateTimeOffset(yDateTimeOffset);
        newPanel.setDateTimeColor(dateTimeColor);
        newPanel.setShowLatLonLines(showLatLong);
        newPanel.setCenterLat(centerLat);
        newPanel.setCenterLong(centerLong);
        newPanel.setZoomFactor(zoomFactor);
        newPanel.setZoomIncrementMultiplier(zoomIncrementMultiplier);
        
        // darkness
        newPanel.setDrawSun(drawSun);
        newPanel.setSunColor(sunColor);
        newPanel.setNumPtsSunFootPrint(numPtsSunFootPrint);
        newPanel.setSunAlpha(sunAlpha);
        
         // region drawing
        newPanel.setRegionDrawingOptions(showLandMassOutlines, dataFileName, landOutlineColor);
    }
    
} //J2DEarthPanelSave class
