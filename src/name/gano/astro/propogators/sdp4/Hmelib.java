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
 * $Id: Hmelib.java,v 4.1 2004/08/09 07:54:20 hme Exp $
 *
 * $Log: Hmelib.java,v $
 * Revision 4.1  2004/08/09 07:54:20  hme
 * Version 2.1.1.
 *
 * Revision 3.1  2004/07/28 10:48:53  hme
 * Version 2.1.
 *
 * Revision 2.7  2004/02/03 23:52:44  hme
 * Added WTime6.
 *
 * Revision 2.6  2004/02/02 23:15:24  hme
 * Added WTime5.
 *
 * Revision 2.5  2003/09/18 18:26:25  hme
 * Fix bug in WTime4 whereby it would always write the minute with on
 * decimal (which was always .0 as it should be).
 *
 * Revision 2.4  2003/09/16 14:53:52  hme
 * Package review.  Renamed from SputnikLib to Hmelib.
 *
 * Revision 2.2  2003/09/15 20:00:51  hme
 * Change date format from Y/M/D- to Y-M-D-.
 *
 * Revision 1.16  2002/07/16 11:35:44  hme
 * RTime3 now throws an exception if the year or month are not integer.
 * Before it would return their integer value instead.
 * Also improved handling of mantissa/exponent numbers in Rfndm, in
 * particular less chance of a crash due to invalid input.
 *
 * Revision 1.14  2002/07/14 09:20:22  hme
 * Consolidating documentation.  Also enhance Rrndm() to read
 * mantissa/exponent numbers.
 *
 * Revision 1.12  2002/07/07 14:27:19  hme
 * Add support for precision 1 to Wfndm.
 *
 * Revision 1.11  2002/06/23 17:42:01  hme
 * Add NormAngle0() and NormAngle180().
 *
 * Revision 1.10  2002/06/22 15:01:23  hme
 * Add SpherAng().
 *
 * Revision 1.9  2002/06/22 09:58:35  hme
 * Add Rect().
 *
 * Revision 1.8  2002/06/17 22:02:34  hme
 * Add Wdms() and Wfexp().
 *
 * Revision 1.7  2002/06/15 14:49:10  hme
 * Add support for precision 0 to Wfndm.  Add Rstring() and Sstring().
 * Add Spher().
 *
 * Revision 1.3  2002/06/12 20:42:09  hme
 * Translated from C++ after Times class was finalised in the prospective
 * version 2 of Sputnik.
 *
 * Revision 1.1  2002/06/09 09:49:20  hme
 * Initial revision
 *
 *-*/

package name.gano.astro.propogators.sdp4;

import java.io.*;
import java.text.*;

