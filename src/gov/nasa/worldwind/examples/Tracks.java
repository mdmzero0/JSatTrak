/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.formats.gpx.GpxReader;
import gov.nasa.worldwind.layers.TrackMarkerLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.tracks.Track;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

/**
 * @author tag
 * @version $Id: Tracks.java 2990 2007-09-22 17:46:32Z tgaskins $
 */
public class Tracks extends ApplicationTemplate
{
    private static final String TRACK_FILE = "tuolumne.gpx";

    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            TrackMarkerLayer layer = this.buildTracksLayer();
            layer.setMaterial(Material.WHITE);
//            layer.setMarkerShape("Cylinder");
            insertBeforeCompass(this.getWwd(), layer);
        }

        private TrackMarkerLayer buildTracksLayer()
        {
            try
            {
                GpxReader reader = new GpxReader();
                reader.readFile(TRACK_FILE);
                List<Track> tracks = reader.getTracks();
                TrackMarkerLayer layer = new TrackMarkerLayer(tracks);
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
        ApplicationTemplate.start("World Wind Tracks", AppFrame.class);
    }
}
