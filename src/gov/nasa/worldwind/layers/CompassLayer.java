/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import com.sun.opengl.util.texture.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.view.*;
import gov.nasa.worldwind.View;

import javax.media.opengl.GL;
import java.awt.*;
import java.io.*;

/**
 * @author tag
 * @version $Id$
 */
public class CompassLayer extends AbstractLayer
{
    public final static String NORTHWEST = "gov.nasa.worldwind.CompassLayer.NorthWest";
    public final static String SOUTHWEST = "gov.nasa.worldwind.CompassLayer.SouthWest";
    public final static String NORTHEAST = "gov.nasa.worldwind.CompassLayer.NorthEast";
    public final static String SOUTHEAST = "gov.nasa.worldwind.CompassLayer.SouthEast";

    /**
     * On window resize, scales the compass icon to occupy a constant relative size of the viewport.
     */
    public final static String RESIZE_STRETCH = "gov.nasa.worldwind.CompassLayer.ResizeStretch";
    /**
     * On window resize, scales the compass icon to occupy a constant relative size of the viewport, but not larger than
     * the icon's inherent size scaled by the layer's icon scale factor.
     */
    public final static String RESIZE_SHRINK_ONLY = "gov.nasa.worldwind.CompassLayer.ResizeShrinkOnly";
    /**
     * Does not modify the compass icon size when the window changes size.
     */
    public final static String RESIZE_KEEP_FIXED_SIZE = "gov.nasa.worldwind.CompassLayer.ResizeKeepFixedSize";

    private String iconFilePath = "images/notched-compass.png"; // TODO: make configurable
    private double compassToViewportScale = 0.2; // TODO: make configurable
    private double iconScale = 0.5;
    private int borderWidth = 20; // TODO: make configurable
    private String position = NORTHEAST; // TODO: make configurable
    private String resizeBehavior = RESIZE_SHRINK_ONLY;
    private int iconWidth;
    private int iconHeight;
    private Vec4 locationCenter = null;
    private boolean showTilt = true;

    // Draw it as ordered with an eye distance of 0 so that it shows up in front of most other things.
    private OrderedIcon orderedImage = new OrderedIcon();

    private class OrderedIcon implements OrderedRenderable
    {
        public double getDistanceFromEye()
        {
            return 0;
        }

        public void pick(DrawContext dc, Point pickPoint)
        {
            // Not implemented
        }

        public void render(DrawContext dc)
        {
            CompassLayer.this.draw(dc);
        }
    }

    public CompassLayer()
    {
        this.setOpacity(0.8); // TODO: make configurable
    }

    public CompassLayer(String iconFilePath)
    {
        this.setIconFilePath(iconFilePath);
        this.setOpacity(0.8); // TODO: make configurable
    }

    /**
     * Returns the layer's current icon file path.
     *
     * @return the icon file path
     */
    public String getIconFilePath()
    {
        return iconFilePath;
    }

