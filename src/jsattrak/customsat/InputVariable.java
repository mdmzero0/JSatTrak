/*
 * Input variable for the solver class - can present a number of variables from different node types
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
 * 
 */

package jsattrak.customsat;

import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author sgano
 */
public class InputVariable 
{
    // variable info
    private CustomTreeTableNode parentNode; // parent Node that the variable belongs to
    int varInt; // variable number of the parent Node - used for getting and setting value in node
    private String nameDescription; // name and short discription of the variable
    double currentValue;  // current value of the variable (unscaled)
    
    // parameters
    private double scale = 1; // value to scale variable when computing error (use inial value for now)
    private double dx = 1E-3; // value used in finite differecing for determining derivatives
    
    // previous value (for restoring values after a bad run - the user specifies when this happens)
    private double previousValue;
    
    public InputVariable(CustomTreeTableNode parentNode, int varInt, String nameDescription, double iniValue)
    {
        this.parentNode = parentNode;
        this.varInt = varInt;
        this.nameDescription = nameDescription;
        this.currentValue = iniValue;  
        
        // set previous value = to current one initially
        previousValue = iniValue;
        
        // set scale
        if(Math.abs(iniValue) > 1E-6)  
        {
            scale = Math.abs(iniValue); 
            
            // set dx via 1/1000 of inital value
            dx = Math.abs(iniValue/1000); 
        } 
        
    }
    
    // returns name of the item -- used for displaying in a JTree
    public String toString()
    {
        return getNameDescription();
    }
    
    // used to set the variable to a given value - no scaling here
    // called by GUI
    public void setValue(double val)
    {
        currentValue = val;
        getParentNode().setVar(varInt, currentValue);
    }
    
    // called by solver
    public void setScaledValue(double scaledVal)
    {
        currentValue = scaledVal*getScale();
        getParentNode().setVar(varInt, currentValue);
    }
    
    // used to get Value of the variable  - Not Scaled
    public double getValue()
    {
        currentValue = getParentNode().getVar(varInt);
        return currentValue;
    }
    
    // used to get scaled Value of the variable 
    public double getScaledValue()
    {
        currentValue = getParentNode().getVar(varInt);
        return currentValue/getScale();
    }

    public double getScale()
    {
        return scale;
    }

    public void setScale(double scale)
    {
        this.scale = scale;
    }

    public double getDx()
    {
        return dx;
    }

    public void setDx(double dx)
    {
        this.dx = dx;
    }

    public String getNameDescription()
    {
        return nameDescription;
    }

    public // variable info
    CustomTreeTableNode getParentNode()
    {
        return parentNode;
    }

    /**
     * Internally saves current value
     */
    public void saveCurrentToPreviousValue()
    {
        previousValue = currentValue;
    }

    /**
     * Reverts value back to previous value
     */
    public void setPrevioustoCurrentValue()
    {
        setValue(previousValue);
        
    }
    
    
}
