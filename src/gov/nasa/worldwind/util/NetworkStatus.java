/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.AVList;

import java.net.URL;

/**
 * @author tag
 * @version $Id: NetworkStatus.java 3735 2007-12-06 02:20:43Z tgaskins $
 */
public interface NetworkStatus extends AVList
{
    void logUnavailableHost(URL url);

    void logAvailableHost(URL url);

    boolean isHostUnavailable(URL url);

    boolean isNetworkUnavailable();

    boolean isWorlWindServerUnavailable();

    int getAttemptLimit();

    long getTryAgainInterval();

    /**
     * Indicates whether World Wind will attempt to connect to the network to retrieve data or for other reasons.
     *
     * @return <code>true</code> if World Wind is in off-line mode, <code>false</code> if not.
     */
    boolean isOfflineMode();

    /**
     * Indicate whether World Wind should attempt to connect to the network to retrieve data or for other reasons.
     * The default value for this attribute is <code>false</code>, indicating that the network should be used.
     *
     * @param offlineMode <code>true</code> if World Wind should use the network, <code>false</code> otherwise
     */
    void setOfflineMode(boolean offlineMode);

    /**
     * Set the number of times a host must be logged as unavailable before it is marked unavailable in this class.
     *
     * @param limit the number of log-unavailability invocations necessary to consider the host unreachable.
     * @throws IllegalArgumentException if the limit is less than 1.
     */
    void setAttemptLimit(int limit);

    /**
     * Set the length of time to wait until a host is marked as not unreachable subsequent to its being marked
     * unreachable.
     *
     * @param interval The length of time, in milliseconds, to wait to unmark a host as unreachable.
     * @throws IllegalArgumentException if the interval is less than 0.
     */
    void setTryAgainInterval(long interval);
}
