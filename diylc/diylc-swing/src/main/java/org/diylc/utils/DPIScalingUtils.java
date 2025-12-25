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
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.Enumeration;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Utility class for handling DPI scaling on high-resolution displays.
 * 
 * This is particularly important on Windows where HTML rendering in Swing
 * components (like JLabel) can fail to properly scale fonts on high DPI displays.
 * 
 * @author Branislav Stojkovic
 */
public class DPIScalingUtils {

  private static Double dpiScaleFactor = null;
  private static final double SCALING_THRESHOLD = 0.85; // 85% threshold to avoid double-scaling
  private static final double OVERSCALE_THRESHOLD = 1.3; // 130% - if font is this much larger than expected, it's over-scaled
  private static final float BASE_FONT_SIZE_96DPI = 12.0f; // Standard font size at 96 DPI
  private static final float MINIMUM_FONT_SIZE = 10.0f;
  private static final float MAXIMUM_REASONABLE_FONT_SIZE = 20.0f;

  /**
   * Gets the DPI scale factor for the current display.
   * This is particularly important on Windows with high DPI displays where
   * HTML rendering in Swing components can fail to properly scale fonts.
   * 
   * @return The DPI scale factor (1.0 for normal displays, > 1.0 for high DPI)
   */
  public static double getDpiScaleFactor() {
    if (dpiScaleFactor == null) {
      try {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        
        // Get the transform scale factor from GraphicsConfiguration
        // This is the most reliable way to detect DPI scaling
        double scaleX = gc.getDefaultTransform().getScaleX();
        double scaleY = gc.getDefaultTransform().getScaleY();
        double transformScale = Math.max(scaleX, scaleY);
        
        // On Windows, Java's DPI awareness can be inconsistent, especially with HTML rendering
        // Check if we're on Windows and if the transform indicates scaling
        if (System.getProperty("os.name", "").toLowerCase().contains("windows")) {
          // If transform scale is > 1.0, we have DPI scaling
          // HTML rendering in Swing on Windows may not respect this, so we need to compensate
          if (transformScale > 1.0) {
            dpiScaleFactor = transformScale;
          } else {
            // Fallback: check screen resolution as a secondary indicator
            int screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
            if (screenResolution > 96) {
              // High DPI display detected via resolution
              dpiScaleFactor = screenResolution / 96.0;
            } else {
              dpiScaleFactor = 1.0;
            }
          }
        } else {
          // On non-Windows systems, trust the transform scale
          dpiScaleFactor = Math.max(1.0, transformScale);
        }
      } catch (Exception e) {
        // Fallback to 1.0 if there's any error
        dpiScaleFactor = 1.0;
      }
    }
    return dpiScaleFactor;
  }

  /**
   * Calculates the expected font size for a given base size at the current DPI.
   * 
   * @param baseSize The base font size (typically at 96 DPI)
   * @return The expected font size at the current DPI
   */
  private static float getExpectedFontSize(float baseSize) {
    double scaleFactor = getDpiScaleFactor();
    return (float) (baseSize * scaleFactor);
  }

  /**
   * Scales a font to match the DPI scaling of the display.
   * This is useful for components that use HTML rendering (like JLabel with HTML text),
   * which may not properly scale fonts on Windows with high DPI displays.
   * 
   * @param font The font to scale
   * @return A scaled font if high DPI is detected and scaling is needed, otherwise the original font
   */
  public static Font scaleFontForDPI(Font font) {
    if (font == null) {
      return null;
    }
    
    double scaleFactor = getDpiScaleFactor();
    
    // Only apply scaling when high DPI is detected
    if (scaleFactor <= 1.0) {
      return font;
    }
    
    float currentSize = font.getSize();
    
    // Calculate what the base size would be (assuming current size is at 96 DPI)
    float assumedBaseSize = currentSize;
    
    // Calculate what the expected size should be at this DPI
    float expectedSize = getExpectedFontSize(assumedBaseSize);
    
    // Calculate the ratio of current to expected size
    double sizeRatio = currentSize / expectedSize;
    
    // Only scale if the current size is significantly smaller than expected
    // This prevents double-scaling on systems that already handle it correctly
    if (sizeRatio < SCALING_THRESHOLD) {
      // Scale to expected size, but ensure minimum readability
      float scaledSize = Math.max(expectedSize, MINIMUM_FONT_SIZE);
      return font.deriveFont(scaledSize);
    }
    
    return font;
  }

  /**
   * Downscales a font that appears to be over-scaled.
   * This calculates what the font size should be based on the actual DPI
   * and scales it down proportionally if it's significantly larger than expected.
   * 
   * @param font The font to downscale
   * @return A downscaled font if needed, otherwise the original font
   */
  private static Font downscaleFontForDPI(Font font) {
    if (font == null) {
      return font;
    }
    
    float currentSize = font.getSize();
    double scaleFactor = getDpiScaleFactor();
    
    // Calculate what the base size would be if we reverse the DPI scaling
    // This assumes the font was scaled from a base size at 96 DPI
    float estimatedBaseSize = (float) (currentSize / scaleFactor);
    
    // Calculate what the expected size should be at this DPI
    float expectedSize = getExpectedFontSize(BASE_FONT_SIZE_96DPI);
    
    // Calculate the ratio of current to expected size
    double sizeRatio = currentSize / expectedSize;
    
    // If the font is significantly larger than expected (over-scaled), downscale it
    if (sizeRatio > OVERSCALE_THRESHOLD) {
      // Calculate target size: use the base size if reasonable, otherwise use expected size
      float targetSize;
      if (estimatedBaseSize >= MINIMUM_FONT_SIZE && estimatedBaseSize <= MAXIMUM_REASONABLE_FONT_SIZE) {
        // Use the calculated base size if it's reasonable
        targetSize = estimatedBaseSize;
      } else {
        // Otherwise, use the expected size for the current DPI
        targetSize = expectedSize;
      }
      
      // Ensure target is within reasonable bounds
      targetSize = Math.max(MINIMUM_FONT_SIZE, Math.min(targetSize, MAXIMUM_REASONABLE_FONT_SIZE));
      
      // Only apply if the reduction is significant enough to matter (>10% change)
      if (Math.abs(targetSize - currentSize) / currentSize > 0.1) {
        return font.deriveFont(targetSize);
      }
    }
    
    return font;
  }

