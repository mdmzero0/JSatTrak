// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GsmEncoder.java

package com.ibm.media.codec.audio.gsm;


public class GsmEncoder
{

    public boolean isVADsupported()
    {
        return vadSupportFlag;
    }

    public void setVAD(boolean newVAD)
    {
        data_vad_mode = vadSupportFlag ? newVAD : false;
    }

    public boolean getVAD()
    {
        return data_vad_mode;
    }

    public void setSIDupdateRate(int newSIDupdateRate)
    {
        sidUpdateRate = newSIDupdateRate;
    }

    public void setComplexity(boolean newcomplexity)
    {
        data_complexity_mode = newcomplexity;
    }

    public boolean getComplexity()
    {
        return data_complexity_mode;
    }

    public int getFrameType()
    {
        return frameType;
    }

    public void gsm_encoder_close()
    {
    }

    public void gsm_encoder_reset()
    {
        data_buffer = 0.0F;
        data_scaledSample = 0.0F;
        for(int i = 0; i < data_first_res.length; i++)
            data_first_res[i] = 0.0F;

        for(int i = 0; i < data_residual.length; i++)
            data_residual[i] = 0.0F;

        for(int i = 0; i < data_u.length; i++)
            data_u[i] = 0.0F;

        for(int i = 0; i < data_prevLARpp.length; i++)
            data_prevLARpp[i] = 0.0F;

        for(int i = 0; i < data_wt.length; i++)
            data_wt[i] = 0.0F;

        data_complexity_mode = true;
        data_vad_mode = false;
        data_Parameters[76] = 0;
        vadSupportFlag = false;
    }

    public GsmEncoder()
    {
        sidUpdateRate = 0;
        frameType = 0;
        aux_correlations = new float[121];
        data_Nc = new int[4];
        data_LAR = new float[8];
        data_currentxmax = new float[4];
        data_Parameters = new int[77];
        data_wt = new float[50];
        data_u = new float[9];
        data_prevLARpp = new float[9];
        data_residual = new float[280];
        data_input = new float[160];
        data_first_res = new float[160];
        data_acf = new float[9];
        data_r = new float[9];
        data_LARpp = new float[9];
        aux_decimated_subsegment = new float[20];
        aux_decimated_q_frstbase = new float[60];
        aux_decimated_correlations = new float[41];
        aux_KK = new float[9];
        aux_P = new float[9];
        encode_outsegment = new float[40];
        encode_pitchArray = new int[1];
        aux_rp = new float[9];
    }

    public void gsm_encode_frame(byte input_samples[], int input_offset, byte output_bits[], int output_offset)
    {
        float outsegment[] = encode_outsegment;
        float input[] = data_input;
        float first_res[] = data_first_res;
        float acf[] = data_acf;
        float r[] = data_r;
        float LARpp[] = data_LARpp;
        int pitchArray[] = encode_pitchArray;
        for(int i = 0; i < 160; i++)
        {
            int temp = input_samples[2 * i + input_offset] & 0xff | input_samples[2 * i + input_offset + 1] << 8;
            input[i] = (float)temp * 3.051758E-005F;
        }

        PreProcessing(input, first_res);
        Autocorrelations(first_res, acf);
        schurRecursion(acf, r);
        rToLAR(r, LARpp);
        latticeFilter(LARpp, first_res);
        data_residual_Index = 120;
        for(int blocknumber = 0; blocknumber < 4; blocknumber++)
        {
            data_blockNumber = blocknumber;
            float max_corr;
            if(data_complexity_mode)
                max_corr = calculatePitch(first_res, blocknumber * 40, pitchArray);
            else
                max_corr = calculatePitch_Light(first_res, blocknumber * 40, pitchArray);
            int pitch = pitchArray[0];
            float gain = calculatePitchGain(max_corr, pitch);
            int offset = analizeSecondResidual(gain, pitch, first_res, blocknumber * 40, outsegment);
            quantizeSecondResidual(offset, outsegment);
            data_residual_Index += 40;
        }

        if(data_vad_mode)
            doVAD();
        else
            frameType = 0;
        packBitStream(output_bits, output_offset);
        System.arraycopy(data_residual, 160, data_residual, 0, 120);
    }

