/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.View;

/**
 * <code>OrbitView</code> provides an interface for communicating viewing state in polar coordinates. For example
 * calling {@link #getLatitude} and {@link #setLatitude} will get and set the View latitude coordinate.
 *
 * @author dcollins
 * @version $Id: OrbitView.java 3461 2007-11-08 19:41:10Z dcollins $
 */
public interface OrbitView extends View
{
    /**
     * Gets the <code>View</code> eye latitude position.
     *
     * @return the current eye latitude positon.
     */
    Angle getLatitude();

    /**
     * Sets the <code>View</code> eye point to the new latitude coordinate.
     *
     * @param newLatitude the new latitude position.
     * @return true when the set is successful; false otherwise.
     * @throws IllegalArgumentException if <code>newLatitude</code> is null.
     */
    boolean setLatitude(Angle newLatitude);

    /**
     * Gets the <code>View</code> eye longitude position.
     *
     * @return the current eye longitude positon.
     */
    Angle getLongitude();

    /**
     * Sets the <code>View</code> eye point to the new longitude coordinate.
     *
     * @param newLongitude the new longitude position.
     * @return true when the set is successful; false otherwise.
     * @throws IllegalArgumentException if <code>newLongitude</code> is null.
     */
    boolean setLongitude(Angle newLongitude);

    /**
     * Gets the <code>View</code> eye altitude above the analytical globe radius.
     *
     * @return the <code>View's</code> altitude above the globe radius.
     */
    double getAltitude();

    /**
     * Sets the <code>View</code> eye point to the new altitude above the analytical globe radius.
     *
     * @param newAltitude the new eye altitude above the globe radius.
     * @return true when the set is successful; false otherwise.
     */
    boolean setAltitude(double newAltitude);

    /**
     * Sets the <code>View</code> eye point to the new (latitude, longitude) coordinate. This has the effect of calling
     * {@link #setLatitude} and {@link #setLongitude}.
     *
     * @param newLatLon the latitude and longitude coordinate of the eye point.
     * @return true when the set is successful; false otherwise.
     */
    boolean setLatLon(LatLon newLatLon);

    /**
     * Sets the <code>View</code> eye point to the new (latitude, longitude, elevation) coordinate. This has the effect
     * of calling {@link #setLatitude}, {@link #setLongitude} and {@link #setAltitude}.
     *
     * @param newPosition the latitude, longitude and elevation coordinate of the eye point.
     * @return true when the set is successful; false otherwise.
     */
    boolean setLatLonAltitude(Position newPosition);

    /**
     * Gets the <code>View's</code> angle from true North.
     *
     * @return the angle from true North.
     */
    Angle getHeading();

    /**
     * Sets the <code>View's</code> angle to true North.
     *
     * @param newHeading the new angle to true North.
     * @return true when the set is successful; false otherwise.
     * @throws IllegalArgumentException if <code>newHeading</code> is null.
     */
    boolean setHeading(Angle newHeading);

    /**
     * Gets the <code>View's</code> angle from the plane tangent to the surface.
     *
     * @return the angle from the surface tangent plane.
     */
    Angle getPitch();

    /**
     * Sets the <code>View's</code> angle to the plane tangent to the surface.
     *
     * @param newPitch the new angle to the surface tangent plane.
     * @return true when the set is successful; false otherwise.
     * @throws IllegalArgumentException if <code>newPitch</code> is null.
     */
    boolean setPitch(Angle newPitch);

    /**
     * Gets the <code>View's</code> translation in its forward direction.
     *
     * @return translation along the forward direction.
     */
    double getZoom();

    /**
     * Sets the <code>View's</code> translation in its forward direction.
     *
     * @param newZoom translation along the forward direction.
     * @return true when the set is successful; false otherwise.
     */
    boolean setZoom(double newZoom);

    /**
     * Gets the latitude at the center of the screen.
     *
     * @return the current center latitude position.
     */
    Angle getLookAtLatitude();

    /**
     * Gets the longitude at the center of the screen.
     *
     * @return the current center longitude position.
     */
    Angle getLookAtLongitude();

    /**
     * Sets the latitude coordinate of the center of the screen.
     *
     * @param newLatitude the new latitude screen center.
     * @return true when the set is successful; false otherwise.
     * @throws IllegalArgumentException if <code>newLatitude</code> is null.
     */
    boolean setLookAtLatitude(Angle newLatitude);

    /**
     * Sets the longitude coordinate of the center of the screen.
     *
     * @param newLongitude the new longitude screen center.
     * @return true when the set is successful; false otherwise.
     * @throws IllegalArgumentException if <code>newLongitude</code> is null.
     */
    boolean setLookAtLongitude(Angle newLongitude);

    /**
     * Sets coordinate at the center of the screen. This has the effect of calling
     * {@link #setLookAtLatitude} and {@link #setLookAtLatitude}.
     *
     * @param newLatLon the latitude and longitude coordinate of the center of the screen.
     * @return true when the set is successful; false otherwise.
     */
    boolean setLookAtLatLon(LatLon newLatLon);

    /**
     * Gets the <code>View</code> eye rotation about the <code>Globe</code> origin as a <code>Quaternion</code>.
     *
     * @return the eye rotation about the <code>Globe</code> origin.
     */
    Quaternion getRotation();

    /**
     * Sets the <code>View</code> eye rotation about the <code>Globe</code> origin.
     *
     * @param newRotation the rotation to set.
     * @return true when the set is successful; false otherwise.
     * @throws IllegalArgumentException if <code>newRotation</code> is null.
     */
    boolean setRotation(Quaternion newRotation);

    /**
     * Returns a <code>Quaternion</code> rotation that will rotate the <code>OrbitView</code> to move
     * <code>beginPosition</code> to <code>endPosition</code>. The <code>Quaternion</code> is suitable for use as
     * the parameter to {@link #setRotation}.
     *
     * @param beginPosition the virtual position that will be moved.
     * @param endPosition   the virtual position to move to.
     * @return a <code>Quaternion</code> transforming <code>beginPosition</code> to <code>endPosition</code>.
     * @throws IllegalArgumentException if <code>beginPosition</code> or <code>endPosition</code> are null.
     */
    Quaternion createRotationBetweenPositions(Position beginPosition, Position endPosition);

    /**
     * Returns a <code>Quaternion</code> rotation that will rotate the <code>OrbitView</code> in the viewer's
     * forward direction.
     *
     * @param amount the amount to move.
     * @return a <code>Quaternion</code> transforming <code>amount</code> degrees along the forward direction.
     * @throws IllegalArgumentException if <code>amount</code> is null.
     */
    Quaternion createRotationForward(Angle amount);

    /**
     * Returns a <code>Quaternion</code> rotation that will rotate the <code>OrbitView</code> in the viewer's
     * right direction.
     *
     * @param amount the amount to move.
     * @return a <code>Quaternion</code> transforming <code>amount</code> degrees along the right direction.
     * @throws IllegalArgumentException if <code>amount</code> is null.
     */
    Quaternion createRotationRight(Angle amount);
}
