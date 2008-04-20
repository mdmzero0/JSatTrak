/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.WWObject;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.render.DrawContext;

/**
 * <p/>
 * Provides the elevations of all points on a {@link Globe} . Every <code>Globe</code> has an elevation model
 * implementing this interface.
 * <p/>
 * Elevations are organized by {@link Sector}. Elevation models return an {@link Elevations} object for requested
 * sectors. This object is then used to query elevations at latitude/longitude positions within that sector.
 * <p/>
 * An <code>ElevationModel</code> typically approximates elevations at multiple levels of spatial resolution. For any
 * given viewing position the model determines an appropriate target resolution. That target resolution may not be
 * immediately achievable, however, because the corresponding elevation data might not be locally available and must be
 * retrieved from a remote location. When this is the case, the <code>Elevations</code> object returned for a sector
 * holds the resolution achievable with the data currently available. That resolution may not be the same as the target
 * resolution. The target resolution and the actual resolution are made available in the interface so that users of this
 * class may use the resolution values to compare previously computed elevation sectors with newly computed ones, and
 * thereby enable effective caching of elevations computed for the sector.
 * <p/>
 *
 * @author Tom Gaskins
 * @version $Id: ElevationModel.java 3404 2007-10-28 07:58:29Z tgaskins $
 */
public interface ElevationModel extends WWObject
{
    boolean isEnabled();

    void setEnabled(boolean enable);

    /**
     * Returns the maximum elevation contained in the elevevation model. This value is the height of the highest point
     * on the globe.
     *
     * @return The maximum elevation of the model
     */
    double getMaxElevation();

    /**
     * Returns the minimum elevation contained in the elevevation model. This value is the height of the lowest point on
     * the globe. It may be negative, indicating a value below mean surface level. (Sea level in the case of Earth.)
     *
     * @return The minimum elevation of the model
     */
    double getMinElevation();

    /**
     * Computes and returns an {@link Elevations} object for the specified {@link Sector} and target resolution. If the
     * target resolution can not currently be achieved, the best available elevations are returned.
     * <p/>
     * Implementing classes of <code>ElevationModel</code> interpret <code>resolution</code> in a class-specific way.
     * See the descriptions of those classes to learn their use of this value. The elevations returned are in the form
     * of an {@link Elevations} object. Specific elevations are returned by that object.
     *
     * @param sector     the sector to return elevations for.
     * @param resolution a value interpreted in a class-specific way by implementing classes.
     * @return An object representing the elevations for the specified sector. Its resolution value will be either the
     *         specified resolution or the best available alternative.
     */
    Elevations getElevations(Sector sector, int resolution);

    /**
     * Returns the resolution appropriate to the given {@link Sector} and view parameters. The view parameters are read
     * from the specified {@link gov.nasa.worldwind.render.DrawContext}. Implementing classes of <code>ElevationModel</code> interpret
     * <code>resolution</code> in class-specific ways. See the descriptions of subclasses to learn their use of this
     * value. This method is used to determine the resolution the model will use if all resources are available to
     * compute that resolution. It is subsequently passed to {@link #getElevations(Sector, int)} when a sector's
     * resolutions are queried.
     *
     * @param dc     the draw context to read the view and rendering parameters from.
     * @param sector the {@link Sector} to compute the target resolution for.
     * @return The appropriate resolution for the sector and draw context values.
     */
    int getTargetResolution(DrawContext dc, Sector sector, int density);

    double getElevation(Angle latitude, Angle longitude);

    Elevations getBestElevations(Sector sector);

    Elevations getElevationsAtResolution(Sector sector, int resolution);

    double[] getMinAndMaxElevations(Sector sector);

    double getMinElevation(Sector sector);

    double getMaxElevation(Sector sector);

    int getTileCountAtResolution(Sector sector, int resolution);

    Double getBestElevation(Angle latitude, Angle longitude);

    Double getElevationAtResolution(Angle latitude, Angle longitude, int resolution);

    int getTargetResolution(Globe globe, double size);

    /**
     * The <code>Elevations</code> interface provides elevations at specified latitude and longitude positions. Objects
     * implementing this interface are created by {@link ElevationModel#getElevations(Sector, int)}.
     */
    public interface Elevations
    {
        /**
         * Indicates whether the object contains useful elevations. An <code>Elevations</code> instance may exist
         * without holding any elevations. This can occur when the resources needed to determine elevations are not yet
         * local. This method enables the detection of that case. Callers typically use it to avoid time-consuming
         * computations that require valid elevations.
         *
         * @return <code>true</code> if a call to {@link #getElevation(double, double)} will return valid elevations,
         *         otherwise <code>false</code> indicating that the value 0 will always be returned from that method.
         */
        boolean hasElevations();

        /**
         * Returns the elevation at a specific latitude and longitude, each specified in radians.
         *
         * @param latRadians the position's latitude in radians, in the range [-&pi;/2, +&pi;/2].
         * @param lonRadians the position's longitude in radians, in the range [-&pi;, +&pi;].
         * @return The elevation at the given position, or 0 if elevations are not available.
         */
        double getElevation(double latRadians, double lonRadians);

        /**
         * Returns the resolution value of the elevations. The meaning and use of this value is defined by subclasses of
         * <code>ElevationModel</code>.
         *
         * @return the resolution associated with <code>this</code>.
         */
        int getResolution();

        /**
         * Returns the {@link Sector} the elevations pertain to.
         *
         * @return The sector the elevations pertain to.
         */
        Sector getSector();

        short[] getExtremes();
    }
}
