/*+
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
 * Updated 13 Feb 2008 - Shawn E. Gano
 *       - added constructor that sets default decimal format seperator to always be '.' reguardless of locale in which the app was run
 * 
 * $Id: SDP4.java,v 4.1 2004/08/09 07:54:20 hme Exp $
 *
 * $Log: SDP4.java,v $
 * Revision 4.1  2004/08/09 07:54:20  hme
 * Version 2.1.1.
 *
 * Revision 3.1  2004/07/28 11:09:50  hme
 * Version 2.1.
 *
 * Revision 2.2  2003/09/16 14:31:15  hme
 * Package review.  Rfndm now throws exception when parsing fails.
 * No longer catch IOExceptions, instead throw our own exceptions when
 * the satellite is not in the file or the TLEs are not in order.
 *
 * Revision 1.10  2003/03/09 16:04:38  hme
 * Documentation.
 *
 * Revision 1.9  2003/03/09 12:56:51  hme
 * Removed dependency on other classes.  Output from driver2 is now badly
 * formatted to avoid used of SputnikLib.Wfndm.  And a simplified version
 * of Rfndm has been copied from SputnikLib.  No exception is being thrown
 * there any more, so the dependency on SputnikException is also gone.
 *
 * Revision 1.8  2003/03/09 11:55:35  hme
 * Changed from a sub-class of NamedObject (and hence Catalog) to a
 * base class.
 *
 * Revision 1.5  2003/03/09 11:24:33  hme
 * In fact the bug was in identifying satellites by name.  The two problem
 * satellites had shorter names than their cousins earlier in the TLE file.
 * The name match is now done for equality after trimming, rather than
 * by "startsWith".  The Java version is now equivalent to the Fortran 77
 * version, except for 26965 where the F77 port still has some discrepancy
 * with Kelso's Pascal port.
 *
 * Revision 1.4  2003/03/09 10:05:05  hme
 * After taking care of Fortran's by-reference calls and the
 * consequential possibility of returned arguments, SDP4 is now
 * working with most satellites.  However, for 27409 DEEP is not
 * being called by SDP4, and for 27414 the results are completely
 * wrong (though consistent with the SDP8 Java port).
 *
 * Revision 1.3  2003/03/08 21:19:45  hme
 * All code ported.  SGP4 seem accurate, SDP4 is not accurate.  Likely flow
 * control problem with the endless loops.
 *
 * Revision 1.1  2003/03/06 21:52:23  hme
 * Initial revision
 *
 *-*/

//package com.chiandh.Lib;
package name.gano.astro.propogators.sdp4;

import java.io.*;
import java.text.*;

/**
 * <p>The <code>SDP4</code> class is a base class to calculate ephemeris for
 * an artificial Earth satellite, using the SGP4 and SDP4 models.
 *
 * <p>The SGP4 and SDP4 models of satellite prediction
 * (Felix R. Hoots, Roland L. Roehrich, T.S. Kelso, 1980, 1988, <em>Spacetrack report no. 3, Models for propagation of NORAD element sets</em>, <a href="http://www.celestrak.com/NORAD/documentation/">http://www.celestrak.com/NORAD/documentation/</a>)
 * give us algorithms to predict a few days' worth of motion of any
 * artificial satellite for which we have a NORAD Two Line Element set (TLE).
 * Hoots' report contains Fortran source code for three generations of
 * models: SGP, SGP4/SDP4 and SGP8/SDP8.  The TLE's are compiled assuming the
 * SGP4/SDP4 model, and that is the model that Kelso has ported to Pascal on
 * DOS.
 * (T.S. Kelso, 2000, <em>TrakStar 2.64</em>, <a href="http://www.celestrak.com">http://www.celestrak.com</a>,
 * T.S. Kelso, 1999, <em>NORAD SGP4/SDP4 units 2.60</em>, <a href="http://www.celestrak.com">http://www.celestrak.com</a>)
 * SGPn is used for near-Earth satellites (orbits shorter than 225&nbsp;min)
 * while SDPn is used for satellites further afield.
 *
 * <p>Hoots' Fortran code does not work under modern compilers, as it assumes
 * implicit zero initialisation of variables and persistence of local variables
 * between calls.  The code also uses GO TO statements rather than more
 * structured constructs like <code>IF ELSE ENDIF</code> blocks, which were
 * not allowed in Fortran at the time.  The SGPn models and the main parts of
 * the SDPn models are described mathematically in the report (albeit with a
 * typo in one SDPn equation).  But the most difficult code to port is the DEEP
 * subroutine, which is used by both SDP4 and SDP8.
 *
 * <ol>
 *   <li>In 1996 I translated the SGP8 and SDP8 models to C++.  This was done
 *   without having working Fortran code so that tests were limited to those
 *   shown in the Hoots et al. report.  These tests to not exercise the more
 *   complex parts of DEEP, namely e.g. satellites in a 12 hour resonance.
 *   That port is the code used in Sputnik up to version 1.9 (June 2002).
 *   <li>In 2002 I further ported this C++ code to Java.  In the course of
 *   this work the test failure for satellites in resonance was discovered.
 *   This never went beyond the testing stage.
 *   <li>As a result, a second port of SGP8/SDP8 from the Hoots et al. Fortran
 *   code to C++ and on to Java was made.  Testing was now extended in two
 *   ways.  Firstly, 18 satellites with between 0.1 and 16.5 revolutions per
 *   day are used.  Secondly, the reference data we want to match with the
 *   port are calculated with Kelso's Pascal port on DOS.  This port came
 *   close to reproducing Kelso's output for most satellites, but not for all.
 *   This also never went beyond the testing stage.
 *   <li>In 2003 I started from scratch and ported the Hoots et al. code
 *   to Fortran 77, with some assistance from studying Kelso's Pascal code.
 *   <ol>
 *     <li>With minor modifications the code compiled under GNU g77, but did
 *     not give proper results.
 *     <li>Step by step more local variables were taken into common blocks to
 *     assure persistence and initialisation was finally moved to a separate
 *     subroutines where the variables are initialised by assignment rather
 *     than DATA.
 *     <li>The DEEP subroutine remained problematic due to its three ENTRYs
 *     points with different argument lists and its mixup between arguments and
 *     local variables.  Kelso's Pascal code pointed the way via three wrapper
 *     routines, an argument-free DEEP subroutine with three argument-free
 *     ENTRYs, and common blocks to pass arguments between wrappers and DEEP
 *     proper.
 *     <li>At this point the port reproduced Hoots' test data.  The
 *     18-satellite test against Kelso's application came close, but not close
 *     enough.  This later turned out to be a Y2K bug in THETAG.
 *     <li>At any rate the Fortran code was now prepared for porting to Java.
 *     Common variables would become state variables and subroutines and
 *     functions would become class methods.  The main problem was to change
 *     the flow control from GO TO to if/else blocks and for(;;) endless loops.
 *     In particular the DEEP routine was difficult to port; its structure had
 *     to be simplified by duplicating some code and in general despagging it.
 *     <li>The Java port then worked quite well.  After fixing the Y2K bug in
 *     THETAG it agrees within numeric precision with Kelso's application.
 *     The Fortran code does equally well, except for one satellite where after
 *     a long interval from the TLE epoch it is slightly less close to Kelso's
 *     application.
 *   </ol>
 *   <li>The 2003 port via Fortran 77 to Java was then ported on to C++ and is
 *   used in Sputnik 1.9.x from 1.9.3 onwards.
 * </ol>
 *
 * <p>TLEs can be picked up from
 * <a href="http://www.celestrak.com/">http://www.celestrak.com/</a>.
 * Current data are split into various groups of satellites.  Check
 * <a href="http://www.celestrak.com/NORAD/elements/master.shtml">http://www.celestrak.com/NORAD/elements/master.shtml</a>
 * to look up a satellite and which file it is in.  Historical elements are
 * also available.
 * <a href="http://www.wingar.demon.co.uk/satevo/">http://www.wingar.demon.co.uk/satevo/</a>
 * contains a few sets of TLEs including one very large set satbase.zip.
 * Note, however, that these use modified formats for the first of the three
 * lines: additional numeric parameters may be present before and after
 * column 22 of the line that is by standard reserved for the common name and
 * is by standard exactly 22 characters long.
 *
 * <p>The position and velocity calculated here from the SGP4 and SDP4 models
 * is geocentric and (presumably) for the (mean?) equinox of date (the epoch
 * of the orbital elements).  It is unlikely to be for J2000 or B1950.
 *
 * <p>To use this class:
 *
 * <ol>
 *   <li>Create an instance of the class.
 *   <li>Invoke its Init() method.
 *   <li>Invoke its NoradByName() method to read the TLE for one satellite
 *   from a file with one or more TLEs for different satellites.
 *   <li>Invoke its GetPosVel() method with the Julian Date of interest.
 *   <li>Retrieve some of the public state variables itsR[], itsV[],
 *   itsEpochJD, itsDesignator, itsName, itsNumber.
 *   <li>Repeat GetPosVel() for different times.
 * </ol>
 *
 * <p>Copyright: &copy; 2003 Horst Meyerdierks.
 *
 * <p>This programme is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public Licence as
 * published by the Free Software Foundation; either version 2 of
 * the Licence, or (at your option) any later version.
 *
 * <p>This programme is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public Licence for more details.
 *
 * <p>You should have received a copy of the GNU General Public Licence
 * along with this programme; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 * <p>$Id: SDP4.java,v 4.1 2004/08/09 07:54:20 hme Exp $
 *
 * <dl>
 * <dt><strong>2.2:</strong> 2003/09/16 hme
 * <dd>Package review.
 * Rfndm now throws exception when parsing fails.
 * No longer catch IOExceptions, instead throw our own exceptions when
 * the satellite is not in the file or the TLEs are not in order.
 * <dt><strong>1.14:</strong> 2003/04/05 hme
 * <dd>Added NoradNext method.
 * <dt><strong>1.10:</strong> 2003/03/09 hme
 * <dd>Documentation.
 * <dt><strong>1.5:</strong> 2003/03/09 hme
 * <dd>Debugged.  Consistent with Kelso's Pascal port.
 * <dt><strong>1.1:</strong> 2003/03/06 hme
 * <dd>Translated from despagged Fortran code.
 * </dl>
 *
 * @author
 *   Horst Meyerdierks, c/o Royal Observatory,
 *   Blackford Hill, Edinburgh, EH9 3HJ, Scotland;
 *   &lt; hme &#64; roe.ac.uk &gt; */


public class SDP4 implements java.io.Serializable
{
  /** Position vector [Gm] */
  public double[] itsR;
  /** Velocity vector [km/s] */
  public double[] itsV;
  /** The name of the satellite. */
  public String itsName;
  /** Year and number of launch, plus part counter */
  public String itsDesignator;
  /** Number of the satellite from lines 1 or 2 */
  public int    itsNumber;
  /** The TLE epoch expressed in JD minus 2450000 days. */
  public double itsEpochJD;


  /** Whether period is >= 225 min */
  protected int    itsIsDeep;


  /**
   * A %f format for parsing a floating point number.
   *
   * <p>Specifying a hash means that , is not recognised as throusand separator
   * but taken to be the end of the numeric field.  Note that an "E" is also
   * taken to be the end of the numeric field, so 123E4 cannot be read
   * properly. */

  final static private DecimalFormat form  = new DecimalFormat("#");


  protected double E1_XMO,E1_XNODEO,E1_OMEGAO,E1_EO,E1_XINCL,
    E1_XNO,E1_XNDT2O,E1_XNDD6O,E1_BSTAR,E1_X,E1_Y,E1_Z,
    E1_XDOT,E1_YDOT,E1_ZDOT,E1_EPOCH,E1_DS50;

  protected double C1_CK2,C1_CK4,C1_E6A,C1_QOMS2T,C1_S,C1_TOTHRD,
    C1_XJ3,C1_XKE,C1_XKMPER,C1_XMNPDA,C1_AE;

  protected double C2_DE2RA,C2_PI,C2_PIO2,C2_TWOPI,C2_X3PIO2;

  protected double SGP4_A1,SGP4_A3OVK2,SGP4_AO,SGP4_AODP,SGP4_AYCOF,
    SGP4_BETAO,SGP4_BETAO2,SGP4_C1,SGP4_C1SQ,
    SGP4_C2,SGP4_C3,SGP4_C4,SGP4_C5,SGP4_COEF,SGP4_COEF1,
    SGP4_COSIO,SGP4_D2,SGP4_D3,SGP4_D4,SGP4_DEL1,SGP4_DELMO,
    SGP4_DELO,SGP4_EETA,
    SGP4_EOSQ,SGP4_ETA,SGP4_ETASQ,SGP4_OMGCOF,SGP4_OMGDOT,
    SGP4_PERIGE,SGP4_PINVSQ,SGP4_PSISQ,
    SGP4_QOMS24,SGP4_S4,SGP4_SINIO,SGP4_SINMO,SGP4_T2COF,
    SGP4_T3COF,SGP4_T4COF,SGP4_T5COF,SGP4_TEMP,SGP4_TEMP1,
    SGP4_TEMP2,SGP4_TEMP3,SGP4_THETA2,SGP4_THETA4,SGP4_TSI,
    SGP4_X1M5TH,SGP4_X1MTH2,SGP4_X3THM1,SGP4_X7THM1,
    SGP4_XHDOT1,SGP4_XLCOF,SGP4_XMCOF,SGP4_XMDOT,SGP4_XNODCF,
    SGP4_XNODOT,SGP4_XNODP;
  protected int SGP4_ISIMP;

  protected double SDP4_A1,SDP4_A3OVK2,SDP4_AO,SDP4_AODP,
    SDP4_AYCOF,SDP4_BETAO,SDP4_BETAO2,SDP4_C1,SDP4_C2,
    SDP4_C4,SDP4_COEF,SDP4_COEF1,SDP4_COSG,SDP4_COSIO,
    SDP4_DEL1,SDP4_DELO,SDP4_EETA,SDP4_EOSQ,
    SDP4_ETA,SDP4_ETASQ,SDP4_OMGDOT,SDP4_PERIGE,SDP4_PINVSQ,
    SDP4_PSISQ,SDP4_QOMS24,SDP4_S4,SDP4_SING,
    SDP4_SINIO,SDP4_T2COF,SDP4_TEMP1,SDP4_TEMP2,SDP4_TEMP3,
    SDP4_THETA2,SDP4_THETA4,SDP4_TSI,SDP4_X1M5TH,
    SDP4_X1MTH2,SDP4_X3THM1,SDP4_X7THM1,SDP4_XHDOT1,SDP4_XLCOF,
    SDP4_XMDOT,SDP4_XNODCF,SDP4_XNODOT,SDP4_XNODP;

  protected double DEEP_A1,DEEP_A2,DEEP_A3,DEEP_A4,DEEP_A5,DEEP_A6,
    DEEP_A7,DEEP_A8,DEEP_A9,DEEP_A10,DEEP_AINV2,DEEP_ALFDP,
    DEEP_AQNV,DEEP_ATIME,DEEP_BETDP,DEEP_BFACT,DEEP_C,DEEP_CC,
    DEEP_COSIS,DEEP_COSOK,DEEP_COSQ,DEEP_CTEM,DEEP_D2201,
    DEEP_D2211,DEEP_D3210,DEEP_D3222,DEEP_D4410,DEEP_D4422,
    DEEP_D5220,DEEP_D5232,DEEP_D5421,DEEP_D5433,DEEP_DALF,
    DEEP_DAY,DEEP_DBET,DEEP_DEL1,DEEP_DEL2,DEEP_DEL3,DEEP_DELT,
    DEEP_DLS,DEEP_E3,DEEP_EE2,DEEP_EOC,DEEP_EQ,DEEP_F2,
    DEEP_F220,DEEP_F221,DEEP_F3,DEEP_F311,DEEP_F321,DEEP_F322,
    DEEP_F330,DEEP_F441,DEEP_F442,DEEP_F522,DEEP_F523,
    DEEP_F542,DEEP_F543,DEEP_FASX2,DEEP_FASX4,DEEP_FASX6,
    DEEP_FT,DEEP_G200,DEEP_G201,DEEP_G211,DEEP_G300,DEEP_G310,
    DEEP_G322,DEEP_G410,DEEP_G422,DEEP_G520,DEEP_G521,DEEP_G532,
    DEEP_G533,DEEP_GAM,DEEP_OMEGAQ,DEEP_PE,DEEP_PGH,DEEP_PH,
    DEEP_PINC,DEEP_PL,DEEP_PREEP,DEEP_S1,DEEP_S2,
    DEEP_S3,DEEP_S4,DEEP_S5,DEEP_S6,DEEP_S7,DEEP_SAVTSN,DEEP_SE,
    DEEP_SE2,DEEP_SE3,DEEP_SEL,DEEP_SES,DEEP_SGH,DEEP_SGH2,
    DEEP_SGH3,DEEP_SGH4,DEEP_SGHL,DEEP_SGHS,DEEP_SH,DEEP_SH2,
    DEEP_SH3,DEEP_SH1,DEEP_SHS,DEEP_SI,DEEP_SI2,DEEP_SI3,
    DEEP_SIL,DEEP_SINI2,DEEP_SINIS,DEEP_SINOK,DEEP_SINQ,
    DEEP_SINZF,DEEP_SIS,DEEP_SL,DEEP_SL2,DEEP_SL3,DEEP_SL4,
    DEEP_SLL,DEEP_SLS,DEEP_SSE,DEEP_SSG,DEEP_SSH,DEEP_SSI,
    DEEP_SSL,DEEP_STEM,DEEP_STEP2,DEEP_STEPN,DEEP_STEPP,
    DEEP_TEMP,DEEP_TEMP1,DEEP_THGR,DEEP_X1,DEEP_X2,DEEP_X2LI,
    DEEP_X2OMI,DEEP_X3,DEEP_X4,DEEP_X5,DEEP_X6,DEEP_X7,DEEP_X8,
    DEEP_XFACT,DEEP_XGH2,DEEP_XGH3,DEEP_XGH4,DEEP_XH2,DEEP_XH3,
    DEEP_XI2,DEEP_XI3,DEEP_XL,DEEP_XL2,DEEP_XL3,DEEP_XL4,
    DEEP_XLAMO,DEEP_XLDOT,DEEP_XLI,DEEP_XLS,
    DEEP_XMAO,DEEP_XNDDT,DEEP_XNDOT,DEEP_XNI,DEEP_XNO2,
    DEEP_XNODCE,DEEP_XNOI,DEEP_XNQ,DEEP_XOMI,DEEP_XPIDOT,
    DEEP_XQNCL,DEEP_Z1,DEEP_Z11,DEEP_Z12,DEEP_Z13,DEEP_Z2,
    DEEP_Z21,DEEP_Z22,DEEP_Z23,DEEP_Z3,DEEP_Z31,DEEP_Z32,
    DEEP_Z33,DEEP_ZCOSG,DEEP_ZCOSGL,DEEP_ZCOSH,DEEP_ZCOSHL,
    DEEP_ZCOSI,DEEP_ZCOSIL,DEEP_ZE,DEEP_ZF,DEEP_ZM,DEEP_ZMO,
    DEEP_ZMOL,DEEP_ZMOS,DEEP_ZN,DEEP_ZSING,DEEP_ZSINGL,
    DEEP_ZSINH,DEEP_ZSINHL,DEEP_ZSINI,DEEP_ZSINIL,DEEP_ZX,DEEP_ZY;
  protected int DEEP_IRESFL,DEEP_ISYNFL,DEEP_IRET,DEEP_IRETN,DEEP_LS;

