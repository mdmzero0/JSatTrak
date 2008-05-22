// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MpegParser.java

package com.ibm.media.parser.video;

import com.sun.media.Log;
import com.sun.media.util.LoopThread;
import com.sun.media.util.MediaThread;
import java.io.IOException;
import javax.media.BadHeaderException;

// Referenced classes of package com.ibm.media.parser.video:
//            BadDataException, MpegParser

class MpegBufferThread extends LoopThread
{

    MpegBufferThread()
    {
        setName(getName() + " (MpegBufferThread)");
        useVideoPriority();
    }

    void setParser(MpegParser p)
    {
        parser = p;
    }

    public boolean process()
    {
        if(parser.EOMflag)
        {
            parser.updateTrackEOM();
            pause();
            return true;
        }
        try
        {
            parser.mpegSystemParseBitstream(false, 0L, false, 0xffffffffffcd232bL);
        }
        catch(BadDataException e)
        {
            parser.parserErrorFlag = true;
        }
        catch(BadHeaderException e)
        {
            parser.parserErrorFlag = true;
        }
        catch(IOException e)
        {
            parser.updateEOMState();
            parser.EOMflag = true;
            if(parser.endPTS == 0xffffffffffcd232bL)
                parser.endPTS = parser.currentPTS;
        }
        if(parser.parserErrorFlag)
        {
            Log.error("MPEG parser error: possibly with a corrupted bitstream.");
            pause();
        }
        return true;
    }

    private MpegParser parser;
}