    protected void doVAD()
    {
    }

    void PreProcessing(float input[], float output[])
    {
        float buffer = data_buffer;
        float scaledSample = data_scaledSample;
        for(int i = 0; i < 160; i++)
        {
            float temp = scaledSample;
            scaledSample = input[i] * 0.5F;
            output[i] = buffer * -0.8599854F;
            buffer = (scaledSample - temp) + 0.9989929F * buffer;
            output[i] += buffer + 1E-015F;
        }

        data_buffer = buffer;
        data_scaledSample = scaledSample;
    }

    void Autocorrelations(float firstResidual[], float AutoCorrelations[])
    {
        for(int correlNumber = 0; correlNumber <= 8; correlNumber++)
        {
            float result = 0.0F;
            for(int i = correlNumber; i < 160; i++)
                result += firstResidual[i] * firstResidual[i - correlNumber];

            AutoCorrelations[correlNumber] = result;
        }

    }

    void schurRecursion(float AutoCorrelations[], float reflectionCoef[])
    {
        float KK[] = aux_KK;
        float P[] = aux_P;
        if(AutoCorrelations[0] == 0.0F)
        {
            for(int i = 1; i <= 8; i++)
                reflectionCoef[i] = 0.0F;

            return;
        }
        P[0] = AutoCorrelations[0];
        for(int i = 1; i <= 8; i++)
            P[i] = KK[9 - i] = AutoCorrelations[i];

        for(int i = 1; i <= 8; i++)
        {
            float temp = P[1] <= 0.0F ? -P[1] : P[1];
            if(P[0] < temp)
            {
                for(int k = i; k <= 8; k++)
                    reflectionCoef[k] = 0.0F;

                return;
            }
            reflectionCoef[i] = temp / P[0];
            if((double)P[1] > 0.0D)
                reflectionCoef[i] = -reflectionCoef[i];
            if(i == 8)
                return;
            P[0] = P[0] + P[1] * reflectionCoef[i];
            for(int k = 1; k <= 8 - i; k++)
            {
                P[k] = P[k + 1] + KK[9 - k] * reflectionCoef[i];
                KK[9 - k] = KK[9 - k] + P[k + 1] * reflectionCoef[i];
            }

        }

    }

    void rToLAR(float r[], float LARpp[])
    {
        float MIC[] = lut_MIC;
        float MAC[] = lut_MAC;
        float A[] = lut_A;
        float B[] = lut_B;
        float INVA[] = lut_INVA;
        int Parameters[] = data_Parameters;
        int pParams = 0;
        float LAR[] = data_LAR;
        for(int i = 1; i <= 8; i++)
        {
            float temp;
            if((double)r[i] >= 1.0D)
            {
                temp = MAC[i];
                LAR[i - 1] = 1.625F;
            } else
            if((double)r[i] <= -1D)
            {
                temp = MIC[i];
                LAR[i - 1] = -1.625F;
            } else
            {
                float reflectionCoef = r[i];
                float absReflectionCoef;
                float sgnReflectionCoef;
                if(reflectionCoef > 0.0F)
                {
                    absReflectionCoef = reflectionCoef;
                    sgnReflectionCoef = 1.0F;
                } else
                {
                    absReflectionCoef = -reflectionCoef;
                    sgnReflectionCoef = -1F;
                }
                float lar;
                if(absReflectionCoef < 0.675F)
                    lar = reflectionCoef;
                else
                if(absReflectionCoef < 0.95F)
                    lar = sgnReflectionCoef * (2.0F * absReflectionCoef - 0.675F);
                else
                    lar = sgnReflectionCoef * (8F * absReflectionCoef - 6.375F);
                LAR[i - 1] = lar;
                temp = A[i] * lar + B[i];
                if(temp >= 0.0F)
                    temp = (float)((double)temp + 0.5D);
                else
                    temp = (float)((double)temp - 0.5D);
                temp = (int)temp;
                if(temp > MAC[i])
                    temp = MAC[i];
                if(temp < MIC[i])
                    temp = MIC[i];
            }
            temp -= MIC[i];
            Parameters[pParams++] = (int)temp;
            LARpp[i] = ((temp + MIC[i]) - B[i]) * INVA[i];
        }

        data_pParams = pParams;
    }

