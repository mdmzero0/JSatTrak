/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import com.sun.opengl.util.texture.TextureData;
import gov.nasa.worldwind.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

/**
 * @author tag
 * @version $Id: SurfaceShape.java 3407 2007-10-28 08:44:01Z tgaskins $
 */
public abstract class SurfaceShape implements Renderable, Disposable, Movable
{
    public static Dimension TEXTURE_SIZE_1024 = new Dimension(1024, 1024);
    public static Dimension TEXTURE_SIZE_512 = new Dimension(512, 512);
    public static Dimension TEXTURE_SIZE_256 = new Dimension(256, 256);
    public static Dimension TEXTURE_SIZE_128 = new Dimension(128, 128);
    public static Dimension TEXTURE_SIZE_64 = new Dimension(64, 64);
    public static Dimension TEXTURE_SIZE_32 = new Dimension(32, 32);
    public static Dimension TEXTURE_SIZE_16 = new Dimension(16, 16);
    public static Dimension TEXTURE_SIZE_8 = new Dimension(8, 8);

    private static final Color DEFAULT_COLOR = new Color(1f, 1f, 0f, 0.4f);
    private static final Color DEFAULT_BORDER_COLOR = new Color(1f, 1f, 0f, 0.7f);
    private static final Dimension DEFAULT_TEXTURE_SIZE = TEXTURE_SIZE_64;
    private static final double DEFAULT_NUM_EDGE_INTERVALS_PER_DEGREE = 1;
    private static final double TEXTURE_MARGIN_PIXELS = 3;

    private ArrayList<TextureTile> tiles = new ArrayList<TextureTile>();
    private Dimension textureSize = DEFAULT_TEXTURE_SIZE;
    protected Globe globe;
    private Paint paint;
    private Color borderColor;
    private Stroke stroke = new BasicStroke();
    private boolean drawBorder = true;
    private boolean drawInterior = true;
    private boolean antiAlias = true;
    private double numEdgeIntervalsPerDegree = DEFAULT_NUM_EDGE_INTERVALS_PER_DEGREE;
    protected ArrayList<LatLon> positions = new ArrayList<LatLon>();

    protected abstract BufferedImage drawShape(Globe globe, Sector sector, BufferedImage image);

