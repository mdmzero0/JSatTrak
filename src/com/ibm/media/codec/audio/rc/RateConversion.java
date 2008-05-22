// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RateConversion.java

package com.ibm.media.codec.audio.rc;


// Referenced classes of package com.ibm.media.codec.audio.rc:
//            RateConversionTables

public final class RateConversion
{

    public RateConversion()
    {
        useMuLaw = false;
        channels2To1 = false;
        channels1To2 = false;
        channels2To2 = false;
        index = new int[2];
        fractionDelta = 0.002272727F;
        precisionCountDelimiter = 1;
    }

    public int init(int maxInputBufferSize, int rateInput, int rateOutput, int inputChannels, int outputChannels, int pcmType, boolean signed, 
            boolean useMuLaw)
    {
        this.useMuLaw = useMuLaw;
        inputSampleSize = 2;
        if(0 != pcmType && 1 != pcmType && 2 != pcmType)
            return -4;
        if(signed)
        {
            bias = 0;
            signMask = -1;
        } else
        {
            bias = 32768;
            signMask = 65535;
        }
        if(2 == pcmType)
            inputSampleSize = 1;
        this.pcmType = pcmType;
        maxInputLength = maxInputBufferSize / (inputSampleSize * inputChannels);
        numberOfInputChannels = inputChannels;
        numberOfOutputChannels = outputChannels;
        if(numberOfInputChannels == 2 && numberOfOutputChannels == 1)
            channels2To1 = true;
        else
            channels2To1 = false;
        if(numberOfInputChannels == 1 && numberOfOutputChannels == 2)
            channels1To2 = true;
        else
            channels1To2 = false;
        if(numberOfInputChannels == 2 && numberOfOutputChannels == 2)
            channels2To2 = true;
        else
            channels2To2 = false;
        needInputCorrection = false;
        delay = 0;
        decimFlag = -1;
        if(rateInput == 44100 && rateOutput == 8000)
        {
            decimFlag = 1;
            rateIn = 11;
            rateOut = 2;
            needInputCorrection = true;
        } else
        if(rateInput == 22050 && rateOutput == 8000)
        {
            decimFlag = 2;
            rateIn = 11;
            rateOut = 4;
            needInputCorrection = true;
        } else
        if(rateInput == 11025 && rateOutput == 8000)
        {
            decimFlag = 3;
            rateIn = 11;
            rateOut = 8;
            needInputCorrection = true;
        } else
        if(rateInput == 48000 && rateOutput == 8000)
        {
            decimFlag = 4;
            rateIn = 6;
            rateOut = 1;
            needInputCorrection = false;
        } else
        if(rateInput == 32000 && rateOutput == 8000)
        {
            decimFlag = 5;
            rateIn = 4;
            rateOut = 1;
            needInputCorrection = false;
        } else
        if(rateInput == 16000 && rateOutput == 8000)
        {
            decimFlag = 6;
            rateIn = 2;
            rateOut = 1;
            needInputCorrection = false;
        } else
        if(rateInput == 11127 && rateOutput == 8000)
        {
            decimFlag = 3;
            rateIn = 11;
            rateOut = 8;
            needInputCorrection = true;
            fractionDelta = 0.01154545F;
            precisionCountDelimiter = 127;
        } else
        if(rateInput == 22254 && rateOutput == 8000)
        {
            decimFlag = 2;
            rateIn = 11;
            rateOut = 4;
            needInputCorrection = true;
            fractionDelta = 0.01154545F;
            precisionCountDelimiter = 127;
        } else
        if(rateInput == 22255 && rateOutput == 8000)
        {
            decimFlag = 2;
            rateIn = 11;
            rateOut = 4;
            needInputCorrection = true;
            fractionDelta = 0.01159091F;
            precisionCountDelimiter = 255;
        } else
        {
            close();
            return -2;
        }
        int filterLength;
        switch(decimFlag)
        {
        case 3: // '\003'
            filterHistoryLength = 25;
            delay = 9;
            filterLength = 200;
            break;

        case 2: // '\002'
            filterHistoryLength = 50;
            delay = 9;
            filterLength = 200;
            break;

        case 1: // '\001'
            filterHistoryLength = 100;
            delay = 9;
            filterLength = 200;
            break;

        case 4: // '\004'
            filterHistoryLength = 128;
            delay = 11;
            filterLength = 128;
            break;

        case 5: // '\005'
            filterHistoryLength = 128;
            delay = 16;
            filterLength = 128;
            break;

        case 6: // '\006'
            filterHistoryLength = 64;
            delay = 16;
            filterLength = 64;
            break;

        default:
            close();
            return -2;
        }
        poly = new float[filterLength];
        x1 = new float[maxInputLength + filterHistoryLength + 4 * rateIn];
        y1 = new float[((maxInputLength + filterHistoryLength + 4 * rateIn) / rateIn) * rateOut];
        if(channels2To2)
        {
            x2 = new float[maxInputLength + filterHistoryLength + 4 * rateIn];
            y2 = new float[((maxInputLength + filterHistoryLength + 4 * rateIn) / rateIn) * rateOut];
        }
        float gain = 1.0F;
        switch(decimFlag)
        {
        default:
            break;

        case 1: // '\001'
            gain = 2.0F;
            for(int i = 0; i < 2; i++)
            {
                for(int j = 0; j < 100; j++)
                    poly[i * 100 + j] = RateConversionTables.filter11[i + j * 2] * gain;

            }

            break;

        case 2: // '\002'
            gain = 4F;
            for(int i = 0; i < 4; i++)
            {
                for(int j = 0; j < 50; j++)
                    poly[i * 50 + j] = RateConversionTables.filter11[i + j * 4] * gain;

            }

            break;

        case 3: // '\003'
            gain = 8F;
            for(int i = 0; i < 8; i++)
            {
                for(int j = 0; j < 25; j++)
                    poly[i * 25 + j] = RateConversionTables.filter11[i + j * 8] * gain;

            }

            break;

        case 4: // '\004'
            for(int i = 0; i < 128; i++)
                poly[i] = RateConversionTables.filter6[i];

            break;

        case 5: // '\005'
            for(int i = 0; i < 128; i++)
                poly[i] = RateConversionTables.filter4[i];

            break;

        case 6: // '\006'
            for(int i = 0; i < 64; i++)
                poly[i] = RateConversionTables.filter2[i];

            break;
        }
        paddingLength = filterHistoryLength / 2;
        maxDrainedSamples = (int)((float)((paddingLength + 4 * rateIn * 2) * rateOut) / (float)rateIn);
        isRateConversionInited = true;
        fractionDelimiter = 1.0F + fractionDelta / 2.0F;
        reset();
        return -1;
    }

