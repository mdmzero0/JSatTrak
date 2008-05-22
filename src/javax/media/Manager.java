package javax.media;

//import com.sun.media.Log;
//import com.sun.media.util.Registry;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import javax.media.control.FormatControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PullBufferDataSource;
import javax.media.protocol.PullDataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushDataSource;
import javax.media.protocol.SourceCloneable;

// Referenced classes of package javax.media:
//            MediaLocator, NoPlayerException, NoProcessorException, Format, 
//            CaptureDeviceInfo, NoDataSourceException, IncompatibleSourceException, CannotRealizeException, 
//            SystemTimeBase, MediaHandler, Player, MediaProxy, 
//            Processor, DataSink, DataSinkProxy, NoDataSinkException, 
//            MCA, ProcessorModel, CaptureDeviceManager, Controller, 
//            PackageManager, TimeBase

public final class Manager
{

    private static String VERSION = "2.1.1e";
    public static final int MAX_SECURITY = 1;
    public static final int CACHING = 2;
    public static final int LIGHTWEIGHT_RENDERER = 3;
    public static final int PLUGIN_PLAYER = 4;
    private static int numberOfHints = 4;
    private static SystemTimeBase sysTimeBase = null;
    public static final String UNKNOWN_CONTENT_NAME = "unknown";
    private static boolean jdkInit = false;
    private static Method forName3ArgsM;
    private static Method getSystemClassLoaderM;
    private static ClassLoader systemClassLoader;
    private static Method getContextClassLoaderM;
    private static String fileSeparator = System.getProperty("file.separator");
    private static Hashtable hintTable;
    static final int DONE = 0;
    static final int SUCCESS = 1;

    private Manager()
    {
    }

    public static String getVersion()
    {
        return VERSION;
    }

    public static Player createPlayer(URL sourceURL)
        throws IOException, NoPlayerException
    {
        return createPlayer(new MediaLocator(sourceURL));
    }

    public static Player createPlayer(MediaLocator sourceLocator)
        throws IOException, NoPlayerException
    {
        Player newPlayer = null;
        Hashtable sources = new Hashtable(10);
        boolean needPluginPlayer = ((Boolean)getHint(4)).booleanValue();
        String protocol = sourceLocator.getProtocol();
        if(protocol != null && (protocol.equalsIgnoreCase("rtp") || protocol.equalsIgnoreCase("rtsp")))
        {
            needPluginPlayer = false;
        }
        try
        {
            newPlayer = createPlayerForContent(sourceLocator, needPluginPlayer, sources);
        }
        catch(NoPlayerException e)
        {
            if(needPluginPlayer)
            {
                throw e;
            }
            newPlayer = createPlayerForContent(sourceLocator, true, sources);
        }
        if(sources.size() != 0)
        {
            DataSource ds;
            for(Enumeration enu = sources.elements(); enu.hasMoreElements(); ds.disconnect())
            {
                ds = (DataSource)enu.nextElement();
            }

        }
        return newPlayer;
    }

    public static Player createPlayer(DataSource source)
        throws IOException, NoPlayerException
    {
        boolean needPluginPlayer = ((Boolean)getHint(4)).booleanValue();
        String contentType = source.getContentType();
        if(contentType != null && (contentType.equalsIgnoreCase("rtp") || contentType.equalsIgnoreCase("rtsp")))
        {
            needPluginPlayer = false;
        }
        Player newPlayer;
        try
        {
            if(needPluginPlayer)
            {
                contentType = "unknown";
            }
            newPlayer = createPlayerForSource(source, contentType, null);
        }
        catch(NoPlayerException e)
        {
            if(needPluginPlayer)
            {
                throw e;
            }
            newPlayer = createPlayerForSource(source, "unknown", null);
        }
        return newPlayer;
    }

    public static Player createRealizedPlayer(URL sourceURL)
        throws IOException, NoPlayerException, CannotRealizeException
    {
        Player p = createPlayer(sourceURL);
        blockingCall(p, 300);
        return p;
    }

    public static Player createRealizedPlayer(MediaLocator ml)
        throws IOException, NoPlayerException, CannotRealizeException
    {
        Player p = createPlayer(ml);
        blockingCall(p, 300);
        return p;
    }

    public static Player createRealizedPlayer(DataSource source)
        throws IOException, NoPlayerException, CannotRealizeException
    {
        Player p = createPlayer(source);
        blockingCall(p, 300);
        return p;
    }

    public static Processor createProcessor(URL sourceURL)
        throws IOException, NoProcessorException
    {
        return createProcessor(new MediaLocator(sourceURL));
    }

