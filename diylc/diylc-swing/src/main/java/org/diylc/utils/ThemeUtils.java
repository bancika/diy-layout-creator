/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.utils;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;

/**
 * Utility class for detecting and applying OS dark theme settings.
 * 
 * Note: Modern Java (11+) with system Look and Feel should automatically
 * respect OS dark theme settings, but this utility provides explicit detection
 * and ensures compatibility with older Java versions.
 * 
 * @author Branislav Stojkovic
 */
public class ThemeUtils {

  private static final Logger LOG = Logger.getLogger(ThemeUtils.class);

  /**
   * Detects and applies OS dark theme settings.
   * This must be called before creating any AWT/Swing components.
   */
  public static void applyOSDarkTheme() {
    try {
      if (Utils.isMac()) {
        applyMacOSDarkTheme();
      } else if (Utils.isWindows()) {
        detectWindowsDarkTheme();
      } else {
        detectLinuxDarkTheme();
      }
    } catch (Exception e) {
      LOG.warn("Error detecting OS dark theme: " + e.getMessage(), e);
    }
  }

  /**
   * Applies dark theme colors to UIManager after the Look and Feel is set.
   * This should be called after UIManager.setLookAndFeel().
   * 
   * @return true if dark mode was detected and colors were applied, false otherwise
   */
  public static boolean applyDarkThemeColors() {
    try {
      if (Utils.isMac()) {
        return applyMacOSDarkThemeColors();
      } else if (Utils.isWindows()) {
        return applyWindowsDarkThemeColors();
      } else {
        return applyLinuxDarkThemeColors();
      }
    } catch (Exception e) {
      LOG.warn("Error applying dark theme colors: " + e.getMessage(), e);
      return false;
    }
  }

  /**
   * Detects and applies macOS dark theme settings.
   */
  private static void applyMacOSDarkTheme() {
    // macOS: Check if dark mode is enabled
    // Modern Java (11+) should automatically detect this, but we'll check explicitly
    String appearance = System.getProperty("apple.awt.application.appearance");
    if (appearance == null) {
      // Try to detect dark mode via system command (for older Java versions)
      try {
        Process process = Runtime.getRuntime().exec(
            new String[]{"defaults", "read", "-g", "AppleInterfaceStyle"});
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
        String result = reader.readLine();
        if ("Dark".equals(result)) {
          // Note: Setting this property after JVM start may not always work,
          // but it's worth trying for older Java versions
          System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
          LOG.info("Detected macOS dark mode - attempting to apply dark theme");
        } else {
          LOG.debug("macOS light mode detected");
        }
      } catch (Exception e) {
        // If detection fails, that's okay - modern Java will handle it automatically
        LOG.debug("Could not detect macOS theme via defaults command: " + e.getMessage());
      }
    } else if ("NSAppearanceNameDarkAqua".equals(appearance)) {
      LOG.info("macOS dark mode already detected by JVM");
    } else {
      LOG.debug("macOS appearance: " + appearance);
    }
  }

