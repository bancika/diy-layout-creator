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
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.text.AttributedCharacterIterator;
import java.util.HashMap;
import java.util.Map;
import org.diylc.core.IDrawingObserver;
import com.bancika.gerberwriter.DataLayer;

public class GerberG2DWrapper extends Graphics2D implements IDrawingObserver, IGerberDrawingObserver {

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

  public GerberG2DWrapper(Graphics2D graphics2d, String diylcVersion) {
    super();
    this.graphics2d = graphics2d;
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
    if (trackingGerber) {
      currentLayers.entrySet().forEach(entry -> {
        double width = 1;
        if (stroke != null && stroke instanceof BasicStroke) {
          width = ((BasicStroke) stroke).getLineWidth();
        }
        DataLayer dataLayer = layerMap.get(entry.getKey());
        GerberExporter.outputPathOutline(s.getPathIterator(null), dataLayer,
            Double.isNaN(tolerance) ? CURVE_APPROXIMATION_TOLERANCE : tolerance, entry.getValue().negative, entry.getValue().function,
            width);
      });
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
  public void drawRenderableImage(RenderableImage img, AffineTransform xform) {

  }

  @Override
  public void drawString(String str, int x, int y) {
    drawString(str, (float) x, (float) y);
  }

  @Override
  public void drawString(String str, float x, float y) {
    TextLayout layout = new TextLayout(str, getFont(), graphics2d.getFontRenderContext());
    Shape shape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
    fill(shape);
  }

  @Override
  public void drawString(AttributedCharacterIterator iterator, int x, int y) {}

  @Override
  public void drawString(AttributedCharacterIterator iterator, float x, float y) {}

  @Override
  public void drawGlyphVector(GlyphVector g, float x, float y) {}

  @Override
  public void fill(Shape s) {
    if (trackingGerber) {
      currentLayers.entrySet().forEach(entry -> {
        DataLayer dataLayer = layerMap.get(entry.getKey());
        GerberExporter.outputPathArea(s.getPathIterator(null), dataLayer,
            Double.isNaN(tolerance) ? CURVE_APPROXIMATION_TOLERANCE : tolerance, entry.getValue().negative, entry.getValue().function);
      });
    }
  }

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
  public void setStroke(Stroke s) {
    this.stroke = s;
  }

  @Override
  public void setRenderingHint(Key hintKey, Object hintValue) {
    // TODO Auto-generated method stub

  }

  @Override
  public Object getRenderingHint(Key hintKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setRenderingHints(Map<?, ?> hints) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addRenderingHints(Map<?, ?> hints) {
    // TODO Auto-generated method stub

  }

  @Override
  public RenderingHints getRenderingHints() {
    // TODO Auto-generated method stub
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
    return tx;
  }

  @Override
  public Paint getPaint() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Composite getComposite() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setBackground(Color color) {
    // TODO Auto-generated method stub

  }

  @Override
  public Color getBackground() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Stroke getStroke() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clip(Shape s) {
    // TODO Auto-generated method stub

  }

  @Override
  public FontRenderContext getFontRenderContext() {
    return graphics2d.getFontRenderContext();
  }

  @Override
  public Graphics create() {
    // TODO Auto-generated method stub
    return null;
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
  public void setPaintMode() {
    // TODO Auto-generated method stub

  }

  @Override
  public void setXORMode(Color c1) {
    // TODO Auto-generated method stub

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
  public Rectangle getClipBounds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clipRect(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setClip(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public Shape getClip() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setClip(Shape clip) {
    // TODO Auto-generated method stub

  }

  @Override
  public void copyArea(int x, int y, int width, int height, int dx, int dy) {
    // TODO Auto-generated method stub

  }

  @Override
  public void drawLine(int x1, int y1, int x2, int y2) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fillRect(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearRect(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
    // TODO Auto-generated method stub

  }

  @Override
  public void drawOval(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fillOval(int x, int y, int width, int height) {
    // TODO Auto-generated method stub

  }

  @Override
  public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
    // TODO Auto-generated method stub

  }

  @Override
  public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) {
    // TODO Auto-generated method stub

  }

  @Override
  public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
    fill(new Polygon(xPoints, yPoints, nPoints));
  }

  @Override
  public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor,
      ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2,
      int sy2, ImageObserver observer) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2,
      int sy2, Color bgcolor, ImageObserver observer) {
    // TODO Auto-generated method stub
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
