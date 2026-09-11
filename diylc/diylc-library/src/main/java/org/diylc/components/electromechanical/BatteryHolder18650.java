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
 * along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.electromechanical;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "18650 Battery Holder", category = "Electro-Mechanical",
    author = "Branislav Stojkovic", description = "PCB-mount 18650 Li-ion Battery Holder (1S or 2S)",
    instanceNamePrefix = "BTR", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class BatteryHolder18650 extends AbstractTransparentComponent<String> {

  private static final long serialVersionUID = 1L;

  public enum CellCount {
    _1S("1S (3.7V) — Single Cell", 1, 77.0, 21.0),
    _2S("2S (7.4V) — Two Cells", 2, 77.0, 42.0);

    private final String label;
    private final int cells;
    private final double lengthMm;
    private final double widthMm;

    CellCount(String label, int cells, double lengthMm, double widthMm) {
      this.label = label;
      this.cells = cells;
      this.lengthMm = lengthMm;
      this.widthMm = widthMm;
    }

    @Override public String toString() { return label; }
    public int getCells() { return cells; }
    public double getLengthMm() { return lengthMm; }
    public double getWidthMm() { return widthMm; }
  }

  public static Color HOLDER_BODY = Color.decode("#2C2C2C");
  public static Color HOLDER_BODY_LIGHT = Color.decode("#3C3C3C");
  public static Color SPRING_COLOR = Color.decode("#C0C0C0");
  public static Color SPRING_BORDER = Color.decode("#909090");
  public static Color PLATE_COLOR = Color.decode("#D0D0D0");
  public static Color CELL_CAVITY = Color.decode("#1A1A1A");

  private static final Font LABEL_FONT = new Font("SansSerif", Font.BOLD, 12);

  private String value = "";
  private CellCount cellCount = CellCount._1S;
  private Orientation orientation = Orientation.DEFAULT;

  // Two control points: Point 0 = + (left), Point 1 = - (right)
  private Point2D[] controlPoints = new Point2D[] {
      new Point2D.Double(0, 0),
      new Point2D.Double(0, 0)
  };

  public BatteryHolder18650() {
    super();
    updateControlPoints();
  }

  @EditableProperty(name = "Cell Count")
  public CellCount getCellCount() {
    return cellCount;
  }

  public void setCellCount(CellCount cellCount) {
    this.cellCount = cellCount;
    updateControlPoints();
  }

  @EditableProperty(name = "Orientation")
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
  }

  private void updateControlPoints() {
    double length = new Size(cellCount.getLengthMm(), SizeUnit.mm).convertToPixels();
    Point2D p0 = controlPoints[0];

    double theta = orientation.toRadians();
    // + at p0 (left), - at p0 + (length, 0) rotated (right)
    double dx = Math.cos(theta) * length;
    double dy = Math.sin(theta) * length;
    controlPoints[1] = new Point2D.Double(p0.getX() + dx, p0.getY() + dy);
  }

  @Override
  public int getControlPointCount() { return 2; }

  @Override
  public Point2D getControlPoint(int index) { return controlPoints[index]; }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    if (index == 0) {
      updateControlPoints();
    }
  }

  @Override
  public boolean isControlPointSticky(int index) { return true; }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) { return false; }

  @Override
  public boolean canControlPointOverlap(int index) { return false; }

  @Override
  public String getControlPointNodeName(int index) {
    return index == 0 ? "+ (VCC)" : "- (GND)";
  }

  @Override
  public String getValue() { return value; }

  @Override
  public void setValue(String value) { this.value = value; }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {

    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();
    double length = new Size(cellCount.getLengthMm(), SizeUnit.mm).convertToPixels();
    double width = new Size(cellCount.getWidthMm(), SizeUnit.mm).convertToPixels();
    double cellD = new Size(18.5d, SizeUnit.mm).convertToPixels();

    AffineTransform oldTx = g2d.getTransform();
    if (orientation != Orientation.DEFAULT) {
      g2d.rotate(orientation.toRadians(), x, y);
    }

    Composite oldComposite = applyAlpha(g2d, componentState);

    // Border color
    Color borderColor;
    if (outlineMode) {
      Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      borderColor = (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING)
          ? SELECTION_COLOR : (theme != null ? theme.getOutlineColor() : Color.BLACK);
    } else {
      borderColor = (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING)
          ? SELECTION_COLOR : HOLDER_BODY.darker();
    }

    double bodyY = y - width / 2.0;

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : HOLDER_BODY);
    g2d.fill(new RoundRectangle2D.Double(x, bodyY, length, width, 10, 10));
    drawingObserver.stopTracking();

    g2d.setColor(borderColor);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(new RoundRectangle2D.Double(x, bodyY, length, width, 10, 10));

    if (!outlineMode) {
      int cells = cellCount.getCells();
      double cellWidth = (width - 8.0) / cells;

      for (int i = 0; i < cells; i++) {
        double cavityX = x + 6;
        double cavityY = bodyY + 4 + i * cellWidth;
        double cavityW = length - 12;
        double cavityH = cellWidth - 4;

        // Cell cavity (dark inset)
        g2d.setColor(CELL_CAVITY);
        g2d.fill(new RoundRectangle2D.Double(cavityX, cavityY, cavityW, cavityH, 6, 6));
        g2d.setColor(HOLDER_BODY_LIGHT);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
        g2d.draw(new RoundRectangle2D.Double(cavityX, cavityY, cavityW, cavityH, 6, 6));

        // Positive flat plate button contact on left (x)
        double plateX = x + 6;
        double plateW = 6;
        double plateH = Math.min(14, cellH(cellD));
        double contactY = cavityY + cavityH / 2.0;
        g2d.setColor(PLATE_COLOR);
        g2d.fill(new Rectangle2D.Double(plateX, contactY - plateH / 2.0, plateW, plateH));
        g2d.setColor(SPRING_BORDER);
        g2d.draw(new Rectangle2D.Double(plateX, contactY - plateH / 2.0, plateW, plateH));

        // Negative spring contact on right (x + length)
        double springX = x + length - 16;
        double springW = 10;
        double springH = Math.min(14, cellH(cellD));
        g2d.setColor(SPRING_COLOR);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
        g2d.draw(new Rectangle2D.Double(springX, contactY - springH / 2.0, springW, springH));
        g2d.draw(new Rectangle2D.Double(springX + 3, contactY - springH / 2.0 + 2, springW - 6, springH - 4));

        // Polarity markings (+ on left, - on right)
        g2d.setColor(Color.WHITE);
        g2d.setFont(LABEL_FONT);
        StringUtils.drawCenteredText(g2d, "+", x + 20, contactY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        StringUtils.drawCenteredText(g2d, "-", x + length - 20, contactY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        // 18650 text in cavity
        g2d.setColor(Color.decode("#555555"));
        StringUtils.drawCenteredText(g2d, "18650", x + length / 2.0, contactY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }

      // Series connector bar between cells for 2S
      if (cells > 1) {
        g2d.setColor(SPRING_COLOR);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
        double midY = bodyY + width / 2.0;
        g2d.draw(new Rectangle2D.Double(x + length - 10, midY - 6, 6, 12));
      }
    }

    g2d.setTransform(oldTx);

    // Draw control point solder tabs (Point 0 = + left, Point 1 = - right)
    if (!outlineMode) {
      for (Point2D cp : controlPoints) {
        g2d.setColor(PLATE_COLOR);
        g2d.fill(new Ellipse2D.Double(cp.getX() - 5, cp.getY() - 5, 10, 10));
        g2d.setColor(SPRING_BORDER);
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
        g2d.draw(new Ellipse2D.Double(cp.getX() - 5, cp.getY() - 5, 10, 10));
        // Inner hole
        g2d.setColor(Constants.CANVAS_COLOR);
        g2d.fill(new Ellipse2D.Double(cp.getX() - 2, cp.getY() - 2, 4, 4));
      }
    }

    g2d.setComposite(oldComposite);
  }

  private double cellH(double cellD) {
    return cellD * 0.45;
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    // Dark holder body
    g2d.setColor(HOLDER_BODY);
    g2d.fill(new RoundRectangle2D.Double(2, height / 2.0 - 8, width - 4, 16, 4, 4));
    g2d.setColor(HOLDER_BODY.brighter());
    g2d.draw(new RoundRectangle2D.Double(2, height / 2.0 - 8, width - 4, 16, 4, 4));

    // Cell cavity
    g2d.setColor(CELL_CAVITY);
    g2d.fill(new RoundRectangle2D.Double(5, height / 2.0 - 6, width - 10, 12, 3, 3));

    // Plate on left (+), spring on right (-)
    g2d.setColor(PLATE_COLOR);
    g2d.fillRect(5, height / 2 - 3, 3, 6);
    g2d.setColor(SPRING_COLOR);
    g2d.fillRect(width - 9, height / 2 - 3, 4, 6);

    // Polarity
    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 7));
    StringUtils.drawCenteredText(g2d, "+", 12, height / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
    StringUtils.drawCenteredText(g2d, "-", width - 13, height / 2.0, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }

  @Override
  public Rectangle2D getCachingBounds() {
    double minX = Math.min(controlPoints[0].getX(), controlPoints[1].getX());
    double minY = Math.min(controlPoints[0].getY(), controlPoints[1].getY());
    double maxX = Math.max(controlPoints[0].getX(), controlPoints[1].getX());
    double maxY = Math.max(controlPoints[0].getY(), controlPoints[1].getY());
    double width = new Size(cellCount.getWidthMm(), SizeUnit.mm).convertToPixels();
    int margin = 50;
    return new Rectangle2D.Double(minX - margin, minY - width / 2.0 - margin,
        (maxX - minX) + 2 * margin, (maxY - minY) + width + 2 * margin);
  }
}
