// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ProcessEngine.java

package com.sun.media;

import com.sun.media.codec.video.colorspace.RGBScaler;
import com.sun.media.controls.ProgressControl;
import com.sun.media.util.Resource;
import java.awt.Dimension;
import java.util.Vector;
import javax.media.*;
import javax.media.control.FrameRateControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;

// Referenced classes of package com.sun.media:
//            PlaybackEngine, BasicTrackControl, BasicMuxModule, BasicSourceModule, 
//            BasicModule, Log, BasicController, GraphNode, 
//            SimpleGraphBuilder, GraphInspector, Connector, OutputConnector, 
//            BasicSinkModule, BasicProcessor, SlowPlugIn

public class ProcessEngine extends PlaybackEngine
{
    class ProcGraphBuilder extends SimpleGraphBuilder
    {

        public Format[] getSupportedOutputFormats(Format input)
        {
            long formatsTime = System.currentTimeMillis();
            Vector collected = new Vector();
            Vector candidates = new Vector();
            GraphNode node = new GraphNode(null, (PlugIn)null, input, null, 0);
            candidates.addElement(node);
            collected.addElement(input);
            nodesVisited++;
            for(; !candidates.isEmpty(); doGetSupportedOutputFormats(candidates, collected));
            Format all[] = new Format[collected.size()];
            int front = 0;
            int back = all.length - 1;
            Format mpegAudio = new AudioFormat("mpegaudio/rtp");
            boolean mpegInput = (new AudioFormat("mpegaudio")).matches(input) || (new AudioFormat("mpeglayer3")).matches(input) || (new VideoFormat("mpeg")).matches(input);
            for(int i = 0; i < all.length; i++)
            {
                Object obj = collected.elementAt(i);
                if(!mpegInput && mpegAudio.matches((Format)obj))
                    all[back--] = (Format)obj;
                else
                    all[front++] = (Format)obj;
            }

            Log.comment("Getting the supported output formats for:");
            Log.comment("  " + input);
            Log.comment("  # of nodes visited: " + nodesVisited);
            Log.comment("  # of formats supported: " + all.length + "\n");
            ProcGraphBuilder _tmp = this;
            PlaybackEngine.profile("getSupportedOutputFormats", formatsTime);
            return all;
        }

        void doGetSupportedOutputFormats(Vector candidates, Vector collected)
        {
            GraphNode node = (GraphNode)candidates.firstElement();
            candidates.removeElementAt(0);
            if(node.input == null && (node.plugin == null || !(node.plugin instanceof Codec)))
            {
                Log.error("Internal error: doGetSupportedOutputFormats");
                return;
            }
            if(node.plugin != null && SimpleGraphBuilder.verifyInput(node.plugin, node.input) == null)
                return;
            Format input;
            Format outs[];
            if(node.plugin != null)
            {
                outs = node.getSupportedOutputs(node.input);
                if(outs == null || outs.length == 0)
                    return;
                for(int j = 0; j < outs.length; j++)
                {
                    int size = collected.size();
                    boolean found = false;
                    for(int k = 0; k < size; k++)
                    {
                        Format other = (Format)collected.elementAt(k);
                        if(other != outs[j] && !other.equals(outs[j]))
                            continue;
                        found = true;
                        break;
                    }

                    if(!found)
                        collected.addElement(outs[j]);
                }

                input = node.input;
            } else
            {
                outs = new Format[1];
                outs[0] = node.input;
                input = null;
            }
            if(node.level >= super.STAGES)
                return;
            for(int i = 0; i < outs.length; i++)
                if((input == null || !input.equals(outs[i])) && (node.plugin == null || SimpleGraphBuilder.verifyOutput(node.plugin, outs[i]) != null))
                {
                    Vector cnames = PlugInManager.getPlugInList(outs[i], null, 2);
                    if(cnames != null && cnames.size() != 0)
                    {
                        for(int j = 0; j < cnames.size(); j++)
                        {
                            GraphNode gn;
                            if((gn = SimpleGraphBuilder.getPlugInNode((String)cnames.elementAt(j), 2, super.plugIns)) != null && !gn.checkAttempted(outs[i]))
                            {
                                Format ins[] = gn.getSupportedInputs();
                                Format fmt;
                                if((fmt = SimpleGraphBuilder.matches(outs[i], ins, null, gn.plugin)) != null)
                                {
                                    GraphNode n = new GraphNode(gn, fmt, node, node.level + 1);
                                    candidates.addElement(n);
                                    nodesVisited++;
                                }
                            }
                        }

                    }
                }

        }

