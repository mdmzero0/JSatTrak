/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.gpx;

import gov.nasa.worldwind.tracks.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author tag
 * @version $Id: GpxReader.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class GpxReader // TODO: I18N, proper exception handling, remove stack-trace prints
{
    private javax.xml.parsers.SAXParser parser;
    private java.util.List<Track> tracks = new java.util.ArrayList<Track>();

    public GpxReader() throws javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException
    {
        javax.xml.parsers.SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);

        this.parser = factory.newSAXParser();
    }

    /**
     * @param path
     * @throws IllegalArgumentException if <code>path</code> is null
     * @throws java.io.IOException      if no file exists at the location specified by <code>path</code>
     * @throws org.xml.sax.SAXException
     */
    public void readFile(String path) throws java.io.IOException, org.xml.sax.SAXException
    {
        if (path == null)
        {
            String msg = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        java.io.File file = new java.io.File(path);
        if (!file.exists())
        {
            String msg = Logging.getMessage("generic.FileNotFound", path);
            Logging.logger().severe(msg);
            throw new java.io.FileNotFoundException(path);
        }

        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        this.doRead(fis);
    }

    /**
     * @param stream
     * @throws IllegalArgumentException if <code>stream</code> is null
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public void readStream(java.io.InputStream stream) throws java.io.IOException, org.xml.sax.SAXException
    {
        if (stream == null)
        {
            String msg = Logging.getMessage("nullValue.InputStreamIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.doRead(stream);
    }

    public java.util.List<Track> getTracks()
    {
        return this.tracks;
    }

    private void doRead(java.io.InputStream fis) throws java.io.IOException, org.xml.sax.SAXException
    {
        this.parser.parse(fis, new Handler());
    }

    private class Handler extends org.xml.sax.helpers.DefaultHandler
    {
        // this is a private class used solely by the containing class, so no validation occurs in it.

        private gov.nasa.worldwind.formats.gpx.ElementParser currentElement = null;

        @Override
        public void warning(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.warning(saxParseException);
        }

        @Override
        public void error(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.error(saxParseException);
        }

        @Override
        public void fatalError(org.xml.sax.SAXParseException saxParseException) throws org.xml.sax.SAXException
        {
            saxParseException.printStackTrace();
            super.fatalError(saxParseException);
        }

        @Override
        public void startElement(String uri, String lname, String qname, org.xml.sax.Attributes attributes)
            throws org.xml.sax.SAXException
        {
            if (this.currentElement != null)
            {
                this.currentElement.startElement(uri, lname, qname, attributes);
            }
            else if (lname.equalsIgnoreCase("trk"))
            {
                GpxTrack track = new GpxTrack(uri, lname, qname, attributes);
                this.currentElement = track;
                GpxReader.this.tracks.add(track);
            }
        }

        @Override
        public void endElement(String uri, String lname, String qname) throws org.xml.sax.SAXException
        {
            if (this.currentElement != null)
            {
                this.currentElement.endElement(uri, lname, qname);

                if (lname.equalsIgnoreCase(this.currentElement.getElementName()))
                    this.currentElement = null;
            }
        }

        @Override
        public void characters(char[] data, int start, int length) throws org.xml.sax.SAXException
        {
            if (this.currentElement != null)
                this.currentElement.characters(data, start, length);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            gov.nasa.worldwind.formats.gpx.GpxReader reader = new gov.nasa.worldwind.formats.gpx.GpxReader();
            reader.readFile("src/worldwinddemo/track data/20061126.gpx");

            System.out.printf("%d tracks\n", reader.getTracks().size());
            for (Track track : reader.getTracks())
            {
                System.out.printf("GpxTrack %d segments\n", track.getSegments().size());
                int i = 0;
                for (TrackSegment segment : track.getSegments())
                {
                    System.out.printf("\tSegment %d, %d points\n", i++, segment.getPoints().size());
                    int j = 0;
                    for (TrackPoint point : segment.getPoints())
                    {
                        System.out.printf("\t\t%4d: %s\n", j++, point);
                    }
                }
            }
        }
        catch (javax.xml.parsers.ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (org.xml.sax.SAXException e)
        {
            e.printStackTrace();
        }
        catch (java.io.IOException e)
        {
            e.printStackTrace();
        }
    }
}
