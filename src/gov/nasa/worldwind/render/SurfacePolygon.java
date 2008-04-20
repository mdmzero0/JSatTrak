/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.Iterator;

/**
 * @author tag
 * @version $Id: SurfacePolygon.java 2543 2007-08-14 03:16:40Z tgaskins $
 */
public class SurfacePolygon extends SurfaceShape
{

    /**
     * A Renderable polygon shape defined by a list of LatLon
     *
     * @param positions   the list of LatLon positions that makes the polygon
     * @param color       the interior fill color
     * @param borderColor the border color
     */
    public SurfacePolygon(Iterable<LatLon> positions, Color color, Color borderColor)
    {
        super(positions, color, borderColor, null);
    }

    /**
     * A Renderable polygon shape defined by a list of LatLon
     *
     * @param positions the list of LatLon positions that makes the polygon
     */
    public SurfacePolygon(Iterable<LatLon> positions)
    {
        super(positions, null, null, null);
    }

    public SurfacePolygon(Iterable<LatLon> positions, Color color, Color borderColor, Dimension textureSize)
    {
        super(positions, color, borderColor, textureSize);
    }

    private static final double TO_RADIANS = (Math.PI / 180);

    /**
     * Draw all or part of the shape that intersects a given Sector into the given BufferedImage
     */
    protected final BufferedImage drawShape(Globe globe, Sector sector, BufferedImage image)
    {
        double rsw = globe.getRadiusAt(sector.getMinLatitude(), sector.getMinLongitude());
        double rne = globe.getRadiusAt(sector.getMaxLatitude(), sector.getMaxLongitude());
        double xsw = rsw * sector.getMinLongitude().radians;
        double ysw = rsw * sector.getMinLatitude().radians;
        double xne = rne * sector.getMaxLongitude().radians;
        double yne = rne * sector.getMaxLatitude().radians;
        double dy = yne - ysw;
        double dx = xne - xsw;

        // Note : WWJ-36 negate latScale to define path upside-down
        // (will be drawn with a mirror transform - this gets paint patterns right)
        double latScale = dy > 0 ? -(image.getHeight() - 1) / dy : 0;
        double lonScale = dx > 0 ? (image.getWidth() - 1) / dx : 0;

        // If we may cross +-180 degrees longitude, then offset
        // all longitudes 180 degrees the other way
        double lonOffset = 0;
        if (sector.getMaxLongitude().getDegrees() == 180 && sector.getDeltaLonDegrees() < 180)
            lonOffset = -180;
        if (sector.getMinLongitude().getDegrees() == -180 && sector.getDeltaLonDegrees() < 180)
            lonOffset = 180;

        GeneralPath path = new GeneralPath();

        Iterator<LatLon> positions = this.getPositions().iterator();
        if (!positions.hasNext())
            return image;

        // Start position
        LatLon pos = this.computeDrawLatLon(positions.next(), sector, lonOffset);
        double r = globe.getRadiusAt(pos.getLatitude(), pos.getLongitude());
        double x = lonScale * (r * pos.getLongitude().radians - r * TO_RADIANS * lonOffset - xsw);
        double y = latScale * (r * pos.getLatitude().radians - ysw);
        path.moveTo((float) x, (float) y);

        while (positions.hasNext())
        {
            // Next position
            LatLon posNext = this.computeDrawLatLon(positions.next(), sector, lonOffset);
            // Compute number of necessary steps
            int numIntervals = (int) Math.max(1d,
                this.getNumEdgeIntervalsPerDegree() * LatLon.sphericalDistance(pos, posNext).degrees);
            double delta = 1d / numIntervals;
            // Draw segments to next position
            for (int i = 1; i < numIntervals; i++)
            {
                // In between steps
                LatLon p = LatLon.interpolate(i * delta, pos, posNext);
                r = globe.getRadiusAt(p.getLatitude(), p.getLongitude());
                x = lonScale * (r * p.getLongitude().radians - r * TO_RADIANS * lonOffset - xsw);
                y = latScale * (r * p.getLatitude().radians - ysw);
                path.lineTo((float) x, (float) y);
            }

            // Set the last point directly to avoid any round-off error in the iteration above.
            r = globe.getRadiusAt(posNext.getLatitude(), posNext.getLongitude());
            x = lonScale * (r * posNext.getLongitude().radians - r * TO_RADIANS * lonOffset - xsw);
            y = latScale * (r * posNext.getLatitude().radians - ysw);
            path.lineTo((float) x, (float) y);
            // Next

            pos = posNext;
        }

        Graphics2D g2 = image.createGraphics();
        // Set mirror Y transform
        g2.setTransform(AffineTransform.getScaleInstance(1, -1));
        // Set antiliasing hint
        if (this.isAntiAlias())
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Draw interior
        if (this.isDrawInterior())
        {
            g2.setPaint(this.getPaint());
            g2.fill(path);
        }
        // Draw border
        if (this.isDrawBorder())
        {
            g2.setPaint(this.getBorderColor());
            g2.setStroke(this.getStroke());
            g2.draw(path);
        }

        return image;
    }

    /**
     * Returns the drawing LatLon relative to a given Sector and a longitude offset Can go beyond +-180 degrees
     * longitude if the offset is zero
     *
     * @param pos       the real LatLon
     * @param sector    the drawing Sector
     * @param lonOffset the current longitude offset in degrees
     * @return the appropiate drawing LatLon
     */
    private LatLon computeDrawLatLon(LatLon pos, Sector sector, double lonOffset)
    {
        int directionOffset;
        directionOffset = sector.getMaxLongitude().degrees - pos.getLongitude().getDegrees() > 180 ?
            360 : 0;
        directionOffset = pos.getLongitude().getDegrees() - sector.getMinLongitude().getDegrees() > 180 ?
            -360 : directionOffset;
        return LatLon.fromDegrees(pos.getLatitude().getDegrees(),
            pos.getLongitude().getDegrees() + directionOffset + lonOffset);
    }
}
