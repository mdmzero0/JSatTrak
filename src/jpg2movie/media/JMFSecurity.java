/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
// Source File Name:   JMFSecurity.java

package jpg2movie.media;

import java.lang.reflect.Method;

public interface JMFSecurity
{

    public abstract String getName();

    public abstract void requestPermission(Method amethod[], Class aclass[], Object aobj[][], int i)
        throws SecurityException;

    public abstract void requestPermission(Method amethod[], Class aclass[], Object aobj[][], int i, String s)
        throws SecurityException;

    public abstract boolean isLinkPermissionEnabled();

    public abstract void permissionFailureNotification(int i);

    public abstract void loadLibrary(String s)
        throws UnsatisfiedLinkError;

    public static final int READ_PROPERTY = 1;
    public static final int READ_FILE = 2;
    public static final int WRITE_FILE = 4;
    public static final int DELETE_FILE = 8;
    public static final int THREAD = 16;
    public static final int THREAD_GROUP = 32;
    public static final int LINK = 64;
    public static final int CONNECT = 128;
    public static final int TOP_LEVEL_WINDOW = 256;
    public static final int MULTICAST = 512;
}
