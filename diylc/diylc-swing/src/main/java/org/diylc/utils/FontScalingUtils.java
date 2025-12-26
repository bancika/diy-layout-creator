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
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;

/**
 * Utility class for applying a configurable font scaling factor to all UI components.
 * 
 * The scaling factor is stored in the application configuration and can be set
 * programmatically or through the configuration system.
 * For example, a factor of 1.5 will make all fonts 50% larger.
 * 
 * @author Branislav Stojkovic
 */
public class FontScalingUtils {

  private static final double DEFAULT_FONT_SCALE_FACTOR = 1.0;
  private static final double MIN_FONT_SCALE_FACTOR = 0.5;
  private static final double MAX_FONT_SCALE_FACTOR = 3.0;

  /**
   * Gets the font scaling factor from ConfigurationManager.
   * 
   * @return The scaling factor (defaults to 1.0 if not set or invalid)
   */
  public static double getFontScaleFactor() {
    try {
      Object factorObj = ConfigurationManager.getInstance().readObject(
          IPlugInPort.FONT_SCALE_FACTOR_KEY, DEFAULT_FONT_SCALE_FACTOR);
      
      if (factorObj instanceof Number) {
        double factor = ((Number) factorObj).doubleValue();
        // Validate the factor is reasonable (between 0.5 and 3.0)
        if (factor >= MIN_FONT_SCALE_FACTOR && factor <= MAX_FONT_SCALE_FACTOR) {
          return factor;
        } else {
          System.err.println("Warning: Font scale factor " + factor + " is out of range ("
              + MIN_FONT_SCALE_FACTOR + "-" + MAX_FONT_SCALE_FACTOR + "). Using default "
              + DEFAULT_FONT_SCALE_FACTOR);
          return DEFAULT_FONT_SCALE_FACTOR;
        }
      } else if (factorObj instanceof String) {
        // Handle string values (for backward compatibility or manual config editing)
        try {
          double factor = Double.parseDouble((String) factorObj);
          if (factor >= MIN_FONT_SCALE_FACTOR && factor <= MAX_FONT_SCALE_FACTOR) {
            return factor;
          }
        } catch (NumberFormatException e) {
          // Invalid string format, use default
        }
      }
    } catch (Exception e) {
      System.err.println("Warning: Could not read font scale factor from configuration: " + e.getMessage());
    }
    
    return DEFAULT_FONT_SCALE_FACTOR;
  }

  /**
   * Sets the font scaling factor in ConfigurationManager.
   * 
   * @param factor The scaling factor (must be between 0.5 and 3.0)
   * @return true if the factor was set successfully, false if invalid
   */
  public static boolean setFontScaleFactor(double factor) {
    if (factor < MIN_FONT_SCALE_FACTOR || factor > MAX_FONT_SCALE_FACTOR) {
      return false;
    }
    
    try {
      ConfigurationManager.getInstance().writeValue(IPlugInPort.FONT_SCALE_FACTOR_KEY, factor);
      return true;
    } catch (Exception e) {
      System.err.println("Warning: Could not write font scale factor to configuration: " + e.getMessage());
      return false;
    }
  }

  /**
   * Applies the font scaling factor to all UIManager font defaults.
   * This should be called once during application initialization, after setting the
   * Look and Feel but before creating any UI components.
   * 
   * The scaling factor is read from ConfigurationManager using the key
   * IPlugInPort.FONT_SCALE_FACTOR_KEY. The factor should be a number between 0.5 and 3.0
   * (e.g., 1.5 for 50% larger fonts). Use setFontScaleFactor() to change the value
   * programmatically.
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
        if (value instanceof Font font) {
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

