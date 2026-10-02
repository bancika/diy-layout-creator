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

  public enum Controller {
    // Each board is dimensioned from its module outline, and the header sits centred on the short
    // top edge at the offset recorded here. Active areas are the nominal diagonal taken at the
    // panel's aspect ratio -- 2.8" at 4:3 gives 43.2 x 57.6, 1.8" at 4:5 gives 28.56 x 35.7, and
    // the round 1.28" gives a 32.5 mm lit circle -- which makes them derivations rather than
    // measurements, noted so they are not built upon. A glass length of zero means the module has
    // no separate dark panel wider than its own lit area, so only the screen is drawn, and a hole
    // size of zero means the board has no mounting holes at all.
    // The round board is described by the same two outline figures as the others: its width is the
    // disc diameter and its length runs from the tab's outer edge to the bottom of the disc, so the
    // tab's projection is the difference between them and is not carried separately.
    ILI9341_2_8("2.8\" ILI9341 240x320 (Touch + SD)", 50.0d, 86.0d, 43.2d, 57.6d, 69.1d, 3.0d,
        3.0d, 3.0d, 0d,
        new String[] {"VCC", "GND", "CS", "RESET", "DC", "MOSI (SDI)", "SCK", "LED", "MISO (SDO)",
            "T_CLK", "T_CS", "T_DIN", "T_DO", "T_IRQ"}),
    ST7735_1_8("1.8\" ST7735 128x160", 34.0d, 45.8d, 28.56d, 35.7d, 0d, 1.5d, 3.0d, 3.0d, 0d,
        new String[] {"GND", "VCC", "SCK", "SDA", "RES", "DC", "CS", "BL"}),
    GC9A01_1_28("1.28\" GC9A01 240x240 Round", 38.0d, 45.5d, 32.5d, 32.5d, 0d, 1.76d, 0d, 0d,
        22.9d,
        new String[] {"VCC", "GND", "SCL", "SDA", "DC", "CS", "RST"});

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

    Controller(String label, double boardWidthMm, double boardLengthMm, double screenWidthMm,
        double screenLengthMm, double glassLengthMm, double headerOffsetMm, double holeInsetMm,
        double holeSizeMm, double tabWidthMm, String[] pinNames) {
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

    /** Inset of every mounting hole from each of the two edges nearest it. */
    public double getHoleInsetMm() { return holeInsetMm; }

    public boolean hasMountingHoles() { return holeSizeMm > 0; }

    public boolean hasGlass() { return glassLengthMm > 0; }

    /** Width of the tab the header sits on; meaningful only for a round board. */
    public double getTabWidthMm() { return tabWidthMm; }

    /** How far the tab projects past the disc: whatever the outline is longer than it is wide. */
    public double getTabProjectionMm() { return boardLengthMm - boardWidthMm; }

    public boolean isRound() { return tabWidthMm > 0; }
  }

  public static Color TFT_RED = Color.decode("#C0392B");
  public static Color SCREEN_BG = Color.decode("#111111");
  public static Color GLASS_COLOR = Color.decode("#1A1A1A");

  public static Size CORNER_RADIUS = new Size(1.5d, SizeUnit.mm);

  private Controller controller = Controller.ILI9341_2_8;

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

  @Override
  protected String getVariantLabel() {
    return getController().toString();
  }

  @Override
  public String getControlPointNodeName(int index) {
    String[] pinNames = getController().getPinNames();
    if (index >= 0 && index < pinNames.length) {
      return pinNames[index];
    }
    return "Pin " + (index + 1);
  }

  private static double px(double millimetres) {
    return new Size(millimetres, SizeUnit.mm).convertToPixels();
  }

  /** Left edge of the board, derived so that the pin row sits centred on the top edge. */
  private double getBoardX(double x) {
    Controller controller = getController();
    double spacing = PIN_SPACING.convertToPixels();
    double span = (controller.getPinNames().length - 1) * spacing;
    return x - (px(controller.getBoardWidthMm()) - span) / 2.0;
  }

  private double getBoardY(double y) {
    return y - px(getController().getHeaderOffsetMm());
  }

  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();

    int pinCount = getController().getPinNames().length;
    double[][] relativeOffsets = new double[pinCount][2];
    for (int i = 0; i < pinCount; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
    }

    rotatePoints(firstPoint, relativeOffsets);
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
      // A disc with a rectangular tab on top. The tab has to be carried down to the disc's centre
      // line before the two are unioned: taken only as far as the top of the disc it would meet it
      // at a single tangent point and leave a notch either side of it. Below the line where the
      // disc is as wide as the tab, the disc governs the outline and only the projection shows.
      double discR = boardW / 2.0;
      double discCy = boardY + px(controller.getTabProjectionMm()) + discR;
      double tabW = px(controller.getTabWidthMm());
      Area body = new Area(new Ellipse2D.Double(boardX, discCy - discR, boardW, boardW));
      body.add(new Area(new Rectangle2D.Double(boardX + (boardW - tabW) / 2.0, boardY, tabW,
          discCy - boardY)));
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
        double holeSize = px(controller.getHoleSizeMm());
        MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + holeInset, holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset, boardY + holeInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + boardH - holeInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset,
            boardY + boardH - holeInset, holeSize);
        bandTop = boardY + holeInset;
        bandBottom = boardY + boardH - holeInset;
      }

      double screenW = px(controller.getScreenWidthMm());
      double screenH = px(controller.getScreenLengthMm());
      Shape screen;

      if (controller.isRound()) {
        // the lit circle is centred on the disc, which sits below the tab rather than on the
        // centre line of the bounding box
        double discR = boardW / 2.0;
        double discCx = boardX + discR;
        double discCy = boardY + px(controller.getTabProjectionMm()) + discR;
        screen = new Ellipse2D.Double(discCx - screenW / 2.0, discCy - screenH / 2.0, screenW,
            screenH);
      } else {
        double bandCentre = (bandTop + bandBottom) / 2.0;
        double screenY;
        if (controller.hasGlass()) {
          double glassH = px(controller.getGlassLengthMm());
          double glassY = bandCentre - glassH / 2.0;
          g2d.setColor(GLASS_COLOR);
          g2d.fill(new Rectangle2D.Double(boardX, glassY, boardW, glassH));
          screenY = glassY + (glassH - screenH) / 2.0;
        } else {
          screenY = bandCentre - screenH / 2.0;
        }
        screen = new Rectangle2D.Double(boardX + (boardW - screenW) / 2.0, screenY, screenW,
            screenH);
      }

      g2d.setColor(SCREEN_BG);
      g2d.fill(screen);
      g2d.setColor(Color.DARK_GRAY);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(screen);
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

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
}
