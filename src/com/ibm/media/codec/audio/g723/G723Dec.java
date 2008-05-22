// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   G723Dec.java

package com.ibm.media.codec.audio.g723;


// Referenced classes of package com.ibm.media.codec.audio.g723:
//            LINEDEF, PFDEF, DecoderData, SFSDEF, 
//            G723Tables

public final class G723Dec
{

    public G723Dec()
    {
        Decod_Acbk_RezBuf = new float[67];
    }

    protected final void decoderOpen()
    {
        decoderData = new DecoderData();
        decoderReset();
    }

    protected final void decoderReset()
    {
        for(int i = 0; i < 10; i++)
        {
            decoderData.PrevLsp[i] = fLspDcTable[i];
            decoderData.SyntIirDl[i] = decoderData.PostFirDl[i] = decoderData.PostIirDl[i] = 0.0F;
        }

        for(int i = 0; i < decoderData.PrevExc.length; i++)
            decoderData.PrevExc[i] = 0.0F;

        decoderData.Ecount = 0;
        decoderData.InterGain = 0.0F;
        decoderData.InterIndx = 0;
        decoderData.Park = 0.0F;
        decoderData.Rseed = 0;
        decoderData.Gain = 1.0F;
        decoderData.BlockIndex = 0;
        decoderData.BlockCount = 0;
        decoderData.WrkRate = 0;
        decoderData.UsePf = true;
        for(int i = 0; i < 10; i++)
            decoderData.PrevLsp[i] = fLspDcTable[i];

    }

    protected final void decodeFrame(byte bits_input[], int indexInp, byte outBuffer[], int indexOut)
    {
        float OutputBuffer[] = decode_OutputBuffer;
        float DataBuff[] = OutputBuffer;
        float SfGain = 0.0F;
        float QntLpc[] = decode_QntLpc;
        float AcbkCont[] = decode_AcbkCont;
        float LspVect[] = decode_LspVect;
        LINEDEF Line = decode_Line;
        PFDEF Pf[] = decode_Pf;
        int flag[] = decode_flag;
        int Ftyp[] = decode_Ftyp;
        float SyntIir[] = decode_SyntIir;
        float PostFir[] = decode_PostFir;
        float PostIir[] = decode_PostIir;
        if(decoderData.BlockIndex == 0)
        {
            decoderData.BlockCount = 0;
            System.arraycopy(decoderData.PrevExc, 960, decoderData.PrevExc, 0, 145);
        }
        int PrevExcIndex = decoderData.BlockIndex * 240 + 145;
        float PrevExc[] = decoderData.PrevExc;
        decoderData.BlockCount++;
        decoderData.BlockIndex = decoderData.BlockCount % 4;
        for(int i = 0; i < 10; i++)
        {
            SyntIir[10 - i - 1] = decoderData.SyntIirDl[i];
            PostFir[10 - i - 1] = decoderData.PostFirDl[i];
            PostIir[10 - i - 1] = decoderData.PostIirDl[i];
        }

        Line_Unpk(bits_input, indexInp, Ftyp, (short)0, flag, Line);
        if(Line.Crc != 0)
            decoderData.Ecount++;
        else
            decoderData.Ecount = 0;
        if(decoderData.Ecount > 3)
            decoderData.Ecount = 3;
        Lsp_Inq(LspVect, decoderData.PrevLsp, Line.LspId, Line.Crc);
        Lsp_Int(QntLpc, LspVect, decoderData.PrevLsp);
        if(decoderData.Ecount == 0)
        {
            int i = Line.Sfs[2].Mamp + Line.Sfs[3].Mamp >> 1;
            decoderData.InterGain = FcbkGainTable[i];
        } else
        {
            decoderData.InterGain *= 0.75F;
        }
        if(decoderData.Ecount == 0)
        {
            for(int i = 0; i < 4; i++)
            {
                Fcbk_Unpk(PrevExc, PrevExcIndex + 60 * i, Line.Sfs[i], Line.Olp[i >> 1], i);
                Decod_Acbk(AcbkCont, PrevExc, (PrevExcIndex + 60 * i) - 145, Line.Olp[i >> 1], Line.Sfs[i].AcLg, Line.Sfs[i].AcGn, decoderData.WrkRate);
                for(int j = 0; j < 60; j++)
                    PrevExc[PrevExcIndex + i * 60 + j] += AcbkCont[j];

            }

            System.arraycopy(PrevExc, PrevExcIndex, DataBuff, 0, 240);
            if(decoderData.UsePf)
            {
                for(int i = 0; i < 4; i++)
                    Comp_Lpf(PrevExc, PrevExcIndex - 145, Line.Olp[i >> 1], i, Pf[i]);

            }
            if(decoderData.UsePf)
            {
                for(int i = 0; i < 4; i++)
                    Filt_Lpf(DataBuff, PrevExc, PrevExcIndex - 145, Pf[i], i);

            }
        }
        float Dpnt[] = DataBuff;
        int indexDpnt = 0;
        for(int i = 0; i < 4; i++)
        {
            Synt(Dpnt, indexDpnt, QntLpc, i * 10, SyntIir, (10 + i * 60) - 1);
            if(decoderData.UsePf)
            {
                SfGain = Spf(Dpnt, indexDpnt, QntLpc, i * 10, PostFir, (10 + i * 60) - 1, PostIir, (10 + i * 60) - 1);
                Scale(Dpnt, indexDpnt, SfGain);
            }
            indexDpnt += 60;
        }

        for(int i = 0; i < 10; i++)
        {
            decoderData.SyntIirDl[i] = SyntIir[250 - i - 1];
            decoderData.PostFirDl[i] = PostFir[250 - i - 1];
            decoderData.PostIirDl[i] = PostIir[250 - i - 1];
        }

        Fl2Sh(DataBuff, 240, outBuffer, indexOut);
    }

