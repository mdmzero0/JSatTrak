/*
 * Shawn E. Gano - Polar Plotting Label
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;

/**
 *
 * @author sgano
 */
public class JPolarPlotLabel   extends JLabel
{
    private double aspectRatio = 2.0;// aspect ratio of the image in the frame (should equal value in J2EarthPanel)
    private int imageWidth = 0, imageHeight = 0;
    private int lastTotalWidth = 0, lastTotalHeight = 0;
    
    private int plotSizePadding = 5; // padding on sides
    
    private Color bgColor = Color.BLACK;
    private Color lineColor = Color.WHITE;
    private Color constraintElvColor = Color.GREEN;
    private Color currentLocColor = Color.ORANGE;
    
    private Color leadColor = Color.BLUE;
    private Color lagColor = Color.BLUE;
    
    
    double[] elevationRange = new double[] {-90.0,90.0};
    double elevationMarkInc = 30; // degrees should be evenly divisible by 90
    double azimuthMarkInc = 30; // deg (should be evenly divisible by 180)
    
    private double[] currentAE = new double[] {0,90};
    
    // rendering hints
    private transient RenderingHints renderHints;
    
    private boolean showLeadLagData = true;
    
    // elevation constraint
    private double elevConst = 0; // deg
    
    
    // lead lag data to plot (if not empty)
    private double[][] aerLead = null;
    private double[][] aerLag = null;
    
    // display options
    private boolean displayTime = true;
    private boolean displayNames = false;
    
    private String timeString = "";
    private String gs2SatNameString = "";
    
    public JPolarPlotLabel()
    {
        
        // create rendering options -- anti-aliasing
        renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        
        //this.setBackground(bgColor);
        
    } // constructor
    
    
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
        g2.setRenderingHints(renderHints);
        
        //g2.drawLine(0,0,w,h); // draw a line across the map
        int plotSize = Math.min(h, w) - plotSizePadding*2; // size is smallest of width and height
        
        int[] plotCenter = new int[] {w/2,h/2}; // center of plot
        
        // set bg color 
        //g2.setBackground(bgColor);
        g2.setPaint(bgColor);
        g2.fillRect(0, 0, w, h);  
        
        // set line color 
        g2.setPaint(lineColor);
        
        // line style
        g2.setStroke(new BasicStroke());  // standard line, no fancy stuff

        // draw plot region -----------------
        
        // draw outside circle
        g2.drawOval(plotCenter[0]-plotSize/2, plotCenter[1]-plotSize/2, plotSize, plotSize);
        
