/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

/**
 * @author tag
 * @version $Id$
 */
public class AbsentResourceList
{
    // Absent resources: A resource is deemed absent if a specified maximum number of attempts have been made to retrieve it.
    // Retrieval attempts are governed by a minimum time interval between successive attempts. If an attempt is made
    // within this interval, the resource is still deemed to be absent until the interval expires.
    private static final int DEFAULT_MAX_ABSENT_RESOURCE_TRIES = 2;
    private static final int DEFAULT_MIN_ABSENT_RESOURCE_CHECK_INTERVAL = 10000;

    private int maxTries = DEFAULT_MAX_ABSENT_RESOURCE_TRIES;
    private int minCheckInterval = DEFAULT_MIN_ABSENT_RESOURCE_CHECK_INTERVAL;

    private static class AbsentResoureEntry
    {
        long timeOfLastMark; // meant to be the time of the most recent attempt to find the resource
        int numTries;
    }

    private final java.util.concurrent.ConcurrentHashMap<Long, AbsentResoureEntry> possiblyAbsent =
        new java.util.concurrent.ConcurrentHashMap<Long, AbsentResoureEntry>();

    private java.util.SortedSet<Long> definitelyAbsent = java.util.Collections.synchronizedSortedSet(
        new java.util.TreeSet<Long>());

    public AbsentResourceList()
    {
    }

    public AbsentResourceList(int maxTries, int minCheckInterval)
    {
        this.maxTries = Math.max(maxTries, 1);
        this.minCheckInterval = Math.max(minCheckInterval, 500);
    }

    public final void markResourceAbsent(long resourceID)
    {
        if (this.definitelyAbsent.contains(resourceID))
            return;

        AbsentResoureEntry entry = this.possiblyAbsent.get(resourceID);
        if (entry == null)
            this.possiblyAbsent.put(resourceID, entry = new AbsentResoureEntry());

        ++entry.numTries;
        entry.timeOfLastMark = System.currentTimeMillis();

        if (entry.numTries >= this.maxTries)
        {
            this.definitelyAbsent.add(resourceID);
            this.possiblyAbsent.remove(resourceID);
            // entry can now be garbage collected
        }
    }

    public final boolean isResourceAbsent(long resourceID)
    {
        if (this.definitelyAbsent.contains(resourceID))
            return true;

        AbsentResoureEntry entry = this.possiblyAbsent.get(resourceID);
        //noinspection SimplifiableIfStatement
        if (entry == null)
            return false;

        return (System.currentTimeMillis() - entry.timeOfLastMark) < this.minCheckInterval;
    }

    public final void unmarkResourceAbsent(long resourceID)
    {
        this.definitelyAbsent.remove(resourceID);
        this.possiblyAbsent.remove(resourceID);
    }
}
