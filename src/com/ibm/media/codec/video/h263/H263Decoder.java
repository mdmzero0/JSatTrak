// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   H263Decoder.java

package com.ibm.media.codec.video.h263;

import java.awt.Point;
import java.io.PrintStream;

// Referenced classes of package com.ibm.media.codec.video.h263:
//            ReadStream, FrameBuffer, H263RtpPayloadParser

public class H263Decoder extends ReadStream
{

    final void init()
    {
        SourceFormat = 9;
    }

    public H263Decoder(boolean standardOnly)
    {
        BlockPtr = new int[6][];
        BlockOffset = new int[6];
        foundPSC = false;
        prevTimeStamp = 0L;
        standardH263Only = standardOnly;
        SourceFormat = 9;
        foundPSC = false;
        TemporalReference = 0;
        LastTemporalReference = 0;
        BlockData = new int[6][64];
        FrameWidthBlock = new int[6];
        MotVectCurrGOB = new Point[88];
        MotVectPrevGOB = new Point[88];
        for(int i = 0; i < 88; i++)
        {
            MotVectCurrGOB[i] = new Point(0, 0);
            MotVectPrevGOB[i] = new Point(0, 0);
        }

        MBtypeCurrGOB = new int[22];
        MBtypePrevGOB = new int[22];
    }

    private final int ChangeFormat(int NewFormat, int UnrestrictedMV)
    {
        switch(NewFormat)
        {
        case 1: // '\001'
            FrameWidth = 128;
            FrameHeight = 96;
            break;

        case 2: // '\002'
            FrameWidth = 176;
            FrameHeight = 144;
            break;

        case 3: // '\003'
            FrameWidth = 352;
            FrameHeight = 288;
            break;

        case 4: // '\004'
        case 5: // '\005'
        case 6: // '\006'
        default:
            return 2;

        case 7: // '\007'
            break;
        }
        GOBperFrame = FrameHeight >> 4;
        MBperGOB = FrameWidth >> 4;
        for(int i = 0; i < GOBperFrame; i++)
        {
            LumiOffset[i] = (FrameWidth << 4) * i;
            CromOffset[i] = (FrameWidth << 2) * i;
            xLumiOffset[i] = 16 + (32 + FrameWidth << 4) * (i + 1);
            xCromOffset[i] = 8 + (16 + (FrameWidth >> 1) << 3) * (i + 1);
        }

        FrameWidthDiv2 = FrameWidth >> 1;
        FrameWidthx8 = FrameWidth << 3;
        CurrentFrame = new FrameBuffer(FrameWidth, FrameHeight);
        PreviousFrame = new FrameBuffer(FrameWidth, FrameHeight);
        FrameWidthBlock[0] = FrameWidth;
        FrameWidthBlock[1] = FrameWidth;
        FrameWidthBlock[2] = FrameWidth;
        FrameWidthBlock[3] = FrameWidth;
        FrameWidthBlock[4] = FrameWidthDiv2;
        FrameWidthBlock[5] = FrameWidthDiv2;
        if(UnrestrictedMV == 1)
        {
            xFrameWidth = FrameWidth + 32;
            xFrameWidthDiv2 = xFrameWidth >> 1;
            xPrevFrame = new FrameBuffer(FrameWidth + 32, FrameHeight + 32);
        } else
        {
            xFrameWidth = FrameWidth;
            xFrameWidthDiv2 = xFrameWidth >> 1;
            xPrevFrame = null;
        }
        SourceFormat = NewFormat;
        return 0;
    }

    public final int DecodeRtpPacket(byte inputBuffer[], int inputOffset, int inputLength, byte packetHeader[], int packetOffset, long timeStamp)
    {
        int mode = H263RtpPayloadParser.getMode(packetHeader, packetOffset);
        int startBit = H263RtpPayloadParser.getStartBit(packetHeader, packetOffset);
        int endBit = H263RtpPayloadParser.getEndBit(packetHeader, packetOffset);
        int src = H263RtpPayloadParser.getSRC(packetHeader, packetOffset);
        int rc = 0;
        super.rdptr = inputOffset;
        if(mode == 0)
        {
            boolean prevdone = false;
            if(!frameDone && foundPSC && timeStamp != prevTimeStamp && timeStamp != 0L)
            {
                rtpTemporalReference = H263RtpPayloadParser.getTemporalReference(packetHeader, packetOffset);
                if(rtpTemporalReference != 0)
                    TemporalReference = rtpTemporalReference;
                frameDone = true;
                foundPSC = false;
                prevdone = true;
            }
            prevTimeStamp = timeStamp;
            while(super.rdptr < inputLength + inputOffset) 
                try
                {
                    rc = DecodeGobs(inputBuffer, super.rdptr);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    System.out.println("[H263Decodeer::decodeRtpPacket] returning H263_RC_PICTURE_FORMAT_NOT_SUPPORTED");
                    return 2;
                }
            if(prevdone)
            {
                prevdone = false;
                return 1;
            } else
            {
                return rc;
            }
        }
        return mode != 1 ? 2 : 2;
    }

    public final int DecodePicture(byte ds_rdbfr[], int ds_rdbfr_offset, boolean EntireFrame)
    {
        int rc = 0;
        if(EntireFrame)
        {
            frameDone = false;
            super.rdptr = ds_rdbfr_offset;
            for(; rc == 0; rc = DecodeGobs(ds_rdbfr, super.rdptr));
        } else
        {
            rc = DecodeGobs(ds_rdbfr, ds_rdbfr_offset);
        }
        return rc;
    }

    public final int DecodeGobs(byte ds_rdbfr[], int ds_rdbfr_offset)
    {
        int rc = 0;
        setInBuf(ds_rdbfr, ds_rdbfr_offset);
        GetNextStartCode();
        int startCode = getBits(22);
        if(startCode != 32 && startCode != 63 && !foundPSC)
            return 3;
        if(startCode == 32)
            foundPSC = true;
        else
        if(startCode == 63)
            return 0;
        if(startCode == 63)
        {
            GetNextStartCode();
            startCode = getBits(22);
        }
        if(startCode != 32 && !foundPSC)
            return 0;
        if(startCode == 32)
        {
            foundPSC = true;
            int tmpTemporalReference = getBits(8);
            TypeInformation = getBits(8);
            HeaderPlus = TypeInformation & 7;
            int srcFormat = HeaderPlus;
            if(HeaderPlus == 7)
            {
                rc = ParseHeaderPlus();
                if(rc == 2)
                    return rc;
            } else
            {
                TypeInformation = getBits(5);
                if((TypeInformation & 7) != 0)
                    return 2;
                CodingType = TypeInformation >> 4 & 1;
                UnrestrictedMV = (TypeInformation & 8) >> 3;
            }
            LastTemporalReference = TemporalReference;
            TemporalReference = tmpTemporalReference;
            Quantizer = getBits(5);
            if(HeaderPlus != 7)
            {
                CPM = getBits(1);
                if(CPM != 0)
                    getBits(2);
            }
            int PEI = getBits(1);
            if(PEI == 1)
                return 2;
            if(srcFormat != SourceFormat)
            {
                rc = ChangeFormat(srcFormat, UnrestrictedMV);
                if(rc == 2)
                    return rc;
            }
            FrameBuffer tmpFrame = CurrentFrame;
            CurrentFrame = PreviousFrame;
            PreviousFrame = tmpFrame;
            BlockPtr[0] = CurrentFrame.Y;
            BlockPtr[1] = CurrentFrame.Y;
            BlockPtr[2] = CurrentFrame.Y;
            BlockPtr[3] = CurrentFrame.Y;
            BlockPtr[4] = CurrentFrame.Cb;
            BlockPtr[5] = CurrentFrame.Cr;
            if(CodingType == 0)
            {
                GroupNumber = 0;
                GetIntraPictMB();
            } else
            {
                if(UnrestrictedMV == 1)
                {
                    xFrameWidth = FrameWidth + 32;
                    xFrameWidthDiv2 = xFrameWidth >> 1;
                    CopyExtendedFrame(PreviousFrame.Y, xPrevFrame.Y, FrameWidth, FrameHeight, xFrameWidth, 16);
                    CopyExtendedFrame(PreviousFrame.Cb, xPrevFrame.Cb, FrameWidthDiv2, FrameHeight >> 1, xFrameWidthDiv2, 8);
                    CopyExtendedFrame(PreviousFrame.Cr, xPrevFrame.Cr, FrameWidthDiv2, FrameHeight >> 1, xFrameWidthDiv2, 8);
                }
                GroupNumber = 0;
                GetInterPictMB();
            }
        } else
        if(startCode >= 33 && startCode <= 62)
        {
            int tmp = startCode & 0x1f;
            if(tmp != GroupNumber)
                GroupNumber = tmp;
            if(CPM != 0)
                skipBits(2);
            GOB_FrameID = getBits(2);
            Quantizer = getBits(5);
            if(CodingType == 0)
                GetIntraPictMB();
            else
                GetInterPictMB();
        } else
        {
            return 2;
        }
        if(GroupNumber == GOBperFrame)
        {
            frameDone = true;
            foundPSC = false;
            if(DeblockingFilter == 1)
                EdgeFilter();
            return 1;
        } else
        {
            frameDone = false;
            return 0;
        }
    }