/**
 * <p><code>Hmelib</code> is a loose collection of methods for all sorts of
 * general little tasks, such as mathematics, string stuff, line i/o etc.  It
 * also collects some constants we need in a variety of classes, similar to
 * Java's <code>Math.E</code> and <code>Math.PI</code>, although this would
 * normally be the task of the common base class of the classes that need a
 * constant.
 *
 * <p>Copyright: &copy; 2002-2004 Horst Meyerdierks.
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
 * <p>$Id: Hmelib.java,v 4.1 2004/08/09 07:54:20 hme Exp $
 *
 * <dl>
 * <dt><strong>2.7:</strong> 2004/02/03 hme
 * <dd>Added WTime6.
 * <dt><strong>2.6:</strong> 2004/02/02 hme
 * <dd>Added WTime5.
 * <dt><strong>2.5:</strong> 2003/09/18 hme
 * <dd>Fix bug in WTime4 whereby it would always write the minute with on
 * decimal (which was always .0 as it should be).
 * <dt><strong>2.4:</strong> 2003/09/16 hme
 * <dd>Package review.
 * Renamed from SputnikLib to Hmelib.
 * <dt><strong>2.2:</strong> 2003/09/15 hme
 * <dd>Change date format from Y/M/D- to Y-M-D-.
 * <dt><strong>1.16:</strong> 2002/07/16 hme
 * <dd>RTime3 now throws an exception if the year or month are not integer.
 *   Before it would return their integer value instead.
 *   Also improved handling of mantissa/exponent numbers in Rfndm, in
 *   particular less chance of a crash due to invalid input.
 * <dt><strong>1.14:</strong> 2002/07/14 hme
 * <dd>Consolidating documentation.  Also enhance Rrndm() to read
 *   mantissa/exponent numbers.
 * <dt><strong>1.13:</strong> 2002/07/13 hme
 * <dd>Fix format for Rfndm so that commas are not taken as part of a number
 *   but as separators.  Add SpherDist().
 * <dt><strong>1.12:</strong> 2002/07/07 hme
 * <dd>Add support for precision 1 to Wfndm.
 * <dt><strong>1.11:</strong> 2002/06/23 hme
 * <dd>Add NormAngle0() and NormAngle180().
 * <dt><strong>1.10:</strong> 2002/06/22 hme
 * <dd>Add SpherAng().
 * <dt><strong>1.9:</strong> 2002/06/22 hme
 * <dd>Add Rect().
 * <dt><strong>1.8:</strong> 2002/06/17 hme
 * <dd>Add Wdms() and Wfexp().
 * <dt><strong>1.7:</strong> 2002/06/15 hme
 * <dd>Add support for precision 0 to Wfndm.  Add Rstring() and Sstring().
 *   Add Spher().
 * <dt><strong>1.3:</strong> 2002/06/09 hme
 * <dd>Translated from C++ after Times class was finalised in the prospective
 *   version 2 of Sputnik.
 * </dl>
 *
 * @author
 *   Horst Meyerdierks, c/o Royal Observatory,
 *   Blackford Hill, Edinburgh, EH9 3HJ, Scotland;
 *   &lt; hme &#64; roe.ac.uk &gt; */

public final class Hmelib
{

  /** Degrees per radian. */

  public static final double DEGPERRAD = 180. / Math.PI;


  /** Blank string used in right-justifying numeric fields. */

  final static private String blank = "                                ";


  /** A %02.0f format for formatting times and sexagesimal angles. */

  final static private DecimalFormat zi2 = new DecimalFormat("00");


  /** A %04.0f format for formatting the year in dates. */

  final static private DecimalFormat zi4 = new DecimalFormat("0000");


  /** A %04.1f format for formatting the last sexagesimal component. */

  final static private DecimalFormat zf4d1 = new DecimalFormat("00.0");


  /** A %n.0f format for formatting integer output. */

  final static private DecimalFormat f1d0 = new DecimalFormat("0");


  /** A %n.1f format for formatting with one decimal digit. */

  final static private DecimalFormat f1d1 = new DecimalFormat("0.0");


  /** A %n.3f format for formatting with three decimal digits. */

  final static private DecimalFormat f1d3 = new DecimalFormat("0.000");


  /** A %n.6f format for formatting with six decimal digits. */

  final static private DecimalFormat f1d6 = new DecimalFormat("0.000000");


  /** A %n.9f format for formatting with nine decimal digits. */

  final static private DecimalFormat f1d9 = new DecimalFormat("0.000000000");


  /** A %n.3g format for formatting with three decimal digits and exponent. */

  final static private DecimalFormat fexp = new DecimalFormat("0.000E00");


  /**
   * A %f format for parsing a floating point number.
   *
   * <p>Specifying a hash means that a comma (,) is not recognised as
   * throusand separator but taken to be the end of the numeric field.
   * Note that an "E" is also taken to be the end of the numeric field, so
   * 123E4 cannot be read properly. */

  final static private DecimalFormat form  = new DecimalFormat("#");


  /**
   * Convert a number into a sexagesimal triplet.
   *
   * <p>The given number (e.g. an angle in degrees or a time in hours) is split
   * into a triplet of numbers (degrees, arcmin, arcsec or hours, minutes,
   * seconds).
   *
   * @param aTime
   *   Time or angle in hours or degrees.
   * @param aTriplet
   *   The triplet of hours, minutes, seconds or &deg;, ', ". */

