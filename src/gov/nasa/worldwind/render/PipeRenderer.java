/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.SectorGeometryList;
import gov.nasa.worldwind.tracks.TrackPoint;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.util.*;

/**
 * @author tag
 * @version $Id: PipeRenderer.java 2984 2007-09-22 02:20:10Z tgaskins $
 */
public class PipeRenderer extends LocationRenderer
{
    private Material junctionMaterial = Material.RED;
    private Material pipeMaterial = Material.WHITE;
    private double pipeRadius = 1000;
    private double junctionShapeRadius = 1.8 * this.pipeRadius;
    private LocationRenderer.Shape junctionShape = SPHERE;
    private PipeRenderer.Pipe pipeShape = new PipeRenderer.Pipe();

    public PipeRenderer()
    {
    }

    public Material getPipeMaterial()
    {
        return pipeMaterial;
    }

    public void setPipeMaterial(Material material)
    {
        if (material == null)
        {
            String msg = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // don't validate material's colors - material does that.

        this.pipeMaterial = material;
    }

    public Material getJunctionMaterial()
    {
        return junctionMaterial;
    }

    public void setJunctionMaterial(Material material)
    {
        if (material == null)
        {
            String msg = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        // don't validate material's colors - material does that.

        this.junctionMaterial = material;
    }

    public void setJunctionShape(String shapeName)
    {
        if (shapeName.equalsIgnoreCase("Cone"))
            this.junctionShape = CONE;
        else if (shapeName.equalsIgnoreCase("Cylinder"))
            this.junctionShape = CYLINDER;
        else
            this.junctionShape = SPHERE;
    }

    public String getJunctionShape()
    {
        return this.junctionShape.name;
    }

    protected Vec4 draw(DrawContext dc, Iterator<TrackPoint> trackPositions)
    {
        if (dc.getVisibleSector() == null)
            return null;

        SectorGeometryList geos = dc.getSurfaceGeometry();
        if (geos == null)
            return null;

        if (!this.pipeShape.isInitialized)
        {
            this.pipeShape.initialize(dc);
        }

        if (!this.junctionShape.isInitialized)
            this.junctionShape.initialize(dc);

        int index = 0;
        List<Vec4> points = new ArrayList<Vec4>();
        while (trackPositions.hasNext())
        {
            TrackPoint tp = trackPositions.next();
            if (index >= this.lowerLimit && index <= this.upperLimit)
            {
                Vec4 point = this.computeSurfacePoint(dc, tp);
                if (point != null)
                {
                    points.add(point);
                }
            }
            if (++index >= this.upperLimit)
                break;
        }

        if (points.size() < 1)
            return null;

        this.begin(dc);
        {
            this.pipeMaterial.apply(dc.getGL(), GL.GL_FRONT);
            Vec4 p1 = points.get(0);
            for (int i = 1; i < points.size(); i++)
            {
                Vec4 p2 = points.get(i);
                this.pipeShape.render(dc, p1, p2, this.pipeRadius);
                p1 = p2;
            }

            this.junctionMaterial.apply(dc.getGL(), GL.GL_FRONT);
            for (Vec4 point : points)
            {
                this.junctionShape.render(dc, point, this.junctionShapeRadius);
            }
        }
        this.end(dc);

        return null; // TODO: return the last-drawn location
    }

    private static class Pipe extends Cylinder
    {
        protected void initialize(DrawContext dc)
        {
            super.initialize(dc);
            this.name = "Pipe";
        }

        private static final double TO_DEGREES = 180d / Math.PI;

        protected void render(DrawContext dc, Vec4 p1, Vec4 p2, double radius)
        {
            // To compute the rotation of the cylinder axis to the orientation of the vector between the two points,
            // this method performs the same operation as Vec4.axisAngle() but with a "v2" of <0, 0, 1>.

            // Compute rotation angle
            double length = p1.distanceTo3(p2);
            Vec4 u1 = new Vec4((p2.x - p1.x) / length, (p2.y - p1.y) / length, (p2.z - p1.z) / length);
            double angle = Math.acos(u1.z);

            // Compute the direction cosine factors that define the rotation axis
            double A = -u1.y;
            double B = u1.x;
            double L = Math.sqrt(A * A + B * B);

            GL gl = dc.getGL();

            // pushReferenceCenter performs the translation of the pipe's origin to point p1 and the necessary
            // push/pop of the modelview stack. Otherwise we'd need to include a glPushMatrix and
            // gl.glTranslated(p1.x, p1.y, p1.z) above the rotation, and a corresponding glPopMatrix after the
            // call to glCallIst.
            dc.getView().pushReferenceCenter(dc, p1);
            gl.glRotated(angle * TO_DEGREES, A / L, B / L, 0);
            gl.glScaled(radius, radius, length / 2); // length / 2 because cylinder is created with length 2
            dc.getGL().glCallList(this.glListId);
            dc.getView().popReferenceCenter(dc);
        }
    }
}
