// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SimpleGraphBuilder.java

package com.sun.media;

import com.sun.media.codec.audio.mpa.DePacketizer;
import com.sun.media.codec.audio.mpa.Packetizer;
import java.util.*;
import javax.media.*;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;

// Referenced classes of package com.sun.media:
//            GraphNode, BasicTrackControl, Log, GraphInspector, 
//            BasicPlugIn

public class SimpleGraphBuilder
{

    public SimpleGraphBuilder()
    {
        STAGES = 4;
        plugIns = new Hashtable(40);
        targetPlugins = null;
        targetPluginNames = null;
        targetType = -1;
        indent = 0;
    }

    public static void setGraphInspector(GraphInspector insp)
    {
        inspector = insp;
    }

    public void reset()
    {
        GraphNode n;
        for(Enumeration enum = plugIns.elements(); enum.hasMoreElements(); n.resetAttempted())
            n = (GraphNode)enum.nextElement();

    }

    boolean buildGraph(BasicTrackControl tc)
    {
        Log.comment("Input: " + tc.getOriginalFormat());
        Vector candidates = new Vector();
        GraphNode node = new GraphNode(null, (PlugIn)null, tc.getOriginalFormat(), null, 0);
        indent = 1;
        Log.setIndent(indent);
        if(!setDefaultTargets(tc.getOriginalFormat()))
            return false;
        candidates.addElement(node);
        while((node = buildGraph(candidates)) != null) 
        {
            GraphNode failed;
            if((failed = buildTrackFromGraph(tc, node)) == null)
            {
                indent = 0;
                Log.setIndent(indent);
                return true;
            }
            removeFailure(candidates, failed, tc.getOriginalFormat());
        }
        indent = 0;
        Log.setIndent(indent);
        return false;
    }

