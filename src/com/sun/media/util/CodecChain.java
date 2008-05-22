// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   CodecChain.java

package com.sun.media.util;

import com.sun.media.*;
import java.awt.Component;
import java.util.Vector;
import javax.media.*;

public class CodecChain
{

    public CodecChain()
    {
        codecs = null;
        buffers = null;
        formats = null;
        renderer = null;
        deallocated = true;
        firstBuffer = true;
        rtpFormat = false;
    }

    boolean isRawFormat(Format format)
    {
        return false;
    }

    public void reset()
    {
        firstBuffer = true;
        for(int i = 0; i < codecs.length; i++)
            if(codecs[i] != null)
                codecs[i].reset();

    }

    public int process(Buffer buffer, boolean render)
    {
        int codecNo = 0;
        return doProcess(codecNo, buffer, render);
    }

    private int doProcess(int codecNo, Buffer input, boolean render)
    {
        Format format = input.getFormat();
        if(codecNo == codecs.length)
            if(render)
            {
                if(renderer != null && formats[codecNo] != null && formats[codecNo] != format && !formats[codecNo].equals(format) && !input.isDiscard())
                {
                    if(renderer.setInputFormat(format) == null)
                    {
                        Log.error("Monitor failed to handle mid-stream format change:");
                        Log.error("  old: " + formats[codecNo]);
                        Log.error("  new: " + format);
                        return 1;
                    }
                    formats[codecNo] = format;
                }
                try
                {
                    return renderer.process(input);
                }
                catch(Exception e)
                {
                    Log.dumpStack(e);
                    return 1;
                }
                catch(Error err)
                {
                    Log.dumpStack(err);
                }
                return 1;
            } else
            {
                return 0;
            }
        if(isRawFormat(format))
        {
            if(!render)
                return 0;
        } else
        if(!rtpFormat && firstBuffer)
        {
            if((input.getFlags() & 0x10) == 0)
                return 0;
            firstBuffer = false;
        }
        Codec codec = codecs[codecNo];
        if(codec != null && formats[codecNo] != null && formats[codecNo] != format && !formats[codecNo].equals(format) && !input.isDiscard())
        {
            if(codec.setInputFormat(format) == null)
            {
                Log.error("Monitor failed to handle mid-stream format change:");
                Log.error("  old: " + formats[codecNo]);
                Log.error("  new: " + format);
                return 1;
            }
            formats[codecNo] = format;
        }
        int returnVal;
        do
        {
            try
            {
                returnVal = codec.process(input, buffers[codecNo]);
            }
            catch(Exception e)
            {
                Log.dumpStack(e);
                return 1;
            }
            catch(Error err)
            {
                Log.dumpStack(err);
                return 1;
            }
            if(returnVal == 1)
                return 1;
            if((returnVal & 4) == 0)
            {
                if(!buffers[codecNo].isDiscard() && !buffers[codecNo].isEOM())
                    doProcess(codecNo + 1, buffers[codecNo], render);
                buffers[codecNo].setOffset(0);
                buffers[codecNo].setLength(0);
                buffers[codecNo].setFlags(0);
            }
        } while((returnVal & 2) != 0);
        return returnVal;
    }

    public Component getControlComponent()
    {
        return null;
    }

    public boolean prefetch()
    {
        if(!deallocated)
            return true;
        try
        {
            renderer.open();
        }
        catch(ResourceUnavailableException e)
        {
            return false;
        }
        renderer.start();
        deallocated = false;
        return true;
    }

    public void deallocate()
    {
        if(deallocated)
            return;
        if(renderer != null)
            renderer.close();
        deallocated = true;
    }

    public void close()
    {
        for(int i = 0; i < codecs.length; i++)
            codecs[i].close();

        if(renderer != null)
            renderer.close();
    }

    protected boolean buildChain(Format input)
    {
        Vector formatList = new Vector(10);
        Vector pluginList;
        if((pluginList = SimpleGraphBuilder.findRenderingChain(input, formatList)) == null)
            return false;
        int len = pluginList.size();
        codecs = new Codec[len - 1];
        buffers = new ExtBuffer[len - 1];
        formats = new Format[len];
        formats[0] = input;
        Log.comment("Monitor codec chain:");
        for(int j = 0; j < codecs.length; j++)
        {
            codecs[j] = (Codec)pluginList.elementAt(len - j - 1);
            formats[j + 1] = (Format)formatList.elementAt(len - j - 2);
            buffers[j] = new ExtBuffer();
            buffers[j].setFormat(formats[j + 1]);
            Log.write("    codec: " + codecs[j]);
            Log.write("      format: " + formats[j]);
        }

        renderer = (Renderer)pluginList.elementAt(0);
        Log.write("    renderer: " + renderer);
        Log.write("      format: " + formats[codecs.length] + "\n");
        if(input.getEncoding() != null)
        {
            String enc = input.getEncoding().toUpperCase();
            if(enc.endsWith("RTP"))
                rtpFormat = true;
        }
        return true;
    }

    static final int STAGES = 5;
    protected Codec codecs[];
    protected Buffer buffers[];
    protected Format formats[];
    protected Renderer renderer;
    private boolean deallocated;
    protected boolean firstBuffer;
    private boolean rtpFormat;
}
