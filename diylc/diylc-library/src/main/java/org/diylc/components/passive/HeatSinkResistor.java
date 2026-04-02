/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.components.passive;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.components.transform.SimpleComponentTransformer;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PositiveMeasureValidator;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.core.measures.Power;
import org.diylc.core.measures.PowerUnit;
import org.diylc.core.measures.Resistance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Heat Sink Resistor", author = "Branislav Stojkovic", category = "Passive",
    creationMethod = CreationMethod.SINGLE_CLICK, instanceNamePrefix = "R",
    description = "Power resistor with heatsink; similar to Arcol HS series",
    zOrder = IDIYComponent.COMPONENT, transformer = SimpleComponentTransformer.class, enableCache = true)
public class HeatSinkResistor extends AbstractLeadedComponent<Resistance> implements IGerberComponentSimple {

  private static final long serialVersionUID = 1L;

  /** Horizontal fin lines on each side of the central label band (datasheet-style). */
  private static final int COOLING_FIN_LINES_EACH_SIDE = 6;
  /**
   * Fraction of fin-band height reserved for the middle (label) strip; fins share the rest equally
   * above and below.
   */
  private static final double COOLING_FIN_LABEL_BAND_FRACTION = 0.35;

  public static final Color HOUSING_COLOR = Color.decode("#c9a227");
  public static final Color HOUSING_BORDER = Color.decode("#8a7018");

  public static Size DEFAULT_MOUNTING_HOLE_DIAMETER = new Size(3.2, SizeUnit.mm);
  public static Size DEFAULT_HORIZONTAL_HOLE_SPACING = new Size(18.3, SizeUnit.mm);
  public static Size DEFAULT_TERMINAL_LEAD_DIAMETER = new Size(2.0, SizeUnit.mm);
  public static Size DEFAULT_LEAD_LENGTH = new Size(11.85, SizeUnit.mm);
  public static Size DEFAULT_LIP_SIZE = new Size(7.0, SizeUnit.mm);
  public static Size DEFAULT_LENGTH = new Size(27.3, SizeUnit.mm);
  public static Size DEFAULT_WIDTH = new Size(28.0, SizeUnit.mm);

  private Resistance value = null;
  private Power power;
  private MountingHoleCount mountingHoles = MountingHoleCount.TWO_DIAGONAL;
  /** L ±0.25 (mounting hole Ø). */
  private Size mountingHoleDiameter = DEFAULT_MOUNTING_HOLE_DIAMETER;
  /** F ±0.3 (hole spacing along body length). */
  private Size horizontalHoleSpacing = DEFAULT_HORIZONTAL_HOLE_SPACING;
  /** Lead Ø 2.0 ±0.25 mm per HS25 outline. */
  private Size terminalLeadDiameter = DEFAULT_TERMINAL_LEAD_DIAMETER;
  /** (B − E) / 2: overall length B=51 mm, body E=27.3 mm. */
  private Size leadLength = DEFAULT_LEAD_LENGTH;
  /**
   * Top/bottom lip thickness; main body height = A − 2 × lip = D (HS25: A=28, D=14 → lip 7 mm each).
   */
  private Size lipSize = DEFAULT_LIP_SIZE;

  public HeatSinkResistor() {
    super();
    bodyColor = HOUSING_COLOR;
    borderColor = HOUSING_BORDER;
    // HS25: E body length 27.3 mm, A total width 28 mm, B 51 mm with leads
    length = DEFAULT_LENGTH;
    width = DEFAULT_WIDTH;
    leadLength = DEFAULT_LEAD_LENGTH;
  }

  private MountingHoleCount holeCount() {
    return mountingHoles == null ? MountingHoleCount.TWO_DIAGONAL : mountingHoles;
  }

  /** Control points span body length plus one lead stub on each side. */
  private void repositionEndpointsToSpanBodyAndLeads() {
    Point2D[] pts = getPoints();
    if (pts.length < 2 || getLength() == null) {
      return;
    }
    Point2D p0 = pts[0];
    Point2D p1 = pts[1];
    double cx = (p0.getX() + p1.getX()) / 2.0;
    double cy = (p0.getY() + p1.getY()) / 2.0;
    double theta = Math.atan2(p1.getY() - p0.getY(), p1.getX() - p0.getX());
    double bodyL = getLength().convertToPixels();
    double ll = leadLength != null ? leadLength.convertToPixels() : 0;
    double half = bodyL / 2.0 + ll;
    p0.setLocation(cx - Math.cos(theta) * half, cy - Math.sin(theta) * half);
    p1.setLocation(cx + Math.cos(theta) * half, cy + Math.sin(theta) * half);
    pts[2] = calculateLabelPosition(p0, p1);
  }