  static public void deg2dms(double aTime, double aTriplet[])
  {
    double theT;

    theT = aTime;
    if (0. > aTime) {
      aTriplet[0] = Math.ceil(theT); theT -= aTriplet[0]; theT *= 60.;
      aTriplet[1] = Math.ceil(theT); theT -= aTriplet[1]; theT *= 60.;
      aTriplet[2] = theT;
    }
    else{
      aTriplet[0] = Math.floor(theT); theT -= aTriplet[0]; theT *= 60.;
      aTriplet[1] = Math.floor(theT); theT -= aTriplet[1]; theT *= 60.;
      aTriplet[2] = theT;
    }
  }


  /**
   * Normalise an angle around zero.
   *
   * <p>This normalises an angle in case it is outside the interval
   * [-&pi;,+&pi;].  The returned value represents the same angle but is
   * within this interval.
   *
   * @param aAngle
   *   The angle before normalisation. */

  static public double NormAngle0(double aAngle)
  {
    double theAngle;
    theAngle = aAngle;
    while (theAngle <= -Math.PI) theAngle += 2. * Math.PI;
    while (theAngle  >  Math.PI) theAngle -= 2. * Math.PI;
    return theAngle;
  }


  /**
   * Normalise an angle around 180&#176;.
   *
   * <p>This normalises an angle in case it is outside the interval
   * [0,2&pi;].  The returned value represents the same angle but is within
   * this interval.
   *
   * @param aAngle
   *   The angle before normalisation. */

  static public double NormAngle180(double aAngle)
  {
    double theAngle;
    theAngle = NormAngle0(aAngle);
    if (0. > theAngle) theAngle += 2. * Math.PI;
    return theAngle;
  }


  /**
   * Convert spherical to orthogonal coordinates.
   *
   * <p>Take the given a,b,r coordinates and convert them to x,y,z where a is
   * the azimuth angle in the x-y plane, b the elevation over the x-y plane,
   * and r the distance from the origin.  a and b are in radian, r in the same
   * units as x, y and z.
   *
   * @param aSpher
   *   Triplet of spherical coordinates a, b, r.
   * @param aRect
   *   Triplet of rectangular coordinates x, y, z. */

  static public void Rect(double aSpher[], double aRect[])
  {
    aRect[0] = aSpher[2] * Math.cos(aSpher[0]) * Math.cos(aSpher[1]);
    aRect[1] = aSpher[2] * Math.sin(aSpher[0]) * Math.cos(aSpher[1]);
    aRect[2] = aSpher[2] * Math.sin(aSpher[1]);
  }


  /**
   * Read a floating point number.
   *
   * <p>Given a string this trims off any leading and trailing white space and
   * reads a floating point number from the start of the string.  If the
   * parsing fails an exception is thrown.  The given
   * string can be a plain number like <code>1234.5678</code> or one that is
   * split into mantissa and exponent like <code>1.2345678E3</code>.  Mantissa
   * or exponent can have a minus sign or indeed a plus sign in front of them.
   * (This would be a trivial statement if this were written in C, but in Java
   * this takes some coding to achieve.)
   *
   * @param aString
   *   The string to read from. */

  static public double Rfndm(String aString)
    throws HmelibInvalidNumException
  {
    ParsePosition where = new ParsePosition(0);
    String theString;
    Number theNumber;
    double m = 0., e = 0.;
    int    i;

    /* Trim off white space and a leading plus sign. */

    theString = aString.trim();
    if (theString.startsWith("+")) theString = theString.substring(1);

    /* Parse the number up to the next invalid character or end of string.
     * Valid characters would be -.0123456789 or something like that. */

    theNumber = form.parse(theString, where);
    if (null == theNumber)
      throw new HmelibInvalidNumException("invalid number");
    m = theNumber.doubleValue();

    /* Check if the next character is an e or an E, in which case we have a
     * decimal magnitude behind it.  Read it and make use of it. */

    i = where.getIndex();
    if (i >= theString.length()) {
      return m;
    }
    else if ('e' == theString.charAt(i) ||
             'E' == theString.charAt(i)) {
      i++;
      if (i >= theString.length()) {
        return m;
      }
      else {
        if ('+' == theString.charAt(i)) i++;
        where.setIndex(i);
	theNumber = form.parse(theString, where);
	if (null == theNumber) {
	    return m;
	}
	else {
	    e = theNumber.doubleValue();
	    return m * Math.pow(10., e);
	}
      }
    }
    else {
      return m;
    }
  }