    /**
     * Sets the compass icon's image location. The layer first searches for this location in the current Java classpath.
     * If not found then the specified path is assumed to refer to the local file system. found there then the
     *
     * @param iconFilePath the path to the icon's image file
     */
    public void setIconFilePath(String iconFilePath)
    {
        if (iconFilePath == null)
        {
            String message = Logging.getMessage("nullValue.IconFilePath");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.iconFilePath = iconFilePath;
    }

    /**
     * Returns the layer's compass-to-viewport scale factor.
     *
     * @return the compass-to-viewport scale factor
     */
    public double getCompassToViewportScale()
    {
        return compassToViewportScale;
    }

    /**
     * Sets the scale factor applied to the viewport size to determine the displayed size of the compass icon. This
     * scale factor is used only when the layer's resize behavior is {@link #RESIZE_STRETCH} or {@link
     * #RESIZE_SHRINK_ONLY}. The icon's width is adjusted to occupy the proportion of the viewport's width indicated by
     * this factor. The icon's height is adjusted to maintain the compass image's native aspect ratio.
     *
     * @param compassToViewportScale the compass to viewport scale factor
     */
    public void setCompassToViewportScale(double compassToViewportScale)
    {
        this.compassToViewportScale = compassToViewportScale;
    }

    /**
     * Returns the icon scale factor. See {@link #setIconScale(double)} for a description of the scale factor.
     *
     * @return the current icon scale
     */
    public double getIconScale()
    {
        return iconScale;
    }

    /**
     * Sets the scale factor defining the displayed size of the compass icon relative to the icon's width and height in
     * its image file. Values greater than 1 magify the image, values less than one minify it. If the layer's resize
     * behavior is other than {@link #RESIZE_KEEP_FIXED_SIZE}, the icon's displayed sized is further affected by the
     * value specified by {@link #setCompassToViewportScale(double)} and the current viewport size.
     *
     * @param iconScale the icon scale factor
     */
    public void setIconScale(double iconScale)
    {
        this.iconScale = iconScale;
    }

    /**
     * Returns the compass icon's resize behavior.
     *
     * @return the icon's resize behavior
     */
    public String getResizeBehavior()
    {
        return resizeBehavior;
    }

    /**
     * Sets the behavior the layer uses to size the compass icon when the viewport size changes, typically when the
     * World Wind window is resized. If the value is {@link #RESIZE_KEEP_FIXED_SIZE}, the icon size is kept to the size
     * specified in its image file scaled by the layer's current icon scale. If the value is {@link #RESIZE_STRETCH},
     * the icon is resized to have a constant size relative to the current viewport size. If the viewport shrinks the
     * icon size decreases; if it expands then the icon file enlarges. The relative size is determined by the current
     * compass-to-viewport scale and by the icon's image file size scaled by the current icon scale. If the value is
     * {@link #RESIZE_SHRINK_ONLY} (the default), icon sizing behaves as for {@link #RESIZE_STRETCH} but the icon will
     * not grow larger than the size specified in its image file scaled by the current icon scale.
     *
     * @param resizeBehavior the desired resize behavior
     */
    public void setResizeBehavior(String resizeBehavior)
    {
        this.resizeBehavior = resizeBehavior;
    }

    public int getBorderWidth()
    {
        return borderWidth;
    }

    /**
     * Sets the compass icon offset from the viewport border.
     *
     * @param borderWidth the number of pixels to offset the compass icon from the borders indicated by {@link
     *                    #setPosition(String)}.
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    /**
     * Returns the current relative compass icon position.
     *
     * @return the current compass position
     */
    public String getPosition()
    {
        return position;
    }

    /**
     * Sets the relative viewport location to display the compass icon. Can be one of {@link #NORTHEAST} (the default),
     * {@link #NORTHWEST}, {@link #SOUTHEAST}, or {@link #SOUTHWEST}. These indicate the corner of the viewport to place
     * the icon.
     *
     * @param position the desired compass position
     */
    public void setPosition(String position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.CompassPositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.position = position;
    }

    public Vec4 getLocationCenter()
    {
        return locationCenter;
    }

    public void setLocationCenter(Vec4 locationCenter)
    {
        this.locationCenter = locationCenter;
    }

    protected void doRender(DrawContext dc)
    {
        dc.addOrderedRenderable(this.orderedImage);
    }

    public boolean isShowTilt()
    {
        return showTilt;
    }

    public void setShowTilt(boolean showTilt)
    {
        this.showTilt = showTilt;
    }

    private void draw(DrawContext dc)
    {
        if (this.iconFilePath == null)
            return;

        GL gl = dc.getGL();

        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try
        {
            gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT
                | GL.GL_COLOR_BUFFER_BIT
                | GL.GL_ENABLE_BIT
                | GL.GL_TEXTURE_BIT
                | GL.GL_TRANSFORM_BIT
                | GL.GL_VIEWPORT_BIT
                | GL.GL_CURRENT_BIT);
            attribsPushed = true;

            Texture iconTexture = dc.getTextureCache().get(this);
            if (iconTexture == null)
            {
                this.initializeTexture(dc);
                iconTexture = dc.getTextureCache().get(this);
                if (iconTexture == null)
                {
                    // TODO: log warning
                    return;
                }
            }

            gl.glEnable(GL.GL_TEXTURE_2D);
            iconTexture.bind();

            gl.glColor4d(1d, 1d, 1d, this.getOpacity());
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDisable(GL.GL_DEPTH_TEST);

            double width = this.getScaledIconWidth();
            double height = this.getScaledIconHeight();

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            java.awt.Rectangle viewport = dc.getView().getViewport();
            gl.glMatrixMode(javax.media.opengl.GL.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double maxwh = width > height ? width : height;
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();

            double scale = this.computeScale(viewport);
            Vec4 locationSW = this.computeLocation(viewport, scale);
            double heading = this.computeHeading(dc.getView());
            double pitch = this.computePitch(dc.getView());

            gl.glTranslated(locationSW.x, locationSW.y, locationSW.z);
            gl.glScaled(scale, scale, 1);

            gl.glTranslated(width / 2, height / 2, 0);
            if (this.showTilt) // formula contributed by Ty Hayden
                gl.glRotated(70d * (pitch / 90.0), 1d, 0d, 0d);
            gl.glRotated(heading, 0d, 0d, 1d);
            gl.glTranslated(-width / 2, -height / 2, 0);

            TextureCoords texCoords = iconTexture.getImageTexCoords();
            gl.glScaled(width, height, 1d);
            dc.drawUnitQuad(texCoords);
        }
        finally
        {
            if (projectionPushed)
            {
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if (modelviewPushed)
            {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if (attribsPushed)
                gl.glPopAttrib();
        }
    }

    private double computeScale(java.awt.Rectangle viewport)
    {
        if (this.resizeBehavior.equals(RESIZE_SHRINK_ONLY))
        {
            return Math.min(1d, (this.compassToViewportScale) * viewport.width / this.getScaledIconWidth());
        }
        else if (this.resizeBehavior.equals(RESIZE_STRETCH))
        {
            return (this.compassToViewportScale) * viewport.width / this.getScaledIconWidth();
        }
        else if (this.resizeBehavior.equals(RESIZE_KEEP_FIXED_SIZE))
        {
            return 1d;
        }
        else
        {
            return 1d;
        }
    }

    private double getScaledIconWidth()
    {
        return this.iconWidth * this.iconScale;
    }

    private double getScaledIconHeight()
    {
        return this.iconHeight * this.iconScale;
    }

    private Vec4 computeLocation(java.awt.Rectangle viewport, double scale)
    {
        double width = this.getScaledIconWidth();
        double height = this.getScaledIconHeight();

        double scaledWidth = scale * width;
        double scaledHeight = scale * height;

        double x;
        double y;

        if (this.locationCenter != null)
        {
            x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
            y = viewport.getHeight() - scaledHeight / 2 - this.borderWidth;
        }
        else if (this.position.equals(NORTHEAST))
        {
            x = viewport.getWidth() - scaledWidth - this.borderWidth;
            y = viewport.getHeight() - scaledHeight - this.borderWidth;
        }
        else if (this.position.equals(SOUTHEAST))
        {
            x = viewport.getWidth() - scaledWidth - this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else if (this.position.equals(NORTHWEST))
        {
            x = 0d + this.borderWidth;
            y = viewport.getHeight() - scaledHeight - this.borderWidth;
        }
        else if (this.position.equals(SOUTHWEST))
        {
            x = 0d + this.borderWidth;
            y = 0d + this.borderWidth;
        }
        else // use North East
        {
            x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
            y = viewport.getHeight() - scaledHeight / 2 - this.borderWidth;
        }

        return new Vec4(x, y, 0);
    }

    private double computeHeading(View view)
    {
        if (view == null)
            return 0.0;

        if (!(view instanceof OrbitView))
            return 0.0;

        OrbitView orbitView = (OrbitView) view;
        return orbitView.getHeading().getDegrees();
    }

    private double computePitch(View view)
    {
        if (view == null)
            return 0.0;

        if (!(view instanceof OrbitView))
            return 0.0;

        OrbitView orbitView = (OrbitView) view;
        return orbitView.getPitch().getDegrees();
    }

    private void initializeTexture(DrawContext dc)
    {
        Texture iconTexture = dc.getTextureCache().get(this);
        if (iconTexture != null)
            return;

        try
        {
            InputStream iconStream = this.getClass().getResourceAsStream("/" + this.iconFilePath);
            if (iconStream == null)
            {
                File iconFile = new File(this.iconFilePath);
                if (iconFile.exists())
                {
                    iconStream = new FileInputStream(iconFile);
                }
            }

            iconTexture = TextureIO.newTexture(iconStream, true, null);
            iconTexture.bind();
            this.iconWidth = iconTexture.getWidth();
            this.iconHeight = iconTexture.getHeight();
            dc.getTextureCache().put(this, iconTexture);
        }
        catch (IOException e)
        {
            String msg = Logging.getMessage("layers.IOExceptionDuringInitialization");
            Logging.logger().severe(msg);
            throw new WWRuntimeException(msg, e);
        }

        GL gl = dc.getGL();
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        // Enable texture anisotropy, improves "tilted" compass quality.
        int[] maxAnisotropy = new int[1];
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy[0]);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.CompassLayer.Name");
    }
}
