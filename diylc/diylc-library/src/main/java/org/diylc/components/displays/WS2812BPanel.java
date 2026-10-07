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
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

import org.diylc.awt.StringUtils;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractAddressableLedBoard;
import org.diylc.components.MakerBoardPainter;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

// Every figure here is read off Adafruit's published EagleCAD board file for the part rather than
// from a product listing, which gives only the outline. The pitch, the hole pattern, the port
// positions and the order the pixels are chained in are all things the drawing needs and no
// vendor states.
//
// Three relationships hold on this board, and they are recorded as the measurements they are
// rather than derived in code, because a panel from another maker need not share any of them: the
// board is exactly eight pitches across, the pixel grid is centred so that half a pitch of margin
// is left outside the outer pixels, and the port row lands in the gap between the first and second
// rows of pixels.
@ComponentDescriptor(name = "NeoPixel Panel", category = "Displays & Outputs",
    author = "Branislav Stojkovic",
    description = "Addressable RGB WS2812B NeoPixel Matrix Panel (8x8, 64 LEDs)",
    instanceNamePrefix = "LED", zOrder = IDIYComponent.COMPONENT,
    bomPolicy = BomPolicy.SHOW_ONLY_TYPE_NAME, keywordPolicy = KeywordPolicy.SHOW_TYPE_NAME,
    enableCache = true)
public class WS2812BPanel extends AbstractAddressableLedBoard {

  private static final long serialVersionUID = 1L;

  public static Color NEO_BLACK = Color.decode("#111111");

  public static Size BOARD_SIZE = new Size(71.12d, SizeUnit.mm);
  public static Size PIXEL_PITCH = new Size(8.89d, SizeUnit.mm);

  /** How far the outer pad of a port sits in from the side edge its port stands against. */
  public static Size PAD_EDGE_INSET = new Size(1.905d, SizeUnit.mm);

  /** How far a port row sits in from the end of the board it runs beside. */
  public static Size PAD_ROW_INSET = new Size(8.89d, SizeUnit.mm);

  public static Size MOUNTING_HOLE_SIZE = new Size(2.8d, SizeUnit.mm);

  public static Size CORNER_RADIUS = new Size(1.0d, SizeUnit.mm);

  /** Pixels along one edge of the square matrix. */
  public static final int MATRIX_ORDER = 8;

  /**
   * Hole centres in millimetres from the middle of the board. Six stand in from the side edges and
   * six in the gaps between pixel columns; the pattern is symmetric about both axes, so which way
   * the vertical axis points does not change the drawing.
   */
  private static final double[][] MOUNTING_HOLES = new double[][] {
      {-33.02d, 17.78d}, {-33.02d, 0d}, {-33.02d, -17.78d},
      {33.02d, 17.78d}, {33.02d, 0d}, {33.02d, -17.78d},
      {-8.89d, 26.67d}, {8.89d, 26.67d},
      {-8.89d, 0d}, {8.89d, 0d},
      {-8.89d, -26.67d}, {8.89d, -26.67d}};

  /**
   * Input first, then output. Both ports carry the same two supply rails, so the rails are
   * numbered to keep the node names distinct the way the Stick's four grounds are; the board
   * prints them simply +5V and GND, and on its back rather than this face.
   */
  public static final String[] PAD_NAMES =
      new String[] {"DIN", "+5V_1", "GND_1", "DOUT", "+5V_2", "GND_2"};

  /**
   * The board file gives its lettering by cap height, which is how Eagle specifies text, while a
   * Java font is specified by em size -- about a third larger again for this family. Converting
   * between the two is what makes the drawn strings reach about as far across the board as the
   * real artwork does; set at the cap height they come out well short of it.
   */
  private static final double CAP_HEIGHT_RATIO = 0.72d;

  /**
   * What the face of the board prints, from layer 21 of the board file. Every line lies in a gap
   * between two rows of pixels, which is the only bare board this part has: the grid reaches
   * within half a pitch of all four edges.
   *
   * <p>The port names are not here. The board prints those on layer 22, its back, as the stick
   * does.
   */
  static final Silk[] SILKSCREEN = new Silk[] {
      new Silk(-19.685d, -17.653d, "Adafruit NeoPixel 8X8", silkFont(2.286d)),
      new Silk(-29.972d, 0.0635d, "64 RGB LEDs", silkFont(1.778d)),
      new Silk(-6.858d, -0.1397d, "24 bit Color", silkFont(1.4224d)),
      new Silk(12.446d, -0.1905d, "Only ONE Pin", silkFont(1.778d)),
      new Silk(-25.4d, 17.8435d, "bLiNkY bLiNkY bLiNkY bLiNkY bLiNkY", silkFont(1.778d))};

  public WS2812BPanel() {
    super();
    this.bodyColor = NEO_BLACK;
    updateControlPoints();
  }

