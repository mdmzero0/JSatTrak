/*
 * CoverageAnalyzer.java
 * 
 * Class to analyze Earth coverage metrics from Satellite(s). Also has the 
 * capability to render metrics to 2D and 3D windows.
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

package jsattrak.coverage;

/**
 *
 * @author Shawn
 */
public class CoverageAnalyzer 
{
    int latPanels = 18; // number of divisons along lines of latitude (grid points -1)
    int longPanels = 36;  // number of divisions along lines of longitude (grid points -1)
    
    double[] latBounds = {-90.0,90.0}; // minimum,maxium latitude to use in coverage anaylsis
    double[] longBounds = {-180.0,180.0}; // minimum,maxium longitude to use in coverage anaylsis
    
    double[][] coverageCumTime; // cumulative coverage time array [latPanels x longPanels]
    double[][] latPanelMidPoints; // middle point latitude of each division/panel
    double[][] lonPanelMidPoints; // middle point longitude of each division/panel
    
    // default constructor
    public CoverageAnalyzer()
    {
        iniParamters();
    } // constructor
    
    // initalized all parameters (used at class construction to create all arrays, etc)
    private void iniParamters()
    {
        
    }// iniParamters
}
