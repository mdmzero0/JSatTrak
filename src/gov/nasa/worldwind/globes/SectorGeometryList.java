/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.globes;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: SectorGeometryList.java 3632 2007-11-28 03:28:17Z tgaskins $
 */
public class SectorGeometryList extends ArrayList<SectorGeometry>
{
    private Sector sector;
    private PickSupport pickSupport = new PickSupport();

    public SectorGeometryList()
    {
    }

    public SectorGeometryList(SectorGeometryList list)
    {
        super(list);
    }

    public Sector getSector()
    {
        return sector;
    }

    public void setSector(Sector sector)
    {
        this.sector = sector;
    }

    public void pick(DrawContext dc, java.awt.Point pickPoint)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (pickPoint == null)
            return;

        this.pickSupport.clearPickList();
        this.pickSupport.beginPicking(dc);

        GL gl = dc.getGL();
        gl.glPushAttrib(GL.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glShadeModel(GL.GL_FLAT);
        gl.glDisable(GL.GL_CULL_FACE);

        try
        {
            // render each sector in unique color
            for (SectorGeometry sector : this)
            {
                Color color = dc.getUniquePickColor();
                dc.getGL().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                sector.render(dc);
                // lat/lon/elevation not used in this case
                this.pickSupport.addPickableObject(color.getRGB(), sector, Position.ZERO, true);
            }

            PickedObject pickedSector = this.pickSupport.getTopObject(dc, pickPoint, null);
            if (pickedSector == null || pickedSector.getObject() == null)
                return; // no sector picked

            SectorGeometry sector = (SectorGeometry) pickedSector.getObject();
            gl.glDepthFunc(GL.GL_LEQUAL);
            sector.pick(dc, pickPoint);
        }
        finally
        {
            gl.glPopAttrib();
            this.pickSupport.endPicking(dc);
            this.pickSupport.clearPickList();
        }
    }

    private HashMap<SectorGeometry, ArrayList<Point>> pickSectors = new HashMap<SectorGeometry, ArrayList<Point>>();

    public ArrayList<PickedObject> pick(DrawContext dc, List<Point> pickPoints)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (pickPoints == null || pickPoints.size() < 1)
            return null;

        this.pickSupport.clearPickList();
        this.pickSupport.beginPicking(dc);

        GL gl = dc.getGL();
        gl.glPushAttrib(GL.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_ENABLE_BIT | GL.GL_CURRENT_BIT);
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glShadeModel(GL.GL_FLAT);
        gl.glDisable(GL.GL_CULL_FACE);

        try
        {
            // render each sector in a unique color
            for (SectorGeometry sector : this)
            {
                Color color = dc.getUniquePickColor();
                dc.getGL().glColor3ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue());
                sector.render(dc);
                // lat/lon/elevation not used in this case
                this.pickSupport.addPickableObject(color.getRGB(), sector, Position.ZERO, true);
            }

            // Determine the sectors underneath the pick points. Assemble a pick-points per sector map.
            // Several pick points might intersect the same sector.
            this.pickSectors.clear();
            for (Point pickPoint : pickPoints)
            {
                PickedObject pickedSector = this.pickSupport.getTopObject(dc, pickPoint, null);
                if (pickedSector == null || pickedSector.getObject() == null)
                    continue;

                SectorGeometry sector = (SectorGeometry) pickedSector.getObject();
                ArrayList<Point> sectorPickPoints;
                if (!this.pickSectors.containsKey(sector))
                {
                    sectorPickPoints = new ArrayList<Point>();
                    this.pickSectors.put(sector, sectorPickPoints);
                }
                else
                {
                    sectorPickPoints = this.pickSectors.get(sector);
                }
                sectorPickPoints.add(pickPoint);
            }

            if (this.pickSectors.size() < 1)
                return null;

            // Now have each sector determine the pick position for each intersecting pick point.
            gl.glDepthFunc(GL.GL_LEQUAL);
            ArrayList<PickedObject> pickedObjects = new ArrayList<PickedObject>();
            for (Map.Entry<SectorGeometry, ArrayList<Point>> sector : this.pickSectors.entrySet())
            {
                ArrayList<Point> sectorPickPoints = sector.getValue();
                PickedObject[] pos = sector.getKey().pick(dc, sectorPickPoints);
                if (pos == null)
                    continue;

                for (PickedObject po : pos)
                {
                    if (po != null)
                        pickedObjects.add(po);
                }
            }

            return pickedObjects;
        }
        finally
        {
            gl.glPopAttrib();
            this.pickSupport.endPicking(dc);
            this.pickSupport.clearPickList();
        }
    }

    public Vec4 getSurfacePoint(Position position)
    {
        return this.getSurfacePoint(position.getLatitude(), position.getLongitude(), position.getElevation());
    }

    public Vec4 getSurfacePoint(Angle latitude, Angle longitude)
    {
        return this.getSurfacePoint(latitude, longitude, 0d);
    }

    public Vec4 getSurfacePoint(Angle latitude, Angle longitude, double metersOffset)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        for (SectorGeometry sg : this)
        {
            if (sg.getSector().contains(latitude, longitude))
            {
                Vec4 point = sg.getSurfacePoint(latitude, longitude, metersOffset);
                if (point != null)
                    return point;
            }
        }

        return null;
    }
}
