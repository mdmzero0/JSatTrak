/*
 * CoolColorMap.java
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
public class CoolColorMap extends ColorMap
{
    public CoolColorMap()
    {
        this.setColorMap(colormap);
    }
    
    // different color map
    private static int[][] colormap = 
    {
     {0,   255,   255},
     {4,   251,   255},
     {8,   247,   255},
    {12,   243,   255},
    {16,   239,   255},
    {20,   235,   255},
    {24,   231,   255},
    {28,   227,   255},
    {32,   223,   255},
    {36,   219,   255},
    {40,   215,   255},
    {45,   210,   255},
    {49,   206,   255},
    {53,   202,   255},
    {57,   198,   255},
    {61,   194,   255},
    {65,   190,   255},
    {69,   186,   255},
    {73,   182,   255},
    {77,   178,   255},
    {81,   174,   255},
    {85,   170,   255},
    {89,   166,   255},
    {93,   162,   255},
    {97,   158,   255},
   {101,   154,   255},
   {105,   150,   255},
   {109,   146,  255},
   {113,   142,  255},
   {117,   138,  255},
   {121,   134,   255},
   {125,   130,   255},
   {130,   125,   255},
   {134,   121,   255},
   {138,   117,   255},
   {142,   113,   255},
   {146,   109,   255},
   {150,   105,   255},
   {154,   101,   255},
   {158,    97,   255},
   {162,    93,   255},
   {166,    89,   255},
   {170,    85,   255},
   {174,    81,   255},
   {178,    77,   255},
   {182,    73,   255},
   {186,    69,   255},
   {190,    65,   255},
   {194,    61,   255},
   {198,    57,   255},
   {202,    53,   255},
   {206,    49,   255},
   {210,    45,   255},
   {215,    40,   255},
   {219,    36,   255},
   {223,    32,   255},
   {227,    28,   255},
   {231,    24,   255},
   {235,    20,   255},
   {239,    16,   255},
   {243,    12,   255},
   {247,     8,   255},
   {251,     4,   255},
   {255,     0,   255},
    };
} //CoolColorMap
