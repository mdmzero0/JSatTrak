/*
 * BoldFontCellRenderer.java
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
 * 
 * Created on August 4, 2007, 11:16 PM
 *
 */

package jsattrak.gui;


import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
public class BoldFontCellRenderer extends DefaultTableCellRenderer implements java.io.Serializable
{
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component cell = super.getTableCellRendererComponent
                (table, value, isSelected, hasFocus, row, column);
        
        //cell.setBackground( Color.red );
        // You can also customize the Font and Foreground this way
        // cell.setForeground();
        // cell.setFont();
        
        
        
        
        
        // bold header row
        if(row == 4 || row == 8 || row ==12)
        {
            cell.setFont( new Font("Default",Font.BOLD,12) );
            
        }
        
        cell.setForeground(Color.BLACK); // fix problem when selecting and text turns white
        
        // background colors
        if(row >=4 && row < 8 )
        {
            //cell.setBackground( new Color(210,231,199)); // light green
            cell.setBackground( new Color(210,231,199)); // light green
        }
        else if(row >=12)
        {
            cell.setBackground( new Color(249,255,202)); // light green
        }
        else
        {
            cell.setBackground( Color.WHITE); // light green
        }
        
        // age of TLE
        try
        {
            if(row == 0 && column == 1)
            {
                String s =  table.getModel().getValueAt(0, 1 ).toString();
                double d = Double.parseDouble(s);
                
                if(Math.abs(d) > 12)//30
                {
                    cell.setBackground( new Color(255,0,0)); // red
                }
                else if(Math.abs(d) > 10)//25
                {
                    cell.setBackground( new Color(255,153,0)); // yellow
                }
                else if(Math.abs(d) > 8)//20
                {
                    cell.setBackground( new Color(255,204,0)); // yellow
                }
                else if(Math.abs(d) > 6)//15
                {
                    cell.setBackground( new Color(255,255,0)); // yellow
                }
                else if(Math.abs(d) > 4)//10
                {
                    cell.setBackground( new Color(204,255,0)); // yellow
                }
                else if(Math.abs(d) > 2) //5
                {
                    cell.setBackground( new Color(153,255,0)); // yellow
                }
                else
                {
                    cell.setBackground( new Color(0,255,0)); // green
                }
            }
        }
        catch(Exception ee)
        {
            //
        }
        
        
        return cell;
        
    } // getTableCellRendererComponent
    
} // BoldFontCellRenderer
