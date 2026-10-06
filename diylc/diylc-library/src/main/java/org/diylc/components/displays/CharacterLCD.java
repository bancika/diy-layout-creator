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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.Display;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
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
import org.diylc.presenter.CalcUtils;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Character LCD", category = "Displays & Outputs",
    author = "Branislav Stojkovic", description = "HD44780-Compatible Character LCD Display (Parallel / I2C Backpack)",
    instanceNamePrefix = "LCD", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class CharacterLCD extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  // clearance from the top edge to the parallel pin row
  public static Size HEADER_OFFSET = new Size(2.54d, SizeUnit.mm);
  // The parallel row is not centred on the board: its leftmost pin starts this far in from the
  // left edge.
  public static Size PARALLEL_HEADER_INSET = new Size(10.0d, SizeUnit.mm);
  // The I2C backpack brings its four pins out as a vertical column standing against the left edge,
  // this far in from it and centred on the board's height.
  public static Size I2C_HEADER_INSET = new Size(2.5d, SizeUnit.mm);
  // Hole centre inset from each edge, and the hole diameter. Both measured. A 3 mm hole centred
  // 2.5 mm in leaves only 1 mm of board outside it, which is tight but is what the part does.
  public static Size MOUNTING_HOLE_INSET = new Size(2.5d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SIZE = new Size(3.0d, SizeUnit.mm);

  public static Color PCB_GREEN = Color.decode("#1B5E20");
  public static Color SCREEN_BG = Color.decode("#1E88E5");
  public static Color BEZEL_COLOR = Color.decode("#212121");
  // Both inks are real: the blue-backlit module shows light characters and the yellow-green one
  // dark ones, so which is used follows the screen colour rather than being fixed.
  public static Color SCREEN_INK_LIGHT = Color.decode("#F5F5F5");
  public static Color SCREEN_INK_DARK = Color.decode("#1B2631");

  public static final String[] PIN_NAMES_I2C = new String[] {"GND", "VCC", "SDA", "SCL"};
  public static final String[] PIN_NAMES_PARALLEL = new String[] {
      "VSS (GND)", "VCC (+5V)", "VEE (Contrast)", "RS", "RW", "E",
      "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7",
      "A (Backlight +)", "K (Backlight -)"
  };

  // What the module prints beside the row, which is the bare designation: the supply rails and
  // the backlight pins carry their function in the node name instead, where a label lying along a
  // 0.1" pitch has no room for it.
  public static final String[] SILK_NAMES_PARALLEL = new String[] {
      "VSS", "VCC", "VEE", "RS", "RW", "E",
      "D0", "D1", "D2", "D3", "D4", "D5", "D6", "D7", "A", "K"
  };

  private LCDSize lcdSize = LCDSize._16x2;
  private LCDInterface lcdInterface = LCDInterface.I2C_Backpack;
  private Color screenColor = SCREEN_BG;
  private Display screen = Display.VALUE;

  public CharacterLCD() {
    super();
    this.bodyColor = PCB_GREEN;
    updateControlPoints();
  }

  @EditableProperty(name = "Size")
  public LCDSize getLcdSize() {
    return lcdSize;
  }

  public void setLcdSize(LCDSize lcdSize) {
    this.lcdSize = lcdSize;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "Interface")
  public LCDInterface getLcdInterface() {
    return lcdInterface;
  }

  public void setLcdInterface(LCDInterface lcdInterface) {
    this.lcdInterface = lcdInterface;
    updateControlPoints();
    invalidateCache();
  }

  /**
   * What the lit area prints. The two sizes differ in outline, but which of them a module is and
   * which interface it carries are what a reader needs and neither is legible from the drawing;
   * {@code NONE} leaves the screen blank.
   */
  @EditableProperty
  public Display getScreen() {
    return screen == null ? Display.VALUE : screen;
  }

  public void setScreen(Display screen) {
    this.screen = screen;
    invalidateCache();
  }

  // Both properties decide which part you buy, so the BOM carries them together.
  @Override
  protected String getVariantLabel() {
    LCDSize lcdSize = getLcdSize();
    if (lcdSize == null) {
      return null;
    }
    LCDInterface lcdInterface = getLcdInterface();
    return lcdInterface == null ? lcdSize.toString() : lcdSize + ", " + lcdInterface;
  }

  @EditableProperty(name = "Backlight Color")
  public Color getScreenColor() {
    return screenColor;
  }

  public void setScreenColor(Color screenColor) {
    this.screenColor = screenColor;
    invalidateCache();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (lcdInterface == LCDInterface.I2C_Backpack) {
      if (index >= 0 && index < PIN_NAMES_I2C.length) return PIN_NAMES_I2C[index];
    } else {
      if (index >= 0 && index < PIN_NAMES_PARALLEL.length) return PIN_NAMES_PARALLEL[index];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected String getSilkPinLabel(int index) {
    if (lcdInterface == LCDInterface.Parallel_16Pin && index >= 0
        && index < SILK_NAMES_PARALLEL.length) {
      return SILK_NAMES_PARALLEL[index];
    }
    return super.getSilkPinLabel(index);
  }

  private int getPinCount() {
    return lcdInterface == LCDInterface.I2C_Backpack ? PIN_NAMES_I2C.length
        : PIN_NAMES_PARALLEL.length;
  }

  /** The ink that contrasts with the backlight the user has chosen. */
  private Color getScreenInk() {
    return CalcUtils.calculateLuminance(screenColor) < 128d ? SCREEN_INK_LIGHT : SCREEN_INK_DARK;
  }

  private double getBoardWidth() {
    return new Size(lcdSize.getWidthMm(), SizeUnit.mm).convertToPixels();
  }

  private double getBoardLength() {
    return new Size(lcdSize.getHeightMm(), SizeUnit.mm).convertToPixels();
  }

  /**
   * Left edge of the board. Neither header is centred on it: the parallel row starts a fixed
   * distance in from the left, and the I2C column stands against that same edge.
   */
  private double getBoardX(double x) {
    Size inset =
        lcdInterface == LCDInterface.I2C_Backpack ? I2C_HEADER_INSET : PARALLEL_HEADER_INSET;
    return x - inset.convertToPixels();
  }

  /**
   * Top edge of the board. The parallel row hangs below it by a fixed clearance; the I2C column is
   * centred on the board's height instead, so the board starts back from the first pin by half of
   * the height the column does not occupy.
   */
  private double getBoardY(double y) {
    if (lcdInterface == LCDInterface.I2C_Backpack) {
      double columnSpan = (getPinCount() - 1) * PIN_SPACING.convertToPixels();
      return y - (getBoardLength() - columnSpan) / 2.0;
    }
    return y - HEADER_OFFSET.convertToPixels();
  }

  private double[][] getRelativeOffsets() {
    double spacing = PIN_SPACING.convertToPixels();
    // the backpack's header stands on end against the left edge; the parallel row lies along the top
    boolean vertical = lcdInterface == LCDInterface.I2C_Backpack;

    double[][] relativeOffsets = new double[getPinCount()][2];
    for (int i = 0; i < relativeOffsets.length; i++) {
      relativeOffsets[i][0] = vertical ? 0 : i * spacing;
      relativeOffsets[i][1] = vertical ? i * spacing : 0;
    }
    return relativeOffsets;
  }

  @Override
  protected void updateControlPoints() {
    rotatePoints(controlPoints[0], getRelativeOffsets());
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()), getBoardWidth(),
        getBoardLength(), 10, 10);
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

    if (!outlineMode) {
      // One mounting hole per corner.
      double holeInset = MOUNTING_HOLE_INSET.convertToPixels();
      double holeSize = MOUNTING_HOLE_SIZE.convertToPixels();
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + holeInset, holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + boardH - holeInset,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset, boardY + holeInset,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset,
          boardY + boardH - holeInset, holeSize);

      // The metal bezel, centred on the board. Its size is the module's own, not a margin taken
      // off the board, so it stays put when the board changes.
      double bezelW = new Size(lcdSize.getBezelWidthMm(), SizeUnit.mm).convertToPixels();
      double bezelH = new Size(lcdSize.getBezelHeightMm(), SizeUnit.mm).convertToPixels();
      double bezelX = boardX + (boardW - bezelW) / 2.0;
      double bezelY = boardY + (boardH - bezelH) / 2.0;

      g2d.setColor(BEZEL_COLOR);
      g2d.fill(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 6, 6));

      // The lit area, centred within the bezel and likewise measured rather than derived.
      double screenW = new Size(lcdSize.getDisplayWidthMm(), SizeUnit.mm).convertToPixels();
      double screenH = new Size(lcdSize.getDisplayHeightMm(), SizeUnit.mm).convertToPixels();
      double screenX = bezelX + (bezelW - screenW) / 2.0;
      double screenY = bezelY + (bezelH - screenH) / 2.0;

      g2d.setColor(screenColor);
      g2d.fill(new Rectangle2D.Double(screenX, screenY, screenW, screenH));

      MakerBoardPainter.drawScreenText(g2d,
          new Rectangle2D.Double(screenX, screenY, screenW, screenH), getScreenInk(), getScreen(),
          getName(), getValueForDisplay());

      // Only the parallel row has anywhere to print. Its names go in the strip between the row and
      // the bezel, which is 2.9 mm deep. The backpack's four pins stand in a column 2.5 mm from
      // the left edge with the bezel beginning 1.4 mm further in, so a label beside them would lie
      // across the metal frame rather than on the board; the backpack prints its names on its own
      // PCB behind the module, which this drawing does not show.
      if (lcdInterface == LCDInterface.Parallel_16Pin) {
        drawFlatRowPinLabels(g2d, x, y, getRelativeOffsets(), 0, controlPoints.length, true,
            SILK_COLOR);
      }
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCB_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));
    g2d.setColor(PCB_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));

    // Screen
    g2d.setColor(SCREEN_BG);
    g2d.fillRect(6, 10, width - 12, height - 20);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 6));
    StringUtils.drawCenteredText(g2d, "LCD", width / 2, height / 2 + 1, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }

  public enum LCDSize {
    // Labels stay short because they reach the BOM's value column; the dimensions are carried by
    // the fields beside them, not by the text. The bezel and the lit area are measured off the
    // module and are sizes in their own right -- deriving them as margins off the board made the
    // window grow with the board instead of staying the size of the part.
    _16x2("16x2", 80.0, 35.0, 72.2, 24.1, 64.5, 14.5),
    _20x4("20x4", 98.0, 60.0, 96.8, 39.3, 77.0, 25.2);

    private final String label;
    private final double widthMm;
    private final double heightMm;
    private final double bezelWidthMm;
    private final double bezelHeightMm;
    private final double displayWidthMm;
    private final double displayHeightMm;

    LCDSize(String label, double widthMm, double heightMm, double bezelWidthMm,
        double bezelHeightMm, double displayWidthMm, double displayHeightMm) {
      this.label = label;
      this.widthMm = widthMm;
      this.heightMm = heightMm;
      this.bezelWidthMm = bezelWidthMm;
      this.bezelHeightMm = bezelHeightMm;
      this.displayWidthMm = displayWidthMm;
      this.displayHeightMm = displayHeightMm;
    }

    @Override public String toString() { return label; }
    public double getWidthMm() { return widthMm; }
    public double getHeightMm() { return heightMm; }
    public double getBezelWidthMm() { return bezelWidthMm; }
    public double getBezelHeightMm() { return bezelHeightMm; }
    public double getDisplayWidthMm() { return displayWidthMm; }
    public double getDisplayHeightMm() { return displayHeightMm; }
  }

  public enum LCDInterface {
    I2C_Backpack("I2C Backpack"),
    Parallel_16Pin("Parallel HD44780");

    private final String label;
    LCDInterface(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }
}
