// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ImageLabel.java

package com.ibm.media.bean.multiplayer;

import java.awt.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageLabel extends Canvas
{

    public ImageLabel()
    {
        imageString = "<Existing Image>";
        debug = false;
        border = 0;
        borderColor = null;
        width = 80;
        height = 60;
        explicitSize = false;
        explicitWidth = 0;
        explicitHeight = 0;
        doneLoading = false;
        newEventsOnly = false;
    }

    public ImageLabel(String imageURLString)
    {
        this(makeURL(imageURLString));
    }

    public ImageLabel(URL imageURL)
    {
        this(loadImage(imageURL));
        imageString = imageURL.toExternalForm();
    }

    public ImageLabel(URL imageDirectory, String file)
    {
        this(makeURL(imageDirectory, file));
        imageString = file;
    }

    public ImageLabel(Image image)
    {
        imageString = "<Existing Image>";
        debug = false;
        border = 0;
        borderColor = null;
        width = 80;
        height = 60;
        explicitSize = false;
        explicitWidth = 0;
        explicitHeight = 0;
        doneLoading = false;
        newEventsOnly = false;
        this.image = image;
        tracker = new MediaTracker(this);
        currentTrackerID = lastTrackerID++;
        tracker.addImage(image, currentTrackerID);
    }

    public void waitForImage(boolean doLayout)
    {
        if(!doneLoading)
        {
            debug("[waitForImage] - Resizing and waiting for " + imageString);
            try
            {
                tracker.waitForID(currentTrackerID);
            }
            catch(InterruptedException ie) { }
            catch(Exception e)
            {
                System.out.println("Error loading " + imageString + ": " + e.getMessage());
                e.printStackTrace();
            }
            if(tracker.isErrorID(0))
                (new Throwable("Error loading image " + imageString)).printStackTrace();
            doneLoading = true;
            if(explicitWidth != 0)
                width = explicitWidth;
            else
                width = image.getWidth(this) + 2 * border;
            if(explicitHeight != 0)
                height = explicitHeight;
            else
                height = image.getHeight(this) + 2 * border;
            resize(width, height);
            debug("[waitForImage] - " + imageString + " is " + width + "x" + height + ".");
            if((parentContainer = getParent()) != null && doLayout)
            {
                setBackground(parentContainer.getBackground());
                parentContainer.layout();
            }
        }
    }

    public void centerAt(int x, int y)
    {
        debug("Placing center of " + imageString + " at (" + x + "," + y + ")");
        move(x - width / 2, y - height / 2);
    }

    public synchronized boolean inside(int x, int y)
    {
        return x >= 0 && x <= width && y >= 0 && y <= height;
    }

    public void paint(Graphics g)
    {
        if(!doneLoading)
        {
            waitForImage(true);
        } else
        {
            if(explicitSize)
                g.drawImage(image, border, border, width - 2 * border, height - 2 * border, this);
            else
                g.drawImage(image, border, border, this);
            drawRect(g, 0, 0, width - 1, height - 1, border, borderColor);
        }
    }

    public Dimension preferredSize()
    {
        if(!doneLoading)
            waitForImage(false);
        return super.preferredSize();
    }

    public Dimension minimumSize()
    {
        if(!doneLoading)
            waitForImage(false);
        return super.minimumSize();
    }

    public void resize(int width, int height)
    {
        if(!doneLoading)
        {
            explicitSize = true;
            if(width > 0)
                explicitWidth = width;
            if(height > 0)
                explicitHeight = height;
        }
        super.resize(width, height);
    }

    public void reshape(int x, int y, int width, int height)
    {
        if(!doneLoading)
        {
            explicitSize = true;
            if(width > 0)
                explicitWidth = width;
            if(height > 0)
                explicitHeight = height;
        }
        super.reshape(x, y, width, height);
    }

    protected void drawRect(Graphics g, int left, int top, int width, int height, int lineThickness, Color rectangleColor)
    {
        g.setColor(rectangleColor);
        for(int i = 0; i < lineThickness; i++)
        {
            g.drawRect(left, top, width, height);
            if(i < lineThickness - 1)
            {
                left++;
                top++;
                width -= 2;
                height -= 2;
            }
        }

    }

    protected void debug(String message)
    {
        if(debug)
            System.out.println(message);
    }

    private static URL makeURL(String s)
    {
        URL u = null;
        try
        {
            u = new URL(s);
        }
        catch(MalformedURLException mue)
        {
            System.out.println("Bad URL " + s + ": " + mue);
            mue.printStackTrace();
        }
        return u;
    }

    private static URL makeURL(URL directory, String file)
    {
        URL u = null;
        try
        {
            u = new URL(directory, file);
        }
        catch(MalformedURLException mue)
        {
            System.out.println("Bad URL " + directory.toExternalForm() + ", " + file + ": " + mue);
            mue.printStackTrace();
        }
        return u;
    }

    private static Image loadImage(URL url)
    {
        Image original = null;
        if(url.getProtocol().equals("file"))
            try
            {
                InputStream imageStream = url.openStream();
                if(imageStream == null)
                {
                    System.out.println("null button image stream");
                    return null;
                }
                int available = 0;
                while(imageStream.available() == 0) ;
                available = imageStream.available();
                byte imageBytes[] = new byte[available];
                imageStream.read(imageBytes);
                original = Toolkit.getDefaultToolkit().createImage(imageBytes);
            }
            catch(IOException ioe)
            {
                System.out.println("Cannot read button image.");
                return null;
            }
        else
            original = Toolkit.getDefaultToolkit().getImage(url);
        return original;
    }

    public Image getImage()
    {
        return image;
    }

    public int getBorder()
    {
        return border;
    }

    public void setBorder(int border)
    {
        this.border = border;
    }

    public Color getBorderColor()
    {
        return borderColor;
    }

    public void setBorderColor(Color borderColor)
    {
        this.borderColor = borderColor;
    }

    public int getWidth()
    {
        return width;
    }

    public int getHeight()
    {
        return height;
    }

    protected boolean hasExplicitSize()
    {
        return explicitSize;
    }

    public static String getDefaultImageString()
    {
        return defaultImageString;
    }

    public static void setDefaultImageString(String file)
    {
        defaultImageString = file;
    }

    protected String getImageString()
    {
        return imageString;
    }

    public boolean isDebugging()
    {
        return debug;
    }

    public void setIsDebugging(boolean debug)
    {
        this.debug = debug;
    }

    protected Image image;
    protected static String defaultImageString = null;
    private String imageString;
    private boolean debug;
    protected int border;
    protected Color borderColor;
    protected int width;
    protected int height;
    protected boolean explicitSize;
    private int explicitWidth;
    private int explicitHeight;
    private MediaTracker tracker;
    private static int lastTrackerID = 0;
    private int currentTrackerID;
    protected boolean doneLoading;
    private Container parentContainer;
    boolean newEventsOnly;

}
