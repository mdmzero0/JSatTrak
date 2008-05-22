// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaHandler.java

package javax.media;

import java.io.IOException;
import javax.media.protocol.DataSource;

// Referenced classes of package javax.media:
//            IncompatibleSourceException

public interface MediaHandler
{

    public abstract void setSource(DataSource datasource)
        throws IOException, IncompatibleSourceException;
}
