// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MergingCDPushBDS.java

package com.ibm.media.protocol;

import java.util.Vector;
import javax.media.CaptureDeviceInfo;
import javax.media.control.FormatControl;
import javax.media.protocol.CaptureDevice;
import javax.media.protocol.PushBufferDataSource;

// Referenced classes of package com.ibm.media.protocol:
//            MergingPushBufferDataSource

public class MergingCDPushBDS extends MergingPushBufferDataSource
    implements CaptureDevice
{

    public MergingCDPushBDS(PushBufferDataSource sources[])
    {
        super(sources);
        fcontrols = null;
        consolidateFormatControls(sources);
    }

    public FormatControl[] getFormatControls()
    {
        return fcontrols;
    }

    public CaptureDeviceInfo getCaptureDeviceInfo()
    {
        return null;
    }

    protected void consolidateFormatControls(PushBufferDataSource sources[])
    {
        Vector fcs = new Vector(1);
        for(int i = 0; i < sources.length; i++)
            if(sources[i] instanceof CaptureDevice)
            {
                CaptureDevice cd = (CaptureDevice)sources[i];
                FormatControl cdfcs[] = cd.getFormatControls();
                for(int j = 0; j < cdfcs.length; j++)
                    fcs.addElement(cdfcs[j]);

            }

        if(fcs.size() > 0)
        {
            fcontrols = new FormatControl[fcs.size()];
            for(int f = 0; f < fcs.size(); f++)
                fcontrols[f] = (FormatControl)fcs.elementAt(f);

        } else
        {
            fcontrols = new FormatControl[0];
        }
    }

    FormatControl fcontrols[];
}
