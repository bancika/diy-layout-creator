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
package org.diylc.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
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
import org.diylc.core.ComponentState;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.netlist.Node;
import org.diylc.utils.Constants;

/**
 * Base abstract class for Maker-focused boards, microcontrollers, sensors, displays, and breakout modules.
 */
public abstract class AbstractMakerBoard extends AbstractTransparentComponent<Void> {

  private static final long serialVersionUID = 1L;

  public static final Color ARDUINO_TEAL = Color.decode("#00878F");
  public static final Color ARDUINO_BLUE = Color.decode("#015687");

  public static Color PIN_COLOR = Color.decode("#00B2EE");
  public static Color PIN_BORDER_COLOR = PIN_COLOR.darker();
  public static Color HEADER_BODY_COLOR = Color.decode("#222222");
  public static Color HEADER_BORDER_COLOR = Color.decode("#111111");
  public static Color MOUNTING_HOLE_COLOR = Color.decode("#B8860B");
  public static Color MOUNTING_HOLE_BORDER = Color.decode("#8B6508");
  public static Color IC_BODY_COLOR = Color.decode("#1A1A1A");
  public static Color IC_TEXT_COLOR = Color.decode("#CCCCCC");
  public static Color SCREW_TERMINAL_COLOR = Color.decode("#90AB66");
  public static Color SCREW_TERMINAL_BORDER = Color.decode("#90AB66").darker();
  public static Color SCREW_CIRCLE_COLOR = LIGHT_METAL_COLOR;
  public static Color METAL_SHIELD_COLOR = Color.decode("#C0C0C0");
  public static Color METAL_SHIELD_BORDER = Color.decode("#909090");
  public static Color USB_METAL_COLOR = Color.decode("#D3D3D3");
  public static Color IC_BORDER_COLOR = Color.decode("#333333");
  public static Color PIN_MARKER_COLOR = Color.decode("#555555");
  public static Color METAL_LABEL_COLOR = Color.decode("#555555");
  public static Color ANTENNA_COLOR = Color.decode("#DAA520");
  public static Color ANTENNA_BG_COLOR = Color.decode("#1E1E1E");
  public static Color PAD_COLOR = Color.decode("#DAA520");
  public static Color CONNECTOR_PLASTIC_COLOR = Color.decode("#F5F5DC");
  public static Color CONNECTOR_PLASTIC_BORDER = Color.decode("#333333");
  public static Color BUTTON_BODY_COLOR = Color.decode("#383838");
  public static Color BUTTON_BORDER_COLOR = Color.decode("#666666");
  public static Color BUTTON_ACTUATOR_COLOR = Color.decode("#A0A0A0");

  public static Size PIN_SIZE = new Size(0.04d, SizeUnit.in);
  public static Size PIN_SPACING = new Size(0.1d, SizeUnit.in);
  public static Size PAD_SIZE = new Size(0.065d, SizeUnit.in);
  public static Size HOLE_SIZE = new Size(0.035d, SizeUnit.in);
  public static Size NOTCH_SIZE = new Size(0.9d, SizeUnit.mm);
  public static Size ANTENNA_WIDTH = new Size(15.0d, SizeUnit.mm);
  public static Size ANTENNA_LENGTH = new Size(7.0d, SizeUnit.mm);
  public static Size BUTTON_WIDTH = new Size(3.5d, SizeUnit.mm);
  public static Size BUTTON_LENGTH = new Size(3.0d, SizeUnit.mm);

  // Standard USB Port Dimensions
  public static Size USB_MICRO_WIDTH = new Size(7.5d, SizeUnit.mm);
  public static Size USB_MICRO_LENGTH = new Size(5.6d, SizeUnit.mm);
  public static Size USB_MICRO_OVERHANG = new Size(0.5d, SizeUnit.mm);

