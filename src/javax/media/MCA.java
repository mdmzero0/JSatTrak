/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   Manager.java

package javax.media;


// Referenced classes of package javax.media:
//            ControllerAdapter, ConfigureCompleteEvent, RealizeCompleteEvent, ControllerErrorEvent, 
//            DeallocateEvent, ControllerClosedEvent

class MCA extends ControllerAdapter
{

    public MCA(boolean sync[], int state)
    {
        this.sync = sync;
        this.state = state;
    }

    private void succeed()
    {
        synchronized(sync)
        {
            sync[0] = true;
            sync[1] = true;
            sync.notify();
        }
    }

    private void fail()
    {
        synchronized(sync)
        {
            sync[0] = true;
            sync[1] = false;
            sync.notify();
        }
    }

    public void configureComplete(ConfigureCompleteEvent evt)
    {
        if(state == 180)
            succeed();
    }

    public void realizeComplete(RealizeCompleteEvent evt)
    {
        if(state == 300)
            succeed();
    }

    public void controllerError(ControllerErrorEvent evt)
    {
        fail();
    }

    public void deallocate(DeallocateEvent evt)
    {
        fail();
    }

    public void controllerClosed(ControllerClosedEvent evt)
    {
        fail();
    }

    boolean sync[];
    int state;
}
