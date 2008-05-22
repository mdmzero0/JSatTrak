// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AudioFormatChooser.java

package com.sun.media.ui;

import com.sun.media.util.JMFI18N;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import java.util.Vector;
import javax.media.Format;
import javax.media.format.AudioFormat;

public class AudioFormatChooser extends Panel
    implements ItemListener
{

    public AudioFormatChooser(Format arrFormats[], AudioFormat formatDefault)
    {
        this(arrFormats, formatDefault, false, null);
    }

    public AudioFormatChooser(Format arrFormats[], AudioFormat formatDefault, boolean boolDisplayEnableTrack, ActionListener listenerEnableTrack)
    {
        arrSupportedFormats = null;
        vectorContSuppFormats = new Vector();
        boolEnableTrackSaved = true;
        boolEnable8 = false;
        boolEnable16 = false;
        boolEnableMono = false;
        boolEnableStereo = false;
        boolEnableEndianBig = false;
        boolEnableEndianLittle = false;
        boolEnableSigned = false;
        arrSupportedFormats = arrFormats;
        this.boolDisplayEnableTrack = boolDisplayEnableTrack;
        this.listenerEnableTrack = listenerEnableTrack;
        int nCount = arrSupportedFormats.length;
        for(int i = 0; i < nCount; i++)
            if(arrSupportedFormats[i] instanceof AudioFormat)
                vectorContSuppFormats.addElement(arrSupportedFormats[i]);

        if(isFormatSupported(formatDefault))
            formatOld = formatDefault;
        else
            formatOld = null;
        try
        {
            init();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void setEnabled(boolean boolEnable)
    {
        super.setEnabled(boolEnable);
        if(checkEnableTrack != null)
            checkEnableTrack.setEnabled(boolEnable);
        enableControls(boolEnable);
    }

    public Format getFormat()
    {
        Format formatResult = null;
        String strEncoding = comboEncoding.getSelectedItem();
        String strSampleRate = comboSampleRate.getSelectedItem();
        double dSampleRate = Double.valueOf(strSampleRate).doubleValue();
        int nBits;
        if(checkBits8.getState() && checkBits8.isEnabled())
            nBits = 8;
        else
        if(checkBits16.getState() && checkBits16.isEnabled())
            nBits = 16;
        else
            nBits = -1;
        int nChannels;
        if(checkMono.getState() && checkMono.isEnabled())
            nChannels = 1;
        else
        if(checkStereo.getState() && checkStereo.isEnabled())
            nChannels = 2;
        else
            nChannels = -1;
        int nEndian;
        if(checkEndianBig.getState() && checkEndianBig.isEnabled())
            nEndian = 1;
        else
        if(checkEndianLittle.getState() && checkEndianLittle.isEnabled())
            nEndian = 0;
        else
            nEndian = -1;
        int nSigned;
        if(checkSigned.getState())
            nSigned = 1;
        else
            nSigned = 0;
        AudioFormat formatAudioNew = new AudioFormat(strEncoding, dSampleRate, nBits, nChannels, nEndian, nSigned);
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize && formatResult == null; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof AudioFormat)
            {
                AudioFormat formatAudio = (AudioFormat)objectFormat;
                if(isFormatGoodForEncoding(formatAudio) && isFormatGoodForSampleRate(formatAudio) && isFormatGoodForBitSize(formatAudio) && isFormatGoodForChannels(formatAudio) && isFormatGoodForEndian(formatAudio) && isFormatGoodForSigned(formatAudio) && formatAudio.matches(formatAudioNew))
                    formatResult = formatAudio.intersects(formatAudioNew);
            }
        }

        return formatResult;
    }

    public void setCurrentFormat(AudioFormat formatDefault)
    {
        if(isFormatSupported(formatDefault))
            formatOld = formatDefault;
        updateFields(formatOld);
    }

    public void setSupportedFormats(Format arrFormats[], AudioFormat formatDefault)
    {
        arrSupportedFormats = arrFormats;
        int nCount = arrSupportedFormats.length;
        vectorContSuppFormats.removeAllElements();
        for(int i = 0; i < nCount; i++)
            if(arrSupportedFormats[i] instanceof AudioFormat)
                vectorContSuppFormats.addElement(arrSupportedFormats[i]);

        if(isFormatSupported(formatDefault))
            formatOld = formatDefault;
        else
            formatOld = null;
        setSupportedFormats(vectorContSuppFormats);
    }

    public void setSupportedFormats(Vector vectorContSuppFormats)
    {
        this.vectorContSuppFormats = vectorContSuppFormats;
        if(vectorContSuppFormats.isEmpty())
        {
            checkEnableTrack.setState(false);
            checkEnableTrack.setEnabled(false);
            onEnableTrack(true);
            return;
        }
        checkEnableTrack.setEnabled(true);
        checkEnableTrack.setState(boolEnableTrackSaved);
        onEnableTrack(true);
        if(!isFormatSupported(formatOld))
            formatOld = null;
        updateFields(formatOld);
    }

    public void setTrackEnabled(boolean boolEnable)
    {
        boolEnableTrackSaved = boolEnable;
        if(checkEnableTrack == null)
        {
            return;
        } else
        {
            checkEnableTrack.setState(boolEnable);
            onEnableTrack(true);
            return;
        }
    }

    public boolean isTrackEnabled()
    {
        boolean boolEnabled = checkEnableTrack.getState();
        return boolEnabled;
    }

    private void init()
        throws Exception
    {
        setLayout(new BorderLayout(6, 6));
        Panel panel = this;
        checkEnableTrack = new Checkbox(JMFI18N.getResource("formatchooser.enabletrack"), true);
        checkEnableTrack.addItemListener(this);
        Panel panelGroup;
        if(boolDisplayEnableTrack)
        {
            panelGroup = new Panel(new BorderLayout());
            panel.add(panelGroup, "North");
            panelGroup.add(checkEnableTrack, "West");
        }
        panelGroup = new Panel(new BorderLayout(6, 6));
        panel.add(panelGroup, "Center");
        panel = panelGroup;
        panelGroup = new Panel(new BorderLayout());
        panel.add(panelGroup, "North");
        Panel panelLabel = new Panel(new GridLayout(0, 1, 6, 6));
        panelGroup.add(panelLabel, "West");
        Panel panelData = new Panel(new GridLayout(0, 1, 6, 6));
        panelGroup.add(panelData, "Center");
        labelEncoding = new Label(JMFI18N.getResource("formatchooser.encoding"), 0);
        panelLabel.add(labelEncoding);
        comboEncoding = new Choice();
        comboEncoding.addItemListener(this);
        panelData.add(comboEncoding);
        labelSampleRate = new Label(JMFI18N.getResource("formatchooser.samplerate"), 0);
        panelLabel.add(labelSampleRate);
        Panel panelEntry = new Panel(new BorderLayout(6, 6));
        panelData.add(panelEntry);
        comboSampleRate = new Choice();
        comboSampleRate.addItemListener(this);
        panelEntry.add(comboSampleRate, "Center");
        labelHz = new Label(JMFI18N.getResource("formatchooser.hz"));
        panelEntry.add(labelHz, "East");
        labelBitsPerSample = new Label(JMFI18N.getResource("formatchooser.bitspersample"), 0);
        panelLabel.add(labelBitsPerSample);
        panelEntry = new Panel(new GridLayout(1, 0, 6, 6));
        panelData.add(panelEntry);
        groupBitsPerSample = new CheckboxGroup();
        checkBits8 = new Checkbox(JMFI18N.getResource("formatchooser.8bit"), groupBitsPerSample, false);
        checkBits8.addItemListener(this);
        panelEntry.add(checkBits8);
        checkBits16 = new Checkbox(JMFI18N.getResource("formatchooser.16bit"), groupBitsPerSample, false);
        checkBits16.addItemListener(this);
        panelEntry.add(checkBits16);
        labelChannels = new Label(JMFI18N.getResource("formatchooser.channels"), 0);
        panelLabel.add(labelChannels);
        panelEntry = new Panel(new GridLayout(1, 0, 6, 6));
        panelData.add(panelEntry);
        groupChannels = new CheckboxGroup();
        checkMono = new Checkbox(JMFI18N.getResource("formatchooser.mono"), groupChannels, false);
        checkMono.addItemListener(this);
        panelEntry.add(checkMono);
        checkStereo = new Checkbox(JMFI18N.getResource("formatchooser.stereo"), groupChannels, false);
        checkStereo.addItemListener(this);
        panelEntry.add(checkStereo);
        labelEndian = new Label(JMFI18N.getResource("formatchooser.endian"), 0);
        panelLabel.add(labelEndian);
        panelEntry = new Panel(new GridLayout(1, 0, 6, 6));
        panelData.add(panelEntry);
        groupEndian = new CheckboxGroup();
        checkEndianBig = new Checkbox(JMFI18N.getResource("formatchooser.endian.big"), groupEndian, false);
        checkEndianBig.addItemListener(this);
        panelEntry.add(checkEndianBig);
        checkEndianLittle = new Checkbox(JMFI18N.getResource("formatchooser.endian.little"), groupEndian, false);
        checkEndianLittle.addItemListener(this);
        panelEntry.add(checkEndianLittle);
        panelGroup = new Panel(new BorderLayout(6, 6));
        panel.add(panelGroup, "Center");
        panel = panelGroup;
        panelGroup = new Panel(new BorderLayout());
        panel.add(panelGroup, "North");
        checkSigned = new Checkbox(JMFI18N.getResource("formatchooser.signed"), true);
        checkSigned.addItemListener(this);
        panelGroup.add(checkSigned, "West");
        updateFields(formatOld);
    }

    private void updateFields(AudioFormat formatDefault)
    {
        String strEncodingPref = null;
        Vector vectorEncoding = new Vector();
        boolean boolEnable = comboEncoding.isEnabled();
        comboEncoding.setEnabled(false);
        comboEncoding.removeAll();
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof AudioFormat)
            {
                AudioFormat formatAudio = (AudioFormat)objectFormat;
                String strEncoding = formatAudio.getEncoding().toUpperCase();
                if(!vectorEncoding.contains(strEncoding))
                {
                    comboEncoding.addItem(strEncoding);
                    vectorEncoding.addElement(strEncoding);
                    if(strEncodingPref == null)
                        strEncodingPref = strEncoding;
                }
            }
        }

        if(formatDefault != null)
        {
            String strEncoding = formatDefault.getEncoding();
            comboEncoding.select(strEncoding);
        } else
        if(strEncodingPref != null)
            comboEncoding.select(strEncodingPref);
        else
        if(comboEncoding.getItemCount() > 0)
            comboEncoding.select(0);
        updateFieldsFromEncoding(formatDefault);
        comboEncoding.setEnabled(boolEnable);
    }

    private void updateFieldsFromEncoding(AudioFormat formatDefault)
    {
        String strSampleRatePref = null;
        Vector vectorRates = new Vector();
        boolean boolEnable = comboSampleRate.isEnabled();
        comboSampleRate.setEnabled(false);
        comboSampleRate.removeAll();
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof AudioFormat)
            {
                AudioFormat formatAudio = (AudioFormat)objectFormat;
                if(isFormatGoodForEncoding(formatAudio))
                {
                    double dSampleRate = formatAudio.getSampleRate();
                    String strSampleRate = Double.toString(dSampleRate);
                    if(!vectorRates.contains(strSampleRate))
                    {
                        comboSampleRate.addItem(strSampleRate);
                        vectorRates.addElement(strSampleRate);
                        if(strSampleRatePref == null)
                            strSampleRatePref = strSampleRate;
                    }
                }
            }
        }

        if(formatDefault != null && isFormatGoodForEncoding(formatDefault))
            comboSampleRate.select(Double.toString(formatDefault.getSampleRate()));
        else
        if(strSampleRatePref != null)
            comboEncoding.select(strSampleRatePref);
        else
        if(comboSampleRate.getItemCount() > 0)
            comboSampleRate.select(0);
        updateFieldsFromRate(formatDefault);
        comboSampleRate.setEnabled(boolEnable);
    }

    private void updateFieldsFromRate(AudioFormat formatDefault)
    {
        int nBitsPref = -1;
        boolEnable8 = false;
        boolEnable16 = false;
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof AudioFormat)
            {
                AudioFormat formatAudio = (AudioFormat)objectFormat;
                if(isFormatGoodForEncoding(formatAudio) && isFormatGoodForSampleRate(formatAudio))
                {
                    int nBits = formatAudio.getSampleSizeInBits();
                    if(nBitsPref == -1)
                        nBitsPref = nBits;
                    if(nBits == -1)
                    {
                        boolEnable8 = true;
                        boolEnable16 = true;
                    } else
                    if(nBits == 8)
                        boolEnable8 = true;
                    else
                    if(nBits == 16)
                        boolEnable16 = true;
                }
            }
        }

        checkBits8.setEnabled(boolEnable8);
        checkBits16.setEnabled(boolEnable16);
        if(formatDefault != null && isFormatGoodForEncoding(formatDefault) && isFormatGoodForSampleRate(formatDefault))
        {
            int nBits = formatDefault.getSampleSizeInBits();
            if(nBits == 8)
                checkBits8.setState(true);
            else
            if(nBits == 16)
                checkBits16.setState(true);
        } else
        if(nBitsPref != -1)
        {
            if(nBitsPref == 8)
                checkBits8.setState(true);
            else
            if(nBitsPref == 16)
                checkBits16.setState(true);
        } else
        if(boolEnable8)
            checkBits8.setState(true);
        else
            checkBits16.setState(true);
        updateFieldsFromBits(formatDefault);
    }

    private void updateFieldsFromBits(AudioFormat formatDefault)
    {
        int nChannelsPref = -1;
        boolEnableMono = false;
        boolEnableStereo = false;
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof AudioFormat)
            {
                AudioFormat formatAudio = (AudioFormat)objectFormat;
                if(isFormatGoodForEncoding(formatAudio) && isFormatGoodForSampleRate(formatAudio) && isFormatGoodForBitSize(formatAudio))
                {
                    int nChannels = formatAudio.getChannels();
                    if(nChannelsPref == -1)
                        nChannelsPref = nChannels;
                    if(nChannels == -1)
                    {
                        boolEnableMono = true;
                        boolEnableStereo = true;
                    } else
                    if(nChannels == 1)
                        boolEnableMono = true;
                    else
                        boolEnableStereo = true;
                }
            }
        }

        checkMono.setEnabled(boolEnableMono);
        checkStereo.setEnabled(boolEnableStereo);
        if(formatDefault != null && isFormatGoodForEncoding(formatDefault) && isFormatGoodForSampleRate(formatDefault) && isFormatGoodForBitSize(formatDefault))
        {
            int nChannels = formatDefault.getChannels();
            if(nChannels == 1)
                checkMono.setState(true);
            else
                checkStereo.setState(true);
        } else
        if(nChannelsPref != -1)
        {
            if(nChannelsPref == 1)
                checkMono.setState(true);
            else
                checkStereo.setState(true);
        } else
        if(boolEnableMono)
            checkMono.setState(true);
        else
            checkStereo.setState(true);
        updateFieldsFromChannels(formatDefault);
    }

    private void updateFieldsFromChannels(AudioFormat formatDefault)
    {
        int nEndianPref = -1;
        boolEnableEndianBig = false;
        boolEnableEndianLittle = false;
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof AudioFormat)
            {
                AudioFormat formatAudio = (AudioFormat)objectFormat;
                if(isFormatGoodForEncoding(formatAudio) && isFormatGoodForSampleRate(formatAudio) && isFormatGoodForBitSize(formatAudio) && isFormatGoodForChannels(formatAudio))
                {
                    int nEndian = formatAudio.getEndian();
                    if(nEndianPref == -1)
                        nEndianPref = nEndian;
                    if(nEndian == -1)
                    {
                        boolEnableEndianBig = true;
                        boolEnableEndianLittle = true;
                    } else
                    if(nEndian == 1)
                        boolEnableEndianBig = true;
                    else
                        boolEnableEndianLittle = true;
                }
            }
        }

        checkEndianBig.setEnabled(boolEnableEndianBig);
        checkEndianLittle.setEnabled(boolEnableEndianLittle);
        if(formatDefault != null && isFormatGoodForEncoding(formatDefault) && isFormatGoodForSampleRate(formatDefault) && isFormatGoodForBitSize(formatDefault) && isFormatGoodForChannels(formatDefault))
        {
            int nEndian = formatDefault.getEndian();
            if(nEndian == 1)
                checkEndianBig.setState(true);
            else
                checkEndianLittle.setState(true);
        } else
        if(nEndianPref != -1)
        {
            if(nEndianPref == 1)
                checkEndianBig.setState(true);
            else
                checkEndianLittle.setState(true);
        } else
        if(boolEnableEndianBig)
            checkEndianBig.setState(true);
        else
            checkEndianLittle.setState(true);
        if(!checkBits16.getState())
        {
            boolEnableEndianBig = false;
            boolEnableEndianLittle = false;
            checkEndianBig.setEnabled(boolEnableEndianBig);
            checkEndianLittle.setEnabled(boolEnableEndianLittle);
        }
        updateFieldsFromEndian(formatDefault);
    }

    private void updateFieldsFromEndian(AudioFormat formatDefault)
    {
        int nSignedPref = -1;
        boolean boolSigned = false;
        boolean boolUnsigned = false;
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof AudioFormat)
            {
                AudioFormat formatAudio = (AudioFormat)objectFormat;
                if(isFormatGoodForEncoding(formatAudio) && isFormatGoodForSampleRate(formatAudio) && isFormatGoodForBitSize(formatAudio) && isFormatGoodForChannels(formatAudio) && isFormatGoodForEndian(formatAudio))
                {
                    int nSigned = formatAudio.getSigned();
                    if(nSignedPref == -1)
                        nSignedPref = nSigned;
                    if(nSigned == -1)
                    {
                        boolSigned = true;
                        boolUnsigned = true;
                    } else
                    if(nSigned == 1)
                        boolSigned = true;
                    else
                        boolUnsigned = true;
                }
            }
        }

        boolEnableSigned = boolSigned && boolUnsigned;
        checkSigned.setEnabled(boolEnableSigned);
        if(formatDefault != null && isFormatGoodForEncoding(formatDefault) && isFormatGoodForSampleRate(formatDefault) && isFormatGoodForBitSize(formatDefault) && isFormatGoodForChannels(formatDefault) && isFormatGoodForEndian(formatDefault))
        {
            int nSigned = formatDefault.getSigned();
            if(nSigned == 1)
                checkSigned.setState(true);
            else
                checkSigned.setState(false);
        } else
        if(nSignedPref != -1)
        {
            if(nSignedPref == 1)
                checkSigned.setState(true);
            else
                checkSigned.setState(false);
        } else
        if(boolSigned)
            checkSigned.setState(true);
        else
            checkSigned.setState(false);
        updateFieldsFromSigned(formatDefault);
    }

    private void updateFieldsFromSigned(AudioFormat audioformat)
    {
    }

    private boolean isFormatGoodForEncoding(AudioFormat format)
    {
        boolean boolResult = false;
        String strEncoding = comboEncoding.getSelectedItem();
        if(strEncoding != null)
            boolResult = format.getEncoding().equalsIgnoreCase(strEncoding);
        return boolResult;
    }

    private boolean isFormatGoodForSampleRate(AudioFormat format)
    {
        boolean boolResult = false;
        String strSampleRate = comboSampleRate.getSelectedItem();
        if(strSampleRate != null)
        {
            double dSampleRate = Double.valueOf(strSampleRate).doubleValue();
            if(format.getSampleRate() == -1D)
                boolResult = true;
            else
            if(format.getSampleRate() == dSampleRate)
                boolResult = true;
        }
        return boolResult;
    }

    private boolean isFormatGoodForBitSize(AudioFormat format)
    {
        boolean boolResult = false;
        int nBits;
        if(checkBits8.getState())
            nBits = 8;
        else
        if(checkBits16.getState())
            nBits = 16;
        else
            nBits = -1;
        if(format.getSampleSizeInBits() == -1)
            boolResult = true;
        else
        if(nBits == -1)
            boolResult = true;
        else
        if(format.getSampleSizeInBits() == nBits)
            boolResult = true;
        else
        if(format.getSampleSizeInBits() < 8)
            boolResult = true;
        return boolResult;
    }

    private boolean isFormatGoodForChannels(AudioFormat format)
    {
        boolean boolResult = false;
        int nChannels;
        if(checkMono.getState())
            nChannels = 1;
        else
        if(checkStereo.getState())
            nChannels = 2;
        else
            nChannels = -1;
        if(format.getChannels() == -1)
            boolResult = true;
        else
        if(nChannels == -1)
            boolResult = true;
        else
        if(format.getChannels() == nChannels)
            boolResult = true;
        return boolResult;
    }

    private boolean isFormatGoodForEndian(AudioFormat format)
    {
        boolean boolResult = false;
        int nEndian;
        if(checkEndianBig.getState())
            nEndian = 1;
        else
        if(checkStereo.getState())
            nEndian = 0;
        else
            nEndian = -1;
        if(format.getEndian() == -1)
            boolResult = true;
        else
        if(nEndian == -1)
            boolResult = true;
        else
        if(format.getEndian() == nEndian)
            boolResult = true;
        return boolResult;
    }

    private boolean isFormatGoodForSigned(AudioFormat format)
    {
        boolean boolResult = false;
        int nSigned;
        if(checkSigned.getState())
            nSigned = 1;
        else
            nSigned = 0;
        if(format.getSigned() == -1)
            boolResult = true;
        else
        if(nSigned == -1)
            boolResult = true;
        else
        if(format.getSigned() == nSigned)
            boolResult = true;
        return boolResult;
    }

    private boolean isFormatSupported(AudioFormat format)
    {
        boolean boolSupported = false;
        if(format == null)
            return boolSupported;
        int nCount = vectorContSuppFormats.size();
        for(int i = 0; i < nCount && !boolSupported; i++)
        {
            AudioFormat formatAudio = (AudioFormat)vectorContSuppFormats.elementAt(i);
            if(formatAudio.matches(format))
                boolSupported = true;
        }

        return boolSupported;
    }

    public void itemStateChanged(ItemEvent event)
    {
        Object objectSource = event.getSource();
        if(objectSource == checkEnableTrack)
        {
            boolEnableTrackSaved = checkEnableTrack.getState();
            onEnableTrack(true);
        } else
        if(objectSource == comboEncoding)
            updateFieldsFromEncoding(formatOld);
        else
        if(objectSource == comboSampleRate)
            updateFieldsFromRate(formatOld);
        else
        if(objectSource == checkBits8 || objectSource == checkBits16)
            updateFieldsFromBits(formatOld);
        else
        if(objectSource == checkMono || objectSource == checkStereo)
            updateFieldsFromChannels(formatOld);
        else
        if(objectSource == checkEndianBig || objectSource == checkEndianLittle)
            updateFieldsFromEndian(formatOld);
        else
        if(objectSource == checkSigned)
            updateFieldsFromSigned(formatOld);
    }

    private void onEnableTrack(boolean notifyListener)
    {
        boolean boolEnable = checkEnableTrack.getState();
        enableControls(boolEnable && isEnabled());
        if(notifyListener && listenerEnableTrack != null)
        {
            ActionEvent event;
            if(boolEnable)
                event = new ActionEvent(this, 1001, "ACTION_AUDIO_TRACK_ENABLED");
            else
                event = new ActionEvent(this, 1001, "ACTION_AUDIO_TRACK_DISABLED");
            listenerEnableTrack.actionPerformed(event);
        }
    }

    private void enableControls(boolean boolEnable)
    {
        labelEncoding.setEnabled(boolEnable);
        comboEncoding.setEnabled(boolEnable);
        labelSampleRate.setEnabled(boolEnable);
        comboSampleRate.setEnabled(boolEnable);
        labelHz.setEnabled(boolEnable);
        labelBitsPerSample.setEnabled(boolEnable);
        checkBits8.setEnabled(boolEnable && boolEnable8);
        checkBits16.setEnabled(boolEnable && boolEnable16);
        labelChannels.setEnabled(boolEnable);
        checkMono.setEnabled(boolEnable && boolEnableMono);
        checkStereo.setEnabled(boolEnable && boolEnableStereo);
        labelEndian.setEnabled(boolEnable);
        checkEndianBig.setEnabled(boolEnable && boolEnableEndianBig);
        checkEndianLittle.setEnabled(boolEnable && boolEnableEndianLittle);
        checkSigned.setEnabled(boolEnable && boolEnableSigned);
    }

    public static final String ACTION_TRACK_ENABLED = "ACTION_AUDIO_TRACK_ENABLED";
    public static final String ACTION_TRACK_DISABLED = "ACTION_AUDIO_TRACK_DISABLED";
    private AudioFormat formatOld;
    private Format arrSupportedFormats[];
    private Vector vectorContSuppFormats;
    private boolean boolDisplayEnableTrack;
    private ActionListener listenerEnableTrack;
    private boolean boolEnableTrackSaved;
    private Checkbox checkEnableTrack;
    private Label labelEncoding;
    private Choice comboEncoding;
    private Label labelSampleRate;
    private Choice comboSampleRate;
    private Label labelHz;
    private Label labelBitsPerSample;
    private CheckboxGroup groupBitsPerSample;
    private Checkbox checkBits8;
    private Checkbox checkBits16;
    private Label labelChannels;
    private CheckboxGroup groupChannels;
    private Checkbox checkMono;
    private Checkbox checkStereo;
    private Label labelEndian;
    private CheckboxGroup groupEndian;
    private Checkbox checkEndianBig;
    private Checkbox checkEndianLittle;
    private Checkbox checkSigned;
    private boolean boolEnable8;
    private boolean boolEnable16;
    private boolean boolEnableMono;
    private boolean boolEnableStereo;
    private boolean boolEnableEndianBig;
    private boolean boolEnableEndianLittle;
    private boolean boolEnableSigned;
}
