/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;

/**
 * <code>View</code> provides a coordinate transformation from <code>Model</code> coordinates to eye coordinates,
 * following the OpenGL convention of a left-handed coordinate system with the origin at the eye point and looking down
 * the negative Z axis. <code>View</code> also provides a transformation from eye coordinates to screen coordinates,
 * following the OpenGL convention of an origin in the lower left hand screen corner.
 * </p>
 * <code>View</code> contains both fixed state and computed state. The computed state is typically updated during a call
 * to the {@link #apply} method. Most accessor methods in this interface return the computed state that was set during
 * the most recent call to <code>apply()</code>.
 *
 * @author Paul Collins
 * @version $Id: View.java 3557 2007-11-17 04:10:32Z dcollins $
 * @see ViewStateIterator
 * @see gov.nasa.worldwind.view.OrbitView
 */
public interface View extends WWObject
{
    /**
     * Calculates and applies <code>View's</code> internal state to the graphics context in <code>DrawContext</code>.
     * All subsequently rendered objects use this new state. Upon return, the OpenGL graphics context reflects the
     * values of this view, as do any computed values of the view, such as the model-view matrix, projection matrix and
     * <code>Frustum</code>.
     *
     * @param dc the current World Wind drawing context on which <code>View's</code> state will apply.
     * @throws IllegalArgumentException if <code>dc</code> is null, or if the <code>Globe</code> or <code>GL</code>
     *                                  instances in <code>dc</code> are null.
     */
    void apply(DrawContext dc);

    /**
     * Maps a <code>Point</code> in model (cartesian) coordinates to a <code>Point</code> in screen coordinates. The
     * returned x and y are relative to the lower left hand screen corner, while z is the screen depth-coordinate. If
     * the model point cannot be sucessfully mapped, this will return null.
     *
     * @param modelPoint the model coordinate <code>Point</code> to project.
     * @return the mapped screen coordinate <code>Point</code>.
     * @throws IllegalArgumentException if <code>modelPoint</code> is null.
     */
    Vec4 project(Vec4 modelPoint);

    /**
     * Maps a <code>Point</code> in screen coordinates to a <code>Point</code> in model coordinates. The input x and y
     * are  relative to the lower left hand screen corner, while z is the screen depth-coordinate.  If the screen point
     * cannot be sucessfully mapped, this will return null.
     *
     * @param windowPoint the window coordinate <code>Point</code> to project.
     * @return the mapped screen coordinate <code>Point</code>.
     * @throws IllegalArgumentException if <code>windowPoint</code> is null.
     */
    Vec4 unProject(Vec4 windowPoint);

    /**
     * Defines and applies a new model-view matrix in which the world origin is located at <code>referenceCenter</code>.
     * Geometry rendered after a call to <code>pushReferenceCenter</code> should be transformed with respect to
     * <code>referenceCenter</code>, rather than the canonical origin (0, 0, 0). Calls to
     * <code>pushReferenceCenter</code> must be followed by {@link #popReferenceCenter} after rendering is complete.
     * Note that calls to {@link #getModelViewMatrix} will not return reference-center model-view matrix, but the
     * original matrix.
     *
     * @param dc              the current World Wind drawing context on which new model-view state will be applied.
     * @param referenceCenter the location to become the new world origin.
     * @return a new model-view matrix with origin is at <code>referenceCenter</code>, or null if this method failed.
     * @throws IllegalArgumentException if <code>referenceCenter</code> is null, if <code>dc</code> is null, or if the
     *                                  <code>Globe</code> or <code>GL</code> instances in <code>dc</code> are null.
     */
    Matrix pushReferenceCenter(DrawContext dc, Vec4 referenceCenter);

    /**
     * Removes the model-view matrix on top of the matrix stack, and restores the original matrix.
     *
     * @param dc the current World Wind drawing context on which the original matrix will be restored.
     * @throws IllegalArgumentException if <code>dc</code> is null, or if the <code>Globe</code> or <code>GL</code>
     *                                  instances in <code>dc</code> are null.
     */
    void popReferenceCenter(DrawContext dc);

    /**
     * Gets the 'model-view' matrix computed in <code>apply()</code>, which transforms model coordinates to eye
     * coordinates (where the eye is located at the origin, facing down the negative-z axis). This matrix is constructed
     * using the model space translation and orientation specific to each implementation of <code>View</code>.
     *
     * @return the current model-view matrix.
     */
    Matrix getModelViewMatrix();

    /**
     * Gets the 'projection' matrix computed in <code>apply()</code>, which transforms eye coordinates to screen
     * coordinates. This matrix is constructed using the projection parameters specific to each implementation of
     * <code>View</code> (e.g. field-of-view). The {@link #getFrustum} method returns the geometry corresponding to this
     * matrix.
     *
     * @return the current projection matrix.
     */
    Matrix getProjectionMatrix();