  protected double DEEP_ZNS,DEEP_C1SS,DEEP_ZES,DEEP_ZNL,DEEP_C1L,
    DEEP_ZEL,DEEP_ZCOSIS,DEEP_ZSINIS,DEEP_ZSINGS,
    DEEP_ZCOSGS,DEEP_Q22,DEEP_Q31,DEEP_Q33,DEEP_G22,DEEP_G32,
    DEEP_G44,DEEP_G52,DEEP_G54,
    DEEP_ROOT22,DEEP_ROOT32,DEEP_ROOT44,DEEP_ROOT52,DEEP_ROOT54,
    DEEP_THDT;

  protected double DPINI_EQSQ,DPINI_SINIQ,DPINI_COSIQ,
    DPINI_RTEQSQ,DPINI_AO,DPINI_COSQ2,DPINI_SINOMO,DPINI_COSOMO,
    DPINI_BSQ,DPINI_XLLDOT,DPINI_OMGDT,DPINI_XNODOT,DPINI_XNODP;

  protected double DPSEC_XLL,DPSEC_OMGASM,DPSEC_XNODES,DPSEC_EM,
    DPSEC_XINC,DPSEC_XN,DPSEC_T;


  // constructor -- Shawn E. Gano - used to set deciaml seperator to always be '.'
  public SDP4()
  {
      // DecimalFormat form  = new DecimalFormat("#");
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator('.');
      form.setDecimalFormatSymbols(dfs);
  }
  
  /**
   * Initialise the SDP4.
   *
   * <p>This initialises the SDP4 object.  Most state variables are set to
   * zero, some to constants necessary for the calculations. */

  public void Init()
  {
    double QO, SO, XJ2, XJ4;

    itsR = new double[3];
    itsV = new double[3];
    itsR[0] = 0.01; itsR[1] = 0.; itsR[2] = 0.;
    itsV[0] = 0.;   itsV[1] = 0.; itsV[2] = 0.;
    itsName       = "Unspecified satellite";
    itsDesignator = "99999Z";
    itsNumber     = 88888;
    itsEpochJD    = 0.;

    /* Initialise /E1/. */

    E1_XMO = 0.;
    E1_XNODEO = 0.;
    E1_OMEGAO = 0.;
    E1_EO = 0.;
    E1_XINCL = 0.;
    E1_XNO = 0.;
    E1_XNDT2O = 0.;
    E1_XNDD6O = 0.;
    E1_BSTAR = 0.;
    E1_X = 0.;
    E1_Y = 0.;
    E1_Z = 0.;
    E1_XDOT = 0.;
    E1_YDOT = 0.;
    E1_ZDOT = 0.;
    E1_EPOCH = 0.;
    E1_DS50 = 0.;

    /* Initialise /C1/. */

    C1_E6A = 1.E-6;
    C1_TOTHRD = .66666667;
    C1_XJ3 = -.253881E-5;
    C1_XKE = .743669161E-1;
    C1_XKMPER = 6378.135;
    C1_XMNPDA = 1440.;
    C1_AE = 1.;

    QO = 120.0;
    SO = 78.0;
    XJ2 = 1.082616E-3;
    XJ4 = -1.65597E-6;
    C1_CK2 = .5 * XJ2 * C1_AE * C1_AE;
    C1_CK4 = -.375 * XJ4 * C1_AE * C1_AE * C1_AE * C1_AE;
    C1_QOMS2T  = ((QO - SO) * C1_AE / C1_XKMPER);
    C1_QOMS2T *= C1_QOMS2T;
    C1_QOMS2T *= C1_QOMS2T;
    C1_S = C1_AE * (1. + SO / C1_XKMPER);

    /* Initialise /C2/. */

    C2_DE2RA = .174532925E-1;
    C2_PI = 3.14159265;
    C2_PIO2 = 1.57079633;
    C2_TWOPI = 6.2831853;
    C2_X3PIO2 = 4.71238898;

    /* Initialisation of /COMSGP4/. */

    SGP4_A1 = 0.;
    SGP4_A3OVK2 = 0.;
    SGP4_AO = 0.;
    SGP4_AODP = 0.;
    SGP4_AYCOF = 0.;
    SGP4_BETAO = 0.;
    SGP4_BETAO2 = 0.;
    SGP4_C1 = 0.;
    SGP4_C1SQ = 0.;
    SGP4_C2 = 0.;
    SGP4_C3 = 0.;
    SGP4_C4 = 0.;
    SGP4_C5 = 0.;
    SGP4_COEF = 0.;
    SGP4_COEF1 = 0.;
    SGP4_COSIO = 0.;
    SGP4_D2 = 0.;
    SGP4_D3 = 0.;
    SGP4_D4 = 0.;
    SGP4_DEL1 = 0.;
    SGP4_DELMO = 0.;
    SGP4_DELO = 0.;
    SGP4_EETA = 0.;
    SGP4_EOSQ = 0.;
    SGP4_ETA = 0.;
    SGP4_ETASQ = 0.;
    SGP4_OMGCOF = 0.;
    SGP4_OMGDOT = 0.;
    SGP4_PERIGE = 0.;
    SGP4_PINVSQ = 0.;
    SGP4_PSISQ = 0.;
    SGP4_QOMS24 = 0.;
    SGP4_S4 = 0.;
    SGP4_SINIO = 0.;
    SGP4_SINMO = 0.;
    SGP4_T2COF = 0.;
    SGP4_T3COF = 0.;
    SGP4_T4COF = 0.;
    SGP4_T5COF = 0.;
    SGP4_TEMP = 0.;
    SGP4_TEMP1 = 0.;
    SGP4_TEMP2 = 0.;
    SGP4_TEMP3 = 0.;
    SGP4_THETA2 = 0.;
    SGP4_THETA4 = 0.;
    SGP4_TSI = 0.;
    SGP4_X1M5TH = 0.;
    SGP4_X1MTH2 = 0.;
    SGP4_X3THM1 = 0.;
    SGP4_X7THM1 = 0.;
    SGP4_XHDOT1 = 0.;
    SGP4_XLCOF = 0.;
    SGP4_XMCOF = 0.;
    SGP4_XMDOT = 0.;
    SGP4_XNODCF = 0.;
    SGP4_XNODOT = 0.;
    SGP4_XNODP = 0.;
    SGP4_ISIMP = 0;

    /* Initialisation of /COMSDP4/. */

    SDP4_A1 = 0.;
    SDP4_A3OVK2 = 0.;
    SDP4_AO = 0.;
    SDP4_AODP = 0.;
    SDP4_AYCOF = 0.;
    SDP4_BETAO = 0.;
    SDP4_BETAO2 = 0.;
    SDP4_C1 = 0.;
    SDP4_C2 = 0.;
    SDP4_C4 = 0.;
    SDP4_COEF = 0.;
    SDP4_COEF1 = 0.;
    SDP4_COSG = 0.;
    SDP4_COSIO = 0.;
    SDP4_DEL1 = 0.;
    SDP4_DELO = 0.;
    SDP4_EETA = 0.;
    SDP4_EOSQ = 0.;
    SDP4_ETA = 0.;
    SDP4_ETASQ = 0.;
    SDP4_OMGDOT = 0.;
    SDP4_PERIGE = 0.;
    SDP4_PINVSQ = 0.;
    SDP4_PSISQ = 0.;
    SDP4_QOMS24 = 0.;
    SDP4_S4 = 0.;
    SDP4_SING = 0.;
    SDP4_SINIO = 0.;
    SDP4_T2COF = 0.;
    SDP4_TEMP1 = 0.;
    SDP4_TEMP2 = 0.;
    SDP4_TEMP3 = 0.;
    SDP4_THETA2 = 0.;
    SDP4_THETA4 = 0.;
    SDP4_TSI = 0.;
    SDP4_X1M5TH = 0.;
    SDP4_X1MTH2 = 0.;
    SDP4_X3THM1 = 0.;
    SDP4_X7THM1 = 0.;
    SDP4_XHDOT1 = 0.;
    SDP4_XLCOF = 0.;
    SDP4_XMDOT = 0.;
    SDP4_XNODCF = 0.;
    SDP4_XNODOT = 0.;
    SDP4_XNODP = 0.;

    /* Initialisation of /COMDEEP1/. */

    DEEP_A1 = 0.;
    DEEP_A2 = 0.;
    DEEP_A3 = 0.;
    DEEP_A4 = 0.;
    DEEP_A5 = 0.;
    DEEP_A6 = 0.;
    DEEP_A7 = 0.;
    DEEP_A8 = 0.;
    DEEP_A9 = 0.;
    DEEP_A10 = 0.;
    DEEP_AINV2 = 0.;
    DEEP_ALFDP = 0.;
    DEEP_AQNV = 0.;
    DEEP_ATIME = 0.;
    DEEP_BETDP = 0.;
    DEEP_BFACT = 0.;
    DEEP_C = 0.;
    DEEP_CC = 0.;
    DEEP_COSIS = 0.;
    DEEP_COSOK = 0.;
    DEEP_COSQ = 0.;
    DEEP_CTEM = 0.;
    DEEP_D2201 = 0.;
    DEEP_D2211 = 0.;
    DEEP_D3210 = 0.;
    DEEP_D3222 = 0.;
    DEEP_D4410 = 0.;
    DEEP_D4422 = 0.;
    DEEP_D5220 = 0.;
    DEEP_D5232 = 0.;
    DEEP_D5421 = 0.;
    DEEP_D5433 = 0.;
    DEEP_DALF = 0.;
    DEEP_DAY = 0.;
    DEEP_DBET = 0.;
    DEEP_DEL1 = 0.;
    DEEP_DEL2 = 0.;
    DEEP_DEL3 = 0.;
    DEEP_DELT = 0.;
    DEEP_DLS = 0.;
    DEEP_E3 = 0.;
    DEEP_EE2 = 0.;
    DEEP_EOC = 0.;
    DEEP_EQ = 0.;
    DEEP_F2 = 0.;
    DEEP_F220 = 0.;
    DEEP_F221 = 0.;
    DEEP_F3 = 0.;
    DEEP_F311 = 0.;
    DEEP_F321 = 0.;
    DEEP_F322 = 0.;
    DEEP_F330 = 0.;
    DEEP_F441 = 0.;
    DEEP_F442 = 0.;
    DEEP_F522 = 0.;
    DEEP_F523 = 0.;
    DEEP_F542 = 0.;
    DEEP_F543 = 0.;
    DEEP_FASX2 = 0.;
    DEEP_FASX4 = 0.;
    DEEP_FASX6 = 0.;
    DEEP_FT = 0.;
    DEEP_G200 = 0.;
    DEEP_G201 = 0.;
    DEEP_G211 = 0.;
    DEEP_G300 = 0.;
    DEEP_G310 = 0.;
    DEEP_G322 = 0.;
    DEEP_G410 = 0.;
    DEEP_G422 = 0.;
    DEEP_G520 = 0.;
    DEEP_G521 = 0.;
    DEEP_G532 = 0.;
    DEEP_G533 = 0.;
    DEEP_GAM = 0.;
    DEEP_OMEGAQ = 0.;
    DEEP_PE = 0.;
    DEEP_PGH = 0.;
    DEEP_PH = 0.;

    /* Initialisation of /COMDEEP2/. */

    DEEP_PINC = 0.;
    DEEP_PL = 0.;
    DEEP_PREEP = 0.;
    DEEP_S1 = 0.;
    DEEP_S2 = 0.;
    DEEP_S3 = 0.;
    DEEP_S4 = 0.;
    DEEP_S5 = 0.;
    DEEP_S6 = 0.;
    DEEP_S7 = 0.;
    DEEP_SAVTSN = 0.;
    DEEP_SE = 0.;
    DEEP_SE2 = 0.;
    DEEP_SE3 = 0.;
    DEEP_SEL = 0.;
    DEEP_SES = 0.;
    DEEP_SGH = 0.;
    DEEP_SGH2 = 0.;
    DEEP_SGH3 = 0.;
    DEEP_SGH4 = 0.;
    DEEP_SGHL = 0.;
    DEEP_SGHS = 0.;
    DEEP_SH = 0.;
    DEEP_SH2 = 0.;
    DEEP_SH3 = 0.;
    DEEP_SH1 = 0.;
    DEEP_SHS = 0.;
    DEEP_SI = 0.;
    DEEP_SI2 = 0.;
    DEEP_SI3 = 0.;
    DEEP_SIL = 0.;
    DEEP_SINI2 = 0.;
    DEEP_SINIS = 0.;
    DEEP_SINOK = 0.;
    DEEP_SINQ = 0.;
    DEEP_SINZF = 0.;
    DEEP_SIS = 0.;
    DEEP_SL = 0.;
    DEEP_SL2 = 0.;
    DEEP_SL3 = 0.;
    DEEP_SL4 = 0.;
    DEEP_SLL = 0.;
    DEEP_SLS = 0.;
    DEEP_SSE = 0.;
    DEEP_SSG = 0.;
    DEEP_SSH = 0.;
    DEEP_SSI = 0.;
    DEEP_SSL = 0.;
    DEEP_STEM = 0.;
    DEEP_STEP2 = 0.;
    DEEP_STEPN = 0.;
    DEEP_STEPP = 0.;
    DEEP_TEMP = 0.;
    DEEP_TEMP1 = 0.;
    DEEP_THGR = 0.;
    DEEP_X1 = 0.;
    DEEP_X2 = 0.;
    DEEP_X2LI = 0.;
    DEEP_X2OMI = 0.;
    DEEP_X3 = 0.;
    DEEP_X4 = 0.;
    DEEP_X5 = 0.;
    DEEP_X6 = 0.;
    DEEP_X7 = 0.;
    DEEP_X8 = 0.;
    DEEP_XFACT = 0.;
    DEEP_XGH2 = 0.;
    DEEP_XGH3 = 0.;
    DEEP_XGH4 = 0.;
    DEEP_XH2 = 0.;
    DEEP_XH3 = 0.;
    DEEP_XI2 = 0.;
    DEEP_XI3 = 0.;
    DEEP_XL = 0.;
    DEEP_XL2 = 0.;
    DEEP_XL3 = 0.;
    DEEP_XL4 = 0.;

    /* Initialisation of /COMDEEP3/. */

    DEEP_XLAMO = 0.;
    DEEP_XLDOT = 0.;
    DEEP_XLI = 0.;
    DEEP_XLS = 0.;
    DEEP_XMAO = 0.;
    DEEP_XNDDT = 0.;
    DEEP_XNDOT = 0.;
    DEEP_XNI = 0.;
    DEEP_XNO2 = 0.;
    DEEP_XNODCE = 0.;
    DEEP_XNOI = 0.;
    DEEP_XNQ = 0.;
    DEEP_XOMI = 0.;
    DEEP_XPIDOT = 0.;
    DEEP_XQNCL = 0.;
    DEEP_Z1 = 0.;
    DEEP_Z11 = 0.;
    DEEP_Z12 = 0.;
    DEEP_Z13 = 0.;
    DEEP_Z2 = 0.;
    DEEP_Z21 = 0.;
    DEEP_Z22 = 0.;
    DEEP_Z23 = 0.;
    DEEP_Z3 = 0.;
    DEEP_Z31 = 0.;
    DEEP_Z32 = 0.;
    DEEP_Z33 = 0.;
    DEEP_ZCOSG = 0.;
    DEEP_ZCOSGL = 0.;
    DEEP_ZCOSH = 0.;
    DEEP_ZCOSHL = 0.;
    DEEP_ZCOSI = 0.;
    DEEP_ZCOSIL = 0.;
    DEEP_ZE = 0.;
    DEEP_ZF = 0.;
    DEEP_ZM = 0.;
    DEEP_ZMO = 0.;
    DEEP_ZMOL = 0.;
    DEEP_ZMOS = 0.;
    DEEP_ZN = 0.;
    DEEP_ZSING = 0.;
    DEEP_ZSINGL = 0.;
    DEEP_ZSINH = 0.;
    DEEP_ZSINHL = 0.;
    DEEP_ZSINI = 0.;
    DEEP_ZSINIL = 0.;
    DEEP_ZX = 0.;
    DEEP_ZY = 0.;
    DEEP_IRESFL = 0;
    DEEP_ISYNFL = 0;
    DEEP_IRET = 0;
    DEEP_IRETN = 0;
    DEEP_LS = 0;

    /* Initialisation of /COMDEEP4/. */

    DEEP_ZNS = 1.19459E-5;
    DEEP_C1SS = 2.9864797E-6;
    DEEP_ZES = 0.01675;
    DEEP_ZNL = 1.5835218E-4;
    DEEP_C1L = 4.7968065E-7;
    DEEP_ZEL = 0.05490;
    DEEP_ZCOSIS = 0.91744867;
    DEEP_ZSINIS = 0.39785416;
    DEEP_ZSINGS = -0.98088458;
    DEEP_ZCOSGS = 0.1945905;
    DEEP_Q22 = 1.7891679E-6;
    DEEP_Q31 = 2.1460748E-6;
    DEEP_Q33 = 2.2123015E-7;
    DEEP_G22 = 5.7686396;
    DEEP_G32 = 0.95240898;
    DEEP_G44 = 1.8014998;
    DEEP_G52 = 1.0508330;
    DEEP_G54 = 4.4108898;
    DEEP_ROOT22 = 1.7891679E-6;
    DEEP_ROOT32 = 3.7393792E-7;
    DEEP_ROOT44 = 7.3636953E-9;
    DEEP_ROOT52 = 1.1428639E-7;
    DEEP_ROOT54 = 2.1765803E-9;
    DEEP_THDT = 4.3752691E-3;

    return;
  }


