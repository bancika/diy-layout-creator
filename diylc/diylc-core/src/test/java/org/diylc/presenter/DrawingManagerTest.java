package org.diylc.presenter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.diylc.appframework.miscutils.InMemoryConfigurationManager;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.PCBLayer;
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
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false,
        Collections.emptySet(), 1.0, 1.0, null);

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

    // the mirror fades through the wrapper, so the component still sees its own composite
    assertEquals(2, component.capturedAlphas.size());
    assertEquals(1.0f, component.capturedAlphas.get(0), 0.01f);
    assertEquals(1.0f, component.capturedAlphas.get(1), 0.01f);
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
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false,
        Collections.emptySet(), 1.0, 1.0, null);

    g2d.dispose();

    assertEquals(2, component.capturedTransforms.size());

    // Mirrored drawing: ABOVE reflection across Y = minY - offset/2 = 100 - 20 = 80
    // Expected y' = 2 * 80 - 150 = 160 - 150 = 10
    AffineTransform mirrorTx = component.capturedTransforms.get(1);
    Point2D mirroredP = mirrorTx.transform(new Point2D.Double(150, 150), null);
    assertEquals(150.0, mirroredP.getX(), 0.001);
    assertEquals(10.0, mirroredP.getY(), 0.001);

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
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false,
        Collections.emptySet(), 1.0, 1.0, null);

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
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false,
        Collections.emptySet(), 1.0, 1.0, null);

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
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false,
        Collections.emptySet(), 1.0, 1.0, null);

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

  @Test
  public void testOffScreenComponentSkippedOnceAreaIsTracked() {
    Project project = new Project();
    DrawCountingComponent component = new DrawCountingComponent();
    project.getComponents().add(component);
    DrawingManager drawingManager = createDrawingManager();

    drawVisibleRect(drawingManager, project, new Rectangle2D.Double(0, 0, 100, 100));
    // the first pass records the area, so it has to draw even though the component is off-screen
    assertEquals(1, component.drawCount);

    drawVisibleRect(drawingManager, project, new Rectangle2D.Double(0, 0, 100, 100));
    assertEquals(1, component.drawCount);

    drawVisibleRect(drawingManager, project, new Rectangle2D.Double(400, 400, 200, 200));
    assertEquals(2, component.drawCount);

    drawVisibleRect(drawingManager, project, null);
    assertEquals(3, component.drawCount);
  }

  @Test
  public void testOffScreenComponentDrawnAgainAfterInvalidation() {
    Project project = new Project();
    DrawCountingComponent component = new DrawCountingComponent();
    project.getComponents().add(component);
    DrawingManager drawingManager = createDrawingManager();

    drawVisibleRect(drawingManager, project, new Rectangle2D.Double(0, 0, 100, 100));
    drawingManager.invalidateComponent(component);
    drawVisibleRect(drawingManager, project, new Rectangle2D.Double(0, 0, 100, 100));

    assertEquals(2, component.drawCount);
  }

  @Test
  public void testUntrackedDrawingKeepsComponentOnScreen() {
    Project project = new Project();
    DrawCountingComponent component = new DrawCountingComponent();
    component.labelPosition = new Point2D.Double(50, 50);
    project.getComponents().add(component);
    DrawingManager drawingManager = createDrawingManager();

    drawVisibleRect(drawingManager, project, new Rectangle2D.Double(0, 0, 100, 100));
    drawVisibleRect(drawingManager, project, new Rectangle2D.Double(0, 0, 100, 100));

    // the label lies outside the selection area but inside the visible rect
    assertEquals(2, component.drawCount);
  }

  @Test
  public void testOffScreenCachedComponentContributesContinuity() {
    Project project = new Project();
    CachedConductorComponent component = new CachedConductorComponent();
    project.getComponents().add(component);
    DrawingManager drawingManager = createDrawingManager();

    drawCachedAndClipped(drawingManager, project, 100);
    // the netlist and continuity checks are built from the areas recorded while painting
    assertEquals(1, drawingManager.getContinuityAreas(project).size());
    assertEquals(1, component.drawCount);

    drawCachedAndClipped(drawingManager, project, 100);
    assertEquals(1, component.drawCount);
  }

  @Test
  public void testCachedComponentRedrawnOnlyAfterInvalidation() {
    Project project = new Project();
    CachedConductorComponent component = new CachedConductorComponent();
    project.getComponents().add(component);
    DrawingManager drawingManager = createDrawingManager();

    drawCachedAndClipped(drawingManager, project, 1000);
    drawCachedAndClipped(drawingManager, project, 1000);
    assertEquals(1, component.drawCount);

    // the cache does not look for changes itself, whoever changes a component invalidates it
    component.setTestField("changed");
    drawCachedAndClipped(drawingManager, project, 1000);
    assertEquals(1, component.drawCount);

    DrawingCache.Instance.invalidate(component);
    drawCachedAndClipped(drawingManager, project, 1000);
    assertEquals(2, component.drawCount);
  }

  @Test
  public void testMovingComponentBypassesCache() {
    Project project = new Project();
    CachedConductorComponent component = new CachedConductorComponent();
    project.getComponents().add(component);
    DrawingManager drawingManager = createDrawingManager();

    // a component moved along by a drag is drawn directly on every frame...
    drawCachedAndClipped(drawingManager, project, 1000, Set.of(component));
    drawCachedAndClipped(drawingManager, project, 1000, Set.of(component));
    assertEquals(2, component.drawCount);

    // ...and leaves nothing in the cache, so the first ordinary repaint renders it afresh
    drawCachedAndClipped(drawingManager, project, 1000);
    assertEquals(3, component.drawCount);
  }

  private void drawCachedAndClipped(DrawingManager drawingManager, Project project, int size) {
    drawCachedAndClipped(drawingManager, project, size, Collections.emptySet());
  }

  private void drawCachedAndClipped(DrawingManager drawingManager, Project project, int size,
      Set<IDIYComponent<?>> movingComponents) {
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    // CanvasPanel clips its buffer to the visible rect before drawing
    g2d.setClip(0, 0, size, size);
    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM, DrawOption.ENABLE_CACHING),
        null, null, Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null,
        null, false, movingComponents, 1.0, 1.0, new Rectangle2D.Double(0, 0, size, size));
    g2d.dispose();
  }

  @Test
  public void testOutlineAreaUnaffectedByLaterContinuityShapes() {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    G2DWrapper wrapper =
        new G2DWrapper(img.createGraphics(), 1.0, EnumSet.noneOf(DrawOption.class));

    wrapper.startedDrawingComponent(0);
    wrapper.startTrackingContinuityArea(true);
    wrapper.setContinuityMarker("lead");
    // the first shape counts towards both the outline and the marker's continuity area, the second
    // only towards the continuity area
    wrapper.fill(new Rectangle2D.Double(0, 0, 10, 10));
    wrapper.stopTracking();
    wrapper.fill(new Rectangle2D.Double(50, 50, 10, 10));
    wrapper.stopTrackingContinuityArea();
    ComponentArea area = wrapper.finishedDrawingComponent();

    assertEquals(new Rectangle2D.Double(0, 0, 10, 10), area.getOutlineArea().getBounds2D());
    assertEquals(1, area.getContinuityPositiveAreas().size());
    assertEquals(new Rectangle2D.Double(0, 0, 60, 60),
        area.getContinuityPositiveAreas().iterator().next().getBounds2D());
  }

  @Test
  public void testOutlineAreaFollowsComponentTransform() {
    BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
    G2DWrapper wrapper =
        new G2DWrapper(img.createGraphics(), 1.0, EnumSet.noneOf(DrawOption.class));

    wrapper.startedDrawingComponent(0);
    wrapper.translate(100, 50);
    wrapper.rotate(Math.PI / 2);
    wrapper.fill(new Rectangle2D.Double(0, 0, 20, 10));
    ComponentArea area = wrapper.finishedDrawingComponent();

    // rotated a quarter turn about the translated origin, the 20 x 10 rectangle spans
    // x 90..100 and y 50..70
    Rectangle2D bounds = area.getOutlineArea().getBounds2D();
    assertEquals(90, bounds.getMinX(), 1e-6);
    assertEquals(100, bounds.getMaxX(), 1e-6);
    assertEquals(50, bounds.getMinY(), 1e-6);
    assertEquals(70, bounds.getMaxY(), 1e-6);
  }

  @Test
  public void testRecordedShapeUnaffectedByLaterChanges() {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    G2DWrapper wrapper =
        new G2DWrapper(img.createGraphics(), 1.0, EnumSet.noneOf(DrawOption.class));

    wrapper.startedDrawingComponent(0);
    Rectangle2D shape = new Rectangle2D.Double(0, 0, 10, 10);
    wrapper.fill(shape);
    // shapes are merged into areas only when asked for, so the recorded one must be a copy
    shape.setRect(50, 50, 10, 10);
    ComponentArea area = wrapper.finishedDrawingComponent();

    assertEquals(new Rectangle2D.Double(0, 0, 10, 10), area.getOutlineArea().getBounds2D());
  }

  @Test
  public void testOutlineBoundsMatchMergedArea() {
    BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
    G2DWrapper wrapper =
        new G2DWrapper(img.createGraphics(), 1.0, EnumSet.noneOf(DrawOption.class));

    wrapper.startedDrawingComponent(0);
    wrapper.fill(new Rectangle2D.Double(0, 0, 10, 10));
    wrapper.translate(100, 50);
    wrapper.fill(new java.awt.geom.Ellipse2D.Double(0, 0, 30, 20));
    ComponentArea area = wrapper.finishedDrawingComponent();

    Rectangle2D merged = area.getOutlineArea().getBounds2D();
    Rectangle2D bounds = area.getOutlineBounds();
    assertEquals(merged.getMinX(), bounds.getMinX(), 1e-6);
    assertEquals(merged.getMinY(), bounds.getMinY(), 1e-6);
    assertEquals(merged.getMaxX(), bounds.getMaxX(), 1e-6);
    assertEquals(merged.getMaxY(), bounds.getMaxY(), 1e-6);
  }

  @Test
  public void testContinuityShapesMergedPerMarker() {
    BufferedImage img = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
    G2DWrapper wrapper =
        new G2DWrapper(img.createGraphics(), 1.0, EnumSet.noneOf(DrawOption.class));

    wrapper.startedDrawingComponent(0);
    wrapper.startTrackingContinuityArea(true);
    wrapper.setContinuityMarker("pad");
    wrapper.fill(new Rectangle2D.Double(0, 0, 10, 10));
    wrapper.fill(new Rectangle2D.Double(5, 0, 10, 10));
    // without a marker each shape is an area of its own
    wrapper.setContinuityMarker(null);
    wrapper.fill(new Rectangle2D.Double(100, 0, 10, 10));
    wrapper.fill(new Rectangle2D.Double(150, 0, 10, 10));
    wrapper.stopTrackingContinuityArea();
    ComponentArea area = wrapper.finishedDrawingComponent();

    assertEquals(3, area.getContinuityPositiveAreas().size());
    boolean merged = false;
    for (Area a : area.getContinuityPositiveAreas()) {
      merged |= a.contains(2, 5) && a.contains(13, 5);
    }
    assertTrue(merged);
  }

  @Test
  public void testOutlineAreaIsUnionOfTrackedShapes() {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    G2DWrapper wrapper =
        new G2DWrapper(img.createGraphics(), 1.0, EnumSet.noneOf(DrawOption.class));

    wrapper.startedDrawingComponent(0);
    wrapper.fill(new Rectangle2D.Double(0, 0, 10, 10));
    wrapper.fill(new Rectangle2D.Double(50, 50, 10, 10));
    ComponentArea area = wrapper.finishedDrawingComponent();

    assertTrue(area.getOutlineArea().contains(5, 5));
    assertTrue(area.getOutlineArea().contains(55, 55));
    assertTrue(!area.getOutlineArea().contains(30, 30));
  }

  @Test
  public void testContinuityCalculationLeavesRecordedAreasIntact() {
    Project project = new Project();
    ConductorComponent strip = new ConductorComponent(new Rectangle2D.Double(0, 0, 100, 10), true);
    ConductorComponent cut = new ConductorComponent(new Rectangle2D.Double(40, 0, 20, 10), false);
    project.getComponents().add(strip);
    project.getComponents().add(cut);
    DrawingManager drawingManager = createDrawingManager();
    drawVisibleRect(drawingManager, project, null);

    // the cut splits the strip in two for the netlist...
    assertEquals(2, drawingManager.getContinuityAreas(project).size());
    // ...but the strip's own recorded area must stay whole, so that moving or deleting the cut
    // does not leave a hole behind
    Area recorded =
        drawingManager.getComponentArea(strip).getContinuityPositiveAreas().iterator().next();
    assertTrue(recorded.contains(50, 5));
  }

  @Test
  public void testBottomSideFadedOnlyWithOption() {
    Project project = new Project();
    project.getComponents().add(new SidePaintingComponent(new Rectangle2D.Double(150, 150, 10, 10), null));
    project.getComponents().add(new SidePaintingComponent(new Rectangle2D.Double(200, 200, 10, 10), PCBLayer._1));
    project.getComponents().add(new SidePaintingComponent(new Rectangle2D.Double(250, 250, 10, 10), PCBLayer._3));

    BufferedImage faded = renderSides(project, EnumSet.of(DrawOption.ZOOM, DrawOption.FADE_FAR_SIDE));
    assertSolid(faded, 155, 155);
    assertFaded(faded, 205, 205);
    // inner layers count as the bottom side
    assertFaded(faded, 255, 255);

    BufferedImage plain = renderSides(project, EnumSet.of(DrawOption.ZOOM));
    assertSolid(plain, 205, 205);
    assertSolid(plain, 255, 255);
  }

  @Test
  public void testBothSidesNeverFaded() {
    Project project = new Project();
    MockBoard board = new MockBoard();
    board.setBoardRectangle(new Rectangle2D.Double(100, 100, 200, 300));
    board.setUndersideDisplay(BoardUndersideDisplay.BELOW);
    board.setUndersideOffset(new Size(20.0, SizeUnit.px));
    project.getComponents().add(board);
    project.getComponents().add(SidePaintingComponent.bothSides(new Rectangle2D.Double(150, 150, 10, 10)));

    BufferedImage img = renderSides(project, EnumSet.of(DrawOption.ZOOM, DrawOption.FADE_FAR_SIDE));
    assertSolid(img, 155, 155);
    assertSolid(img, 155, 665);
  }

  @Test
  public void testLockedBottomSideNotFadedTwice() {
    Project project = new Project();
    SidePaintingComponent bottom = new SidePaintingComponent(new Rectangle2D.Double(200, 200, 10, 10), PCBLayer._1);
    project.getComponents().add(bottom);

    DrawingManager drawingManager = createDrawingManager();
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    drawingManager.drawProject(g2d, project,
        EnumSet.of(DrawOption.ZOOM, DrawOption.FADE_FAR_SIDE, DrawOption.LOCKED_ALPHA), null, null,
        Collections.emptyList(), Collections.singleton(bottom), Collections.emptySet(), null, null,
        false, Collections.emptySet(), 1.0, 1.0, null);
    g2d.dispose();

    // the stronger of the 50% locked alpha and the far-side alpha decides, not their product
    assertFaded(img, 205, 205, Math.min(0.5f, farSideAlpha()));
  }

  @Test
  public void testMirrorShowsCopperSideSolid() {
    Project project = new Project();
    MockBoard board = new MockBoard();
    board.setBoardRectangle(new Rectangle2D.Double(100, 100, 200, 300));
    board.setUndersideDisplay(BoardUndersideDisplay.BELOW);
    board.setUndersideOffset(new Size(20.0, SizeUnit.px));
    board.setUndersideTransparency(true);
    project.getComponents().add(board);
    project.getComponents().add(new SidePaintingComponent(new Rectangle2D.Double(150, 150, 10, 10), null));
    project.getComponents().add(new SidePaintingComponent(new Rectangle2D.Double(200, 200, 10, 10), PCBLayer._1));

    // reflection across y = 410 puts the top-side square at y 660..670 and the bottom one at 610..620
    BufferedImage img = renderSides(project, EnumSet.of(DrawOption.ZOOM, DrawOption.FADE_FAR_SIDE));
    assertFaded(img, 155, 665);
    assertSolid(img, 205, 615);

    board.setUndersideTransparency(false);
    img = renderSides(project, EnumSet.of(DrawOption.ZOOM, DrawOption.FADE_FAR_SIDE));
    assertSolid(img, 155, 665);
    assertSolid(img, 205, 615);
  }

  @Test
  public void testFarSideFadeKeepsComponentComposite() {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D canvas = img.createGraphics();
    G2DWrapper wrapper = new G2DWrapper(canvas, 1.0, EnumSet.of(DrawOption.FADE_FAR_SIDE));
    float farSideAlpha = farSideAlpha();

    Composite half = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    wrapper.setComposite(half);
    wrapper.startDrawingSide(PCBLayer._1);
    assertSame(half, wrapper.getComposite());
    // the stronger of the two fades wins rather than multiplying
    assertEquals(Math.min(0.5f, farSideAlpha), ((AlphaComposite) canvas.getComposite()).getAlpha(), 0.001f);

    wrapper.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
    assertEquals(Math.min(0.25f, farSideAlpha), ((AlphaComposite) canvas.getComposite()).getAlpha(), 0.001f);

    wrapper.setComposite(half);
    wrapper.stopDrawingSide();
    assertSame(half, canvas.getComposite());
    canvas.dispose();
  }

  @Test
  public void testFarSideNotFadedInOutlineMode() {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D canvas = img.createGraphics();
    Composite original = canvas.getComposite();
    G2DWrapper wrapper = new G2DWrapper(canvas, 1.0,
        EnumSet.of(DrawOption.FADE_FAR_SIDE, DrawOption.OUTLINE_MODE));
    wrapper.startDrawingSide(PCBLayer._1);
    assertSame(original, canvas.getComposite());
    wrapper.stopDrawingSide();
    canvas.dispose();
  }

  @Test
  public void testFarSideFadeLeavesContinuityUnchanged() {
    Area faded = drawBottomConductor(EnumSet.of(DrawOption.FADE_FAR_SIDE));
    Area plain = drawBottomConductor(EnumSet.noneOf(DrawOption.class));
    assertTrue(faded.equals(plain));
    assertEquals(new Rectangle2D.Double(10, 10, 30, 20), faded.getBounds2D());
  }

  private Area drawBottomConductor(Set<DrawOption> drawOptions) {
    BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
    Graphics2D canvas = img.createGraphics();
    G2DWrapper wrapper = new G2DWrapper(canvas, 1.0, drawOptions);
    wrapper.startedDrawingComponent(0);
    wrapper.startDrawingSide(PCBLayer._1);
    wrapper.startTrackingContinuityArea(true);
    wrapper.fill(new Rectangle2D.Double(10, 10, 30, 20));
    wrapper.stopTrackingContinuityArea();
    wrapper.stopDrawingSide();
    ComponentArea componentArea = wrapper.finishedDrawingComponent();
    canvas.dispose();
    Area area = new Area();
    for (Area a : componentArea.getContinuityPositiveAreas()) {
      area.add(a);
    }
    return area;
  }

  private BufferedImage renderSides(Project project, Set<DrawOption> drawOptions) {
    DrawingManager drawingManager = createDrawingManager();
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    drawingManager.drawProject(g2d, project, drawOptions, null, null, Collections.emptyList(),
        Collections.emptySet(), Collections.emptySet(), null, null, false, Collections.emptySet(),
        1.0, 1.0, null);
    g2d.dispose();
    return img;
  }

  @Test
  public void testCutAppliesAfterStripIsRedrawn() {
    Project project = new Project();
    ConductorComponent strip = new ConductorComponent(new Rectangle2D.Double(0, 0, 100, 10), true);
    ConductorComponent cut = new ConductorComponent(new Rectangle2D.Double(40, 0, 20, 10), false);
    project.getComponents().add(strip);
    project.getComponents().add(cut);
    DrawingManager drawingManager = createDrawingManager();
    drawVisibleRect(drawingManager, project, null);

    // an edit to the board records its areas again, after those of the cuts already on it
    drawingManager.invalidateComponent(strip);
    drawVisibleRect(drawingManager, project, null);

    assertEquals(2, drawingManager.getContinuityAreas(project).size());
  }

  @Test
  public void testCutOutOfViewAppliesWhenAreasAreRecorded() {
    Project project = new Project();
    project.getComponents()
        .add(new ConductorComponent(new Rectangle2D.Double(0, 0, 300, 10), true));
    project.getComponents()
        .add(new SelfCullingConductorComponent(new Rectangle2D.Double(240, 0, 20, 10), false));
    DrawingManager drawingManager = createDrawingManager();

    drawCachedAndClipped(drawingManager, project, 100);

    assertEquals(2, drawingManager.getContinuityAreas(project).size());
  }

  @Test
  public void testHighlightFollowsAreasRecordedAfterIt() {
    Project project = new Project();
    project.getComponents()
        .add(new ConductorComponent(new Rectangle2D.Double(0, 0, 100, 10), true));
    DrawingManager drawingManager = createDrawingManager();
    drawVisibleRect(drawingManager, project, null);
    drawingManager.findContinuityAreaAtPoint(project, new Point2D.Double(10, 5));

    // nothing was edited, the cut was only drawn for the first time, e.g. scrolled into view
    project.getComponents()
        .add(new ConductorComponent(new Rectangle2D.Double(40, 0, 20, 10), false));
    drawVisibleRect(drawingManager, project, null);
    drawingManager.findContinuityAreaAtPoint(project, new Point2D.Double(10, 5));

    BufferedImage img = render(drawingManager, project);
    assertTrue(img.getRGB(10, 5) != img.getRGB(80, 5));
  }

  private BufferedImage render(DrawingManager drawingManager, Project project) {
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM), null, null,
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false,
        Collections.emptySet(), 1.0, 1.0, null);
    g2d.dispose();
    return img;
  }

  private static float farSideAlpha() {
    return (float) (DrawingManager.FAR_SIDE_ALPHA.getValue().doubleValue() / 100d);
  }

  private static int red(BufferedImage img, int x, int y) {
    return new Color(img.getRGB(x, y)).getRed();
  }

  // the squares are painted black, so the red channel alone shows how much of the background is left
  private static void assertSolid(BufferedImage img, int x, int y) {
    assertEquals(0, red(img, x, y));
  }

  private static void assertFaded(BufferedImage img, int x, int y) {
    assertFaded(img, x, y, farSideAlpha());
  }

  private static void assertFaded(BufferedImage img, int x, int y, float alpha) {
    double background = red(img, 5, 5);
    assertEquals(background * (1 - alpha), red(img, x, y), 3);
  }

  private void drawVisibleRect(DrawingManager drawingManager, Project project,
      Rectangle2D visibleRect) {
    BufferedImage img = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = img.createGraphics();
    drawingManager.drawProject(g2d, project, EnumSet.of(DrawOption.ZOOM), null, null,
        Collections.emptyList(), Collections.emptySet(), Collections.emptySet(), null, null, false,
        Collections.emptySet(), 1.0, 1.0, visibleRect);
    g2d.dispose();
  }

  public static class DrawCountingComponent extends MockDIYComponent {

    private static final long serialVersionUID = 1L;

    public int drawCount = 0;
    public Point2D labelPosition = null;

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
        Project project, IDrawingObserver drawingObserver) {
      drawCount++;
      g2d.fill(new Rectangle2D.Double(500, 500, 20, 20));
      if (labelPosition != null) {
        drawingObserver.stopTracking();
        g2d.drawString("R1", (float) labelPosition.getX(), (float) labelPosition.getY());
      }
    }
  }

  @ComponentDescriptor(name = "CachedConductor", author = "test", category = "Test",
      instanceNamePrefix = "C", description = "Cached conductive component for testing",
      zOrder = IDIYComponent.COMPONENT, enableCache = true)
  public static class CachedConductorComponent extends MockDIYComponent {

    private static final long serialVersionUID = 1L;

    public int drawCount = 0;
    private Point2D controlPoint = new Point2D.Double(500, 500);

    @Override
    public int getControlPointCount() {
      return 1;
    }

    @Override
    public Point2D getControlPoint(int index) {
      return controlPoint;
    }

    @Override
    public Rectangle2D getCachingBounds() {
      return new Rectangle2D.Double(490, 490, 40, 40);
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
        Project project, IDrawingObserver drawingObserver) {
      drawCount++;
      drawingObserver.startTrackingContinuityArea(true);
      g2d.fill(new Rectangle2D.Double(500, 500, 20, 20));
      drawingObserver.stopTrackingContinuityArea();
    }
  }

  public static class ConductorComponent extends MockDIYComponent {

    private static final long serialVersionUID = 1L;

    private Rectangle2D shape;
    private boolean positive;

    public ConductorComponent(Rectangle2D shape, boolean positive) {
      this.shape = shape;
      this.positive = positive;
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
        Project project, IDrawingObserver drawingObserver) {
      drawingObserver.startTrackingContinuityArea(positive);
      g2d.fill(shape);
      drawingObserver.stopTrackingContinuityArea();
    }
  }

  // paints an opaque black square, declared on the given side or, when null, left undeclared
  public static class SidePaintingComponent extends MockDIYComponent {

    private static final long serialVersionUID = 1L;

    private Rectangle2D shape;
    private PCBLayer side;
    private boolean bothSides;

    public SidePaintingComponent(Rectangle2D shape, PCBLayer side) {
      this.shape = shape;
      this.side = side;
    }

    public static SidePaintingComponent bothSides(Rectangle2D shape) {
      SidePaintingComponent component = new SidePaintingComponent(shape, null);
      component.bothSides = true;
      return component;
    }

    @Override
    public int getControlPointCount() {
      return 1;
    }

    @Override
    public Point2D getControlPoint(int index) {
      return new Point2D.Double(shape.getCenterX(), shape.getCenterY());
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
        Project project, IDrawingObserver drawingObserver) {
      if (bothSides) {
        drawingObserver.startDrawingBothSides();
      } else if (side != null) {
        drawingObserver.startDrawingSide(side);
      }
      g2d.setColor(Color.black);
      g2d.fill(shape);
      drawingObserver.stopDrawingSide();
    }
  }

  // skips drawing when clipped out, as TraceCut and the boards do
  public static class SelfCullingConductorComponent extends ConductorComponent {

    private static final long serialVersionUID = 1L;

    private Point2D center;

    public SelfCullingConductorComponent(Rectangle2D shape, boolean positive) {
      super(shape, positive);
      this.center = new Point2D.Double(shape.getCenterX(), shape.getCenterY());
    }

    @Override
    public int getControlPointCount() {
      return 1;
    }

    @Override
    public Point2D getControlPoint(int index) {
      return center;
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
        Project project, IDrawingObserver drawingObserver) {
      if (checkPointsClipped(g2d.getClip())) {
        return;
      }
      super.draw(g2d, componentState, outlineMode, project, drawingObserver);
    }
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