    /**
     * Gets a Rectangle representing the window bounds (x, y, width, height) of the viewport, computed in
     * <code>apply()</code>. Implementations of <code>View</code> will configure themselves to render in this viewport.
     *
     * @return the current window bounds of the viewport.
     */
    java.awt.Rectangle getViewport();

    /**
     * Gets the viewing <code>Frustum</code> in eye coordinates, computed in <code>apply()</code>. The
     * <code>Frustum</code> is the portion of viewable space defined by three sets of parallel 'clipping' planes. The
     * method {@link #getFrustumInModelCoordinates} maintains the shape of this <code>Frustum</code>, but it has been
     * translated and aligned with the eye in model space.
     *
     * @return the current viewing frustum in eye coordinates.
     */
    Frustum getFrustum();

    /**
     * Gets the viewing <code>Frustum</code> transformed to model coordinates. Model coordinate frustums are useful for
     * performing visibility tests against world geometry.
     *
     * @return the current viewing frustum in model coordinates.
     */
    Frustum getFrustumInModelCoordinates();

    /**
     * Gets the horizontal field-of-view angle (the angle of visibility) associated with this <code>View</code>, or null
     * if the <code>View</code> implementation does not support a field-of-view.
     *
     * @return horizontal field-of-view angle, or null if none exists.
     */
    Angle getFieldOfView();

    /**
     * Sets the horiziontal field-of-view angle (the angle of visibillity) associated with this <code>View</code>. This
     * call may be ignored by implementations that do not support a field-of-view.
     *
     * @param newFov the new horizontal field-of-view angle.
     * @throws IllegalArgumentException if <code>newFov</code> is null.
     */
    void setFieldOfView(Angle newFov);

    /**
     * Gets the <code>View</code> eye position in model coordinates.
     *
     * @return the eye position in model coordinates.
     */
    Vec4 getEyePoint();

    /**
     * Gets the position of the <code>View</code> eye point in geographic coordinates.
     *
     * @return the geographic coordinates of the eye point.
     */
    Position getEyePosition();

    /**
     * Gets the <code>View</code> y-axis orientation in model coordinates.
     *
     * @return the y-axis vector in model coordinates.
     */
    Vec4 getUpVector();

    /**
     * Gets the <code>View</code> z-axis orientation in model coordinates.
     *
     * @return the z-axis vector in model coordinates.
     */
    Vec4 getForwardVector();

    /**
     * Computes a line, in model coordinates, originating from the eye point, and passing throught the point contained
     * by (x, y) on the <code>View's</code> projection plane (or after projection into model space).
     *
     * @param x the horizontal coordinate originating from the left side of <code>View's</code> projection plane.
     * @param y the vertical coordinate originating from the top of <code>View's</code> projection plane.
     * @return a line beginning at the <code>View's</code> eye point and passing throught (x, y) transformed into model
     *         space.
     */
    Line computeRayFromScreenPoint(double x, double y);

    /**
     * Computes the intersection of a line originating from the eye point (passing throught (x, y)) with the last
     * rendered <code>SectorGeometry</code>, or the last analytical <code>Globe</code> if no rendered geometry exists.
     *
     * @param x the horizontal coordinate originating from the left side of <code>View's</code> projection plane.
     * @param y the vertical coordinate originating from the top of <code>View's</code> projection plane.
     * @return the point on the surface in polar coordiantes.
     */
    Position computePositionFromScreenPoint(double x, double y);

    /**
     * Computes the screen-aligned dimension (in meters) that a screen pixel would cover at a given distance (also in
     * meters). This computation assumes that pixels dimensions are square, and therefore returns a single dimension.
     *
     * @param distance the distance from the eye point, in eye coordinates, along the z-axis. This value must be
     *                 positive but is otherwise unbounded.
     * @return the dimension of a pixel (in meters) at the given distance.
     * @throws IllegalArgumentException if <code>distance</code> is negative.
     */
    double computePixelSizeAtDistance(double distance);

    /**
     * Gets the distance from the <code>View's</code> eye point to the horizon point on the last rendered
     * <code>Globe</code>.
     *
     * @return the distance from the eye point to the horizon (in meters).
     */
    double computeHorizonDistance();

    /**
     * Iterates over <code>View</code> state changes in <code>ViewStateIterator</code> and applies them to the
     * <code>View</code>. The <code>View</code> will automatically refresh and request state from
     * <code>viewStateIterator</code> until the iteration is complete, or <code>View</code> has been stopped by invoking
     * {@link #stopStateIterators}.
     *
     * @param viewStateIterator the <code>ViewStateIterator</code> to iterate over.
     */
    void applyStateIterator(ViewStateIterator viewStateIterator);

    /**
     * Returns true when <code>View</code> is actively iterating over an instance of <code>ViewStateIterator</code>.
     *
     * @return true when iterating over <code>ViewStateIterator</code>; false otherwise.
     */
    boolean hasStateIterator();

    /**
     * Immediately stops all active iteration over <code>ViewStateIterator</code>.
     */
    void stopStateIterators();
}