    public int reset()
    {
        if(!isRateConversionInited)
            return -3;
        inputRemainedSamples = 0;
        isDrained = false;
        frac = 1.0F;
        precisionCount = precisionCountDelimiter - 1;
        prev_fsample1 = 0.0F;
        prev_fsample2 = 0.0F;
        for(int i = 0; i < filterHistoryLength; i++)
            x1[i] = 0.0F;

        index[0] = 0;
        if(channels2To2)
        {
            for(int i = 0; i < filterHistoryLength; i++)
                x2[i] = 0.0F;

            index[1] = 0;
        }
        lastInputSample1 = 0.0F;
        lastInputSample2 = 0.0F;
        return -1;
    }

    public void close()
    {
        isRateConversionInited = false;
        x1 = null;
        x2 = null;
        y1 = null;
        y2 = null;
        poly = null;
    }

    public int getDelay()
    {
        if(!isRateConversionInited)
            return -3;
        int outputDelayLength = delay;
        if(!useMuLaw)
            outputDelayLength *= 2;
        return outputDelayLength;
    }

    public int process(byte inputData[], int inputDataOffset, int inputDataLength, byte output[], int outputDataOffset)
    {
        if(!isRateConversionInited)
            return -3;
        if(!isDrained && inputDataLength > maxInputLength * inputSampleSize * numberOfInputChannels)
            enlargeBufferAllocation(inputDataLength);
        if(0 == inputDataLength)
            return 0;
        int inputOffset = inputRemainedSamples + filterHistoryLength;
        inputDataLength = extractInput(inputData, inputDataOffset, inputDataLength, inputOffset);
        int inputLength = inputDataLength + inputRemainedSamples;
        int inputBlocks = inputLength / (4 * rateIn);
        inputRemainedSamples = inputLength - inputBlocks * (4 * rateIn);
        inputLength -= inputRemainedSamples;
        inputOffset = inputRemainedSamples + filterHistoryLength;
        if(inputLength == 0)
            return 0;
        int outputLength = (inputLength / rateIn) * rateOut;
        int numberOfChannels = 1;
        if(channels2To2)
            numberOfChannels = 2;
        for(int i = 0; i < numberOfChannels; i++)
        {
            float x[];
            float y[];
            if(i > 0)
            {
                x = x2;
                y = y2;
            } else
            {
                x = x1;
                y = y1;
            }
            switch(decimFlag)
            {
            case 3: // '\003'
                index[i] = downsampleMtoL(x, y, index[i], poly, 25, 8, 11, (outputLength >> 5) * 11, outputLength >> 2);
                break;

            case 2: // '\002'
                index[i] = downsampleMtoL(x, y, index[i], poly, 50, 4, 11, (outputLength >> 4) * 11, outputLength >> 2);
                break;

            case 1: // '\001'
                index[i] = downsampleMtoL(x, y, index[i], poly, 100, 2, 11, (outputLength >> 3) * 11, outputLength >> 2);
                break;

            case 4: // '\004'
                downsampleM(x, y, poly, 128, 6, outputLength);
                break;

            case 5: // '\005'
                downsampleM(x, y, poly, 128, 4, outputLength);
                break;

            case 6: // '\006'
                downsampleM(x, y, poly, 64, 2, outputLength);
                break;
            }
            for(int j = 0; j < inputOffset; j++)
                x[j] = x[j + inputLength];

        }

        if(!useMuLaw)
        {
            if(numberOfOutputChannels == 1)
            {
                Fl2Byte(y1, outputLength, output, outputDataOffset);
                return 2 * outputLength;
            }
            if(channels1To2)
                y2 = y1;
            Fl2ByteStereo(y1, y2, outputLength, output, outputDataOffset);
            return 4 * outputLength;
        } else
        {
            convertToMuLaw(y1, outputLength, output, outputDataOffset);
            return outputLength;
        }
    }

