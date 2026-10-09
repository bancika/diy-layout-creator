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
import java.util.List;

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

  public static Size HEADER_OFFSET = new Size(2.54d, SizeUnit.mm);
  // The I2C backpack's four pins stand in a column against the left edge, centred on the height.
  public static Size I2C_HEADER_INSET = new Size(2.5d, SizeUnit.mm);
  // A 3 mm hole centred 2.5 mm in leaves only 1 mm of board outside it, which is what the part
  // does.
  public static Size MOUNTING_HOLE_INSET = new Size(2.5d, SizeUnit.mm);
  public static Size MOUNTING_HOLE_SIZE = new Size(3.0d, SizeUnit.mm);

  // The HD44780 character box. Square dots on a 0.6 mm pitch make the cell 2.95 x 4.75 mm, the
  // figure the datasheet quotes, so the cell is derived from the pitch rather than stored twice.
  public static Size DOT_SIZE = new Size(0.55d, SizeUnit.mm);
  public static Size DOT_PITCH = new Size(0.6d, SizeUnit.mm);
  public static Size CHAR_PITCH_X = new Size(3.55d, SizeUnit.mm);
  public static Size CHAR_PITCH_Y = new Size(5.35d, SizeUnit.mm);
  public static final int DOT_COLUMNS = 5;
  public static final int DOT_ROWS = 8;

  public static Color PCB_GREEN = Color.decode("#1B5E20");
  public static Color SCREEN_BG = Color.decode("#1E88E5");
  public static Color BEZEL_COLOR = Color.decode("#212121");
  // Both inks are real: the blue-backlit module shows light characters and the yellow-green one
  // dark, so the ink follows the screen colour.
  public static Color SCREEN_INK_LIGHT = Color.decode("#F5F5F5");
  public static Color SCREEN_INK_DARK = Color.decode("#1B2631");

  public static final String[] PIN_NAMES_I2C = new String[] {"GND", "VCC", "SDA", "SCL"};
  // The bare designation the module prints, with the function in parentheses where the pin's name
  // does not give it away. The silkscreen label is the part before the parenthesis, there being no
  // room for more along a 0.1" pitch.
  public static final String[] PIN_NAMES_PARALLEL = new String[] {
      "VSS (GND)", "VDD (+5V)", "VO (Contrast)", "RS", "R/W", "E",
      "DB0", "DB1", "DB2", "DB3", "DB4", "DB5", "DB6", "DB7",
      "LEDA (Backlight +)", "LEDK (Backlight -)"
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

  @EditableProperty
  public Display getScreen() {
    return screen == null ? Display.VALUE : screen;
  }

  public void setScreen(Display screen) {
    this.screen = screen;
    invalidateCache();
  }

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

  private int getPinCount() {
    return lcdInterface == LCDInterface.I2C_Backpack ? PIN_NAMES_I2C.length
        : PIN_NAMES_PARALLEL.length;
  }

  /** The ink that contrasts with the backlight the user has chosen. */
  private Color getScreenInk() {
    return CalcUtils.calculateLuminance(screenColor) < 128d ? SCREEN_INK_LIGHT : SCREEN_INK_DARK;
  }

  /**
   * An unlit dot is the backlight seen through the cell, so it shows as a shade of the backlight
   * rather than a colour of its own and follows whatever the user picks.
   */
  private Color getDotColor() {
    return screenColor.darker();
  }

  private double getBoardWidth() {
    return new Size(lcdSize.getWidthMm(), SizeUnit.mm).convertToPixels();
  }

  private double getBoardLength() {
    return new Size(lcdSize.getHeightMm(), SizeUnit.mm).convertToPixels();
  }

  /**
   * Neither header is centred: the parallel row starts a distance in from the left that the module
   * itself dictates, and the I2C column stands against that same edge whatever the module.
   */
  private double getBoardX(double x) {
    Size inset = lcdInterface == LCDInterface.I2C_Backpack ? I2C_HEADER_INSET
        : new Size(lcdSize.getParallelHeaderInsetMm(), SizeUnit.mm);
    return x - inset.convertToPixels();
  }

  /**
   * The parallel row hangs below the top edge by a fixed clearance; the I2C column is centred on
   * the board's height instead.
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
      double holeInset = MOUNTING_HOLE_INSET.convertToPixels();
      double holeSize = MOUNTING_HOLE_SIZE.convertToPixels();
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + holeInset, holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + boardH - holeInset,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset, boardY + holeInset,
          holeSize);
      MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset,
          boardY + boardH - holeInset, holeSize);

      // The bezel's size is the module's own, not a margin taken off the board, so it stays put
      // when the board changes.
      double bezelW = new Size(lcdSize.getBezelWidthMm(), SizeUnit.mm).convertToPixels();
      double bezelH = new Size(lcdSize.getBezelHeightMm(), SizeUnit.mm).convertToPixels();
      double bezelX = boardX + (boardW - bezelW) / 2.0;
      double bezelY = boardY + (boardH - bezelH) / 2.0;

      g2d.setColor(BEZEL_COLOR);
      g2d.fill(new RoundRectangle2D.Double(bezelX, bezelY, bezelW, bezelH, 6, 6));

      double screenW = new Size(lcdSize.getDisplayWidthMm(), SizeUnit.mm).convertToPixels();
      double screenH = new Size(lcdSize.getDisplayHeightMm(), SizeUnit.mm).convertToPixels();
      double screenX = bezelX + (bezelW - screenW) / 2.0;
      double screenY = bezelY + (bezelH - screenH) / 2.0;

      g2d.setColor(screenColor);
      g2d.fill(new Rectangle2D.Double(screenX, screenY, screenW, screenH));

      drawCharacterMatrix(g2d, new Rectangle2D.Double(screenX, screenY, screenW, screenH));

      // Only the parallel row has anywhere to print. The backpack's column sits 2.5 mm from the
      // left edge with the bezel 1.4 mm further in, so a label beside it would lie across the metal
      // frame; the backpack prints its names on its own PCB behind the module, not drawn here.
      if (lcdInterface == LCDInterface.Parallel_16Pin) {
        drawFlatRowPinLabels(g2d, x, y, getRelativeOffsets(), 0, controlPoints.length, true,
            SILK_COLOR);
      }
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  /**
   * The character grid, centred in the lit area: every cell's dots in the unlit shade first, then
   * the dots the text lights over the top of them. The matrix is narrower than the glass on both
   * modules, which is why it is centred rather than inset by a margin.
   */
  private void drawCharacterMatrix(Graphics2D g2d, Rectangle2D screenArea) {
    double dotSize = DOT_SIZE.convertToPixels();
    double dotPitch = DOT_PITCH.convertToPixels();
    double charPitchX = CHAR_PITCH_X.convertToPixels();
    double charPitchY = CHAR_PITCH_Y.convertToPixels();

    double cellWidth = (DOT_COLUMNS - 1) * dotPitch + dotSize;
    double cellHeight = (DOT_ROWS - 1) * dotPitch + dotSize;
    double matrixWidth = (lcdSize.getColumns() - 1) * charPitchX + cellWidth;
    double matrixHeight = (lcdSize.getRows() - 1) * charPitchY + cellHeight;
    double matrixX = screenArea.getX() + (screenArea.getWidth() - matrixWidth) / 2.0;
    double matrixY = screenArea.getY() + (screenArea.getHeight() - matrixHeight) / 2.0;

    // The 20x4 comes to 3200 dots, so the rectangle is reused rather than allocated per dot, and
    // the two shades are laid down in one pass each rather than a colour change per dot.
    Rectangle2D.Double dot = new Rectangle2D.Double(0, 0, dotSize, dotSize);

    g2d.setColor(getDotColor());
    for (int row = 0; row < lcdSize.getRows(); row++) {
      for (int column = 0; column < lcdSize.getColumns(); column++) {
        double cellX = matrixX + column * charPitchX;
        double cellY = matrixY + row * charPitchY;
        for (int dotRow = 0; dotRow < DOT_ROWS; dotRow++) {
          for (int dotColumn = 0; dotColumn < DOT_COLUMNS; dotColumn++) {
            dot.x = cellX + dotColumn * dotPitch;
            dot.y = cellY + dotRow * dotPitch;
            g2d.fill(dot);
          }
        }
      }
    }

    List<String> lines = DotMatrixScreen.layOutText(getScreen(), getName(),
        getValueForDisplay(), lcdSize.getColumns(), lcdSize.getRows());
    if (lines.isEmpty()) {
      return;
    }

    g2d.setColor(getScreenInk());
    int firstRow = (lcdSize.getRows() - lines.size()) / 2;
    for (int line = 0; line < lines.size(); line++) {
      String text = lines.get(line);
      int firstColumn = (lcdSize.getColumns() - text.length()) / 2;
      double cellY = matrixY + (firstRow + line) * charPitchY;
      for (int i = 0; i < text.length(); i++) {
        char c = text.charAt(i);
        double cellX = matrixX + (firstColumn + i) * charPitchX;
        for (int dotRow = 0; dotRow < DotMatrixFont.GLYPH_HEIGHT; dotRow++) {
          for (int dotColumn = 0; dotColumn < DotMatrixFont.GLYPH_WIDTH; dotColumn++) {
            if (DotMatrixFont.isDotLit(c, dotRow, dotColumn)) {
              dot.x = cellX + dotColumn * dotPitch;
              dot.y = cellY + dotRow * dotPitch;
              g2d.fill(dot);
            }
          }
        }
      }
    }
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(PCB_GREEN);
    g2d.fill(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));
    g2d.setColor(PCB_GREEN.darker());
    g2d.draw(new RoundRectangle2D.Double(2, 6, width - 4, height - 12, 3, 3));

    g2d.setColor(SCREEN_BG);
    g2d.fillRect(6, 10, width - 12, height - 20);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 6));
    StringUtils.drawCenteredText(g2d, "LCD", width / 2, height / 2 + 1, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
  }

  public enum LCDSize {
    // Labels reach the BOM's value column, so they stay short; the dimensions are carried by the
    // fields beside them. The bezel and lit area are measured off the module rather than derived as
    // margins off the board, which made the window grow with the board. The last figure is where
    // the parallel header starts, which the two modules do not agree on.
    _16x2("16x2", 16, 2, 80.0, 36.0, 72.2, 24.1, 64.5, 14.5, 7.5),
    _20x4("20x4", 20, 4, 98.0, 60.0, 96.8, 39.3, 77.0, 25.2, 10.0);

    private final String label;
    private final int columns;
    private final int rows;
    private final double widthMm;
    private final double heightMm;
    private final double bezelWidthMm;
    private final double bezelHeightMm;
    private final double displayWidthMm;
    private final double displayHeightMm;
    private final double parallelHeaderInsetMm;

    LCDSize(String label, int columns, int rows, double widthMm, double heightMm,
        double bezelWidthMm, double bezelHeightMm, double displayWidthMm, double displayHeightMm,
        double parallelHeaderInsetMm) {
      this.label = label;
      this.columns = columns;
      this.rows = rows;
      this.widthMm = widthMm;
      this.heightMm = heightMm;
      this.bezelWidthMm = bezelWidthMm;
      this.bezelHeightMm = bezelHeightMm;
      this.displayWidthMm = displayWidthMm;
      this.displayHeightMm = displayHeightMm;
      this.parallelHeaderInsetMm = parallelHeaderInsetMm;
    }

    @Override public String toString() { return label; }
    public int getColumns() { return columns; }
    public int getRows() { return rows; }
    public double getWidthMm() { return widthMm; }
    public double getHeightMm() { return heightMm; }
    public double getBezelWidthMm() { return bezelWidthMm; }
    public double getBezelHeightMm() { return bezelHeightMm; }
    public double getDisplayWidthMm() { return displayWidthMm; }
    public double getDisplayHeightMm() { return displayHeightMm; }
    public double getParallelHeaderInsetMm() { return parallelHeaderInsetMm; }
  }

  public enum LCDInterface {
    I2C_Backpack("I2C Backpack"),
    Parallel_16Pin("Parallel HD44780");

    private final String label;
    LCDInterface(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }
}