  /**
   * Read a string.
   *
   * <p>Given a string this trims off any leading and trailing white space and
   * reads a string from the start.  The string read is usually a single word.
   * But if the string begins with a double quote (") then the string read is
   * all words between this and the matching closing quote.  The quotes are
   * sripped off.  This routine will not recognise escaped quotes as part of
   * the string, but as end of the string.
   *
   * @param aString
   *   The string to read from. */

  static public String Rstring(String aString)
  {
    String theString;
    int    theIndex;

    /* Trim white space. */

    theString = aString.trim();

    /* If quoted string. */

    if (theString.startsWith("\"")) {

      /* Strip opening quote, find closing one. */

      theString = theString.substring(1);
      theIndex  = theString.indexOf('\"');

      /* If empty string. */

      if (0 == theIndex) {
	theString = "";
      }

      /* Else if genuine string. */

      else if (0 < theIndex) {
	theString = theString.substring(0, theIndex);
      }

      /* Else (no closing quote), do nothing, the String is fine. */

    }

    /* Else (unquoted string). */

    else {

      /* Find space character. */

      theIndex  = theString.indexOf(' ');

      /* If there is one. */

      if (0 < theIndex) {
	theString = theString.substring(0, theIndex);
      }

      /* Else (no space character), do nothing, the String is fine. */
    }
    return theString;
  }


  /**
   * Read date as year, month and day, time as hours, minutes and seconds.
   *
   * <p>Given a string of the form <code>2002-12-13-12:34:56.7</code>, this
   * routine returns the six numbers.  The third number in the string can also
   * be floating point.  In that case its fractional part is added to the hours
   * in order that all three date numbers can be returned as integers.  All
   * numbers can be negative (inlcuding the hours, which are given behind a -
   * character).  Each negative sign affects only the one number.  The given
   * string will be trimmed for leading and trailing blanks before reading.
   * But the separators between the numbers must be precisely as shown,
   * i.e. single characters -,-,-,:,:.  If this is not the case an
   * exception is thrown.
   *
   * @param aString
   *   The string to read from.
   * @param date
   *   The triplet of integers that are year, month and day.
   * @param time
   *   The triplet of floating point numbers that are hours, minutes and
   *   seconds. */

  final static public void RTime3(String aString, int date[], double time[])
    throws HmelibInvalidDateException
  {
    ParsePosition where = new ParsePosition(0);
    double theDayd, theDayi;
    String theString;
    Number theNumber;

    theString = aString.trim();

    theNumber = form.parse(theString, where);
    if (null == theNumber)
      throw new HmelibInvalidDateException("invalid date");
    date[0] = theNumber.intValue();
    if (date[0] != theNumber.doubleValue())
      throw new HmelibInvalidDateException("invalid date");
    if (!(theString.substring(where.getIndex())).startsWith("-"))
      throw new HmelibInvalidDateException("invalid date");

    where.setIndex(1 + where.getIndex());
    theNumber = form.parse(theString, where);
    if (null == theNumber)
      throw new HmelibInvalidDateException("invalid date");
    date[1] = theNumber.intValue();
    if (date[1] != theNumber.doubleValue())
      throw new HmelibInvalidDateException("invalid date");
    if (!(theString.substring(where.getIndex())).startsWith("-"))
      throw new HmelibInvalidDateException("invalid date");

    where.setIndex(1 + where.getIndex());
    theNumber = form.parse(theString, where);
    if (null == theNumber)
      throw new HmelibInvalidDateException("invalid date");
    theDayd = theNumber.doubleValue();
    if (!(theString.substring(where.getIndex())).startsWith("-"))
      throw new HmelibInvalidDateException("invalid date");

    where.setIndex(1 + where.getIndex());
    theNumber = form.parse(theString, where);
    if (null == theNumber)
      throw new HmelibInvalidDateException("invalid date");
    time[0] = theNumber.doubleValue();
    if (!(theString.substring(where.getIndex())).startsWith(":"))
      throw new HmelibInvalidDateException("invalid date");

    where.setIndex(1 + where.getIndex());
    theNumber = form.parse(theString, where);
    if (null == theNumber)
      throw new HmelibInvalidDateException("invalid date");
    time[1] = theNumber.doubleValue();
    if (!(theString.substring(where.getIndex())).startsWith(":"))
      throw new HmelibInvalidDateException("invalid date");

    where.setIndex(1 + where.getIndex());
    theNumber = form.parse(theString, where);
    if (null == theNumber)
      throw new HmelibInvalidDateException("invalid date");
    time[2] = theNumber.doubleValue();

    theDayi  = Math.floor(theDayd);
    theDayd -= theDayi; date[2] = (int)theDayi;
    time[0] += 24. * theDayd;
  }