  public static Size USB_C_WIDTH = new Size(8.94d, SizeUnit.mm);
  public static Size USB_C_LENGTH = new Size(7.5d, SizeUnit.mm);
  public static Size USB_C_OVERHANG = new Size(0.5d, SizeUnit.mm);

  public static Size USB_A_WIDTH = new Size(14.5d, SizeUnit.mm);
  public static Size USB_A_LENGTH = new Size(14.0d, SizeUnit.mm);
  public static Size USB_A_DUAL_LENGTH = new Size(17.5d, SizeUnit.mm);

  public static Size USB_B_WIDTH = new Size(0.45d, SizeUnit.in);
  public static Size USB_B_LENGTH = new Size(0.51d, SizeUnit.in);
  public static Size USB_B_OVERHANG = new Size(0.14d, SizeUnit.in);

  public static Size USB_MINI_WIDTH = new Size(0.30d, SizeUnit.in);
  public static Size USB_MINI_LENGTH = new Size(0.36d, SizeUnit.in);
  public static Size USB_MINI_OVERHANG = new Size(0.05d, SizeUnit.in);

  public enum UsbPortType {
    MICRO,
    TYPE_C,
    TYPE_A,
    TYPE_B,
    MINI
  }

  public static Font SILK_FONT_SMALL = new Font("SansSerif", Font.PLAIN, 10);
  public static Font SILK_FONT = new Font("SansSerif", Font.BOLD, 11);
  public static Font SILK_FONT_LARGE = new Font("SansSerif", Font.BOLD, 13);
  public static Size PIN_LABEL_OFFSET = new Size(1.6d, SizeUnit.mm);
  // diagonal labels are anchored by both coordinates at once, so they sit closer to the pad than
  // the straight ones without touching it
  public static Size PIN_LABEL_CORNER_OFFSET = new Size(1.0d, SizeUnit.mm);
  public static Size PIN_ROW_LABEL_OFFSET = new Size(2.0d, SizeUnit.mm);
  public static Font PIN_FONT = new Font("SansSerif", Font.PLAIN, 8);
  // labels along a row run across the board rather than down a column of pins, so they can afford
  // a size the tightly stacked column labels cannot
  public static Font PIN_ROW_FONT = new Font("SansSerif", Font.PLAIN, 9);

  protected Orientation orientation = Orientation.DEFAULT;
  protected Point2D[] controlPoints = new Point2D[] {new Point2D.Double(0, 0)};
  protected Color bodyColor = Color.decode("#006699");

  public AbstractMakerBoard() {
    super();
  }

  @EditableProperty(name = "Orientation")
  public Orientation getOrientation() {
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "Board Color")
  public Color getBodyColor() {
    return bodyColor;
  }

  public void setBodyColor(Color bodyColor) {
    this.bodyColor = bodyColor;
    invalidateCache();
  }

  @Override
  public Point2D getControlPoint(int index) {
    return controlPoints[index];
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    controlPoints[index].setLocation(point);
    invalidateCache();
  }

  @Override
  public int getControlPointCount() {
    return controlPoints.length;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return true;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    return false;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}

  protected abstract void updateControlPoints();

  protected void invalidateCache() {}

  /**
   * Helper to rotate an array of relative point offsets around the origin (first point) by orientation.
   */
  protected void rotatePoints(Point2D firstPoint, double[][] relativeOffsets) {
    controlPoints = new Point2D[relativeOffsets.length];
    controlPoints[0] = firstPoint;
    double theta = orientation.toRadians();
    AffineTransform tx = AffineTransform.getRotateInstance(theta, firstPoint.getX(), firstPoint.getY());
    for (int i = 1; i < relativeOffsets.length; i++) {
      Point2D p = new Point2D.Double(firstPoint.getX() + relativeOffsets[i][0], firstPoint.getY() + relativeOffsets[i][1]);
      tx.transform(p, p);
      controlPoints[i] = p;
    }
  }

