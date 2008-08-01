/**
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
 * 
 */
package jsattrak.gui;

import jsattrak.objects.GroundStation;
import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import java.awt.image.*;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import jsattrak.coverage.JSatTrakRenderable;
import jsattrak.objects.AbstractSatellite;
import jsattrak.utilities.LandMassRegions;
import name.gano.astro.bodies.Sun;
import name.gano.astro.time.Time;

public class J2DEarthPanel extends JPanel implements ComponentListener , java.io.Serializable
{
    // options
    private Color backgroundColor = new Color(236,233,216); //Color.LIGHT_GRAY;
        
    //private transient ImageIcon planetImage;
    //private JLabel imageMap;
    private J2dEarthLabel2 imageMap;
    
    public transient JPopupMenu popup;
    private transient JMenuItem latlonLinesMenu, moreOptionsMenu; // , earthPeMenu, earthNoaaMenu
    private transient ImageIcon checkMark;
    
    //private int imageMapNum = 0; //0=blue marble, 1=NOAA
    
    
    private transient BufferedImage bimage;
    
    private String backgroundImagePath = "/images/Earth_PE_small.jpg"; // default image
    
    private double aspectRatio = 2.0; // width/height
    
    private int imageScalingOption = BufferedImage.SCALE_FAST;
    
    // toggle button references used to see if we should be zooming
    private transient JToggleButton zoomInToggleButton;
    private transient JToggleButton zoomOutToggleButton;
    private transient JToggleButton recenterToggleButton;
    
    // current time
    private transient Time currentTime;
    
    // hastable of all the satellites currently processing
    //Hashtable<String,SatelliteProps> satHash = new Hashtable();
     
     // app
     private transient JSatTrak app;
     
     // sun
     private Sun sun;
     
     
     // land mass  --  only hard coded for now
    // edit jsattrak.utilities.LandMassRegions to load either US map or World Map
    LandMassRegions landMass = new LandMassRegions();
    int indexSpacing = 1; // index increment for plotting regions (1=best, >1 faster)
//    boolean showLandMassOutlines = false;
//    Color landOutlineColor = Color.WHITE;
    
    
    // bean -- no inputs
    public J2DEarthPanel()
    {
        super(false); // no double buffering
        
        Hashtable<String,AbstractSatellite> satHash = new Hashtable<String,AbstractSatellite>();
        Hashtable<String,GroundStation> gsHash = new Hashtable<String, GroundStation>();
        iniObject(satHash,gsHash);
               
    }
    
    public J2DEarthPanel(Hashtable<String,AbstractSatellite> satHash, Hashtable<String,GroundStation> gsHash, JToggleButton zoomInToggleButton, JToggleButton zoomOutToggleButton, JToggleButton recenterToggleButton, Time currentTime, Sun sun, JSatTrak app)
    {
        super(false);//not double buffered
       
        // current time
        this.currentTime = currentTime;
        // parent app to show 2d earth window properties
        this.app = app;
        // sun
        this.sun = sun;
        
        iniObject(satHash,gsHash);
        
        // save buttons
        this.zoomInToggleButton = zoomInToggleButton;
        this.zoomOutToggleButton = zoomOutToggleButton;
        this.recenterToggleButton = recenterToggleButton;
        
        this.setBackground(backgroundColor);
        
        // TEMP try out the LandMassRegions -- move this to top class, so each map doesn't have their own? or leave it this way so each can be customized
//        if(showLandMassOutlines)
//        {
//            landMass = new LandMassRegions();
//        }
               
    } // TwoDFrame
    
