/*
 * EphemerisFromFileNode.java
 * 
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
 * Created: 10 Jul 2009
 */

package jsattrak.customsat;

import java.awt.Toolkit;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import jsattrak.customsat.gui.EphemerisFromFilePanel;
import jsattrak.gui.JSatTrak;
import jsattrak.utilities.StateVector;
import name.gano.file.StkEphemerisReader;
import name.gano.swingx.treetable.CustomTreeTableNode;

/**
 *
 * @author Shawn E. Gano, shawn@gano.name
 */
public class EphemerisFromFileNode extends CustomTreeTableNode
{

    private String filename = "";

    public EphemerisFromFileNode(CustomTreeTableNode parentNode)
    {
        super(new String[] {"Ephemeris From File","",""}); // initialize node, default values

        // set icon for this type
        setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/customSatIcons/file.png")) ) );
        //set Node Type
        setNodeType("Ephemeris From File");

       
        // add this node to parent - last thing
        if( parentNode != null)
            parentNode.add(this);
    }


     // meant to be overridden by implementing classes
    @Override
    public void execute(Vector<StateVector> ephemeris)
    {
         // try to read from file -- if error report to log!  and continue!
        StkEphemerisReader r = new StkEphemerisReader();
        try
        {
            Vector<StateVector> e = r.readStkEphemeris(filename);
           
            for(StateVector sv : e)
            {
                // add state to ephemeris
                ephemeris.add(sv);
            }

            // set inital time for this node
            this.setStartTTjulDate(e.get(0).state[0]);

            System.out.println( " - Node:" + getValueAt(0) + ", Ephemeris Points Read: " + e.size() );

        }
        catch(Exception e)
        {
            System.out.println("Error Reading Ephemeris from File- Node:" + getValueAt(0) + ", message : " + e.toString());
        }

        

    }// execute


    // passes in main app to add the internal frame to
    public void displaySettings(JSatTrak app)
    {

        String windowName = "" + getValueAt(0);
        JInternalFrame iframe = new JInternalFrame(windowName,true,true,true,true);

        // show satellite browser window
        EphemerisFromFilePanel gsBrowser = new EphemerisFromFilePanel(this); // non-modal version
        gsBrowser.setIframe(iframe);

        iframe.setContentPane( gsBrowser );
        iframe.setSize(365,150); // w,h
        iframe.setLocation(5,5);

        app.addInternalFrame(iframe);
    }

    /**
     * @return the filename
     */
    public String getFilename()
    {
        return filename;
    }

    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

}
