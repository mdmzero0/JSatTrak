/*
 * SatPropertyPanelSave.java
 *=====================================================================
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
 * Created on August 20, 2007, 12:52 PM
 *
 * 
 */

package jsattrak.utilities;

import jsattrak.gui.SatPropertyPanel;
import java.awt.Dimension;
import java.io.Serializable;

/**
 *
 * @author sgano
 */
public class SatPropertyPanelSave implements Serializable
{
    private int xPos; // location of window
    private int yPos;
    
    private int width;
    private int height;
    
    private String name;
    
    
    /** Creates a new instance of SatPropertyPanelSave */
    public SatPropertyPanelSave(SatPropertyPanel panel, int x, int y, Dimension d)
    {
        xPos = x;
        yPos = y;
        
        width = d.width;
        height = d.height;
        
        name = panel.getSatName();
    }

    public int getXPos()
    {
        return xPos;
    }

    public void setXPos(int xPos)
    {
        this.xPos = xPos;
    }

    public int getYPos()
    {
        return yPos;
    }

    public void setYPos(int yPos)
    {
        this.yPos = yPos;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
}
