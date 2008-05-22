// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MediaProxy.java

package javax.media;

import java.io.IOException;
import javax.media.protocol.DataSource;

// Referenced classes of package javax.media:
//            MediaHandler, NoDataSourceException

public interface MediaProxy
    extends MediaHandler
{

    public abstract DataSource getDataSource()
        throws IOException, NoDataSourceException;
}
