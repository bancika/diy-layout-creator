package org.diylc.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.core.BoardUndersideDisplay;
import org.diylc.core.ComponentState;
import org.diylc.core.IBoard;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.testcomponents.MockDIYComponent;
import org.junit.Test;

public class DrawingManagerTest {

  @ComponentDescriptor(name = "MockBoard", author = "test", category = "Boards", instanceNamePrefix = "B",
      description = "Mock board for testing", zOrder = IDIYComponent.BOARD)
  public static class MockBoard extends MockDIYComponent implements IBoard {

    private static final long serialVersionUID = 1L;

    private Rectangle2D boardRectangle = new Rectangle2D.Double(100, 100, 200, 300);
    private BoardUndersideDisplay undersideDisplay = BoardUndersideDisplay.BELOW;
    private Size undersideOffset = new Size(20.0, SizeUnit.px);
    private Boolean undersideTransparency = Boolean.TRUE;

    public List<AffineTransform> capturedTransforms = new ArrayList<>();
    public List<Float> capturedAlphas = new ArrayList<>();

    @Override
    public Rectangle2D getBoardRectangle() {
      return boardRectangle;
    }

    public void setBoardRectangle(Rectangle2D boardRectangle) {
      this.boardRectangle = boardRectangle;
    }

    @Override
    public BoardUndersideDisplay getUndersideDisplay() {
      return undersideDisplay;
    }

    public void setUndersideDisplay(BoardUndersideDisplay undersideDisplay) {
      this.undersideDisplay = undersideDisplay;
    }

    @Override
    public Size getUndersideOffset() {
      return undersideOffset;
    }

    public void setUndersideOffset(Size undersideOffset) {
      this.undersideOffset = undersideOffset;
    }

    @Override
    public Boolean getUndersideTransparency() {
      return undersideTransparency;
    }

    public void setUndersideTransparency(Boolean undersideTransparency) {
      this.undersideTransparency = undersideTransparency;
    }

    @Override
    public boolean shouldExportToGerber() {
      return false;
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
        IDrawingObserver drawingObserver) {
      capturedTransforms.add(new AffineTransform(g2d.getTransform()));
      if (g2d.getComposite() instanceof AlphaComposite) {
        capturedAlphas.add(((AlphaComposite) g2d.getComposite()).getAlpha());
      } else {
        capturedAlphas.add(1.0f);
      }
    }
  }

  public static class TransformTrackingComponent extends MockDIYComponent {

    private static final long serialVersionUID = 1L;

    public List<AffineTransform> capturedTransforms = new ArrayList<>();
    public List<Float> capturedAlphas = new ArrayList<>();
    private Point2D controlPoint = new Point2D.Double(150, 150);

    @Override
    public int getControlPointCount() {
      return 1;
    }

    @Override
    public Point2D getControlPoint(int index) {
      return controlPoint;
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode, Project project,
        IDrawingObserver drawingObserver) {
      capturedTransforms.add(new AffineTransform(g2d.getTransform()));
      if (g2d.getComposite() instanceof AlphaComposite) {
        capturedAlphas.add(((AlphaComposite) g2d.getComposite()).getAlpha());
      } else {
        capturedAlphas.add(1.0f);
      }
    }
  }

  private DrawingManager createDrawingManager() {
    return new DrawingManager(new MessageDispatcher<EventType>(false), InMemoryConfigurationManager.getInstance());
  }

