package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.formats.nitfs.*;
import gov.nasa.worldwind.util.WWIO;

import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.text.MessageFormat;
/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author lado
 * @version $Id: RpfImageFile Apr 8, 2007 8:51:57 AM
 */
public class RPFImageFile extends RPFFile
{
    private NITFSImageSegment imageSegment = null;
    private UserDefinedImageSubheader imageSubheader = null;
    private RPFFrameFileComponents rpfFrameFileComponents = null;

    public RPFFrameFileComponents getRPFFrameFileComponents()
    {
        return this.rpfFrameFileComponents;
    }

    public UserDefinedImageSubheader getImageSubheader()
    {
        return this.imageSubheader;
    }

    public NITFSImageSegment getImageSegment()
    {
        return this.imageSegment;
    }


    
    private RPFImageFile(java.io.File rpfFile) throws java.io.IOException, NITFSRuntimeException
    {
        super(rpfFile);

        this.imageSegment = (NITFSImageSegment) this.getNITFSSegment(NITFSSegmentType.IMAGE_SEGMENT);
        this.validateRPFImage();

        this.imageSubheader = this.imageSegment.getUserDefinedImageSubheader();
        this.rpfFrameFileComponents = this.imageSubheader.getRPFFrameFileComponents();
    }

    private void validateRPFImage() throws NITFSRuntimeException
    {
        if ( null == this.imageSegment )
            throw new NITFSRuntimeException("NITFSReader.ImageSegmentWasNotFound");
        if( null == this.imageSegment.getUserDefinedImageSubheader())
            throw new NITFSRuntimeException("NITFSReader.UserDefinedImageSubheaderWasNotFound");
        if( null == this.imageSegment.getUserDefinedImageSubheader().getRPFFrameFileComponents())
            throw new NITFSRuntimeException("NITFSReader.RPFFrameFileComponentsWereNotFoundInUserDefinedImageSubheader");
    }

    public int[] getImagePixelsAsArray(int[] dest, RPFImageType imageType)
    {
        IntBuffer buffer = IntBuffer.wrap(dest);
        this.getImagePixelsAsBuffer(buffer, imageType);
        return dest;
    }

    public ByteBuffer getImageAsDdsTexture()
    {
        if (null != this.imageSegment)
            return this.imageSegment.getImageAsDdsTexture();
        return null;
    }


    public IntBuffer getImagePixelsAsBuffer(IntBuffer dest, RPFImageType imageType)
    {
        if (null != this.imageSegment)
            this.imageSegment.getImagePixelsAsArray(dest, imageType);
        return dest;
    }

    public BufferedImage getBufferedImage()
    {
        if (null == this.imageSegment)
            return null;

        BufferedImage bimage = new BufferedImage(
            this.getImageSegment().numSignificantCols,
            this.getImageSegment().numSignificantRows,
            BufferedImage.TYPE_INT_ARGB);

        WritableRaster raster = bimage.getRaster();
        java.awt.image.DataBufferInt dataBuffer = (java.awt.image.DataBufferInt) raster.getDataBuffer();

        IntBuffer buffer = IntBuffer.wrap(dataBuffer.getData());
        this.getImageSegment().getImagePixelsAsArray(buffer, RPFImageType.IMAGE_TYPE_ALPHA_RGB);
        return bimage;
    }

    public boolean hasTransparentAreas()
    {
        if(null != this.imageSegment)
            return (this.imageSegment.hasTransparentPixels() || this.imageSegment.hasMaskedSubframes());
        return false;
    }

    public static RPFImageFile load(java.io.File rpfFile) throws java.io.IOException, NITFSRuntimeException {
        return new RPFImageFile(rpfFile);
    }

    public static void main(String args[]) throws IOException
    {
        String cibFilename = (Configuration.isWindowsOS())
            ? "C:\\depot\\WorldWindJ\\utils\\gdal\\020g222a.i42" : "/depot/WorldWindJ/utils/gdal/020g222a.i42";
        String cadrgFilename = (Configuration.isWindowsOS())
                    ? "C:\\depot\\nitfs\\CADRG\\CTLM50\\CT50Z02\\02F7W053.TL2" : "/depot/nitfs/CADRG/CTLM50/CT50Z02/02F7W053.TL2";
        String transparentCadrgFilename = (Configuration.isWindowsOS())
                    ? "C:\\depot\\nitfs\\CADRG\\CTLM50\\CT50Z02\\0D6MM013.TL1" : "/depot/nitfs/CADRG/CTLM50/CT50Z02/0D6MM013.TL1";

        //noinspection UnnecessaryLocalVariable
        String rpfFilename = cibFilename; // cibFilename; // cadrgFilename; transparentCadrgFilename;

        try
        {
            long startTime = System.currentTimeMillis();

            RPFImageFile rpfImageFile = load(new File(rpfFilename));

// ---------- getImageAsDdsTexture example -----------------------

            ByteBuffer ddsBuffer = rpfImageFile.getImageAsDdsTexture();
            System.out.println(MessageFormat.format("RPF file loaded in {0} mSec", (System.currentTimeMillis() - startTime)));
            WWIO.saveBuffer(ddsBuffer, new File(
                (Configuration.isWindowsOS()) ?  "c:\\depot\\nitfs\\DDS\\test.dds" : "/depot/nitfs/DDS/test.dds"
            ));

// ---------- getImageAsArray example -----------------------
//
//            int size = rpfImageFile.getImageSegment().numSignificantCols
//                * rpfImageFile.getImageSegment().numSignificantRows;
//            IntBuffer rgbaBuffer = IntBuffer.allocate(size);
//            rpfImageFile.getImagePixelsAsArray(rgbaBuffer.array(), RPFImageType.IMAGE_TYPE_RGB_ALPHA);
//
//            System.out.println(MessageFormat.format("RPF file loaded in {0} mSec",
//                (System.currentTimeMillis() - startTime)));

// ---------- getBufferedImage example -----------------------
//            BufferedImage bimage = rpfImageFile.getBufferedImage();
//
//            System.out.println(MessageFormat.format("RPF file loaded in {0} mSec",
//                (System.currentTimeMillis() - startTime)));
//
//            Icon icon = new ImageIcon(bimage);
//            JLabel label = new JLabel(icon);
//
//            final JFrame f = new JFrame("RPF Viewer");
//            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//            f.getContentPane().add(label);
//            f.pack();
//            SwingUtilities.invokeLater(new Runnable()
//            {
//                public void run()
//                {
//                    f.setLocationRelativeTo(null);
//                    f.setVisible(true);
//                }
//            });
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}

