/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.wms;

import java.net.*;

/**
 * @author tag
 * @version $Id: CapabilitiesRequest.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public final class CapabilitiesRequest extends Request
{
    public CapabilitiesRequest()
    {
    }

    public CapabilitiesRequest(URI uri) throws URISyntaxException
    {
        super(uri);
    }

    protected void initialize()
    {
        super.initialize();
        this.setParam("REQUEST", "GetCapabilities");
    }
}