    private final void Fcbk_Unpk(float ExcBuf[], int ExcBufIndex, SFSDEF Sfs, int pitch, int SfrNum)
    {
        switch(decoderData.WrkRate)
        {
        case 0: // '\0'
            int PlsNum = SfrNum != 0 && SfrNum != 2 ? 5 : 6;
            for(int i = ExcBufIndex; i < ExcBufIndex + 60; i++)
                ExcBuf[i] = 0.0F;

            if(Sfs.Ppos >= MaxPosTable[SfrNum])
                return;
            int j = 6 - PlsNum;
            long CombCounter = Sfs.Ppos;
            for(int i = 0; i < 30; i++)
                if(CombCounter < (long)CombinatorialTable[j][i])
                {
                    j++;
                    if((Sfs.Pamp & 1 << 6 - j) != 0)
                        ExcBuf[Sfs.Grid + 2 * i + ExcBufIndex] = -FcbkGainTable[Sfs.Mamp];
                    else
                        ExcBuf[Sfs.Grid + 2 * i + ExcBufIndex] = FcbkGainTable[Sfs.Mamp];
                    if(j == 6)
                        break;
                } else
                {
                    CombCounter -= CombinatorialTable[j][i];
                }

            if(Sfs.Tran == 1)
                Gen_Trn(ExcBuf, ExcBuf, ExcBufIndex, pitch);
            break;
        }
    }

    private final void Comp_Lpf(float DecExc[], int DecExcIndex, int pitch, int SfrNum, PFDEF Pf)
    {
        float Lcr0 = 0.0F;
        float Lcr1 = 0.0F;
        float Lcr2 = 0.0F;
        float Lcr3 = 0.0F;
        float Lcr4 = 0.0F;
        Pf.Indx = 0;
        Pf.Gain = 0.0F;
        Pf.ScGn = 1.0F;
        int Bindx = Find_B(DecExc, DecExcIndex, pitch, SfrNum);
        int Findx = Find_F(DecExc, DecExcIndex, pitch, SfrNum);
        if(Bindx == 0 && Findx == 0)
            return;
        int DecExcPointer = DecExcIndex + 145 + SfrNum * 60;
        for(int j = 0; j < 60; j += 2)
        {
            float DecExc0 = DecExc[DecExcPointer + j];
            float DecExc1 = DecExc[DecExcPointer + j + 1];
            Lcr0 += DecExc0 * DecExc0 + DecExc1 * DecExc1;
        }

        DecExcPointer = DecExcIndex + 145 + SfrNum * 60;
        if(Bindx != 0)
        {
            for(int j = 0; j < 60; j += 2)
            {
                float DecExc0 = DecExc[DecExcPointer + Bindx + j];
                float DecExc1 = DecExc[DecExcPointer + Bindx + j + 1];
                Lcr1 += DecExc[DecExcPointer + j] * DecExc0 + DecExc[DecExcPointer + j + 1] * DecExc1;
                Lcr2 += DecExc0 * DecExc0 + DecExc1 * DecExc1;
            }

        }
        DecExcPointer = DecExcIndex + 145 + SfrNum * 60;
        if(Findx != 0)
        {
            for(int j = 0; j < 60; j += 2)
            {
                float DecExc0 = DecExc[DecExcPointer + Findx + j];
                float DecExc1 = DecExc[DecExcPointer + Findx + j + 1];
                Lcr3 += DecExc[DecExcPointer + j] * DecExc0 + DecExc[DecExcPointer + j + 1] * DecExc1;
                Lcr4 += DecExc0 * DecExc0 + DecExc1 * DecExc1;
            }

        }
        if((float)Bindx != 0.0F && (float)Findx == 0.0F)
            Get_Ind(Bindx, Lcr0, Lcr1, Lcr2, Pf);
        if((float)Bindx == 0.0F && (float)Findx != 0.0F)
            Get_Ind(Findx, Lcr0, Lcr3, Lcr4, Pf);
        if(Bindx != 0 && Findx != 0)
            if(Lcr1 * Lcr1 * Lcr4 > Lcr3 * Lcr3 * Lcr2)
                Get_Ind(Bindx, Lcr0, Lcr1, Lcr2, Pf);
            else
                Get_Ind(Findx, Lcr0, Lcr3, Lcr4, Pf);
    }

