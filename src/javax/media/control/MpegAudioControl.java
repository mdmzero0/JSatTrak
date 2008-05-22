// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MpegAudioControl.java

package javax.media.control;

import javax.media.Control;

public interface MpegAudioControl
    extends Control
{

    public abstract int getSupportedAudioLayers();

    public abstract int getSupportedSamplingRates();

    public abstract int getSupportedChannelLayouts();

    public abstract boolean isLowFrequencyChannelSupported();

    public abstract boolean isMultilingualModeSupported();

    public abstract int setAudioLayer(int i);

    public abstract int getAudioLayer();

    public abstract int setChannelLayout(int i);

    public abstract int getChannelLayout();

    public abstract boolean setLowFrequencyChannel(boolean flag);

    public abstract boolean getLowFrequencyChannel();

    public abstract boolean setMultilingualMode(boolean flag);

    public abstract boolean getMultilingualMode();

    public static final int LAYER_1 = 1;
    public static final int LAYER_2 = 2;
    public static final int LAYER_3 = 4;
    public static final int SAMPLING_RATE_16 = 1;
    public static final int SAMPLING_RATE_22_05 = 2;
    public static final int SAMPLING_RATE_24 = 4;
    public static final int SAMPLING_RATE_32 = 8;
    public static final int SAMPLING_RATE_44_1 = 16;
    public static final int SAMPLING_RATE_48 = 32;
    public static final int SINGLE_CHANNEL = 1;
    public static final int TWO_CHANNELS_STEREO = 2;
    public static final int TWO_CHANNELS_DUAL = 4;
    public static final int THREE_CHANNELS_2_1 = 4;
    public static final int THREE_CHANNELS_3_0 = 8;
    public static final int FOUR_CHANNELS_2_0_2_0 = 16;
    public static final int FOUR_CHANNELS_2_2 = 32;
    public static final int FOUR_CHANNELS_3_1 = 64;
    public static final int FIVE_CHANNELS_3_0_2_0 = 128;
    public static final int FIVE_CHANNELS_3_2 = 256;
}
