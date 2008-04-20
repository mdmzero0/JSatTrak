/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.Earth;

import com.sun.opengl.util.texture.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.Configuration;

import javax.media.opengl.GL;
import java.awt.*;
import java.io.*;

/**
 * Displays a world map overlay with a current position crosshair in a screen corner. Supports picking at a position on
 * the map.
 *
 * @author Patrick Murris
 * @version $Id$
 */
public class WorldMapLayer extends RenderableLayer
{
    // Positionning constants
    public final static String NORTHWEST = "gov.nasa.worldwind.WorldmapLayer.NorthWest";
    public final static String SOUTHWEST = "gov.nasa.worldwind.WorldmapLayer.SouthWest";
    public final static String NORTHEAST = "gov.nasa.worldwind.WorldmapLayer.NorthEast";
    public final static String SOUTHEAST = "gov.nasa.worldwind.WorldmapLayer.SouthEast";
    // Stretching behavior constants
    public final static String RESIZE_STRETCH = "gov.nasa.worldwind.WorldmapLayer.Stretch";
    public final static String RESIZE_SHRINK_ONLY = "gov.nasa.worldwind.WorldmapLayer.ShrinkOnly";
    public final static String RESIZE_KEEP_FIXED_SIZE = "gov.nasa.worldwind.WorldmapLayer.FixedSize";