  /**
   * Convert orthogonal to spherical coordinates.
   *
   * <p>Take the given x,y,z coordinates and convert them to a,b,r where a is
   * the azimuth angle in the x-y plane, b the elevation over the x-y plane,
   * and r the distance from the origin.  a and b are in radian, r in the same
   * units as x, y and z.
   *
   * @param aRect
   *   Triplet of rectangular coordinates x, y, z.
   * @param aSpher
   *   Triplet of spherical coordinates a, b, r. */

  static public void Spher(double aRect[], double aSpher[])
  {
    double xx, yy, zz;

    if (aRect[0] == 0. && aRect[1] == 0. && aRect[2] == 0.) {
      aSpher[0] = 0; aSpher[1] = 0; aSpher[2] = 0; return;
    }

    aSpher[2] = Math.sqrt(aRect[0] * aRect[0] + aRect[1] * aRect[1]
		        + aRect[2] * aRect[2]);

    xx = aRect[0] / aSpher[2];
    yy = aRect[1] / aSpher[2];
    zz = aRect[2] / aSpher[2];
    aSpher[1] = Math.asin(zz);
    aSpher[0] = Math.atan2(yy, xx);
    if (aSpher[0] < 0.) aSpher[0] += 2. * Math.PI;

    return;
  }


  /**
   * Calculate a spherical angle.
   *
   * <p>The three given xyz triplets define a spherical triangle on the unit
   * sphere.  The returned value is the angle at the second corner.  To
   * calculate a postition angle, give the North Pole {0,0,1} as the first
   * position, the bright component or reference positions as second and the
   * faint component as third.  To calculate the parallactic angle give the
   * zenith {0,0,1} as first, the position of interest Rect({A,h,1}) as
   * second and the North Celestial Pole
   * Rect({0,&phi;,1}) as third.
   *
   * @param Triplets
   *   Nine numbers x1, y1, z1, x2, y2, z2, x3, y3, z3.  Each group of three
   *   is a vector that defines one corner of the spherical triangle. */

  static public double SpherAng(double Triplets[])
  {
    double mat[] = new double[9];
    double x1, x2, x3, y1, y2, y3, z1, z2, z3, t1;
    int    i;

    /* Get unit vectors. */

    t1 = Math.sqrt(Triplets[0] * Triplets[0]
                 + Triplets[1] * Triplets[1] + Triplets[2] * Triplets[2]);
    x1 = Triplets[0] / t1; y1 = Triplets[1] / t1; z1 = Triplets[2] / t1;
    t1 = Math.sqrt(Triplets[3] * Triplets[3]
                 + Triplets[4] * Triplets[4] + Triplets[5] * Triplets[5]);
    x2 = Triplets[3] / t1; y2 = Triplets[4] / t1; z2 = Triplets[5] / t1;
    t1 = Math.sqrt(Triplets[6] * Triplets[6]
                 + Triplets[7] * Triplets[7] + Triplets[8] * Triplets[8]);
    x3 = Triplets[6] / t1; y3 = Triplets[7] / t1; z3 = Triplets[8] / t1;

    /* New z axis to point behind our head. */

    mat[6] = -x2;
    mat[7] = -y2;
    mat[8] = -z2;

    /* New x axis is projection of north pole vertical to line of sight. */

    t1 = x1 * x2 + y1 * y2 + z1 * z2;
    mat[0] = x1 - t1 * x2;
    mat[1] = y1 - t1 * y2;
    mat[2] = z1 - t1 * z2;
    t1 = Math.sqrt(mat[0] * mat[0] + mat[1] * mat[1] + mat[2] * mat[2]);
    for (i = 0; i < 3; i++) mat[i] /= t1;

    /* New y axis is z cross x. */

    mat[3] = mat[7] * mat[2] - mat[1] * mat[8];
    mat[4] = mat[0] * mat[8] - mat[6] * mat[2];
    mat[5] = mat[6] * mat[1] - mat[0] * mat[7];

    /* Transform the third position with the matrix defined by the first two.
     * Then the position angle is the longitude in the transformed system. */

    x1 = mat[0] * x3 + mat[1] * y3 + mat[2] * z3;
    y1 = mat[3] * x3 + mat[4] * y3 + mat[5] * z3;
    z1 = mat[6] * x3 + mat[7] * y3 + mat[8] * z3;

    x3 = Math.atan2(y1, x1);
    if (x3 < 0.) x3 += 2. * Math.PI;

    return x3;
  }


