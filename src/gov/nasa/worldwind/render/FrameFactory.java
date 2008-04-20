/*
Copyright (C) 2001, 2006, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.render;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import java.awt.*;
import java.nio.DoubleBuffer;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.util.Logging;

/**
 * Static class for drawing 2D frames.
 * <p>
 * All shapes are drawn inside a bounding rectangle whose lower left corner
 * is at the origin. Shapes with a leader use an offset point that indicate where the
 * leader triangle should point at - it usually has a negative y since the leader connects
 * at the bottom of the frame (at y = 0).
 * </p>
 * @author Patrick Murris
 * @version $Id$
 * @see AbstractAnnotation
 */
public class FrameFactory {
    public static final String SHAPE_RECTANGLE = "Render.FrameFactory.ShapeRectangle";
    public static final String SHAPE_ELLIPSE = "Render.FrameFactory.ShapeEllipse";
    public static final String SHAPE_NONE = "Render.FrameFactory.ShapeNone";

    public static final String LEADER_TRIANGLE = "Render.FrameFactory.LeaderTriangle";
    public static final String LEADER_NONE = "Render.FrameFactory.LeaderNone";

    private static int cornerSteps = 5;
    private static int leaderGapWidth = 6;
    private static int circleSteps = 36;

    /**
     * Draw a shape with the specified width and height, gl mode and corner radius. GL mode came be one of
     * <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>. Corner radius only apply
     * to <code>SHAPE_RECTANGLE</code> - set to zero for square corners.
     * @param dc the current <code>DrawContext</code>.
     * @param shape the shape - can be one of <code>SHAPE_RECTANGLE</code> or <code>SHAPE_ELLIPSE</code>.
     * @param width the width of the overall shape.
     * @param height the height of the shape.
     * @param glMode the GL mode - can be one of <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>.
     * @param cornerRadius the rounded corners radius. Set to zero for square corners.
     */
    public static void drawShape(DrawContext dc, String shape, double width, double height, int glMode, int cornerRadius)
    {
        if (shape.compareTo(SHAPE_NONE) != 0)
            drawBuffer(dc, glMode, createShapeBuffer(shape, width, height, cornerRadius));
    }

    /**
     * Draw a shape with the specified width and height, gl mode and corner radius. The shape includes a
     * leader triangle pointing to a specified point. GL mode came be one of <code>GL.GL_TRIANGLE_FAN</code>
     * and <code>GL.LINE_STRIP</code>. Corner radius only apply to <code>SHAPE_RECTANGLE</code> - set to zero for square corners.
     * @param dc the current <code>DrawContext</code>.
     * @param shape the shape - can be one of <code>SHAPE_RECTANGLE</code> or <code>SHAPE_ELLIPSE</code>.
     * @param width the width of the overall shape.
     * @param height the height of the shape excluding the leader.
     * @param leaderOffset the coordinates of the point to which the leader leads.
     * @param glMode the GL mode - can be one of <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>.
     * @param cornerRadius the rounded corners radius. Set to zero for square corners.
     */
    public static void drawShapeWithLeader(DrawContext dc, String shape, double width, double height, Point leaderOffset, int glMode, int cornerRadius)
    {
        if (shape.compareTo(SHAPE_NONE) != 0)
            drawBuffer(dc, glMode, createShapeWithLeaderBuffer(shape, width, height, leaderOffset, cornerRadius));
    }

    /**
     * Create a vertex buffer for a shape with the specified width, height and corner radius. Corner radius only apply
     * to <code>SHAPE_RECTANGLE</code> - set to zero for square corners.
     * @param shape the shape - can be one of <code>SHAPE_RECTANGLE</code> or <code>SHAPE_ELLIPSE</code>.
     * @param width the width of the overall shape.
     * @param height the height of the shape.
     * @param cornerRadius the rounded corners radius. Set to zero for square corners.
     * @return the vertex buffer.
     */
    public static DoubleBuffer createShapeBuffer(String shape, double width, double height, int cornerRadius)
    {
        if (shape.equals(SHAPE_RECTANGLE))
            return createRoundedRectangleBuffer(width, height, cornerRadius);
        else if (shape.equals(SHAPE_ELLIPSE))
            return createEllipseBuffer(width, height, circleSteps);
        else if (shape.equals(SHAPE_NONE))
            return null;
        else
            // default to rectangle if shape unknown
            return createRoundedRectangleBuffer(width, height, cornerRadius);

    }

