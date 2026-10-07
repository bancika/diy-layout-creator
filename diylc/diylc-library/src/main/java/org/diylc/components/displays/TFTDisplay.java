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

  /**
   * The SD row drops the prefix its node names carry: the default label strips at the underscore
   * and would print four pins all reading "SD", and the full forms are 24 to 29 px against the
   * 20 px the 0.1" pitch allows. MOSI and MISO are 19 px and just fit, so this row prints them
   * where the main row has to fall back on the board's own SDI and SDO. Nothing but the row's
   * position says these four belong to the card socket, which is also all the board itself says
   * once the prefix is off.
   */
  public static final String[] SILK_NAMES_SD = new String[] {"CS", "MOSI", "MISO", "SCK"};

  /**
   * The eight-pin row the 1.8" and the 1.54" share, and the same lines the 0.96" brings out. Two
   * different controllers behind one header: what a board of this size exposes is the four-wire SPI
   * interface plus reset, data/command and the backlight, and that does not vary with the driver.
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
   * Top edge of the board. A board headed at the bottom stands above control point zero, so its
   * offset is measured up from the bottom edge; the three headed at the top hang below their row
   * instead.
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

    // The second row faces the first across the board, so its distance from control point zero is
    // what the two offsets leave of the length -- above the first row on a board headed at the
    // bottom and below it on one headed at the top. Both rows are centred on the width, which puts
    // the shorter one in by half the difference between the spans; that horizontal placement is the
    // one figure of this header that is not measured.
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
        double sideInset = px(controller.getHoleSideInsetMm());
        double headerSideInset = px(controller.getHeaderSideHoleInsetMm());
        double holeSize = px(controller.getHoleSizeMm());
        // The deeper inset, where a board has one, belongs to the pair sharing the header's edge.
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
          // A panel as wide as its board and one centred across it come out at the same place, so
          // only the 0.96", which is pushed into one side, needs the other branch.
          double glassX = glass.isPlacedFromTheRightEdge()
              ? boardX + boardW - px(glass.getRightInsetMm()) - glassW
              : boardX + (boardW - glassW) / 2.0;
          double glassY = bandCentre - glassH / 2.0;
          g2d.setColor(GLASS_COLOR);
          g2d.fill(new Rectangle2D.Double(glassX, glassY, glassW, glassH));

          // The lit area belongs to the panel rather than to the board, so it is centred in the
          // glass -- which is the same thing as centring it on the board for every variant whose
          // glass spans the board, and is not for the 0.96", whose panel is pushed to one side.
          // It is centred vertically too unless the variant states its own figure, which every
          // board here does but the 0.96": their panels carry the driver's bonding region along
          // the bottom, so the lit area sits high in the frame and centring it would drop it.
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
      // end needs 4.7 mm. Each row prints into the board rather than off its own edge, so the
      // names are above the pins on a row along the bottom edge and below them on one along the
      // top -- the 0.96"'s main row and, where there is one, the SD row facing the main header.
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

    // The glass reaches both edges of the board and is centred down it, as it is on the boards
    // themselves -- every variant whose panel spans the board draws it from edge to edge.
    g2d.setColor(SCREEN_BG);
    g2d.fillRect(8, 6, width - 16, height - 12);

    g2d.setColor(Color.WHITE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "TFT", width / 2, height / 2, HorizontalAlignment.CENTER,
        VerticalAlignment.CENTER);
  }

  public enum Controller {
    // The constants run from the smallest diagonal to the largest, which is the order the part's
    // controller list offers them in; a new variant goes in at its place in that run rather than
    // at the end.
    //
    // Each board is dimensioned from its module outline, and its main header sits centred on the
    // short bottom edge at the offset recorded here -- every board in this family is drawn that
    // way round, so the board grows upwards from its pin row and switching variants never turns a
    // drawing end for end. Active areas are the nominal diagonal taken at the panel's aspect ratio
    // -- the square 1.54" gives 27.66 either way and the round 1.28" a 32.5 mm lit circle -- which
    // makes those derivations rather than measurements, noted so they are not built upon. The two
    // ILI9341 boards and the 1.8" are measured throughout, and the 2.8"'s 43.2 x 57.6 lit area
    // happens to be what 4:3 would have given anyway. A glass length of zero
    // means the module has no separate dark panel wider than its own lit area, so only the screen
    // is drawn, and a hole size of zero means the board has no mounting holes at all.
    // The round board is described by the same two outline figures as the others: its width is the
    // disc diameter and its length runs from the tab's outer edge to the far side of the disc, so
    // the tab's projection is the difference between them and is not carried separately.
    //
    // Three of the six are headed at the top and three at the bottom. There is no default to fall
    // back on, so every constant states its own edge -- see the plan's 12.2.3.
    //
    // The only board in the family whose panel does not span it: a 23.7 x 12.8 mm bezel pushed
    // into the right-hand side, 2 mm from that edge and 4.3 mm from the other, with the driver's
    // circuitry filling the bare L the offset leaves. Its lit area is landscape where every other
    // board's is portrait, it is measured rather than derived, and it is centred in the bezel with
    // a 1 mm frame all round. The bezel is offset only across the board; down it, it keeps the
    // family's rule and sits centred, which on a board whose holes share one inset is the same as
    // centred in the PCB.
    ST7735_0_96("0.96\" ST7735 80x160", 30.0d, 24.0d, 21.7d, 10.8d,
        new Glass(12.8d, 23.7d, 2.0d), 1.44d, 2.5d, 2.0d, 0d, PIN_NAMES_SPI_8PIN, 0d, null, 0d,
        2.5d, true),
    GC9A01_1_28("1.28\" GC9A01 240x240 Round", 38.0d, 45.5d, 32.5d, 32.5d, null, 1.76d, 0d, 0d,
        22.9d,
        new String[] {"RST", "CS", "DC", "SDA", "SCL", "GND", "VCC"}, false),
    // The lit area sits 1.5 mm above the middle of the bezel, which the centring rule puts at
    // 5.01 mm from the top edge, so the 6.53 mm here; move the hole rows and it has to be
    // rederived.
    ST7789_1_54("1.54\" ST7789 240x240", 32.0d, 43.72d, 27.66d, 27.66d, new Glass(33.7d), 1.5d,
        2.5d, 2.0d, 0d, PIN_NAMES_SPI_8PIN, 6.53d, null, 0d, 2.5d, true),
    // Measured throughout, and the second board whose bezel is narrower than the PCB -- 33 mm of
    // a 35 mm board -- but centred across it rather than pushed to one side as the 0.96"'s is.
    // Down the board the centring rule puts the bezel 5.09 mm from the top edge, which is the
    // 5.1 mm the board measures to within the width of the line it is drawn with. The lit area
    // sits 2 mm above the middle of the bezel, as the ILI9341s' do, which is the 8.5 mm here;
    // move the bezel and it has to be rederived.
    ST7735_1_8("1.8\" ST7735 128x160", 35.0d, 56.0d, 28.0d, 35.0d, new Glass(45.83d, 33.0d), 1.41d,
        2.5d, 2.0d, 0d, PIN_NAMES_SPI_8PIN, 8.5d, null, 0d, 2.5d, true),
    // The same panel on a longer board that brings its micro-SD socket out the way the ILI9341s
    // do, which turns the module round: the 8-pin row leaves by the bottom edge and the SD row by
    // the top. It is the one board whose holes are not the same distance in from every edge --
    // 2.75 mm from the sides and 3 mm from the ends, which is what holds them 28.5 mm apart across
    // a 34 mm board and 52 mm apart down a 58 mm one -- and the first to need the side figure
    // stated apart from the shared one. Its bezel is the plain 1.8"'s, so the lit area keeps the
    // same 2 mm above the middle of it, which on this board's hole rows is 9.5 mm from the top
    // edge. Both rows stand 2.5 mm off their own edge, which puts the 8-pin row alongside the
    // holes on its edge rather than clear of them; they pass either side of it.
    ST7735_1_8_SD("1.8\" ST7735 128x160 (SD)", 34.0d, 58.0d, 28.0d, 35.0d,
        new Glass(45.83d, 33.0d), 2.5d, 3.0d, 2.0d, 0d, PIN_NAMES_SPI_8PIN, 9.5d, PIN_NAMES_SD,
        2.5d, 3.0d, 2.75d, false),
    // Measured rather than derived throughout, as the 2.8" is, and laid out the same way round:
    // the 14-pin row on the bottom edge, the SD row in from the opposite one, a lit area measured
    // from the top rather than centred in the glass, and a deeper pair of holes beside the pin
    // row. Each board's figures are its own: 2 mm to the SD row, 9.26 mm to the lit area and
    // 6.92 mm to that pair -- at 3 mm the holes would sit on the row, its 14 pins spanning 33 mm
    // of a 42.72 mm board. The glass keeps the family's rule, centred between the hole rows, which
    // the deeper pair puts at 6.50 to 66.76 mm: 2.76 mm of frame above the lit area and 8.54 mm
    // below it, the asymmetry being the driver's bonding region along the bottom of the panel.
    ILI9341_2_4("2.4\" ILI9341 240x320 (Touch + SD)", 42.72d, 77.18d, 36.72d, 48.96d,
        new Glass(60.26d), 2.0d, 3.0d, 3.0d, 0d, PIN_NAMES_ILI9341, 9.26d, PIN_NAMES_SD, 2.0d,
        6.92d, false),
    // The 2.8"'s micro-SD socket comes out on a four-pin row of its own, the way the 2.4"'s does,
    // on the top edge opposite the main header, 2.5 mm in from it. Its own 14-pin row is 2 mm
    // from the bottom edge, and the pair of holes sharing that edge is 6.50 mm in, well clear of
    // the row rather than level with it as a 3 mm pair would be. Carrying that pair up the board
    // carries the panel with it, the glass being centred between the hole rows: 1.75 mm for the
    // 3.50 mm the holes moved. The 50 x 69.2 mm bezel spans the board and the centring rule puts
    // it 6.65 mm from the top edge; the 43.2 x 57.6 mm lit area is measured 3.05 mm below the
    // bezel's own top edge, which is the 9.70 mm recorded here, and leaves 8.55 mm of frame below
    // it for the driver's bonding region. Move the hole rows and that 9.70 has to be rederived.
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
     * Inset of a mounting hole from each of the two edges nearest it. It is every hole's figure
     * except where {@link #getHeaderSideHoleInsetMm()} overrides the pin row's pair down the
     * board or {@link #getHoleSideInsetMm()} overrides every hole's across it.
     */
    public double getHoleInsetMm() { return holeInsetMm; }

    /**
     * Inset of every hole from the side of the board it is near. It differs from the shared
     * figure only on the 1.8" with the SD socket, whose holes are drilled closer to the sides
     * than to the ends.
     */
    public double getHoleSideInsetMm() { return holeSideInsetMm; }

    /**
     * Inset of the two holes on the header's edge. It is deeper than the shared figure on both
     * ILI9341 boards: the 2.4"'s 14-pin row is wide enough for its pads to reach a hole drilled at
     * the other pairs' 3 mm, and the 2.8" stands its pair clear of a row it would otherwise sit
     * level with. Every other board's row passes between them and keeps one inset throughout.
     */
    public double getHeaderSideHoleInsetMm() { return headerSideHoleInsetMm; }

    public boolean hasMountingHoles() { return holeSizeMm > 0; }

    public boolean hasGlass() { return glass != null; }

    /**
     * True where the main row leaves by the top edge, so the board hangs below it. Three of the
     * six do; see the plan's section 12.2.3 for why the family is not uniform and why no constant
     * may leave this to a default.
     */
    public boolean isHeaderAtTop() { return headerAtTop; }

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

  /**
   * The dark panel bonded to the front of the board, which is not the same thing as the lit area:
   * it is the glass the lit area sits in, and the frame it leaves around it is what a reader sees
   * as the display's border. Every panel is centred between its board's mounting holes, and
   * across the board unless it measures an inset from the right edge, so what varies is how far
   * across the board it reaches and from which side that is taken. Carried as an object rather
   * than as more figures on {@link Controller} because only two boards so far need more than its
   * height, and a constructor that already takes fifteen arguments should not take seventeen.
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

    /** Zero where the panel is as wide as the board, which is every variant but the 0.96". */
    public double getWidthMm() { return widthMm; }

    /** How far the panel stops short of the right edge; meaningful only where it is narrower. */
    public double getRightInsetMm() { return rightInsetMm; }

    public boolean spansTheBoard() { return widthMm <= 0; }

    /** True for a narrower panel placed from the right edge rather than centred on the board. */
    public boolean isPlacedFromTheRightEdge() { return rightInsetMm > 0; }
  }
}