  /**
   * Calculate a spherical distance.
   *
   * <p>The given xyz triplets define two directions.  The returned value is
   * the angle between the two vectors.  In the first instance the distance is
   * calculated as the arccos of the dot product of the two unit vectors.  If
   * this results in a distance less than ~1&#176; or more than ~179&#176;,
   * then the distance is recalculated as the arcsin of the cross product.
   *
   * @param aTriplet1
   *   The first vector.
   * @param aTriplet2
   *   The second vector. */

  static public double SpherDist(double aTriplet1[], double aTriplet2[])
  {
    double ax, ay, az, bx, by, bz, ra, rb;
    double dot, cross, cx, cy, cz;

    /* Unit vectors. */

    ax = aTriplet1[0]; ay = aTriplet1[1]; az = aTriplet1[2];
    bx = aTriplet2[0]; by = aTriplet2[1]; bz = aTriplet2[2];
    ra = Math.sqrt(ax * ax + ay * ay + az * az);
    rb = Math.sqrt(bx * bx + by * by + bz * bz);
    ax /= ra; ay /= ra; az /= ra;
    bx /= rb; by /= rb; bz /= rb;

    /* Dot product. */

    dot = ax * bx + ay * by + az * bz;
    if (0.9998 > Math.abs(dot)) {
      return Math.acos(dot);
    }
    else if (0. < dot) {
      cx = ay * bz - az * by;
      cy = az * bx - ax * bz;
      cz = ax * by - ay * bx;
      cross = Math.sqrt(cx * cx + cy * cy + cz * cz);
      return Math.asin(cross);
    }
    else {
      cx = ay * bz - az * by;
      cy = az * bx - ax * bz;
      cz = ax * by - ay * bx;
      cross = Math.sqrt(cx * cx + cy * cy + cz * cz);
      return Math.PI - Math.asin(cross);
    }
  }


  /**
   * Skip a string.
   *
   * <p>Given a string this behaves like {@link #Rstring Rstring},
   * but returns the remainder of the given string instead of the string read.
   *
   * @param aString
   *   The string to read from. */

  static public String Sstring(String aString)
  {
    String theString;
    int    theIndex;

    /* Trim white space. */

    theString = aString.trim();

    /* If quoted string. */

    if (theString.startsWith("\"")) {

      /* Strip opening quote, find closing one. */

      theString = theString.substring(1);
      theIndex  = theString.indexOf('\"');

      /* If no closing quote, the remainder string is empty. */

      if (-1 == theIndex) {
	theString = "";
      }

      /* Else, the remainder starts after it. */

      else {
	theString = theString.substring(theIndex + 1);
      }
    }

    /* Else (unquoted string). */

    else {

      /* Find space character. */

      theIndex  = theString.indexOf(' ');

      /* If there is none, the remainder is empty. */

      if (-1 == theIndex) {
	theString = "";
      }

      /* Else, the remainder starts with it. */

      else {
	theString = theString.substring(theIndex);
      }
    }
    return theString;
  }


