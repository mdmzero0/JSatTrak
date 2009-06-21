/*
 * Node for Custom Sat Class Mission Designer
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

package jsattrak.customsat;

import java.awt.Toolkit;
import javax.swing.ImageIcon;
import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author sgano
 */
public class StopNode extends CustomTreeTableNode
{  
    
    public StopNode(CustomTreeTableNode parentNode)
    {
        super(new String[] {"Stop","",""}); // initialize node, default values
        // set icon for this type
        setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/customSatIcons/stop.png")) ) );
        //set Node Type
        setNodeType("Stop");
        
        // add this node to parent - last thing
        if( parentNode != null)
            parentNode.add(this);
    }
    
    
}
