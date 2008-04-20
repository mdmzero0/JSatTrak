/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;

/**
 * @author tag
 * @version $Id$
 */
public class SurfaceImages extends ApplicationTemplate
{
    private static final String WWJ_SPLASH_PATH = "images/400x230-splash-nww.png";
    private static final String GEORSS_ICON_PATH = "images/georss.png";
    private static final String NASA_ICON_PATH = "images/32x32-icon-nasa.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                SurfaceImage si1 = new SurfaceImage(WWJ_SPLASH_PATH, Sector.fromDegrees(35, 45, -115, -95));
                SurfaceImage si2 = new SurfaceImage(GEORSS_ICON_PATH, Sector.fromDegrees(25, 33, -120, -110));
                SurfaceImage si3 = new SurfaceImage(NASA_ICON_PATH, Sector.fromDegrees(25, 35, -100, -90));
                si1.setOpacity(0.7);

                RenderableLayer layer = new RenderableLayer();
                layer.setName("Surface Images");
                layer.addRenderable(si1);
                layer.addRenderable(si2);
                layer.addRenderable(si3);

                insertBeforeCompass(this.getWwd(), layer);

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
        ApplicationTemplate.start("World Wind Surface Images", SurfaceImages.AppFrame.class);
    }
}
