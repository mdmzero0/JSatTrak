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

package name.gano.math.nonlinsolvers;

/**
 *
 * @author sgano
 */
public abstract class NonLinearEquationSystemSolver {

    public abstract double[] getDx();

    public abstract double[] getFGoals();

    public abstract double getFinalError();

    public abstract int getFuncEval();

    public abstract int getMaxIter();

    public abstract String getOutputMessage();

    public abstract double getTol();

    public abstract double[] getX();

    public abstract boolean isSolverConverged();

    public abstract boolean isVerbose();

    public abstract void setDx(double[] dx);

    public abstract void setFGoals(double[] fGoals);

    public abstract void setMaxIter(int maxIter);

    public abstract void setTol(double tol);

    public abstract void setVerbose(boolean verbose);

    public abstract void setX(double[] X);

    public abstract boolean solve();

}
