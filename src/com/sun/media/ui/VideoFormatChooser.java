// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   VideoFormatChooser.java

package com.sun.media.ui;

import com.sun.media.util.JMFI18N;
import java.awt.*;
import java.awt.event.*;
import java.util.EventObject;
import java.util.Vector;
import javax.media.Format;
import javax.media.format.*;

// Referenced classes of package com.sun.media.ui:
//            VideoSizeControl, VideoSize

public class VideoFormatChooser extends Panel
    implements ItemListener, ActionListener
{

    public VideoFormatChooser(Format arrFormats[], VideoFormat formatDefault, float frameRates[])
    {
        this(arrFormats, formatDefault, false, null, frameRates);
    }

    public VideoFormatChooser(Format arrFormats[], VideoFormat formatDefault)
    {
        this(arrFormats, formatDefault, false, null, ((float []) (null)));
    }

    public VideoFormatChooser(Format arrFormats[], VideoFormat formatDefault, boolean boolDisplayEnableTrack, ActionListener listenerEnableTrack)
    {
        this(arrFormats, formatDefault, boolDisplayEnableTrack, listenerEnableTrack, ((float []) (null)));
    }

    public VideoFormatChooser(Format arrFormats[], VideoFormat formatDefault, boolean boolDisplayEnableTrack, ActionListener listenerEnableTrack, boolean capture)
    {
        this(arrFormats, formatDefault, boolDisplayEnableTrack, listenerEnableTrack, capture ? standardCaptureRates : null);
    }

    public VideoFormatChooser(Format arrFormats[], VideoFormat formatDefault, boolean boolDisplayEnableTrack, ActionListener listenerEnableTrack, float frameRates[])
    {
        arrSupportedFormats = null;
        customFrameRates = null;
        vectorContSuppFormats = new Vector();
        boolEnableTrackSaved = true;
        nWidthLabel = 0;
        nWidthData = 0;
        arrSupportedFormats = arrFormats;
        this.boolDisplayEnableTrack = boolDisplayEnableTrack;
        this.listenerEnableTrack = listenerEnableTrack;
        customFrameRates = frameRates;
        int nCount = arrSupportedFormats.length;
        for(int i = 0; i < nCount; i++)
            if(arrSupportedFormats[i] instanceof VideoFormat)
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
        String strYuvType = null;
        VideoFormat formatVideo = null;
        String strEncoding = comboEncoding.getSelectedItem();
        int nSize = vectorContSuppFormats.size();
        int i;
        for(i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(!(objectFormat instanceof VideoFormat))
                continue;
            formatVideo = (VideoFormat)objectFormat;
            if(!isFormatGoodForEncoding(formatVideo) || !isFormatGoodForVideoSize(formatVideo) || !isFormatGoodForFrameRate(formatVideo))
                continue;
            if(strEncoding.equalsIgnoreCase("rgb") && (formatVideo instanceof RGBFormat))
            {
                RGBFormat formatRGB = (RGBFormat)formatVideo;
                Integer integerBitsPerPixel = new Integer(formatRGB.getBitsPerPixel());
                String strBitsPerPixel = integerBitsPerPixel.toString();
                if(comboExtra.getSelectedItem().equals(strBitsPerPixel))
                    break;
                continue;
            }
            if(!strEncoding.equalsIgnoreCase("yuv") || !(formatVideo instanceof YUVFormat))
                break;
            YUVFormat formatYUV = (YUVFormat)formatVideo;
            int nYuvType = formatYUV.getYuvType();
            strYuvType = getYuvType(nYuvType);
            if(strYuvType != null && comboExtra.getSelectedItem().equals(strYuvType))
                break;
        }

        if(i >= nSize)
            return null;
        if(formatVideo.getSize() == null)
        {
            VideoFormat formatVideoNew = new VideoFormat(null, controlSize.getVideoSize(), -1, null, -1F);
            formatVideo = (VideoFormat)formatVideoNew.intersects(formatVideo);
        }
        if(customFrameRates != null && formatVideo != null)
        {
            VideoFormat formatVideoNew = new VideoFormat(null, null, -1, null, getFrameRate());
            formatVideo = (VideoFormat)formatVideoNew.intersects(formatVideo);
        }
        return formatVideo;
    }

    public float getFrameRate()
    {
        String selection = comboFrameRate.getSelectedItem();
        if(selection != null)
        {
            if(selection.equals(DEFAULT_STRING))
                return -1F;
            try
            {
                float fr = Float.valueOf(selection).floatValue();
                return fr;
            }
            catch(NumberFormatException nfe) { }
        }
        return -1F;
    }

    public void setCurrentFormat(VideoFormat formatDefault)
    {
        if(isFormatSupported(formatDefault))
            formatOld = formatDefault;
        updateFields(formatOld);
    }

    public void setFrameRate(float frameRate)
    {
        for(int i = 0; i < comboFrameRate.getItemCount(); i++)
        {
            float value = Float.valueOf(comboFrameRate.getItem(i)).floatValue();
            if((double)Math.abs(frameRate - value) < 0.5D)
            {
                comboFrameRate.select(i);
                return;
            }
        }

    }

    public void setSupportedFormats(Format arrFormats[], VideoFormat formatDefault)
    {
        arrSupportedFormats = arrFormats;
        vectorContSuppFormats.removeAllElements();
        int nCount = arrSupportedFormats.length;
        for(int i = 0; i < nCount; i++)
            if(arrSupportedFormats[i] instanceof VideoFormat)
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

    public Dimension getPreferredSize()
    {
        Dimension dim = new Dimension();
        Dimension dimControl;
        if(boolDisplayEnableTrack)
        {
            dimControl = checkEnableTrack.getPreferredSize();
            dim.width = Math.max(dim.width, dimControl.width);
            dim.height += dimControl.height + 6;
        }
        Dimension dimLabel = labelEncoding.getPreferredSize();
        nWidthLabel = Math.max(nWidthLabel, dimLabel.width);
        dimControl = comboEncoding.getPreferredSize();
        nWidthData = Math.max(nWidthData, dimControl.width);
        dim.height += Math.max(dimLabel.height, dimControl.height) + 6;
        dimLabel = labelSize.getPreferredSize();
        nWidthLabel = Math.max(nWidthLabel, dimLabel.width);
        dimControl = controlSize.getPreferredSize();
        nWidthData = Math.max(nWidthData, dimControl.width);
        dim.height += Math.max(dimLabel.height, dimControl.height) + 6;
        dimLabel = labelFrameRate.getPreferredSize();
        nWidthLabel = Math.max(nWidthLabel, dimLabel.width);
        dimControl = comboFrameRate.getPreferredSize();
        nWidthData = Math.max(nWidthData, dimControl.width);
        dim.height += Math.max(dimLabel.height, dimControl.height) + 6;
        dimLabel = labelExtra.getPreferredSize();
        nWidthLabel = Math.max(nWidthLabel, dimLabel.width);
        dimControl = comboExtra.getPreferredSize();
        nWidthData = Math.max(nWidthData, dimControl.width);
        dim.height += Math.max(dimLabel.height, dimControl.height);
        dim.width = Math.max(dim.width, nWidthLabel + 12 + nWidthData);
        return dim;
    }

    public void doLayout()
    {
        getPreferredSize();
        int nOffsetY = 0;
        int nLabelOffsetX = 0;
        int nDataOffsetX = nWidthLabel + 12;
        Dimension dimThis = getSize();
        Dimension dimControl;
        if(boolDisplayEnableTrack)
        {
            dimControl = checkEnableTrack.getPreferredSize();
            checkEnableTrack.setBounds(nLabelOffsetX, nOffsetY, dimControl.width, dimControl.height);
            nOffsetY += dimControl.height + 6;
        }
        Dimension dimLabel = labelEncoding.getPreferredSize();
        dimControl = comboEncoding.getPreferredSize();
        labelEncoding.setBounds(nLabelOffsetX, nOffsetY, nWidthLabel, dimLabel.height);
        comboEncoding.setBounds(nDataOffsetX, nOffsetY, dimThis.width - nDataOffsetX, dimControl.height);
        nOffsetY += Math.max(dimLabel.height, dimControl.height) + 6;
        dimLabel = labelSize.getPreferredSize();
        dimControl = controlSize.getPreferredSize();
        labelSize.setBounds(nLabelOffsetX, nOffsetY, nWidthLabel, dimLabel.height);
        controlSize.setBounds(nDataOffsetX, nOffsetY, dimThis.width - nDataOffsetX, dimControl.height);
        nOffsetY += Math.max(dimLabel.height, dimControl.height) + 6;
        dimLabel = labelFrameRate.getPreferredSize();
        dimControl = comboFrameRate.getPreferredSize();
        labelFrameRate.setBounds(nLabelOffsetX, nOffsetY, nWidthLabel, dimLabel.height);
        comboFrameRate.setBounds(nDataOffsetX, nOffsetY, dimThis.width - nDataOffsetX, dimControl.height);
        nOffsetY += Math.max(dimLabel.height, dimControl.height) + 6;
        dimLabel = labelExtra.getPreferredSize();
        dimControl = comboExtra.getPreferredSize();
        labelExtra.setBounds(nLabelOffsetX, nOffsetY, nWidthLabel, dimLabel.height);
        comboExtra.setBounds(nDataOffsetX, nOffsetY, dimThis.width - nDataOffsetX, dimControl.height);
        nOffsetY += Math.max(dimLabel.height, dimControl.height) + 6;
    }

    private void init()
        throws Exception
    {
        setLayout(null);
        checkEnableTrack = new Checkbox(JMFI18N.getResource("formatchooser.enabletrack"), true);
        checkEnableTrack.addItemListener(this);
        if(boolDisplayEnableTrack)
            add(checkEnableTrack);
        labelEncoding = new Label(JMFI18N.getResource("formatchooser.encoding"), 2);
        add(labelEncoding);
        comboEncoding = new Choice();
        comboEncoding.addItemListener(this);
        add(comboEncoding);
        labelSize = new Label(JMFI18N.getResource("formatchooser.videosize"), 2);
        add(labelSize);
        if(formatOld == null)
        {
            controlSize = new VideoSizeControl();
        } else
        {
            VideoSize sizeVideo = new VideoSize(formatOld.getSize());
            controlSize = new VideoSizeControl(sizeVideo);
        }
        controlSize.addActionListener(this);
        add(controlSize);
        labelFrameRate = new Label(JMFI18N.getResource("formatchooser.framerate"), 2);
        add(labelFrameRate);
        comboFrameRate = new Choice();
        comboFrameRate.addItemListener(this);
        add(comboFrameRate);
        labelExtra = new Label("Extra:", 2);
        labelExtra.setVisible(false);
        add(labelExtra);
        comboExtra = new Choice();
        comboExtra.setVisible(false);
        add(comboExtra);
        updateFields(formatOld);
    }

    private void updateFields(VideoFormat formatDefault)
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
            if(objectFormat instanceof VideoFormat)
            {
                VideoFormat formatVideo = (VideoFormat)objectFormat;
                String strEncoding = formatVideo.getEncoding().toUpperCase();
                if(strEncodingPref == null)
                    strEncodingPref = strEncoding;
                if(!vectorEncoding.contains(strEncoding))
                {
                    comboEncoding.addItem(strEncoding);
                    vectorEncoding.addElement(strEncoding);
                }
            }
        }

        if(formatDefault != null)
        {
            String strEncoding = formatDefault.getEncoding().toUpperCase();
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

    private void updateFieldsFromEncoding(VideoFormat formatDefault)
    {
        VideoSize sizeVideoPref = null;
        boolean boolVideoSizePref = false;
        boolean boolEnable = controlSize.isEnabled();
        controlSize.setEnabled(false);
        controlSize.removeAll();
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof VideoFormat)
            {
                VideoFormat formatVideo = (VideoFormat)objectFormat;
                if(isFormatGoodForEncoding(formatVideo))
                {
                    Dimension formatVideoSize = formatVideo.getSize();
                    VideoSize sizeVideo;
                    if(formatVideoSize == null)
                        sizeVideo = null;
                    else
                        sizeVideo = new VideoSize(formatVideoSize);
                    if(!boolVideoSizePref)
                    {
                        boolVideoSizePref = true;
                        sizeVideoPref = sizeVideo;
                    }
                    controlSize.addItem(sizeVideo);
                }
            }
        }

        if(formatDefault != null && isFormatGoodForEncoding(formatDefault))
        {
            Dimension formatVideoSize = formatDefault.getSize();
            VideoSize sizeVideo;
            if(formatVideoSize == null)
                sizeVideo = null;
            else
                sizeVideo = new VideoSize(formatVideoSize);
            controlSize.select(sizeVideo);
        } else
        if(boolVideoSizePref)
            controlSize.select(sizeVideoPref);
        else
        if(controlSize.getItemCount() > 0)
            controlSize.select(0);
        updateFieldsFromSize(formatDefault);
        controlSize.setEnabled(boolEnable);
    }

    private void updateFieldsFromSize(VideoFormat formatDefault)
    {
        Float floatFrameRatePref = null;
        Vector vectorRates = new Vector();
        boolean boolEnable = comboFrameRate.isEnabled();
        comboFrameRate.setEnabled(false);
        if(customFrameRates == null)
            comboFrameRate.removeAll();
        else
        if(comboFrameRate.getItemCount() < 1)
        {
            for(int i = 0; i < customFrameRates.length; i++)
                comboFrameRate.addItem(Float.toString(customFrameRates[i]));

        }
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof VideoFormat)
            {
                VideoFormat formatVideo = (VideoFormat)objectFormat;
                if(isFormatGoodForEncoding(formatVideo) && isFormatGoodForVideoSize(formatVideo) && customFrameRates == null)
                {
                    Float floatFrameRate = new Float(formatVideo.getFrameRate());
                    if(floatFrameRatePref == null)
                        floatFrameRatePref = floatFrameRate;
                    if(!vectorRates.contains(floatFrameRate))
                    {
                        if(floatFrameRate.floatValue() == -1F)
                            comboFrameRate.addItem(DEFAULT_STRING);
                        else
                            comboFrameRate.addItem(floatFrameRate.toString());
                        vectorRates.addElement(floatFrameRate);
                    }
                }
            }
        }

        if(formatDefault != null && customFrameRates == null && isFormatGoodForEncoding(formatDefault) && isFormatGoodForVideoSize(formatDefault))
        {
            Float floatFrameRate = new Float(formatDefault.getFrameRate());
            if(floatFrameRate.floatValue() == -1F)
                comboFrameRate.select(DEFAULT_STRING);
            else
                comboFrameRate.select(floatFrameRate.toString());
        } else
        if(floatFrameRatePref != null)
        {
            if(floatFrameRatePref.floatValue() == -1F)
                comboFrameRate.select(DEFAULT_STRING);
            else
                comboFrameRate.select(floatFrameRatePref.toString());
        } else
        if(comboFrameRate.getItemCount() > 0)
            comboFrameRate.select(0);
        updateFieldsFromRate(formatDefault);
        comboFrameRate.setEnabled(boolEnable);
    }

    private void updateFieldsFromRate(VideoFormat formatDefault)
    {
        String strYuvType = null;
        Vector vectorExtra = new Vector();
        boolean boolRGB = false;
        boolean boolYUV = false;
        String strEncoding = comboEncoding.getSelectedItem();
        if(strEncoding == null)
            return;
        if(strEncoding.equalsIgnoreCase("rgb"))
        {
            labelExtra.setText(JMFI18N.getResource("formatchooser.bitsperpixel"));
            labelExtra.setVisible(true);
            comboExtra.setVisible(true);
            boolRGB = true;
        } else
        if(strEncoding.equalsIgnoreCase("yuv"))
        {
            labelExtra.setText(JMFI18N.getResource("formatchooser.yuvtype"));
            labelExtra.setVisible(true);
            comboExtra.setVisible(true);
            boolYUV = true;
        } else
        {
            labelExtra.setVisible(false);
            comboExtra.setVisible(false);
            return;
        }
        boolean boolEnable = comboExtra.isEnabled();
        comboExtra.setEnabled(false);
        comboExtra.removeAll();
        int nSize = vectorContSuppFormats.size();
        for(int i = 0; i < nSize; i++)
        {
            Object objectFormat = vectorContSuppFormats.elementAt(i);
            if(objectFormat instanceof VideoFormat)
            {
                VideoFormat formatVideo = (VideoFormat)objectFormat;
                if(isFormatGoodForEncoding(formatVideo) && isFormatGoodForVideoSize(formatVideo) && isFormatGoodForFrameRate(formatVideo))
                    if(boolRGB && (formatVideo instanceof RGBFormat))
                    {
                        RGBFormat formatRGB = (RGBFormat)formatVideo;
                        Integer integerBitsPerPixel = new Integer(formatRGB.getBitsPerPixel());
                        if(!vectorExtra.contains(integerBitsPerPixel))
                        {
                            comboExtra.addItem(integerBitsPerPixel.toString());
                            vectorExtra.addElement(integerBitsPerPixel);
                        }
                    } else
                    if(boolYUV && (formatVideo instanceof YUVFormat))
                    {
                        YUVFormat formatYUV = (YUVFormat)formatVideo;
                        int nYuvType = formatYUV.getYuvType();
                        strYuvType = getYuvType(nYuvType);
                        if(strYuvType != null && !vectorExtra.contains(strYuvType))
                        {
                            comboExtra.addItem(strYuvType);
                            vectorExtra.addElement(strYuvType);
                        }
                    }
            }
        }

        if(formatDefault != null && isFormatGoodForEncoding(formatDefault) && isFormatGoodForVideoSize(formatDefault) && isFormatGoodForFrameRate(formatDefault))
        {
            if(boolRGB && (formatDefault instanceof RGBFormat))
            {
                RGBFormat formatRGB = (RGBFormat)formatDefault;
                Integer integerBitsPerPixel = new Integer(formatRGB.getBitsPerPixel());
                comboExtra.select(integerBitsPerPixel.toString());
            } else
            if(boolYUV && (formatDefault instanceof YUVFormat))
            {
                YUVFormat formatYUV = (YUVFormat)formatDefault;
                int nYuvType = formatYUV.getYuvType();
                strYuvType = getYuvType(nYuvType);
                if(strYuvType != null)
                    comboExtra.select(strYuvType);
            } else
            if(comboExtra.getItemCount() > 0)
                comboExtra.select(0);
        } else
        if(comboExtra.getItemCount() > 0)
            comboExtra.select(0);
        comboExtra.setEnabled(boolEnable);
    }

    private boolean isFormatGoodForEncoding(VideoFormat format)
    {
        boolean boolResult = false;
        String strEncoding = comboEncoding.getSelectedItem();
        if(strEncoding != null)
            boolResult = format.getEncoding().equalsIgnoreCase(strEncoding);
        return boolResult;
    }

    private boolean isFormatGoodForVideoSize(VideoFormat format)
    {
        boolean boolResult = false;
        VideoSize sizeVideo = controlSize.getVideoSize();
        Dimension formatVideoSize = format.getSize();
        if(formatVideoSize == null)
            boolResult = true;
        else
            boolResult = sizeVideo.equals(formatVideoSize);
        return boolResult;
    }

    private boolean isFormatGoodForFrameRate(VideoFormat format)
    {
        boolean boolResult = false;
        if(customFrameRates != null)
            return true;
        String strFrameRate = comboFrameRate.getSelectedItem();
        if(strFrameRate.equals(DEFAULT_STRING))
            return true;
        float fFrameRate2 = format.getFrameRate();
        if(fFrameRate2 == -1F)
            return true;
        if(strFrameRate != null)
        {
            float fFrameRate1 = Float.valueOf(strFrameRate).floatValue();
            boolResult = fFrameRate1 == fFrameRate2;
        }
        return boolResult;
    }

    private boolean isFormatSupported(VideoFormat format)
    {
        boolean boolSupported = false;
        if(format == null)
            return boolSupported;
        int nCount = vectorContSuppFormats.size();
        for(int i = 0; i < nCount && !boolSupported; i++)
        {
            VideoFormat formatVideo = (VideoFormat)vectorContSuppFormats.elementAt(i);
            if(formatVideo.matches(format))
                boolSupported = true;
        }

        return boolSupported;
    }

    public void actionPerformed(ActionEvent event)
    {
        if(event.getActionCommand().equals("Size Changed"))
            updateFieldsFromSize(formatOld);
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
        if(objectSource == controlSize)
            updateFieldsFromSize(formatOld);
        else
        if(objectSource == comboFrameRate)
            updateFieldsFromRate(formatOld);
    }

    private void onEnableTrack(boolean notifyListener)
    {
        boolean boolEnable = checkEnableTrack.getState();
        enableControls(boolEnable && isEnabled());
        if(notifyListener && listenerEnableTrack != null)
        {
            ActionEvent event;
            if(boolEnable)
                event = new ActionEvent(this, 1001, "ACTION_VIDEO_TRACK_ENABLED");
            else
                event = new ActionEvent(this, 1001, "ACTION_VIDEO_TRACK_DISABLED");
            listenerEnableTrack.actionPerformed(event);
        }
    }

    private void enableControls(boolean boolEnable)
    {
        labelEncoding.setEnabled(boolEnable);
        comboEncoding.setEnabled(boolEnable);
        labelSize.setEnabled(boolEnable);
        controlSize.setEnabled(boolEnable);
        labelFrameRate.setEnabled(boolEnable);
        comboFrameRate.setEnabled(boolEnable);
        labelExtra.setEnabled(boolEnable);
        comboExtra.setEnabled(boolEnable);
    }

    private String getYuvType(int nType)
    {
        String strType = null;
        if((nType & 2) == 2)
            strType = JMFI18N.getResource("formatchooser.yuv.4:2:0");
        else
        if((nType & 4) == 4)
            strType = JMFI18N.getResource("formatchooser.yuv.4:2:2");
        else
        if((nType & 0x20) == 32)
            strType = JMFI18N.getResource("formatchooser.yuv.YUYV");
        else
        if((nType & 8) == 8)
            strType = JMFI18N.getResource("formatchooser.yuv.1:1:1");
        else
        if((nType & 1) == 1)
            strType = JMFI18N.getResource("formatchooser.yuv.4:1:1");
        else
        if((nType & 0x10) == 16)
            strType = JMFI18N.getResource("formatchooser.yuv.YVU9");
        else
            strType = null;
        return strType;
    }

    public static final String ACTION_TRACK_ENABLED = "ACTION_VIDEO_TRACK_ENABLED";
    public static final String ACTION_TRACK_DISABLED = "ACTION_VIDEO_TRACK_DISABLED";
    private VideoFormat formatOld;
    private Format arrSupportedFormats[];
    private float customFrameRates[];
    private Vector vectorContSuppFormats;
    private boolean boolDisplayEnableTrack;
    private ActionListener listenerEnableTrack;
    private boolean boolEnableTrackSaved;
    private Checkbox checkEnableTrack;
    private Label labelEncoding;
    private Choice comboEncoding;
    private Label labelSize;
    private VideoSizeControl controlSize;
    private Label labelFrameRate;
    private Choice comboFrameRate;
    private Label labelExtra;
    private Choice comboExtra;
    private int nWidthLabel;
    private int nWidthData;
    private static final int MARGINH = 12;
    private static final int MARGINV = 6;
    private static final float standardCaptureRates[] = {
        15F, 1.0F, 2.0F, 5F, 7.5F, 10F, 12.5F, 20F, 24F, 25F, 
        30F
    };
    private static final String DEFAULT_STRING = JMFI18N.getResource("formatchooser.default");

}