    private final int ParseHeaderPlus()
    {
        int srcFormat = 0;
        int UFEP = getBits(3);
        int tmpBits;
        if(UFEP == 1)
        {
            srcFormat = getBits(3);
            tmpBits = getBits(1);
            if(tmpBits == 1)
                return 2;
            UnrestrictedMV = getBits(1);
            tmpBits = getBits(4);
            if(tmpBits > 1)
                return 2;
            DeblockingFilter = tmpBits & 1;
            tmpBits = getBits(9);
            if(tmpBits != 8)
                return 2;
        }
        CodingType = getBits(3);
        if(CodingType > 1)
            return 2;
        tmpBits = getBits(6);
        if(tmpBits != 1)
            return 2;
        CPM = getBits(1);
        if(CPM != 0)
            getBits(2);
        if(srcFormat == 6)
        {
            tmpBits = getBits(4);
            if(tmpBits != 2)
                return 2;
            int PDI = getBits(9);
            tmpBits = PDI + 1 << 2;
            if(tmpBits != FrameWidth)
            {
                SourceFormat = 9;
                FrameWidth = tmpBits;
            }
            tmpBits = getBits(1);
            PDI = getBits(9);
            tmpBits = PDI << 2;
            if(tmpBits != FrameHeight)
            {
                SourceFormat = 9;
                FrameHeight = tmpBits;
            }
        }
        return 1;
    }

    private final void EdgeFilter()
    {
        HorizEdgeFilter(CurrentFrame.Y, 0);
        VertEdgeFilter(CurrentFrame.Y, 0);
        HorizEdgeFilter(CurrentFrame.Cb, 1);
        VertEdgeFilter(CurrentFrame.Cb, 1);
        HorizEdgeFilter(CurrentFrame.Cr, 1);
        VertEdgeFilter(CurrentFrame.Cr, 1);
    }

    private final void HorizEdgeFilter(int rec[], int divisor)
    {
        int width = FrameWidth >> divisor;
        int height = FrameHeight >> divisor;
        for(int j = 8; j < height; j += 8)
        {
            int mbr = j >> 4 - divisor;
            int mbr_above = j + (divisor - 1 << 3) >> 4;
            int index = j * width;
            for(int i = 0; i < width;)
            {
                int mbc = i >> 4 - divisor;
                if(CodedMap[(mbr + 1) * (mbc + 1)] > 0 || CodedMap[(mbr_above + 1) * (mbc + 1)] > 0)
                {
                    int delta = ((rec[index - (width << 1)] - (rec[index - width] << 2)) + (rec[index] << 2)) - rec[index + width] >> 3;
                    int d1 = (delta >= 0 ? 1 : -1) * Math.max(0, Math.abs(delta) - Math.max(0, Math.abs(delta) - STRENGTH[Quantizer - 1] << 1));
                    int d2 = Math.min(Math.abs(d1 >> 1), Math.max(-Math.abs(d1 >> 1), rec[index - (width << 1)] - rec[index + width] >> 2));
                    rec[index + width] = rec[index + width] + d2;
                    rec[index] = DfiltClip[(rec[index] - d1) + 128];
                    rec[index - width] = DfiltClip[rec[index - width] + d1 + 128];
                    rec[index - (width << 1)] = rec[index - (width << 1)] - d2;
                }
                i++;
                index++;
            }

        }

    }

    private final void VertEdgeFilter(int rec[], int divisor)
    {
        int width = FrameWidth >> divisor;
        int height = FrameHeight >> divisor;
        for(int i = 8; i < width; i += 8)
        {
            int mbc = i >> 4 - divisor;
            int mbc_left = i + (divisor - 1 << 3) >> 4;
            int index = i;
            for(int j = 0; j < height;)
            {
                int mbr = j >> 4 - divisor;
                if(CodedMap[(mbr + 1) * (mbc + 1)] > 0 || CodedMap[(mbr + 1) * (mbc_left + 1)] > 0)
                {
                    int delta = ((rec[index - 2] - (rec[index - 1] << 2)) + (rec[index] << 2)) - rec[index + 1] >> 3;
                    int d1 = (delta >= 0 ? 1 : -1) * Math.max(0, Math.abs(delta) - Math.max(0, Math.abs(delta) - STRENGTH[Quantizer - 1] << 1));
                    int d2 = Math.min(Math.abs(d1 >> 1), Math.max(-Math.abs(d1 >> 1), rec[index - 2] - rec[index + 1] >> 2));
                    rec[index + 1] = rec[index + 1] + d2;
                    rec[index] = DfiltClip[(rec[index] - d1) + 128];
                    rec[index - 1] = DfiltClip[rec[index - 1] + d1 + 128];
                    rec[index - 2] = rec[index - 2] - d2;
                }
                j++;
                index += width;
            }

        }

    }

