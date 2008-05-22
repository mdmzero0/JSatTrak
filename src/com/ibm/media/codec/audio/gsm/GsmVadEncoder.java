// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GsmVadEncoder.java

package com.ibm.media.codec.audio.gsm;


// Referenced classes of package com.ibm.media.codec.audio.gsm:
//            GsmEncoder

public class GsmVadEncoder extends GsmEncoder
{

    public GsmVadEncoder()
    {
        vad_prevLARs = new float[4][8];
        vad_prevxmax = new float[4][4];
        vad_prevSID = new int[77];
        vad_LAR = new float[8];
        vad_currentxmax = new float[4];
        vad_rvad = new float[9];
        vad_sacf = new float[3][9];
        vad_sav0 = new float[4][9];
        vadAux_av0 = new float[9];
        vadAux_av1 = new float[9];
        vadAux_refcoef = new float[9];
        vadAux_rav1 = new float[9];
        vadAux_coef = new float[9];
        vadAux_tempcoef = new float[9];
        vadAux_aav1 = new float[9];
    }

    protected void doVAD()
    {
        vad_calculations();
        DisConTrans();
    }

    public void gsm_encoder_reset()
    {
        super.gsm_encoder_reset();
        super.vadSupportFlag = true;
        vad_sincelastSID = 24;
        vad_sinceSPEECHburst = 0;
        for(int i = 0; i < vad_prevLARs.length; i++)
        {
            for(int j = 0; j < vad_prevLARs[i].length; j++)
                vad_prevLARs[i][j] = 0.0F;

        }

        for(int i = 0; i < vad_prevxmax.length; i++)
        {
            vad_prevxmax[i][0] = 0.0F;
            vad_prevxmax[i][1] = 0.0F;
            vad_prevxmax[i][2] = 0.0F;
            vad_prevxmax[i][3] = 0.0F;
        }

        for(int i = 0; i < vad_prevSID.length; i++)
            vad_prevSID[i] = 0;

        vad_prevSID[0] = 2;
        vad_prevSID[1] = 28;
        vad_prevSID[2] = 18;
        vad_prevSID[3] = 12;
        vad_prevSID[4] = 7;
        vad_prevSID[5] = 5;
        vad_prevSID[6] = 3;
        vad_prevSID[7] = 2;
        vad_hangover = 1;
        vad_LARindex = 0;
        vad_SP = true;
        vad_prevSP = true;
        vad_lastsent = 0;
        vad_rvad[0] = 6F;
        vad_rvad[1] = -4F;
        vad_rvad[2] = 1.0F;
        for(int i = 3; i < vad_rvad.length; i++)
            vad_rvad[i] = 0.0F;

        for(int i = 0; i < vad_sacf.length; i++)
        {
            for(int j = 0; j < vad_sacf[i].length; j++)
                vad_sacf[i][j] = 0.0F;

        }

        for(int i = 0; i < vad_sav0.length; i++)
        {
            for(int j = 0; j < vad_sav0[i].length; j++)
                vad_sav0[i][j] = 0.0F;

        }

        vad_sacfPt = 0;
        vad_sav0Pt = 0;
        vad_lastdm = 0.0F;
        vad_oldlagcount = 0;
        vad_veryoldlagcount = 0;
        vad_adaptcount = 0;
        vad_burstcount = 0;
        vad_hangcount = -1;
        vad_oldlag = 40;
        vad_thvad = 0.0002328306F;
    }