    private final int Find_B(float DecExc[], int DecExcIndex, int pitch, int SfrNum)
    {
        int Indx = 0;
        int DecExcPointer = DecExcIndex + 145 + SfrNum * 60;
        float BestCorr = 0.0F;
        float corr6 = 0.0F;
        float corr5 = 0.0F;
        float corr4 = 0.0F;
        float corr3 = 0.0F;
        float corr2 = 0.0F;
        float corr1 = 0.0F;
        float corr0 = 0.0F;
        if(pitch > 142)
            pitch = 142;
        int DecExcCrossPointer = DecExcPointer - (pitch - 3);
        for(int j = 0; j < 60; j++)
        {
            float DecExcPointer_j = DecExc[DecExcPointer + j];
            corr0 += DecExcPointer_j * DecExc[DecExcCrossPointer + j];
            corr1 += DecExcPointer_j * DecExc[(DecExcCrossPointer + j) - 1];
            corr2 += DecExcPointer_j * DecExc[(DecExcCrossPointer + j) - 2];
            corr3 += DecExcPointer_j * DecExc[(DecExcCrossPointer + j) - 3];
            corr4 += DecExcPointer_j * DecExc[(DecExcCrossPointer + j) - 4];
            corr5 += DecExcPointer_j * DecExc[(DecExcCrossPointer + j) - 5];
            corr6 += DecExcPointer_j * DecExc[(DecExcCrossPointer + j) - 6];
        }

        if(corr0 > BestCorr)
        {
            BestCorr = corr0;
            Indx = -(pitch - 3);
        }
        if(corr1 > BestCorr)
        {
            BestCorr = corr1;
            Indx = -(pitch - 2);
        }
        if(corr2 > BestCorr)
        {
            BestCorr = corr2;
            Indx = -(pitch - 1);
        }
        if(corr3 > BestCorr)
        {
            BestCorr = corr3;
            Indx = -pitch;
        }
        if(corr4 > BestCorr)
        {
            BestCorr = corr4;
            Indx = -(pitch + 1);
        }
        if(corr5 > BestCorr)
        {
            BestCorr = corr5;
            Indx = -(pitch + 2);
        }
        if(corr6 > BestCorr)
        {
            BestCorr = corr6;
            Indx = -(pitch + 3);
        }
        return Indx;
    }

    private final int Find_F(float DecExc[], int DecExcIndex, int pitch, int SfrNum)
    {
        int Indx = 0;
        int max = 240 - SfrNum * 60 - 60;
        int DecExcPointer = DecExcIndex + 145 + SfrNum * 60;
        if(pitch > 142)
            pitch = 142;
        float BestCorr = 0.0F;
        if(max > pitch + 3)
        {
            float corr0 = 0.0F;
            float corr1 = 0.0F;
            float corr2 = 0.0F;
            float corr3 = 0.0F;
            float corr4 = 0.0F;
            float corr5 = 0.0F;
            float corr6 = 0.0F;
            int DecExcCrossPointer = DecExcPointer + (pitch - 3);
            for(int j = 0; j < 60; j++)
            {
                float DecExcPointer_j = DecExc[DecExcPointer + j];
                corr0 += DecExcPointer_j * DecExc[DecExcCrossPointer + j];
                corr1 += DecExcPointer_j * DecExc[DecExcCrossPointer + j + 1];
                corr2 += DecExcPointer_j * DecExc[DecExcCrossPointer + j + 2];
                corr3 += DecExcPointer_j * DecExc[DecExcCrossPointer + j + 3];
                corr4 += DecExcPointer_j * DecExc[DecExcCrossPointer + j + 4];
                corr5 += DecExcPointer_j * DecExc[DecExcCrossPointer + j + 5];
                corr6 += DecExcPointer_j * DecExc[DecExcCrossPointer + j + 6];
            }

            if(corr0 > BestCorr)
            {
                BestCorr = corr0;
                Indx = pitch - 3;
            }
            if(corr1 > BestCorr)
            {
                BestCorr = corr1;
                Indx = pitch - 2;
            }
            if(corr2 > BestCorr)
            {
                BestCorr = corr2;
                Indx = pitch - 1;
            }
            if(corr3 > BestCorr)
            {
                BestCorr = corr3;
                Indx = pitch;
            }
            if(corr4 > BestCorr)
            {
                BestCorr = corr4;
                Indx = pitch + 1;
            }
            if(corr5 > BestCorr)
            {
                BestCorr = corr5;
                Indx = pitch + 2;
            }
            if(corr6 > BestCorr)
            {
                BestCorr = corr6;
                Indx = pitch + 3;
            }
        } else
        {
            for(int i = pitch - 3; i <= max; i++)
            {
                int DecExcCrossPointer = DecExcPointer + i;
                float corr = 0.0F;
                for(int j = 0; j < 60; j++)
                    corr += DecExc[DecExcPointer + j] * DecExc[DecExcCrossPointer + j];

                if(corr > BestCorr)
                {
                    BestCorr = corr;
                    Indx = i;
                }
            }

        }
        return Indx;
    }