    private final void GetIntraPictMB()
    {
        int clipQ[] = clipQ_tab;
        do
        {
            FirstMBinGOB = GroupNumber * MBperGOB;
            CurrentLumiOffset = LumiOffset[GroupNumber];
            CurrentCromOffset = CromOffset[GroupNumber];
            MB_address = FirstMBinGOB;
            for(int MBinNextGOB = FirstMBinGOB + MBperGOB; MB_address < MBinNextGOB; MB_address++)
            {
                int MCBPC;
                do
                    MCBPC = GetIntraMCBPC_VLC();
                while(MCBPC == -1);
                int MBtype = MCBPC >> 4;
                int CBPY = GetCBPY_VLC() >> 4;
                if(MBtype == 4)
                {
                    Quantizer = clipQ[Quantizer + dQuant[nextBits(2)]];
                    skipBits(2);
                }
                CodedMap[MB_address] = 2;
                BlockOffset[0] = CurrentLumiOffset;
                BlockOffset[1] = BlockOffset[0] + 8;
                BlockOffset[2] = BlockOffset[0] + FrameWidthx8;
                BlockOffset[3] = BlockOffset[2] + 8;
                BlockOffset[4] = CurrentCromOffset;
                BlockOffset[5] = CurrentCromOffset;
                int CBP = CBPY << 2 | MCBPC & 3;
                for(int cnt = 0; cnt < 6; cnt++)
                {
                    int tempDC = getBits(8);
                    if(tempDC == 255)
                        tempDC = 128;
                    BlockData[cnt][0] = tempDC << 12;
                    if((CBP & 0x20) != 0)
                        GetCoefficients(1, cnt);
                    idct8x8(BlockData[cnt], BlockPtr[cnt], BlockOffset[cnt], FrameWidthBlock[cnt], 1);
                    CBP <<= 1;
                }

                CurrentLumiOffset += 16;
                CurrentCromOffset += 8;
            }

            GroupNumber++;
        } while(StartCodeFound() == 0 && GroupNumber < GOBperFrame);
    }

    private final void GetInterPictMB()
    {
        int MCBPC = 0;
        int clipQ[] = clipQ_tab;
        HeaderInGOB = true;
        do
        {
            FirstMBinGOB = GroupNumber * MBperGOB;
            CurrentLumiOffset = LumiOffset[GroupNumber];
            CurrentCromOffset = CromOffset[GroupNumber];
            xCurrentLumiOffset = xLumiOffset[GroupNumber];
            xCurrentCromOffset = xCromOffset[GroupNumber];
            MB_address = FirstMBinGOB;
            for(int MBinNextGOB = FirstMBinGOB + MBperGOB; MB_address < MBinNextGOB; MB_address++)
            {
                MBpositionInGOB = MB_address - FirstMBinGOB;
                int COD;
                do
                {
                    COD = getBits(1);
                    if(COD != 0)
                    {
                        if(UnrestrictedMV != 0)
                        {
                            Copy16x16Pel(xPrevFrame.Y, xCurrentLumiOffset, CurrentFrame.Y, CurrentLumiOffset);
                            Copy8x8Pel(xPrevFrame.Cr, xCurrentCromOffset, CurrentFrame.Cr, CurrentCromOffset);
                            Copy8x8Pel(xPrevFrame.Cb, xCurrentCromOffset, CurrentFrame.Cb, CurrentCromOffset);
                        } else
                        {
                            Copy16x16Pel(PreviousFrame.Y, CurrentLumiOffset, CurrentFrame.Y, CurrentLumiOffset);
                            Copy8x8Pel(PreviousFrame.Cr, CurrentCromOffset, CurrentFrame.Cr, CurrentCromOffset);
                            Copy8x8Pel(PreviousFrame.Cb, CurrentCromOffset, CurrentFrame.Cb, CurrentCromOffset);
                        }
                        MotVectCurrGOB[MBpositionInGOB].x = 0;
                        MotVectCurrGOB[MBpositionInGOB].y = 0;
                        CurrentLumiOffset += 16;
                        CurrentCromOffset += 8;
                        xCurrentLumiOffset += 16;
                        xCurrentCromOffset += 8;
                        break;
                    }
                    MCBPC = GetInterMCBPC_VLC();
                } while(MCBPC == -1);
                if(COD == 0)
                {
                    int MBtype = MCBPC >> 4;
                    BlockOffset[0] = CurrentLumiOffset;
                    BlockOffset[1] = BlockOffset[0] + 8;
                    BlockOffset[2] = BlockOffset[0] + FrameWidthx8;
                    BlockOffset[3] = BlockOffset[2] + 8;
                    BlockOffset[4] = CurrentCromOffset;
                    BlockOffset[5] = CurrentCromOffset;
                    if(MBtype >= 3)
                    {
                        CodedMap[MB_address] = 2;
                        int CBPY = GetCBPY_VLC() >> 4;
                        if(MBtype == 4)
                        {
                            Quantizer = clipQ[Quantizer + dQuant[nextBits(2)]];
                            skipBits(2);
                        }
                        int CBP = CBPY << 2 | MCBPC & 3;
                        for(int cnt = 0; cnt < 6; cnt++)
                        {
                            int tempDC = getBits(8);
                            if(tempDC == 255)
                                tempDC = 128;
                            BlockData[cnt][0] = tempDC << 12;
                            if((CBP & 0x20) != 0)
                                GetCoefficients(1, cnt);
                            idct8x8(BlockData[cnt], BlockPtr[cnt], BlockOffset[cnt], FrameWidthBlock[cnt], 1);
                            CBP <<= 1;
                        }

                        MotVectCurrGOB[MBpositionInGOB].x = 0;
                        MotVectCurrGOB[MBpositionInGOB].y = 0;
                    } else
                    {
                        CodedMap[MB_address] = 1;
                        int CBPY = GetCBPY_VLC();
                        if(MBtype == 1)
                        {
                            Quantizer = clipQ[Quantizer + dQuant[nextBits(2)]];
                            skipBits(2);
                        }
                        FindMV();
                        BasePredPel();
                        int CBP = CBPY << 2 | MCBPC & 3;
                        for(int cnt = 0; cnt < 6; cnt++)
                        {
                            if((CBP & 0x20) != 0)
                            {
                                GetCoefficients(0, cnt);
                                idct8x8(BlockData[cnt], BlockPtr[cnt], BlockOffset[cnt], FrameWidthBlock[cnt], 0);
                            }
                            CBP <<= 1;
                        }

                    }
                    CurrentLumiOffset += 16;
                    CurrentCromOffset += 8;
                    xCurrentLumiOffset += 16;
                    xCurrentCromOffset += 8;
                } else
                {
                    CodedMap[MB_address] = 0;
                }
            }

            Point MotVectTemp[] = MotVectCurrGOB;
            MotVectCurrGOB = MotVectPrevGOB;
            MotVectPrevGOB = MotVectTemp;
            GroupNumber++;
            HeaderInGOB = false;
        } while(StartCodeFound() == 0 && GroupNumber < GOBperFrame);
    }

    private final void GetCoefficients(int startIndex, int cnt)
    {
        int bits = 0;
        int vlcValue = 0;
        int vlcBits = 0;
        int zigzag[] = zigzag_tab;
        int pre8x8[] = pre8x8_tab;
        int recLevel[][] = recLevel_tab;
        int tempData[] = BlockData[cnt];
        int TCOEFF10[] = TCOEFF1_tab0;
        int TCOEFF11[] = TCOEFF1_tab1;
        int TCOEFF20[] = TCOEFF2_tab0;
        int TCOEFF21[] = TCOEFF2_tab1;
        int TCOEFF30[] = TCOEFF3_tab0;
        int TCOEFF31[] = TCOEFF3_tab1;
        int i;
        for(i = startIndex; i < 64; i++)
            tempData[i] = 0;

        i = startIndex - 1;
        int last;
        do
        {
            bits = nextBits(13);
            if(bits >= 1024)
            {
                int offset = (bits >> 6) - 16;
                vlcValue = TCOEFF10[offset];
                vlcBits = TCOEFF11[offset];
            } else
            if(bits >= 256)
            {
                int offset = (bits >> 3) - 32;
                vlcValue = TCOEFF20[offset];
                vlcBits = TCOEFF21[offset];
            } else
            {
                int offset = (bits >> 1) - 8;
                vlcValue = TCOEFF30[offset];
                vlcBits = TCOEFF31[offset];
            }
            skipBits(vlcBits + 1);
            int value = vlcValue;
            int level;
            int run;
            if(value != 7167)
            {
                level = (bits >> 12 - vlcBits & 1) == 0 ? value & 0xf : -(value & 0xf);
                run = value >> 4 & 0x3f;
                last = value & 0x1000;
            } else
            {
                value = getBits(15);
                level = (value << 24) >> 24;
                run = value >> 8 & 0x3f;
                last = value & 0x4000;
            }
            i += run + 1;
            i &= 0x3f;
            int index = zigzag[i];
            if(level != 0)
            {
                int sign = level >= 0 ? 0 : 1;
                level = Math.abs(level);
                if((Quantizer & 1) != 0)
                    tempData[index] = pre8x8[index] * (sign == 0 ? recLevel[Quantizer][level] : -recLevel[Quantizer][level]);
                else
                    tempData[index] = pre8x8[index] * (sign == 0 ? recLevel[Quantizer][level] - 1 : 1 - recLevel[Quantizer][level]);
            }
        } while(last == 0);
        LastValue = i;
    }