    void vad_calculations()
    {
        int lg2s[] = GsmEncoder.lut_lg2s;
        boolean vadF = VAD(super.data_acf, super.data_Nc);
        if(vadF)
        {
            vad_sinceSPEECHburst = 0;
            vad_SP = true;
            vad_LARindex = vad_LARindex != 3 ? vad_LARindex + 1 : 0;
            for(int i = 0; i < 8; i++)
                vad_prevLARs[vad_LARindex][i] = vad_LAR[i];

            for(int i = 0; i < 4; i++)
                vad_prevxmax[vad_LARindex][i] = vad_currentxmax[i];

            if(vad_sincelastSID < 24)
                vad_sincelastSID++;
        } else
        {
            if(vad_sinceSPEECHburst++ == 0)
                vad_hangover = vad_sincelastSID < 24 ? 0 : 1;
            else
            if(vad_sinceSPEECHburst > 4)
                vad_hangover = -1;
            if(vad_hangover == 1)
            {
                vad_LARindex = vad_LARindex != 3 ? vad_LARindex + 1 : 0;
                for(int i = 0; i < 8; i++)
                    vad_prevLARs[vad_LARindex][i] = super.data_LAR[i];

                for(int i = 0; i < 4; i++)
                    vad_prevxmax[vad_LARindex][i] = vad_currentxmax[i];

                if(vad_sincelastSID < 24)
                    vad_sincelastSID++;
                vad_SP = true;
            } else
            if(vad_hangover == 0)
            {
                vad_LARindex = vad_LARindex != 3 ? vad_LARindex + 1 : 0;
                for(int i = 0; i < 8; i++)
                    vad_prevLARs[vad_LARindex][i] = super.data_LAR[i];

                for(int i = 0; i < 4; i++)
                    vad_prevxmax[vad_LARindex][i] = vad_currentxmax[i];

                for(int i = 0; i < 77; i++)
                    super.data_Parameters[i] = vad_prevSID[i];

                if(vad_sincelastSID < 24)
                    vad_sincelastSID++;
                vad_SP = false;
            } else
            {
                for(int i = 1; i <= 8; i++)
                {
                    float LARinter = 0.25F * (vad_prevLARs[0][i - 1] + vad_prevLARs[1][i - 1] + vad_prevLARs[2][i - 1] + vad_prevLARs[3][i - 1]);
                    LARinter = GsmEncoder.lut_A[i] * LARinter + GsmEncoder.lut_B[i];
                    if(LARinter > GsmEncoder.lut_MAC[i])
                        LARinter = GsmEncoder.lut_MAC[i];
                    if(LARinter < GsmEncoder.lut_MIC[i])
                        LARinter = GsmEncoder.lut_MIC[i];
                    vad_prevSID[i - 1] = (int)((LARinter - GsmEncoder.lut_MIC[i]) + 0.5F);
                }

                float XMAXinter = 0.0F;
                for(int i = 0; i < 4; i++)
                {
                    for(int k = 0; k < 4; k++)
                        XMAXinter += vad_prevxmax[k][i];

                }

                XMAXinter *= 0.0625F;
                if((double)XMAXinter < 0.015625D)
                {
                    XMAXinter *= 1024F;
                } else
                {
                    int temp = (int)(32768D * (double)XMAXinter);
                    temp >>= 10;
                    if(temp < 31)
                    {
                        int i = lg2s[temp];
                        XMAXinter = (float)(i << 3) + XMAXinter * (float)(1024 >> i);
                    } else
                    {
                        XMAXinter = 63F;
                    }
                }
                for(int i = 0; i < 4; i++)
                    vad_prevSID[11 + 17 * i] = (int)XMAXinter;

                vad_LARindex = vad_LARindex != 3 ? vad_LARindex + 1 : 0;
                for(int i = 0; i < 8; i++)
                    vad_prevLARs[vad_LARindex][i] = super.data_LAR[i];

                for(int i = 0; i < 4; i++)
                    vad_prevxmax[vad_LARindex][i] = vad_currentxmax[i];

                for(int i = 0; i < 77; i++)
                    super.data_Parameters[i] = vad_prevSID[i];

                vad_sincelastSID = 0;
                vad_SP = false;
            }
        }
    }