    private final void Get_Ind(int Ind, float Ten, float Ccr, float Enr, PFDEF Pf)
    {
        Pf.Indx = Ind;
        float LpfConstTable_WrkRate = decoderData.WrkRate != 0 ? 0.25F : 0.1875F;
        if(Ccr * Ccr > 0.25F * Ten * Enr)
        {
            if(Ccr >= Enr)
                Pf.Gain = LpfConstTable_WrkRate;
            else
                Pf.Gain = (LpfConstTable_WrkRate * Ccr) / Enr;
            float denom = Ten + 2.0F * Pf.Gain * Ccr + Pf.Gain * Pf.Gain * Enr;
            if(denom < 1.175494E-038F)
                Pf.ScGn = 0.0F;
            else
                Pf.ScGn = (float)Math.sqrt(Ten / denom);
        } else
        {
            Pf.Gain = 0.0F;
            Pf.ScGn = 1.0F;
        }
        Pf.Gain *= Pf.ScGn;
    }

    private final void Filt_Lpf(float ExcBuf[], float DecExc[], int DecExcIndex, PFDEF Pf, int SfrNum)
    {
        float ScGn = Pf.ScGn;
        float Gain = Pf.Gain;
        int Indx = Pf.Indx;
        int ExcBufIndex = SfrNum * 60;
        int DecExcIndex1 = DecExcIndex + 145 + SfrNum * 60;
        int DecExcIndex2 = DecExcIndex + 145 + SfrNum * 60 + Indx;
        for(int i = 0; i < 60; i++)
            ExcBuf[ExcBufIndex + i] = DecExc[DecExcIndex1 + i] * ScGn + DecExc[DecExcIndex2 + i] * Gain;

    }