    public SurfaceShape(Iterable<LatLon> positions, Color color, Color borderColor, Dimension textureSize)
    {
        if (positions == null)
        {
            String message = Logging.getMessage("nullValue.PositionsListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (textureSize != null)
            this.textureSize = textureSize;

        // Set draw attributes
        this.paint = color != null ? color : DEFAULT_COLOR;
        this.borderColor = borderColor != null ? borderColor : DEFAULT_BORDER_COLOR;

        // Copy positions list.
        this.replacePositions(positions);

        // Make tile(s)
        createTextureTiles();
    }

    private void replacePositions(Iterable<LatLon> newPositions)
    {
        this.positions.clear();
        for (LatLon position : newPositions)
        {
            this.positions.add(position);
        }
    }
//
//    protected void createTextureTiles()
//    {
//        this.tiles.clear();
//        if (!LatLon.positionsCrossDateLine(this.getPositions()))
//        {
//            this.tiles.add(
//                new TextureTile(this.computeProportionedSector(Sector.boundingSectorfromLatLons(this.getPositions()))));
//        }
//        else
//        {
//            Sector[] sectors = this.computeSplitSectors(this.getPositions());
//            this.tiles.add(new TextureTile(this.computeProportionedSector(sectors[0])));
//            this.tiles.add(new TextureTile(this.computeProportionedSector(sectors[1])));
//        }
//    }

    protected void createTextureTiles()
    {
        this.tiles.clear();
        if (!LatLon.positionsCrossDateLine(this.getPositions()))
        {
            this.tiles.add(
                new TextureTile(Sector.boundingSectorfromLatLons(this.getPositions())));
        }
        else
        {
            Sector[] sectors = this.computeSplitSectors(this.getPositions());
            this.tiles.add(new TextureTile(sectors[0]));
            this.tiles.add(new TextureTile(sectors[1]));
        }
    }

    /**
     * Returns a sector that will have the same apparent proportions as the texture when projected on the globe, and
     * that contains a given sector. Also adds a margin around.
     *
     * @param sector the sector to be included
     * @return the appropriate sector
     */
    private Sector computeProportionedSector(Sector sector)
    {
        //if(true) return sector;   // uncomment to disable
        // Make it look the same aspect ratio as the texture - without going over the edges
        // taking into account the sector centroid latitude distortion
        Angle latSpan = sector.getDeltaLat();
        Angle lonSpan = sector.getDeltaLon();
        Double midLatCos = sector.getCentroid().getLatitude().cos();
        Double aspectRatio = this.getTextureSize().getWidth() / this.getTextureSize().getHeight();
        if (lonSpan.degrees / latSpan.degrees * midLatCos < aspectRatio)
        {
            // Adjust longitude extent
            Angle halfDelta = latSpan.divide(midLatCos).multiply(aspectRatio).subtract(lonSpan).divide(2d);
            if (halfDelta.degrees * 2 > 360 - lonSpan.degrees)
                halfDelta = Angle.fromDegrees((360 - lonSpan.degrees) / 2);
            // Add latitude margin
            Angle latMargin = latSpan.divide(this.getTextureSize().getHeight()).multiply(TEXTURE_MARGIN_PIXELS);
            Angle northMargin = sector.getMaxLatitude().add(latMargin).degrees > 90 ? Angle.fromDegrees(
                90 - sector.getMaxLatitude().degrees) : latMargin;
            Angle southMargin = sector.getMinLatitude().subtract(latMargin).degrees < -90 ? Angle.fromDegrees(
                sector.getMinLatitude().degrees + 90) : latMargin;
            if (sector.getMinLongitude().degrees - halfDelta.degrees < -180)
            {
                // -180 degrees longitude crossing
                Angle westDelta = Angle.fromDegrees(sector.getMinLongitude().degrees + 180);
                Angle eastDelta = halfDelta.add(halfDelta).subtract(westDelta);
                sector =
                    new Sector(sector.getMinLatitude().subtract(southMargin), sector.getMaxLatitude().add(northMargin),
                        sector.getMinLongitude().subtract(westDelta), sector.getMaxLongitude().add(eastDelta));
            }
            else if (sector.getMaxLongitude().degrees + halfDelta.degrees > 180)
            {
                // +180 degrees longitude crossing
                Angle eastDelta = Angle.fromDegrees(180 - sector.getMaxLongitude().degrees);
                Angle westDelta = halfDelta.add(halfDelta).subtract(eastDelta);
                sector =
                    new Sector(sector.getMinLatitude().subtract(southMargin), sector.getMaxLatitude().add(northMargin),
                        sector.getMinLongitude().subtract(westDelta), sector.getMaxLongitude().add(eastDelta));
            }
            else
            {
                // No edge crossing
                sector =
                    new Sector(sector.getMinLatitude().subtract(southMargin), sector.getMaxLatitude().add(northMargin),
                        sector.getMinLongitude().subtract(halfDelta), sector.getMaxLongitude().add(halfDelta));
            }
        }
        else if (lonSpan.degrees / latSpan.degrees * midLatCos > aspectRatio)
        {
            // Adjust latitude extent
            Angle halfDelta = lonSpan.multiply(midLatCos).divide(aspectRatio).subtract(latSpan).divide(2d);
            if (halfDelta.degrees * 2 > 180 - latSpan.degrees)
                halfDelta = Angle.fromDegrees((180 - latSpan.degrees) / 2);
            // Add longitude margin
            Angle lonMargin = lonSpan.divide(this.getTextureSize().getWidth()).multiply(TEXTURE_MARGIN_PIXELS);
            Angle eastMargin = sector.getMaxLongitude().add(lonMargin).degrees > 180 ? Angle.fromDegrees(
                180 - sector.getMaxLongitude().degrees) : lonMargin;
            Angle westMargin = sector.getMinLongitude().subtract(lonMargin).degrees < -180 ? Angle.fromDegrees(
                sector.getMinLongitude().degrees + 180) : lonMargin;
            if (sector.getMinLatitude().degrees - halfDelta.degrees < -90)
            {
                // -90 degrees latitude crossing
                Angle southDelta = Angle.fromDegrees(sector.getMinLatitude().degrees + 90);
                Angle northDelta = halfDelta.add(halfDelta).subtract(southDelta);
                sector =
                    new Sector(sector.getMinLatitude().subtract(southDelta), sector.getMaxLatitude().add(northDelta),
                        sector.getMinLongitude().subtract(westMargin), sector.getMaxLongitude().add(eastMargin));
            }
            else if (sector.getMaxLatitude().degrees + halfDelta.degrees > 90)
            {
                // +90 degrees latitude crossing
                Angle northDelta = Angle.fromDegrees(90 - sector.getMaxLatitude().degrees);
                Angle southDelta = halfDelta.add(halfDelta).subtract(northDelta);
                sector =
                    new Sector(sector.getMinLatitude().subtract(southDelta), sector.getMaxLatitude().add(northDelta),
                        sector.getMinLongitude().subtract(westMargin), sector.getMaxLongitude().add(eastMargin));
            }
            else
            {
                // No edge crossing
                sector = new Sector(sector.getMinLatitude().subtract(halfDelta), sector.getMaxLatitude().add(halfDelta),
                    sector.getMinLongitude().subtract(westMargin), sector.getMaxLongitude().add(eastMargin));
            }
        }
        else
        {
            // Proportions ok, just add margin
            // Add latitude margin
            Angle latMargin = latSpan.divide(this.getTextureSize().getHeight()).multiply(TEXTURE_MARGIN_PIXELS);
            Angle northMargin = sector.getMaxLatitude().add(latMargin).degrees > 90 ? Angle.fromDegrees(
                90 - sector.getMaxLatitude().degrees) : latMargin;
            Angle southMargin = sector.getMinLatitude().subtract(latMargin).degrees < -90 ? Angle.fromDegrees(
                sector.getMinLatitude().degrees + 90) : latMargin;
            // Add longitude margin
            Angle lonMargin = lonSpan.divide(this.getTextureSize().getWidth()).multiply(TEXTURE_MARGIN_PIXELS);
            Angle eastMargin = sector.getMaxLongitude().add(lonMargin).degrees > 180 ? Angle.fromDegrees(
                180 - sector.getMaxLongitude().degrees) : lonMargin;
            Angle westMargin = sector.getMinLongitude().subtract(lonMargin).degrees < -180 ? Angle.fromDegrees(
                sector.getMinLongitude().degrees + 180) : lonMargin;
            sector = new Sector(sector.getMinLatitude().subtract(southMargin), sector.getMaxLatitude().add(northMargin),
                sector.getMinLongitude().subtract(westMargin), sector.getMaxLongitude().add(eastMargin));
        }
        //System.out.println(sector.toString());
        return sector;
    }

    /**
     * Returns two 'mirror' sectors each on one side of the longitude boundary - for boundary crossing shapes
     *
     * @param positions the shape positions
     * @return an array of two sectors representing the shape
     */
    private Sector[] computeSplitSectors(Iterable<LatLon> positions)
    {
        Sector[] sectors = new Sector[2];
        // Find out longitude extremes for each sides
        double maxWest = Angle.NEG180.getDegrees();
        double minEast = Angle.POS180.getDegrees();
        // Find out absolute latitude extremes
        double minSouth = Angle.POS90.getDegrees();
        double maxNorth = Angle.NEG90.getDegrees();

        LatLon lastPos = null;
        for (LatLon pos : positions)
        {
            double lat = pos.getLatitude().getDegrees();
            if (lat > maxNorth)
                maxNorth = lat;
            if (lat < minSouth)
                minSouth = lat;

            double lon = pos.getLongitude().getDegrees();
            if (lon <= 0 && lon > maxWest)
                maxWest = lon;
            if (lon >= 0 && lon < minEast)
                minEast = lon;

            if (lastPos != null)
            {
                double lastLon = lastPos.getLongitude().getDegrees();
                if (Math.signum(lon) != Math.signum(lastLon))
                    if (Math.abs(lon - lastLon) < 180)
                    {
                        // Crossing the zero longitude line too
                        maxWest = 0;
                        minEast = 0;
                    }
            }
            lastPos = pos;
        }
        // Mirror the two sectors - same longitude span
        maxWest = minEast < -maxWest ? -minEast : maxWest;
        minEast = minEast > -maxWest ? -maxWest : minEast;

        sectors[0] = Sector.fromDegrees(minSouth, maxNorth, minEast, 180d); // East side
        sectors[1] = Sector.fromDegrees(minSouth, maxNorth, -180d, maxWest); // West side

        return sectors;
    }

    public void dispose()
    {
        tiles.clear();
    }

    public ArrayList<Sector> getSectors()
    {
        ArrayList<Sector> sectors = new ArrayList<Sector>();
        for (TextureTile tile : this.tiles)
        {
            sectors.add(tile.getSector());
        }
        return sectors;
    }

    public Iterable<LatLon> getPositions()
    {
        return this.positions;
    }

    public void setPositions(Iterable<LatLon> positions)
    {
        this.replacePositions(positions);
        this.createTextureTiles();
    }

    public Paint getPaint()
    {
        return paint;
    }

    public void setPaint(Paint paint)
    {
        this.paint = paint;
        this.clearTextureData();
    }

    public Color getBorderColor()
    {
        return borderColor;
    }

    public void setBorderColor(Color borderColor)
    {
        this.borderColor = borderColor;
        this.clearTextureData();
    }

    public Dimension getTextureSize()
    {
        return textureSize;
    }

    public void setTextureSize(Dimension textureSize)
    {
        this.textureSize = textureSize;
        this.createTextureTiles();   // Rebuild tile(s) sectors
    }

    public Stroke getStroke()
    {
        return stroke;
    }

    public void setStroke(Stroke stroke)
    {
        this.stroke = stroke;
        this.clearTextureData();
    }

    public boolean isDrawBorder()
    {
        return drawBorder;
    }

    public void setDrawBorder(boolean drawBorder)
    {
        this.drawBorder = drawBorder;
        this.clearTextureData();
    }

    public boolean isDrawInterior()
    {
        return drawInterior;
    }

    public void setDrawInterior(boolean drawInterior)
    {
        this.drawInterior = drawInterior;
        this.clearTextureData();
    }

    public boolean isAntiAlias()
    {
        return antiAlias;
    }

    public void setAntiAlias(boolean antiAlias)
    {
        this.antiAlias = antiAlias;
        this.clearTextureData();
    }

    public double getNumEdgeIntervalsPerDegree()
    {
        return numEdgeIntervalsPerDegree;
    }

    public void setNumEdgeIntervalsPerDegree(double numEdgeIntervals)
    {
        this.numEdgeIntervalsPerDegree = numEdgeIntervals;
        this.clearTextureData();
    }

    private boolean intersects(Sector sector)
    {
        for (TextureTile tile : this.tiles)
        {
            if (tile.getSector().intersects(sector))
                return true;
        }
        return false;
    }

    public void render(DrawContext dc)
    {
        this.globe = dc.getGlobe(); // retain the globe used, for potential subsequent move

        if (this.tiles.size() == 0)
            this.createTextureTiles();

        if (!this.intersects(dc.getVisibleSector()))
            return;

        if (!this.tiles.get(0).isTextureInMemory(dc.getTextureCache()))
            makeTextureData(dc, this.textureSize);

        GL gl = dc.getGL();

        gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL.GL_POLYGON_BIT);

        try
        {
            if (!dc.isPickingMode())
            {
                gl.glEnable(GL.GL_BLEND);
                gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            }

            gl.glPolygonMode(GL.GL_FRONT, GL.GL_FILL);
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);

            dc.getGeographicSurfaceTileRenderer().renderTiles(dc, this.tiles);
        }
        finally
        {
            gl.glPopAttrib();
        }
    }

