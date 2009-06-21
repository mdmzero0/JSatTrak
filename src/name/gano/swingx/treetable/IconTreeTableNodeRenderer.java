/*
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

package name.gano.swingx.treetable;

import java.awt.Component;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.JTree;
import org.jvnet.substance.api.renderers.SubstanceDefaultTreeCellRenderer;

/**
 *
 * @author sgano
 */
public class IconTreeTableNodeRenderer extends SubstanceDefaultTreeCellRenderer //DefaultTreeCellRenderer // to work with Substance LAF
{
    
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus)
    {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);

        Icon icon = ((CustomTreeTableNode) value).getIcon();

        if (icon == null)
        {
            Hashtable icons = (Hashtable) tree.getClientProperty("JTree.icons");
            String name = ((CustomTreeTableNode) value).getIconName();
            if ((icons != null) && (name != null))
            {
                icon = (Icon) icons.get(name);
                if (icon != null)
                {
                    setIcon(icon);
                }
            }
            
            // also set text of the first column correctly
            setText(((CustomTreeTableNode) value).getValueAt(0).toString());
        }
        else
        {
            setIcon(icon);
            // also set text of the first column correctly
            setText(((CustomTreeTableNode) value).getValueAt(0).toString());
        }

        return this;
    }
}