    /**
     * Create a vertex buffer for a shape with the specified width, height and corner radius. The shape includes a
     * leader triangle pointing to a specified point. Corner radius only apply to <code>SHAPE_RECTANGLE</code>
     * - set to zero for square corners.
     * @param shape the shape - can be one of <code>SHAPE_RECTANGLE</code> or <code>SHAPE_ELLIPSE</code>.
     * @param width the width of the overall shape.
     * @param height the height of the shape excluding the leader.
     * @param leaderOffset the coordinates of the point to which the leader leads.
     * @param cornerRadius the rounded corners radius. Set to zero for square corners.
     * @return the vertex buffer.
     */
    public static DoubleBuffer createShapeWithLeaderBuffer(String shape, double width, double height, Point leaderOffset, int cornerRadius)
    {
        if (shape.equals(SHAPE_RECTANGLE))
            return createRoundedRectangleWithLeaderBuffer(width, height, leaderOffset, cornerRadius);
        else if (shape.equals(SHAPE_ELLIPSE))
            return createEllipseWithLeaderBuffer(width, height, leaderOffset, circleSteps);
        else if (shape.equals(SHAPE_NONE))
            return null;
        else
            // default to rectangle if shape unknown
            return createRoundedRectangleWithLeaderBuffer(width, height, leaderOffset, cornerRadius);
    }

    /**
     * Draw a vertex buffer in a given gl mode. Vertex buffers coming from the createShapeBuffer() methods support
     * both <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>.
     * @param dc the current DrawContext.
     * @param glMode the desired drawing GL mode.
     * @param verts the vertex buffer to draw.
     */
    public static void drawBuffer(DrawContext dc, int glMode, DoubleBuffer verts)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (verts == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        GL gl = dc.getGL();
        // Set up
        gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(2, GL.GL_DOUBLE, 0, verts.rewind());
        // Draw
        gl.glDrawArrays(glMode, 0, verts.limit() / 2);
        // Restore
        gl.glPopClientAttrib();
    }

