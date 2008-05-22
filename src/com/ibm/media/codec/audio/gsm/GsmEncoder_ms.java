// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GsmEncoder_ms.java

package com.ibm.media.codec.audio.gsm;


// Referenced classes of package com.ibm.media.codec.audio.gsm:
//            GsmEncoder

public class GsmEncoder_ms extends GsmEncoder
{

    public GsmEncoder_ms()
    {
    }

    public void gsm_encode_frame(byte input_samples[], int input_offset, byte output_bits[], int output_offset)
    {
        isOddFrame = true;
        super.gsm_encode_frame(input_samples, input_offset, output_bits, output_offset);
        isOddFrame = false;
        super.gsm_encode_frame(input_samples, input_offset + 320, output_bits, output_offset);
    }

    protected void packBitStream(byte outputBits[], int output_offset)
    {
        int param[] = super.data_Parameters;
        if(isOddFrame)
        {
            outputBits[output_offset + 0] = (byte)(param[0] | param[1] << 6);
            outputBits[output_offset + 1] = (byte)(param[1] >> 2 | param[2] << 4);
            outputBits[output_offset + 2] = (byte)(param[2] >> 4 | param[3] << 1 | param[4] << 6);
            outputBits[output_offset + 3] = (byte)(param[4] >> 2 | param[5] << 2 | param[6] << 6);
            outputBits[output_offset + 4] = (byte)(param[6] >> 2 | param[7] << 1 | param[8] << 4);
            outputBits[output_offset + 5] = (byte)(param[8] >> 4 | param[9] << 3 | param[10] << 5 | param[11] << 7);
            outputBits[output_offset + 6] = (byte)(param[11] >> 1 | param[12] << 5);
            outputBits[output_offset + 7] = (byte)(param[13] | param[14] << 3 | param[15] << 6);
            outputBits[output_offset + 8] = (byte)(param[15] >> 2 | param[16] << 1 | param[17] << 4 | param[18] << 7);
            outputBits[output_offset + 9] = (byte)(param[18] >> 1 | param[19] << 2 | param[20] << 5);
            outputBits[output_offset + 10] = (byte)(param[21] | param[22] << 3 | param[23] << 6);
            outputBits[output_offset + 11] = (byte)(param[23] >> 2 | param[24] << 1 | param[25] << 4);
            outputBits[output_offset + 12] = (byte)(param[25] >> 4 | param[26] << 3 | param[27] << 5 | param[28] << 7);
            outputBits[output_offset + 13] = (byte)(param[28] >> 1 | param[29] << 5);
            outputBits[output_offset + 14] = (byte)(param[30] | param[31] << 3 | param[32] << 6);
            outputBits[output_offset + 15] = (byte)(param[32] >> 2 | param[33] << 1 | param[34] << 4 | param[35] << 7);
            outputBits[output_offset + 16] = (byte)(param[35] >> 1 | param[36] << 2 | param[37] << 5);
            outputBits[output_offset + 17] = (byte)(param[38] | param[39] << 3 | param[40] << 6);
            outputBits[output_offset + 18] = (byte)(param[40] >> 2 | param[41] << 1 | param[42] << 4);
            outputBits[output_offset + 19] = (byte)(param[42] >> 4 | param[43] << 3 | param[44] << 5 | param[45] << 7);
            outputBits[output_offset + 20] = (byte)(param[45] >> 1 | param[46] << 5);
            outputBits[output_offset + 21] = (byte)(param[47] | param[48] << 3 | param[49] << 6);
            outputBits[output_offset + 22] = (byte)(param[49] >> 2 | param[50] << 1 | param[51] << 4 | param[52] << 7);
            outputBits[output_offset + 23] = (byte)(param[52] >> 1 | param[53] << 2 | param[54] << 5);
            outputBits[output_offset + 24] = (byte)(param[55] | param[56] << 3 | param[57] << 6);
            outputBits[output_offset + 25] = (byte)(param[57] >> 2 | param[58] << 1 | param[59] << 4);
            outputBits[output_offset + 26] = (byte)(param[59] >> 4 | param[60] << 3 | param[61] << 5 | param[62] << 7);
            outputBits[output_offset + 27] = (byte)(param[62] >> 1 | param[63] << 5);
            outputBits[output_offset + 28] = (byte)(param[64] | param[65] << 3 | param[66] << 6);
            outputBits[output_offset + 29] = (byte)(param[66] >> 2 | param[67] << 1 | param[68] << 4 | param[69] << 7);
            outputBits[output_offset + 30] = (byte)(param[69] >> 1 | param[70] << 2 | param[71] << 5);
            outputBits[output_offset + 31] = (byte)(param[72] | param[73] << 3 | param[74] << 6);
            outputBits[output_offset + 32] = (byte)(param[74] >> 2 | param[75] << 1);
        } else
        {
            outputBits[output_offset + 32] |= (byte)(param[0] << 4);
            outputBits[output_offset + 33] = (byte)(param[0] >> 4 | param[1] << 2);
            outputBits[output_offset + 34] = (byte)(param[2] | param[3] << 5);
            outputBits[output_offset + 35] = (byte)(param[3] >> 3 | param[4] << 2 | param[5] << 6);
            outputBits[output_offset + 36] = (byte)(param[5] >> 2 | param[6] << 2 | param[7] << 5);
            outputBits[output_offset + 37] = (byte)(param[8] | param[9] << 7);
            outputBits[output_offset + 38] = (byte)(param[9] >> 1 | param[10] << 1 | param[11] << 3);
            outputBits[output_offset + 39] = (byte)(param[11] >> 5 | param[12] << 1 | param[13] << 4 | param[14] << 7);
            outputBits[output_offset + 40] = (byte)(param[14] >> 1 | param[15] << 2 | param[16] << 5);
            outputBits[output_offset + 41] = (byte)(param[17] | param[18] << 3 | param[19] << 6);
            outputBits[output_offset + 42] = (byte)(param[19] >> 2 | param[20] << 1 | param[21] << 4 | param[22] << 7);
            outputBits[output_offset + 43] = (byte)(param[22] >> 1 | param[23] << 2 | param[24] << 5);
            outputBits[output_offset + 44] = (byte)(param[25] | param[26] << 7);
            outputBits[output_offset + 45] = (byte)(param[26] >> 1 | param[27] << 1 | param[28] << 3);
            outputBits[output_offset + 46] = (byte)(param[28] >> 5 | param[29] << 1 | param[30] << 4 | param[31] << 7);
            outputBits[output_offset + 47] = (byte)(param[31] >> 1 | param[32] << 2 | param[33] << 5);
            outputBits[output_offset + 48] = (byte)(param[34] | param[35] << 3 | param[36] << 6);
            outputBits[output_offset + 49] = (byte)(param[36] >> 2 | param[37] << 1 | param[38] << 4 | param[39] << 7);
            outputBits[output_offset + 50] = (byte)(param[39] >> 1 | param[40] << 2 | param[41] << 5);
            outputBits[output_offset + 51] = (byte)(param[42] | param[43] << 7);
            outputBits[output_offset + 52] = (byte)(param[43] >> 1 | param[44] << 1 | param[45] << 3);
            outputBits[output_offset + 53] = (byte)(param[45] >> 5 | param[46] << 1 | param[47] << 4 | param[48] << 7);
            outputBits[output_offset + 54] = (byte)(param[48] >> 1 | param[49] << 2 | param[50] << 5);
            outputBits[output_offset + 55] = (byte)(param[51] | param[52] << 3 | param[53] << 6);
            outputBits[output_offset + 56] = (byte)(param[53] >> 2 | param[54] << 1 | param[55] << 4 | param[56] << 7);
            outputBits[output_offset + 57] = (byte)(param[56] >> 1 | param[57] << 2 | param[58] << 5);
            outputBits[output_offset + 58] = (byte)(param[59] | param[60] << 7);
            outputBits[output_offset + 59] = (byte)(param[60] >> 1 | param[61] << 1 | param[62] << 3);
            outputBits[output_offset + 60] = (byte)(param[62] >> 5 | param[63] << 1 | param[64] << 4 | param[65] << 7);
            outputBits[output_offset + 61] = (byte)(param[65] >> 1 | param[66] << 2 | param[67] << 5);
            outputBits[output_offset + 62] = (byte)(param[68] | param[69] << 3 | param[70] << 6);
            outputBits[output_offset + 63] = (byte)(param[70] >> 2 | param[71] << 1 | param[72] << 4 | param[73] << 7);
            outputBits[output_offset + 64] = (byte)(param[73] >> 1 | param[74] << 2 | param[75] << 5);
        }
    }

    public static final String a_copyright_notice = "(c) Copyright IBM Corporation 1997,1999.";
    private boolean isOddFrame;
}