        boolean buildGraph(BasicTrackControl tc, int trackID, int numTracks)
        {
            this.trackID = trackID;
            this.numTracks = numTracks;
            if(tc.isCustomized())
            {
                Log.comment("Input: " + tc.getOriginalFormat());
                return buildCustomGraph((ProcTControl)tc);
            } else
            {
                return super.buildGraph(tc);
            }
        }

        protected GraphNode buildTrackFromGraph(BasicTrackControl tc, GraphNode node)
        {
            return engine.buildTrackFromGraph((ProcTControl)tc, node);
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
            if(targetFormat != null)
            {
                Format matched = null;
                if((matched = SimpleGraphBuilder.matches(outs, targetFormat, node.plugin, null)) == null)
                    return null;
                if(SimpleGraphBuilder.inspector != null && !SimpleGraphBuilder.inspector.verify((Codec)node.plugin, node.input, matched))
                    return null;
                if(super.targetPlugins == null && targetMuxes == null)
                {
                    node.output = matched;
                    return node;
                }
                outs = new Format[1];
                outs[0] = matched;
            }
            GraphNode n;
            if(super.targetPlugins != null)
                if((n = verifyTargetPlugins(node, outs)) != null)
                    return n;
                else
                    return null;
            if(targetMuxes != null && (n = verifyTargetMuxes(node, outs)) != null)
                return n;
            else
                return null;
        }

        GraphNode verifyTargetMuxes(GraphNode node, Format outs[])
        {
            for(int i = 0; i < targetMuxes.length; i++)
            {
                GraphNode gn;
                if((gn = targetMuxes[i]) == null)
                {
                    String name = (String)targetMuxNames.elementAt(i);
                    if(name == null)
                        continue;
                    if((gn = SimpleGraphBuilder.getPlugInNode(name, 5, super.plugIns)) == null)
                    {
                        targetMuxNames.setElementAt(null, i);
                        continue;
                    }
                    Multiplexer mux = (Multiplexer)gn.plugin;
                    if(mux.setContentDescriptor(outputContentDes) == null)
                    {
                        targetMuxNames.setElementAt(null, i);
                        continue;
                    }
                    if(mux.setNumTracks(numTracks) != numTracks)
                    {
                        targetMuxNames.setElementAt(null, i);
                        continue;
                    }
                    targetMuxes[i] = gn;
                }
                if(targetMux == null || gn == targetMux)
                {
                    for(int j = 0; j < outs.length; j++)
                    {
                        Format fmt;
                        if((fmt = ((Multiplexer)gn.plugin).setInputFormat(outs[j], trackID)) != null && (SimpleGraphBuilder.inspector == null || node.plugin == null || SimpleGraphBuilder.inspector.verify((Codec)node.plugin, node.input, fmt)))
                        {
                            targetMux = gn;
                            targetMuxFormats[trackID] = fmt;
                            node.output = fmt;
                            return node;
                        }
                    }

                }
            }

            return null;
        }

        boolean setDefaultTargets(Format in)
        {
            if(outputContentDes != null)
                return setDefaultTargetMux();
            else
                return setDefaultTargetRenderer(in);
        }

        boolean setDefaultTargetRenderer(Format in)
        {
            if(!super.setDefaultTargetRenderer(in))
            {
                return false;
            } else
            {
                targetMuxes = null;
                return true;
            }
        }

        boolean setDefaultTargetMux()
        {
            if(targetMuxes != null)
                return true;
            Log.comment("An output content type is specified: " + outputContentDes);
            targetMuxNames = PlugInManager.getPlugInList(null, outputContentDes, 5);
            if(targetMuxNames == null || targetMuxNames.size() == 0)
            {
                Log.error("No multiplexer is found for that content type: " + outputContentDes);
                return false;
            } else
            {
                targetMuxes = new GraphNode[targetMuxNames.size()];
                targetMux = null;
                targetMuxFormats = new Format[numTracks];
                super.targetPluginNames = null;
                super.targetPlugins = null;
                return true;
            }
        }

