// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JavaRGBConverter.java

package com.sun.media.codec.video.colorspace;

import com.sun.media.BasicCodec;
import javax.media.Format;
import javax.media.format.RGBFormat;

// Referenced classes of package com.sun.media.codec.video.colorspace:
//            RGBConverter

public class JavaRGBConverter extends RGBConverter
{

    public JavaRGBConverter()
    {
        super.inputFormats = (new Format[] {
            new RGBFormat()
        });
        super.outputFormats = (new Format[] {
            new RGBFormat()
        });
    }

    public String getName()
    {
        return "RGB Converter";
    }

    protected void componentToComponent(Object inData, int inPS, int inLS, int inBPP, int inRed, int inGreen, int inBlue, 
            boolean inPacked, int inEndian, Object outData, int outPS, int outLS, int outBPP, int outRed, 
            int outGreen, int outBlue, boolean outPacked, int outEndian, int width, int height, boolean flip)
    {
        int srcPtr = 0;
        int dstPtr = 0;
        int srcInc = inLS - width * inPS;
        int dstInc = outLS - width * outPS;
        if(flip)
        {
            dstPtr = outLS * (height - 1);
            dstInc = -(3 * outLS - width * outPS);
        }
        if(inPacked && outPacked)
        {
            int in[] = (int[])inData;
            int out[] = (int[])outData;
            if(inRed == outRed && inGreen == outGreen && inBlue == outBlue)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        out[dstPtr] = in[srcPtr];
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            } else
            {
                int inrs = getShift(inRed);
                int ings = getShift(inGreen);
                int inbs = getShift(inBlue);
                int outrs = getShift(outRed);
                int outgs = getShift(outGreen);
                int outbs = getShift(outBlue);
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int inPixel = in[srcPtr];
                        int outPixel = (inPixel >> inrs & 0xff) << outrs | (inPixel >> ings & 0xff) << outgs | (inPixel >> inbs & 0xff) << outbs;
                        out[dstPtr] = outPixel;
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            }
        } else
        if(inPacked && !outPacked)
        {
            int in[] = (int[])inData;
            byte out[] = (byte[])outData;
            int redShift = getShift(inRed);
            int greenShift = getShift(inGreen);
            int blueShift = getShift(inBlue);
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    int pixel = in[srcPtr];
                    byte red = (byte)(pixel >> redShift & 0xff);
                    byte green = (byte)(pixel >> greenShift & 0xff);
                    byte blue = (byte)(pixel >> blueShift & 0xff);
                    out[(dstPtr + outRed) - 1] = red;
                    out[(dstPtr + outGreen) - 1] = green;
                    out[(dstPtr + outBlue) - 1] = blue;
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        } else
        if(!inPacked && outPacked)
        {
            byte in[] = (byte[])inData;
            int out[] = (int[])outData;
            int redShift = getShift(outRed);
            int greenShift = getShift(outGreen);
            int blueShift = getShift(outBlue);
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    byte red = in[(srcPtr + inRed) - 1];
                    byte green = in[(srcPtr + inGreen) - 1];
                    byte blue = in[(srcPtr + inBlue) - 1];
                    int pixel = (red & 0xff) << redShift | (green & 0xff) << greenShift | (blue & 0xff) << blueShift;
                    out[dstPtr] = pixel;
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        } else
        if(!inPacked && !outPacked)
        {
            byte in[] = (byte[])inData;
            byte out[] = (byte[])outData;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    out[(dstPtr + outRed) - 1] = in[(srcPtr + inRed) - 1];
                    out[(dstPtr + outGreen) - 1] = in[(srcPtr + inGreen) - 1];
                    out[(dstPtr + outBlue) - 1] = in[(srcPtr + inBlue) - 1];
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        }
    }

    protected void componentToSixteen(Object inData, int inPS, int inLS, int inBPP, int inRed, int inGreen, int inBlue, 
            boolean inPacked, int inEndian, Object outData, int outPS, int outLS, int outBPP, int outRed, 
            int outGreen, int outBlue, boolean outPacked, int outEndian, int width, int height, boolean flip)
    {
        int srcPtr = 0;
        int dstPtr = 0;
        int srcInc = inLS - width * inPS;
        int dstInc = outLS - width * outPS;
        int outrs = getShift(outRed) - 3;
        int outgs = getShift(outGreen) - (outGreen != 2016 ? 3 : 2);
        int inrs = getShift(inRed);
        int ings = getShift(inGreen);
        int inbs = getShift(inBlue);
        int outfs = 0;
        int outss = 0;
        if(!outPacked)
            if(outEndian == 0)
            {
                outfs = 8;
                outss = 0;
            } else
            {
                outfs = 0;
                outss = 8;
            }
        if(flip)
        {
            dstPtr = outLS * (height - 1);
            dstInc = -(3 * outLS - width * outPS);
        }
        if(inPacked && outPacked)
        {
            int in[] = (int[])inData;
            short out[] = (short[])outData;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    int pixel = in[srcPtr];
                    out[dstPtr] = (short)((pixel >> inrs) << outrs & outRed | (pixel >> ings) << outgs & outGreen | (pixel >> inbs & 0xff) >> 3);
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        } else
        if(!inPacked && outPacked)
        {
            byte in[] = (byte[])inData;
            short out[] = (short[])outData;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    out[dstPtr] = (short)(in[(srcPtr + inRed) - 1] << outrs & outRed | in[(srcPtr + inGreen) - 1] << outgs & outGreen | (in[(srcPtr + inBlue) - 1] & 0xff) >> 3);
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        } else
        if(!inPacked && !outPacked)
        {
            byte in[] = (byte[])inData;
            byte out[] = (byte[])outData;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    int pixel = in[(srcPtr + inRed) - 1] << outrs & outRed | in[(srcPtr + inGreen) - 1] << outgs & outGreen | (in[(srcPtr + inBlue) - 1] & 0xff) >> 3;
                    out[dstPtr] = (byte)(pixel >> outfs);
                    out[dstPtr + 1] = (byte)(pixel >> outss);
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        } else
        {
            int in[] = (int[])inData;
            byte out[] = (byte[])outData;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    int pixel = in[srcPtr];
                    pixel = (pixel >> inrs) << outrs & outRed | (pixel >> ings) << outgs & outGreen | (pixel >> inbs & 0xff) >> 3;
                    out[dstPtr] = (byte)(pixel >> outfs);
                    out[dstPtr + 1] = (byte)(pixel >> outss);
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        }
    }

    protected void sixteenToComponent(Object inData, int inPS, int inLS, int inBPP, int inRed, int inGreen, int inBlue, 
            boolean inPacked, int inEndian, Object outData, int outPS, int outLS, int outBPP, int outRed, 
            int outGreen, int outBlue, boolean outPacked, int outEndian, int width, int height, boolean flip)
    {
        int srcPtr = 0;
        int dstPtr = 0;
        int srcInc = inLS - width * inPS;
        int dstInc = outLS - width * outPS;
        if(flip)
        {
            dstPtr = outLS * (height - 1);
            dstInc = -(3 * outLS - width * outPS);
        }
        int inrs = getShift(inRed) - 3;
        int ings = getShift(inGreen) - (inGreen != 2016 ? 3 : 2);
        int outrs = getShift(outRed);
        int outgs = getShift(outGreen);
        int outbs = getShift(outBlue);
        if(inPacked && outPacked)
        {
            short in[] = (short[])inData;
            int out[] = (int[])outData;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    int pixel = in[srcPtr] & 0xffff;
                    int outpixel = ((pixel & inRed) >> inrs) << outrs | ((pixel & inGreen) >> ings) << outgs | (pixel & inBlue) << 3 << outbs;
                    out[dstPtr] = outpixel;
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        } else
        if(!inPacked && outPacked)
        {
            byte in[] = (byte[])inData;
            int out[] = (int[])outData;
            int fbshift;
            int sbshift;
            if(inEndian == 0)
            {
                fbshift = 8;
                sbshift = 0;
            } else
            {
                fbshift = 0;
                sbshift = 8;
            }
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    int pixel = (in[srcPtr] & 0xff) << fbshift | (in[srcPtr + 1] & 0xff) << sbshift;
                    int outpixel = ((pixel & inRed) >> inrs) << outrs | ((pixel & inGreen) >> ings) << outgs | (pixel & inBlue) << 3 << outbs;
                    out[dstPtr] = outpixel;
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        } else
        if(inPacked && !outPacked)
        {
            short in[] = (short[])inData;
            byte out[] = (byte[])outData;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    int pixel = in[srcPtr];
                    out[(dstPtr + outRed) - 1] = (byte)((pixel & inRed) >> inrs);
                    out[(dstPtr + outGreen) - 1] = (byte)((pixel & inGreen) >> ings);
                    out[(dstPtr + outBlue) - 1] = (byte)((pixel & inBlue) << 3);
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        } else
        {
            byte in[] = (byte[])inData;
            byte out[] = (byte[])outData;
            int fbshift;
            int sbshift;
            if(inEndian == 0)
            {
                fbshift = 8;
                sbshift = 0;
            } else
            {
                fbshift = 0;
                sbshift = 8;
            }
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                {
                    int pixel = (in[srcPtr] & 0xff) << fbshift | (in[srcPtr + 1] & 0xff) << sbshift;
                    out[(dstPtr + outRed) - 1] = (byte)((pixel & inRed) >> inrs);
                    out[(dstPtr + outGreen) - 1] = (byte)((pixel & inGreen) >> ings);
                    out[(dstPtr + outBlue) - 1] = (byte)((pixel & inBlue) << 3);
                    srcPtr += inPS;
                    dstPtr += outPS;
                }

                srcPtr += srcInc;
                dstPtr += dstInc;
            }

        }
    }

    protected void sixteenToSixteen(Object inData, int inPS, int inLS, int inBPP, int inRed, int inGreen, int inBlue, 
            boolean inPacked, int inEndian, Object outData, int outPS, int outLS, int outBPP, int outRed, 
            int outGreen, int outBlue, boolean outPacked, int outEndian, int width, int height, boolean flip)
    {
        int srcPtr = 0;
        int dstPtr = 0;
        int srcInc = inLS - width * inPS;
        int dstInc = outLS - width * outPS;
        int shift = 0;
        int infs = 0;
        int inss = 0;
        int outfs = 0;
        int outss = 0;
        if(flip)
        {
            dstPtr = outLS * (height - 1);
            dstInc = -(3 * outLS - width * outPS);
        }
        if(!inPacked)
            if(inEndian == 0)
            {
                infs = 8;
                inss = 0;
            } else
            {
                infs = 0;
                inss = 8;
            }
        if(!outPacked)
            if(outEndian == 0)
            {
                outfs = 8;
                outss = 0;
            } else
            {
                outfs = 0;
                outss = 8;
            }
        if(inRed != outRed || inGreen != outGreen)
            if(inRed > outRed)
                shift = 1;
            else
                shift = -1;
        if(inPacked && outPacked)
        {
            short in[] = (short[])inData;
            short out[] = (short[])outData;
            if(shift == 0)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        out[dstPtr] = in[srcPtr];
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            } else
            if(shift == 1)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = in[srcPtr];
                        out[dstPtr] = (short)(pixel >> 1 & (outGreen | outRed) | pixel & outBlue);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            } else
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = in[srcPtr];
                        out[dstPtr] = (short)((pixel & (inGreen | inRed)) << 1 | pixel & outBlue);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            }
        } else
        if(!inPacked && outPacked)
        {
            byte in[] = (byte[])inData;
            short out[] = (short[])outData;
            if(shift == 0)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = (in[srcPtr] & 0xff) << infs | (in[srcPtr + 1] & 0xff) << inss;
                        out[dstPtr] = (short)pixel;
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            } else
            if(shift == 1)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = (in[srcPtr] & 0xff) << infs | (in[srcPtr + 1] & 0xff) << inss;
                        out[dstPtr] = (short)(pixel >> 1 & (outGreen | outRed) | pixel & outBlue);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            } else
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = (in[srcPtr] & 0xff) << infs | (in[srcPtr + 1] & 0xff) << inss;
                        out[dstPtr] = (short)((pixel & (inGreen | inRed)) << 1 | pixel & outBlue);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            }
        } else
        if(!inPacked && !outPacked)
        {
            byte in[] = (byte[])inData;
            byte out[] = (byte[])outData;
            if(shift == 0)
            {
                if(inEndian == outEndian)
                {
                    for(int y = 0; y < height; y++)
                    {
                        for(int x = 0; x < width; x++)
                        {
                            out[dstPtr] = in[srcPtr];
                            out[dstPtr + 1] = in[srcPtr + 1];
                            srcPtr += inPS;
                            dstPtr += outPS;
                        }

                        srcPtr += srcInc;
                        dstPtr += dstInc;
                    }

                } else
                {
                    for(int y = 0; y < height; y++)
                    {
                        for(int x = 0; x < width; x++)
                        {
                            int pixel = (in[srcPtr] & 0xff) << infs | (in[srcPtr + 1] & 0xff) << inss;
                            out[dstPtr] = (byte)(pixel >> outfs);
                            out[dstPtr + 1] = (byte)(pixel >> outss);
                            srcPtr += inPS;
                            dstPtr += outPS;
                        }

                        srcPtr += srcInc;
                        dstPtr += dstInc;
                    }

                }
            } else
            if(shift == 1)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = (in[srcPtr] & 0xff) << infs | (in[srcPtr + 1] & 0xff) << inss;
                        pixel = pixel >> 1 & (outGreen | outRed) | pixel & outBlue;
                        out[dstPtr] = (byte)(pixel >> outfs);
                        out[dstPtr + 1] = (byte)(pixel >> outss);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            } else
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = (in[srcPtr] & 0xff) << infs | (in[srcPtr + 1] & 0xff) << inss;
                        pixel = pixel >> 1 & (outGreen | outRed) | pixel & outBlue;
                        out[dstPtr] = (byte)(pixel >> outfs);
                        out[dstPtr + 1] = (byte)(pixel >> outss);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            }
        } else
        {
            short in[] = (short[])inData;
            byte out[] = (byte[])outData;
            if(shift == 0)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = in[srcPtr];
                        out[dstPtr] = (byte)(pixel >> outfs);
                        out[dstPtr + 1] = (byte)(pixel >> outss);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            } else
            if(shift == 1)
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = in[srcPtr];
                        pixel = pixel >> 1 & (outGreen | outRed) | pixel & outBlue;
                        out[dstPtr] = (byte)(pixel >> outfs);
                        out[dstPtr + 1] = (byte)(pixel >> outss);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            } else
            {
                for(int y = 0; y < height; y++)
                {
                    for(int x = 0; x < width; x++)
                    {
                        int pixel = in[srcPtr];
                        pixel = pixel >> 1 & (outGreen | outRed) | pixel & outBlue;
                        out[dstPtr] = (byte)(pixel >> outfs);
                        out[dstPtr + 1] = (byte)(pixel >> outss);
                        srcPtr += inPS;
                        dstPtr += outPS;
                    }

                    srcPtr += srcInc;
                    dstPtr += dstInc;
                }

            }
        }
    }

    private static final String PLUGIN_NAME = "RGB Converter";
}
