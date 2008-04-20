/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import javax.media.opengl.GL;
import java.awt.*;

/**
 * Represent a text label attached to a Point on the viewport and its rendering attributes.
 * @author Patrick Murris
 * @version $Id$
 * @see AbstractAnnotation
 * @see AnnotationAttributes
 */
public class ScreenAnnotation extends AbstractAnnotation
{
    private Point screenPoint;

    /**
     * Creates a <code>ScreenAnnotation</code> with the given text, at the given viewport position.
     * @param text the annotation text.
     * @param position the annotation viewport position.
     */
    public ScreenAnnotation(String text, Point position)
    {
        this.init(text, position, null, null);
    }

    /**
     * Creates a <code>ScreenAnnotation</code> with the given text, at the given viewport position.
     * Specifiy the <code>Font</code> to be used.
     * @param text the annotation text.
     * @param position the annotation viewport position.
     * @param font the <code>Font</code> to use.
     */
    public ScreenAnnotation(String text, Point position, Font font)
    {
        this.init(text, position, font, null);
    }

    /**
     * Creates a <code>ScreenAnnotation</code> with the given text, at the given viewport position.
     * Specifiy the <code>Font</code> and text <code>Color</code> to be used.
     * @param text the annotation text.
     * @param position the annotation viewport position.
     * @param font the <code>Font</code> to use.
     * @param textColor the text <code>Color</code>.
     */
    public ScreenAnnotation(String text, Point position, Font font, Color textColor)
    {
        this.init(text, position, font, textColor);
    }

    /**
     * Creates a <code>ScreenAnnotation</code> with the given text, at the given viewport position.
     * Specify the default {@link AnnotationAttributes} set.
     * @param text the annotation text.
     * @param position the annotation viewport position.
     * @param defaults the default {@link AnnotationAttributes} set.
     */
    public ScreenAnnotation(String text, Point position, AnnotationAttributes defaults)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (defaults == null)
        {
            String message = Logging.getMessage("nullValue.AnnotationAttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setText(text);
        this.screenPoint = position;
        this.getAttributes().setDefaults(defaults);
        this.getAttributes().setLeader(FrameFactory.LEADER_NONE);
        this.getAttributes().setDrawOffset(new Point(0, 0));
    }

    private void init(String text, Point position, Font font, Color textColor)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setText(text);
        this.screenPoint = position;
        this.getAttributes().setFont(font);
        this.getAttributes().setTextColor(textColor);
        this.getAttributes().setLeader(FrameFactory.LEADER_NONE);
        this.getAttributes().setDrawOffset(new Point(0, 0));
    }


    //-- Properties ---------------------------------------------------------------

    /**
     * Get the <code>Point</code> where the annotation is drawn in the viewport.
     * @return the <code>Point</code> where the annotation is drawn in the viewport.
     */
    public Point getScreenPoint()
    {
        return this.screenPoint;
    }

    /**
     * Set the <code>Point</code> where the annotation will be drawn in the viewport.
     * @param position the <code>Point</code> where the annotation will be drawn in the viewport.
     */
    public void setScreenPoint(Point position)
    {
        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.screenPoint = position;
    }

    //-- Rendering ----------------------------------------------------------------

    protected void doDraw(DrawContext dc)
    {
        if (dc.isPickingMode() && this.getPickSupport() == null)
            return;

        // Prepare to draw
        GL gl = dc.getGL();
        gl.glDepthFunc(GL.GL_ALWAYS);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Translate to screenpoint
        gl.glTranslated(screenPoint.x, screenPoint.y, 0d);

        // Draw
        drawAnnotation(dc, screenPoint, 1, 1, null);
    }
}
