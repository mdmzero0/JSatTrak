/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;

/**
 * @author dcollins
 * @version $Id$
 */
class RPFNonpolarFrameTransform extends RPFFrameTransform
{
    private final RPFNonpolarFrameStructure frameStructure;

    private RPFNonpolarFrameTransform(RPFNonpolarFrameStructure frameStructure)
    {
        this.frameStructure = frameStructure;
    }

    static RPFNonpolarFrameTransform createNonpolarFrameTransform(char zoneCode, String rpfDataType, double resolution)
    {
        if (!RPFZone.isZoneCode(zoneCode))
        {
            String message = Logging.getMessage("RPFZone.UnknownZoneCode", zoneCode);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (rpfDataType == null || !RPFDataSeries.isRPFDataType(rpfDataType))
        {
            String message = Logging.getMessage("RPFDataSeries.UnkownDataType", rpfDataType);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }
        if (resolution < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", rpfDataType);
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        RPFNonpolarFrameStructure frameStructure = RPFNonpolarFrameStructure.computeStructure(zoneCode, rpfDataType,
            resolution);
        return new RPFNonpolarFrameTransform(frameStructure);
    }

    // ============== Geographic To Frame ======================= //
    // ============== Geographic To Frame ======================= //
    // ============== Geographic To Frame ======================= //

    public int frameAtLatLon(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        double northSouthPixelConstant = this.frameStructure.getNorthSouthPixelConstant();
        double eastWestPixelConstant = this.frameStructure.getEastWestPixelConstant();
        double equatorwardExtent = this.frameStructure.getEquatorwardExtent().degrees;
        double pixelRowsPerFrame = RPFFrameStructure.getPixelRowsPerFrame();
        return this.frameNumberFromRowCol(
            computeFrameRowFromLatitude(latitude, northSouthPixelConstant, equatorwardExtent, pixelRowsPerFrame),
            computeFrameColumnFromLongitude(longitude, eastWestPixelConstant, pixelRowsPerFrame));
    }

    /* [Section 30.3.1, MIL-C-89038] */
    /* [Section A.3.3.1, MIL-PRF-89041A] */
    private static int computeFrameRowFromLatitude(Angle latitude, double northSouthPixelConstant,
        double equatorwardExtent, double pixelRowsPerFrame)
    {
        if (latitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        return (int) (((latitude.degrees - equatorwardExtent) / 90d)
            * (northSouthPixelConstant / pixelRowsPerFrame));
    }

    /* [Section 30.3.2, MIL-C-89038] */
    /* [Section A.3.3.2, MIL-PRF-89041A] */
    private static int computeFrameColumnFromLongitude(Angle longitude, double eastWestPixelConstant,
        double pixelRowsPerFrame)
    {
        if (longitude == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        return (int) (((longitude.degrees + 180d) / 360d)
            * (eastWestPixelConstant / pixelRowsPerFrame));
    }

    // ============== Frame To Geographic ======================= //
    // ============== Frame To Geographic ======================= //
    // ============== Frame To Geographic ======================= //

    public Sector frameExtent(int frameNumber)
    {
        if (frameNumber < 0 || frameNumber >= this.getMaximumFrameNumber())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        double polewardExtent = this.frameStructure.getPolewardExtent().degrees;
        double equatorwardExtent = this.frameStructure.getEquatorwardExtent().degrees;
        int latFrames = this.frameStructure.getLatitudinalFrames();
        int lonFrames = this.frameStructure.getLongitudinalFrames();
        Angle latExtent = computeLatitudinalFrameExtent(polewardExtent, equatorwardExtent, latFrames);
        Angle lonExtent = computeLongitudinalFrameExtent(lonFrames);

        LatLon origin = this.frameOrigin(frameNumber);
        return new Sector(
            origin.getLatitude().subtract(latExtent), origin.getLatitude(),
            origin.getLongitude(), origin.getLongitude().add(lonExtent));
    }

    public Iterable<Integer> framesInSector(Sector sector)
    {
        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().fine(message);
            throw new IllegalArgumentException(message);
        }

        double minLat, maxLat;
        double polewardExtent = this.frameStructure.getPolewardExtent().degrees;
        double equatorwardExtent = this.frameStructure.getEquatorwardExtent().degrees;
        if (polewardExtent < equatorwardExtent)
        {
            minLat = polewardExtent;
            maxLat = equatorwardExtent;
        }
        else
        {
            minLat = equatorwardExtent;
            maxLat = polewardExtent;
        }

        Sector intersection = Sector.fromDegrees(minLat, maxLat, -180, 180).intersection(sector);
        if (intersection == null)
            return null;

        double northSouthPixelConstant = this.frameStructure.getNorthSouthPixelConstant();
        double eastWestPixelConstant = this.frameStructure.getEastWestPixelConstant();
        double pixelRowsPerFrame = RPFFrameStructure.getPixelRowsPerFrame();

        int startRow = computeFrameRowFromLatitude(intersection.getMinLatitude(), northSouthPixelConstant,
            equatorwardExtent, pixelRowsPerFrame);
        int endRow = computeFrameRowFromLatitude(intersection.getMaxLatitude(), northSouthPixelConstant,
            equatorwardExtent, pixelRowsPerFrame);
        int startCol = computeFrameColumnFromLongitude(intersection.getMinLongitude(), eastWestPixelConstant,
            pixelRowsPerFrame);
        int endCol = computeFrameColumnFromLongitude(intersection.getMaxLongitude(), eastWestPixelConstant,
            pixelRowsPerFrame);

        ArrayList<Integer> frames = new ArrayList<Integer>();
        for (int row = startRow; row <= endRow; row++)
        {
            for (int col = startCol; col <= endCol; col++)
            {
                int frameNumber = this.frameNumberFromRowCol(row, col);
                frames.add(frameNumber);
            }
        }

        return frames;
    }

    private LatLon frameOrigin(int frameNumber)
    {
        int[] rowCol = this.rowColFromFrameNumber(frameNumber);
        double nsPixelConst = this.frameStructure.getNorthSouthPixelConstant();
        double ewPixelConst = this.frameStructure.getEastWestPixelConstant();
        double equatorwardExtent = this.frameStructure.getEquatorwardExtent().degrees;
        double pixelRowsPerFrame = RPFFrameStructure.getPixelRowsPerFrame();
        return new LatLon(
            computeLatitudinalFrameOrigin(rowCol[0], nsPixelConst, equatorwardExtent, pixelRowsPerFrame),
            computeLongitudinalFrameOrigin(rowCol[1], ewPixelConst, pixelRowsPerFrame));
    }

    /* [Section 30.3.1, MIL-C-89038] */
    /* [Section A.3.3.1, MIL-PRF-89041A] */
    private static Angle computeLatitudinalFrameOrigin(int row, double northSouthPixelConstant,
        double equatorwardExtent, double pixelRowsPerFrame)
    {
        double degrees = (90d / northSouthPixelConstant) * pixelRowsPerFrame * (row + 1)
            + equatorwardExtent;
        return Angle.fromDegrees(degrees);
    }

    /* [Section 30.3.2, MIL-C-89038] */
    /* [Section A.3.3.2, MIL-PRF-89041A] */
    private static Angle computeLongitudinalFrameOrigin(int column, double eastWestPixelConstant,
        double pixelRowsPerFrame)
    {
        double degrees = (360d / eastWestPixelConstant) * pixelRowsPerFrame
            * column - 180d;
        return Angle.fromDegrees(degrees);
    }

    private static Angle computeLatitudinalFrameExtent(double polewardExtent, double equatorwardExtent,
        double latitudinalFrames)
    {
        double degrees = Math.abs(polewardExtent - equatorwardExtent) / latitudinalFrames;
        return Angle.fromDegrees(degrees);
    }

    private static Angle computeLongitudinalFrameExtent(double longitudinalFrames)
    {
        double degrees = 360d / longitudinalFrames;
        return Angle.fromDegrees(degrees);
    }

    // ============== Frame Numbering ======================= //
    // ============== Frame Numbering ======================= //
    // ============== Frame Numbering ======================= //

    private int frameNumberFromRowCol(int row, int column)
    {
        return computeFrameNumber(row, column, this.frameStructure.getLongitudinalFrames());
    }

    private int[] rowColFromFrameNumber(int frameNumber)
    {
        int lonFrames = this.frameStructure.getLongitudinalFrames();
        return computeFrameRowAndColumn(frameNumber, lonFrames);
    }

    public int getMaximumFrameNumber()
    {
        return computeMaximumFrameNumber(this.frameStructure.getLatitudinalFrames(),
            this.frameStructure.getLongitudinalFrames());
    }
}