        void setTargetPlugin(PlugIn p, int type)
        {
            super.targetPlugins = new GraphNode[1];
            super.targetPlugins[0] = new GraphNode(p, null, null, 0);
            super.targetPlugins[0].custom = true;
            super.targetPlugins[0].type = type;
        }

        boolean buildCustomGraph(ProcTControl tc)
        {
            codecs = tc.codecChainWanted;
            rend = tc.rendererWanted;
            format = tc.formatWanted;
            if((format instanceof VideoFormat) && (tc.getOriginalFormat() instanceof VideoFormat))
            {
                Dimension s1 = ((VideoFormat)tc.getOriginalFormat()).getSize();
                Dimension s2 = ((VideoFormat)format).getSize();
                if(s1 != null && s2 != null && !s1.equals(s2))
                {
                    RGBScaler scaler = new RGBScaler(s2);
                    if(codecs == null || codecs.length == 0)
                    {
                        codecs = new Codec[1];
                        codecs[0] = scaler;
                    } else
                    {
                        codecs = new Codec[tc.codecChainWanted.length + 1];
                        int i;
                        if(!PlaybackEngine.isRawVideo(format))
                        {
                            codecs[0] = scaler;
                            i = 1;
                        } else
                        {
                            codecs[tc.codecChainWanted.length] = scaler;
                            i = 0;
                        }
                        for(int j = 0; j < tc.codecChainWanted.length; j++)
                            codecs[i++] = tc.codecChainWanted[j];

                    }
                }
            }
            GraphNode node;
            GraphNode failed;
            return (node = buildCustomGraph(tc.getOriginalFormat())) != null && (failed = buildTrackFromGraph(tc, node)) == null;
        }

        GraphNode buildCustomGraph(Format in)
        {
            Vector candidates = new Vector();
            GraphNode n = null;
            GraphNode node = new GraphNode(null, (PlugIn)null, in, null, 0);
            candidates.addElement(node);
            Log.comment("Custom options specified.");
            super.indent = 1;
            Log.setIndent(super.indent);
            if(codecs != null)
            {
                resetTargets();
                for(int i = 0; i < codecs.length; i++)
                    if(codecs[i] != null)
                    {
                        Log.comment("A custom codec is specified: " + codecs[i]);
                        setTargetPlugin(codecs[i], 2);
                        if((node = buildGraph(candidates)) == null)
                        {
                            Log.error("The input format is not compatible with the given codec plugin: " + codecs[i]);
                            super.indent = 0;
                            Log.setIndent(super.indent);
                            return null;
                        }
                        node.level = 0;
                        candidates = new Vector();
                        candidates.addElement(node);
                    }

            }
            if(outputContentDes != null)
            {
                resetTargets();
                if(format != null)
                {
                    targetFormat = format;
                    Log.comment("An output format is specified: " + format);
                }
                if(!setDefaultTargetMux())
                    return null;
                if((node = buildGraph(candidates)) == null)
                {
                    Log.error("Failed to build a graph for the given custom options.");
                    super.indent = 0;
                    Log.setIndent(super.indent);
                    return null;
                }
            } else
            {
                if(format != null)
                {
                    resetTargets();
                    targetFormat = format;
                    Log.comment("An output format is specified: " + format);
                    if((node = buildGraph(candidates)) == null)
                    {
                        Log.error("The input format cannot be transcoded to the specified target format.");
                        super.indent = 0;
                        Log.setIndent(super.indent);
                        return null;
                    }
                    node.level = 0;
                    candidates = new Vector();
                    candidates.addElement(node);
                    targetFormat = null;
                }
                if(rend != null)
                {
                    Log.comment("A custom renderer is specified: " + rend);
                    setTargetPlugin(rend, 4);
                    if((node = buildGraph(candidates)) == null)
                    {
                        if(format != null)
                            Log.error("The customed transocoded format is not compatible with the given renderer plugin: " + rend);
                        else
                            Log.error("The input format is not compatible with the given renderer plugin: " + rend);
                        super.indent = 0;
                        Log.setIndent(super.indent);
                        return null;
                    }
                } else
                {
                    if(!setDefaultTargetRenderer(format != null ? format : in))
                        return null;
                    if((node = buildGraph(candidates)) == null)
                    {
                        if(format != null)
                            Log.error("Failed to find a renderer that supports the customed transcoded format.");
                        else
                            Log.error("Failed to build a graph to render the input format with the given custom options.");
                        super.indent = 0;
                        Log.setIndent(super.indent);
                        return null;
                    }
                }
            }
            super.indent = 0;
            Log.setIndent(super.indent);
            return node;
        }

