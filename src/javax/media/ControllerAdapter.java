// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ControllerAdapter.java

package javax.media;

import java.util.EventListener;
import javax.media.format.FormatChangeEvent;

// Referenced classes of package javax.media:
//            CachingControlEvent, ControllerErrorEvent, DataLostErrorEvent, InternalErrorEvent, 
//            ResourceUnavailableEvent, ConnectionErrorEvent, DurationUpdateEvent, MediaTimeSetEvent, 
//            RateChangeEvent, StopTimeChangeEvent, AudioDeviceUnavailableEvent, ControllerClosedEvent, 
//            SizeChangeEvent, TransitionEvent, ConfigureCompleteEvent, PrefetchCompleteEvent, 
//            RealizeCompleteEvent, StartEvent, StopEvent, DeallocateEvent, 
//            EndOfMediaEvent, RestartingEvent, StopAtTimeEvent, StopByRequestEvent, 
//            DataStarvedEvent, ControllerListener, ControllerEvent

public class ControllerAdapter
    implements ControllerListener, EventListener
{

    public ControllerAdapter()
    {
    }

    public void cachingControl(CachingControlEvent cachingcontrolevent)
    {
    }

    public void controllerError(ControllerErrorEvent controllererrorevent)
    {
    }

    public void dataLostError(DataLostErrorEvent datalosterrorevent)
    {
    }

    public void dataStarved(DataStarvedEvent datastarvedevent)
    {
    }

    public void internalError(InternalErrorEvent internalerrorevent)
    {
    }

    public void resourceUnavailable(ResourceUnavailableEvent resourceunavailableevent)
    {
    }

    public void durationUpdate(DurationUpdateEvent durationupdateevent)
    {
    }

    public void mediaTimeSet(MediaTimeSetEvent mediatimesetevent)
    {
    }

    public void rateChange(RateChangeEvent ratechangeevent)
    {
    }

    public void stopTimeChange(StopTimeChangeEvent stoptimechangeevent)
    {
    }

    public void transition(TransitionEvent transitionevent)
    {
    }

    public void prefetchComplete(PrefetchCompleteEvent prefetchcompleteevent)
    {
    }

    public void realizeComplete(RealizeCompleteEvent realizecompleteevent)
    {
    }

    public void start(StartEvent startevent)
    {
    }

    public void stop(StopEvent stopevent)
    {
    }

    public void deallocate(DeallocateEvent deallocateevent)
    {
    }

    public void endOfMedia(EndOfMediaEvent endofmediaevent)
    {
    }

    public void restarting(RestartingEvent restartingevent)
    {
    }

    public void stopAtTime(StopAtTimeEvent stopattimeevent)
    {
    }

    public void stopByRequest(StopByRequestEvent stopbyrequestevent)
    {
    }

    public void audioDeviceUnavailable(AudioDeviceUnavailableEvent audiodeviceunavailableevent)
    {
    }

    public void configureComplete(ConfigureCompleteEvent configurecompleteevent)
    {
    }

    public void controllerClosed(ControllerClosedEvent controllerclosedevent)
    {
    }

    public void sizeChange(SizeChangeEvent sizechangeevent)
    {
    }

    public void connectionError(ConnectionErrorEvent connectionerrorevent)
    {
    }

    public void formatChange(FormatChangeEvent formatchangeevent)
    {
    }

    public void replaceURL(Object obj)
    {
    }

    public void showDocument(Object obj)
    {
    }

    public void controllerUpdate(ControllerEvent e)
    {
        if(e instanceof FormatChangeEvent)
            formatChange((FormatChangeEvent)e);
        else
        if(e instanceof CachingControlEvent)
            cachingControl((CachingControlEvent)e);
        else
        if(e instanceof ControllerErrorEvent)
        {
            controllerError((ControllerErrorEvent)e);
            if(e instanceof DataLostErrorEvent)
                dataLostError((DataLostErrorEvent)e);
            else
            if(e instanceof InternalErrorEvent)
                internalError((InternalErrorEvent)e);
            else
            if(e instanceof ResourceUnavailableEvent)
                resourceUnavailable((ResourceUnavailableEvent)e);
            else
            if(e instanceof ConnectionErrorEvent)
                connectionError((ConnectionErrorEvent)e);
        } else
        if(e instanceof DurationUpdateEvent)
            durationUpdate((DurationUpdateEvent)e);
        else
        if(e instanceof MediaTimeSetEvent)
            mediaTimeSet((MediaTimeSetEvent)e);
        else
        if(e instanceof RateChangeEvent)
            rateChange((RateChangeEvent)e);
        else
        if(e instanceof StopTimeChangeEvent)
            stopTimeChange((StopTimeChangeEvent)e);
        else
        if(e instanceof AudioDeviceUnavailableEvent)
            audioDeviceUnavailable((AudioDeviceUnavailableEvent)e);
        else
        if(e instanceof ControllerClosedEvent)
            controllerClosed((ControllerClosedEvent)e);
        else
        if(e instanceof SizeChangeEvent)
            sizeChange((SizeChangeEvent)e);
        else
        if(e instanceof TransitionEvent)
        {
            transition((TransitionEvent)e);
            if(e instanceof ConfigureCompleteEvent)
                configureComplete((ConfigureCompleteEvent)e);
            else
            if(e instanceof PrefetchCompleteEvent)
                prefetchComplete((PrefetchCompleteEvent)e);
            else
            if(e instanceof RealizeCompleteEvent)
                realizeComplete((RealizeCompleteEvent)e);
            else
            if(e instanceof StartEvent)
                start((StartEvent)e);
            else
            if(e instanceof StopEvent)
            {
                stop((StopEvent)e);
                if(e instanceof DeallocateEvent)
                    deallocate((DeallocateEvent)e);
                else
                if(e instanceof EndOfMediaEvent)
                    endOfMedia((EndOfMediaEvent)e);
                else
                if(e instanceof RestartingEvent)
                    restarting((RestartingEvent)e);
                else
                if(e instanceof StopAtTimeEvent)
                    stopAtTime((StopAtTimeEvent)e);
                else
                if(e instanceof StopByRequestEvent)
                    stopByRequest((StopByRequestEvent)e);
                else
                if(e instanceof DataStarvedEvent)
                    dataStarved((DataStarvedEvent)e);
            }
        } else
        if(e.getClass().getName().equals("com.ibm.media.ReplaceURLEvent"))
            replaceURL(e);
        else
        if(e.getClass().getName().equals("com.ibm.media.ShowDocumentEvent"))
            showDocument(e);
    }
}
