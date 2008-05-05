/*
 *  Interface for 3D view windows (both internal and dialogs)
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

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;
import java.awt.Point;
import jsattrak.gui.JSatTrak;

/**
 *
 * @author sgano
 */
public interface  J3DEarthComponent 
{
    public boolean getTerrainProfileEnabled();
    public String getTerrainProfileSat();
    public double getTerrainProfileLongSpan();
    public void setTerrainProfileSat(String terrainProfileSat);
    public void setTerrainProfileLongSpan(double terrainProfileLongSpan);
    public void setTerrainProfileEnabled(boolean enabled);
    public boolean isViewModeECI();
    public void setViewModeECI(boolean viewModeECI);
    public JSatTrak getApp();
    public WorldWindow getWwd();
    public String getDialogTitle();
    public int getWwdWidth();
    public int getWwdHeight();
    public Point getWwdLocationOnScreen();
    public LayerList getLayerList();
    public void setFarClipDistance(double clipDist);
    public void setNearClipDistance(double clipDist);
}
