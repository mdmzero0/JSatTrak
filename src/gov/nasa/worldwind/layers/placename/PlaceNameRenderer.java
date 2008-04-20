/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers.placename;

import com.sun.opengl.util.j2d.TextRenderer;
import gov.nasa.worldwind.Disposable;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.SectorGeometryList;
import gov.nasa.worldwind.pick.PlaceName;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author dcollins
 * @version $Id: PlaceNameRenderer.java 3319 2007-10-17 18:36:03Z dcollins $
 */
public class PlaceNameRenderer implements Disposable
{
    private static final Font DEFAULT_FONT = Font.decode("Arial-12-PLAIN");
    private static final Color DEFAULT_COLOR = Color.white;
    private final Map<Font, TextRenderer> textRenderers = new HashMap<Font, TextRenderer>();
    private TextRenderer lastTextRenderer = null;
    private final GLU glu = new GLU();

    public PlaceNameRenderer()
    {
    }

    public void dispose()
    {
        for (TextRenderer textRenderer : textRenderers.values())
        {
            if (textRenderer != null)
                textRenderer.dispose();
        }

        this.textRenderers.clear();
    }

    public void render(DrawContext dc, Iterator<PlaceName> placeNames, boolean enableDepthTest)
    {
        this.drawMany(dc, placeNames, enableDepthTest);
    }

    public void render(DrawContext dc, PlaceName placeName, Vec4 placeNamePoint, boolean enableDepthTest)
    {
        if (!isNameValid(placeName, false))
            return;

        this.drawOne(dc, placeName, placeNamePoint, enableDepthTest);
    }

    private void drawMany(DrawContext dc, Iterator<PlaceName> placeNames, boolean enableDepthTest)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        if (geos == null)
            return;

        if (placeNames == null)
        {
            String msg = Logging.getMessage("nullValue.Iterator");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!placeNames.hasNext())
            return;

        Frustum frustumInModelCoords = dc.getView().getFrustumInModelCoordinates();
        double horizon = dc.getView().computeHorizonDistance();

        this.beginDrawNames(dc, enableDepthTest);

        while (placeNames.hasNext())
        {
            PlaceName placeName = placeNames.next();
            if (!isNameValid(placeName, true))
                continue;

            if (!placeName.isVisible())
                continue;

            Angle lat = placeName.getPosition().getLatitude();
            Angle lon = placeName.getPosition().getLongitude();

            if (!dc.getVisibleSector().contains(lat, lon))
                continue;

            Vec4 namePoint = geos.getSurfacePoint(lat, lon, placeName.getPosition().getElevation());
            if (namePoint == null)
                continue;

            double eyeDistance = dc.getView().getEyePoint().distanceTo3(namePoint);
            if (eyeDistance > horizon)
                continue;

            if (!frustumInModelCoords.contains(namePoint))
                continue;

            this.drawName(dc, placeName, namePoint, enableDepthTest);
        }