    public static Processor createProcessor(MediaLocator sourceLocator)
        throws IOException, NoProcessorException
    {
        Processor newProcessor = null;
        Hashtable sources = new Hashtable(10);
        try
        {
            newProcessor = createProcessorForContent(sourceLocator, false, sources);
        }
        catch(NoProcessorException e)
        {
            newProcessor = createProcessorForContent(sourceLocator, true, sources);
        }
        if(sources.size() != 0)
        {
            DataSource ds;
            for(Enumeration enu = sources.elements(); enu.hasMoreElements(); ds.disconnect())
            {
                ds = (DataSource)enu.nextElement();
            }

        }
        return newProcessor;
    }

    public static Processor createProcessor(DataSource source)
        throws IOException, NoProcessorException
    {
        Processor newProcessor;
        try
        {
            newProcessor = createProcessorForSource(source, source.getContentType(), null);
        }
        catch(NoProcessorException e)
        {
            newProcessor = createProcessorForSource(source, "unknown", null);
        }
        return newProcessor;
    }

    public static Processor createRealizedProcessor(ProcessorModel model)
        throws IOException, NoProcessorException, CannotRealizeException
    {
        DataSource ds = null;
        MediaLocator ml = null;
        Processor processor = null;
        boolean matched = false;
        Format reqFormats[] = null;
        int reqNumTracks = -1;
        int nTracksEnabled = 0;
        if(model == null)
        {
            throw new NoProcessorException("null ProcessorModel");
        }
        ds = model.getInputDataSource();
        if(ds != null)
        {
            processor = createProcessor(ds);
        } else
        if((ml = model.getInputLocator()) != null)
        {
            processor = createProcessor(ml);
        } else
        {
            int nDevices = getNTypesOfCaptureDevices();
            Vector dataSourceList = new Vector(1);
            reqNumTracks = model.getTrackCount(nDevices);
            reqFormats = new Format[reqNumTracks];
            for(int i = 0; i < reqNumTracks; i++)
            {
                reqFormats[i] = model.getOutputTrackFormat(i);
                Vector deviceList = CaptureDeviceManager.getDeviceList(reqFormats[i]);
                if(deviceList == null || deviceList.size() == 0)
                {
                    if(reqFormats[i] instanceof AudioFormat)
                    {
                        deviceList = CaptureDeviceManager.getDeviceList(new AudioFormat(null));
                    } else
                    if(reqFormats[i] instanceof VideoFormat)
                    {
                        deviceList = CaptureDeviceManager.getDeviceList(new VideoFormat(null));
                    }
                }
                if(deviceList.size() != 0)
                {
                    CaptureDeviceInfo cdi = (CaptureDeviceInfo)deviceList.elementAt(0);
                    if(cdi != null && cdi.getLocator() != null)
                    {
                        try
                        {
                            DataSource crds = createDataSource(cdi.getLocator());
                            if(crds instanceof CaptureDevice)
                            {
                                FormatControl fc[] = ((CaptureDevice)crds).getFormatControls();
                                if(fc.length > 0)
                                {
                                    Format supported[] = fc[0].getSupportedFormats();
                                    if(supported.length > 0)
                                    {
                                        for(int f = 0; f < supported.length; f++)
                                        {
                                            if(!supported[f].matches(reqFormats[i]))
                                            {
                                                continue;
                                            }
                                            Format intersect = supported[f].intersects(reqFormats[i]);
                                            if(intersect != null && fc[0].setFormat(intersect) != null)
                                            {
                                                break;
                                            }
                                        }

                                    }
                                }
                            }
                            dataSourceList.addElement(crds);
                        }
                        catch(IOException ioe) { }
                        catch(NoDataSourceException ndse) { }
                    }
                }
            }

            if(dataSourceList.size() == 0)
            {
                throw new NoProcessorException("No suitable capture devices found!");
            }
            if(dataSourceList.size() > 1)
            {
                DataSource dataSourceArray[] = new DataSource[dataSourceList.size()];
                for(int k = 0; k < dataSourceList.size(); k++)
                {
                    dataSourceArray[k] = (DataSource)dataSourceList.elementAt(k);
                }

                try
                {
                    ds = createMergingDataSource(dataSourceArray);
                }
                catch(IncompatibleSourceException ise)
                {
                    throw new NoProcessorException("Couldn't merge capture devices");
                }
            } else
            {
                ds = (DataSource)dataSourceList.elementAt(0);
            }
            processor = createProcessor(ds);
        }
        if(processor == null)
        {
            throw new NoProcessorException("Couldn't create Processor for source");
        }
        blockingCall(processor, 180);
        ContentDescriptor rcd = model.getContentDescriptor();
        if(rcd == null)
        {
            processor.setContentDescriptor(null);
        } else
        {
            ContentDescriptor cds[] = processor.getSupportedContentDescriptors();
            if(cds == null || cds.length == 0)
            {
                throw new NoProcessorException("Processor doesn't support output");
            }
            for(int i = 0; i < cds.length; i++)
            {
                if(!rcd.matches(cds[i]) || processor.setContentDescriptor(cds[i]) == null)
                {
                    continue;
                }
                matched = true;
                break;
            }

            if(!matched)
            {
                throw new NoProcessorException("Processor doesn't support requested output ContentDescriptor");
            }
        }
        javax.media.control.TrackControl procTracks[] = processor.getTrackControls();
        if(procTracks != null && procTracks.length > 0)
        {
            int nValidTracks = 0;
            for(int i = 0; i < procTracks.length; i++)
            {
                if(procTracks[i].isEnabled())
                {
                    nValidTracks++;
                }
            }

            if(reqNumTracks == -1)
            {
                reqNumTracks = model.getTrackCount(nValidTracks);
            }
            if(reqNumTracks > 0)
            {
                if(reqFormats == null)
                {
                    reqFormats = new Format[reqNumTracks];
                }
                int procToReqMap[] = new int[reqNumTracks];
                for(int i = 0; i < reqNumTracks; i++)
                {
                    if(reqFormats[i] == null)
                    {
                        reqFormats[i] = model.getOutputTrackFormat(i);
                    }
                    procToReqMap[i] = -1;
                }

                boolean enabled[] = new boolean[procTracks.length];
                for(int i = 0; i < procTracks.length; i++)
                {
                    enabled[i] = false;
                    if(procTracks[i].isEnabled())
                    {
                        Format prefFormat = procTracks[i].getFormat();
                        for(int j = 0; j < reqNumTracks; j++)
                        {
                            if(procToReqMap[j] != -1 || reqFormats[j] != null && !prefFormat.matches(reqFormats[j]) || !model.isFormatAcceptable(j, prefFormat))
                            {
                                continue;
                            }
                            procToReqMap[j] = i;
                            enabled[i] = true;
                            nTracksEnabled++;
                            break;
                        }

                    }
                }

                for(int i = 0; i < procTracks.length && nTracksEnabled < reqNumTracks; i++)
                {
                    boolean used = false;
                    if(procTracks[i].isEnabled())
                    {
                        for(int j = 0; j < reqNumTracks; j++)
                        {
                            if(procToReqMap[j] == i)
                            {
                                used = true;
                            }
                        }

                        if(!used)
                        {
                            Format suppFormats[] = procTracks[i].getSupportedFormats();
                            if(suppFormats != null && suppFormats.length != 0)
                            {
                                matched = false;
                                for(int k = 0; k < suppFormats.length && !matched; k++)
                                {
                                    Format prefFormat = suppFormats[k];
                                    for(int j = 0; j < reqNumTracks && !matched; j++)
                                    {
                                        if(procToReqMap[j] != -1 || reqFormats[j] != null && !prefFormat.matches(reqFormats[j]) || !model.isFormatAcceptable(j, prefFormat) || procTracks[i].setFormat(prefFormat) == null)
                                        {
                                            continue;
                                        }
                                        procToReqMap[j] = i;
                                        enabled[i] = true;
                                        nTracksEnabled++;
                                        matched = true;
                                        break;
                                    }

                                }

                            }
                        }
                    }
                }

                if(nTracksEnabled < reqNumTracks)
                {
                    throw new CannotRealizeException("Unable to provide all requested tracks");
                }
            }
        }
        blockingCall(processor, 300);
        return processor;
    }

