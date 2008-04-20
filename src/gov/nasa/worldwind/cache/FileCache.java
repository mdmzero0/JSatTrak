/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.cache;

/**
 * @author Tom Gaskins
 * @version $Id: FileCache.java 2422 2007-07-25 23:07:49Z tgaskins $
 */
public interface FileCache
{
    public static final String OS_SPECIFIC_DATA_PATH = "FileCache.OSSpecificDataPathKey";

    public boolean contains(String fileName);

    public java.io.File newFile(String fileName);

    java.net.URL findFile(String fileName, boolean checkClassPath);

    void removeFile(java.net.URL url);

    void addCacheLocation(String newPath);

    void removeCacheLocation(String newPath);

    java.util.List<java.io.File> getCacheLocations();

    java.io.File getWriteLocation();

    void addCacheLocation(int index, String newPath);
}
