/*
 * JCustomSatConfigPanel.java
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
 * Created on January 7, 2008, 1:25 PM
 */
package jsattrak.gui;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;
import javax.swing.JFileChooser;
import jsattrak.customsat.InputVariable;
import jsattrak.customsat.ManeuverNode;
import jsattrak.customsat.PropogatorNode;
import jsattrak.customsat.SolverNode;
import jsattrak.customsat.StopNode;
import jsattrak.customsat.swingworker.MissionDesignPropagator;
import jsattrak.objects.CustomSatellite;
import jsattrak.utilities.CustomFileFilter;
import jsattrak.utilities.StateVector;
import name.gano.astro.AstroConst;
import name.gano.astro.time.Time;
import name.gano.swingx.treetable.CustomTreeTableNode;
import name.gano.swingx.treetable.IconTreeTableNodeRenderer;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.treetable.DefaultTreeTableModel;

/**
 *
 * @author  sgano
 */
public class JCustomSatConfigPanel extends javax.swing.JPanel
{

    DefaultTreeTableModel treeTableModel; // any TreeTableModel
    CustomTreeTableNode rootNode; // root node in mission designer treetable
    CustomSatellite sat;
    private Vector<StateVector> ephemeris;
    JSatTrak app; // used to add message etc.

    /** Creates new form JCustomSatConfigPanel */
    public JCustomSatConfigPanel(CustomSatellite sat, Vector<StateVector> ephemeris, JSatTrak app)
    {
        this.treeTableModel = sat.getMissionTableModel(); //treeTableModel;
        this.ephemeris = ephemeris;
        this.app = app;
        this.sat = sat;

        initComponents();

        missionDesignJXTreeTable.setTreeTableModel(treeTableModel);
        missionDesignJXTreeTable.setRootVisible(true);

        // get root node
        rootNode = (CustomTreeTableNode) treeTableModel.getRoot();

        // highlighter --same as old AlternateRowHighlighter.genericGrey
        missionDesignJXTreeTable.addHighlighter(new ColorHighlighter(HighlightPredicate.EVEN, Color.WHITE, Color.black)); // even, background, foregrond
        missionDesignJXTreeTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ODD, new Color(229, 229, 229), Color.black)); // even, background, foregrond
        // old way 
        //missionDesignJXTreeTable.addHighlighter(AlternateRowHighlighter.genericGrey);
        
        // roll over effect? (not supported anymore in swingx?)
        //missionDesignJXTreeTable.addHighlighter(new RolloverHighlighter(Color.BLACK, Color.WHITE));
        //missionDesignJXTreeTable.setRolloverEnabled(true);

        // expand all nodes at first
        missionDesignJXTreeTable.expandAll(); // expand all (for now) - for some reason stops the ability to add components to treetable
        missionDesignJXTreeTable.setColumnControlVisible(true); // column control

        // for custom icons rendering
        missionDesignJXTreeTable.setTreeCellRenderer(new IconTreeTableNodeRenderer());

        // console option
        consoleCheckBox.setSelected(sat.isShowConsoleOnPropogate());


    }

    public void packTableCols()
    {
    // is this working?
    //missionDesignJXTreeTable.doLayout();
    //missionDesignJXTreeTable.packAll();
    //missionDesignJXTreeTable.sizeColumnsToFit(0);
    }

    public void addNode2MissionDesigner(CustomTreeTableNode node)
    {
        // see if something is selected
        if (missionDesignJXTreeTable.getSelectedRow() >= 0)
        {
            // get selected object                  
            CustomTreeTableNode selectedNode = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();

            // make sure selected node not root
            if (selectedNode == ((CustomTreeTableNode) treeTableModel.getRoot()))
            {
                // add new node to begining of root after ini conditions
                treeTableModel.insertNodeInto(node, (CustomTreeTableNode) treeTableModel.getRoot(), 1);
            }
            else
            {
                // get parent of selected object
                CustomTreeTableNode selectedNodeParent = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getParentPath().getLastPathComponent();
                // get index of selected object from parent
                int childIndex = treeTableModel.getIndexOfChild(selectedNodeParent, selectedNode);

                // add new node after the selected one
                treeTableModel.insertNodeInto(node, selectedNodeParent, childIndex + 1);
            }

        }
        else
        {
            // add node to the end of root node
            treeTableModel.insertNodeInto(node, rootNode, rootNode.getChildCount());
        }


    } // addNode2MissionDesigner

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        addPropButton = new javax.swing.JButton();
        addBurnButton = new javax.swing.JButton();
        addSolverButton = new javax.swing.JButton();
        propMissionButton = new javax.swing.JButton();
        consoleCheckBox = new javax.swing.JCheckBox();
        exportEphemerisButton = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        missionDesignJXTreeTable = new org.jdesktop.swingx.JXTreeTable();
        jPanel6 = new javax.swing.JPanel();
        upNodeButton = new javax.swing.JButton();
        downNodeButton = new javax.swing.JButton();
        deleteNodeButton = new javax.swing.JButton();
        showNodeSettingsButton = new javax.swing.JButton();
        jump2NodeStartButton = new javax.swing.JButton();
        revertParamValuesButton = new javax.swing.JButton();

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel1.setText("Custom Satellite Configuration");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/Orbit.png"))); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 184, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel2))
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 27, Short.MAX_VALUE))
                .addContainerGap())
        );

        jLabel3.setText("Mission Designer:");

        addPropButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/customSatIcons/prop.png"))); // NOI18N
        addPropButton.setToolTipText("Add propogation node");
        addPropButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addPropButtonActionPerformed(evt);
            }
        });

        addBurnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/customSatIcons/burn.png"))); // NOI18N
        addBurnButton.setToolTipText("Add maneuver node");
        addBurnButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addBurnButtonActionPerformed(evt);
            }
        });

        addSolverButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/customSatIcons/solver.png"))); // NOI18N
        addSolverButton.setToolTipText("Add solver node");
        addSolverButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addSolverButtonActionPerformed(evt);
            }
        });

        propMissionButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/Play16.gif"))); // NOI18N
        propMissionButton.setToolTipText("Propogate Mission");
        propMissionButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                propMissionButtonActionPerformed(evt);
            }
        });

        consoleCheckBox.setText("Show console on execution");
        consoleCheckBox.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                consoleCheckBoxActionPerformed(evt);
            }
        });

        exportEphemerisButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-revert.png"))); // NOI18N
        exportEphemerisButton.setToolTipText("Export Ephemeris");
        exportEphemerisButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                exportEphemerisButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel3)
                        .addGap(105, 105, 105)
                        .addComponent(addPropButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addBurnButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addSolverButton))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(propMissionButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(consoleCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 126, Short.MAX_VALUE)
                        .addComponent(exportEphemerisButton)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addPropButton)
                        .addComponent(jLabel3))
                    .addComponent(addBurnButton)
                    .addComponent(addSolverButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(propMissionButton)
                        .addComponent(consoleCheckBox))
                    .addComponent(exportEphemerisButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setLayout(new java.awt.BorderLayout());

        missionDesignJXTreeTable.setRootVisible(true);
        missionDesignJXTreeTable.setSelectionBackground(new java.awt.Color(153, 204, 255));
        jScrollPane1.setViewportView(missionDesignJXTreeTable);

        jPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        upNodeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/Up16.gif"))); // NOI18N
        upNodeButton.setToolTipText("Move Node Up");
        upNodeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                upNodeButtonActionPerformed(evt);
            }
        });

        downNodeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/Down16.gif"))); // NOI18N
        downNodeButton.setToolTipText("Move Node Down");
        downNodeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                downNodeButtonActionPerformed(evt);
            }
        });

        deleteNodeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/Delete16.gif"))); // NOI18N
        deleteNodeButton.setToolTipText("Delete Node");
        deleteNodeButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                deleteNodeButtonActionPerformed(evt);
            }
        });

        showNodeSettingsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/preferences-desktop.png"))); // NOI18N
        showNodeSettingsButton.setToolTipText("Display Selected Node's Settings");
        showNodeSettingsButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                showNodeSettingsButtonActionPerformed(evt);
            }
        });

        jump2NodeStartButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/StepForward16.gif"))); // NOI18N
        jump2NodeStartButton.setToolTipText("Jump to Time Node Begins");
        jump2NodeStartButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jump2NodeStartButtonActionPerformed(evt);
            }
        });

        revertParamValuesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/gtk-revert-to-saved.png"))); // NOI18N
        revertParamValuesButton.setToolTipText("Reset Variables to Previous Values (Entire Mission)");
        revertParamValuesButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                revertParamValuesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(upNodeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(downNodeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deleteNodeButton)
                .addGap(28, 28, 28)
                .addComponent(showNodeSettingsButton)
                .addGap(28, 28, 28)
                .addComponent(jump2NodeStartButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(revertParamValuesButton)
                .addContainerGap(17, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(deleteNodeButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(downNodeButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(upNodeButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jump2NodeStartButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(revertParamValuesButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(showNodeSettingsButton, javax.swing.GroupLayout.Alignment.TRAILING)))
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jTabbedPane1.addTab("Propogation", jPanel3);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    private void addPropButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addPropButtonActionPerformed
    {//GEN-HEADEREND:event_addPropButtonActionPerformed
        addNode2MissionDesigner(new PropogatorNode(null));
}//GEN-LAST:event_addPropButtonActionPerformed

    private void addBurnButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addBurnButtonActionPerformed
    {//GEN-HEADEREND:event_addBurnButtonActionPerformed
        addNode2MissionDesigner(new ManeuverNode(null));
}//GEN-LAST:event_addBurnButtonActionPerformed

    private void addSolverButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addSolverButtonActionPerformed
    {//GEN-HEADEREND:event_addSolverButtonActionPerformed
        addNode2MissionDesigner(new SolverNode(null, true)); // add default objects
    }//GEN-LAST:event_addSolverButtonActionPerformed

    private void deleteNodeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteNodeButtonActionPerformed
    {//GEN-HEADEREND:event_deleteNodeButtonActionPerformed
        // delete selected node if not STOP or INI or ROOT or LOOP
        if (missionDesignJXTreeTable.getSelectedRow() >= 0) // make sure something selected
        {
            // get selected object                  
            CustomTreeTableNode selectedNode = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();

            // make sure selected node not root
            if (selectedNode != ((CustomTreeTableNode) treeTableModel.getRoot()))
            {
                if (!selectedNode.getNodeType().equalsIgnoreCase("Initial Conditions") && !selectedNode.getNodeType().equalsIgnoreCase("Stop") && !selectedNode.getNodeType().equalsIgnoreCase("Loop"))
                {
                    // okay to delete
                    treeTableModel.removeNodeFromParent(selectedNode);
                }
            }
        }
    }//GEN-LAST:event_deleteNodeButtonActionPerformed

    private void upNodeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_upNodeButtonActionPerformed
    {//GEN-HEADEREND:event_upNodeButtonActionPerformed
        // move node up if not root or INI -- also move if node above is a solver add it as a child to it
        // if node above is INI leave it alone


        if (missionDesignJXTreeTable.getSelectedRow() >= 0) // make sure something selected
        {
            // get selected object                  
            CustomTreeTableNode selectedNode = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();

            int selectionIndex = missionDesignJXTreeTable.getSelectionModel().getMinSelectionIndex();

            // make sure selected node not root
            if (selectedNode != ((CustomTreeTableNode) treeTableModel.getRoot()) && !selectedNode.getNodeType().equalsIgnoreCase("Initial Conditions"))
            {
                // get parent of selected object
                CustomTreeTableNode selectedNodeParent = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getParentPath().getLastPathComponent();

                // get index of selected object from parent
                int childIndex = treeTableModel.getIndexOfChild(selectedNodeParent, selectedNode);

                // if index not 0, get node above, else if 0 and parent not root, get parents parents last node
                if (childIndex != 0)
                {
                    // get node that is above the selected one
                    CustomTreeTableNode nodeAbove = (CustomTreeTableNode) treeTableModel.getChild(selectedNodeParent, childIndex - 1);

                    if (!nodeAbove.getNodeType().equalsIgnoreCase("Initial Conditions"))
                    {

                        // not below INI - so move it up
                        treeTableModel.removeNodeFromParent(selectedNode); // remove node
                        treeTableModel.insertNodeInto(selectedNode, selectedNodeParent, childIndex - 1);
                    // reselect node  TODO 
                    //?

                    // sorta works unless below a node with children that is open
                    //missionDesignJXTreeTable.getSelectionModel().addSelectionInterval(selectionIndex - 1, selectionIndex - 1);


                    } // not below ini


                } // chile index not 0


            } // not root or an ini node
        } // something selected
        
    }//GEN-LAST:event_upNodeButtonActionPerformed

    private void downNodeButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_downNodeButtonActionPerformed
    {//GEN-HEADEREND:event_downNodeButtonActionPerformed
        if (missionDesignJXTreeTable.getSelectedRow() >= 0) // make sure something selected
        {
            // get selected object                  
            CustomTreeTableNode selectedNode = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();

            int selectionIndex = missionDesignJXTreeTable.getSelectionModel().getMinSelectionIndex();

            // make sure selected node not root
            if (selectedNode != ((CustomTreeTableNode) treeTableModel.getRoot()) && !selectedNode.getNodeType().equalsIgnoreCase("Initial Conditions"))
            {
                // get parent of selected object
                CustomTreeTableNode selectedNodeParent = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getParentPath().getLastPathComponent();

                // get index of selected object from parent
                int childIndex = treeTableModel.getIndexOfChild(selectedNodeParent, selectedNode);

                // if index not last node, get node below
                if (childIndex + 1 != selectedNodeParent.getChildCount())
                {
                    // get node that is above the selected one
                    //CustomTreeTableNode nodeAbove = (CustomTreeTableNode) treeTableModel.getChild(selectedNodeParent, childIndex + 1);

//                   
                    // not below INI - so move it down
                    treeTableModel.removeNodeFromParent(selectedNode); // remove node
                    treeTableModel.insertNodeInto(selectedNode, selectedNodeParent, childIndex + 1);

                // reselect node  TODO 
                //?

                // sorta works unless below a node with children that is open
                //missionDesignJXTreeTable.getSelectionModel().addSelectionInterval(selectionIndex - 1, selectionIndex - 1);


                } // chile index not 0


            } // not root or an ini node
        } // something selected
    }//GEN-LAST:event_downNodeButtonActionPerformed

    private void propMissionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_propMissionButtonActionPerformed
    {//GEN-HEADEREND:event_propMissionButtonActionPerformed
        // for now just go through and print out names of each element in order - traversing all children
        // propbably use recursive calling of a function

        // need to turn on the status bar animation
        app.startStatusAnimation();
        app.setStatusProgressBarValue(0);
        app.setStatusProgressBarText("");
        app.setStatusProgressBarVisible(true);

        // console dialog
        if (consoleCheckBox.isSelected())
        {
            app.showConsoleWindow(true);
        }

        // thread to run in background
        MissionDesignPropagator mdp = new MissionDesignPropagator(rootNode, treeTableModel, ephemeris, sat, app);
        mdp.execute();
        
    }//GEN-LAST:event_propMissionButtonActionPerformed

    private void showNodeSettingsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showNodeSettingsButtonActionPerformed
    {//GEN-HEADEREND:event_showNodeSettingsButtonActionPerformed
        // get selected node (or first selected if any are selected)
        if (missionDesignJXTreeTable.getSelectedRow() >= 0)
        {
            CustomTreeTableNode selectedNode = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();

            // display settings
            selectedNode.displaySettings(app);
        }
    }//GEN-LAST:event_showNodeSettingsButtonActionPerformed

    private void jump2NodeStartButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jump2NodeStartButtonActionPerformed
    {//GEN-HEADEREND:event_jump2NodeStartButtonActionPerformed
        // first see if a node is selected
        if (missionDesignJXTreeTable.getSelectedRow() >= 0) // make sure something selected
        {
            // get selected object                  
            CustomTreeTableNode selectedNode = (CustomTreeTableNode) missionDesignJXTreeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();

            double nodeStartTime = selectedNode.getStartTTjulDate();

            if (nodeStartTime > 0) // if it has a valid value
            {
                // convert time to UTC
                double deltaTT2UTC = Time.deltaT(nodeStartTime - AstroConst.JDminusMJD); // = TT - UTC
                Time n = new Time();

                // set app time
                app.setTime(nodeStartTime - deltaTT2UTC);
                
                // force repaint and regeneration of ground paths - SEG 24 Sept 2008
                sat.setGroundTrackIni2False(); //force update of ground track data
                app.forceRepainting(false); // repait without updating positional data 
                

            } // valid value
        } // node selected
    }//GEN-LAST:event_jump2NodeStartButtonActionPerformed

    private void revertParamValuesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_revertParamValuesButtonActionPerformed
    {//GEN-HEADEREND:event_revertParamValuesButtonActionPerformed
        // revert all parameter values back to previous values
        resetAllVariables(rootNode);
        
    }//GEN-LAST:event_revertParamValuesButtonActionPerformed

    private void consoleCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_consoleCheckBoxActionPerformed
    {//GEN-HEADEREND:event_consoleCheckBoxActionPerformed
        // save option to sat
        sat.setShowConsoleOnPropogate(consoleCheckBox.isSelected());
    }//GEN-LAST:event_consoleCheckBoxActionPerformed

    private void exportEphemerisButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exportEphemerisButtonActionPerformed
    {//GEN-HEADEREND:event_exportEphemerisButtonActionPerformed

        // open a save as dialog to save the file .e extension (default to file saved dir)
        final JFileChooser fc = new JFileChooser(app.getFileSaveAs());
        CustomFileFilter xmlFilter = new CustomFileFilter("e", "*.e (STK Ephemeris Format)");
        fc.addChoosableFileFilter(xmlFilter);

        int returnVal = fc.showSaveDialog(app);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();

            String extension = app.getExtension(file);
            String fileString = file.getAbsolutePath();
            // force .e extension
            if (extension != null)
            {
                fileString = file.getAbsolutePath();
            }
            else
            {
                // append the extension
                fileString = (new File(file.getAbsolutePath() + ".e")).getAbsolutePath();
            }


            //  export
            if (exportEphemeris2File(fileString))
            {
                app.setStatusMessage(sat.getName() + " ephemeris exported to " + fileString);
            }

        } // file selected

      
    }//GEN-LAST:event_exportEphemerisButtonActionPerformed

    // returns if successful
    public boolean exportEphemeris2File(String filename)
    {
        // output settings - can be parameters later if needed
        String stkInterpMethod = "Lagrange";
        int stkInterpSamp = 7;
        String stkCentralBody = "Earth";
        String stkCoordSys = "J2000";

        DecimalFormat d12 = new DecimalFormat("0.00000000000E0"); // display format

        // create scenario epoch time in STK format
        // scenario epoch using Gregorian UTC time (dd mmm yyyy hh:mm:ss.s)
        String stkEpoch = "";
        if(ephemeris.size() > 0)
        {
            double deltaTT2UTC = Time.deltaT(ephemeris.firstElement().state[0] - AstroConst.JDminusMJD); // = TT - UTC
            GregorianCalendar cal = Time.convertJD2Calendar(ephemeris.firstElement().state[0] - deltaTT2UTC);
            //cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat dateformatShort1 = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
            dateformatShort1.setTimeZone(TimeZone.getTimeZone("UTC"));
            stkEpoch = dateformatShort1.format(cal.getTime()); 
        }

        // open file for writing
        BufferedWriter buffWriter;
        try
        {
            buffWriter = new BufferedWriter(new FileWriter(filename));
            //	add stk header
            buffWriter.write("stk.v.4.0\n\n");
            buffWriter.write("BEGIN Ephemeris\n\n");
            buffWriter.write("NumberOfEphemerisPoints " + ephemeris.size() + "\n\n");
            buffWriter.write("ScenarioEpoch           " + stkEpoch + "\n\n"); // scenario epoch using Gregorian UTC time (dd mmm yyyy hh:mm:ss.s). 
            buffWriter.write("InterpolationMethod     " + stkInterpMethod + "\n\n");
            buffWriter.write("InterpolationSamplesM1  " + stkInterpSamp + "\n\n");
            buffWriter.write("CentralBody             " + stkCentralBody + "\n\n");
            buffWriter.write("CoordinateSystem        " + stkCoordSys + "\n\n");
            buffWriter.write("EphemerisTimePosVel\n\n");

            // write each epheris line - time should range from 0 to n seconds
            double time = 0;
            for (StateVector sv : ephemeris)
            {
                time = (sv.state[0] - ephemeris.firstElement().state[0])*24.0*60.0*60.0 ; // seconds since start
                buffWriter.write(d12.format(time) + " " + d12.format(sv.state[1]) + " " + d12.format(sv.state[2]) + " " + d12.format(sv.state[3]) + " " + d12.format(sv.state[4]) + " " + d12.format(sv.state[5]) + " " + d12.format(sv.state[6]) + "\n");
            }
            
            buffWriter.write("\nEND Ephemeris\n\n");
            buffWriter.close();
                        
        }
        catch (Exception e)
        {
            System.out.println("Error Writing to Ephemeris Output File: " + e.toString());
            return false;
        }


        return true;
    } // exportEphemeris2File

    // goes throug tree recursively and looks for solver nodes and saves all thier variables
    private void resetAllVariables(Object o)
    {

        boolean propMissionTreeStop = false;

        if (!propMissionTreeStop)
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
                    if (child instanceof SolverNode)
                    {
                        for (InputVariable iv : ((SolverNode) child).getInputVarVec())
                        {
                            iv.setPrevioustoCurrentValue(); // revert value
                        }
                    }

                    // now travese its children
                    resetAllVariables(child);
                }
            } // for each child
        } // mission stop?


    } // revert variables
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addBurnButton;
    private javax.swing.JButton addPropButton;
    private javax.swing.JButton addSolverButton;
    private javax.swing.JCheckBox consoleCheckBox;
    private javax.swing.JButton deleteNodeButton;
    private javax.swing.JButton downNodeButton;
    private javax.swing.JButton exportEphemerisButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JButton jump2NodeStartButton;
    private org.jdesktop.swingx.JXTreeTable missionDesignJXTreeTable;
    private javax.swing.JButton propMissionButton;
    private javax.swing.JButton revertParamValuesButton;
    private javax.swing.JButton showNodeSettingsButton;
    private javax.swing.JButton upNodeButton;
    // End of variables declaration//GEN-END:variables
}