        public void reset()
        {
            super.reset();
            resetTargets();
        }

        void resetTargets()
        {
            targetFormat = null;
            super.targetPlugins = null;
        }

        protected ProcessEngine engine;
        protected Format targetFormat;
        protected int trackID;
        protected int numTracks;
        protected int nodesVisited;
        Codec codecs[];
        Renderer rend;
        Format format;

        ProcGraphBuilder(ProcessEngine engine)
        {
            trackID = 0;
            numTracks = 1;
            nodesVisited = 0;
            codecs = null;
            rend = null;
            format = null;
            this.engine = engine;
        }
    }

    class ProcTControl extends BasicTrackControl
        implements Owned
    {

        public Object getOwner()
        {
            return player;
        }

        public Format getFormat()
        {
            return formatWanted != null ? formatWanted : super.track.getFormat();
        }

        public Format[] getSupportedFormats()
        {
            if(supportedFormats == null && (supportedFormats = Resource.getDB(super.track.getFormat())) == null)
            {
                if(gb == null)
                    gb = new ProcGraphBuilder((ProcessEngine)super.engine);
                else
                    gb.reset();
                supportedFormats = gb.getSupportedOutputFormats(super.track.getFormat());
                supportedFormats = Resource.putDB(super.track.getFormat(), supportedFormats);
                PlaybackEngine.needSavingDB = true;
            }
            if(outputContentDes != null)
                return verifyMuxInputs(outputContentDes, supportedFormats);
            else
                return supportedFormats;
        }

        Format[] verifyMuxInputs(ContentDescriptor cd, Format inputs[])
        {
            if(cd == null || cd.getEncoding() == "raw")
                return inputs;
            Vector cnames = PlugInManager.getPlugInList(null, cd, 5);
            if(cnames == null || cnames.size() == 0)
                return new Format[0];
            Multiplexer mux[] = new Multiplexer[cnames.size()];
            int total = 0;
            for(int i = 0; i < cnames.size(); i++)
            {
                Multiplexer m;
                if((m = (Multiplexer)SimpleGraphBuilder.createPlugIn((String)cnames.elementAt(i), 5)) == null)
                    continue;
                try
                {
                    m.setContentDescriptor(outputContentDes);
                }
                catch(Exception e)
                {
                    continue;
                }
                if(m.setNumTracks(1) >= 1)
                    mux[total++] = m;
            }

            Format tmp[] = new Format[inputs.length];
            int vtotal = 0;
            for(int i = 0; i < inputs.length; i++)
            {
                Format fmt;
                if(total == 1)
                {
                    if((fmt = mux[0].setInputFormat(inputs[i], 0)) != null)
                        tmp[vtotal++] = fmt;
                } else
                {
                    for(int j = 0; j < total; j++)
                    {
                        if((fmt = mux[j].setInputFormat(inputs[i], 0)) == null)
                            continue;
                        tmp[vtotal++] = fmt;
                        break;
                    }

                }
            }

            Format verified[] = new Format[vtotal];
            System.arraycopy(tmp, 0, verified, 0, vtotal);
            return verified;
        }

        public Format setFormat(Format format)
        {
            if(super.engine.getState() > 180)
                return getFormat();
            if(format != null && !format.matches(super.track.getFormat()))
                formatWanted = checkSize(format);
            else
                return format;
            return formatWanted;
        }

        private Format checkSize(Format fmt)
        {
            if(!(fmt instanceof VideoFormat))
                return fmt;
            VideoFormat vfmt = (VideoFormat)fmt;
            Dimension size = ((VideoFormat)fmt).getSize();
            if(size == null)
            {
                Format ofmt = getOriginalFormat();
                if(ofmt == null || (size = ((VideoFormat)ofmt).getSize()) == null)
                    return fmt;
            }
            int w = size.width;
            int h = size.height;
            if(fmt.matches(new VideoFormat("jpeg/rtp")) || fmt.matches(new VideoFormat("jpeg")))
            {
                if(size.width % 8 != 0)
                    w = (size.width / 8) * 8;
                if(size.height % 8 != 0)
                    h = (size.height / 8) * 8;
                if(w == 0 || h == 0)
                {
                    w = size.width;
                    h = size.height;
                }
            } else
            if(fmt.matches(new VideoFormat("h263/rtp")) || fmt.matches(new VideoFormat("h263-1998/rtp")) || fmt.matches(new VideoFormat("h263")))
                if(size.width >= 352)
                {
                    w = 352;
                    h = 288;
                } else
                if(size.width >= 160)
                {
                    w = 176;
                    h = 144;
                } else
                {
                    w = 128;
                    h = 96;
                }
            if(w != size.width || h != size.height)
            {
                Log.comment("setFormat: " + fmt.getEncoding() + ": video aspect ratio mismatched.");
                Log.comment("  Scaled from " + size.width + "x" + size.height + " to " + w + "x" + h + ".\n");
                fmt = (new VideoFormat(null, new Dimension(w, h), -1, null, -1F)).intersects(fmt);
            }
            return fmt;
        }

        public boolean buildTrack(int trackID, int numTracks)
        {
            if(gb == null)
                gb = new ProcGraphBuilder((ProcessEngine)super.engine);
            else
                gb.reset();
            boolean rtn = gb.buildGraph(this, trackID, numTracks);
            gb = null;
            return rtn;
        }

        public boolean isTimeBase()
        {
            for(int j = 0; j < super.modules.size(); j++)
                if(super.modules.elementAt(j) == masterSink)
                    return true;

            return false;
        }

        public void setCodecChain(Codec codec[])
            throws NotConfiguredError, UnsupportedPlugInException
        {
            if(super.engine.getState() > 180)
                throwError(new NotConfiguredError("Cannot set a PlugIn before reaching the configured state."));
            if(codec.length < 1)
                throw new UnsupportedPlugInException("No codec specified in the array.");
            codecChainWanted = new Codec[codec.length];
            for(int i = 0; i < codec.length; i++)
                codecChainWanted[i] = codec[i];

        }

        public void setRenderer(Renderer renderer)
            throws NotConfiguredError
        {
            if(super.engine.getState() > 180)
                throwError(new NotConfiguredError("Cannot set a PlugIn before reaching the configured state."));
            rendererWanted = renderer;
            if(renderer instanceof SlowPlugIn)
                ((SlowPlugIn)renderer).forceToUse();
        }

        public boolean isCustomized()
        {
            return formatWanted != null || codecChainWanted != null || rendererWanted != null;
        }

        public void prError()
        {
            if(!isCustomized())
            {
                super.prError();
                return;
            }
            Log.error("  Cannot build a flow graph with the customized options:");
            if(formatWanted != null)
            {
                Log.error("    Unable to transcode format: " + getOriginalFormat());
                Log.error("      to: " + getFormat());
                if(outputContentDes != null)
                    Log.error("      outputting to: " + outputContentDes);
            }
            if(codecChainWanted != null)
            {
                Log.error("    Unable to add customed codecs: ");
                for(int i = 0; i < codecChainWanted.length; i++)
                    Log.error("      " + codecChainWanted[i]);

            }
            if(rendererWanted != null)
                Log.error("    Unable to add customed renderer: " + rendererWanted);
            Log.write("\n");
        }

        protected ProgressControl progressControl()
        {
            return ProcessEngine.this.progressControl;
        }

        protected FrameRateControl frameRateControl()
        {
            super.muxModule = getMuxModule();
            return ProcessEngine.this.frameRateControl;
        }

        protected Format formatWanted;
        protected Codec codecChainWanted[];
        protected Renderer rendererWanted;
        protected ProcGraphBuilder gb;
        protected Format supportedFormats[];

        public ProcTControl(ProcessEngine engine, Track track, OutputConnector oc)
        {
            super(engine, track, oc);
            formatWanted = null;
            codecChainWanted = null;
            rendererWanted = null;
            supportedFormats = null;
        }
    }


