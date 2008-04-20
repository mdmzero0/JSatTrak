/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.rpf;

/**
 * @author dcollins
 * @version $Id$
 */
class RPFFrameStructure
{
    private static final int PIXEL_ROWS_PER_FRAME = 1536;
    private static final int SUBFRAME_ROWS_PER_FRAME = 6;

    public static int getPixelRowsPerFrame()
    {
        return PIXEL_ROWS_PER_FRAME;
    }

    public static int getSubframeRowsPerFrame()
    {
        return SUBFRAME_ROWS_PER_FRAME;
    }

    // ============== Pixel Spacing Constants ======================= //
    // ============== Pixel Spacing Constants ======================= //
    // ============== Pixel Spacing Constants ======================= //

    /* [Table III, Section 70, MIL-A-89007] */
    private static final int[] EAST_WEST_PIXEL_SPACING_CONSTANT = {
        369664, 302592, 245760, 199168, 163328, 137216, 110080, 82432};

    /* [Table III, Section 70, MIL-A-89007] */
    private static final int NORTH_SOUTH_PIXEL_SPACING_CONSTANT = 400384;

    static int eastWestPixelSpacingConstant(char zoneCode)
    {
        int index = RPFZone.indexFor(zoneCode) % 8;
        if (index < 0)
            return -1;

        return EAST_WEST_PIXEL_SPACING_CONSTANT[index];
    }

    static int northSouthPixelSpacingConstant()
    {
        return NORTH_SOUTH_PIXEL_SPACING_CONSTANT;
    }

    // ============== Nominal Boundaries ======================= //
    // ============== Nominal Boundaries ======================= //
    // ============== Nominal Boundaries ======================= //

    /* [Table III, Section 70, MIL-A-89007] */
    private static final int[] EQUATORWARD_NOMINAL_BOUNDARY = {
        0,  32, 48, 56, 64, 68, 72, 76, 80};

    /* [Table III, Section 70, MIL-A-89007] */
    private static final int[] POLEWARD_NOMINAL_BOUNDARY = {
        32, 48, 56, 64, 68, 72, 76, 80, 90};

    static int equatorwardNominalBoundary(char zoneCode)
    {
        return nominalBoundary(zoneCode, EQUATORWARD_NOMINAL_BOUNDARY);
    }

    static int polewardNominalBoundary(char zoneCode)
    {
        return nominalBoundary(zoneCode, POLEWARD_NOMINAL_BOUNDARY);
    }

    private static int nominalBoundary(char zoneCode, int[] boundaryArray)
    {
        int index = RPFZone.indexFor(zoneCode) % 9;
        if (index < 0)
            return -1;

        if (!RPFZone.isZoneInUpperHemisphere(zoneCode))
            return 0 - boundaryArray[index];
        return boundaryArray[index];
    }
}
