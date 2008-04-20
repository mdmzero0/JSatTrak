/*
 * Runs Mission Design Propogation in a seperate Thread
 * =====================================================================
 * Copyright (C) 2008 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 * 
 */

package jsattrak.customsat.swingworker;

import java.util.List;
import java.util.Vector;
import javax.swing.SwingWorker;
import jsattrak.customsat.InputVariable;
import jsattrak.customsat.SolverNode;
import jsattrak.customsat.StopNode;
import jsattrak.gui.JSatTrak;
import jsattrak.objects.CustomSatellite;
import jsattrak.utilities.StateVector;
import name.gano.swingx.treetable.CustomTreeTableNode;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

/**
 *
 * @author Shawn
 */


public class MissionDesignPropagator extends SwingWorker<Object, Integer> 
{
    
    private boolean propMissionTreeStop = false; 
    
    CustomTreeTableNode rootNode;
    DefaultTreeTableModel  treeTableModel; 
    private Vector<StateVector> ephemeris;
    
    CustomSatellite sat; // sat for this propogation to save data back to
    
    String currentMessage = "";
    
    JSatTrak app;
    
    long totalExeTime = 0;
    
    int childCount = 0; // count for number of children
    int currentChild = 0;
    
    boolean debug = false;
    
    public MissionDesignPropagator(CustomTreeTableNode rootNode, DefaultTreeTableModel  treeTableModel, Vector<StateVector> ephemeris, CustomSatellite sat, JSatTrak app) 
    {
       // transfer in needed objects to run
        this.rootNode = rootNode;
        this.treeTableModel = treeTableModel;
        this.ephemeris = ephemeris;
        this.sat = sat;
        this.app = app;
        
        
    }
    
    @Override
    protected Object doInBackground() throws Exception 
    {
        System.out.println("-----------------------------");
        System.out.println("Mission propogation initiated.\n");
        
        propMissionTreeStop = false; // first change state so function dosen't stop imediatly
        countMissionTreeNodes(rootNode);
        
        // save all variable values
        propMissionTreeStop = false; // first change state so function dosen't stop imediatly
        saveAllVariables(rootNode);
        
        propMissionTreeStop = false; // first change state so function dosen't stop imediatly
        ephemeris.clear(); // clears ephemeris before propogating
        
        long startTime = System.currentTimeMillis(); // start timer
        
        propMissionTree(rootNode);
        
        totalExeTime = System.currentTimeMillis() - startTime;
        
        // prop done, now save results
        sat.setEphemeris(ephemeris);
        
        System.out.println("\nMission propogation finished. (" +  totalExeTime/1000.0 + " s)");
        System.out.println("-----------------------------\n");
        
        return true;
    }
    
    
    @Override
    protected void done() 
    {
        // update gui as needed when finished... stop animation reset progress bar etc.
        
        // stop animation etc
        app.stopStatusAnimation();
        
        // set progress bar to 0, set invisible
        app.setStatusProgressBarVisible(false);
        
        // should add message with how long this took to propogate
        app.setStatusMessage(sat.getName() + " - Finished Propogating; CPU time [s]: " + ((totalExeTime)/1000.0) );
        
        // repaint
        sat.setGroundTrackIni2False(); // force recalculation of ground tracks
        app.updateTime(); // update time of everything and repaint
        //app.forceRepainting();
    }
    
