/*
 * ProgressBarWorker.java
 *=====================================================================
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
 * Created on August 22, 2007, 1:23 PM
 *
 */

package jsattrak.utilities;

import jsattrak.gui.JProgressDialog;
import jsattrak.gui.LoadTleDirectDialog;
import java.awt.Frame;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author sgano
 */
public class SatBrowserTleDataLoader extends SwingWorker
{
    JProgressDialog jProgDialog = null;
    boolean dialogCreated = false;
    
    //JProgressDialog dialog;
    
    boolean notDone = true; // not done yet
    
    //
    DefaultMutableTreeNode topTreeNode;
    private Hashtable<String,TLE> tleHash;
    Frame parentComponent;
    JTextArea tleOutputTextArea;
    JTree satTree;

    // node hash
    Hashtable<String,DefaultMutableTreeNode> mainNodesHash;
    Hashtable<String,DefaultMutableTreeNode> secondaryNodesHash;
    
    
    /** Creates a new instance of ProgressBarWorker
     * @param parentComponent
     * @param topTreeNode
     * @param tleHash
     * @param tleOutputTextArea
     * @param satTree 
     */
    public SatBrowserTleDataLoader(Frame parentComponent, DefaultMutableTreeNode topTreeNode, Hashtable<String,TLE> tleHash, JTextArea tleOutputTextArea, JTree satTree)
    {
        this.topTreeNode = topTreeNode;
        this.tleHash = tleHash;
        this.parentComponent = parentComponent;
        this.tleOutputTextArea = tleOutputTextArea;
        this.satTree = satTree;
        
    }
    