    private String iconFilePath;
    private double toViewportScale = 0.2; // TODO: make configurable
    private double iconScale = 0.5;
    private int borderWidth = 20; // TODO: make configurable
    private String position = NORTHWEST; // TODO: make configurable
    private String resizeBehavior = RESIZE_SHRINK_ONLY;
    private int iconWidth;
    private int iconHeight;
    private Vec4 locationCenter = null;
    private Color color = Color.white;
    private Color backColor = new Color(0f, 0f, 0f, 0.4f);
    private PickSupport pickSupport = new PickSupport();
    private double pickAltitude = 1000e3;        // Altitude for picked position

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
            WorldMapLayer.this.drawIcon(dc);
        }

        public void render(DrawContext dc)
        {
            WorldMapLayer.this.drawIcon(dc);
        }
    }

    /**
     * Displays a world map overlay with a current position crosshair in a screen corner
     */
    public WorldMapLayer()
    {
        this.setName(Logging.getMessage("layers.Earth.WorldMapLayer.Name"));
        this.setOpacity(0.6);
        this.setIconFilePath(Configuration.getStringValue(AVKey.WORLD_MAP_IMAGE_PATH));
    }

    /**
     * Displays a world map overlay with a current position crosshair in a screen corner
     *
     * @param iconFilePath the world map image path and filename
     */
    public WorldMapLayer(String iconFilePath)
    {
        this.setName(Logging.getMessage("layers.Earth.WorldMapLayer.Name"));
        this.setOpacity(0.6);
        this.setIconFilePath(iconFilePath);
    }

    // Public properties
    
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
     * Sets the world map icon's image location. The layer first searches for this location in the current Java classpath.
     * If not found then the specified path is assumed to refer to the local file system. found there then the
     *
     * @param iconFilePath the path to the icon's image file
     */
    public void setIconFilePath(String iconFilePath)
    {
        if (iconFilePath == null || iconFilePath.length() == 0)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.iconFilePath = iconFilePath;
    }

    /**
     * Returns the layer's world map-to-viewport scale factor.
     *
     * @return the world map-to-viewport scale factor
     */
    public double getToViewportScale()
    {
        return toViewportScale;
    }

    /**
     * Sets the scale factor applied to the viewport size to determine the displayed size of the world map icon. This scale
     * factor is used only when the layer's resize behavior is {@link #RESIZE_STRETCH} or {@link #RESIZE_SHRINK_ONLY}. The
     * icon's width is adjusted to occupy the proportion of the viewport's width indicated by this factor. The icon's
     * height is adjusted to maintain the world map image's native aspect ratio.
     *
     * @param toViewportScale the world map to viewport scale factor
     */
    public void setToViewportScale(double toViewportScale)
    {
        this.toViewportScale = toViewportScale;
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
     * Sets the scale factor defining the displayed size of the world map icon relative to the icon's width and height in
     * its image file. Values greater than 1 magify the image, values less than one minify it. If the layer's resize
     * behavior is other than {@link #RESIZE_KEEP_FIXED_SIZE}, the icon's displayed sized is further affected by the value
     * specified by {@link #setToViewportScale(double)} and the current viewport size.
     *
     * @param iconScale the icon scale factor
     */
    public void setIconScale(double iconScale)
    {
        this.iconScale = iconScale;
    }

    /**
     * Returns the world map icon's resize behavior.
     *
     * @return the icon's resize behavior
     */
    public String getResizeBehavior()
    {
        return resizeBehavior;
    }

    /**
     * Sets the behavior the layer uses to size the world map icon when the viewport size changes, typically when the World
     * Wind window is resized. If the value is {@link #RESIZE_KEEP_FIXED_SIZE}, the icon size is kept to the size specified
     * in its image file scaled by the layer's current icon scale. If the value is {@link #RESIZE_STRETCH}, the icon is
     * resized to have a constant size relative to the current viewport size. If the viewport shrinks the icon size
     * decreases; if it expands then the icon file enlarges. The relative size is determined by the current world
     * map-to-viewport scale and by the icon's image file size scaled by the current icon scale. If the value is {@link
     * #RESIZE_SHRINK_ONLY} (the default), icon sizing behaves as for {@link #RESIZE_STRETCH} but the icon will not grow
     * larger than the size specified in its image file scaled by the current icon scale.
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
     * Sets the world map icon offset from the viewport border.
     *
     * @param borderWidth the number of pixels to offset the world map icon from the borders indicated by {@link
     *                    #setPosition(String)}.
     */
    public void setBorderWidth(int borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    /**
     * Returns the current relative world map icon position.
     *
     * @return the current world map position
     */
    public String getPosition()
    {
        return position;
    }

    /**
     * Sets the relative viewport location to display the world map icon. Can be one of {@link #NORTHEAST} (the default),
     * {@link #NORTHWEST}, {@link #SOUTHEAST}, or {@link #SOUTHWEST}. These indicate the corner of the viewport to place
     * the icon.
     *
     * @param position the desired world map position
     */
    public void setPosition(String position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
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

    public Color getBackgrounColor()
    {
        return this.backColor;
    }

    public void setBackgroundColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.backColor = color;
    }

    @Override
    public void doRender(DrawContext dc)
    {
        // Delegate drawing to the ordered renderable list
        dc.addOrderedRenderable(this.orderedImage);
    }

    @Override
    public void doPick(DrawContext dc, Point pickPoint)
    {
        // Delegate drawing to the ordered renderable list
        dc.addOrderedRenderable(this.orderedImage);
    }

    private void drawIcon(DrawContext dc)
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

            // Initialize texture if not done yet
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

            // Translate and scale
            double scale = this.computeScale(viewport);
            Vec4 locationSW = this.computeLocation(viewport, scale);
            gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
            // Scale to 0..1 space
            gl.glScaled(scale, scale, 1);
            gl.glScaled(width, height, 1d);

            if (!dc.isPickingMode())
            {
                // Draw background color behind the map
                gl.glColor4ub((byte) this.backColor.getRed(), (byte) this.backColor.getGreen(),
                    (byte) this.backColor.getBlue(), (byte) this.backColor.getAlpha());
                gl.glDisable(GL.GL_TEXTURE_2D);        // no textures
                gl.glBegin(GL.GL_POLYGON);
                gl.glVertex3d(0, 0, 0);
                gl.glVertex3d(1, 0, 0);
                gl.glVertex3d(1, 1, 0);
                gl.glVertex3d(0, 1, 0);
                gl.glVertex3d(0, 0, 0);
                gl.glEnd();

                // Draw world map icon
                gl.glColor4d(1d, 1d, 1d, this.getOpacity());
                gl.glEnable(GL.GL_TEXTURE_2D);
                iconTexture.bind();

                TextureCoords texCoords = iconTexture.getImageTexCoords();
                dc.drawUnitQuad(texCoords);

                // Draw crosshair for current location
                gl.glLoadIdentity();
                gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
                // Scale to width x height space
                gl.glScaled(scale, scale, 1);
                // Set color
                float[] colorRGB = this.color.getRGBColorComponents(null);
                gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], this.getOpacity());
                gl.glDisable(GL.GL_TEXTURE_2D);        // no textures

                // Find coordinates in map icon space
                Position groundPos = this.computeGroundPosition(dc, dc.getView());
                if (groundPos != null)
                {
                    int x = (int) (width * (groundPos.getLongitude().degrees + 180) / 360);
                    int y = (int) (height * (groundPos.getLatitude().degrees + 90) / 180);
                    int w = 10; // cross branch length
                    // Draw
                    gl.glBegin(GL.GL_LINE_STRIP);
                    gl.glVertex3d(x - w, y, 0);
                    gl.glVertex3d(x + w + 1, y, 0);
                    gl.glEnd();
                    gl.glBegin(GL.GL_LINE_STRIP);
                    gl.glVertex3d(x, y - w, 0);
                    gl.glVertex3d(x, y + w + 1, 0);
                    gl.glEnd();
                }
                // Draw 1px border around and inside the map
                gl.glBegin(GL.GL_LINE_STRIP);
                gl.glVertex3d(0, 0, 0);
                gl.glVertex3d(width, 0, 0);
                gl.glVertex3d(width, height - 1, 0);
                gl.glVertex3d(0, height - 1, 0);
                gl.glVertex3d(0, 0, 0);
                gl.glEnd();
            }
            else
            {
                // Picking
                this.pickSupport.clearPickList();
                this.pickSupport.beginPicking(dc);
                // Where in the world are we picking ?
                Position pickPosition =
                    computePickPosition(dc, locationSW, new Dimension((int) (width * scale), (int) (height * scale)));
                // Draw unique color across the map
                Color color = dc.getUniquePickColor();
                int colorCode = color.getRGB();
                // Add our object(s) to the pickable list
                this.pickSupport.addPickableObject(colorCode, this, pickPosition, false);
                gl.glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                gl.glBegin(GL.GL_POLYGON);
                gl.glVertex3d(0, 0, 0);
                gl.glVertex3d(1, 0, 0);
                gl.glVertex3d(1, 1, 0);
                gl.glVertex3d(0, 1, 0);
                gl.glVertex3d(0, 0, 0);
                gl.glEnd();
                // Done picking
                this.pickSupport.endPicking(dc);
                this.pickSupport.resolvePick(dc, dc.getPickPoint(), this);
            }

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
            return Math.min(1d, (this.toViewportScale) * viewport.width / this.getScaledIconWidth());
        }
        else if (this.resizeBehavior.equals(RESIZE_STRETCH))
        {
            return (this.toViewportScale) * viewport.width / this.getScaledIconWidth();
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
        // Enable texture anisotropy, improves "tilted" world map quality.
        int[] maxAnisotropy = new int[1];
        gl.glGetIntegerv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy, 0);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy[0]);
    }

    /**
     * Compute the lat/lon position of the view center
     *
     * @param dc   the current DrawContext
     * @param view the current View
     * @return the ground position of the view center or null
     */
    private Position computeGroundPosition(DrawContext dc, View view)
    {
        if (view == null)
            return null;

        Position groundPos = view.computePositionFromScreenPoint(
            view.getViewport().getWidth() / 2, view.getViewport().getHeight() / 2);
        if (groundPos == null)
            return null;

        double elevation = dc.getGlobe().getElevation(groundPos.getLatitude(), groundPos.getLongitude());
        return new Position(
            groundPos.getLatitude(),
            groundPos.getLongitude(),
            elevation * dc.getVerticalExaggeration());
    }

    /**
     * Computes the lat/lon of the pickPoint over the world map
     *
     * @param dc         the current DrawContext
     * @param locationSW the screen location of the bottom left corner of the map
     * @param mapSize    the world map screen dimension in pixels
     * @return the picked Position
     */
    private Position computePickPosition(DrawContext dc, Vec4 locationSW, Dimension mapSize)
    {
        Position pickPosition = null;
        Point pickPoint = dc.getPickPoint();
        if (pickPoint != null)
        {
            Rectangle viewport = dc.getView().getViewport();
            // Check if pickpoint is inside the map
            if (pickPoint.getX() >= locationSW.getX()
                && pickPoint.getX() < locationSW.getX() + mapSize.width
                && viewport.height - pickPoint.getY() >= locationSW.getY()
                && viewport.height - pickPoint.getY() < locationSW.getY() + mapSize.height)
            {
                double lon = (pickPoint.getX() - locationSW.getX()) / mapSize.width * 360 - 180;
                double lat = (viewport.height - pickPoint.getY() - locationSW.getY()) / mapSize.height * 180 - 90;
                pickPosition = new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), pickAltitude);
            }
        }
        return pickPosition;
    }

    public void dispose()
    {
        // TODO: dispose of the icon texture
	}


	@Override
	public String toString()
	{
		return this.getName();
	}
}