    // runs every once in a while to update GUI, use publish( int ) and the int will be added to the List
    @Override
    protected void process(List<Integer> chunks) 
    {
      //System.out.println("process: " + chunks);
        
        // hmm also set progress bar message to name of currently working on segment
        
        //currentMessage;
        app.setStatusProgressBarValue( (int) (chunks.get(chunks.size()-1)*100.0/(childCount)) );
        app.setStatusProgressBarText(currentMessage);
        
    }
    
    
    protected void propMissionTree(Object o)
    {
        // now just exe children only if a child has children it is responsible for running them
        if(!propMissionTreeStop)
        {
            for (int i = 0; i < treeTableModel.getChildCount(o); i++)
            {
                // get child
                CustomTreeTableNode child = (CustomTreeTableNode) treeTableModel.getChild(o, i);
                
                // set message to name
                currentMessage = child.getValueAt(0).toString();
                
                currentChild++; // increment where we are
                // publish progress
                publish(currentChild);
                
                // see if this is a stop node - if so end propogation
                if (child instanceof StopNode)
                {
                    // hmm how to break the recursive call
                    propMissionTreeStop = true;
                    child.execute(ephemeris); // execute stop (if some clean up needed)
                    
                    // temp print out ephemeris
                    if(debug)
                    {
                        printEphemeris();  // DEBUG print out ephemeris
                    }
                    
                    return;
                } // if stop
                
                child.execute(ephemeris); // run the child
                   
            } // for each child
        } // no stop hit
        
        
        
        // removing recursive tree calling 
//        if(!propMissionTreeStop)
//        {
//            int cc;
//            cc = treeTableModel.getChildCount(o);
//            for (int i = 0; i < cc; i++)
//            {
//                CustomTreeTableNode child = (CustomTreeTableNode) treeTableModel.getChild(o, i);
//                
//                // set message to name
//                currentMessage = child.getValueAt(0).toString();
//                
//                currentChild++;
//                
//                publish(currentChild);
//                
//                // see if this is a stop node - if so end propogation
//                if (child instanceof StopNode)
//                {
//                    // hmm how to break the recursive call
//                    propMissionTreeStop = true;
//                    child.execute(ephemeris); // execute stop (if some clean up needed)
//                    
//                    // temp print out ephemeris
//                    if(debug)
//                    {
//                        printEphemeris();  // DEBUG print out ephemeris
//                    }
//                    
//                    return;
//                }
//
//                if (treeTableModel.isLeaf(child))
//                {
//                    //System.out.println(child.getValueAt(0).toString());
//                    child.execute(ephemeris);
//
//                }
//                else
//                {
//                    //System.out.println(child.getValueAt(0).toString());
//                    child.execute(ephemeris);
//
//                    // now travese its children
//                    propMissionTree(child);
//                }
//            } // for each child
//        } // mission stop?
        
        
    } // propMissionTree
    
    private void countMissionTreeNodes(Object o)
    {
       
        childCount = rootNode.getChildCount();
        
        // not useing recursive execution here anymore (solvers will run their own children)
        
//        if(!propMissionTreeStop)
//        {
//            int cc;
//            cc = treeTableModel.getChildCount(o);
//            for (int i = 0; i < cc; i++)
//            {
//                CustomTreeTableNode child = (CustomTreeTableNode) treeTableModel.getChild(o, i);
//                
//                childCount++;
//                
//                // see if this is a stop node - if so end propogation
//                if (child instanceof StopNode)
//                {
//                    // hmm how to break the recursive call
//                    propMissionTreeStop = true;
//                                       
//                    return;
//                }
//
//                if (treeTableModel.isLeaf(child))
//                {
//
//                }
//                else
//                {
//                   
//                    // now travese its children
//                    propMissionTree(child);
//                }
//            } // for each child
//        } // mission stop?
        
        
    } // countMissionTreeNodes
    
    
    // goes throug tree recursively and looks for solver nodes and saves all thier variables
    private void saveAllVariables(Object o)
    {
       
        
        if(!propMissionTreeStop)
        {
            int cc;
            cc = treeTableModel.getChildCount(o);
            for (int i = 0; i < cc; i++)
            {
                CustomTreeTableNode child = (CustomTreeTableNode) treeTableModel.getChild(o, i);
                
                
                // see if this is a stop node - if so end propogation
                if (child instanceof StopNode)
                {
                    // hmm how to break the recursive call
                    propMissionTreeStop = true;   
                    
                    return;
                }

                if (treeTableModel.isLeaf(child))
                {

                }
                else
                {
                   if( child instanceof SolverNode)
                   {
                       for(InputVariable iv :((SolverNode)child).getInputVarVec())
                       {
                           iv.saveCurrentToPreviousValue(); // save value
                       }
                   }
                       
                    // now travese its children
                    saveAllVariables(child);
                }
            } // for each child
        } // mission stop?
        
        
    } // save all vars
    
    
    // debug function to print epeheris
    private void printEphemeris()
    {
        System.out.println("t, x, y, z, dx, dy, dz");
        System.out.println("===============================");
        
        for(StateVector sv : ephemeris)
        {
            System.out.println(sv.state[0] + ", " +sv.state[1] + ", " +sv.state[2] + ", " +sv.state[3] + ", " +sv.state[4] + ", " +sv.state[5] + ", " +sv.state[6]);
        }
        
        System.out.println("===============================");
        
    } // printEphemeris
    
       
    
} // MissionDesignPropagator