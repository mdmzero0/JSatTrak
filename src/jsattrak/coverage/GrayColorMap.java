/*
 * GrayColorMap.java
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
public class GrayColorMap extends ColorMap
{
    public GrayColorMap()
    {
        this.setColorMap(colormap);
    }
    
    // different color map
    private static int[][] colormap = 
    {
     {0,     0,     0},
     {4,     4,     4},
     {8,     8,     8},
    {12,    12,    12},
    {16,    16,    16},
    {20,    20,    20},
    {24,    24,    24},
    {28,    28,    28},
    {32,    32,    32},
    {36,    36,    36},
    {40,    40,    40},
    {45,    45,    45},
    {49,    49,    49},
    {53,    53,    53},
    {57,    57,    57},
    {61,    61,    61},
    {65,    65,    65},
    {69,    69,    69},
    {73,    73,    73},
    {77,    77,    77},
    {81,    81,    81},
    {85,    85,    85},
    {89,    89,    89},
    {93,    93,    93},
    {97,    97,    97},
   {101,   101,   101},
   {105,   105,   105},
   {109,   109,   109},
   {113,   113,   113},
   {117,   117,   117},
   {121,   121,   121},
   {125,   125,   125},
   {130,   130,   130},
   {134,   134,   134},
   {138,   138,   138},
   {142,   142,   142},
   {146,   146,   146},
   {150,   150,   150},
   {154,   154,   154},
   {158,   158,   158},
   {162,   162,   162},
   {166,   166,   166},
   {170,   170,   170},
   {174,   174,   174},
   {178,   178,   178},
   {182,   182,   182},
   {186,   186,   186},
   {190,   190,   190},
   {194,   194,   194},
   {198,   198,   198},
   {202,   202,   202},
   {206,   206,   206},
   {210,   210,   210},
   {215,   215,   215},
   {219,   219,   219},
   {223,   223,   223},
   {227,   227,   227},
   {231,   231,   231},
   {235,   235,   235},
   {239,   239,   239},
   {243,   243,   243},
   {247,   247,   247},
   {251,   251,   251},
   {255,   255,   255},
    };
} //GrayColorMap
