/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
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
