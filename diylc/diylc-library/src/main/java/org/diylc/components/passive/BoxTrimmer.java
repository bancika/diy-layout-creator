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
package org.diylc.components.passive;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.Display;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.ResistanceUnit;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Box Multi-Turn Pot", author = "Clive", category = "Passive",
    instanceNamePrefix = "VR", description = "Box style multi-turn potentiometer (e.g. Bourns PV36)",
    zOrder = IDIYComponent.COMPONENT, transformer = org.diylc.components.transform.BoxTrimmerTransformer.class)
public class BoxTrimmer extends AbstractTransparentComponent<Resistance> {

  private static final long serialVersionUID = 1L;
  
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Size BODY_WIDTH = new Size(0.19d, SizeUnit.in);  // 2x 2.41mm = 4.82mm ≈ 0.19"
  public static Size BODY_LENGTH = new Size(0.375d, SizeUnit.in); // 9.53mm from datasheet
  public static Color BODY_COLOR = Color.decode("#4477BB");
  public static Color BORDER_COLOR = BODY_COLOR.darker();
  public static Color PIN_COLOR = Color.decode("#CCCCCC");
  
  private Resistance value = new Resistance(10d, ResistanceUnit.K);
  private Orientation orientation = Orientation._90;  // Default to vertical (90 degrees)
  private Display display = Display.NAME;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  protected String name;
  private boolean screwFlippedHorizontally = false;  // Track if screw flipped left-right
  private boolean screwFlippedVertically = false;    // Track if screw flipped top-bottom
  
  private Point2D[] controlPoints = new Point2D[3];

  public BoxTrimmer() {
    super();
    updateControlPoints();
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(
        IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    int bodyWidth = (int) BODY_WIDTH.convertToPixels();
    int bodyLength = (int) BODY_LENGTH.convertToPixels();
    
    // Calculate positions - body is centered on the middle pin
    int centerX = (int) controlPoints[1].getX();
    int centerY = (int) controlPoints[1].getY();
    
    int bodyX, bodyY, bodyW, bodyH;
    
    // Position body based on orientation - fits tightly around pins
    if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
      // Horizontal - body is wide, pins are below/above
      bodyW = bodyLength;
      bodyH = bodyWidth;
      bodyX = centerX - bodyLength / 2;
      bodyY = centerY - bodyWidth / 2;
    } else {
      // Vertical - body is tall, pins are left/right  
      bodyW = bodyWidth;
      bodyH = bodyLength;
      bodyX = centerX - bodyWidth / 2;
      bodyY = centerY - bodyLength / 2;
    }
    
    // Draw body
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
          1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fillRoundRect(bodyX, bodyY, bodyW, bodyH, bodyW / 10, bodyH / 10);
    g2d.setComposite(oldComposite);
    
    // Draw border
    Color finalBorderColor = componentState == ComponentState.SELECTED || 
        componentState == ComponentState.DRAGGING ? SELECTION_COLOR : 
        (outlineMode ? theme.getOutlineColor() : borderColor);
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.drawRoundRect(bodyX, bodyY, bodyW, bodyH, bodyW / 10, bodyH / 10);
    
    // Draw adjustment screw (slotted screw head) - position based on orientation
    if (!outlineMode) {
      // Brass/gold color for screw head
      Color brassColor = new Color(184, 134, 11); // Dark goldenrod
      
      int screwDiameter = Math.min(bodyW, bodyH) / 3;
      int screwX, screwY;
      
      // Position screw based on orientation and flip state
      // The screw should be in the corner near pin 0, but can flip horizontally or vertically
      switch (orientation) {
        case DEFAULT:  // Horizontal, pins go right from pin 0
          screwX = screwFlippedHorizontally ? bodyX + bodyW - screwDiameter : bodyX;
          screwY = screwFlippedVertically ? bodyY : bodyY + bodyH - screwDiameter;
          break;
        case _90:  // Vertical, pins go down from pin 0
          screwX = screwFlippedHorizontally ? bodyX + bodyW - screwDiameter : bodyX;
          screwY = screwFlippedVertically ? bodyY : bodyY + bodyH - screwDiameter;
          break;
        case _180:  // Horizontal, pins go left from pin 0
          screwX = screwFlippedHorizontally ? bodyX : bodyX + bodyW - screwDiameter;
          screwY = screwFlippedVertically ? bodyY : bodyY + bodyH - screwDiameter;
          break;
        case _270:  // Vertical, pins go up from pin 0
          screwX = screwFlippedHorizontally ? bodyX + bodyW - screwDiameter : bodyX;
          screwY = screwFlippedVertically ? bodyY + bodyH - screwDiameter : bodyY;
          break;
        default:
          screwX = bodyX;
          screwY = bodyY + bodyH - screwDiameter;
          break;
      }
      
      // Draw screw head circle
      g2d.setColor(brassColor);
      g2d.fillOval(screwX, screwY, screwDiameter, screwDiameter);
      
      // Draw slot in screw - centered horizontally through the circle
      g2d.setColor(new Color(80, 80, 80)); // Dark gray for slot
      int slotLength = (int)(screwDiameter * 0.7);
      int slotThickness = Math.max(1, screwDiameter / 10);
      int slotCenterX = screwX + screwDiameter / 2;
      int slotCenterY = screwY + screwDiameter / 2;
      g2d.fillRect(slotCenterX - slotLength / 2, slotCenterY - slotThickness / 2, slotLength, slotThickness);
    }
    
    // Draw pins
    g2d.setColor(outlineMode ? theme.getOutlineColor() : PIN_COLOR);
    for (Point2D p : controlPoints) {
      g2d.fillOval((int)p.getX() - 2, (int)p.getY() - 2, 4, 4);
    }
    
    // Draw label - position outside body for readability
    Color labelColor = componentState == ComponentState.SELECTED || 
        componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED : LABEL_COLOR;
    g2d.setColor(labelColor);
    g2d.setFont(project.getFont());
    
    String label = display == Display.NAME ? getName() : 
                   display == Display.VALUE ? getValue().toString() :
                   display == Display.BOTH ? getName() + " " + getValue() : "";
    
    if (!label.isEmpty()) {
      // Position label outside the component body based on orientation
      int labelX, labelY;
      java.awt.FontMetrics fm = g2d.getFontMetrics();
      
      if (orientation == Orientation.DEFAULT || orientation == Orientation._180) {
        // Horizontal orientation - label below body
        labelX = bodyX + bodyW / 2;
        labelY = bodyY + bodyH + fm.getHeight();
      } else {
        // Vertical orientation - label to the right of body
        labelX = bodyX + bodyW + fm.stringWidth(label) / 2 + 5; // 5 pixel gap
        labelY = bodyY + bodyH / 2;
      }
      
      drawCenteredText(g2d, label, labelX, labelY);
    }
  }