        this.endDrawNames(dc);
    }

    private void drawOne(DrawContext dc, PlaceName placeName, Vec4 namePoint, boolean enableDepthTest)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }
        if (dc.getView() == null)
        {
            String msg = Logging.getMessage("nullValue.ViewIsNull");
            Logging.logger().fine(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        if (geos == null)
            return;

        if (!placeName.isVisible())
            return;

        if (namePoint == null)
        {
            if (placeName.getPosition() == null)
                return;

            Angle lat = placeName.getPosition().getLatitude();
            Angle lon = placeName.getPosition().getLongitude();

            if (!dc.getVisibleSector().contains(lat, lon))
                return;

            namePoint = geos.getSurfacePoint(lat, lon, placeName.getPosition().getElevation());
            if (namePoint == null)
                return;
        }

        double horizon = dc.getView().computeHorizonDistance();
        double eyeDistance = dc.getView().getEyePoint().distanceTo3(namePoint);
        if (eyeDistance > horizon)
            return;

        if (!dc.getView().getFrustumInModelCoordinates().contains(namePoint))
            return;

        this.beginDrawNames(dc, enableDepthTest);
        this.drawName(dc, placeName, namePoint, enableDepthTest);
        this.endDrawNames(dc);
    }

    private static boolean isNameValid(PlaceName placeName, boolean checkPosition)
    {
        if (placeName == null || placeName.getText() == null)
            return false;

        //noinspection RedundantIfStatement
        if (checkPosition && placeName.getPosition() == null)
            return false;

        return true;
    }

    private final int[] viewportArray = new int[4];

    private void beginDrawNames(DrawContext dc, boolean enableDepthTest)
    {
        GL gl = dc.getGL();
        int attribBits =
            GL.GL_ENABLE_BIT
                | GL.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
                | GL.GL_CURRENT_BIT      // for current color
                | GL.GL_TRANSFORM_BIT    // for modelview and perspective
                | (enableDepthTest ? GL.GL_VIEWPORT_BIT | GL.GL_DEPTH_BUFFER_BIT : 0); // for depth func, depth range
        gl.glPushAttrib(attribBits);

        gl.glGetIntegerv(GL.GL_VIEWPORT, viewportArray, 0);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, viewportArray[2], 0, viewportArray[3]);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        // Enable the depth test but don't write to the depth buffer.
        if (enableDepthTest)
        {
            gl.glEnable(GL.GL_DEPTH_TEST);
            gl.glDepthFunc(GL.GL_LESS);
            gl.glDepthMask(false);
        }
        else
        {
            gl.glDisable(GL.GL_DEPTH_TEST);
        }
        // Suppress polygon culling.
        gl.glDisable(GL.GL_CULL_FACE);
        // Suppress any fully transparent image pixels
        final float ALPHA_EPSILON = 0.001f;
        gl.glEnable(GL.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL.GL_GREATER, ALPHA_EPSILON);
    }

    private void endDrawNames(DrawContext dc)
    {
        if (this.lastTextRenderer != null)
        {
            this.lastTextRenderer.end3DRendering();
            this.lastTextRenderer = null;
        }

        GL gl = dc.getGL();

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPopMatrix();

        gl.glPopAttrib();
    }

    private Vec4 drawName(DrawContext dc, PlaceName name, Vec4 namePoint, boolean enableDepthTest)
    {
        if (namePoint == null)
        {
            String msg = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(msg);
            return null;
        }

        final CharSequence text = name.getText();
        if (text == null)
            return null;

        final Vec4 screenPoint = dc.getView().project(namePoint);
        if (screenPoint == null)
            return null;

        Font font = name.getFont();
        if (font == null)
            font = DEFAULT_FONT;

        TextRenderer textRenderer = this.textRenderers.get(font);
        if (textRenderer == null)
            textRenderer = this.initializeTextRenderer(font);
        if (textRenderer != this.lastTextRenderer)
        {
            if (this.lastTextRenderer != null)
                this.lastTextRenderer.end3DRendering();
            textRenderer.begin3DRendering();
            this.lastTextRenderer = textRenderer;
        }

        if (enableDepthTest)
            this.setDepthFunc(dc, screenPoint);

        Rectangle2D nameBound = textRenderer.getBounds(text);
        float x = (float) (screenPoint.x - nameBound.getWidth() / 2d);
        float y = (float) (screenPoint.y);

        Color color = name.getColor();
        if (color == null)
            color = DEFAULT_COLOR;

        this.setBackgroundColor(textRenderer, color);
        textRenderer.draw3D(text, x + 1, y - 1, 0, 1);
        textRenderer.setColor(color);
        textRenderer.draw3D(text, x, y, 0, 1);
        textRenderer.flush();

        return screenPoint;
    }

    private void setDepthFunc(DrawContext dc, Vec4 screenPoint)
    {
        double depth = screenPoint.z - (8d * 0.00048875809d);
        depth = (depth < 0) ? 0 : ((depth > 1) ? 1 : depth);

        dc.getGL().glDepthRange(depth, depth);
    }

    private final float[] compArray = new float[4];

    private void setBackgroundColor(TextRenderer textRenderer, Color color)
    {
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
        if (compArray[2] > 0.5)
            textRenderer.setColor(0, 0, 0, 0.7f);
        else
            textRenderer.setColor(1, 1, 1, 0.7f);
    }

    private TextRenderer initializeTextRenderer(Font font)
    {
        TextRenderer textRenderer = new TextRenderer(font, true, true);
        TextRenderer oldTextRenderer;
        oldTextRenderer = this.textRenderers.put(font, textRenderer);
        if (oldTextRenderer != null)
            oldTextRenderer.dispose();
        return textRenderer;
    }

    public String toString()
    {
        return Logging.getMessage("layers.PlaceNameLayer.Name");
    }
}