    public int drain(byte output[], int outputOffset)
    {
        if(!isRateConversionInited)
            return -3;
        int inputSamples = (paddingLength + 4 * rateIn) * numberOfInputChannels * inputSampleSize;
        isDrained = true;
        int actualOutputSamples = (int)((float)((paddingLength + inputRemainedSamples) * rateOut) / (float)rateIn);
        if(!useMuLaw)
            actualOutputSamples *= 2;
        int numberOfOutputSamples = process(null, 0, inputSamples, output, outputOffset);
        isDrained = false;
        if(actualOutputSamples < numberOfOutputSamples)
            numberOfOutputSamples = actualOutputSamples;
        return numberOfOutputSamples;
    }

    public int getMaxOutputLength()
    {
        if(!isRateConversionInited)
            return -3;
        int outputLength = ((maxInputLength + rateIn * 4) * rateOut) / rateIn;
        if(!useMuLaw)
            outputLength *= 2;
        return outputLength;
    }

    public int getMaxOutputLength(int inputLength)
    {
        if(!isRateConversionInited)
            return -3;
        int inputSamples = inputLength / (inputSampleSize * numberOfInputChannels);
        int outputLength = ((inputSamples + rateIn * 4) * rateOut) / rateIn;
        if(!useMuLaw)
            outputLength *= 2;
        outputLength *= numberOfOutputChannels;
        return outputLength;
    }

    public int getDrainMaxLength()
    {
        if(!isRateConversionInited)
            return -3;
        int drainMaxLength = maxDrainedSamples;
        if(!useMuLaw)
            drainMaxLength *= 2;
        return drainMaxLength;
    }

