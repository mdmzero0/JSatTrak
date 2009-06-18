// reads TLE file
package name.gano.astro.propogators.sgp4_cssi;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;


/**
 *
 * @author sgano
 */
public class SGP4io
{
    public static double pi = SGP4unit.pi;

    public static char OPSMODE_AFSPC = 'a';
    public static char OPSMODE_IMPROVED = 'i';

    /**
     * Reads the data from the TLE and initializes the SGP4 propogator variables and stores them in the SGP4unit.Gravconsttype object
     * DOES NOT PERFORM ANY INTERNAL CHECK BEYOND BASICS OF THE TLE DATA use other methods to do that first if desired.
     *
     * @param satName
     * @param line1  TLE line 1
     * @param line2  TLE line 2
     * @param opsmode 
     * @param whichconst which constants to use in propogation
     * @param satrec  object to store the SGP4 data
     * @return if the sgp4 propogator was initialized properly
     */
    public static boolean readTLEandIniSGP4(String satName, String line1, String line2, char opsmode, SGP4unit.Gravconsttype whichconst, SGP4SatData satrec)
    {
       final double deg2rad  =   pi / 180.0;         //   0.0174532925199433
       final double xpdotp   =  1440.0 / (2.0 *pi);  // 229.1831180523293

       double sec, mu, radiusearthkm, tumin, xke, j2, j3, j4, j3oj2;
       double startsec, stopsec, startdayofyr, stopdayofyr, jdstart, jdstop;
       int startyear, stopyear, startmon, stopmon, startday, stopday,
           starthr, stophr, startmin, stopmin;
       int cardnumb,  j; // numb,
       //long revnum = 0, elnum = 0;
       //char classification, intldesg[11], tmpstr[80];
       int year = 0;
       int mon, day, hr, minute;//, nexp, ibexp;

       double[] temp = SGP4unit.getgravconst( whichconst );
       tumin = temp[0];
       mu = temp[1];
       radiusearthkm = temp[2];
       xke = temp[3];
       j2 = temp[4];
       j3 = temp[5];
       j4 = temp[6];
       j3oj2 = temp[7];

       satrec.error = 0;

       satrec.name = satName;

       // SEG -- SAVE GRAVITY CONSTANT TO DATA
       satrec.gravconsttype = whichconst;

       // get variables from the two lines
       satrec.line1 = line1;
       try
       {
           readLine1(line1, satrec);
       }
       catch(Exception e)
       {
           System.out.println("Error Reading TLE line 1: " + e.toString());
            satrec.tleDataOk = false;
            satrec.error = 7;
            return false;
       }
//       sscanf(longstr1,"%2d %5ld %1c %10s %2d %12lf %11lf %7lf %2d %7lf %2d %2d %6ld ",
//                       &cardnumb,&satrec.satnum,&classification, intldesg, &satrec.epochyr,
//                       &satrec.epochdays,&satrec.ndot, &satrec.nddot, &nexp, &satrec.bstar,
//                       &ibexp, &numb, &elnum );
       satrec.line2 = line2;
       try
       {
            readLine2(line2, satrec);
       }
       catch(Exception e)
       {
            System.out.println("Error Reading TLE line 2: " + e.toString());
            satrec.tleDataOk = false;
            satrec.error = 7;
            return false;
       }
//       sscanf(longstr2,"%2d %5ld %9lf %9lf %8lf %9lf %9lf %11lf %6ld \n",
//                       &cardnumb,&satrec.satnum, &satrec.inclo,
//                       &satrec.nodeo,&satrec.ecco, &satrec.argpo, &satrec.mo, &satrec.no,
//                       &revnum );


       // ---- find no, ndot, nddot ----
       satrec.no   = satrec.no / xpdotp; //* rad/min
       satrec.nddot= satrec.nddot * Math.pow(10.0, satrec.nexp);
       satrec.bstar= satrec.bstar * Math.pow(10.0, satrec.ibexp);

       // ---- convert to sgp4 units ----
       satrec.a    = Math.pow( satrec.no*tumin , (-2.0/3.0) );
       satrec.ndot = satrec.ndot  / (xpdotp*1440.0);  //* ? * minperday
       satrec.nddot= satrec.nddot / (xpdotp*1440.0*1440);

       // ---- find standard orbital elements ----
       satrec.inclo = satrec.inclo  * deg2rad;
       satrec.nodeo = satrec.nodeo  * deg2rad;
       satrec.argpo = satrec.argpo  * deg2rad;
       satrec.mo    = satrec.mo     * deg2rad;

       satrec.alta = satrec.a*(1.0 + satrec.ecco) - 1.0;
       satrec.altp = satrec.a*(1.0 - satrec.ecco) - 1.0;

       // ----------------------------------------------------------------
       // find sgp4epoch time of element set
       // remember that sgp4 uses units of days from 0 jan 1950 (sgp4epoch)
       // and minutes from the epoch (time)
       // ----------------------------------------------------------------

       // ---------------- temp fix for years from 1957-2056 -------------------
       // --------- correct fix will occur when year is 4-digit in tle ---------
       if (satrec.epochyr < 57)
           year= satrec.epochyr + 2000;
         else
           year= satrec.epochyr + 1900;

       // computes the m/d/hr/min/sec from year and epoch days
       MDHMS mdhms = days2mdhms ( year,satrec.epochdays );
       mon = mdhms.mon;
       day = mdhms.day;
       hr = mdhms.hr;
       minute = mdhms.minute;
       sec = mdhms.sec;
       // computes the jd from  m/d/...
       satrec.jdsatepoch = jday( year,mon,day,hr,minute,sec );

       // ---------------- initialize the orbit at sgp4epoch -------------------
       boolean result = SGP4unit.sgp4init( whichconst, opsmode, satrec.satnum,
                 satrec.jdsatepoch-2433281.5, satrec.bstar,
                 satrec.ecco, satrec.argpo, satrec.inclo, satrec.mo, satrec.no,
                 satrec.nodeo, satrec);

       return result;

    } // readTLEandIniSGP4