  /**
   * Test routine.
   *
   * <p>This test uses a spectrum of satellites with different periods
   * contained in a local file <code>test.tle</code>. */

  public final void driver2()
  {
    double theEpoch, theTsince;

    Init();

    try {NoradByName("test.tle", "Cosmos 2388 casing");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSGP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "STS-110");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSGP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "Jason 1 DPAF");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSGP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "Cosmos 1149 deb Q");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSGP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "Cosmos 1149 deb P");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSGP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "GPS B2A-11 PAM-D deb F");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSGP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "GPS 2-18 PAM-D deb D");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSGP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "GPS 2-26 PAM-D deb D");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "Cos 2382 aux motor");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "Intelsat 904 Ariane 44");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "CRRES deb W (canister)");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "NSS-7 Ariane 44L r");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "SL-06 R/B (2)");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "Cosmos 2388");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "NSS-7");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "Intelsat 903");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "Ekran 21 Breeze-M r");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    try {NoradByName("test.tle", "MAP");}
    catch(Exception e) {System.err.println(e); return;}
    theEpoch = 2384.5;
    System.out.println(
      "\nSDP4 TSINCE              X                Y                Z\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print(theTsince);     System.out.print("  ");
      System.out.print(1E6 * itsR[0]); System.out.print("  ");
      System.out.print(1E6 * itsR[1]); System.out.print("  ");
      System.out.print(1E6 * itsR[2]); System.out.println();
    }
    System.out.println(
      "\n\n                      XDOT             YDOT             ZDOT\n");
    for (theTsince = 0; theTsince < 1500.; theTsince += 360.) {
      GetPosVel(theEpoch + theTsince / 1440.);
      System.out.print("      ");
      System.out.print(itsV[0]); System.out.print("  ");
      System.out.print(itsV[1]); System.out.print("  ");
      System.out.print(itsV[2]); System.out.println();
    }

    return;
  }

  // Same as next method but input is standard Julian Date
  public final void GetPosVelJulDate(double julDate)
  {
    GetPosVel( (julDate-2450000) );
  }
  
  /**
   * Calculate position and velocity.
   *
   * <p>This integrates the satellite orbit read previously
   * and thereby calculates the state variables itsR[] (in Gm) and itsV[]
   * (in km/s) for the time given.
   *
   * @param aJulDate
   *   The time for which the calculation should take place.  This must be
   *   given in units of days as the Julian Day minus 2450000 days. */

  public final void GetPosVel(double aJulDate)
  {
    double[] TS = new double[1];
    int[] IFLAG = new int[1];

    TS[0] = C1_XMNPDA * (aJulDate - itsEpochJD);

    IFLAG[0] = 1;
    if (itsIsDeep == 0) {RunSGP4(IFLAG, TS);}
    else                {RunSDP4(IFLAG, TS);}

    return;
  }


  /**
   * Read one named NORAD TLE from file.
   *
   * <p>This method scans the named file for the given satellite name.  It
   * reads the two-line NORAD element set from the two lines behind the first
   * occurence of the satellite name.
   *
   * <p>Although we look for two-line elements there are three lines per
   * satellite.  For the format see the
   * {@link #NoradNext NoradNext} and {@link #ReadNorad12 ReadNorad12} methods.
   *
   * @param aFileName
   *   The name of the file that contains the TLE line triplets.
   * @param aName
   *   The name of the satellite, for comparison with the first line of the
   *   line triplets.  This name and the names read from file are stripped
   *   of leading and trailing blanks before comparison.  But they have to
   *   be equal in length and case-sensitive content, including any internal
   *   space characters. */

  public final void NoradByName(String aFileName, String aName)
    throws SDP4NoSatException, SDP4NoLineOneException,
	   SDP4InvalidNumException, IOException
  {
    BufferedReader theFile;
    String  theLine, theName;
    int     i;
    boolean success = false;

    /* Trim the given name. */

    theName = aName.trim();

    /* Open the file. */

    theFile = new BufferedReader(new FileReader(aFileName));

    /* Forward until the satellite name is found (line 0). */

    for (;;) {
      if ((theLine = theFile.readLine()) == null) break;
      theLine = theLine.trim();
      if (theLine.equals(theName)) {success = true; break;}
    }

    if (!success) {
      theFile.close();
      throw new SDP4NoSatException("no such satellite");
    }

    /* Copy name, should be 22 characters exactly.
     * If the line from file is shorter, we use all of it.
     * If the line from file is longer. */

    if (22 >= theLine.length()) {itsName = theLine;}
    else {itsName = theLine.substring(0, 22);}
    itsName = itsName.trim();

    /* Read lines 1 and 2. */

    ReadNorad12(theFile);

    /* Close the file. */

    theFile.close();

    return;
  }


  /**
   * Read one NORAD TLE from file.
   *
   * <p>This method reads the two-line NORAD element set from the next three
   * lines from the current position of the given file.  The file is left
   * positioned behind the lines read, ready for the next call of this method.
   *
   * <p>Although we look for two-line elements there are three lines per
   * satellite.  The format of the first line is
   *
<pre>
AAAAAAAAAAAAAAAAAAAAAA
</pre>
   *
   * <p>It is 22 characters long and contains the common name of
   * the satellite.  For the other two lines see the
   * {@link #ReadNorad12 ReadNorad12} method.
   *
   * @param aFile
   *   The file that is already open and contains the TLE line triplets. */

  public final void NoradNext(BufferedReader aFile)
    throws SDP4NoLineOneException, SDP4InvalidNumException, IOException
  {
    String  theLine;
    int     i;

    /* Read the line with the satellite name. */

    theLine = aFile.readLine();
    theLine = theLine.trim();

    /* Copy name, should be 22 characters exactly.
     * If the line from file is shorter, we use all of it.
     * If the line from file is longer. */

    if (22 >= theLine.length()) {itsName = theLine;}
    else {itsName = theLine.substring(0, 22);}
    itsName = itsName.trim();

    /* Read lines 1 and 2. */

    ReadNorad12(aFile);

    return;
  }
  
  // overloaded version of NoradNext, where the user speficies the name and two lines of the TLE
  // instead of specifiing a file
  public final void NoradLoadTLE(String name, String tle1, String tle2)
  throws SDP4NoLineOneException, SDP4InvalidNumException, IOException
{
  String  theLine;
  int     i;

  /* Read the line with the satellite name. */

  //theLine = aFile.readLine();
  //theLine = theLine.trim();

  /* Copy name, should be 22 characters exactly.
   * If the line from file is shorter, we use all of it.
   * If the line from file is longer. */

  //if (22 >= theLine.length()) {itsName = theLine;}
  //else {itsName = theLine.substring(0, 22);}
  //itsName = itsName.trim();
  itsName = name; // save name
  
  
  /* Read lines 1 and 2. */

  ReadNorad12(tle1,tle2);

  return;
}


  /**
   * A helper routine to calculate the two-dimensional inverse tangens. */

  protected final double ACTAN(double SINX, double COSX)
  {
    double value, TEMP;

    if (COSX == 0.) {
      if (SINX == 0.) {
        value = 0.;
      }
      else if (SINX > 0.) {
	value = C2_PIO2;
      }
      else {
	value = C2_X3PIO2;
      }
    }
    else if (COSX > 0.) {
      if (SINX == 0.) {
	value = 0.;
      }
      else if (SINX > 0.) {
	TEMP = SINX / COSX;
        value = Math.atan(TEMP);
      }
      else {
	value = C2_TWOPI;
	TEMP = SINX / COSX;
	value = value + Math.atan(TEMP);
      }
    }
    else {
      value = C2_PI;
      TEMP = SINX / COSX;
      value = value + Math.atan(TEMP);
    }

    return value;
  }


  /**
   * Deep space initialisation. */

  protected final void DEEP1()
  {
    DEEP_THGR = THETAG(E1_EPOCH);
    DEEP_EQ = E1_EO;
    DEEP_XNQ = DPINI_XNODP;
    DEEP_AQNV = 1./DPINI_AO;
    DEEP_XQNCL = E1_XINCL;
    DEEP_XMAO = E1_XMO;
    DEEP_XPIDOT = DPINI_OMGDT + DPINI_XNODOT;
    DEEP_SINQ = Math.sin(E1_XNODEO);
    DEEP_COSQ = Math.cos(E1_XNODEO);
    DEEP_OMEGAQ = E1_OMEGAO;

    /* Initialise lunar solar terms. */

    DEEP_DAY = E1_DS50 + 18261.5;
    if (DEEP_DAY != DEEP_PREEP) {
      DEEP_PREEP = DEEP_DAY;
      DEEP_XNODCE = 4.5236020 - 9.2422029E-4 * DEEP_DAY;
      DEEP_STEM = Math.sin(DEEP_XNODCE);
      DEEP_CTEM = Math.cos(DEEP_XNODCE);
      DEEP_ZCOSIL = .91375164 - .03568096 * DEEP_CTEM;
      DEEP_ZSINIL = Math.sqrt(1. - DEEP_ZCOSIL * DEEP_ZCOSIL);
      DEEP_ZSINHL = .089683511 * DEEP_STEM / DEEP_ZSINIL;
      DEEP_ZCOSHL = Math.sqrt(1. - DEEP_ZSINHL * DEEP_ZSINHL);
      DEEP_C = 4.7199672 + .22997150 * DEEP_DAY;
      DEEP_GAM = 5.8351514 + .0019443680 * DEEP_DAY;
      DEEP_ZMOL = FMOD2P(DEEP_C - DEEP_GAM);
      DEEP_ZX = .39785416 * DEEP_STEM / DEEP_ZSINIL;
      DEEP_ZY = DEEP_ZCOSHL * DEEP_CTEM + 0.91744867 * DEEP_ZSINHL * DEEP_STEM;
      DEEP_ZX = ACTAN(DEEP_ZX, DEEP_ZY);
      DEEP_ZX = DEEP_GAM + DEEP_ZX - DEEP_XNODCE;
      DEEP_ZCOSGL = Math.cos(DEEP_ZX);
      DEEP_ZSINGL = Math.sin(DEEP_ZX);
      DEEP_ZMOS = 6.2565837 + .017201977 * DEEP_DAY;
      DEEP_ZMOS = FMOD2P(DEEP_ZMOS);
    }

    /* Do solar terms. */

    DEEP_SAVTSN = 1.E20;
    DEEP_ZCOSG = DEEP_ZCOSGS;
    DEEP_ZSING = DEEP_ZSINGS;
    DEEP_ZCOSI = DEEP_ZCOSIS;
    DEEP_ZSINI = DEEP_ZSINIS;
    DEEP_ZCOSH = DEEP_COSQ;
    DEEP_ZSINH = DEEP_SINQ;
    DEEP_CC = DEEP_C1SS;
    DEEP_ZN = DEEP_ZNS;
    DEEP_ZE = DEEP_ZES;
    DEEP_ZMO = DEEP_ZMOS;
    DEEP_XNOI = 1./DEEP_XNQ;

    /* First pass through label 20. */

    DEEP_A1  =  DEEP_ZCOSG * DEEP_ZCOSH + DEEP_ZSING * DEEP_ZCOSI * DEEP_ZSINH;
    DEEP_A3  = -DEEP_ZSING * DEEP_ZCOSH + DEEP_ZCOSG * DEEP_ZCOSI * DEEP_ZSINH;
    DEEP_A7  = -DEEP_ZCOSG * DEEP_ZSINH + DEEP_ZSING * DEEP_ZCOSI * DEEP_ZCOSH;
    DEEP_A8  =  DEEP_ZSING * DEEP_ZSINI;
    DEEP_A9  =  DEEP_ZSING * DEEP_ZSINH + DEEP_ZCOSG * DEEP_ZCOSI * DEEP_ZCOSH;
    DEEP_A10 =  DEEP_ZCOSG * DEEP_ZSINI;
    DEEP_A2  =  DPINI_COSIQ * DEEP_A7 + DPINI_SINIQ * DEEP_A8;
    DEEP_A4  =  DPINI_COSIQ * DEEP_A9 + DPINI_SINIQ * DEEP_A10;
    DEEP_A5  = -DPINI_SINIQ * DEEP_A7 + DPINI_COSIQ * DEEP_A8;
    DEEP_A6  = -DPINI_SINIQ * DEEP_A9 + DPINI_COSIQ * DEEP_A10;

    DEEP_X1 =  DEEP_A1 * DPINI_COSOMO + DEEP_A2 * DPINI_SINOMO;
    DEEP_X2 =  DEEP_A3 * DPINI_COSOMO + DEEP_A4 * DPINI_SINOMO;
    DEEP_X3 = -DEEP_A1 * DPINI_SINOMO + DEEP_A2 * DPINI_COSOMO;
    DEEP_X4 = -DEEP_A3 * DPINI_SINOMO + DEEP_A4 * DPINI_COSOMO;
    DEEP_X5 =  DEEP_A5 * DPINI_SINOMO;
    DEEP_X6 =  DEEP_A6 * DPINI_SINOMO;
    DEEP_X7 =  DEEP_A5 * DPINI_COSOMO;
    DEEP_X8 =  DEEP_A6 * DPINI_COSOMO;

    DEEP_Z31 = 12. * DEEP_X1 * DEEP_X1 - 3. * DEEP_X3 * DEEP_X3;
    DEEP_Z32 = 24. * DEEP_X1 * DEEP_X2 - 6. * DEEP_X3 * DEEP_X4;
    DEEP_Z33 = 12. * DEEP_X2 * DEEP_X2 - 3. * DEEP_X4 * DEEP_X4;
    DEEP_Z1  =  3. * (DEEP_A1 * DEEP_A1 + DEEP_A2 * DEEP_A2)
      + DEEP_Z31 * DPINI_EQSQ;
    DEEP_Z2  =  6. * (DEEP_A1 * DEEP_A3 + DEEP_A2 * DEEP_A4)
      + DEEP_Z32 * DPINI_EQSQ;
    DEEP_Z3  =  3. * (DEEP_A3 * DEEP_A3 + DEEP_A4 * DEEP_A4)
      + DEEP_Z33 * DPINI_EQSQ;
    DEEP_Z11 = -6. * DEEP_A1 * DEEP_A5
      + DPINI_EQSQ * (-24. * DEEP_X1 * DEEP_X7 - 6. * DEEP_X3 * DEEP_X5);
    DEEP_Z12 = -6. * (DEEP_A1 *DEEP_A6 + DEEP_A3 * DEEP_A5)
      + DPINI_EQSQ * (-24. * (DEEP_X2 * DEEP_X7 + DEEP_X1 * DEEP_X8)
		      - 6. * (DEEP_X3 * DEEP_X6 + DEEP_X4 * DEEP_X5));
    DEEP_Z13 = -6. * DEEP_A3 * DEEP_A6
      + DPINI_EQSQ * (-24. * DEEP_X2 * DEEP_X8 - 6. * DEEP_X4 * DEEP_X6);
    DEEP_Z21 =  6. * DEEP_A2 * DEEP_A5
      + DPINI_EQSQ * ( 24. * DEEP_X1 * DEEP_X5 - 6. * DEEP_X3 * DEEP_X7);
    DEEP_Z22 =  6. * (DEEP_A4 * DEEP_A5 + DEEP_A2 * DEEP_A6)
      + DPINI_EQSQ * ( 24. * (DEEP_X2 * DEEP_X5 + DEEP_X1 * DEEP_X6)
		      - 6. * (DEEP_X4 * DEEP_X7 + DEEP_X3 * DEEP_X8));
    DEEP_Z23 =  6. * DEEP_A4 * DEEP_A6
      + DPINI_EQSQ * ( 24. * DEEP_X2 * DEEP_X6 - 6. * DEEP_X4 * DEEP_X8);
    DEEP_Z1 =  DEEP_Z1 + DEEP_Z1 + DPINI_BSQ * DEEP_Z31;
    DEEP_Z2 =  DEEP_Z2 + DEEP_Z2 + DPINI_BSQ * DEEP_Z32;
    DEEP_Z3 =  DEEP_Z3 + DEEP_Z3 + DPINI_BSQ * DEEP_Z33;
    DEEP_S3 =  DEEP_CC * DEEP_XNOI;
    DEEP_S2 = -.5 * DEEP_S3 / DPINI_RTEQSQ;
    DEEP_S4 =  DEEP_S3 * DPINI_RTEQSQ;
    DEEP_S1 = -15. * DEEP_EQ * DEEP_S4;
    DEEP_S5 =  DEEP_X1 * DEEP_X3 + DEEP_X2 * DEEP_X4;
    DEEP_S6 =  DEEP_X2 * DEEP_X3 + DEEP_X1 * DEEP_X4;
    DEEP_S7 =  DEEP_X2 * DEEP_X4 - DEEP_X1 * DEEP_X3;
    DEEP_SE =  DEEP_S1 * DEEP_ZN * DEEP_S5;
    DEEP_SI =  DEEP_S2 * DEEP_ZN * (DEEP_Z11 + DEEP_Z13);
    DEEP_SL = -DEEP_ZN * DEEP_S3 * (DEEP_Z1  + DEEP_Z3
				    - 14. - 6. * DPINI_EQSQ);
    DEEP_SGH =  DEEP_S4 * DEEP_ZN * (DEEP_Z31 + DEEP_Z33 - 6.);
    DEEP_SH  = -DEEP_ZN * DEEP_S2 * (DEEP_Z21 + DEEP_Z23);
    if (DEEP_XQNCL < 5.2359877E-2) DEEP_SH = 0.0;
    DEEP_EE2 =  2. * DEEP_S1 * DEEP_S6;
    DEEP_E3  =  2. * DEEP_S1 * DEEP_S7;
    DEEP_XI2 =  2. * DEEP_S2 * DEEP_Z12;
    DEEP_XI3 =  2. * DEEP_S2 * (DEEP_Z13 - DEEP_Z11);
    DEEP_XL2 = -2. * DEEP_S3 * DEEP_Z2;
    DEEP_XL3 = -2. * DEEP_S3 * (DEEP_Z3 - DEEP_Z1);
    DEEP_XL4 = -2. * DEEP_S3 * (-21. - 9. * DPINI_EQSQ) * DEEP_ZE;
    DEEP_XGH2 =   2. * DEEP_S4 * DEEP_Z32;
    DEEP_XGH3 =   2. * DEEP_S4 * (DEEP_Z33 - DEEP_Z31);
    DEEP_XGH4 = -18. * DEEP_S4 * DEEP_ZE;
    DEEP_XH2 = -2. * DEEP_S2 * DEEP_Z22;
    DEEP_XH3 = -2. * DEEP_S2 * (DEEP_Z23 - DEEP_Z21);

    /* Do lunar terms (label 30). */

    DEEP_SSE = DEEP_SE;
    DEEP_SSI = DEEP_SI;
    DEEP_SSL = DEEP_SL;
    DEEP_SSH = DEEP_SH / DPINI_SINIQ;
    DEEP_SSG = DEEP_SGH - DPINI_COSIQ * DEEP_SSH;
    DEEP_SE2 = DEEP_EE2;
    DEEP_SI2 = DEEP_XI2;
    DEEP_SL2 = DEEP_XL2;
    DEEP_SGH2 = DEEP_XGH2;
    DEEP_SH2 = DEEP_XH2;
    DEEP_SE3 = DEEP_E3;
    DEEP_SI3 = DEEP_XI3;
    DEEP_SL3 = DEEP_XL3;
    DEEP_SGH3 = DEEP_XGH3;
    DEEP_SH3 = DEEP_XH3;
    DEEP_SL4 = DEEP_XL4;
    DEEP_SGH4 = DEEP_XGH4;
    DEEP_ZCOSG = DEEP_ZCOSGL;
    DEEP_ZSING = DEEP_ZSINGL;
    DEEP_ZCOSI = DEEP_ZCOSIL;
    DEEP_ZSINI = DEEP_ZSINIL;
    DEEP_ZCOSH = DEEP_ZCOSHL * DEEP_COSQ + DEEP_ZSINHL * DEEP_SINQ;
    DEEP_ZSINH = DEEP_SINQ * DEEP_ZCOSHL - DEEP_COSQ * DEEP_ZSINHL;
    DEEP_ZN = DEEP_ZNL;
    DEEP_CC = DEEP_C1L;
    DEEP_ZE = DEEP_ZEL;
    DEEP_ZMO = DEEP_ZMOL;

    /* Second pass through label 20. */

    DEEP_A1  =  DEEP_ZCOSG * DEEP_ZCOSH + DEEP_ZSING * DEEP_ZCOSI * DEEP_ZSINH;
    DEEP_A3  = -DEEP_ZSING * DEEP_ZCOSH + DEEP_ZCOSG * DEEP_ZCOSI * DEEP_ZSINH;
    DEEP_A7  = -DEEP_ZCOSG * DEEP_ZSINH + DEEP_ZSING * DEEP_ZCOSI * DEEP_ZCOSH;
    DEEP_A8  =  DEEP_ZSING * DEEP_ZSINI;
    DEEP_A9  =  DEEP_ZSING * DEEP_ZSINH + DEEP_ZCOSG * DEEP_ZCOSI * DEEP_ZCOSH;
    DEEP_A10 =  DEEP_ZCOSG * DEEP_ZSINI;
    DEEP_A2  =  DPINI_COSIQ * DEEP_A7 + DPINI_SINIQ * DEEP_A8;
    DEEP_A4  =  DPINI_COSIQ * DEEP_A9 + DPINI_SINIQ * DEEP_A10;
    DEEP_A5  = -DPINI_SINIQ * DEEP_A7 + DPINI_COSIQ * DEEP_A8;
    DEEP_A6  = -DPINI_SINIQ * DEEP_A9 + DPINI_COSIQ * DEEP_A10;

    DEEP_X1 =  DEEP_A1 * DPINI_COSOMO + DEEP_A2 * DPINI_SINOMO;
    DEEP_X2 =  DEEP_A3 * DPINI_COSOMO + DEEP_A4 * DPINI_SINOMO;
    DEEP_X3 = -DEEP_A1 * DPINI_SINOMO + DEEP_A2 * DPINI_COSOMO;
    DEEP_X4 = -DEEP_A3 * DPINI_SINOMO + DEEP_A4 * DPINI_COSOMO;
    DEEP_X5 =  DEEP_A5 * DPINI_SINOMO;
    DEEP_X6 =  DEEP_A6 * DPINI_SINOMO;
    DEEP_X7 =  DEEP_A5 * DPINI_COSOMO;
    DEEP_X8 =  DEEP_A6 * DPINI_COSOMO;

    DEEP_Z31 = 12. * DEEP_X1 * DEEP_X1 - 3. * DEEP_X3 * DEEP_X3;
    DEEP_Z32 = 24. * DEEP_X1 * DEEP_X2 - 6. * DEEP_X3 * DEEP_X4;
    DEEP_Z33 = 12. * DEEP_X2 * DEEP_X2 - 3. * DEEP_X4 * DEEP_X4;
    DEEP_Z1  =  3. * (DEEP_A1 * DEEP_A1 + DEEP_A2 * DEEP_A2)
      + DEEP_Z31 * DPINI_EQSQ;
    DEEP_Z2  =  6. * (DEEP_A1 * DEEP_A3 + DEEP_A2 * DEEP_A4)
      + DEEP_Z32 * DPINI_EQSQ;
    DEEP_Z3  =  3. * (DEEP_A3 * DEEP_A3 + DEEP_A4 * DEEP_A4)
      + DEEP_Z33 * DPINI_EQSQ;
    DEEP_Z11 = -6. * DEEP_A1 * DEEP_A5
      + DPINI_EQSQ * (-24. * DEEP_X1 * DEEP_X7 - 6. * DEEP_X3 * DEEP_X5);
    DEEP_Z12 = -6. * (DEEP_A1 *DEEP_A6 + DEEP_A3 * DEEP_A5)
      + DPINI_EQSQ * (-24. * (DEEP_X2 * DEEP_X7 + DEEP_X1 * DEEP_X8)
		      - 6. * (DEEP_X3 * DEEP_X6 + DEEP_X4 * DEEP_X5));
    DEEP_Z13 = -6. * DEEP_A3 * DEEP_A6
      + DPINI_EQSQ * (-24. * DEEP_X2 * DEEP_X8 - 6. * DEEP_X4 * DEEP_X6);
    DEEP_Z21 =  6. * DEEP_A2 * DEEP_A5
      + DPINI_EQSQ * ( 24. * DEEP_X1 * DEEP_X5 - 6. * DEEP_X3 * DEEP_X7);
    DEEP_Z22 =  6. * (DEEP_A4 * DEEP_A5 + DEEP_A2 * DEEP_A6)
      + DPINI_EQSQ * ( 24. * (DEEP_X2 * DEEP_X5 + DEEP_X1 * DEEP_X6)
		      - 6. * (DEEP_X4 * DEEP_X7 + DEEP_X3 * DEEP_X8));
    DEEP_Z23 =  6. * DEEP_A4 * DEEP_A6
      + DPINI_EQSQ * ( 24. * DEEP_X2 * DEEP_X6 - 6. * DEEP_X4 * DEEP_X8);
    DEEP_Z1 =  DEEP_Z1 + DEEP_Z1 + DPINI_BSQ * DEEP_Z31;
    DEEP_Z2 =  DEEP_Z2 + DEEP_Z2 + DPINI_BSQ * DEEP_Z32;
    DEEP_Z3 =  DEEP_Z3 + DEEP_Z3 + DPINI_BSQ * DEEP_Z33;
    DEEP_S3 =  DEEP_CC * DEEP_XNOI;
    DEEP_S2 = -.5 * DEEP_S3 / DPINI_RTEQSQ;
    DEEP_S4 =  DEEP_S3 * DPINI_RTEQSQ;
    DEEP_S1 = -15. * DEEP_EQ * DEEP_S4;
    DEEP_S5 =  DEEP_X1 * DEEP_X3 + DEEP_X2 * DEEP_X4;
    DEEP_S6 =  DEEP_X2 * DEEP_X3 + DEEP_X1 * DEEP_X4;
    DEEP_S7 =  DEEP_X2 * DEEP_X4 - DEEP_X1 * DEEP_X3;
    DEEP_SE =  DEEP_S1 * DEEP_ZN * DEEP_S5;
    DEEP_SI =  DEEP_S2 * DEEP_ZN * (DEEP_Z11 + DEEP_Z13);
    DEEP_SL = -DEEP_ZN * DEEP_S3 * (DEEP_Z1  + DEEP_Z3
				    - 14. - 6. * DPINI_EQSQ);
    DEEP_SGH =  DEEP_S4 * DEEP_ZN * (DEEP_Z31 + DEEP_Z33 - 6.);
    DEEP_SH  = -DEEP_ZN * DEEP_S2 * (DEEP_Z21 + DEEP_Z23);
    if (DEEP_XQNCL < 5.2359877E-2) DEEP_SH = 0.0;
    DEEP_EE2 =  2. * DEEP_S1 * DEEP_S6;
    DEEP_E3  =  2. * DEEP_S1 * DEEP_S7;
    DEEP_XI2 =  2. * DEEP_S2 * DEEP_Z12;
    DEEP_XI3 =  2. * DEEP_S2 * (DEEP_Z13 - DEEP_Z11);
    DEEP_XL2 = -2. * DEEP_S3 * DEEP_Z2;
    DEEP_XL3 = -2. * DEEP_S3 * (DEEP_Z3 - DEEP_Z1);
    DEEP_XL4 = -2. * DEEP_S3 * (-21. - 9. * DPINI_EQSQ) * DEEP_ZE;
    DEEP_XGH2 =   2. * DEEP_S4 * DEEP_Z32;
    DEEP_XGH3 =   2. * DEEP_S4 * (DEEP_Z33 - DEEP_Z31);
    DEEP_XGH4 = -18. * DEEP_S4 * DEEP_ZE;
    DEEP_XH2 = -2. * DEEP_S2 * DEEP_Z22;
    DEEP_XH3 = -2. * DEEP_S2 * (DEEP_Z23 - DEEP_Z21);

    /* Label 40. */

    DEEP_SSE = DEEP_SSE + DEEP_SE;
    DEEP_SSI = DEEP_SSI + DEEP_SI;
    DEEP_SSL = DEEP_SSL + DEEP_SL;
    DEEP_SSG = DEEP_SSG + DEEP_SGH - DPINI_COSIQ / DPINI_SINIQ * DEEP_SH;
    DEEP_SSH = DEEP_SSH + DEEP_SH / DPINI_SINIQ;

    /* Geopotential resonance initialisation for 12 hour orbits. */

    DEEP_IRESFL = 0;
    DEEP_ISYNFL = 0;
    if (DEEP_XNQ >= .0052359877 || DEEP_XNQ <= .0034906585) {
      if (DEEP_XNQ < 8.26E-3  || DEEP_XNQ > 9.24E-3) return;
      if (DEEP_EQ  < 0.5) return;
      DEEP_IRESFL = 1;
      DEEP_EOC = DEEP_EQ * DPINI_EQSQ;
      DEEP_G201 = -.306 - (DEEP_EQ - .64) * .440;

      if (DEEP_EQ <= .65) {
	DEEP_G211 =     3.616  -    13.247  * DEEP_EQ
	          +    16.290  * DPINI_EQSQ;
	DEEP_G310 =   -19.302  +   117.390  * DEEP_EQ
	          -   228.419  * DPINI_EQSQ +   156.591  * DEEP_EOC;
	DEEP_G322 =   -18.9068 +   109.7927 * DEEP_EQ
	          -   214.6334 * DPINI_EQSQ +   146.5816 * DEEP_EOC;
	DEEP_G410 =   -41.122  +   242.694  * DEEP_EQ
	          -   471.094  * DPINI_EQSQ +   313.953  * DEEP_EOC;
	DEEP_G422 =  -146.407  +   841.880  * DEEP_EQ
	          -  1629.014  * DPINI_EQSQ +  1083.435  * DEEP_EOC;
	DEEP_G520 =  -532.114  +  3017.977  * DEEP_EQ
	          -  5740.     * DPINI_EQSQ +  3708.276  * DEEP_EOC;
      }
      else {
	DEEP_G211 =   -72.099  +   331.819  * DEEP_EQ
                  -   508.738  * DPINI_EQSQ +   266.724  * DEEP_EOC;
	DEEP_G310 =  -346.844  +  1582.851  * DEEP_EQ
                  -  2415.925  * DPINI_EQSQ +  1246.113  * DEEP_EOC;
	DEEP_G322 =  -342.585  +  1554.908  * DEEP_EQ
                  -  2366.899  * DPINI_EQSQ +  1215.972  * DEEP_EOC;
	DEEP_G410 = -1052.797  +  4758.686  * DEEP_EQ
                  -  7193.992  * DPINI_EQSQ +  3651.957  * DEEP_EOC;
	DEEP_G422 = -3581.69   + 16178.11   * DEEP_EQ
                  - 24462.77   * DPINI_EQSQ + 12422.52   * DEEP_EOC;
	if (DEEP_EQ <= .715) {
	  DEEP_G520 =  1464.74 -  4664.75 * DEEP_EQ +  3763.64 * DPINI_EQSQ;
	}
	else {
	  DEEP_G520 = -5149.66 + 29936.92 * DEEP_EQ - 54087.36 * DPINI_EQSQ
                    + 31324.56 * DEEP_EOC;
	}
      }

      if (DEEP_EQ < .7) {
	DEEP_G533 = -919.2277  + 4988.61   * DEEP_EQ
                  - 9064.77   * DPINI_EQSQ + 5542.21  * DEEP_EOC;
	DEEP_G521 = -822.71072 + 4568.6173 * DEEP_EQ
                  - 8491.4146 * DPINI_EQSQ + 5337.524 * DEEP_EOC;
	DEEP_G532 = -853.666   + 4690.25   * DEEP_EQ
                  - 8624.77   * DPINI_EQSQ + 5341.4   * DEEP_EOC;
      }
      else {
	DEEP_G533 = -37995.78  + 161616.52 * DEEP_EQ
                  - 229838.2  * DPINI_EQSQ + 109377.94 * DEEP_EOC;
	DEEP_G521 = -51752.104 + 218913.95 * DEEP_EQ
                  - 309468.16 * DPINI_EQSQ + 146349.42 * DEEP_EOC;
	DEEP_G532 = -40023.88  + 170470.89 * DEEP_EQ
                  - 242699.48 * DPINI_EQSQ + 115605.82 * DEEP_EOC;
      }

      DEEP_SINI2 = DPINI_SINIQ * DPINI_SINIQ;
      DEEP_F220 =   .75 * (1. + 2. * DPINI_COSIQ + DPINI_COSQ2);
      DEEP_F221 =  1.5     * DEEP_SINI2;
      DEEP_F321 =  1.875   * DPINI_SINIQ * (1. - 2. * DPINI_COSIQ
					  - 3. * DPINI_COSQ2);
      DEEP_F322 = -1.875   * DPINI_SINIQ * (1. + 2. * DPINI_COSIQ
					  - 3. * DPINI_COSQ2);
      DEEP_F441 = 35.      * DEEP_SINI2 * DEEP_F220;
      DEEP_F442 = 39.3750  * DEEP_SINI2 * DEEP_SINI2;
      DEEP_F522 =  9.84375   * DPINI_SINIQ * (DEEP_SINI2 * ( 1.
	- 2. * DPINI_COSIQ -  5. * DPINI_COSQ2)
	+  .33333333 * (-2. + 4. * DPINI_COSIQ + 6. * DPINI_COSQ2));
      DEEP_F523 = DPINI_SINIQ * (4.92187512 * DEEP_SINI2
	* (-2. - 4. * DPINI_COSIQ
	+ 10. * DPINI_COSQ2) + 6.56250012 * ( 1. + 2. * DPINI_COSIQ
					      - 3. * DPINI_COSQ2));
      DEEP_F542 = 29.53125 * DPINI_SINIQ * ( 2. - 8. * DPINI_COSIQ
	+ DPINI_COSQ2 * (-12. + 8. * DPINI_COSIQ + 10. * DPINI_COSQ2));
      DEEP_F543 = 29.53125 * DPINI_SINIQ * (-2. - 8. * DPINI_COSIQ
	+ DPINI_COSQ2 * ( 12. + 8. * DPINI_COSIQ - 10. * DPINI_COSQ2));
      DEEP_XNO2 = DEEP_XNQ * DEEP_XNQ;
      DEEP_AINV2 = DEEP_AQNV * DEEP_AQNV;
      DEEP_TEMP1 = 3. * DEEP_XNO2 * DEEP_AINV2;
      DEEP_TEMP = DEEP_TEMP1 * DEEP_ROOT22;
      DEEP_D2201 = DEEP_TEMP * DEEP_F220*DEEP_G201;
      DEEP_D2211 = DEEP_TEMP * DEEP_F221*DEEP_G211;
      DEEP_TEMP1 = DEEP_TEMP1 * DEEP_AQNV;
      DEEP_TEMP = DEEP_TEMP1 * DEEP_ROOT32;
      DEEP_D3210 = DEEP_TEMP * DEEP_F321 * DEEP_G310;
      DEEP_D3222 = DEEP_TEMP * DEEP_F322 * DEEP_G322;
      DEEP_TEMP1 = DEEP_TEMP1 * DEEP_AQNV;
      DEEP_TEMP = 2. * DEEP_TEMP1 * DEEP_ROOT44;
      DEEP_D4410 = DEEP_TEMP * DEEP_F441 * DEEP_G410;
      DEEP_D4422 = DEEP_TEMP * DEEP_F442 * DEEP_G422;
      DEEP_TEMP1 = DEEP_TEMP1 * DEEP_AQNV;
      DEEP_TEMP = DEEP_TEMP1 * DEEP_ROOT52;
      DEEP_D5220 = DEEP_TEMP * DEEP_F522 * DEEP_G520;
      DEEP_D5232 = DEEP_TEMP * DEEP_F523 * DEEP_G532;
      DEEP_TEMP = 2. * DEEP_TEMP1 * DEEP_ROOT54;
      DEEP_D5421 = DEEP_TEMP * DEEP_F542 * DEEP_G521;
      DEEP_D5433 = DEEP_TEMP * DEEP_F543 * DEEP_G533;
      DEEP_XLAMO = DEEP_XMAO + E1_XNODEO + E1_XNODEO - DEEP_THGR - DEEP_THGR;
      DEEP_BFACT = DPINI_XLLDOT + DPINI_XNODOT + DPINI_XNODOT
	- DEEP_THDT - DEEP_THDT;
      DEEP_BFACT = DEEP_BFACT + DEEP_SSL + DEEP_SSH + DEEP_SSH;
    }

    /* Synchronous resonance terms initialisation. */

    else {
      DEEP_IRESFL = 1;
      DEEP_ISYNFL = 1;
      DEEP_G200 = 1.0 + DPINI_EQSQ * (-2.5 + .8125 * DPINI_EQSQ);
      DEEP_G310 = 1.0 + 2.0 * DPINI_EQSQ;
      DEEP_G300 = 1.0 + DPINI_EQSQ * (-6.0 + 6.60937 * DPINI_EQSQ);
      DEEP_F220 = .75 * (1. + DPINI_COSIQ) * (1. + DPINI_COSIQ);
      DEEP_F311 = .9375 * DPINI_SINIQ * DPINI_SINIQ * (1. + 3. * DPINI_COSIQ)
	- .75 * (1. + DPINI_COSIQ);
      DEEP_F330 = 1. + DPINI_COSIQ;
      DEEP_F330 = 1.875 * DEEP_F330 * DEEP_F330 * DEEP_F330;
      DEEP_DEL1 = 3. * DEEP_XNQ  * DEEP_XNQ  * DEEP_AQNV * DEEP_AQNV;
      DEEP_DEL2 = 2. * DEEP_DEL1 * DEEP_F220 * DEEP_G200 * DEEP_Q22;
      DEEP_DEL3 = 3. * DEEP_DEL1 * DEEP_F330 * DEEP_G300 * DEEP_Q33 * DEEP_AQNV;
      DEEP_DEL1 = DEEP_DEL1 * DEEP_F311 * DEEP_G310 * DEEP_Q31 * DEEP_AQNV;
      DEEP_FASX2 = .13130908;
      DEEP_FASX4 = 2.8843198;
      DEEP_FASX6 = .37448087;
      DEEP_XLAMO = DEEP_XMAO + E1_XNODEO + E1_OMEGAO - DEEP_THGR;
      DEEP_BFACT = DPINI_XLLDOT + DEEP_XPIDOT - DEEP_THDT;
      DEEP_BFACT = DEEP_BFACT + DEEP_SSL + DEEP_SSG + DEEP_SSH;
    }

    DEEP_XFACT = DEEP_BFACT - DEEP_XNQ;

    /* Initialise integrator. */

    DEEP_XLI = DEEP_XLAMO;
    DEEP_XNI = DEEP_XNQ;
    DEEP_ATIME =    0.;
    DEEP_STEPP =  720.;
    DEEP_STEPN = -720.;
    DEEP_STEP2 = 259200.;
    return;
  }


  /**
   * Deep space secular effects. */

  protected final void DEEP2()
  {
    DPSEC_XLL    = DPSEC_XLL    + DEEP_SSL * DPSEC_T;
    DPSEC_OMGASM = DPSEC_OMGASM + DEEP_SSG * DPSEC_T;
    DPSEC_XNODES = DPSEC_XNODES + DEEP_SSH * DPSEC_T;
    DPSEC_EM   = E1_EO    + DEEP_SSE * DPSEC_T;
    DPSEC_XINC = E1_XINCL + DEEP_SSI * DPSEC_T;
    if (DPSEC_XINC < 0.) {
      DPSEC_XINC   = -DPSEC_XINC;
      DPSEC_XNODES =  DPSEC_XNODES + C2_PI;
      DPSEC_OMGASM =  DPSEC_OMGASM - C2_PI;
    }
    if (DEEP_IRESFL == 0) return;

    /* Label 100. */

    for (;;) {

      if (DEEP_ATIME == 0. ||
	  (DPSEC_T >= 0. && DEEP_ATIME <  0.) ||
	  (DPSEC_T <  0. && DEEP_ATIME >= 0.)) {
	if (DPSEC_T < 0.) {
          DEEP_DELT = DEEP_STEPN;
	}
	else {
	  DEEP_DELT = DEEP_STEPP;
	}
	DEEP_ATIME = 0.;
	DEEP_XNI = DEEP_XNQ;
	DEEP_XLI = DEEP_XLAMO;
	if (Math.abs(DPSEC_T - DEEP_ATIME) >= DEEP_STEPP) {
          DEEP_IRET  = 125;
	  DEEP_IRETN = 165;
	}
	else {
          DEEP_FT = DPSEC_T - DEEP_ATIME;
	  DEEP_IRETN = 140;
	}
      }
      else if (Math.abs(DPSEC_T) >= Math.abs(DEEP_ATIME)) {
	DEEP_DELT = DEEP_STEPN;
	if (DPSEC_T > 0.) DEEP_DELT = DEEP_STEPP;
	if (Math.abs(DPSEC_T - DEEP_ATIME) >= DEEP_STEPP) {
          DEEP_IRET  = 125;
	  DEEP_IRETN = 165;
	}
	else {
          DEEP_FT = DPSEC_T-DEEP_ATIME;
	  DEEP_IRETN = 140;
	}
      }
      else {
	DEEP_DELT = DEEP_STEPP;
        if (DPSEC_T >= 0.) DEEP_DELT = DEEP_STEPN;
	DEEP_IRET  = 100;
	DEEP_IRETN = 165;
      }

      /* Dot terms calculated (label 150).
       * Label 125 return point moved here by duplicating some code above. */

      for (;;) {

	if (DEEP_ISYNFL != 0) {
	  DEEP_XNDOT = DEEP_DEL1 * Math.sin(DEEP_XLI - DEEP_FASX2)
	    + DEEP_DEL2 * Math.sin(2. * (DEEP_XLI - DEEP_FASX4))
	    + DEEP_DEL3 * Math.sin(3. * (DEEP_XLI - DEEP_FASX6));
	  DEEP_XNDDT = DEEP_DEL1 * Math.cos(DEEP_XLI - DEEP_FASX2)
	    + 2. * DEEP_DEL2 * Math.cos(2. * (DEEP_XLI - DEEP_FASX4))
	    + 3. * DEEP_DEL3 * Math.cos(3. * (DEEP_XLI - DEEP_FASX6));
	}
	else {
	  DEEP_XOMI  = DEEP_OMEGAQ + DPINI_OMGDT * DEEP_ATIME;
	  DEEP_X2OMI = DEEP_XOMI + DEEP_XOMI;
	  DEEP_X2LI  = DEEP_XLI + DEEP_XLI;
	  DEEP_XNDOT = DEEP_D2201 * Math.sin(DEEP_X2OMI + DEEP_XLI - DEEP_G22)
            + DEEP_D2211 * Math.sin( DEEP_XLI   - DEEP_G22)
	    + DEEP_D3210 * Math.sin( DEEP_XOMI  + DEEP_XLI  - DEEP_G32)
            + DEEP_D3222 * Math.sin(-DEEP_XOMI  + DEEP_XLI  - DEEP_G32)
            + DEEP_D4410 * Math.sin( DEEP_X2OMI + DEEP_X2LI - DEEP_G44)
            + DEEP_D4422 * Math.sin( DEEP_X2LI  - DEEP_G44)
            + DEEP_D5220 * Math.sin( DEEP_XOMI  + DEEP_XLI  - DEEP_G52)
            + DEEP_D5232 * Math.sin(-DEEP_XOMI  + DEEP_XLI  - DEEP_G52)
            + DEEP_D5421 * Math.sin( DEEP_XOMI  + DEEP_X2LI - DEEP_G54)
	    + DEEP_D5433 * Math.sin(-DEEP_XOMI  + DEEP_X2LI - DEEP_G54);
         DEEP_XNDDT = DEEP_D2201 * Math.cos(DEEP_X2OMI + DEEP_XLI - DEEP_G22)
            + DEEP_D2211 * Math.cos( DEEP_XLI   - DEEP_G22)
            + DEEP_D3210 * Math.cos( DEEP_XOMI  + DEEP_XLI  - DEEP_G32)
            + DEEP_D3222 * Math.cos(-DEEP_XOMI  + DEEP_XLI  - DEEP_G32)
            + DEEP_D5220 * Math.cos( DEEP_XOMI  + DEEP_XLI  - DEEP_G52)
            + DEEP_D5232 * Math.cos(-DEEP_XOMI  + DEEP_XLI  - DEEP_G52)
            + 2. * (DEEP_D4410 * Math.cos(DEEP_X2OMI + DEEP_X2LI - DEEP_G44)
            + DEEP_D4422 * Math.cos( DEEP_X2LI  - DEEP_G44)
            + DEEP_D5421 * Math.cos( DEEP_XOMI  + DEEP_X2LI - DEEP_G54)
	    + DEEP_D5433 * Math.cos(-DEEP_XOMI  + DEEP_X2LI - DEEP_G54));
	}
	DEEP_XLDOT = DEEP_XNI + DEEP_XFACT;
	DEEP_XNDDT = DEEP_XNDDT * DEEP_XLDOT;
	if (DEEP_IRETN == 140) {
	  DPSEC_XN = DEEP_XNI + DEEP_XNDOT * DEEP_FT
	    + DEEP_XNDDT * DEEP_FT * DEEP_FT * 0.5;
	  DEEP_XL = DEEP_XLI + DEEP_XLDOT * DEEP_FT
            + DEEP_XNDOT * DEEP_FT * DEEP_FT * 0.5;
	  DEEP_TEMP = -DPSEC_XNODES + DEEP_THGR + DPSEC_T * DEEP_THDT;
	  DPSEC_XLL = DEEP_XL - DPSEC_OMGASM + DEEP_TEMP;
	  if (DEEP_ISYNFL == 0) DPSEC_XLL = DEEP_XL + DEEP_TEMP + DEEP_TEMP;
	  return;
	}
	if (DEEP_IRETN == 165) {
	  DEEP_XLI = DEEP_XLI + DEEP_XLDOT * DEEP_DELT
	    + DEEP_XNDOT * DEEP_STEP2;
	  DEEP_XNI = DEEP_XNI + DEEP_XNDOT * DEEP_DELT
	    + DEEP_XNDDT * DEEP_STEP2;
	  DEEP_ATIME = DEEP_ATIME + DEEP_DELT;
	}
	if (DEEP_IRET == 125) {
	  if (Math.abs(DPSEC_T - DEEP_ATIME) >= DEEP_STEPP) {
	    DEEP_IRET  = 125;
            DEEP_IRETN = 165;
	  }
	  else {
	    DEEP_FT = DPSEC_T - DEEP_ATIME;
            DEEP_IRETN = 140;
	  }
	}
	if (DEEP_IRET != 125) break;
      }
    }
  }


  /**
   * Deep space lunar-solar periodics. */

  protected final void DEEP3()
  {
    DEEP_SINIS = Math.sin(DPSEC_XINC);
    DEEP_COSIS = Math.cos(DPSEC_XINC);
    if (Math.abs(DEEP_SAVTSN - DPSEC_T) >= 30.) {
      DEEP_SAVTSN = DPSEC_T;
      DEEP_ZM = DEEP_ZMOS +    DEEP_ZNS * DPSEC_T;
      DEEP_ZF = DEEP_ZM + 2. * DEEP_ZES * Math.sin(DEEP_ZM);
      DEEP_SINZF = Math.sin(DEEP_ZF);
      DEEP_F2 =  .5 * DEEP_SINZF * DEEP_SINZF - .25;
      DEEP_F3 = -.5 * DEEP_SINZF * Math.cos(DEEP_ZF);
      DEEP_SES  = DEEP_SE2  * DEEP_F2 + DEEP_SE3  * DEEP_F3;
      DEEP_SIS  = DEEP_SI2  * DEEP_F2 + DEEP_SI3  * DEEP_F3;
      DEEP_SLS  = DEEP_SL2  * DEEP_F2 + DEEP_SL3  * DEEP_F3
	+ DEEP_SL4  * DEEP_SINZF;
      DEEP_SGHS = DEEP_SGH2 * DEEP_F2 + DEEP_SGH3 * DEEP_F3
	+ DEEP_SGH4 * DEEP_SINZF;
      DEEP_SHS  = DEEP_SH2  * DEEP_F2 + DEEP_SH3  * DEEP_F3;
      DEEP_ZM = DEEP_ZMOL + DEEP_ZNL * DPSEC_T;
      DEEP_ZF = DEEP_ZM + 2. * DEEP_ZEL * Math.sin(DEEP_ZM);
      DEEP_SINZF = Math.sin(DEEP_ZF);
      DEEP_F2 =  .5 * DEEP_SINZF * DEEP_SINZF - .25;
      DEEP_F3 = -.5 * DEEP_SINZF * Math.cos(DEEP_ZF);
      DEEP_SEL  = DEEP_EE2  * DEEP_F2 + DEEP_E3   * DEEP_F3;
      DEEP_SIL  = DEEP_XI2  * DEEP_F2 + DEEP_XI3  * DEEP_F3;
      DEEP_SLL  = DEEP_XL2  * DEEP_F2 + DEEP_XL3  * DEEP_F3
	+ DEEP_XL4  * DEEP_SINZF;
      DEEP_SGHL = DEEP_XGH2 * DEEP_F2 + DEEP_XGH3 * DEEP_F3
	+ DEEP_XGH4 * DEEP_SINZF;
      DEEP_SH1 = DEEP_XH2 * DEEP_F2 + DEEP_XH3 * DEEP_F3;
      DEEP_PE   = DEEP_SES + DEEP_SEL;
      DEEP_PINC = DEEP_SIS + DEEP_SIL;
      DEEP_PL   = DEEP_SLS + DEEP_SLL;
    }
    DEEP_PGH = DEEP_SGHS + DEEP_SGHL;
    DEEP_PH  = DEEP_SHS  + DEEP_SH1;
    DPSEC_XINC = DPSEC_XINC + DEEP_PINC;
    DPSEC_EM = DPSEC_EM + DEEP_PE;

    /* Apply periodics directly. */

    if (DEEP_XQNCL >= .2) {
      DEEP_PH = DEEP_PH / DPINI_SINIQ;
      DEEP_PGH = DEEP_PGH - DPINI_COSIQ * DEEP_PH;
      DPSEC_OMGASM = DPSEC_OMGASM + DEEP_PGH;
      DPSEC_XNODES = DPSEC_XNODES + DEEP_PH;
      DPSEC_XLL = DPSEC_XLL + DEEP_PL;
    }

    /* Apply periodics with Lyddane modification. */

    else {
      DEEP_SINOK = Math.sin(DPSEC_XNODES);
      DEEP_COSOK = Math.cos(DPSEC_XNODES);
      DEEP_ALFDP = DEEP_SINIS*DEEP_SINOK;
      DEEP_BETDP = DEEP_SINIS*DEEP_COSOK;
      DEEP_DALF  =  DEEP_PH * DEEP_COSOK + DEEP_PINC * DEEP_COSIS * DEEP_SINOK;
      DEEP_DBET  = -DEEP_PH * DEEP_SINOK + DEEP_PINC * DEEP_COSIS * DEEP_COSOK;
      DEEP_ALFDP = DEEP_ALFDP + DEEP_DALF;
      DEEP_BETDP = DEEP_BETDP + DEEP_DBET;
      DEEP_XLS   = DPSEC_XLL + DPSEC_OMGASM + DEEP_COSIS * DPSEC_XNODES;
      DEEP_DLS   = DEEP_PL + DEEP_PGH - DEEP_PINC * DPSEC_XNODES * DEEP_SINIS;
      DEEP_XLS   = DEEP_XLS + DEEP_DLS;
      DPSEC_XNODES =ACTAN(DEEP_ALFDP, DEEP_BETDP);
      DPSEC_XLL    = DPSEC_XLL + DEEP_PL;
      DPSEC_OMGASM = DEEP_XLS - DPSEC_XLL
	- Math.cos(DPSEC_XINC) * DPSEC_XNODES;
    }

    return;
  }


  /**
   * Wrapper for deep space initialisation. */

  protected final void DPINIT(double EOSQ, double SINIO, double COSIO,
    double BETAO, double AODP, double THETA2, double SING, double COSG,
    double BETAO2, double XMDOT, double OMGDOT, double XNODOTT, double XNODPP)
  {
    /* Although this is a ported Fortran subroutine, it is in fact called
     * only with arguments that are instance variables.  So the problem
     * of returning values does not arise even if the call is by value only.
     * It also is probably the case that this routine does not have returned
     * arguments anyway. */

    DPINI_EQSQ = EOSQ;
    DPINI_SINIQ = SINIO;
    DPINI_COSIQ = COSIO;
    DPINI_RTEQSQ = BETAO;
    DPINI_AO = AODP;
    DPINI_COSQ2 = THETA2;
    DPINI_SINOMO = SING;
    DPINI_COSOMO = COSG;
    DPINI_BSQ = BETAO2;
    DPINI_XLLDOT = XMDOT;
    DPINI_OMGDT = OMGDOT;
    DPINI_XNODOT = XNODOTT;
    DPINI_XNODP = XNODPP;
    DEEP1();
    EOSQ = DPINI_EQSQ;
    SINIO = DPINI_SINIQ;
    COSIO = DPINI_COSIQ;
    BETAO = DPINI_RTEQSQ;
    AODP = DPINI_AO;
    THETA2 = DPINI_COSQ2;
    SING = DPINI_SINOMO;
    COSG = DPINI_COSOMO;
    BETAO2 = DPINI_BSQ;
    XMDOT = DPINI_XLLDOT;
    OMGDOT = DPINI_OMGDT;
    XNODOTT = DPINI_XNODOT;
    XNODPP = DPINI_XNODP;
    return;
  }


  /**
   * Wrapper for deep space lunar-solar periodics. */

  protected final void DPPER(double[] dpper_args)
  {
    DPSEC_EM     = dpper_args[0];
    DPSEC_XINC   = dpper_args[1];
    DPSEC_OMGASM = dpper_args[2];
    DPSEC_XNODES = dpper_args[3];
    DPSEC_XLL    = dpper_args[4];
    DEEP3();
    dpper_args[0] = DPSEC_EM;
    dpper_args[1] = DPSEC_XINC;
    dpper_args[2] = DPSEC_OMGASM;
    dpper_args[3] = DPSEC_XNODES;
    dpper_args[4] = DPSEC_XLL;
    return;
  }


  /**
   * Wrapper for deep space secular effects. */

  protected final void DPSEC(double[] dpsec_args, double[] TSINCE)
  {
    DPSEC_XLL    = dpsec_args[0];
    DPSEC_OMGASM = dpsec_args[1];
    DPSEC_XNODES = dpsec_args[2];
    /* DPSEC_EM = EMM
     * DPSEC_XINC = XINCC */
    DPSEC_XN = dpsec_args[5];
    DPSEC_T = TSINCE[0];
    DEEP2();
    dpsec_args[0] = DPSEC_XLL;
    dpsec_args[1] = DPSEC_OMGASM;
    dpsec_args[2] = DPSEC_XNODES;
    dpsec_args[3] = DPSEC_EM;
    dpsec_args[4] = DPSEC_XINC;
    dpsec_args[5] = DPSEC_XN;
    TSINCE[0] = DPSEC_T;
    return;
  }


  /**
   * A helper routine to calculate the modulo 2 pi. */

  protected final double FMOD2P(double X)
  {
    double value;
    int    I;
    value = X;
    I = (int)(value/C2_TWOPI);
    value = value - I * C2_TWOPI;
    if (value < 0) value += C2_TWOPI;
    return value;
  }


  /**
   * Read TLE from open file.
   *
   * <p>This method reads two lines at the current position of the open file,
   * and from these reads the satellite elements according to the NORAD
   * two-line format.  The file remains open and is left pointed behind the
   * two lines read.
   *
   * <p>Note that TLE sets, although the T stands for "two", have line
   * triplets.  This routine reads the two lines with the actual elements.
   * The line that precedes them must have been read before this call.
   *
   * <p>The format of these two lines is this:
   *
<pre>
1 NNNNNU NNNNNAAA NNNNN.NNNNNNNN +.NNNNNNNN +NNNNN-N +NNNNN-N N NNNNN
2 NNNNN NNN.NNNN NNN.NNNN NNNNNNN NNN.NNNN NNN.NNNN NN.NNNNNNNNNNNNNN
</pre>
   *
   * <p>The first line contains the following information:
   *
<pre>
1 NNNNNU NNNNNAAA NNNNN.NNNNNNNN +.NNNNNNNN +NNNNN-N +NNNNN-N N NNNNN
  |---|                          satellite number
         |------|                international designator
         ||                      launch year last two digits in 1957-2056
           |-|                   launch number within year
              |-|                piece of launch
                  ||             year of epoch last two digits in 1957-2056
                    |----------| Julian Day of epoch
   1)                            |--------|
   2)                                       |------|
   3)                                                |------|
   ephemeris type                                             |
   element no.                                                  |--|
   check sum                                                        |
</pre>
   *
   * <ol>
   *   <li>First time derivative of the mean motion or ballistic coefficient.
   *   <li>Second time derivative of mean motion.  Field may be blank, meaning
   *   that the quantity is not available and must be assumed zero.  The format
   *   requires to read two integers and construct the number as the first
   *   divided by 100000 multiplied by 10 to the power of the second.
   *   <li>BSTAR drag term if GP4 general perturbation theory was used.
   *   Otherwise, radiation pressure coefficient.
   *   The format requires to read two integers and construct the number
   *   as the first divided by 100000 multiplied by 10 to the power of the
   *   second.
   * </ol>
   *
   * <p>The second line contains the following information:
   *
<pre>
2 NNNNN NNN.NNNN NNN.NNNN NNNNNNN NNN.NNNN NNN.NNNN NN.NNNNNNNNNNNNNN
  |---|                           satellite number
        |------|                  inclination [deg]
                 |------|         RA of ascending node [deg]
                          |-----| 1E7 times eccentricity
   argument of perigee [deg]      |------|
   mean anomaly [deg]                      |------|
   mean motion [rev/day]                            |---------|
   rev.no.                                                     |---|
   check sum                                                        |
</pre>
   *
   * @param aFile
   *   The open file. */

  protected final void ReadNorad12(BufferedReader aFile)
    throws SDP4NoLineOneException, SDP4InvalidNumException, IOException
  {
    String theLine;
    double year, day, t, A1, DEL1, AO, DELO, XNODP;

    /* Read the first line of elements.
     * We read its_n0dot and its_n0dd, but the values are unused.
     * SGPConst and SDPConst will work out their own values. */

    theLine = aFile.readLine();
    if (!theLine.startsWith("1 "))
      throw new SDP4NoLineOneException("TLE line 1 not found");

    itsNumber     = (int)Rfndm(theLine.substring(2));
    itsDesignator = theLine.substring(9, 17);

    E1_EPOCH  = Rfndm(theLine.substring(18, 32));
    E1_XNDT2O = Rfndm(theLine.substring(33));
    if ((theLine.substring(44,52)).equals("        ")) {
      E1_XNDD6O = 0.;
    }
    else {
      E1_XNDD6O  = Rfndm(theLine.substring(44)) / 1E5;
      t          = Rfndm(theLine.substring(50));
      E1_XNDD6O *= Math.pow(10, t);
    }
    E1_BSTAR  = Rfndm(theLine.substring(53)) / 1E5;
    t         = Rfndm(theLine.substring(59));
    E1_BSTAR *= Math.pow(10, t) / C1_AE;

    /* Read the second line of elements. */

    theLine = aFile.readLine();
    if (!theLine.startsWith("2 "))
      throw new SDP4NoLineOneException("TLE line 2 not found");

    E1_XINCL  = Rfndm(theLine.substring(8));
    E1_XNODEO = Rfndm(theLine.substring(17));
    E1_EO     = Rfndm(theLine.substring(26)) / 1e7;
    E1_OMEGAO = Rfndm(theLine.substring(34));
    E1_XMO    = Rfndm(theLine.substring(43));
    E1_XNO    = Rfndm(theLine.substring(52,63));

    E1_XNODEO = E1_XNODEO * C2_DE2RA;
    E1_OMEGAO = E1_OMEGAO * C2_DE2RA;
    E1_XMO    = E1_XMO    * C2_DE2RA;
    E1_XINCL  = E1_XINCL  * C2_DE2RA;
    E1_XNO    = E1_XNO    * C2_TWOPI / C1_XMNPDA;
    E1_XNDT2O = E1_XNDT2O * C2_TWOPI / C1_XMNPDA / C1_XMNPDA;
    E1_XNDD6O = E1_XNDD6O * C2_TWOPI / C1_XMNPDA / C1_XMNPDA / C1_XMNPDA;

    /* Figure out which side of 225 minutes the period is. */

    A1 = Math.pow(C1_XKE / E1_XNO, C1_TOTHRD);
    t = 1.5 * C1_CK2 * (3. * Math.cos(E1_XINCL) * Math.cos(E1_XINCL) - 1.)
      / Math.pow(1.-E1_EO*E1_EO, 1.5);
    DEL1 = t / (A1 * A1);
    AO = A1 * (1. - DEL1 * (.5 * C1_TOTHRD + DEL1 * (1. + 134. / 81. * DEL1)));
    DELO = t / (AO * AO);
    XNODP = E1_XNO / (1. + DELO);
    if (C2_TWOPI / XNODP >= 225.) {itsIsDeep = 1;} else {itsIsDeep = 0;}

    /* Turn the epoch into a Julian Date minus 2450000 d.
     * First split into two-digit year and day of year.
     * Then turn to four-digit year, taking account of the Y2K kludge.
     * Month is implied to be January, avoid months 1 and 2 (leap day). */

    year  = Math.floor(E1_EPOCH / 1000.);
    day   = E1_EPOCH - 1000. * year;
    if (year < 57) {year += 2000.;} else {year += 1900.;}
    year--;
    itsEpochJD  = 5643.5 - 10000. + day;
    itsEpochJD += 365. * (year - 1985.)
	+ Math.floor(year/4.)
	- Math.floor(year/100.)
	+ Math.floor(year/400.)
	+ 306.;

    return;
  }
  
  // overloaded version of the ReadNorad12 function to allow direct passage of tle lines as strings
  // requires no file I/O
  protected final void ReadNorad12(String tle1, String tle2)
  throws SDP4NoLineOneException, SDP4InvalidNumException, IOException
{
  String theLine;
  double year, day, t, A1, DEL1, AO, DELO, XNODP;

  /* Read the first line of elements.
   * We read its_n0dot and its_n0dd, but the values are unused.
   * SGPConst and SDPConst will work out their own values. */

  //theLine = aFile.readLine();
  theLine = tle1; // first line
  if (!theLine.startsWith("1 "))
    throw new SDP4NoLineOneException("TLE line 1 not found");

  itsNumber     = (int)Rfndm(theLine.substring(2));
  itsDesignator = theLine.substring(9, 17);

  E1_EPOCH  = Rfndm(theLine.substring(18, 32));
  E1_XNDT2O = Rfndm(theLine.substring(33));
  if ((theLine.substring(44,52)).equals("        ")) {
    E1_XNDD6O = 0.;
  }
  else {
    E1_XNDD6O  = Rfndm(theLine.substring(44)) / 1E5;
    t          = Rfndm(theLine.substring(50));
    E1_XNDD6O *= Math.pow(10, t);
  }
  E1_BSTAR  = Rfndm(theLine.substring(53)) / 1E5;
  t         = Rfndm(theLine.substring(59));
  E1_BSTAR *= Math.pow(10, t) / C1_AE;

  /* Read the second line of elements. */

  //theLine = aFile.readLine();
  theLine = tle2; // second line
  if (!theLine.startsWith("2 "))
    throw new SDP4NoLineOneException("TLE line 2 not found");

  E1_XINCL  = Rfndm(theLine.substring(8));
  E1_XNODEO = Rfndm(theLine.substring(17));
  E1_EO     = Rfndm(theLine.substring(26)) / 1e7;
  E1_OMEGAO = Rfndm(theLine.substring(34));
  E1_XMO    = Rfndm(theLine.substring(43));
  E1_XNO    = Rfndm(theLine.substring(52,63));

  E1_XNODEO = E1_XNODEO * C2_DE2RA;
  E1_OMEGAO = E1_OMEGAO * C2_DE2RA;
  E1_XMO    = E1_XMO    * C2_DE2RA;
  E1_XINCL  = E1_XINCL  * C2_DE2RA;
  E1_XNO    = E1_XNO    * C2_TWOPI / C1_XMNPDA;
  E1_XNDT2O = E1_XNDT2O * C2_TWOPI / C1_XMNPDA / C1_XMNPDA;
  E1_XNDD6O = E1_XNDD6O * C2_TWOPI / C1_XMNPDA / C1_XMNPDA / C1_XMNPDA;

  /* Figure out which side of 225 minutes the period is. */

  A1 = Math.pow(C1_XKE / E1_XNO, C1_TOTHRD);
  t = 1.5 * C1_CK2 * (3. * Math.cos(E1_XINCL) * Math.cos(E1_XINCL) - 1.)
    / Math.pow(1.-E1_EO*E1_EO, 1.5);
  DEL1 = t / (A1 * A1);
  AO = A1 * (1. - DEL1 * (.5 * C1_TOTHRD + DEL1 * (1. + 134. / 81. * DEL1)));
  DELO = t / (AO * AO);
  XNODP = E1_XNO / (1. + DELO);
  if (C2_TWOPI / XNODP >= 225.) {itsIsDeep = 1;} else {itsIsDeep = 0;}

  /* Turn the epoch into a Julian Date minus 2450000 d.
   * First split into two-digit year and day of year.
   * Then turn to four-digit year, taking account of the Y2K kludge.
   * Month is implied to be January, avoid months 1 and 2 (leap day). */

  year  = Math.floor(E1_EPOCH / 1000.);
  day   = E1_EPOCH - 1000. * year;
  if (year < 57) {year += 2000.;} else {year += 1900.;}
  year--;
  itsEpochJD  = 5643.5 - 10000. + day;
  itsEpochJD += 365. * (year - 1985.)
	+ Math.floor(year/4.)
	- Math.floor(year/100.)
	+ Math.floor(year/400.)
	+ 306.;

  return;
}


  /**
   * A helper routine to read a floating point number.
   *
   * <p>This is (or should be) a copy of the same method in the Hmelib class.
   * It is copied here merely to make the SDP4 class self-contained, in case
   * other programmers want to use it without the sticky lump of the Hmelib
   * class. */

  protected final double Rfndm(String aString)
    throws SDP4InvalidNumException
  {
    ParsePosition where = new ParsePosition(0);
    String theString;
    Number theNumber;

    /* Trim off white space and a leading plus sign. */

    theString = aString.trim();
    if (theString.startsWith("+")) theString = theString.substring(1);

    /* Parse the number up to the next invalid character or end of string.
     * Valid characters would be -.0123456789 or something like that. */

    theNumber = form.parse(theString, where);
    if (null == theNumber) throw new SDP4InvalidNumException("invalid number");

    return theNumber.doubleValue();
  }


  /**
   * Run the SDP4 model.
   *
   * <p>This should be run for long-period satellites.  The criterion is
   * evaluated on reading the orbital data from the TLE and stored in the
   * state variable itsIsDeep (should be 1 for calling this routine).
   *
   * @param IFLAG
   *   IFLAG[0] must be given as 1 for the first call for any given satellite.
   *   It is then returned as 0 and can be given as 0 for further calls.
   * @param TSINCE
   *   TSINCE[0] is the time difference between the time of interest and the
   *   epoch of the TLE.  It must be given in minutes. */

  protected final void RunSDP4(int[] IFLAG, double[] TSINCE)
  {
    double A, AXN, AYN, AYNL, BETA, BETAL, CAPU, COS2U, COSEPW,
      COSIK, COSNOK, COSU, COSUK, E, ECOSE, ELSQ, EM, EPW, ESINE, OMGADF,
      PL, R, RDOT, RDOTK, RFDOT, RFDOTK, RK, SIN2U, SINEPW, SINIK,
      SINNOK, SINU, SINUK, TEMP, TEMP4, TEMP5, TEMP6, TEMPA,
      TEMPE, TEMPL, TSQ, U, UK, UX, UY, UZ, VX, VY, VZ, XINC, XINCK,
      XL, XLL, XLT, XMAM, XMDF, XMX, XMY, XN, XNODDF, XNODE, XNODEK;
    double[] dpsec_args = new double[6];
    double[] dpper_args = new double[5];
    int I;

    /* The Java compiler requires these initialisations. */

    TEMP4 = 0.;
    TEMP5 = 0.;
    TEMP6 = 0.;
    COSEPW = 0.;
    SINEPW = 0.;
    EM = 0.;
    XINC = 0.;

    if (IFLAG[0] != 0) {

      /* RECOVER ORIGINAL MEAN MOTION (SDP4_XNODP) AND SEMIMAJOR AXIS
       * (SDP4_AODP) FROM INPUT ELEMENTS */

      SDP4_A1 = Math.pow(C1_XKE / E1_XNO, C1_TOTHRD);
      SDP4_COSIO = Math.cos(E1_XINCL);
      SDP4_THETA2 = SDP4_COSIO * SDP4_COSIO;
      SDP4_X3THM1 = 3. * SDP4_THETA2 - 1.;
      SDP4_EOSQ = E1_EO * E1_EO;
      SDP4_BETAO2 = 1. - SDP4_EOSQ;
      SDP4_BETAO = Math.sqrt(SDP4_BETAO2);
      SDP4_DEL1 = 1.5 * C1_CK2 * SDP4_X3THM1
	/ (SDP4_A1 * SDP4_A1 * SDP4_BETAO * SDP4_BETAO2);
      SDP4_AO = SDP4_A1 * (1. - SDP4_DEL1 * (.5 * C1_TOTHRD + SDP4_DEL1
	* (1. + 134./81. * SDP4_DEL1)));
      SDP4_DELO = 1.5 * C1_CK2 * SDP4_X3THM1
	/ (SDP4_AO * SDP4_AO * SDP4_BETAO * SDP4_BETAO2);
      SDP4_XNODP = E1_XNO / (1. + SDP4_DELO);
      SDP4_AODP = SDP4_AO / (1. - SDP4_DELO);

      /* INITIALIZATION
       *
       * FOR PERIGEE BELOW 156 KM, THE VALUES OF
       * S AND QOMS2T ARE ALTERED */

      SDP4_S4 = C1_S;
      SDP4_QOMS24 = C1_QOMS2T;
      SDP4_PERIGE = (SDP4_AODP * (1. - E1_EO) - C1_AE) * C1_XKMPER;
      if (SDP4_PERIGE < 156.) {
	SDP4_S4 = SDP4_PERIGE - 78.;
	if (SDP4_PERIGE <= 98.) {
	  SDP4_S4 = 20.;
        }
	SDP4_QOMS24 = ((120. - SDP4_S4) * C1_AE / C1_XKMPER);
	SDP4_QOMS24 *= SDP4_QOMS24;
	SDP4_QOMS24 *= SDP4_QOMS24;
	SDP4_S4 = SDP4_S4 / C1_XKMPER + C1_AE;
      }
      SDP4_PINVSQ = 1. / (SDP4_AODP * SDP4_AODP * SDP4_BETAO2 * SDP4_BETAO2);
      SDP4_SING = Math.sin(E1_OMEGAO);
      SDP4_COSG = Math.cos(E1_OMEGAO);
      SDP4_TSI = 1. / (SDP4_AODP - SDP4_S4);
      SDP4_ETA = SDP4_AODP * E1_EO * SDP4_TSI;
      SDP4_ETASQ = SDP4_ETA * SDP4_ETA;
      SDP4_EETA = E1_EO * SDP4_ETA;
      SDP4_PSISQ = Math.abs(1. - SDP4_ETASQ);
      SDP4_COEF = SDP4_QOMS24 * SDP4_TSI * SDP4_TSI * SDP4_TSI * SDP4_TSI;
      SDP4_COEF1 = SDP4_COEF / Math.pow(SDP4_PSISQ, 3.5);
      SDP4_C2 = SDP4_COEF1 * SDP4_XNODP * (SDP4_AODP * (1. + 1.5 * SDP4_ETASQ
	+ SDP4_EETA * (4. + SDP4_ETASQ))
	+ .75 * C1_CK2 * SDP4_TSI / SDP4_PSISQ * SDP4_X3THM1
	* (8. + 3. * SDP4_ETASQ * (8. + SDP4_ETASQ)));
      SDP4_C1 = E1_BSTAR * SDP4_C2;
      SDP4_SINIO = Math.sin(E1_XINCL);
      SDP4_A3OVK2 = -C1_XJ3 / C1_CK2 * C1_AE * C1_AE * C1_AE;
      SDP4_X1MTH2 = 1. - SDP4_THETA2;
      SDP4_C4 = 2. * SDP4_XNODP * SDP4_COEF1 * SDP4_AODP * SDP4_BETAO2
	* (SDP4_ETA * (2. + .5 * SDP4_ETASQ) + E1_EO * (.5 + 2. * SDP4_ETASQ)
	- 2. * C1_CK2 * SDP4_TSI / (SDP4_AODP * SDP4_PSISQ)
	* (-3. * SDP4_X3THM1 * (1. - 2. * SDP4_EETA + SDP4_ETASQ
	* (1.5 - .5 * SDP4_EETA)) + .75 * SDP4_X1MTH2
	* (2. * SDP4_ETASQ - SDP4_EETA * (1. + SDP4_ETASQ))
	* Math.cos(2. * E1_OMEGAO)));
      SDP4_THETA4 = SDP4_THETA2 * SDP4_THETA2;
      SDP4_TEMP1 = 3. * C1_CK2 * SDP4_PINVSQ * SDP4_XNODP;
      SDP4_TEMP2 = SDP4_TEMP1 * C1_CK2 * SDP4_PINVSQ;
      SDP4_TEMP3 = 1.25 * C1_CK4 * SDP4_PINVSQ * SDP4_PINVSQ * SDP4_XNODP;
      SDP4_XMDOT = SDP4_XNODP + .5 * SDP4_TEMP1 * SDP4_BETAO * SDP4_X3THM1
	+ .0625 * SDP4_TEMP2 * SDP4_BETAO
	* (13. - 78. * SDP4_THETA2 + 137. * SDP4_THETA4);
      SDP4_X1M5TH = 1. - 5. * SDP4_THETA2;
      SDP4_OMGDOT = -.5 * SDP4_TEMP1 * SDP4_X1M5TH
	+ .0625 * SDP4_TEMP2 * (7. - 114. * SDP4_THETA2 + 395. * SDP4_THETA4)
	+ SDP4_TEMP3 * (3. - 36. * SDP4_THETA2 + 49. * SDP4_THETA4);
      SDP4_XHDOT1 = -SDP4_TEMP1 * SDP4_COSIO;
      SDP4_XNODOT = SDP4_XHDOT1 + (.5 * SDP4_TEMP2 * (4. - 19. * SDP4_THETA2)
	+ 2. * SDP4_TEMP3 * (3. - 7. * SDP4_THETA2)) * SDP4_COSIO;
      SDP4_XNODCF = 3.5 * SDP4_BETAO2 * SDP4_XHDOT1 * SDP4_C1;
      SDP4_T2COF = 1.5 * SDP4_C1;
      SDP4_XLCOF = .125 * SDP4_A3OVK2 * SDP4_SINIO
	* (3. + 5. * SDP4_COSIO) / (1. + SDP4_COSIO);
      SDP4_AYCOF = .25 * SDP4_A3OVK2 * SDP4_SINIO;
      SDP4_X7THM1 = 7. * SDP4_THETA2 - 1.;
      IFLAG[0] = 0;
      DPINIT(SDP4_EOSQ, SDP4_SINIO, SDP4_COSIO, SDP4_BETAO, SDP4_AODP,
	SDP4_THETA2, SDP4_SING, SDP4_COSG, SDP4_BETAO2, SDP4_XMDOT,
	SDP4_OMGDOT, SDP4_XNODOT, SDP4_XNODP);
    }

    /* UPDATE FOR SECULAR GRAVITY AND ATMOSPHERIC DRAG */

    XMDF   = E1_XMO    + SDP4_XMDOT  * TSINCE[0];
    OMGADF = E1_OMEGAO + SDP4_OMGDOT * TSINCE[0];
    XNODDF = E1_XNODEO + SDP4_XNODOT * TSINCE[0];
    TSQ = TSINCE[0] * TSINCE[0];
    XNODE = XNODDF + SDP4_XNODCF * TSQ;
    TEMPA = 1. - SDP4_C1 * TSINCE[0];
    TEMPE = E1_BSTAR * SDP4_C4 * TSINCE[0];
    TEMPL = SDP4_T2COF * TSQ;
    XN = SDP4_XNODP;

    dpsec_args[0] = XMDF;
    dpsec_args[1] = OMGADF;
    dpsec_args[2] = XNODE;
    dpsec_args[3] = EM;
    dpsec_args[4] = XINC;
    dpsec_args[5] = XN;
    DPSEC(dpsec_args, TSINCE);
    XMDF   = dpsec_args[0];
    OMGADF = dpsec_args[1];
    XNODE  = dpsec_args[2];
    EM     = dpsec_args[3];
    XINC   = dpsec_args[4];
    XN     = dpsec_args[5];

    A = Math.pow(C1_XKE / XN, C1_TOTHRD) * TEMPA * TEMPA;
    E = EM - TEMPE;
    XMAM = XMDF + SDP4_XNODP * TEMPL;

    dpper_args[0] = E;
    dpper_args[1] = XINC;
    dpper_args[2] = OMGADF;
    dpper_args[3] = XNODE;
    dpper_args[4] = XMAM;
    DPPER(dpper_args);
    E      = dpper_args[0];
    XINC   = dpper_args[1];
    OMGADF = dpper_args[2];
    XNODE  = dpper_args[3];
    XMAM   = dpper_args[4];

    XL = XMAM + OMGADF + XNODE;
    BETA = Math.sqrt(1. - E * E);
    XN = C1_XKE / Math.pow(A, 1.5);

    /* LONG PERIOD PERIODICS */

    AXN = E * Math.cos(OMGADF);
    TEMP = 1. / (A * BETA * BETA);
    XLL = TEMP * SDP4_XLCOF * AXN;
    AYNL = TEMP * SDP4_AYCOF;
    XLT = XL + XLL;
    AYN = E * Math.sin(OMGADF) + AYNL;

    /* SOLVE KEPLERS EQUATION */

    CAPU = FMOD2P(XLT - XNODE);
    SDP4_TEMP2 = CAPU;
    for (I = 1; I < 11; I++) {
      SINEPW = Math.sin(SDP4_TEMP2);
      COSEPW = Math.cos(SDP4_TEMP2);
      SDP4_TEMP3 = AXN * SINEPW;
      TEMP4 = AYN * COSEPW;
      TEMP5 = AXN * COSEPW;
      TEMP6 = AYN * SINEPW;
      EPW = (CAPU - TEMP4 + SDP4_TEMP3 - SDP4_TEMP2)
	/ (1. - TEMP5 - TEMP6) + SDP4_TEMP2;
      if (Math.abs(EPW-SDP4_TEMP2) <= C1_E6A) break;
      SDP4_TEMP2 = EPW;
    }

    /* SHORT PERIOD PRELIMINARY QUANTITIES */

    ECOSE = TEMP5 + TEMP6;
    ESINE = SDP4_TEMP3 - TEMP4;
    ELSQ = AXN * AXN + AYN * AYN;
    TEMP = 1. - ELSQ;
    PL = A * TEMP;
    R = A * (1. - ECOSE);
    SDP4_TEMP1 = 1. / R;
    RDOT = C1_XKE * Math.sqrt(A) * ESINE * SDP4_TEMP1;
    RFDOT = C1_XKE * Math.sqrt(PL) * SDP4_TEMP1;
    SDP4_TEMP2 = A * SDP4_TEMP1;
    BETAL = Math.sqrt(TEMP);
    SDP4_TEMP3 = 1. / (1. + BETAL);
    COSU = SDP4_TEMP2 * (COSEPW - AXN + AYN * ESINE * SDP4_TEMP3);
    SINU = SDP4_TEMP2 * (SINEPW - AYN - AXN * ESINE * SDP4_TEMP3);
    U = ACTAN(SINU, COSU);
    SIN2U =2. * SINU * COSU;
    COS2U =2. * COSU * COSU - 1.;
    TEMP = 1. / PL;
    SDP4_TEMP1 = C1_CK2 * TEMP;
    SDP4_TEMP2 = SDP4_TEMP1 * TEMP;

    /* UPDATE FOR SHORT PERIODICS */

    RK = R * (1. - 1.5 * SDP4_TEMP2 * BETAL * SDP4_X3THM1)
      + .5 * SDP4_TEMP1 * SDP4_X1MTH2 * COS2U;
    UK = U - .25 * SDP4_TEMP2 * SDP4_X7THM1 * SIN2U;
    XNODEK = XNODE + 1.5 * SDP4_TEMP2 * SDP4_COSIO * SIN2U;
    XINCK = XINC + 1.5 * SDP4_TEMP2 * SDP4_COSIO * SDP4_SINIO * COS2U;
    RDOTK = RDOT - XN * SDP4_TEMP1 * SDP4_X1MTH2 * SIN2U;
    RFDOTK = RFDOT + XN * SDP4_TEMP1
      * (SDP4_X1MTH2 * COS2U + 1.5 * SDP4_X3THM1);

    /* ORIENTATION VECTORS */

    SINUK = Math.sin(UK);
    COSUK = Math.cos(UK);
    SINIK = Math.sin(XINCK);
    COSIK = Math.cos(XINCK);
    SINNOK = Math.sin(XNODEK);
    COSNOK = Math.cos(XNODEK);
    XMX = -SINNOK * COSIK;
    XMY =  COSNOK * COSIK;
    UX = XMX * SINUK + COSNOK * COSUK;
    UY = XMY * SINUK + SINNOK * COSUK;
    UZ = SINIK * SINUK;
    VX = XMX * COSUK - COSNOK * SINUK;
    VY = XMY * COSUK - SINNOK * SINUK;
    VZ = SINIK * COSUK;

    /* POSITION AND VELOCITY */

    E1_X = RK * UX;
    E1_Y = RK * UY;
    E1_Z = RK * UZ;
    E1_XDOT = RDOTK * UX + RFDOTK * VX;
    E1_YDOT = RDOTK * UY + RFDOTK * VY;
    E1_ZDOT = RDOTK * UZ + RFDOTK * VZ;

    itsR[0] = E1_X    * C1_XKMPER / C1_AE / 1E6;
    itsR[1] = E1_Y    * C1_XKMPER / C1_AE / 1E6;
    itsR[2] = E1_Z    * C1_XKMPER / C1_AE / 1E6;
    itsV[0] = E1_XDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;
    itsV[1] = E1_YDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;
    itsV[2] = E1_ZDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;

    return;
  }


  /**
   * Run the SGP4 model.
   *
   * <p>This should be run for short-period satellites.  The criterion is
   * evaluated on reading the orbital data from the TLE and stored in the
   * state variable itsIsDeep (should be 0 for calling this routine).
   *
   * @param IFLAG
   *   IFLAG[0] must be given as 1 for the first call for any given satellite.
   *   It is then returned as 0 and can be given as 0 for further calls.
   * @param TSINCE
   *   TSINCE[0] is the time difference between the time of interest and the
   *   epoch of the TLE.  It must be given in minutes. */

  protected final void RunSGP4(int[] IFLAG, double[] TSINCE)
  {
    double COSUK, SINUK, RFDOTK, VX, VY, VZ, UX, UY, UZ, XMY, XMX,
      COSNOK, SINNOK, COSIK, SINIK, RDOTK, XINCK, XNODEK, UK, RK,
      COS2U, SIN2U, U, SINU, COSU, BETAL, RFDOT, RDOT, R, PL, ELSQ,
      ESINE, ECOSE, EPW, TEMP6, TEMP5, TEMP4, COSEPW, SINEPW,
      CAPU, AYN, XLT, AYNL, XLL, AXN, XN, BETA, XL, E, A, TFOUR,
      TCUBE, DELM, DELOMG, TEMPL, TEMPE, TEMPA, XNODE, TSQ, XMP,
      OMEGA, XNODDF, OMGADF, XMDF;
    int I;

    /* The Java compiler requires these initialisations. */

    TEMP4 = 0.;
    TEMP5 = 0.;
    TEMP6 = 0.;
    COSEPW = 0.;
    SINEPW = 0.;

    if (IFLAG[0] != 0) {

      /* RECOVER ORIGINAL MEAN MOTION (SGP4_XNODP) AND SEMIMAJOR AXIS
       * (SGP4_AODP) FROM INPUT ELEMENTS */

      SGP4_A1 = Math.pow(C1_XKE / E1_XNO, C1_TOTHRD);
      SGP4_COSIO = Math.cos(E1_XINCL);
      SGP4_THETA2 = SGP4_COSIO * SGP4_COSIO;
      SGP4_X3THM1 = 3. * SGP4_THETA2 - 1.;
      SGP4_EOSQ = E1_EO * E1_EO;
      SGP4_BETAO2 = 1. - SGP4_EOSQ;
      SGP4_BETAO = Math.sqrt(SGP4_BETAO2);
      SGP4_DEL1 = 1.5 * C1_CK2 * SGP4_X3THM1
	/ (SGP4_A1 * SGP4_A1 * SGP4_BETAO * SGP4_BETAO2);
      SGP4_AO = SGP4_A1 * (1. - SGP4_DEL1
	* (.5 * C1_TOTHRD + SGP4_DEL1 * (1. + 134./81. * SGP4_DEL1)));
      SGP4_DELO = 1.5 * C1_CK2 * SGP4_X3THM1
	/ (SGP4_AO * SGP4_AO * SGP4_BETAO * SGP4_BETAO2);
      SGP4_XNODP = E1_XNO / (1. + SGP4_DELO);
      SGP4_AODP = SGP4_AO / (1. - SGP4_DELO);

      /* INITIALIZATION
       *
       * FOR PERIGEE LESS THAN 220 KILOMETERS, THE SGP4_ISIMP FLAG IS SET AND
       * THE EQUATIONS ARE TRUNCATED TO LINEAR VARIATION IN SQRT A AND
       * QUADRATIC VARIATION IN MEAN ANOMALY.  ALSO, THE SGP4_C3 TERM, THE
       * DELTA OMEGA TERM, AND THE DELTA M TERM ARE DROPPED. */

      SGP4_ISIMP = 0;
      if ((SGP4_AODP * (1. - E1_EO) / C1_AE) < (220. / C1_XKMPER + C1_AE)) {
	SGP4_ISIMP = 1;
      }

      /* FOR PERIGEE BELOW 156 KM, THE VALUES OF
       * S AND QOMS2T ARE ALTERED */

      SGP4_S4 = C1_S;
      SGP4_QOMS24 = C1_QOMS2T;
      SGP4_PERIGE = (SGP4_AODP * (1. - E1_EO) - C1_AE) * C1_XKMPER;
      if (SGP4_PERIGE < 156.) {
        SGP4_S4 = SGP4_PERIGE - 78.;
	if (SGP4_PERIGE <= 98.) {SGP4_S4 = 20.;}
	SGP4_QOMS24 = (120. - SGP4_S4) * C1_AE / C1_XKMPER;
	SGP4_QOMS24 *= SGP4_QOMS24;
	SGP4_QOMS24 *= SGP4_QOMS24;
	SGP4_S4 = SGP4_S4 / C1_XKMPER + C1_AE;
      }
      SGP4_PINVSQ = 1. / (SGP4_AODP * SGP4_AODP * SGP4_BETAO2 * SGP4_BETAO2);
      SGP4_TSI = 1. / (SGP4_AODP - SGP4_S4);
      SGP4_ETA = SGP4_AODP * E1_EO * SGP4_TSI;
      SGP4_ETASQ = SGP4_ETA * SGP4_ETA;
      SGP4_EETA = E1_EO * SGP4_ETA;
      SGP4_PSISQ = Math.abs(1. - SGP4_ETASQ);
      SGP4_COEF = SGP4_QOMS24 * SGP4_TSI * SGP4_TSI * SGP4_TSI * SGP4_TSI;
      SGP4_COEF1 = SGP4_COEF / Math.pow(SGP4_PSISQ, 3.5);
      SGP4_C2 = SGP4_COEF1 * SGP4_XNODP * (SGP4_AODP * (1. + 1.5 * SGP4_ETASQ
	+ SGP4_EETA * (4. + SGP4_ETASQ)) + .75 * C1_CK2 * SGP4_TSI / SGP4_PSISQ
	* SGP4_X3THM1 * (8. + 3. * SGP4_ETASQ * (8. + SGP4_ETASQ)));
      SGP4_C1 = E1_BSTAR * SGP4_C2;
      SGP4_SINIO = Math.sin(E1_XINCL);
      SGP4_A3OVK2 = -C1_XJ3 / C1_CK2 * C1_AE * C1_AE * C1_AE;
      SGP4_C3 = SGP4_COEF * SGP4_TSI * SGP4_A3OVK2 * SGP4_XNODP * C1_AE
	* SGP4_SINIO / E1_EO;
      SGP4_X1MTH2 = 1. - SGP4_THETA2;
      SGP4_C4 = 2. * SGP4_XNODP * SGP4_COEF1 * SGP4_AODP * SGP4_BETAO2
	* (SGP4_ETA * (2. + .5 * SGP4_ETASQ) + E1_EO * (.5 + 2. * SGP4_ETASQ)
	- 2. * C1_CK2 * SGP4_TSI / (SGP4_AODP * SGP4_PSISQ)
	* (-3. * SGP4_X3THM1 * (1. - 2. * SGP4_EETA + SGP4_ETASQ
	* (1.5 - .5 * SGP4_EETA)) + .75 * SGP4_X1MTH2
	* (2. * SGP4_ETASQ - SGP4_EETA * (1. + SGP4_ETASQ))
	* Math.cos(2. * E1_OMEGAO)));
      SGP4_C5 = 2. * SGP4_COEF1 * SGP4_AODP * SGP4_BETAO2
	* (1. + 2.75 * (SGP4_ETASQ + SGP4_EETA) + SGP4_EETA * SGP4_ETASQ);
      SGP4_THETA4 = SGP4_THETA2 * SGP4_THETA2;
      SGP4_TEMP1 = 3. * C1_CK2 * SGP4_PINVSQ * SGP4_XNODP;
      SGP4_TEMP2 = SGP4_TEMP1 * C1_CK2 * SGP4_PINVSQ;
      SGP4_TEMP3 = 1.25 * C1_CK4 * SGP4_PINVSQ * SGP4_PINVSQ * SGP4_XNODP;
      SGP4_XMDOT = SGP4_XNODP + .5 * SGP4_TEMP1 * SGP4_BETAO * SGP4_X3THM1
	+ .0625 * SGP4_TEMP2 * SGP4_BETAO * (13. - 78. * SGP4_THETA2
	+ 137. * SGP4_THETA4);
      SGP4_X1M5TH = 1. - 5. * SGP4_THETA2;
      SGP4_OMGDOT = -.5 * SGP4_TEMP1 * SGP4_X1M5TH
	+ .0625 * SGP4_TEMP2 * (7. - 114. * SGP4_THETA2 + 395. * SGP4_THETA4)
	+ SGP4_TEMP3 * (3. - 36. * SGP4_THETA2 + 49. * SGP4_THETA4);
      SGP4_XHDOT1 = -SGP4_TEMP1 * SGP4_COSIO;
      SGP4_XNODOT = SGP4_XHDOT1 + (.5 * SGP4_TEMP2 * (4. - 19. * SGP4_THETA2)
	+ 2. * SGP4_TEMP3 * (3. - 7. * SGP4_THETA2)) * SGP4_COSIO;
      SGP4_OMGCOF = E1_BSTAR * SGP4_C3 * Math.cos(E1_OMEGAO);
      SGP4_XMCOF = -C1_TOTHRD * SGP4_COEF * E1_BSTAR * C1_AE / SGP4_EETA;
      SGP4_XNODCF = 3.5 * SGP4_BETAO2 * SGP4_XHDOT1 * SGP4_C1;
      SGP4_T2COF = 1.5 * SGP4_C1;
      SGP4_XLCOF = .125 * SGP4_A3OVK2 * SGP4_SINIO 
	  * (3. + 5. * SGP4_COSIO) / (1. + SGP4_COSIO);
      SGP4_AYCOF = .25 * SGP4_A3OVK2 * SGP4_SINIO;
      SGP4_DELMO = (1. + SGP4_ETA * Math.cos(E1_XMO));
      SGP4_DELMO *= (SGP4_DELMO * SGP4_DELMO);
      SGP4_SINMO = Math.sin(E1_XMO);
      SGP4_X7THM1 = 7. * SGP4_THETA2 - 1.;
      if (SGP4_ISIMP != 1) {
	SGP4_C1SQ = SGP4_C1 * SGP4_C1;
	SGP4_D2 = 4. * SGP4_AODP * SGP4_TSI * SGP4_C1SQ;
	SGP4_TEMP = SGP4_D2 * SGP4_TSI * SGP4_C1 / 3.;
	SGP4_D3 = (17. * SGP4_AODP + SGP4_S4) * SGP4_TEMP;
	SGP4_D4 = .5 * SGP4_TEMP * SGP4_AODP * SGP4_TSI
	  * (221. * SGP4_AODP + 31. * SGP4_S4) * SGP4_C1;
	SGP4_T3COF = SGP4_D2 + 2. * SGP4_C1SQ;
	SGP4_T4COF = .25 * (3. * SGP4_D3 + SGP4_C1
	  * (12. * SGP4_D2 + 10. * SGP4_C1SQ));
	SGP4_T5COF = .2 * (3. * SGP4_D4 + 12. * SGP4_C1 * SGP4_D3
	  + 6. * SGP4_D2 * SGP4_D2
	  + 15. * SGP4_C1SQ * (2. * SGP4_D2 + SGP4_C1SQ));
      }
      IFLAG[0] = 0;
    }

    /* UPDATE FOR SECULAR GRAVITY AND ATMOSPHERIC DRAG */

    XMDF   = E1_XMO    + SGP4_XMDOT  * TSINCE[0];
    OMGADF = E1_OMEGAO + SGP4_OMGDOT * TSINCE[0];
    XNODDF = E1_XNODEO + SGP4_XNODOT * TSINCE[0];
    OMEGA = OMGADF;
    XMP = XMDF;
    TSQ = TSINCE[0] * TSINCE[0];
    XNODE = XNODDF + SGP4_XNODCF * TSQ;
    TEMPA = 1. - SGP4_C1 * TSINCE[0];
    TEMPE = E1_BSTAR * SGP4_C4 * TSINCE[0];
    TEMPL = SGP4_T2COF * TSQ;
    if (SGP4_ISIMP != 1) {
      DELOMG = SGP4_OMGCOF * TSINCE[0];
      DELM = SGP4_XMCOF * (Math.pow(1. + SGP4_ETA * Math.cos(XMDF), 3.)
	- SGP4_DELMO);
      SGP4_TEMP = DELOMG + DELM;
      XMP = XMDF + SGP4_TEMP;
      OMEGA = OMGADF - SGP4_TEMP;
      TCUBE = TSQ * TSINCE[0];
      TFOUR = TSINCE[0] * TCUBE;
      TEMPA = TEMPA - SGP4_D2 * TSQ - SGP4_D3 * TCUBE - SGP4_D4 * TFOUR;
      TEMPE = TEMPE + E1_BSTAR * SGP4_C5 * (Math.sin(XMP) - SGP4_SINMO);
      TEMPL = TEMPL + SGP4_T3COF * TCUBE
	+ TFOUR * (SGP4_T4COF + TSINCE[0] * SGP4_T5COF);
    }
    A = SGP4_AODP * TEMPA * TEMPA;
    E = E1_EO - TEMPE;
    XL = XMP + OMEGA + XNODE + SGP4_XNODP * TEMPL;
    BETA = Math.sqrt(1. - E * E);
    XN = C1_XKE / Math.pow(A, 1.5);

    /* LONG PERIOD PERIODICS */

    AXN = E * Math.cos(OMEGA);
    SGP4_TEMP = 1. / (A * BETA * BETA);
    XLL = SGP4_TEMP * SGP4_XLCOF * AXN;
    AYNL = SGP4_TEMP * SGP4_AYCOF;
    XLT = XL + XLL;
    AYN = E * Math.sin(OMEGA) + AYNL;

    /* SOLVE KEPLERS EQUATION */

    CAPU = FMOD2P(XLT - XNODE);
    SGP4_TEMP2 = CAPU;
    for (I = 1; I < 11; I++) {
      SINEPW = Math.sin(SGP4_TEMP2);
      COSEPW = Math.cos(SGP4_TEMP2);
      SGP4_TEMP3 = AXN * SINEPW;
      TEMP4 = AYN * COSEPW;
      TEMP5 = AXN * COSEPW;
      TEMP6 = AYN * SINEPW;
      EPW = (CAPU - TEMP4 + SGP4_TEMP3 - SGP4_TEMP2) 
	/ (1. - TEMP5 - TEMP6) + SGP4_TEMP2;
      if (Math.abs(EPW - SGP4_TEMP2) <= C1_E6A) break;
      SGP4_TEMP2 = EPW;
    }

    /* SHORT PERIOD PRELIMINARY QUANTITIES */

    ECOSE = TEMP5 + TEMP6;
    ESINE = SGP4_TEMP3 - TEMP4;
    ELSQ = AXN * AXN + AYN * AYN;
    SGP4_TEMP = 1. - ELSQ;
    PL = A * SGP4_TEMP;
    R = A * (1. - ECOSE);
    SGP4_TEMP1 = 1. / R;
    RDOT = C1_XKE * Math.sqrt(A) * ESINE * SGP4_TEMP1;
    RFDOT = C1_XKE * Math.sqrt(PL) * SGP4_TEMP1;
    SGP4_TEMP2 = A * SGP4_TEMP1;
    BETAL = Math.sqrt(SGP4_TEMP);
    SGP4_TEMP3 = 1. / (1. + BETAL);
    COSU = SGP4_TEMP2 * (COSEPW - AXN + AYN * ESINE * SGP4_TEMP3);
    SINU = SGP4_TEMP2 * (SINEPW - AYN - AXN * ESINE * SGP4_TEMP3);
    U = ACTAN(SINU, COSU);
    SIN2U = 2. * SINU * COSU;
    COS2U = 2. * COSU * COSU - 1.;
    SGP4_TEMP = 1. / PL;
    SGP4_TEMP1 = C1_CK2 * SGP4_TEMP;
    SGP4_TEMP2 = SGP4_TEMP1 * SGP4_TEMP;

    /* UPDATE FOR SHORT PERIODICS */

    RK = R * (1. - 1.5 * SGP4_TEMP2 * BETAL * SGP4_X3THM1)
      + .5 * SGP4_TEMP1 * SGP4_X1MTH2 * COS2U;
    UK = U - .25 * SGP4_TEMP2 * SGP4_X7THM1 * SIN2U;
    XNODEK = XNODE + 1.5 * SGP4_TEMP2 * SGP4_COSIO * SIN2U;
    XINCK = E1_XINCL + 1.5 * SGP4_TEMP2 * SGP4_COSIO * SGP4_SINIO*COS2U;
    RDOTK = RDOT - XN * SGP4_TEMP1 * SGP4_X1MTH2 * SIN2U;
    RFDOTK = RFDOT + XN * SGP4_TEMP1
      * (SGP4_X1MTH2 * COS2U + 1.5 * SGP4_X3THM1);

    /* ORIENTATION VECTORS */

    SINUK = Math.sin(UK);
    COSUK = Math.cos(UK);
    SINIK = Math.sin(XINCK);
    COSIK = Math.cos(XINCK);
    SINNOK = Math.sin(XNODEK);
    COSNOK = Math.cos(XNODEK);
    XMX = -SINNOK * COSIK;
    XMY =  COSNOK * COSIK;
    UX = XMX * SINUK + COSNOK * COSUK;
    UY = XMY * SINUK + SINNOK * COSUK;
    UZ = SINIK * SINUK;
    VX = XMX * COSUK - COSNOK * SINUK;
    VY = XMY * COSUK - SINNOK * SINUK;
    VZ = SINIK * COSUK;

    /* POSITION AND VELOCITY */

    E1_X = RK * UX;
    E1_Y = RK * UY;
    E1_Z = RK * UZ;
    E1_XDOT = RDOTK * UX + RFDOTK * VX;
    E1_YDOT = RDOTK * UY + RFDOTK * VY;
    E1_ZDOT = RDOTK * UZ + RFDOTK * VZ;

    itsR[0] = E1_X    * C1_XKMPER / C1_AE / 1E6;
    itsR[1] = E1_Y    * C1_XKMPER / C1_AE / 1E6;
    itsR[2] = E1_Z    * C1_XKMPER / C1_AE / 1E6;
    itsV[0] = E1_XDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;
    itsV[1] = E1_YDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;
    itsV[2] = E1_ZDOT * C1_XKMPER / C1_AE * C1_XMNPDA / 86400.;

    return;
  }


  /**
   * A helper routine to calculate the Greenwich sidereal time. */

  protected final double THETAG(double EP)
  {
    double D, THETA, TWOPI, YR, TEMP, value;
    int JY, N, I;
    TWOPI = 6.28318530717959;
    YR = (EP + 2.E-7) * 1.E-3;
    JY = (int)YR;
    YR = JY;
    D = EP - YR * 1.E3;
    if (JY < 57) JY = JY + 100;
    N = (JY - 69) / 4;
    if (JY < 70) N = (JY - 72) / 4;
    E1_DS50 = 7305. + 365. * (JY - 70) + N + D;
    THETA = 1.72944494 + 6.3003880987 * E1_DS50;
    TEMP = THETA / TWOPI;
    I = (int)TEMP;
    TEMP = I;
    value = THETA - TEMP * TWOPI;
    if (value < 0.) value = value + TWOPI;
    return value;
  }

}
