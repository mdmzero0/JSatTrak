/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.View;

/**
 * @author tag
 * @version $Id: BasicDragger.java 3459 2007-11-08 18:48:01Z dcollins $
 */
public class BasicDragger implements SelectListener
{
    private final WorldWindow wwd;
    private boolean dragging = false;

    public BasicDragger(WorldWindow wwd)
    {
        if (wwd == null)
        {
            String msg = Logging.getMessage("nullValue.WorldWindow");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.wwd = wwd;
    }

    public boolean isDragging()
    {
        return this.dragging;
    }

    public void selected(SelectEvent event)
    {
        if (event == null)
        {
            String msg = Logging.getMessage("nullValue.EventIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (event.getEventAction().equals(SelectEvent.DRAG_END))
        {
            this.dragging = false;
        }
        else if (event.getEventAction().equals(SelectEvent.DRAG))
        {
            DragSelectEvent dragEvent = (DragSelectEvent) event;
            Object topObject = dragEvent.getTopObject();
            if (topObject == null)
                return;

            if (!(topObject instanceof Movable))
                return;

            Movable dragObject = (Movable) topObject;

            View view = wwd.getView();
            Globe globe = wwd.getModel().getGlobe();

            // Compute ref-point position in screen coordinates.
            Position refPos = dragObject.getReferencePosition();
            Vec4 refPoint = globe.computePointFromPosition(refPos);
            Vec4 screenRefPoint = view.project(refPoint);

            // Compute screen-coord delta since last event.
            int dx = dragEvent.getPickPoint().x - dragEvent.getPreviousPickPoint().x;
            int dy = dragEvent.getPickPoint().y - dragEvent.getPreviousPickPoint().y;

            // Find intersection of screen coord ref-point with globe.
            double x = screenRefPoint.x + dx;
            double y = event.getMouseEvent().getComponent().getSize().height - screenRefPoint.y + dy - 1;
            Line ray = view.computeRayFromScreenPoint(x, y);
            Intersection inters[] = globe.intersect(ray, refPos.getElevation());

            if (inters != null)
            {
                // Intersection with globe. Move reference point to the intersection point.
                Position p = globe.computePositionFromPoint(inters[0].getIntersectionPoint());
                dragObject.moveTo(p);
            }
//            else
//            {
//                // No intersection.
//                if (!this.maintainAltitude)
//                {
//                    // No intersection, so increment screen ref-point and compute corresponding position.
//                    // Don't use "newScreenPoint" because it's values have been truncated to integers.
//                    Vec4 b = new Vec4(screenRefPoint.x + dx, screenRefPoint.y - dy, screenRefPoint.z, 1);
//                    Vec4 a = view.unProject(b);
//                    Position newPosition = globe.computePositionFromPoint(a);
//                    dragObject.moveTo(newPosition);
//                }
//                else
//                {
//                    // No intersection and maintaining altitude, so do nothing.
//                }
//            }

            this.dragging = true;
        }
    }
}
