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
package name.gano.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.RenderableLayer;
import jsattrak.coverage.CoverageAnalyzer;
import name.gano.worldwind.geom.CoverageDataGeom;
import name.gano.worldwind.geom.CoverageJoglColorBar;

/**
 *
 * @author Shawn Gano
 */
public class CoverageRenderableLayer extends RenderableLayer
{

    CoverageJoglColorBar cb;
    CoverageDataGeom dataGeom;
    
    public CoverageRenderableLayer(CoverageAnalyzer ca)
    {
        super();
        // create colorbar object
        cb = new CoverageJoglColorBar(ca);
        
        // add geometry to layer
        dataGeom = new CoverageDataGeom(ca);
        this.addRenderable(dataGeom);
        
        if(ca != null)
        {
            if(ca.isShowColorBar())
            {
                this.addRenderable(cb);
            }
        }
    }
    
    
    public void updateNewCoverageObject(CoverageAnalyzer ca)
    {
        this.removeAllRenderables();
        
        cb = new CoverageJoglColorBar(ca);
        
        // add geometry to layer
        dataGeom = new CoverageDataGeom(ca);
        this.addRenderable(dataGeom);
        
        if(ca != null)
        {
            if(ca.isShowColorBar())
            {
                this.addRenderable(cb);
            }
        }
    } // updateNewCoverageObject
    
    @Override
    public String toString()
    {
        return "Coverage Data Layer";
    }
    
    
    
}

