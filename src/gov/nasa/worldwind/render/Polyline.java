/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.Movable;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: Polyline.java 3608 2007-11-22 16:44:28Z tgaskins $
 */
public class Polyline implements Renderable, Movable
{
    public final static int GREAT_CIRCLE = 0;
    public final static int LINEAR = 1;
//    public final static int RHUMB_LINE = 1;
//    public final static int LOXODROME = RHUMB_LINE;

    public final static int ANTIALIAS_DONT_CARE = GL.GL_DONT_CARE;
    public final static int ANTIALIAS_FASTEST = GL.GL_FASTEST;
    public final static int ANTIALIAS_NICEST = GL.GL_NICEST;

    private ArrayList<Position> positions;
    private Vec4 referenceCenterPoint;
    private Position referenceCenterPosition = Position.ZERO;
    private int antiAliasHint = GL.GL_FASTEST;
    private Color color = Color.WHITE;
    private double lineWidth = 1;
    private boolean filled = false; // makes it a polygon
    private boolean followTerrain = false;
    private double offset = 0;
    private double terrainConformance = 10;
    private int pathType = GREAT_CIRCLE;
    private ArrayList<ArrayList<Vec4>> currentSpans;
    private double length;
    private short stipplePattern = (short) 0xAAAA;
    private int stippleFactor = 0;
    private Globe globe;
    private int numSubsegments = 10;

    public Polyline()
    {
        this.setPositions(null);
    }

    public Polyline(Iterable<Position> positions)
    {
        this.setPositions(positions);
    }

    public Polyline(Iterable<LatLon> positions, double elevation)
    {
        this.setPositions(positions, elevation);
    }