    private final int GetIntraMCBPC_VLC()
    {
        int bits = nextBits(9);
        if(bits == 1)
        {
            skipBits(9);
            return -1;
        } else
        {
            int index = bits >> 3;
            int vlcValue = IntraMCBPC_VLC_tab0[index];
            int vlcBits = IntraMCBPC_VLC_tab1[index];
            skipBits(vlcBits);
            return vlcValue;
        }
    }

    private final int GetCBPY_VLC()
    {
        int index = nextBits(6);
        int vlcValue = CBPY_VLC_tab0[index];
        int vlcBits = CBPY_VLC_tab1[index];
        skipBits(vlcBits);
        return vlcValue;
    }

    private final int GetInterMCBPC_VLC()
    {
        int index = nextBits(9);
        int vlcValue = InterMCBPC_VLC_tab0[index];
        int vlcBits = InterMCBPC_VLC_tab1[index];
        skipBits(vlcBits);
        return vlcValue;
    }

    private final int GetMVD_VLC()
    {
        int bits = nextBits(13);
        int vlcValue;
        int vlcBits;
        if(bits >= 192)
        {
            int index = bits >> 5;
            vlcValue = MVD1_VLC_tab0[index];
            vlcBits = MVD1_VLC_tab1[index];
        } else
        {
            int index = bits;
            vlcValue = MVD2_VLC_tab0[index];
            vlcBits = MVD2_VLC_tab1[index];
        }
        skipBits(vlcBits);
        return vlcValue;
    }

    public final void CopyExtendedFrame(int src[], int dest[], int Width, int Height, int xWidth, int edge)
    {
        int imageSize = Width * Height;
        int xImageSize = xWidth * (Height + (edge << 1));
        int xStripe = xWidth * edge;
        int srcIndex = 0;
        int destIndex;
        for(destIndex = xStripe + edge; srcIndex < imageSize; destIndex += xWidth)
        {
            System.arraycopy(src, srcIndex, dest, destIndex, Width);
            srcIndex += Width;
        }

        destIndex = xStripe;
        for(srcIndex = 0; destIndex < xImageSize - xStripe; srcIndex += Width)
        {
            for(int Index = 0; Index < edge; Index++)
            {
                dest[destIndex + Index] = src[srcIndex];
                dest[destIndex + Width + edge + Index] = src[(srcIndex + Width) - 1];
            }

            destIndex += xWidth;
        }

        for(destIndex = 0; destIndex < xStripe; destIndex += xWidth)
        {
            System.arraycopy(dest, xStripe, dest, destIndex, edge);
            System.arraycopy(dest, xStripe + Width + edge, dest, destIndex + Width + edge, edge);
        }

        for(destIndex = xImageSize - xStripe; destIndex < xImageSize; destIndex += xWidth)
        {
            System.arraycopy(dest, xImageSize - xStripe - xWidth, dest, destIndex, edge);
            System.arraycopy(dest, xImageSize - xStripe - edge, dest, destIndex + Width + edge, edge);
        }

        for(destIndex = edge; destIndex < xStripe + edge; destIndex += xWidth)
        {
            System.arraycopy(dest, xStripe + edge, dest, destIndex, Width);
            System.arraycopy(dest, (xImageSize - xStripe - xWidth) + edge, dest, (destIndex + xImageSize) - xStripe, Width);
        }

    }

    private final void BasePredPel()
    {
        int x_vec = MotVectCurrGOB[MBpositionInGOB].x;
        int y_vec = MotVectCurrGOB[MBpositionInGOB].y;
        if(x_vec < -32 && MBpositionInGOB == 0)
            x_vec = -32;
        else
        if(x_vec > 32 && MBpositionInGOB == GOBperFrame - 1)
            x_vec = 32;
        HorMV = x_vec >> 1;
        Half_HorMV = x_vec & 1;
        if(y_vec < -32 && GroupNumber == 0)
            y_vec = -32;
        else
        if(y_vec > 32 && GroupNumber == GOBperFrame - 1)
            y_vec = 32;
        VerMV = y_vec >> 1;
        Half_VerMV = y_vec & 1;
        int dest[] = CurrentFrame.Y;
        int destIndex = CurrentLumiOffset;
        int src[];
        int srcIndex;
        if(UnrestrictedMV != 0)
        {
            src = xPrevFrame.Y;
            srcIndex = xCurrentLumiOffset + VerMV * xFrameWidth + HorMV;
        } else
        {
            src = PreviousFrame.Y;
            srcIndex = CurrentLumiOffset + VerMV * FrameWidth + HorMV;
        }
        if(Half_HorMV != 0 && Half_VerMV != 0)
            InterpF16x16Pel(src, srcIndex, dest, destIndex);
        else
        if(Half_HorMV != 0 || Half_VerMV != 0)
            Interp16x16Pel(src, srcIndex, dest, destIndex, Half_HorMV, Half_VerMV);
        else
            Copy16x16Pel(src, srcIndex, dest, destIndex);
        Half_HorMV |= HorMV & 1;
        HorMV >>= 1;
        Half_VerMV |= VerMV & 1;
        VerMV >>= 1;
        dest = CurrentFrame.Cr;
        destIndex = CurrentCromOffset;
        int dest1[] = CurrentFrame.Cb;
        int src1[];
        if(UnrestrictedMV != 0)
        {
            src = xPrevFrame.Cr;
            srcIndex = xCurrentCromOffset + VerMV * xFrameWidthDiv2 + HorMV;
            src1 = xPrevFrame.Cb;
        } else
        {
            src = PreviousFrame.Cr;
            srcIndex = CurrentCromOffset + VerMV * FrameWidthDiv2 + HorMV;
            src1 = PreviousFrame.Cb;
        }
        if(Half_HorMV != 0 && Half_VerMV != 0)
        {
            InterpF8x8Pel(src, srcIndex, dest, destIndex);
            InterpF8x8Pel(src1, srcIndex, dest1, destIndex);
        } else
        if(Half_HorMV != 0 || Half_VerMV != 0)
        {
            Interp8x8Pel(src, srcIndex, dest, destIndex, Half_HorMV, Half_VerMV);
            Interp8x8Pel(src1, srcIndex, dest1, destIndex, Half_HorMV, Half_VerMV);
        } else
        {
            Copy8x8Pel(src, srcIndex, dest, destIndex);
            Copy8x8Pel(src1, srcIndex, dest1, destIndex);
        }
    }