    void DisConTrans()
    {
        boolean sendF = false;
        if(vad_SP)
        {
            super.frameType = 0;
            sendF = true;
        } else
        if(!vad_SP && vad_prevSP)
        {
            super.frameType = 1;
            sendF = true;
        }
        vad_prevSP = vad_SP;
        if(sendF)
            vad_lastsent = 0;
        else
        if(super.sidUpdateRate > 0 && ++vad_lastsent >= super.sidUpdateRate)
        {
            vad_lastsent = 0;
            sendF = true;
            super.frameType = 1;
        } else
        {
            for(int i = 0; i < 77; i++)
                super.data_Parameters[i] = 0;

            super.frameType = 2;
        }
    }

    boolean VAD(float ACF[], int Nc[])
    {
        float av0[] = vadAux_av0;
        float av1[] = vadAux_av1;
        float refcoef[] = vadAux_refcoef;
        float rav1[] = vadAux_rav1;
        float pvad = adaptiveFiltering(ACF);
        ACFaverage(ACF, av0, av1);
        schurRecursion(av1, refcoef);
        Step_up(refcoef, rav1);
        boolean statF = SpectralComp(rav1, av0);
        boolean pitchF = PitchDetect();
        ThresAdapt(pitchF, statF, ACF, rav1, pvad);
        boolean vadF = vad_decision(pvad);
        pitchCounting(Nc);
        return vadF;
    }

    float adaptiveFiltering(float ACF[])
    {
        float pvad;
        if(ACF[0] < 1E-015F)
        {
            pvad = 0.0F;
            ACF[0] = 0.0F;
            return pvad;
        }
        pvad = 0.0F;
        for(int i = 1; i <= 8; i++)
            pvad += vad_rvad[i] * ACF[i];

        pvad = 2.0F * pvad + vad_rvad[0] * ACF[0];
        return pvad;
    }

    void ACFaverage(float ACF[], float av0[], float av1[])
    {
        int sacfPt = vad_sacfPt;
        int sav0Pt = vad_sav0Pt;
        for(int i = 0; i <= 8; i++)
        {
            av0[i] = ACF[i] + vad_sacf[0][i] + vad_sacf[1][i] + vad_sacf[2][i];
            vad_sacf[sacfPt][i] = ACF[i];
            av1[i] = vad_sav0[sav0Pt][i];
            vad_sav0[sav0Pt][i] = av0[i];
        }

        vad_sacfPt = sacfPt != 2 ? sacfPt + 1 : 0;
        vad_sav0Pt = sav0Pt != 3 ? sav0Pt + 1 : 0;
    }

    void Step_up(float refcoef[], float rav1[])
    {
        float coef[] = vadAux_coef;
        float tempcoef[] = vadAux_tempcoef;
        float aav1[] = vadAux_aav1;
        coef[0] = 1.0F;
        coef[1] = refcoef[1];
        for(int m = 2; m < 9; m++)
        {
            for(int i = 1; i < m; i++)
                tempcoef[i] = coef[i] + refcoef[m] * coef[m - i];

            for(int i = 1; i < m; i++)
                coef[i] = tempcoef[i];

            coef[m] = refcoef[m];
        }

        for(int i = 0; i <= 8; i++)
            aav1[i] = coef[i];

        for(int i = 0; i <= 8; i++)
        {
            rav1[i] = 0.0F;
            for(int k = 0; k <= 8 - i; k++)
                rav1[i] = rav1[i] + aav1[k] * aav1[k + i];

        }

    }

    boolean SpectralComp(float rav1[], float av0[])
    {
        float distortion;
        if(av0[0] < 1E-015F)
        {
            distortion = 0.0F;
            for(int i = 1; i <= 8; i++)
                distortion += rav1[i];

            distortion = 2.0F * distortion + rav1[0];
        } else
        {
            distortion = 0.0F;
            for(int i = 1; i <= 8; i++)
                distortion += rav1[i] * av0[i];

            distortion = 2.0F * distortion + rav1[0] * av0[0];
            distortion /= av0[0];
        }
        float difference = distortion - vad_lastdm;
        boolean statF = difference < 0.05F && difference > -0.05F;
        vad_lastdm = distortion;
        return statF;
    }

    boolean PitchDetect()
    {
        return vad_oldlagcount + vad_veryoldlagcount >= 4;
    }

