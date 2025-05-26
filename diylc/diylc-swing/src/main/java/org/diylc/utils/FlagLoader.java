/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.utils;

import org.apache.log4j.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Loads image resources as Icons.
 * 
 * @author Branislav Stojkovic
 */
public class FlagLoader {

  public static Icon getIcon(String name) {
    java.net.URL imgURL = FlagLoader.class.getResource("/flags/flag_" + name + ".png");
    if (imgURL != null) {
      return new ImageIcon(imgURL, name);
    } else {
      Logger.getLogger(FlagLoader.class).error("Couldn't find file: " + name);
      return null;
    }
  }
}
