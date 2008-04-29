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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Hashtable;
import java.util.Vector;
import jsattrak.gui.J2dEarthLabel2;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import name.gano.astro.GeoFunctions;
import name.gano.astro.time.Time;

/**
 *
 * @author Shawn
 */
public class CoverageAnalyzer implements JSatTrakRenderable,JSatTrakTimeDependent
{
    int latPanels = 72;//36; //18// number of divisons along lines of latitude (grid points -1)
    int longPanels = 144;//72; //36 // number of divisions along lines of longitude (grid points -1)
    
    // in degrees
    double[] latBounds = {-90.0,90.0}; // minimum,maxium latitude to use in coverage anaylsis
    double[] longBounds = {-180.0,180.0}; // minimum,maxium longitude to use in coverage anaylsis
    
    // in days
    double[][] coverageCumTime; // cumulative coverage time array [latPanels x longPanels]
    // in degrees
    double[] latPanelMidPoints; // middle point latitude of each division/panel
    double[] lonPanelMidPoints; // middle point longitude of each division/panel
    double[] latGridPoints;  // grid end points for latitude
    double[] lonGridPoints;  // grid end points for longitude 
    
    // current maximum and minimum (NOT ZERO) values 
    double minNotZeroVal = 1;
    double maxVal = 100;
    
    ColorMap colorMap = new ColorMap();
    
    double lastMJD = -1; // last MJD update time
    
    // settings
    int alpha = 150;//151; // tranparency, 0=can't see it, 255=solid
    
    boolean dynamicUpdating = true; // if dynamic updating from GUI time stepping is enabled
    boolean plotCoverageGrid = false;
    boolean showColorBar = true;
    
    double elevationLimit = 15;//15; // elevation limit for ground coverage (must be higher for this to count as coverage
    
    Vector<String> satsUsedInCoverage = new Vector<String>(); // vector of satellites used in Coverage anaylsis
    
    NumberFormat formatter = new DecimalFormat("0.00E0");
    
    // default constructor
    public CoverageAnalyzer()
    {
        iniParamters();
        
        // debug testing fill in some points
//        Random rnd = new Random(System.currentTimeMillis());
//        for(int i=0; i<80; i++) // fill in random
//        {
//            coverageCumTime[rnd.nextInt(latPanels)][rnd.nextInt(longPanels)] = rnd.nextInt(99)+1;
//        }
//        // draw spectrum
//         for(int i=0; i<longPanels; i++) // fill in 20
//        {
//            coverageCumTime[latPanels-1][i] = i*2.7+1;
//            coverageCumTime[latPanels-3][i] = 100-i*2.7;
//        }
        
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
        
        // clear last mjd update
        lastMJD = -1;
        
    }// iniParamters
    
    // test main function
    public static void main(String[] args)
    {
        CoverageAnalyzer ca = new CoverageAnalyzer();
    } // main
    
    //  update the time
    public void updateTime(final Time currentJulianDate, final Hashtable<String,AbstractSatellite> satHash, final Hashtable<String,GroundStation> gsHash)
    {
        if(!dynamicUpdating)
        {
            return; // don't update converage anlysis from JSatTrack GUI
        }
        
        performCoverageAnalysis(currentJulianDate,satHash,gsHash); // do the analysis
        
    } // updateTime
    
