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
 * Represent a text label attached to a Position on the globe and its rendering attributes.
 * @author Patrick Murris
 * @version $Id$
 * @see AbstractAnnotation
 * @see AnnotationAttributes
 */
public class GlobeAnnotation extends AbstractAnnotation implements Locatable, Movable
{
    private Position position;

    /**
     * Creates a <code>GlobeAnnotation</code> with the given text, at the given globe <code>Position</code>.
     * @param text the annotation text.
     * @param position the annotation <code>Position</code>.
     */
    public GlobeAnnotation(String text, Position position)
    {
        this.init(text, position, null, null);
    }

    /**
     * Creates a <code>GlobeAnnotation</code> with the given text, at the given globe <code>Position</code>.
     * Specifiy the <code>Font</code> to be used.
     * @param text the annotation text.
     * @param position the annotation <code>Position</code>.
     * @param font the <code>Font</code> to use.
     */
    public GlobeAnnotation(String text, Position position, Font font)
    {
        this.init(text, position, font, null);
    }

    /**
     * Creates a <code>GlobeAnnotation</code> with the given text, at the given globe <code>Position</code>.
     * Specifiy the <code>Font</code> and text <code>Color</code> to be used.
     * @param text the annotation text.
     * @param position the annotation <code>Position</code>.
     * @param font the <code>Font</code> to use.
     * @param textColor the text <code>Color</code>.
     */
    public GlobeAnnotation(String text, Position position, Font font, Color textColor)
    {
        this.init(text, position, font, textColor);
    }

    /**
     * Creates a <code>GlobeAnnotation</code> with the given text, at the given globe <code>Position</code>.
     * Specify the default {@link AnnotationAttributes} set.
     * @param text the annotation text.
     * @param position the annotation <code>Position</code>.
     * @param defaults the default {@link AnnotationAttributes} set.
     */
    public GlobeAnnotation(String text, Position position, AnnotationAttributes defaults)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
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
        this.position = position;
        this.getAttributes().setDefaults(defaults);
    }

    private void init(String text, Position position, Font font, Color textColor)
    {
        if (text == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (position == null)
        {
            String message = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.setText(text);
        this.position = position;
        this.getAttributes().setFont(font);
        this.getAttributes().setTextColor(textColor);
    }


    //-- Rendering ----------------------------------------------------------------

    protected void doDraw(DrawContext dc)
    {
        if (dc.isPickingMode() && this.getPickSupport() == null)
            return;

        Vec4 point = dc.getAnnotationRenderer().getAnnotationDrawPoint(dc, this);
        if (point == null)
            return;

        double eyeDistance = dc.getView().getEyePoint().distanceTo3(point);
        final Vec4 screenPoint = dc.getView().project(point);
        if (screenPoint == null)
            return;

        Position pos = dc.getGlobe().computePositionFromPoint(point);

        // Determine scaling and transparency factors based on distance from eye vs the distance to the look at point
        double drawScale = computeLookAtDistance(dc) / eyeDistance;  // TODO: cache lookAtDistance for one frame cycle
        double drawAlpha = Math.min(1d, Math.max(attributes.getDistanceMinOpacity(), Math.sqrt(drawScale)));
        drawScale = Math.min(attributes.getDistanceMaxScale(),
                Math.max(attributes.getDistanceMinScale(), drawScale));  // Clamp to factor range

        // Prepare to draw
        this.setDepthFunc(dc, screenPoint);
        GL gl = dc.getGL();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        // Translate to screenpoint
        gl.glTranslated(screenPoint.x, screenPoint.y, 0d);

        // Draw
        drawAnnotation(dc, new Point((int)screenPoint.x, (int)screenPoint.y), drawScale, drawAlpha, pos);
    }

    //-- Locatable ----------------------------------------------------------------
    public Position getPosition()   // Locatable
    {
        return this.position;
    }

    public void setPosition(Position position)
    {
        this.position = position;
    }

    //-- Movable ------------------------------------------------------------------
    public void move(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.position = this.position.add(position);
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.position = position;
    }

    public Position getReferencePosition()
    {
        return this.position;
    }

}
