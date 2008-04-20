/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.pick.Pickable;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.Disposable;

import javax.media.opengl.GL;
import java.awt.*;

/**
 * Represent a text label and its rendering attributes.
 * @author Patrick Murris
 * @version $Id$
 */
public interface Annotation extends Renderable, Pickable, Disposable
{
    public static final String IMAGE_REPEAT_NONE = "render.Annotation.RepeatNone";
    public static final String IMAGE_REPEAT_X = "render.Annotation.RepeatX";
    public static final String IMAGE_REPEAT_Y = "render.Annotation.RepeatY";
    public static final String IMAGE_REPEAT_XY = "render.Annotation.RepeatXY";

    public final static int ANTIALIAS_DONT_CARE = GL.GL_DONT_CARE;
    public final static int ANTIALIAS_FASTEST = GL.GL_FASTEST;
    public final static int ANTIALIAS_NICEST = GL.GL_NICEST;

    public final static String SIZE_FIXED = "render.Annotation.SizeFixed";
    public final static String SIZE_FIT_TEXT = "render.Annotation.SizeFitText";
    
    public String getText();

    public void setText(String text);

    public AnnotationAttributes getAttributes();

    public void setAttributes(AnnotationAttributes attrs);

    public void setPickSupport(PickSupport pickSupport);

    public void draw(DrawContext dc);

}
