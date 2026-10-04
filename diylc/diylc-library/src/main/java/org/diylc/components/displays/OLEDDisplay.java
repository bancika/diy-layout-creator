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

  public static final String[] PIN_NAMES_I2C = new String[] {"GND", "VCC", "SCL", "SDA"};
  public static final String[] PIN_NAMES_SPI = new String[] {"GND", "VCC", "D0 (CLK)", "D1 (MOSI)", "RES", "DC", "CS"};

  private Version version = Version.SSD1306_0_96;
  private OLEDInterface oledInterface = OLEDInterface.I2C_4Pin;
  private Display screen = Display.VALUE;

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

  /**
   * What the lit area prints. Six boards share this outline and this colour, so the glass carries
   * the size, the controller and the interface to tell them apart; {@code NONE} leaves the panel
   * dark for a drawing that wants the unpowered part.
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
    return getVersion() + ", " + getOledInterface();
  }

  /**
   * The board the two properties name between them. On the 0.96" and the 1.3" the interface only
   * changes how many pins the header carries, but the 0.91" is sold as two different boards -- a
   * 38 x 12 mm one with a four-pin column on its left edge and a 38 x 20 mm one with a seven-pin
   * row on top -- so the outline, the header edge, the hole pattern and the panel's position all
   * come from the pair rather than from the size alone.
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

  /**
   * Left edge of the board. A row on the top edge is centred across the board; a column on the
   * left edge stands its own clearance in from that edge instead.
   */
  private double getBoardX(double x) {
    Layout layout = getLayout();
    if (layout.isHeaderOnLeftEdge()) {
      return x - px(layout.getHeaderOffsetMm());
    }
    double span = (getPinCount() - 1) * PIN_SPACING.convertToPixels();
    return x - (px(layout.getBoardWidthMm()) - span) / 2.0;
  }

  /**
   * Top edge of the board. A row on the top edge hangs below it by its clearance; a column on the
   * left edge is centred on the board's height instead, so the board starts back from the first
   * pin by half of the height the column does not occupy.
   */
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

      // The lit area is offset within the panel horizontally -- the driver's bonding region takes
      // one end of the glass -- and centred across it. Both are figures of the panel part rather
      // than of the board it is mounted on, so the two 0.91" boards share them.
      double activeW = px(version.getActiveWidthMm());
      double activeH = px(version.getActiveLengthMm());
      double activeX = glassX + px(version.getActiveLeftInPanelMm());
      double activeY = glassY + (glassH - activeH) / 2.0;
      g2d.setColor(ACTIVE_AREA_COLOR);
      g2d.fill(new RoundRectangle2D.Double(activeX, activeY, activeW, activeH, 2, 2));

      MakerBoardPainter.drawScreenText(g2d,
          new Rectangle2D.Double(activeX, activeY, activeW, activeH), PIXEL_BLUE, getScreen(),
          getName(), getValueForDisplay());
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

    // Screen
    g2d.setColor(GLASS_COLOR);
    g2d.fillRect(7, 12, width - 14, height - 18);

    g2d.setColor(PIXEL_BLUE);
    g2d.setFont(new Font("SansSerif", Font.BOLD, 5));
    StringUtils.drawCenteredText(g2d, "OLED", width / 2, height / 2 + 3, HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

    // Pins
    g2d.setColor(PIN_COLOR);
    for (int i = 0; i < 4; i++) {
      g2d.fillRect(10 + i * 3, 5, 2, 2);
    }
  }

  public enum OLEDInterface {
    // Labels stay short because they reach the BOM's value column through getVariantLabel; the pin
    // legend they used to carry is already in the node names. The pin array belongs here rather
    // than on the board, because all three sizes bring the same lines out in the same order and
    // only the interface decides which set that is.
    I2C_4Pin("I2C", PIN_NAMES_I2C),
    SPI_7Pin("SPI", PIN_NAMES_SPI);

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
    // The panel and its lit area belong to the display part, so they are carried here and shared
    // by both of a size's boards; everything that belongs to a PCB is on the Layout beside them.
    // Lit areas are the nominal pixel count at the panel's dot pitch -- 128 x 64 at 0.17 mm,
    // 128 x 32 at 0.175 mm, 128 x 64 at 0.23 mm -- which makes them derivations rather than
    // measurements, noted so they are not built upon.
    //
    // The lit area's offset inside the panel is measured on the 0.91" and happens to be the
    // centred position on the other two; oneCentredPanel in the test pins that down, so a later
    // correction to a panel or a board cannot leave a stale offset behind.
    SSD1306_0_96("0.96\" SSD1306 128x64", 26.7d, 19.3d, 21.744d, 10.864d, 2.478d,
        new Layout(27.0d, 27.0d, 1.8d, false, 1.5d, 2.0d, 0.15d, 3.85d)),
    SSD1306_0_91("0.91\" SSD1306 128x32", 30.0d, 12.0d, 22.384d, 5.584d, 2.1d,
        new Layout(38.0d, 12.0d, 1.5d, true, 0d, 0d, 5.0d, 0d),
        new Layout(38.0d, 20.0d, 1.5d, false, 2.5d, 2.0d, 1.25d, 4.0d)),
    SH1106_1_3("1.3\" SH1106 128x64", 31.4d, 16.7d, 29.42d, 14.7d, 0.99d,
        new Layout(35.4d, 33.5d, 1.5d, false, 2.0d, 3.0d, 2.0d, 8.4d));

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

    public Layout getLayout(OLEDInterface oledInterface) {
      return oledInterface == OLEDInterface.I2C_4Pin ? i2cLayout : spiLayout;
    }
  }

  /**
   * The PCB a size is sold on in one interface. The two are the same board on the 0.96" and the
   * 1.3", which carry the same outline whichever header they are built with, and two genuinely
   * different boards on the 0.91".
   *
   * <p>Hole positions are an inset from each of the two edges nearest a hole, following the TFT
   * boards. The 0.96"'s pattern was given as a 24 mm centre-to-centre spacing instead, which on
   * its 27 mm board is this inset; {@code holePatternMatchesTheSpecifiedSpacing} keeps the two
   * readings in step.
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

    Layout(double boardWidthMm, double boardLengthMm, double headerOffsetMm,
        boolean headerOnLeftEdge, double holeInsetMm, double holeSizeMm, double panelLeftMm,
        double panelTopMm) {
      this.boardWidthMm = boardWidthMm;
      this.boardLengthMm = boardLengthMm;
      this.headerOffsetMm = headerOffsetMm;
      this.headerOnLeftEdge = headerOnLeftEdge;
      this.holeInsetMm = holeInsetMm;
      this.holeSizeMm = holeSizeMm;
      this.panelLeftMm = panelLeftMm;
      this.panelTopMm = panelTopMm;
    }

    public double getBoardWidthMm() { return boardWidthMm; }
    public double getBoardLengthMm() { return boardLengthMm; }
    public double getHoleInsetMm() { return holeInsetMm; }
    public double getHoleSizeMm() { return holeSizeMm; }
    public double getPanelLeftMm() { return panelLeftMm; }
    public double getPanelTopMm() { return panelTopMm; }

    /** Clearance from the edge the header sits on, whichever edge that is. */
    public double getHeaderOffsetMm() { return headerOffsetMm; }

    /** True when the pins stand in a column on the left edge rather than a row along the top. */
    public boolean isHeaderOnLeftEdge() { return headerOnLeftEdge; }

    public boolean hasMountingHoles() { return holeSizeMm > 0; }
  }
}
