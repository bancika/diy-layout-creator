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

import java.awt.Font;
import java.util.Enumeration;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Utility class for applying a configurable font scaling factor to all UI components.
 * 
 * The scaling factor can be set via the VM argument: -Dorg.diylc.fontScaleFactor=1.5
 * For example, a factor of 1.5 will make all fonts 50% larger.
 * 
 * @author Branislav Stojkovic
 */
public class FontScalingUtils {

  private static final String FONT_SCALE_FACTOR_PROPERTY = "org.diylc.fontScaleFactor";
  private static Double fontScaleFactor = null;

  /**
   * Gets the font scaling factor from the VM argument.
   * 
   * @return The scaling factor (defaults to 1.0 if not set or invalid)
   */
  public static double getFontScaleFactor() {
    if (fontScaleFactor == null) {
      String factorStr = System.getProperty(FONT_SCALE_FACTOR_PROPERTY);
      if (factorStr != null && !factorStr.trim().isEmpty()) {
        try {
          double factor = Double.parseDouble(factorStr.trim());
          // Validate the factor is reasonable (between 0.5 and 3.0)
          if (factor >= 0.5 && factor <= 3.0) {
            fontScaleFactor = factor;
          } else {
            System.err.println("Warning: Font scale factor " + factor + " is out of range (0.5-3.0). Using default 1.0");
            fontScaleFactor = 1.0;
          }
        } catch (NumberFormatException e) {
          System.err.println("Warning: Invalid font scale factor '" + factorStr + "'. Using default 1.0");
          fontScaleFactor = 1.0;
        }
      } else {
        fontScaleFactor = 1.0;
      }
    }
    return fontScaleFactor;
  }

  /**
   * Applies the font scaling factor to all UIManager font defaults.
   * This should be called once during application initialization, after setting the
   * Look and Feel but before creating any UI components.
   * 
   * The scaling factor is read from the VM argument: -Dorg.diylc.fontScaleFactor=X
   * where X is a number between 0.5 and 3.0 (e.g., 1.5 for 50% larger fonts).
   */
  public static void applyFontScaling() {
    double scaleFactor = getFontScaleFactor();
    
    // If factor is 1.0, no scaling needed
    if (scaleFactor == 1.0) {
      return;
    }
    
    try {
      UIDefaults defaults = UIManager.getDefaults();
      Enumeration<Object> keys = defaults.keys();
      
      while (keys.hasMoreElements()) {
        Object key = keys.nextElement();
        Object value = defaults.get(key);
        
        // Check if this is a font property
        if (value instanceof Font) {
          Font font = (Font) value;
          float newSize = (float) (font.getSize() * scaleFactor);
          Font scaledFont = font.deriveFont(newSize);
          defaults.put(key, scaledFont);
        }
      }
    } catch (Exception e) {
      System.err.println("Warning: Could not apply font scaling: " + e.getMessage());
    }
  }
}