    private void iniObject(Hashtable<String,AbstractSatellite> satHash, Hashtable<String,GroundStation> gsHash)
    {
        //...Create the GUI and put it in the window...
        
        //Create the first label.
        ImageIcon planetImage = createImageIcon(backgroundImagePath,"Earth");//"/images/Earth_PE_small.png","Earth");
        bimage = getBufferedImage(planetImage);
        
        //imageMap = new JLabel(planetImage);
        imageMap = new J2dEarthLabel2(planetImage,aspectRatio, satHash, gsHash, backgroundColor, currentTime, sun);
        imageMap.setHorizontalAlignment(JLabel.CENTER);
        //JScrollPane tableScrollPane = new JScrollPane(imageMap);
        
        super.addComponentListener(this); // adds the listner to the whole internal window
        
        setLayout(new GridLayout(1, 1));
        add(imageMap);
        //add(new JLabel(planetImage));
        
        
        // add these lines so the split pane will resize correctly
        setMinimumSize(new Dimension(0, 0));
        setMaximumSize(new Dimension(0, 0));
        
        //...Then set the window size or call pack...
        //setSize(400,230);
        
        //Set the window's location.
        //setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        
        // Popup Menu
        popup = new JPopupMenu();
        ActionListener menuListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                // handle the menu selection!
                //System.out.println("Popup menu item ["+ event.getActionCommand() + "] was pressed.");
                //latlonLinesMenu, earthPeMenu, earthNoaaMenu
                if( event.getSource() == latlonLinesMenu)
                {
                    // toggle lat long lines
                    imageMap.showLatLong = !imageMap.showLatLong; // toogle lat long
                    imageMap.repaint();
                } 
//                else if( event.getSource() == earthPeMenu)
//                {
//                    // Toolkit.getDefaultToolkit().getImage(getClass().getResource("/toolbarButtonGraphics/custom/sattrak.png"))
//                    backgroundImagePath = "/images/Earth_PE_small.png";
//                    ImageIcon temp = createImageIcon(backgroundImagePath,"Earth");
//                    //ImageIcon temp = createImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("../images/Earth_PE_small.png")),"Earth");
//                    bimage = getBufferedImage(temp);
//                    ScaleImageMap(); // repaint the image
//                    earthNoaaMenu.setIcon(null);
//                    earthPeMenu.setIcon(checkMark);
//                    
//                    imageMapNum = 0; //0=PE, 1=NOAA
//                    
//                }
//                else if( event.getSource() == earthNoaaMenu)
//                {
//                    backgroundImagePath = "/images/Earth_NOAA_NGDC_small.png";
//                    ImageIcon temp = createImageIcon(backgroundImagePath,"Earth");
//                    bimage = getBufferedImage(temp);
//                    ScaleImageMap(); // repaint the image
//                    earthNoaaMenu.setIcon(checkMark);
//                    earthPeMenu.setIcon(null);
//                    
//                    imageMapNum =1; //0=PE, 1=NOAA
//                }
                else if( event.getSource() == moreOptionsMenu)
                {
                    // open more options
                    app.open2dWindowOptions();
                    
                }
                
                
                
                
            }
        };
        
        // JMenuItem latlonLinesMenu, earthPeMenu, earthNoaaMenu
        latlonLinesMenu = new JMenuItem("Toggle Lat/Lon"); //, new ImageIcon("1.gif")
        popup.add( latlonLinesMenu );
        latlonLinesMenu.setHorizontalTextPosition(JMenuItem.RIGHT);
        latlonLinesMenu.addActionListener(menuListener);
        
        popup.addSeparator();
        
