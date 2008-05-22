// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   H263Adapter.java

package com.sun.media.controls;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.lang.reflect.Method;
import javax.media.Codec;
import javax.media.control.H263Control;

// Referenced classes of package com.sun.media.controls:
//            VFlowLayout

public class H263Adapter
    implements H263Control
{
    class H263AdapterListener
        implements ItemListener
    {

        public void itemStateChanged(ItemEvent e)
        {
            Object result = null;
            try
            {
                boolean newState = cb.getState();
                Boolean operands[] = {
                    newState ? Boolean.TRUE : Boolean.FALSE
                };
                result = m.invoke(owner, (Object[])operands);
            }
            catch(Exception exception) { }
            cb.setState(result.equals(Boolean.TRUE));
        }

        Checkbox cb;
        Method m;
        H263Adapter owner;

        public H263AdapterListener(Checkbox source, H263Adapter h263adaptor, Method action)
        {
            cb = source;
            m = action;
            owner = h263adaptor;
        }
    }


    public H263Adapter(Codec newOwner, boolean newAdvancedPrediction, boolean newArithmeticCoding, boolean newErrorCompensation, boolean newPBFrames, boolean newUnrestrictedVector, int newHrd_B, 
            int newBppMaxKb, boolean newIsSetable)
    {
        owner = null;
        advancedPrediction = false;
        arithmeticCoding = false;
        errorCompensation = false;
        pbFrames = false;
        unrestrictedVector = false;
        hrd_B = -1;
        bppMaxKb = -1;
        component = null;
        CONTROL_ADVANCEDPREDICTION_STRING = "Advanced Prediction";
        CONTROL_ARITHMETICCODING_STRING = "Arithmetic Coding";
        CONTROL_ERRORCOMPENSATION_STRING = "Error Compensation";
        CONTROL_PBFRAMES_STRING = "PB Frames";
        CONTROL_UNRESTRICTEDVECTOR_STRING = "Unrestricted Vector";
        CONTROL_HRD_B_STRING = "Hrd B";
        CONTROL_BPPMAXKB_STRING = "Bpp Max Kb";
        advancedPrediction = newAdvancedPrediction;
        arithmeticCoding = newArithmeticCoding;
        errorCompensation = newErrorCompensation;
        pbFrames = newPBFrames;
        unrestrictedVector = newUnrestrictedVector;
        hrd_B = newHrd_B;
        bppMaxKb = newBppMaxKb;
        owner = newOwner;
        isSetable = newIsSetable;
    }

    public boolean isUnrestrictedVectorSupported()
    {
        return unrestrictedVector;
    }

    public boolean setUnrestrictedVector(boolean newUnrestrictedVectorMode)
    {
        return unrestrictedVector;
    }

    public boolean getUnrestrictedVector()
    {
        return unrestrictedVector;
    }

    public boolean isArithmeticCodingSupported()
    {
        return arithmeticCoding;
    }

    public boolean setArithmeticCoding(boolean newArithmeticCodingMode)
    {
        return arithmeticCoding;
    }

    public boolean getArithmeticCoding()
    {
        return arithmeticCoding;
    }

    public boolean isAdvancedPredictionSupported()
    {
        return advancedPrediction;
    }

    public boolean setAdvancedPrediction(boolean newAdvancedPredictionMode)
    {
        return advancedPrediction;
    }

    public boolean getAdvancedPrediction()
    {
        return advancedPrediction;
    }

    public boolean isPBFramesSupported()
    {
        return pbFrames;
    }

    public boolean setPBFrames(boolean newPBFramesMode)
    {
        return pbFrames;
    }

    public boolean getPBFrames()
    {
        return pbFrames;
    }

    public boolean isErrorCompensationSupported()
    {
        return errorCompensation;
    }

    public boolean setErrorCompensation(boolean newtErrorCompensationMode)
    {
        return errorCompensation;
    }

    public boolean getErrorCompensation()
    {
        return errorCompensation;
    }

    public int getHRD_B()
    {
        return hrd_B;
    }

    public int getBppMaxKb()
    {
        return bppMaxKb;
    }

    public Component getControlComponent()
    {
        if(component == null)
            try
            {
                Class booleanArray[] = {
                    Boolean.TYPE
                };
                Panel componentPanel = new Panel();
                componentPanel.setLayout(new VFlowLayout(1));
                Panel tempPanel = new Panel();
                tempPanel.setLayout(new BorderLayout());
                tempPanel.add("Center", new Label(CONTROL_ADVANCEDPREDICTION_STRING, 1));
                Checkbox cb = new Checkbox(null, null, advancedPrediction);
                cb.setEnabled(isSetable);
                cb.addItemListener(new H263AdapterListener(cb, this, getClass().getMethod("setAdvancedPrediction", booleanArray)));
                tempPanel.add("East", cb);
                tempPanel.invalidate();
                componentPanel.add(tempPanel);
                tempPanel = new Panel();
                tempPanel.setLayout(new BorderLayout());
                tempPanel.add("Center", new Label(CONTROL_ARITHMETICCODING_STRING, 1));
                cb = new Checkbox(null, null, arithmeticCoding);
                cb.setEnabled(isSetable);
                cb.addItemListener(new H263AdapterListener(cb, this, getClass().getMethod("setArithmeticCoding", booleanArray)));
                tempPanel.add("East", cb);
                tempPanel.invalidate();
                componentPanel.add(tempPanel);
                tempPanel = new Panel();
                tempPanel.setLayout(new BorderLayout());
                tempPanel.add("Center", new Label(CONTROL_ERRORCOMPENSATION_STRING, 1));
                cb = new Checkbox(null, null, errorCompensation);
                cb.setEnabled(isSetable);
                cb.addItemListener(new H263AdapterListener(cb, this, getClass().getMethod("setErrorCompensation", booleanArray)));
                tempPanel.add("East", cb);
                tempPanel.invalidate();
                componentPanel.add(tempPanel);
                tempPanel = new Panel();
                tempPanel.setLayout(new BorderLayout());
                tempPanel.add("Center", new Label(CONTROL_PBFRAMES_STRING, 1));
                cb = new Checkbox(null, null, pbFrames);
                cb.setEnabled(isSetable);
                cb.addItemListener(new H263AdapterListener(cb, this, getClass().getMethod("setPBFrames", booleanArray)));
                tempPanel.add("East", cb);
                tempPanel.invalidate();
                componentPanel.add(tempPanel);
                tempPanel = new Panel();
                tempPanel.setLayout(new BorderLayout());
                tempPanel.add("Center", new Label(CONTROL_UNRESTRICTEDVECTOR_STRING, 1));
                cb = new Checkbox(null, null, unrestrictedVector);
                cb.setEnabled(isSetable);
                cb.addItemListener(new H263AdapterListener(cb, this, getClass().getMethod("setUnrestrictedVector", booleanArray)));
                tempPanel.add("East", cb);
                tempPanel.invalidate();
                componentPanel.add(tempPanel);
                tempPanel = new Panel();
                tempPanel.setLayout(new BorderLayout());
                tempPanel.add("Center", new Label(CONTROL_HRD_B_STRING, 1));
                tempPanel.add("East", new Label(hrd_B + "", 1));
                tempPanel.invalidate();
                componentPanel.add(tempPanel);
                tempPanel = new Panel();
                tempPanel.setLayout(new BorderLayout());
                tempPanel.add("Center", new Label(CONTROL_BPPMAXKB_STRING, 1));
                tempPanel.add("East", new Label(bppMaxKb + "", 1));
                tempPanel.invalidate();
                componentPanel.add(tempPanel);
                component = componentPanel;
            }
            catch(Exception exception) { }
        return component;
    }

    boolean isSetable;
    Codec owner;
    boolean advancedPrediction;
    boolean arithmeticCoding;
    boolean errorCompensation;
    boolean pbFrames;
    boolean unrestrictedVector;
    int hrd_B;
    int bppMaxKb;
    Component component;
    String CONTROL_ADVANCEDPREDICTION_STRING;
    String CONTROL_ARITHMETICCODING_STRING;
    String CONTROL_ERRORCOMPENSATION_STRING;
    String CONTROL_PBFRAMES_STRING;
    String CONTROL_UNRESTRICTEDVECTOR_STRING;
    String CONTROL_HRD_B_STRING;
    String CONTROL_BPPMAXKB_STRING;
}
