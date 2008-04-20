/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.cache;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.Logging;

import java.io.*;

/**
 * @author Tom Gaskins
 * @version $Id: BasicDataFileCache.java 3335 2007-10-19 04:05:22Z tgaskins $
 */
public class BasicDataFileCache extends AbstractFileCache
{
    public BasicDataFileCache()
    {
        String cachePathName = Configuration.getStringValue(AVKey.DATA_FILE_CACHE_CONFIGURATION_FILE_NAME);
        if (cachePathName == null)
        {
            String message = Logging.getMessage("FileCache.NoConfiguration");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        java.io.InputStream is = null;
        File file = new File(cachePathName);
        if (file.exists())
        {
            try
            {
                is = new FileInputStream(file);
            }
            catch (FileNotFoundException e)
            {
                String message = Logging.getMessage("FileCache.LocalConfigFileNotFound", cachePathName);
                Logging.logger().finest(message);
            }
        }

        if (is == null)
        {
            is = this.getClass().getClassLoader().getResourceAsStream(cachePathName);
        }

        if (is == null)
        {
            String message = Logging.getMessage("FileCache.ConfigurationNotFound", cachePathName);
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.initialize(is);
    }
}