    /**
     * 
     * @return
     */
    @Override
    public Boolean doInBackground()
    {
        boolean result = true;
        
        dialogCreated = true;
       
        //=================================================================================================
        
        
        TLEDownloader tleDownloader = new TLEDownloader();
        
        // create a hashmap of top level nodes
        mainNodesHash = new Hashtable<String,DefaultMutableTreeNode>();
        secondaryNodesHash = new Hashtable<String,DefaultMutableTreeNode>();
    
        // iterate for each primary category, only create node when it is different than item before
        
        // current main node we are working with
        DefaultMutableTreeNode currentMainNode = topTreeNode;
        DefaultMutableTreeNode currentSecondaryNode;
        
        // current TLE
        TLE currentTLE = null;
        
        int satCount = 0;
        
        
        // Check to see if TLE directory exisits if not ask user if they want to download them from the web
        // no Local TLE were found would you like to download them directly from the web?
        boolean loadTLEfromWeb = false;
        //ProgressBarWorker worker = null;
        //dialog  = new JProgressDialog(parentComponent, true);
        if(parentComponent == null)
        {
            System.out.println("2 JProgress Dialog Parent == NULL" );
        }
        
        final JProgressDialog dialog = new JProgressDialog(parentComponent, false);
        if( !(new File(tleDownloader.getLocalPath()).exists()) ||  !(new File(tleDownloader.getTleFilePath(0)).exists()) )
        {
            // ask user if they want to load them from the web: (and tab for proxy options)
            // tell them it may take longer to open
            LoadTleDirectDialog dlg = new LoadTleDirectDialog(parentComponent, true); // modal so it waits for the user to respond
            dlg.setVisible(true);
            
            
            // if they want to load them from the web: 
            if(dlg.isWasYesSelected())
            {
                loadTLEfromWeb = true; // must load them
                
                // maybe pop open a progress bar here??
                // create a swing worker thread to download data from
                //worker = new ProgressBarWorker(parent);
                //worker.execute();
                //dialog = new JProgressDialog(parent, false);
                dialog.setVisible(true);
                
                
                
            }
            else
            {
                return new Boolean(result); // return as if nothing was wrong but no sat browswer will be shown
            }
            
        } // TLE files exisit?

        

                
        // Assumes each satellite TLE has a unique name
        for(int i=0; i<tleDownloader.fileNames.length; i++)
        {
            
            // see if the primary category exisits
            if(mainNodesHash.containsKey(tleDownloader.primCat[i]))
            {
                // do not add another main, get the already exisiting node
                currentMainNode = mainNodesHash.get(tleDownloader.primCat[i]);
            }
            else
            {
                // create a new main node and add it to the hashmap
                currentMainNode = new DefaultMutableTreeNode(tleDownloader.primCat[i]);
                mainNodesHash.put(tleDownloader.primCat[i],currentMainNode);
                // add node to the top node
                topTreeNode.add(currentMainNode);
            }
            
            // okay, now add a new secondary node to the main node (assume it is not already there, files are uniquly named)
            currentSecondaryNode = new DefaultMutableTreeNode(tleDownloader.secondCat[i]);
            currentMainNode.add(currentSecondaryNode);
            secondaryNodesHash.put(tleDownloader.secondCat[i],currentSecondaryNode); // save to keep track
            
                        
            // now parse through the textfile and create a TLE for each entry
            // add each TLE to the hashmap and to the JTree
            try
            {
                BufferedReader tleReader = null; // initalization of reader (either local or web)
                
                if(!loadTLEfromWeb)
                {
                    // read local file
                    File tleFile = new File(tleDownloader.getTleFilePath(i));
                    FileReader tleFileReader = new FileReader(tleFile);
                    tleReader = new BufferedReader(tleFileReader); // from local file
                }
                else
                {
                    // does this work??
                    if(!dialog.isVisible()) // if progress bar is close cancel this operation
                    {
                        result = false;
                        return new Boolean(result);
                    }
                    
                    // read from web
                    URL url = new URL(tleDownloader.getTleWebPath(i));
                    URLConnection c = url.openConnection();
                    InputStreamReader isr = new InputStreamReader(c.getInputStream());
                    tleReader = new BufferedReader(isr); // from the web
                    
                    // update progress?
                    dialog.setProgress( (int) Math.round( (i*100.0)/ tleDownloader.fileNames.length) );
                    dialog.repaint();
                    dialog.setStatusText(tleDownloader.fileNames[i]);
                    
                    
                                       
                }
                
                
                
                String nextLine = null;
                
                while( (nextLine = tleReader.readLine()) != null)
                {
                    // needs three lines
                    currentTLE = new TLE(nextLine,tleReader.readLine(),tleReader.readLine());
                    
                    // save TLE
                    tleHash.put(currentTLE.getSatName(),currentTLE);
                    
                    // add to tree
                    currentSecondaryNode.add( new DefaultMutableTreeNode(currentTLE.getSatName()) );
                    
                    satCount++;
                }// while there are more lines to read
                                
            }
            catch(Exception e)
            {
                // print out error
                System.out.println("ERROR IN TLE READING POSSIBLE FILE FORMAT OR MISSING TLE FILES:" + e.toString());
                e.printStackTrace();
                // the next line cause the app to hang
                //JOptionPane.showMessageDialog(parentComponent, "Error Loading Satellite TLE Data. Try updating data.\n"+e.toString(), "TLE LOADING ERROR", JOptionPane.ERROR_MESSAGE);
                
                result = false;
                return new Boolean(result); // quit, so user doesn't get tons of error messages
            }
  
            
            
        } // for each primary category
        
        if(loadTLEfromWeb)
        {
            dialog.setVisible(false); // close progress dialog
        }
        
        
        // display number of satellites in list
        tleOutputTextArea.setText("Number of Satellites in list: "+satCount);
        
               
        // auto expand root node
        satTree.expandRow(0);
        
        //=================================================================================================
        
        //System.out.println("I am done");
        
        return new Boolean(result);
        
    }
    
    public void setProg(int percent)
    {
        jProgDialog.setProgress(percent);
    }


    // =========================================================

