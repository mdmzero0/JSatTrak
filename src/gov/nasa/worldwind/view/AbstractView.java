/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

/**
 * @author Paul Collins
 * @version $Id: AbstractView.java 3528 2007-11-15 16:49:06Z dcollins $
 */
public abstract class AbstractView extends WWObjectImpl implements View
{
    // ============== Viewing State ======================= //
    // ============== Viewing State ======================= //
    // ============== Viewing State ======================= //

    private Matrix modelView;
    private Matrix projection;
    private java.awt.Rectangle viewport;
    private final double[] matrixArray = new double[32];
    private final double[] vecArray = new double[4];
    private final int[] viewportArray = new int[4];
    private final int[] matrixModeArray = new int[1];

    private static final double MIN_NEAR_CLIP_DISTANCE = 10;
    private static final double MIN_FAR_CLIP_DISTANCE = 10000000000d;

    public void apply(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (dc.getGlobe() == null)
        {
            String message = Logging.getMessage("layers.AbstractLayer.NoGlobeSpecifiedInDrawingContext");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.updateViewingState(dc);
        this.updateStateIterator();
        this.doApply(dc);
    }

    private void updateViewingState(DrawContext dc)
    {
        this.clearAttributes();
        this.drawContext = dc;

        // Get the current OpenGL viewport state.
        dc.getGL().glGetIntegerv(GL.GL_VIEWPORT, this.viewportArray, 0);
        this.viewport = new java.awt.Rectangle(
            this.viewportArray[0],
            this.viewportArray[1],
            this.viewportArray[2],
            this.viewportArray[3]);

        if (!this.isInitialized)
        {
            this.initialize(dc);
            this.isInitialized = true;
        }
    }

    private int getMatrixMode(GL gl)
    {
        gl.glGetIntegerv(GL.GL_MATRIX_MODE, this.matrixModeArray, 0);
        return this.matrixModeArray[0];
    }

    protected void loadModelViewProjection(DrawContext dc, Matrix modelView, Matrix projection)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (modelView == null)
        {
            Logging.logger().fine("nullValue.ModelViewIsNull");
        }

        if (projection == null)
        {
            Logging.logger().fine("nullValue.ProjectionIsNull");
        }

        this.modelView = modelView;
        this.projection = projection;

        GL gl = dc.getGL();
        // Store the current matrix-mode state.
        final int matrixMode = this.getMatrixMode(gl);

        // Apply the model-view matrix to the current OpenGL context held by 'dc'.
        gl.glMatrixMode(GL.GL_MODELVIEW);
        if (this.modelView != null)
        {
            this.modelView.toArray(this.matrixArray, 0, false);
            gl.glLoadMatrixd(this.matrixArray, 0);
        }
        else
        {
            gl.glLoadIdentity();
        }

        // Apply the projection matrix to the current OpenGL context held by 'dc'.
        gl.glMatrixMode(GL.GL_PROJECTION);
        if (this.projection != null)
        {
            this.projection.toArray(this.matrixArray, 0, false);
            gl.glLoadMatrixd(this.matrixArray, 0);
        }
        else
        {
            gl.glLoadIdentity();    
        }

        // Restore matrix-mode state.
        gl.glMatrixMode(matrixMode);
    }

    protected abstract void doApply(DrawContext dc);