    /**
     * Draw a vertex buffer with texture coordinates in a given gl mode. Vertex buffers coming from the
     * createShapeBuffer() methods support both <code>GL.GL_TRIANGLE_FAN</code> and <code>GL.LINE_STRIP</code>.
     * @param dc the current DrawContext.
     * @param glMode the desired drawing GL mode.
     * @param verts the vertex buffer to draw.
     */
    public static void drawBuffer(DrawContext dc, int glMode, DoubleBuffer verts, DoubleBuffer coords)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (verts == null || coords == null)
        {
            String message = Logging.getMessage("nullValue.BufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        GL gl = dc.getGL();
        // Set up
        gl.glPushClientAttrib(GL.GL_CLIENT_VERTEX_ARRAY_BIT);
        gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
        gl.glVertexPointer(2, GL.GL_DOUBLE, 0, verts.rewind());
        gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
        gl.glTexCoordPointer(2, GL.GL_DOUBLE, 0, coords.rewind());
        // Draw
        gl.glDrawArrays(glMode, 0, verts.limit() / 2);
        // Restore
        gl.glPopClientAttrib();
    }

    //-- Shape creation
    //-- Rectangle ------------------------------------------------------------------

    private static DoubleBuffer createRoundedRectangleBuffer(double width, double height, int cornerRadius)
    {
        int numVertices = 9 + (cornerRadius < 1 ? 0 : 4 * (cornerSteps - 2));
        DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 2);
        int idx = 0;
        // Drawing counter clockwise from bottom-left
        // Bottom
        verts.put(idx++, (double)cornerRadius);
        verts.put(idx++, 0d);
        verts.put(idx++, width - cornerRadius);
        verts.put(idx++, 0d);
        idx = drawCorner(width - cornerRadius, cornerRadius, cornerRadius, -Math.PI / 2, 0, cornerSteps, verts, idx);
        // Right
        verts.put(idx++, width);
        verts.put(idx++, (double)cornerRadius);
        verts.put(idx++, width);
        verts.put(idx++, height - cornerRadius);
        idx = drawCorner(width - cornerRadius, height - cornerRadius, cornerRadius, 0, Math.PI / 2, cornerSteps, verts, idx);
        // Top
        verts.put(idx++, width - cornerRadius);
        verts.put(idx++, height);
        verts.put(idx++, (double)cornerRadius);
        verts.put(idx++, height);
        idx = drawCorner(cornerRadius, height - cornerRadius, cornerRadius, Math.PI / 2, Math.PI, cornerSteps, verts, idx);
        // Left
        verts.put(idx++, 0d);
        verts.put(idx++, height - cornerRadius);
        verts.put(idx++, 0d);
        verts.put(idx++, (double)cornerRadius);
        idx = drawCorner(cornerRadius, cornerRadius, cornerRadius, Math.PI, Math.PI * 1.5, cornerSteps, verts, idx);
        // Finish up to starting point
        verts.put(idx++, (double)cornerRadius);
        verts.put(idx++, 0d);

        return verts;
    }

    private static DoubleBuffer createRoundedRectangleWithLeaderBuffer(double width, double height, Point leaderOffset, int cornerRadius)
    {
        int numVertices = 12 + (cornerRadius < 1 ? 0 : 4 * (cornerSteps - 2));
        DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 2);
        int idx = 0;
        // Drawing counter clockwise from right leader connection at the bottom
        // so as to accomodate GL_TRIANGLE_FAN and GL_LINE_STRIP (inside and border)
        // Bottom right
        verts.put(idx++, width / 2 + leaderGapWidth / 2);
        verts.put(idx++, 0d);
        verts.put(idx++, width - cornerRadius);
        verts.put(idx++, 0d);
        idx = drawCorner(width - cornerRadius, cornerRadius, cornerRadius, -Math.PI / 2, 0, cornerSteps, verts, idx);
        // Right
        verts.put(idx++, width);
        verts.put(idx++, (double)cornerRadius);
        verts.put(idx++, width);
        verts.put(idx++, height - cornerRadius);
        idx = drawCorner(width - cornerRadius, height - cornerRadius, cornerRadius, 0, Math.PI / 2, cornerSteps, verts, idx);
        // Top
        verts.put(idx++, width - cornerRadius);
        verts.put(idx++, height);
        verts.put(idx++, (double)cornerRadius);
        verts.put(idx++, height);
        idx = drawCorner(cornerRadius, height - cornerRadius, cornerRadius, Math.PI / 2, Math.PI, cornerSteps, verts, idx);
        // Left
        verts.put(idx++, 0d);
        verts.put(idx++, height - cornerRadius);
        verts.put(idx++, 0d);
        verts.put(idx++, (double)cornerRadius);
        idx = drawCorner(cornerRadius, cornerRadius, cornerRadius, Math.PI, Math.PI * 1.5, cornerSteps, verts, idx);
        // Bottom left
        verts.put(idx++, (double)cornerRadius);
        verts.put(idx++, 0d);
        verts.put(idx++, width / 2 - leaderGapWidth / 2);
        verts.put(idx++, 0d);
        // Draw leader
        verts.put(idx++, leaderOffset.x);
        verts.put(idx++, leaderOffset.y);
        verts.put(idx++, width / 2 + leaderGapWidth / 2);
        verts.put(idx++, 0d);