    public static DataSource createDataSource(URL sourceURL)
        throws IOException, NoDataSourceException
    {
        return createDataSource(new MediaLocator(sourceURL));
    }

    public static DataSource createDataSource(MediaLocator sourceLocator)
        throws IOException, NoDataSourceException
    {
        DataSource source = null;
        for(Enumeration protoList = getDataSourceList(sourceLocator.getProtocol()).elements(); protoList.hasMoreElements();)
        {
            String protoClassName = (String)protoList.nextElement();
            try
            {
                Class protoClass = getClassForName(protoClassName);
                source = (DataSource)protoClass.newInstance();
                source.setLocator(sourceLocator);
                source.connect();
                break;
            }
            catch(ClassNotFoundException e)
            {
                source = null;
            }
            catch(InstantiationException e)
            {
                source = null;
            }
            catch(IllegalAccessException e)
            {
                source = null;
            }
            catch(Exception e)
            {
                source = null;
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
                throw new NoDataSourceException(err);
            }
            catch(Error e)
            {
                source = null;
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
                throw new NoDataSourceException(err);
            }
        }

        if(source == null)
        {
            throw new NoDataSourceException("Cannot find a DataSource for: " + sourceLocator);
        } else
        {
            //Log.comment("DataSource created: " + source + "\n");
            return source;
        }
    }