    private final void Fl2Sh(float BufF[], int Len, byte outBuffer[], int indexOut)
    {
        int j = 0;
        for(int i = 0; i < Len; i++)
        {
            float sample = BufF[i];
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

    private final float Rand(int p[])
    {
        int Temp = p[0];
        Temp &= 0xffff;
        Temp = Temp * 521 + 259;
        p[0] = (short)Temp;
        return (float)(short)Temp * 3.051758E-005F;
    }

    private final void Scale(float signal[], int indexSignal, float SfGain)
    {
        float AlphaGain = 0.0625F * SfGain;
        float local_Gain = decoderData.Gain;
        int iEnd = 60 + indexSignal;
        for(int i = indexSignal; i < iEnd; i++)
        {
            local_Gain = 0.9375F * local_Gain + AlphaGain;
            signal[i] *= local_Gain * 1.0625F;
        }

        decoderData.Gain = local_Gain;
    }

    private final void Synt(float signal[], int indexSig, float Lpc[], int indexLpc, float iir[], int indexIir)
    {
        float Lpc0 = Lpc[indexLpc + 0];
        float Lpc1 = Lpc[indexLpc + 1];
        float Lpc2 = Lpc[indexLpc + 2];
        float Lpc3 = Lpc[indexLpc + 3];
        float Lpc4 = Lpc[indexLpc + 4];
        float Lpc5 = Lpc[indexLpc + 5];
        float Lpc6 = Lpc[indexLpc + 6];
        float Lpc7 = Lpc[indexLpc + 7];
        float Lpc8 = Lpc[indexLpc + 8];
        float Lpc9 = Lpc[indexLpc + 9];
        int iEnd = 60 + indexSig;
        for(int i = indexSig; i < iEnd; i++)
        {
            float SigFilt = signal[i] + Lpc0 * iir[indexIir - 0] + Lpc1 * iir[indexIir - 1] + Lpc2 * iir[indexIir - 2] + Lpc3 * iir[indexIir - 3] + Lpc4 * iir[indexIir - 4] + Lpc5 * iir[indexIir - 5] + Lpc6 * iir[indexIir - 6] + Lpc7 * iir[indexIir - 7] + Lpc8 * iir[indexIir - 8] + Lpc9 * iir[indexIir - 9];
            signal[i] = SigFilt;
            iir[++indexIir] = SigFilt;
        }

    }

    private final float Spf(float SyntSig[], int idxSyntSig, float Lpc[], int indexLpc, float fir[], int idxFir, float iir[], 
            int idxIir)
    {
        float FirCoef[] = new float[10];
        float IirCoef[] = new float[10];
        float pole = 1.0F;
        float zero = 1.0F;
        for(int i = 0; i < 10; i++)
        {
            IirCoef[i] = (pole *= 0.75F) * Lpc[i + indexLpc];
            FirCoef[i] = (zero *= 0.65F) * Lpc[i + indexLpc];
        }

        float corr = 0.0F;
        float enr = 0.0F;
        enr = SyntSig[idxSyntSig] * SyntSig[idxSyntSig];
        int loopStart = idxSyntSig;
        int loopEnd = loopStart + 60;
        for(int i = loopStart + 1; i < loopEnd; i++)
        {
            float tmpValue = SyntSig[i];
            corr += tmpValue * SyntSig[i - 1];
            enr += tmpValue * tmpValue;
        }

        float TmpPark;
        if((double)enr != 0.0D)
            TmpPark = corr / enr;
        else
            TmpPark = 0.0F;
        float energy = enr;
        decoderData.Park = 0.75F * decoderData.Park + 0.25F * TmpPark;
        float IirCoef0 = IirCoef[0];
        float IirCoef1 = IirCoef[1];
        float IirCoef2 = IirCoef[2];
        float IirCoef3 = IirCoef[3];
        float IirCoef4 = IirCoef[4];
        float IirCoef5 = IirCoef[5];
        float IirCoef6 = IirCoef[6];
        float IirCoef7 = IirCoef[7];
        float IirCoef8 = IirCoef[8];
        float IirCoef9 = IirCoef[9];
        float FirCoef0 = FirCoef[0];
        float FirCoef1 = FirCoef[1];
        float FirCoef2 = FirCoef[2];
        float FirCoef3 = FirCoef[3];
        float FirCoef4 = FirCoef[4];
        float FirCoef5 = FirCoef[5];
        float FirCoef6 = FirCoef[6];
        float FirCoef7 = FirCoef[7];
        float FirCoef8 = FirCoef[8];
        float FirCoef9 = FirCoef[9];
        float PostFilteredEnergy = 0.0F;
        int loopEnd = idxSyntSig + 60;
        float localPark = decoderData.Park;
        for(int i = idxSyntSig; i < loopEnd; i++)
        {
            float FiltSig = (((((((((((((((((((SyntSig[i] + IirCoef0 * iir[idxIir]) - FirCoef0 * fir[idxFir]) + IirCoef1 * iir[idxIir - 1]) - FirCoef1 * fir[idxFir - 1]) + IirCoef2 * iir[idxIir - 2]) - FirCoef2 * fir[idxFir - 2]) + IirCoef3 * iir[idxIir - 3]) - FirCoef3 * fir[idxFir - 3]) + IirCoef4 * iir[idxIir - 4]) - FirCoef4 * fir[idxFir - 4]) + IirCoef5 * iir[idxIir - 5]) - FirCoef5 * fir[idxFir - 5]) + IirCoef6 * iir[idxIir - 6]) - FirCoef6 * fir[idxFir - 6]) + IirCoef7 * iir[idxIir - 7]) - FirCoef7 * fir[idxFir - 7]) + IirCoef8 * iir[idxIir - 8]) - FirCoef8 * fir[idxFir - 8]) + IirCoef9 * iir[idxIir - 9]) - FirCoef9 * fir[idxFir - 9];
            fir[++idxFir] = SyntSig[i];
            iir[++idxIir] = FiltSig;
            float tmp;
            SyntSig[i] = tmp = FiltSig - 0.25F * iir[idxIir - 1] * localPark;
            PostFilteredEnergy += tmp * tmp;
        }

        if((double)PostFilteredEnergy != 0.0D)
            return (float)Math.sqrt(energy / PostFilteredEnergy);
        else
            return 1.0F;
    }

    private final void Line_Unpk(byte Vinp[], int indexInp, int Ftyp[], short Crc, int flag[], LINEDEF Line)
    {
        int BitStream[] = Line_Unpk_BitStream;
        short Bound_AcGn = 0;
        int index[] = Line_Unpk_index;
        index[0] = 0;
        Line.Crc = Crc;
        flag[0] = 0;
        if(Crc != 0)
            return;
        for(int i = 0; i < 192; i++)
            BitStream[i] = Vinp[indexInp + (i >> 3)] >> (i & 7) & 1;

        short frame_info = (short)Ser2Par(BitStream, index, 2);
        if(frame_info == 3 || frame_info == 2)
        {
            Line.Crc = 1;
            flag[0] = 5;
            return;
        }
        if(frame_info == 3)
        {
            Ftyp[0] = 0;
            Line.LspId = 0;
            return;
        }
        Line.LspId = Ser2Par(BitStream, index, 24);
        if(frame_info == 2)
        {
            Line.Sfs[0].Mamp = (short)Ser2Par(BitStream, index, 6);
            Ftyp[0] = 2;
            return;
        }
        Ftyp[0] = 1;
        int Temp = Ser2Par(BitStream, index, 7);
        if(Temp <= 123)
        {
            Line.Olp[0] = (short)Temp + 18;
        } else
        {
            flag[0] = 3;
            Line.Crc = 1;
            return;
        }
        Line.Sfs[1].AcLg = (short)Ser2Par(BitStream, index, 2);
        Temp = Ser2Par(BitStream, index, 7);
        if(Temp <= 123)
        {
            Line.Olp[1] = (short)Temp + 18;
        } else
        {
            flag[0] = 3;
            Line.Crc = 1;
            return;
        }
        Line.Sfs[3].AcLg = (short)Ser2Par(BitStream, index, 2);
        Line.Sfs[0].AcLg = 1;
        Line.Sfs[2].AcLg = 1;
        for(int i = 0; i < 4; i++)
        {
            Temp = Ser2Par(BitStream, index, 12);
            Line.Sfs[i].Tran = 0;
            Bound_AcGn = 170;
            if(decoderData.WrkRate == 0 && Line.Olp[i >> 1] < 58)
            {
                Line.Sfs[i].Tran = (short)(Temp >> 11);
                Temp = (int)((long)Temp & 2047L);
                Bound_AcGn = 85;
            }
            Line.Sfs[i].AcGn = (short)(Temp / 24);
            if(Line.Sfs[i].AcGn < Bound_AcGn)
            {
                Line.Sfs[i].Mamp = (short)(Temp % 24);
            } else
            {
                flag[0] = 3;
                Line.Crc = 1;
                return;
            }
        }

        for(int i = 0; i < 4; i++)
            Line.Sfs[i].Grid = BitStream[index[0]++];

        if(decoderData.WrkRate == 0)
        {
            if(frame_info != 0)
            {
                flag[0] = 2;
                Line.Crc = 1;
                return;
            }
            index[0]++;
            Temp = Ser2Par(BitStream, index, 13);
            Line.Sfs[0].Ppos = Temp / 90 / 9;
            Line.Sfs[1].Ppos = (Temp / 90) % 9;
            Line.Sfs[2].Ppos = (Temp % 90) / 9;
            Line.Sfs[3].Ppos = Temp % 90 % 9;
            Line.Sfs[0].Ppos = (Line.Sfs[0].Ppos << 16) + Ser2Par(BitStream, index, 16);
            Line.Sfs[1].Ppos = (Line.Sfs[1].Ppos << 14) + Ser2Par(BitStream, index, 14);
            Line.Sfs[2].Ppos = (Line.Sfs[2].Ppos << 16) + Ser2Par(BitStream, index, 16);
            Line.Sfs[3].Ppos = (Line.Sfs[3].Ppos << 14) + Ser2Par(BitStream, index, 14);
            Line.Sfs[0].Pamp = (short)Ser2Par(BitStream, index, 6);
            Line.Sfs[1].Pamp = (short)Ser2Par(BitStream, index, 5);
            Line.Sfs[2].Pamp = (short)Ser2Par(BitStream, index, 6);
            Line.Sfs[3].Pamp = (short)Ser2Par(BitStream, index, 5);
        } else
        {
            flag[0] = 2;
            Line.Crc = 1;
            return;
        }
    }

    private final int Ser2Par(int Pnt[], int idxPnt[], int Count)
    {
        int Rez = 0;
        int index = idxPnt[0];
        for(int i = 0; i < Count; i++)
            Rez += Pnt[index++] << i;

        idxPnt[0] = index;
        return Rez;
    }

    private final void Gen_Trn(float DstBuf[], float SrcBuf[], int index, int period)
    {
        float TmpBuf[] = Gen_Trn_TmpBuf;
        int k = period;
        System.arraycopy(SrcBuf, index, TmpBuf, 0, 60);
        System.arraycopy(SrcBuf, index, DstBuf, index, 60);
        for(k = period; k < 60; k += period)
        {
            for(int i = k; i < 60; i++)
                DstBuf[i + index] += TmpBuf[i - k];

        }

    }

    private final void Get_Rez(float ResBuf[], float PrevExc[], int idxPrevExc, int Lag)
    {
        int idxTemp = (idxPrevExc + 145) - Lag - 2 - 3;
        if(idxTemp >= 0)
        {
            ResBuf[0] = PrevExc[idxTemp];
            ResBuf[1] = PrevExc[idxTemp + 1];
            ResBuf[2] = PrevExc[idxTemp + 2];
        }
        ResBuf[3] = PrevExc[idxTemp + 3];
        ResBuf[4] = PrevExc[idxTemp + 4];
        int MaxHarm = 61 / Lag;
        int index = 5;
        idxTemp = (idxPrevExc + 145) - Lag;
        int loopEnd = idxTemp + Lag;
        for(int i = 0; i < MaxHarm; i++)
        {
            for(int j = idxTemp; j < loopEnd;)
            {
                ResBuf[index] = PrevExc[j];
                j++;
                index++;
            }

        }

        int size = (62 - index) + 2 + 3;
        idxTemp = (idxPrevExc + 145) - Lag;
        loopEnd = idxTemp + size;
        for(int j = idxTemp; j < loopEnd;)
        {
            ResBuf[index] = PrevExc[j];
            j++;
            index++;
        }

    }

    private final void Decod_Acbk(float CurExc[], float PrevExc[], int idxPrevExc, int pitch, int Lid, int Gid, int WrkRate)
    {
        float RezBuf[] = Decod_Acbk_RezBuf;
        int idxsPnt = 0;
        int idxRezBufPointer = 3;
        Get_Rez(RezBuf, PrevExc, idxPrevExc, (pitch - 1) + Lid);
        float RezBufPointer[] = RezBuf;
        int i = 0;
        if(decoderData.WrkRate == 0)
        {
            if(pitch >= 58)
                i = 1;
        } else
        {
            i = 1;
        }
        float sPnt[] = AcbkGainTablePtr[i];
        idxsPnt += Gid * 20;
        float sPnt0 = sPnt[idxsPnt + 0];
        float sPnt1 = sPnt[idxsPnt + 1];
        float sPnt2 = sPnt[idxsPnt + 2];
        float sPnt3 = sPnt[idxsPnt + 3];
        float sPnt4 = sPnt[idxsPnt + 4];
        for(i = 0; i < 60; i++)
            CurExc[i] = RezBufPointer[i + 3 + 0] * sPnt0 + RezBufPointer[i + 3 + 1] * sPnt1 + RezBufPointer[i + 3 + 2] * sPnt2 + RezBufPointer[i + 3 + 3] * sPnt3 + RezBufPointer[i + 3 + 4] * sPnt4;

    }

    private final void Lsp_Inq(float Lsp[], float PrevLsp[], int packed_indexes, int packet_loss)
    {
        float stability_band;
        float prediction_gain;
        if(packet_loss == 0)
        {
            stability_band = 2.0F;
            prediction_gain = 0.375F;
        } else
        {
            packed_indexes = 0;
            stability_band = 4F;
            prediction_gain = 0.71875F;
        }
        for(int i = 2; i >= 0; i--)
        {
            int index = packed_indexes & 0xff;
            packed_indexes >>= 8;
            float LspQntPnt[] = fBandQntTable[i];
            for(int j = 0; j < fBandInfoTable[i][1]; j++)
                Lsp[fBandInfoTable[i][0] + j] = LspQntPnt[index * fBandInfoTable[i][1] + j];

        }

        for(int j = 0; j < 10; j++)
        {
            Lsp[j] += (PrevLsp[j] - fLspDcTable[j]) * prediction_gain;
            Lsp[j] += fLspDcTable[j];
        }

        for(int i = 0; i < 10; i++)
        {
            if(Lsp[0] < 3F)
                Lsp[0] = 3F;
            if(Lsp[9] > 252F)
                Lsp[9] = 252F;
            for(int j = 1; j < 10; j++)
            {
                float bandwidth = (stability_band + Lsp[j - 1]) - Lsp[j];
                if(bandwidth > 0.0F)
                {
                    bandwidth *= 0.5F;
                    Lsp[j - 1] -= bandwidth;
                    Lsp[j] += bandwidth;
                }
            }

            boolean stability_test = false;
            for(int j = 1; j < 10; j++)
                if((double)(Lsp[j] - Lsp[j - 1]) < (double)stability_band - 0.03125D)
                    stability_test = true;

            if(!stability_test)
                break;
        }

    }

    private final void Lsp_Int(float QntLpc[], float CurrLsp[], float PrevLsp[])
    {
        for(int i = 0; i < 4; i++)
        {
            float weight1 = (float)(3 - i) * 0.25F;
            float weight2 = 1.0F - weight1;
            for(int j = 0; j < 10; j++)
                lspParamLsp[j] = weight1 * PrevLsp[j] + weight2 * CurrLsp[j];

            LsptoA();
            System.arraycopy(lspParamLsp, 0, QntLpc, 10 * i, 10);
        }

        System.arraycopy(CurrLsp, 0, PrevLsp, 0, 10);
    }

    private final void LsptoA()
    {
        float P[] = lspArrayP;
        float Q[] = lspArrayQ;
        float Lsp[] = lspParamLsp;
        for(int i = 0; i < 10; i++)
        {
            int j = (int)Lsp[i];
            float cos1 = CosFunction[j];
            float cos2 = CosFunction[j + 1];
            Lsp[i] = ((Lsp[i] - (float)j) * cos2 + ((float)(j + 1) - Lsp[i]) * cos1) * -2F;
        }

        P[0] = 1.0F;
        P[1] = Lsp[0] + Lsp[2];
        P[2] = 2.0F + Lsp[0] * Lsp[2];
        Q[0] = 1.0F;
        Q[1] = Lsp[1] + Lsp[3];
        Q[2] = 2.0F + Lsp[1] * Lsp[3];
        for(int i = 2; i < 5; i++)
        {
            P[i + 1] = Lsp[2 * i + 0] * P[i] + 2.0F * P[i - 1];
            Q[i + 1] = Lsp[2 * i + 1] * Q[i] + 2.0F * Q[i - 1];
            for(int j = i; j >= 2; j--)
            {
                P[j] += Lsp[2 * i + 0] * P[j - 1] + P[j - 2];
                Q[j] += Lsp[2 * i + 1] * Q[j - 1] + Q[j - 2];
            }

            P[1] += Lsp[2 * i + 0];
            Q[1] += Lsp[2 * i + 1];
        }

        for(int i = 0; i < 5; i++)
        {
            Lsp[i] = -(((P[i] + P[i + 1]) - Q[i]) + Q[i + 1]) * 0.5F;
            Lsp[9 - i] = -((P[i] + P[i + 1] + Q[i]) - Q[i + 1]) * 0.5F;
        }

    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997, 1998.";
    private DecoderData decoderData;
    private final float decode_OutputBuffer[] = new float[240];
    private final float decode_QntLpc[] = new float[40];
    private final float decode_AcbkCont[] = new float[60];
    private final float decode_LspVect[] = new float[10];
    private final LINEDEF decode_Line = new LINEDEF();
    private final PFDEF decode_Pf[] = {
        new PFDEF(), new PFDEF(), new PFDEF(), new PFDEF()
    };
    private final int decode_flag[] = new int[1];
    private final int decode_Ftyp[] = new int[1];
    private final float decode_SyntIir[] = new float[250];
    private final float decode_PostFir[] = new float[250];
    private final float decode_PostIir[] = new float[250];
    private final int Line_Unpk_index[] = new int[1];
    private final int Line_Unpk_BitStream[] = new int[192];
    private final float Gen_Trn_TmpBuf[] = new float[60];
    float Decod_Acbk_RezBuf[];
    private final float lspArrayP[] = new float[6];
    private final float lspArrayQ[] = new float[6];
    private final float lspParamLsp[] = new float[10];
    private static final float fLspDcTable[];
    private static final float fBand0Tb8[];
    private static final float fBand1Tb8[];
    private static final float fBand2Tb8[];
    private static final float fBandQntTable[][];
    private static final float fAcbkGainTable085[];
    private static final float fAcbkGainTable170[];
    private static final float AcbkGainTablePtr[][];
    private static final float FcbkGainTable[];
    private static final float CosFunction[];
    private static final int CombinatorialTable[][];
    private static final int fBandInfoTable[][] = {
        {
            0, 3
        }, {
            3, 3
        }, {
            6, 4
        }
    };
    private static final int MaxPosTable[] = {
        0x90f6f, 0x22caa, 0x90f6f, 0x22caa
    };
    private static final boolean INCLUDE_POST_FILTER = true;
    private static final int Rate63 = 0;
    private static final short CRC = 0;
    private static final int LpcOrder = 10;
    private static final int SubFrames = 4;
    private static final int Frame = 240;
    private static final int LpcFrame = 180;
    private static final int SubFrLen = 60;
    private static final int LspQntBands = 3;
    private static final int LspCbSize = 256;
    private static final int LspCbBits = 8;
    private static final float BandExpFactor = 0.994F;
    private static final float LspPredictor = 0.375F;
    private static final float LspPredictor2 = 0.71875F;
    private static final int ClPitchOrd = 5;
    private static final int PITCH_INDEX = 2;
    private static final int Pstep = 1;
    private static final int PitchMin = 18;
    private static final int PitchMax = 145;
    private static final int HIGH_RATE_BITSTREAM = 0;
    private static final int LOW_RATE_BITSTREAM = 1;
    private static final int SID_BITSTREAM = 2;
    private static final int UNTRANSMITTED_BITSTREAM = 3;
    private static final int G723_OK = 0;
    private static final int G723_ERR = 1;
    private static final int G723_RATE_MISMATCH = 2;
    private static final int G723_ILLEGAL_BITSTREAM = 3;
    private static final int G723_ILLEGAL_PARAMETER = 4;
    private static final int G723_VAD_BITSTREAM = 5;
    private static final int SILENCE_FRAME = 0;
    private static final int VOICE_FRAME = 1;
    private static final int SID_FRAME = 2;
    private static final int NumOfGainLev = 24;
    private static final int MaxPulseNum = 6;
    private static final int Sgrid = 2;
    private static final int DECODER_OUTPUT_SIZE = 240;
    private static final int NUM_OF_BLOCKS = 4;
    private static final int ErrMaxNum = 3;
    private static final float ALPHA = 0.0625F;
    private static final float FLT_MIN = 1.175494E-038F;
    private static final float SHRT_MIN = -32767F;
    private static final float SHRT_MAX = 32767F;

    static 
    {
        fLspDcTable = G723Tables.fLspDcTable;
        fBand0Tb8 = G723Tables.fBand0Tb8;
        fBand1Tb8 = G723Tables.fBand1Tb8;
        fBand2Tb8 = G723Tables.fBand2Tb8;
        fBandQntTable = (new float[][] {
            fBand0Tb8, fBand1Tb8, fBand2Tb8
        });
        fAcbkGainTable085 = G723Tables.fAcbkGainTable085;
        fAcbkGainTable170 = G723Tables.fAcbkGainTable170;
        AcbkGainTablePtr = (new float[][] {
            fAcbkGainTable085, fAcbkGainTable170
        });
        FcbkGainTable = G723Tables.FcbkGainTable;
        CosFunction = G723Tables.CosFunction;
        CombinatorialTable = G723Tables.CombinatorialTable;
    }
}