//        checkMark = createImageIcon("/icons/other/emblem-default.png","check");
//        
//        earthPeMenu = new JMenuItem("Blue Marble Image",checkMark);
//        popup.add(earthPeMenu);
//        earthPeMenu.setHorizontalTextPosition(JMenuItem.RIGHT);
//        earthPeMenu.addActionListener(menuListener);
//        
//        earthNoaaMenu = new JMenuItem("NOAA NGDC Image"); //,checkMark
//        popup.add(earthNoaaMenu);
//        earthNoaaMenu.setHorizontalTextPosition(JMenuItem.RIGHT);
//        earthNoaaMenu.addActionListener(menuListener);
//        
//        popup.addSeparator();
        
        moreOptionsMenu = new JMenuItem("More Options..."); //,checkMark
        popup.add(moreOptionsMenu);
        moreOptionsMenu.setHorizontalTextPosition(JMenuItem.RIGHT);
        moreOptionsMenu.addActionListener(menuListener);
        
        // menu properties
        //popup.setLabel("Justification");
        popup.setBorder(new BevelBorder(BevelBorder.RAISED));
        popup.addPopupMenuListener(new PopupPrintListener());
        
        addMouseListener(new MousePopupListener());
                
    } // ini Object
    
    public void setSatHashTable(Hashtable<String,AbstractSatellite> satHash)
    {
        imageMap.setSatHashTable(satHash);
        
    } // TwoDFrame
    
    // called when component is repainted
 /*   public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
                Dimension dim = getSize();
                int w = dim.width;
        int h = dim.height;
  
        //System.out.println("test");
        g2.setPaint(Color.black);
        //g.fill
        g2.drawRect(0,0,70,70);
  
  
    }
  */
    /** Returns an ImageIcon, or null if the path was invalid. */
    //protected static ImageIcon createImageIcon(String path, String description)
    public ImageIcon createImageIcon(String path, String description)        
    {
        java.net.URL imgURL = J2DEarthPanel.class.getResource(path);
        if (imgURL != null)
        {
            return new ImageIcon(imgURL, description); // first check in the JAR
        }
        else
        {
            if( (new File(path)).exists() ) // see if file exsists on system
            {
                ImageIcon imag =  new ImageIcon(path, description); // load from file system
                return imag; 
            }
            
            // Can't find file so revert to default - stored in JAR!
            System.out.println("Couldn't find 2d pixmap file: " + path + " -- Reverting to default");
            this.backgroundImagePath = "/images/Earth_PE_small.jpg";
            imgURL = J2DEarthPanel.class.getResource(backgroundImagePath);
            
            return new ImageIcon(imgURL, description);
        }
    }
    
    // Frame resized... scale the image!
    public void componentResized(ComponentEvent e)
    {
        //Component c = e.getComponent();
        //System.out.println("componentResized event from "+ c.getClass().getName()+ "; new size: "+ c.getSize().width+ ", "  + c.getSize().height);
        
        //BufferedImage bimage = getBufferedImage(planetImage);
        
        ScaleImageMap();
        
    }
    
    public void ScaleImageMap()
    {
        
        //System.out.println("HERE");
        int newWidth=1, newHeight=1;
        if(imageMap.getHeight() != 0)
        {
            if(imageMap.getWidth() / imageMap.getHeight() > aspectRatio)
            {
                // label has larger aspect ratio, constraint by height
                newHeight = imageMap.getHeight();
                newWidth = (int) (imageMap.getHeight()*aspectRatio);
            }
            else
            {
                // label has lower aspect ratio
                newWidth = imageMap.getWidth();
                newHeight = (int) (imageMap.getWidth()*1.0/aspectRatio);
            }
            
            // take into account zoom: (might not be the best way as it can create a huge image in memory... but okay for now)
            // in future may want to calculate the part of the image that will be displayed an clip the rest somehow
            //newWidth = (int)Math.round(newWidth*imageMap.getZoomFactor());
            //newHeight = (int)Math.round(newHeight*imageMap.getZoomFactor());
            
            // first get the sub image that will be scaled up
            //BufferedImage midImage = bimage.getSubimage(int x, int y, int w, int h);
            // gunna need orginal image size, new image size, total image size... etc.
            // center lat,long... zoomfactor.
            
            int midHeight = (int)Math.round( bimage.getHeight()/imageMap.getZoomFactor() );
            int midWidth = (int)Math.round( bimage.getWidth()/imageMap.getZoomFactor() );
            // uper left corner of image
            int midXupLeft = (int)Math.round( (bimage.getWidth()-midWidth)/2.0 + bimage.getWidth()*imageMap.getCenterLong()/360.0);
            int midYupLeft = (int)Math.round( (bimage.getHeight()-midHeight)/2.0 - bimage.getHeight()*imageMap.getCenterLat()/180.0);
            
            // make sure sub image height/width are not <= 0!!
            if(midHeight <= 0)
            {
                midHeight = 1;
            }
            if(midWidth <= 0)
            {
                midWidth = 1;
            }
            
            // check that center lat/long and zoom will not call for a sub image
            // with bounds outside of image...
            // if so then readjust center/lat long so that edges are on map
            
            // check x direction for overflow of image
            if(midXupLeft < 0) // too close to left side
            {
                //System.out.println("midXupLeft ini: " + midXupLeft);
                
                // new center long value: (solve midXupLeft == 0, for centerLong)
                double newCenterLong = (0.0-(bimage.getWidth()-midWidth)/2.0)*360.0/bimage.getWidth();
                imageMap.setCenterLong( newCenterLong );
                                
                // recalc sub image calculations
                midXupLeft = (int)Math.round( (bimage.getWidth()-midWidth)/2.0 + bimage.getWidth()*imageMap.getCenterLong()/360.0);
                //System.out.println("midXupLeft final: " + midXupLeft);
            }
            else if(midXupLeft + midWidth > bimage.getWidth())
            {
                // solve  midXupLeft + midWidth == bimage.getWidth(), for centerLong
                // too close to right side
                double newCenterLong = ((bimage.getWidth()-midWidth)-(bimage.getWidth()-midWidth)/2.0)*360.0/bimage.getWidth();
                imageMap.setCenterLong( newCenterLong );
                
                // recalc sub image calculations
                midXupLeft = (int)Math.round( (bimage.getWidth()-midWidth)/2.0 + bimage.getWidth()*imageMap.getCenterLong()/360.0);
            }
            
            // check y direction for image corner not at the corner
            if(midYupLeft < 0) // too close to top
            {
                // midYupLeft =  (bimage.getHeight()-midHeight)/2.0 - bimage.getHeight()*imageMap.getCenterLat()/180.0;
                double newCenterLat =  -((0.0) - (bimage.getHeight()-midHeight)/2.0)*180.0/bimage.getHeight();
                imageMap.setCenterLat( newCenterLat );
                
                // recalc sub image calculations
                midYupLeft = (int)Math.round( (bimage.getHeight()-midHeight)/2.0 - bimage.getHeight()*imageMap.getCenterLat()/180.0);
            }
            else if(midYupLeft + midHeight >= bimage.getHeight())
            {
                // too close to bottom
                double newCenterLat =  -((bimage.getHeight()-midHeight) - (bimage.getHeight()-midHeight)/2.0)*180.0/bimage.getHeight();
                imageMap.setCenterLat( newCenterLat );
                
                // recalc sub image calculations
                midYupLeft = (int)Math.round( (bimage.getHeight()-midHeight)/2.0 - bimage.getHeight()*imageMap.getCenterLat()/180.0);
            }
            
            // get sub image that will be scaled to fit window
            BufferedImage midImage = bimage.getSubimage(midXupLeft, midYupLeft, midWidth, midHeight);
            
            
//            // draw region outlines if required:
//            if(showLandMassOutlines)
//            {
//                Graphics2D g2 = (Graphics2D)midImage.getGraphics();
//                drawLandMasses(g2, midImage.getWidth(), midImage.getHeight());
//            }
            
            // incon image used to set icon with
            ImageIcon im; 
                        
            // draw region outlines if required:
            if(landMass.isShowLandMassOutlines())
            {
                // get a scaled version of them image to workwith
                Image scaledImage = midImage.getScaledInstance(newWidth,newHeight,imageScalingOption);
                BufferedImage bim = new BufferedImage(newWidth, newHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
                bim.createGraphics().drawImage(scaledImage, 0, 0, null);
                
                Graphics2D g2 = (Graphics2D)bim.getGraphics();
                drawLandMasses(g2, newWidth, newHeight);
                
                im = new ImageIcon(bim); // create image icon
            }
            else // no land mass drawing
            {
                // just resize buffered image and get going
                im = new ImageIcon(midImage.getScaledInstance(newWidth,newHeight,imageScalingOption));
            }
             
            imageMap.setImageWidth(newWidth);
            imageMap.setImageHeight(newHeight);
            imageMap.setIcon(im);  
            
        }
        else
        {
            System.out.println("Height=0");
        }
    }
    
    public BufferedImage getBufferedImage(ImageIcon img)
    {
        //Component c = e.getComponent();
        //System.out.println("componentResized event from "+ c.getClass().getName()+ "; new size: "+ c.getSize().width+ ", "  + c.getSize().height);
        
        
        Image image = img.getImage();
        BufferedImage bimage = null;
        
        boolean hasAlpha = hasAlpha(image);
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try
        {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha)
            {
                transparency = Transparency.BITMASK;
            }
            
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        }
        catch (HeadlessException e2)
        {
            // The system does not have a screen
        }
        
        if (bimage == null)
        {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha)
            {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return bimage;
        
    }
    
    
    public static boolean hasAlpha(Image image)
    {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage)
        {
            BufferedImage bimage = (BufferedImage)image;
            return bimage.getColorModel().hasAlpha();
        }
        
        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try
        {
            pg.grabPixels();
        }
        catch (InterruptedException e)
        {
        }
        
        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }
    
    public void itemStateChanged(ItemEvent e)
    {}
    public void componentHidden(ComponentEvent e)
    {}
    public void componentMoved(ComponentEvent e)
    {}
    public void componentShown(ComponentEvent e)
    {}
    
    // PopUp Menu ------------------------