    private final void FindMV()
    {
        int MBpositionInGOB = MB_address - FirstMBinGOB;
        int HorMVD = GetMVD_VLC();
        int VerMVD = GetMVD_VLC();
        int MV1x;
        int MV1y;
        if(MBpositionInGOB == 0)
        {
            MV1x = 0;
            MV1y = 0;
        } else
        {
            MV1x = MotVectCurrGOB[MBpositionInGOB - 1].x;
            MV1y = MotVectCurrGOB[MBpositionInGOB - 1].y;
        }
        if(HeaderInGOB)
        {
            MotVectCurrGOB[MBpositionInGOB].x = CALC_MVC(HorMVD, MV1x);
            MotVectCurrGOB[MBpositionInGOB].y = CALC_MVC(VerMVD, MV1y);
            return;
        }
        int MV2x = MotVectPrevGOB[MBpositionInGOB].x;
        int MV2y = MotVectPrevGOB[MBpositionInGOB].y;
        int MV3x;
        int MV3y;
        if(MBpositionInGOB + 1 == MBperGOB)
        {
            MV3x = 0;
            MV3y = 0;
        } else
        {
            MV3x = MotVectPrevGOB[MBpositionInGOB + 1].x;
            MV3y = MotVectPrevGOB[MBpositionInGOB + 1].y;
        }
        int MVx = median(MV1x, MV2x, MV3x);
        int MVy = median(MV1y, MV2y, MV3y);
        MotVectCurrGOB[MBpositionInGOB].x = CALC_MVC(HorMVD, MVx);
        MotVectCurrGOB[MBpositionInGOB].y = CALC_MVC(VerMVD, MVy);
    }

    private final int median(int mv1, int mv2, int mv3)
    {
        return mv1 < mv2 ? mv1 < mv3 ? mv2 < mv3 ? mv2 : mv3 : mv1 : mv2 < mv3 ? mv1 < mv3 ? mv1 : mv3 : mv2;
    }

    private final int CALC_MVC(int MVd, int Pc)
    {
        return UnrestrictedMV != 0 ? Pc < 33 ? Pc > -32 ? MVd + Pc : -(-(MVd + Pc) & 0x3f) : MVd + Pc & 0x3f : (MVd + Pc + 96 & 0x3f) - 32;
    }

    private final void Copy16x16Pel(int src[], int srcIndex, int dest[], int destIndex)
    {
        int frameW = FrameWidth;
        int xframeW = xFrameWidth;
        for(int i = 0; i < 16; i++)
        {
            System.arraycopy(src, srcIndex, dest, destIndex, 16);
            destIndex += frameW;
            srcIndex += xframeW;
        }

    }

    private final void Interp16x16Pel(int src[], int srcIndex, int dest[], int destIndex, int Hor, int Ver)
    {
        int offset1 = srcIndex;
        int offset2 = srcIndex + Ver * xFrameWidth + Hor;
        int frameW = FrameWidth;
        int xframeW = xFrameWidth;
        for(int i = 0; i < 16; i++)
        {
            for(int j = 0; j < 16; j++)
                dest[destIndex + j] = src[offset1 + j] + src[offset2 + j] + 1 >> 1;

            destIndex += frameW;
            offset1 += xframeW;
            offset2 += xframeW;
        }

    }

    private final void InterpF16x16Pel(int src[], int srcIndex, int dest[], int destIndex)
    {
        int offset1 = srcIndex;
        int offset2 = srcIndex + xFrameWidth;
        int width1 = FrameWidth - 15;
        int width2 = xFrameWidth - 16;
        for(int i = 0; i < 16; i++)
        {
            int x10 = src[offset1++];
            int x11 = src[offset1++];
            int x12 = src[offset1++];
            int x13 = src[offset1++];
            int x14 = src[offset1++];
            int x15 = src[offset1++];
            int x16 = src[offset1++];
            int x17 = src[offset1++];
            int x18 = src[offset1++];
            int x19 = src[offset1++];
            int x110 = src[offset1++];
            int x111 = src[offset1++];
            int x112 = src[offset1++];
            int x113 = src[offset1++];
            int x114 = src[offset1++];
            int x115 = src[offset1++];
            int x116 = src[offset1];
            int x20 = src[offset2++];
            int x21 = src[offset2++];
            int x22 = src[offset2++];
            int x23 = src[offset2++];
            int x24 = src[offset2++];
            int x25 = src[offset2++];
            int x26 = src[offset2++];
            int x27 = src[offset2++];
            int x28 = src[offset2++];
            int x29 = src[offset2++];
            int x210 = src[offset2++];
            int x211 = src[offset2++];
            int x212 = src[offset2++];
            int x213 = src[offset2++];
            int x214 = src[offset2++];
            int x215 = src[offset2++];
            int x216 = src[offset2];
            dest[destIndex++] = x10 + x11 + x20 + x21 + 2 >> 2;
            dest[destIndex++] = x11 + x12 + x21 + x22 + 2 >> 2;
            dest[destIndex++] = x12 + x13 + x22 + x23 + 2 >> 2;
            dest[destIndex++] = x13 + x14 + x23 + x24 + 2 >> 2;
            dest[destIndex++] = x14 + x15 + x24 + x25 + 2 >> 2;
            dest[destIndex++] = x15 + x16 + x25 + x26 + 2 >> 2;
            dest[destIndex++] = x16 + x17 + x26 + x27 + 2 >> 2;
            dest[destIndex++] = x17 + x18 + x27 + x28 + 2 >> 2;
            dest[destIndex++] = x18 + x19 + x28 + x29 + 2 >> 2;
            dest[destIndex++] = x19 + x110 + x29 + x210 + 2 >> 2;
            dest[destIndex++] = x110 + x111 + x210 + x211 + 2 >> 2;
            dest[destIndex++] = x111 + x112 + x211 + x212 + 2 >> 2;
            dest[destIndex++] = x112 + x113 + x212 + x213 + 2 >> 2;
            dest[destIndex++] = x113 + x114 + x213 + x214 + 2 >> 2;
            dest[destIndex++] = x114 + x115 + x214 + x215 + 2 >> 2;
            dest[destIndex] = x115 + x116 + x215 + x216 + 2 >> 2;
            destIndex += width1;
            offset1 += width2;
            offset2 += width2;
        }

    }

    private final void Copy8x8Pel(int src[], int srcIndex, int dest[], int destIndex)
    {
        int width1 = FrameWidthDiv2;
        int width2 = xFrameWidthDiv2;
        for(int i = 0; i < 8; i++)
        {
            System.arraycopy(src, srcIndex, dest, destIndex, 8);
            destIndex += width1;
            srcIndex += width2;
        }

    }

    private final void Interp8x8Pel(int src[], int srcIndex, int dest[], int destIndex, int Hor, int Ver)
    {
        int offset1 = srcIndex;
        int offset2 = srcIndex + Ver * xFrameWidthDiv2 + Hor;
        int width1 = FrameWidthDiv2 - 8;
        int width2 = xFrameWidthDiv2 - 8;
        for(int i = 0; i < 8; i++)
        {
            for(int j = 0; j < 8; j++)
                dest[destIndex++] = src[offset1++] + src[offset2++] + 1 >> 1;

            destIndex += width1;
            offset1 += width2;
            offset2 += width2;
        }

    }