    public ProcessEngine(BasicProcessor p)
    {
        super(p);
        outputContentDes = null;
        prefetchError = "Failed to prefetch: " + this;
        targetMuxNames = null;
        targetMuxes = null;
        targetMux = null;
        targetMuxFormats = null;
    }

    protected boolean doConfigure()
    {
        if(!doConfigure1())
            return false;
        String names[] = super.source.getOutputConnectorNames();
        super.trackControls = new BasicTrackControl[super.tracks.length];
        for(int i = 0; i < super.tracks.length; i++)
            super.trackControls[i] = new ProcTControl(this, super.tracks[i], super.source.getOutputConnector(names[i]));

        if(!doConfigure2())
        {
            return false;
        } else
        {
            outputContentDes = new ContentDescriptor("raw");
            reenableHintTracks();
            return true;
        }
    }

    protected synchronized boolean doRealize()
    {
        targetMuxes = null;
        if(!super.doRealize1())
            return false;
        if(targetMux != null && !connectMux())
        {
            Log.error(super.realizeError);
            Log.error("  Cannot connect the multiplexer\n");
            super.player.processError = super.genericProcessorError;
            return false;
        }
        return super.doRealize2();
    }

    boolean isRTPFormat(Format fmt)
    {
        return fmt != null && fmt.getEncoding() != null && fmt.getEncoding().endsWith("rtp") || fmt.getEncoding().endsWith("RTP");
    }