    // SEG - 11 Jan 2009 - so custom files can be loaded
    /**
     * loads a given file and adds the TLE's to the sat Browser
     * @param tleFile file containing TLE's (3line format, (1) name, (2) TLE line 1 (3) line 2
     * @param primaryCategory Primary list category to display the satellites under
     * @param secondaryCategory secondary category to display the satellites under (can be null)
     * @return if loading of TLE file was successful
     */
    public Boolean loadTLEDataFile(File tleFile, String primaryCategory, String secondaryCategory)
    {
        boolean result = true;
        int newSatCount = 0;

        // current TLE
        TLE currentTLE = null;

        // Assumes each satellite TLE has a unique name
//        for (int i = 0; i < tleDownloader.fileNames.length; i++)
//        {

        DefaultMutableTreeNode currentMainNode = topTreeNode;
        DefaultMutableTreeNode currentSecondaryNode;

            // see if the primary category exisits
            if (mainNodesHash.containsKey(primaryCategory))
            {
                // do not add another main, get the already exisiting node
                currentMainNode = mainNodesHash.get(primaryCategory);
            }
            else
            {
                // create a new main node and add it to the hashmap
                currentMainNode = new DefaultMutableTreeNode(primaryCategory);
                mainNodesHash.put(primaryCategory, currentMainNode);
                // add node to the top node
                topTreeNode.add(currentMainNode);
            }


            // see if the secondary category exisits
        if(secondaryCategory != null)
        {
            if (secondaryNodesHash.containsKey(secondaryCategory))
            {
                // do not add another main, get the already exisiting node
                currentSecondaryNode = secondaryNodesHash.get(secondaryCategory);
            }
            else
            {
                // create a new main node and add it to the hashmap
                currentSecondaryNode = new DefaultMutableTreeNode(secondaryCategory);
                secondaryNodesHash.put(secondaryCategory, currentSecondaryNode);
                // add node to the top node
                currentMainNode.add(currentSecondaryNode);
            }
        }
        else // secondary categort is null
        {
            currentSecondaryNode = currentMainNode; // just point to main node
        }




            // now parse through the textfile and create a TLE for each entry
            // add each TLE to the hashmap and to the JTree
            try
            {
                BufferedReader tleReader = null; // initalization of reader (either local or web)

//                if (!loadTLEfromWeb)
//                {
                    // read local file
                    //File tleFile = new File(tleDownloader.getTleFilePath(i));
                    FileReader tleFileReader = new FileReader(tleFile);
                    tleReader = new BufferedReader(tleFileReader); // from local file
//                }
//                else
//                {
//                    // does this work??
//                    if (!dialog.isVisible()) // if progress bar is close cancel this operation
//                    {
//                        result = false;
//                        return new Boolean(result);
//                    }
//
//                    // read from web
//                    URL url = new URL(tleDownloader.getTleWebPath(i));
//                    URLConnection c = url.openConnection();
//                    InputStreamReader isr = new InputStreamReader(c.getInputStream());
//                    tleReader = new BufferedReader(isr); // from the web
//
//                    // update progress?
//                    dialog.setProgress((int) Math.round((i * 100.0) / tleDownloader.fileNames.length));
//                    dialog.repaint();
//                    dialog.setStatusText(tleDownloader.fileNames[i]);
//
//
//
//                }



                String nextLine = null;

                while ((nextLine = tleReader.readLine()) != null)
                {
                    // needs three lines
                    currentTLE = new TLE(nextLine, tleReader.readLine(), tleReader.readLine());

                    // save TLE
                    tleHash.put(currentTLE.getSatName(), currentTLE);

                    // add to tree
                    currentSecondaryNode.add(new DefaultMutableTreeNode(currentTLE.getSatName()));

                    newSatCount++;
                }// while there are more lines to read

            }
            catch (Exception e)
            {

                // print out error
                System.out.println("ERROR IN TLE READING POSSIBLE FILE FORMAT OR MISSING TLE FILES:" + e.toString());
                e.printStackTrace();
                // the next line cause the app to hang
                //JOptionPane.showMessageDialog(parentComponent, "Error Loading Satellite TLE Data. Try updating data.\n"+e.toString(), "TLE LOADING ERROR", JOptionPane.ERROR_MESSAGE);

                JOptionPane.showMessageDialog(parentComponent, "Error Loading TLE Data File, bad permissions or file format: \n" + e.toString() , "TLE LOADING ERROR", JOptionPane.ERROR_MESSAGE);

                result = false;
                return new Boolean(result); // quit, so user doesn't get tons of error messages
            }



        //} // for each primary category

      

        // tell tree that the data has been updated!
        ((DefaultTreeModel)satTree.getModel()).reload();

        // auto expand new Node and select it!
        satTree.expandPath( getTreePath(currentSecondaryNode) ); // currentSecondaryNode
        satTree.getSelectionModel().setSelectionPath( getTreePath(currentSecondaryNode) );

        // debug
        //satTree.expandRow(1);

         // display number of satellites in list
        tleOutputTextArea.setText("Number of New Satellites add to list: "+newSatCount);
        //satTree.repaint();

         return new Boolean(result); // quit, so user doesn't get tons of error messages
         
    } // loadTLEDataFile

    // Returns a TreePath containing the specified node.
    public TreePath getTreePath(TreeNode node)
    {
        List<TreeNode> list = new ArrayList<TreeNode>();

        // Add all nodes to list
        while (node != null)
        {
            list.add(node);
            node = node.getParent();
        }
        Collections.reverse(list);

        // Convert array of nodes to TreePath
        return new TreePath(list.toArray());
    }

    
}
