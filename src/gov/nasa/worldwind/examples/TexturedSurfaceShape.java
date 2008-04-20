/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.event.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

/**
 * @author tag
 * @version $Id$
 */
public class TexturedSurfaceShape extends ApplicationTemplate
{
    private static final String IMAGE_PATH = "images/400x230-splash-nww.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                BufferedImage image = this.makeImage(IMAGE_PATH);
                Dimension imageSize = new Dimension(image.getWidth(), image.getHeight());
                TexturePaint paint = new TexturePaint(image, new Rectangle(imageSize));
                SurfaceSector si = new SurfaceSector(Sector.fromDegrees(35, 45, -115, -95), null, null, imageSize);
                si.setDrawBorder(false);
                si.setTextureSize(imageSize);
                si.setPaint(paint);

                RenderableLayer layer = new RenderableLayer();
                layer.addRenderable(si);

                insertBeforeCompass(this.getWwd(), layer);

                this.getWwd().addSelectListener(new SelectListener()
                {
                    private BasicDragger dragger = new BasicDragger(getWwd());

                    public void selected(SelectEvent event)
                    {
                        // Have drag events drag the selected object.
                        if (event.getEventAction().equals(SelectEvent.DRAG_END)
                            || event.getEventAction().equals(SelectEvent.DRAG))
                        {
                            // Delegate dragging computations to a dragger.
                            this.dragger.selected(event);
                        }
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        private BufferedImage makeImage(String texturePath) throws Exception
        {
            try
            {
                InputStream imageStream = this.getClass().getResourceAsStream("/" + texturePath);
                if (imageStream == null)
                {
                    File textureFile = new File(texturePath);
                    if (textureFile.exists())
                    {
                        imageStream = new FileInputStream(textureFile);
                    }
                }

                return ImageIO.read(imageStream);
            }
            catch (Exception e)
            {
                System.out.printf("Unable to open image %s\n", texturePath);
                throw e;
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Textured Surface Shapes", TexturedSurfaceShape.AppFrame.class);
    }
}