    private final void InterpF8x8Pel(int src[], int srcIndex, int dest[], int destIndex)
    {
        int offset1 = srcIndex;
        int offset2 = srcIndex + xFrameWidthDiv2;
        int width1 = FrameWidthDiv2 - 7;
        int width2 = xFrameWidthDiv2 - 8;
        for(int i = 0; i < 8; i++)
        {
            int x10 = src[offset1++];
            int x11 = src[offset1++];
            int x12 = src[offset1++];
            int x13 = src[offset1++];
            int x14 = src[offset1++];
            int x15 = src[offset1++];
            int x16 = src[offset1++];
            int x17 = src[offset1++];
            int x18 = src[offset1];
            int x20 = src[offset2++];
            int x21 = src[offset2++];
            int x22 = src[offset2++];
            int x23 = src[offset2++];
            int x24 = src[offset2++];
            int x25 = src[offset2++];
            int x26 = src[offset2++];
            int x27 = src[offset2++];
            int x28 = src[offset2];
            dest[destIndex++] = x10 + x11 + x20 + x21 + 2 >> 2;
            dest[destIndex++] = x11 + x12 + x21 + x22 + 2 >> 2;
            dest[destIndex++] = x12 + x13 + x22 + x23 + 2 >> 2;
            dest[destIndex++] = x13 + x14 + x23 + x24 + 2 >> 2;
            dest[destIndex++] = x14 + x15 + x24 + x25 + 2 >> 2;
            dest[destIndex++] = x15 + x16 + x25 + x26 + 2 >> 2;
            dest[destIndex++] = x16 + x17 + x26 + x27 + 2 >> 2;
            dest[destIndex] = x17 + x18 + x27 + x28 + 2 >> 2;
            destIndex += width1;
            offset1 += width2;
            offset2 += width2;
        }

    }

    private final void GetNextStartCode()
    {
        for(; nextBits(17) != 1; skipBits(1));
    }

    private final int StartCodeFound()
    {
        int code = nextBits(16);
        if(code == 0)
        {
            for(int count = 0; (code = nextBits(17)) != 1 && count < 7; count++)
                skipBits(1);

            return code != 1 ? 0 : 1;
        } else
        {
            return 0;
        }
    }

