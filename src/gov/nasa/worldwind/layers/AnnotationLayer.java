/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.layers;

import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Collection;

/**
 * Defines a list of annotations to be displayed over the globe or the screen
 * @author Patrick Murris
 * @version $Id$
 */
public class AnnotationLayer extends AbstractLayer
{
    private final Collection<Annotation> annotations = new ConcurrentLinkedQueue<Annotation>();
    private AnnotationRenderer annotationRenderer = new BasicAnnotationRenderer();

    public AnnotationLayer()
    {
    }

    public void addAnnotation(Annotation annotation)
    {
        if (annotation == null)
        {
            String msg = Logging.getMessage("nullValue.AnnotationIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.annotations.add(annotation);
    }

    public void removeAnnotation(Annotation annotation)
    {
        if (annotation == null)
        {
            String msg = Logging.getMessage("nullValue.IconIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.annotations.remove(annotation);
    }

    public Collection<Annotation> getAnnotations()
    {
        return this.annotations;
    }

    public AnnotationRenderer getAnnotationRenderer()
    {
        return this.annotationRenderer;
    }

    public void setAnnotationRenderer(AnnotationRenderer ar)
    {
        if (ar == null)
        {
            String msg = Logging.getMessage("nullValue.AnnotationRendererIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        this.annotationRenderer = ar;
    }

    @Override
    protected void doPick(DrawContext dc, java.awt.Point pickPoint)
    {
        this.annotationRenderer.pick(dc, this.annotations, pickPoint, this);
    }

    @Override
    protected void doRender(DrawContext dc)
    {
        this.annotationRenderer.render(dc, this.annotations);
    }

    @Override
    public String toString()
    {
        return Logging.getMessage("layers.AnnotationLayer.Name");
    }
}
