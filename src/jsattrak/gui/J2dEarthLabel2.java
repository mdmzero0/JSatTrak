/**
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

package jsattrak.gui;


import jsattrak.objects.GroundStation;
import java.awt.*;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.*;
import jsattrak.coverage.JSatTrakRenderable;
import jsattrak.objects.AbstractSatellite;
import name.gano.astro.AstroConst;
import name.gano.astro.bodies.Sun;
import name.gano.astro.time.Time;

public class J2dEarthLabel2 extends JLabel  implements java.io.Serializable
{
    // options -------- (look in Panel too)    
    private boolean showDateTime = true;
    private int xDateTimeOffset = 10; // pixles from bottom/left
    private int yDateTimeOffset = 3; 
    private Color dateTimeColor = Color.BLUE;
    
    private double aspectRatio = 2.0;// aspect ratio of the image in the frame (should equal value in J2EarthPanel)
    private int imageWidth = 0, imageHeight = 0;
    private int lastTotalWidth = 0, lastTotalHeight = 0;
    
    public boolean showLatLong = true;
    
    // hastable of all the satellites currently processing
    private transient Hashtable<String,AbstractSatellite> satHash = new Hashtable<String,AbstractSatellite>();
    private transient Hashtable<String,GroundStation> gsHash;
    
    // rendering hints
    private transient RenderingHints renderHints;
    
    // zooming data
    private double centerLat = 0; // center latitude
    private double centerLong = 0; // center longitude
    private double zoomFactor = 1.0; // current zoom factor (>= 1.0)
    private double zoomIncrementMultiplier = 2.0;  // how much to zoom at a time, e.g., 2= zoom in a factor of 2
    
    private Color backgroundColor; 
    
    // sun drawing options
    private boolean drawSun = true;
    private Color sunColor = Color.DARK_GRAY;//Color.BLACK;
    private int numPtsSunFootPrint = 101;
    private float sunAlpha = 0.55f;
    
    // sun
    Sun sun;
    
    // vector of renderable objects to be drawn - SEG 26-April-2008
    Vector<JSatTrakRenderable> renderableObjects = new Vector<JSatTrakRenderable>();
    

    private transient Time currentTime; // object storing the current time
    
    public J2dEarthLabel2(ImageIcon image, double aspect, Hashtable<String,AbstractSatellite> satHash, Hashtable<String,GroundStation> gsHash, Color backgroundColor, Time currentTime, Sun sun)
    {
        //super(image);
        aspectRatio = aspect;
        this.satHash = satHash;
        this.gsHash = gsHash;
        this.sun = sun;
        
        // create rendering options -- anti-aliasing
        renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        // high quality drawing
        //renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // for image scaling
        //renderHints.put(RenderingHints.KEY_INTERPOLATION ,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // set background color:
        this.backgroundColor = backgroundColor; 
        
        this.currentTime = currentTime;
        
//        // TEMP try out the LandMassRegions -- move this to top class, so each map doesn't have their own? or leave it this way so each can be customized
//        if(showLandMassOutlines)
//        {
//            landMass = new LandMassRegions();
//        }
        
    }
    
    // used in case it needs to be updated
    public void setSatHashTable(Hashtable<String,AbstractSatellite> satHash)
    {
         this.satHash = satHash;
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);  // repaints what is already on the buffer
        
        Graphics2D g2 = (Graphics2D)g; // cast to a @D graphics object
        Dimension dim = getSize();
        int w = dim.width;
        int h = dim.height;
        
        // save last width/height
        lastTotalWidth = w;
        lastTotalHeight = h;
        
        // sent rendering options
        //g2.setRenderingHints(Graphics2D.ANTIALIASING,Graphics2D.ANTIALIAS_ON);
        g2.setRenderingHints(renderHints);
        
        //g2.drawLine(0,0,w,h); // draw a line across the map
        
        // vars
        int[] xy = new int[2];
        int[] xy_old = new int[2];
        
        
        // draw renderable objects first
        for(JSatTrakRenderable renderable : renderableObjects)
        {
            renderable.draw2d(g2, this, w, h, imageWidth, imageHeight, zoomFactor, centerLat, centerLong);
        }
        
        // draw sun if desired
        // draw the foot print
        if(drawSun)
        {
            // -- bad this is just ecef -- cool to see equinixes though
            //double[] lla = ecef2lla(sun.getOpositeSunPosition());
            // calculate Lat,Long,Alt 
            //double[] lla = GeoFunctions.GeodeticJulDate( sun.getOpositeSunPositionMOD() ,currentTime.getJulianDate());
            double[] lla = sun.getCurrentDarkLLA();
            drawFootPrint(g2, lla[0], lla[1], lla[2], true, sunColor, sunColor, sunAlpha, numPtsSunFootPrint ); // draw footprint
            //System.out.println("Sun -x = " + sun.getOpositeSunPosition()[0]);
        }
        
        
        
        // draw lat long
        g2.setPaint(Color.gray);
        // dashed line, 2 pix on 2 pix off
        float[] dashPattern = { 2, 2, 2, 2 };
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10,
                dashPattern, 0));
        if(showLatLong)
        {
            for(int lat = -90; lat <= 90; lat+=30)
            {
                xy = findXYfromLL(lat, -180, w, h, imageWidth, imageHeight);
                xy_old = findXYfromLL(lat, 180, w, h, imageWidth, imageHeight);
                
                g2.drawLine(xy_old[0],xy_old[1],xy[0],xy[1]); // draw a line across the map
            }
            
            for(int lon = -180; lon <= 180; lon+=30)
            {
                xy = findXYfromLL(90, lon, w, h, imageWidth, imageHeight);
                xy_old = findXYfromLL(-90, lon, w, h, imageWidth, imageHeight);
                
                g2.drawLine(xy_old[0],xy_old[1],xy[0],xy[1]); // draw a line across the map
            }
            
        }
        
        g2.setStroke(new BasicStroke());  // standard line, no fancy stuff
        
        // Draw Ground Stations
        for(GroundStation gs : gsHash.values() ) // search through all sat nodes
        {
            
            if(gs.isShow2D())
            {
                g2.setPaint(gs.getStationColor());

                int[] gs_xy = findXYfromLL(gs.getLatitude(), gs.getLongitude(), w, h, imageWidth, imageHeight);

                int sz = gs.getGroundStation2DPixelSize();

                g2.fillRect(gs_xy[0] - sz / 2, gs_xy[1] - sz / 2, sz, sz);
                
                if(gs.isShow2DName())
                {
                    // show name?
                    g2.drawString(gs.getStationName().trim(),gs_xy[0]+sz/2+3,gs_xy[1]+4);
                    
                } // if show name

            } // if show GS
            
        } // for each Ground Station
        
        
        // paint Groundtracks of all the SATS that have LLA Stored -----
        double[] LLA = new double[2];
        double[] LLA_old = new double[2];
        
        Double nanDbl = new Double(Double.NaN);
                
        for(AbstractSatellite sat : satHash.values() ) // search through all sat nodes
        {
            if( sat.getShowGroundTrack() && sat.getGroundTrackIni() && sat.getPlot2D())
            {                
                //System.out.println("Plotting node:"+i);
                // okay plot LLA's of the satellite
                g2.setPaint( sat.getSatColor() );
                
                ///// LEAD track:
                // first Lead point
                if(sat.getNumGroundTrackLeadPts() > 0)
                {
                    LLA = sat.getGroundTrackLlaLeadPt(0);
                    
                    xy_old = findXYfromLL(LLA[0]*180.0/Math.PI, LLA[1]*180.0/Math.PI, w, h, imageWidth, imageHeight);
                    LLA_old[0] = LLA[0];
                    LLA_old[1] = LLA[1];
                    //LLA_old[2] = LLA[2];
                    
                    for(int j=1;j<sat.getNumGroundTrackLeadPts();j++)
                    {
                        LLA = sat.getGroundTrackLlaLeadPt(j);
                        
                        xy = findXYfromLL(LLA[0]*180.0/Math.PI, LLA[1]*180.0/Math.PI, w, h, imageWidth, imageHeight);
                        
                        // check to see if Longitude flipped sign But not near origin
                        //if( (LLA[1] > 0 && LLA_old[1] > 0) || (LLA[1] < 0 && LLA_old[1] < 0) || Math.abs(LLA[1]) < 1.0  )
                        if ( !(nanDbl.equals(LLA_old[0]) || nanDbl.equals(LLA[0]))) // make sure they are not NAN (not in time)
                        {
                            if (Math.abs(LLA[1] - LLA_old[1]) < 4.0)
                            {

                                g2.drawLine(xy_old[0], xy_old[1], xy[0], xy[1]); // draw a line across the map
                            //System.out.println("xy: (" + xy_old[0] +"," +xy_old[1] +"),(" +xy[0] +"," +xy[1]+"), j=" + j + "LLA_old,new:" +LLA_old[1] + ", " +LLA[1] ); // draw a line across the map
                            }
                            else
                            {
                                // draw this line segment next time, jump from side to side
                                double newLat = linearInterpDiscontLat(LLA_old[0], LLA_old[1], LLA[0], LLA[1]);

                                // get xy points for both the old and new side (positive and negative long)
                                int[] xyMid_pos = findXYfromLL(newLat * 180.0 / Math.PI, 180.0, w, h, imageWidth, imageHeight);
                                int[] xyMid_neg = findXYfromLL(newLat * 180.0 / Math.PI, -180.0, w, h, imageWidth, imageHeight);

                                // draw 2 lines - one for each side of the date line
                                if (LLA_old[1] > 0) // then the old one is on the positive side
                                {
                                    g2.drawLine(xy_old[0], xy_old[1], xyMid_pos[0], xyMid_pos[1]);
                                    g2.drawLine(xy[0], xy[1], xyMid_neg[0], xyMid_neg[1]);
                                }
                                else // the new one is on the positive side
                                {
                                    g2.drawLine(xy[0], xy[1], xyMid_pos[0], xyMid_pos[1]);
                                    g2.drawLine(xy_old[0], xy_old[1], xyMid_neg[0], xyMid_neg[1]);
                                }


                            } // jump in footprint
                        } // NaN check
                        
                        
                        xy_old[0]=xy[0];
                        xy_old[1]=xy[1];
                        LLA_old[0] = LLA[0];
                        LLA_old[1] = LLA[1];
                        //LLA_old[2] = LLA[2];
                    } // lead track drawing
                }// lead track
                
                 ///// Lag track:
                // first Lag point
                if(sat.getNumGroundTrackLagPts() > 0)
                {
                    LLA = sat.getGroundTrackLlaLagPt(0);
                    
                    xy_old = findXYfromLL(LLA[0]*180.0/Math.PI, LLA[1]*180.0/Math.PI, w, h, imageWidth, imageHeight);
                    LLA_old[0] = LLA[0];
                    LLA_old[1] = LLA[1];
                    //LLA_old[2] = LLA[2];
                    
                    for(int j=1;j<sat.getNumGroundTrackLagPts();j++)
                    {
                        LLA = sat.getGroundTrackLlaLagPt(j);
                        
                        xy = findXYfromLL(LLA[0]*180.0/Math.PI, LLA[1]*180.0/Math.PI, w, h, imageWidth, imageHeight);
                        
                        // check to see if Longitude flipped sign But not near origin
                        //if( (LLA[1] > 0 && LLA_old[1] > 0) || (LLA[1] < 0 && LLA_old[1] < 0) || Math.abs(LLA[1]) < 1.0  ) 
                        if ( !(nanDbl.equals(LLA_old[0]) || nanDbl.equals(LLA[0]))) // make sure they are not NAN (not in time)
                        {
                            if (Math.abs(LLA[1] - LLA_old[1]) < 4.0)
                            {

                                g2.drawLine(xy_old[0], xy_old[1], xy[0], xy[1]); // draw a line across the map
                            //System.out.println("xy: (" + xy_old[0] +"," +xy_old[1] +"),(" +xy[0] +"," +xy[1]+"), j=" + j + "LLA_old,new:" +LLA_old[1] + ", " +LLA[1] ); // draw a line across the map
                            }
                            else
                            {
                                // draw this line segment next time, jump from side to side
                                double newLat = linearInterpDiscontLat(LLA_old[0], LLA_old[1], LLA[0], LLA[1]);

                                // get xy points for both the old and new side (positive and negative long)
                                int[] xyMid_pos = findXYfromLL(newLat * 180.0 / Math.PI, 180.0, w, h, imageWidth, imageHeight);   
                                int[] xyMid_neg = findXYfromLL(newLat * 180.0 / Math.PI, -180.0, w, h, imageWidth, imageHeight);
                                

                                // draw 2 lines - one for each side of the date line
                                if (LLA_old[1] > 0) // then the old one is on the positive side
                                {
                                    g2.drawLine(xy_old[0], xy_old[1], xyMid_pos[0], xyMid_pos[1]);
                                    g2.drawLine(xy[0], xy[1], xyMid_neg[0], xyMid_neg[1]);
                                //System.out.println("here2");
                                }
                                else // the new one is on the positive side
                                {
                                    g2.drawLine(xy[0], xy[1], xyMid_pos[0], xyMid_pos[1]);
                                    g2.drawLine(xy_old[0], xy_old[1], xyMid_neg[0], xyMid_neg[1]);
                                //System.out.println("here3");
                                }
                            }
                        } // NaN check
                        
                        
                        xy_old[0]=xy[0];
                        xy_old[1]=xy[1];
                        LLA_old[0] = LLA[0];
                        LLA_old[1] = LLA[1];
                        //LLA_old[2] = LLA[2];
                    } // lag track drawing
                } // lag track
                
                
            }
        } // ground tracks
        
        // draw current positions
        for(AbstractSatellite sat : satHash.values() ) // search through all sat nodes
        {
            if( sat.getPlot2D() ) // if option to plot is on
            {
                // draw a small circle around where the current position of the sat is
                
                g2.setPaint( sat.getSatColor() );
                
                double lat = sat.getLatitude();
                double lon = sat.getLongitude();
                
                //System.out.println("Lat/Lon =" + lat*180.0/Math.PI + "/" + lon*180.0/Math.PI);
                
                // find the drawing location
                xy = findXYfromLL(lat*180.0/Math.PI, lon*180.0/Math.PI, w, h, imageWidth, imageHeight);
                
                // drawOval
                g2.fillOval(xy[0]-5,xy[1]-5,10,10); // not sure what the arguments are?
                
                // add name if required
                if(sat.isShowName2D())
                {
                    g2.drawString(sat.getName().trim(),xy[0]+10,xy[1]+3);
                }
                
                
            }
        } // draw current positions
        
                        
        for( AbstractSatellite sat : satHash.values() ) // search through all sat nodes
        {
            if( sat.getPlot2D() && sat.getPlot2DFootPrint() )
            {
                // draw footprint, if desired
                
                // get info about sat
                double lat =  sat.getLatitude();
                double lon = sat.getLongitude();
                double alt =  sat.getAltitude();
                
                // draw the foot print
                drawFootPrint(g2, lat, lon, alt, sat.isFillFootPrint(), sat.getSatColor(), sat.getSatColor(), 0.2f, sat.getNumPtsFootPrint() ); // draw footprint
                //Graphics2D g2, double lat, double lon, double alt, boolean fillFootPrint, Color outlineColor, Color FillColor, alpha(0.2f) int numPtsFootPrint
                
            } // if sat
        } // sat footprings
        
        if(zoomFactor > 1.0001)
        {
            
            g2.setPaint( backgroundColor ); // set paint color like background
            
            // we need to overpaint excess lines drawn outside map area
            if(getWidth() / getHeight() > aspectRatio)
            {                
                // draw in ends
                g2.fillRect(0,0,(int)((getWidth()-imageWidth)/2.0),getHeight());
                g2.fillRect((int)((getWidth()-imageWidth)/2.0 + imageWidth),0,(int)((getWidth()-imageWidth)/2.0),getHeight());
            }
            else
            {
                // draw on top/bottom
                g2.fillRect(0,0,getWidth(),(int)((getHeight()-imageHeight)/2.0));
                g2.fillRect(0,(int)((getHeight()-imageHeight)/2.0+imageHeight),getWidth(),(int)((getHeight()-imageHeight)/2.0));
            }
            
        } // zoom factor>1
        
        
        // show time and date
        if(showDateTime)
        {
            //xDateTimeOffset;
            g2.setPaint( dateTimeColor );
            g2.drawString( currentTime.getDateTimeStr() ,(int)((getWidth()-imageWidth)/2.0)+xDateTimeOffset,(int)((getHeight()-imageHeight)/2.0+imageHeight-yDateTimeOffset));
            
        } // show time and date
        
    } //paintComponent
    
    // function to find the Linearly interpolated latitude at long = +/- 180
    // this is used to correct discontinutities in the plots
    // return double = latitude for the longitude of +/- 180 (both)
    private double linearInterpDiscontLat(double lat1, double long1, double lat2, double long2)
    {
        // one longitude should be negative one positive, make them both positive
        if(long1 > long2)
        {
            long2 += 2*Math.PI; // in radians
        }
        else
        {
            long1 += 2*Math.PI;
        }
        
        return  ( lat1+(Math.PI - long1)*(lat2-lat1)/(long2-long1) );
    }
    
    // function to convert earth-centered earth-fixed (ECEF) cartesian coordinates to Lat, Long, Alt
    // DOES NOT INCLUDE UPDATES FOR time
    public double[] ecef2lla(double[] pos) // d is current MDT time
    {
        double[] lla = new double[3];
        
        // WGS84 ellipsoid constants:
        double a = 6378137;
        double e = 8.1819190842622e-2; // 0;%8.1819190842622e-2/a;%8.1819190842622e-2;  % 0.003352810664747
        
        double b = Math.sqrt(Math.pow(a,2.0)*(1-Math.pow(e,2)));
        double ep = Math.sqrt( (Math.pow(a,2.0)-Math.pow(b,2.0))/Math.pow(b,2.0));
        double p   = Math.sqrt(Math.pow(pos[0],2.0)+Math.pow(pos[1],2.0));
        double th  = Math.atan2(a*pos[2],b*p);
        lla[1] = Math.atan2(pos[1],pos[0]);
        lla[0] = Math.atan2((pos[2]+Math.pow(ep,2.0)*b*Math.pow(Math.sin(th),3.0)),(p-Math.pow(e,2.0)*a*Math.pow(Math.cos(th),3.0)));
        double N   = a/Math.sqrt(1-Math.pow(e,2.0)*Math.pow(Math.sin(lla[0]),2.0));
        lla[2] = p/Math.cos(lla[0])-N;
        
        
        if(lla[1] < 0)
        {
            lla[1] = 2.0*Math.PI + lla[1];
        }
        
        // return lon in range [0,2*pi)
        lla[1] = lla[1] % (2.0*Math.PI); // modulus
        
        // correct for numerical instability in altitude near exact poles:
        // (after this correction, error is about 2 millimeters, which is about
        // the same as the numerical precision of the overall function)
        
        if(Math.abs(pos[0])<1.0 & Math.abs(pos[1])<1.0)
        {
            lla[2] = Math.abs(pos[2])-b;
        }
        
        // now scale longitude from [0,360] -> [-180,180]
        if(lla[1] > Math.PI) // > 180
        {
            lla[1] = lla[1] - 2.0*Math.PI;
        }
/*
                // now correct for time shift
                // account for earth rotations
                lla[1] = lla[1]-(280.4606 +360.9856473*d)*Math.PI/180.0;
 
                // correction ??
                //lla[1] = lla[1]-Math.PI/2.0;
 
                // now insure [-180,180] range
                double div = Math.floor(lla[1]/(2*Math.PI));
                lla[1] = lla[1] - div*2*Math.PI;
                if(lla[1] > Math.PI)
                {
                        lla[1] = lla[1]- 2.0*Math.PI;
                }
 */
        
        return lla;
        
    } // ecef2lla
    
    public void setImageWidth(int w)
    {
        imageWidth = w;
    }
    
    public void setImageHeight(int h)
    {
        imageHeight = h;
    }
    
    // assumes local zoom factor centered centerLat, centerLong
    public int[] findXYfromLL(double lat, double lon, int totWidth, int totHeight, int imgWidth, int imgHeight)
    {
        return findXYfromLL(lat, lon, totWidth, totHeight, imgWidth, imgHeight, zoomFactor, centerLat, centerLong);
    }
    
    // includs calculations for zoom Facter, central lat and central longitude- input degrees
    public int[] findXYfromLL(double lat, double lon, int totWidth, int totHeight, int imgWidth, int imgHeight, double zoomFac, double cLat, double cLong)
    {
        int[] xy = new int[2];
        
        // scale to take into account zoom factor
        //totWidth = (int)Math.round( totWidth*zoomFac );
        //totHeight = (int)Math.round( totHeight*zoomFac );
        
        double longSpan = 360.0/zoomFac; // span of longitude
        double latSpan = 180.0/zoomFac;
        
        int midX = (int) totWidth/2;
        int midY = (int) totHeight/2;
        
        int leftX = midX - imgWidth/2;
        int rightX = midX + imgWidth/2;
        
        int topY = midY - imgHeight/2;
        int botY = midY + imgHeight/2;
        
        xy[0] = (int)( ((rightX-leftX)/longSpan)*(lon-cLong) + (rightX+leftX)/2.0 );
        xy[1] = (int)( ((topY-botY)/latSpan)*(lat-cLat) + (topY+botY)/2.0 );
        
        return xy;
    }
    
    // assumes local zoom factor centered centerLat, centerLong
    public double[] findLLfromXY(int x, int y, int totWidth, int totHeight, int imgWidth, int  imgHeight)
    {
        return findLLfromXY(x, y, totWidth, totHeight, imgWidth,  imgHeight, zoomFactor, centerLat, centerLong);
    }
    
    // find LL from xy position - used for clicking on map: - returns in degrees
    public double[] findLLfromXY(int x, int y, int totWidth, int totHeight, int imgWidth, int  imgHeight, double zoomFac, double cLat, double cLong)
    {
        double[] ll = new double[2]; // lat and longitude 
        
        double longSpan = 360.0/zoomFac; // span of longitude
        double latSpan = 180.0/zoomFac;
        
        int midX = (int) totWidth/2;
        int midY = (int) totHeight/2;
        
        int leftX = midX - imgWidth/2;
        int rightX = midX + imgWidth/2;
        
        int topY = midY - imgHeight/2;
        int botY = midY + imgHeight/2;
        
        ll[1] = (x - (rightX+leftX)/2.0) /  ((rightX-leftX)/longSpan) + cLong; // longitude
        ll[0] = (y - (topY+botY)/2.0) / ((topY-botY)/latSpan) + cLat; // latitude
        
        // do range tests
        if(ll[0] > 90)
            ll[0] = 90;
        else if(ll[0] < -90)
            ll[0] = -90;
        
        if(ll[1] > 180)
            ll[1] = 180;
        else if(ll[1] < -180)
            ll[1] = -180;
        
        return ll; 
    }
    

    
    // math utilities
    // multiply two matrices 3x3
    public static double[][] mult(double[][] a, double[][] b)
    {
        double[][] c = new double[3][3];
        
        for (int i = 0; i < 3; i++) // row
        {
            for (int j = 0; j < 3; j++) // col
            {
                c[i][j] = 0.0;
                for (int k = 0; k < 3; k++)
                {
                    c[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        
        return c;
        
    } // mult 3x3 matrices
    
    // multiply matrix 3x3 by vector 3x1
    public static double[] mult(double[][] a, double[] b)
    {
        double[] c = new double[3];
        
        for (int i = 0; i < 3; i++) // row
        {
            c[i] = 0.0;
            for (int k = 0; k < 3; k++)
            {
                c[i] += a[i][k] * b[k];
            }
        }
        
        return c;
        
    } // mult 3x3 matrices
    
    
// ==============================================================================================
///  FUNCTION TO DRAW A FOOT PRINT ==============================================================
     // alpha = 0-1 amount of transparency in footprint (~0.2f)
    private void drawFootPrint(Graphics2D g2, double lat, double lon, double alt, boolean fillFootPrint, Color outlineColor, Color FillColor, float alpha, int numPtsFootPrint)
    {
        
        // vars: ===============================
        // draw footprints of satellites
        Polygon footprint;
        // varaibles for fixing disconnect around international date line (for filled footprints)
        int disconnectCount = 0; // how many disconnect points, 0=good, 1= far away sat, sees north/south pole, 2= spans date line
        int disconnectIndex1 = 0; // first index of disconnect
        int disconnectIndex2 = 0; // second index of disconnect
        int[] disconnect1pos = new int[2]; // corrected x,y point at each discontinutiy
        int[] disconnect1neg = new int[2];
        int[] disconnect2pos = new int[2];
        int[] disconnect2neg = new int[2];
        
        // size
        // save last width/height
        int w = lastTotalWidth;
        int h = lastTotalHeight;
        
        //========================================
        
        //disconnectCount = 0; // reset disconnect count
        
        // set color -- outline
        g2.setPaint( outlineColor  );
        
        // Polygon shape -- for filling?
        footprint = new Polygon();
        
        double lambda0 = Math.acos(AstroConst.R_Earth/(AstroConst.R_Earth+alt));
        
        double beta = (90*Math.PI/180.0-lat); // latitude center (pitch)
        double gamma = -lon+180.0*Math.PI/180.0; // longitude (yaw)
        
        // rotation matrix
        double[][] M = new double[][] {{Math.cos(beta)*Math.cos(gamma), Math.sin(gamma), -Math.sin(beta)*Math.cos(gamma)},
        {-Math.cos(beta)*Math.sin(gamma),Math.cos(gamma), Math.sin(beta)*Math.sin(gamma)},
        {Math.sin(beta), 0.0, Math.cos(beta)}};
        double theta = 0+Math.PI/2.0; // with extra offset of pi/2 so circle starts left of center going counter clockwise
        double phi = lambda0;
        
        // position
        double[] pos = new double[3];
        pos[0] = AstroConst.R_Earth*Math.cos(theta)*Math.sin(phi);
        pos[1] = AstroConst.R_Earth*Math.sin(theta)*Math.sin(phi);
        pos[2] = AstroConst.R_Earth*Math.cos(phi);
        
        // rotate to center around satellite sub point
        pos = mult(M,pos);
        
        // calculate Lat Long of point (first time save it)
        double[] llaOld = ecef2lla(pos);
        //llaOld[1] = llaOld[1] - 90.0*Math.PI/180.0;
        double[] lla = new double[3]; // prepare array
        
        // copy of orginal point
        double[] lla0 = new double[3];
        int[] xy0 = new int[2];
        lla0[0] = llaOld[0];
        lla0[1] = llaOld[1];
        lla0[2] = llaOld[2];
        
        // point
        int[] xy;
        
        // original point
        int[] xy_old = findXYfromLL(lla0[0]*180.0/Math.PI, lla0[1]*180.0/Math.PI, w, h, imageWidth, imageHeight);
        xy0[0] = xy_old[0];
        xy0[1] = xy_old[1];
        
        // add to polygon
        if( fillFootPrint )
        {
            footprint.addPoint(xy_old[0],xy_old[1]);
        }
        
        // footprint parameters
        double dt = 2.0*Math.PI/(numPtsFootPrint-1.0);
        
        for(int j=1;j<numPtsFootPrint;j++)
        {
            theta = j*dt+Math.PI/2.0; // +Math.PI/2.0 // offset so it starts at the side
            //phi = lambda0;
            
            // find position - unrotated about north pole
            pos[0] = AstroConst.R_Earth*Math.cos(theta)*Math.sin(phi);
            pos[1] = AstroConst.R_Earth*Math.sin(theta)*Math.sin(phi);
            pos[2] = AstroConst.R_Earth*Math.cos(phi);
            
            // rotate to center around satellite sub point
            pos = mult(M,pos);
            
            // find lla
            lla = ecef2lla(pos);
            //lla[1] = lla[1]-90.0*Math.PI/180.0;
            //System.out.println("ll=" +lla[0]*180.0/Math.PI + "," + (lla[1]*180.0/Math.PI));
            
            xy = findXYfromLL(lla[0]*180.0/Math.PI, lla[1]*180.0/Math.PI, w, h, imageWidth, imageHeight);
            
            // draw line (only if not accross the screen)
            if( Math.abs(lla[1] - llaOld[1]) < 4.0)
            {
                
                g2.drawLine(xy_old[0],xy_old[1],xy[0],xy[1]);
                //System.out.println("" +lla[0] + " " + lla[1]);
            }
            else
            {
                // draw line in correctly ======================
                double newLat = linearInterpDiscontLat(llaOld[0], llaOld[1], lla[0], lla[1]);
                
                // get xy points for both the old and new side (positive and negative long)
                int[] xyMid_pos = findXYfromLL(newLat*180.0/Math.PI, 180.0, w, h, imageWidth, imageHeight);
                int[] xyMid_neg = findXYfromLL(newLat*180.0/Math.PI, -180.0, w, h, imageWidth, imageHeight);
                
                // draw 2 lines - one for each side of the date line
                if(llaOld[1] > 0) // then the old one is on the positive side
                {
                    g2.drawLine(xy_old[0],xy_old[1],xyMid_pos[0],xyMid_pos[1]);
                    g2.drawLine(xy[0],xy[1],xyMid_neg[0],xyMid_neg[1]);
                }
                else // the new one is on the positive side
                {
                    g2.drawLine(xy[0],xy[1],xyMid_pos[0],xyMid_pos[1]);
                    g2.drawLine(xy_old[0],xy_old[1],xyMid_neg[0],xyMid_neg[1]);
                }
                //==============================================
                
                // save info about disconnect
                disconnectCount++;
                if(disconnectCount == 1)
                {
                    disconnectIndex1 = j;
                    //System.out.println("Disconnect 1 found:"+disconnectIndex1);
                    disconnect1pos = xyMid_pos; // save this point
                    disconnect1neg = xyMid_neg;
                }
                else if(disconnectCount == 2)
                {
                    disconnectIndex2 = j;
                    //System.out.println("Disconnect 2 found:"+disconnectIndex2);
                    disconnect2pos = xyMid_pos; // save this point
                    disconnect2neg = xyMid_neg;
                }
                
            } // disconnect in line
            
            // save point
            xy_old[0]=xy[0];
            xy_old[1]=xy[1];
            llaOld[0] = lla[0];
            llaOld[1] = lla[1];
            llaOld[2] = lla[2];
            
            // add point to polygon
            if( fillFootPrint )
            {
                footprint.addPoint(xy_old[0],xy_old[1]);
            }
            
        } // for each point around footprint
        
        // draw point from last point back to first
        if( Math.abs(llaOld[1]-lla0[1]) < 4.0)
        {
            g2.drawLine(xy_old[0],xy_old[1],xy0[0],xy0[1]);
        }
        
        // draw polygon -- won't work when part of the foot print is on other side
        // seems not to work for GEO sats too, fills wrong side at times
        // see if polygon should be split into two polygons
        // -- could be divited up 4 seperate peices!
        // NEED to make this very transparent
        if( fillFootPrint )
        {
            Color satCol = FillColor;
            Color transColor = new Color(satCol.getRed()/255.0f,satCol.getGreen()/255.0f,satCol.getBlue()/255.0f,alpha);
            g2.setPaint( transColor );
            
            if(disconnectCount == 0)
            {
                // no disconnects fill like normal
                g2.fill(footprint);
            }
            else if(disconnectCount == 1)
            {
                // okay this is at a pole, add in edges and fill in
                // figure out N or S based on sat position ( lat > 0 or < 0)
                boolean northPoleVisible = ( lat > 0);
                
                int[] ptPos = new int[2];
                int[] ptNeg = new int[2];
                
                Polygon fullFootPrint = new Polygon();
                
                if(northPoleVisible)
                {
                    
                    ptPos = findXYfromLL(90.0, 180.0, w, h, imageWidth, imageHeight);
                    ptNeg = findXYfromLL(90.0, -180.0, w, h, imageWidth, imageHeight);
                    
                    // counter clockwise - add points
                    for(int k=0; k<disconnectIndex1;k++)
                    {
                        fullFootPrint.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    fullFootPrint.addPoint(disconnect1pos[0],disconnect1pos[1]);
                    fullFootPrint.addPoint(ptPos[0],ptPos[1]);
                    fullFootPrint.addPoint(ptNeg[0],ptNeg[1]);
                    fullFootPrint.addPoint(disconnect1neg[0],disconnect1neg[1]);
                    for(int k=disconnectIndex1; k<footprint.npoints;k++)
                    {
                        fullFootPrint.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    
                }
                else
                {
                    // counter clockwise - add points
                    ptPos = findXYfromLL(-90.0, 180.0, w, h, imageWidth, imageHeight);
                    ptNeg = findXYfromLL(-90.0, -180.0, w, h, imageWidth, imageHeight);
                    
                    for(int k=0; k<disconnectIndex1;k++)
                    {
                        fullFootPrint.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    
                    fullFootPrint.addPoint(disconnect1neg[0],disconnect1neg[1]);
                    fullFootPrint.addPoint(ptNeg[0],ptNeg[1]);
                    fullFootPrint.addPoint(ptPos[0],ptPos[1]);
                    fullFootPrint.addPoint(disconnect1pos[0],disconnect1pos[1]);
                    
                    for(int k=disconnectIndex1; k<footprint.npoints;k++)
                    {
                        fullFootPrint.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    
                }// south pole visible
                
                // fill full print
                g2.fill(fullFootPrint);
                
                
            }
            else if(disconnectCount == 2)
            {
                // this is the case when a sat spans the international dateline
                // make two polygons to fill
                
                // polygon starts left of center and goes counter clockwise
                
                // check to see if this is a standard case (discontinuity
                // 1 is vertically lower than discon #2) (y is backwards in java)
                if( disconnect1neg[1]  >= disconnect2neg[1])
                {
                    // STANDARD drawing case for 2 discontinuities
                    
                    
                    
                    // new polygon - part on left side
                    Polygon footprintPartLeft = new Polygon();
                    
                    // get both sides of discontinuity 1
                    footprintPartLeft.addPoint( disconnect1neg[0], disconnect1neg[1]); // first point at left edge
                    for(int k=disconnectIndex1;k<disconnectIndex2;k++)
                    {
                        
                        footprintPartLeft.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    footprintPartLeft.addPoint( disconnect2neg[0], disconnect2neg[1]);
                    
                    g2.fill(footprintPartLeft);
                    
                    // now create Right side of the polygon
                    Polygon footprintPartRight = new Polygon();
                    // fill in first part
                    for(int k=0;k<disconnectIndex1;k++)
                    {
                        
                        footprintPartRight.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    // add disconnect points
                    footprintPartRight.addPoint( disconnect1pos[0], disconnect1pos[1]);
                    footprintPartRight.addPoint( disconnect2pos[0], disconnect2pos[1]);
                    // fill in last part
                    for(int k=disconnectIndex2;k<footprint.npoints;k++)
                    {
                        
                        footprintPartRight.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    g2.fill(footprintPartRight);
                }// standard 2-discont drawing
                else  // non standard discont 1 is above discont 2
                {
                    // new polygon - part on left side
                    Polygon footprintPartLeft = new Polygon();
                    
                    for(int k=0;k<disconnectIndex1;k++)
                    {
                        
                        footprintPartLeft.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    footprintPartLeft.addPoint( disconnect1neg[0], disconnect1neg[1]); // left edge top
                    footprintPartLeft.addPoint( disconnect2neg[0], disconnect2neg[1]); // left edge bottom
                    for(int k=disconnectIndex2;k<footprint.npoints;k++)
                    {
                        
                        footprintPartLeft.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    g2.fill(footprintPartLeft);
                    
                    // Right part of foot print
                    Polygon footprintPartRight = new Polygon();
                    footprintPartRight.addPoint( disconnect1pos[0], disconnect1pos[1]);
                    for(int k=disconnectIndex1;k<disconnectIndex2;k++)
                    {
                        
                        footprintPartRight.addPoint(footprint.xpoints[k],footprint.ypoints[k]);
                    }
                    footprintPartRight.addPoint( disconnect2pos[0], disconnect2pos[1]);
                    g2.fill(footprintPartRight);
                    
                    
                }// non standard 2-discont drawing
                
            } // 2 disconnect points
            
        } // fill foot print
    }
    
// ========= FOOTPRINT ===========================================================================
//================================================================================================
    
    
    public double getCenterLat()
    {
        return centerLat;
    }
    
    public void setCenterLat(double centerLat)
    {
        this.centerLat = centerLat;
    }
    
    public double getCenterLong()
    {
        return centerLong;
    }
    
    public void setCenterLong(double centerLong)
    {
        this.centerLong = centerLong;
    }
    
    public double getZoomFactor()
    {
        return zoomFactor;
    }
    
    public void setZoomFactor(double zoomFactor)
    {
        this.zoomFactor = zoomFactor;
    }
    
    public double getZoomIncrementMultiplier()
    {
        
        return zoomIncrementMultiplier;
    }
    
    public void setZoomIncrementMultiplier(double zoomIncrementMultiplier)
    {
        if(zoomIncrementMultiplier < 1.0)
        {
            zoomIncrementMultiplier = 1.1; // default lowest
        }
        
        this.zoomIncrementMultiplier = zoomIncrementMultiplier;
    }
    
    public int getLastTotalWidth()
    {
        return lastTotalWidth;
    }
    
    public int getLastTotalHeight()
    {
        return lastTotalHeight;
    }
    
    public int getImageWidth()
    {
        return imageWidth;
    }
    
    public int getImageHeight()
    {
        return imageHeight;
    }
    
    public boolean isShowDateTime()
    {
        return showDateTime;
    }
    
    public void setShowDateTime(boolean showDateTime)
    {
        this.showDateTime = showDateTime;
    }
    
    public int getXDateTimeOffset()
    {
        return xDateTimeOffset;
    }
    
    public void setXDateTimeOffset(int xDateTimeOffset)
    {
        this.xDateTimeOffset = xDateTimeOffset;
    }
    
    public int getYDateTimeOffset()
    {
        return yDateTimeOffset;
    }
    
    public void setYDateTimeOffset(int yDateTimeOffset)
    {
        this.yDateTimeOffset = yDateTimeOffset;
    }
    
    public Color getDateTimeColor()
    {
        return dateTimeColor;
    }
    
    public void setDateTimeColor(Color dateTimeColor)
    {
        this.dateTimeColor = dateTimeColor;
    }
    
    public void setBackgroundColor(Color backgroundColor)
    {
        this.backgroundColor = backgroundColor;
        
    }

    public boolean isDrawSun()
    {
        return drawSun;
    }

    public void setDrawSun(boolean drawSun)
    {
        this.drawSun = drawSun;
    }

    public Color getSunColor()
    {
        return sunColor;
    }

    public void setSunColor(Color sunColor)
    {
        this.sunColor = sunColor;
    }

    public int getNumPtsSunFootPrint()
    {
        return numPtsSunFootPrint;
    }

    public void setNumPtsSunFootPrint(int numPtsSunFootPrint)
    {
        this.numPtsSunFootPrint = numPtsSunFootPrint;
    }

    public float getSunAlpha()
    {
        return sunAlpha;
    }

    public void setSunAlpha(float sunAlpha)
    {
        this.sunAlpha = sunAlpha;
    }
    
    public void addRenderableObject(JSatTrakRenderable renderable)
    {
        renderableObjects.add(renderable);
    }
    
    public boolean removeRenderableObject(JSatTrakRenderable renderable)
    {
        return renderableObjects.remove(renderable);
    }
    
    public void clearRenderableObjects()
    {
        renderableObjects.clear();
    }
}
