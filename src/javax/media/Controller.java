/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   Controller.java

package javax.media;


// Referenced classes of package javax.media:
//            Time, Clock, Duration, Control, 
//            ControllerListener

public interface Controller
    extends Clock, Duration
{

    public abstract int getState();

    public abstract int getTargetState();

    public abstract void realize();

    public abstract void prefetch();

    public abstract void deallocate();

    public abstract void close();

    public abstract Time getStartLatency();

    public abstract Control[] getControls();

    public abstract Control getControl(String s);

    public abstract void addControllerListener(ControllerListener controllerlistener);

    public abstract void removeControllerListener(ControllerListener controllerlistener);

    public static final Time LATENCY_UNKNOWN = new Time(0x7fffffffffffffffL);
    public static final int Unrealized = 100;
    public static final int Realizing = 200;
    public static final int Realized = 300;
    public static final int Prefetching = 400;
    public static final int Prefetched = 500;
    public static final int Started = 600;

}