//  An inner class to check whether mouse events are the popup trigger
    class MousePopupListener extends MouseAdapter
    {
        public void mousePressed(MouseEvent e)
        {
            checkPopup(e);
            
            // do something here to see if zoom was enabled.. and not right click
            // if so ... zoom in/out (set factor, and rescale image... etc).
            if(e.getButton() == 2) // middle button prints out lat/long
            {
                System.out.println("Loc (x,y): " + e.getX() + ", " + e.getY() );
                double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
                
                // in degrees
                System.out.println("Loc (l,l): " + ll[0] + ", " + ll[1] +"\n");
            }
            
            if(e.getButton() == 1 && zoomInToggleButton.isSelected())
            {
                // get lat/long click first before setting new zoom factor
                double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
                
                //System.out.println("Zoom In");
                // new zoom factor
                double newZoomFactor = imageMap.getZoomFactor()*imageMap.getZoomIncrementMultiplier();
                //newZoomFactor = Math.round(newZoomFactor*100.0)/100.0;
                imageMap.setZoomFactor( newZoomFactor );
                //System.out.println("Zoom Factor: " + newZoomFactor);
                
                // set center lat / long where clicked
                imageMap.setCenterLat(ll[0]);
                imageMap.setCenterLong(ll[1]);
                
                // rescale
                ScaleImageMap();
                
            } // zoom in
            
            if(e.getButton() == 1 && zoomOutToggleButton.isSelected())
            {
                // get lat/long click first before setting new zoom factor
                double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
                    
                //System.out.println("Zoom Out");
                double newZoomFactor =  imageMap.getZoomFactor()/imageMap.getZoomIncrementMultiplier();
                imageMap.setZoomFactor( newZoomFactor );
                             
                if(imageMap.getZoomFactor() <= 1.00001) // don't let it be less than 1
                {
                    imageMap.setZoomFactor(1.0);
                    
                    // reset center lat/long to 0.0 -- recenter image
                    imageMap.setCenterLat(0.0);
                    imageMap.setCenterLong(0.0); 
                }
                else
                {
                    // set center lat / long where clicked
                    imageMap.setCenterLat(ll[0]);
                    imageMap.setCenterLong(ll[1]);
                }
                
                //System.out.println("Zoom Factor: " + imageMap.getZoomFactor());
                
                // rescale
                ScaleImageMap();
            }
            
            if(e.getButton() == 1 && recenterToggleButton.isSelected())
            {
                // just recenter
                double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
                
                // save lat,long
                imageMap.setCenterLat(ll[0]);
                imageMap.setCenterLong(ll[1]);
                    
                // rescale
                ScaleImageMap();
                
            }
            
            // double click - reset zoom back to 1
            if(e.getClickCount() == 2 && e.getButton() == 1)
            {
                // set zoomFactor back to 1.0 and repaint/rescale
                imageMap.setZoomFactor(1.0);
                
                // reset center lat/long to 0.0 -- recenter image
                imageMap.setCenterLat(0.0);
                imageMap.setCenterLong(0.0);
                
                // rescale
                ScaleImageMap();
            }
            
            // if middle button hit, turn off zoom in/out  buttons
            if(e.getButton() == e.BUTTON2)
            {
                zoomInToggleButton.setSelected(false);
                zoomOutToggleButton.setSelected(false); 
                recenterToggleButton.setSelected(false);
            }
            
        } // mousePressed
        
        public void mouseClicked(MouseEvent e)
        {
            checkPopup(e);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            checkPopup(e);
        }
        
        private void checkPopup(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popup.show(J2DEarthPanel.this, e.getX(), e.getY());
            }
        }
    } // MousePopupListener class
    
    // An inner class to show when popup events occur
    class PopupPrintListener implements PopupMenuListener
    {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e)
        {
            //System.out.println("Popup menu will be visible!");
        }
        
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
        {
            //System.out.println("Popup menu will be invisible!");
        }
        
        public void popupMenuCanceled(PopupMenuEvent e)
        {
            //System.out.println("Popup menu is hidden!");
        }
    }  // PopupPrintListener class
    
    
    
    
    // get and set Options for saving and loading file
    public boolean getShowLatLonLines()
    {
        return imageMap.showLatLong;
    }
    
    public void setShowLatLonLines(boolean bool)
    {
        imageMap.showLatLong = bool;
        imageMap.repaint();
    }
    
    public String getTwoDMap()
    {
        //return imageMapNum; //0=PE, 1=NOAA
        return backgroundImagePath;
    }
    
    //public void setTwoDMap(int map)
    public void setTwoDMap(String map)
    {
        if(map.equalsIgnoreCase(backgroundImagePath))
        {
            // do nothing
            return;
        }
        
        backgroundImagePath = map;
        ImageIcon temp = createImageIcon(backgroundImagePath,"Earth");
        bimage = getBufferedImage(temp);
        ScaleImageMap(); // repaint the image
        
//        switch(map)
//        {
//            case 0:
//                ImageIcon temp = createImageIcon("/images/Earth_PE_small.png","Earth");
//                bimage = getBufferedImage(temp);
//                ScaleImageMap(); // repaint the image
//                earthNoaaMenu.setIcon(null);
//                earthPeMenu.setIcon(checkMark);
//                imageMapNum = 0; //0=PE, 1=NOAA
//                break;
//            case 1:
//                temp = createImageIcon("/images/Earth_NOAA_NGDC_small.png","Earth");
//                bimage = getBufferedImage(temp);
//                ScaleImageMap(); // repaint the image
//                earthNoaaMenu.setIcon(checkMark);
//                earthPeMenu.setIcon(null);
//                imageMapNum = 1; //0=PE, 1=NOAA
//                break;
//            default:
//                temp = createImageIcon("/images/Earth_NOAA_NGDC_small.png","Earth");
//                bimage = getBufferedImage(temp);
//                ScaleImageMap(); // repaint the image
//                earthNoaaMenu.setIcon(checkMark);
//                earthPeMenu.setIcon(null);
//                imageMapNum = 1; //0=PE, 1=NOAA
//                break;
//        } // switch
    } // setTwoDMap

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.setBackground(backgroundColor);
        
        this.backgroundColor = backgroundColor;
        imageMap.setBackgroundColor(backgroundColor);
    }