  /**
   * Write angle as degrees, arc minutes and arc seconds.
   *
   * <p>Format the given triplet of in the form <code>"&nbsp;12:34:56"</code>
   * or <code>"-12:34:56"</code>.  Note the leading blank for positive angles.
   *
   * @param stream
   *   The stream to write to.
   * @param degree
   *   The angle in degrees. */

  final static public void Wdms(PrintStream stream, double degree)
  {
    double theDMS[] = new double[3];

    deg2dms(Math.abs(degree), theDMS);
    if (0. > degree) {stream.print("-");}
    else             {stream.print(" ");}

    theDMS[2] = Math.floor(0.5 + theDMS[2]);
    if (60. == theDMS[2]) {
      theDMS[2] -= 60.; theDMS[1] += 1.;
      if (60. == theDMS[1]) {theDMS[1] -= 60.; theDMS[0] += 1.;}
    }
    stream.print(zi2.format(theDMS[0]) + ":"
               + zi2.format(theDMS[1]) + ":"
               + zi2.format(theDMS[2]));
  }


  /**
   * Write a number in <code>1.234E12</code> format.
   *
   * <p>Format the given floating point number into a string of lenth 8 with
   * 4 significant digits, 3 of them behind the decimal dot.
   *
   * @param aStream
   *   The stream to write to.
   * @param aNum
   *   The number to format and write. */

  final static public void Wfexp(PrintStream aStream, double aNum)
  {
    aStream.print(fexp.format(aNum));
  }


  /**
   * Write a number to a fixed-width field.
   *
   * <p>Format the given floating point number into a string of fixed lenth
   * with the number on its right (blank-padded on the left).  The given field
   * width can have any positive integer value.  If it is too small to fit the
   * number a longer string is generated.  The precision can currently be given
   * as 0, 1, 3, 6 or 9 digits behind the decimal point.  If another precision
   * is given, 3 is used anyway.  As an example the number
   * <code>-1234.567</code> with a width of 14 and a precision of 6 should come
   * out as <code>"&nbsp;&nbsp;-1234.567000"</code>.
   *
   * @param aStream
   *   The stream to write to.
   * @param aWidth
   *   The number of characters to write.
   * @param aPrec
   *   The number of digits behind the decimal point.
   * @param aNum
   *   The number to format and write. */

  final static public void Wfndm(PrintStream aStream, int aWidth, int aPrec,
    double aNum)
  {
    String theOut;
    int theLen;

    /* Format the number into a string, which necessarily is left justified. */

    switch(aPrec) {
    case 9:  theOut = f1d9.format(aNum); break;
    case 6:  theOut = f1d6.format(aNum); break;
    case 3:  theOut = f1d3.format(aNum); break;
    case 1:  theOut = f1d1.format(aNum); break;
    case 0:  theOut = f1d0.format(aNum); break;
    default: theOut = f1d3.format(aNum); break;
    }

    /* If the string is narrower than the requested field length, print a few
     * space characters and then the number.  Else print the number. */

    theLen = theOut.length();
    if (theLen < aWidth)
      aStream.print(blank.substring(theLen,aWidth) + theOut);
    else
      aStream.print(theOut);
  }


  /**
   * Write time as hours and minutes.
   *
   * <p>Round and format the given triplet of hour, minute and second into
   * hours and minutes of the form <code>12:35</code> where the second number
   * takes into account the third.  The rounding is also modified to prevent
   * the second number to reach the value 60 on output.
   *
   * @param stream
   *   The stream to write to.
   * @param hour
   *   First sexagesimal unit, hour or &#176;.
   * @param min
   *   Second sexagesimal unit, minute or '.
   * @param sec
   *   Third sexagesimal unit, second or ". */

  final static public void WTime1(PrintStream stream,
    double hour, double min, double sec)
  {
    double h, m;

    h = hour; m = Math.floor(0.5 + min + sec / 60.);
    if (60. == m) {m -= 60.; h += 1.;}
    stream.print(zi2.format(h) + ":" + zi2.format(m));
  }


  /**
   * Write time as hours, minutes and seconds.
   *
   * <p>Format the given triplet of hour, minute and second in the form
   * <code>12:34:56.7</code>.
   *
   * @param stream
   *   The stream to write to.
   * @param hour
   *   First sexagesimal unit, hour or &#176;.
   * @param min
   *   Second sexagesimal unit, minute or '.
   * @param sec
   *   Third sexagesimal unit, second or ". */