    void latticeFilter(float LARpp[], float first_res[])
    {
        float rp[] = aux_rp;
        float u[] = data_u;
        float prevLARpp[] = data_prevLARpp;
        int k_start[] = lut_k_start;
        for(int j = 0; j < 4; j++)
        {
            float PrevCoef = 0.75F - (float)j * 0.25F;
            float PresentCoef = 1.0F - PrevCoef;
            for(int i = 1; i <= 8; i++)
            {
                float lar = prevLARpp[i] * PrevCoef + LARpp[i] * PresentCoef;
                float absLar;
                float sgnLar;
                if(lar > 0.0F)
                {
                    absLar = lar;
                    sgnLar = 1.0F;
                } else
                {
                    absLar = -lar;
                    sgnLar = -1F;
                }
                float reflectionCoef;
                if(absLar < 0.675F)
                    reflectionCoef = lar;
                else
                if(absLar < 1.225F)
                    reflectionCoef = sgnLar * (0.5F * absLar + 0.3375F);
                else
                    reflectionCoef = sgnLar * (0.125F * absLar + 0.796875F);
                rp[i] = reflectionCoef;
            }

            float u0 = u[0];
            float u1 = u[1];
            float u2 = u[2];
            float u3 = u[3];
            float u4 = u[4];
            float u5 = u[5];
            float u6 = u[6];
            float u7 = u[7];
            float rp1 = rp[1];
            float rp2 = rp[2];
            float rp3 = rp[3];
            float rp4 = rp[4];
            float rp5 = rp[5];
            float rp6 = rp[6];
            float rp7 = rp[7];
            float rp8 = rp[8];
            int k_end = k_start[j + 1];
            for(int k = k_start[j]; k < k_end; k++)
            {
                float di = first_res[k];
                float temp2 = di;
                float temp = u0 + rp1 * di;
                di += rp1 * u0;
                u0 = temp2;
                temp2 = temp;
                temp = u1 + rp2 * di;
                di += rp2 * u1;
                u1 = temp2;
                temp2 = temp;
                temp = u2 + rp3 * di;
                di += rp3 * u2;
                u2 = temp2;
                temp2 = temp;
                temp = u3 + rp4 * di;
                di += rp4 * u3;
                u3 = temp2;
                temp2 = temp;
                temp = u4 + rp5 * di;
                di += rp5 * u4;
                u4 = temp2;
                temp2 = temp;
                temp = u5 + rp6 * di;
                di += rp6 * u5;
                u5 = temp2;
                temp2 = temp;
                temp = u6 + rp7 * di;
                di += rp7 * u6;
                u6 = temp2;
                temp2 = temp;
                temp = u7 + rp8 * di;
                di += rp8 * u7;
                u7 = temp2;
                temp2 = temp;
                first_res[k] = di;
            }

            u[0] = u0;
            u[1] = u1;
            u[2] = u2;
            u[3] = u3;
            u[4] = u4;
            u[5] = u5;
            u[6] = u6;
            u[7] = u7;
            rp[1] = rp1;
            rp[2] = rp2;
            rp[3] = rp3;
            rp[4] = rp4;
            rp[5] = rp5;
            rp[6] = rp6;
            rp[7] = rp7;
            rp[8] = rp8;
        }

        System.arraycopy(LARpp, 1, prevLARpp, 1, 8);
    }