  /**
   * Scales a font size value to match the DPI scaling of the display.
   * 
   * @param fontSize The base font size to scale
   * @return The scaled font size if high DPI is detected, otherwise the original size
   */
  public static float scaleFontSizeForDPI(float fontSize) {
    double scaleFactor = getDpiScaleFactor();
    if (scaleFactor > 1.0) {
      return (float) (fontSize * scaleFactor);
    }
    return fontSize;
  }

  /**
   * Scales an integer font size value to match the DPI scaling of the display.
   * 
   * @param fontSize The base font size to scale
   * @return The scaled font size if high DPI is detected, otherwise the original size
   */
  public static int scaleFontSizeForDPI(int fontSize) {
    return Math.round(scaleFontSizeForDPI((float) fontSize));
  }

  /**
   * Resets the cached DPI scale factor. This is useful if the display configuration
   * changes at runtime (e.g., moving the window to a different display).
   */
  public static void resetCache() {
    dpiScaleFactor = null;
  }

  /**
   * Checks if the current display is a high DPI display.
   * 
   * @return true if the DPI scale factor is greater than 1.0
   */
  public static boolean isHighDPIDisplay() {
    return getDpiScaleFactor() > 1.0;
  }

  /**
   * Scales all UIManager font defaults to match the DPI scaling of the display.
   * This applies globally to all Swing components, ensuring consistent font scaling
   * across the entire application without needing to modify individual components.
   * 
   * This should be called once during application initialization, after setting the
   * Look and Feel but before creating any UI components.
   * 
   * On Windows: Fixes HTML rendering issues where fonts don't scale properly.
   * On Linux: May downscale fonts if they appear to be over-scaled by the system.
   * On macOS: Does nothing (macOS handles DPI scaling correctly).
   * 
   * The scaling is conservative - it only scales fonts that appear to need adjustment,
   * preventing double-scaling on systems that already handle it correctly.
   */
  public static void scaleUIManagerFonts() {
    String osName = System.getProperty("os.name", "").toLowerCase();
    boolean isWindows = osName.contains("windows");
    boolean isLinux = osName.contains("linux");
    boolean isMac = osName.contains("mac");
    
    // On macOS, Java/Swing handles DPI scaling perfectly, so do nothing
    if (isMac) {
      return;
    }
    
    double scaleFactor = getDpiScaleFactor();
    
    try {
      UIDefaults defaults = UIManager.getDefaults();
      Enumeration<Object> keys = defaults.keys();
      
      // Get a reference font size to check if fonts are already scaled
      // Typical default font size is 12-13pt at 96 DPI
      Font defaultFont = UIManager.getFont("Label.font");
      if (defaultFont == null) {
        defaultFont = UIManager.getFont("Button.font");
      }
      
      if (defaultFont == null) {
        return;
      }
      
      float defaultSize = defaultFont.getSize();
      
      // Calculate expected font size at current DPI (assuming base size of 12pt at 96 DPI)
      float expectedSize = getExpectedFontSize(BASE_FONT_SIZE_96DPI);
      
      // Calculate the ratio of actual to expected size
      double sizeRatio = defaultSize / expectedSize;
      
      // Determine if we need to scale up or down based on the ratio
      boolean needsUpScaling = false;
      boolean needsDownScaling = false;
      
      if (isWindows && scaleFactor > 1.0) {
        // On Windows with high DPI, fonts may be too small due to HTML rendering issues
        // If font is significantly smaller than expected, it needs up-scaling
        if (sizeRatio < SCALING_THRESHOLD) {
          needsUpScaling = true;
        }
      } else if (isLinux) {
        // On Linux, fonts may be over-scaled by the system
        // If font is significantly larger than expected, it needs down-scaling
        if (sizeRatio > OVERSCALE_THRESHOLD) {
          needsDownScaling = true;
        }
      }
      
      // If no scaling is needed, return early
      if (!needsUpScaling && !needsDownScaling) {
        return;
      }
      
      while (keys.hasMoreElements()) {
        Object key = keys.nextElement();
        Object value = defaults.get(key);
        
        // Check if this is a font property
        if (value instanceof Font) {
          Font font = (Font) value;
          Font adjustedFont = font;
          
          if (needsUpScaling) {
            // Scale up for Windows (fix HTML rendering issues)
            adjustedFont = scaleFontForDPI(font);
          } else if (needsDownScaling) {
            // Scale down for Linux (fix over-scaling)
            adjustedFont = downscaleFontForDPI(font);
          }
          
          // Only update if the font was actually adjusted
          if (adjustedFont != font) {
            defaults.put(key, adjustedFont);
          }
        }
      }
    } catch (Exception e) {
      // Log error but don't fail - font scaling is a nice-to-have feature
      System.err.println("Warning: Could not scale UIManager fonts: " + e.getMessage());
    }
  }
}

