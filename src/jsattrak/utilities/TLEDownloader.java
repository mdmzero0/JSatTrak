/**
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
 */
// package to download all new TLE files from web
// second version, updates progress via input gui items


package jsattrak.utilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

public class TLEDownloader implements java.io.Serializable
{
	// root URL for all TLEs
	String rootWeb = "http://celestrak.com/NORAD/elements/";
	
	// names of all TLE files to update
	public String[] fileNames = new String[] {
                        "sts.txt",
			"stations.txt",
                        "tle-new.txt", // added 26 Sept 2008 - SEG
                       // "1999-025.txt",
			"weather.txt",
			"noaa.txt",
			"goes.txt",
			"resource.txt",
			"sarsat.txt",
			"dmc.txt",
			"tdrss.txt",
			"geo.txt",
			"intelsat.txt",
			"gorizont.txt",
			"raduga.txt",
			"molniya.txt",
			"iridium.txt",
			"orbcomm.txt",
			"globalstar.txt",
			"amateur.txt",
			"x-comm.txt",
			"other-comm.txt",
			"gps-ops.txt",
			"glo-ops.txt",
			"galileo.txt",
			"sbas.txt",
			"nnss.txt",
			"musson.txt",
			"science.txt",
			"geodetic.txt",
			"engineering.txt",
			"education.txt",
			"military.txt",
			"radar.txt",
			"cubesat.txt",
			"other.txt",
			
	};
	
	// primary category for each TLE
	public String[] primCat = new String[] {
                        "Special-Interest",  //removed all : " Satellites" redundant
			"Special-Interest",
                        "Special-Interest", // new
                        //"Special-Interest Satellites",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Weather & Earth Resources",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Communications",
			"Navigation",
			"Navigation",
			"Navigation",
			"Navigation",
			"Navigation",
			"Navigation",
			"Scientific",
			"Scientific",
			"Scientific",
			"Scientific",
			"Miscellaneous",
			"Miscellaneous",
			"Miscellaneous",
			"Miscellaneous"			
	};
	
	// secondary category for each TLE
	public String[] secondCat = new String[] {
                        "STS",
			"International Space Station",
                        "Last 30 Days' Launches", // new
                        //"FENGYUN 1C Debris",
			"Weather",
			"NOAA",
			"GOES",
			"Earth Resources",
			"Search & Rescue (SARSAT)",
			"Disaster Monitoring",
			"Tracking and Data Relay Satellite System (TDRSS)",
			"Geostationary",
			"Intelsat",
			"Gorizont",
			"Raduga",
			"Molniya",
			"Iridium",
			"Orbcomm",
			"Globalstar",
			"Amateur Radio",
			"Experimental",
			"Other",
			"GPS Operational",
			"Glonass Operational",
			"Galileo",
			"Satellite-Based Augmentation System (WAAS/EGNOS/MSAS)",
			"Navy Navigation Satellite System (NNSS)",
			"Russian LEO Navigation",
			"Space & Earth Science",
			"Geodetic",
			"Engineering",
			"Education",
			"Miscellaneous Military",
			"Radar Calibration",
			"CubeSats",
			"Other"			
	};
	
	// local path to save files
	private String localPath = "data/tle/";
	
	// proxy info
	private String proxyHost = "proxy1.lmco.com";
	private String proxyPort = "80";
	private boolean usingProxy = false;
	
	// error text 
	String errorText = "";
        
        // GUI components to be updated and if in a gui
        boolean inGUI = false; 
	JProgressBar progBar;
        JLabel progLabel = null;
	
	// main constructor
	public TLEDownloader()
	{
				
	} //TLEDownloader
        
        // constructor for use in a GUI - with progress bar and text area
        public TLEDownloader(JProgressBar progBar, JLabel progLabel)
	{
            // we are in a GUI
            inGUI = true;
            
            this.progBar = progBar;
            this.progLabel = progLabel;
            
	} //TLEDownloader
	
