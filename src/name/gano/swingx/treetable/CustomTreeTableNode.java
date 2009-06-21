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

import java.io.Serializable;
import java.util.Vector;
import javax.swing.Icon;
import jsattrak.customsat.GoalParameter;
import jsattrak.customsat.InputVariable;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.StateVector;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;

/**
 *
 * @author Shawn
 */
public class CustomTreeTableNode extends DefaultMutableTreeTableNode implements Serializable
{
    private String nodeType = "default";
    
    protected Icon icon;
    protected String iconName;
    
    Object[] userObject;
        
    private double startTTjulDate = -1; // save julian date when this node starts (-1) if not set yet.
    
    
    public CustomTreeTableNode(Object[] userObject) 
    {
        super(userObject);
        
        this.userObject = userObject;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setValueAt(Object aValue, int column) 
    {
        if( column > (getColumnCount()-1) )
        {
            // no nothing
        }
        else
        {
            //((Object[])getUserObject())[column] = aValue;
            userObject[column] = aValue;
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    public int getColumnCount() 
    {      
        //return  ((Object[])getUserObject()).length;
        return  userObject.length;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getValueAt(int column) 
    {
        if( column > (getColumnCount()-1) )
        {
            return "";
        }
        else
        {
            //return ((Object[])getUserObject())[column];
            return userObject[column];
        }
    }
    
    // newNode.setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/toolbarButtonGraphics/treeicons/Satellite_TLE.png")) ) );
    public void setIcon(Icon icon)
    {
        this.icon = icon;
    }

    public Icon getIcon()
    {
        return icon;
    }

    public String getIconName()
    {
        if (iconName != null)
        {
            return iconName;
        }
        else
        {
            String str = userObject.toString();
            int index = str.lastIndexOf(".");
            if (index != -1)
            {
                return str.substring(++index);
            }
            else
            {
                return null;
            }
        }
    }

    public void setIconName(String name)
    {
        iconName = name;
    }

    public String getNodeType()
    {
        return nodeType;
    }

    protected void setNodeType(String nodeType)
    {
        this.nodeType = nodeType;
    }
    
    // meant to be overridden by implementing classes
    // passes in main app to add the internal frame to
    public void displaySettings(JSatTrak app)
    {
        // dummy should open a window for node settings
        //System.out.println("Should open settings for : " + getValueAt(0) );
    }
    
    // meant to be overridden by implementing classes
    public void execute(Vector<StateVector> ephemeris)
    {
         // dummy but should do something based on input ephemeris
        //System.out.println("Executing : " + getValueAt(0) );
        
        // save initial time of the node ( TT)
        this.setStartTTjulDate(ephemeris.lastElement().state[0]);
        
    }// execute

    // meant to be over ridden if there are any input vars
    public double getVar(int varInt)
    {
        return 0;
    }

    // meant to be over ridden if there are any input vars
    public void setVar(int varInt, double val)
    {
    }

    // meant to be over ridden if there are any input vars
    public Vector<InputVariable> getInputVarVector()
    {
        return new Vector<InputVariable>(1);
    }
    
    
    // meant to be over ridden if there are any input vars
    public Double getGoal(int goalInt)
    {
        return new Double(0);
    }

    // meant to be over ridden if there are any input vars
    public Vector<GoalParameter> getGoalParamVector()
    {
        return new Vector<GoalParameter>(1);
    }

    public double getStartTTjulDate()
    {
        return startTTjulDate;
    }

    public void setStartTTjulDate(double startTTjulDate)
    {
        this.startTTjulDate = startTTjulDate;
    }
    
}
