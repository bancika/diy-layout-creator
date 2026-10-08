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
import java.awt.FontMetrics;
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
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "OLED Display", category = "Displays & Outputs",
    author = "Branislav Stojkovic",
    description = "Monochrome OLED Display Module (SSD1306 / SH1106, I2C or SPI)",
    instanceNamePrefix = "DISP", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class OLEDDisplay extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  public static Color OLED_BLUE = Color.decode("#004488");
  public static Color GLASS_COLOR = Color.decode("#0D1B2A");
  public static Color GLASS_BORDER_COLOR = Color.decode("#334E68");
  public static Color ACTIVE_AREA_COLOR = Color.decode("#060D15");
  public static Color PIXEL_BLUE = Color.decode("#00D4FF");

  // Clearance a name keeps from the edge of its pad. Tighter than the offset
  // drawFlatRowPinLabels uses, which does not fit the 2.27 mm strip on the 0.96".
  public static Size LABEL_GAP = new Size(0.5d, SizeUnit.mm);

  public static final String[] PIN_NAMES_I2C = new String[] {"GND", "VCC", "SCL", "SDA"};

  /**
   * Clock and data keep their I2C names in both interfaces, since the same two pads carry them.
   * SPI-labelled boards print SCK or CLK and MOSI, and the SSD1306 datasheet D0 and D1.
   */
  public static final String[] PIN_NAMES_SPI =
      new String[] {"GND", "VCC", "SCL", "SDA", "RES", "DC", "CS"};

  /** Chip select tied low on the board instead of brought out, so the bus cannot be shared. */
  public static final String[] PIN_NAMES_SPI_NO_CS =
      new String[] {"GND", "VCC", "SCL", "SDA", "RES", "DC"};

  /**
   * VIN (4.8 - 5.2 V) and VDD (2.8 - 3.3 V logic) are separate rails and must not be tied
   * together. The row follows the controller's pin numbering, which runs the reverse of the silk
   * on the other boards.
   */
  public static final String[] PIN_NAMES_SPI_SPLIT_SUPPLY =
      new String[] {"SDA", "SCL", "DC", "RES", "CS", "VDD", "VIN", "GND"};

  private Version version = Version.SSD1306_0_96;
  private OLEDInterface oledInterface = OLEDInterface.I2C_4Pin;
  private Display screen = Display.VALUE;
  private Color pixelColor = PIXEL_BLUE;

  public OLEDDisplay() {
    super();
    this.bodyColor = OLED_BLUE;
    updateControlPoints();
  }

  @EditableProperty(name = "Size")
  public Version getVersion() {
    return version == null ? Version.SSD1306_0_96 : version;
  }

  public void setVersion(Version version) {
    this.version = version;
    updateControlPoints();
    invalidateCache();
  }

  @EditableProperty(name = "Interface")
  public OLEDInterface getOledInterface() {
    return oledInterface == null ? OLEDInterface.I2C_4Pin : oledInterface;
  }

  public void setOledInterface(OLEDInterface oledInterface) {
    this.oledInterface = oledInterface;
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

  @EditableProperty(name = "Pixel Color")
  public Color getPixelColor() {
    return pixelColor == null ? PIXEL_BLUE : pixelColor;
  }

  public void setPixelColor(Color pixelColor) {
    this.pixelColor = pixelColor;
    invalidateCache();
  }

  @Override
  protected String getVariantLabel() {
    return getVersion() + ", " + getOledInterface();
  }

  /**
   * The 0.91" is sold as two different boards, one with a column on its left edge and one with a
   * row on top, so outline, header edge, holes and panel position follow from size and interface
   * together rather than from the size alone.
   */
  private Layout getLayout() {
    return getVersion().getLayout(getOledInterface());
  }

  @Override
  public String getControlPointNodeName(int index) {
    String[] pinNames = getOledInterface().getPinNames();
    if (index >= 0 && index < pinNames.length) {
      return pinNames[index];
    }
    return "Pin " + (index + 1);
  }

  private int getPinCount() {
    return getOledInterface().getPinNames().length;
  }

  private static double px(double millimetres) {
    return new Size(millimetres, SizeUnit.mm).convertToPixels();
  }

  /** A top-edge row is centred across the board; a left-edge column stands its clearance in. */
  private double getBoardX(double x) {
    Layout layout = getLayout();
    if (layout.isHeaderOnLeftEdge()) {
      return x - px(layout.getHeaderOffsetMm());
    }
    double span = (getPinCount() - 1) * PIN_SPACING.convertToPixels();
    return x - (px(layout.getBoardWidthMm()) - span) / 2.0;
  }

  /** A top-edge row hangs below the edge by its clearance; a left-edge column is centred on the
   * board's height. */
  private double getBoardY(double y) {
    Layout layout = getLayout();
    if (layout.isHeaderOnLeftEdge()) {
      double columnSpan = (getPinCount() - 1) * PIN_SPACING.convertToPixels();
      return y - (px(layout.getBoardLengthMm()) - columnSpan) / 2.0;
    }
    return y - px(layout.getHeaderOffsetMm());
  }

  private double[][] getRelativeOffsets() {
    double spacing = PIN_SPACING.convertToPixels();
    boolean vertical = getLayout().isHeaderOnLeftEdge();

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
    Layout layout = getLayout();
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()),
        px(layout.getBoardWidthMm()), px(layout.getBoardLengthMm()), 8, 8);
  }

  /**
   * Pin names lying flat in the strip between the header and the glass. On the 0.91" I2C board the
   * header is a column, so its names go to the right of the pins rather than below them.
   */
  private void drawPinNames(Graphics2D g2d, double boardX, double boardY) {
    Layout layout = getLayout();
    double[][] offsets = getRelativeOffsets();
    Point2D p0 = controlPoints[0];
    double gap = LABEL_GAP.convertToPixels();

    // drawPinHeader fills a square one pixel wider than PIN_SIZE either side of the pin, which is
    // the edge a name has to clear rather than the pin itself.
    double padHalf = Math.round(PIN_SIZE.convertToPixels()) + 1;

    g2d.setFont(PIN_ROW_FLAT_FONT);
    g2d.setColor(SILK_COLOR);
    FontMetrics metrics = g2d.getFontMetrics();

    // One reference glyph keeps a row of names on a single line. Every name here is upper case
    // with no descender, so centring on the cap height lands them all alike.
    double capHeight = g2d.getFont().createGlyphVector(g2d.getFontRenderContext(), "X")
        .getVisualBounds().getHeight();

    boolean column = layout.isHeaderOnLeftEdge();
    double glassEdge = column ? boardX + px(layout.getPanelLeftMm())
        : boardY + px(layout.getPanelTopMm());

    for (int i = 0; i < offsets.length; i++) {
      String label = getSilkPinLabel(i);
      if (label == null || label.isEmpty()) {
        continue;
      }
      double pinX = p0.getX() + offsets[i][0];
      double pinY = p0.getY() + offsets[i][1];
      double room = glassEdge - (column ? pinX : pinY) - padHalf;
      double extent = column ? metrics.stringWidth(label) : capHeight;
      if (extent > room) {
        continue;
      }

      // Centred in the strip where it is too shallow for the full gap, which the 0.96" is: 1.13 mm
      // between pad and glass to print 0.73 mm of lettering in.
      double offset = padHalf + Math.min(gap, (room - extent) / 2.0);

      if (column) {
        StringUtils.drawCenteredText(g2d, label, pinX + offset, pinY, HorizontalAlignment.LEFT,
            VerticalAlignment.CENTER);
      } else {
        StringUtils.drawCenteredText(g2d, label, pinX, pinY + offset + capHeight / 2.0,
            HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
      }
    }
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

    Layout layout = getLayout();
    double boardW = px(layout.getBoardWidthMm());
    double boardH = px(layout.getBoardLengthMm());
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
      if (layout.hasMountingHoles()) {
        double holeInset = px(layout.getHoleInsetMm());
        double holeSize = px(layout.getHoleSizeMm());
        MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + holeInset, holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset, boardY + holeInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + holeInset, boardY + boardH - holeInset,
            holeSize);
        MakerBoardPainter.drawMountingHole(g2d, boardX + boardW - holeInset,
            boardY + boardH - holeInset, holeSize);
      }

      Version version = getVersion();
      double glassW = px(version.getPanelWidthMm());
      double glassH = px(version.getPanelLengthMm());
      double glassX = boardX + px(layout.getPanelLeftMm());
      double glassY = boardY + px(layout.getPanelTopMm());

      g2d.setColor(GLASS_COLOR);
      g2d.fill(new RoundRectangle2D.Double(glassX, glassY, glassW, glassH, 4, 4));
      g2d.setColor(GLASS_BORDER_COLOR);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.draw(new RoundRectangle2D.Double(glassX, glassY, glassW, glassH, 4, 4));

      // The driver's bonding region takes one end of the glass and its bottom, so the lit area is
      // offset within the panel and sits high in it. Its top is measured from the board's edge.
      double activeW = px(version.getActiveWidthMm());
      double activeH = px(version.getActiveLengthMm());
      double activeX = glassX + px(version.getActiveLeftInPanelMm());
      double activeY = boardY + px(layout.getActiveTopMm());
      g2d.setColor(ACTIVE_AREA_COLOR);
      g2d.fill(new RoundRectangle2D.Double(activeX, activeY, activeW, activeH, 2, 2));

      MakerBoardPainter.drawScreenText(g2d,
          new Rectangle2D.Double(activeX, activeY, activeW, activeH), getPixelColor(), getScreen(),
          getName(), getValueForDisplay());

      drawPinNames(g2d, boardX, boardY);
    }

    g2d.setTransform(oldTx);

    drawPinHeader(g2d, 0, controlPoints.length, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(OLED_BLUE);
    g2d.fill(new RoundRectangle2D.Double(4, 4, width - 8, height - 8, 3, 3));
    g2d.setColor(OLED_BLUE.darker());
    g2d.draw(new RoundRectangle2D.Double(4, 4, width - 8, height - 8, 3, 3));

    g2d.setColor(GLASS_COLOR);
    g2d.fillRect(7, 12, width - 14, height - 18);

    g2d.setColor(PIXEL_BLUE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "OLED", width / 2, height / 2 + 3, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {
      g2d.fillRect(10 + i * 3, 5, 2, 2);
    }
  }

  public enum OLEDInterface {
    // Labels reach the BOM's value column and are printed on the lit area, which the 0.91" has
    // little of, so they stay short. Each SPI label states its pin count, the only thing that
    // tells the three apart.
    I2C_4Pin("I2C", PIN_NAMES_I2C),
    SPI_6Pin("SPI-6", PIN_NAMES_SPI_NO_CS),
    SPI_7Pin("SPI-7", PIN_NAMES_SPI),
    SPI_8Pin("SPI-8", PIN_NAMES_SPI_SPLIT_SUPPLY);

    private final String label;
    private final String[] pinNames;

    OLEDInterface(String label, String[] pinNames) {
      this.label = label;
      this.pinNames = pinNames;
    }

    @Override public String toString() { return label; }
    public String[] getPinNames() { return pinNames; }
  }

  public enum Version {
    // Panel and lit-area sizes belong to the display part and are shared by both of a size's
    // boards. Everything belonging to a PCB is on the Layout, including where the lit area sits
    // down the board, which the two 0.91" boards disagree on.
    SSD1306_0_91("0.91\" SSD1306 128x32", 30.0d, 12.0d, 22.384d, 5.584d, 2.1d,
        new Layout(35.8d, 12.0d, 1.5d, true, 0d, 0d, 5.0d, 0d, 2.35d),
        new Layout(32.5d, 20.5d, 1.5d, false, 2.5d, 2.0d, 1.25d, 4.0d, 6.6d)),
    SSD1306_0_96("0.96\" SSD1306 128x64", 26.7d, 19.26d, 21.74d, 11.0d, 2.48d,
        new Layout(27.0d, 27.0d, 1.6d, false, 2.0d, 2.0d, 0.15d, 3.87d, 6.14d)),
    SH1106_1_3("1.3\" SH1106 128x64", 35.4d, 23.0d, 29.42d, 14.7d, 2.99d,
        new Layout(35.4d, 33.5d, 1.5d, false, 2.5d, 3.0d, 0d, 5.25d, 7.35d));

    private final String label;
    private final double panelWidthMm;
    private final double panelLengthMm;
    private final double activeWidthMm;
    private final double activeLengthMm;
    private final double activeLeftInPanelMm;
    private final Layout i2cLayout;
    private final Layout spiLayout;

    /** For a size sold on one board, which both headers are built onto. */
    Version(String label, double panelWidthMm, double panelLengthMm, double activeWidthMm,
        double activeLengthMm, double activeLeftInPanelMm, Layout layout) {
      this(label, panelWidthMm, panelLengthMm, activeWidthMm, activeLengthMm, activeLeftInPanelMm,
          layout, layout);
    }

    Version(String label, double panelWidthMm, double panelLengthMm, double activeWidthMm,
        double activeLengthMm, double activeLeftInPanelMm, Layout i2cLayout, Layout spiLayout) {
      this.label = label;
      this.panelWidthMm = panelWidthMm;
      this.panelLengthMm = panelLengthMm;
      this.activeWidthMm = activeWidthMm;
      this.activeLengthMm = activeLengthMm;
      this.activeLeftInPanelMm = activeLeftInPanelMm;
      this.i2cLayout = i2cLayout;
      this.spiLayout = spiLayout;
    }

    @Override public String toString() { return label; }
    public double getPanelWidthMm() { return panelWidthMm; }
    public double getPanelLengthMm() { return panelLengthMm; }
    public double getActiveWidthMm() { return activeWidthMm; }
    public double getActiveLengthMm() { return activeLengthMm; }
    public double getActiveLeftInPanelMm() { return activeLeftInPanelMm; }

    /** The 6-pin and 8-pin modules borrow the 7-pin board; the header is centred either way. */
    public Layout getLayout(OLEDInterface oledInterface) {
      return oledInterface == OLEDInterface.I2C_4Pin ? i2cLayout : spiLayout;
    }
  }

  /**
   * The PCB a size is sold on in one interface. Hole positions are an inset from each of the two
   * edges nearest the hole.
   */
  public static class Layout {

    private final double boardWidthMm;
    private final double boardLengthMm;
    private final double headerOffsetMm;
    private final boolean headerOnLeftEdge;
    private final double holeInsetMm;
    private final double holeSizeMm;
    private final double panelLeftMm;
    private final double panelTopMm;
    private final double activeTopMm;

    Layout(double boardWidthMm, double boardLengthMm, double headerOffsetMm,
        boolean headerOnLeftEdge, double holeInsetMm, double holeSizeMm, double panelLeftMm,
        double panelTopMm, double activeTopMm) {
      this.boardWidthMm = boardWidthMm;
      this.boardLengthMm = boardLengthMm;
      this.headerOffsetMm = headerOffsetMm;
      this.headerOnLeftEdge = headerOnLeftEdge;
      this.holeInsetMm = holeInsetMm;
      this.holeSizeMm = holeSizeMm;
      this.panelLeftMm = panelLeftMm;
      this.panelTopMm = panelTopMm;
      this.activeTopMm = activeTopMm;
    }

    public double getBoardWidthMm() { return boardWidthMm; }
    public double getBoardLengthMm() { return boardLengthMm; }
    public double getHoleInsetMm() { return holeInsetMm; }
    public double getHoleSizeMm() { return holeSizeMm; }
    public double getPanelLeftMm() { return panelLeftMm; }
    public double getPanelTopMm() { return panelTopMm; }

    /** Measured from the board's top edge rather than from the glass. */
    public double getActiveTopMm() { return activeTopMm; }

    /** Clearance from the edge the header sits on, whichever edge that is. */
    public double getHeaderOffsetMm() { return headerOffsetMm; }

    public boolean isHeaderOnLeftEdge() { return headerOnLeftEdge; }

    public boolean hasMountingHoles() { return holeSizeMm > 0; }
  }
}
