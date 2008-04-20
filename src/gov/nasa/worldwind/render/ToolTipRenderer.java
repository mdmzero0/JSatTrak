/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import com.sun.opengl.util.j2d.TextRenderer;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * @author dcollins
 * @version $Id: ToolTipRenderer.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class ToolTipRenderer
{
    private static final Font toolTipFont = UIManager.getFont("ToolTip.font");
    private static final Color toolTipFg = UIManager.getColor("ToolTip.foreground");
    private static final Color toolTipBg = UIManager.getColor("ToolTip.background");
    private static final Border toolTipBorder = UIManager.getBorder("ToolTip.border");

    private Color foreground;
    private Color background;
    private float[] compArray;
    private Insets insets;
    private float borderWidth;
    private boolean useSystemLookAndFeel = false;

    private TextRenderer textRenderer;
    private int orthoWidth;
    private int orthoHeight;
    private GLU glu = new GLU();

    public ToolTipRenderer()
    {
        this(new TextRenderer(toolTipFont, true, true), false);
    }

    public ToolTipRenderer(TextRenderer textRenderer)
    {
        this(textRenderer, false);
    }

    public ToolTipRenderer(TextRenderer textRenderer, boolean useSystemLookAndFeel)
    {
        if (textRenderer == null)
        {
            String message = Logging.getMessage(""); // TODO
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        this.textRenderer = textRenderer;
        this.useSystemLookAndFeel = useSystemLookAndFeel;
        this.borderWidth = 1;
    }

    public void beginRendering(int width, int height, boolean disableDepthTest)
    {
        GL gl = GLU.getCurrentGL();
        int attribBits =
            GL.GL_ENABLE_BIT
                | GL.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
                | GL.GL_CURRENT_BIT      // for current color
                | GL.GL_TRANSFORM_BIT    // for modelview and perspective
                | GL.GL_POLYGON_BIT;     // for polygon mode
        gl.glPushAttrib(attribBits);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        glu.gluOrtho2D(0, width, 0, height);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_TEXTURE_2D);
        if (disableDepthTest)
            gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
        // Suppress any fully transparent image pixels
        final float ALPHA_EPSILON = 0.001f;
        gl.glEnable(GL.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL.GL_GREATER, ALPHA_EPSILON);

        this.orthoWidth = width;
        this.orthoHeight = height;
    }

    public void endRendering()
    {
        GL gl = GLU.getCurrentGL();

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPopMatrix();

        gl.glPopAttrib();
    }

    private static Rectangle2D ensureVisibleBounds(Rectangle2D bounds, int orthoWidth, int orthoHeight)
    {
        double newX;
        if (bounds.getMinX() < 0)
            newX = 0;
        else if (bounds.getMaxX() > orthoWidth)
            newX = orthoWidth - bounds.getWidth() - 1;
        else
            newX = bounds.getX();

        double newY;
        if (bounds.getMinY() < 0)
            newY = 0;
        else if (bounds.getMaxY() > orthoHeight)
            newY = orthoHeight - bounds.getHeight() - 1;
        else
            newY = bounds.getY();

        return new Rectangle2D.Double(newX, newY, bounds.getWidth(), bounds.getHeight());
    }

    public Color getBackground()
    {
        return this.background;
    }

    public float getBorderWidth()
    {
        return this.borderWidth;
    }

    public Color getForeground()
    {
        return this.foreground;
    }

    public Insets getInsets()
    {
        return this.insets;
    }

    public boolean getUseSystemLookAndFeel()
    {
        return this.useSystemLookAndFeel;
    }

    public void draw(String str, int x, int y)
    {
        if (str == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        Color fg;
        Color bg;
        Insets insets;

        GL gl = GLU.getCurrentGL();
        if (this.useSystemLookAndFeel)
        {
            insets = toolTipBorder.getBorderInsets(null);
            fg = toolTipFg;
            bg = toolTipBg;
        }
        else
        {
            if (this.foreground != null)
            {
                fg = this.foreground;
            }
            else
            {
                gl.glGetFloatv(GL.GL_CURRENT_COLOR, compArray, 0);
                fg = new Color(compArray[0], compArray[1], compArray[2], compArray[3]);
            }

            if (this.background != null)
            {
                bg = this.background;
            }
            else
            {
                if (compArray == null)
                    compArray = new float[4];
                Color.RGBtoHSB(fg.getRed(), fg.getGreen(), fg.getBlue(), compArray);
                bg = Color.getHSBColor(0, 0, (compArray[2] + 0.5f) % 1f);
            }

            if (this.insets != null)
                insets = this.insets;
            else
                insets = new Insets(1, 1, 1, 1);
        }

        Rectangle2D strBounds = this.textRenderer.getBounds(str);
        Rectangle2D ttBounds = new Rectangle2D.Double(
            x, y,
            strBounds.getWidth() + insets.left + insets.right,
            strBounds.getHeight() + insets.bottom + insets.top);
        ttBounds = ensureVisibleBounds(ttBounds, this.orthoWidth, this.orthoHeight);
        double strX = ttBounds.getX() + insets.left - strBounds.getX();
        double strY = ttBounds.getY() + insets.bottom + strBounds.getY() + strBounds.getHeight();

        this.setDrawColor(bg);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
        gl.glRectd(ttBounds.getMinX(), ttBounds.getMinY(), ttBounds.getMaxX(), ttBounds.getMaxY());

        this.setDrawColor(fg);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
        gl.glLineWidth(this.borderWidth);
        gl.glRectd(ttBounds.getMinX(), ttBounds.getMinY(), ttBounds.getMaxX(), ttBounds.getMaxY());

        this.textRenderer.setColor(fg);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
        this.textRenderer.begin3DRendering();
        this.textRenderer.draw(str, (int) strX, (int) strY);
        this.textRenderer.end3DRendering();
    }

    public void setBackground(Color color)
    {
        this.background = color;
    }

    public void setBorderWidth(float borderWidth)
    {
        this.borderWidth = borderWidth;
    }

    private void setDrawColor(float r, float g, float b, float a)
    {
        GL gl = GLU.getCurrentGL();
        gl.glColor4f(r * a, g * a, b * a, a);
    }

    private void setDrawColor(Color color)
    {
        if (this.compArray == null)
            this.compArray = new float[4];
        color.getRGBComponents(this.compArray);
        this.setDrawColor(this.compArray[0], this.compArray[1], this.compArray[2], this.compArray[3]);
    }

    public void setForeground(Color color)
    {
        this.foreground = color;
    }

    public void setInsets(Insets insets)
    {
        this.insets = insets;
    }

    public void setUseSystemLookAndFeel(boolean useSystemLookAndFeel)
    {
        this.useSystemLookAndFeel = useSystemLookAndFeel;
    }
}
