/*
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
 */

package jsattrak.utilities;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.plaf.metal.MetalIconFactory;

class TextIcons extends MetalIconFactory.TreeLeafIcon {

  protected String label;

  private static Hashtable<String,String> labels;

  protected TextIcons() {
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    super.paintIcon(c, g, x, y);
    if (label != null) {
      FontMetrics fm = g.getFontMetrics();

      int offsetX = (getIconWidth() - fm.stringWidth(label)) / 2;
      int offsetY = (getIconHeight() - fm.getHeight()) / 2 - 2;

      g.drawString(label, x + offsetX, y + offsetY + fm.getHeight());
    }
  }

  public static Icon getIcon(String str) {
//    if (labels == null) {
//      labels = new Hashtable<String,String>();
//      setDefaultSet();
//    }
    TextIcons icon = new TextIcons();
    icon.label = (String) labels.get(str);
    return icon;
  }

  public static void setLabelSet(String ext, String label) {
//    if (labels == null) {
//      labels = new Hashtable<String,String>();
//      setDefaultSet();
//    }
    labels.put(ext, label);
  }

//  private static void setDefaultSet() {
//    labels.put("c", "C");
//    labels.put("java", "J");
//    labels.put("html", "H");
//    labels.put("htm", "H");
//
//    // and so on
//    /*
//     * labels.put("txt" ,"TXT"); labels.put("TXT" ,"TXT"); labels.put("cc"
//     * ,"C++"); labels.put("C" ,"C++"); labels.put("cpp" ,"C++");
//     * labels.put("exe" ,"BIN"); labels.put("class" ,"BIN");
//     * labels.put("gif" ,"GIF"); labels.put("GIF" ,"GIF");
//     * 
//     * labels.put("", "");
//     */
//  }
}
