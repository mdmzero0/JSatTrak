/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id$
 */
class RPFNonpolarFrameStructure extends RPFFrameStructure
{
    private final int eastWestPixelConstant;
    private final int northSouthPixelConstant;
    private final int latitudinalFrames;
    private final int longitudinalFrames;
    private final Angle equatorwardExtent;
    private final Angle polewardExtent;

    private RPFNonpolarFrameStructure(
        int eastWestPixelConstant, int northSouthPixelConstant,
        int latitudinalFrames, int longitudinalFrames,
        Angle equatorwardExtent, Angle polewardExtent)
    {
        this.eastWestPixelConstant = eastWestPixelConstant;
        this.northSouthPixelConstant = northSouthPixelConstant;
        this.latitudinalFrames = latitudinalFrames;
        this.longitudinalFrames = longitudinalFrames;
        this.equatorwardExtent = equatorwardExtent;
        this.polewardExtent = polewardExtent;
    }

    public static RPFNonpolarFrameStructure computeStructure(char zoneCode, String rpfDataType, double resolution)
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

        // Constant zone properties.
        int ewPixelSpacingConst = eastWestPixelSpacingConstant(zoneCode);
        int nsPixelSpacingConst = northSouthPixelSpacingConstant();
        int equatorwardNominalBound = equatorwardNominalBoundary(zoneCode);
        int polewardNominalBound = polewardNominalBoundary(zoneCode);
        // Scale/GSD specific zone properties.
        int ewPixelConst = computeEastWestPixelConstant(ewPixelSpacingConst, rpfDataType, resolution);
        int nsPixelConst = computeNorthSouthPixelConstant(nsPixelSpacingConst, rpfDataType, resolution);
        Angle equatorwardExtent = computeEquatorwardExtent(equatorwardNominalBound, nsPixelConst,
            getPixelRowsPerFrame());
        Angle polewardExtent = computePolewardExtent(polewardNominalBound, nsPixelConst, getPixelRowsPerFrame());
        int latFrames = computeLatitudinalFrames(polewardExtent.degrees, equatorwardExtent.degrees, nsPixelConst,
            getPixelRowsPerFrame());
        int lonFrames = computeLongitudinalFrames(ewPixelConst, getPixelRowsPerFrame());
        return new RPFNonpolarFrameStructure(
            ewPixelConst, nsPixelConst,
            latFrames, lonFrames,
            equatorwardExtent, polewardExtent);
    }

    public final int getEastWestPixelConstant()
    {
        return this.eastWestPixelConstant;
    }

    public final int getNorthSouthPixelConstant()
    {
        return this.northSouthPixelConstant;
    }

    public final int getLatitudinalFrames()
    {
        return this.latitudinalFrames;
    }

    public final int getLongitudinalFrames()
    {
        return this.longitudinalFrames;
    }

    public final Angle getEquatorwardExtent()
    {
        return this.equatorwardExtent;
    }

    public final Angle getPolewardExtent()
    {
        return this.polewardExtent;
    }

    private static double clamp(double x, double min, double max)
    {
        return (x < min) ? min : ((x > max) ? max : x);
    }

    // ============== Pixel Constants ======================= //
    // ============== Pixel Constants ======================= //
    // ============== Pixel Constants ======================= //

    private static int computeEastWestPixelConstant(double eastWestPixelSpacingConstant, String rpfDataType,
        double scaleOrGSD)
    {
        if (RPFDataSeries.isCADRGDataType(rpfDataType))
            return computeEastWestPixelConstantCADRG(eastWestPixelSpacingConstant, scaleOrGSD);
        else if (RPFDataSeries.isCIBDataType(rpfDataType))
            return computeEastWestPixelConstantCIB(eastWestPixelSpacingConstant, scaleOrGSD);
        return -1;
    }

    /* [Section 60.1.2, MIL-C-89038] */
    private static int computeEastWestPixelConstantCADRG(double eastWestPixelSpacingConstant, double scale)
    {
        double S = 1000000d / scale;
        double tmp = eastWestPixelSpacingConstant * S;
        tmp = 512d * (int) Math.ceil(tmp / 512d);
        tmp /= (150d / 100d);
        return 256 * (int) Math.round(tmp / 256d);
    }

    /* [Section A.5.1.1, MIL-PRF-89041A] */
    private static int computeEastWestPixelConstantCIB(double eastWestPixelSpacingConstant,
        double groundSampleDistance)
    {
        double S = 100d / groundSampleDistance;
        double tmp = eastWestPixelSpacingConstant * S;
        return 512 * (int) Math.ceil(tmp / 512d);
    }

    static int computeNorthSouthPixelConstant(double northSouthPixelSpacingConstant, String rpfDataType,
        double scaleOrGSD)
    {
        if (RPFDataSeries.isCADRGDataType(rpfDataType))
            return computeNorthSouthPixelConstantCADRG(northSouthPixelSpacingConstant, scaleOrGSD);
        else if (RPFDataSeries.isCIBDataType(rpfDataType))
            return computeNorthSouthPixelConstantCIB(northSouthPixelSpacingConstant, scaleOrGSD);
        return -1;
    }

    /* [Section 60.1.1, MIL-C-89038] */
    private static int computeNorthSouthPixelConstantCADRG(double northSouthPixelConstant, double scale)
    {
        double S = 1000000d / scale;
        double tmp = northSouthPixelConstant * S;
        tmp = 512d * (int) Math.ceil(tmp / 512d);
        tmp /= 4d;
        tmp /= (150d / 100d);
        return 256 * (int) Math.round(tmp / 256d);
    }

    /* [Section A.5.1.1, MIL-PRF-89041A] */
    private static int computeNorthSouthPixelConstantCIB(double northSouthPixelSpacingConstant,
        double groundSampleDistance)
    {
        double S = 100d / groundSampleDistance;
        double tmp = northSouthPixelSpacingConstant * S;
        tmp = 512d * (int) Math.ceil(tmp / 512d);
        tmp /= 4d;
        return 256 * (int) Math.round(tmp / 256d);
    }

    // ============== Frame Counts ======================= //
    // ============== Frame Counts ======================= //
    // ============== Frame Counts ======================= //

    /* [Section 60.1.6, MIL-C-89038] */
    /* [Section A.5.1.3, MIL-PRF-89041A] */
    private static int computeLatitudinalFrames(double polewardExtentDegrees, double equatorwardExtentDegrees,
        double northSouthPixelConstant, double pixelRowsPerFrame)
    {
        double nsPixelsPerDegree = northSouthPixelConstant / 90d;
        double extent = Math.abs(polewardExtentDegrees - equatorwardExtentDegrees);
        return (int) Math.rint(extent * nsPixelsPerDegree / pixelRowsPerFrame);
    }

    /* [Section 60.1.7, MIL-C-89038] */
    /* [Section A.5.1.4, MIL-PRF-89041A] */
    private static int computeLongitudinalFrames(double eastWestPixelConstant, double pixelRowsPerFrame)
    {
        return (int) Math.ceil(eastWestPixelConstant / pixelRowsPerFrame);
    }

    // ============== Extents ======================= //
    // ============== Extents ======================= //
    // ============== Extents ======================= //

    /* [Section 60.1.5.c, MIL-C-89038] */
    /* [Section A.5.1.2.c, MIL-PRF-89041A] */
    private static Angle computeEquatorwardExtent(double equatorwardNominalBoundary, double northSouthPixelConstant,
        double pixelRowsPerFrame)
    {
        double nsPixelsPerDegree = northSouthPixelConstant / 90d;
        double degrees = Math.signum(equatorwardNominalBoundary)
            * clamp((int) (nsPixelsPerDegree * Math.abs(equatorwardNominalBoundary) / pixelRowsPerFrame)
            * pixelRowsPerFrame / nsPixelsPerDegree, 0, 90);
        return Angle.fromDegrees(degrees);
    }

    /* [Section 60.1.5.b, MIL-C-89038] */
    /* [Section A.5.1.2.b, MIL-PRF-89041A] */
    private static Angle computePolewardExtent(double polewardNominalBoundary, double northSouthPixelConstant,
        double pixelRowsPerFrame)
    {
        double nsPixelsPerDegree = northSouthPixelConstant / 90d;
        double degrees = Math.signum(polewardNominalBoundary)
            * clamp(Math.ceil(nsPixelsPerDegree * Math.abs(polewardNominalBoundary) / pixelRowsPerFrame)
            * pixelRowsPerFrame / nsPixelsPerDegree, 0, 90);
        return Angle.fromDegrees(degrees);
    }
}
