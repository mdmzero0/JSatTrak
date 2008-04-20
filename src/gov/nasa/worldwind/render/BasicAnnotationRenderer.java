/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Locatable;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.SectorGeometryList;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * Basic implementation of AnnotationRenderer. Process Annotation rendering as OrderedRenderable objects batch.
 *
 * @author Patrick Murris
 * @version $Id$
 * @see AbstractAnnotation
 * @see AnnotationAttributes
 * @see AnnotationLayer
 */
public class BasicAnnotationRenderer implements AnnotationRenderer
{
    private PickSupport pickSupport = new PickSupport();


    private static boolean isAnnotationValid(Annotation annotation, boolean checkPosition)
    {
        if (annotation == null || annotation.getText() == null)
            return false;

        //noinspection RedundantIfStatement
        if (checkPosition && annotation instanceof Locatable)
            return ((Locatable)annotation).getPosition() != null;

        return true;
    }

    public void pick(DrawContext dc, Iterable<Annotation> annotations, Point pickPoint, Layer layer)
    {
        this.drawMany(dc, annotations);
    }

    public void pick(DrawContext dc, Annotation annotation, Vec4 annotationPoint, java.awt.Point pickPoint, Layer layer)
    {
        if (!isAnnotationValid(annotation, false))
            return;

        this.drawOne(dc, annotation, annotationPoint);
    }

    public void render(DrawContext dc, Iterable<Annotation> annotations)
    {
        this.drawMany(dc, annotations);
    }

    public void render(DrawContext dc, Annotation annotation, Vec4 annotationPoint)
    {
        if (!isAnnotationValid(annotation, false))
            return;

        this.drawOne(dc, annotation, annotationPoint);
    }

    private void drawMany(DrawContext dc, Iterable<Annotation> annotations)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        //noinspection RedundantIfStatement
        if (geos == null)
            return;

        if (annotations == null)
        {
            String msg = Logging.getMessage("nullValue.AnnotationIterator");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Iterator<Annotation> iterator = annotations.iterator();

        if (!iterator.hasNext())
            return;

        while (iterator.hasNext())
        {
            Annotation annotation = iterator.next();
            if (!isAnnotationValid(annotation, true))
                continue;

            if (!annotation.getAttributes().isVisible())
                continue;

            // TODO: cull annotations that are beyound the horizon or outside the view frustrum
            double eyeDistance = 1;
            if (annotation instanceof Locatable)
            {
                // Determine Cartesian position from the surface geometry if the annotation is near the surface,
                // otherwise draw it from the globe.
                Vec4 annotationPoint = getAnnotationDrawPoint(dc, annotation);
                if (annotationPoint == null)
                    continue;
                eyeDistance = dc.getView().getEyePoint().distanceTo3(annotationPoint);
            }
            // The annotations aren't drawn here, but added to the ordered queue to be drawn back-to-front.
            dc.addOrderedRenderable(new OrderedAnnotation(annotation, eyeDistance));
        }
    }

    private void drawOne(DrawContext dc, Annotation annotation, Vec4 annotationPoint)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (dc.getVisibleSector() == null)
            return;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        //noinspection RedundantIfStatement
        if (geos == null)
            return;

        if (!annotation.getAttributes().isVisible())
            return;