  /**
   * After default grid placement, expand pin span to body length + lead stubs on each side (see
   * {@link AbstractLeadedComponent#draw} lead length from control-point distance minus pin spacing).
   */
  @Override
  public void createdIn(Project project) {
    super.createdIn(project);
    repositionEndpointsToSpanBodyAndLeads();
  }

  @EditableProperty(name = "Mounting Holes")
  public MountingHoleCount getMountingHoles() {
    return holeCount();
  }

  public void setMountingHoles(MountingHoleCount mountingHoles) {
    this.mountingHoles = mountingHoles;
  }

  @EditableProperty(validatorClass = PositiveMeasureValidator.class)
  public Resistance getValue() {
    return value;
  }

  public void setValue(Resistance value) {
    this.value = value;
  }

  @EditableProperty(name = "Power Rating")
  public Power getPower() {
    return power;
  }

  public void setPower(Power power) {
    this.power = power;
  }

  @EditableProperty(name = "Hole Spacing", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getHorizontalHoleSpacing() {
    return horizontalHoleSpacing;
  }

  public void setHorizontalHoleSpacing(Size horizontalHoleSpacing) {
    this.horizontalHoleSpacing = horizontalHoleSpacing;
  }

  @EditableProperty(name = "Hole Diameter", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getMountingHoleDiameter() {
    return mountingHoleDiameter;
  }

  public void setMountingHoleDiameter(Size mountingHoleDiameter) {
    this.mountingHoleDiameter = mountingHoleDiameter;
  }

  @EditableProperty(name = "Lead Diameter", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getTerminalLeadDiameter() {
    return terminalLeadDiameter;
  }

  public void setTerminalLeadDiameter(Size terminalLeadDiameter) {
    this.terminalLeadDiameter = terminalLeadDiameter;
  }

  @EditableProperty(name = "Lead Length", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getLeadLength() {
    return leadLength;
  }

  public void setLeadLength(Size leadLength) {
    this.leadLength = leadLength;
    repositionEndpointsToSpanBodyAndLeads();
  }

  @EditableProperty(name = "Lip Width", validatorClass = PositiveNonZeroMeasureValidator.class)
  public Size getLipSize() {
    return lipSize;
  }

  public void setLipSize(Size lipSize) {
    this.lipSize = lipSize;
  }

  private double lipThicknessPixels() {
    if (lipSize == null) {
      return DEFAULT_LIP_SIZE.convertToPixels();
    }
    return lipSize.convertToPixels();
  }

  private double horizontalHolePitchXPixels(double bodyL, int holeD, MountingHoleCount holes) {
    Size s = horizontalHoleSpacing != null ? horizontalHoleSpacing : DEFAULT_HORIZONTAL_HOLE_SPACING;
    double f = s.convertToPixels();
    double minF = Math.max(2.0 * holeD, 2.0);
    double maxSpan = Math.max(minF, bodyL - 2.0);
    double maxF = holes == MountingHoleCount.SIX ? maxSpan / 2.0 : maxSpan;
    return Math.min(maxF, Math.max(minF, f));
  }

  /** Y centers of top and bottom lip strips (holes centered vertically in each lip). */
  private static double[] holeRowCentersYInLips(double lip, double totalW) {
    return new double[] {lip / 2.0, totalW - lip / 2.0};
  }

  /** Lip thickness clamped so main band remains and holes fit. */
  private double clampedLipThickness(double totalW, int holeD) {
    double lip = Math.max(holeD, lipThicknessPixels());
    double maxLip = Math.max(holeD, (totalW - 1.0) / 2.0);
    return Math.min(lip, maxLip);
  }

  /** {@code [lip, mainH]} matching {@link #getBodyShape()} after the minimum main-band clamp. */
  private double[] resolveLipGeometry(double totalW, int holeD) {
    double lipRaw = clampedLipThickness(totalW, holeD);
    double mainH = totalW - 2 * lipRaw;
    double lip = lipRaw;
    if (mainH < 1) {
      mainH = 1;
      lip = (totalW - mainH) / 2.0;
    }
    return new double[] {lip, mainH};
  }

  /**
   * Full-length horizontal lines in the fin rectangle, six per side of the central band, evenly
   * spaced within each side (datasheet: seven intervals).
   */
  private void drawCoolingFinLines(Graphics2D g2d, double bodyL, double lipTopY, double mainH, Color lineColor,
      float strokeWidth) {
    Stroke prev = g2d.getStroke();
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(strokeWidth));
    g2d.setColor(lineColor);
    int n = COOLING_FIN_LINES_EACH_SIDE;
    double band = mainH * COOLING_FIN_LABEL_BAND_FRACTION;
    double finZoneH = (mainH - band) / 2.0;
    if (finZoneH < 0.75) {
      g2d.setStroke(prev);
      return;
    }
    double step = finZoneH / (n + 1);
    for (int k = 1; k <= n; k++) {
      double y = lipTopY + step * k;
      g2d.draw(new Line2D.Double(0, y, bodyL, y));
    }
    double lower0 = lipTopY + finZoneH + band;
    for (int k = 1; k <= n; k++) {
      double y = lower0 + step * k;
      g2d.draw(new Line2D.Double(0, y, bodyL, y));
    }
    g2d.setStroke(prev);
  }

  @Override
  public void setLength(Size length) {
    super.setLength(length);
    if (length != null) {
      repositionEndpointsToSpanBodyAndLeads();
    }
  }

  @Override
  public void setWidth(Size width) {
    super.setWidth(width);
    if (width != null) {
      repositionEndpointsToSpanBodyAndLeads();
    }
  }

  @Override
  public String getValueForDisplay() {
    String v = getValue() == null ? "" : getValue().toString();
    String p = getPower() == null ? "" : (" " + getPower().toString());
    return (v + p).trim();
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return false;
  }

  @Override
  protected Size getDefaultLength() {
    return DEFAULT_LENGTH;
  }

  @Override
  protected Size getDefaultWidth() {
    return DEFAULT_WIDTH;
  }

  @Deprecated
  @Override
  public boolean getMoveLabel() {
    return super.getMoveLabel();
  }

  /**
   * Pin spacing along the lead axis equals {@link #getLength()} (body only); lead stubs are outside
   * the body shape.
   */
  @Override
  protected int calculatePinSpacing(java.awt.Rectangle shapeRect) {
    if (getLength() == null) {
      return shapeRect.width;
    }
    return (int) Math.round(getLength().convertToPixels());
  }

  /**
   * Use property length/width for body placement so it matches {@link #calculatePinSpacing} and lead
   * rendering (default {@code true} uses integer shape bounds and can zero-out leads).
   */
  @Override
  protected boolean useShapeRectAsPosition() {
    return false;
  }

  @Override
  protected float getLeadThickness() {
    if (terminalLeadDiameter == null) {
      return super.getLeadThickness();
    }
    return getClosestOdd((float) terminalLeadDiameter.convertToPixels());
  }

  @Override
  protected Shape getBodyShape() {
    double bodyL = Math.max(4.0, getLength().convertToPixels());
    double totalW = Math.max(4.0, getWidth().convertToPixels());
    int holeD = Math.max(1, (int) Math.round(mountingHoleDiameter.convertToPixels()));
    MountingHoleCount holes = holeCount();
    double fPx = horizontalHolePitchXPixels(bodyL, holeD, holes);
    double[] lg = resolveLipGeometry(totalW, holeD);
    double lip = lg[0];
    double mainH = lg[1];
    Area housing = buildLippedBody(bodyL, totalW, lip, mainH, holes, fPx);
    cutMountingHoles(housing, holes, bodyL, totalW, fPx, lip, holeD);
    return housing;
  }

  /**
   * HS10–HS50 style: central fin band plus mounting tabs; tab length along body = body length − F
   * (flush with body ends), tab width = lip (same as top/bottom lip thickness). HS75+ style:
   * full-length top/bottom lips (datasheet E).
   */
  private static Area buildLippedBody(double bodyL, double totalW, double lip, double mainH, MountingHoleCount holes,
      double fPx) {
    Area area = new Area();
    if (holes == MountingHoleCount.TWO_DIAGONAL) {
      area.add(new Area(new Rectangle2D.Double(0, lip, bodyL, mainH)));
      double tabAlongBody = Math.max(1.0, bodyL - fPx);
      double hcx = bodyL / 2.0;
      double[] rows = holeRowCentersYInLips(lip, totalW);
      double yTop = rows[0];
      double yBot = rows[1];
      addMountingTab(area, hcx + fPx / 2.0, yTop, tabAlongBody, lip);
      addMountingTab(area, hcx - fPx / 2.0, yBot, tabAlongBody, lip);
    } else {
      area.add(new Area(new Rectangle2D.Double(0, 0, bodyL, lip)));
      area.add(new Area(new Rectangle2D.Double(0, lip, bodyL, mainH)));
      area.add(new Area(new Rectangle2D.Double(0, totalW - lip, bodyL, lip)));
    }
    return area;
  }

  /** Mounting tab rectangle, centered on the hole: extent along body × lip width. */
  private static void addMountingTab(Area area, double holeCx, double holeCy, double alongBody, double lipWidth) {
    area.add(new Area(new Rectangle2D.Double(holeCx - alongBody / 2.0, holeCy - lipWidth / 2.0, alongBody,
        lipWidth)));
  }

  private void cutMountingHoles(Area housing, MountingHoleCount holes, double bodyL, double totalW, double fPx,
      double lip, int holeD) {
    double cx = bodyL / 2.0;
    double[] rows = holeRowCentersYInLips(lip, totalW);
    double yTop = rows[0];
    double yBot = rows[1];

    switch (holes) {
      case TWO_DIAGONAL:
        cutHole(housing, cx + fPx / 2, yTop, holeD);
        cutHole(housing, cx - fPx / 2, yBot, holeD);
        break;
      case FOUR:
        cutHole(housing, cx - fPx / 2, yTop, holeD);
        cutHole(housing, cx + fPx / 2, yTop, holeD);
        cutHole(housing, cx - fPx / 2, yBot, holeD);
        cutHole(housing, cx + fPx / 2, yBot, holeD);
        break;
      case SIX:
      default:
        cutHole(housing, cx - fPx, yTop, holeD);
        cutHole(housing, cx + fPx, yTop, holeD);
        cutHole(housing, cx - fPx, yBot, holeD);
        cutHole(housing, cx + fPx, yBot, holeD);
        cutHole(housing, cx, yTop, holeD);
        cutHole(housing, cx, yBot, holeD);
        break;
    }
  }

  private static void cutHole(Area housing, double x, double y, int holeD) {
    housing.subtract(new Area(new Ellipse2D.Double(x - holeD / 2.0, y - holeD / 2.0, holeD, holeD)));
  }

  /**
   * Union of lip + main + lip has no internal boundary for {@link #getBodyShape()}, so the border
   * stroke is only the outer rectangle. Draw seam lines at lip/main interfaces so the package reads
   * correctly (and matches drill-template intent).
   */
  @Override
  protected boolean decorateAboveBorder() {
    return true;
  }

  @Override
  protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
    double bodyL = Math.max(4.0, getLength().convertToPixels());
    double totalW = Math.max(4.0, getWidth().convertToPixels());
    int holeD = Math.max(1, (int) Math.round(mountingHoleDiameter.convertToPixels()));
    double[] lg = resolveLipGeometry(totalW, holeD);
    double lip = lg[0];
    double mainH = lg[1];
    if (mainH < 0.5) {
      return;
    }
    float outlineStroke = getOutlineStrokeSize();
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(outlineStroke));
    Color seam;
    if (outlineMode) {
      Theme theme =
          (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
      seam = theme.getOutlineColor();
    } else {
      seam = borderColor != null ? borderColor.darker() : HOUSING_BORDER.darker();
    }
    g2d.setColor(seam);
    g2d.draw(new Line2D.Double(0, lip, bodyL, lip));
    g2d.draw(new Line2D.Double(0, totalW - lip, bodyL, totalW - lip));
    float finStroke = Math.max(0.5f, outlineStroke * 0.55f);
    drawCoolingFinLines(g2d, bodyL, lip, mainH, seam, finStroke);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.rotate(-Math.PI / 4, width / 2, height / 2);
    g2d.setColor(LEAD_COLOR_ICON);
    g2d.drawLine(0, height / 2, width, height / 2);

    int bodyW = width / 2;
    int bodyH = height / 3;
    int finW = bodyW / 3;
    int finH = bodyH / 2;

    int bx = (width - bodyW) / 2;
    int by = (height - bodyH) / 2;

    Area a = new Area(new Rectangle2D.Double(bx, by, bodyW, bodyH));
    a.add(new Area(new Rectangle2D.Double(bx, by + bodyH, finW, finH)));
    a.add(new Area(new Rectangle2D.Double(bx + bodyW - finW, by - finH, finW, finH)));

    int holeD = 2;
    a.subtract(new Area(new Ellipse2D.Double(bx + (finW - holeD) / 2.0, by + bodyH + (finH - holeD) / 2.0, holeD, holeD)));
    a.subtract(new Area(new Ellipse2D.Double(bx + bodyW - finW + (finW - holeD) / 2.0, by - finH + (finH - holeD) / 2.0, holeD, holeD)));

    g2d.setColor(HOUSING_COLOR);
    g2d.fill(a);

    g2d.setColor(HOUSING_BORDER.darker());
    for (int i = 1; i <= 3; i++) {
        int ly = by + (int) (i * bodyH / 4.0);
        g2d.drawLine(bx, ly, bx + bodyW, ly);
    }

    g2d.setColor(HOUSING_BORDER);
    g2d.draw(a);
  }

  @Override
  public boolean isPolarized() {
    return false;
  }

  public enum MountingHoleCount {
    TWO_DIAGONAL("2 diagonal"),
    FOUR("4 holes"),
    SIX("6 holes");

    private final String label;

    MountingHoleCount(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
