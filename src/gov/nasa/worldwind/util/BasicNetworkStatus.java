/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.Configuration;

import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.*;
import java.io.IOException;

/**
 * Provides tracking of per-host network availability. Host computers that fail network requests can be logged to this
 * class' tracking list. When a host has been logged a specified number of times, it is marked as unreachable. Users can
 * query instances of this class to determine whether a host has been marked as unreachable.
 * <p/>
 * Users are expected to invoke this class' {@link #logUnavailableHost(java.net.URL)} method when an attempt to contact
 * a host fails. Each invocation increments the failure count by one. When the count exceeds the attempt limit, the host
 * is marked as unreachable. When attempts to contact the host <em>are</em> successful, users should invoke this class'
 * {@link #logAvailableHost(java.net.URL)} method to clear its status.
 * <p/>
 * A host may become reachable at a time subsequent to its being logged. To detect this, this class will mark a host as
 * not unreachable after a specifiable interval of time. If the host is once more logged as unavailable, its entry will
 * return to the unavailable state. This cycle continues indefinitely.
 * <p/>
 * Methods are also provided to determine whether the public network can be reached and whether the NASA World Wind
 * servers cab be reached.
 *
 * @author tag
 * @version $Id: BasicNetworkStatus.java 3735 2007-12-06 02:20:43Z tgaskins $
 */
public class BasicNetworkStatus extends AVListImpl implements NetworkStatus
{
    private static final long DEFAULT_TRY_AGAIN_INTERVAL = (long) 12e3; // seconds
    private static final int DEFAULT_ATTEMPT_LIMIT = 10; // number of unavailable events to declare host unavailable
    private static final String[] networkTestSites = new String[]
        {"www.nasa.gov", "worldwind.arc.nasa.gov", "google.com", "microsoft.com", "yahoo.com"};
    private static final long NETWORK_STATUS_REPORT_INTERVAL = (long) 60e3;

    private static class HostInfo
    {
        private final long tryAgainInterval;
        private final int attemptLimit;
        private AtomicInteger logCount = new AtomicInteger();
        private AtomicLong lastLogTime = new AtomicLong();

        private HostInfo(int attemptLimit, long tryAgainInterval)
        {
            this.lastLogTime.set(System.currentTimeMillis());
            this.logCount.set(1);
            this.tryAgainInterval = tryAgainInterval;
            this.attemptLimit = attemptLimit;
        }

        private boolean isUnavailable()
        {
            return this.logCount.get() >= this.attemptLimit;
        }

        private boolean isTimeToTryAgain()
        {
            return System.currentTimeMillis() - this.lastLogTime.get() >= this.tryAgainInterval;
        }
    }

    private ConcurrentHashMap<String, HostInfo> hostMap = new ConcurrentHashMap<String, HostInfo>();

    private AtomicLong tryAgainInterval = new AtomicLong(DEFAULT_TRY_AGAIN_INTERVAL);
    private AtomicInteger attemptLimit = new AtomicInteger(DEFAULT_ATTEMPT_LIMIT);

    // Fields for determining overall network status.
    private boolean offlineMode;
    private AtomicLong lastUnavailableLogTime = new AtomicLong(System.currentTimeMillis());
    private AtomicLong lastAvailableLogTime = new AtomicLong(System.currentTimeMillis() + 1);
    private AtomicLong lastNetworkCheckTime = new AtomicLong(System.currentTimeMillis());
    private AtomicLong lastNetworkStatusReportTime = new AtomicLong(0);
    private AtomicBoolean lastNetworkUnavailableResult = new AtomicBoolean(false);

    public BasicNetworkStatus()
    {
        String oms = Configuration.getStringValue(AVKey.OFFLINE_MODE, "false");
        this.offlineMode = oms.startsWith("t") || oms.startsWith("T");
    }

    /**
     * Indicates whether World Wind will attempt to connect to the network to retrieve data or for other reasons.
     *
     * @return <code>true</code> if World Wind is in off-line mode, <code>false</code> if not.
     */
    public boolean isOfflineMode()
    {
        return offlineMode;
    }

