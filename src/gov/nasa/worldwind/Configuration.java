/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
/**
 *
 @version $Id: Configuration.java 3632 2007-11-28 03:28:17Z tgaskins $
 @author Tom Gaskins
 */
package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.cache.*;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.retrieve.BasicRetrievalService;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.view.BasicOrbitView;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;

public class Configuration // Singleton
{
    private static final String DEFAULT_LOGGER_NAME = "gov.nasa.worldwind";
    private static final String CONFIG_FILE_NAME = "config/worldwind.properties";
    private static final String CONFIG_FILE_PROPERTY_KEY = "gov.nasa.worldwind.config.file";

    private static Configuration ourInstance = new Configuration();

    private static Configuration getInstance()
    {
        return ourInstance;
    }

    private final Properties properties;

    private Configuration()
    {
        this.properties = new Properties(initializeDefaults());
        this.initializeCustom();
    }

    private Properties initializeDefaults()
    {
        Properties defaults = new Properties();
        defaults.setProperty(AVKey.LOGGER_NAME, "gov.nasa.worldwind");
        defaults.setProperty(AVKey.DATA_FILE_CACHE_CONFIGURATION_FILE_NAME, "config/DataFileCache.xml");
        defaults.setProperty(AVKey.DATA_FILE_CACHE_CLASS_NAME, BasicDataFileCache.class.getName());
        defaults.setProperty(AVKey.GLOBE_CLASS_NAME, Earth.class.getName());
        defaults.setProperty(AVKey.TESSELLATOR_CLASS_NAME, GlobeRectangularTessellator.class.getName());
        defaults.setProperty(AVKey.INPUT_HANDLER_CLASS_NAME, AWTInputHandler.class.getName());
        defaults.setProperty(AVKey.MEMORY_CACHE_SET_CLASS_NAME, BasicMemoryCacheSet.class.getName());
        defaults.setProperty(AVKey.WORLD_WINDOW_CLASS_NAME, WorldWindowGLAutoDrawable.class.getName());
        defaults.setProperty(AVKey.MODEL_CLASS_NAME, BasicModel.class.getName());
        defaults.setProperty(AVKey.RETRIEVAL_SERVICE_CLASS_NAME, BasicRetrievalService.class.getName());
        defaults.setProperty(AVKey.SCENE_CONTROLLER_CLASS_NAME, BasicSceneController.class.getName());
        defaults.setProperty(AVKey.TASK_SERVICE_CLASS_NAME, ThreadedTaskService.class.getName());
        defaults.setProperty(AVKey.VIEW_CLASS_NAME, BasicOrbitView.class.getName());
        defaults.setProperty(AVKey.NETWORK_STATUS_CLASS_NAME, BasicNetworkStatus.class.getName());
        defaults.setProperty(AVKey.LAYERS_CLASS_NAMES,
            gov.nasa.worldwind.layers.Earth.StarsLayer.class.getName()
                + "," + gov.nasa.worldwind.layers.Earth.BMNGSurfaceLayer.class.getName()
                + "," + gov.nasa.worldwind.layers.Earth.LandsatI3.class.getName()
                + "," + gov.nasa.worldwind.layers.Earth.USGSUrbanAreaOrtho.class.getName()
                + "," + gov.nasa.worldwind.layers.Earth.EarthNASAPlaceNameLayer.class.getName()
                + "," + gov.nasa.worldwind.layers.CompassLayer.class.getName()
        );
        defaults.setProperty(AVKey.BMNG_ONE_IMAGE_PATH, "images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg");
        defaults.setProperty(AVKey.WORLD_MAP_IMAGE_PATH, "images/earth-map-512x256.png");

        java.util.TimeZone tz = java.util.Calendar.getInstance().getTimeZone();
        if (tz != null)
            defaults.setProperty(AVKey.INITIAL_LONGITUDE,
                Double.toString(
                    Angle.fromDegrees(180.0 * tz.getOffset(System.currentTimeMillis()) / (12.0 * 3.6e6)).degrees));
        return defaults;
    }

