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
 * $Id: SDP4Exception.java,v 4.1 2004/08/09 07:54:20 hme Exp $
 *
 * $Log: SDP4Exception.java,v $
 * Revision 4.1  2004/08/09 07:54:20  hme
 * Version 2.1.1.
 *
 * Revision 3.1  2004/07/28 10:48:53  hme
 * Version 2.1.
 *
 * Revision 2.3  2003/09/16 14:48:39  hme
 * *** empty log message ***
 *
 * Revision 2.2  2003/09/16 13:30:31  hme
 * *** empty log message ***
 *
 *-*/

//package com.chiandh.Lib;
package name.gano.astro.propogators.sdp4;

/**
 * <p>The <code>SDP4Exception</code> is superclass for any exceptions that
 * may be thrown by the {@link SDP4 SDP4} class.
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
 * <p>$Id: SDP4Exception.java,v 4.1 2004/08/09 07:54:20 hme Exp $
 *
 * <dl>
 * <dt><strong>2.2:</strong> 2003/09/16 hme
 * <dd>Initial revision.
 * </dl>
 *
 * @author
 *   Horst Meyerdierks, c/o Royal Observatory,
 *   Blackford Hill, Edinburgh, EH9 3HJ, Scotland;
 *   &lt; hme &#64; roe.ac.uk &gt; */

public class SDP4Exception extends Exception {
  public SDP4Exception()         {super();}
  public SDP4Exception(String s) {super(s);}
}