    void ThresAdapt(boolean pitchF, boolean statF, float ACF[], float rav1[], float pvad)
    {
        float thvad = vad_thvad;
        if(ACF[0] < 6.984919E-005F)
        {
            vad_thvad = 0.0001862645F;
            return;
        }
        if(pitchF || !statF)
        {
            vad_adaptcount = 0;
            return;
        }
        if(++vad_adaptcount <= 8)
            return;
        thvad -= thvad / 32F;
        float temp = pvad * 3F;
        if(thvad < temp)
        {
            thvad += thvad / 16F;
            if(temp < thvad)
                thvad = temp;
        }
        temp = pvad + 0.01862645F;
        if(thvad > temp)
            thvad = temp;
        vad_thvad = thvad;
        for(int i = 0; i <= 8; i++)
            vad_rvad[i] = rav1[i];

        vad_adaptcount = 9;
    }

    boolean vad_decision(float pvad)
    {
        boolean vadF;
        if(pvad > vad_thvad)
        {
            vadF = true;
            vad_burstcount++;
            if(vad_burstcount >= 3)
            {
                vad_hangcount = 5;
                vad_burstcount = 3;
            }
        } else
        {
            vadF = false;
            vad_burstcount = 0;
        }
        if(vad_hangcount >= 0)
        {
            vadF = true;
            vad_hangcount--;
        }
        return vadF;
    }

    void pitchCounting(int Nc[])
    {
        int lagcount = 0;
        for(int i = 0; i <= 3; i++)
        {
            int minlag;
            int maxlag;
            if(vad_oldlag > Nc[i])
            {
                minlag = Nc[i];
                maxlag = vad_oldlag;
            } else
            {
                minlag = vad_oldlag;
                maxlag = Nc[i];
            }
            int smallag = maxlag % minlag;
            if(smallag < 2 || minlag - smallag < 2)
                lagcount++;
            vad_oldlag = Nc[i];
        }

        vad_veryoldlagcount = vad_oldlagcount;
        vad_oldlagcount = lagcount;
    }

    private static final float DIST_TH = 0.05F;
    private static final int PITCH_TH = 4;
    private static final int PITCH_PRECISION = 2;
    private static final float PTH = 6.984919E-005F;
    private static final float MARGIN = 0.01862645F;
    private static final float PLEV = 0.0001862645F;
    private static final float FAC = 3F;
    private static final int ADP = 8;
    private static final float INC = 16F;
    private static final float DEC = 32F;
    private static final float EPSILON = 1E-015F;
    private static final int NEW_SID = -1;
    private static final float MAX_LAR = 1.625F;
    private static final int E_PTH = 19;
    private static final int M_PTH = 18750;
    private static final int E_MARGIN = 27;
    private static final int M_MARGIN = 19531;
    private static final int E_PLEV = 20;
    private static final int M_PLEV = 25000;
    public static final int UPDATE_RATE = 10;
    private static final int nFRAMES = 4;
    int vad_sincelastSID;
    int vad_sinceSPEECHburst;
    float vad_prevLARs[][];
    float vad_prevxmax[][];
    int vad_prevSID[];
    int vad_hangover;
    int vad_LARindex;
    boolean vad_SP;
    float vad_LAR[];
    float vad_currentxmax[];
    int vad_blocknumber;
    int vad_lastsent;
    boolean vad_prevSP;
    float vad_rvad[];
    float vad_sacf[][];
    float vad_sav0[][];
    int vad_sacfPt;
    int vad_sav0Pt;
    float vad_lastdm;
    int vad_oldlagcount;
    int vad_veryoldlagcount;
    int vad_adaptcount;
    int vad_burstcount;
    int vad_hangcount;
    int vad_oldlag;
    float vad_thvad;
    float vadAux_av0[];
    float vadAux_av1[];
    float vadAux_refcoef[];
    float vadAux_rav1[];
    float vadAux_coef[];
    float vadAux_tempcoef[];
    float vadAux_aav1[];
}
