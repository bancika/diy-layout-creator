/*
 * 
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.utils;

import java.awt.Color;
import java.util.function.Function;
import javax.swing.UIManager;

/**
 * Utility class for Swing UI-related operations.
 * 
 * @author Branislav Stojkovic
 */
public class SwingUtils {

  /**
   * Fixes tooltip colors for Linux systems where they may be unreadable.
   * Some Linux L&F themes set white text on light backgrounds, making tooltips invisible.
   * This method ensures tooltips always have dark text on light background for readability.
   * 
   * Should be called after UIManager.setLookAndFeel() to override system defaults.
   */
  public static void fixTooltipColors() {
    Color tooltipBg = UIManager.getColor("ToolTip.background");
    Color tooltipFg = UIManager.getColor("ToolTip.foreground");
    
    // Helper function to calculate brightness (0.0 = black, 1.0 = white)
    Function<Color, Double> getBrightness = (c) -> {
      return (0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue()) / 255.0;
    };
    
    // Check if we have a problematic color combination (light text on light background)
    boolean needsFix = false;
    if (tooltipFg != null && tooltipBg != null) {
      double fgBrightness = getBrightness.apply(tooltipFg);
      double bgBrightness = getBrightness.apply(tooltipBg);
      // If both foreground and background are light, it's unreadable
      if (fgBrightness > 0.7 && bgBrightness > 0.5) {
        needsFix = true;
      }
    } else if (tooltipFg == null || tooltipBg == null) {
      // Missing color definitions - set safe defaults
      needsFix = true;
    }
    
    if (needsFix) {
      UIManager.put("ToolTip.background", new Color(255, 255, 220)); // Light yellow
      UIManager.put("ToolTip.foreground", Color.BLACK);
    }
  }
}

