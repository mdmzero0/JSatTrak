/*
 * CoverageAnalyzer.java
 * 
 * Class to analyze Earth coverage metrics from Satellite(s). Also has the 
 * capability to render metrics to 2D and 3D windows.
 * 
 * =====================================================================
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
 */

package jsattrak.coverage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;
import jsattrak.gui.J2dEarthLabel2;
import name.gano.astro.time.Time;

/**
 *
 * @author Shawn
 */
public class CoverageAnalyzer implements JSatTrakRenderable,JSatTrakTimeDependent
{
    int latPanels = 18; // number of divisons along lines of latitude (grid points -1)
    int longPanels = 36;  // number of divisions along lines of longitude (grid points -1)
    
    // in degrees
    double[] latBounds = {-90.0,90.0}; // minimum,maxium latitude to use in coverage anaylsis
    double[] longBounds = {-180.0,180.0}; // minimum,maxium longitude to use in coverage anaylsis
    
    // in seconds
    double[][] coverageCumTime; // cumulative coverage time array [latPanels x longPanels]
    // in degrees
    double[] latPanelMidPoints; // middle point latitude of each division/panel
    double[] lonPanelMidPoints; // middle point longitude of each division/panel
    double[] latGridPoints;  // grid end points for latitude
    double[] lonGridPoints;  // grid end points for longitude 
    
    // default constructor
    public CoverageAnalyzer()
    {
        iniParamters();
        
        // debug testing fill in some points
        Random rnd = new Random(System.currentTimeMillis());
        for(int i=0; i<20; i++) // fill in 20
        {
            coverageCumTime[rnd.nextInt(latPanels)][rnd.nextInt(longPanels)] = 5.0;
        }
        
    } // constructor
    
    // initalized all parameters (used at class construction to create all arrays, etc)
    private void iniParamters()
    {
        // cumulative time create new array (default 0)
        coverageCumTime = new double[latPanels][longPanels];
        // mid points
        latPanelMidPoints = new double[latPanels];
        lonPanelMidPoints = new double[longPanels];
        // grid points
        latGridPoints = new double[latPanels+1];
        lonGridPoints = new double[longPanels+1];
        
        // calulate grid points, mid points
        for(int i=0;i<latPanels+1;i++)
        {
            latGridPoints[i] = i*(latBounds[1]-latBounds[0])/(latPanels)+latBounds[0];
            if(i>0)
            {
                latPanelMidPoints[i-1] = (latGridPoints[i]+latGridPoints[i-1])/2.0;
            }
        }
        for(int i=0;i<longPanels+1;i++)
        {
            lonGridPoints[i] = i*(longBounds[1]-longBounds[0])/(longPanels)+longBounds[0];
            if(i>0)
            {
                lonPanelMidPoints[i-1] = (lonGridPoints[i]+lonGridPoints[i-1])/2.0;
            }
        }
        
    }// iniParamters
    
    // test main function
    public static void main(String[] args)
    {
        CoverageAnalyzer ca = new CoverageAnalyzer();
    } // main
    
    //  update the time
    public void updateTime(final Time currentJulianDate)
    {
        
    } // updateTime
    
    // draw 2d
    public void draw2d(Graphics2D g2, J2dEarthLabel2 earthLabel, int totWidth, int totHeight, int imgWidth, int imgHeight, double zoomFac, double cLat, double cLong)
    {
        int[] xy = new int[2];
        int[] xy_old = new int[2];
        
        // draw in grid lines for lat and long of coverage area
        g2.setColor( new Color(0.0f,1.0f,0.0f,0.2f));
        for (double lat : latGridPoints)
        {
            xy = earthLabel.findXYfromLL(lat, longBounds[0],totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);
            xy_old = earthLabel.findXYfromLL(lat, longBounds[1],totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);

            g2.drawLine(xy_old[0], xy_old[1], xy[0], xy[1]); // draw a line across the map
        }
        g2.setColor( new Color(0.0f,1.0f,0.0f,0.2f));
        for (double lon : lonGridPoints)
        {
            xy = earthLabel.findXYfromLL(latBounds[0], lon,totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);
            xy_old = earthLabel.findXYfromLL(latBounds[1], lon,totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);

            g2.drawLine(xy_old[0], xy_old[1], xy[0], xy[1]); // draw a line across the map
        }
        // draw center points
        g2.setColor( new Color(0.0f,1.0f,0.0f,0.2f));
        int dotSize = 1;
        for(double lat : latPanelMidPoints)
        {
            for(double lon : lonPanelMidPoints)
            {
                xy = earthLabel.findXYfromLL(lat, lon,totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);
                
                g2.drawRect(xy[0]-dotSize/2, xy[1]-dotSize/2, dotSize, dotSize);
            }
        }
        // fill in color scaled panels based on cumulative Coverage time
        // should combine with above... just use alpha =0.0 when panel is blank
        int xmax, xmin, ymax, ymin;
        for(int i=0;i<latPanelMidPoints.length;i++)
        {
            for(int j=0;j<lonPanelMidPoints.length;j++)
            {
                xy = earthLabel.findXYfromLL(latGridPoints[i], lonGridPoints[j],totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);
                xy_old = earthLabel.findXYfromLL(latGridPoints[i+1], lonGridPoints[j+1],totWidth,totHeight,imgWidth,imgHeight,zoomFac,cLat,cLong);
                
                xmax = Math.max(xy[0], xy_old[0]);
                xmin = Math.min(xy[0], xy_old[0]);
                ymax = Math.max(xy[1], xy_old[1]);
                ymin = Math.min(xy[1], xy_old[1]);
                
                // color based on: coverageCumTime[i][j]
                // dummy way for now black or white
                if(coverageCumTime[i][j]>0)
                {
                    g2.setColor( new Color(0.0f,0.0f,1.0f,0.2f));
                }
                else
                {
                    g2.setColor( new Color(0.0f,0.0f,0.0f,0.0f));
                }
                
                g2.fillRect(xmin, ymin, xmax-xmin,ymax-ymin);//xy_old[0]-xy[0], xy_old[1]-xy[1]);
            }
        }
        
        
    } // draw 2d
    
    // draw 3d
    public void draw3d()
    {
        
    } // draw 3d
    
} // CoverageAnalyzer
