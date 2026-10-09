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
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
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
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "TFT Display", category = "Displays & Outputs",
    author = "Branislav Stojkovic",
    description = "Color TFT LCD display module with an SPI interface",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class TFTDisplay extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  /**
   * Silkscreen names for the 2.8". The default label would strip the touch prefix and print five
   * pins all reading "T", and the names it keeps do not fit the 0.1" pitch. The nodes keep the
   * full forms for the tooltips and the netlist.
   */
  public static final String[] SILK_NAMES_ILI9341 = new String[] {"VCC", "GND", "CS", "RST", "DC",
      "SDI", "SCK", "LED", "SDO", "TCK", "TCS", "TDI", "TDO", "IRQ"};

  /** The 2.4" and the 2.8" are the same product with a different panel in it. */
  public static final String[] PIN_NAMES_ILI9341 = new String[] {"VCC", "GND", "CS", "RESET", "DC",
      "MOSI (SDI)", "SCK", "LED", "MISO (SDO)", "T_CLK", "T_CS", "T_DIN", "T_DO", "T_IRQ"};

  /** The micro-SD socket, brought out on its own row on the opposite edge from everything else. */
  public static final String[] PIN_NAMES_SD =
      new String[] {"SD_CS", "SD_MOSI", "SD_MISO", "SD_SCK"};

  /**
   * The SD row drops the prefix its node names carry, which the default label would reduce to four
   * pins all reading "SD". Only the row's position marks these as the card socket's.
   */
  public static final String[] SILK_NAMES_SD = new String[] {"CS", "MOSI", "MISO", "SCK"};

  /**
   * Four-wire SPI plus reset, data/command and the backlight, which is what a board of this size
   * brings out whichever controller is behind it.
   */
  public static final String[] PIN_NAMES_SPI_8PIN =
      new String[] {"GND", "VCC", "SCL", "SDA", "RES", "DC", "CS", "BLK"};

  public static Color TFT_RED = Color.decode("#C0392B");
  public static Color SCREEN_BG = Color.decode("#111111");
  public static Color GLASS_COLOR = Color.decode("#1A1A1A");
  public static Color SCREEN_INK = Color.decode("#E0E0E0");

  public static Size CORNER_RADIUS = new Size(1.5d, SizeUnit.mm);

  private Controller controller = Controller.ILI9341_2_8;
  private Display screen = Display.VALUE;

  public TFTDisplay() {
    super();
    this.bodyColor = TFT_RED;
    updateControlPoints();
  }

  @EditableProperty(name = "Controller")
  public Controller getController() {
    return controller == null ? Controller.ILI9341_2_8 : controller;
  }

  public void setController(Controller controller) {
    this.controller = controller;
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
    return getController().toString();
  }

  @Override
  public String getControlPointNodeName(int index) {
    Controller controller = getController();
    String[] pinNames = controller.getPinNames();
    if (index >= 0 && index < pinNames.length) {
      return pinNames[index];
    }
    String[] secondary = controller.getSecondaryPinNames();
    if (secondary != null && index >= pinNames.length
        && index < pinNames.length + secondary.length) {
      return secondary[index - pinNames.length];
    }
    return "Pin " + (index + 1);
  }

  @Override
  protected String getSilkPinLabel(int index) {
    Controller controller = getController();
    if (controller.getPinNames() == PIN_NAMES_ILI9341 && index >= 0
        && index < SILK_NAMES_ILI9341.length) {
      return SILK_NAMES_ILI9341[index];
    }
    int secondaryIndex = index - controller.getPinNames().length;
    if (controller.getSecondaryPinNames() == PIN_NAMES_SD && secondaryIndex >= 0
        && secondaryIndex < SILK_NAMES_SD.length) {
      return SILK_NAMES_SD[secondaryIndex];
    }
    return super.getSilkPinLabel(index);
  }

  private static double px(double millimetres) {
    return new Size(millimetres, SizeUnit.mm).convertToPixels();
  }

  /** Left edge of the board, derived so that the primary pin row sits centred across it. */
  private double getBoardX(double x) {
    Controller controller = getController();
    double spacing = PIN_SPACING.convertToPixels();
    double span = (controller.getPinNames().length - 1) * spacing;
    return x - (px(controller.getBoardWidthMm()) - span) / 2.0;
  }

  /**
   * A board headed at the bottom stands above control point zero; one headed at the top hangs
   * below it.
   */
  private double getBoardY(double y) {
    Controller controller = getController();
    if (controller.isHeaderAtTop()) {
      return y - px(controller.getHeaderOffsetMm());
    }
    return y - px(controller.getBoardLengthMm() - controller.getHeaderOffsetMm());
  }

  private double[][] getRelativeOffsets() {
    double spacing = PIN_SPACING.convertToPixels();
    Controller controller = getController();
    int primaryCount = controller.getPinNames().length;
    String[] secondary = controller.getSecondaryPinNames();
    int secondaryCount = secondary == null ? 0 : secondary.length;

    double[][] relativeOffsets = new double[primaryCount + secondaryCount][2];
    for (int i = 0; i < primaryCount; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }

    // The second row faces the first, so its distance from control point zero is what the two
    // offsets leave of the length. Both rows are centred on the width.
    if (secondaryCount > 0) {
      double indent = (primaryCount - secondaryCount) * spacing / 2.0;
      double across = px(controller.getBoardLengthMm() - controller.getHeaderOffsetMm()
          - controller.getSecondaryHeaderOffsetMm());
      if (!controller.isHeaderAtTop()) {
        across = -across;
      }
      for (int i = 0; i < secondaryCount; i++) {
        relativeOffsets[primaryCount + i][0] = indent + i * spacing;
        relativeOffsets[primaryCount + i][1] = across;
      }
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
    Controller controller = getController();
    double boardX = getBoardX(p0.getX());
    double boardY = getBoardY(p0.getY());
    double boardW = px(controller.getBoardWidthMm());
    double boardH = px(controller.getBoardLengthMm());

    if (controller.isRound()) {
      // The tab has to reach the disc's centre line before the two are unioned: stopped at the
      // bottom of the disc it would meet it at a tangent and leave a notch either side.
      double discR = boardW / 2.0;
      double discCy = boardY + discR;
      double tabW = px(controller.getTabWidthMm());
      Area body = new Area(new Ellipse2D.Double(boardX, boardY, boardW, boardW));
      body.add(new Area(new Rectangle2D.Double(boardX + (boardW - tabW) / 2.0, discCy, tabW,
          boardY + boardH - discCy)));
      return body;
    }

    double radius = CORNER_RADIUS.convertToPixels();
    return new RoundRectangle2D.Double(boardX, boardY, boardW, boardH, radius, radius);
  }

  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
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

    Controller controller = getController();
    double boardW = px(controller.getBoardWidthMm());
    double boardH = px(controller.getBoardLengthMm());
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
      // Centring the panel between the hole rows rather than on an edge is what keeps it clear of
      // both pairs. A board without holes falls back to the space below the pin row.
      double bandTop = boardY + px(controller.getHeaderOffsetMm());
      double bandBottom = boardY + boardH;

      if (controller.hasMountingHoles()) {
        double holeInset = px(controller.getHoleInsetMm());
        double sideInset = px(controller.getHoleSideInsetMm());
        double headerSideInset = px(controller.getHeaderSideHoleInsetMm());
        double holeSize = px(controller.getHoleSizeMm());
        double topInset = controller.isHeaderAtTop() ? headerSideInset : holeInset;
        double bottomInset = controller.isHeaderAtTop() ? holeInset : headerSideInset;
        MakerBoardPainter.drawMountingHole(g2d, boardX + sideInset, boardY + topInset, holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - sideInset, boardY + topInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + sideInset, boardY + boardH - bottomInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - sideInset,
            boardY + boardH - bottomInset, holeSize);
        bandTop = boardY + topInset;
        bandBottom = boardY + boardH - bottomInset;
      }

      double screenW = px(controller.getScreenWidthMm());
      double screenH = px(controller.getScreenLengthMm());
      Shape panel;

      if (controller.isRound()) {
        // the lit circle is centred on the disc, which sits above the tab rather than on the
        // centre line of the bounding box
        double discR = boardW / 2.0;
        double discCx = boardX + discR;
        double discCy = boardY + discR;
        panel = new Ellipse2D.Double(discCx - screenW / 2.0, discCy - screenH / 2.0, screenW,
            screenH);
      } else {
        double bandCentre = (bandTop + bandBottom) / 2.0;
        double screenX;
        double screenY;
        if (controller.hasGlass()) {
          Glass glass = controller.getGlass();
          double glassH = px(glass.getLengthMm());
          double glassW = glass.spansTheBoard() ? boardW : px(glass.getWidthMm());
          double glassX = glass.isPlacedFromTheRightEdge()
              ? boardX + boardW - px(glass.getRightInsetMm()) - glassW
              : boardX + (boardW - glassW) / 2.0;
          double glassY = bandCentre - glassH / 2.0;
          g2d.setColor(GLASS_COLOR);
          g2d.fill(new Rectangle2D.Double(glassX, glassY, glassW, glassH));

          // Centred in the glass, and vertically too unless the board states its own figure: the
          // driver's bonding region takes the bottom of the panel, so centring would drop it.
          screenX = glassX + (glassW - screenW) / 2.0;
          screenY = controller.hasMeasuredScreenTop() ? boardY + px(controller.getScreenTopMm())
              : glassY + (glassH - screenH) / 2.0;
        } else {
          screenX = boardX + (boardW - screenW) / 2.0;
          screenY = controller.hasMeasuredScreenTop() ? boardY + px(controller.getScreenTopMm())
              : bandCentre - screenH / 2.0;
        }
        panel = new Rectangle2D.Double(screenX, screenY, screenW, screenH);
      }

      g2d.setColor(SCREEN_BG);
      g2d.fill(panel);
      g2d.setColor(Color.DARK_GRAY);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(panel);

      // Only the inscribed square is safely inside a round panel.
      Rectangle2D textArea = panel.getBounds2D();
      if (controller.isRound()) {
        double side = textArea.getWidth() / Math.sqrt(2.0);
        textArea = new Rectangle2D.Double(textArea.getCenterX() - side / 2.0,
            textArea.getCenterY() - side / 2.0, side, side);
      }
      MakerBoardPainter.drawScreenText(g2d, textArea, SCREEN_INK, getScreen(), getName(),
          getValueForDisplay());

      // Names lie flat: the strip is 3.5 mm deep on the two smallest boards and a label stood on
      // end needs 4.7 mm. Each row prints into the board rather than off its own edge.
      double[][] offsets = getRelativeOffsets();
      drawFlatRowPinLabels(g2d, x, y, offsets, 0, controller.getPinNames().length,
          controller.isHeaderAtTop(), SILK_COLOR);
      if (controller.hasSecondaryHeader()) {
        drawFlatRowPinLabels(g2d, x, y, offsets, controller.getPinNames().length,
            controller.getSecondaryPinNames().length, !controller.isHeaderAtTop(), SILK_COLOR);
      }
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controller.getPinNames().length, outlineMode, drawingObserver);
    if (controller.hasSecondaryHeader()) {
      drawPinHeader(g2d, controller.getPinNames().length,
          controller.getSecondaryPinNames().length, outlineMode, drawingObserver);
    }

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(TFT_RED);
    g2d.fill(new RoundRectangle2D.Double(8, 1, width - 16, height - 2, 3, 3));
    g2d.setColor(TFT_RED.darker());
    g2d.draw(new RoundRectangle2D.Double(8, 1, width - 16, height - 2, 3, 3));

    // The glass reaches both edges of the board and is centred down it.
    g2d.setColor(SCREEN_BG);
    g2d.fillRect(8, 6, width - 16, height - 12);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "TFT", width / 2, height / 2, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  public enum Controller {
    // Ordered by diagonal; a new variant goes in at its place in that run rather than at the end.
    // A glass length of zero means the module has no dark panel wider than its own lit area, so
    // only the screen is drawn, and a hole size of zero means no mounting holes at all. The round
    // board carries the same two outline figures as the rest: width is the disc diameter and
    // length runs from the tab's outer edge to the far side of the disc, so the tab's projection
    // is the difference between them.
    //
    // A board headed at the top hangs below its row and one headed at the bottom grows upwards
    // from it. Both occur here, so every constant states its own edge rather than taking a
    // default.

    // The one board whose bezel does not span it, being pushed into the right-hand side with the
    // driver's circuitry filling the bare L that leaves.
    ST7735_0_96("0.96\" ST7735 80x160", 30.0d, 24.0d, 21.7d, 10.8d,
        new Glass(12.8d, 23.7d, 2.0d), 1.44d, 2.5d, 2.0d, 0d, PIN_NAMES_SPI_8PIN, 0d, null, 0d,
        2.5d, true),
    GC9A01_1_28("1.28\" GC9A01 240x240 Round", 38.0d, 45.5d, 32.5d, 32.5d, null, 1.76d, 0d, 0d,
        22.9d,
        new String[] {"RST", "CS", "DC", "SDA", "SCL", "GND", "VCC"}, false),
    // Lit area 1.5 mm above the middle of the bezel; rederive it if the hole rows move.
    ST7789_1_54("1.54\" ST7789 240x240", 32.0d, 43.72d, 27.66d, 27.66d, new Glass(33.7d), 1.5d,
        2.5d, 2.0d, 0d, PIN_NAMES_SPI_8PIN, 6.53d, null, 0d, 2.5d, true),
    // Bezel narrower than the PCB but centred across it, unlike the 0.96"'s. Lit area 2 mm above
    // the middle of the bezel; rederive it if the bezel moves.
    ST7735_1_8("1.8\" ST7735 128x160", 35.0d, 56.0d, 28.0d, 35.0d, new Glass(45.83d, 33.0d), 1.41d,
        2.5d, 2.0d, 0d, PIN_NAMES_SPI_8PIN, 8.5d, null, 0d, 2.5d, true),
    // The plain 1.8"'s panel on a longer board with a micro-SD row, which turns the module round.
    // The one board whose holes are not the same distance in from every edge, and whose 8-pin row
    // runs alongside the holes on its edge rather than between them.
    ST7735_1_8_SD("1.8\" ST7735 128x160 (SD)", 34.0d, 58.0d, 28.0d, 35.0d,
        new Glass(45.83d, 33.0d), 2.5d, 3.0d, 2.0d, 0d, PIN_NAMES_SPI_8PIN, 9.5d, PIN_NAMES_SD,
        2.5d, 3.0d, 2.75d, false),
    // The pair of holes beside the 14-pin row is set deeper than the rest: at the shared 3 mm the
    // row's 14 pins, spanning 33 mm of a 42.72 mm board, would sit on them.
    ILI9341_2_4("2.4\" ILI9341 240x320 (Touch + SD)", 42.72d, 77.18d, 36.72d, 48.96d,
        new Glass(60.26d), 2.0d, 3.0d, 3.0d, 0d, PIN_NAMES_ILI9341, 9.26d, PIN_NAMES_SD, 2.0d,
        6.92d, false),
    // As the 2.4", with the pair beside the pin row set deeper again. That carries the panel with
    // it, the glass being centred between the hole rows, so the lit area's 9.70 mm has to be
    // rederived if the holes move.
    ILI9341_2_8("2.8\" ILI9341 240x320 (Touch + SD)", 50.0d, 86.0d, 43.2d, 57.6d, new Glass(69.2d),
        2.0d, 3.0d, 3.0d, 0d, PIN_NAMES_ILI9341, 9.70d, PIN_NAMES_SD, 2.5d, 6.50d, false);
    private final String label;
    private final double boardWidthMm;
    private final double boardLengthMm;
    private final double screenWidthMm;
    private final double screenLengthMm;
    private final Glass glass;
    private final double headerOffsetMm;
    private final double holeInsetMm;
    private final double holeSizeMm;
    private final double tabWidthMm;
    private final String[] pinNames;
    private final double screenTopMm;
    private final String[] secondaryPinNames;
    private final double secondaryHeaderOffsetMm;
    private final double headerSideHoleInsetMm;
    private final double holeSideInsetMm;
    private final boolean headerAtTop;

    /** For a board whose lit area is centred and whose pins all leave on one row. */
    Controller(String label, double boardWidthMm, double boardLengthMm, double screenWidthMm,
        double screenLengthMm, Glass glass, double headerOffsetMm, double holeInsetMm,
        double holeSizeMm, double tabWidthMm, String[] pinNames, boolean headerAtTop) {
      this(label, boardWidthMm, boardLengthMm, screenWidthMm, screenLengthMm, glass, headerOffsetMm,
          holeInsetMm, holeSizeMm, tabWidthMm, pinNames, 0d, null, 0d, holeInsetMm, headerAtTop);
    }

    /** For a board whose holes are the same distance in from every edge they are near. */
    Controller(String label, double boardWidthMm, double boardLengthMm, double screenWidthMm,
        double screenLengthMm, Glass glass, double headerOffsetMm, double holeInsetMm,
        double holeSizeMm, double tabWidthMm, String[] pinNames, double screenTopMm,
        String[] secondaryPinNames, double secondaryHeaderOffsetMm, double headerSideHoleInsetMm,
        boolean headerAtTop) {
      this(label, boardWidthMm, boardLengthMm, screenWidthMm, screenLengthMm, glass, headerOffsetMm,
          holeInsetMm, holeSizeMm, tabWidthMm, pinNames, screenTopMm, secondaryPinNames,
          secondaryHeaderOffsetMm, headerSideHoleInsetMm, holeInsetMm, headerAtTop);
    }

    Controller(String label, double boardWidthMm, double boardLengthMm, double screenWidthMm,
        double screenLengthMm, Glass glass, double headerOffsetMm, double holeInsetMm,
        double holeSizeMm, double tabWidthMm, String[] pinNames, double screenTopMm,
        String[] secondaryPinNames, double secondaryHeaderOffsetMm, double headerSideHoleInsetMm,
        double holeSideInsetMm, boolean headerAtTop) {
      this.label = label;
      this.boardWidthMm = boardWidthMm;
      this.boardLengthMm = boardLengthMm;
      this.screenWidthMm = screenWidthMm;
      this.screenLengthMm = screenLengthMm;
      this.glass = glass;
      this.headerOffsetMm = headerOffsetMm;
      this.holeInsetMm = holeInsetMm;
      this.holeSizeMm = holeSizeMm;
      this.tabWidthMm = tabWidthMm;
      this.pinNames = pinNames;
      this.screenTopMm = screenTopMm;
      this.secondaryPinNames = secondaryPinNames;
      this.secondaryHeaderOffsetMm = secondaryHeaderOffsetMm;
      this.headerSideHoleInsetMm = headerSideHoleInsetMm;
      this.holeSideInsetMm = holeSideInsetMm;
      this.headerAtTop = headerAtTop;
    }

    @Override public String toString() { return label; }
    public double getBoardWidthMm() { return boardWidthMm; }
    public double getBoardLengthMm() { return boardLengthMm; }
    public double getScreenWidthMm() { return screenWidthMm; }
    public double getScreenLengthMm() { return screenLengthMm; }
    /** The dark panel, or {@code null} on a board whose lit area is all there is to draw. */
    public Glass getGlass() { return glass; }
    public double getHeaderOffsetMm() { return headerOffsetMm; }
    public double getHoleSizeMm() { return holeSizeMm; }
    public String[] getPinNames() { return pinNames; }

    /**
     * Inset from each of the two edges nearest a hole, except where
     * {@link #getHeaderSideHoleInsetMm()} or {@link #getHoleSideInsetMm()} overrides it.
     */
    public double getHoleInsetMm() { return holeInsetMm; }

    /** Overrides the shared inset across the board; only the 1.8" with the SD socket needs it. */
    public double getHoleSideInsetMm() { return holeSideInsetMm; }

    /**
     * Inset of the two holes on the header's edge, set deeper on boards whose row would reach them.
     */
    public double getHeaderSideHoleInsetMm() { return headerSideHoleInsetMm; }

    public boolean hasMountingHoles() { return holeSizeMm > 0; }

    public boolean hasGlass() { return glass != null; }

    /** True where the main row leaves by the top edge, so the board hangs below it. */
    public boolean isHeaderAtTop() { return headerAtTop; }

    /** Width of the tab the header sits on; meaningful only for a round board. */
    public double getTabWidthMm() { return tabWidthMm; }

    /** How far the tab projects past the disc: whatever the outline is longer than it is wide. */
    public double getTabProjectionMm() { return boardLengthMm - boardWidthMm; }

    public boolean isRound() { return tabWidthMm > 0; }

    /** Top of the lit area from the board's top edge; zero where it is centred in the glass. */
    public double getScreenTopMm() { return screenTopMm; }

    public boolean hasMeasuredScreenTop() { return screenTopMm > 0; }

    /** The second pin row, or {@code null} on a board that brings everything out on one. */
    public String[] getSecondaryPinNames() { return secondaryPinNames; }

    public boolean hasSecondaryHeader() { return secondaryPinNames != null; }

    /** Clearance from the second row to the top edge, the one the main header does not use. */
    public double getSecondaryHeaderOffsetMm() { return secondaryHeaderOffsetMm; }

  }

  /**
   * The dark panel the lit area sits in, whose frame is the display's visible border. Centred
   * between the board's mounting holes, and across the board unless it states an inset from the
   * right edge.
   *
   * @author Branislav Stojkovic
   */
  public static class Glass {

    private final double lengthMm;
    private final double widthMm;
    private final double rightInsetMm;

    /** A panel that spans the board. */
    public Glass(double lengthMm) {
      this(lengthMm, 0d, 0d);
    }

    /** A panel narrower than its board, centred across it. */
    public Glass(double lengthMm, double widthMm) {
      this(lengthMm, widthMm, 0d);
    }

    /** A panel narrower than its board and pushed towards the right edge. */
    public Glass(double lengthMm, double widthMm, double rightInsetMm) {
      this.lengthMm = lengthMm;
      this.widthMm = widthMm;
      this.rightInsetMm = rightInsetMm;
    }

    public double getLengthMm() { return lengthMm; }

    /** Zero where the panel is as wide as the board. */
    public double getWidthMm() { return widthMm; }

    /** How far the panel stops short of the right edge; meaningful only where it is narrower. */
    public double getRightInsetMm() { return rightInsetMm; }

    public boolean spansTheBoard() { return widthMm <= 0; }

    /** True for a narrower panel placed from the right edge rather than centred on the board. */
    public boolean isPlacedFromTheRightEdge() { return rightInsetMm > 0; }
  }
}