    // internal function to actually perform the anaylsis - so it can be used by GUI update calls or coverage tool
    private void performCoverageAnalysis(final Time currentJulianDate, final Hashtable<String,AbstractSatellite> satHash, final Hashtable<String,GroundStation> gsHash)
    {
        // if first time update, save time and quit (only start calc after first time step)
        if(lastMJD == -1)
        {
            lastMJD = currentJulianDate.getMJD();
            return;
        }
        
        // check time make sure this time is past when the last time update was 
        if(currentJulianDate.getMJD() <= lastMJD)
        {
            return; // do nothing as this time is later
        }
        // calc time diff, and save time
        double timeDiffDays = currentJulianDate.getMJD() - lastMJD;
        lastMJD = currentJulianDate.getMJD();
        
        // create temp array for time cumlation (so we don't double count sat coverage)
        // each panel either has access or it doesn't for the current time step -- boolean 
        boolean[][] tempAcessArray = new boolean[latPanels][longPanels];
        
        // === do coverage anaylsis, for each satellite ===
        for(String satName : satsUsedInCoverage)
        {
            // get sat Object
            AbstractSatellite currentSat = satHash.get(satName);
            
            // check to see if satellite is in lat/long AOI coverage box
            if(currentSat.getLatitude()*180/Math.PI  >= latBounds[0]  && 
               currentSat.getLatitude()*180/Math.PI  <= latBounds[1]  &&
               currentSat.getLongitude()*180/Math.PI >= longBounds[0] &&
               currentSat.getLongitude()*180/Math.PI <= longBounds[1]    )
            {
                
                // find closest panel under satellite and the index of that panel
                double latPercentile = (currentSat.getLatitude()*180/Math.PI-latBounds[0]) / (latBounds[1]-latBounds[0]);
                int latIndex = (int)Math.floor(latPercentile*latPanels);
                double longPercentile = (currentSat.getLongitude()*180/Math.PI-longBounds[0]) / (longBounds[1]-longBounds[0]);
                int longIndex = (int)Math.floor(longPercentile*longPanels);
                
                // Coverage assumes sat doesn't have a shaped sensor and it can look straight down (nadir)
                // debug for now mark point as access added
                double[] aer = new double[3];
//                aer = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(), 
//                        new double[]{latPanelMidPoints[latIndex],lonPanelMidPoints[longIndex],0},  // sea level
//                        currentSat.getPosMOD());
//                
//                // see if sat sub-point panel has access (otherwise nothing does)
//                if(aer[1] >= elevationLimit)
//                {
//                    tempAcessArray[latIndex][longIndex] = true; // can't assume access here!
//                }
                
                // Search up=====================================================
                // search upwards until no access (careful of lat >90)
                int i = latIndex; // includes satellite sub point
                double tempElevation2=0; // used in searching to the left and right
                do
                {
                    // take care of when i >= latPanels (reflection for longitude index and make lat go down instead of up (and stay at top one iter)
                    
                    aer = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(), 
                        new double[]{latPanelMidPoints[i],lonPanelMidPoints[longIndex],0},  // sea level
                        currentSat.getPosMOD());
                    
                    if(aer[1] >= elevationLimit)
                    {
                        tempAcessArray[i][longIndex] = true;
                        // search to the left =============================
                        int j=longIndex-1;
                        int longSearchCount = 0;
                        int jWrappedIndex = j; // updated so seach can wrap around map
                        do
                        {
                            // take car of i and j with wrap arounds
                            if(j < 0)
                            {
                                jWrappedIndex = longPanels + j;
                            }
                            else if(j >= longPanels)
                            {
                                jWrappedIndex = j - longPanels;
                            }
                            else
                            {
                                jWrappedIndex = j;
                            }
                            
                            tempElevation2 = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(), 
                                new double[]{latPanelMidPoints[i],lonPanelMidPoints[jWrappedIndex],0},  // sea level
                                currentSat.getPosMOD())[1];
                            if(tempElevation2 >= elevationLimit)
                            {
                                tempAcessArray[i][jWrappedIndex] = true;
                            }
                            
                            j--;
                            longSearchCount++; // make sure we don't get stuck on seaching a row over and over
                        }while(tempElevation2 >= elevationLimit && longSearchCount < longPanels); 
                        // search to the left =============================
                        // search to the Right =============================
                        j=longIndex+1;
                        longSearchCount = 0;
                        jWrappedIndex = j; // updated so seach can wrap around map
                        do
                        {
                            // take car of i and j with wrap arounds
                            if(j < 0)
                            {
                                jWrappedIndex = longPanels + j;
                            }
                            else if(j >= longPanels)
                            {
                                jWrappedIndex = j - longPanels;
                            }
                            else
                            {
                                jWrappedIndex = j;
                            }
                            
                            tempElevation2 = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(), 
                                new double[]{latPanelMidPoints[i],lonPanelMidPoints[jWrappedIndex],0},  // sea level
                                currentSat.getPosMOD())[1];
                            if(tempElevation2 >= elevationLimit)
                            {
                                tempAcessArray[i][jWrappedIndex] = true;
                            }
                            
                            j++;
                            longSearchCount++; // make sure we don't get stuck on seaching a row over and over
                        }while(tempElevation2 >= elevationLimit && longSearchCount < longPanels); 
                        // search to the Right =============================
                        
                    }
                    
                    i++;
                }while(aer[1] >= elevationLimit && i < latPanels); // do while - only search up to top of panel
                // Search up=====================================================
                // Search down=====================================================
                // search down until no access (careful of lat >90)
                i = latIndex - 1; // includes satellite sub point
                tempElevation2 = 0; // used in searching to the left and right
                if (i >= 0) // avoid searching down if i is already 0
                {
                    do
                    {
                        // take care of when i >= latPanels (reflection for longitude index and make lat go down instead of up (and stay at top one iter)
                        aer = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(),
                                new double[]
                                {
                                    latPanelMidPoints[i], lonPanelMidPoints[longIndex], 0
                                }, // sea level
                                currentSat.getPosMOD());

                        if (aer[1] >= elevationLimit)
                        {
                            tempAcessArray[i][longIndex] = true;
                            // search to the left =============================
                            int j = longIndex - 1;
                            int longSearchCount = 0;
                            int jWrappedIndex = j; // updated so seach can wrap around map

                            do
                            {
                                // take car of i and j with wrap arounds
                                if (j < 0)
                                {
                                    jWrappedIndex = longPanels + j;
                                }
                                else if (j >= longPanels)
                                {
                                    jWrappedIndex = j - longPanels;
                                }
                                else
                                {
                                    jWrappedIndex = j;
                                }

                                tempElevation2 = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(),
                                        new double[] {latPanelMidPoints[i], lonPanelMidPoints[jWrappedIndex], 0}, // sea level
                                        currentSat.getPosMOD())[1];
                                if (tempElevation2 >= elevationLimit)
                                {
                                    tempAcessArray[i][jWrappedIndex] = true;
                                }

                                j--;
                                longSearchCount++; // make sure we don't get stuck on seaching a row over and over
                            }while (tempElevation2 >= elevationLimit && longSearchCount < longPanels);
                            // search to the left =============================
                            // search to the Right =============================
                            j = longIndex + 1;
                            longSearchCount = 0;
                            jWrappedIndex = j; // updated so seach can wrap around map
                            do
                            {
                                // take car of i and j with wrap arounds
                                if (j < 0)
                                {
                                    jWrappedIndex = longPanels + j;
                                }
                                else if (j >= longPanels)
                                {
                                    jWrappedIndex = j - longPanels;
                                }
                                else
                                {
                                    jWrappedIndex = j;
                                }
                                
                                tempElevation2 = GeoFunctions.calculate_AER(currentJulianDate.getJulianDate(),
                                        new double[]{latPanelMidPoints[i], lonPanelMidPoints[jWrappedIndex], 0}, // sea level
                                        currentSat.getPosMOD())[1];
                                if (tempElevation2 >= elevationLimit)
                                {
                                    tempAcessArray[i][jWrappedIndex] = true;
                                }

                                j++;
                                longSearchCount++; // make sure we don't get stuck on seaching a row over and over
                            }
                            while (tempElevation2 >= elevationLimit && longSearchCount < longPanels);
                        // search to the Right =============================

                        } // if in elecation limit

