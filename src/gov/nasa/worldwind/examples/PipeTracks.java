/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.formats.gpx.GpxReader;
import gov.nasa.worldwind.tracks.Track;

import javax.xml.parsers.ParserConfigurationException;
import java.util.List;
import java.io.IOException;

import org.xml.sax.SAXException;

/**
 * @author tag
 * @version $Id: PipeTracks.java 2990 2007-09-22 17:46:32Z tgaskins $
 */
public class PipeTracks extends ApplicationTemplate
{
    private static final String TRACK_FILE1 = "src/gov/nasa/worldwind/examples/PipeTrackTest.gpx";
    private static final String TRACK_FILE2 = "src/gov/nasa/worldwind/examples/PipeTracks2.gpx";

    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            TrackPipesLayer layer = this.buildTracksLayer(TRACK_FILE1);
            layer.setPipeMaterial(Material.WHITE);
            layer.setJunctionMaterial(Material.RED);
            insertBeforeCompass(this.getWwd(), layer);

            layer = this.buildTracksLayer(TRACK_FILE2);
            layer.setPipeMaterial(Material.GREEN);
            layer.setJunctionMaterial(Material.YELLOW);
            insertBeforeCompass(this.getWwd(), layer);
        }

        private TrackPipesLayer buildTracksLayer(String fileName)
        {
            try
            {
                GpxReader reader = new GpxReader();
                reader.readFile(fileName);
                List<Track> tracks = reader.getTracks();
                TrackPipesLayer layer = new TrackPipesLayer(tracks);
                return layer;
            }
            catch (ParserConfigurationException e)
            {
                e.printStackTrace();
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Pipe Tracks", AppFrame.class);
    }
}
