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
package org.diylc.components.semiconductors;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.common.Orientation;
import org.diylc.common.Percentage;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.components.transform.RGBLEDTransformer;
import org.diylc.core.annotations.AiNote;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@AiNote("Control points 0-3 are the free ends of the four leads, pin 1 first; control point 4 is the body, and the leads run from a tight row at the body to their ends")
@AiNote("'Pin order' lists the leads from pin 1 using R, G, B and C for the common lead; 'Common' decides whether C is the anode (A) or the cathode (K)")
@AiNote("The flat on the rim marks the pin 1 end; 'View' draws the LED from the top or from the side")
@ComponentDescriptor(name = "RGB LED", author = "Jindra Helcl", category = "Semiconductors",
    instanceNamePrefix = "D", description = "4-lead RGB LED with flexible leads",
    zOrder = IDIYComponent.COMPONENT, keywordPolicy = KeywordPolicy.SHOW_VALUE,
    transformer = RGBLEDTransformer.class, enableCache = true)
public class RGBLED extends AbstractTransistorPackage {

  private static final long serialVersionUID = 1L;

  public static Color BODY_COLOR = new Color(205, 228, 242);
  public static Color BORDER_COLOR = new Color(120, 140, 155);
  public static Color FRAME_COLOR = new Color(165, 165, 170);
  public static Color LEAD_COLOR = Color.decode("#CCCCCC");
  public static Color LABEL_COLOR = Color.black;
  public static Color[] DIE_COLORS =
      new Color[] {new Color(230, 40, 40), new Color(40, 190, 60), new Color(50, 90, 230)};
  public static Size LEAD_THICKNESS = new Size(0.6d, SizeUnit.mm);
  public static Size LEAD_PITCH = new Size(0.05d, SizeUnit.in);
  public static Size LENS_SIZE = new Size(5d, SizeUnit.mm);
  public static String PIN_ORDER = "RCGB";

  private static final int BODY = 4;
  private static final double PIXELS_PER_MM = new Size(1d, SizeUnit.mm).convertToPixels();

  private Size leadPitch = LEAD_PITCH;
  private Size size = LENS_SIZE;
  private CommonType common = CommonType.CATHODE;
  private String pinOrder = PIN_ORDER;
  private View view = View.TOP;
  private Color leadColor = LEAD_COLOR;

  public RGBLED() {
    super();
    controlPoints = new Point2D[5];
    for (int i = 0; i < 5; i++) {
      controlPoints[i] = new Point2D.Double(0, 0);
    }
    // lead 1 at the origin, ends on a 0.1in grid, body above them
    double grid = new Size(0.1d, SizeUnit.in).convertToPixels();
    for (int i = 0; i < 4; i++) {
      controlPoints[i].setLocation(i * grid, 0);
    }
    controlPoints[BODY].setLocation(1.5 * grid, -1.5 * grid);
    this.alphaPercent = new Percentage(60);
    bodyColor = BODY_COLOR;
    borderColor = BORDER_COLOR;
    labelColor = LABEL_COLOR;
  }

  @EditableProperty(name = "View")
  public View getView() {
    if (view == null) {
      view = View.TOP;
    }
    return view;
  }

  public void setView(View view) {
    this.view = view;
    body = null;
  }

  // spacing of the leads where they leave the body, not where they end
  @EditableProperty(name = "Lead pitch")
  public Size getLeadPitch() {
    if (leadPitch == null) {
      leadPitch = LEAD_PITCH;
    }
    return leadPitch;
  }

  public void setLeadPitch(Size leadPitch) {
    this.leadPitch = leadPitch;
    body = null;
  }

  @EditableProperty(name = "Size")
  public Size getSize() {
    if (size == null) {
      size = LENS_SIZE;
    }
    return size;
  }

  public void setSize(Size size) {
    this.size = size;
    body = null;
  }