                        i--;
                    }while (aer[1] >= elevationLimit && i >= 0); // do while
                } // if already at 0 no need to search down
                // Search down=====================================================
                
                
            }// sat is in coverage AOI           
        } // for each satellite - Coverage anaylsis
        
        // merge temp and timecumarray // and update max and min values
        minNotZeroVal = Double.MAX_VALUE; // really high to start
        maxVal = -1; // really low to start
        for(int i=0;i<latPanels;i++) 
        {
            for(int j=0;j<longPanels;j++)
            {
                // DEBUG CLEAR VALUE SO ONLY POINTS CURRENTLY IN VIEW SHOW UP
                //coverageCumTime[i][j] = 0;
                
                if(tempAcessArray[i][j])
                {
                    coverageCumTime[i][j] += timeDiffDays;
                } // if access at this point
                
                // update max and min
                if(coverageCumTime[i][j] > maxVal)
                {
                    maxVal = coverageCumTime[i][j];
                }
                if(coverageCumTime[i][j] < minNotZeroVal && coverageCumTime[i][j] > 0)
                {
                   minNotZeroVal =  coverageCumTime[i][j];
                }
                
            } // long panels (j)
        } // lat panels (i) (merge data)
        

        
    } // performCoverageAnalysis
    
    // draw 2d
    public void draw2d(Graphics2D g2, J2dEarthLabel2 earthLabel, int totWidth, int totHeight, int imgWidth, int imgHeight, double zoomFac, double cLat, double cLong)
    {
        int[] xy = new int[2];
        int[] xy_old = new int[2];
        
        // draw in grid lines for lat and long of coverage area
        if (plotCoverageGrid)
        {
            g2.setColor(new Color(0.0f, 1.0f, 0.0f, 0.2f));
            for (double lat : latGridPoints)
            {
                xy = earthLabel.findXYfromLL(lat, longBounds[0], totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);
                xy_old = earthLabel.findXYfromLL(lat, longBounds[1], totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);

                g2.drawLine(xy_old[0], xy_old[1], xy[0], xy[1]); // draw a line across the map

            }
            g2.setColor(new Color(0.0f, 1.0f, 0.0f, 0.2f));
            for (double lon : lonGridPoints)
            {
                xy = earthLabel.findXYfromLL(latBounds[0], lon, totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);
                xy_old = earthLabel.findXYfromLL(latBounds[1], lon, totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);

                g2.drawLine(xy_old[0], xy_old[1], xy[0], xy[1]); // draw a line across the map

            }
            // draw center points
            g2.setColor(new Color(0.0f, 1.0f, 0.0f, 0.2f));
            int dotSize = 1;
            for (double lat : latPanelMidPoints)
            {
                for (double lon : lonPanelMidPoints)
                {
                    xy = earthLabel.findXYfromLL(lat, lon, totWidth, totHeight, imgWidth, imgHeight, zoomFac, cLat, cLong);

                    g2.drawRect(xy[0] - dotSize / 2, xy[1] - dotSize / 2, dotSize, dotSize);
                }
            }
        } // graw grid and center points
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
                    g2.setColor( colorMap.getColor(coverageCumTime[i][j], minNotZeroVal, maxVal, alpha) );
                    g2.fillRect(xmin, ymin, xmax-xmin,ymax-ymin);//xy_old[0]-xy[0], xy_old[1]-xy[1]);
                }
                else
                {
                    // don't draw anything
                }
                
            } // for lon panels
        } // for lat panels
        
        // Draw color bar if wanted!!
        if(showColorBar)
        {
            int pixelsFromBottom = 20;
            int pixelsFromLeft = 20;
            int colorBarLen = 100;
            int colorBarHeight = 10;
            int colorBarTextSpacing = 5; // pixels below bar where text is displayed
            
            // color bar specturm
            for(int i=0;i<colorBarLen;i++)
            {
                g2.setColor( colorMap.getColor(i, 0, colorBarLen, 255) );
                g2.drawLine(pixelsFromLeft+i, totHeight-pixelsFromBottom, pixelsFromLeft+i, totHeight-pixelsFromBottom-colorBarHeight);
            }
            
            // color bar labeling
            // 0 %
            int textHeight = 10;
            g2.setColor( Color.BLACK );
            g2.drawLine(pixelsFromLeft-1, totHeight-pixelsFromBottom+colorBarTextSpacing, pixelsFromLeft-1,totHeight-pixelsFromBottom-colorBarHeight);
            g2.drawString(formatter.format(minNotZeroVal*24*60*60) + " sec", pixelsFromLeft-1, totHeight-pixelsFromBottom+colorBarTextSpacing+textHeight);
            
            // at 100%
            g2.setColor( Color.BLACK );
            g2.drawLine(pixelsFromLeft+colorBarLen, totHeight-pixelsFromBottom+colorBarTextSpacing, pixelsFromLeft+colorBarLen,totHeight-pixelsFromBottom-colorBarHeight);
            g2.drawString(formatter.format(maxVal*24*60*60), pixelsFromLeft+colorBarLen, totHeight-pixelsFromBottom+colorBarTextSpacing+textHeight);
            
        } // showColorBar
        
    } // draw 2d
    
    // draw 3d
    public void draw3d()
    {
        
    } // draw 3d
    
    // Settings ==================================
    
    public void addSatToCoverageAnaylsis(String satName)
    {
        // first check to make sure sat isn't already in list
        for(String name : satsUsedInCoverage)
        {
            if(satName.equalsIgnoreCase(name))
            {
                return; // already in the list
            }
        }
        
        satsUsedInCoverage.add(satName);
    } // addSatToCoverageAnaylsis
    
    public void clearSatCoverageVector()
    {
        satsUsedInCoverage.clear();
    }
    
    public void removeSatFromCoverageAnaylsis(String satName)
    {
        // make sure name is in the Vector
        int i=0; // counter
        for(String name : satsUsedInCoverage)
        {
            if(satName.equalsIgnoreCase(name))
            {
                satsUsedInCoverage.remove(i);
                return; // already in the list
            }
            i++;
        }
    } // removeSatFromCoverageAnaylsis
    
} // CoverageAnalyzer