    private void makeTextureData(DrawContext dc, Dimension size)
    {
        for (TextureTile tile : this.tiles)
        {
            BufferedImage image = new BufferedImage((int) size.getWidth(), (int) size.getHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);
            /*// Debug - show tile extent with fill color
            Graphics2D g2 = image.createGraphics();
            g2.setPaint(this.getPaint());
            g2.fillRect(0, 0, (int) size.getWidth(), (int) size.getHeight());
            // end debug */
            TextureData td = new TextureData(GL.GL_RGBA, GL.GL_RGBA, false,
                this.drawShape(dc.getGlobe(), tile.getSector(), image));
            td.setMustFlipVertically(false);
            tile.setTextureData(td);
        }
    }

    private void clearTextureData()
    {
        tiles.clear();
    }

    public Position getReferencePosition()
    {
        LatLon centroid = this.tiles.get(0).getSector().getCentroid();
        return new Position(centroid, 0);
    }

    public void move(Position delta)
    {
        if (delta == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.moveTo(this.getReferencePosition().add(delta));
    }

    /**
     * Move the shape over the sphereoid surface without maintaining its original azimuth -- its orientation relative to
     * North.
     *
     * @param position the new position to move the shapes reference position to.
     */
    public void shiftTo(Position position)
    {
        if (this.globe == null)
            return;

        if (position == null)
        {
            String msg = Logging.getMessage("nullValue.PositionIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        Vec4 p1 = globe.computePointFromPosition(this.getReferencePosition().getLatitude(),
            this.getReferencePosition().getLongitude(), 0);
        Vec4 p2 = globe.computePointFromPosition(position.getLatitude(), position.getLongitude(), 0);
        Vec4 delta = p2.subtract3(p1);

        for (int i = 0; i < this.positions.size(); i++)
        {
            LatLon ll = this.positions.get(i);
            Vec4 p = globe.computePointFromPosition(ll.getLatitude(), ll.getLongitude(), 0);
            p = p.add3(delta);
            Position pos = globe.computePositionFromPoint(p);

            this.positions.set(i, new LatLon(pos.getLatitude(), pos.getLongitude()));
        }

        this.createTextureTiles();
    }

    /**
     * Move the shape over the sphereoid surface while maintaining its original azimuth -- its orientation relative to
     * North.
     *
     * @param position the new position to move the shapes reference position to.
     */
    public void moveTo(Position position)
    {
        if (LatLon.positionsCrossDateLine(this.positions))
        {
            // TODO: Replace this hack by figuring out how to *accurately* move date-line crossing shapes using the
            // distance/azimuth method used below for shapes that do not cross the dateline.
            shiftTo(position);
            return;
        }

        LatLon oldRef = this.getReferencePosition().getLatLon();
        LatLon newRef = position.getLatLon();

        for (int i = 0; i < this.positions.size(); i++)
        {
            LatLon p = this.positions.get(i);
            double distance = LatLon.sphericalDistance(oldRef, p).radians;
            double azimuth = LatLon.azimuth(oldRef, p).radians;
            LatLon pp = LatLon.endPosition(newRef, azimuth, distance);
            this.positions.set(i, pp);
        }

        this.createTextureTiles();
    }

    public static SurfaceShape createEllipse(Globe globe, LatLon center, double majorAxisLength,
        double minorAxisLength, Angle orientation, int intervals, Color interiorColor, Color borderColor,
        Dimension textureSize)
    {
        if (orientation == null)
            orientation = Angle.ZERO;

        if (majorAxisLength <= 0)
        {
            String message = Logging.getMessage("Geom.MajorAxisInvalid");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (minorAxisLength <= 0)
        {
            String message = Logging.getMessage("Geom.MajorAxisInvalid");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numPositions = 1 + Math.max(intervals, 4);
        final ArrayList<LatLon> positions = new ArrayList<LatLon>();

        double radius = globe.getRadiusAt(center.getLatitude(), center.getLongitude());
        double da = 2 * Math.PI / (numPositions - 1);
        for (int i = 0; i < numPositions; i++)
        {
            // azimuth runs positive clockwise from north and through 360¡.
            double angle = (i != numPositions - 1) ? i * da : 0;
            double azimuth = Math.PI / 2 - (angle + orientation.radians);
            double xLength = majorAxisLength * Math.cos(angle);
            double yLength = minorAxisLength * Math.sin(angle);
            double distance = Math.sqrt(xLength * xLength + yLength * yLength);
            LatLon p = LatLon.endPosition(center, azimuth, distance / radius);
            positions.add(p);
        }

        return new SurfacePolygon(positions, interiorColor, borderColor, textureSize);
    }
}
