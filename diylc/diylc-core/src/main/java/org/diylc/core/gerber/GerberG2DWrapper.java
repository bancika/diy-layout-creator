package org.diylc.core.gerber;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;

import com.bancika.gerberwriter.DataLayer;
import com.bancika.gerberwriter.Point;
import com.bancika.gerberwriter.padmasters.Circle;

import org.diylc.core.IDrawingObserver;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

public class GerberG2DWrapper extends Graphics2D
    implements IDrawingObserver, IGerberDrawingObserver {

  private static final double CURVE_APPROXIMATION_TOLERANCE = 1d;

  private Map<GerberLayer, DataLayer> layerMap;
  private boolean trackingGerber = false;
  private Map<GerberLayer, GerberProperties> currentLayers =
      new HashMap<GerberLayer, GerberProperties>();
  private String diylcVersion;

  private Graphics2D graphics2d;
  private Color color;
  private Stroke stroke;
  private Font font;
  private AffineTransform tx;
  private double tolerance;

  private Rectangle2D boardRect;

  public GerberG2DWrapper(Graphics2D graphics2d, String diylcVersion, Rectangle2D boardRect) {
    super();
    this.graphics2d = graphics2d;
    this.boardRect = boardRect;
    this.layerMap = new HashMap<GerberLayer, DataLayer>();
    this.diylcVersion = diylcVersion;
  }

  public void startedDrawingComponent() {
    this.tx = new AffineTransform();
    this.tolerance = Double.NaN;
  }

  public boolean isTrackingGerber() {
    return trackingGerber;
  }

  @Override
  public void stopTracking() {
    // TODO Auto-generated method stub

  }

  @Override
  public void startTracking() {
    // TODO Auto-generated method stub

  }

  @Override
  public void startTrackingContinuityArea(boolean positive) {
    // TODO Auto-generated method stub

  }

  @Override
  public void stopTrackingContinuityArea() {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isTrackingContinuityArea() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void setContinuityMarker(String marker) {
    // TODO Auto-generated method stub

  }

  @Override
  public void startGerberOutput(GerberLayer layer, String function, boolean negative) {
    layerMap.computeIfAbsent(layer, k -> layer.buildLayer(diylcVersion));
    currentLayers.put(layer, new GerberProperties(function, negative));
    trackingGerber = true;
  }

  @Override
  public void setGerberFunction(GerberLayer layer, String function) {
    currentLayers.get(layer).function = function;
  }

  @Override
  public void setGerberNegative(GerberLayer layer, boolean negative) {
    currentLayers.get(layer).negative = negative;
  }

  @Override
  public void setApproximationToleranceOverride(double tolerance) {
    this.tolerance = tolerance;
    // TODO Auto-generated method stub

  }

  @Override
  public void stopGerberOutput(GerberLayer layer) {
    currentLayers.remove(layer);
    if (currentLayers.isEmpty()) {
      trackingGerber = false;
    }
  }

  @Override
  public void stopGerberOutput() {
    currentLayers.clear();
    trackingGerber = false;
  }

  public Map<GerberLayer, DataLayer> getLayerMap() {
    return layerMap;
  }

  @Override
  public void draw(Shape s) {
    if (trackingGerber && !getColor().equals(Constants.TRANSPARENT_COLOR)) {
      currentLayers.entrySet().forEach(entry -> {
        double width = 1;
        if (stroke != null && stroke instanceof BasicStroke) {
          width = ((BasicStroke) stroke).getLineWidth();
        }
        DataLayer dataLayer = layerMap.get(entry.getKey());
        AffineTransform finalTx = getFinalTx(entry.getKey().isMirrored());
        GerberPathRenderer.outputPathOutline(s.getPathIterator(finalTx), dataLayer,
            Double.isNaN(tolerance) ? CURVE_APPROXIMATION_TOLERANCE : tolerance,
            entry.getValue().negative, entry.getValue().function, width);
      });
    }
  }

  @Override
  public void drawString(String str, float x, float y) {
    if (str == null || str.trim().isEmpty()) {
      return;
    }

    
    GlyphVector gv = font.createGlyphVector(graphics2d.getFontRenderContext(), str);
    
    AffineTransform translateInstance = AffineTransform.getTranslateInstance(x, y);

    for (int i = 0; i < gv.getNumGlyphs(); i++) {
        Shape glyphShape = gv.getGlyphOutline(i);
        
        fillShape(translateInstance.createTransformedShape(glyphShape));
    }
    
//    Font f = getFont();
//    TextLayout layout = new TextLayout(str, f, graphics2d.getFontRenderContext());
//    Shape shape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
//    fillShape(shape);
  }

  @Override
  public void fill(Shape s) {
    if (s instanceof Ellipse2D
        && (((Ellipse2D) s).getWidth() - ((Ellipse2D) s).getHeight()) < 0.1) {
      Ellipse2D e = (Ellipse2D) s;
      fillFlash(e);
    } else {
      fillShape(s);
    }
  }

  public void fillShape(Shape s) {
    if (trackingGerber && !getColor().equals(Constants.TRANSPARENT_COLOR)) {
      currentLayers.entrySet().forEach(entry -> {
        DataLayer dataLayer = layerMap.get(entry.getKey());
        AffineTransform finalTx = getFinalTx(entry.getKey().isMirrored());
        GerberPathRenderer.outputPathArea(s.getPathIterator(finalTx), dataLayer,
            Double.isNaN(tolerance) ? CURVE_APPROXIMATION_TOLERANCE : tolerance,
            entry.getValue().negative, entry.getValue().function);
      });
    }
  }

  private void fillFlash(Ellipse2D c) {
    if (trackingGerber && !getColor().equals(Constants.TRANSPARENT_COLOR)) {
      currentLayers.entrySet().forEach(entry -> {
        DataLayer dataLayer = layerMap.get(entry.getKey());
        AffineTransform finalTx = getFinalTx(entry.getKey().isMirrored());
        if (Math.abs(c.getWidth() - c.getHeight()) < 0.1 && finalTx.getShearX() == 0
            && finalTx.getShearY() == 0
            && Math.abs(Math.abs(finalTx.getScaleX()) - Math.abs(finalTx.getScaleY())) < 0.0001) {
          
          final double d = Math.abs(c.getWidth() * finalTx.getScaleX()) * SizeUnit.px.getFactor();
          Circle circle = new Circle(d, entry.getValue().function, entry.getValue().negative);
          dataLayer.addPad(circle, new Point(
              (c.getX() * finalTx.getScaleX() + finalTx.getTranslateX()) * SizeUnit.px.getFactor()
                + (entry.getKey().isMirrored() ? -1 : 1) * d / 2,
              (c.getY() * finalTx.getScaleY() + finalTx.getTranslateY()) * SizeUnit.px.getFactor()
                  - d / 2));
        } else {
          // not a circle after transformation, default to polygon approximation
          fillShape(c);
        }
      });
    }
  }

  private AffineTransform getFinalTx(boolean isMirrored) {
    AffineTransform finalTx = new AffineTransform();
    if (isMirrored) {
      finalTx.translate(boardRect.getWidth(), 0);
      finalTx.scale(-1, 1);
    }
    finalTx.scale(1, -1);
    finalTx.translate(-boardRect.getMinX(), -boardRect.getMaxY());
    finalTx.concatenate(tx);
    return finalTx;
  }

  @Override
  public RenderingHints getRenderingHints() {
    return null;
  }

  @Override
  public void translate(int x, int y) {
    this.tx.translate(x, y);
  }

  @Override
  public void translate(double tx, double ty) {
    this.tx.translate(tx, ty);
  }

  @Override
  public void rotate(double theta) {
    this.tx.rotate(theta);
  }

  @Override
  public void rotate(double theta, double x, double y) {
    this.tx.rotate(theta, x, y);
  }

  @Override
  public void scale(double sx, double sy) {
    this.tx.scale(sx, sy);
  }

  @Override
  public void shear(double shx, double shy) {
    this.tx.shear(shx, shy);
  }

  @Override
  public void transform(AffineTransform Tx) {
    this.tx.concatenate(Tx);
  }

  @Override
  public void setTransform(AffineTransform Tx) {
    this.tx = Tx;
  }

  @Override
  public AffineTransform getTransform() {
    return new AffineTransform(tx);
  }

  @Override
  public FontRenderContext getFontRenderContext() {
    return graphics2d.getFontRenderContext();
  }

  @Override
  public Color getColor() {
    return color;
  }

  @Override
  public void setColor(Color c) {
    this.color = c;
  }

  @Override
  public Font getFont() {
    return this.font;
  }

  @Override
  public void setFont(Font font) {
    this.font = font;
  }

  @Override
  public FontMetrics getFontMetrics(Font f) {
    return graphics2d.getFontMetrics(f);
  }

  @Override
  public void setStroke(Stroke s) {
    this.stroke = s;
  }

  @Override
  public Stroke getStroke() {
    return stroke;
  }

  @Override
  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    fillShape(new Polygon(xPoints, yPoints, nPoints));
  }

  @Override
  public void drawString(String str, int x, int y) {
    drawString(str, (float) x, (float) y);
  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2) {
    draw(new Line2D.Double(x1, y1, x2, y2));
  }

  @Override
  public void fillRect(int x, int y, int width, int height) {
    fillShape(new Rectangle2D.Double(x, y, width, height));
  }

  @Override
  public void drawOval(int x, int y, int width, int height) {
    draw(new Ellipse2D.Double(x, y, width, height));
  }

  @Override
  public void fillOval(int x, int y, int width, int height) {
    if (width == height) {
      fillFlash(new Ellipse2D.Double(x, y, width, height));
    } else {
      fillShape(new Ellipse2D.Double(x, y, width, height));
    }
  }

  @Override
  public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
    return false;
  }

  @Override
  public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {}

  @Override
  public void drawRenderedImage(RenderedImage img, AffineTransform xform) {}

  @Override
  public void drawRenderableImage(RenderableImage img, AffineTransform xform) {}

  @Override
  public void drawString(AttributedCharacterIterator iterator, int x, int y) {}

  @Override
  public void drawString(AttributedCharacterIterator iterator, float x, float y) {}

  @Override
  public void drawGlyphVector(GlyphVector g, float x, float y) {}

  @Override
  public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
    return false;
  }

  @Override
  public GraphicsConfiguration getDeviceConfiguration() {
    return null;
  }

  @Override
  public void setComposite(Composite comp) {}

  @Override
  public void setPaint(Paint paint) {}

  @Override
  public void setRenderingHint(Key hintKey, Object hintValue) {}

  @Override
  public Object getRenderingHint(Key hintKey) {
    return null;
  }

  @Override
  public void setRenderingHints(Map<?, ?> hints) {}

  @Override
  public void addRenderingHints(Map<?, ?> hints) {}

  @Override
  public Paint getPaint() {
    return null;
  }

  @Override
  public Composite getComposite() {
    return null;
  }

  @Override
  public void setBackground(Color color) {}

  @Override
  public Color getBackground() {
    return null;
  }

  @Override
  public void clip(Shape s) {}

  @Override
  public Graphics create() {
    return null;
  }

  @Override
  public void setPaintMode() {}

  @Override
  public void setXORMode(Color c1) {}

  @Override
  public Rectangle getClipBounds() {
    return null;
  }

  @Override
  public void clipRect(int x, int y, int width, int height) {}

  @Override
  public void setClip(int x, int y, int width, int height) {}

  @Override
  public Shape getClip() {
    return null;
  }

  @Override
  public void setClip(Shape clip) {}

  @Override
  public void copyArea(int x, int y, int width, int height, int dx, int dy) {}

  @Override
  public void clearRect(int x, int y, int width, int height) {}

  @Override
  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {}

  @Override
  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {}

  @Override
  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}

  @Override
  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {}

  @Override
  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {}

  @Override
  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {}

  @Override
  public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
      ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2,
      int sy2, ImageObserver observer) {
    return false;
  }

  @Override
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2,
      int sy2, Color bgcolor, ImageObserver observer) {
    return false;
  }

  @Override
  public void dispose() {}

  static class GerberProperties {
    String function;
    boolean negative;

    public GerberProperties(String function, boolean negative) {
      super();
      this.function = function;
      this.negative = negative;
    }
  }
}
