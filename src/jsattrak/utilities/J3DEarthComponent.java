/*
 *  Interface for 3D view windows (both internal and dialogs)
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

package jsattrak.utilities;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.LayerList;
import java.awt.Point;
import jsattrak.gui.JSatTrak;
import name.gano.worldwind.geom.ECIRadialGrid;
import name.gano.worldwind.layers.Earth.EcefTimeDepRenderableLayer;

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
    public void setOrbitFarClipDistance(double clipDist);
    public void setOrbitNearClipDistance(double clipDist);
    public double getOrbitFarClipDistance();
    public double getOrbitNearClipDistance();
    public boolean isModelViewMode();
    public void setModelViewMode(boolean modelViewMode);
    public String getModelViewString();
    public void setModelViewString(String modelViewString);
    public double getModelViewNearClip();
    public void setModelViewNearClip(double modelViewNearClip);
    public double getModelViewFarClip();
    public void setModelViewFarClip(double modelViewFarClip);
    public void resetWWJdisplay();
    public boolean isSmoothViewChanges();
    public void setSmoothViewChanges(boolean smoothViewChanges);
    // sun shading
    public void setSunShadingOn(boolean useSunShading);
    public boolean isSunShadingOn();
    public void setAmbientLightLevel(int level);
    public int getAmbientLightLevel();
    public boolean isLensFlareEnabled();
    public void setLensFlare(boolean enabled);
    public EcefTimeDepRenderableLayer getEcefTimeDepRenderableLayer();
    // grid
    public ECIRadialGrid getEciRadialGrid();

}