    void reenableHintTracks()
    {
        for(int i = 0; i < super.trackControls.length; i++)
        {
            if(!isRTPFormat(super.trackControls[i].getOriginalFormat()))
                continue;
            super.trackControls[i].setEnabled(true);
            break;
        }

    }

    BasicMuxModule getMuxModule()
    {
        return muxModule;
    }

    boolean connectMux()
    {
        BasicTrackControl tcs[] = new BasicTrackControl[super.trackControls.length];
        int total = 0;
        Multiplexer mux = (Multiplexer)targetMux.plugin;
        for(int i = 0; i < super.trackControls.length; i++)
            if(super.trackControls[i].isEnabled())
                tcs[total++] = super.trackControls[i];

        try
        {
            mux.setContentDescriptor(outputContentDes);
        }
        catch(Exception e)
        {
            Log.comment("Failed to set the output content descriptor on the multiplexer.");
            return false;
        }
        boolean failed = false;
        if(mux.setNumTracks(targetMuxFormats.length) != targetMuxFormats.length)
        {
            Log.comment("Failed  to set number of tracks on the multiplexer.");
            return false;
        }
        for(int mf = 0; mf < targetMuxFormats.length; mf++)
        {
            if(targetMuxFormats[mf] != null && mux.setInputFormat(targetMuxFormats[mf], mf) != null)
                continue;
            Log.comment("Failed to set input format on the multiplexer.");
            failed = true;
            break;
        }

        if(failed)
            return false;
        if(SimpleGraphBuilder.inspector != null && !SimpleGraphBuilder.inspector.verify(mux, targetMuxFormats))
            return false;
        BasicMuxModule bmm = new BasicMuxModule(mux, targetMuxFormats);
        if(PlaybackEngine.DEBUG)
            bmm.setJMD(super.jmd);
        for(int j = 0; j < targetMuxFormats.length; j++)
        {
            InputConnector ic = bmm.getInputConnector(BasicMuxModule.ConnectorNamePrefix + j);
            if(ic == null)
            {
                Log.comment("BasicMuxModule: connector mismatched.");
                return false;
            }
            ic.setFormat(targetMuxFormats[j]);
            tcs[j].lastOC.setProtocol(ic.getProtocol());
            tcs[j].lastOC.connectTo(ic, targetMuxFormats[j]);
        }

        if(!bmm.doRealize())
        {
            return false;
        } else
        {
            bmm.setModuleListener(this);
            bmm.setController(this);
            super.modules.addElement(bmm);
            super.sinks.addElement(bmm);
            muxModule = bmm;
            return true;
        }
    }

    protected BasicSinkModule findMasterSink()
    {
        if(muxModule != null && muxModule.getClock() != null)
            return muxModule;
        else
            return super.findMasterSink();
    }

