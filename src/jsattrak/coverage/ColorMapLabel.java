/*
 * ColorMapLabel.java
 * 
 * Class to analyze Earth coverage metrics from Satellite(s). Also has the 
 * capability to render metrics to 2D and 3D windows.
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

import java.awt.Graphics;
import javax.swing.JLabel;

/**
 *
 * @author sgano
 */
public class ColorMapLabel  extends JLabel
{
    
    private ColorMap colorMap = new ColorMap(); // default "jet"
    
    public ColorMapLabel()
    {
        this.setText("");
    }
    
    public ColorMapLabel(ColorMap colorMap)
    {
        this.colorMap = colorMap;
        this.setText("");
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);  // repaints what is already on the buffer
        
        int width = this.getWidth();
        int height = this.getHeight();
        
        if(width > 0 && height >0)
        {
            for(int i=0;i<width;i++)
            {
                g.setColor(colorMap.getColor(i, 0, width-1));
                g.drawLine(i, 0, i, height);
            }
        }
    } // paintComponent

    public ColorMap getColorMap()
    {
        return colorMap;
    }

    public void setColorMap(ColorMap colorMap)
    {
        this.colorMap = colorMap;
        this.repaint();
    }
}
