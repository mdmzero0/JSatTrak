/*
 * Lagrange interpolating polynomial
 * Shawn Gano, 4-Sept-2006
 * http://mathworld.wolfram.com/LagrangeInterpolatingPolynomial.html
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

package name.gano.math.interpolation;

/**
 *
 * @author sgano
 */
public class LagrangeInterp
{
	// special variation for n=3
	// returns f(x), from points f(x1),f(x2), f(x3)
	public static double Lagrange3pt(double x,double x1,double f1,double x2,double f2,double x3,double f3)
	{
		double fx = (x - x2)*(x-x3)/((x1-x2)*(x1-x3))*f1 + 
					(x - x1)*(x-x3)/((x2-x1)*(x2-x3))*f2 + 
					(x - x1)*(x-x2)/((x3-x1)*(x3-x2))*f3;
		return fx;
	}
	
	// general version for arbitrary n
}
