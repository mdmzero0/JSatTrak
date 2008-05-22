/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   Clock.java

package javax.media;


// Referenced classes of package javax.media:
//            Time, IncompatibleTimeBaseException, ClockStoppedException, TimeBase

public interface Clock
{

    public abstract void setTimeBase(TimeBase timebase)
        throws IncompatibleTimeBaseException;

    public abstract void syncStart(Time time);

    public abstract void stop();

    public abstract void setStopTime(Time time);

    public abstract Time getStopTime();

    public abstract void setMediaTime(Time time);

    public abstract Time getMediaTime();

    public abstract long getMediaNanoseconds();

    public abstract Time getSyncTime();

    public abstract TimeBase getTimeBase();

    public abstract Time mapToTimeBase(Time time)
        throws ClockStoppedException;

    public abstract float getRate();

    public abstract float setRate(float f);

    public static final Time RESET = new Time(0x7fffffffffffffffL);

}
