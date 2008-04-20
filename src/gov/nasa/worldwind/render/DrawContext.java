/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import com.sun.opengl.util.texture.TextureCoords;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.cache.TextureCache;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.PerformanceStatistic;
import gov.nasa.worldwind.View;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import java.awt.*;
import java.util.*;

/**
 * @author Tom Gaskins
 * @version $Id: DrawContext.java 3604 2007-11-21 06:26:53Z patrickmurris $
 */
public interface DrawContext extends WWObject
{
    /**
     * Assigns this <code>DrawContext</code> a new </code>javax.media.opengl.GLContext</code>. May throw a
     * <code>NullPointerException</code> if <code>glContext</code> is null.
     *
     * @param glContext the new <code>javax.media.opengl.GLContext</code>
     * @throws NullPointerException if glContext is null
     * @since 1.5
     */
    void setGLContext(GLContext glContext);

    /**
     * Retrieves this <code>DrawContext</code>s </code>javax.media.opengl.GLContext</code>. If this method returns null,
     * then there are potentially no active <code>GLContext</code>s and rendering should be aborted.
     *
     * @return this <code>DrawContext</code>s </code>javax.media.opengl.GLContext</code>.
     * @since 1.5
     */
    GLContext getGLContext();

    /**
     * Retrieves the current <code>javax.media.opengl.GL</code>. A <code>GL</code> or <code>GLU</code> is required for
     * all graphical rendering in World Wind Raptor.
     *
     * @return the current <code>GL</code> if available, null otherwise
     * @since 1.5
     */
    GL getGL();

    /**
     * Retrieves the current <code>javax.media.opengl.glu.GLU</code>. A <code>GLU</code> or <code>GL</code> is required
     * for all graphical rendering in World Wind Raptor.
     *
     * @return the current <code>GLU</code> if available, null otherwise
     * @since 1.5
     */
    GLU getGLU();

    /**
     * Retrieves the current<code>javax.media.opengl.GLDrawable</code>. A <code>GLDrawable</code> can be used to create
     * a <code>GLContext</code>, which can then be used for rendering.
     *
     * @return the current <code>GLDrawable</code>, null if none available
     * @since 1.5
     */
    GLDrawable getGLDrawable();

    /**
     * Retrieves the drawable width of this <code>DrawContext</code>.
     *
     * @return the drawable width of this <code>DrawCOntext</code>
     * @since 1.5
     */
    int getDrawableWidth();

    /**
     * Retrieves the drawable height of this <code>DrawContext</code>.
     *
     * @return the drawable height of this <code>DrawCOntext</code>
     * @since 1.5
     */
    int getDrawableHeight();

    /**
     * Initializes this <code>DrawContext</code>. This method should be called at the beginning of each frame to prepare
     * the <code>DrawContext</code> for the coming render pass.
     *
     * @param glContext the <code>javax.media.opengl.GLContext</code> to use for this render pass
     * @since 1.5
     */
    void initialize(GLContext glContext);

    /**
     * Assigns a new <code>View</code>. Some layers cannot function properly with a null <code>View</code>. It is
     * recommended that the <code>View</code> is never set to null during a normal render pass.
     *
     * @param view the enw <code>View</code>
     * @since 1.5
     */
    void setView(View view);

    /**
     * Retrieves the current <code>View</code>, which may be null.
     *
     * @return the current <code>View</code>, which may be null
     * @since 1.5
     */
    View getView();

    /**
     * Assign a new <code>Model</code>. Some layers cannot function properly with a null <code>Model</code>. It is
     * recommended that the <code>Model</code> is never set to null during a normal render pass.
     *
     * @param model the new <code>Model</code>
     * @since 1.5
     */
    void setModel(Model model);

    /**
     * Retrieves the current <code>Model</code>, which may be null.
     *
     * @return the current <code>Model</code>, which may be null
     * @since 1.5
     */
    Model getModel();

    /**
     * Retrieves the current <code>Globe</code>, which may be null.
     *
     * @return the current <code>Globe</code>, which may be null
     * @since 1.5
     */
    Globe getGlobe();

    /**
     * Retrieves a list containing all the current layers. No guarantee is made about the order of the layers.
     *
     * @return a <code>LayerList</code> containing all the current layers
     * @since 1.5
     */
    LayerList getLayers();

    /**
     * Retrieves a <code>Sector</code> which is at least as large as the current visible sector. The value returned is
     * the value passed to <code>SetVisibleSector</code>. This method may return null.
     *
     * @return a <code>Sector</code> at least the size of the curernt visible sector, null if unavailable
     * @since 1.5
     */
    Sector getVisibleSector();

    /**
     * Sets the visible <code>Sector</code>. The new visible sector must completely encompass the Sector which is
     * visible on the display.
     *
     * @param s the new visible <code>Sector</code>
     * @since 1.5
     */
    void setVisibleSector(Sector s);