    private void initializeCustom()
    {
        String configFileName = System.getProperty(CONFIG_FILE_PROPERTY_KEY, CONFIG_FILE_NAME);
        try
        {
            java.io.InputStream propsStream = null;
            File file = new File(configFileName);
            if (file.exists())
            {
                try
                {
                    propsStream = new FileInputStream(file);
                }
                catch (FileNotFoundException e)
                {
                    String message = Logging.getMessage("Configuration.LocalConfigFileNotFound", configFileName);
                    Logging.logger().finest(message);
                }
            }

            if (propsStream == null)
            {
                propsStream = this.getClass().getResourceAsStream("/" + configFileName);
            }

            if (propsStream == null)
            {
                Logging.logger().log(Level.WARNING, "Configuration.UnavailablePropsFile", configFileName);
            }

            if (propsStream != null)
                this.properties.load(propsStream);
        }
        // Use a named logger in all the catch statements below to prevent Logger from calling back into
        // Configuration when this Configuration instance is not yet fully instantiated.
        catch (FileNotFoundException e)
        {
            Logging.logger(DEFAULT_LOGGER_NAME).log(Level.WARNING, "Configuration.UnavailablePropsFile",
                configFileName);
        }
        catch (IOException e)
        {
            Logging.logger(DEFAULT_LOGGER_NAME).log(Level.SEVERE, "Configuration.ExceptionReadingPropsFile", e);
        }
        catch (Exception e)
        {
            Logging.logger(DEFAULT_LOGGER_NAME).log(Level.SEVERE, "Configuration.ExceptionReadingPropsFile", e);
        }
    }

    public static synchronized String getStringValue(String key, String defaultValue)
    {
        String v = getStringValue(key);
        return v != null ? v : defaultValue;
    }

    public static synchronized String getStringValue(String key)
    {
        return getInstance().properties.getProperty(key);
    }

    public static synchronized Integer getIntegerValue(String key, Integer defaultValue)
    {
        Integer v = getIntegerValue(key);
        return v != null ? v : defaultValue;
    }

    public static synchronized Integer getIntegerValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Integer.parseInt(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    public static synchronized Long getLongValue(String key, Long defaultValue)
    {
        Long v = getLongValue(key);
        return v != null ? v : defaultValue;
    }

    public static synchronized Long getLongValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Long.parseLong(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    public static synchronized Double getDoubleValue(String key, Double defaultValue)
    {
        Double v = getDoubleValue(key);
        return v != null ? v : defaultValue;
    }

    public static synchronized Double getDoubleValue(String key)
    {
        String v = getStringValue(key);
        if (v == null)
            return null;

        try
        {
            return Double.parseDouble(v);
        }
        catch (NumberFormatException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    public static synchronized boolean hasKey(String key)
    {
        return getInstance().properties.contains(key);
    }

    public static synchronized void removeKey(String key)
    {
        getInstance().properties.remove(key);
    }

    public static synchronized void setValue(String key, Object value)
    {
        getInstance().properties.put(key, value.toString());
    }

    // OS, user, and run-time specific system properties. //

    public static String getCurrentWorkingDirectory()
    {
        String dir = System.getProperty("user.dir");
        return (dir != null) ? dir : ".";
    }

    public static String getUserHomeDirectory()
    {
        String dir = System.getProperty("user.home");
        return (dir != null) ? dir : ".";
    }

    public static String getSystemTempDirectory()
    {
        String dir = System.getProperty("java.io.tmpdir");
        return (dir != null) ? dir : ".";
    }

    public static boolean isMacOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("mac");
    }

    public static boolean isWindowsOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("windows");
    }

    public static boolean isLinuxOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("linux");
    }

    public static boolean isUnixOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("unix");
    }

    public static boolean isSolarisOS()
    {
        String osName = System.getProperty("os.name");
        return osName != null && osName.toLowerCase().contains("solaris");
    }
}
