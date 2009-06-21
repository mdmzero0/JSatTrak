/*
 * Condition used to test if the propogator should stop (early)
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

/**
 *
 * @author sgano
 */
public interface StoppingCondition 
{
    // check the new step to see if it meets the stopping condition
    public boolean checkStoppingCondition(double t, double[] x, double[] dx);
    
    
    // iniatlizes stopping condition
    public void iniStoppingCondition(double t, double[] x, double[] dx);
}