    private void convertToMuLaw(float inBuffer[], int Len, byte outBuffer[], int indexOut)
    {
        for(int i = 0; i < Len; i++)
        {
            float inSample = inBuffer[i];
            if(inSample < -32767F)
                inSample = -32767F;
            else
            if(inSample > 32767F)
                inSample = 32767F;
            int sample = (int)inSample;
            int signBit;
            if(sample >= 0)
            {
                signBit = 128;
            } else
            {
                sample = -sample;
                signBit = 0;
            }
            sample = 132 + sample >> 3;
            if(sample < 32)
                outBuffer[indexOut++] = (byte)(signBit | 0x70 | 31 - (sample >> 0));
            else
            if(sample < 64)
                outBuffer[indexOut++] = (byte)(signBit | 0x60 | 31 - (sample >> 1));
            else
            if(sample < 128)
                outBuffer[indexOut++] = (byte)(signBit | 0x50 | 31 - (sample >> 2));
            else
            if(sample < 256)
                outBuffer[indexOut++] = (byte)(signBit | 0x40 | 31 - (sample >> 3));
            else
            if(sample < 512)
                outBuffer[indexOut++] = (byte)(signBit | 0x30 | 31 - (sample >> 4));
            else
            if(sample < 1024)
                outBuffer[indexOut++] = (byte)(signBit | 0x20 | 31 - (sample >> 5));
            else
            if(sample < 2048)
                outBuffer[indexOut++] = (byte)(signBit | 0x10 | 31 - (sample >> 6));
            else
            if(sample < 4096)
                outBuffer[indexOut++] = (byte)(signBit | 0 | 31 - (sample >> 7));
            else
                outBuffer[indexOut++] = (byte)(signBit | 0 | 0);
        }

    }

    private final void Fl2Byte(float inBuffer[], int Len, byte outBuffer[], int indexOut)
    {
        int j = 0;
        for(int i = 0; i < Len; i++)
        {
            float sample = inBuffer[i];
            if(sample < -32767F)
                sample = -32767F;
            else
            if(sample > 32767F)
                sample = 32767F;
            int TempInt = (int)sample;
            outBuffer[indexOut + j] = (byte)(TempInt & 0xff);
            outBuffer[indexOut + j + 1] = (byte)(TempInt >> 8);
            j += 2;
        }

    }

    private final void Fl2ByteStereo(float inBuffer1[], float inBuffer2[], int Len, byte outBuffer[], int indexOut)
    {
        int j = 0;
        for(int i = 0; i < Len; i++)
        {
            float sample = inBuffer1[i];
            if(sample < -32767F)
                sample = -32767F;
            else
            if(sample > 32767F)
                sample = 32767F;
            int TempInt = (int)sample;
            outBuffer[indexOut + j] = (byte)(TempInt & 0xff);
            outBuffer[indexOut + j + 1] = (byte)(TempInt >> 8);
            sample = inBuffer2[i];
            if(sample < -32767F)
                sample = -32767F;
            else
            if(sample > 32767F)
                sample = 32767F;
            TempInt = (int)sample;
            outBuffer[indexOut + j + 2] = (byte)(TempInt & 0xff);
            outBuffer[indexOut + j + 3] = (byte)(TempInt >> 8);
            j += 4;
        }

    }