    /**
     * Sets the vertical exaggeration. Vertical exaggeration affects the appearance of areas with varied elevation. A
     * vertical exaggeration of zero creates a surface which exactly fits the shape of the underlying
     * <code>Globe</code>. A vertical exaggeration of 3 will create mountains and valleys which are three times as
     * high/deep as they really are.
     *
     * @param verticalExaggeration the new vertical exaggeration.
     * @since 1.5
     */
    void setVerticalExaggeration(double verticalExaggeration);

    /**
     * Retrieves the current vertical exaggeration. Vertical exaggeration affects the appearance of areas with varied
     * elevation. A vertical exaggeration of zero creates a surface which exactly fits the shape of the underlying
     * <code>Globe</code>. A vertical exaggeration of 3 will create mountains and valleys which are three times as
     * high/deep as they really are.
     *
     * @return the current vertical exaggeration
     * @since 1.5
     */
    double getVerticalExaggeration();

    // not used (12th January 2007)
    final static String HIGH_PRIORITY = "gov.nasa.worldwind.DrawContext.HighPriority";
    final static String LOW_PRIORITY = "gov.nasa.worldwind.DrawContext.LowPriority";

    /**
     * Retrieves a list of all the sectors rendered so far this frame.
     *
     * @return a <code>SectorGeometryList</code> containing every <code>SectorGeometry</code> rendered so far this
     *         render pass.
     * @since 1.5
     */
    SectorGeometryList getSurfaceGeometry();

    //
//    /**
//     * Sets the average render time per frame in milliseconds.
//     *
//     * @param timeMillis the new average time in milliseconds
//     * @since 1.5
//     */
//    void setAverageRenderTimeMillis(double timeMillis);
//
//    /**
//     * Retrieves the current average render time for a frame. The average render time can be used to calculate the
//     * framerate.
//     *
//     * @return the current average render time for a frame
//     * @since 1.5
//     */
//    double getAverageRenderTimeMillis();

    /**
     * Returns the list of objects picked during the most recent pick traversal.
     *
     * @return the list of picked objects
     */
    PickedObjectList getPickedObjects();

    /**
     * Adds a collection of picked objects to the current picked-object list
     *
     * @param pickedObjects the objects to add
     */
    void addPickedObjects(PickedObjectList pickedObjects);

    /**
     * Adds a single insatnce of the picked object to the current picked-object list
     *
     * @param pickedObject the object to add
     */
    void addPickedObject(PickedObject pickedObject);

    /**
     * Returns a unique color to serve as a pick identifier during picking.
     *
     * @return a unique pick color
     */
    java.awt.Color getUniquePickColor();

    java.awt.Color getClearColor();

    /**
     * Enables color picking mode
     */
    void enablePickingMode();

    /**
     * Returns true if the Picking mode is active, otherwise return false
     *
     * @return true for Picking mode, otherwise false
     */
    boolean isPickingMode();

    /**
     * Disables color picking mode
     */
    void disablePickingMode();

    void addOrderedRenderable(OrderedRenderable orderedRenderable);

    java.util.Queue<OrderedRenderable> getOrderedRenderables();

    void drawUnitQuad();

    void drawUnitQuad(TextureCoords texCoords);

    int getNumTextureUnits();

    void setNumTextureUnits(int numTextureUnits);

    void setSurfaceGeometry(SectorGeometryList surfaceGeometry);

    Vec4 getPointOnGlobe(Angle latitude, Angle longitude);

    SurfaceTileRenderer getGeographicSurfaceTileRenderer();

    Point getPickPoint();

    void setPickPoint(Point pickPoint);

    TextureCache getTextureCache();

    void setTextureCache(TextureCache textureCache);

    Collection<PerformanceStatistic> getPerFrameStatistics();

    void setPerFrameStatisticsKeys(Set<String> statKeys, Collection<PerformanceStatistic> stats);

    void setPerFrameStatistic(String key, String displayName, Object statistic);

    void setPerFrameStatistics(Collection<PerformanceStatistic> stats);

    Set<String> getPerFrameStatisticsKeys();

    Point getViewportCenterScreenPoint();

    void setViewportCenterScreenPoint(Point viewportCenterPoint);

    Position getViewportCenterPosition();

    void setViewportCenterPosition(Position viewportCenterPosition);

    TextRendererCache getTextRendererCache();

    void setTextRendererCache(TextRendererCache textRendererCache);

    Vec4 getViewportCenterSurfacePoint();

    Vec4 getViewportCenterGlobePoint();

    AnnotationRenderer getAnnotationRenderer();

    void setAnnotationRenderer(AnnotationRenderer annotationRenderer);
}
