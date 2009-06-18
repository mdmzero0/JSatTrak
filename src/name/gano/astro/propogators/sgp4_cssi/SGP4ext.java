/*     ----------------------------------------------------------------
*
*                               sgp4ext.cpp
*
*    this file contains extra routines needed for the main test program for sgp4.
*    these routines are derived from the astro libraries.
*
*                            companion code for
*               fundamentals of astrodynamics and applications
*                                    2007
*                              by david vallado
*
*       (w) 719-573-2600, email dvallado@agi.com
*
*    current :
*               7 may 08  david vallado
*                           fix sgn
*    changes :
*               2 apr 07  david vallado
*                           fix jday floor and str lengths
*                           updates for constants
*              14 aug 06  david vallado
*                           original baseline
*       ----------------------------------------------------------------      */
package name.gano.astro.propogators.sgp4_cssi;

/**
 *
 * @author Shawn E. Gano, 18 June 2009
 */
public class SGP4ext
{
    /* -----------------------------------------------------------------------------
*
*                           function rv2coe
*
*  this function finds the classical orbital elements given the geocentric
*    equatorial position and velocity vectors.
*
*  author        : david vallado                  719-573-2600   21 jun 2002
*
*  revisions
*    vallado     - fix special cases                              5 sep 2002
*    vallado     - delete extra check in inclination code        16 oct 2002
*    vallado     - add constant file use                         29 jun 2003
*    vallado     - add mu                                         2 apr 2007
*
*  inputs          description                    range / units
*    r           - ijk position vector            km
*    v           - ijk velocity vector            km / s
*    mu          - gravitational parameter        km3 / s2
*
*  outputs       :
*    p           - semilatus rectum               km
*    a           - semimajor axis                 km
*    ecc         - eccentricity
*    incl        - inclination                    0.0  to pi rad
*    omega       - longitude of ascending node    0.0  to 2pi rad
*    argp        - argument of perigee            0.0  to 2pi rad
*    nu          - true anomaly                   0.0  to 2pi rad
*    m           - mean anomaly                   0.0  to 2pi rad
*    arglat      - argument of latitude      (ci) 0.0  to 2pi rad
*    truelon     - true longitude            (ce) 0.0  to 2pi rad
*    lonper      - longitude of periapsis    (ee) 0.0  to 2pi rad
*
*  locals        :
*    hbar        - angular momentum h vector      km2 / s
*    ebar        - eccentricity     e vector
*    nbar        - line of nodes    n vector
*    c1          - v**2 - u/r
*    rdotv       - r dot v
*    hk          - hk unit vector
*    sme         - specfic mechanical energy      km2 / s2
*    i           - index
*    e           - eccentric, parabolic,
*                  hyperbolic anomaly             rad
*    temp        - temporary variable
*    typeorbit   - type of orbit                  ee, ei, ce, ci
*
*  coupling      :
*    mag         - magnitude of a vector
*    cross       - cross product of two vectors
*    angle       - find the angle between two vectors
*    newtonnu    - find the mean anomaly
*
*  references    :
*    vallado       2007, 126, alg 9, ex 2-5
* --------------------------------------------------------------------------- */
// returns array:
// [p, a, ecc, incl, omega, argp, nu, m, arglat, truelon, lonper]
public static double[] rv2coe
     (
       double[] r, double[] v, double mu//,
//       double& p, double& a, double& ecc, double& incl, double& omega, double& argp,
//       double& nu, double& m, double& arglat, double& truelon, double& lonper
     )
     {

    // return variables
    double p, a, ecc, incl, omega, argp, nu, m, arglat, truelon, lonper;

    // internal

       double undefined, small, magr, magv, magn,  sme,
              rdotv, infinite, temp, c1, hk, twopi, magh, halfpi, e;
       double[] hbar = new double[3];
       double[] nbar = new double[3];
       double[] ebar = new double[3];

       int i;
       String typeorbit;

     twopi  = 2.0 * Math.PI;
     halfpi = 0.5 * Math.PI;
     small  = 0.00000001;
     undefined = 999999.1;
     infinite  = 999999.9;

     // SEG m needs to be ini
     m = undefined;

     // -------------------------  implementation   -----------------
     magr = mag( r );
     magv = mag( v );

     // ------------------  find h n and e vectors   ----------------
     cross( r,v, hbar );
     magh = mag( hbar );
     if ( magh > small )
       {
         nbar[0]= -hbar[1];
         nbar[1]=  hbar[0];
         nbar[2]=   0.0;
         magn = mag( nbar );
         c1 = magv*magv - mu /magr;
         rdotv = dot( r,v );
         for (i= 0; i <= 2; i++)
             ebar[i]= (c1*r[i] - rdotv*v[i])/mu;
         ecc = mag( ebar );

         // ------------  find a e and semi-latus rectum   ----------
         sme= ( magv*magv*0.5  ) - ( mu /magr );
         if ( Math.abs( sme ) > small )
             a= -mu  / (2.0 *sme);
           else
             a= infinite;
         p = magh*magh/mu;

         // -----------------  find inclination   -------------------
         hk= hbar[2]/magh;
         incl= Math.acos( hk );

         // --------  determine type of orbit for later use  --------
         // ------ elliptical, parabolic, hyperbolic inclined -------
         typeorbit = "ei";
         if ( ecc < small )
           {
             // ----------------  circular equatorial ---------------
             if  ((incl<small) | (Math.abs(incl-Math.PI)<small))
                 typeorbit = "ce";
               else
                 // --------------  circular inclined ---------------
                 typeorbit = "ci";
           }
           else
           {
             // - elliptical, parabolic, hyperbolic equatorial --
             if  ((incl<small) | (Math.abs(incl-Math.PI)<small))
                 typeorbit = "ee";
           }

         // ----------  find longitude of ascending node ------------
         if ( magn > small )
           {
             temp= nbar[0] / magn;
             if ( Math.abs(temp) > 1.0  )
                 temp= Math.signum(temp);
             omega= Math.acos( temp );
             if ( nbar[1] < 0.0  )
                 omega= twopi - omega;
           }
           else
             omega= undefined;

         // ---------------- find argument of perigee ---------------
         if ( typeorbit.equalsIgnoreCase("ei") == true ) // 0 = true for cpp strcmp
           {
             argp = angle( nbar,ebar);
             if ( ebar[2] < 0.0  )
                 argp= twopi - argp;
           }
           else
             argp= undefined;

         // ------------  find true anomaly at epoch    -------------
         if (typeorbit.startsWith("e") )//typeorbit[0] == 'e' )
           {
             nu =  angle( ebar,r);
             if ( rdotv < 0.0  )
                 nu= twopi - nu;
           }
           else
             nu= undefined;

         // ----  find argument of latitude - circular inclined -----
         if ( typeorbit.equalsIgnoreCase("ci") == true )
           {
             arglat = angle( nbar,r );
             if ( r[2] < 0.0  )
                 arglat= twopi - arglat;
             m = arglat;
           }
           else
             arglat= undefined;

         // -- find longitude of perigee - elliptical equatorial ----
         if  (( ecc>small ) && (typeorbit.equalsIgnoreCase("ee") == true))
           {
             temp= ebar[0]/ecc;
             if ( Math.abs(temp) > 1.0  )
                 temp= Math.signum(temp);
             lonper= Math.acos( temp );
             if ( ebar[1] < 0.0  )
                 lonper= twopi - lonper;
             if ( incl > halfpi )
                 lonper= twopi - lonper;
           }
           else
           {
             lonper= undefined;
           }

         // -------- find true longitude - circular equatorial ------
         if  (( magr>small ) && ( typeorbit.equalsIgnoreCase("ce") == true ))
           {
             temp= r[0]/magr;
             if ( Math.abs(temp) > 1.0  )
                 temp= Math.signum(temp);
             truelon= Math.acos( temp );
             if ( r[1] < 0.0  )
                 truelon= twopi - truelon;
             if ( incl > halfpi )
                 truelon= twopi - truelon;
             m = truelon;
           }
           else
           {
             truelon= undefined;
           }

         // ------------ find mean anomaly for all orbits -----------
         if ( typeorbit.startsWith("e") )//typeorbit[0] == 'e' )
         {
             double[] tt = newtonnu(ecc,nu );
             e = tt[0];
             m = tt[1];
         }
     }
      else
     {
        p    = undefined;
        a    = undefined;
        ecc  = undefined;
        incl = undefined;
        omega= undefined;
        argp = undefined;
        nu   = undefined;
        m    = undefined;
        arglat = undefined;
        truelon= undefined;
        lonper = undefined;
     }

     return new double[] {p, a, ecc, incl, omega, argp, nu, m, arglat, truelon, lonper};
   }  // end rv2coe

/* -----------------------------------------------------------------------------
*
*                           function newtonnu
*
*  this function solves keplers equation when the true anomaly is known.
*    the mean and eccentric, parabolic, or hyperbolic anomaly is also found.
*    the parabolic limit at 168ø is arbitrary. the hyperbolic anomaly is also
*    limited. the hyperbolic sine is used because it's not double valued.
*
*  author        : david vallado                  719-573-2600   27 may 2002
*
*  revisions
*    vallado     - fix small                                     24 sep 2002
*
*  inputs          description                    range / units
*    ecc         - eccentricity                   0.0  to
*    nu          - true anomaly                   -2pi to 2pi rad
*
*  outputs       :
*    e0          - eccentric anomaly              0.0  to 2pi rad       153.02 ø
*    m           - mean anomaly                   0.0  to 2pi rad       151.7425 ø
*
*  locals        :
*    e1          - eccentric anomaly, next value  rad
*    sine        - sine of e
*    cose        - cosine of e
*    ktr         - index
*
*  coupling      :
*    asinh       - arc hyperbolic sine
*
*  references    :
*    vallado       2007, 85, alg 5
* --------------------------------------------------------------------------- */
// returns [e0, m]
    public static double[] newtonnu(double ecc, double nu)
    {
        // return vars
        double e0, m;

        // internal

        double small, sine, cose;

        // ---------------------  implementation   ---------------------
        e0 = 999999.9;
        m = 999999.9;
        small = 0.00000001;

        // --------------------------- circular ------------------------
        if(Math.abs(ecc) < small)
        {
            m = nu;
            e0 = nu;
        }
        else // ---------------------- elliptical -----------------------
        if(ecc < 1.0 - small)
        {
            sine = (Math.sqrt(1.0 - ecc * ecc) * Math.sin(nu)) / (1.0 + ecc * Math.cos(nu));
            cose = (ecc + Math.cos(nu)) / (1.0 + ecc * Math.cos(nu));
            e0 = Math.atan2(sine, cose);
            m = e0 - ecc * Math.sin(e0);
        }
        else // -------------------- hyperbolic  --------------------
        if(ecc > 1.0 + small)
        {
            if((ecc > 1.0) && (Math.abs(nu) + 0.00001 < Math.PI - Math.acos(1.0 / ecc)))
            {
                sine = (Math.sqrt(ecc * ecc - 1.0) * Math.sin(nu)) / (1.0 + ecc * Math.cos(nu));
                e0 = asinh(sine);
                m = ecc * Math.sinh(e0) - e0;
            }
        }
        else // ----------------- parabolic ---------------------
        if(Math.abs(nu) < 168.0 * Math.PI / 180.0)
        {
            e0 = Math.tan(nu * 0.5);
            m = e0 + (e0 * e0 * e0) / 3.0;
        }

        if(ecc < 1.0)
        {
            m = (m % (2.0 * Math.PI));
            if(m < 0.0)
            {
                m = m + 2.0 * Math.PI;
            }
            e0 = e0 % (2.0 * Math.PI);
        }

     return new double[] {e0, m};
   }  // end newtonnu


