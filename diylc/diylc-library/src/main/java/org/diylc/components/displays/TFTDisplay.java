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
   * Only the 2.8" board needs its own silkscreen names. Its node names carry the MOSI and MISO
   * aliases, which the default label would print in full, and its touch pins are prefixed, which
   * the default would strip at the underscore and print as five pins all reading "T". Measured at
   * the flat label font, every name here fits inside the 0.1" pitch and the ones it replaces do
   * not: RESET is 22 px and T_CLK 24 px against 20 px of room. The full forms stay on the nodes,
   * so the tooltips and the netlist still spell them out.
   */
  public static final String[] SILK_NAMES_ILI9341 = new String[] {"VCC", "GND", "CS", "RST", "DC",
      "SDI", "SCK", "LED", "SDO", "TCK", "TCS", "TDI", "TDO", "IRQ"};

  /**
   * The 2.4" and the 2.8" are the same product with a different panel in it, so one array serves
   * both -- the treatment the two bare four-digit 7-segment packages already get.
   */
  public static final String[] PIN_NAMES_ILI9341 = new String[] {"VCC", "GND", "CS", "RESET", "DC",
      "MOSI (SDI)", "SCK", "LED", "MISO (SDO)", "T_CLK", "T_CS", "T_DIN", "T_DO", "T_IRQ"};

  /** The micro-SD socket, brought out on its own row on the opposite edge from everything else. */
  public static final String[] PIN_NAMES_SD =
      new String[] {"SD_CS", "SD_MOSI", "SD_MISO", "SD_SCK"};

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

  /**
   * What the lit area prints. The four controllers differ in diagonal, resolution and shape but
   * not in colour, and the 1.8" and 1.54" boards are within 2 mm of each other, so the panel
   * carries the description; {@code NONE} leaves it dark.
   */
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
    if (getController().getPinNames() == PIN_NAMES_ILI9341 && index >= 0
        && index < SILK_NAMES_ILI9341.length) {
      return SILK_NAMES_ILI9341[index];
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
   * Top edge of the board. Every variant carries its main header on the bottom edge, so the board
   * always stands above control point zero and the offset is always measured up from the bottom.
   */
  private double getBoardY(double y) {
    Controller controller = getController();
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

    // The second row faces the first across the board -- always above it, since the main header is
    // always on the bottom edge -- so its distance from control point zero is what the two offsets
    // leave of the length. Both rows are centred on the width, which puts the shorter one in by
    // half the difference between the spans; that horizontal placement is the one figure of this
    // header that is not measured.
    if (secondaryCount > 0) {
      double indent = (primaryCount - secondaryCount) * spacing / 2.0;
      double across = -px(controller.getBoardLengthMm() - controller.getHeaderOffsetMm()
          - controller.getSecondaryHeaderOffsetMm());
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
      // A disc with a rectangular tab hanging below it. The tab has to be carried up to the disc's
      // centre line before the two are unioned: taken only as far as the bottom of the disc it
      // would meet it at a single tangent point and leave a notch either side of it. Above that
      // line the disc is wider than the tab and governs the outline, so only the projection shows.
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
      // The panel is centred between the two rows of mounting holes rather than measured from an
      // edge, which is what keeps it clear of both pairs. A board without holes has nothing to be
      // centred between, so it falls back to the space below the pin row.
      double bandTop = boardY + px(controller.getHeaderOffsetMm());
      double bandBottom = boardY + boardH;

      if (controller.hasMountingHoles()) {
        double holeInset = px(controller.getHoleInsetMm());
        double headerSideInset = px(controller.getHeaderSideHoleInsetMm());
        double holeSize = px(controller.getHoleSizeMm());
        MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + holeInset, holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset, boardY + holeInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset,
            boardY + boardH - headerSideInset, holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset,
            boardY + boardH - headerSideInset, holeSize);
        bandTop = boardY + holeInset;
        bandBottom = boardY + boardH - headerSideInset;
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
        double screenY;
        if (controller.hasGlass()) {
          double glassH = px(controller.getGlassLengthMm());
          double glassY = bandCentre - glassH / 2.0;
          g2d.setColor(GLASS_COLOR);
          g2d.fill(new Rectangle2D.Double(boardX, glassY, boardW, glassH));
          // The lit area is centred in the glass unless the variant measured it, which the 2.4"
          // does: its panel carries the driver's bonding region along the bottom, so the lit area
          // sits high in the frame and centring it would drop it 5 mm.
          screenY = controller.hasMeasuredScreenTop() ? boardY + px(controller.getScreenTopMm())
              : glassY + (glassH - screenH) / 2.0;
        } else {
          screenY = controller.hasMeasuredScreenTop() ? boardY + px(controller.getScreenTopMm())
              : bandCentre - screenH / 2.0;
        }
        panel = new Rectangle2D.Double(boardX + (boardW - screenW) / 2.0, screenY, screenW,
            screenH);
      }

      g2d.setColor(SCREEN_BG);
      g2d.fill(panel);
      g2d.setColor(Color.DARK_GRAY);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(panel);

      // Only the square inscribed in a round panel is safely inside it, so the disc hands over
      // that rather than the bounding box every other variant can use whole.
      Rectangle2D textArea = panel.getBounds2D();
      if (controller.isRound()) {
        double side = textArea.getWidth() / Math.sqrt(2.0);
        textArea = new Rectangle2D.Double(textArea.getCenterX() - side / 2.0,
            textArea.getCenterY() - side / 2.0, side, side);
      }
      MakerBoardPainter.drawScreenText(g2d, textArea, SCREEN_INK, getScreen(), getName(),
          getValueForDisplay());

      // The names go in the strip between the pin row and the panel, lying flat along the row as
      // the Nokia's do: the strip is 3.5 mm deep on the two smallest boards and a label stood on
      // end needs 4.7 mm. The row is on the bottom edge on every variant, so the strip, and the
      // names in it, are always above the pins.
      //
      // Only the primary row is named. The SD row has the glass a couple of millimetres from it and
      // nothing but board edge on its other side, so there is no strip to print in; those four pins
      // are left to their tooltips, as the matrix's covered rows are.
      drawFlatRowPinLabels(g2d, x, y, getRelativeOffsets(), 0, controller.getPinNames().length,
          false, SILK_COLOR);
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

    g2d.setColor(SCREEN_BG);
    g2d.fillRect(10, 8, width - 20, height - 12);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "TFT", width / 2, height / 2 + 3, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  public enum Controller {
    // Each board is dimensioned from its module outline, and its main header sits centred on the
    // short bottom edge at the offset recorded here -- every board in this family is drawn that
    // way round, so the board grows upwards from its pin row and switching variants never turns a
    // drawing end for end. Active areas are the nominal diagonal taken at the
    // panel's aspect ratio -- 2.8" at 4:3 gives 43.2 x 57.6, 1.8" at 4:5 gives 28.56 x 35.7, the
    // square 1.54" gives 27.66 either way and the round 1.28" a 32.5 mm lit circle -- which makes
    // them derivations rather than measurements, noted so they are not built upon. A glass length
    // of zero means the module has no separate dark panel wider than its own lit area, so only the
    // screen is drawn, and a hole size of zero means the board has no mounting holes at all.
    // The round board is described by the same two outline figures as the others: its width is the
    // disc diameter and its length runs from the tab's outer edge to the far side of the disc, so
    // the tab's projection is the difference between them and is not carried separately.
    //
    // The 2.8"'s micro-SD socket comes out on a four-pin row of its own, the way the 2.4"'s does,
    // on the top edge opposite the main header. It keeps the board's own 3 mm offset rather than
    // the 2.4"'s 2 mm, each board putting its second row as far in as its first.
    ILI9341_2_8("2.8\" ILI9341 240x320 (Touch + SD)", 50.0d, 86.0d, 43.2d, 57.6d, 69.1d, 3.0d,
        3.0d, 3.0d, 0d, PIN_NAMES_ILI9341, 0d, PIN_NAMES_SD, 3.0d, 3.0d),
    // The one board here measured rather than derived throughout, and the only one with a second
    // header. Four of its figures do not behave like the others': the 14-pin row is on the bottom
    // edge, the lit area is measured 9.26 mm down from the top rather than centred in the glass,
    // the SD row stands 2 mm in from the opposite edge, and the two holes nearest the pin row are
    // 6.92 mm in rather than sharing the 3 mm of the other pair -- at 3 mm they would sit on the
    // row. The glass keeps the family's rule, centred between the hole rows, which the deeper pair
    // puts at 6.50 to 66.76 mm: 2.76 mm of frame above the lit area and 8.54 mm below it, the
    // asymmetry being the driver's bonding region along the bottom of the panel.
    ILI9341_2_4("2.4\" ILI9341 240x320 (Touch + SD)", 42.72d, 77.18d, 36.72d, 48.96d, 60.26d, 2.0d,
        3.0d, 3.0d, 0d, PIN_NAMES_ILI9341, 9.26d, PIN_NAMES_SD, 2.0d, 6.92d),
    ST7735_1_8("1.8\" ST7735 128x160", 34.0d, 45.8d, 28.56d, 35.7d, 0d, 1.5d, 3.0d, 3.0d, 0d,
        new String[] {"GND", "VCC", "SCK", "SDA", "RES", "DC", "CS", "BL"}),
    ST7789_1_54("1.54\" ST7789 240x240", 32.0d, 43.72d, 27.66d, 27.66d, 33.7d, 1.5d, 2.5d, 2.0d,
        0d,
        new String[] {"GND", "VCC", "SCL", "SDA", "RES", "DC", "CS", "BLK"}),
    GC9A01_1_28("1.28\" GC9A01 240x240 Round", 38.0d, 45.5d, 32.5d, 32.5d, 0d, 1.76d, 0d, 0d,
        22.9d,
        new String[] {"RST", "CS", "DC", "SDA", "SCL", "GND", "VCC"});

    private final String label;
    private final double boardWidthMm;
    private final double boardLengthMm;
    private final double screenWidthMm;
    private final double screenLengthMm;
    private final double glassLengthMm;
    private final double headerOffsetMm;
    private final double holeInsetMm;
    private final double holeSizeMm;
    private final double tabWidthMm;
    private final String[] pinNames;
    private final double screenTopMm;
    private final String[] secondaryPinNames;
    private final double secondaryHeaderOffsetMm;
    private final double headerSideHoleInsetMm;

    /** For a board whose lit area is centred and whose pins all leave on one row. */
    Controller(String label, double boardWidthMm, double boardLengthMm, double screenWidthMm,
        double screenLengthMm, double glassLengthMm, double headerOffsetMm, double holeInsetMm,
        double holeSizeMm, double tabWidthMm, String[] pinNames) {
      this(label, boardWidthMm, boardLengthMm, screenWidthMm, screenLengthMm, glassLengthMm,
          headerOffsetMm, holeInsetMm, holeSizeMm, tabWidthMm, pinNames, 0d, null, 0d, holeInsetMm);
    }

    Controller(String label, double boardWidthMm, double boardLengthMm, double screenWidthMm,
        double screenLengthMm, double glassLengthMm, double headerOffsetMm, double holeInsetMm,
        double holeSizeMm, double tabWidthMm, String[] pinNames, double screenTopMm,
        String[] secondaryPinNames, double secondaryHeaderOffsetMm,
        double headerSideHoleInsetMm) {
      this.label = label;
      this.boardWidthMm = boardWidthMm;
      this.boardLengthMm = boardLengthMm;
      this.screenWidthMm = screenWidthMm;
      this.screenLengthMm = screenLengthMm;
      this.glassLengthMm = glassLengthMm;
      this.headerOffsetMm = headerOffsetMm;
      this.holeInsetMm = holeInsetMm;
      this.holeSizeMm = holeSizeMm;
      this.tabWidthMm = tabWidthMm;
      this.pinNames = pinNames;
      this.screenTopMm = screenTopMm;
      this.secondaryPinNames = secondaryPinNames;
      this.secondaryHeaderOffsetMm = secondaryHeaderOffsetMm;
      this.headerSideHoleInsetMm = headerSideHoleInsetMm;
    }

    @Override public String toString() { return label; }
    public double getBoardWidthMm() { return boardWidthMm; }
    public double getBoardLengthMm() { return boardLengthMm; }
    public double getScreenWidthMm() { return screenWidthMm; }
    public double getScreenLengthMm() { return screenLengthMm; }
    public double getGlassLengthMm() { return glassLengthMm; }
    public double getHeaderOffsetMm() { return headerOffsetMm; }
    public double getHoleSizeMm() { return holeSizeMm; }
    public String[] getPinNames() { return pinNames; }

    /**
     * Inset of a mounting hole from each of the two edges nearest it. It is every hole's figure
     * except where {@link #getHeaderSideHoleInsetMm()} overrides the pin row's pair.
     */
    public double getHoleInsetMm() { return holeInsetMm; }

    /**
     * Inset of the two holes on the header's edge. It is deeper than the shared figure only on the
     * 2.4", whose 14-pin row is wide enough for its pads to reach a hole drilled at the other
     * pairs' 3 mm; every other board's row passes between them and keeps one inset throughout.
     */
    public double getHeaderSideHoleInsetMm() { return headerSideHoleInsetMm; }

    public boolean hasMountingHoles() { return holeSizeMm > 0; }

    public boolean hasGlass() { return glassLengthMm > 0; }

    /** Width of the tab the header sits on; meaningful only for a round board. */
    public double getTabWidthMm() { return tabWidthMm; }

    /** How far the tab projects past the disc: whatever the outline is longer than it is wide. */
    public double getTabProjectionMm() { return boardLengthMm - boardWidthMm; }

    public boolean isRound() { return tabWidthMm > 0; }

    /** Top of the lit area, measured from the board's top edge; zero where it is not measured. */
    public double getScreenTopMm() { return screenTopMm; }

    public boolean hasMeasuredScreenTop() { return screenTopMm > 0; }

    /** The second pin row, or {@code null} on a board that brings everything out on one. */
    public String[] getSecondaryPinNames() { return secondaryPinNames; }

    public boolean hasSecondaryHeader() { return secondaryPinNames != null; }

    /** Clearance from the second row to the top edge, the one the main header does not use. */
    public double getSecondaryHeaderOffsetMm() { return secondaryHeaderOffsetMm; }

  }
}
