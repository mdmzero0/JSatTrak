package gov.nasa.worldwind.formats.rpf;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.formats.nitfs.*;

import java.io.*;
import java.text.MessageFormat;
/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author Lado Garakanidze
 * @version $Id: RpfTocFile Apr 4, 2007 2:24:00 PM lado
 */
public class RPFTOCFile extends RPFFile
{
    private RPFFileComponents rpfFileComponents;

    public RPFHeaderSection getHeaderSection()
    {
        return (null != this.rpfFileComponents) ? this.rpfFileComponents.getRPFHeaderSection() : null;
    }

    public RPFFrameFileIndexSection getFrameFileIndexSection()
    {
        return (null != this.rpfFileComponents) ? this.rpfFileComponents.getRPFFrameFileIndexSection() : null;
    }

    public RPFFileComponents getRPFFileComponents()
    {
        return this.rpfFileComponents;
    }

    protected RPFTOCFile(java.io.File rpfFile) throws IOException, NITFSRuntimeException {
        super(rpfFile);

        RPFUserDefinedHeaderSegment segment =
            (RPFUserDefinedHeaderSegment)this.getNITFSSegment( NITFSSegmentType.USER_DEFINED_HEADER_SEGMENT);

        if(null ==  segment)
            throw new NITFSRuntimeException("NITFSReader.UserDefinedHeaderSegmentWasNotFound");

        this.rpfFileComponents = segment.getRPFFileComponents();
        if(null == this.rpfFileComponents)
            throw new NITFSRuntimeException("NITFSReader.RPFFileComponents.Were.Not.Found.In.UserDefinedHeaderSegment");
    }

    public static RPFTOCFile load(java.io.File tocFile) throws java.io.IOException
    {
        return new RPFTOCFile(tocFile);
    }

    public static void main(String args[])
    {
        String testTOCFilename = (Configuration.isWindowsOS()) ?  "C:\\RPF\\A.TOC" : "/depot/WorldWindJ/utils/rpf/A.TOC";
        try
        {
            long startTime = System.currentTimeMillis();

            RPFTOCFile toc = load(new File(testTOCFilename));

            System.out.println(MessageFormat.format("TOC file loaded in {0} mSec", (System.currentTimeMillis() - startTime)));
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