        return verts;
    }

    private static int drawCorner(double x0, double y0, double cornerRadius, double start, double end, int steps, DoubleBuffer verts, int startIdx)
    {
        if(cornerRadius < 1)
            return startIdx;

        GL gl = GLU.getCurrentGL();
        double step = (end - start) / (steps - 1);
        for(int i = 1; i < steps - 1; i++)
        {
            double a = start + step * i;
            double x = x0 + Math.cos(a) * cornerRadius;
            double y = y0 + Math.sin(a) * cornerRadius;
            verts.put(startIdx++, x);
            verts.put(startIdx++, y);
        }
        return startIdx;
    }

    //-- Circle / Ellipse -----------------------------------------------------------

    private static DoubleBuffer createEllipseBuffer(double width, double height, int steps)
    {
        int numVertices = steps + 1;
        DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 2);
        int idx = 0;
        // Drawing counter clockwise from bottom-left
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfPI = Math.PI / 2;
        double x0 = halfWidth;
        double y0 = halfHeight;
        double step = Math.PI * 2 / steps;
        for(int i = 0; i <= steps; i++)
        {
            double a = step * i - halfPI;
            double x = x0 + Math.cos(a) * halfWidth;
            double y = y0 + Math.sin(a) * halfHeight;
            verts.put(idx++, x);
            verts.put(idx++, y);
        }
        return verts;
    }

    private static DoubleBuffer createEllipseWithLeaderBuffer(double width, double height, Point leaderOffset, int steps)
    {
        int numVertices = steps + 3;
        DoubleBuffer verts = BufferUtil.newDoubleBuffer(numVertices * 2);
        int idx = 0;
        // Drawing counter clockwise from right leader connection at the bottom
        // so as to accomodate GL_TRIANGLE_FAN and GL_LINE_STRIP (inside and border)
        double halfWidth = width / 2;
        double halfHeight = height / 2;
        double halfPI = Math.PI / 2;
        double x0 = halfWidth;
        double y0 = halfHeight;
        double step = Math.PI * 2 / steps;
        double halfGap = leaderGapWidth / 2 / halfWidth;
        for(int i = 0; i <= steps; i++)
        {
            double a = step * i - halfPI;
            if (i == 0) a += halfGap;
            if (i == steps) a -= halfGap;
            double x = x0 + Math.cos(a) * halfWidth;
            double y = y0 + Math.sin(a) * halfHeight;
            verts.put(idx++, x);
            verts.put(idx++, y);
        }
        // Draw leader
        verts.put(idx++, leaderOffset.x);
        verts.put(idx++, leaderOffset.y);
        verts.put(idx++, x0 + Math.cos(halfGap - halfPI) * halfWidth);
        verts.put(idx++, y0 + Math.sin(halfGap - halfPI) * halfHeight);
        return verts;
    }

    //-- Texture coordinates --------------------------------------------------------

    /**
     * Computes texture coordinates for a vertex buffer, a dimension and a texture size. Coordinates are computed
     * so that the texture image proportions and size are preserved.  The texture is aligned at top left corner
     * of the vertices bounding rectangle.
     *
     * @param verts the vertex buffer containing the vertices for which texture coordinates have to be computed.
     * @param width the vertices bounding rectangle width - excluding the leader if any.
     * @param height the vertices bounding rectangle height - excluding the leader if any.
     * @param textureWidth the texture width
     * @param textureHeight the texture height
     * @return the texture coordinates DoubleBuffer
     */
    public static DoubleBuffer getTextureCoordinates(DoubleBuffer verts, double width, double height, double textureWidth, double textureHeight)
    {
        if (verts == null)
            return null;

        int numVertices = verts.limit() / 2 ;
        DoubleBuffer coords = BufferUtil.newDoubleBuffer(numVertices * 2);
        int idx = 0;
        for(int i = 0; i < verts.limit(); i +=2)
        {
            // Top-left aligned
            coords.put(idx++, verts.get(i) / textureWidth); // Tu
            coords.put(idx++, (height - verts.get(i + 1)) / textureHeight); // Tv
        }

        return coords;
    }

}
