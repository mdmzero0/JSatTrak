/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.util.Logging;

import java.util.Date;

/**
 * @author dcollins
 * @version $Id: ScheduledOrbitViewInterpolator.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class ScheduledOrbitViewInterpolator implements OrbitViewInterpolator
{
    private long startTime = -1;
    private final long length;

    public ScheduledOrbitViewInterpolator(long lengthMillis)
    {
        this(null, lengthMillis);
    }

    public ScheduledOrbitViewInterpolator(Date startTime, long lengthMillis)
    {
        if (lengthMillis < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", lengthMillis);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (startTime != null)
            this.startTime = startTime.getTime();
        this.length = lengthMillis;
    }

    public ScheduledOrbitViewInterpolator(Date startTime, Date stopTime)
    {
        if (startTime == null || stopTime == null)
        {
            String message = Logging.getMessage("nullValue.DateIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (startTime.after(stopTime))
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", startTime);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.startTime = startTime.getTime();
        this.length = stopTime.getTime() - startTime.getTime();
    }

    public final double nextInterpolant(OrbitView orbitView)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        long currentTime = System.currentTimeMillis();
        // When no start time is specified, begin counting time on the first run.
        if (this.startTime < 0)
            this.startTime = currentTime;
        // Exit when current time is before starting time.
        if (currentTime < this.startTime)
            return 0;

        long elapsedTime = currentTime - this.startTime;
        double unclampedInterpolant = ((double) elapsedTime) / ((double) this.length);
        return clampDouble(unclampedInterpolant, 0, 1);
    }

    public final OrbitViewInterpolator coalesceWith(OrbitView orbitView, OrbitViewInterpolator interpolator)
    {
        if (orbitView == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (interpolator == null)
        {
            String message = Logging.getMessage("nullValue.OrbitViewInterpolatorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this;
    }

    private static double clampDouble(double value, double min, double max)
    {
        return value < min ? min : (value > max ? max : value);
    }
}
