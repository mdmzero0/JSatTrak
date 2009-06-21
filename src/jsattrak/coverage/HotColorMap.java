/*
 * HotColorMap.java
 * 
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

package jsattrak.coverage;

/**
 *
 * @author Shawn
 */
public class HotColorMap extends ColorMap
{
    public HotColorMap()
    {
        this.setColorMap(colormap);
    }
    
    // different color map
    private static int[][] colormap = 
    {
    {11,     0,     0},
    {21,     0,    0},
    {32,     0,     0},
    {43,     0,     0},
    {53,     0,     0},
    {64,     0,     0},
    {74,     0,     0},
    {85,     0,     0},
    {96,     0,     0},
   {106,     0,     0},
   {117,     0,     0},
   {128,     0,     0},
   {138,     0,     0},
   {149,     0,     0},
   {159,     0,     0},
   {170,     0,     0},
   {181,     0,     0},
   {191,     0,     0},
   {202,     0,     0},
   {213,     0,     0},
   {223,     0,     0},
   {234,     0,     0},
   {244,     0,     0},
   {255,     0,     0},
   {255,    11,     0},
   {255,    21,     0},
   {255,    32,     0},
   {255,    43,     0},
   {255,    53,     0},
   {255,    64,     0},
   {255,    74,     0},
   {255,    85,     0},
   {255,    96,     0},
   {255,   106,     0},
   {255,   117,     0},
   {255,   128,     0},
   {255,   138,     0},
   {255,   149,     0},
   {255,   159,     0},
   {255,   170,     0},
   {255,   181,     0},
   {255,   191,     0},
   {255,   202,     0},
   {255,   213,     0},
   {255,   223,     0},
   {255,   234,     0},
   {255,   244,     0},
   {255,   255,     0},
   {255,   255,    16},
   {255,   255,    32},
   {255,   255,    48},
   {255,   255,    64},
   {255,   255,    80},
   {255,   255,    96},
   {255,   255,   112},
   {255,   255,   128},
   {255,   255,   143},
   {255,   255,   159},
   {255,   255,   175},
   {255,   255,   191},
   {255,   255,   207},
   {255,   255,   223},
   {255,   255,   239},
   {255,   255,   255},
    };
} //HotColorMap