    protected synchronized boolean doPrefetch()
    {
        if(super.prefetched)
            return true;
        if(!doPrefetch1())
            return false;
        if(muxModule != null && !muxModule.doPrefetch())
        {
            Log.error(prefetchError);
            Log.error("  Cannot prefetch the multiplexer: " + muxModule.getMultiplexer() + "\n");
            return false;
        } else
        {
            return doPrefetch2();
        }
    }

    protected synchronized void doStart()
    {
        if(super.started)
            return;
        doStart1();
        if(muxModule != null)
            muxModule.doStart();
        doStart2();
    }

    protected synchronized void doStop()
    {
        if(!super.started)
            return;
        doStop1();
        if(muxModule != null)
            muxModule.doStop();
        doStop2();
    }

    public TrackControl[] getTrackControls()
        throws NotConfiguredError
    {
        if(getState() < 180)
            throwError(new NotConfiguredError("getTrackControls " + PlaybackEngine.NOT_CONFIGURED_ERROR));
        return super.trackControls;
    }

    public ContentDescriptor[] getSupportedContentDescriptors()
        throws NotConfiguredError
    {
        if(getState() < 180)
            throwError(new NotConfiguredError("getSupportedContentDescriptors " + PlaybackEngine.NOT_CONFIGURED_ERROR));
        Vector names = PlugInManager.getPlugInList(null, null, 5);
        Vector fmts = new Vector();
        for(int i = 0; i < names.size(); i++)
        {
            Format fs[] = PlugInManager.getSupportedOutputFormats((String)names.elementAt(i), 5);
            if(fs != null)
            {
                for(int j = 0; j < fs.length; j++)
                    if(fs[j] instanceof ContentDescriptor)
                    {
                        boolean duplicate = false;
                        for(int k = 0; k < fmts.size(); k++)
                        {
                            if(!fmts.elementAt(k).equals(fs[j]))
                                continue;
                            duplicate = true;
                            break;
                        }

                        if(!duplicate)
                            fmts.addElement(fs[j]);
                    }

            }
        }

        ContentDescriptor cds[] = new ContentDescriptor[fmts.size()];
        for(int i = 0; i < fmts.size(); i++)
            cds[i] = (ContentDescriptor)fmts.elementAt(i);

        return cds;
    }

    public ContentDescriptor setContentDescriptor(ContentDescriptor ocd)
        throws NotConfiguredError
    {
        if(getState() < 180)
            throwError(new NotConfiguredError("setContentDescriptor " + PlaybackEngine.NOT_CONFIGURED_ERROR));
        if(getState() > 180)
            return null;
        if(ocd != null)
        {
            Vector cnames = PlugInManager.getPlugInList(null, ocd, 5);
            if(cnames == null || cnames.size() == 0)
                return null;
        }
        outputContentDes = ocd;
        return outputContentDes;
    }

    public ContentDescriptor getContentDescriptor()
        throws NotConfiguredError
    {
        if(getState() < 180)
            throwError(new NotConfiguredError("getContentDescriptor " + PlaybackEngine.NOT_CONFIGURED_ERROR));
        return outputContentDes;
    }

    public DataSource getDataOutput()
        throws NotRealizedError
    {
        if(getState() < 300)
            throwError(new NotRealizedError("getDataOutput " + PlaybackEngine.NOT_REALIZED_ERROR));
        if(muxModule != null)
            return muxModule.getDataOutput();
        else
            return null;
    }

    protected long getBitRate()
    {
        if(muxModule != null)
            return muxModule.getBitsWritten();
        else
            return super.source.getBitsRead();
    }

    protected void resetBitRate()
    {
        if(muxModule != null)
            muxModule.resetBitsWritten();
        else
            super.source.resetBitsRead();
    }

    protected PlugIn getPlugIn(BasicModule m)
    {
        if(m instanceof BasicMuxModule)
            return ((BasicMuxModule)m).getMultiplexer();
        else
            return super.getPlugIn(m);
    }

    protected BasicMuxModule muxModule;
    protected ContentDescriptor outputContentDes;
    String prefetchError;
    protected Vector targetMuxNames;
    protected GraphNode targetMuxes[];
    protected GraphNode targetMux;
    protected Format targetMuxFormats[];
}
