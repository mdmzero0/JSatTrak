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
 * 
 */

package jsattrak.customsat;

import java.awt.Toolkit;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import jsattrak.customsat.gui.SolverPanel;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.StateVector;
import name.gano.math.nonlinsolvers.ModifiedBroydenSolver;
import name.gano.math.nonlinsolvers.ModifiedNewtonFiniteDiffSolver;
import name.gano.math.nonlinsolvers.NonLinearEquationSystemProblem;
import name.gano.math.nonlinsolvers.NonLinearEquationSystemSolver;
import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author sgano
 */
public class SolverNode extends CustomTreeTableNode implements NonLinearEquationSystemProblem   
{  
    public static int NEWTONSOLVER = 0;
    public static int BROYDENSOLVER = 1;
    
    private int solver = SolverNode.BROYDENSOLVER; // default to Broyden
    private int maxIter = 25; 
    private boolean solverActive = true;
    private double convergenceTol = 1E-6;
        
    private Vector<InputVariable> inputVarVec = new Vector<InputVariable>(1);
    private Vector<GoalParameter> goalParamVec = new Vector<GoalParameter>(1);
    
    Vector<StateVector> ephemerisInternal = new Vector<StateVector>(30,30);
    StateVector lastStateFromEphemerisExternal;
    
       
    public SolverNode(CustomTreeTableNode parentNode)
    {
        super(new String[] {"Solver","",""}); // initialize node, default values
        
        iniNode(parentNode, true);
    }
    
    public SolverNode(CustomTreeTableNode parentNode, boolean addDefaultcomponets)
    {
        super(new String[] {"Solver","",""}); // initialize node, default values
        
        iniNode(parentNode, addDefaultcomponets);
        
    }
    
    private void iniNode(CustomTreeTableNode parentNode, boolean addDefaultcomponets)
    {
        
        // set icon for this type
        setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/customSatIcons/solver.png")) ) );
        //set Node Type
        setNodeType("Solver");
        
        // add default (burn and propogate componets as children) if desired
        if(addDefaultcomponets)
        {
            new ManeuverNode(this);
            new PropogatorNode(this);
        }
        
        // always gets an end solver / loop node
        new LoopNode(this);
        
        // add this node to parent - last thing
        if( parentNode != null)
            parentNode.add(this);
    } // iniNode
    
    // passes in main app to add the internal frame to
    public void displaySettings(JSatTrak app)
    {
        
        String windowName = "" + getValueAt(0);
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);
        
        // show satellite browser window
        SolverPanel panel = new SolverPanel(this,iframe,app); // non-modal version
        //panel.setIframe(iframe);        
        
        iframe.setContentPane( panel );
        iframe.setSize(410+75,380+20); // w,h
        iframe.setLocation(5,5);
        