  @EditableProperty(name = "Common")
  public CommonType getCommon() {
    if (common == null) {
      common = CommonType.CATHODE;
    }
    return common;
  }

  public void setCommon(CommonType common) {
    this.common = common;
  }

  @EditableProperty(name = "Pin order", validatorClass = PinOrderValidator.class)
  public String getPinOrder() {
    if (pinOrder == null) {
      pinOrder = PIN_ORDER;
    }
    return pinOrder;
  }

  public void setPinOrder(String pinOrder) {
    this.pinOrder = pinOrder == null ? null : pinOrder.trim().toUpperCase();
  }

  @EditableProperty(name = "Lead Color")
  public Color getLeadColor() {
    if (leadColor == null) {
      leadColor = LEAD_COLOR;
    }
    return leadColor;
  }

  public void setLeadColor(Color leadColor) {
    this.leadColor = leadColor;
  }

  // hidden: transistor pinouts and folding don't apply to an LED
  @Override
  public TransistorPinout getPinout() {
    return null;
  }

  @Override
  public boolean getFolded() {
    return false;
  }

  @Override
  public String getComment() {
    return getCommon().toString() + ", pins " + getPinLabel(0) + getPinLabel(1) + getPinLabel(2)
        + getPinLabel(3);
  }

  @Override
  public String getPrefix() {
    return null;
  }

  @Override
  public int mapToSpiceNode(int index) {
    return index;
  }

  /** @return R, G, B, K or A for the given lead, or its number if the pin order is invalid */
  public String getPinLabel(int index) {
    String order = getPinOrder();
    if (order.length() != 4 || index < 0 || index > 3) {
      return Integer.toString(index + 1);
    }
    char c = order.charAt(index);
    if (isCommon(c)) {
      return getCommon() == CommonType.ANODE ? "A" : "K";
    }
    return Character.toString(c);
  }

  private static boolean isCommon(char c) {
    return c == 'C' || c == 'K' || c == 'A';
  }

  @Override
  public String getControlPointNodeName(int index) {
    if (index < 0 || index >= 4) {
      return null;
    }
    return getPinLabel(index);
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return index < 4;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return index < 4 ? VisibilityPolicy.ALWAYS : VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public boolean canPointMoveFreely(int pointIndex) {
    return true;
  }

  @Override
  protected void updateControlPoints() {
    // the leads are flexible, so orientation only turns the body around its control point
  }

  /**
   * Maps body-local coordinates to the canvas: x runs along the row of leads (pin 1 first), y
   * points from the body towards the leads, and the origin is the body control point.
   */
  private AffineTransform localToCanvas() {
    Point2D b = controlPoints[BODY];
    Orientation o = getOrientation();
    if (o == Orientation._90) {
      return new AffineTransform(0, 1, -1, 0, b.getX(), b.getY());
    }
    if (o == Orientation._180) {
      return new AffineTransform(-1, 0, 0, -1, b.getX(), b.getY());
    }
    if (o == Orientation._270) {
      return new AffineTransform(0, -1, 1, 0, b.getX(), b.getY());
    }
    return new AffineTransform(1, 0, 0, 1, b.getX(), b.getY());
  }

  private double px(double mm) {
    return mm * PIXELS_PER_MM;
  }

  // the dimensions below scale with the lens size; values are for a 5mm LED
  private double scale() {
    return getSize().convertToPixels() / px(5);
  }

  private double lensRadius() {
    return getSize().convertToPixels() / 2;
  }

  private double rimRadius() {
    return lensRadius() * 1.16;
  }

  private double rimHeight() {
    return px(1) * scale();
  }

  private double bodyHeight() {
    return px(8.6) * scale();
  }

  private double leadX(int i) {
    return (i - 1.5) * getLeadPitch().convertToPixels();
  }

  private Point2D leadExit(int i) {
    return new Point2D.Double(leadX(i), 0);
  }

  private Area localBody() {
    double rr = rimRadius();
    double cut = rr * 0.86;
    if (getView() == View.SIDE) {
      double r = lensRadius();
      double h = bodyHeight();
      Area a = new Area(new Rectangle2D.Double(-cut, -rimHeight(), cut + rr, rimHeight()));
      a.add(new Area(new Rectangle2D.Double(-r, -(h - r), 2 * r, h - r - rimHeight() + 0.5)));
      a.add(new Area(new Ellipse2D.Double(-r, -h, 2 * r, 2 * r)));
      return a;
    }
    Area a = new Area(new Ellipse2D.Double(-rr, -rr, 2 * rr, 2 * rr));
    a.subtract(new Area(new Rectangle2D.Double(-rr - 1, -rr, rr - cut + 1, 2 * rr)));
    return a;
  }

  public Area[] getBody() {
    if (body == null) {
      body = new Area[] {localBody().createTransformedArea(localToCanvas())};
    }
    return body;
  }

  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, IDrawingObserver drawingObserver) {
    if (checkPointsClipped(g2d.getClip())) {
      return;
    }
    Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
        Constants.DEFAULT_THEME);
    boolean selected =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING;
    AffineTransform t = localToCanvas();

