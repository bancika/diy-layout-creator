/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.diylc.common.HorizontalAlignment;
import org.diylc.common.Orientation;
import org.diylc.common.PCBLayer;
import org.diylc.common.VerticalAlignment;
import org.diylc.components.AbstractComponent;
import org.diylc.components.transform.TextTransformer;
import org.diylc.core.ComponentState;
import org.diylc.core.GerberLayer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ILayeredComponent;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.gerber.IGerberComponent;
import org.diylc.core.measures.SizeUnit;
import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.GerberFunctions;
import com.bancika.gerberwriter.Point;
import com.bancika.gerberwriter.path.Path;

@ComponentDescriptor(name = "PCB Text", author = "Branislav Stojkovic", category = "Misc",
    description = "Mirrored text for PCB artwork", instanceNamePrefix = "L", zOrder = IDIYComponent.TRACE,
    flexibleZOrder = false, bomPolicy = BomPolicy.NEVER_SHOW, transformer = TextTransformer.class)
public class PCBText extends AbstractComponent<Void> implements ILayeredComponent, IGerberComponent {

  public static String DEFAULT_TEXT = "Double click to edit text";

  public static Font DEFAULT_FONT = new Font("Courier New", Font.BOLD, 15);

  private static final long serialVersionUID = 1L;

  private Point2D.Double point = new Point2D.Double(0, 0);
  private String text = DEFAULT_TEXT;
  private Font font = DEFAULT_FONT;
  private Color color = LABEL_COLOR;
  private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
  private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;
  private Orientation orientation = Orientation.DEFAULT;
  
  private PCBLayer layer = PCBLayer._1;
  
  private transient Rectangle2D boundingRect;

  @SuppressWarnings("incomplete-switch")
  @Override
  public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
      IDrawingObserver drawingObserver) {

    g2d.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED : color);
    g2d.setFont(font);
    
