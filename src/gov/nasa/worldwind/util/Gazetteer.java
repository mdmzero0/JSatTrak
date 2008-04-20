/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.util.WWIO;

import javax.swing.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.net.*;
import java.util.ArrayList;

import org.xml.sax.SAXException;

/**
 * @author tag
 * @version $Id: Gazetteer.java 3612 2007-11-22 16:48:56Z tgaskins $
 */
public class Gazetteer extends JPanel
{
    private static final String service =
        "http://local.yahooapis.com/MapsService/V1/geocode?appid=nasaworldwind&location=";

    public ArrayList<Gazetteer.Location> getLocations(String lookupString) throws IOException,
        ParserConfigurationException, XPathExpressionException, SAXException, GazetteerException
    {
        if (lookupString == null || lookupString.length() < 1)
            return null;
        
        String locationString = this.lookupLocation(lookupString.replaceAll(" ", "+"));
        if (locationString == null || locationString.length() < 1)
            return null;

        return this.parseLocationString(locationString);
    }

    public String lookupLocation2(String lookupString) throws IOException
    {
        if (lookupString == null || lookupString.length() < 1)
            return null;

        String urlString = service + lookupString;
        ByteBuffer buffer = WWIO.readURLContentToBuffer(new URL(urlString));
        if (buffer == null || !buffer.hasRemaining())
            return null;

        StringBuffer sb = new StringBuffer();
        while (buffer.hasRemaining() && !Thread.currentThread().isInterrupted())
        {
            sb.append((char) buffer.get());
        }

        return sb.toString();
    }

    public String lookupLocation(String lookupString) throws IOException, GazetteerException
    {
        if (lookupString == null || lookupString.length() < 1)
            return null;

        String urlString = service + lookupString;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();

        HttpURLConnection htpc = (HttpURLConnection) connection;
        int responseCode = htpc.getResponseCode();
        String responseMessage = htpc.getResponseMessage();
        InputStream inputStream = null;

        try
        {
            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                inputStream = connection.getInputStream();
                ByteBuffer buffer = WWIO.readStreamToBuffer(inputStream);
                StringBuffer sb = new StringBuffer();
                while (buffer.hasRemaining() && !Thread.currentThread().isInterrupted())
                {
                    sb.append((char) buffer.get());
                }
                return sb.toString();
            }
            else
            {
                throw new GazetteerException(responseMessage);
            }
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<Location> parseLocationString(String locationString) throws ParserConfigurationException,
        IOException, SAXException, XPathExpressionException
    {
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        docBuilderFactory.setNamespaceAware(false);
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = docBuilder.parse(new ByteArrayInputStream(locationString.getBytes("UTF-8")));

        XPathFactory xpFactory = XPathFactory.newInstance();
        XPath xpath = xpFactory.newXPath();

        org.w3c.dom.NodeList resultNodes =
            (org.w3c.dom.NodeList) xpath.evaluate("/ResultSet/Result", doc, XPathConstants.NODESET);

        ArrayList<Location> positions = new ArrayList<Location>(resultNodes.getLength());

        for (int i = 0; i < resultNodes.getLength(); i++)
        {
            org.w3c.dom.Node location = resultNodes.item(i);
            String lat = xpath.evaluate("Latitude", location);
            String lon = xpath.evaluate("Longitude", location);

            if (lat != null && lon != null)
            {
                LatLon latlon = LatLon.fromDegrees(Double.parseDouble(lat), Double.parseDouble(lon));
                Location loc = new Location(null, latlon, null, null, null, null, null);
                positions.add(loc);
            }
        }

        return positions;
    }

    public static class Location
    {
        public Location(String precision, LatLon latlon, String address, String city, String state, String zip,
            String country)
        {
            this.precision = precision;
            this.latlon = latlon;
            this.address = address;
            this.city = city;
            this.state = state;
            this.zip = zip;
            this.country = country;
        }

        private final String precision;
        private final LatLon latlon;
        private final String address;
        private final String city;
        private final String state;
        private final String zip;
        private final String country;

        public String getPrecision()
        {
            return precision;
        }

        public LatLon getLatlon()
        {
            return latlon;
        }

        public String getAddress()
        {
            return address;
        }

        public String getCity()
        {
            return city;
        }

        public String getState()
        {
            return state;
        }

        public String getZip()
        {
            return zip;
        }

        public String getCountry()
        {
            return country;
        }
    }

    public static class GazetteerException extends Exception
    {
        public GazetteerException(String string)
        {
            super(string);
        }
    }
}