        app.addInternalFrame(iframe);
        
    }

    // =  EXE NODE ===============================================
    
   
    public void execute(Vector<StateVector> ephemeris)
    {
         // dummy but should do something based on input ephemeris
        //System.out.println("Executing : " + getValueAt(0) );
        
        //JLogBoxDialog logBox = new JLogBoxDialog(null,false,this.getValueAt(0) + ": results");
        //logBox.get
       
        // save initial time of the node ( TT)
        this.setStartTTjulDate(ephemeris.lastElement().state[0]);
        
        // should run each child of this node (top level should not run recursivley should be done here)
        // run also if no goals set (just run through children)
        if( !this.isSolverActive() || goalParamVec.size() == 0)
        {
            // solver not active -- just run through children one executing them (using given ephemeris)
            executeChildren(ephemeris);
        }
        else
        {
            // USE SOLVER!
            System.out.println("Running Solver: " + getValueAt(0) );
            
            // save ephemeris last state
            lastStateFromEphemerisExternal = new StateVector(ephemeris.lastElement().state);
            
            // create solver (and create function eval method) - used scaled values, scale goals
            double[] fGoals = new double[goalParamVec.size()];
            double[] X0 = new double[inputVarVec.size()];
            double[] dX = new double[inputVarVec.size()];
            
            // setup goals and X and Dx -- all scaled!
            for(int i=0;i<goalParamVec.size();i++)
            {
                fGoals[i] = goalParamVec.get(i).getGoalValue()/goalParamVec.get(i).getScale();
            }
            for(int i=0;i<inputVarVec.size();i++)
            {
                X0[i] = inputVarVec.get(i).getScaledValue();
                dX[i] = inputVarVec.get(i).getDx()/inputVarVec.get(i).getScale(); // scale dx
            }
            
            NonLinearEquationSystemSolver nonLinsolver;
            if(this.solver == SolverNode.NEWTONSOLVER)
            {
                nonLinsolver = new ModifiedNewtonFiniteDiffSolver(this, fGoals, X0);
            }
            else
            {
                nonLinsolver = new ModifiedBroydenSolver(this, fGoals, X0);
            }
            // set dX 
            nonLinsolver.setDx(dX);
            
            // run solver (clear epeheris each run if not complete)
            nonLinsolver.setVerbose(true); // so we can see results
            boolean solveSuccess = nonLinsolver.solve();
            
            System.out.println("Solver complete: " + nonLinsolver.getOutputMessage());
            
            //when solver complete ... copy last internal ephemeris to (minus first elelemt) to real ephemeris
            for(int i=1;i<ephemerisInternal.size();i++) // skip first entry
            {
                ephemeris.add( ephemerisInternal.get(i) );
            }
            
        } // use solver (end)
        
    }// execute
    
    
    private void executeChildren(Vector<StateVector> ephemeris)
    {
        
        for (int i = 0; i <  this.getChildCount(); i++)
        {
            // get child
            CustomTreeTableNode child = (CustomTreeTableNode) this.getChildAt(i);
            
            child.execute(ephemeris); // run the child
            
        }
    }
    
    // SOLVERS - function to be evaluated!
    public double[] evaluateSystemOfEquations(double[] x)
     {
        ephemerisInternal.clear(); // clear the internal epemeris
        ephemerisInternal.add(new StateVector(lastStateFromEphemerisExternal.state)); // add inital state
        
        
        // set the new variable values
         for (int i = 0; i < inputVarVec.size(); i++)
         {
            inputVarVec.get(i).setScaledValue(x[i]);
         }
        
        // run children
        executeChildren(ephemerisInternal);
        
        // extract results:   
         double[] f = new double[goalParamVec.size()];
         for (int i = 0; i < goalParamVec.size(); i++)
         {
            f[i] = goalParamVec.get(i).getScaledValue();
         }
         
         return f;
    }
    
    // ============================================================
    
    public Vector<InputVariable> getInputVarVec()
    {
        return inputVarVec;
    }

    public void setInputVarVec(Vector<InputVariable> inputVarVec)
    {
        this.inputVarVec = inputVarVec;
    }

    public Vector<GoalParameter> getGoalParamVec()
    {
        return goalParamVec;
    }

    public void setGoalParamVec(Vector<GoalParameter> goalParamVec)
    {
        this.goalParamVec = goalParamVec;
    }

    public int getSolver()
    {
        return solver;
    }

    public void setSolver(int solver)
    {
        this.solver = solver;
    }

    public int getMaxIter()
    {
        return maxIter;
    }

    public void setMaxIter(int maxIter)
    {
        this.maxIter = maxIter;
    }

    public boolean isSolverActive()
    {
        return solverActive;
    }

    public void setSolverActive(boolean solverActive)
    {
        this.solverActive = solverActive;
    }

    public double getConvergenceTol()
    {
        return convergenceTol;
    }

    public void setConvergenceTol(double convergenceTol)
    {
        this.convergenceTol = convergenceTol;
    }
}