        double eyeDistance = 1;
        if (annotation instanceof Locatable)
        {
            if (annotationPoint == null)
            {
                Position pos = ((Locatable)annotation).getPosition();

                if (!dc.getVisibleSector().contains(pos.getLatitude(), pos.getLongitude()))
                    return;

                // Determine Cartesian position from the surface geometry if the annotation is near the surface,
                // otherwise draw it from the globe.
                annotationPoint = getAnnotationDrawPoint(dc, annotation);
                if (annotationPoint == null)
                    return;
            }

            if (!dc.getView().getFrustumInModelCoordinates().contains(annotationPoint))
                return;

            double horizon = dc.getView().computeHorizonDistance();
            eyeDistance = dc.getView().getEyePoint().distanceTo3(annotationPoint);
            if (eyeDistance > horizon)
                return;
        }
        // The annotation isn't drawn here, but added to the ordered queue to be drawn back-to-front.
        dc.addOrderedRenderable(new OrderedAnnotation(annotation, eyeDistance));
    }

    /**
     * Get the final Vec4 point at which an annotation will be drawn. If the annotation Position elevation
     * is lower then the highest elevation on the globe, it will be drawn above the ground using its elevation as an
     * offset. Otherwise, the original elevation will be used.
     * @param dc the current DrawContext.
     * @param annotation the annotation
     * @return the annotation draw cartesian point
     */
    public Vec4 getAnnotationDrawPoint(DrawContext dc, Annotation annotation)
    {
        Vec4 drawPoint = null;
        if(annotation instanceof Locatable)
        {
            Position pos = ((Locatable)annotation).getPosition();
            if (pos.getElevation() < dc.getGlobe().getMaxElevation())
                drawPoint = dc.getSurfaceGeometry().getSurfacePoint(pos.getLatitude(), pos.getLongitude(), pos.getElevation());
            if (drawPoint == null)
                drawPoint = dc.getGlobe().computePointFromPosition(pos);
        }
        return drawPoint;
    }

    private class OrderedAnnotation implements OrderedRenderable
    {
        Annotation annotation;
        double eyeDistance;
        Layer layer;

        OrderedAnnotation(Annotation annotation, double eyeDistance)
        {
            this.annotation = annotation;
            this.eyeDistance = eyeDistance;
        }

        OrderedAnnotation(Annotation annotation, Layer layer, double eyeDistance)
        {
            this.annotation = annotation;
            this.eyeDistance = eyeDistance;
            this.layer = layer;
        }

        public double getDistanceFromEye()
        {
            return this.eyeDistance;
        }

        public void render(DrawContext dc)
        {
            BasicAnnotationRenderer.this.beginDrawAnnotations(dc);
            try
            {
                this.annotation.draw(dc);
                // Draw as many as we can in a batch to save ogl state switching.
                while (dc.getOrderedRenderables().peek() instanceof OrderedAnnotation)
                {
                    OrderedAnnotation oa = (OrderedAnnotation) dc.getOrderedRenderables().poll();
                    oa.annotation.draw(dc);
                }
            }
            catch (WWRuntimeException e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhileRenderingAnnotation", e);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhileRenderingAnnotation", e);
            }
            finally
            {
                BasicAnnotationRenderer.this.endDrawAnnotations(dc);
            }
        }

        public void pick(DrawContext dc, java.awt.Point pickPoint)
        {
            BasicAnnotationRenderer.this.pickSupport.clearPickList();
            BasicAnnotationRenderer.this.beginDrawAnnotations(dc);
            try
            {
                this.annotation.setPickSupport(BasicAnnotationRenderer.this.pickSupport);
                this.annotation.draw(dc);
                // Draw as many as we can in a batch to save ogl state switching.
                while (dc.getOrderedRenderables().peek() instanceof OrderedAnnotation)
                {
                    OrderedAnnotation oa = (OrderedAnnotation) dc.getOrderedRenderables().poll();
                    oa.annotation.setPickSupport(BasicAnnotationRenderer.this.pickSupport);
                    oa.annotation.draw(dc);
                }
            }
            catch (WWRuntimeException e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhileRenderingAnnotation", e);
            }
            catch (Exception e)
            {
                Logging.logger().log(Level.SEVERE, "generic.ExceptionWhilePickingAnnotation", e);
            }
            finally
            {
                BasicAnnotationRenderer.this.endDrawAnnotations(dc);
                BasicAnnotationRenderer.this.pickSupport.resolvePick(dc, pickPoint, layer);
                BasicAnnotationRenderer.this.pickSupport.clearPickList(); // to ensure entries can be garbage collected
            }
        }
    }

    private void beginDrawAnnotations(DrawContext dc)
    {
        GL gl = dc.getGL();

        int attributeMask =
            GL.GL_DEPTH_BUFFER_BIT // for depth test, depth mask and depth func
                | GL.GL_TRANSFORM_BIT // for modelview and perspective
                | GL.GL_VIEWPORT_BIT // for depth range
                | GL.GL_CURRENT_BIT // for current color
                | GL.GL_COLOR_BUFFER_BIT // for alpha test func and ref, and blend
                | GL.GL_TEXTURE_BIT // for texture env
                | GL.GL_DEPTH_BUFFER_BIT // for depth func
                | GL.GL_ENABLE_BIT // for enable/disable changes
                | GL.GL_LINE_BIT
                | GL.GL_HINT_BIT;
        gl.glPushAttrib(attributeMask);

        // Apply the depth buffer but don't change it.
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthMask(false);

        // Suppress any fully transparent image pixels
        gl.glEnable(GL.GL_ALPHA_TEST);
        gl.glAlphaFunc(GL.GL_GREATER, 0.001f);

        gl.glDisable(GL.GL_LIGHTING);
        gl.glDisable(GL.GL_CULL_FACE);

        // Load a parallel projection with dimensions (viewportWidth, viewportHeight)
        int[] viewport = new int[4];
        gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glOrtho(0d, viewport[2], 0d, viewport[3], -1d, 1d);

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPushMatrix();

        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPushMatrix();

        if (dc.isPickingMode())
        {
            this.pickSupport.beginPicking(dc);

/*            // Set up to replace the non-transparent texture colors with the single pick color.
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_COMBINE);
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_SRC0_RGB, GL.GL_PREVIOUS);
            gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_COMBINE_RGB, GL.GL_REPLACE);  */

            // No textures, no blend.
            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glDisable(GL.GL_BLEND);
        }
        else
        {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    private void endDrawAnnotations(DrawContext dc)
    {
        if (dc.isPickingMode())
            this.pickSupport.endPicking(dc);

        GL gl = dc.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();

        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();

        gl.glMatrixMode(GL.GL_TEXTURE);
        gl.glPopMatrix();

        gl.glPopAttrib();
    }


    //-- Collision avoidance ---------------------------------------------------
    ArrayList<Rectangle> usedRectangles = new ArrayList<Rectangle>();
    Point defaultDrawOffset = new Point(-10, 20);

    // Try to find a free rectangular space around a point
    // TODO: Fix me
    private Point computeOffset(Point point, Dimension dimension)
    {
        Point offset = this.defaultDrawOffset;
        Rectangle r = new Rectangle(point.x + offset.x - dimension.width / 2,
                point.y + offset.y + dimension.height,
                dimension.width, dimension.height);
        double radius = 20;
        Angle angle = Angle.ZERO;
        int step = 0;
        int angleStep = 1;
        while(rectangleIntersectsUsed(r))
        {
            // Give up after some number of tries
            if(step++ > 100)
            {
                usedRectangles.clear();
                return this.defaultDrawOffset;
            }

            // Increment angle and radius
            int a = 90 + (10 * (angleStep / 2) * (angleStep % 2 == 0 ? 1 : -1));
            if(Math.abs(a) <= 10)
            {
                angleStep = 1;
                radius += 50;
            }
            else
                angleStep++;

            // Compute new rectangle
            angle = Angle.fromDegrees(a);
            offset.x = (int)(radius * angle.cos());
            offset.y = (int)(radius * angle.sin());
            r.setBounds(point.x + offset.x - dimension.width / 2,
                    point.y + offset.y + dimension.height,
                    dimension.width, dimension.height);
        }

        // Keep track of used rectangle
        this.usedRectangles.add(r);

        return offset;
    }

    // Test if a rectangle intersects one of the previously used rectangles
    private boolean rectangleIntersectsUsed(Rectangle r)
    {
        for(Rectangle ur : this.usedRectangles)
            if(r.intersects(ur))
                return true;
        return false;
    }

    
}