    private float calculatePitch(float subsegment[], int seg_offset, int pitch[])
    {
        float max_corr = 0.0F;
        float correlations[] = aux_correlations;
        int Nc[] = data_Nc;
        int blocknumber = data_blockNumber;
        int residual_Index = data_residual_Index;
        float residual[] = data_residual;
        int pParams = data_pParams;
        int Parameters[] = data_Parameters;
        pitch[0] = 40;
        for(int ii = 40; ii <= 120; ii += 3)
        {
            float result1 = 0.0F;
            float result2 = 0.0F;
            float result3 = 0.0F;
            float residual_p1 = residual[residual_Index - 1 - ii];
            float residual_p0 = residual[residual_Index - 2 - ii];
            for(int jj = 0; jj < 40; jj += 2)
            {
                float subsegment_temp0 = subsegment[seg_offset + jj];
                float subsegment_temp1 = subsegment[seg_offset + jj + 1];
                int temp_index = (residual_Index + jj) - ii;
                float residual_n1 = residual_p1;
                float residual_n2 = residual_p0;
                residual_p1 = residual[temp_index + 1];
                residual_p0 = residual[temp_index];
                result1 += subsegment_temp0 * residual_p0 + subsegment_temp1 * residual_p1;
                result2 += subsegment_temp0 * residual_n1 + subsegment_temp1 * residual_p0;
                result3 += subsegment_temp0 * residual_n2 + subsegment_temp1 * residual_n1;
            }

            correlations[ii] = result1;
            correlations[ii + 1] = result2;
            correlations[ii + 2] = result3;
        }

        for(int jj = 40; jj <= 120; jj++)
            if(max_corr < correlations[jj])
            {
                max_corr = correlations[jj];
                pitch[0] = jj;
            }

        Nc[blocknumber] = Parameters[pParams++] = pitch[0];
        data_pParams = pParams;
        return max_corr;
    }

    private float calculatePitch_Light(float subsegment[], int seg_offset, int pitch[])
    {
        float max_corr = 0.0F;
        float correlations[] = aux_correlations;
        int Nc[] = data_Nc;
        int blocknumber = data_blockNumber;
        int residual_Index = data_residual_Index;
        float residual[] = data_residual;
        int pParams = data_pParams;
        int Parameters[] = data_Parameters;
        float decimated_subsegment[] = aux_decimated_subsegment;
        float decimated_q_frstbase[] = aux_decimated_q_frstbase;
        float decimated_correlations[] = aux_decimated_correlations;
        decimated_subsegment[0] = 0.5F * (subsegment[seg_offset + 0] + subsegment[seg_offset + 1]);
        int ii = 1;
        for(int jj = 2; ii <= 19; jj += 2)
        {
            decimated_subsegment[ii] = 0.25F * (subsegment[(seg_offset + jj) - 1] + subsegment[seg_offset + jj + 1]) + 0.5F * subsegment[seg_offset + jj];
            ii++;
        }

        decimated_q_frstbase[0] = 0.5F * (residual[residual_Index - 120] + residual[residual_Index - 119]);
        ii = 1;
        for(int jj = -118; ii <= 59; jj += 2)
        {
            int index = residual_Index + jj;
            decimated_q_frstbase[ii] = 0.25F * (residual[index - 1] + residual[index + 1]) + 0.5F * residual[index];
            ii++;
        }

        int decimated_q_frstbase_index = 60;
        float result1;
        float result2;
        for(ii = 20; ii < 60; ii += 2)
        {
            result1 = 0.0F;
            result2 = 0.0F;
            for(int jj = 0; jj < 20; jj += 2)
            {
                float decimatedSubsegmentSample0 = decimated_subsegment[jj];
                float decimatedSubsegmentSample1 = decimated_subsegment[jj + 1];
                int index = (decimated_q_frstbase_index + jj) - ii;
                result1 += decimatedSubsegmentSample0 * decimated_q_frstbase[index] + decimatedSubsegmentSample1 * decimated_q_frstbase[index + 1];
                result2 += decimatedSubsegmentSample0 * decimated_q_frstbase[index - 1] + decimatedSubsegmentSample1 * decimated_q_frstbase[index];
            }

            decimated_correlations[ii - 20] = result1;
            decimated_correlations[(ii + 1) - 20] = result2;
        }

        result1 = 0.0F;
        for(int jj = 0; jj < 20; jj++)
            result1 += decimated_subsegment[jj] * decimated_q_frstbase[(decimated_q_frstbase_index + jj) - ii];

        decimated_correlations[ii - 20] = result1;
        int decimated_pitch = 20;
        max_corr = 0.0F;
        for(int jj = 20; jj <= 60; jj++)
            if(max_corr < decimated_correlations[jj - 20])
            {
                max_corr = decimated_correlations[jj - 20];
                decimated_pitch = jj;
            }

        ii = 2 * decimated_pitch - 1;
        if(ii == 39)
            ii = 40;
        if(ii == 119)
            ii = 118;
        result1 = 0.0F;
        result2 = 0.0F;
        float result3 = 0.0F;
        for(int jj = 0; jj <= 39; jj++)
        {
            int index = (residual_Index + jj) - ii;
            float subsegmentSample = subsegment[seg_offset + jj];
            result1 += subsegmentSample * residual[index];
            result2 += subsegmentSample * residual[index - 1];
            result3 += subsegmentSample * residual[index - 2];
        }

        correlations[ii] = result1;
        correlations[ii + 1] = result2;
        correlations[ii + 2] = result3;
        pitch[0] = 40;
        max_corr = 0.0F;
        for(int jj = ii; jj <= ii + 2; jj++)
            if(max_corr < correlations[jj])
            {
                max_corr = correlations[jj];
                pitch[0] = jj;
            }

        Nc[blocknumber] = Parameters[pParams++] = pitch[0];
        data_pParams = pParams;
        return max_corr;
    }

