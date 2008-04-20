/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.util.PerformanceStatistic;

import java.util.Collection;

/**
 * @author tag
 * @version $Id: MemoryCacheSet.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public interface MemoryCacheSet
{
    boolean containsCache(String key);

    MemoryCache getCache(String cacheKey);

    MemoryCache addCache(String key, MemoryCache cache);

    Collection<PerformanceStatistic> getPerformanceStatistics();

    void clear();
}