    private int extractInput(byte input[], int inputOffset, int inputLength, int internalBufferOffset)
    {
        int internalBufferIndex = internalBufferOffset;
        float fsample1 = 0.0F;
        float fsample2 = 0.0F;
        int sample1 = 0;
        int sample2 = 0;
        int inputSample = 0;
        int i;
        if(isDrained)
        {
            int length = inputLength / (numberOfInputChannels * inputSampleSize);
            for(i = 0; i < length; i++)
                x1[internalBufferIndex + i] = 0.0F;

            if(channels2To2)
                for(i = 0; i < length; i++)
                    x2[internalBufferIndex + i] = 0.0F;

            return length;
        }
        int lsbOffset;
        int msbOffset;
        if(pcmType == 1)
        {
            lsbOffset = -1;
            msbOffset = 1;
        } else
        {
            lsbOffset = 1;
            msbOffset = 0;
        }
        i = inputOffset + msbOffset;
        while(i < inputLength + inputOffset) 
        {
            if(1 == inputSampleSize)
            {
                sample1 = input[i++] << 8;
                if(numberOfInputChannels == 2)
                    sample2 = input[i++] << 8;
            } else
            {
                sample1 = (input[i] << 8) + (0xff & input[i + lsbOffset]);
                i += 2;
                if(numberOfInputChannels == 2)
                {
                    sample2 = (input[i] << 8) + (0xff & input[i + lsbOffset]);
                    i += 2;
                }
            }
            if(channels2To1)
                sample1 = (sample1 & signMask) + (sample2 & signMask) >> 1;
            fsample1 = (short)(sample1 + bias);
            if(channels2To2)
                fsample2 = (short)(sample2 + bias);
            if(channels1To2)
                fsample2 = fsample1;
            if(needInputCorrection)
            {
                if(frac > fractionDelimiter)
                {
                    precisionCount++;
                    if(precisionCount == precisionCountDelimiter)
                    {
                        precisionCount = 0;
                        frac = fractionDelta;
                    } else
                    {
                        frac--;
                    }
                    prev_fsample1 = fsample1;
                    prev_fsample2 = fsample2;
                    continue;
                }
                x1[internalBufferIndex] = prev_fsample1 * (1.0F - frac) + frac * fsample1;
                prev_fsample1 = fsample1;
                if(channels2To2)
                {
                    x2[internalBufferIndex] = prev_fsample2 * (1.0F - frac) + frac * fsample2;
                    prev_fsample2 = fsample2;
                }
                frac += fractionDelta;
            } else
            {
                x1[internalBufferIndex] = fsample1;
                if(channels2To2)
                    x2[internalBufferIndex] = fsample2;
            }
            internalBufferIndex++;
        }
        return internalBufferIndex - internalBufferOffset;
    }

    private float remove_dc(float input[], int inputOffset, float previous_sample, int i)
    {
        return 0.0F;
    }

    private int downsampleMtoL(float x[], float y[], int index, float poly[], int poly_length, int interpolation_factor, int decimation_factor, 
            int input_block_size, int output_block_size)
    {
        int m = 0;
        for(int n = 0; n < output_block_size; n++)
        {
            float sum1;
            float sum2;
            float sum3;
            float sum0 = sum1 = sum2 = sum3 = 0.0F;
            int offset = index * poly_length;
            for(int j = 0; j < poly_length; j++)
            {
                float polySample = poly[offset + j];
                sum0 += polySample * x[j + m];
                sum1 += polySample * x[j + m + 1 * input_block_size];
                sum2 += polySample * x[j + m + 2 * input_block_size];
                sum3 += polySample * x[j + m + 3 * input_block_size];
            }

            y[n + 1 * output_block_size] = sum1;
            y[n + 2 * output_block_size] = sum2;
            y[n + 3 * output_block_size] = sum3;
            y[n] = sum0;
            while(index < decimation_factor) 
            {
                index += interpolation_factor;
                m++;
            }
            index -= decimation_factor;
        }

        return index;
    }

    void downsampleM(float x[], float y[], float filter[], int filter_length, int decimation_factor, int output_length)
    {
        int filt_length = filter_length / 2;
        int offset_inc = decimation_factor * 4;
        int sym_offset = filter_length - 1;
        int offset1 = 0;
        for(int n = 0; n < output_length;)
        {
            float sum1;
            float sum2;
            float sum3;
            float sum0 = sum1 = sum2 = sum3 = 0.0F;
            int offset2 = offset1 + decimation_factor;
            int offset3 = offset2 + decimation_factor;
            int offset4 = offset3 + decimation_factor;
            for(int i = 0; i < filt_length; i++)
            {
                float filterSample = filter[i];
                sum0 += filterSample * (x[offset1 + i] + x[(offset1 + sym_offset) - i]);
                sum1 += filterSample * (x[offset2 + i] + x[(offset2 + sym_offset) - i]);
                sum2 += filterSample * (x[offset3 + i] + x[(offset3 + sym_offset) - i]);
                sum3 += filterSample * (x[offset4 + i] + x[(offset4 + sym_offset) - i]);
            }

            y[n++] = sum0;
            y[n++] = sum1;
            y[n++] = sum2;
            y[n++] = sum3;
            offset1 += offset_inc;
        }

    }