    private final void idct8x8(int blk[], int pixel[], int pixOffset, int width, int flag)
    {
        int i8 = 7;
        int i16 = 15;
        int i24 = 23;
        int i32 = 31;
        int i40 = 39;
        int i48 = 47;
        int i56 = 55;
        for(int i = -1; i < 7;)
        {
            int blk_i = blk[++i];
            int blk_i8 = blk[++i8];
            int blk_i16 = blk[++i16];
            int blk_i24 = blk[++i24];
            int blk_i32 = blk[++i32];
            int blk_i40 = blk[++i40];
            int blk_i48 = blk[++i48];
            int blk_i56 = blk[++i56];
            if((blk_i8 | blk_i16 | blk_i24 | blk_i32 | blk_i40 | blk_i48 | blk_i56) == 0)
            {
                blk[i8] = blk[i16] = blk[i24] = blk[i32] = blk[i40] = blk[i48] = blk[i56] = blk_i;
            } else
            {
                int x8 = blk_i + blk_i32;
                int x9 = blk_i - blk_i32;
                int x11 = blk_i16 + blk_i48;
                int x10 = (362 * (blk_i16 - blk_i48) >> 8) - x11;
                int x0 = x8 + x11;
                int x3 = x8 - x11;
                int x1 = x9 + x10;
                int x2 = x9 - x10;
                int y5 = blk_i40 + blk_i24;
                int y2 = blk_i40 - blk_i24;
                int y3 = blk_i8 + blk_i56;
                int y4 = blk_i8 - blk_i56;
                int x7 = y3 + y5;
                x9 = 362 * (y3 - y5) >> 8;
                int y1 = 473 * (y2 + y4) >> 8;
                x8 = (277 * y4 >> 8) - y1;
                x10 = (-669 * y2 >> 8) + y1;
                int x6 = x10 - x7;
                int x5 = x9 - x6;
                int x4 = x8 + x5;
                blk[i] = x0 + x7;
                blk[i8] = x1 + x6;
                blk[i16] = x2 + x5;
                blk[i24] = x3 - x4;
                blk[i32] = x3 + x4;
                blk[i40] = x2 - x5;
                blk[i48] = x1 - x6;
                blk[i56] = x0 - x7;
            }
        }

        int offset = 0;
        int clip[] = clipTable;
        for(int i = 0; i < 8; i++)
        {
            int o0;
            int blk_o0 = blk[o0 = offset++];
            int o1;
            int blk_o1 = blk[o1 = offset++];
            int o2;
            int blk_o2 = blk[o2 = offset++];
            int o3;
            int blk_o3 = blk[o3 = offset++];
            int o4;
            int blk_o4 = blk[o4 = offset++];
            int o5;
            int blk_o5 = blk[o5 = offset++];
            int o6;
            int blk_o6 = blk[o6 = offset++];
            int o7;
            int blk_o7 = blk[o7 = offset++];
            if((blk_o1 | blk_o2 | blk_o3 | blk_o4 | blk_o5 | blk_o6 | blk_o7) == 0)
            {
                int temp = blk_o0 + 2048 >> 12;
                blk[o0] = blk[o1] = blk[o2] = blk[o3] = blk[o4] = blk[o5] = blk[o6] = blk[o7] = temp;
                if(flag != 0)
                {
                    temp = clip[0x3ff & temp];
                    pixel[pixOffset++] = pixel[pixOffset++] = pixel[pixOffset++] = pixel[pixOffset++] = pixel[pixOffset++] = pixel[pixOffset++] = pixel[pixOffset++] = pixel[pixOffset++] = temp;
                } else
                {
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + temp];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + temp];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + temp];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + temp];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + temp];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + temp];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + temp];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + temp];
                }
                pixOffset += width - 8;
            } else
            {
                int x8 = blk_o0 + blk_o4;
                int x9 = blk_o0 - blk_o4;
                int x11 = blk_o2 + blk_o6;
                int x10 = (362 * (blk_o2 - blk_o6) >> 8) - x11;
                int x0 = x8 + x11 + 2048;
                int x3 = (x8 - x11) + 2048;
                int x1 = x9 + x10 + 2048;
                int x2 = (x9 - x10) + 2048;
                int y5 = blk_o5 + blk_o3;
                int y2 = blk_o5 - blk_o3;
                int y3 = blk_o1 + blk_o7;
                int y4 = blk_o1 - blk_o7;
                int x7 = y3 + y5;
                x9 = 362 * (y3 - y5) >> 8;
                int y1 = 473 * (y2 + y4) >> 8;
                x8 = (277 * y4 >> 8) - y1;
                x10 = (-669 * y2 >> 8) + y1;
                int x6 = x10 - x7;
                int x5 = x9 - x6;
                int x4 = x8 + x5;
                if(flag != 0)
                {
                    pixel[pixOffset++] = clip[0x3ff & (blk[o0] = x0 + x7 >> 12)];
                    pixel[pixOffset++] = clip[0x3ff & (blk[o1] = x1 + x6 >> 12)];
                    pixel[pixOffset++] = clip[0x3ff & (blk[o2] = x2 + x5 >> 12)];
                    pixel[pixOffset++] = clip[0x3ff & (blk[o3] = x3 - x4 >> 12)];
                    pixel[pixOffset++] = clip[0x3ff & (blk[o4] = x3 + x4 >> 12)];
                    pixel[pixOffset++] = clip[0x3ff & (blk[o5] = x2 - x5 >> 12)];
                    pixel[pixOffset++] = clip[0x3ff & (blk[o6] = x1 - x6 >> 12)];
                    pixel[pixOffset++] = clip[0x3ff & (blk[o7] = x0 - x7 >> 12)];
                } else
                {
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + (blk[o0] = x0 + x7 >> 12)];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + (blk[o1] = x1 + x6 >> 12)];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + (blk[o2] = x2 + x5 >> 12)];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + (blk[o3] = x3 - x4 >> 12)];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + (blk[o4] = x3 + x4 >> 12)];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + (blk[o5] = x2 - x5 >> 12)];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + (blk[o6] = x1 - x6 >> 12)];
                    pixel[pixOffset] = clip[0x3ff & pixel[pixOffset++] + (blk[o7] = x0 - x7 >> 12)];
                }
                pixOffset += width - 8;
            }
        }

    }

    public void finalize()
    {
        CurrentFrame = null;
        PreviousFrame = null;
        xPrevFrame = null;
        MotVectCurrGOB = null;
        MotVectPrevGOB = null;
        MBtypeCurrGOB = null;
        MBtypePrevGOB = null;
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997-1999.";
    private static final boolean DEBUG = false;
    private static final int I_PICTURE = 0;
    private static final int P_PICTURE = 1;
    private static final int INTRA_STUFFING = -1;
    private static final int INTER_STUFFING = -1;
    private static final int ESCAPE_CODE = 7167;
    private static final int PSC = 32;
    private static final int GBSC_1 = 33;
    private static final int GBSC_17 = 49;
    private static final int GBSC_30 = 62;
    private static final int EOS = 63;
    private static final int INIT_FORMAT = 9;
    private static final int USER_DEFINED_FORMAT = 7;
    private static final int SUB_QCIF_FORMAT = 1;
    private static final int QCIF_PAL_FORMAT = 2;
    private static final int CIF_PAL_FORMAT = 3;
    private static final int W0 = 277;
    private static final int W1 = 362;
    private static final int W2 = 473;
    private static final int W3 = 669;
    public static final int H263_RC_PICTURE_NOT_DONE = 0;
    public static final int H263_RC_PICTURE_DONE = 1;
    public static final int H263_RC_PICTURE_FORMAT_NOT_SUPPORTED = 2;
    public static final int H263_RC_PICTURE_FORMAT_NOT_INITED = 3;
    public int TemporalReference;
    public int LastTemporalReference;
    private int rtpTemporalReference;
    private int HeaderPlus;
    private int TypeInformation;
    private int CodingType;
    private int UnrestrictedMV;
    private int DeblockingFilter;
    private int SourceFormat;
    private int CPM;
    private int Quantizer;
    private int GroupNumber;
    private int GOB_FrameID;
    private int BlockData[][];
    private int BlockPtr[][];
    private int BlockOffset[];
    private int FrameWidthBlock[];
    private int LastValue;
    private int FrameWidth;
    private int FrameWidthDiv2;
    private int FrameWidthx8;
    private int FrameHeight;
    private int HorMV;
    private int VerMV;
    private int Half_HorMV;
    private int Half_VerMV;
    private int xFrameWidth;
    private int xFrameWidthDiv2;
    public FrameBuffer CurrentFrame;
    protected boolean frameDone;
    private boolean foundPSC;
    private boolean standardH263Only;
    private long prevTimeStamp;
    private FrameBuffer PreviousFrame;
    private FrameBuffer xPrevFrame;
    private int MB_address;
    private int FirstMBinGOB;
    private int MBperGOB;
    private int MBpositionInGOB;
    private int GOBperFrame;
    private boolean HeaderInGOB;
    private int CurrentLumiOffset;
    private int CurrentCromOffset;
    private int xCurrentLumiOffset;
    private int xCurrentCromOffset;
    private static final int LumiOffset[] = new int[36];
    private static final int CromOffset[] = new int[36];
    private static final int xLumiOffset[] = new int[36];
    private static final int xCromOffset[] = new int[36];
    private static final int CodedMap[] = new int[2048];
    private int MBtypeCurrGOB[];
    private int MBtypePrevGOB[];
    private Point MotVectCurrGOB[];
    private Point MotVectPrevGOB[];
    private int prevCBP;
    private static final int dQuant[] = {
        1, 0, 3, 4
    };
    private static final int recLevel_tab[][];
    private static final int clipQ_tab[];
    private static final int pre8x8_tab[] = {
        512, 710, 669, 602, 512, 402, 277, 141, 710, 985, 
        928, 835, 710, 558, 384, 196, 669, 928, 874, 787, 
        669, 526, 362, 185, 602, 835, 787, 708, 602, 473, 
        326, 166, 512, 710, 669, 602, 512, 402, 277, 141, 
        402, 558, 526, 473, 402, 316, 218, 111, 277, 384, 
        362, 326, 277, 218, 150, 76, 141, 196, 185, 166, 
        141, 111, 76, 39
    };
    private static final int STRENGTH[];
    private static final int zigzag_tab[];
    private static final int IntraMCBPC_VLC_tab0[];
    private static final int IntraMCBPC_VLC_tab1[];
    private static final int InterMCBPC_VLC_tab0[];
    private static final int InterMCBPC_VLC_tab1[];
    private static final int CBPY_VLC_tab0[];
    private static final int CBPY_VLC_tab1[];
    private static final int MVD1_VLC_tab0[];
    private static final int MVD1_VLC_tab1[];
    private static final int MVD2_VLC_tab0[];
    private static final int MVD2_VLC_tab1[];
    private static final int TCOEFF1_tab0[];
    private static final int TCOEFF1_tab1[];
    private static final int TCOEFF2_tab0[];
    private static final int TCOEFF2_tab1[];
    private static final int TCOEFF3_tab0[];
    private static final int TCOEFF3_tab1[];
    private static final int clipTable[];
    private static final int DfiltClip[];

    static 
    {
        recLevel_tab = new int[32][128];
        clipQ_tab = new int[36];
        STRENGTH = new int[31];
        zigzag_tab = new int[64];
        IntraMCBPC_VLC_tab0 = new int[64];
        IntraMCBPC_VLC_tab1 = new int[64];
        InterMCBPC_VLC_tab0 = new int[512];
        InterMCBPC_VLC_tab1 = new int[512];
        CBPY_VLC_tab0 = new int[64];
        CBPY_VLC_tab1 = new int[64];
        MVD1_VLC_tab0 = new int[256];
        MVD1_VLC_tab1 = new int[256];
        MVD2_VLC_tab0 = new int[256];
        MVD2_VLC_tab1 = new int[256];
        TCOEFF1_tab0 = new int[112];
        TCOEFF1_tab1 = new int[112];
        TCOEFF2_tab0 = new int[96];
        TCOEFF2_tab1 = new int[96];
        TCOEFF3_tab0 = new int[120];
        TCOEFF3_tab1 = new int[120];
        clipTable = new int[1024];
        DfiltClip = new int[512];
        int row = 0;
        int col = 0;
        int direction = 1;
        for(int i = 0; i < 32; i++)
        {
            int temp = row * 8 + col;
            zigzag_tab[i] = temp;
            zigzag_tab[63 - i] = 63 - temp;
            row -= direction;
            col += direction;
            if(col < 0)
            {
                direction = 1;
                col = 0;
            }
            if(row < 0)
            {
                direction = -1;
                row = 0;
            }
        }

        for(int i = 0; i < 1024; i++)
            clipTable[i] = i >= 256 ? ((int) (i >= 512 ? 0 : 255)) : i;

        for(int i = 0; i < recLevel_tab.length; i++)
        {
            for(int j = 0; j < recLevel_tab[i].length; j++)
                if(j == 0)
                {
                    recLevel_tab[i][j] = 0;
                } else
                {
                    int temp = i * (2 * j + 1);
                    recLevel_tab[i][j] = temp >= -2048 ? temp <= 2047 ? temp : 2047 : -2048;
                }

        }

        for(int i = 0; i < clipQ_tab.length; i++)
        {
            int temp = i - 2;
            clipQ_tab[i] = temp >= 1 ? temp <= 31 ? temp : 31 : 1;
        }

        for(int i = 1; i < IntraMCBPC_VLC_tab0.length; i++)
        {
            IntraMCBPC_VLC_tab0[i] = i > 3 ? i > 7 ? i > 31 ? 48 : i / 8 + 48 : 64 : 64 + i;
            IntraMCBPC_VLC_tab1[i] = i > 3 ? ((int) (i > 7 ? ((int) (i > 31 ? 1 : 3)) : 4)) : 6;
        }

        for(int i = 1; i < InterMCBPC_VLC_tab0.length; i++)
        {
            InterMCBPC_VLC_tab0[i] = i > 1 ? i > 4 ? i > 5 ? i > 9 ? ((int) (i > 11 ? ((int) (i > 15 ? ((int) (i > 19 ? ((int) (i > 23 ? ((int) (i > 27 ? ((int) (i > 31 ? ((int) (i > 39 ? ((int) (i > 47 ? ((int) (i > 63 ? ((int) (i > 95 ? ((int) (i > 127 ? ((int) (i > 191 ? ((int) (i >= 256 ? 0 : 16)) : 32)) : 1)) : 2)) : 48)) : 3)) : 64)) : 17)) : 18)) : 33)) : 34)) : 51)) : 35)) : 53 - i / 2 : 19 : 69 - i : -1;
            InterMCBPC_VLC_tab1[i] = i > 5 ? ((int) (i > 11 ? ((int) (i > 31 ? ((int) (i > 47 ? ((int) (i > 63 ? ((int) (i > 127 ? ((int) (i > 255 ? 1 : 3)) : 4)) : 5)) : 6)) : 7)) : 8)) : 9;
        }

        for(int i = 2; i < CBPY_VLC_tab0.length; i++)
        {
            CBPY_VLC_tab0[i] = i > 2 ? i > 3 ? ((int) (i > 5 ? ((int) (i > 7 ? ((int) (i > 9 ? ((int) (i > 11 ? ((int) (i > 15 ? ((int) (i > 19 ? ((int) (i > 23 ? ((int) (i > 27 ? ((int) (i > 31 ? ((int) (i > 35 ? ((int) (i > 39 ? ((int) (i > 43 ? ((int) (i > 47 ? 240 : 120)) : 180)) : 60)) : 210)) : 90)) : 225)) : 165)) : 195)) : 15)) : 30)) : 45)) : 75)) : 135)) : 150 : 105;
            CBPY_VLC_tab1[i] = i > 3 ? ((int) (i > 11 ? ((int) (i > 47 ? 2 : 4)) : 5)) : 6;
        }

        for(int i = 6; i < MVD1_VLC_tab0.length; i++)
        {
            MVD1_VLC_tab0[i] = i > 11 ? i > 15 ? i > 31 ? i > 63 ? i >= 128 ? 0 : 1 - (1 & i >> 5) * 2 : 2 * (1 - (1 & i >> 4) * 2) : 3 * (1 - (1 & i >> 3) * 2) : 4 * (1 - (i & 2)) : (10 - i / 2) * (1 - (i & 1) * 2);
            MVD1_VLC_tab1[i] = i > 11 ? ((int) (i > 15 ? ((int) (i > 31 ? ((int) (i > 63 ? ((int) (i > 127 ? 1 : 3)) : 4)) : 5)) : 7)) : 8;
        }

        for(int i = 0; i < MVD2_VLC_tab0.length; i++)
        {
            MVD2_VLC_tab0[i] = i >= 5 ? i != 5 ? i > 7 ? i > 31 ? i > 143 ? (19 - (i >> 4)) * (1 - (1 & i >> 3) * 2) : (28 - (i >> 3)) * (1 - (1 & i >> 2) * 2) : (32 - (i >> 2)) * (1 - (1 & i >> 1) * 2) : 31 * (1 - (i & 1) * 2) : -32 : 0;
            MVD2_VLC_tab1[i] = i > 4 ? ((int) (i > 7 ? ((int) (i > 31 ? ((int) (i > 143 ? ((int) (i > 191 ? 5 : 10)) : 11)) : 12)) : 13)) : 5;
        }

        for(int i = 0; i < TCOEFF1_tab0.length; i++)
        {
            TCOEFF1_tab0[i] = i > 3 ? i > 6 ? i > 7 ? i > 15 ? i > 23 ? i > 25 ? i > 27 ? i > 39 ? ((int) (i > 47 ? ((int) (i > 79 ? ((int) (i > 95 ? ((int) (i > 103 ? 2 : 33)) : 17)) : 1)) : 4097)) : 193 - (i / 4) * 16 : 3 : 18 : 273 - (i / 2) * 16 : 4225 - (i / 2) * 16 : 4 : 257 - i * 16 : 4225 - i * 16;
            TCOEFF1_tab1[i] = i > 7 ? ((int) (i > 27 ? ((int) (i > 39 ? ((int) (i > 47 ? ((int) (i > 79 ? ((int) (i > 95 ? 4 : 3)) : 2)) : 4)) : 5)) : 6)) : 7;
        }

        for(int i = 0; i < TCOEFF2_tab0.length; i++)
        {
            TCOEFF2_tab0[i] = i > 1 ? i > 17 ? i > 19 ? i > 35 ? i > 39 ? i > 43 ? i > 75 ? i > 83 ? i > 91 ? 5 : 349 - (i / 4) * 15 : 529 - (i / 4) * 16 : 4529 - (i / 4) * 16 : 27 - i / 2 : 354 - (i / 2) * 16 : 513 - (i / 2) * 16 : 4098 : 4497 - (i / 2) * 16 : 9 - i;
            TCOEFF2_tab1[i] = i > 1 ? ((int) (i > 43 ? 8 : 9)) : 10;
        }

        for(int i = 0; i < TCOEFF3_tab0.length; i++)
        {
            TCOEFF3_tab0[i] = i > 3 ? i > 7 ? i > 23 ? i > 43 ? i > 51 ? i > 55 ? i > 59 ? i > 63 ? i > 71 ? i > 72 ? i > 73 ? i > 74 ? i > 75 ? i > 76 ? i > 77 ? i > 78 ? i > 79 ? i > 87 ? 7167 : i * 16 + 3345 : 417 : 401 : 162 : 99 : 83 : 67 : 36 : 22 : (i / 2) * 16 + 4049 : (i / 2) * 16 - 111 : (i / 2) * 9 - 240 : 20 : 227 - (i / 4) * 16 : 242 - (i / 4) * 16 : 4577 - (i / 4) * 16 : 13 - i / 2 : 4114 - (i / 2) * 15;
            TCOEFF3_tab1[i] = i > 7 ? ((int) (i > 55 ? ((int) (i > 71 ? ((int) (i > 87 ? 6 : 12)) : 11)) : 10)) : 11;
        }

        for(int i = 0; i < 31; i++)
            STRENGTH[i] = i >= 6 ? i >= 9 ? i >= 13 ? (i + 2) / 3 + 2 : i + 1 >> 1 : 4 : (i >> 1) + 1;

        for(int i = 0; i < 512; i++)
            DfiltClip[i] = i >= 128 ? i >= 384 ? 255 : i - 128 : 0;

    }
}