  private void drawCenteredText(Graphics2D g2d, String text, int x, int y) {
    java.awt.FontMetrics fm = g2d.getFontMetrics();
    int w = fm.stringWidth(text);
    int h = fm.getHeight();
    g2d.drawString(text, x - w / 2, y + fm.getAscent() - h / 2);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    // Side profile view of PV36 trimmer
    int bodyWidth = width - 8;
    int bodyHeight = height - 10;
    int bodyX = 4;
    int bodyY = 2;
    
    // Draw blue body (side profile - rectangular)
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(bodyX, bodyY, bodyWidth, bodyHeight);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(bodyX, bodyY, bodyWidth, bodyHeight);
    
    // Draw brass adjustment screw on top left
    Color brassColor = new Color(184, 134, 11);
    int screwSize = Math.min(bodyWidth, bodyHeight) / 3;
    g2d.setColor(brassColor);
    g2d.fillOval(bodyX + 2, bodyY - screwSize / 2, screwSize, screwSize);
    
    // Draw slot in screw
    g2d.setColor(new Color(80, 80, 80));
    int slotLen = (int)(screwSize * 0.6);
    g2d.drawLine(bodyX + 2 + screwSize / 2 - slotLen / 2, bodyY, 
                 bodyX + 2 + screwSize / 2 + slotLen / 2, bodyY);
    
    // Draw three pins at bottom (light gray)
    g2d.setColor(PIN_COLOR);
    int pinSpacing = bodyWidth / 4;
    for (int i = 0; i < 3; i++) {
      int pinX = bodyX + pinSpacing * (i + 1) - 1;
      int pinY = bodyY + bodyHeight;
      g2d.fillRect(pinX, pinY, 2, 6);
    }
  }

  @EditableProperty
  public Resistance getValue() {
    return value;
  }

  public void setValue(Resistance value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
  }

  public boolean getScrewFlippedHorizontally() {
    return screwFlippedHorizontally;
  }

  public void setScrewFlippedHorizontally(boolean flipped) {
    this.screwFlippedHorizontally = flipped;
  }

  public boolean getScrewFlippedVertically() {
    return screwFlippedVertically;
  }

  public void setScrewFlippedVertically(boolean flipped) {
    this.screwFlippedVertically = flipped;
  }

  @EditableProperty
  public Display getDisplay() {
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @EditableProperty
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color color) {
    this.bodyColor = color;
  }

  @EditableProperty
  public Color getBorderColor() {
    return borderColor;
  }

  public void setBorderColor(Color color) {
    this.borderColor = color;
  }

  @Override
  public int getControlPointCount() {
    return 3;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    // Only allow moving via the first control point
    if (index == 0) {
      controlPoints[0] = new Point2D.Double(point.getX(), point.getY());
      updateControlPoints();
    }
  }

  private void updateControlPoints() {
    int pinSpacing = (int) PIN_SPACING.convertToPixels();
    double x = controlPoints[0] != null ? controlPoints[0].getX() : 0;
    double y = controlPoints[0] != null ? controlPoints[0].getY() : 0;
    
    controlPoints[0] = new Point2D.Double(x, y);
    
    // Pin arrangement depends on orientation
    // Pin 0 is always CCW, Pin 1 is Wiper, Pin 2 is CW
    switch (orientation) {
      case DEFAULT:  // Horizontal, pins go right from pin 0
        controlPoints[1] = new Point2D.Double(x + pinSpacing, y);
        controlPoints[2] = new Point2D.Double(x + 2 * pinSpacing, y);
        break;
      case _90:  // Vertical, pins go down from pin 0
        controlPoints[1] = new Point2D.Double(x, y + pinSpacing);
        controlPoints[2] = new Point2D.Double(x, y + 2 * pinSpacing);
        break;
      case _180:  // Horizontal mirrored, pins go left from pin 0
        controlPoints[1] = new Point2D.Double(x - pinSpacing, y);
        controlPoints[2] = new Point2D.Double(x - 2 * pinSpacing, y);
        break;
      case _270:  // Vertical mirrored, pins go up from pin 0
        controlPoints[1] = new Point2D.Double(x, y - pinSpacing);
        controlPoints[2] = new Point2D.Double(x, y - 2 * pinSpacing);
        break;
    }
  }

  @Override
  public String getControlPointNodeName(int index) {
    return new String[] {"CCW (1)", "Wiper (2)", "CW (3)"}[index];
  }

  @Override
  public String getName() {
    return name != null && !name.trim().isEmpty() ? name : super.getName();
  }

  @Override
  @EditableProperty(defaultable = false)
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    // First control point is sticky so component can be placed from side panel
    // But setControlPoint ensures all pins move together
    return index == 0;
  }
}