    private void enlargeBufferAllocation(int length)
    {
        maxInputLength = length / (inputSampleSize * numberOfInputChannels);
        float inputBuffer[] = new float[maxInputLength + filterHistoryLength + 4 * rateIn];
        float outputBuffer[] = new float[((maxInputLength + filterHistoryLength + 4 * rateIn) / rateIn) * rateOut];
        for(int i = 0; i < x1.length; i++)
            inputBuffer[i] = x1[i];

        for(int i = 0; i < y1.length; i++)
            outputBuffer[i] = y1[i];

        x1 = inputBuffer;
        y1 = outputBuffer;
        if(channels2To2)
        {
            inputBuffer = new float[maxInputLength + filterHistoryLength + 4 * rateIn];
            outputBuffer = new float[((maxInputLength + filterHistoryLength + 4 * rateIn) / rateIn) * rateOut];
            for(int i = 0; i < x2.length; i++)
                inputBuffer[i] = x2[i];

            for(int i = 0; i < y2.length; i++)
                outputBuffer[i] = y2[i];

            x2 = inputBuffer;
            y2 = outputBuffer;
        }
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1998.";
    public static final int RATE_CONVERSION_OK = -1;
    public static final int RATE_CONVERSION_NOT_SUPPORTED = -2;
    public static final int RATE_CONVERSION_NOT_INITIALIZED = -3;
    public static final int RATE_CONVERSION_ILLEGAL_PARAMETER = -4;
    public static final int RATE_CONVERSION_RECOMMENDED_INPUT_SIZE = 1056;
    public static final int RATE_CONVERSION_MAX_SUPPORTED_CHANNELS = 2;
    public static final int RATE_CONVERSION_MAX_OUTPUT_FACTOR = 5;
    public static final int RATE_CONVERSION_BIG_ENDIAN_FORMAT = 0;
    public static final int RATE_CONVERSION_LITTLE_ENDIAN_FORMAT = 1;
    public static final int RATE_CONVERSION_BYTE_FORMAT = 2;
    private static final boolean USE_REMOVE_DC = false;
    private boolean useMuLaw;
    private static final int MAX_RATE_IN = 11;
    private static final int UNROLLING_ORDER = 4;
    private static final int CORRECTION_FRAME_SIZE = 441;
    private static final int CONV_ERROR = -1;
    private static final int CONV_11to2 = 1;
    private static final int CONV_11to4 = 2;
    private static final int CONV_11to8 = 3;
    private static final int CONV_6to1 = 4;
    private static final int CONV_4to1 = 5;
    private static final int CONV_2to1 = 6;
    private static final int MAX_MEM_SIZE = 792;
    private static final float DCFACT = 0.9921875F;
    private static final float SHRT_MIN = -32767F;
    private static final float SHRT_MAX = 32767F;
    private static final float FRACTION_DELTA = 0.002272727F;
    private static final float FRACTION_DELIMITER = 1.001136F;
    private int bias;
    private int signMask;
    private int filterHistoryLength;
    private int decimFlag;
    private int numberOfInputChannels;
    private int numberOfOutputChannels;
    private boolean channels2To1;
    private boolean channels1To2;
    private boolean channels2To2;
    private int rateIn;
    private int rateOut;
    private int index[];
    private int inputRemainedSamples;
    private int maxInputLength;
    private int paddingLength;
    private int maxDrainedSamples;
    private int pcmType;
    private int inputSampleSize;
    private float poly[];
    private float x1[];
    private float x2[];
    private float y1[];
    private float y2[];
    private boolean needInputCorrection;
    private boolean isDrained;
    private boolean isRateConversionInited;
    private int delay;
    private float lastInputSample1;
    private float lastInputSample2;
    private float frac;
    private float prev_fsample1;
    private float prev_fsample2;
    private float fractionDelta;
    private float fractionDelimiter;
    private int precisionCountDelimiter;
    private int precisionCount;
}
