// Shawn Gano, filters out a specific type of file
// case in-sensative
/**
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 *
 * This file is part of JSatTrak.
 *
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 */
package name.gano.file;

import java.io.File;

/**
 *
 * @author sgano
 */
public class IOFileFilter implements java.io.FileFilter
{
    private String[] fileTypes;
    private boolean acceptDirs = false;

    public IOFileFilter(String... fileExtensions)
    {
        fileTypes = fileExtensions;
    }


    public boolean accept(File f)
    {
        if(f.isDirectory())
        {
            if(acceptDirs)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        String name = f.getName().toLowerCase();
        
        for(String ext : fileTypes)
        {
            if( name.endsWith(ext.toLowerCase()))
            {
                return true;
            }
        }

        return false;
    }//end accept

    /**
     * @return the fileTypes
     */
    public String[] getFileTypes()
    {
        return fileTypes;
    }

    /**
     * @param fileTypes the fileTypes to set
     */
    public void setFileTypes(String[] fileTypes)
    {
        this.fileTypes = fileTypes;
    }

    /**
     * @return the acceptDirs
     */
    public boolean isAcceptDirs()
    {
        return acceptDirs;
    }

    /**
     * @param acceptDirs the acceptDirs to set
     */
    public void setAcceptDirs(boolean acceptDirs)
    {
        this.acceptDirs = acceptDirs;
    }
}//end class IOFileFilter