    /**
     * Indicate whether World Wind should attempt to connect to the network to retrieve data or for other reasons.
     * The default value for this attribute is <code>false</code>, indicating that the network should be used.
     *
     * @param offlineMode <code>true</code> if World Wind should use the network, <code>false</code> otherwise
     */
    public void setOfflineMode(boolean offlineMode)
    {
        this.offlineMode = offlineMode;
    }

    /**
     * Set the number of times a host must be logged as unavailable before it is marked unavailable in this class.
     *
     * @param limit the number of log-unavailability invocations necessary to consider the host unreachable.
     * @throws IllegalArgumentException if the limit is less than 1.
     */
    public void setAttemptLimit(int limit)
    {
        if (limit < 1)
        {
            String message = Logging.getMessage("NetworkStatus.InvalidAttemptLimit");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.attemptLimit.set(limit);
    }

    /**
     * Set the length of time to wait until a host is marked as not unreachable subsequent to its being marked
     * unreachable.
     *
     * @param interval The length of time, in milliseconds, to wait to unmark a host as unreachable.
     * @throws IllegalArgumentException if the interval is less than 0.
     */
    public void setTryAgainInterval(long interval)
    {
        if (interval < 0)
        {
            String message = Logging.getMessage("NetworkStatus.InvalidTryAgainInterval");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.tryAgainInterval.set(interval);
    }

    /**
     * Returns the number of times a host must be logged as unavailable before it is marked unavailable in this class.
     *
     * @return the limit.
     */
    public int getAttemptLimit()
    {
        return this.attemptLimit.get();
    }

    /**
     * Returns the length of time to wait until a host is marked as not unreachable subsequent to its being marked
     * unreachable.
     *
     * @return the interval, in milliseconds.
     */
    public long getTryAgainInterval()
    {
        return this.tryAgainInterval.get();
    }

    /**
     * Log a host as unavailable. Each invocation increments the host's attempt count. When the count equals or exceeds
     * the attempt limit, the host is marked as unavailable.
     *
     * @param url a url containing the host to mark as unavailable.
     */
    public void logUnavailableHost(URL url)
    {
        if (this.offlineMode)
            return;

        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String hostName = url.getHost();
        if (this.hostMap.containsKey(hostName))
        {
            HostInfo hi = this.hostMap.get(hostName);
            if (!hi.isUnavailable())
                hi.logCount.incrementAndGet();
            hi.lastLogTime.set(System.currentTimeMillis());
        }
        else
        {
            HostInfo hi = new HostInfo(this.attemptLimit.get(), this.tryAgainInterval.get());
            this.hostMap.put(hostName, hi);
        }

        this.lastUnavailableLogTime.set(System.currentTimeMillis());
    }

    /**
     * Log a host as available. Each invocation causes the host to no longer be marked as unavailable. Its
     * unavailability count is effectively set to 0.
     *
     * @param url a url containing the host to mark as available.
     */
    public void logAvailableHost(URL url)
    {
        if (this.offlineMode)
            return;

        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String hostName = url.getHost();
        if (this.hostMap.containsKey(hostName))
            this.hostMap.remove(hostName);

        this.lastAvailableLogTime.set(System.currentTimeMillis());
    }

    /**
     * Indicates whether the host has been marked as unavailable. To be marked unavailable a host's attempt count must
     * exceed the specified attempt limit.
     *
     * @param url a url containing the host to check for availability.
     * @return true if the host is marked as unavailable, otherwise false.
     */
    public boolean isHostUnavailable(URL url)
    {
        if (this.offlineMode)
            return true;

        if (url == null)
        {
            String message = Logging.getMessage("nullValue.URLIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String hostName = url.getHost();
        if (!this.hostMap.containsKey(hostName))
            return false;

        HostInfo hi = this.hostMap.get(hostName);
        if (hi.isTimeToTryAgain())
        {
            this.removeKey(hostName);
            return false;
        }

        return hi.isUnavailable();
    }

    /**
     * Indicates whether a public network can be reached or has been reached in the previous five seconds.
     *
     * @return false if the network can be reached or has been reached in the previous five seconds, otherwise true.
     */
    public boolean isNetworkUnavailable()
    {
        return this.offlineMode || this.isNetworkUnavailable(5000L);
    }

    /**
     * Indicates whether a public network can be reached or has been reached in a specified previous amount of time.
     *
     * @param checkInterval the number of milliseconds in the past used to determine whether the server was avaialble
     *                      recently.
     * @return false if the network can be reached or has been reached in a specified time, otherwise true.
     */
    public boolean isNetworkUnavailable(long checkInterval)
    {
        if (this.offlineMode)
            return true;

        // If there's been success since failure, network assumed to be reachable.
        if (this.lastAvailableLogTime.get() > this.lastUnavailableLogTime.get())
        {
            this.lastNetworkUnavailableResult.set(false);
            return this.lastNetworkUnavailableResult.get();
        }

        long now = System.currentTimeMillis();

        // If there's been success recently, network assumed to be reachable.
        if (!this.lastNetworkUnavailableResult.get() && now - this.lastAvailableLogTime.get() < checkInterval)
        {
            return this.lastNetworkUnavailableResult.get();
        }

        // If query comes too soon after an earlier one that addressed the network, return the earlier result.
        if (now - this.lastNetworkCheckTime.get() < checkInterval)
        {
            return this.lastNetworkUnavailableResult.get();
        }

        this.lastNetworkCheckTime.set(now);

        if (!this.isWorlWindServerUnavailable())
        {
            this.lastNetworkUnavailableResult.set(false); // network not unreachable
            return this.lastNetworkUnavailableResult.get();
        }

        for (String testHost : networkTestSites)
        {
            if (this.isHostReachable(testHost))
            {
                {
                    this.lastNetworkUnavailableResult.set(false); // network not unreachable
                    return this.lastNetworkUnavailableResult.get();
                }
            }
        }

        if (now - this.lastNetworkStatusReportTime.get() > NETWORK_STATUS_REPORT_INTERVAL)
        {
            this.lastNetworkStatusReportTime.set(now);
            String message = Logging.getMessage("NetworkStatus.NetworkUnreachable");
            Logging.logger().info(message);
        }

        this.lastNetworkUnavailableResult.set(true); // if no successful contact then network is unreachable
        return this.lastNetworkUnavailableResult.get();
    }

    /**
     * Indicates whether the NASA World Wind servers can be reached.
     *
     * @return false if the servers can be reached, otherwise true.
     */
    public boolean isWorlWindServerUnavailable()
    {
        return this.offlineMode || !this.isHostReachable("worldwind.arc.nasa.gov");
    }

    private boolean isHostReachable(String hostName)
    {
        try
        {
            // Assume host is unreachable if we can't get its dns entry without getting an exception
            //noinspection ResultOfMethodCallIgnored
            InetAddress.getByName(hostName);
        }
        catch (UnknownHostException e)
        {
            String message = Logging.getMessage("NetworkStatus.UnreachableTestHost", hostName);
            Logging.logger().fine(message);
            return false;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("NetworkStatus.ExceptionTestingHost", hostName);
            Logging.logger().info(message);
            return false;
        }

        // Was able to get internet address, but host still might not be reachable because the address might have been
        // cached earlier when it was available. So need to try something else.

        URLConnection connection = null;
        try
        {
            URL url = new URL("http://" + hostName);
            Proxy proxy = WWIO.configureProxy();
            if (proxy != null)
                connection = url.openConnection(proxy);
            else
                connection = url.openConnection();

            connection.setConnectTimeout(2000);
            String ct = connection.getContentType();
            if (ct != null)
                return true;
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("NetworkStatus.ExceptionTestingHost", hostName);
            Logging.logger().info(message);
        }
        finally
        {
            if (connection != null && connection instanceof HttpURLConnection)
                ((HttpURLConnection) connection).disconnect();
        }

        return false;
    }
//
//    public static void main(String[] args)
//    {
//        try
//        {
//            NetworkStatus ns = new BasicNetworkStatus();
//            boolean tf = ns.isWorlWindServerUnavailable();
//            tf = ns.isNetworkUnavailable();
//        }
//        catch (Exception e)
//        {
//            e.printStackTrace();
//        }
//    }
}