    private static boolean readLine1(String line1, SGP4SatData satrec) throws Exception
    {
        String tleLine1 = line1; // first line
        if(!tleLine1.startsWith("1 "))
        {
            throw new Exception("TLE line 1 not valid first line");
        }

        // satnum
        satrec.satnum = (int)readFloatFromString(tleLine1.substring(2,7));
        // classification
        satrec.classification = tleLine1.substring(7, 8); // 1 char
        // intln designator
        satrec.intldesg = tleLine1.substring(9, 17); // should be within 8

        // epochyr
        satrec.epochyr = (int) readFloatFromString(tleLine1.substring(18, 20));

        // epoch days
        satrec.epochdays = readFloatFromString(tleLine1.substring(20, 32));

        // ndot
        satrec.ndot = readFloatFromString(tleLine1.substring(33,43));

        // nddot
        //nexp
        if((tleLine1.substring(44, 52)).equals("        "))
        {
            satrec.nddot = 0;
            satrec.nexp = 0;
        }
        else
        {
            satrec.nddot = readFloatFromString(tleLine1.substring(44,50)) / 1.0E5;
            //nexp
            satrec.nexp = (int)readFloatFromString(tleLine1.substring(50,52));
        }
        //bstar
        satrec.bstar = readFloatFromString(tleLine1.substring(53,59)) / 1.0E5;
        //ibex
        satrec.ibexp = (int)readFloatFromString(tleLine1.substring(59,61));

        // num b.
        satrec.numb = (int)readFloatFromString(tleLine1.substring(62,63));

        //  elnum // check sum
        satrec.elnum = (long) readFloatFromString(tleLine1.substring(64));

        // if no errors yet everything went ok
        return true;
    } // readLine1

    private static boolean readLine2(String line2, SGP4SatData satrec) throws Exception
    {
        /* Read the second line of elements. */

        //theLine = aFile.readLine();
        String tleLine2 = line2; // second line
        if(!tleLine2.startsWith("2 "))
        {
            throw new Exception("TLE line 2 not valid second line");
        }

        // satnum
        int satnum = (int)readFloatFromString(tleLine2.substring(2,7));
        if(satnum != satrec.satnum)
        {
            System.out.println("Warning TLE line 2 Sat Num doesn't match line1 for sat: " + satrec.name);
        }

        // inclination
        satrec.inclo = readFloatFromString(tleLine2.substring(8,17));

        // nodeo
        satrec.nodeo = readFloatFromString(tleLine2.substring(17, 26));

        //satrec.ecco
        satrec.ecco = readFloatFromString(tleLine2.substring(26,34)) / 1.0E7;

        // satrec.argpo
        satrec.argpo = readFloatFromString(tleLine2.substring(34,43));

        // satrec.mo
        satrec.mo = readFloatFromString(tleLine2.substring(43,52));

        // no
        satrec.no = readFloatFromString(tleLine2.substring(52, 63));
        
        // revnum
        satrec.revnum = (long)readFloatFromString(tleLine2.substring(63,69));

        return true;
    } // readLine1

