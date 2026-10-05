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
package org.diylc.components.displays;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.components.AbstractMakerBoard;
import org.diylc.components.MakerBoardPainter;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "LED Matrix", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "MAX7219 Dot LED Matrix Display Module (Cascadable SPI)",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class LEDMatrix extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color PCB_BLUE = Color.decode("#0055A5");
  public static Color MATRIX_BODY = Color.decode("#1A1A1A");
  public static Color LED_RED = Color.decode("#E53935");
  public static Color LED_OFF = Color.decode("#333333");

  // The modules are flush with each other and with the board, so the outline bounds them rather
  // than their being inset from it, and the dot pitch runs unbroken across the whole strip.
  public static Size MATRIX_SIZE = new Size(32.0d, SizeUnit.mm);
  // Clearance from a board edge to the pin row of the header nearest it. Both headers keep the
  // same distance from their own edge, so the spacing between the rows follows from the board
  // height rather than being a figure of its own.
  public static Size HEADER_OFFSET = new Size(2.54d, SizeUnit.mm);
  public static Size CHIP_MARGIN_X = new Size(2.54d, SizeUnit.mm);
  public static Size CHIP_LENGTH = new Size(5.7d, SizeUnit.mm);
  public static Size CHIP_GAP = new Size(1.9d, SizeUnit.mm);

  public static final String[] PIN_NAMES = new String[] {
      // Input Header (0..4)
      "VCC_IN", "GND_IN", "DIN", "CS_IN", "CLK_IN",
      // Output Header (5..9)
      "VCC_OUT", "GND_OUT", "DOUT", "CS_OUT", "CLK_OUT"
  };

  private Modules modules = Modules.Single_8x8;
  private Color dotColor = LED_RED;

  public LEDMatrix() {
    super();
    this.bodyColor = PCB_BLUE;
    updateControlPoints();
  }

  @EditableProperty(name = "Modules")
  public Modules getModules() {
    if (modules == null) {
      modules = Modules.Single_8x8;
    }
    return modules;
  }

  public void setModules(Modules modules) {
    this.modules = modules;
    updateControlPoints();
    invalidateCache();
  }

  @Override
  protected String getVariantLabel() {
    Modules modules = getModules();
    return modules == null ? null : modules.toString();
  }

  @EditableProperty(name = "Dot Color")
  public Color getDotColor() {
    return dotColor;
  }

  public void setDotColor(Color dotColor) {
    this.dotColor = dotColor;
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PIN_NAMES.length) {
      return PIN_NAMES[index];
    }
    return "Pin " + (index + 1);
  }

  private double[][] getRelativeOffsets() {
    double spacing = PIN_SPACING.convertToPixels();

    double[][] relativeOffsets = new double[PIN_NAMES.length][2];

    // Each row sits the same clearance in from its own edge, so the gap between them is whatever
    // the board leaves. On the original single that is the height and the rows lie across it; on
    // the boards that bring their headers out on the short edges it is the length and the rows
    // stand on end.
    boolean vertical = getModules().hasVerticalHeaders();
    double far = vertical ? getHeaderSpacing() : -getHeaderSpacing();
    for (int i = 0; i < 5; i++) {
      relativeOffsets[i][0] = vertical ? 0 : i * spacing;
      relativeOffsets[i][1] = vertical ? i * spacing : 0;
      relativeOffsets[5 + i][0] = vertical ? far : i * spacing;
      relativeOffsets[5 + i][1] = vertical ? i * spacing : far;
    }
    return relativeOffsets;
  }

  @Override
  protected void updateControlPoints() {
    rotatePoints(controlPoints[0], getRelativeOffsets());
  }

  private double getBoardWidth() {
    return new Size(getModules().getBoardWidthMm(), SizeUnit.mm).convertToPixels();
  }

  private double getBoardLength() {
    return new Size(getModules().getBoardHeightMm(), SizeUnit.mm).convertToPixels();
  }

  /** The span a five-pin row occupies, which the board centres on whichever axis it runs across. */
  private static double getPinRunLength() {
    return 4 * PIN_SPACING.convertToPixels();
  }

  /**
   * Distance between the two pin rows: the board along the axis they face each other across, less
   * the clearance each keeps from its own edge.
   */
  private double getHeaderSpacing() {
    double span = getModules().hasVerticalHeaders() ? getBoardWidth() : getBoardLength();
    return span - 2 * HEADER_OFFSET.convertToPixels();
  }

  /**
   * Left edge of the board. The original single centres its horizontal row across the width; a
   * board with vertical headers stands its row against the left edge, one clearance in.
   */
  private double getBoardX(double x) {
    if (getModules().hasVerticalHeaders()) {
      return x - HEADER_OFFSET.convertToPixels();
    }
    return x - (getBoardWidth() - getPinRunLength()) / 2.0;
  }

  /**
   * Top edge of the board. Control point 0 is an input pin: on the single it sits one clearance up
   * from the bottom edge, with vertical headers it is the top pin of a column centred on the
   * height.
   */
  private double getBoardY(double y) {
    if (getModules().hasVerticalHeaders()) {
      return y - (getBoardLength() - getPinRunLength()) / 2.0;
    }
    return y - getBoardLength() + HEADER_OFFSET.convertToPixels();
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()), getBoardWidth(),
        getBoardLength(), 8, 8);
  }

  /** The demo pattern, in the coordinates of a single 8x8 module. */
  private static boolean isDotLit(int row, int col) {
    return (row == 0 && (col == 2 || col == 5))
        || (row == 1 && (col == 1 || col == 3 || col == 4 || col == 6))
        || (row == 2 && col >= 1 && col <= 6)
        || (row == 3 && col >= 1 && col <= 6)
        || (row == 4 && col >= 2 && col <= 5)
        || (row == 5 && col >= 3 && col <= 4);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }

    Point2D p0 = controlPoints[0];
    double x = p0.getX();
    double y = p0.getY();

    AffineTransform oldTx = g2d.getTransform();
    if (orientation != Orientation.DEFAULT) {
      g2d.rotate(orientation.toRadians(), x, y);
    }

    double boardW = getBoardWidth();
    double boardH = getBoardLength();
    double boardX = getBoardX(x);
    double boardY = getBoardY(y);

    Shape boardShape = getBodyShape();

    Composite oldComposite = applyAlpha(g2d, componentState);

    drawingObserver.startTracking();
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : bodyColor);
    g2d.fill(boardShape);
    drawingObserver.stopTracking();

    g2d.setColor(getFinalBorderColor(componentState, outlineMode));
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1.5f));
    g2d.draw(boardShape);

    // Any header the modules cover is drawn first, so that they cover it. On the single that is
    // the output row alone, which sits behind the module while the input row is out on bare board
    // below it; on the others the modules fill the whole board, so both rows go down here.
    //
    // These are drawn and painted over rather than skipped: drawPinHeader is what registers the
    // pins as a conductive area, so leaving a call out would look identical and quietly drop five
    // pins from continuity. The header works in unrotated control-point coordinates, so the board
    // rotation comes off for it and goes back on for the modules.
    boolean coversBothHeaders = getModules().hasVerticalHeaders();
    g2d.setTransform(oldTx);
    drawPinHeader(g2d, 5, 5, outlineMode, drawingObserver);
    if (coversBothHeaders) {
      drawPinHeader(g2d, 0, 5, outlineMode, drawingObserver);
    }
    if (orientation != Orientation.DEFAULT) {
      g2d.rotate(orientation.toRadians(), x, y);
    }

    if (!outlineMode) {
      Modules modules = getModules();
      double matrixSize = MATRIX_SIZE.convertToPixels();
      double matrixX = boardX + (boardW - modules.getCount() * matrixSize) / 2.0;
      double matrixY = boardY;

      // The modules are square and flush with the board, but its corners are rounded, so the
      // outline clips them rather than their being given a radius they do not have.
      Shape oldClip = g2d.getClip();
      g2d.clip(boardShape);

      g2d.setColor(MATRIX_BODY);
      g2d.fill(new Rectangle2D.Double(matrixX, matrixY, modules.getCount() * matrixSize,
          matrixSize));

      // Because the modules butt against each other the pitch runs unbroken across the strip, so
      // the dots are one grid rather than a tiled one. The pattern is centred across the full
      // width for the same reason: a cascade shows one image, not the same image once per module.
      double dotPitch = matrixSize / 8.0;
      double dotR = dotPitch * 0.7;
      int cols = 8 * modules.getCount();
      int patternOffset = (cols - 8) / 2;
      for (int row = 0; row < 8; row++) {
        for (int col = 0; col < cols; col++) {
          double dx = matrixX + col * dotPitch + (dotPitch - dotR) / 2.0;
          double dy = matrixY + row * dotPitch + (dotPitch - dotR) / 2.0;
          g2d.setColor(isDotLit(row, col - patternOffset) ? dotColor : LED_OFF);
          g2d.fill(new Ellipse2D.Double(dx, dy, dotR, dotR));
        }
      }

      g2d.setClip(oldClip);

      // Only the tall single exposes its driver. The compact one fits it under the module and the
      // cascaded boards hide every one of theirs the same way, which is what makes them
      // tileable.
      if (modules == Modules.Single_8x8) {
        double chipMarginX = CHIP_MARGIN_X.convertToPixels();
        MakerBoardPainter.drawChip(g2d, boardX + chipMarginX,
            matrixY + matrixSize + CHIP_GAP.convertToPixels(), boardW - 2 * chipMarginX,
            CHIP_LENGTH.convertToPixels(), "MAX7219");
      }

      // No IN/OUT silkscreen. With the modules flush there is no strip left for the OUT label, and
      // labelling only one of a matched pair reads worse than labelling neither; the names are
      // left to the node tooltips and the netlist, as on the Ring and the Stick.
      //
      // The pin names are a different matter, because the single has bare board to print them on.
      // They go above its input row rather than below it: below is the 2.54 mm to the board edge,
      // which a label overruns, while above there is clear board between the row and the driver.
      // The output row has the module over it, as do both rows of the other two variants, so
      // those pins are named by their tooltips alone.
      if (modules == Modules.Single_8x8) {
        drawFlatRowPinLabels(g2d, x, y, getRelativeOffsets(), 0, 5, false, SILK_COLOR);
      }
    }

    g2d.setTransform(oldTx);

    // The input row is left to draw only where the modules do not cover it; where they cover both
    // it went down with the output row before them.
    if (!coversBothHeaders) {
      drawPinHeader(g2d, 0, 5, outlineMode, drawingObserver);
    }

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCB_BLUE);
    g2d.fill(new RoundRectangle2D.Double(4, 2, width - 8, height - 4, 3, 3));
    g2d.setColor(PCB_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(4, 2, width - 8, height - 4, 3, 3));

    // Matrix block
    g2d.setColor(MATRIX_BODY);
    g2d.fillRect(6, 4, width - 12, height - 12);

    // 4x4 sample dots
    g2d.setColor(LED_RED);
    for (int r = 0; r < 4; r++) {
      for (int c = 0; c < 4; c++) {
        g2d.fillOval(9 + c * 4, 7 + r * 4, 2, 2);
      }
    }
  }

  /**
   * Which board the modules sit on, which is not the same question as how many there are: two of
   * these carry one module each. The original single puts its driver and both headers on a strip
   * below the module, so the board is taller than it is wide. The compact single fits the driver
   * under the module instead, leaving a board that is only the module, which is what lets several
   * be butted together; the 4-in-1 and the 8-in-1 are that same idea sold as a single PCB. All
   * three hide every driver and bring their headers out on the short edges, standing the pin rows
   * on end.
   *
   * <p>The cascaded boards carry no dimensions of their own: a strip of flush 32 mm modules is
   * 32 mm tall and as wide as it has modules, so the 4-in-1's 128 mm and the 8-in-1's 256 mm are
   * the module count times {@code MATRIX_SIZE} rather than figures read off a board.
   */
  public enum Modules {
    Single_8x8("Single 8x8 (32x50mm)", 1, 32.0d, 50.0d, false),
    Compact_8x8("Compact 8x8 (32x32mm)", 1, 32.0d, 32.0d, true),
    FourInOne_32x8("4-in-1 32x8 (128x32mm)", 4, 128.0d, 32.0d, true),
    EightInOne_64x8("8-in-1 64x8 (256x32mm)", 8, 256.0d, 32.0d, true);

    private final String label;
    private final int count;
    private final double boardWidthMm;
    private final double boardHeightMm;
    private final boolean verticalHeaders;

    Modules(String label, int count, double boardWidthMm, double boardHeightMm,
        boolean verticalHeaders) {
      this.label = label;
      this.count = count;
      this.boardWidthMm = boardWidthMm;
      this.boardHeightMm = boardHeightMm;
      this.verticalHeaders = verticalHeaders;
    }

    @Override public String toString() { return label; }
    public int getCount() { return count; }
    public double getBoardWidthMm() { return boardWidthMm; }
    public double getBoardHeightMm() { return boardHeightMm; }

    /** True where the headers leave by the short edges, so their rows run down rather than across. */
    public boolean hasVerticalHeaders() { return verticalHeaders; }
  }
}