  /**
   * The panel comes in one size, so unlike a board with a geometry property the grid is not what
   * keeps the BOM's rows apart -- the package is.
   */
  @Override
  protected String getVariantLabel() {
    return "8x8, 64 LEDs, " + getLedType();
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index >= 0 && index < PAD_NAMES.length) {
      return PAD_NAMES[index];
    }
    return Integer.toString(index + 1);
  }

  private static double px(double millimetres) {
    return new Size(millimetres, SizeUnit.mm).convertToPixels();
  }

  private static Font silkFont(double capHeightMm) {
    return new Font(SILK_FONT_FAMILY, Font.PLAIN,
        (int) Math.round(px(capHeightMm) / CAP_HEIGHT_RATIO));
  }

  /** Left edge of the board; the input port's first pad sits one inset in from it. */
  private double getBoardX(double x) {
    return x - PAD_EDGE_INSET.convertToPixels();
  }

  /** Top edge of the board; the input port's row sits its own inset down from it. */
  private double getBoardY(double y) {
    return y - PAD_ROW_INSET.convertToPixels();
  }

  /**
   * The two ports are each other turned about the centre of the board, which is what lets panels
   * be chained: the input runs inward from the left edge near the top, the output inward from the
   * right edge near the bottom.
   */
  @Override
  protected void updateControlPoints() {
    Point2D firstPoint = controlPoints[0];
    double spacing = PIN_SPACING.convertToPixels();
    double board = BOARD_SIZE.convertToPixels();
    double acrossBoard = board - 2 * PAD_EDGE_INSET.convertToPixels();
    double downBoard = board - 2 * PAD_ROW_INSET.convertToPixels();

    double[][] relativeOffsets = new double[PAD_NAMES.length][2];
    for (int i = 0; i < 3; i++) {
      relativeOffsets[i][0] = i * spacing;
      relativeOffsets[i][1] = 0;
      relativeOffsets[3 + i][0] = acrossBoard - i * spacing;
      relativeOffsets[3 + i][1] = downBoard;
    }

    rotatePoints(firstPoint, relativeOffsets);
  }

  @Override
  public Shape getBodyShape() {
    Point2D p0 = controlPoints[0];
    double board = BOARD_SIZE.convertToPixels();
    double radius = CORNER_RADIUS.convertToPixels();
    return new RoundRectangle2D.Double(getBoardX(p0.getX()), getBoardY(p0.getY()), board, board,
        radius, radius);
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

    double board = BOARD_SIZE.convertToPixels();
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
      double centreX = boardX + board / 2.0;
      double centreY = boardY + board / 2.0;
      double holeSize = MOUNTING_HOLE_SIZE.convertToPixels();
      for (double[] hole : MOUNTING_HOLES) {
        MakerBoardPainter.drawMountingHole(g2d, centreX + px(hole[0]), centreY + px(hole[1]),
            holeSize);
      }

      // Printed before the packages go down, so a glyph that strays under one is covered by it,
      // which is what the board itself does with silk under a part.
      g2d.setColor(SILK_COLOR);
      for (Silk silk : SILKSCREEN) {
        g2d.setFont(silk.getFont());
        StringUtils.drawCenteredText(g2d, silk.getText(), centreX + px(silk.getXMm()),
            centreY + px(silk.getYMm()), HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
      }

      double pitch = PIXEL_PITCH.convertToPixels();
      double margin = (board - (MATRIX_ORDER - 1) * pitch) / 2.0;
      double ledSize = RGB_LED_SIZE.convertToPixels();

      // The colour is handed out along the data chain rather than across the board, so the sweep
      // shows the order the pixels are addressed in: this panel is wired as a progressive raster,
      // left to right along each row and then back to the left of the next, not as the serpentine
      // most cheap panels use.
      int ledCount = MATRIX_ORDER * MATRIX_ORDER;
      Color[] ledColors = getLedColors(ledCount);
      LedType ledType = getLedType();
      for (int i = 0; i < ledCount; i++) {
        MakerBoardPainter.drawAddressableLed(g2d, boardX + margin + (i % MATRIX_ORDER) * pitch,
            boardY + margin + (i / MATRIX_ORDER) * pitch, ledSize, ledColors[i], 0, ledType);
      }

      // The port names are printed on the back of the real board, so this face carries none.
    }

    g2d.setTransform(oldTx);

    drawPcbSolderPads(g2d, 0, PAD_NAMES.length, false, outlineMode, drawingObserver);

    g2d.setComposite(oldComposite);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    double boardSize = Math.min(width, height) - 4;
    double boardX = (width - boardSize) / 2.0;
    double boardY = (height - boardSize) / 2.0;

    g2d.setColor(NEO_BLACK);
    g2d.fill(new RoundRectangle2D.Double(boardX, boardY, boardSize, boardSize, 2, 2));
    g2d.setColor(Color.DARK_GRAY);
    g2d.draw(new RoundRectangle2D.Double(boardX, boardY, boardSize, boardSize, 2, 2));

    // A four by four grid rather than the part's eight by eight: at toolbox size eight rows of
    // dots fill in to a solid block and the panel stops reading as a panel.
    Color[] rainbow = new Color[] {
        Color.decode("#FF3333"),
        Color.decode("#FFD700"),
        Color.decode("#00E676"),
        Color.decode("#00E5FF"),
        Color.decode("#E040FB"),
        Color.decode("#FF6B35")
    };

    int order = 4;
    double pitch = boardSize / order;
    double dotSize = Math.max(2.0, pitch * 0.55);

    for (int i = 0; i < order * order; i++) {
      double dotX = boardX + (i % order + 0.5) * pitch - dotSize / 2.0;
      double dotY = boardY + (i / order + 0.5) * pitch - dotSize / 2.0;
      g2d.setColor(rainbow[i % rainbow.length]);
      g2d.fill(new Ellipse2D.Double(dotX, dotY, dotSize, dotSize));
    }
  }

  /**
   * One line of silkscreen. The position is the text's left edge and vertical centre in
   * millimetres from the middle of the board, with y running down the drawing rather than up as
   * Eagle records it.
   */
  static class Silk {

    private final double xMm;
    private final double yMm;
    private final String text;
    private final Font font;

    Silk(double xMm, double yMm, String text, Font font) {
      this.xMm = xMm;
      this.yMm = yMm;
      this.text = text;
      this.font = font;
    }

    public double getXMm() { return xMm; }
    public double getYMm() { return yMm; }
    public String getText() { return text; }
    public Font getFont() { return font; }
  }
}