  final static public void WTime2(PrintStream stream,
    double hour, double min, double sec)
  {
    double h, m, s;

    h = hour; m = min; s = 0.1 * Math.floor(0.5 + 10. * sec);
    if (60. == s) {
      s -= 60.; m += 1.;
      if (60. == m) {m -= 60.; h += 1.;}
    }
    stream.print(zi2.format(h) + ":" + zi2.format(m) + ":" + zf4d1.format(s));
  }


  /**
   * Write date as year, month and day, time as hours, minutes and seconds.
   *
   * <p>Format the given sextuplet of year, month, day, hour, minute and
   * second in the form <code>2002-12-13-12:34:56.7</code>.
   *
   * @param stream
   *   The stream to write to.
   * @param year
   *   Year.
   * @param month
   *   Month.
   * @param day
   *   Day.
   * @param hour
   *   Hour.
   * @param min
   *   Minute.
   * @param sec
   *   Second. */

  final static public void WTime3(PrintStream stream,
    double year, double month, double day,
    double hour, double min, double sec)
  {
    double h, m, s;

    h = hour; m = min; s = 0.1 * Math.floor(0.5 + 10. * sec);
    if (60. == s) {
      s -= 60.; m += 1.;
      if (60. == m) {m -= 60.; h += 1.;}
    }
    stream.print(zi4.format(year) + "-" + zi2.format(month) + "-" +
		 zi2.format(day)  + "-" + zi2.format(h)     + ":" +
		 zi2.format(m)    + ":" + zf4d1.format(s));
  }


  /**
   * Write date as year, month and day, time as hours and minutes.
   *
   * <p>Format the given sextuplet of year, month, day, hour, minute and
   * second in the form <code>2002-12-13-12:35</code> where the fifth number
   * takes into account the sixth.  The rounding is also modified to prevent
   * the fifth number to reach the value 60 on output.
   *
   * @param stream
   *   The stream to write to.
   * @param year
   *   Year.
   * @param month
   *   Month.
   * @param day
   *   Day.
   * @param hour
   *   Hour.
   * @param min
   *   Minute.
   * @param sec
   *   Second. */

  final static public void WTime4(PrintStream stream,
    double year, double month, double day,
    double hour, double min, double sec)
  {
    double h, m;

    h = hour; m = Math.floor(0.5 + min + sec / 60.);
    if (60. == m) {m -= 60.; h += 1.;}
    stream.print(zi4.format(year) + "-" + zi2.format(month) + "-" +
		 zi2.format(day)  + "-" + zi2.format(h)     + ":" +
		 zi2.format(m));
  }


  /**
   * Write date as year, month and day.
   *
   * <p>Format the given triplet of year, month and day
   * in the form <code>2002-12-13</code>.
   *
   * @param stream
   *   The stream to write to.
   * @param year
   *   Year.
   * @param month
   *   Month.
   * @param day
   *   Day. */

  final static public void WTime5(PrintStream stream,
    double year, double month, double day)
  {
    stream.print(zi4.format(year)
         + "-" + zi2.format(month)
         + "-" + zi2.format(day));
  }


  /**
   * Write time as hours, minutes and seconds.
   *
   * <p>Format the given triplet of hour, minute and second in the form
   * <code>12:34:56</code>.
   *
   * @param stream
   *   The stream to write to.
   * @param hour
   *   First sexagesimal unit, hour or &#176;.
   * @param min
   *   Second sexagesimal unit, minute or '.
   * @param sec
   *   Third sexagesimal unit, second or ". */

  final static public void WTime6(PrintStream stream,
    double hour, double min, double sec)
  {
    double h, m, s;

    h = hour; m = min; s = 0.1 * Math.floor(0.5 + 10. * sec);
    if (60. == s) {
      s -= 60.; m += 1.;
      if (60. == m) {m -= 60.; h += 1.;}
    }
    stream.print(zi2.format(h) + ":" + zi2.format(m) + ":" + zi2.format(s));
  }


  /**
   * This class cannot be instantiated. */

  private Hmelib() {}

}
