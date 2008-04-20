/**
 * Test SDP4 routines
 * =====================================================================
 * Copyright (C) 2008 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 *
 */

package name.gano.astro.propogators.sdp4;

public class TestSDP4
{
    public static void main(String args[])
    {
        //System.out.println("Test");
        // create SDP4
        SDP4 itsSDP4 = new SDP4();
        itsSDP4.Init();
        
        // file name with TLE
        String aFileName = "stations.txt";
        String aName = "ISS (ZARYA)"; // name of satellite
        
        String tle1 = "1 25544U 98067A   07014.31794521  .00016398  00000-0  10090-3 0  4371";
        String tle2 = "2 25544  51.6319 179.0382 0023514  90.4796 348.4612 15.77019593466479";
        
        try
        {
            itsSDP4.NoradLoadTLE(aName, tle1,tle2);  // call to load TLE from strings (internal)
            //itsSDP4.NoradByName(aFileName, aName); // call to load TLE from file
            String itsName = itsSDP4.itsName;
        }
        catch(Exception e)
        {}
        
        // SGP4 to a given Julean Date
        double aFileEpoch = 2454115 - 2450000; // 14 Jan 2007
        double aJulDate = 2454116 - 2450000; // 15 Jan 2007 = 2454116
        itsSDP4.GetPosVel(aJulDate);
        // CAREFUL DEALING WITH half day... these come out to 12:00 on jan 15th
        
        double time0 = 0;
        double time;
        double dt = 60; // seconds
        
        int nSteps = 1441;  // one day at dt=60
        
        double[] j2kPos = new double[3];
        double[] j2kVel = new double[3];
        
        for(int i = 0; i< nSteps; i++)
        {
            
            time =time0 + i*dt;
            aJulDate = 2454116 - 2450000 + time/86400;
            
            itsSDP4.GetPosVel(aJulDate);
            
            // transform SGP4 default mean EOD to J2000 -Also extract/transform the velocity.
            // should NOT be GetJulEpoch(aJulDate0)
            Mean2J2000(1, GetJulEpoch(aJulDate), itsSDP4.itsR, j2kPos ); // line 554 satellite.java
            Mean2J2000(1, GetJulEpoch(aJulDate), itsSDP4.itsV, j2kVel ); // line 554 satellite.java
            
            // position in giga-meters (Gm)
            // j2kPos[0]*1000000000.0
            System.out.println(time + "  " + j2kPos[0]*1000000000.0+ "  " + j2kPos[1]*1000000000.0+ "  " + j2kPos[2]*1000000000.0 + "  " + j2kVel[0]*1000000000.0+ "  " + j2kVel[1]*1000000000.0+ "  " + j2kVel[2]*1000000000.0); //+ " " + itsSDP4.itsR[0]);
            
        }
        // converting Vallado's c++ version to start of this -- very close!
        //Mean2J2000(1, GetJulEpoch(aFileEpoch), new double[] {-4287127.419*1E-9,3553156.391*1E-9,-3785426.170*1E-9}, j2kPos);
        //System.out.println("FINAL CONV:  " + j2kPos[0]*1000000000.0+ "  " + j2kPos[1]*1000000000.0+ "  " + j2kPos[2]*1000000000.0);
        
        // output values
    } // main
    