    private void reset()
    {
        if (this.currentSpans != null)
            this.currentSpans.clear();
        this.currentSpans = null;
    }

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        if (color == null)
        {
            String msg = Logging.getMessage("nullValue.ColorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.color = color;
    }

    public int getAntiAliasHint()
    {
        return antiAliasHint;
    }

    public void setAntiAliasHint(int hint)
    {
        if (!(hint == ANTIALIAS_DONT_CARE || hint == ANTIALIAS_FASTEST || hint == ANTIALIAS_NICEST))
        {
            String msg = Logging.getMessage("generic.InvalidHint");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.antiAliasHint = hint;
    }

    public boolean isFilled()
    {
        return filled;
    }

    public void setFilled(boolean filled)
    {
        this.filled = filled;
    }

    public int getPathType()
    {
        return pathType;
    }

    /**
     * Sets the type of path to draw, one of {@link #GREAT_CIRCLE}, which draws each segment of the path as a great
     * circle, or {@link #LINEAR}, which determines the intermediate positions between segments by interpolating the
     * segment endpoints.
     *
     * @param pathType the type of path to draw.
     */
    public void setPathType(int pathType)
    {
        this.reset();
        this.pathType = pathType;
    }

    public boolean isFollowTerrain()
    {
        return followTerrain;
    }

    /**
     * Indicates whether the path should follow the terrain's surface. If the value is <code>true</code>, the elevation
     * values in this path's positions are ignored and the path is drawn on the terrain surface. Otherwise the path is
     * drawn according to the elevations given in the path's positions. If following the terrain, the path may also have
     * an offset. See {@link #setOffset(double)};
     *
     * @param followTerrain <code>true</code> to follow the terrain, otherwise <code>false</code>.
     */
    public void setFollowTerrain(boolean followTerrain)
    {
        this.reset();
        this.followTerrain = followTerrain;
    }

    public double getOffset()
    {
        return offset;
    }

    /**
     * Specifies an offset, in meters, to add to the path points when the path's follow-terrain attribute is true. See
     * {@link #setFollowTerrain(boolean)}.
     *
     * @param offset the path pffset in meters.
     */
    public void setOffset(double offset)
    {
        this.reset();
        this.offset = offset;
    }

    public double getTerrainConformance()
    {
        return terrainConformance;
    }

    /**
     * Specifies the precision to which the path follows the terrain when the follow-terrain attribute it true. The
     * conformance value indicates the approximate length of each sub-segment of the path as it's drawn, in pixels.
     * Lower values specify higher precision, but at the cost of performance.
     *
     * @param terrainConformance the path conformance in pixels.
     */
    public void setTerrainConformance(double terrainConformance)
    {
        this.terrainConformance = terrainConformance;
    }

    public double getLineWidth()
    {
        return lineWidth;
    }

    public void setLineWidth(double lineWidth)
    {
        this.lineWidth = lineWidth;
    }

    /**
     * Returns the length of the line as drawn. If the path follows the terrain, the length returned is the distance one
     * would travel if on the surface. If the path does not follow the terrain, the length returned is the distance
     * along the full length of the path at the path's elevations and current path type.
     *
     * @return the path's length in meters.
     */
    public double getLength()
    {
        return length;
    }

    public short getStipplePattern()
    {
        return stipplePattern;
    }

    /**
     * Sets the stipple pattern for specifying line types other than solid. See the OpenGL specification or programming
     * guides for a description of this parameter. Stipple is also affected by the path's stipple factor, {@link
     * #setStippleFactor(int)}.
     *
     * @param stipplePattern the stipple pattern.
     */
    public void setStipplePattern(short stipplePattern)
    {
        this.stipplePattern = stipplePattern;
    }

    public int getStippleFactor()
    {
        return stippleFactor;
    }

    /**
     * Sets the stipple factor for specifying line types other than solid. See the OpenGL specification or programming
     * guides for a description of this parameter. Stipple is also affected by the path's stipple pattern, {@link
     * #setStipplePattern(short)}.
     *
     * @param stippleFactor the stipple factor.
     */
    public void setStippleFactor(int stippleFactor)
    {
        this.stippleFactor = stippleFactor;
    }

    public int getNumSubsegments()
    {
        return numSubsegments;
    }

    /**
     * Specifies the number of intermediate segments to draw for each segment between positions. The end points of the
     * intermediate segments are calculated according to the current path type and follow-terrain setting.
     *
     * @param numSubsegments
     */
    public void setNumSubsegments(int numSubsegments)
    {
        this.reset();
        this.numSubsegments = numSubsegments;
    }

    /**
     * Specifies the path's positions.
     *
     * @param inPositions the path positions.
     */
    public void setPositions(Iterable<Position> inPositions)
    {
        this.reset();
        this.positions = new ArrayList<Position>();
        if (inPositions != null)
        {
            for (Position position : inPositions)
            {
                this.positions.add(position);
            }
        }

        if ((this.filled && this.positions.size() < 3))
        {
            String msg = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Sets the paths positions as latitude and longitude values at a constant altitude.
     *
     * @param inPositions the latitudes and longitudes of the positions.
     * @param elevation   the elevation to assign each position.
     */
    public void setPositions(Iterable<LatLon> inPositions, double elevation)
    {
        this.reset();
        this.positions = new ArrayList<Position>();
        if (inPositions != null)
        {
            for (LatLon position : inPositions)
            {
                this.positions.add(new Position(position, elevation));
            }
        }

        if (this.filled && this.positions.size() < 3)
        {
            String msg = Logging.getMessage("generic.InsufficientPositions");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
    }

    public Iterable<Position> getPositions()
    {
        return this.positions;
    }

    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.globe = dc.getGlobe();

        if (this.positions.size() < 2)
            return;

        if (this.currentSpans == null || this.followTerrain) // vertices computed every frame to follow terrain changes
        {
            // Reference center must be computed prior to computing vertices.
            this.computeReferenceCenter(dc);
            this.makeVertices(dc);
        }

        if (this.currentSpans == null || this.currentSpans.size() < 1)
            return;

        GL gl = dc.getGL();

        int attrBits = GL.GL_HINT_BIT | GL.GL_CURRENT_BIT | GL.GL_LINE_BIT;
        if (!dc.isPickingMode())
        {
            if (this.color.getAlpha() != 255)
                attrBits |= GL.GL_COLOR_BUFFER_BIT;
        }

        gl.glPushAttrib(attrBits);
        gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
        dc.getView().pushReferenceCenter(dc, this.referenceCenterPoint);

        try
        {
            if (!dc.isPickingMode())
            {
                if (this.color.getAlpha() != 255)
                {
                    gl.glEnable(GL.GL_BLEND);
                    gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
                }
                dc.getGL().glColor4ub((byte) this.color.getRed(), (byte) this.color.getGreen(),
                    (byte) this.color.getBlue(), (byte) this.color.getAlpha());
            }

            if (this.stippleFactor > 0)
            {
                gl.glEnable(GL.GL_LINE_STIPPLE);
                gl.glLineStipple(this.stippleFactor, this.stipplePattern);
            }
            else
            {
                gl.glDisable(GL.GL_LINE_STIPPLE);
            }

            int hintAttr = GL.GL_LINE_SMOOTH_HINT;
            if (this.filled)
                hintAttr = GL.GL_POLYGON_SMOOTH_HINT;
            gl.glHint(hintAttr, this.antiAliasHint);

            int primType = GL.GL_LINE_STRIP;
            if (this.filled)
                primType = GL.GL_POLYGON;

            gl.glLineWidth((float) this.lineWidth);

            gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
            if (this.followTerrain)
                this.pushOffest(dc);
            for (ArrayList<Vec4> span : this.currentSpans)
            {
                if (span == null)
                    continue;

                DoubleBuffer vertBuffer = this.bufferVertices(span);

                gl.glVertexPointer(3, GL.GL_DOUBLE, 0, vertBuffer.rewind());
                gl.glDrawArrays(primType, 0, vertBuffer.capacity() / 3);
            }
            if (this.followTerrain)
                this.popOffest(dc);
        }
        finally
        {
            gl.glPopClientAttrib();
            gl.glPopAttrib();
            dc.getView().popReferenceCenter(dc);
        }
    }

    private void pushOffest(DrawContext dc)
    {
        // Modify the projection transform to shift the depth values slightly toward the camera in order to
        // ensure the lines are selected during depth buffering.
        GL gl = dc.getGL();

        float[] pm = new float[16];
        gl.glGetFloatv(GL.GL_PROJECTION_MATRIX, pm, 0);
        pm[10] *= 0.99; // TODO: See Lengyel 2 ed. Section 9.1.2 to compute optimal/minimal offset

        gl.glPushAttrib(GL.GL_TRANSFORM_BIT);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadMatrixf(pm, 0);
    }

    private void popOffest(DrawContext dc)
    {
        GL gl = dc.getGL();
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glPopAttrib();
    }

    private DoubleBuffer bufferVertices(ArrayList<Vec4> vertArray)
    {
        if (vertArray == null)
            return null;

        DoubleBuffer db = BufferUtil.newDoubleBuffer(3 * vertArray.size());
        for (Vec4 v : vertArray)
            db.put(v.x).put(v.y).put(v.z);

        return db;
    }

    protected void makeVertices(DrawContext dc)
    {
        if (this.currentSpans == null)
            this.currentSpans = new ArrayList<ArrayList<Vec4>>();
        else
            this.currentSpans.clear();

        this.length = 0;

        if (this.positions.size() < 1)
            return;

        Position posA = this.positions.get(0);
        for (int i = 1; i < this.positions.size(); i++)
        {
            Position posB = this.positions.get(i);

            if (this.followTerrain && !this.isSegmentVisible(dc, posA, posB))
            {
                posA = posB;
                continue;
            }

            ArrayList<Vec4> span;
            span = this.makeSegment(dc, posA, posB);

            if (span != null)
                this.addSpan(span);

            posA = posB;
        }
    }

    private void addSpan(ArrayList<Vec4> span)
    {
        if (span == null || span.size() < 1)
            return;

        if (this.currentSpans.size() < 1)
        {
            this.currentSpans.add(span);
            return;
        }

        this.currentSpans.add(span);
    }

    private boolean isSegmentVisible(DrawContext dc, Position posA, Position posB)
    {
        Frustum f = dc.getView().getFrustumInModelCoordinates();

        Vec4 ptA = this.computePoint(dc, posA, true);
        if (f.contains(ptA))
            return true;

        Vec4 ptB = this.computePoint(dc, posB, true);
        if (f.contains(ptB))
            return true;

        if (ptA.equals(ptB))
            return false;

        Position posC = Position.interpolate(0.5, posA, posB);
        Vec4 ptC = this.computePoint(dc, posC, true);
        if (f.contains(ptC))
            return true;

        double r = Line.distanceToSegment(ptA, ptB, ptC);
        Cylinder cyl = new Cylinder(ptA, ptB, r == 0 ? 1 : r);

        return cyl.intersects(dc.getView().getFrustumInModelCoordinates());
    }

    private Vec4 computePoint(DrawContext dc, Position pos, boolean applyOffset)
    {
        if (this.followTerrain)
        {
            double height = !applyOffset ? 0 : this.offset;
            return this.computeTerrainPoint(dc, pos.getLatitude(), pos.getLongitude(), height);
        }
        else
        {
            double height = pos.getElevation() + (applyOffset ? this.offset : 0);
            return dc.getGlobe().computePointFromPosition(pos.getLatitude(), pos.getLongitude(), height);
        }
    }

    private double computeSegmentLength(DrawContext dc, Position posA, Position posB)
    {
        LatLon llA = new LatLon(posA.getLatitude(), posA.getLongitude());
        LatLon llB = new LatLon(posB.getLatitude(), posB.getLongitude());

        Angle ang = LatLon.sphericalDistance(llA, llB);

        if (this.followTerrain)
        {
            return ang.radians * (dc.getGlobe().getRadius() + this.offset);
        }
        else
        {
            double height = this.offset + 0.5 * (posA.getElevation() + posB.getElevation());
            return ang.radians * (dc.getGlobe().getRadius() + height);
        }
    }

    private ArrayList<Vec4> makeSegment(DrawContext dc, Position posA, Position posB)
    {
        ArrayList<Vec4> span = null;

        Vec4 ptA = this.computePoint(dc, posA, false); // point w/o offset applied
        Vec4 ptB;

        double arcLength = this.computeSegmentLength(dc, posA, posB);
        if (arcLength <= 0) // points differing only in altitude
        {
            ptA = this.computePoint(dc, posA, true); // points w/o offset applied
            ptB = this.computePoint(dc, posB, true);
            span = this.addPointToSpan(ptA, span);
            if (!ptA.equals(ptB))
                span = this.addPointToSpan(ptB, span);
            return span;
        }

        Vec4 origPtA = null;
        Vec4 axis = null;
        Quaternion qA = null;
        Quaternion qB = null;

        for (double s = 0, p = 0; s < 1;)
        {
            if (origPtA == null)
            {
                origPtA = ptA;
                ptA = this.computePoint(dc, posA, true);
            }

            if (this.followTerrain)
                p += this.terrainConformance * dc.getView().computePixelSizeAtDistance(
                    ptA.distanceTo3(dc.getView().getEyePoint()));
            else
                p += arcLength / this.numSubsegments;

            s = p / arcLength;

            Position pos;
            if (s >= 1)
            {
                pos = posB;
            }
            else if (this.pathType == LINEAR)
            {
                pos = Position.interpolate(s, posA, posB);
            }
            else
            {
                if (axis == null)
                {
                    ptB = this.computePoint(dc, posB, false);
                    axis = origPtA.cross3(ptB).normalize3();
                    Angle ang = origPtA.angleBetween3(ptB);
                    qA = Quaternion.fromAxisAngle(Angle.ZERO, axis);
                    qB = Quaternion.fromAxisAngle(ang, axis);
                }
                Quaternion q = Quaternion.slerp(s, qA, qB);
                Vec4 pp = origPtA.transformBy3(q);
                pos = dc.getGlobe().computePositionFromPoint(pp);
            }

            ptB = this.computePoint(dc, pos, true);
            span = this.clipAndAdd(dc, ptA, ptB, span);
            this.length += ptA.distanceTo3(ptB);

            ptA = ptB;
        }

        return span;
    }

    private ArrayList<Vec4> clipAndAdd(DrawContext dc, Vec4 ptA, Vec4 ptB, ArrayList<Vec4> span)
    {
        // Line clipping appears to be useful only for long lines with few segments. It's costly otherwise.
        // TODO: Investigate trade-off of line clipping.
//        if (Line.clipToFrustum(ptA, ptB, dc.getView().getFrustumInModelCoordinates()) == null)
//        {
//            if (span != null)
//            {
//                this.addSpan(span);
//                span = null;
//            }
//            return span;
//        }

        if (span == null)
            span = this.addPointToSpan(ptA, span);

        return this.addPointToSpan(ptB, span);
    }

    private ArrayList<Vec4> addPointToSpan(Vec4 p, ArrayList<Vec4> span)
    {
        if (span == null)
            span = new ArrayList<Vec4>();

        span.add(p.subtract3(this.referenceCenterPoint));

        return span;
    }

    private void computeReferenceCenter(DrawContext dc)
    {
        if (this.positions.size() < 1)
            return;

        if (this.positions.size() < 3)
            this.referenceCenterPosition = this.positions.get(0);
        else
            this.referenceCenterPosition = this.positions.get(this.positions.size() / 2);

        this.referenceCenterPoint = this.computeTerrainPoint(dc,
            this.referenceCenterPosition.getLatitude(), this.referenceCenterPosition.getLongitude(), this.offset);
    }

    public Position getReferencePosition()
    {
        return this.referenceCenterPosition;
    }

    private Vec4 computeTerrainPoint(DrawContext dc, Angle lat, Angle lon, double offset)
    {
        Vec4 p = dc.getSurfaceGeometry().getSurfacePoint(lat, lon, offset);

        if (p == null)
        {
            p = dc.getGlobe().computePointFromPosition(lat, lon,
                offset + dc.getGlobe().getElevation(lat, lon) * dc.getVerticalExaggeration());
        }

        return p;
    }

    public void move(Position delta)
    {
        if (delta == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.moveTo(this.getReferencePosition().add(delta));
    }

    public void moveTo(Position position)
    {
        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.reset();

        if (this.positions.size() < 1)
            return;

        Vec4 origRef = this.referenceCenterPoint;
        Vec4 newRef = this.globe.computePointFromPosition(position);
        Angle distance =
            LatLon.sphericalDistance(this.referenceCenterPosition.getLatLon(), position.getLatLon());
        Vec4 axis = origRef.cross3(newRef).normalize3();
        Quaternion q = Quaternion.fromAxisAngle(distance, axis);

        for (int i = 0; i < this.positions.size(); i++)
        {
            Position pos = this.positions.get(i);
            Vec4 p = this.globe.computePointFromPosition(pos);
            p = p.transformBy3(q);
            pos = this.globe.computePositionFromPoint(p);
            this.positions.set(i, pos);
        }
    }
}