    /* -----------------------------------------------------------------------------
     *
     *                           function asinh
     *
     *  this function evaluates the inverse hyperbolic sine function.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    xval        - angle value                                  any real
     *
     *  outputs       :
     *    arcsinh     - result                                       any real
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    none.
     *
     * --------------------------------------------------------------------------- */
    public static double asinh(double xval)
    {
        return Math.log(xval + Math.sqrt(xval * xval + 1.0));
    }  // end asinh

    /* -----------------------------------------------------------------------------
     *
     *                           function mag
     *
     *  this procedure finds the magnitude of a vector.  the tolerance is set to
     *    0.000001, thus the 1.0e-12 for the squared test of underflows.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    vec         - vector
     *
     *  outputs       :
     *    vec         - answer stored in fourth component
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    none.
     * --------------------------------------------------------------------------- */
    public static double mag(double[] x)
    {
        return Math.sqrt(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
    }  // end mag

    /* -----------------------------------------------------------------------------
     *
     *                           procedure cross
     *
     *  this procedure crosses two vectors.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    vec1        - vector number 1
     *    vec2        - vector number 2
     *
     *  outputs       :
     *    outvec      - vector result of a x b
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    mag           magnitude of a vector
    ---------------------------------------------------------------------------- */
    public static void cross(double[] vec1, double[] vec2, double[] outvec)
    {
        outvec[0] = vec1[1] * vec2[2] - vec1[2] * vec2[1];
        outvec[1] = vec1[2] * vec2[0] - vec1[0] * vec2[2];
        outvec[2] = vec1[0] * vec2[1] - vec1[1] * vec2[0];
    }  // end cross

    /* -----------------------------------------------------------------------------
     *
     *                           function dot
     *
     *  this function finds the dot product of two vectors.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    vec1        - vector number 1
     *    vec2        - vector number 2
     *
     *  outputs       :
     *    dot         - result
     *
     *  locals        :
     *    none.
     *
     *  coupling      :
     *    none.
     *
     * --------------------------------------------------------------------------- */
    public static double dot(double[] x, double[] y)
    {
        return (x[0] * y[0] + x[1] * y[1] + x[2] * y[2]);
    }  // end dot

    /* -----------------------------------------------------------------------------
     *
     *                           procedure angle
     *
     *  this procedure calculates the angle between two vectors.  the output is
     *    set to 999999.1 to indicate an undefined value.  be sure to check for
     *    this at the output phase.
     *
     *  author        : david vallado                  719-573-2600    1 mar 2001
     *
     *  inputs          description                    range / units
     *    vec1        - vector number 1
     *    vec2        - vector number 2
     *
     *  outputs       :
     *    theta       - angle between the two vectors  -pi to pi
     *
     *  locals        :
     *    temp        - temporary real variable
     *
     *  coupling      :
     *    dot           dot product of two vectors
     * --------------------------------------------------------------------------- */
    public static double angle(double[] vec1, double[] vec2)
    {
        double small, undefined, magv1, magv2, temp;
        small = 0.00000001;
        undefined = 999999.1;

        magv1 = mag(vec1);
        magv2 = mag(vec2);

        if(magv1 * magv2 > small * small)
        {
            temp = dot(vec1, vec2) / (magv1 * magv2);
            if(Math.abs(temp) > 1.0)
            {
                temp = Math.signum(temp) * 1.0;
            }
            return Math.acos(temp);
        }
        else
        {
            return undefined;
        }
    }  // end angle





}