    /**
     * The Julian Day (UT) minus 2.45 million days.
     *
     * <p>A zero here is equivalent to 1995-10-09.5. */
    //protected double itsJD;
    // output - fraction of year or something?
    public static final double GetJulEpoch(double itsJD)
    {
        return ((itsJD - 1545. + DeltaT(itsJD)) / 365.25 + 2000.);
    }
    
// julEpoch (from old Time class) - not sure if it is the current time or the epoch from TLE
// use from getJulEpoch function
    protected static void Mean2J2000(int aNpos, double julEpoch,
            double inTriplets[], double outTriplets[])
    {
        double t, zeta, z, theta;
        int    i;
        
        t     = (julEpoch- 2000.) / 100.;
        zeta  = .6406161 * t +  8.39e-5 * t * t
                +  5e-6    * t * t * t;
        z     = .6406161 * t + 3.041e-4 * t * t
                +  5.1e-6  * t * t * t;
        theta = .556753  * t - 1.185e-4 * t * t
                - 1.16e-5  * t * t * t;
        zeta  /= Hmelib.DEGPERRAD;
        z     /= Hmelib.DEGPERRAD;
        theta /= Hmelib.DEGPERRAD;
        
        double mat[] = {
            +Math.cos(zeta) * Math.cos(theta) * Math.cos(z)
            - Math.sin(zeta)                   * Math.sin(z),
            -Math.sin(zeta) * Math.cos(theta) * Math.cos(z)
            - Math.cos(zeta)                   * Math.sin(z),
            -Math.sin(theta) * Math.cos(z),
            +Math.cos(zeta) * Math.cos(theta) * Math.sin(z)
            + Math.sin(zeta)                   * Math.cos(z),
            -Math.sin(zeta) * Math.cos(theta) * Math.sin(z)
            + Math.cos(zeta)                   * Math.cos(z),
            -Math.sin(theta) * Math.sin(z),
            +Math.cos(zeta) * Math.sin(theta),
            -Math.sin(zeta) * Math.sin(theta),
            +Math.cos(theta)
        };
        
        for (i = 0; i < aNpos; i++)
        {
            outTriplets[3*i+0] = mat[0] * inTriplets[3*i+0]
                    + mat[3] * inTriplets[3*i+1]
                    + mat[6] * inTriplets[3*i+2];
            outTriplets[3*i+1] = mat[1] * inTriplets[3*i+0]
                    + mat[4] * inTriplets[3*i+1]
                    + mat[7] * inTriplets[3*i+2];
            outTriplets[3*i+2] = mat[2] * inTriplets[3*i+0]
                    + mat[5] * inTriplets[3*i+1]
                    + mat[8] * inTriplets[3*i+2];
        }
    }
    
    
    /**
     * Return TT minus UT.
     *
     * <p>Up to 1983 Ephemeris Time (ET) was used in place of TT, between
     * 1984 and 2000 Temps Dynamique Terrestrial (TDT) was used in place of TT.
     * The three time scales, while defined differently, form a continuous time
     * scale for most purposes.  TT has a fixed offset from TAI (Temps Atomique
     * International).
     *
     * <p>This method returns the difference TT - UT in days.  Usually this
     * would be looked up in a table published after the fact.  Here we use
     * polynomial fits for the distant past, for the future and also for the
     * time where the table exists.  Except for 1987 to 2015, the expressions
     * are taken from
     * Jean Meeus, 1991, <I>Astronomical Algorithms</I>, Willmann-Bell, Richmond VA, p.73f.
     * For the present (1987 to 2015 we use our own graphical linear fit to the
     * data 1987 to 2001 from
     * USNO/RAL, 2001, <I>Astronomical Almanach 2003</I>, U.S. Government Printing Office, Washington DC, Her Majesty's Stationery Office, London,
p.K9:
     *
     * <p>t = Ep - 2002
     * <p>DeltaT/s = 9.2 * t / 15 + 65
     *
     * <p>Close to the present (1900 to 1987) we use Schmadl and Zech:
     *
     * <p>t = (Ep - 1900) / 100
     * <p>DeltaT/d = -0.000020      + 0.000297 * t
     *    + 0.025184 * t<sup>2</sup> - 0.181133 * t<sup>3</sup><BR>
     *    + 0.553040 * t<sup>4</sup> - 0.861938 * t<sup>5</sup>
     *    + 0.677066 * t<sup>6</sup> - 0.212591 * t<sup>7</sup>
     *
     * <p>This work dates from 1988 and the equation is supposed to be valid only
     * to 1987, but we extend its use into the near future.  For the 19th
     * century we use Schmadl and Zech:
     *
     * <p>t = (Ep - 1900) / 100
     * <p>DeltaT/d = -0.000009      +  0.003844 * t
     *     +  0.083563 * t<sup>2</sup> +  0.865736 * t<sup>3</sup><BR>
     *     +  4.867575 * t<sup>4</sup> + 15.845535 * t<sup>5</sup>
     *     + 31.332267 * t<sup>6</sup> + 38.291999 * t<sup>7</sup><BR>
     *     + 28.316289 * t<sup>8</sup> + 11.636204 * t<sup>9</sup>
     *     +  2.043794 * t<sup>10</sup>
     *
     * <p>Stephenson and Houlden are credited with the equations for times before
     * 1600.  First for the period 948 to 1600:
     *
     * <p>t = (Ep - 1850) / 100
     * <p>DeltaT/s = 22.5 * t<sup>2</sup>
     *
     * <p>and before 948:
     *
     * <p>t = (Ep - 948) / 100
     * <p>DeltaT/s = 1830 - 405 * t + 46.5 * t<sup>2</sup>
     *
     * <p>This leaves no equation for times between 1600 and 1800 and beyond
     * 2015.  For such times we use the equation of Morrison and Stephenson:
     *
     * <p>t = Ep - 1810
     * <p>DeltaT/s = -15 + 0.00325 * t<sup>2</sup> */
    