    public Vec4 project(Vec4 modelPoint)
    {
        if (modelPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        if (this.modelView == null || this.projection == null || this.viewport == null)
            return null;

        this.modelView.toArray(this.matrixArray, 0, false);
        this.projection.toArray(this.matrixArray, 16, false);

        if (!this.glu.gluProject(
            modelPoint.x, modelPoint.y, modelPoint.z,
            this.matrixArray, 0,
            this.matrixArray, 16,
            this.viewportArray, 0,
            this.vecArray, 0))
        {
            return null;
        }

        return Vec4.fromArray3(this.vecArray, 0);
    }

    public Vec4 unProject(Vec4 windowPoint)
    {
        if (windowPoint == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        if (this.modelView == null || this.projection == null || this.viewport == null)
            return null;

        this.modelView.toArray(this.matrixArray, 0, false);
        this.projection.toArray(this.matrixArray, 16, false);

        if (!this.glu.gluUnProject(
            windowPoint.x, windowPoint.y, windowPoint.z,
            this.matrixArray, 0,
            this.matrixArray, 16,
            this.viewportArray, 0,
            this.vecArray, 0))
        {
            return null;
        }

        return Vec4.fromArray3(this.vecArray, 0);
    }

    public Matrix pushReferenceCenter(DrawContext dc, Vec4 referenceCenter)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (referenceCenter == null)
        {
            String message = Logging.getMessage("nullValue.PointIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Compute a new model-view matrix with origin at referenceCenter.
        Matrix matrix = null;
        if (this.modelView != null)
            matrix = this.modelView.multiply(Matrix.fromTranslation(referenceCenter));

        GL gl = dc.getGL();
        // Store the current matrix-mode state.
        final int matrixMode = this.getMatrixMode(gl);

        if (matrixMode != GL.GL_MODELVIEW)
            gl.glMatrixMode(GL.GL_MODELVIEW);

        // Push and load a new model-view matrix to the current OpenGL context held by 'dc'.
        gl.glPushMatrix();
        if (matrix != null)
        {
            matrix.toArray(this.matrixArray, 0, false);
            gl.glLoadMatrixd(this.matrixArray, 0);
        }

        // Restore matrix-mode state.
        if (matrixMode != GL.GL_MODELVIEW)
            gl.glMatrixMode(matrixMode);

        return matrix;
    }

    public void popReferenceCenter(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (dc.getGL() == null)
        {
            String message = Logging.getMessage("nullValue.DrawingContextGLIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        GL gl = dc.getGL();
        // Store the current matrix-mode state.
        final int matrixMode = this.getMatrixMode(gl);

        // Pop a model-view matrix off the current OpenGL context held by 'dc'.
        if (matrixMode != GL.GL_MODELVIEW)
            gl.glMatrixMode(GL.GL_MODELVIEW);

        // Pop the top model-view matrix.
        gl.glPopMatrix();

        // Restore matrix-mode state.
        if (matrixMode != GL.GL_MODELVIEW)
            gl.glMatrixMode(matrixMode);
    }

    public Matrix getModelViewMatrix()
    {
        return this.modelView;
    }

    public Matrix getProjectionMatrix()
    {
        return this.projection;
    }

    public java.awt.Rectangle getViewport()
    {
        if (this.viewport == null)
            return null;
        
        return new java.awt.Rectangle(this.viewport);
    }

    public Frustum getFrustumInModelCoordinates()
    {
        if (this.frustumInModelCoords == null)
        {
            // Compute the current model-view coordinate frustum.
            Frustum frust = this.getFrustum();
            if (frust != null && this.modelView != null)
                this.frustumInModelCoords = frust.transformBy(this.modelView.getTranspose());
        }
        return this.frustumInModelCoords;
    }

    // ============== Runtime Initialization ======================= //
    // ============== Runtime Initialization ======================= //
    // ============== Runtime Initialization ======================= //

    private boolean isInitialized = false;

    private void initialize(DrawContext dc)
    {
        this.fieldOfView = getInitialFieldOfView(this.fieldOfView);
        this.doInitialize(dc);
    }

    protected void doInitialize(DrawContext dc)
    {
    }

    private static Angle getInitialFieldOfView(Angle clientValue)
    {
        // Use value specified by client.
        if (clientValue != null)
            return clientValue;

        // Use value from configuration.
        Double configValue = Configuration.getDoubleValue(AVKey.FOV, 45d);
        if (configValue != null)
            return Angle.fromDegrees(configValue);

        // Fallback to zero.
        return Angle.fromDegrees(45.0);
    }

    // ============== Attribute Accessors ======================= //
    // ============== Attribute Accessors ======================= //
    // ============== Attribute Accessors ======================= //

    // Current DrawContext state.
    private DrawContext drawContext = null;
    // Cached viewing attribute computations.
    private Vec4 eye = null;
    private Vec4 up = null;
    private Vec4 forward = null;
    private Position eyePos = null;
    private Frustum frustumInModelCoords = null;
    private Angle fieldOfView = null;
    private double pixelSizeScale = -1;
    private double horizonDistance = -1;

    private void clearAttributes()
    {
        this.drawContext = null;
        this.eye = null;
        this.up = null;
        this.forward = null;
        this.eyePos = null;
        this.frustumInModelCoords = null;
        this.pixelSizeScale = -1;
        this.horizonDistance = -1;
    }

    protected DrawContext getDrawContext()
    {
        return this.drawContext;
    }

    public Angle getFieldOfView()
    {
        return this.fieldOfView;
    }

    public void setFieldOfView(Angle newFov)
    {
        if (newFov == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        this.fieldOfView = newFov;
    }

    public Vec4 getEyePoint()
    {
        if (this.eye == null)
        {
            Matrix modelViewInv;
            if (this.modelView != null && (modelViewInv = this.modelView.getInverse()) != null)
                this.eye = Vec4.UNIT_W.transformBy4(modelViewInv);
        }
        return this.eye;
    }

    public Position getEyePosition()
    {
        if (this.eyePos == null)
        {
            if (this.drawContext != null && this.drawContext.getGlobe() != null)
            {
                Vec4 eyeVec = this.getEyePoint();
                if (eyeVec != null)
                    this.eyePos = this.drawContext.getGlobe().computePositionFromPoint(eyeVec);
            }
        }
        return this.eyePos;
    }

    public Vec4 getUpVector()
    {
        if (this.up == null)
        {
            Matrix modelViewInv;
            if (this.modelView != null && (modelViewInv = this.modelView.getInverse()) != null)
                this.up = Vec4.UNIT_Y.transformBy4(modelViewInv);
        }
        return this.up;
    }

    public Vec4 getForwardVector()
    {
        if (this.forward == null)
        {
            Matrix modelViewInv;
            if (this.modelView != null && (modelViewInv = this.modelView.getInverse()) != null)
                this.forward = Vec4.UNIT_NEGATIVE_Z.transformBy4(modelViewInv);
        }
        return this.forward;
    }

    // ============== Utilities ======================= //
    // ============== Utilities ======================= //
    // ============== Utilities ======================= //

    private final GLU glu = new GLU();

    // TODO: this should be expressed in OpenGL screen coordinates, not toolkit (e.g. AWT) coordinates
    public Line computeRayFromScreenPoint(double x, double y)
    {
        if (this.viewport == null)
            return null;

        double invY = this.viewport.height - y - 1; // TODO: should be computed by caller
        Vec4 a = this.unProject(new Vec4(x, invY, 0, 0));
        Vec4 b = this.unProject(new Vec4(x, invY, 1, 0));
        if (a == null || b == null)
            return null;

        return new Line(a, b.subtract3(a).normalize3());
    }

    public Position computePositionFromScreenPoint(double x, double y)
    {
        if (this.drawContext == null)
            return null;

        Globe globe = this.drawContext.getGlobe();
        if (globe == null)
            return null;

        Line line = this.computeRayFromScreenPoint(x, y);
        if (line == null)
            return null;

        return globe.getIntersectionPosition(line);
    }

    public double computePixelSizeAtDistance(double distance)
    {
        if (this.pixelSizeScale < 0)
        {
            // Compute the current coefficient for computing the size of a pixel.
            if (this.fieldOfView != null && this.viewport.width > 0)
                this.pixelSizeScale = 2 * this.fieldOfView.tanHalfAngle() / (double) this.viewport.width;
            else if (this.viewport.width > 0)
                this.pixelSizeScale = 1 / (double) this.viewport.width;
        }
        if (this.pixelSizeScale < 0)
            return -1;
        return this.pixelSizeScale * Math.abs(distance);
    }

    public double computeHorizonDistance()
    {
        if (this.horizonDistance < 0)
        {
            if (this.drawContext == null)
                return this.horizonDistance;

            double altitude = this.computeHeightAboveGlobe(this.drawContext, this.getEyePoint());
            this.horizonDistance = this.computeHorizonDistance(this.drawContext, altitude);
        }
        return this.horizonDistance;
    }

    protected double computeHorizonDistance(DrawContext dc, double altitude)
    {
        Globe globe = dc.getGlobe();
        if (globe == null)
            return -1;

        if (altitude <= 0.0)
            return 0;

        // Compute the (approximate) distance from eye point to globe horizon.
        double radius = globe.getMaximumRadius();
        return Math.sqrt(altitude * (2 * radius + altitude));
    }

    protected double computeHeightAboveGlobe(DrawContext dc, Vec4 point)
    {
        Globe globe = dc.getGlobe();
        if (globe == null)
            return 0;

        Position globePos = globe.computePositionFromPoint(point);
        return globePos.getElevation();
    }

    protected double computeHeightAboveSurface(DrawContext dc, Vec4 point)
    {
        Globe globe = dc.getGlobe();
        if (globe == null)
            return 0;

        Position globePos = globe.computePositionFromPoint(point);
        double elevation = globe.getElevation(globePos.getLatitude(), globePos.getLongitude())
                * dc.getVerticalExaggeration();

        return globePos.getElevation() - elevation;
    }

    protected double computeNearClipDistance(DrawContext dc, Vec4 eyeVec)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (eyeVec == null)
        {
            String message = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Angle fov = this.getFieldOfView();
        if (fov == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double heightAboveGlobe = this.computeHeightAboveGlobe(dc, eyeVec);
        double tanHalfFov = fov.tanHalfAngle();
        // Compute the most distant near clipping plane.
        double nearClipDist = heightAboveGlobe / (2 * Math.sqrt(2 * tanHalfFov * tanHalfFov + 1));

        if (nearClipDist < MIN_NEAR_CLIP_DISTANCE)
            return MIN_NEAR_CLIP_DISTANCE;
        return nearClipDist;
    }

    protected double computeFarClipDistance(DrawContext dc, Vec4 eyeVec)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (eyeVec == null)
        {
            String message = Logging.getMessage("nullValue.Vec4IsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double heightAboveGlobe = this.computeHeightAboveGlobe(dc, eyeVec);
        double heightAboveSurface = this.computeHeightAboveSurface(dc, eyeVec);
        double farClipDist = Math.max(
                this.computeHorizonDistance(dc, heightAboveGlobe),
                this.computeHorizonDistance(dc, heightAboveSurface));

        if (farClipDist < MIN_FAR_CLIP_DISTANCE)
            return MIN_FAR_CLIP_DISTANCE;
        return farClipDist;
    }

//    public LatLon computeVisibleLatLonRange()
//    {
//        return null; // TODO
//    }

    // ============== State Iterating ======================= //
    // ============== State Iterating ======================= //
    // ============== State Iterating ======================= //

    private ViewStateIterator viewStateIterator = null;

    public void applyStateIterator(ViewStateIterator viewStateIterator)
    {
        if (viewStateIterator == null)
        {
            this.stopStateIterators();
            return;
        }

        this.viewStateIterator = viewStateIterator.coalesceWith(this, this.viewStateIterator);
        this.firePropertyChange(AVKey.VIEW, null, this);
    }

    public boolean hasStateIterator()
    {
        return this.viewStateIterator != null;
    }

    public void stopStateIterators()
    {
        this.viewStateIterator = null;
    }

    private void updateStateIterator()
    {
        if (!this.hasStateIterator())
            return;

        if (this.viewStateIterator.hasNextState(this))
        {
            this.viewStateIterator.nextState(this);
            this.firePropertyChange(AVKey.VIEW, null, this);
        }
        else
        {
            this.stopStateIterators();
            this.fireViewQuiet();
        }
    }

    private void fireViewQuiet()
    {
        this.firePropertyChange(AVKey.VIEW_QUIET, null, this);    
    }
}
