// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GraphNode.java

package com.sun.media;

import javax.media.*;

class GraphNode
{

    GraphNode(PlugIn plugin, Format input, GraphNode prev, int level)
    {
        this(plugin != null ? plugin.getClass().getName() : null, plugin, input, prev, level);
    }

    GraphNode(String cname, PlugIn plugin, Format input, GraphNode prev, int level)
    {
        type = -1;
        output = null;
        failed = false;
        custom = false;
        attemptedIdx = 0;
        attempted = null;
        this.cname = cname;
        this.plugin = plugin;
        this.input = input;
        this.prev = prev;
        this.level = level;
    }

    GraphNode(GraphNode gn, Format input, GraphNode prev, int level)
    {
        type = -1;
        output = null;
        failed = false;
        custom = false;
        attemptedIdx = 0;
        attempted = null;
        cname = gn.cname;
        plugin = gn.plugin;
        type = gn.type;
        custom = gn.custom;
        this.input = input;
        this.prev = prev;
        this.level = level;
        supportedIns = gn.supportedIns;
        if(gn.input == input)
            supportedOuts = gn.supportedOuts;
    }

    Format[] getSupportedInputs()
    {
        if(supportedIns != null)
            return supportedIns;
        if(plugin == null)
            return null;
        if((type == -1 || type == 2) && (plugin instanceof Codec))
            supportedIns = ((Codec)plugin).getSupportedInputFormats();
        else
        if((type == -1 || type == 4) && (plugin instanceof Renderer))
            supportedIns = ((Renderer)plugin).getSupportedInputFormats();
        else
        if(plugin instanceof Multiplexer)
            supportedIns = ((Multiplexer)plugin).getSupportedInputFormats();
        return supportedIns;
    }

    Format[] getSupportedOutputs(Format in)
    {
        if(in == input && supportedOuts != null)
            return supportedOuts;
        if(plugin == null)
            return null;
        if((type == -1 || type == 4) && (plugin instanceof Renderer))
            return null;
        if((type == -1 || type == 2) && (plugin instanceof Codec))
        {
            Format outs[] = ((Codec)plugin).getSupportedOutputFormats(in);
            if(input == in)
                supportedOuts = outs;
            return outs;
        } else
        {
            return null;
        }
    }

    public void resetAttempted()
    {
        attemptedIdx = 0;
        attempted = null;
    }

    boolean checkAttempted(Format input)
    {
        if(attempted == null)
        {
            attempted = new Format[ARRAY_INC];
            attempted[attemptedIdx++] = input;
            return false;
        }
        for(int j = 0; j < attemptedIdx; j++)
            if(input.equals(attempted[j]))
                return true;

        if(attemptedIdx >= attempted.length)
        {
            Format newarray[] = new Format[attempted.length + ARRAY_INC];
            System.arraycopy(attempted, 0, newarray, 0, attempted.length);
            attempted = newarray;
        }
        attempted[attemptedIdx++] = input;
        return false;
    }

    Class clz;
    String cname;
    PlugIn plugin;
    int type;
    Format input;
    Format output;
    Format supportedIns[];
    Format supportedOuts[];
    GraphNode prev;
    int level;
    boolean failed;
    boolean custom;
    static int ARRAY_INC = 30;
    int attemptedIdx;
    Format attempted[];

}