  @Test
  public void testMirrorBelow() {
    Project project = new Project();
    MockBoard board = new MockBoard();
    board.setBoardRectangle(new Rectangle2D.Double(100, 100, 200, 300));
    board.setUndersideDisplay(BoardUndersideDisplay.BELOW);
    board.setUndersideOffset(new Size(20.0, SizeUnit.px));
    board.setUndersideTransparency(true);

    TransformTrackingComponent component = new TransformTrackingComponent();
    project.getComponents().add(board);
    project.getComponents().add(component);

    DrawingManager drawingManager = createDrawingManager();
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM), null, null,
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false, 1.0, 1.0, null);

    g2d.dispose();

    // The component should be drawn twice: once normally and once mirrored
    assertEquals(2, component.capturedTransforms.size());

    // Original drawing: identity / no reflection
    AffineTransform normalTx = component.capturedTransforms.get(0);
    Point2D p = new Point2D.Double(150, 150);
    Point2D normalP = normalTx.transform(p, null);
    assertEquals(150.0, normalP.getX(), 0.001);
    assertEquals(150.0, normalP.getY(), 0.001);

    // Mirrored drawing: BELOW reflection across Y = maxY + offset/2 = 400 + 10 = 410
    // Expected y' = 2 * 410 - 150 = 820 - 150 = 670
    AffineTransform mirrorTx = component.capturedTransforms.get(1);
    Point2D mirroredP = mirrorTx.transform(p, null);
    assertEquals(150.0, mirroredP.getX(), 0.001);
    assertEquals(670.0, mirroredP.getY(), 0.001);

    // Check transparency
    assertEquals(2, component.capturedAlphas.size());
    assertEquals(1.0f, component.capturedAlphas.get(0), 0.01f);
    assertEquals(0.5f, component.capturedAlphas.get(1), 0.01f);
  }

  @Test
  public void testMirrorAbove() {
    Project project = new Project();
    MockBoard board = new MockBoard();
    board.setBoardRectangle(new Rectangle2D.Double(100, 100, 200, 300));
    board.setUndersideDisplay(BoardUndersideDisplay.ABOVE);
    board.setUndersideOffset(new Size(40.0, SizeUnit.px));
    board.setUndersideTransparency(false);

    TransformTrackingComponent component = new TransformTrackingComponent();
    project.getComponents().add(board);
    project.getComponents().add(component);

    DrawingManager drawingManager = createDrawingManager();
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM), null, null,
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false, 1.0, 1.0, null);

    g2d.dispose();

    assertEquals(2, component.capturedTransforms.size());

    // Mirrored drawing: ABOVE reflection across Y = minY - offset/2 = 100 - 20 = 80
    // Expected y' = 2 * 80 - 150 = 160 - 150 = 10
    AffineTransform mirrorTx = component.capturedTransforms.get(1);
    Point2D mirroredP = mirrorTx.transform(new Point2D.Double(150, 150), null);
    assertEquals(150.0, mirroredP.getX(), 0.001);
    assertEquals(10.0, mirroredP.getY(), 0.001);

    // Transparency should not be 0.5 when undersideTransparency is false
    assertEquals(1.0f, component.capturedAlphas.get(1), 0.01f);
  }

  @Test
  public void testMirrorLeft() {
    Project project = new Project();
    MockBoard board = new MockBoard();
    board.setBoardRectangle(new Rectangle2D.Double(100, 100, 200, 300));
    board.setUndersideDisplay(BoardUndersideDisplay.LEFT);
    board.setUndersideOffset(new Size(20.0, SizeUnit.px));

    TransformTrackingComponent component = new TransformTrackingComponent();
    project.getComponents().add(board);
    project.getComponents().add(component);

    DrawingManager drawingManager = createDrawingManager();
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM), null, null,
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false, 1.0, 1.0, null);

    g2d.dispose();

    assertEquals(2, component.capturedTransforms.size());

    // Mirrored drawing: LEFT reflection across X = minX - offset/2 = 100 - 10 = 90
    // Expected x' = 2 * 90 - 150 = 180 - 150 = 30
    AffineTransform mirrorTx = component.capturedTransforms.get(1);
    Point2D mirroredP = mirrorTx.transform(new Point2D.Double(150, 150), null);
    assertEquals(30.0, mirroredP.getX(), 0.001);
    assertEquals(150.0, mirroredP.getY(), 0.001);
  }

  @Test
  public void testMirrorRight() {
    Project project = new Project();
    MockBoard board = new MockBoard();
    board.setBoardRectangle(new Rectangle2D.Double(100, 100, 200, 300));
    board.setUndersideDisplay(BoardUndersideDisplay.RIGHT);
    board.setUndersideOffset(new Size(20.0, SizeUnit.px));

    TransformTrackingComponent component = new TransformTrackingComponent();
    project.getComponents().add(board);
    project.getComponents().add(component);

    DrawingManager drawingManager = createDrawingManager();
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM), null, null,
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false, 1.0, 1.0, null);

    g2d.dispose();

    assertEquals(2, component.capturedTransforms.size());

    // Mirrored drawing: RIGHT reflection across X = maxX + offset/2 = 300 + 10 = 310
    // Expected x' = 2 * 310 - 150 = 620 - 150 = 470
    AffineTransform mirrorTx = component.capturedTransforms.get(1);
    Point2D mirroredP = mirrorTx.transform(new Point2D.Double(150, 150), null);
    assertEquals(470.0, mirroredP.getX(), 0.001);
    assertEquals(150.0, mirroredP.getY(), 0.001);
  }

  @Test
  public void testUndersideDisplayNone() {
    Project project = new Project();
    MockBoard board = new MockBoard();
    board.setUndersideDisplay(BoardUndersideDisplay.NONE);

    TransformTrackingComponent component = new TransformTrackingComponent();
    project.getComponents().add(board);
    project.getComponents().add(component);

    DrawingManager drawingManager = createDrawingManager();
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();

    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM), null, null,
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false, 1.0, 1.0, null);

    g2d.dispose();

    // No mirrored drawing
    assertEquals(1, component.capturedTransforms.size());
    assertEquals(1, board.capturedTransforms.size());
  }

  @Test
  public void testTextUnmirroringHorizontal() {
    BufferedImage img = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    final List<AffineTransform> capturedDuringDrawString = new ArrayList<>();
    Graphics2D canvasG2d = new Graphics2DProxy(img.createGraphics()) {
      @Override
      public void drawString(String str, int x, int y) {
        capturedDuringDrawString.add(new AffineTransform(getTransform()));
        super.drawString(str, x, y);
      }

      @Override
      public void drawString(String str, float x, float y) {
        capturedDuringDrawString.add(new AffineTransform(getTransform()));
        super.drawString(str, x, y);
      }
    };

    G2DWrapper wrapper = new G2DWrapper(canvasG2d, 1.0, EnumSet.noneOf(DrawOption.class));

    // Simulate horizontal mirror mode
    wrapper.setMirrorDirection(G2DWrapper.MirrorDirection.HORIZONTAL);
    AffineTransform mirrorTx = new AffineTransform();
    mirrorTx.translate(300, 0);
    mirrorTx.scale(-1.0, 1.0);
    mirrorTx.translate(-300, 0);
    wrapper.transform(mirrorTx);

    wrapper.drawString("10K Log", 200, 200);

    assertEquals(1, capturedDuringDrawString.size());
    AffineTransform textTx = capturedDuringDrawString.get(0);

    // The determinant should be positive (un-mirrored / right-reading text)
    assertTrue("Text transform determinant should be positive (not mirrored)", textTx.getDeterminant() > 0);

    // After drawing, wrapper transform should still be the mirrorTx
    assertEquals(-1.0, wrapper.getTransform().getScaleX(), 0.001);
  }

  @Test
  public void testTextUnmirroringVertical() {
    BufferedImage img = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
    final List<AffineTransform> capturedDuringDrawString = new ArrayList<>();
    Graphics2D canvasG2d = new Graphics2DProxy(img.createGraphics()) {
      @Override
      public void drawString(String str, int x, int y) {
        capturedDuringDrawString.add(new AffineTransform(getTransform()));
        super.drawString(str, x, y);
      }

      @Override
      public void drawString(String str, float x, float y) {
        capturedDuringDrawString.add(new AffineTransform(getTransform()));
        super.drawString(str, x, y);
      }
    };

    G2DWrapper wrapper = new G2DWrapper(canvasG2d, 1.0, EnumSet.noneOf(DrawOption.class));

    // Simulate vertical mirror mode
    wrapper.setMirrorDirection(G2DWrapper.MirrorDirection.VERTICAL);
    AffineTransform mirrorTx = new AffineTransform();
    mirrorTx.translate(0, 300);
    mirrorTx.scale(1.0, -1.0);
    mirrorTx.translate(0, -300);
    wrapper.transform(mirrorTx);

    wrapper.drawString("47uF", 200, 200);

    assertEquals(1, capturedDuringDrawString.size());
    AffineTransform textTx = capturedDuringDrawString.get(0);

    // The determinant should be positive (un-mirrored / right-reading text)
    assertTrue("Text transform determinant should be positive (not mirrored)", textTx.getDeterminant() > 0);

    // After drawing, wrapper transform should still be the mirrorTx
    assertEquals(-1.0, wrapper.getTransform().getScaleY(), 0.001);
  }

  // Simple delegating Graphics2D proxy for testing
  private static class Graphics2DProxy extends Graphics2D {
    private final Graphics2D delegate;

    public Graphics2DProxy(Graphics2D delegate) {
      this.delegate = delegate;
    }

    @Override
    public void draw(java.awt.Shape s) { delegate.draw(s); }
    @Override
    public boolean drawImage(java.awt.Image img, AffineTransform xform, java.awt.image.ImageObserver obs) { return delegate.drawImage(img, xform, obs); }
    @Override
    public void drawImage(BufferedImage img, java.awt.image.BufferedImageOp op, int x, int y) { delegate.drawImage(img, op, x, y); }
    @Override
    public void drawRenderedImage(java.awt.image.RenderedImage img, AffineTransform xform) { delegate.drawRenderedImage(img, xform); }
    @Override
    public void drawRenderableImage(java.awt.image.renderable.RenderableImage img, AffineTransform xform) { delegate.drawRenderableImage(img, xform); }
    @Override
    public void drawString(String str, int x, int y) { delegate.drawString(str, x, y); }
    @Override
    public void drawString(String str, float x, float y) { delegate.drawString(str, x, y); }
    @Override
    public void drawString(java.text.AttributedCharacterIterator iterator, int x, int y) { delegate.drawString(iterator, x, y); }
    @Override
    public void drawString(java.text.AttributedCharacterIterator iterator, float x, float y) { delegate.drawString(iterator, x, y); }
    @Override
    public void drawGlyphVector(java.awt.font.GlyphVector g, float x, float y) { delegate.drawGlyphVector(g, x, y); }
    @Override
    public void fill(java.awt.Shape s) { delegate.fill(s); }
    @Override
    public boolean hit(java.awt.Rectangle rect, java.awt.Shape s, boolean onStroke) { return delegate.hit(rect, s, onStroke); }
    @Override
    public java.awt.GraphicsConfiguration getDeviceConfiguration() { return delegate.getDeviceConfiguration(); }
    @Override
    public void setComposite(java.awt.Composite comp) { delegate.setComposite(comp); }
    @Override
    public void setPaint(java.awt.Paint paint) { delegate.setPaint(paint); }
    @Override
    public void setStroke(java.awt.Stroke s) { delegate.setStroke(s); }
    @Override
    public void setRenderingHint(java.awt.RenderingHints.Key hintKey, Object hintValue) { delegate.setRenderingHint(hintKey, hintValue); }
    @Override
    public Object getRenderingHint(java.awt.RenderingHints.Key hintKey) { return delegate.getRenderingHint(hintKey); }
    @Override
    public void setRenderingHints(java.util.Map<?, ?> hints) { delegate.setRenderingHints(hints); }
    @Override
    public void addRenderingHints(java.util.Map<?, ?> hints) { delegate.addRenderingHints(hints); }
    @Override
    public java.awt.RenderingHints getRenderingHints() { return delegate.getRenderingHints(); }
    @Override
    public void translate(int x, int y) { delegate.translate(x, y); }
    @Override
    public void translate(double tx, double ty) { delegate.translate(tx, ty); }
    @Override
    public void rotate(double theta) { delegate.rotate(theta); }
    @Override
    public void rotate(double theta, double x, double y) { delegate.rotate(theta, x, y); }
    @Override
    public void scale(double sx, double sy) { delegate.scale(sx, sy); }
    @Override
    public void shear(double shx, double shy) { delegate.shear(shx, shy); }
    @Override
    public void transform(AffineTransform Tx) { delegate.transform(Tx); }
    @Override
    public void setTransform(AffineTransform Tx) { delegate.setTransform(Tx); }
    @Override
    public AffineTransform getTransform() { return delegate.getTransform(); }
    @Override
    public java.awt.Paint getPaint() { return delegate.getPaint(); }
    @Override
    public java.awt.Composite getComposite() { return delegate.getComposite(); }
    @Override
    public void setBackground(java.awt.Color color) { delegate.setBackground(color); }
    @Override
    public java.awt.Color getBackground() { return delegate.getBackground(); }
    @Override
    public java.awt.Stroke getStroke() { return delegate.getStroke(); }
    @Override
    public void clip(java.awt.Shape s) { delegate.clip(s); }
    @Override
    public java.awt.font.FontRenderContext getFontRenderContext() { return delegate.getFontRenderContext(); }
    @Override
    public java.awt.Graphics create() { return delegate.create(); }
    @Override
    public java.awt.Color getColor() { return delegate.getColor(); }
    @Override
    public void setColor(java.awt.Color c) { delegate.setColor(c); }
    @Override
    public void setPaintMode() { delegate.setPaintMode(); }
    @Override
    public void setXORMode(java.awt.Color c1) { delegate.setXORMode(c1); }
    @Override
    public java.awt.Font getFont() { return delegate.getFont(); }
    @Override
    public void setFont(java.awt.Font font) { delegate.setFont(font); }
    @Override
    public java.awt.FontMetrics getFontMetrics(java.awt.Font f) { return delegate.getFontMetrics(f); }
    @Override
    public java.awt.Rectangle getClipBounds() { return delegate.getClipBounds(); }
    @Override
    public void clipRect(int x, int y, int width, int height) { delegate.clipRect(x, y, width, height); }
    @Override
    public void setClip(int x, int y, int width, int height) { delegate.setClip(x, y, width, height); }
    @Override
    public java.awt.Shape getClip() { return delegate.getClip(); }
    @Override
    public void setClip(java.awt.Shape clip) { delegate.setClip(clip); }
    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) { delegate.copyArea(x, y, width, height, dx, dy); }
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) { delegate.drawLine(x1, y1, x2, y2); }
    @Override
    public void fillRect(int x, int y, int width, int height) { delegate.fillRect(x, y, width, height); }
    @Override
    public void clearRect(int x, int y, int width, int height) { delegate.clearRect(x, y, width, height); }
    @Override
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) { delegate.drawRoundRect(x, y, width, height, arcWidth, arcHeight); }
    @Override
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) { delegate.fillRoundRect(x, y, width, height, arcWidth, arcHeight); }
    @Override
    public void drawOval(int x, int y, int width, int height) { delegate.drawOval(x, y, width, height); }
    @Override
    public void fillOval(int x, int y, int width, int height) { delegate.fillOval(x, y, width, height); }
    @Override
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) { delegate.drawArc(x, y, width, height, startAngle, arcAngle); }
    @Override
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) { delegate.fillArc(x, y, width, height, startAngle, arcAngle); }
    @Override
    public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints) { delegate.drawPolyline(xPoints, yPoints, nPoints); }
    @Override
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) { delegate.drawPolygon(xPoints, yPoints, nPoints); }
    @Override
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) { delegate.fillPolygon(xPoints, yPoints, nPoints); }
    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, java.awt.image.ImageObserver observer) { return delegate.drawImage(img, x, y, observer); }
    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.image.ImageObserver observer) { return delegate.drawImage(img, x, y, width, height, observer); }
    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) { return delegate.drawImage(img, x, y, bgcolor, observer); }
    @Override
    public boolean drawImage(java.awt.Image img, int x, int y, int width, int height, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) { return delegate.drawImage(img, x, y, width, height, bgcolor, observer); }
    @Override
    public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.image.ImageObserver observer) { return delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer); }
    @Override
    public boolean drawImage(java.awt.Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, java.awt.Color bgcolor, java.awt.image.ImageObserver observer) { return delegate.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, bgcolor, observer); }
    @Override
    public void dispose() { delegate.dispose(); }
  }
}

