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
package org.diylc.components.semiconductors;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Multiwatt IC", author = "Branislav Stojkovic", category = "Semiconductors",
    instanceNamePrefix = "IC", description = "Multiwatt package IC typically used for audio amplifiers of high power applications",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE,
    enableCache = true)
public class MultiwattIC extends AbstractTransparentComponent<String> implements IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = Color.gray;
  public static Color BORDER_COLOR = Color.gray.darker();
  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color LABEL_COLOR = Color.white;
  public static Color TAB_COLOR = Color.lightGray;
  public static Color TAB_BORDER_COLOR = TAB_COLOR.darker();
  public static int EDGE_RADIUS = 6;
  public static Size PIN_SIZE = new Size(0.7d, SizeUnit.mm);
  public static Size THICKNESS = new Size(5d, SizeUnit.mm);
  public static Size TAB_THICKNESS = new Size(1.6d, SizeUnit.mm);
  // Multiwatt packages use 0.1 inch (2.54mm) spacing between pins in the same row
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  // Multiwatt packages have staggered pins with 0.2 inch (5.08mm) row spacing
  public static Size ROW_SPACING = new Size(0.2d, SizeUnit.in);
  // Lead offset due to bent leads
  public static Size LEAD_OFFSET = new Size(4d, SizeUnit.mm);

  private String value = "";
  private Orientation orientation = Orientation.DEFAULT;
  private MultiwattType type = MultiwattType.MULTIWATT_11;
  private Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  protected Display display = Display.NAME;
  private Color bodyColor = BODY_COLOR;
  private Color borderColor = BORDER_COLOR;
  private Color labelColor = LABEL_COLOR;
  private Color tabColor = TAB_COLOR;
  private Color tabBorderColor = TAB_BORDER_COLOR;
  transient private Area[] body;

  public MultiwattIC() {
    super();
    updateControlPoints();
    alpha = 100;
  }

  @EditableProperty
  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @EditableProperty
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    // Reset body shape.
    body = null;
  }

  @EditableProperty(name = "Type")
  public MultiwattType getType() {
    return type;
  }

  public void setType(MultiwattType type) {
    this.type = type;
    updateControlPoints();
    // Reset body shape;
    body = null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @EditableProperty
  public Display getDisplay() {
    if (display == null) {
      display = Display.VALUE;
    }
    return display;
  }

  public void setDisplay(Display display) {
    this.display = display;
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.NEVER;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    body = null;
  }

  private void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    int pinCount = type.getPinCount();
    int pinsLeft = type.getPinsLeft();
    int pinsRight = type.getPinsRight();
    controlPoints = new Point2D.Double[pinCount];
    controlPoints[0] = firstPoint;
    double pinSpacing = PIN_SPACING.convertToPixels();
    double rowSpacing = ROW_SPACING.convertToPixels();
    // Update control points for staggered layout
    double dx1, dy1, dx2, dy2;
    
    // Left side pins (first row)
    for (int i = 0; i < pinsLeft; i++) {
      switch (orientation) {
        case DEFAULT:
          dx1 = 0;
          dy1 = i * pinSpacing;
          break;
        case _90:
          dx1 = -i * pinSpacing;
          dy1 = 0;
          break;
        case _180:
          dx1 = 0;
          dy1 = -i * pinSpacing;
          break;
        case _270:
          dx1 = i * pinSpacing;
          dy1 = 0;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i] = new Point2D.Double((int) (firstPoint.getX() + dx1), (int) (firstPoint.getY() + dy1));
    }
    
    // Right side pins (second row, staggered - offset by half pin spacing)
    for (int i = 0; i < pinsRight; i++) {
      switch (orientation) {
        case DEFAULT:
          dx2 = rowSpacing;
          dy2 = (i + 0.5) * pinSpacing; // Offset by half spacing for staggered effect
          break;
        case _90:
          dx2 = -(i + 0.5) * pinSpacing; // Offset by half spacing for staggered effect
          dy2 = rowSpacing;
          break;
        case _180:
          dx2 = -rowSpacing;
          dy2 = -(i + 0.5) * pinSpacing; // Offset by half spacing for staggered effect
          break;
        case _270:
          dx2 = (i + 0.5) * pinSpacing; // Offset by half spacing for staggered effect
          dy2 = -rowSpacing;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      controlPoints[i + pinsLeft] = new Point2D.Double((int) (firstPoint.getX() + dx2), (int) (firstPoint.getY() + dy2));
    }
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[2];
      double x = controlPoints[0].getX();
      double y = controlPoints[0].getY();
      double pinSize = PIN_SIZE.convertToPixels();
      double pinSpacing = PIN_SPACING.convertToPixels();
      double rowSpacing = ROW_SPACING.convertToPixels();
      double tabThickness = TAB_THICKNESS.convertToPixels();
      double leadOffset = LEAD_OFFSET.convertToPixels();
      int pinsLeft = type.getPinsLeft();
      int pinsRight = type.getPinsRight();
      // Calculate height accounting for staggered pins (right row offset by half spacing)
      double leftRowHeight = (pinsLeft - 1) * pinSpacing;
      double rightRowHeight = (pinsRight - 1 + 0.5) * pinSpacing; // Account for half-spacing offset
      double maxHeight = Math.max(leftRowHeight, rightRowHeight) + pinSpacing; // Add spacing for last pin
      double bodyX, bodyY, bodyWidth, bodyHeight;
      double tabX, tabY, tabWidth, tabHeight;
      
      switch (orientation) {
        case DEFAULT:
          bodyWidth = rowSpacing - pinSize;
          bodyHeight = maxHeight;
          // Offset body to the left by lead offset (bent leads)
          bodyX = x + pinSize / 2 - leadOffset;
          bodyY = y - pinSpacing / 2;
          // Tab on the left side (where there are more pins) inside the body bounds
          tabX = bodyX;
          tabY = bodyY;
          tabWidth = tabThickness;
          tabHeight = bodyHeight;
          break;
        case _90:
          bodyWidth = maxHeight;
          bodyHeight = rowSpacing - pinSize;
          // Body positioned between the two pin rows
          // Left pins extend to negative X, body starts from leftmost pin
          // For _90: left pins go left (negative X), so leftmost is at x - (pinsLeft-1)*pinSpacing
          // Offset body upward by lead offset (bent leads)
          bodyX = x - (pinsLeft - 1) * pinSpacing - pinSpacing / 2;
          bodyY = y + pinSize / 2 - leadOffset;
          // Tab on the top side (where there are more pins - left row at y) inside the body bounds
          tabX = bodyX;
          tabY = bodyY;
          tabWidth = bodyWidth;
          tabHeight = tabThickness;
          break;
        case _180:
          bodyWidth = rowSpacing - pinSize;
          bodyHeight = maxHeight;
          // Body positioned between the two pin rows
          // For _180: left pins go up (negative Y), right pins at x - rowSpacing
          // Left row is at x, right row is at x - rowSpacing
          // Pins extend upward from y, so body should start from top of pin array
          // Offset body to the right by lead offset (bent leads)
          bodyX = x - rowSpacing + pinSize / 2 + leadOffset;
          bodyY = y - (pinsLeft - 1) * pinSpacing - pinSpacing / 2;
          // Tab on the right side (where there are more pins - left row at x) inside the body bounds
          tabX = bodyX + bodyWidth - tabThickness;
          tabY = bodyY;
          tabWidth = tabThickness;
          tabHeight = bodyHeight;
          break;
        case _270:
          bodyWidth = maxHeight;
          bodyHeight = rowSpacing - pinSize;
          // Body positioned between the two pin rows
          // For _270: left pins go right (positive X), so leftmost is at x
          // Left row is at y, right row is at y - rowSpacing
          // Similar to _90 but mirrored
          // Offset body downward by lead offset (bent leads)
          bodyX = x - pinSpacing / 2;
          bodyY = y - rowSpacing + pinSize / 2 + leadOffset;
          // Tab on the bottom side (where there are more pins - left row at y) inside the body bounds
          tabX = bodyX;
          tabY = bodyY + bodyHeight - tabThickness;
          tabWidth = bodyWidth;
          tabHeight = tabThickness;
          break;
        default:
          throw new RuntimeException("Unexpected orientation: " + orientation);
      }
      body[0] = new Area(new Rectangle2D.Double(bodyX, bodyY, bodyWidth, bodyHeight));
      body[1] = new Area(new Rectangle2D.Double(tabX, tabY, tabWidth, tabHeight));
    }
    return body;
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Area mainArea = getBody()[0];

    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
    if (!outlineMode) {
      int pinSize = (int) PIN_SIZE.convertToPixels() / 2 * 2;
      Rectangle2D bodyBounds = mainArea.getBounds2D();
      double leadThickness = pinSize; // Lead width matches pin size
      
      for (Point2D point : controlPoints) {
        double pinX = point.getX();
        double pinY = point.getY();
        
        // Check if pin is inside the body
        boolean insideBody = mainArea.contains(pinX, pinY);
        
        if (insideBody) {
          // Draw pin as square inside body
          Rectangle2D rect = new Rectangle2D.Double(pinX - pinSize / 2,
              pinY - pinSize / 2, pinSize, pinSize);
          g2d.setColor(PIN_COLOR);
          drawingObserver.startTracking();
          g2d.fill(rect);
          drawingObserver.stopTracking();
          g2d.setColor(PIN_BORDER_COLOR);
          g2d.draw(rect);
        } else {
          // Draw rectangular lead from body edge to pin position
          double bodyX = bodyBounds.getX();
          double bodyY = bodyBounds.getY();
          double bodyWidth = bodyBounds.getWidth();
          double bodyHeight = bodyBounds.getHeight();
          
          // Find the closest point on body edge to the pin
          double edgeX, edgeY;
          
          // Determine which side of the body the pin is on
          if (pinX < bodyX) {
            // Pin is to the left of body
            edgeX = bodyX;
            edgeY = Math.max(bodyY, Math.min(bodyY + bodyHeight, pinY));
          } else if (pinX > bodyX + bodyWidth) {
            // Pin is to the right of body
            edgeX = bodyX + bodyWidth;
            edgeY = Math.max(bodyY, Math.min(bodyY + bodyHeight, pinY));
          } else if (pinY < bodyY) {
            // Pin is above body
            edgeX = Math.max(bodyX, Math.min(bodyX + bodyWidth, pinX));
            edgeY = bodyY;
          } else {
            // Pin is below body
            edgeX = Math.max(bodyX, Math.min(bodyX + bodyWidth, pinX));
            edgeY = bodyY + bodyHeight;
          }
          
          // Draw rectangular lead
          double leadX = Math.min(edgeX, pinX) - leadThickness / 2;
          double leadY = Math.min(edgeY, pinY) - leadThickness / 2;
          double leadWidth = Math.abs(pinX - edgeX) + leadThickness;
          double leadHeight = Math.abs(pinY - edgeY) + leadThickness;
          
          // If lead is horizontal or vertical, draw as rectangle
          if (Math.abs(pinX - edgeX) < 0.1) {
            // Vertical lead
            leadWidth = leadThickness;
            leadX = edgeX - leadThickness / 2;
          } else if (Math.abs(pinY - edgeY) < 0.1) {
            // Horizontal lead
            leadHeight = leadThickness;
            leadY = edgeY - leadThickness / 2;
          }
          
          Rectangle2D leadRect = new Rectangle2D.Double(leadX, leadY, leadWidth, leadHeight);
          g2d.setColor(PIN_COLOR);
          drawingObserver.startTracking();
          g2d.fill(leadRect);
          drawingObserver.stopTracking();
          g2d.setColor(PIN_BORDER_COLOR);
          g2d.draw(leadRect);
          
          // Draw pin at the end of the lead
          Rectangle2D pinRect = new Rectangle2D.Double(pinX - pinSize / 2,
              pinY - pinSize / 2, pinSize, pinSize);
          g2d.setColor(PIN_COLOR);
          drawingObserver.startTracking();
          g2d.fill(pinRect);
          drawingObserver.stopTracking();
          g2d.setColor(PIN_BORDER_COLOR);
          g2d.draw(pinRect);
        }
      }
    }

    drawingObserver.startTracking();
    Composite oldComposite = g2d.getComposite();
    if (alpha < MAX_ALPHA) {
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f * alpha / MAX_ALPHA));
    }
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    g2d.fill(mainArea);
    drawingObserver.stopTracking();

    // Draw heat sink tab
    Area tabArea = getBody()[1];
    Color finalTabColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalTabColor = theme.getOutlineColor();
    } else {
      finalTabColor = tabColor;
    }
    g2d.setColor(finalTabColor);
    drawingObserver.startTracking();
    g2d.fill(tabArea);
    drawingObserver.stopTracking();
    g2d.setComposite(oldComposite);

    if (!outlineMode) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.setColor(tabBorderColor);
      g2d.draw(tabArea);
    }

    Color finalBorderColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : theme.getOutlineColor();
    } else {
      finalBorderColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? SELECTION_COLOR
              : getBorderColor();
    }
    g2d.setColor(finalBorderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);
    // Draw label.
    g2d.setFont(project.getFont());
    Color finalLabelColor;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : theme.getOutlineColor();
    } else {
      finalLabelColor =
          componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING ? LABEL_COLOR_SELECTED
              : getLabelColor();
    }
    g2d.setColor(finalLabelColor);
    FontMetrics fontMetrics = g2d.getFontMetrics(g2d.getFont());
    String label = "";
    label = (getDisplay() == Display.NAME) ? getName() : getValue();
    if (getDisplay() == Display.NONE) {
      label = "";
    }
    if (getDisplay() == Display.BOTH) {
      label = getName() + "  " + (getValue() == null ? "" : getValue().toString());
    }
    Rectangle2D rect = fontMetrics.getStringBounds(label, g2d);
    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());
    // Center text horizontally and vertically
    Rectangle bounds = mainArea.getBounds();
    int x = (int) (bounds.getX() + (bounds.width - textWidth) / 2);
    int y = (int) (bounds.getY() + (bounds.height - textHeight) / 2 + fontMetrics.getAscent());
    g2d.drawString(label, x, y);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    int margin = 2;
    int pinSize = 2 * width / 32;
    int leftPinX = width / 6 + 9; // Move left pins 9px to the right (4px + 3px + 2px)
    int rightPinX = 5 * width / 6;
    int leadOffset = (int) (LEAD_OFFSET.convertToPixels() * width / 100); // Scale lead offset for icon
    int bodyX = leftPinX + pinSize - leadOffset + 2; // Offset body to the left, then move 2px to the right
    int bodyWidth = (rightPinX - bodyX - pinSize) / 2 + 2; // Thinner body, make 2px thicker
    int bodyHeight = height - 2 * margin;
    int bodyY = margin;
    
    // Calculate pin spacing to use all vertical space
    // Use same spacing for both rows, with front row offset by half spacing
    int backPins = 4;
    int frontPins = 3;
    // Use the spacing that fits the back row (more pins)
    int pinSpacing = (bodyHeight - pinSize) / (backPins - 1);
    int backStartY = margin + pinSize / 2 + 1; // Move pins 1px down
    // Front pins start with half spacing offset for staggered effect
    int frontStartY = margin + pinSize / 2 + pinSpacing / 2 + 1; // Move pins 1px down
    
    // Draw thin body between pin rows
    g2d.setColor(BODY_COLOR);
    g2d.fillRect(bodyX, bodyY, bodyWidth, bodyHeight);
    g2d.setColor(BORDER_COLOR);
    g2d.drawRect(bodyX, bodyY, bodyWidth, bodyHeight);
    
    // Draw heat sink tab on left side (where there are more pins)
    int tabWidth = pinSize * 2;
    g2d.setColor(TAB_COLOR);
    g2d.fillRect(bodyX, bodyY, tabWidth, bodyHeight);
    g2d.setColor(TAB_BORDER_COLOR);
    g2d.drawRect(bodyX, bodyY, tabWidth, bodyHeight);
    
    // Draw 4 pins in the back (left row, more pins) as squares - inside body
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < backPins; i++) {
      int pinY = backStartY + i * pinSpacing - pinSize / 2;
      g2d.fillRect(leftPinX - pinSize, pinY, pinSize, pinSize);
      g2d.setColor(PIN_BORDER_COLOR);
      g2d.drawRect(leftPinX - pinSize, pinY, pinSize, pinSize);
      g2d.setColor(PIN_COLOR);
    }
    
    // Draw 3 pins in the front (right row, fewer pins) with leads going left to body
    // Use same spacing as back row, just offset by half spacing
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < frontPins; i++) {
      int pinY = frontStartY + i * pinSpacing;
      int pinX = rightPinX;
      int bodyRightEdge = bodyX + bodyWidth;
      
      // Draw lead from body edge to pin (horizontal lead going left)
      if (pinX > bodyRightEdge) {
        int leadX = bodyRightEdge;
        int leadY = pinY - pinSize / 2;
        int leadWidth = pinX - bodyRightEdge;
        int leadHeight = pinSize;
        
        g2d.fillRect(leadX, leadY, leadWidth, leadHeight);
        g2d.setColor(PIN_BORDER_COLOR);
        g2d.drawRect(leadX, leadY, leadWidth, leadHeight);
        g2d.setColor(PIN_COLOR);
      }
      
      // Draw pin at the end of the lead
      g2d.fillRect(pinX - pinSize / 2, pinY - pinSize / 2, pinSize, pinSize);
      g2d.setColor(PIN_BORDER_COLOR);
      g2d.drawRect(pinX - pinSize / 2, pinY - pinSize / 2, pinSize, pinSize);
      g2d.setColor(PIN_COLOR);
    }
  }

  @EditableProperty(name = "Body")
  public Color getBodyColor() {
    if (bodyColor == null) {
      bodyColor = BODY_COLOR;
    }
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
  }

  @EditableProperty(name = "Border")
  public Color getBorderColor() {
    if (borderColor == null) {
      borderColor = BORDER_COLOR;
    }
    return borderColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  @EditableProperty(name = "Label")
  public Color getLabelColor() {
    if (labelColor == null) {
      labelColor = LABEL_COLOR;
    }
    return labelColor;
  }

  public void setLabelColor(Color labelColor) {
    this.labelColor = labelColor;
  }

  @EditableProperty(name = "Tab")
  public Color getTabColor() {
    if (tabColor == null) {
      tabColor = TAB_COLOR;
    }
    return tabColor;
  }

  public void setTabColor(Color tabColor) {
    this.tabColor = tabColor;
  }

  @EditableProperty(name = "Tab Border")
  public Color getTabBorderColor() {
    if (tabBorderColor == null) {
      tabBorderColor = TAB_BORDER_COLOR;
    }
    return tabBorderColor;
  }

  public void setTabBorderColor(Color tabBorderColor) {
    this.tabBorderColor = tabBorderColor;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    int margin = 50;
    for (int i = 0; i < getControlPointCount(); i++) {
      Point2D p = getControlPoint(i);
      if (p.getX() < minX)
        minX = p.getX();
      if (p.getX() > maxX)
        maxX = p.getX();
      if (p.getY() < minY)
        minY = p.getY();
      if (p.getY() > maxY)
        maxY = p.getY();
    }
    
    return new Rectangle2D.Double(minX - margin, minY - margin, maxX - minX + 2 * margin, maxY - minY + 2 * margin);
  }

  public static enum MultiwattType {
    // Multiwatt-11: 6 pins on left, 5 pins on right (staggered)
    MULTIWATT_11(11, 6, 5),
    // Multiwatt-15: 8 pins on left, 7 pins on right (staggered)
    MULTIWATT_15(15, 8, 7);

    private final int pinCount;
    private final int pinsLeft;
    private final int pinsRight;

    MultiwattType(int pinCount, int pinsLeft, int pinsRight) {
      this.pinCount = pinCount;
      this.pinsLeft = pinsLeft;
      this.pinsRight = pinsRight;
    }

    public int getPinCount() {
      return pinCount;
    }

    public int getPinsLeft() {
      return pinsLeft;
    }

    public int getPinsRight() {
      return pinsRight;
    }

    @Override
    public String toString() {
      return "Multiwatt " + pinCount;
    }
  }
}