//    public int getImageMapNum()
//    {
//        return imageMapNum;
//    }

//    public void setImageMapNum(int imageMapNum)
//    {
//        this.imageMapNum = imageMapNum;
//        
//        if( imageMapNum == 0)
//        {
//            ImageIcon temp = createImageIcon("/images/Earth_PE_small.png","Earth");
//            bimage = getBufferedImage(temp);
//            ScaleImageMap(); // repaint the image
//            earthNoaaMenu.setIcon(null);
//            earthPeMenu.setIcon(checkMark);
//            
//            imageMapNum = 0; //0=PE, 1=NOAA
//            
//        }
//        
//        if( imageMapNum == 1)
//        {
//            ImageIcon temp = createImageIcon("/images/Earth_NOAA_NGDC_small.png","Earth");
//            bimage = getBufferedImage(temp);
//            ScaleImageMap(); // repaint the image
//            earthNoaaMenu.setIcon(checkMark);
//            earthPeMenu.setIcon(null);
//            
//            imageMapNum =1; //0=PE, 1=NOAA
//        }
//    } // setImageMapNum
    
    
    // Graw on landmasses
    private void drawLandMasses(Graphics2D g2, int width, int height)
    {
        //System.out.println("drawing land masses");
        
        // --- land mass -------------------
        if(landMass.isShowLandMassOutlines())
        {
            g2.setPaint(landMass.getLandOutlineColor());
            
            for(String key : landMass.getLandMassHash().keySet())
            {
                Vector<float[]> vec = landMass.getLandMassVector(key);
                
                if( vec.size() > 0 )
                {
                    
                    //System.out.println("here");
                    
                    float[] lastLL = vec.elementAt(0);
                    //int[] lastXY = imageMap.findXYfromLL(lastLL[0], lastLL[1], w, h, imageWidth, imageHeight);
                    int[] lastXY = imageMap.findXYfromLL(lastLL[0], lastLL[1], width, height, width, height);
                    
                    float[] currLL;
                    int[] currXY;
                    
                    for(int i=1;i<vec.size();i+=indexSpacing)
                    {
                        currLL = vec.elementAt(i);
                        //currXY = imageMap.findXYfromLL(currLL[0], currLL[1], w, h, imageWidth, imageHeight);
                        currXY = imageMap.findXYfromLL(currLL[0], currLL[1], width, height, width, height);
                        
                        g2.drawLine(lastXY[0],lastXY[1],currXY[0],currXY[1]);
                        
                        // save current to old
                        lastXY = currXY;
                        
                    }
                    
                    // first position
                    currLL = vec.elementAt(0);
                    currXY = imageMap.findXYfromLL(currLL[0], currLL[1], width, height, width, height);
                    
                    // only draw end point connection if not too far away
                    if(Math.sqrt( Math.pow(currXY[0] - lastXY[0],2)+Math.pow(currXY[1] - lastXY[1],2)) > 15)
                    {
                        //System.out.println(">15 detected: " + key + ", end");
                    }
                    else
                    {
                        // connect back to first
                        g2.drawLine(lastXY[0],lastXY[1],currXY[0],currXY[1]);
                    }
                    
                }
                
            } // for each land mass
            
        } // show land mass
        // ---- end: land mass --------------
    }
    
    public double getZoomIncrementMultiplier()
    {
        return imageMap.getZoomIncrementMultiplier();
    }

    public void setZoomIncrementMultiplier(double zoomIncrementMultiplier)
    {
        imageMap.setZoomIncrementMultiplier(zoomIncrementMultiplier);
    }

    public int getImageScalingOption()
    {
        return imageScalingOption;
    }

    public void setImageScalingOption(int imageScalingOption)
    {
        this.imageScalingOption = imageScalingOption;
    }
    
    public boolean isShowDateTime()
    {
        return imageMap.isShowDateTime();
    }

    public void setShowDateTime(boolean showDateTime)
    {
        imageMap.setShowDateTime(showDateTime);
    }
    
    public int getXDateTimeOffset()
    {
        return imageMap.getXDateTimeOffset();
    }

    public void setXDateTimeOffset(int xDateTimeOffset)
    {
        imageMap.setXDateTimeOffset( xDateTimeOffset );
    }

    public int getYDateTimeOffset()
    {
        return imageMap.getYDateTimeOffset();
    }

    public void setYDateTimeOffset(int yDateTimeOffset)
    {
        imageMap.setYDateTimeOffset(yDateTimeOffset);
    }

    public Color getDateTimeColor()
    {
        return imageMap.getDateTimeColor();
    }

    public void setDateTimeColor(Color dateTimeColor)
    {
        imageMap.setDateTimeColor(dateTimeColor);
    }
    