    public static DataSource createMergingDataSource(DataSource sources[])
        throws IncompatibleSourceException
    {
        if(sources.length == 0)
        {
            throw new IncompatibleSourceException("No sources");
        }
        if(sources[0] instanceof PullDataSource)
        {
            for(int i = 1; i < sources.length; i++)
            {
                if(!(sources[i] instanceof PullDataSource))
                {
                    throw new IncompatibleSourceException("One of the sources isn't matching the others");
                }
            }

            PullDataSource pds[] = new PullDataSource[sources.length];
            for(int i = 0; i < pds.length; i++)
            {
                pds[i] = (PullDataSource)sources[i];
            }

            return reflectMDS("com.ibm.media.protocol.MergingPullDataSource", pds);
        }
        if(sources[0] instanceof PushDataSource)
        {
            for(int i = 1; i < sources.length; i++)
            {
                if(!(sources[i] instanceof PushDataSource))
                {
                    throw new IncompatibleSourceException("One of the sources isn't matching the others");
                }
            }

            PushDataSource pds[] = new PushDataSource[sources.length];
            for(int i = 0; i < pds.length; i++)
            {
                pds[i] = (PushDataSource)sources[i];
            }

            return reflectMDS("com.ibm.media.protocol.MergingPushDataSource", pds);
        }
        if(sources[0] instanceof PullBufferDataSource)
        {
            for(int i = 1; i < sources.length; i++)
            {
                if(!(sources[i] instanceof PullBufferDataSource))
                {
                    throw new IncompatibleSourceException("One of the sources isn't matching the others");
                }
            }

            PullBufferDataSource pds[] = new PullBufferDataSource[sources.length];
            for(int i = 0; i < pds.length; i++)
            {
                pds[i] = (PullBufferDataSource)sources[i];
            }

            return reflectMDS("com.ibm.media.protocol.MergingPullBufferDataSource", pds);
        }
        if(sources[0] instanceof PushBufferDataSource)
        {
            boolean anyCapture = false;
            for(int i = 1; i < sources.length; i++)
            {
                if(!(sources[i] instanceof PushBufferDataSource))
                {
                    throw new IncompatibleSourceException("One of the sources isn't matching the others");
                }
                if(sources[i] instanceof CaptureDevice)
                {
                    anyCapture = true;
                }
            }

            PushBufferDataSource pds[] = new PushBufferDataSource[sources.length];
            for(int i = 0; i < pds.length; i++)
            {
                pds[i] = (PushBufferDataSource)sources[i];
            }

            if(anyCapture)
            {
                return reflectMDS("com.ibm.media.protocol.MergingCDPushBDS", pds);
            } else
            {
                return reflectMDS("com.ibm.media.protocol.MergingPushBufferDataSource", pds);
            }
        } else
        {
            return null;
        }
    }

