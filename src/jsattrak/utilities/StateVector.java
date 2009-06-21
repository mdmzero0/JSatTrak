/*
 * State Vector - Shawn Gano
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

import java.io.Serializable;

public class StateVector implements Serializable
{
	public double[] state; // t,x,y,z,dx,dy,dz - seven elements
	
	public StateVector()
	{
		state = new double[7];
	}
	
	public StateVector(double[] newStateWithTime)
	{
		state = new double[7];
		for(int i=0;i<7;i++)
		{
			state[i] = newStateWithTime[i];
		}
	}
        
        // constructor given a new state that doesn't include time (x,y,z,dx,dy,dz), and time
        public StateVector(double[] newState, double time)
	{
		state = new double[7];
                
                state[0] = time;
		for(int i=1;i<7;i++)
		{
			state[i] = newState[i-1];
		}
	}
	
	public StateVector(double t, double x, double y, double z, double dx, double dy, double dz)
	{
		state = new double[7];
		state[0] = t;
		state[1] = x;
		state[2] = y;
		state[3] = z;
		state[4] = dx;
		state[5] = dy;
		state[6] = dz;
	}
        
        public String toString()
        {
            return "" + state[0] + ","+ state[1] + ","+ state[2] + ","+ state[3] + ","+ state[4] + ","+ state[5] + ","+ state[6];
        }
}
