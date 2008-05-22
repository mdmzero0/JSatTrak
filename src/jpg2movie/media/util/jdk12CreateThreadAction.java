/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   jdk12CreateThreadAction.java

package jpg2movie.media.util;

import java.lang.reflect.Constructor;
import java.security.PrivilegedAction;

public class jdk12CreateThreadAction
    implements PrivilegedAction
{

    public jdk12CreateThreadAction(Class threadclass, String name)
    {
        this.name = null;
        try
        {
            this.threadclass = threadclass;
            this.name = name;
        }
        catch(Throwable e) { }
    }

    public jdk12CreateThreadAction(Class threadclass)
    {
        this(threadclass, null);
    }

    public Object run()
    {
        try
        {
            Object object = threadclass.newInstance();
            if(name != null)
                ((Thread)object).setName(name);
            return object;
        }
        catch(Throwable e)
        {
            return null;
        }
    }

    static Class _mthclass$(String x0)
    {
        try
        {
            return Class.forName(x0);
        }
        catch(ClassNotFoundException x1)
        {
            throw new NoClassDefFoundError(x1.getMessage());
        }
    }

    private Class threadclass;
    private String name;
    public static Constructor cons;
    public static Constructor conswithname;

    static 
    {
        try
        {
            cons = (jpg2movie.media.util.jdk12CreateThreadAction.class).getConstructor(new Class[] {
                java.lang.Class.class
            });
            conswithname = (jpg2movie.media.util.jdk12CreateThreadAction.class).getConstructor(new Class[] {
                java.lang.Class.class, java.lang.String.class
            });
        }
        catch(Throwable e) { }
    }
}