    protected GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node)
    {
        return null;
    }

    GraphNode buildGraph(Format input)
    {
        Log.comment("Input: " + input);
        Vector candidates = new Vector();
        GraphNode node = new GraphNode(null, (PlugIn)null, input, null, 0);
        indent = 1;
        Log.setIndent(indent);
        if(!setDefaultTargets(input))
            return null;
        candidates.addElement(node);
        while((node = buildGraph(candidates)) != null) 
        {
            GraphNode failed;
            if((failed = verifyGraph(node)) == null)
            {
                indent = 0;
                Log.setIndent(indent);
                return node;
            }
            removeFailure(candidates, failed, input);
        }
        indent = 0;
        Log.setIndent(indent);
        return node;
    }

    GraphNode buildGraph(Vector candidates)
    {
        GraphNode node;
        while((node = doBuildGraph(candidates)) == null) 
            if(candidates.isEmpty())
                break;
        return node;
    }

    GraphNode doBuildGraph(Vector candidates)
    {
        if(candidates.isEmpty())
            return null;
        GraphNode node = (GraphNode)candidates.firstElement();
        candidates.removeElementAt(0);
        if(node.input == null && (node.plugin == null || !(node.plugin instanceof Codec)))
        {
            Log.error("Internal error: doBuildGraph");
            return null;
        }
        int oldIndent = indent;
        Log.setIndent(node.level + 1);
        if(node.plugin != null && verifyInput(node.plugin, node.input) == null)
            return null;
        GraphNode n;
        if((n = findTarget(node)) != null)
        {
            indent = oldIndent;
            Log.setIndent(indent);
            return n;
        }
        if(node.level >= STAGES)
        {
            indent = oldIndent;
            Log.setIndent(indent);
            return null;
        }
        boolean mp3Pkt = false;
        Format input;
        Format outs[];
        if(node.plugin != null)
        {
            if(node.output != null)
            {
                outs = new Format[1];
                outs[0] = node.output;
            } else
            {
                outs = node.getSupportedOutputs(node.input);
                if(outs == null || outs.length == 0)
                {
                    indent = oldIndent;
                    Log.setIndent(indent);
                    return null;
                }
            }
            input = node.input;
            if(node.plugin instanceof Packetizer)
                mp3Pkt = true;
        } else
        {
            outs = new Format[1];
            outs[0] = node.input;
            input = null;
        }
        boolean foundSomething = false;
        for(int i = 0; i < outs.length; i++)
        {
            if(!node.custom && input != null && input.equals(outs[i]))
                continue;
            if(node.plugin != null)
            {
                if(verifyOutput(node.plugin, outs[i]) == null)
                {
                    if(inspector != null && inspector.detailMode())
                        inspector.verifyOutputFailed(node.plugin, outs[i]);
                    continue;
                }
                if(inspector != null && !inspector.verify((Codec)node.plugin, node.input, outs[i]))
                    continue;
            }
            Vector cnames = PlugInManager.getPlugInList(outs[i], null, 2);
            if(cnames != null && cnames.size() != 0)
            {
                for(int j = 0; j < cnames.size(); j++)
                {
                    GraphNode gn;
                    if((gn = getPlugInNode((String)cnames.elementAt(j), 2, plugIns)) != null && (!mp3Pkt || !(gn.plugin instanceof DePacketizer)) && !gn.checkAttempted(outs[i]))
                    {
                        Format ins[] = gn.getSupportedInputs();
                        Format fmt;
                        if((fmt = matches(outs[i], ins, null, gn.plugin)) == null)
                        {
                            if(inspector != null && inspector.detailMode())
                                inspector.verifyInputFailed(gn.plugin, outs[i]);
                        } else
                        if(inspector == null || !inspector.detailMode() || inspector.verify((Codec)gn.plugin, fmt, null))
                        {
                            n = new GraphNode(gn, fmt, node, node.level + 1);
                            candidates.addElement(n);
                            foundSomething = true;
                        }
                    }
                }

            }
        }

        indent = oldIndent;
        Log.setIndent(indent);
        return null;
    }

    GraphNode findTarget(GraphNode node)
    {
        Format outs[];
        if(node.plugin == null)
        {
            outs = new Format[1];
            outs[0] = node.input;
        } else
        if(node.output != null)
        {
            outs = new Format[1];
            outs[0] = node.output;
        } else
        {
            outs = node.getSupportedOutputs(node.input);
            if(outs == null || outs.length == 0)
                return null;
        }
        GraphNode n;
        if(targetPlugins != null && (n = verifyTargetPlugins(node, outs)) != null)
            return n;
        else
            return null;
    }

    GraphNode verifyTargetPlugins(GraphNode node, Format outs[])
    {
        for(int i = 0; i < targetPlugins.length; i++)
        {
            GraphNode gn;
            if((gn = targetPlugins[i]) == null)
            {
                String name = (String)targetPluginNames.elementAt(i);
                if(name == null)
                    continue;
                Format base[] = PlugInManager.getSupportedInputFormats(name, targetType);
                if(matches(outs, base, null, null) == null)
                    continue;
                if((gn = getPlugInNode(name, targetType, plugIns)) == null)
                {
                    targetPluginNames.setElementAt(null, i);
                    continue;
                }
                targetPlugins[i] = gn;
            }
            Format fmt;
            if((fmt = matches(outs, gn.getSupportedInputs(), node.plugin, gn.plugin)) != null && (inspector == null || (node.plugin == null || inspector.verify((Codec)node.plugin, node.input, fmt)) && (gn.type != -1 && gn.type != 2 || !(gn.plugin instanceof Codec) ? gn.type != -1 && gn.type != 4 || !(gn.plugin instanceof Renderer) || inspector.verify((Renderer)gn.plugin, fmt) : inspector.verify((Codec)gn.plugin, fmt, null))))
                return new GraphNode(gn, fmt, node, node.level + 1);
        }

        return null;
    }

    boolean setDefaultTargets(Format in)
    {
        return setDefaultTargetRenderer(in);
    }

    boolean setDefaultTargetRenderer(Format in)
    {
        if(in instanceof AudioFormat)
            targetPluginNames = PlugInManager.getPlugInList(new AudioFormat(null, -1D, -1, -1, -1, -1, -1, -1D, null), null, 4);
        else
        if(in instanceof VideoFormat)
            targetPluginNames = PlugInManager.getPlugInList(new VideoFormat(null, null, -1, null, -1F), null, 4);
        else
            targetPluginNames = PlugInManager.getPlugInList(null, null, 4);
        if(targetPluginNames == null || targetPluginNames.size() == 0)
        {
            return false;
        } else
        {
            targetPlugins = new GraphNode[targetPluginNames.size()];
            targetType = 4;
            return true;
        }
    }

    protected GraphNode verifyGraph(GraphNode node)
    {
        Format prevFormat = null;
        Vector used = new Vector(5);
        if(node.plugin == null)
            return null;
        Log.setIndent(indent++);
        for(; node != null && node.plugin != null; node = node.prev)
        {
            if(used.contains(node.plugin))
            {
                PlugIn p;
                if(node.cname == null || (p = createPlugIn(node.cname, -1)) == null)
                {
                    Log.write("Failed to instantiate " + node.cname);
                    return node;
                }
                node.plugin = p;
            } else
            {
                used.addElement(node.plugin);
            }
            if((node.type == -1 || node.type == 4) && (node.plugin instanceof Renderer))
                ((Renderer)node.plugin).setInputFormat(node.input);
            else
            if((node.type == -1 || node.type == 2) && (node.plugin instanceof Codec))
            {
                ((Codec)node.plugin).setInputFormat(node.input);
                if(prevFormat != null)
                    ((Codec)node.plugin).setOutputFormat(prevFormat);
                else
                if(node.output != null)
                    ((Codec)node.plugin).setOutputFormat(node.output);
            }
            if(node.type != -1 && node.type != 4 || !(node.plugin instanceof Renderer))
                try
                {
                    node.plugin.open();
                }
                catch(Exception e)
                {
                    Log.warning("Failed to open: " + node.plugin);
                    node.failed = true;
                    return node;
                }
            prevFormat = node.input;
        }

        Log.setIndent(indent--);
        return null;
    }

    void removeFailure(Vector candidates, GraphNode failed, Format input)
    {
        if(failed.plugin == null)
            return;
        Log.comment("Failed to open plugin " + failed.plugin + ". Will re-build the graph allover again");
        candidates.removeAllElements();
        GraphNode hsyn = new GraphNode(null, (PlugIn)null, input, null, 0);
        indent = 1;
        Log.setIndent(indent);
        candidates.addElement(hsyn);
        failed.failed = true;
        plugIns.put(failed.plugin.getClass().getName(), failed);
        for(Enumeration e = plugIns.keys(); e.hasMoreElements();)
        {
            String ss = (String)e.nextElement();
            GraphNode nn = (GraphNode)plugIns.get(ss);
            if(!nn.failed)
                plugIns.remove(ss);
        }

    }

    public static GraphNode getPlugInNode(String name, int type, Hashtable plugIns)
    {
        GraphNode gn = null;
        Object obj = null;
        boolean add = false;
        if(plugIns == null || (gn = (GraphNode)plugIns.get(name)) == null)
        {
            PlugIn p = createPlugIn(name, type);
            gn = new GraphNode(name, p, null, null, 0);
            if(plugIns != null)
                plugIns.put(name, gn);
            if(p == null)
            {
                gn.failed = true;
                return null;
            } else
            {
                return gn;
            }
        }
        if(gn.failed)
            return null;
        if(verifyClass(gn.plugin, type))
            return gn;
        else
            return null;
    }

    public static Codec findCodec(Format in, Format out, Format selectedIn[], Format selectedOut[])
    {
        Vector cnames = PlugInManager.getPlugInList(in, out, 2);
        if(cnames == null)
            return null;
        Codec c = null;
        for(int i = 0; i < cnames.size(); i++)
            if((c = (Codec)createPlugIn((String)cnames.elementAt(i), 2)) != null)
            {
                Format fmts[] = c.getSupportedInputFormats();
                Format matched;
                if((matched = matches(in, fmts, null, c)) != null)
                {
                    if(selectedIn != null && selectedIn.length > 0)
                        selectedIn[0] = matched;
                    fmts = c.getSupportedOutputFormats(matched);
                    if(fmts != null && fmts.length != 0)
                    {
                        boolean success = false;
                        for(int j = 0; j < fmts.length; j++)
                        {
                            if(out != null)
                            {
                                if(!out.matches(fmts[j]) || (matched = out.intersects(fmts[j])) == null)
                                    continue;
                            } else
                            {
                                matched = fmts[j];
                            }
                            if(c.setOutputFormat(matched) == null)
                                continue;
                            success = true;
                            break;
                        }

                        if(success)
                        {
                            try
                            {
                                c.open();
                            }
                            catch(ResourceUnavailableException e) { }
                            if(selectedOut != null && selectedOut.length > 0)
                                selectedOut[0] = matched;
                            return c;
                        }
                    }
                }
            }

        return null;
    }

    public static Renderer findRenderer(Format in)
    {
        Vector names = PlugInManager.getPlugInList(in, null, 4);
        if(names == null)
            return null;
        Renderer r = null;
        for(int i = 0; i < names.size(); i++)
            if((r = (Renderer)createPlugIn((String)names.elementAt(i), 4)) != null)
            {
                Format fmts[] = r.getSupportedInputFormats();
                Format matched;
                if((matched = matches(in, fmts, null, r)) != null)
                {
                    try
                    {
                        r.open();
                    }
                    catch(ResourceUnavailableException e) { }
                    return r;
                }
            }

        return null;
    }

    public static Vector findRenderingChain(Format in, Vector formats)
    {
        SimpleGraphBuilder gb = new SimpleGraphBuilder();
        GraphNode n;
        if((n = gb.buildGraph(in)) == null)
            return null;
        Vector list = new Vector(10);
        for(; n != null && n.plugin != null; n = n.prev)
        {
            list.addElement(n.plugin);
            if(formats != null)
                formats.addElement(n.input);
        }

        return list;
    }

    public static PlugIn createPlugIn(String name, int type)
    {
        Object obj;
        try
        {
            Class cls = BasicPlugIn.getClassForName(name);
            obj = cls.newInstance();
        }
        catch(Exception e)
        {
            return null;
        }
        catch(Error e)
        {
            return null;
        }
        if(verifyClass(obj, type))
            return (PlugIn)obj;
        else
            return null;
    }

    public static boolean verifyClass(Object obj, int type)
    {
        Class cls;
        switch(type)
        {
        case 2: // '\002'
            cls = javax.media.Codec.class;
            break;

        case 4: // '\004'
            cls = javax.media.Renderer.class;
            break;

        case 5: // '\005'
            cls = javax.media.Multiplexer.class;
            break;

        case 3: // '\003'
        default:
            cls = javax.media.PlugIn.class;
            break;
        }
        return cls.isInstance(obj);
    }

    public static Format matches(Format outs[], Format ins[], PlugIn up, PlugIn down)
    {
        if(outs == null)
            return null;
        for(int i = 0; i < outs.length; i++)
        {
            Format fmt;
            if((fmt = matches(outs[i], ins, up, down)) != null)
                return fmt;
        }

        return null;
    }

    public static Format matches(Format out, Format ins[], PlugIn up, PlugIn down)
    {
        if(out == null || ins == null)
            return null;
        for(int i = 0; i < ins.length; i++)
            if(ins[i] != null && ins[i].getClass().isAssignableFrom(out.getClass()) && out.matches(ins[i]))
            {
                Format fmt = out.intersects(ins[i]);
                if(fmt != null && (down == null || (fmt = verifyInput(down, fmt)) != null))
                {
                    Format refined = fmt;
                    if((up == null || (refined = verifyOutput(up, fmt)) != null) && (down == null || refined == fmt || verifyInput(down, refined) != null))
                        return refined;
                }
            }

        return null;
    }

    public static Format matches(Format outs[], Format in, PlugIn up, PlugIn down)
    {
        Format ins[] = new Format[1];
        ins[0] = in;
        return matches(outs, ins, up, down);
    }

    public static Format verifyInput(PlugIn p, Format in)
    {
        if(p instanceof Codec)
            return ((Codec)p).setInputFormat(in);
        if(p instanceof Renderer)
            return ((Renderer)p).setInputFormat(in);
        else
            return null;
    }

    public static Format verifyOutput(PlugIn p, Format out)
    {
        if(p instanceof Codec)
            return ((Codec)p).setOutputFormat(out);
        else
            return null;
    }

    protected int STAGES;
    protected Hashtable plugIns;
    protected GraphNode targetPlugins[];
    protected Vector targetPluginNames;
    protected int targetType;
    int indent;
    protected static GraphInspector inspector;
}
