// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProcessorModel.java

package javax.media;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

// Referenced classes of package javax.media:
//            Format, MediaLocator

public class ProcessorModel
{

    public ProcessorModel()
    {
        formats = null;
        inputLocator = null;
        inputDataSource = null;
        outputContentDescriptor = null;
    }

    public ProcessorModel(Format formats[], ContentDescriptor outputContentDescriptor)
    {
        this.formats = null;
        inputLocator = null;
        inputDataSource = null;
        this.outputContentDescriptor = null;
        this.outputContentDescriptor = outputContentDescriptor;
        this.formats = formats;
    }

    public ProcessorModel(DataSource inputDataSource, Format formats[], ContentDescriptor outputContentDescriptor)
    {
        this.formats = null;
        inputLocator = null;
        this.inputDataSource = null;
        this.outputContentDescriptor = null;
        this.inputDataSource = inputDataSource;
        this.formats = formats;
        this.outputContentDescriptor = outputContentDescriptor;
    }

    public ProcessorModel(MediaLocator inputLocator, Format formats[], ContentDescriptor outputContentDescriptor)
    {
        this.formats = null;
        this.inputLocator = null;
        inputDataSource = null;
        this.outputContentDescriptor = null;
        this.inputLocator = inputLocator;
        this.formats = formats;
        this.outputContentDescriptor = outputContentDescriptor;
    }

    public int getTrackCount(int availableTrackCount)
    {
        if(formats != null)
            return formats.length;
        else
            return -1;
    }

    public Format getOutputTrackFormat(int tIndex)
    {
        if(formats != null && formats.length > tIndex)
            return formats[tIndex];
        else
            return null;
    }

    public boolean isFormatAcceptable(int tIndex, Format tFormat)
    {
        if(formats != null && formats.length > tIndex)
            return tFormat.matches(formats[tIndex]);
        else
            return true;
    }

    public ContentDescriptor getContentDescriptor()
    {
        return outputContentDescriptor;
    }

    public DataSource getInputDataSource()
    {
        return inputDataSource;
    }

    public MediaLocator getInputLocator()
    {
        return inputLocator;
    }

    private Format formats[];
    private MediaLocator inputLocator;
    private DataSource inputDataSource;
    private ContentDescriptor outputContentDescriptor;
}
