// Shawn Gano, filters out a specific type of file
// Does compare exact file extension given, plus an all caps and all lowercase variants only
/**
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 */
package name.gano.file;

import java.io.FilenameFilter;
import java.io.File;

public class FileTypeFilter implements FilenameFilter
{

    private String fileExtension;

    public FileTypeFilter(String fileExtension)
    {
        this.fileExtension = fileExtension;
    }

    public boolean accept(File dir, String name)
    {
        if(name.endsWith("." + fileExtension))
            return true;
        else if(name.endsWith("." + fileExtension.toUpperCase()))
            return true;
         else if(name.endsWith("." + fileExtension.toLowerCase()))
             return true;
        // no matches return false
        return false;
    }
}

