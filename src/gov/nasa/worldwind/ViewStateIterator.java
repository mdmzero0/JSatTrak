/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

/**
 * An iteration over <code>View</code> state changes.
 *
 * @author dcollins
 * @version $Id: ViewStateIterator.java 3557 2007-11-17 04:10:32Z dcollins $
 * @see View
 */
public interface ViewStateIterator
{
    /**
     * If possible, merges this <code>ViewStateIterator</code> with <code>stateIterator</code> and returns the result.
     *
     * @param view          the <code>View</code> context.
     * @param stateIterator the <code>ViewStateIterator</code> to merge with.
     * @return the merged <code>ViewStateIterator</code>.
     * @throws IllegalArgumentException if <code>view</code> or <code>stateIterator</code> are null.
     */
    ViewStateIterator coalesceWith(View view, ViewStateIterator stateIterator);

    /**
     * Returns true if <code>ViewStateIterator</code> has more state changes.
     *
     * @param view the <code>View</code> context.
     * @return true if <code>ViewStateIterator</code> has more state changes; false otherwise.
     */
    boolean hasNextState(View view);

    /**
     * Applies the the next viewing state change to <code>view</code>.
     *
     * @param view the <code>View</code> context.
     */
    void nextState(View view);
}
