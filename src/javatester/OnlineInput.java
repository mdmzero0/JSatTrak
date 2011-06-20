package javatester;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URL;
import java.io.*;

public class OnlineInput
{
    private String[] SatelliteName;
    private String[] EphemerisLocation;
    private String[] Co;
    private boolean[] ModelCentered;
    BufferedReader input;
	public OnlineInput(String path) throws IOException
	{	
		UpdateInput(path);
	}
        private void UpdateInput(String file) throws IOException
	{ //reads a file - updates user input

        try {
		URL url = new URL(file);
		InputStream in = url.openStream();
		input = new BufferedReader(new InputStreamReader(in));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OnlineInput.class.getName()).log(Level.SEVERE, null, ex);
        }
		String SatelliteNameLong = null;
		String EphemerisLocationLong = null;
		String ColorLong = null;
		String ModelCenteredLong = null;               
                String line = null;
		//read lines - assign to a single string- parse later into the arrays
		while ((line = input.readLine()) != null)
		{
			if (line.startsWith("satellitename"))
			{
				SatelliteNameLong = line.substring(14).trim();
			}
			else if (line.startsWith("ephemerislocation"))
			{
				EphemerisLocationLong = line.substring(18).trim();
			}
			else if (line.startsWith("get2Dsatcolor"))
			{
				ColorLong = line.substring(14).trim();
			}
			else if (line.startsWith("viewobject"))
			{
				ModelCenteredLong = line.substring(11).trim();
			}
		}
		//seperate long strings into smaller arrays
		SatelliteName = SatelliteNameLong.split(";");
		EphemerisLocation = EphemerisLocationLong.split(";");
		String[] ModelCenteredArray = ModelCenteredLong.split(";");
                ModelCentered = new boolean [ModelCenteredArray.length];
		//convert each string in array to boolean
		for (int i = 0; i < ModelCenteredArray.length; i++)
		{
			ModelCentered[i] = Boolean.parseBoolean(ModelCenteredArray[i]);
		}
		Co = ColorLong.split(";");
	}
	public void removeSatellite(int location)
	{ //remove satellite from list
		SatelliteName[location] = null;
		EphemerisLocation[location] = null;
		Co[location] = null;
		ModelCentered[location] = false;
	}
	public String getSatelliteName(int location)
	{ //return satellite name for given location in array
		return SatelliteName[location];
	}
	public String getEphemerisLocation(int location)
	{ //return ephemeris location
		return EphemerisLocation[location];
	}
	public String getColor(int location)
	{ //returns satellite color
		return Co[location];
	}
	public boolean getModelCentered(int location)
	{ //returns if 3D view should be model centered or not
		return ModelCentered[location];
	}
	public void setSatelliteName(String name, int location)
	{ //renames satellite
		SatelliteName[location] = name;
	}
	public void setEphemerisLocation(String path, int location)
	{ //change ephemeris location
		EphemerisLocation[location] = path;
	}
	public void setColor(String c, int location)
	{ //change satellite color
		Co[location] = c;
	}
	public void setModelCentered(boolean model, int location)
	{ //change if view is model-centered
		ModelCentered[location] = model;
	}
	public int getSize()
	{ //number of satellites
		return SatelliteName.length;
	}
}