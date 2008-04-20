/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import com.sun.opengl.util.texture.*;
import gov.nasa.worldwind.cache.TextureCache;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.*;
import java.awt.image.*;
import java.util.logging.Level;

/**
 * @author tag
 * @version $Id$
 */
public class SurfaceImage implements SurfaceTile, Renderable
{
    private final Object imageSource;
    private final Sector sector;
    private Extent extent;
    private double extentVerticalExaggertion = Double.MIN_VALUE; // VE used to calculate the extent
    private double opacity = 1.0;

    public SurfaceImage(Object imageSource, Sector sector)
    {
        if (imageSource == null)
        {
            String message = Logging.getMessage("nullValue.ImageSource");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.Sector");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.imageSource = imageSource;
        this.sector = sector;
    }

    public Object getImageSource()
    {
        return imageSource;
    }

    public Sector getSector()
    {
        return this.sector;
    }

    public Extent getExtent(DrawContext dc)
    {
        if (this.extent == null || dc.getVerticalExaggeration() != this.extentVerticalExaggertion)
        {
            this.extent = dc.getGlobe().computeBoundingCylinder(dc.getVerticalExaggeration(), this.sector);
            this.extentVerticalExaggertion = dc.getVerticalExaggeration();
        }

        return this.extent;
    }

    public double getOpacity()
    {
        return opacity;
    }

    public void setOpacity(double opacity)
    {
        this.opacity = opacity;
    }

    private void setTexture(TextureCache tc, Texture texture)
    {
        if (tc == null)
        {
            String message = Logging.getMessage("nullValue.TextureCacheIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        tc.put(this.imageSource, texture);
    }

    private Texture getTexture(TextureCache tc)
    {
        if (tc == null)
        {
            String message = Logging.getMessage("nullValue.TextureCacheIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        return tc.get(this.imageSource);
    }

    private Texture initializeTexture(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        try
        {
            Texture t = null;

            if (this.imageSource instanceof String)
            {
                String path = (String) this.imageSource;
                java.io.InputStream iconStream = this.getClass().getResourceAsStream("/" + path);
                if (iconStream == null)
                {
                    java.io.File iconFile = new java.io.File(path);
                    if (iconFile.exists())
                    {
                        iconStream = new java.io.FileInputStream(iconFile);
                    }
                }
                t = TextureIO.newTexture(iconStream, true, null);
            }
            else if (this.imageSource instanceof BufferedImage)
            {
                try
                {
                    t = TextureIO.newTexture((BufferedImage) this.imageSource, true);
                }
                catch (Exception e)
                {
                    Logging.logger().log(
                        java.util.logging.Level.SEVERE, "layers.TextureLayer.ExceptionAttemptingToReadTextureFile", e);
                    return null;
                }
            }
            else
            {
                // TODO: Log case of unknown image-source type.
            }

            if (t == null)
            {
                // TODO: Log case.
                return null;
            }

            // Textures with the same path are assumed to be identical textures, so key the texture id off the
            // image source.
            this.setTexture(dc.getTextureCache(), t);
            t.bind();

            GL gl = dc.getGL();
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);//_MIPMAP_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);

            return t;
        }
        catch (java.io.IOException e)
        {
            String msg = Logging.getMessage("generic.IOExceptionDuringTextureInitialization");
            Logging.logger().log(Level.SEVERE, msg, e);
            throw new WWRuntimeException(msg, e);
        }
    }

    public boolean bind(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Texture t = this.getTexture(dc.getTextureCache());
        if (t == null)
        {
            t = this.initializeTexture(dc);
            if (t != null)
                return true; // texture was bound during initialization.
        }

        if (t != null)
            t.bind();

        return t != null;
    }

    public void applyInternalTransform(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        // Use the tile's texture if available.
        Texture t = this.getTexture(dc.getTextureCache());
        if (t == null)
            t = this.initializeTexture(dc);

        if (t != null)
        {
            if (t.getMustFlipVertically())
            {
                GL gl = GLContext.getCurrent().getGL();
                gl.glMatrixMode(GL.GL_TEXTURE);
                gl.glLoadIdentity();
                gl.glScaled(1, -1, 1);
                gl.glTranslated(0, -1, 0);
            }
        }
    }

    // Render the surface image tile
    public void render(DrawContext dc)
    {
        if (!this.sector.intersects(dc.getVisibleSector()))
            return;

        GL gl = dc.getGL();
        try
        {
            if (!dc.isPickingMode())
            {
                if (this.getOpacity() < 1)
                {
                    gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT | GL.GL_CURRENT_BIT);
                    gl.glColor4d(1d, 1d, 1d, this.getOpacity());
                }
                else
                {
                    gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT);
                }
                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            }
            else
            {
                gl.glPushAttrib(GL.GL_POLYGON_BIT);
            }

            gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);

            dc.getGeographicSurfaceTileRenderer().renderTile(dc, this);
        }
        finally
        {
            gl.glPopAttrib();
        }
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SurfaceImage that = (SurfaceImage) o;

        return imageSource.equals(that.imageSource) && sector.equals(that.sector);

    }

    public int hashCode()
    {
        int result;
        result = imageSource.hashCode();
        result = 31 * result + sector.hashCode();
        return result;
    }
}
