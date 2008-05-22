/*  * Copyright (c) 1999-2001 Sun Microsystems, Inc. All Rights Reserved.  *  * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,  * modify and redistribute this software in source and binary code form,  * provided that i) this copyright notice and license appear on all copies of  * the software; and ii) Licensee does not utilize the software in a manner  * which is disparaging to Sun.  *  * This software is provided "AS IS," without a warranty of any kind. ALL  * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR  * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE  * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING  * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS  * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,  * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF  * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE  * POSSIBILITY OF SUCH DAMAGES.  *  * This software is not designed or intended for use in on-line control of  * aircraft, air traffic, aircraft navigation or aircraft communications; or in  * the design, construction, operation or maintenance of any nuclear  * facility. Licensee represents and warrants that it will not use or  * redistribute the Software for such purposes.  */

 
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
