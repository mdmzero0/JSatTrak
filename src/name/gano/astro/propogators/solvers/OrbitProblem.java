/*
 * Shawn E. Gano
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

package name.gano.astro.propogators.solvers;

import jsattrak.utilities.StateVector;

public interface OrbitProblem
{
        // this function advances the time 
        // this computes the velocity at the next time step  
        public double[] deriv(double[] pos, double[] v, double t);
	
	public void setVerbose(boolean verbose);
	
	public boolean getVerbose();
	
	//public Vector getEphemerisVector();
        
        public void addState2Ephemeris(StateVector state);
        
        //public boolean getStoreEphemeris();
        
} // Derivable
