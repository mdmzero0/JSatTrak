/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.pick.PickedObjectList;
import gov.nasa.worldwind.render.*;

import java.awt.*;
import java.awt.font.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: DraggingShapes.java 3209 2007-10-06 21:57:53Z tgaskins $
 */
public class DraggingShapes extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            insertBeforeCompass(this.getWwd(), this.buildShapesLayer());
            insertBeforeCompass(this.getWwd(), this.buildIconLayer());
            this.getLayerPanel().update(this.getWwd());

            this.getWwd().addSelectListener(new SelectListener()
            {
                private WWIcon lastToolTipIcon = null;
                private BasicDragger dragger = new BasicDragger(getWwd());

                public void selected(SelectEvent event)
                {
                    // Have hover selections show a picked icon's tool tip.
                    if (event.getEventAction().equals(SelectEvent.HOVER))
                    {
                        // If a tool tip is already showing, undisplay it.
                        if (lastToolTipIcon != null)
                        {
                            lastToolTipIcon.setShowToolTip(false);
                            this.lastToolTipIcon = null;
                            AppFrame.this.getWwd().repaint();
                        }

                        // If there's a selection, we're not dragging, and the selection is an icon, show tool tip.
                        if (event.hasObjects() && !this.dragger.isDragging())
                        {
                            if (event.getTopObject() instanceof WWIcon)
                            {
                                this.lastToolTipIcon = (WWIcon) event.getTopObject();
                                lastToolTipIcon.setShowToolTip(true);
                                AppFrame.this.getWwd().repaint();
                            }
                        }
                    }
                    // Have rollover events highlight the rolled-over object.
                    else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging())
                    {
                        AppFrame.this.highlight(event.getTopObject());
                    }

                    // Have drag events drag the selected object.
                    else if (event.getEventAction().equals(SelectEvent.DRAG_END)
                        || event.getEventAction().equals(SelectEvent.DRAG))
                    {
                        // Delegate dragging computations to a dragger.
                        this.dragger.selected(event);

                        // We missed any roll-over events while dragging, so highlight any under the cursor now,
                        // or de-highlight the dragged shape if it's no longer under the cursor.
                        if (event.getEventAction().equals(SelectEvent.DRAG_END))
                        {
                            PickedObjectList pol = getWwd().getObjectsAtCurrentPosition();
                            if (pol != null)
                            {
                                AppFrame.this.highlight(pol.getTopObject());
                                AppFrame.this.getWwd().repaint();
                            }
                        }
                    }
                }
            });
        }

        WWIcon lastPickedIcon;

        private void highlight(Object o)
        {
            // Manage highlighting of icons.

            if (this.lastPickedIcon == o)
                return; // same thing selected

            // Turn off highlight if on.
            if (this.lastPickedIcon != null)
            {
                this.lastPickedIcon.setHighlighted(false);
                this.lastPickedIcon = null;
            }

            // Turn on highlight if object selected.
            if (o != null && o instanceof WWIcon)
            {
                this.lastPickedIcon = (WWIcon) o;
                this.lastPickedIcon.setHighlighted(true);
            }
        }

        private IconLayer buildIconLayer()
        {
            IconLayer layer = new IconLayer();
            Font font = this.makeToolTipFont();

            // Distribute little NASA icons around the equator. Put a few at non-zero altitude.
            for (double lat = 0; lat < 10; lat += 10)
            {
                for (double lon = -180; lon < 180; lon += 10)
                {
                    double alt = 0;
                    if (lon % 90 == 0)
                        alt = 2000000;
                    WWIcon icon = new UserFacingIcon("images/32x32-icon-nasa.png",
                        new Position(Angle.fromDegrees(lat), Angle.fromDegrees(lon), alt));
                    icon.setHighlightScale(1.5);
                    icon.setToolTipFont(font);
                    icon.setToolTipText(icon.getImageSource().toString());
                    icon.setToolTipTextColor(java.awt.Color.YELLOW);
                    layer.addIcon(icon);
                }
            }

            return layer;
        }

        private Font makeToolTipFont()
        {
            HashMap<TextAttribute, Object> fontAttributes = new HashMap<TextAttribute, Object>();

            fontAttributes.put(TextAttribute.BACKGROUND, new java.awt.Color(0.4f, 0.4f, 0.4f, 1f));
            return Font.decode("Arial-BOLD-14").deriveFont(fontAttributes);
        }

        private RenderableLayer buildShapesLayer()
        {
            // Create a layer with shapes to drag.

            RenderableLayer layer = new RenderableLayer();

            // Create a yellow, semi-transparent surface shape over Mt. Shasta.
            Color interiorColor = new Color(1f, 1f, 0f, 0.3f);
            Color borderColor = new Color(1f, 1f, 0f, 0.8f);
            SurfaceShape surfaceSector = new SurfaceSector(new Sector(
                Angle.fromDegrees(41.0), Angle.fromDegrees(41.6),
                Angle.fromDegrees(-122.5), Angle.fromDegrees(-121.7)),
                interiorColor, borderColor, null);
            layer.addRenderable(surfaceSector);

            // Create a turquoise, semi-transparent surface shape over Lake Tahoe.
            SurfaceShape surfaceQuad = new SurfaceSector(new Sector(
                Angle.fromDegrees(38.9), Angle.fromDegrees(39.3),
                Angle.fromDegrees(-120.2), Angle.fromDegrees(-119.9)),
                new Color(0f, 1f, 1f, 0.3f), new Color(0.5f, 1f, 1f, 0.8f), null);
            layer.addRenderable(surfaceQuad);

            // Create an ellipse over the center of the States.
            SurfaceShape surfaceEllipse = new SurfaceEllipse(getWwd().getModel().getGlobe(),
                LatLon.fromDegrees(38, -104), 1.5e5, 1e5, Angle.fromDegrees(15), 16,
                new Color(0f, 1f, 1f, 0.3f), new Color(0.5f, 1f, 1f, 0.8f), null);
            layer.addRenderable(surfaceEllipse);

            // Create a quadrilateral over  the States.
            SurfaceShape surfaceQuadShape = new SurfaceQuad(getWwd().getModel().getGlobe(),
                LatLon.fromDegrees(42, -104), 1e5, 1.3e5, Angle.fromDegrees(20),
                new Color(0f, 1f, 1f, 0.3f), new Color(0.5f, 1f, 1f, 0.8f), null);
            layer.addRenderable(surfaceQuadShape);

            // Create a square over the  States.
            SurfaceShape surfaceSquareShape = new SurfaceSquare(getWwd().getModel().getGlobe(),
                LatLon.fromDegrees(45, -104), 1e5,
                new Color(0f, 1f, 1f, 0.3f), new Color(0.5f, 1f, 1f, 0.8f), null);
            layer.addRenderable(surfaceSquareShape);

            // Create a circle over the  States.
            SurfaceShape surfaceCircleShape = new SurfaceCircle(getWwd().getModel().getGlobe(),
                LatLon.fromDegrees(36, -104), 1e5, 16,
                new Color(0f, 1f, 1f, 0.3f), new Color(0.5f, 1f, 1f, 0.8f), null);
            layer.addRenderable(surfaceCircleShape);

            // Create an elevated quadrilateral above Lake Tahoe.
            Quadrilateral quad = new Quadrilateral(new Sector(
                Angle.fromDegrees(38.9), Angle.fromDegrees(39.3),
                Angle.fromDegrees(-120.2), Angle.fromDegrees(-119.9)),
                50000d);
            quad.setColor(new Color(1f, 1f, 0f, 1f));
            layer.addRenderable(quad);

            // Create a big polygon over Florida.
            double originLat = 28;
            double originLon = -82;
            ArrayList<LatLon> positions = new ArrayList<LatLon>();
            positions.add(new LatLon(Angle.fromDegrees(originLat + 5.0), Angle.fromDegrees(originLon + 2.5)));
            positions.add(new LatLon(Angle.fromDegrees(originLat + 5.0), Angle.fromDegrees(originLon - 2.5)));
            positions.add(new LatLon(Angle.fromDegrees(originLat + 2.5), Angle.fromDegrees(originLon - 5.0)));
            positions.add(new LatLon(Angle.fromDegrees(originLat - 2.5), Angle.fromDegrees(originLon - 5.0)));
            positions.add(new LatLon(Angle.fromDegrees(originLat - 5.0), Angle.fromDegrees(originLon - 2.5)));
            positions.add(new LatLon(Angle.fromDegrees(originLat - 5.0), Angle.fromDegrees(originLon + 2.5)));
            positions.add(new LatLon(Angle.fromDegrees(originLat - 2.5), Angle.fromDegrees(originLon + 5.0)));
            positions.add(new LatLon(Angle.fromDegrees(originLat + 2.5), Angle.fromDegrees(originLon + 5.0)));
            positions.add(new LatLon(Angle.fromDegrees(originLat + 5.0), Angle.fromDegrees(originLon + 2.5)));
            SurfacePolygon polygon = new SurfacePolygon(positions,
                new Color(1f, 0.11f, 0.2f, 0.4f), new Color(1f, 0f, 0f, 0.6f));
            polygon.setStroke(new BasicStroke(2f));
            layer.addRenderable(polygon);

            // Test +180/-180 lon span Polyline
            positions = new ArrayList<LatLon>();
            positions.add(new LatLon(Angle.fromDegrees(-10), Angle.fromDegrees(170)));
            positions.add(new LatLon(Angle.fromDegrees(-10), Angle.fromDegrees(-170)));
            Polyline polyline = new Polyline(positions, 1000);
            polyline.setPathType(Polyline.GREAT_CIRCLE);
            layer.addRenderable(polyline);

            // Test +180/-180 lon span SurfacePolyline
            positions = new ArrayList<LatLon>();
            positions.add(new LatLon(Angle.fromDegrees(20), Angle.fromDegrees(-170)));
            positions.add(new LatLon(Angle.fromDegrees(15), Angle.fromDegrees(170)));
            positions.add(new LatLon(Angle.fromDegrees(10), Angle.fromDegrees(-175)));
            positions.add(new LatLon(Angle.fromDegrees(5), Angle.fromDegrees(170)));
            positions.add(new LatLon(Angle.fromDegrees(0), Angle.fromDegrees(-170)));
            positions.add(new LatLon(Angle.fromDegrees(20), Angle.fromDegrees(-170)));
            SurfacePolygon surfacePolygon = new SurfacePolygon(positions,
                new Color(1f, 0.11f, 0.2f, 0.4f), new Color(1f, 0f, 0f, 0.6f));
            surfacePolygon.setStroke(new BasicStroke(2f));
            layer.addRenderable(surfacePolygon);

            return layer;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Shape Dragging", AppFrame.class);
    }
}