  /**
   * Detects Windows dark theme settings.
   */
  private static void detectWindowsDarkTheme() {
    // Windows: Try to detect dark theme via registry
    try {
      Process process = Runtime.getRuntime().exec(
          new String[]{"reg", "query", 
              "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 
              "/v", "AppsUseLightTheme"});
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.contains("AppsUseLightTheme")) {
          // If value is 0, dark theme is enabled
          if (line.contains("0x0") || line.endsWith("0")) {
            // For Windows, the system Look and Feel should automatically respect dark mode
            // in Java 11+, but we can log it for debugging
            LOG.info("Detected Windows dark mode");
          }
          break;
        }
      }
    } catch (Exception e) {
      LOG.debug("Could not detect Windows theme via registry: " + e.getMessage());
    }
  }

  /**
   * Detects Linux dark theme settings.
   */
  private static void detectLinuxDarkTheme() {
    // Linux: Check GTK theme or environment variables
    try {
      String gtkTheme = System.getenv("GTK_THEME");
      if (gtkTheme != null && (gtkTheme.toLowerCase().contains("dark") || 
          gtkTheme.toLowerCase().contains("Dark"))) {
        LOG.info("Detected Linux dark theme: " + gtkTheme);
      }
      // Also check COLORFGBG environment variable (used by some terminals/desktops)
      String colorFgBg = System.getenv("COLORFGBG");
      if (colorFgBg != null) {
        // Format is typically "15;0" where second number < 8 indicates dark background
        String[] parts = colorFgBg.split(";");
        if (parts.length > 1) {
          try {
            int bgColor = Integer.parseInt(parts[parts.length - 1]);
            if (bgColor < 8) {
              LOG.info("Detected Linux dark background via COLORFGBG");
            }
          } catch (NumberFormatException e) {
            // Ignore
          }
        }
      }
    } catch (Exception e) {
      LOG.debug("Could not detect Linux theme: " + e.getMessage());
    }
  }

  /**
   * Applies macOS dark theme colors to UIManager.
   * @return true if dark mode is active
   */
  private static boolean applyMacOSDarkThemeColors() {
    boolean isDarkMode = false;
    
    // Check if dark mode is active
    String appearance = System.getProperty("apple.awt.application.appearance");
    if (appearance == null) {
      try {
        Process process = Runtime.getRuntime().exec(
            new String[]{"defaults", "read", "-g", "AppleInterfaceStyle"});
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()));
        String result = reader.readLine();
        isDarkMode = "Dark".equals(result);
      } catch (Exception e) {
        LOG.debug("Could not detect macOS theme: " + e.getMessage());
        // Try to infer from current UI colors
        Color menuBarBg = UIManager.getColor("MenuBar.background");
        if (menuBarBg != null && isDarkColor(menuBarBg)) {
          isDarkMode = true;
        }
      }
    } else {
      isDarkMode = "NSAppearanceNameDarkAqua".equals(appearance);
    }

    if (isDarkMode) {
      LOG.info("Applying macOS dark theme colors");
      applyDarkColors();
      return true;
    }
    return false;
  }

  /**
   * Applies Windows dark theme colors to UIManager.
   * @return true if dark mode is active
   */
  private static boolean applyWindowsDarkThemeColors() {
    boolean isDarkMode = false;
    
    try {
      Process process = Runtime.getRuntime().exec(
          new String[]{"reg", "query", 
              "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize", 
              "/v", "AppsUseLightTheme"});
      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.contains("AppsUseLightTheme")) {
          // If value is 0, dark theme is enabled
          if (line.contains("0x0") || line.endsWith("0")) {
            isDarkMode = true;
          }
          break;
        }
      }
    } catch (Exception e) {
      LOG.debug("Could not detect Windows theme: " + e.getMessage());
      // Try to infer from current UI colors
      Color menuBarBg = UIManager.getColor("MenuBar.background");
      if (menuBarBg != null && isDarkColor(menuBarBg)) {
        isDarkMode = true;
      }
    }

    if (isDarkMode) {
      LOG.info("Applying Windows dark theme colors");
      applyDarkColors();
      return true;
    }
    return false;
  }

  /**
   * Applies Linux dark theme colors to UIManager.
   * @return true if dark mode is active
   */
  private static boolean applyLinuxDarkThemeColors() {
    boolean isDarkMode = false;
    
    try {
      String gtkTheme = System.getenv("GTK_THEME");
      if (gtkTheme != null && (gtkTheme.toLowerCase().contains("dark") || 
          gtkTheme.toLowerCase().contains("Dark"))) {
        isDarkMode = true;
      }
      
      // Also check COLORFGBG
      if (!isDarkMode) {
        String colorFgBg = System.getenv("COLORFGBG");
        if (colorFgBg != null) {
          String[] parts = colorFgBg.split(";");
          if (parts.length > 1) {
            try {
              int bgColor = Integer.parseInt(parts[parts.length - 1]);
              if (bgColor < 8) {
                isDarkMode = true;
              }
            } catch (NumberFormatException e) {
              // Ignore
            }
          }
        }
      }
      
      // Try to infer from current UI colors
      if (!isDarkMode) {
        Color menuBarBg = UIManager.getColor("MenuBar.background");
        if (menuBarBg != null && isDarkColor(menuBarBg)) {
          isDarkMode = true;
        }
      }
    } catch (Exception e) {
      LOG.debug("Could not detect Linux theme: " + e.getMessage());
    }

    if (isDarkMode) {
      LOG.info("Applying Linux dark theme colors");
      applyDarkColors();
      return true;
    }
    return false;
  }

  /**
   * Applies dark theme colors to common UI components.
   * Only overrides colors that would cause visibility issues (e.g., dark text on dark background).
   */
  private static void applyDarkColors() {
    // Menu bar and menu colors - fix text colors if background is dark
    Color menuBarBg = UIManager.getColor("MenuBar.background");
    Color menuBarFg = UIManager.getColor("MenuBar.foreground");
    
    if (menuBarBg != null && isDarkColor(menuBarBg)) {
      // Background is dark, ensure text is light
      if (menuBarFg == null || isDarkColor(menuBarFg)) {
        UIManager.put("MenuBar.foreground", Color.WHITE);
      }
      
      // Fix menu colors
      Color menuBg = UIManager.getColor("Menu.background");
      Color menuFg = UIManager.getColor("Menu.foreground");
      if (menuBg == null || isDarkColor(menuBg)) {
        UIManager.put("Menu.background", new Color(50, 50, 50));
      }
      if (menuFg == null || (menuBg != null && isDarkColor(menuBg) && isDarkColor(menuFg))) {
        UIManager.put("Menu.foreground", Color.WHITE);
      }
      UIManager.put("Menu.selectionBackground", new Color(60, 60, 60));
      UIManager.put("Menu.selectionForeground", Color.WHITE);
      
      // Fix menu item colors
      Color menuItemBg = UIManager.getColor("MenuItem.background");
      Color menuItemFg = UIManager.getColor("MenuItem.foreground");
      if (menuItemBg == null || isDarkColor(menuItemBg)) {
        UIManager.put("MenuItem.background", new Color(50, 50, 50));
      }
      if (menuItemFg == null || (menuItemBg != null && isDarkColor(menuItemBg) && isDarkColor(menuItemFg))) {
        UIManager.put("MenuItem.foreground", Color.WHITE);
      }
      UIManager.put("MenuItem.selectionBackground", new Color(60, 60, 60));
      UIManager.put("MenuItem.selectionForeground", Color.WHITE);
    }
    
    // Popup menu colors
    Color popupBg = UIManager.getColor("PopupMenu.background");
    if (popupBg == null || isDarkColor(popupBg)) {
      UIManager.put("PopupMenu.background", new Color(50, 50, 50));
      UIManager.put("PopupMenu.foreground", Color.WHITE);
      UIManager.put("PopupMenu.borderColor", new Color(70, 70, 70));
    }
    
    // Checkbox and radio button menu items
    Color checkBoxMenuItemBg = UIManager.getColor("CheckBoxMenuItem.background");
    if (checkBoxMenuItemBg == null || isDarkColor(checkBoxMenuItemBg)) {
      UIManager.put("CheckBoxMenuItem.background", new Color(50, 50, 50));
      UIManager.put("CheckBoxMenuItem.foreground", Color.WHITE);
      UIManager.put("CheckBoxMenuItem.selectionBackground", new Color(60, 60, 60));
      UIManager.put("CheckBoxMenuItem.selectionForeground", Color.WHITE);
    }
    
    Color radioMenuItemBg = UIManager.getColor("RadioButtonMenuItem.background");
    if (radioMenuItemBg == null || isDarkColor(radioMenuItemBg)) {
      UIManager.put("RadioButtonMenuItem.background", new Color(50, 50, 50));
      UIManager.put("RadioButtonMenuItem.foreground", Color.WHITE);
      UIManager.put("RadioButtonMenuItem.selectionBackground", new Color(60, 60, 60));
      UIManager.put("RadioButtonMenuItem.selectionForeground", Color.WHITE);
    }
  }

  /**
   * Checks if a color is dark (suitable for dark theme).
   */
  private static boolean isDarkColor(Color color) {
    if (color == null) {
      return false;
    }
    // Calculate luminance
    double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
    return luminance < 0.5;
  }
}