    public static double DeltaT(double itsJD)
    {
        double theEpoch; /* Julian Epoch */
        double t; /* Time parameter used in the equations. */
        double D; /* The return value. */
        
        theEpoch = 2000. + (itsJD - 1545.) / 365.25;
        
    /* For 1987 to 2015 we use a graphical linear fit to the annual tabulation
     * from USNO/RAL, 2001, Astronomical Almanach 2003, p.K9.  We use this up
     * to 2015 about as far into the future as it is based on data in the past.
     * The result is slightly higher than the predictions from that source. */
        
        if (1987 <= theEpoch && 2015 >= theEpoch)
        {
            t = (theEpoch - 2002.);
            D = 9.2 * t / 15. + 65.;
            D /= 86400.;
        }
        
    /* For 1900 to 1987 we use the equation from Schmadl and Zech as quoted in
     * Meeus, 1991, Astronomical Algorithms, p.74.  This is precise within
     * 1.0 second. */
        
        else if (1900 <= theEpoch && 1987 > theEpoch)
        {
            t  = (theEpoch - 1900.) / 100.;
            D = -0.212591 * t * t * t * t * t * t * t
                    + 0.677066 * t * t * t * t * t * t
                    - 0.861938 * t * t * t * t * t
                    + 0.553040 * t * t * t * t
                    - 0.181133 * t * t * t
                    + 0.025184 * t * t
                    + 0.000297 * t
                    - 0.000020;
        }
        
    /* For 1800 to 1900 we use the equation from Schmadl and Zech as quoted in
     * Meeus, 1991, Astronomical Algorithms, p.74.  This is precise within 1.0
     * second. */
        
        else if (1800 <= theEpoch && 1900 > theEpoch)
        {
            t  = (theEpoch - 1900.) / 100.;
            D =  2.043794 * t * t * t * t * t * t * t * t * t * t
                    + 11.636204 * t * t * t * t * t * t * t * t * t
                    + 28.316289 * t * t * t * t * t * t * t * t
                    + 38.291999 * t * t * t * t * t * t * t
                    + 31.332267 * t * t * t * t * t * t
                    + 15.845535 * t * t * t * t * t
                    +  4.867575 * t * t * t * t
                    +  0.865736 * t * t * t
                    +  0.083563 * t * t
                    +  0.003844 * t
                    -  0.000009;
        }
        
    /* For 948 to 1600 we use the equation from Stephenson and Houlden as
     * quoted in Meeus, 1991, Astronomical Algorithms, p.73. */
        
        else if (948 <= theEpoch && 1600 >= theEpoch)
        {
            t  = (theEpoch - 1850.) / 100.;
            D  = 22.5 * t * t;
            D /= 86400.;
        }
        
    /* Before 948 we use the equation from Stephenson and Houlden as quoted
     * in Meeus, 1991, Astronomical Algorithms, p.73. */
        
        else if (948 > theEpoch)
        {
            t  = (theEpoch - 948.) / 100.;
            D  = 46.5 * t * t - 405. * t + 1830.;
            D /= 86400.;
        }
        
    /* Else (between 1600 and 1800 and after 2010) we use the equation from
     * Morrison and Stephenson, quoted as eqation 9.1 in Meeus, 1991,
     * Astronomical Algorithms, p.73. */
        
        else
        {
            t  = theEpoch - 1810.;
            D  = 0.00325 * t * t - 15.;
            D /= 86400.;
        }
        
        return D;
    } // DeltaT
    
    
    
}// class
