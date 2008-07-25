/*
 * RelativePath.java
 * 
 * =====================================================================
 * Copyright (C) 2008 Shawn E. Gano
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

package jsattrak.utilities;

import java.io.*;
import java.util.*;

/**
 * this class provides functions used to generate a relative path
 * from two absolute paths
 * http://www.devx.com/tips/Tip/13737
 * 
 * updated: Shawn E. Gano - 25 July 2008 - fixed path seperator to be /
 * 
 * @author David M. Howard, Shawn E. Gano
 */
public class RelativePath
{
    
    static String pathSep = "/";//File.separator;  // made this cononical - SEG
    /**
     * break a path down into individual elements and add to a list.
     * example : if a path is /a/b/c/d.txt, the breakdown will be [d.txt,c,b,a]
     * @param f input file
     * @return a List collection with the individual elements of the path in
    reverse order
     */
    private static List getPathList(File f)
    {
        List l = new ArrayList();
        File r;
        try
        {
            r = f.getCanonicalFile();
            while(r != null)
            {
                l.add(r.getName());
                r = r.getParentFile();
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            l = null;
        }
        return l;
    }

    /**
     * figure out a string representing the relative path of
     * 'f' with respect to 'r'
     * @param r home path
     * @param f path of file
     */
    private static String matchPathLists(List r, List f)
    {
        int i;
        int j;
        String s;
        // start at the beginning of the lists
        // iterate while both lists are equal
        s = "";
        i = r.size() - 1;
        j = f.size() - 1;

        // first eliminate common root
        while((i >= 0) && (j >= 0) && (r.get(i).equals(f.get(j))))
        {
            i--;
            j--;
        }

        // for each remaining level in the home path, add a ..
        for(; i >= 0; i--)
        {
            s += ".." + pathSep;
        }

        // for each level in the file path, add the path
        for(; j >= 1; j--)
        {
            s += f.get(j) + pathSep;
        }

        // file name
        s += f.get(j);
        return s;
    }

    /**
     * get relative path of File 'f' with respect to 'home' directory
     * example : home = /a/b/c
     *           f    = /a/d/e/x.txt
     *           s = getRelativePath(home,f) = ../../d/e/x.txt
     * @param home base path, should be a directory, not a file, or it doesn't
    make sense
     * @param f file to generate path for
     * @return path from home to f as a string
     */
    public static String getRelativePath(File home, File f)
    {
        File r;
        List homelist;
        List filelist;
        String s;

        homelist = getPathList(home);
        filelist = getPathList(f);
        s = matchPathLists(homelist, filelist);

        return s;
    }

    /**
     * test the function
     */
    public static void main(String args[])
    {
        //args = new String[] {"c:/test/","c:/test1/test.html"};
        //args = new String[] {"c:/test1","c:/test1/models/test.html"};
        args = new String[] {"C:\\Documents and Settings\\sgano\\Desktop\\JSatTrak\\JSatTrak\\data\\models\\","C:\\Documents and Settings\\sgano\\Desktop\\JSatTrak\\JSatTrak\\data\\models\\isscomplete\\iss_complete.3ds"};
        
        
        if(args.length != 2)
        {
            System.out.println("RelativePath <home> <file>");
            return;
        }
        System.out.println("home = " + args[0]);
        System.out.println("file = " + args[1]);
        System.out.println("path = " + getRelativePath(new File(args[0]), new File(args[1])));
    }
}