  /**
   * Helper to draw standard pin header pins at given control point indices.
   */
  protected void drawPinHeader(Graphics2D g2d, int startIndex, int count, boolean outlineMode,
      IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    int pinPx = (int) Math.round(PIN_SIZE.convertToPixels());
    drawingObserver.startTrackingContinuityArea(true);
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      g2d.setColor(HEADER_BODY_COLOR);
      g2d.fill(new Rectangle2D.Double(p.getX() - pinPx - 1, p.getY() - pinPx - 1, (pinPx + 1) * 2, (pinPx + 1) * 2));
      g2d.setColor(PIN_COLOR);
      g2d.fill(new Rectangle2D.Double(p.getX() - pinPx / 2.0, p.getY() - pinPx / 2.0, pinPx, pinPx));
    }
    drawingObserver.stopTrackingContinuityArea();

    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      g2d.setColor(PIN_BORDER_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(new Rectangle2D.Double(p.getX() - pinPx / 2.0, p.getY() - pinPx / 2.0, pinPx, pinPx));
    }
  }

  /**
   * Helper to format a pin name for silkscreen display (e.g. "3V3_1" -> "3V3", "A0 (ADC0)" -> "A0").
   *
   * <p>Stripping the parenthesized annotation is the same operation the netlist performs, so it is
   * delegated to {@link Node#sanitizeNodeName(String)} rather than duplicated. The silkscreen goes
   * one step further and also drops the trailing disambiguator, because a board prints "GND" on
   * every ground pin even though the netlist has to tell "GND_1" from "GND_2".
   */
  public static String getDisplayPinLabel(String name) {
    String sanitized = Node.sanitizeNodeName(name);
    if (sanitized == null) {
      return "";
    }
    int underscoreIdx = sanitized.indexOf('_');
    return (underscoreIdx == -1 ? sanitized : sanitized.substring(0, underscoreIdx)).trim();
  }

  /**
   * Silkscreen text printed next to a pin. Defaults to the node name with its annotation and
   * disambiguator stripped, which is right whenever the node name is the silkscreen name plus
   * extra detail. A board whose silkscreen genuinely differs from its node names -- the ESP
   * DevKits print bare GPIO numbers while their nodes carry the full function list -- overrides
   * this to return its own silkscreen label.
   */
  protected String getSilkPinLabel(int index) {
    return getDisplayPinLabel(getControlPointNodeName(index));
  }

  /**
   * Helper to draw rotated control point / pin names next to the pins for dual-row DIP/header boards.
   *
   * @param g2d Graphics2D context (already transformed for board orientation)
   * @param x Unrotated top-left pin X coordinate (P0.getX())
   * @param y Unrotated top-left pin Y coordinate (P0.getY())
   * @param offsets Array of [x, y] relative offsets for all control points
   * @param silkColor Silkscreen text color
   */
  protected void drawPinLabels(Graphics2D g2d, double x, double y, double[][] offsets, Color silkColor) {
    if (offsets == null || offsets.length == 0) return;
    drawPinLabels(g2d, x, y, offsets, offsets.length / 2, silkColor);
  }

  /**
   * Variant for boards whose two pin rows do not take up the whole control point array, such as a
   * board that carries an ICSP block or a connector after them. Control points past the two rows
   * are left unlabelled.
   *
   * @param g2d Graphics2D context (already transformed for board orientation)
   * @param x Unrotated top-left pin X coordinate (P0.getX())
   * @param y Unrotated top-left pin Y coordinate (P0.getY())
   * @param offsets Array of [x, y] relative offsets for all control points
   * @param pinsPerRow Number of pins in each of the two rows
   * @param silkColor Silkscreen text color
   */
  protected void drawPinLabels(Graphics2D g2d, double x, double y, double[][] offsets,
      int pinsPerRow, Color silkColor) {
    if (offsets == null || offsets.length == 0) return;
    double labelOffset = PIN_LABEL_OFFSET.convertToPixels();

    g2d.setColor(silkColor);
    g2d.setFont(PIN_FONT);

    for (int i = 0; i < 2 * pinsPerRow && i < offsets.length; i++) {
      String label = getSilkPinLabel(i);
      if (label == null || label.isEmpty()) {
        continue;
      }
      boolean leftRow = i < pinsPerRow;
      double pinX = x + offsets[i][0];
      double pinY = y + offsets[i][1];
      double textX = leftRow ? (pinX + labelOffset) : (pinX - labelOffset);
      double textY = pinY;

      // the two rows are mirrored so that each label reads away from the pin it belongs to, the
      // way a board prints them
      AffineTransform oldLabelTx = g2d.getTransform();
      g2d.rotate(leftRow ? Math.PI / 2 : -Math.PI / 2, textX, textY);
      StringUtils.drawCenteredText(g2d, label, textX, textY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setTransform(oldLabelTx);
    }
  }

  /**
   * Variant for boards too crowded for labels that stand square to their row. Each label is turned
   * 45 degrees and tucked into the corner of its pad on the side the row reads towards - the top
   * right corner for the left row and the top left corner for the right one - which is the trick
   * the Teensy 4.1 uses to fit its pad names between two rows a tenth of an inch apart.
   *
   * @param g2d Graphics2D context (already transformed for board orientation)
   * @param x Unrotated top-left pin X coordinate (P0.getX())
   * @param y Unrotated top-left pin Y coordinate (P0.getY())
   * @param offsets Array of [x, y] relative offsets for all control points
   * @param pinsPerRow Number of pins in each of the two rows
   * @param silkColor Silkscreen text color
   */
  protected void drawDiagonalPinLabels(Graphics2D g2d, double x, double y, double[][] offsets,
      int pinsPerRow, Color silkColor) {
    if (offsets == null || offsets.length == 0) return;
    double cornerOffset = PIN_LABEL_CORNER_OFFSET.convertToPixels();
    double straightOffset = PIN_LABEL_OFFSET.convertToPixels();

    g2d.setColor(silkColor);
    g2d.setFont(PIN_FONT);

    for (int i = 0; i < 2 * pinsPerRow && i < offsets.length; i++) {
      String label = getSilkPinLabel(i);
      if (label == null || label.isEmpty()) {
        continue;
      }
      boolean leftRow = i < pinsPerRow;
      // the pad that starts each row has the board edge above it rather than another pad, so it
      // keeps the square on label the rest of the row has no room for
      boolean square = i == 0 || i == pinsPerRow;
      double offset = square ? straightOffset : cornerOffset;
      double textX = x + offsets[i][0] + (leftRow ? offset : -offset);
      double textY = y + offsets[i][1] - (square ? 0 : cornerOffset);

      // the two rows slant away from each other so that each label runs along the diagonal gap
      // between its own pad and the one above it
      AffineTransform oldLabelTx = g2d.getTransform();
      g2d.rotate((square ? Math.PI / 2 : Math.PI / 4) * (leftRow ? 1 : -1), textX, textY);
      StringUtils.drawCenteredText(g2d, label, textX, textY, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      g2d.setTransform(oldLabelTx);
    }
  }

  /**
   * Helper to draw rotated pin names next to a horizontal row of pins, the way Arduino-style boards
   * print their headers. Labels read top to bottom, are centered on the pin they belong to and
   * start {@link #PIN_ROW_LABEL_OFFSET} away from it, growing away from the row.
   *
   * @param g2d Graphics2D context (already transformed for board orientation)
   * @param x Unrotated first control point X coordinate (P0.getX())
   * @param y Unrotated first control point Y coordinate (P0.getY())
   * @param offsets Array of [x, y] relative offsets for all control points
   * @param startIndex Index of the first pin of the row
   * @param count Number of pins in the row
   * @param below True to print the labels below the row, false to print them above it
   * @param silkColor Silkscreen text color
   */
  protected void drawRowPinLabels(Graphics2D g2d, double x, double y, double[][] offsets,
      int startIndex, int count, boolean below, Color silkColor) {
    if (offsets == null || offsets.length == 0) return;
    double labelOffset = PIN_ROW_LABEL_OFFSET.convertToPixels();

    g2d.setColor(silkColor);
    g2d.setFont(PIN_ROW_FONT);

    for (int i = startIndex; i < startIndex + count && i < offsets.length; i++) {
      String label = getSilkPinLabel(i);
      if (label == null || label.isEmpty()) {
        continue;
      }
      double pinX = x + offsets[i][0];
      double pinY = y + offsets[i][1];

      // the quarter turn maps the text advance direction onto -y, so a label above the row is
      // anchored by its first character and one below the row by its last, which keeps both of
      // them clear of the pin by the same gap
      AffineTransform oldLabelTx = g2d.getTransform();
      g2d.rotate(-Math.PI / 2, pinX, pinY);
      StringUtils.drawCenteredText(g2d, label, pinX + (below ? -labelOffset : labelOffset), pinY,
          below ? HorizontalAlignment.RIGHT : HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      g2d.setTransform(oldLabelTx);
    }
  }

  /**
   * Helper to draw a complete PCB terminal block (green body + inner compartment lines + screw lugs)
   * that matches the visuals of the PCBTerminalBlock component.
   *
   * @param g2d Graphics2D context
   * @param startIndex First control point index
   * @param count Number of positions in this terminal block
   * @param isHorizontal true if terminal block runs horizontally (along X), false if vertically (along Y)
   * @param wireEntryOffset Direction/offset from pins to the wire entry edge (positive or negative)
   * @param blockDepth Total depth of the green body (e.g. 50.0px for 5.08mm blocks, or 35.0px for compact)
   * @param outlineMode Outline mode flag
   * @param drawingObserver Observer
   */
  protected void drawTerminalBlock(Graphics2D g2d, int startIndex, int count, boolean isHorizontal,
      double wireEntryOffset, double blockDepth, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    if (count <= 0 || startIndex < 0 || startIndex >= controlPoints.length) return;

    Point2D p0 = controlPoints[startIndex];
    Point2D pLast = controlPoints[Math.min(startIndex + count - 1, controlPoints.length - 1)];

    double pitchPx = count > 1 ?
        (isHorizontal ? Math.abs(pLast.getX() - p0.getX()) / (count - 1) : Math.abs(pLast.getY() - p0.getY()) / (count - 1))
        : new Size(5.08d, SizeUnit.mm).convertToPixels();
    if (pitchPx <= 0) {
      pitchPx = new Size(5.08d, SizeUnit.mm).convertToPixels();
    }

    double minX = Math.min(p0.getX(), pLast.getX());
    double minY = Math.min(p0.getY(), pLast.getY());
    double maxX = Math.max(p0.getX(), pLast.getX());
    double maxY = Math.max(p0.getY(), pLast.getY());

    Rectangle2D outerBody;
    Rectangle2D innerCompartment;

    if (isHorizontal) {
      double bx = minX - pitchPx / 2.0;
      double bw = (maxX - minX) + pitchPx;
      double by = wireEntryOffset >= 0 ? minY - (blockDepth - pitchPx) / 2.0 : minY - (blockDepth - pitchPx / 2.0);
      double bh = blockDepth;
      outerBody = new Rectangle2D.Double(bx, by, bw, bh);
      innerCompartment = new Rectangle2D.Double(bx, minY - pitchPx / 2.0, bw, pitchPx);
    } else {
      double by = minY - pitchPx / 2.0;
      double bh = (maxY - minY) + pitchPx;
      double bx = wireEntryOffset >= 0 ? minX - (blockDepth - pitchPx) / 2.0 : minX - (blockDepth - pitchPx / 2.0);
      double bw = blockDepth;
      outerBody = new Rectangle2D.Double(bx, by, bw, bh);
      innerCompartment = new Rectangle2D.Double(minX - pitchPx / 2.0, by, pitchPx, bh);
    }

    // Draw green body and compartment
    g2d.setColor(SCREW_TERMINAL_COLOR);
    g2d.fill(outerBody);
    g2d.setColor(SCREW_TERMINAL_BORDER);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(innerCompartment);
    g2d.draw(outerBody);

    // Draw screw contacts
    drawScrewTerminals(g2d, startIndex, count, pitchPx, outlineMode, drawingObserver);
  }

  /**
   * Helper to draw screw terminal block contacts at given control points.
   * Visuals match the PCBTerminalBlock component: light metal screw circles with diagonal slot.
   */
  protected void drawScrewTerminals(Graphics2D g2d, int startIndex, int count, double pitchPx, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    double effectivePitch = pitchPx > 0 ? pitchPx : PIN_SPACING.convertToPixels();
    int circleDiameter = getClosestOdd((int) (effectivePitch * 3d / 5));

    drawingObserver.startTrackingContinuityArea(true);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2f));
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      // Light metal screw head circle
      g2d.setColor(SCREW_CIRCLE_COLOR);
      g2d.fillOval((int) Math.round(p.getX() - circleDiameter / 2.0), (int) Math.round(p.getY() - circleDiameter / 2.0),
          circleDiameter, circleDiameter);
      // Diagonal screw slot line
      g2d.setColor(SCREW_CIRCLE_COLOR.darker());
      g2d.drawLine((int) (p.getX() + Math.cos(Math.PI / 4) * circleDiameter / 2.0),
          (int) (p.getY() + Math.sin(Math.PI / 4) * circleDiameter / 2.0),
          (int) (p.getX() + Math.cos(5 * Math.PI / 4) * circleDiameter / 2.0),
          (int) (p.getY() + Math.sin(5 * Math.PI / 4) * circleDiameter / 2.0));
    }
    drawingObserver.stopTrackingContinuityArea();
  }

  /**
   * Helper to draw rectangular tinned solder pads with drill holes at given control points.
   */
  protected void drawSolderPads(Graphics2D g2d, int startIndex, int count, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    double padW = 22.0;
    double padH = 16.0;
    double holeD = 7.0;

    drawingObserver.startTrackingContinuityArea(true);
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      RoundRectangle2D pad = new RoundRectangle2D.Double(p.getX() - padW / 2.0, p.getY() - padH / 2.0, padW, padH, 2, 2);
      g2d.setColor(LIGHT_METAL_COLOR);
      g2d.fill(pad);
      g2d.setColor(LIGHT_METAL_COLOR.darker());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(pad);

      // Central through-hole / drill hole
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(p.getX() - holeD / 2.0, p.getY() - holeD / 2.0, holeD, holeD));
      g2d.setColor(Color.DARK_GRAY);
      g2d.draw(new Ellipse2D.Double(p.getX() - holeD / 2.0, p.getY() - holeD / 2.0, holeD, holeD));
    }
    drawingObserver.stopTrackingContinuityArea();
  }

  /**
   * Helper to draw standard PCB through-hole solder pads (gold/copper pads with drill holes, square for Pin 1).
   *
   * @param g2d Graphics2D context
   * @param startIndex First control point index
   * @param count Number of pads to draw
   * @param squarePin1 If true, the first pad (index == startIndex) is drawn as a square pad
   * @param outlineMode Outline mode flag
   * @param drawingObserver Observer
   */
  protected void drawPcbSolderPads(Graphics2D g2d, int startIndex, int count, boolean squarePin1, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;
    int diameter = getClosestOdd((int) Math.round(PAD_SIZE.convertToPixels()));
    int holeDiameter = getClosestOdd((int) Math.round(HOLE_SIZE.convertToPixels()));

    drawingObserver.startTrackingContinuityArea(true);
    for (int i = startIndex; i < startIndex + count && i < controlPoints.length; i++) {
      Point2D p = controlPoints[i];
      if (squarePin1 && i == startIndex) {
        // Pin 1 is a square solder pad
        g2d.setColor(PAD_COLOR);
        g2d.fill(new Rectangle2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
        g2d.setColor(PAD_COLOR.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new Rectangle2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
      } else {
        // Round solder pads
        g2d.setColor(PAD_COLOR);
        g2d.fill(new Ellipse2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
        g2d.setColor(PAD_COLOR.darker());
        g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
        g2d.draw(new Ellipse2D.Double(p.getX() - diameter / 2.0, p.getY() - diameter / 2.0, diameter, diameter));
      }

      // Central drill hole
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(p.getX() - holeDiameter / 2.0, p.getY() - holeDiameter / 2.0, holeDiameter, holeDiameter));
      g2d.setColor(PAD_COLOR.darker());
      g2d.draw(new Ellipse2D.Double(p.getX() - holeDiameter / 2.0, p.getY() - holeDiameter / 2.0, holeDiameter, holeDiameter));
    }
    drawingObserver.stopTrackingContinuityArea();
  }

  /**
   * Pad, drill-hole and edge-notch diameters used by {@link #drawCastellatedPads} and
   * {@link #subtractCastellationNotches}. A board whose castellations differ from the package
   * defaults overrides these rather than reimplementing the pad geometry.
   */
  protected Size getCastellatedPadSize() {
    return PAD_SIZE;
  }

  protected Size getCastellatedHoleSize() {
    return HOLE_SIZE;
  }

  protected Size getCastellatedNotchSize() {
    return NOTCH_SIZE;
  }

  protected Color getCastellatedPadColor() {
    return PAD_COLOR;
  }

  /**
   * Helper to draw one column of castellated edge pads: a round through-hole pad joined to the
   * board edge by a stub, with a semicircular notch bitten out of the edge itself. Pair it with
   * {@link #subtractCastellationNotches} so the outline and the pads agree.
   *
   * <p>Unlike the control-point based pad helpers this one works in unrotated board coordinates,
   * so it has to be called while the board rotation is still applied to {@code g2d}.
   *
   * @param g2d Graphics2D context, already transformed for board orientation
   * @param pinX Unrotated X coordinate of the pad column
   * @param pinY Unrotated Y coordinate of the first pad
   * @param count Number of pads in the column
   * @param spacing Pad pitch in pixels
   * @param edgeX Unrotated X coordinate of the board edge the pads reach; the stub is drawn toward
   *        it, so it may lie on either side of {@code pinX}
   */
  protected void drawCastellatedPads(Graphics2D g2d, double pinX, double pinY, int count, double spacing,
      double edgeX, boolean outlineMode, IDrawingObserver drawingObserver) {
    if (outlineMode) return;

    int padD = getClosestOdd((int) Math.round(getCastellatedPadSize().convertToPixels()));
    int holeD = getClosestOdd((int) Math.round(getCastellatedHoleSize().convertToPixels()));
    int notchD = getClosestOdd((int) Math.round(getCastellatedNotchSize().convertToPixels()));
    double padR = padD / 2.0;
    double holeR = holeD / 2.0;
    double notchR = notchD / 2.0;
    double stubX = Math.min(pinX, edgeX);
    double stubW = Math.abs(edgeX - pinX);
    Color padColor = getCastellatedPadColor();

    drawingObserver.startTrackingContinuityArea(true);
    for (int i = 0; i < count; i++) {
      double py = pinY + i * spacing;

      Area padArea = new Area(new Rectangle2D.Double(stubX, py - padR, stubW, padD));
      padArea.add(new Area(new Ellipse2D.Double(pinX - padR, py - padR, padD, padD)));
      padArea.subtract(new Area(new Ellipse2D.Double(edgeX - notchR, py - notchR, notchD, notchD)));
      padArea.subtract(new Area(new Ellipse2D.Double(pinX - holeR, py - holeR, holeD, holeD)));

      g2d.setColor(padColor);
      g2d.fill(padArea);
      g2d.setColor(padColor.darker());
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(padArea);

      // Inner through-hole drill hole (white circle matching Zero and perfboard)
      g2d.setColor(Constants.CANVAS_COLOR);
      g2d.fill(new Ellipse2D.Double(pinX - holeR, py - holeR, holeD, holeD));
      g2d.setColor(padColor.darker());
      g2d.draw(new Ellipse2D.Double(pinX - holeR, py - holeR, holeD, holeD));
    }
    drawingObserver.stopTrackingContinuityArea();
  }

  /**
   * Helper to bite the semicircular castellation notches for one column of pads out of a board
   * outline, matching what {@link #drawCastellatedPads} draws along the same edge.
   */
  protected void subtractCastellationNotches(Area boardArea, double pinY, int count, double spacing,
      double edgeX) {
    int notchD = getClosestOdd((int) Math.round(getCastellatedNotchSize().convertToPixels()));
    double notchR = notchD / 2.0;
    for (int i = 0; i < count; i++) {
      double py = pinY + i * spacing;
      boardArea.subtract(new Area(new Ellipse2D.Double(edgeX - notchR, py - notchR, notchD, notchD)));
    }
  }

  /**
   * Helper to get outline border color.
   */
  protected Color getFinalBorderColor(ComponentState componentState, boolean outlineMode) {
    if (outlineMode) {
      Theme theme = Constants.DEFAULT_THEME;
      try {
        Theme t = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
        if (t != null) theme = t;
      } catch (Exception ignored) {}
      return (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) ? SELECTION_COLOR : theme.getOutlineColor();
    }
    return (componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING) ? SELECTION_COLOR : bodyColor.darker();
  }

  /**
   * Returns the unrotated body shape in local coordinate space (relative to unrotated p0).
   */
  public abstract Shape getBodyShape();

  @Override
  public Rectangle2D getCachingBounds() {
    double minX = Double.MAX_VALUE;
    double maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE;
    double maxY = -Double.MAX_VALUE;

    if (controlPoints != null) {
      for (Point2D p : controlPoints) {
        if (p != null) {
          if (p.getX() < minX) minX = p.getX();
          if (p.getX() > maxX) maxX = p.getX();
          if (p.getY() < minY) minY = p.getY();
          if (p.getY() > maxY) maxY = p.getY();
        }
      }
    }

    Shape bodyShape = getBodyShape();
    if (bodyShape != null && controlPoints != null && controlPoints.length > 0 && controlPoints[0] != null) {
      Point2D p0 = controlPoints[0];
      AffineTransform tx = AffineTransform.getRotateInstance(orientation.toRadians(), p0.getX(), p0.getY());
      Shape rotated = tx.createTransformedShape(bodyShape);
      Rectangle2D b = rotated.getBounds2D();
      if (b.getMinX() < minX) minX = b.getMinX();
      if (b.getMaxX() > maxX) maxX = b.getMaxX();
      if (b.getMinY() < minY) minY = b.getMinY();
      if (b.getMaxY() > maxY) maxY = b.getMaxY();
    }

    if (minX == Double.MAX_VALUE) {
      Point2D p0 = (controlPoints != null && controlPoints.length > 0 && controlPoints[0] != null)
          ? controlPoints[0] : new Point2D.Double(0, 0);
      return new Rectangle2D.Double(p0.getX() - 50, p0.getY() - 50, 100, 100);
    }

    int margin = 50;
    return new Rectangle2D.Double(minX - margin, minY - margin, (maxX - minX) + 2 * margin, (maxY - minY) + 2 * margin);
  }
}
