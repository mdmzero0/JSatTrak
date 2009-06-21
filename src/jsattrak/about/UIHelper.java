/*
 * 
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 * 
 * This file is part of JSatTrak.
 * 
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 * 
 * $Id: UIHelper.java,v 1.1 2005/05/25 23:13:23 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 */
package jsattrak.about;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public class UIHelper {
    public static ImageIcon readImageIcon(String fileName)
    {
        Image image = readImage(fileName);
        if (image == null)
            return null;

        return new ImageIcon(image);
    }

    public static Image readImage(String fileName)
    {
        URL url = UIHelper.class.getResource("images/" + fileName);
        if (url == null)
            return null;

        return java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }
}
