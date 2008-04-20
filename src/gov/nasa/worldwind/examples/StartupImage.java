/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.layers.Earth.BMNGOneImage;

/**
 * @author tag
 * @version $Id: StartupImage.java 2990 2007-09-22 17:46:32Z tgaskins $
 */
public class StartupImage extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                insertBeforeLayerName(this.getWwd(), new BMNGOneImage(), "Blue Marble");
                this.getLayerPanel().update(this.getWwd());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Startup Image", AppFrame.class);
    }
}