	public boolean downloadTLEs()
	{
		boolean success = true; // flag to tell if everyting worked

		// for proxy stuff: (if needed)
		if(usingProxy)
		{
			Properties systemSettings = System.getProperties();
			systemSettings.put( "proxySet", "true" );
			systemSettings.put("http.proxyHost",proxyHost);
			systemSettings.put("http.proxyPort", proxyPort);
		}
                
                // first see if local directory exists if not try to create it
                if( !(new File(getLocalPath()).exists()) )
                {
                    // local directory doesn't exsist try to create it --
                    // Create a directory; all non-existent ancestor directories are
                    // automatically created
                    success = (new File(getLocalPath())).mkdirs();
                    if (!success)
                    {
                        // Directory creation failed
                        //JOptionPane.showMessageDialog(null, "Error Creating Local TLE Data Directory: Check File Permissions", "ERROR", JOptionPane.ERROR_MESSAGE);
                        errorText = "Error Creating Local TLE Data Directory: Check File Permissions";
                        return false; // return error
                    }
                } // check local path
		
		// put for loop around this
		for(int i = 0;i<fileNames.length;i++)
		//for(int i = 0;i<1;i++)
		{
                    if(inGUI)
                    {
                        
                        progBar.setValue( (int) Math.round( (i*100.0)/ fileNames.length) );
                        progBar.repaint();
                        progLabel.setText("Downloading File: " + fileNames[i]);  
                    }
                    
			try
			{
				// open file on the web
				URL url = new URL(rootWeb + fileNames[i]);
				URLConnection c = url.openConnection();
				InputStreamReader isr = new InputStreamReader(c.getInputStream());
				BufferedReader br = new BufferedReader(isr);

				// open file on local drive to save to
				File outFile = new File(localPath + fileNames[i]);
				BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

				// save file locally
				String currentLine = "";
				while ((currentLine=br.readLine()) != null) 
				{
					writer.write(currentLine);
					writer.newLine();   // Write system dependent end of line.
				}

				// close file and web connection
				br.close();
				writer.close();
                                
                                if(i == (fileNames.length-1)) // last one update to 100%
                                {
                                    progBar.setValue( 100 );
                                    progBar.repaint();
                                }

			}
			catch(Exception e)
			{
				System.out.println("Error Reading/Writing TLE - " + fileNames[i] + "\n" + e.toString());
				//e.printStackTrace();
				success = false;
				
				errorText = e.toString();
				
				return false; // return after first error, to prevent slow response
			}
		} // for each TLE file

		return success;

	} // downloadTLEs
	
	
	public String getErrorText()
	{
		return errorText;
	}
	
	
	// test driving main fuction
	 public static void main(String[] args) 
	 {
		 TLEDownloader td = new TLEDownloader();
		 boolean result = td.downloadTLEs();
		 
		 System.out.println("Update of TLEs was sucessful? : " + result);
		 
	 } // main
         
         public int getTleFileCount()
         {
             return fileNames.length;
         }
         
         public String getTleFilePath(int index)
         {
             return (localPath + fileNames[index]);
         }
         
         public String getTleWebPath(int index)
         {
             return rootWeb + fileNames[index];
         }
         
        
        public void setUsingProxy(boolean b)
        {
            this.usingProxy = b;
        }
        
        public boolean getUsingProxy()
        {
            return usingProxy;
        }
        
        public void setProxyPort(String portStr)
        {
            this.proxyPort = portStr;
        }
        
        public String getProxyPort()
        {
            return proxyPort;
        }
         
        public void setProxyHost(String proxyHost)
        {
            this.proxyHost = proxyHost;
        }
        
        public String getProxyHost()
        {
            return proxyHost;
        }

    public String getLocalPath()
    {
        return localPath;
    }

    public void setLocalPath(String localPath)
    {
        this.localPath = localPath;
    }
} // TLEDownloader