    /**
     * Read float data from a string
     * @param inStr
     * @return
     * @throws Exception 
     */
    protected static double readFloatFromString(String inStr) throws Exception
    {
        // make sure decimal sparator is '.' so it works in other countries
        // because of this can't use Double.parse
        DecimalFormat dformat = new DecimalFormat("#");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        dformat.setDecimalFormatSymbols(dfs);

        // trim white space and if there is a + at the start
        String trimStr = inStr.trim();
        if(trimStr.startsWith("+"))
        {
            trimStr = trimStr.substring(1);
        }

        // parse until we hit the end or invalid char
        ParsePosition pp = new ParsePosition(0);
        Number num = dformat.parse(trimStr, pp);
        if(null == num)
        {
            throw new Exception("Invalid Float In TLE");
        }

        return num.doubleValue();
    }

    /* -----------------------------------------------------------------------------
*
*                           procedure jday
*
*  this procedure finds the julian date given the year, month, day, and time.
*    the julian date is defined by each elapsed day since noon, jan 1, 4713 bc.
*
*  algorithm     : calculate the answer in one step for efficiency
*
*  author        : david vallado                  719-573-2600    1 mar 2001
*
*  inputs          description                    range / units
*    year        - year                           1900 .. 2100
*    mon         - month                          1 .. 12
*    day         - day                            1 .. 28,29,30,31
*    hr          - universal time hour            0 .. 23
*    min         - universal time min             0 .. 59
*    sec         - universal time sec             0.0 .. 59.999
*
*  outputs       :
*    jd          - julian date                    days from 4713 bc
*
*  locals        :
*    none.
*
*  coupling      :
*    none.
*
*  references    :
*    vallado       2007, 189, alg 14, ex 3-14
*
* --------------------------------------------------------------------------- */

private static double jday
        (
          int year, int mon, int day, int hr, int minute, double sec//,
          //double& jd
        )
   {
    double jd;
     jd = 367.0 * year -
          Math.floor((7 * (year + Math.floor((mon + 9) / 12.0))) * 0.25) +
          Math.floor( 275 * mon / 9.0 ) +
          day + 1721013.5 +
          ((sec / 60.0 + minute) / 60.0 + hr) / 24.0;  // ut in days
          // - 0.5*sgn(100.0*year + mon - 190002.5) + 0.5;

     return jd;
   }  // end jday

/* -----------------------------------------------------------------------------
*
*                           procedure days2mdhms
*
*  this procedure converts the day of the year, days, to the equivalent month
*    day, hour, minute and second.
*
*
*
*  algorithm     : set up array for the number of days per month
*                  find leap year - use 1900 because 2000 is a leap year
*                  loop through a temp value while the value is < the days
*                  perform int conversions to the correct day and month
*                  convert remainder into h m s using type conversions
*
*  author        : david vallado                  719-573-2600    1 mar 2001
*
*  inputs          description                    range / units
*    year        - year                           1900 .. 2100
*    days        - julian day of the year         0.0  .. 366.0
*
*  outputs       :
*    mon         - month                          1 .. 12
*    day         - day                            1 .. 28,29,30,31
*    hr          - hour                           0 .. 23
*    min         - minute                         0 .. 59
*    sec         - second                         0.0 .. 59.999
*
*  locals        :
*    dayofyr     - day of year
*    temp        - temporary extended values
*    inttemp     - temporary int value
*    i           - index
*    lmonth[12]  - int array containing the number of days per month
*
*  coupling      :
*    none.
* --------------------------------------------------------------------------- */
// returns MDHMS object with the mdhms variables
private static MDHMS days2mdhms
        (
          int year, double days//,
          //int& mon, int& day, int& hr, int& minute, double& sec
        )
   {
    // return variables
    //int mon, day, hr, minute, sec
    MDHMS mdhms = new MDHMS();

     int i, inttemp, dayofyr;
     double    temp;
     int lmonth[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

     dayofyr = (int)Math.floor(days);
     /* ----------------- find month and day of month ---------------- */
     if ( (year % 4) == 0 ) // doesn't work for dates starting 2100 and beyond
     {
       lmonth[1] = 29;
     }

     i = 1;
     inttemp = 0;
     while ((dayofyr > inttemp + lmonth[i-1]) && (i < 12))
     {
       inttemp = inttemp + lmonth[i-1];
       i++;
     }
     mdhms.mon = i;
     mdhms.day = dayofyr - inttemp;

     /* ----------------- find hours minutes and seconds ------------- */
     temp = (days - dayofyr) * 24.0;
     mdhms.hr   = (int)Math.floor(temp);
     temp = (temp - mdhms.hr) * 60.0;
     mdhms.minute  = (int)Math.floor(temp);
     mdhms.sec  = (temp - mdhms.minute) * 60.0;

     return mdhms;
   }  // end days2mdhms

    // Month Day Hours Min Sec
    private static class MDHMS
    {
        int mon = 0;;
        int day = 0;;
        int hr = 0;;
        int minute = 0;;
        double sec = 0;
    }

}
