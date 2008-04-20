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
import java.util.Hashtable;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;

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
    

    public Boolean doInBackground()
    {
        boolean result = true;
        
        dialogCreated = true;
       
        //=================================================================================================
        
        
        TLEDownloader tleDownloader = new TLEDownloader();
        
        // create a hashmap of top level nodes
        Hashtable<String,DefaultMutableTreeNode> mainNodesHash = new Hashtable<String,DefaultMutableTreeNode>();
    
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
    
}