    private final float calculatePitchGain(float max_corr, int pitch)
    {
        float residual[] = data_residual;
        int residual_Index = data_residual_Index;
        int pParams = data_pParams;
        int Parameters[] = data_Parameters;
        float power = 0.0F;
        int index = residual_Index - pitch;
        for(int i = 0; i < 40; i++)
        {
            float sample = residual[index++];
            power += sample * sample;
        }

        float gain;
        if((double)max_corr <= 0.20000000000000001D * (double)power)
        {
            Parameters[pParams++] = 0;
            gain = 0.1F;
        } else
        if((double)max_corr <= 0.5D * (double)power)
        {
            Parameters[pParams++] = 1;
            gain = 0.35F;
        } else
        if((double)max_corr <= 0.80000000000000004D * (double)power)
        {
            Parameters[pParams++] = 2;
            gain = 0.65F;
        } else
        {
            Parameters[pParams++] = 3;
            gain = 1.0F;
        }
        data_pParams = pParams;
        return gain;
    }

    private final int analizeSecondResidual(float gain, int pitch, float subsegment[], int subseg_ofs, float outsegment[])
    {
        int Mc = 0;
        float EM = 0.0F;
        float wt[] = data_wt;
        int residual_Index = data_residual_Index;
        float residual[] = data_residual;
        int pParams = data_pParams;
        int Parameters[] = data_Parameters;
        int residual_index = residual_Index;
        for(int i = 0; i < 40; i++)
        {
            float pitchPredictSample = gain * residual[residual_index - pitch];
            residual[residual_index] = pitchPredictSample;
            wt[5 + i] = subsegment[subseg_ofs + i] - pitchPredictSample;
            residual_index++;
        }

        for(int i = 0; i < 40; i++)
            outsegment[i] = -0.01635742F * (wt[i] + wt[i + 10]) + -0.0456543F * (wt[i + 1] + wt[i + 9]) + 0.2507324F * (wt[i + 3] + wt[i + 7]) + 0.7008057F * (wt[i + 4] + wt[i + 6]) + wt[i + 5];

        for(int j = 0; j <= 3; j++)
        {
            float temp = 0.0F;
            int index = j;
            for(int i = 0; i <= 12; i++)
            {
                float sample = outsegment[index];
                index += 3;
                temp += sample * sample;
            }

            if(EM < temp)
            {
                Mc = j;
                EM = temp;
            }
        }

        Parameters[pParams++] = Mc;
        data_pParams = pParams;
        return Mc;
    }