        // set line pattern to dotted
        // dashed line, 2 pix on 2 pix off
        float[] dashPattern = { 2, 2, 2, 2 };
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10,
                dashPattern, 0));
        
        // draw elevation rings
        //double elevationDegPerPixel = (elevationRange[1]-elevationRange[0])/(plotSize/2);
        for(double elv=elevationRange[0];elv<=elevationRange[1];elv+=elevationMarkInc)
        {
            double radiusPix = (1.0-(elv-elevationRange[0])/(elevationRange[1]-elevationRange[0]))*(plotSize/2);
            
            g2.drawOval((int)(plotCenter[0]-radiusPix),(int)( plotCenter[1]-radiusPix), (int)(radiusPix*2), (int)(radiusPix*2));
            
            // add text along +x-axis
            g2.drawString(""+((int)elv), (int)(plotCenter[0]+radiusPix),plotCenter[1]+12);
        }
        
        
        // line style
        g2.setStroke(new BasicStroke());  // standard line, no fancy stuff
        // draw line at elevation constraint
        double radiusPix = (1.0-(elevConst-elevationRange[0])/(elevationRange[1]-elevationRange[0]))*(plotSize/2);
        g2.setPaint(constraintElvColor);
        g2.drawOval((int)(plotCenter[0]-radiusPix),(int)( plotCenter[1]-radiusPix), (int)(radiusPix*2), (int)(radiusPix*2));
        g2.setPaint(lineColor); // reset paint color
        
        // draw azimuth sectors
        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10,
                dashPattern, 0));
        for(double az=0;az<360;az+=azimuthMarkInc)
        {
            double endX = plotCenter[0]+(plotSize/2)*Math.sin(az*Math.PI/180.0);
            double endY = plotCenter[1] - (plotSize/2)*Math.cos(az*Math.PI/180.0);
            
            g2.drawLine(plotCenter[0], plotCenter[1], (int)endX, (int)endY);
            g2.drawString("" + ((int)az), (int)endX+2, (int)endY);
        }
        
        // end draw plot region ----------------------
        
        // display Time 
        if(displayTime)
        {
            g2.setPaint(Color.WHITE);
            g2.drawString(timeString, 2, plotCenter[1]+plotSize/2 );
        }
        if(displayNames)
        {
            g2.setPaint(Color.WHITE);
            g2.drawString(gs2SatNameString, 2, plotCenter[1]- plotSize/2 + 12);
        }
        
        double x=0;
        double y=0;
        double r=0;
        
        // plot lead / lag data
        if (showLeadLagData)
        {
            int xOld = 0, yOld = 0;

            // plot lead data - dots
            g2.setStroke(new BasicStroke());  // standard line, no fancy stuff
            g2.setPaint(leadColor);

            Double nanDbl = new Double(Double.NaN);

            if (aerLead != null)
            {
                for (int i = 0; i < aerLead.length; i++)
                {

                    r = (1.0 - (aerLead[i][1] - elevationRange[0]) / (elevationRange[1] - elevationRange[0])) * (plotSize / 2);

                    x = plotCenter[0] + (r) * Math.sin(aerLead[i][0] * Math.PI / 180.0);
                    y = plotCenter[1] - (r) * Math.cos(aerLead[i][0] * Math.PI / 180.0);

                    if (!nanDbl.equals(aerLead[i][0]))
                    {
                        g2.fillOval((int) (x - 1), (int) (y - 1), 3, 3);

                        // connect
                        if (i > 0)
                        {
                            if(!(xOld ==0 && yOld==0))
                            {
                                g2.drawLine((int) x, (int) y, xOld, yOld);
                            }
                        }
                    }
                    xOld = (int) x;
                    yOld = (int) y;
                }
            }

            // plot lag data - dots
            g2.setPaint(lagColor);
            if (aerLag != null)
            {
                for (int i = 0; i < aerLag.length; i++)
                {

                    r = (1.0 - (aerLag[i][1] - elevationRange[0]) / (elevationRange[1] - elevationRange[0])) * (plotSize / 2);

                    x = plotCenter[0] + (r) * Math.sin(aerLag[i][0] * Math.PI / 180.0);
                    y = plotCenter[1] - (r) * Math.cos(aerLag[i][0] * Math.PI / 180.0);

                    if (!nanDbl.equals(aerLag[i][0]))
                    {
                        g2.fillOval((int) (x - 1), (int) (y - 1), 3, 3);

                        // connect
                        if (i > 0)
                        {
                            if(!(xOld ==0 && yOld==0))
                            {
                                g2.drawLine((int) x, (int) y, xOld, yOld);
                            }
                        }
                    }
                    xOld = (int) x;
                    yOld = (int) y;
                }
            }
        } // plot lead / lag data

        // plot current point:
        // given currentAE, find x,y pixel location
        g2.setPaint(currentLocColor);
        r = (1.0 - (currentAE[1] - elevationRange[0]) / (elevationRange[1] - elevationRange[0])) * (plotSize / 2);

        x = plotCenter[0] + (r) * Math.sin(currentAE[0] * Math.PI / 180.0);
        y = plotCenter[1] - (r) * Math.cos(currentAE[0] * Math.PI / 180.0);

        g2.fillOval((int) (x - 5), (int) (y - 5), 10, 10);



    } // draw

    public // deg (should be evenly divisible by 180)
            double[] getCurrentAE()
    {
        return currentAE;
    }

    public void setCurrentAE(double[] currentAE)
    {
        this.currentAE = currentAE;
    }

    public // TEMP -- swap this out with  HASH !!!!!
            double getElevConst()
    {
        return elevConst;
    }

    public void setElevConst(double elevConst)
    {
        this.elevConst = elevConst;
    }

    public // deg
            // lead lag data to plot (if not empty)
            double[][] getAerLead()
    {
        return aerLead;
    }

    public void setAerLead(double[][] aerLead)
    {
        this.aerLead = aerLead;
    }

    public double[][] getAerLag()
    {
        return aerLag;
    }

    public void setAerLag(double[][] aerLag)
    {
        this.aerLag = aerLag;
    }

    // clears the data
    public void clearLeadLagData()
    {
        aerLead = null;
        aerLag = null;
    }
    
    public void resetCurrentPosition()
    {
        currentAE[0] =0;
        currentAE[1] =90;
    }

    public boolean isShowLeadLagData()
    {
        return showLeadLagData;
    }

    public void setShowLeadLagData(boolean showLeadLagData)
    {
        this.showLeadLagData = showLeadLagData;
    }

    public String getGs2SatNameString()
    {
        return gs2SatNameString;
    }

    public void setGs2SatNameString(String gs2SatNameString)
    {
        this.gs2SatNameString = gs2SatNameString;
    }

    public String getTimeString()
    {
        return timeString;
    }

    public void setTimeString(String timeString)
    {
        this.timeString = timeString;
    }

    public // display options
            boolean isDisplayTime()
    {
        return displayTime;
    }

    public void setDisplayTime(boolean displayTime)
    {
        this.displayTime = displayTime;
    }

    public boolean isDisplayNames()
    {
        return displayNames;
    }

    public void setDisplayNames(boolean displayNames)
    {
        this.displayNames = displayNames;
    }
}
