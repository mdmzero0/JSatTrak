/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.wms;

import gov.nasa.worldwind.util.Logging;

import java.net.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: Request.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public abstract class Request
{
    private URI uri = null;

    // Use a TreeMap to hold the query params so that they'll always be attached to the
    // URL query string in the same order. This allows a simple string comparison to
    // determine whether two url strings address the same document.
    private TreeMap<String, String> queryParams = new TreeMap<String, String>();

    public Request()
    {
        this.initialize();
    }

    public Request(URI uri) throws URISyntaxException
    {
        if (uri != null)
        {
            try
            {
                this.setUri(uri);
            }
            catch (URISyntaxException e)
            {
                Logging.logger().fine(Logging.getMessage("generic.URIInvalid", uri.toString()));
                throw e;
            }
        }

        this.initialize();
    }

    public Request(Request sourceRequest) throws URISyntaxException
    {
        sourceRequest.copyParamsTo(this);
        this.setUri(sourceRequest.getUri());
    }

    protected void initialize()
    {
        this.queryParams.put("SERVICE", "WMS");
        this.queryParams.put("EXCEPTIONS", "application/vnd.ogc.se_xml");
    }

    private void copyParamsTo(Request destinationRequest)
    {
        for (Map.Entry<String, String> entry : this.queryParams.entrySet())
        {
            destinationRequest.setParam((String) ((Map.Entry) entry).getKey(), (String) ((Map.Entry) entry).getValue());
        }
    }

    protected void setUri(URI uri) throws URISyntaxException
    {
        try
        {
            this.uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(),
                this.buildQueryString(), null);
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri.toString());
            Logging.logger().fine(message);
            throw e;
        }
    }

    public String getRequestName()
    {
        return this.getParam("REQUEST");
    }

    public String getVersion()
    {
        return this.getParam("VERSION");
    }

    public void setVersion(String version)
    {
        this.setParam("VERSION", version);
    }

    public void setParam(String key, String value)
    {
        if (key != null)
            this.queryParams.put(key, value);
    }

    public String getParam(String key)
    {
        return key != null ? this.queryParams.get(key) : null;
    }

    public URI getUri() throws URISyntaxException
    {
        if (this.uri == null)
            return null;

        try
        {
            return new URI(this.uri.getScheme(), this.uri.getUserInfo(), this.uri.getHost(), this.uri.getPort(),
                uri.getPath(), this.buildQueryString(), null);
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri.toString());
            Logging.logger().fine(message);
            throw e;
        }
    }

    private String buildQueryString()
    {
        StringBuffer queryString = new StringBuffer();

        for (Map.Entry<String, String> entry : this.queryParams.entrySet())
        {
            if (((Map.Entry) entry).getKey() != null && ((Map.Entry) entry).getValue() != null)
            {
                queryString.append(((Map.Entry) entry).getKey());
                queryString.append("=");
                queryString.append(((Map.Entry) entry).getValue());
                queryString.append("&");
            }
        }

        return queryString.toString();
    }

    public String toString()
    {
        String errorMessage = "Error converting wms-request URI to string.";
        try
        {
            java.net.URI fullUri = this.getUri();
            return fullUri != null ? fullUri.toString() : errorMessage;
        }
        catch (URISyntaxException e)
        {
            return errorMessage;
        }
    }
}
