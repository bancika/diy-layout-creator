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

import java.awt.Color;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.Utils;

/**
 * Installs the Swing look and feel selected under Config > UI Theme. The native look and feel
 * remains the default; the light and dark themes are provided by FlatLaf.
 *
 * @author Branislav Stojkovic
 */
public class LookAndFeelUtils {

  public static final String UI_THEME_KEY = "uiTheme";
  public static final String UI_THEME_SYSTEM = "System";
  public static final String UI_THEME_LIGHT = "Light";
  public static final String UI_THEME_DARK = "Dark";
  public static final String UI_THEME_DEFAULT = UI_THEME_SYSTEM;

  /**
   * Installs the configured look and feel. Must run before any UI component is created, since
   * switching requires an application restart.
   */
  public static void applyLookAndFeel() throws ClassNotFoundException, InstantiationException,
      IllegalAccessException, UnsupportedLookAndFeelException {
    String theme = ConfigurationManager.getInstance().readString(UI_THEME_KEY, UI_THEME_DEFAULT);
    if (UI_THEME_DARK.equalsIgnoreCase(theme)) {
      UIManager.setLookAndFeel(Utils.isMac() ? new FlatMacDarkLaf() : new FlatDarkLaf());
    } else if (UI_THEME_LIGHT.equalsIgnoreCase(theme)) {
      UIManager.setLookAndFeel(Utils.isMac() ? new FlatMacLightLaf() : new FlatLightLaf());
    } else {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }
  }

  public static boolean isDark() {
    return FlatLaf.isLafDark();
  }

  /**
   * Picks between two variants of a color that the look and feel does not provide, such as an
   * accent or a highlight.
   */
  public static Color themeColor(Color light, Color dark) {
    return isDark() ? dark : light;
  }

  /**
   * Same as {@link #themeColor(Color, Color)} for colors embedded into HTML, in "#rrggbb" form.
   */
  public static String themeHtmlColor(String light, String dark) {
    return isDark() ? dark : light;
  }
}