    // leads, drawn first so that they show through the clear lens
    float thickness = getClosestOdd(LEAD_THICKNESS.convertToPixels());
    // the stroked shape comes out about 1px thicker than the stroke, as in AbstractLeadedComponent
    Stroke leadStroke =
        ObjectCache.getInstance().fetchStroke(Math.max(1f, thickness - 1), BasicStroke.CAP_ROUND);
    Color leadFill = selected ? SELECTION_COLOR : getLeadColor();
    for (int i = 0; i < 4; i++) {
      Point2D exit = t.transform(leadExit(i), null);
      Shape lead = leadStroke.createStrokedShape(new Line2D.Double(exit, controlPoints[i]));
      g2d.setColor(leadFill);
      g2d.fill(lead);
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));
      g2d.setColor(leadFill.darker());
      g2d.draw(lead);
    }
    drawingObserver.stopTracking();

    Area mainArea = getBody()[0];
    Composite oldComposite = applyAlpha(g2d, componentState);
    g2d.setColor(outlineMode ? Constants.TRANSPARENT_COLOR : getBodyColor());
    drawingObserver.startTracking();
    g2d.fill(mainArea);
    drawingObserver.stopTracking();
    g2d.setComposite(oldComposite);

    Color border;
    if (outlineMode) {
      border = selected ? SELECTION_COLOR : theme.getOutlineColor();
    } else {
      border = selected ? SELECTION_COLOR : getBorderColor();
    }

    if (!outlineMode) {
      drawLeadFrame(g2d, t);
    }

    g2d.setColor(border);
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
    g2d.draw(mainArea);
    if (getView() == View.TOP) {
      double r = lensRadius();
      g2d.draw(t.createTransformedShape(new Ellipse2D.Double(-r, -r, 2 * r, 2 * r)));
    } else {
      double r = lensRadius();
      g2d.draw(t.createTransformedShape(new Line2D.Double(-r, -rimHeight(), r, -rimHeight())));
    }

    drawLabels(g2d, componentState, outlineMode, project, theme, t);
  }

  private void drawLeadFrame(Graphics2D g2d, AffineTransform t) {
    double s = scale();
    double post = px(0.5) * s;
    double die = px(0.4) * s;
    String order = getPinOrder();
    int commonIndex = -1;
    if (order.length() == 4) {
      for (int i = 0; i < 4; i++) {
        if (isCommon(order.charAt(i))) {
          commonIndex = i;
        }
      }
    }
    if (commonIndex < 0) {
      commonIndex = 1;
    }
    Color frameBorder = FRAME_COLOR.darker();
    g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1f));

    if (getView() == View.SIDE) {
      double base = -rimHeight();
      double postTop = base - px(2.4) * s;
      double cupTop = base - px(3.2) * s;
      double cupW = px(1.5) * s;
      Point2D[] tops = new Point2D[4];
      for (int i = 0; i < 4; i++) {
        double x = leadX(i);
        Shape p;
        if (i == commonIndex) {
          Area a = new Area(new Rectangle2D.Double(x - post / 2, cupTop, post, -cupTop));
          a.add(new Area(new Rectangle2D.Double(x - cupW / 2, cupTop, cupW, px(0.6) * s)));
          p = a;
          tops[i] = new Point2D.Double(x, cupTop);
        } else {
          p = new Rectangle2D.Double(x - post / 2, postTop, post, -postTop);
          tops[i] = new Point2D.Double(x, postTop);
        }
        Shape cp = t.createTransformedShape(p);
        g2d.setColor(FRAME_COLOR);
        g2d.fill(cp);
        g2d.setColor(frameBorder);
        g2d.draw(cp);
      }
      // dies on the cup and bond wires to the other posts
      double cx = leadX(commonIndex);
      int d = 0;
      for (int i = 0; i < 4; i++) {
        if (i == commonIndex) {
          continue;
        }
        double dx = cx + (d - 1) * die * 1.2;
        Shape dieShape = t.createTransformedShape(
            new Rectangle2D.Double(dx - die / 2, cupTop - die, die, die));
        g2d.setColor(dieColor(order, i, d));
        g2d.fill(dieShape);
        g2d.setColor(Color.darkGray);
        g2d.draw(t.createTransformedShape(
            new Line2D.Double(dx, cupTop - die, tops[i].getX(), tops[i].getY())));
        d++;
      }
      return;
    }

    // top view: thin posts and the wider cup carrying the three dies
    double len = px(1.6) * s;
    double cupW = px(1.3) * s;
    Point2D[] tips = new Point2D[4];
    for (int i = 0; i < 4; i++) {
      double x = leadX(i);
      double w = i == commonIndex ? cupW : post;
      Shape p = t.createTransformedShape(new Rectangle2D.Double(x - w / 2, -len / 2, w, len));
      g2d.setColor(FRAME_COLOR);
      g2d.fill(p);
      g2d.setColor(frameBorder);
      g2d.draw(p);
      tips[i] = new Point2D.Double(x, -len / 2);
    }
    double cx = leadX(commonIndex);
    int d = 0;
    for (int i = 0; i < 4; i++) {
      if (i == commonIndex) {
        continue;
      }
      double dy = (d - 1) * die * 1.3;
      Shape dieShape =
          t.createTransformedShape(new Rectangle2D.Double(cx - die / 2, dy - die / 2, die, die));
      g2d.setColor(dieColor(order, i, d));
      g2d.fill(dieShape);
      g2d.setColor(Color.darkGray);
      g2d.draw(t.createTransformedShape(new Line2D.Double(cx, dy, tips[i].getX(), tips[i].getY())));
      d++;
    }
  }

  private Color dieColor(String order, int lead, int fallback) {
    char c = order.length() == 4 ? order.charAt(lead) : ' ';
    if (c == 'R') {
      return DIE_COLORS[0];
    }
    if (c == 'G') {
      return DIE_COLORS[1];
    }
    if (c == 'B') {
      return DIE_COLORS[2];
    }
    return DIE_COLORS[fallback % 3];
  }

  private void drawLabels(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
      Project project, Theme theme, AffineTransform t) {
    boolean selected =
        componentState == ComponentState.SELECTED || componentState == ComponentState.DRAGGING;
    g2d.setFont(project.getFont());
    if (getFontSizeOverride() != null) {
      g2d.setFont(g2d.getFont().deriveFont(1f * getFontSizeOverride()));
    }
    Color labelColor;
    if (outlineMode) {
      labelColor = selected ? LABEL_COLOR_SELECTED : theme.getOutlineColor();
    } else {
      labelColor = selected ? LABEL_COLOR_SELECTED : getLabelColor();
    }
    g2d.setColor(labelColor);
    // canvas direction from the body towards the leads
    Point2D o = t.transform(new Point2D.Double(0, 0), null);
    Point2D v1 = t.transform(new Point2D.Double(0, 1), null);
    double vx = v1.getX() - o.getX();
    double vy = v1.getY() - o.getY();

    TransistorDisplay display = getDisplay();
    if (display == TransistorDisplay.PINOUT) {
      Font f = g2d.getFont();
      g2d.setFont(f.deriveFont((float) (f.getSize() * 0.7)));
      double gap = px(1.2);
      for (int i = 0; i < 4; i++) {
        Point2D p = controlPoints[i];
        drawText(g2d, getPinLabel(i), p.getX() + vx * gap, p.getY() + vy * gap, vx, vy);
      }
      return;
    }
    String label;
    if (display == TransistorDisplay.NONE) {
      return;
    } else if (display == TransistorDisplay.VALUE) {
      label = getValue();
    } else if (display == TransistorDisplay.BOTH) {
      label = getName() + "  " + getValue();
    } else {
      label = getName();
    }
    if (label == null || label.length() == 0) {
      return;
    }
    // past the end of the body, away from the leads
    double extent = getView() == View.SIDE ? bodyHeight() : rimRadius();
    Point2D anchor = t.transform(new Point2D.Double(0, -extent - px(0.8)), null);
    drawText(g2d, label, anchor.getX(), anchor.getY(), -vx, -vy);
  }

  /** Draws text next to (x, y), pushed away from that point in the direction (dx, dy). */
  private void drawText(Graphics2D g2d, String text, double x, double y, double dx, double dy) {
    FontMetrics fm = g2d.getFontMetrics(g2d.getFont());
    double w = fm.getStringBounds(text, g2d).getWidth();
    double tx = x - w / 2;
    double ty = y + (fm.getAscent() - fm.getDescent()) / 2.0;
    if (dx > 0.5) {
      tx = x;
    } else if (dx < -0.5) {
      tx = x - w;
    }
    if (dy > 0.5) {
      ty = y + fm.getAscent();
    } else if (dy < -0.5) {
      ty = y - fm.getDescent();
    }
    g2d.drawString(text, (float) tx, (float) ty);
  }

  public void drawIcon(Graphics2D g2d, int width, int height) {
    int cx = width / 2;
    int r = width * 7 / 32;
    int top = height * 3 / 32;
    int rimY = top + r * 3;
    g2d.setColor(LEAD_COLOR.darker());
    for (int i = 0; i < 4; i++) {
      int x = cx + (i * 2 - 3) * width / 16;
      g2d.drawLine(x, rimY, cx + (i * 2 - 3) * width / 10, height - 2);
    }
    // body seen from the side
    Area a = new Area(new Ellipse2D.Double(cx - r, top, 2 * r, 2 * r));
    a.add(new Area(new Rectangle2D.Double(cx - r, top + r, 2 * r, 2 * r)));
    a.add(new Area(new Rectangle2D.Double(cx - r - 2, rimY - 2, 2 * r + 4, 3)));
    g2d.setColor(BODY_COLOR);
    g2d.fill(a);
    g2d.setColor(BORDER_COLOR);
    g2d.draw(a);
    int die = Math.max(2, width / 14);
    for (int i = 0; i < 3; i++) {
      g2d.setColor(DIE_COLORS[i]);
      g2d.fillRect(cx + (i - 1) * (die + 1) - die / 2, top + r + 2, die, die);
    }
  }

  public enum CommonType {

    CATHODE("Common cathode"), ANODE("Common anode");

    private String label;

    private CommonType(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }

  public enum View {

    TOP("Top"), SIDE("Side");

    private String label;

    private View(String label) {
      this.label = label;
    }

    @Override
    public String toString() {
      return label;
    }
  }
}
