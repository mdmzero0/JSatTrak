/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.retrieve.RetrievalService;
import gov.nasa.worldwind.util.*;

import java.util.logging.Level;
import java.beans.PropertyChangeListener;

/**
 * @author Tom Gaskins
 * @version $Id: WorldWind.java 3735 2007-12-06 02:20:43Z tgaskins $
 */
public final class WorldWind
{
    public static final String SHUTDOWN_EVENT = "gov.nasa.worldwind.ShutDown";

    private static WorldWind instance = new WorldWind();

    private WWObjectImpl wwo;
    private MemoryCacheSet memoryCacheSet;
    private FileCache dataFileCache;
    private RetrievalService retrievalService;
    private TaskService taskService;
    private NetworkStatus networkStatus;

    private WorldWind() // Singleton, prevent public instantiation.
    {
        this.initialize();
    }

    private void initialize()
    {
        this.wwo = new WWObjectImpl();
        this.retrievalService = (RetrievalService) createConfigurationComponent(AVKey.RETRIEVAL_SERVICE_CLASS_NAME);
        this.taskService = (TaskService) createConfigurationComponent(AVKey.TASK_SERVICE_CLASS_NAME);
        this.dataFileCache = (FileCache) createConfigurationComponent(AVKey.DATA_FILE_CACHE_CLASS_NAME);
        this.memoryCacheSet = (MemoryCacheSet) createConfigurationComponent(AVKey.MEMORY_CACHE_SET_CLASS_NAME);
        this.networkStatus = (NetworkStatus) createConfigurationComponent(AVKey.NETWORK_STATUS_CLASS_NAME);
    }

    private void dispose()
    {
        if (this.taskService != null)
            this.taskService.shutdown(true);
        if (this.retrievalService != null)
            this.retrievalService.shutdown(true);
        if (this.memoryCacheSet != null)
            this.memoryCacheSet.clear();
    }

    /**
     * Reinitialize World Wind to its initial ready state. Shut down and restart all World Wind services and clear all
     * World Wind memory caches. Cache memory will be released at the next JVM garbage collection.
     * <p/>
     * Call this method to reduce World Wind's current resource usage to its initial, empty state. This is typically
     * required by applets when the user leaves the applet page.
     * <p/>
     * World Wind can continue to be used after calling this method. The state of any existing World Wind drawables is
     * subsequently indeterminate and they should be disposed.
     */
    public static synchronized void shutDown()
    {
        instance.wwo.firePropertyChange("gov.nasa.worldwind.ShutDown", null, -1);
        instance.dispose();
        instance = new WorldWind();
        instance.initialize();
    }

    public static MemoryCacheSet getMemoryCacheSet()
    {
        return instance.memoryCacheSet;
    }

    public static synchronized MemoryCache getMemoryCache(String key)
    {
        return instance.memoryCacheSet.getCache(key);
    }

    public static FileCache getDataFileCache()
    {
        return instance.dataFileCache;
    }

    public static RetrievalService getRetrievalService()
    {
        return instance.retrievalService;
    }

    public static TaskService getTaskService()
    {
        return instance.taskService;
    }

    public static NetworkStatus getNetworkStatus()
    {
        return instance.networkStatus;
    }

    /**
     * Indicates whether World Wind will attempt to connect to the network to retrieve data or for other reasons.
     *
     * @return <code>true</code> if World Wind is in off-line mode, <code>false</code> if not.
     * @see NetworkStatus
     */
    public boolean isOfflineMode()
    {
        return getNetworkStatus().isOfflineMode();
    }

    /**
     * Indicate whether World Wind should attempt to connect to the network to retrieve data or for other reasons. The
     * default value for this attribute is <code>false</code>, indicating that the network should be used.
     *
     * @param offlineMode <code>true</code> if World Wind should use the network, <code>false</code> otherwise
     * @see NetworkStatus
     */
    public void setOfflineMode(boolean offlineMode)
    {
        getNetworkStatus().setOfflineMode(offlineMode);
    }

    /**
     * @param className the full name, including package names, of the component to create
     * @return the new component
     * @throws WWRuntimeException       if the <code>Object</code> could not be created
     * @throws IllegalArgumentException if <code>className</code> is null or zero length
     */
    public static Object createComponent(String className) throws WWRuntimeException
    {
        if (className == null || className.length() == 0)
        {
            Logging.logger().severe("WorldWind.ClassNameKeyNulZero");
            throw new IllegalArgumentException(Logging.getMessage("WorldWind.ClassNameKeyNulZero"));
        }

        try
        {
            Class c = Class.forName(className);
            return c.newInstance();
        }
        catch (Exception e)
        {
            Logging.logger().log(Level.SEVERE, "WorldWind.ExceptionCreatingComponent", className);
            throw new WWRuntimeException(Logging.getMessage("WorldWind.ExceptionCreatingComponent", className), e);
        }
        catch (Throwable t)
        {
            Logging.logger().log(Level.SEVERE, "WorldWind.ErrorCreatingComponent", className);
            throw new WWRuntimeException(Logging.getMessage("WorldWind.ErrorCreatingComponent", className), t);
        }
    }

    /**
     * @param classNameKey the key identifying the component
     * @return the new component
     * @throws IllegalStateException    if no name could be found which corresponds to <code>classNameKey</code>
     * @throws IllegalArgumentException if <code>classNameKey<code> is null
     * @throws WWRuntimeException       if the component could not be created
     */
    public static Object createConfigurationComponent(String classNameKey)
        throws IllegalStateException, IllegalArgumentException
    {
        if (classNameKey == null)
        {
            Logging.logger().severe("WorldWind.ClassNameKeyNulZero");
            throw new IllegalArgumentException(Logging.getMessage("WorldWind.ClassNameKeyNulZero"));
        }

        String name = Configuration.getStringValue(classNameKey);
        if (name == null)
        {
            Logging.logger().log(Level.SEVERE, "WorldWind.NoClassNameInConfigurationForKey", classNameKey);
            throw new WWRuntimeException(
                Logging.getMessage("WorldWind.NoClassNameInConfigurationForKey", classNameKey));
        }

        try
        {
            return WorldWind.createComponent(name);
        }
        catch (Throwable e)
        {
            Logging.logger().log(Level.SEVERE, "WorldWind.UnableToCreateClassForConfigurationKey", name);
            throw new IllegalStateException(
                Logging.getMessage("WorldWind.UnableToCreateClassForConfigurationKey", name), e);
        }
    }

    public static void setValue(String key, String value)
    {
        instance.wwo.setValue(key, value);
    }

    public static Object getValue(String key)
    {
        return instance.wwo.getValue(key);
    }

    public static String getStringValue(String key)
    {
        return instance.wwo.getStringValue(key);
    }

    public static boolean hasKey(String key)
    {
        return instance.wwo.hasKey(key);
    }

    public static void removeKey(String key)
    {
        instance.wwo.removeKey(key);
    }

    public static void addPropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        instance.wwo.addPropertyChangeListener(propertyName, listener);
    }

    public static void removePropertyChangeListener(String propertyName, PropertyChangeListener listener)
    {
        instance.wwo.removePropertyChangeListener(propertyName, listener);
    }

    public static void addPropertyChangeListener(PropertyChangeListener listener)
    {
        instance.wwo.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(PropertyChangeListener listener)
    {
        instance.wwo.removePropertyChangeListener(listener);
    }
}
