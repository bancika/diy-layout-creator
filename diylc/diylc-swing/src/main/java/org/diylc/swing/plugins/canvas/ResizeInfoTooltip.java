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
package org.diylc.swing.plugins.canvas;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Window;
import java.text.DecimalFormat;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.presenter.Presenter;
import org.diylc.utils.Constants;

/**
 * A lightweight tooltip window that displays resize dimensions and tracks the mouse cursor.
 */
public class ResizeInfoTooltip {

  private static final int OFFSET_X = 16;
  private static final int OFFSET_Y = 16;
  private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("0.00");

  private JWindow tooltipWindow;
  private JLabel tooltipLabel;
  private Component parentComponent;
  private IConfigurationManager<?> configManager;
  private Dimension currentDimensions;

  public ResizeInfoTooltip(Component parentComponent, IConfigurationManager<?> configManager) {
    this.parentComponent = parentComponent;
    this.configManager = configManager;
    initializeTooltip();
  }

  private void initializeTooltip() {
    Window parentWindow = SwingUtilities.getWindowAncestor(parentComponent);
    if (parentWindow == null) {
      return;
    }

    tooltipWindow = new JWindow(parentWindow);
    tooltipLabel = new JLabel() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        super.paintComponent(g2d);
        g2d.dispose();
      }
    };

    // Use system tooltip font and colors
    Font tooltipFont = UIManager.getFont("ToolTip.font");
    if (tooltipFont != null) {
      tooltipLabel.setFont(tooltipFont);
    }
    Color tooltipBackground = UIManager.getColor("ToolTip.background");
    Color tooltipForeground = UIManager.getColor("ToolTip.foreground");
    Color tooltipBorderColor = UIManager.getColor("ToolTip.border");
    if (tooltipBorderColor == null) {
      tooltipBorderColor = Color.GRAY;
    }
    
    tooltipLabel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(tooltipBorderColor, 1),
        BorderFactory.createEmptyBorder(4, 6, 4, 6)));
    tooltipLabel.setOpaque(true);
    tooltipLabel.setBackground(tooltipBackground != null ? tooltipBackground : new Color(255, 255, 220));
    tooltipLabel.setForeground(tooltipForeground != null ? tooltipForeground : Color.BLACK);

    tooltipWindow.add(tooltipLabel);
    tooltipWindow.setFocusableWindowState(false);
    tooltipWindow.setAlwaysOnTop(true);
  }

  /**
   * Updates the tooltip with new dimensions and mouse position.
   */
  public void update(Dimension dimensions, Point mousePosition) {
    this.currentDimensions = dimensions;

    if (dimensions == null || mousePosition == null) {
      hideTooltip();
      return;
    }

    if (tooltipWindow == null) {
      initializeTooltip();
      if (tooltipWindow == null) {
        return;
      }
    }

    // Format the dimensions
    String text = formatDimensions(dimensions);
    tooltipLabel.setText(text);
    tooltipLabel.setPreferredSize(null); // Reset to calculate preferred size
    tooltipWindow.pack();

    // Position the tooltip near the mouse cursor
    updateTooltipPosition(mousePosition);
    tooltipWindow.setVisible(true);
  }

  /**
   * Updates the tooltip position based on the current mouse position.
   */
  public void updatePosition(Point mousePosition) {
    if (currentDimensions != null && mousePosition != null && tooltipWindow != null && tooltipWindow.isVisible()) {
      updateTooltipPosition(mousePosition);
    }
  }

  private void updateTooltipPosition(Point mousePosition) {
    if (tooltipWindow == null || parentComponent == null) {
      return;
    }

    // Convert mouse position to screen coordinates
    Point screenPoint = new Point(mousePosition);
    SwingUtilities.convertPointToScreen(screenPoint, parentComponent);

    // Position tooltip 16 pixels down and right from cursor
    int x = screenPoint.x + OFFSET_X;
    int y = screenPoint.y + OFFSET_Y;

    // Ensure tooltip stays on screen
    Dimension tooltipSize = tooltipWindow.getSize();
    if (tooltipSize == null) {
      tooltipWindow.pack();
      tooltipSize = tooltipWindow.getSize();
    }

    if (tooltipSize != null) {
      java.awt.Toolkit toolkit = java.awt.Toolkit.getDefaultToolkit();
      Dimension screenSize = toolkit.getScreenSize();

      // Adjust if tooltip would go off screen
      if (x + tooltipSize.width > screenSize.width) {
        x = screenPoint.x - tooltipSize.width - OFFSET_X;
      }
      if (y + tooltipSize.height > screenSize.height) {
        y = screenPoint.y - tooltipSize.height - OFFSET_Y;
      }
      if (x < 0) {
        x = 0;
      }
      if (y < 0) {
        y = 0;
      }
    }

    tooltipWindow.setLocation(x, y);
  }

  private String formatDimensions(Dimension dimensions) {
    boolean metric = configManager.readBoolean(Presenter.METRIC_KEY, true);

    // Convert pixels to the selected unit
    double widthInPixels = dimensions.width;
    double heightInPixels = dimensions.height;

    // Convert to inches first, then to the target unit
    double widthInInches = widthInPixels / Constants.PIXELS_PER_INCH;
    double heightInInches = heightInPixels / Constants.PIXELS_PER_INCH;

    double width = metric ? widthInInches * 25.4 : widthInInches;
    double height = metric ? heightInInches * 25.4 : heightInInches;

    String unitStr = metric ? "mm" : "in";
    return String.format("<html>W: %s %s<br>H: %s %s</html>",
        SIZE_FORMAT.format(width), unitStr,
        SIZE_FORMAT.format(height), unitStr);
  }

  /**
   * Hides the tooltip.
   */
  public void hideTooltip() {
    if (tooltipWindow != null) {
      tooltipWindow.setVisible(false);
    }
    currentDimensions = null;
  }

  /**
   * Disposes the tooltip window.
   */
  public void dispose() {
    if (tooltipWindow != null) {
      tooltipWindow.dispose();
      tooltipWindow = null;
    }
  }
}