//    FontRenderContext frc = new FontRenderContext(null, false, true);
//    TextLayout layout = new TextLayout(getText(), getFont(), frc);
//    Rectangle2D rect = layout.getBounds();
    
    FontMetrics fontMetrics = g2d.getFontMetrics();
    // hack to store bounding rect, due to inconsistencies between methods that calculate it
    // to be used for gerber export
    boundingRect = fontMetrics.getStringBounds(text, g2d);

    int textHeight = (int) boundingRect.getHeight();
    int textWidth = (int) boundingRect.getWidth();

    double x = point.getX();
    double y = point.getY();
    switch (getVerticalAlignment()) {
      case CENTER:
        y = point.getY() - textHeight / 2 + fontMetrics.getAscent();
        break;
      case TOP:
        y = point.getY() - textHeight + fontMetrics.getAscent();
        break;
      case BOTTOM:
        y = point.getY() + fontMetrics.getAscent();
        break;
      default:
        throw new RuntimeException("Unexpected alignment: " + getVerticalAlignment());
    }
    switch (getHorizontalAlignment()) {
      case CENTER:
        x = point.getX() - textWidth / 2;
        break;
      case LEFT:
        x = point.getX();
        break;
      case RIGHT:
        x = point.getX() - textWidth;
        break;
      default:
        throw new RuntimeException("Unexpected alignment: " + getHorizontalAlignment());
    }

    switch (getOrientation()) {
      case _90:
        g2d.rotate(Math.PI / 2, point.getX(), point.getY());
        break;
      case _180:
        g2d.rotate(Math.PI, point.getX(), point.getY());
        break;
      case _270:
        g2d.rotate(Math.PI * 3 / 2, point.getX(), point.getY());
        break;
    }
    
    AffineTransform oldTx = g2d.getTransform();

    // Flip horizontally
    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    tx.translate(-2 * x - textWidth, 0);
    g2d.transform(tx);

    g2d.drawString(text, (int)x, (int)y);
    
    g2d.setTransform(oldTx);
  }

  @Override
  public void drawIcon(Graphics2D g2d, int width, int height) {
    g2d.setColor(LABEL_COLOR);
    g2d.setFont(DEFAULT_FONT.deriveFont(15f * width / 32).deriveFont(Font.BOLD));

    FontMetrics fontMetrics = g2d.getFontMetrics();
    Rectangle2D rect = fontMetrics.getStringBounds("Abc", g2d);

    int textHeight = (int) (rect.getHeight());
    int textWidth = (int) (rect.getWidth());

    // Center text horizontally and vertically.
    int x = (width - textWidth) / 2 + 1;
    int y = (height - textHeight) / 2 + fontMetrics.getAscent();
    g2d.scale(-1, 1);
    g2d.translate(-width, 0);

    g2d.drawString("Abc", x, y);
  }

  @EditableProperty
  public Font getFont() {
    return font;
  }

  public void setFont(Font font) {
    this.font = font;
  }

  // Bold and italic fields are named to be alphabetically after Font. This is
  // important!

  @EditableProperty(name = "Font Bold")
  public boolean getBold() {
    return font.isBold();
  }

  public void setBold(boolean bold) {
    if (bold) {
      if (font.isItalic()) {
        font = font.deriveFont(Font.BOLD + Font.ITALIC);
      } else {
        font = font.deriveFont(Font.BOLD);
      }
    } else {
      if (font.isItalic()) {
        font = font.deriveFont(Font.ITALIC);
      } else {
        font = font.deriveFont(Font.PLAIN);
      }
    }
  }

  @EditableProperty(name = "Font Italic")
  public boolean getItalic() {
    return font.isItalic();
  }

  public void setItalic(boolean italic) {
    if (italic) {
      if (font.isBold()) {
        font = font.deriveFont(Font.BOLD + Font.ITALIC);
      } else {
        font = font.deriveFont(Font.ITALIC);
      }
    } else {
      if (font.isBold()) {
        font = font.deriveFont(Font.BOLD);
      } else {
        font = font.deriveFont(Font.PLAIN);
      }
    }
  }

  @EditableProperty
  public Orientation getOrientation() {
    if (orientation == null) {
      orientation = Orientation.DEFAULT;
    }
    return orientation;
  }

  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
  }

  @EditableProperty(name = "Font Size")
  public int getFontSize() {
    return font.getSize();
  }

  public void setFontSize(int size) {
    font = font.deriveFont((float) size);
  }

  @Override
  public int getControlPointCount() {
    return 1;
  }

  @Override
  public Point2D getControlPoint(int index) {
    return point;
  }

  @Override
  public boolean isControlPointSticky(int index) {
    return false;
  }

  @Override
  public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
    return VisibilityPolicy.WHEN_SELECTED;
  }

  @Override
  public void setControlPoint(Point2D point, int index) {
    this.point.setLocation(point);
  }

  @EditableProperty(defaultable = false)
  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  @EditableProperty
  public Color getColor() {
    return color;
  }

  public void setColor(Color color) {
    this.color = color;
  }

  @EditableProperty(name = "Vertical Alignment")
  public VerticalAlignment getVerticalAlignment() {
    if (verticalAlignment == null) {
      verticalAlignment = VerticalAlignment.CENTER;
    }
    return verticalAlignment;
  }

  public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
    this.verticalAlignment = verticalAlignment;
  }

  @EditableProperty(name = "Horizontal Alignment")
  public HorizontalAlignment getHorizontalAlignment() {
    if (horizontalAlignment == null) {
      horizontalAlignment = HorizontalAlignment.CENTER;
    }
    return horizontalAlignment;
  }

  public void setHorizontalAlignment(HorizontalAlignment alignment) {
    this.horizontalAlignment = alignment;
  }
  
  @EditableProperty
  public PCBLayer getLayer() {
    if (layer == null) {
      layer = PCBLayer._1;
    }
    return layer;
  }

  public void setLayer(PCBLayer layer) {
    this.layer = layer;
  }

  @Override
  public String getName() {
    return super.getName();
  }

  @Override
  public Void getValue() {
    return null;
  }

  @Override
  public void setValue(Void value) {}
  
  @Override
  public String getControlPointNodeName(int index) {
    return null;
  }

  @SuppressWarnings("incomplete-switch")
  @Override
  public void drawToGerber(DataLayer dataLayer) {
    if (!dataLayer.getFunction().equals("Copper,L" + getLayerId() + ",Top,Signal")) {
      return;
    }
    FontRenderContext frc = new FontRenderContext(null, false, true);
    TextLayout layout = new TextLayout(getText(), getFont(), frc);
    
    // hack to reuse bounding rect from drawing to the screen as layou.getBounds produces slightly different results
    Rectangle2D rect = Optional.ofNullable(boundingRect).orElse(layout.getBounds());
    
    AffineTransform tx = new AffineTransform();

    int textHeight = (int) rect.getHeight();
    int textWidth = (int) rect.getWidth();

    double x = point.getX();
    double y = point.getY();
    switch (getVerticalAlignment()) {
      case CENTER:
        y = point.getY() - textHeight / 2 + layout.getAscent();
        break;
      case TOP:
        y = point.getY() - textHeight + layout.getAscent();
        break;
      case BOTTOM:
        y = point.getY() + layout.getAscent();
        break;
      default:
        throw new RuntimeException("Unexpected alignment: " + getVerticalAlignment());
    }
    switch (getHorizontalAlignment()) {
      case CENTER:
        x = point.getX() - textWidth / 2;
        break;
      case LEFT:
        x = point.getX();
        break;
      case RIGHT:
        x = point.getX() - textWidth;
        break;
      default:
        throw new RuntimeException("Unexpected alignment: " + getHorizontalAlignment());
    }

    switch (getOrientation()) {
      case _90:
        tx.rotate(Math.PI / 2, point.getX(), point.getY());
        break;
      case _180:
        tx.rotate(Math.PI, point.getX(), point.getY());
        break;
      case _270:
        tx.rotate(Math.PI * 3 / 2, point.getX(), point.getY());
        break;
    }
    
    // Flip horizontally
    tx.scale(-1, 1);
    tx.translate(-2 * x - textWidth, 0);
    tx.translate(x, y);
    
    Shape outline = layout.getOutline(tx);
    
    PathIterator pathIterator = outline.getPathIterator(null);
    
    double d = 1;
    Color c = getColor();
    // treat light colors as negative etched into a ground plane
    float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
    subdivide(pathIterator, dataLayer, d, hsb[2] > 0.5);
  }

  @Override
  public List<GerberLayer> getGerberLayers() {
    List<GerberLayer> layers = new ArrayList<GerberLayer>();
    layers.add(new GerberLayer("Copper,L" + getLayerId() + ",Top,Signal", "gtl"));
    return layers;
  }

  @Override
  public int getLayerId() {
    return 1;
  }
  
  protected void subdivide(PathIterator pathIterator, DataLayer dataLayer, double d, boolean isNegative) {
    double x = 0;
    double y = 0;
    Path path = null;
    Path2D lastPath = null;
    Area lastArea = null;
    boolean currentIsNegative = isNegative;
    while (!pathIterator.isDone()) {
      double[] coords = new double[6];
      int operation = pathIterator.currentSegment(coords);
      switch (operation) {
        case PathIterator.SEG_MOVETO:
          path = new Path();
          lastPath = new Path2D.Double();
          lastPath.moveTo(coords[0], coords[1]);
          path.moveTo(new Point(-coords[0] * SizeUnit.px.getFactor(), -coords[1] * SizeUnit.px.getFactor()));
          x = coords[0];
          y = coords[1];
          break;
        case PathIterator.SEG_LINETO:
          lastPath.lineTo(coords[0], coords[1]);
          path.lineTo(new Point(-coords[0] * SizeUnit.px.getFactor(), -coords[1] * SizeUnit.px.getFactor()));
          x = coords[0];
          y = coords[1];
          break;
        case PathIterator.SEG_CLOSE:
          lastPath.closePath();
          if (lastArea == null) {
            dataLayer.addRegion(path, GerberFunctions.CONDUCTOR, currentIsNegative);
          } else {
            lastArea.intersect(new Area(lastPath));
            if (lastArea.isEmpty()) {
              currentIsNegative = isNegative;
            } else {
              currentIsNegative = !currentIsNegative;
            }
            dataLayer.addRegion(path, GerberFunctions.CONDUCTOR, currentIsNegative);
          }
          lastArea = new Area(lastPath);
          path = null;
          break;
        case PathIterator.SEG_CUBICTO:
          lastPath.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          CubicCurve2D curve1 = new CubicCurve2D.Double(x, y, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
          subdivide(curve1, path, d);
          x = coords[4];
          y = coords[5];
          break;
        case PathIterator.SEG_QUADTO:
          lastPath.curveTo(coords[0], coords[1], (coords[0] + 2 * coords[2]) / 3, (coords[3] + 2 * coords[1]) / 3, coords[2], coords[3]);
          QuadCurve2D curve2 = new QuadCurve2D.Double(x, y, coords[0], coords[1], coords[2], coords[3]);
          subdivide(curve2, path, d);
          x = coords[2];
          y = coords[3];
          break;
      }
      pathIterator.next();
    }    
  }
  
  private void subdivide(CubicCurve2D curve, Path path, double d) {
    if (/*curve.getFlatness() < d || */new Point2D.Double(curve.getX1(), curve.getY1()).distance(curve.getX2(), curve.getY2()) < d) {
//      path.lineTo(new Point(curve.getX1() * SizeUnit.px.getFactor(), curve.getY1() * SizeUnit.px.getFactor()));
      path.lineTo(new Point(-curve.getX2() * SizeUnit.px.getFactor(), -curve.getY2() * SizeUnit.px.getFactor()));
      return;
    }
    CubicCurve2D left = new CubicCurve2D.Double();
    CubicCurve2D right = new CubicCurve2D.Double();
    curve.subdivide(left, right);
    subdivide(left, path, d);
    subdivide(right, path, d);
  }
  
  private void subdivide(QuadCurve2D curve, Path path, double d) {
    if (/*curve.getFlatness() < d || */new Point2D.Double(curve.getX1(), curve.getY1()).distance(curve.getX2(), curve.getY2()) < d) {
//      path.lineTo(new Point(curve.getX1() * SizeUnit.px.getFactor(), curve.getY1() * SizeUnit.px.getFactor()));
      path.lineTo(new Point(-curve.getX2() * SizeUnit.px.getFactor(), -curve.getY2() * SizeUnit.px.getFactor()));
      return;
    }
    QuadCurve2D left = new QuadCurve2D.Double();
    QuadCurve2D right = new  QuadCurve2D.Double();
    curve.subdivide(left, right);
    subdivide(left, path, d);
    subdivide(right, path, d);
  }
}