    private static DataSource reflectMDS(String cname, Object pds)
    {
        Class paramTypes[] = new Class[1];
        Object arg[] = new Object[1];
        try
        {
            Class cls = Class.forName(cname);
            paramTypes[0] = pds.getClass();
            Constructor cc = cls.getConstructor(paramTypes);
            if(cname.indexOf("PullDataSource") >= 0)
            {
                arg[0] = (PullDataSource[])pds;
            } else
            if(cname.indexOf("PushDataSource") >= 0)
            {
                arg[0] = (PushDataSource[])pds;
            } else
            if(cname.indexOf("PullBufferDataSource") >= 0)
            {
                arg[0] = (PullBufferDataSource[])pds;
            } else
            if(cname.indexOf("PushBufferDataSource") >= 0)
            {
                arg[0] = (PushBufferDataSource[])pds;
            } else
            if(cname.indexOf("CDPushBDS") >= 0)
            {
                arg[0] = (PushBufferDataSource[])pds;
            }
            return (DataSource)cc.newInstance(arg);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    private static DataSource reflectDS(String cname, DataSource source)
    {
        Class paramTypes[] = new Class[1];
        Object arg[] = new Object[1];
        try
        {
            Class cls = Class.forName(cname);
            if(cname.indexOf("PullDataSource") >= 0)
            {
                paramTypes[0] = javax.media.protocol.PullDataSource.class;
                arg[0] = (PullDataSource)source;
            } else
            if(cname.indexOf("PushDataSource") >= 0)
            {
                paramTypes[0] = javax.media.protocol.PushDataSource.class;
                arg[0] = (PushDataSource)source;
            } else
            if(cname.indexOf("PullBufferDataSource") >= 0)
            {
                paramTypes[0] = javax.media.protocol.PullBufferDataSource.class;
                arg[0] = (PullBufferDataSource)source;
            } else
            if(cname.indexOf("PushBufferDataSource") >= 0)
            {
                paramTypes[0] = javax.media.protocol.PushBufferDataSource.class;
                arg[0] = (PushBufferDataSource)source;
            }
            Constructor cc = cls.getConstructor(paramTypes);
            return (DataSource)cc.newInstance(arg);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    public static DataSource createCloneableDataSource(DataSource source)
    {
        if(source instanceof SourceCloneable)
        {
            return source;
        }
        if(source instanceof CaptureDevice)
        {
            if(source instanceof PullDataSource)
            {
                return reflectDS("com.ibm.media.protocol.CloneableCapturePullDataSource", source);
            }
            if(source instanceof PushDataSource)
            {
                return reflectDS("com.ibm.media.protocol.CloneableCapturePushDataSource", source);
            }
            if(source instanceof PullBufferDataSource)
            {
                return reflectDS("com.ibm.media.protocol.CloneableCapturePullBufferDataSource", source);
            }
            if(source instanceof PushBufferDataSource)
            {
                return reflectDS("com.ibm.media.protocol.CloneableCapturePushBufferDataSource", source);
            }
        }
        if(source instanceof PullDataSource)
        {
            return reflectDS("com.ibm.media.protocol.CloneablePullDataSource", source);
        }
        if(source instanceof PushDataSource)
        {
            return reflectDS("com.ibm.media.protocol.CloneablePushDataSource", source);
        }
        if(source instanceof PullBufferDataSource)
        {
            return reflectDS("com.ibm.media.protocol.CloneablePullBufferDataSource", source);
        }
        if(source instanceof PushBufferDataSource)
        {
            return reflectDS("com.ibm.media.protocol.CloneablePushBufferDataSource", source);
        } else
        {
            return null;
        }
    }

    public static TimeBase getSystemTimeBase()
    {
        if(sysTimeBase == null)
        {
            sysTimeBase = new SystemTimeBase();
        }
        return sysTimeBase;
    }

    static Player createPlayerForContent(MediaLocator sourceLocator, boolean useUnknownContent, Hashtable sources)
        throws IOException, NoPlayerException
    {
        Player newPlayer = null;
        boolean sourceUsed[] = new boolean[1];
        sourceUsed[0] = false;
        for(Enumeration protoList = getDataSourceList(sourceLocator.getProtocol()).elements(); protoList.hasMoreElements();)
        {
            String protoClassName = (String)protoList.nextElement();
            DataSource source = null;
            try
            {
                if((source = (DataSource)sources.get(protoClassName)) == null)
                {
                    Class protoClass = getClassForName(protoClassName);
                    source = (DataSource)protoClass.newInstance();
                    source.setLocator(sourceLocator);
                    source.connect();
                } else
                {
                    sources.remove(protoClassName);
                }
                try
                {
                    if(useUnknownContent)
                    {
                        newPlayer = createPlayerForSource(source, "unknown", sourceUsed);
                    } else
                    {
                        newPlayer = createPlayerForSource(source, source.getContentType(), sourceUsed);
                    }
                    break;
                }
                catch(NoPlayerException e)
                {
                    newPlayer = null;
                    if(sourceUsed[0])
                    {
                        source.disconnect();
                    } else
                    {
                        sources.put(protoClassName, source);
                    }
                }
            }
            catch(ClassNotFoundException e)
            {
                source = null;
            }
            catch(InstantiationException e)
            {
                source = null;
            }
            catch(IllegalAccessException e)
            {
                source = null;
            }
            catch(Exception e)
            {
                source = null;
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
                throw new NoPlayerException(err);
            }
            catch(Error e)
            {
                source = null;
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
                throw new NoPlayerException(err);
            }
        }

        if(newPlayer == null)
        {
            throw new NoPlayerException("Cannot find a Player for :" + sourceLocator);
        } else
        {
            return newPlayer;
        }
    }

    static Player createPlayerForSource(DataSource source, String contentTypeName, boolean sourceUsed[])
        throws IOException, NoPlayerException
    {
        Player newPlayer = null;
        if(sourceUsed != null)
        {
            sourceUsed[0] = true;
        }
        Enumeration playerList = getHandlerClassList(contentTypeName).elements();
        DataSource newSource = null;
        while(playerList.hasMoreElements()) 
        {
            String handlerClassName = (String)playerList.nextElement();
            try
            {
                Class handlerClass = getClassForName(handlerClassName);
                MediaHandler mHandler = (MediaHandler)handlerClass.newInstance();
                mHandler.setSource(source);
                if(mHandler instanceof Player)
                {
                    newPlayer = (Player)mHandler;
                    break;
                }
                MediaProxy mProxy = (MediaProxy)mHandler;
                newSource = mProxy.getDataSource();
                String newContentType = newSource.getContentType();
                try
                {
                    newPlayer = createPlayerForSource(newSource, newContentType, null);
                    continue;
                }
                catch(NoPlayerException e)
                {
                    newPlayer = createPlayerForSource(newSource, "unknown", null);
                }
                if(newPlayer != null)
                {
                    break;
                }
            }
            catch(ClassNotFoundException e)
            {
                newPlayer = null;
                if(sourceUsed != null)
                {
                    sourceUsed[0] = false;
                }
            }
            catch(InstantiationException e)
            {
                newPlayer = null;
                if(sourceUsed != null)
                {
                    sourceUsed[0] = false;
                }
            }
            catch(IllegalAccessException e)
            {
                newPlayer = null;
                if(sourceUsed != null)
                {
                    sourceUsed[0] = false;
                }
            }
            catch(IncompatibleSourceException e)
            {
                newPlayer = null;
            }
            catch(NoDataSourceException e)
            {
                newPlayer = null;
            }
            catch(Exception e)
            {
                newPlayer = null;
                String err = "Error instantiating class: " + handlerClassName + " : " + e;
                throw new NoPlayerException(err);
            }
            catch(Error e)
            {
                String err = "Error instantiating class: " + handlerClassName + " : " + e;
                //Log.error(e);
                throw new NoPlayerException(err);
            }
        }
        if(newPlayer == null)
        {
            throw new NoPlayerException("Cannot find a Player for: " + source);
        } else
        {
            //Log.comment("Player created: " + newPlayer);
            //Log.comment("  using DataSource: " + source + "\n");
            return newPlayer;
        }
    }

    static Processor createProcessorForContent(MediaLocator sourceLocator, boolean useUnknownContent, Hashtable sources)
        throws IOException, NoProcessorException
    {
        Processor newProcessor = null;
        boolean sourceUsed[] = new boolean[1];
        sourceUsed[0] = false;
        for(Enumeration protoList = getDataSourceList(sourceLocator.getProtocol()).elements(); protoList.hasMoreElements();)
        {
            String protoClassName = (String)protoList.nextElement();
            DataSource source = null;
            try
            {
                if((source = (DataSource)sources.get(protoClassName)) == null)
                {
                    Class protoClass = getClassForName(protoClassName);
                    source = (DataSource)protoClass.newInstance();
                    source.setLocator(sourceLocator);
                    source.connect();
                } else
                {
                    sources.remove(protoClassName);
                }
                try
                {
                    if(useUnknownContent)
                    {
                        newProcessor = createProcessorForSource(source, "unknown", sourceUsed);
                    } else
                    {
                        newProcessor = createProcessorForSource(source, source.getContentType(), sourceUsed);
                    }
                    break;
                }
                catch(NoProcessorException e)
                {
                    newProcessor = null;
                    if(sourceUsed[0])
                    {
                        source.disconnect();
                    } else
                    {
                        sources.put(protoClassName, source);
                    }
                }
            }
            catch(ClassNotFoundException e)
            {
                source = null;
            }
            catch(InstantiationException e)
            {
                source = null;
            }
            catch(IllegalAccessException e)
            {
                source = null;
            }
            catch(Exception e)
            {
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
                throw new NoProcessorException(err);
            }
            catch(Error e)
            {
                String err = "Error instantiating class: " + protoClassName + " : " + e;
                //Log.error(e);
                throw new NoProcessorException(err);
            }
        }

        if(newProcessor == null)
        {
            throw new NoProcessorException("Cannot find a Processor for: " + sourceLocator);
        } else
        {
            return newProcessor;
        }
    }

    static Processor createProcessorForSource(DataSource source, String contentTypeName, boolean sourceUsed[])
        throws IOException, NoProcessorException
    {
        Processor newProcessor = null;
        if(sourceUsed != null)
        {
            sourceUsed[0] = true;
        }
        Enumeration playerList = getProcessorClassList(contentTypeName).elements();
        DataSource newSource = null;
        while(playerList.hasMoreElements()) 
        {
            String handlerClassName = (String)playerList.nextElement();
            try
            {
                Class handlerClass = getClassForName(handlerClassName);
                MediaHandler mHandler = (MediaHandler)handlerClass.newInstance();
                mHandler.setSource(source);
                if(mHandler instanceof Processor)
                {
                    newProcessor = (Processor)mHandler;
                    break;
                }
                MediaProxy mProxy = (MediaProxy)mHandler;
                newSource = mProxy.getDataSource();
                String newContentType = newSource.getContentType();
                try
                {
                    newProcessor = createProcessorForSource(newSource, newContentType, null);
                    continue;
                }
                catch(NoProcessorException e)
                {
                    newProcessor = createProcessorForSource(newSource, "unknown", null);
                }
                if(newProcessor != null)
                {
                    break;
                }
            }
            catch(ClassNotFoundException e)
            {
                newProcessor = null;
                if(sourceUsed != null)
                {
                    sourceUsed[0] = false;
                }
            }
            catch(InstantiationException e)
            {
                newProcessor = null;
                if(sourceUsed != null)
                {
                    sourceUsed[0] = false;
                }
            }
            catch(IllegalAccessException e)
            {
                newProcessor = null;
                if(sourceUsed != null)
                {
                    sourceUsed[0] = false;
                }
            }
            catch(IncompatibleSourceException e)
            {
                newProcessor = null;
            }
            catch(NoDataSourceException e)
            {
                newProcessor = null;
            }
            catch(Exception e)
            {
                newProcessor = null;
                String err = "Error instantiating class: " + handlerClassName + " : " + e;
                //Log.error(e);
                throw new NoProcessorException(err);
            }
            catch(Error e)
            {
                newProcessor = null;
                String err = "Error instantiating class: " + handlerClassName + " : " + e;
                //Log.error(e);
                throw new NoProcessorException(err);
            }
        }
        if(newProcessor == null)
        {
            throw new NoProcessorException("Cannot find a Processor for: " + source);
        } else
        {
            //Log.comment("Processor created: " + newProcessor);
            //Log.comment("  using DataSource: " + source + "\n");
            return newProcessor;
        }
    }

    public static DataSink createDataSink(DataSource datasource, MediaLocator destLocator)
        throws NoDataSinkException
    {
        String handlerName = "media.datasink." + destLocator.getProtocol() + ".Handler";
        Vector classList = buildClassList(getContentPrefixList(), handlerName);
        Enumeration handlerList = classList.elements();
        DataSink dataSink = null;
        for(boolean done = false; !done && handlerList.hasMoreElements();)
        {
            String handlerClassName = (String)handlerList.nextElement();
            try
            {
                Class handlerClass = getClassForName(handlerClassName);
                Object object = handlerClass.newInstance();
                if(object instanceof DataSink)
                {
                    dataSink = (DataSink)object;
                    dataSink.setSource(datasource);
                    dataSink.setOutputLocator(destLocator);
                    done = true;
                    break;
                }
                DataSinkProxy dsProxy = (DataSinkProxy)object;
                String contentType = dsProxy.getContentType(destLocator);
                handlerName = "media.datasink." + destLocator.getProtocol() + "." + contentType + ".Handler";
                Vector dataSinkList = buildClassList(getContentPrefixList(), handlerName);
                for(Enumeration elements = dataSinkList.elements(); elements.hasMoreElements();)
                {
                    String dsClassName = (String)elements.nextElement();
                    try
                    {
                        dataSink = (DataSink)getClassForName(dsClassName).newInstance();
                        dataSink.setSource(datasource);
                        dataSink.setOutputLocator(destLocator);
                        done = true;
                        break;
                    }
                    catch(Exception e)
                    {
                        dataSink = null;
                    }
                }

            }
            catch(Exception e)
            {
                dataSink = null;
            }
            catch(Error e)
            {
                dataSink = null;
            }
        }

        if(dataSink == null)
        {
            throw new NoDataSinkException("Cannot find a DataSink for: " + datasource);
        } else
        {
            //Log.comment("DataSink created: " + dataSink);
            //Log.comment("  using DataSource: " + datasource + "\n");
            return dataSink;
        }
    }

//    public static String getCacheDirectory()
//    {
//        Object cdir = Registry.get("secure.cacheDir");
//        String cacheDir;
//        if(cdir != null && (cdir instanceof String))
//        {
//            cacheDir = (String)cdir;
//            if(cacheDir.indexOf(fileSeparator) == -1)
//            {
//                if(fileSeparator.equals("/"))
//                {
//                    cacheDir = "/tmp";
//                } else
//                if(fileSeparator.equals("\\"))
//                {
//                    cacheDir = "C:" + fileSeparator + "temp";
//                } else
//                {
//                    cacheDir = null;
//                }
//            }
//            return cacheDir;
//        }
//        if(fileSeparator.equals("/"))
//        {
//            cacheDir = "/tmp";
//        } else
//        if(fileSeparator.equals("\\"))
//        {
//            cacheDir = "C:" + fileSeparator + "temp";
//        } else
//        {
//            cacheDir = null;
//        }
//        return cacheDir;
//    }

    public static void setHint(int hint, Object value)
    {
        if(value != null && hint >= 1 && hint <= numberOfHints)
        {
            hintTable.put(new Integer(hint), value);
        }
    }

    public static Object getHint(int hint)
    {
        if(hint >= 1 && hint <= numberOfHints)
        {
            return hintTable.get(new Integer(hint));
        } else
        {
            return null;
        }
    }

    private static void blockingCall(Player p, int state)
        throws CannotRealizeException
    {
        boolean sync[] = new boolean[2];
        sync[0] = false;
        sync[1] = false;
        ControllerListener cl = new MCA(sync, state);
        p.addControllerListener(cl);
        if(state == 300)
        {
            p.realize();
        } else
        if(state == 180)
        {
            ((Processor)p).configure();
        }
        synchronized(sync)
        {
            while(!sync[0]) 
            {
                try
                {
                    sync.wait();
                }
                catch(InterruptedException e) { }
            }
        }
        p.removeControllerListener(cl);
        if(!sync[1])
        {
            throw new CannotRealizeException();
        } else
        {
            return;
        }
    }

    public static Vector getDataSourceList(String protocolName)
    {
        String sourceName = "media.protocol." + protocolName + ".DataSource";
        return buildClassList(getProtocolPrefixList(), sourceName);
    }

    public static Vector getHandlerClassList(String contentName)
    {
        String handlerName = "media.content." + ContentDescriptor.mimeTypeToPackageName(contentName) + ".Handler";
        return buildClassList(getContentPrefixList(), handlerName);
    }

    public static Vector getProcessorClassList(String contentName)
    {
        String handlerName = "media.processor." + ContentDescriptor.mimeTypeToPackageName(contentName) + ".Handler";
        return buildClassList(getContentPrefixList(), handlerName);
    }

    static Vector buildClassList(Vector prefixList, String name)
    {
        Vector classList = new Vector();
        classList.addElement(name);
        String prefixName;
        for(Enumeration prefix = prefixList.elements(); prefix.hasMoreElements(); classList.addElement(prefixName + "." + name))
        {
            prefixName = (String)prefix.nextElement();
        }

        return classList;
    }

    static Vector getContentPrefixList()
    {
        return (Vector)PackageManager.getContentPrefixList().clone();
    }

    static Vector getProtocolPrefixList()
    {
        return (Vector)PackageManager.getProtocolPrefixList().clone();
    }

    private static int getNTypesOfCaptureDevices()
    {
        int nDevices = 0;
        Vector audioDevs = CaptureDeviceManager.getDeviceList(new AudioFormat(null));
        Vector videoDevs = CaptureDeviceManager.getDeviceList(new VideoFormat(null));
        if(audioDevs != null && audioDevs.size() > 0)
        {
            nDevices++;
        }
        if(videoDevs != null && videoDevs.size() > 0)
        {
            nDevices++;
        }
        return nDevices;
    }

    private static boolean checkIfJDK12()
    {
        if(jdkInit)
        {
            return forName3ArgsM != null;
        }
        jdkInit = true;
        try
        {
            forName3ArgsM = (java.lang.Class.class).getMethod("forName", new Class[] {
                java.lang.String.class, Boolean.TYPE, java.lang.ClassLoader.class
            });
            getSystemClassLoaderM = (java.lang.ClassLoader.class).getMethod("getSystemClassLoader", null);
            systemClassLoader = (ClassLoader)getSystemClassLoaderM.invoke(java.lang.ClassLoader.class, null);
            getContextClassLoaderM = (java.lang.Thread.class).getMethod("getContextClassLoader", null);
            return true;
        }
        catch(Throwable t)
        {
            forName3ArgsM = null;
        }
        return false;
    }

    static Class getClassForName(String className)
        throws ClassNotFoundException
    {
        try
        {
            return Class.forName(className);
        }
        catch(Exception e)
        {
            if(!checkIfJDK12())
            {
                throw new ClassNotFoundException(e.getMessage());
            }
        }
        catch(Error e)
        {
            if(!checkIfJDK12())
            {
                throw e;
            }
        }
        try
        {
            return (Class)forName3ArgsM.invoke(java.lang.Class.class, new Object[] {
                className, new Boolean(true), systemClassLoader
            });
        }
        catch(Throwable e) { }
        try
        {
            ClassLoader contextClassLoader = (ClassLoader)getContextClassLoaderM.invoke(Thread.currentThread(), null);
            return (Class)forName3ArgsM.invoke(java.lang.Class.class, new Object[] {
                className, new Boolean(true), contextClassLoader
            });
        }
        catch(Exception e)
        {
            throw new ClassNotFoundException(e.getMessage());
        }
        catch(Error e)
        {
            throw e;
        }
    }

    static 
    {
        hintTable = new Hashtable();
        hintTable.put(new Integer(1), new Boolean(false));
        hintTable.put(new Integer(2), new Boolean(true));
        hintTable.put(new Integer(3), new Boolean(false));
        hintTable.put(new Integer(4), new Boolean(false));
    }
}