    private final void quantizeSecondResidual(int Mc, float outsegment[])
    {
        int blocknumber = data_blockNumber;
        float currentxmax[] = data_currentxmax;
        float residual[] = data_residual;
        int residual_Index = data_residual_Index;
        int lg2s[] = lut_lg2s;
        int pParams = data_pParams;
        int Parameters[] = data_Parameters;
        float xmax = 0.0F;
        for(int i = 0; i < 13; i++)
        {
            float temp = outsegment[Mc + i * 3];
            if(temp > xmax)
                xmax = temp;
            else
            if(-temp > xmax)
                xmax = -temp;
        }

        currentxmax[blocknumber] = xmax;
        float xmaxp;
        if(xmax < 0.015625F)
        {
            int tempint = (int)(xmax * 1024F);
            Parameters[pParams++] = tempint;
            xmaxp = 31 + tempint * 32;
            int xmaxc = (int)(xmax * 1024F);
        } else
        {
            int tempint = (int)(32768F * xmax);
            tempint >>= 10;
            if(tempint < 31)
            {
                int i = lg2s[tempint];
                int xmaxc = (int)((float)(i << 3) + xmax * (float)(1024 >> i));
                Parameters[pParams++] = xmaxc;
                xmaxp = (((256 << i) + (32 << i)) - 1) + (xmaxc - 8 - 8 * i) * (32 << i);
            } else
            {
                Parameters[pParams++] = 63;
                int xmaxc = 63;
                xmaxp = 32767F;
            }
        }
        float div_xmaxp = 1.0F / xmaxp;
        for(int i = 0; i < 13; i++)
        {
            int tempint = (int)(outsegment[Mc + i * 3] * div_xmaxp * 262144F);
            if(tempint > 7)
                tempint = 7;
            else
            if(tempint < -7)
                tempint = -7;
            tempint = tempint + 7 >> 1;
            Parameters[pParams++] = tempint;
            residual[residual_Index + Mc + i * 3] += (0.25F * (float)tempint - 0.875F) * 3.051758E-005F * xmaxp;
        }

        data_pParams = pParams;
    }

