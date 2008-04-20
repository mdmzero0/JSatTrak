/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.geom.Vec4;

import java.awt.*;

/**
 * @author Patrick Murris
 * @version $Id$
 */
public interface AnnotationRenderer {

    void pick(DrawContext dc, Iterable<Annotation> annotations, Point pickPoint, Layer annotationLayer);

    void pick(DrawContext dc, Annotation annotation, Vec4 annotationPoint, Point pickPoint, Layer annotationLayer);

    void render(DrawContext dc, Iterable<Annotation> annotations);

    void render(DrawContext dc, Annotation annotation, Vec4 annotationPoint);

    Vec4 getAnnotationDrawPoint(DrawContext dc, Annotation annotation);
    
}
