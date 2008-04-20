/* 
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dcollins
 * @version $Id$
 */
public abstract class RPFFrameTransform
{
    RPFFrameTransform()
    {
    }

    public static RPFFrameTransform createFrameTransform(char zoneCode, String rpfDataType, double resolution)
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

        return getOrCreateFrameTransform(zoneCode, rpfDataType, resolution);
    }

    private static RPFFrameTransform newFrameTransform(char zoneCode, String rpfDataType, double resolution)
    {
        boolean isNonpolarZone = !RPFZone.isPolarZone(zoneCode);
        if (isNonpolarZone)
            return RPFNonpolarFrameTransform.createNonpolarFrameTransform(zoneCode, rpfDataType, resolution);
        else
            return null;
//            frameTransform = RPFPolarFrameTransform.createPolarTransform(zoneCode, rpfDataType, resolution);
    }
    
    public abstract int frameAtLatLon(Angle latitude, Angle longitude);

    public abstract Sector frameExtent(int frameNumber);

    public abstract Iterable<Integer> framesInSector(Sector sector);

    public abstract int getMaximumFrameNumber();

    // ============== Frame Numbering ======================= //
    // ============== Frame Numbering ======================= //
    // ============== Frame Numbering ======================= //

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    static int computeFrameNumber(int row, int column, int columnFrames)
    {
        return column + row * columnFrames;
    }

    static int[] computeFrameRowAndColumn(int frameNumber, int columnFrames)
    {
        int frameRow = computeFrameRow(frameNumber, columnFrames);
        int frameCol = computeFrameColumn(frameNumber, frameRow, columnFrames);
        return new int[]{frameRow, frameCol};
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    private static int computeFrameColumn(int frameNumber, int frameRow, int columnFrames)
    {
        return frameNumber - (frameRow * columnFrames);
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    private static int computeFrameRow(int frameNumber, int columnFrames)
    {
        return (int) (frameNumber / (double) columnFrames);
    }

    /* [Section 30.6, MIL-C-89038] */
    /* [Section A.3.6, MIL-PRF-89041A] */
    static int computeMaximumFrameNumber(int rowFrames, int columnFrames)
    {
        return (rowFrames * columnFrames) - 1;
    }

    // ============== Cached Transforms ======================= //
    // ============== Cached Transforms ======================= //
    // ============== Cached Transforms ======================= //

    private static Map<Key, RPFFrameTransform> cachedFrameTransforms = null;

    private static RPFFrameTransform getOrCreateFrameTransform(char zoneCode, String rpfDataType, double resolution)
    {
        if (cachedFrameTransforms == null)
            cachedFrameTransforms = new HashMap<Key, RPFFrameTransform>();

        Key key = new Key(zoneCode, rpfDataType, resolution);
        RPFFrameTransform frameTransform = cachedFrameTransforms.get(key);
        if (frameTransform == null)
        {
            frameTransform = newFrameTransform(zoneCode, rpfDataType, resolution);
            if (frameTransform != null)
                cachedFrameTransforms.put(key, frameTransform);
        }

        return frameTransform;
    }

    private static class Key
    {
        private final char zoneCode;
        private final String rpfDataType;
        private final double resolution;
        private int hashCode;

        public Key(char zoneCode, String rpfDataType, double resolution)
        {
            this.zoneCode = zoneCode;
            this.rpfDataType = rpfDataType;
            this.resolution = resolution;
            this.hashCode = this.computeHash();
        }

        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null || this.getClass() != obj.getClass())
                return false;

            Key that = (Key) obj;

            if (Double.compare(that.resolution, this.resolution) != 0)
                return false;
            if (this.zoneCode != that.zoneCode)
                return false;
            //noinspection RedundantIfStatement
            if (this.rpfDataType != null ? !this.rpfDataType.equalsIgnoreCase(that.rpfDataType) : that.rpfDataType != null)
                return false;

            return true;
        }

        private int computeHash()
        {
            int result;
            long temp;
            result = (int) this.zoneCode;
            result = 31 * result + (this.rpfDataType != null ? this.rpfDataType.hashCode() : 0);
            temp = this.resolution != +0.0d ? Double.doubleToLongBits(this.resolution) : 0L;
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        public int hashCode()
        {
            return this.hashCode;
        }
    }
}