    protected void packBitStream(byte outputBits[], int output_offset)
    {
        int Params[] = data_Parameters;
        int outPtr = output_offset;
        outputBits[outPtr++] = (byte)(0xd0 | Params[0] >> 2 & 0xf);
        outputBits[outPtr++] = (byte)((Params[0] & 3) << 6 | Params[1] & 0x3f);
        outputBits[outPtr++] = (byte)((Params[2] & 0x1f) << 3 | Params[3] >> 2 & 7);
        outputBits[outPtr++] = (byte)((Params[3] & 3) << 6 | (Params[4] & 0xf) << 2 | Params[5] >> 2 & 3);
        outputBits[outPtr++] = (byte)((Params[5] & 3) << 6 | (Params[6] & 7) << 3 | Params[7] & 7);
        int paramPtr = 8;
        for(int blocknum = 0; blocknum < 4; blocknum++)
        {
            outputBits[outPtr++] = (byte)((Params[paramPtr++] & 0x7f) << 1 | Params[paramPtr] >> 1 & 1);
            outputBits[outPtr++] = (byte)((Params[paramPtr++] & 1) << 7 | (Params[paramPtr++] & 3) << 5 | Params[paramPtr] >> 1 & 0x1f);
            outputBits[outPtr++] = (byte)((Params[paramPtr++] & 1) << 7 | (Params[paramPtr++] & 7) << 4 | (Params[paramPtr++] & 7) << 1 | Params[paramPtr] >> 2 & 1);
            outputBits[outPtr++] = (byte)((Params[paramPtr++] & 3) << 6 | (Params[paramPtr++] & 7) << 3 | Params[paramPtr++] & 7);
            outputBits[outPtr++] = (byte)((Params[paramPtr++] & 7) << 5 | (Params[paramPtr++] & 7) << 2 | Params[paramPtr] >> 1 & 3);
            outputBits[outPtr++] = (byte)((Params[paramPtr++] & 1) << 7 | (Params[paramPtr++] & 7) << 4 | (Params[paramPtr++] & 7) << 1 | Params[paramPtr] >> 2 & 1);
            outputBits[outPtr++] = (byte)((Params[paramPtr++] & 3) << 6 | (Params[paramPtr++] & 7) << 3 | Params[paramPtr++] & 7);
        }

    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
    protected static final int ORDER = 8;
    protected static final int BLOCKSIZE = 160;
    protected static final int SBLOCKSIZE = 40;
    protected static final int SBLOCKS = 4;
    protected static final int SUBSAMPBLOCKS = 4;
    protected static final int SUBSAMPSIZE = 13;
    protected static final int nPARAMETERS = 77;
    private static final int INTERPLAR = 4;
    public static final boolean USE_VAD = true;
    public static final boolean NO_VAD = false;
    public static final boolean GSM_FULL = true;
    public static final boolean GSM_LIGHT = false;
    protected int sidUpdateRate;
    public static final int GSM_SPEECH_FRAME = 0;
    public static final int GSM_SID_FRAME = 1;
    public static final int GSM_SILENCE_FRAME = 2;
    protected int frameType;
    protected boolean vadSupportFlag;
    protected float data_buffer;
    protected float data_scaledSample;
    protected int data_blockNumber;
    protected float aux_correlations[];
    protected int data_Nc[];
    protected float data_LAR[];
    protected float data_currentxmax[];
    protected int data_Parameters[];
    protected int data_pParams;
    protected float data_wt[];
    protected float data_u[];
    protected float data_prevLARpp[];
    protected boolean data_complexity_mode;
    protected boolean data_vad_mode;
    protected float data_residual[];
    protected int data_residual_Index;
    protected float data_input[];
    protected float data_first_res[];
    protected float data_acf[];
    protected float data_r[];
    protected float data_LARpp[];
    protected float aux_decimated_subsegment[];
    protected float aux_decimated_q_frstbase[];
    protected float aux_decimated_correlations[];
    protected float aux_KK[];
    protected float aux_P[];
    protected float encode_outsegment[];
    protected int encode_pitchArray[];
    protected float aux_rp[];
    protected static float lut_A[] = {
        0.0F, 20F, 20F, 20F, 20F, 13.637F, 15F, 8.334F, 8.824F
    };
    protected static float lut_INVA[] = {
        0.0F, 0.05F, 0.05F, 0.05F, 0.05F, 0.07332991F, 0.06666667F, 0.1199904F, 0.1133273F
    };
    protected static float lut_MIC[] = {
        0.0F, -32F, -32F, -16F, -16F, -8F, -8F, -4F, -4F
    };
    protected static float lut_MAC[] = {
        0.0F, 31F, 31F, 15F, 15F, 7F, 7F, 3F, 3F
    };
    protected static float lut_B[] = {
        0.0F, 0.0F, 0.0F, 4F, -5F, 0.184F, -3.5F, -0.666F, -2.235F
    };
    protected static int lut_k_start[] = {
        0, 13, 27, 40, 160
    };
    protected static int lut_lg2s[];
    private static final int GSM_MAGIC = 13;

    static 
    {
        lut_lg2s = new int[32];
        int temp_index = 0;
        lut_lg2s[temp_index++] = 1;
        lut_lg2s[temp_index++] = 2;
        for(int ii = 3; ii <= 6; ii++)
        {
            for(int jj = 1; jj <= 1 << ii - 2; jj++)
                lut_lg2s[temp_index++] = ii;

        }

    }
}