//    public void prepare2Save()
//    {
//        // routines to prepare object for saving to make file size smaller
//        this.setSize(1,1);
//    }
     public double getCenterLat()
    {
        return imageMap.getCenterLat();
    }

    public void setCenterLat(double centerLat)
    {
        imageMap.setCenterLat(centerLat);
    }

    public double getCenterLong()
    {
        return imageMap.getCenterLong();
    }

    public void setCenterLong(double centerLong)
    {
        imageMap.setCenterLong(centerLong);
    }

    public double getZoomFactor()
    {
        return imageMap.getZoomFactor();
    }

    public void setZoomFactor(double zoomFactor)
    {
        imageMap.setZoomFactor(zoomFactor);
    }
    
    public boolean isDrawSun()
    {
        return imageMap.isDrawSun();
    }

    public void setDrawSun(boolean drawSun)
    {
        imageMap.setDrawSun(drawSun);
    }

    public Color getSunColor()
    {
        return imageMap.getSunColor();
    }

    public void setSunColor(Color sunColor)
    {
        imageMap.setSunColor(sunColor);
    }

    public int getNumPtsSunFootPrint()
    {
        return imageMap.getNumPtsSunFootPrint();
    }

    public void setNumPtsSunFootPrint(int numPtsSunFootPrint)
    {
        imageMap.setNumPtsSunFootPrint(numPtsSunFootPrint);
    }

    public float getSunAlpha()
    {
        return imageMap.getSunAlpha();
    }

    public void setSunAlpha(float sunAlpha)
    {
        imageMap.setSunAlpha(sunAlpha);
    }
    
    public String getPathRegionFiles()
    {
        return landMass.getRootDir();
    }
    
    public String getRegionFileName()
    {
        return landMass.getDataFileName();
    }
    
    public boolean isRegionDrawOn()
    {
        return landMass.isShowLandMassOutlines();
    }
    
    public Color getRegionLineColor()
    {
        return landMass.getLandOutlineColor();
    }  
    
    public void setRegionDrawingOptions(boolean bool, String fileName, Color color)
    {
        // save old data
        String oldName = getRegionFileName(); // save old
        boolean preBool = landMass.isShowLandMassOutlines();
        Color prevColor = landMass.getLandOutlineColor();
        
        //save data
        landMass.setRegionOptions(bool,fileName,color);
        
        // see if any changes
        if(!oldName.equalsIgnoreCase(fileName) || preBool != bool || prevColor != color)
        {
            ScaleImageMap(); // repaint the image
        }
        
    } // setRegionDrawingOptions
    
    /**
     * Add renderable object to the 2D Earth Label
     * @param renderable
     */
    public void addRenderableObject(JSatTrakRenderable renderable)
    {
        if(!checkIfIncludesRenderableObject(renderable))
        {
            imageMap.addRenderableObject(renderable);
        }
    }
    
    public boolean checkIfIncludesRenderableObject(JSatTrakRenderable renderable)
    {
        // check to see if object is in list
        for(JSatTrakRenderable ren : imageMap.getRenderableObjects())
        {
            //if(ren instanceof CoverageAnalyzer)
            if(ren.equals(renderable))
            {   
                return true;
            }
        }
        return false;
    }
    
    public void removeRenderableObject(JSatTrakRenderable renderable)
    {
        Vector<JSatTrakRenderable> renVec = imageMap.getRenderableObjects();
        for(int i=0;i<renVec.size();i++)
        {
            //if(ren instanceof CoverageAnalyzer)
            if(renVec.get(i).equals(renderable))
            {   
                // remove it
                renVec.remove(renderable);
            }
        }
    }
}