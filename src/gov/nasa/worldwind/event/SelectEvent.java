/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.event;

import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;

/**
 * @author tag
 * @version $Id: SelectEvent.java 2471 2007-07-31 21:50:57Z tgaskins $
 */
public class SelectEvent extends EventObject
{
    public static final String LEFT_CLICK = "gov.nasa.worldwind.SelectEvent.LeftClick";
    public static final String LEFT_DOUBLE_CLICK = "gov.nasa.worldwind.SelectEvent.LeftDoubleClick";
    public static final String RIGHT_CLICK =  "gov.nasa.worldwind.SelectEvent.RightClick";
    public static final String LEFT_PRESS = "gov.nasa.worldwind.SelectEvent.LeftPress";
    public static final String RIGHT_PRESS = "gov.nasa.worldwind.SelectEvent.RightPress";
    public static final String HOVER =  "gov.nasa.worldwind.SelectEvent.Hover";
    public static final String ROLLOVER =  "gov.nasa.worldwind.SelectEvent.Rollover";
    public static final String DRAG =  "gov.nasa.worldwind.SelectEvent.Drag";
    public static final String DRAG_END =  "gov.nasa.worldwind.SelectEvent.DragEnd";

    private final String eventAction;
    private final java.awt.Point pickPoint;
    private final MouseEvent mouseEvent;
    private final PickedObjectList pickedObjects;

    public SelectEvent(Object source, String eventAction, MouseEvent mouseEvent, PickedObjectList pickedObjects)
    {
        super(source);
        this.eventAction = eventAction;
        this.pickPoint = mouseEvent != null ? mouseEvent.getPoint() : null;
        this.mouseEvent = mouseEvent;
        this.pickedObjects = pickedObjects;
    }

    public SelectEvent(Object source, String eventAction, java.awt.Point pickPoint, PickedObjectList pickedObjects)
    {
        super(source);
        this.eventAction = eventAction;
        this.pickPoint = pickPoint;
        this.mouseEvent = null;
        this.pickedObjects = pickedObjects;
    }

    public String getEventAction()
    {
        return this.eventAction != null ? this.eventAction : "gov.nasa.worldwind.SelectEvent.UnknownEventAction";
    }

    public Point getPickPoint()
    {
        return pickPoint;
    }

    public MouseEvent getMouseEvent()
    {
        return mouseEvent;
    }

    public boolean hasObjects()
    {
        return this.pickedObjects != null && this.pickedObjects.size() > 0;
    }

    public PickedObjectList getObjects()
    {
        return this.pickedObjects;
    }

    public PickedObject getTopPickedObject()
    {
        return this.hasObjects() ? this.pickedObjects.getTopPickedObject() : null;
    }

    public Object getTopObject()
    {
        PickedObject tpo = this.getTopPickedObject();
        return tpo != null ? tpo.getObject() : null;
    }

    @Override
    public String toString()
    {
        return this.getClass().getName() + " "
            + this.eventAction != null ? this.eventAction : Logging.getMessage("generic.Unknown");
    }
}
