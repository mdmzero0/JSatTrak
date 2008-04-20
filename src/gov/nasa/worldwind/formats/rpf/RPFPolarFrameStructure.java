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
class RPFPolarFrameStructure extends RPFFrameStructure
{
    private final int polarPixelConstant;
    private final int numFrames;
    private final Angle equatorwardExtent;
    private final Angle polewardExtent;

    // TODO: include supporting sources for CADRG in docs

    private RPFPolarFrameStructure(
        int polarPixelConstant, int numFrames,
        Angle equatorwardExtent, Angle polewardExtent)
    {
        this.numFrames = numFrames;
        this.polarPixelConstant = polarPixelConstant;
        this.equatorwardExtent = equatorwardExtent;
        this.polewardExtent = polewardExtent;
    }

    public static RPFPolarFrameStructure computeStructure(char zoneCode, String rpfDataType, double resolution)
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

        int nsPixelSpacingConst = northSouthPixelSpacingConstant();
        int nsPixelConst = RPFNonpolarFrameStructure.computeNorthSouthPixelConstant(nsPixelSpacingConst, rpfDataType,
            resolution);
        int polarPixelConst = computePolarPixelConstant(nsPixelSpacingConst, nsPixelConst, rpfDataType, resolution);
        int frames = computePolarFrames(polarPixelConst, rpfDataType);
        Angle equatorwardExtent = computeEquatorwardExtent(equatorwardNominalBoundary(zoneCode));
        Angle polewardExtent = computePolewardExtent(polewardNominalBoundary(zoneCode));
        return new RPFPolarFrameStructure(
            polarPixelConst, frames,
            equatorwardExtent, polewardExtent);
    }

    public final int getPolarPixelConstant()
    {
        return this.polarPixelConstant;
    }

    public final int getNumFrames()
    {
        return this.numFrames;
    }

    public final Angle getEquatorwardExtent()
    {
        return this.equatorwardExtent;
    }

    public final Angle getPolewardExtent()
    {
        return this.polewardExtent;
    }

    // ============== Pixel Constants ======================= //
    // ============== Pixel Constants ======================= //
    // ============== Pixel Constants ======================= //

    private static int computePolarPixelConstant(double northSouthPixelSpacingConstant,
        double northSouthPixelConstant, String rpfDataType, double scaleOrGSD)
    {
        if (RPFDataSeries.isCADRGDataType(rpfDataType))
            return computePolarPixelConstantCADRG(northSouthPixelSpacingConstant, scaleOrGSD);
        else if (RPFDataSeries.isCIBDataType(rpfDataType))
            return computePolarPixelConstantCIB(northSouthPixelConstant);
        return -1;
    }

    /* [Section 60.2.1, MIL-C-89038] */
    private static int computePolarPixelConstantCADRG(double northSouthPixelSpacingConstant, double scale)
    {
        double S = 1000000d / scale;
        double tmp = northSouthPixelSpacingConstant * S;
        tmp = 512d * (int) Math.ceil(tmp / 512d);
        tmp *= (20d / 360d);
        tmp /= (150d / 100d);
        tmp = 512d * (int) Math.round(tmp / 512d);
        return (int) (tmp * 360d / 20d);
    }

    /* [Section A.5.2.1, MIL-PRF-89041A */
    private static int computePolarPixelConstantCIB(double northSouthPixelConstant)
    {
        double tmp = northSouthPixelConstant * 20d / 90d;
        tmp = 512d * (int) Math.round(tmp / 512d);
        return (int) (tmp * 90d / 20d);
    }

    // ============== Frame Counts ======================= //
    // ============== Frame Counts ======================= //
    // ============== Frame Counts ======================= //

    private static int computePolarFrames(double polarPixelConstant, String rpfDataType)
    {
        if (RPFDataSeries.isCADRGDataType(rpfDataType))
            return computePolarFramesCADRG(polarPixelConstant);
        else if (RPFDataSeries.isCIBDataType(rpfDataType))
            return computePolarFramesCIB(polarPixelConstant);
        return -1;
    }

    /* [Section 60.2.3, MIL-C-89038] */
    private static int computePolarFramesCADRG(double polarPixelConstant)
    {
        double tmp = polarPixelConstant * 20d / 360d;
        tmp /= 256d;
        tmp /= 6d;
        tmp = Math.ceil(tmp);
        if (((int) tmp) % 2 == 0)
            tmp = tmp + 1;
        return (int) tmp;
    }

    /* [Section A.5.2.2, MIL-PRF-89041A] */
    private static int computePolarFramesCIB(double polarPixelConstant)
    {
        double tmp = polarPixelConstant * 20d / 90d;
        tmp /= 256d;
        tmp += 4d;
        tmp /= 6d;
        tmp = Math.ceil(tmp);
        if (((int) tmp) % 2 == 0)
            tmp = tmp + 1;
        return (int) tmp;
    }

    // ============== Extents ======================= //
    // ============== Extents ======================= //
    // ============== Extents ======================= //

    /* [Section A.5.2.3, MIL-PRF-89041A] */
    private static Angle computeEquatorwardExtent(double equatorwardNominalBoundary)
    {
        return Angle.fromDegrees(equatorwardNominalBoundary);
    }

    /* [Section A.5.2.3, MIL-PRF-89041A] */
    private static Angle computePolewardExtent(double polewardNominalBoundary)
    {
        return Angle.fromDegrees(polewardNominalBoundary);
    }
}
