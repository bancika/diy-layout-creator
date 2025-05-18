/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.presenter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.appframework.simplemq.MessageDispatcher;

import org.diylc.common.*;
import org.diylc.components.AbstractTransparentComponent;
import org.diylc.core.*;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.netlist.ContinuityGraph;
import org.diylc.netlist.NetlistBuilder;
import org.diylc.utils.Constants;

/**
 * Utility that deals with painting {@link Project} on the {@link Graphics2D} and keeps areas taken
 * by each drawn component.
 * 
 * @author Branislav Stojkovic
 */
public class DrawingManager {

  private static final Logger LOG = Logger.getLogger(DrawingManager.class);
  public static final byte MIRROR_ALPHA = 64;

  public static int CONTROL_POINT_SIZE = 7;
  public static double EXTRA_SPACE = 0.25;

  public static final String ZOOM_KEY = "zoom";

  private static boolean SHADE_EXTRA_SPACE = true;

  public static String DEBUG_COMPONENT_AREAS = "org.diylc.debugComponentAreas";
  public static String DEBUG_CONTINUITY_AREAS = "org.diylc.debugContinuityAreas";

  public static Color CONTROL_POINT_COLOR = Color.blue;
  public static Color SELECTED_CONTROL_POINT_COLOR = Color.green;

  private Theme theme;

  // Maps keyed by object reference.
  // Keeps Area object of each drawn component.
  private Map<IDIYComponent<?>, ComponentArea> componentAreaMap;
  // Maps components to the last state they are drawn in. Also, used to
  // determine which components are invalidated when they are not in the map.
  private Map<IDIYComponent<?>, ComponentState> lastDrawnStateMap;

  private List<Area> currentContinuityAreas;
  private ContinuityGraph continuityGraphCache = null;

  private Composite slotComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
  private Composite lockedComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
  private List<IDIYComponent<?>> failedComponents = new ArrayList<IDIYComponent<?>>();

  private double zoomLevel = 1d;// configManager.readDouble(ZOOM_KEY,
  // 1d);

  private MessageDispatcher<EventType> messageDispatcher;

  private boolean debugComponentAreas;
  private boolean debugContinuityAreas;

  // rendering stats
  private Map<String, Counter> renderStatsByType = new HashMap<String, Counter>();
  private long lastStatsReportedTime = System.currentTimeMillis();
  private long statReportFrequencyMs = 1000 * 60;
  private Counter totalStats = new Counter();

  private IConfigurationManager<?> configManager;

  private List<Area> proximityMarkers = null;

  public DrawingManager(MessageDispatcher<EventType> messageDispatcher,
      IConfigurationManager<?> configManager) {
    super();
    this.messageDispatcher = messageDispatcher;
    this.configManager = configManager;

    try {
      this.theme = (Theme) configManager.readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    } catch (Exception e) {
      LOG.error("Error loading theme", e);
      this.theme = Constants.DEFAULT_THEME;
      // replace bad value with default
      configManager.writeValue(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
    }

    componentAreaMap = new LinkedHashMap<IDIYComponent<?>, ComponentArea>();
    lastDrawnStateMap = new HashMap<IDIYComponent<?>, ComponentState>();
    String debugComponentAreasStr = System.getProperty(DEBUG_COMPONENT_AREAS);
    debugComponentAreas =
        debugComponentAreasStr != null && debugComponentAreasStr.equalsIgnoreCase("true");

    String debugContinuityAreasStr = System.getProperty(DEBUG_CONTINUITY_AREAS);
    debugContinuityAreas =
        debugContinuityAreasStr != null && debugContinuityAreasStr.equalsIgnoreCase("true");
  }

  /**
   * Paints the project onto the canvas and returns the list of components that failed to draw.
   * 
   * @param g2d
   * @param project
   * @param drawOptions
   * @param filter
   * @param selectionRect
   * @param selectedComponents
   * @param lockedComponents
   * @param groupedComponents
   * @param controlPointSlot
   * @param componentSlot
   * @param dragInProgress
   * @param externalZoom
   * @param scaleFactor 
   * @param visibleRect
   * @return
   */
  public List<IDIYComponent<?>> drawProject(Graphics2D g2d, Project project,
      Set<DrawOption> drawOptions, IComponentFilter filter, Rectangle selectionRect,
      Collection<IDIYComponent<?>> selectedComponents, Set<IDIYComponent<?>> lockedComponents,
      Set<IDIYComponent<?>> groupedComponents, List<Point2D> controlPointSlot,
      List<IDIYComponent<?>> componentSlot, boolean dragInProgress, Double externalZoom,
      Double scaleFactor, Rectangle2D visibleRect) {
    long totalStartTime = System.nanoTime();
    failedComponents.clear();
    if (project == null) {
      return failedComponents;
    }

    double zoom = 1d;
    if (drawOptions.contains(DrawOption.ZOOM)) {
      zoom = zoomLevel;
    } else {
      zoom = 1 / Constants.PIXEL_SIZE;
    }

    if (externalZoom != null)
      zoom *= externalZoom;

    G2DWrapper g2dWrapper = new G2DWrapper(g2d, zoom);

    configureRenderingHints(g2d, drawOptions);

    // group components with boards that contain them
    Map<IBoard, List<IDIYComponent<?>>> boardMap = project.getComponents().stream()
            .filter(IBoard.class::isInstance)
            .map(IBoard.class::cast)
            .filter(b -> b.getUndersideDisplay() != BoardUndersideDisplay.NONE)
            .collect(Collectors.toMap(x -> x, x -> new ArrayList<IDIYComponent<?>>()));
    Map<IDIYComponent<?>, Set<IBoard>> componentBoardMap = new HashMap<IDIYComponent<?>, Set<IBoard>>();

    for (IBoard board : boardMap.keySet()) {
      Rectangle2D boardRect = board.getBoardRectangle();
      for (IDIYComponent<?> c : project.getComponents()) {
        if (c == board) {
          continue;
        }
        boolean include = false;
        for (int i = 0; i < c.getControlPointCount(); i++) {
          if (boardRect.contains(c.getControlPoint(i))) {
            include = true;
            break;
          }
        }
        if (include) {
          boardMap.get(board).add(c);
          componentBoardMap.computeIfAbsent(c, key -> new HashSet<IBoard>()).add(board);
        }
      }
    }

    // AffineTransform initialTx = g2d.getTransform();
    Dimension canvasDimension =
        getCanvasDimensions(project, zoom, drawOptions.contains(DrawOption.EXTRA_SPACE));

    g2dWrapper.setColor(theme.getBgColor());
    g2dWrapper.fillRect(0, 0, canvasDimension.width, canvasDimension.height);
    g2d.clip(new Rectangle(new Point(0, 0), canvasDimension));

    Rectangle2D extraSpaceRect = new Rectangle2D.Double();
    AffineTransform extraSpaceTx = new AffineTransform();

    // calculate size to be rendered
    double extraSpace = getExtraSpace(project) * zoom;

    drawGrid(project, g2d, zoom, extraSpace, canvasDimension, visibleRect, drawOptions,
        extraSpaceRect, extraSpaceTx);

    // apply zoom
    if (Math.abs(1.0 - zoom) > 1e-4) {
      g2dWrapper.scale(zoom, zoom);
      if (visibleRect != null)
        visibleRect.setRect(visibleRect.getX() / zoom, visibleRect.getY() / zoom,
            visibleRect.getWidth() / zoom, visibleRect.getHeight() / zoom);
    }

    // Composite mainComposite = g2d.getComposite();
    // Composite alphaComposite =
    // AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    // g2dWrapper.resetTx();

    // put components and their states in a map
    Map<IDIYComponent<?>, ComponentState> componentStateMap = new HashMap<IDIYComponent<?>, ComponentState>();
    List<ComponentForRender> componentForRenderList = new ArrayList<ComponentForRender>();
    for (int i = 0; i < project.getComponents().size(); i++) {
      IDIYComponent<?> component = project.getComponents().get(i);      
      ComponentState state = ComponentState.NORMAL;
      if (drawOptions.contains(DrawOption.SELECTION) && selectedComponents.contains(component)) {
        if (dragInProgress) {
          state = ComponentState.DRAGGING;
        } else {
          state = ComponentState.SELECTED;
        }
      }
      componentStateMap.put(component, state);
      componentForRenderList.add(new ComponentForRender(component, state, i));
    }

    boolean outlineMode = drawOptions.contains(DrawOption.OUTLINE_MODE);

    // give the cache a chance to prepare all the components in multiple threads
    if (drawOptions.contains(DrawOption.ENABLE_CACHING)) {
      DrawingCache.Instance.bulkPrepare(componentForRenderList, g2dWrapper, outlineMode, project, zoom, scaleFactor);
    }

    // componentAreaMap.clear();

    for (int i = 0; i < project.getComponents().size(); i++) {
      IDIYComponent<?> component = project.getComponents().get(i);
      
      // Do not draw the component if it's filtered out.
      if (filter != null && !filter.testComponent(component)) {
        continue;
      }
      ComponentState state = componentStateMap.get(component);
      // Do not track the area if component is not invalidated and was
      // drawn in the same state.
      boolean trackArea = lastDrawnStateMap.get(component) != state;

      synchronized (this) {
        g2dWrapper.startedDrawingComponent(i);
        if (!trackArea) {
          g2dWrapper.stopTracking();
        }
        Composite oldComposite = g2d.getComposite();
        Font oldFont = g2d.getFont();
        // Draw locked components in a new composite.
        if (lockedComponents.contains(component) && drawOptions.contains(DrawOption.LOCKED_ALPHA)) {
          g2d.setComposite(lockedComposite);
        }
        // Draw the component through the g2dWrapper.
        long componentStart = System.nanoTime();
        try {

          drawComponent(project, drawOptions, scaleFactor, visibleRect, component, g2dWrapper, state, zoom, i, outlineMode);

          if (g2dWrapper.isTrackingContinuityArea()) {
            LOG.info("Component " + component.getName() + " of type "
                + component.getClass().getName() + " did not stop tracking continuity area.");
          }
        } catch (Exception e) {
          LOG.error("Error drawing " + component.getName(), e);
          failedComponents.add(component);
        } finally {
          // just in case, stop all tracking
          g2dWrapper.stopTrackingContinuityArea();
          g2dWrapper.stopTracking();
          // revert composite
          g2d.setComposite(oldComposite);
          g2d.setFont(oldFont);
          // record render stats
          long componentEnd = System.nanoTime();
          Counter stats = null;
          String key = component.getClass().getCanonicalName().replace("org.diylc.components.", "");
          if (renderStatsByType.containsKey(key)) {
            stats = renderStatsByType.get(key);
          } else {
            stats = new Counter();
            renderStatsByType.put(key, stats);
          }
          stats.add(componentEnd - componentStart);
        }
        ComponentArea area = g2dWrapper.finishedDrawingComponent();
        if (trackArea && area != null && !area.getOutlineArea().isEmpty()) {
          componentAreaMap.put(component, area);
          lastDrawnStateMap.put(component, state);
        }

        try {
          // Mirror components that belong to boards that need to be mirrored
          if (componentBoardMap.containsKey(component)) {
            @SuppressWarnings("unchecked")
            ComponentType componentType = ComponentProcessor.getInstance()
                .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
            if (componentType.getTransformer() != null) {
              Set<IBoard> boards = componentBoardMap.get(component);
              for (IBoard board : boards) {
                drawMirroredComponent(project, board, component, componentType, g2dWrapper, state,
                    outlineMode);
              }
            }
          }

          // if a component itself is a board that needs to be mirrored, do it
          if (IBoard.class.isInstance(component) && componentBoardMap.values().stream()
              .anyMatch(boards -> boards.contains(component))) {
            ComponentType componentType = ComponentProcessor.getInstance()
                .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
            if (componentType.getTransformer() != null) {
              drawMirroredComponent(project, (IBoard) component, component, componentType,
                  g2dWrapper, state, outlineMode);
            }
          }
        } finally {
          // revert composite
          g2d.setComposite(oldComposite);
          g2d.setFont(oldFont);
        }
      }
    }

    // Draw control points.
    if (drawOptions.contains(DrawOption.CONTROL_POINTS)) {
      // Draw unselected points first to make sure they are below.
      if (dragInProgress || drawOptions.contains(DrawOption.OUTLINE_MODE)) {
        for (IDIYComponent<?> component : project.getComponents()) {
          for (int i = 0; i < component.getControlPointCount(); i++) {
            if (lockedComponents.contains(component))
              continue;
            VisibilityPolicy visibilityPolicy = component.getControlPointVisibilityPolicy(i);
            if ((groupedComponents.contains(component)
                && (visibilityPolicy == VisibilityPolicy.ALWAYS
                    || (selectedComponents.contains(component)
                        && visibilityPolicy == VisibilityPolicy.WHEN_SELECTED))
                || (!groupedComponents.contains(component)
                    && !selectedComponents.contains(component)
                    && component.getControlPointVisibilityPolicy(i) == VisibilityPolicy.ALWAYS))) {
              g2dWrapper.setColor(CONTROL_POINT_COLOR);
              Point2D controlPoint = component.getControlPoint(i);
              int pointSize = CONTROL_POINT_SIZE - 2;
              g2dWrapper.fillOval((int) (controlPoint.getX() - pointSize / 2),
                  (int) (controlPoint.getY() - pointSize / 2), pointSize, pointSize);
            }
          }
        }
      }
      // Then draw the selected ones.
      for (IDIYComponent<?> component : selectedComponents) {
        for (int i = 0; i < component.getControlPointCount(); i++) {
          if (!groupedComponents.contains(component)
              && (component.getControlPointVisibilityPolicy(i) == VisibilityPolicy.WHEN_SELECTED
                  || component.getControlPointVisibilityPolicy(i) == VisibilityPolicy.ALWAYS)) {

            Point2D controlPoint = component.getControlPoint(i);
            int pointSize = CONTROL_POINT_SIZE;

            g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR.darker());
            g2dWrapper.fillOval((int) (controlPoint.getX() - pointSize / 2),
                (int) (controlPoint.getY() - pointSize / 2), pointSize, pointSize);
            g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR);
            g2dWrapper.fillOval((int) (controlPoint.getX() - CONTROL_POINT_SIZE / 2 + 1),
                (int) (controlPoint.getY() - CONTROL_POINT_SIZE / 2 + 1), CONTROL_POINT_SIZE - 2,
                CONTROL_POINT_SIZE - 2);
          }
        }
      }
    }

    // Draw component slot in a separate composite.
    if (componentSlot != null) {
      g2dWrapper.startedDrawingComponent(project.getComponents().size());
      g2dWrapper.setComposite(slotComposite);
      for (IDIYComponent<?> component : componentSlot) {
        try {

          component.draw(g2dWrapper, ComponentState.NORMAL,
              drawOptions.contains(DrawOption.OUTLINE_MODE), project, g2dWrapper);

        } catch (Exception e) {
          LOG.error("Error drawing " + component.getName(), e);
          failedComponents.add(component);
        }
      }
      g2dWrapper.finishedDrawingComponent();
    }

    // Draw control points of the component in the slot.
    if (controlPointSlot != null) {
      for (Point2D point : controlPointSlot) {
        if (point != null) {
          g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR.darker());
          g2dWrapper.fillOval((int) (point.getX() - CONTROL_POINT_SIZE / 2),
              (int) (point.getY() - CONTROL_POINT_SIZE / 2), CONTROL_POINT_SIZE,
              CONTROL_POINT_SIZE);
          g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR);
          g2dWrapper.fillOval((int) (point.getX() - CONTROL_POINT_SIZE / 2 + 1),
              (int) (point.getY() - CONTROL_POINT_SIZE / 2 + 1), CONTROL_POINT_SIZE - 2,
              CONTROL_POINT_SIZE - 2);
        }
      }
    }

    // Go back to the original transformation and zoom in to draw the
    // selection rectangle and other similar elements.
    // g2d.setTransform(initialTx);
    // if ((drawOptions.contains(DrawOption.ZOOM)) && (Math.abs(1.0 -
    // zoomLevel) > 1e-4)) {
    // g2d.scale(zoomLevel, zoomLevel);
    // }

    // At the end draw selection rectangle if needed.
    if (drawOptions.contains(DrawOption.SELECTION) && (selectionRect != null)) {
      g2d.setColor(Color.white);
      g2d.draw(selectionRect);
      g2d.setColor(Color.black);
      g2d.setStroke(Constants.DASHED_STROKE);
      g2d.draw(selectionRect);
    }

    // Draw component area for test
    if (debugComponentAreas) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.setColor(Color.red);
      for (ComponentArea area : componentAreaMap.values()) {
        g2d.draw(area.getOutlineArea());
      }
    }

    // Draw continuity area for test
    if (debugContinuityAreas) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(1));
      g2d.setColor(Color.green);
      for (ComponentArea area : componentAreaMap.values()) {
        for (Area a : area.getContinuityPositiveAreas())
          g2d.draw(a);
      }
      g2d.setColor(Color.blue);
      for (ComponentArea area : componentAreaMap.values()) {
        for (Area a : area.getContinuityNegativeAreas())
          g2d.draw(a);
      }
    }

    if (this.proximityMarkers != null) {
      g2d.setStroke(ObjectCache.getInstance().fetchBasicStroke(2));
      g2d.setColor(Color.red);
      for (Shape s : this.proximityMarkers)
        g2d.draw(s);
    }

    if (currentContinuityAreas != null) {
      Composite oldComposite = g2d.getComposite();
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g2d.setColor(Color.green);
      for (Area a : currentContinuityAreas)
        g2d.fill(a);
      g2d.setComposite(oldComposite);
    }

    // shade extra space
    if (drawOptions.contains(DrawOption.EXTRA_SPACE) && SHADE_EXTRA_SPACE && extraSpaceRect != null) {
      Area extraSpaceArea = new Area(
          new Rectangle2D.Double(0, 0, canvasDimension.getWidth(), canvasDimension.getHeight()));
      extraSpaceArea.subtract(new Area(extraSpaceRect));
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f));
      g2d.setTransform(extraSpaceTx);
      g2d.setColor(theme.getOutlineColor());
      g2d.fill(extraSpaceArea);
    }

    long totalEndTime = System.nanoTime();

    totalStats.add(totalEndTime - totalStartTime);

    logStats();

    return failedComponents;
  }

  private static void drawMirroredComponent(Project project, IBoard board, IDIYComponent<?> component,
                                            ComponentType componentType, G2DWrapper g2dWrapper, ComponentState state, boolean outlineMode) {
    double offset = board.getUndersideOffset().convertToPixels();
    Rectangle2D boardRectangle = board.getBoardRectangle();
    try {
        IDIYComponent<?> clonedComponent = component.clone();
        int direction = 0;
        Point2D pivotPoint = new Point2D.Double();
        switch (board.getUndersideDisplay()) {
          case ABOVE:
            direction = IComponentTransformer.VERTICAL;
            pivotPoint.setLocation(boardRectangle.getMinX(), boardRectangle.getMinY() - offset / 2);
            break;
          case BELOW:
            direction = IComponentTransformer.VERTICAL;
            pivotPoint.setLocation(boardRectangle.getMinX(), boardRectangle.getMaxY() + offset / 2);
            break;
          case LEFT:
            direction = IComponentTransformer.HORIZONTAL;
            pivotPoint.setLocation(boardRectangle.getMinX() - offset / 2, boardRectangle.getMinY());
            break;
          case RIGHT:
            direction = IComponentTransformer.HORIZONTAL;
            pivotPoint.setLocation(boardRectangle.getMaxX() + offset / 2, boardRectangle.getMinY());
            break;
        }
      componentType.getTransformer().mirror(clonedComponent, pivotPoint, direction);
      if (board.getUndersideTransparency() && AbstractTransparentComponent.class.isInstance(clonedComponent)) {
        AbstractTransparentComponent<?> transparentComponent = (AbstractTransparentComponent<?>) clonedComponent;
        transparentComponent.setAlpha(MIRROR_ALPHA);
      }
      clonedComponent.draw(g2dWrapper, state, outlineMode, project, g2dWrapper);
    } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
    }
  }

  private static void drawComponent(Project project, Set<DrawOption> drawOptions, Double scaleFactor, Rectangle2D visibleRect, IDIYComponent<?> component, G2DWrapper g2dWrapper, ComponentState state, double zoom, int i, boolean outlineMode) {
    if (drawOptions.contains(DrawOption.ENABLE_CACHING)) // go through the DrawingCache
      DrawingCache.Instance.draw(component, g2dWrapper, state,
          drawOptions.contains(DrawOption.OUTLINE_MODE), project, zoom, scaleFactor, i, visibleRect);
    else // go straight to the wrapper
      component.draw(g2dWrapper, state, outlineMode, project, g2dWrapper);
  }

  private void configureRenderingHints(Graphics2D g2d, Set<DrawOption> drawOptions) {
    if (drawOptions.contains(DrawOption.ANTIALIASING)) {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    } else {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }
    if (configManager.readBoolean(IPlugInPort.HI_QUALITY_RENDER_KEY, false)) {
      g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
          RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
          RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      // g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
      // RenderingHints.VALUE_STROKE_PURE);
    } else {
      g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
          RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
      g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
          RenderingHints.VALUE_COLOR_RENDER_SPEED);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }
    g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
  }

  private double zoomCached = 0;
  private Dimension canvasDimensionCached;
  private double extraSpaceCached;
  private GridType gridTypeCached;
  private BufferedImage cachedGridImage;

  private void drawGrid(Project project, Graphics2D g2dIn, double zoom, double extraSpace,
      Dimension canvasDimension, Rectangle2D visibleRect, Set<DrawOption> drawOptions,
      Rectangle2D extraSpaceRect, AffineTransform extraSpaceTx) {

    GridType gridType = GridType.LINES;

    Dimension innerCanvasDimension = getCanvasDimensions(project, zoom, false);

    // draw from cache
    if (zoom != zoomCached || !canvasDimension.equals(canvasDimensionCached)
        || extraSpace != extraSpaceCached || gridType != gridTypeCached) {      
      try {
        cachedGridImage = new BufferedImage((int) canvasDimension.getWidth(),
            (int) canvasDimension.getHeight(), BufferedImage.TYPE_INT_ARGB);
        // create graphics
        Graphics2D g2d = cachedGridImage.createGraphics();

        if (drawOptions.contains(DrawOption.GRID) && gridType != GridType.NONE) {
          double zoomStep = project.getGridSpacing().convertToPixels() * zoom;
          float gridThickness = (float) (1f * (zoom > 1 ? 1 : zoom));
          if (gridType == GridType.CROSSHAIR) {
            g2d.setStroke(new BasicStroke(gridThickness, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f,
                new float[] {(float) zoomStep / 2, (float) zoomStep / 2}, (float) zoomStep / 4));
          } else if (gridType == GridType.DOT) {
            g2d.setStroke(new BasicStroke(gridThickness, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10f, new float[] {1f, (float) zoomStep - 1}, 0f));
          } else {
            g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(gridThickness));
          }

          g2d.setColor(theme.getGridColor());
          for (double i = zoomStep; i < canvasDimension.width; i += zoomStep) {
            g2d.draw(new Line2D.Double(i, 0, i, canvasDimension.height - 1));
          }
          for (double j = zoomStep; j < canvasDimension.height; j += zoomStep) {
            g2d.draw(new Line2D.Double(0, j, canvasDimension.width - 1, j));
          }
          // draw dots if needed
          if (project.getDotSpacing() > 1 && zoomStep > 8) {
            g2d.setStroke(ObjectCache.getInstance().fetchZoomableStroke(gridThickness * 3));
            g2d.setColor(theme.getDotColor());
            for (double i = extraSpace + zoomStep * project.getDotSpacing(); i < extraSpace
                + innerCanvasDimension.width; i += zoomStep * project.getDotSpacing())
              for (double j = extraSpace + zoomStep * project.getDotSpacing(); j < extraSpace
                  + innerCanvasDimension.height; j += zoomStep * project.getDotSpacing()) {
                g2d.fillOval((int) Math.round(i - 1), (int) Math.round(j - 1), 3, 3);
              }
          }
        }
      } finally {
        zoomCached = zoom;
        canvasDimensionCached = (Dimension) canvasDimension.clone();
        extraSpaceCached = extraSpace;
        gridTypeCached = gridType;
      }
    }
    
    if (cachedGridImage == null) {
      LOG.warn("Cached grid image is null!");
      return;
    }
    
    if (drawOptions.contains(DrawOption.GRID) && gridType != GridType.NONE) {
      g2dIn.drawImage(cachedGridImage, 0, 0, null);
    }

    // manage extra space
    if (drawOptions.contains(DrawOption.EXTRA_SPACE)) {
      float borderThickness = (float) (3f * (zoom > 1 ? 1 : zoom));
      g2dIn.setStroke(ObjectCache.getInstance().fetchStroke(borderThickness,
          new float[] {borderThickness * 4, borderThickness * 4,}, 0, BasicStroke.CAP_BUTT));
      g2dIn.setColor(theme.getOutlineColor());
      extraSpaceRect.setRect(new Rectangle2D.Double(extraSpace, extraSpace,
          innerCanvasDimension.getWidth(), innerCanvasDimension.getHeight()));
      g2dIn.draw(extraSpaceRect);
      extraSpaceTx.setTransform(g2dIn.getTransform());

      // translate to the new (0, 0)
      g2dIn.transform(AffineTransform.getTranslateInstance(extraSpace, extraSpace));
      if (visibleRect != null)
        visibleRect.setRect(visibleRect.getX() - extraSpace, visibleRect.getY() - extraSpace,
            visibleRect.getWidth(), visibleRect.getHeight());
    }
  }

  public void logStats() {
    // log render time stats periodically
    if (System.currentTimeMillis() - lastStatsReportedTime > statReportFrequencyMs) {
      lastStatsReportedTime = System.currentTimeMillis();
      String mapAsString = renderStatsByType.entrySet().stream()
          .sorted(
              (e1, e2) -> -Long.compare(e1.getValue().getNanoTime(), e2.getValue().getNanoTime()))
          .map(e -> e.toString()).collect(Collectors.joining("; ", "{", "}"));
      LOG.debug("Render stats: " + mapAsString);
      LOG.debug("Page stats: " + totalStats.toAvgString());
      DrawingCache.Instance.logStats();
    }
  }

  public double getZoomLevel() {
    return zoomLevel;
  }

  public void setZoomLevel(double zoomLevel) {
    this.zoomLevel = zoomLevel;
    fireZoomChanged();
    // configManager.writeValue(ZOOM_KEY, zoomLevel);
  }

  public void invalidateComponent(IDIYComponent<?> component) {
    componentAreaMap.remove(component);
    lastDrawnStateMap.remove(component);
    proximityMarkers = null;
  }

  public ComponentArea getComponentArea(IDIYComponent<?> component) {
    return componentAreaMap.get(component);
  }

  public void clearComponentAreaMap() {
    componentAreaMap.clear();
    lastDrawnStateMap.clear();
    proximityMarkers = null;
  }

  public void clearContinuityArea() {
    currentContinuityAreas = null;
    continuityGraphCache = null;
    proximityMarkers = null;
  }

  public List<IDIYComponent<?>> findComponentsAt(Point2D point, Project project) {
    List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>();
    for (int i = 0; i < project.getComponents().size(); i++) {
      ComponentArea area = componentAreaMap.get(project.getComponents().get(i));
      if (area != null && area.getOutlineArea().contains(point)) {
        components.add(0, project.getComponents().get(i));
      }
    }
    return components;
  }

  public double getExtraSpace(Project project) {
    double width = project.getWidth().convertToPixels();
    double height = project.getHeight().convertToPixels();
    double targetExtraSpace = EXTRA_SPACE * Math.max(width, height);
    return CalcUtils.roundToGrid(targetExtraSpace,
        project.getGridSpacing().scale(project.getDotSpacing()));
  }

  public Dimension getCanvasDimensions(Project project, Double zoomLevel,
      boolean includeExtraSpace) {
    double width = project.getWidth().convertToPixels();
    double height = project.getHeight().convertToPixels();

    if (includeExtraSpace) {
      double extraSpace = getExtraSpace(project);
      width += 2 * extraSpace;
      height += 2 * extraSpace;
    }

    width *= zoomLevel;
    height *= zoomLevel;

    return new Dimension((int) width, (int) height);
  }

  public void fireZoomChanged() {
    messageDispatcher.dispatchMessage(EventType.ZOOM_CHANGED, zoomLevel);
    messageDispatcher.dispatchMessage(EventType.REPAINT);
  }

  public Theme getTheme() {
    return theme;
  }

  public void setTheme(Theme theme) {
    this.theme = theme;
    configManager.writeValue(IPlugInPort.THEME_KEY, theme);
    messageDispatcher.dispatchMessage(EventType.REPAINT);
  }

  public void findContinuityAreaAtPoint(Point2D p) {
    if (continuityGraphCache == null)
      continuityGraphCache = getContinuityGraph();

    currentContinuityAreas = continuityGraphCache.findAreasFor(p);
  }
  
  public List<ContinuityArea> getContinuityAreas() {
    // Find all individual continuity areas for all components
    List<ContinuityArea> preliminaryAreas = new ArrayList<ContinuityArea>();
    List<Boolean> checkBreakout = new ArrayList<Boolean>();

    for (Map.Entry<IDIYComponent<?>, ComponentArea> entry : this.componentAreaMap.entrySet()) {
      ComponentArea a = entry.getValue();

      if (a == null || a.getOutlineArea() == null)
        continue;
      
      IDIYComponent<?> component = entry.getKey();
      
      int layerId;
      
      if (component instanceof ILayeredComponent) {
        layerId = ((ILayeredComponent)component).getLayerId();
      } else {
        layerId = 0;
      }
      
      Collection<Area> positiveAreas = a.getContinuityPositiveAreas();
      if (positiveAreas != null) {
        for (Area a1 : positiveAreas) {
          preliminaryAreas.add(new ContinuityArea(layerId, a1));
          checkBreakout.add(false);
        }
      }
      
      Collection<Area> negativeAreas = a.getContinuityNegativeAreas();
      if (negativeAreas != null) {
        for (Area na : negativeAreas) {
          for (int i = 0; i < preliminaryAreas.size(); i++) {
            ContinuityArea a1 = preliminaryAreas.get(i);
            if (a1.getLayerId() == layerId && a1.getArea().intersects(na.getBounds2D())) {
              a1.getArea().subtract(na);
              checkBreakout.set(i, true);
            }
          }
        }
      }
    }

    // Check if we need to break some areas out in case they are interrupted
    List<ContinuityArea> areas = new ArrayList<ContinuityArea>();
    for (int i = 0; i < preliminaryAreas.size(); i++) {
      ContinuityArea a = preliminaryAreas.get(i);
      if (checkBreakout.get(i)) {
        List<Area> breakoutAreas = AreaUtils.tryAreaBreakout(a.getArea());
        if (breakoutAreas.size() == 1) {
          areas.add(a);
        } else {
          areas.addAll(breakoutAreas.stream()
              .map(area -> new ContinuityArea(a.getLayerId(), area))
              .collect(Collectors.toList())); 
        }        
      } else
        areas.add(a);
    }

    return areas;
  }

  public ContinuityGraph getContinuityGraph() {
    Set<Connection> connections = new HashSet<Connection>();
    int z = 0;
    for (IDIYComponent<?> c : this.componentAreaMap.keySet()) {
      if (c instanceof IContinuity) {
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (((IContinuity) c).arePointsConnected(i, j))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j), z));
      }
      z++;
    }
    
    List<ContinuityArea> areas = getContinuityAreas();

    return NetlistBuilder.buildContinuityGraph(areas, connections);
  }

  public List<Area> getContinuityAreaProximity(float threshold) {
    List<ContinuityArea> continuityAreas = getContinuityAreas();
    Stroke s = ObjectCache.getInstance().fetchBasicStroke(threshold - 1); // value eyeballed for
                                                                          // approx good results

    List<Area> areas = new ArrayList<>(continuityAreas.stream().map(ContinuityArea::getArea).toList());
    AreaUtils.crunchAreas(areas, null);

    List<Area> expanded = areas.parallelStream()
        .map((a) -> new Area(s.createStrokedShape(a))).toList();

    List<Area> intersections = new ArrayList<Area>();
    for (int i = 0; i < expanded.size() - 1; i++) {
      for (int j = i + 1; j < expanded.size(); j++) {
        Area first = new Area(expanded.get(i));
        Area second = expanded.get(j);
        first.intersect(second);
        if (!first.isEmpty())
          intersections.add(first);
      }
    }

    AreaUtils.crunchAreas(intersections, null);
    double minSize = new Size(1d, SizeUnit.cm).convertToPixels();

    List<Area> shapes = new ArrayList<Area>();
    for (Area a : intersections) {
      Rectangle2D bounds2d = a.getBounds2D();
      if (bounds2d.getWidth() < minSize)
        bounds2d = new Rectangle2D.Double(bounds2d.getCenterX() - minSize / 2, bounds2d.getY(),
            minSize, bounds2d.getHeight());
      if (bounds2d.getHeight() < minSize)
        bounds2d = new Rectangle2D.Double(bounds2d.getX(), bounds2d.getCenterY() - minSize / 2,
            bounds2d.getWidth(), minSize);

      shapes.add(new Area(new Ellipse2D.Double(bounds2d.getX(), bounds2d.getY(),
          bounds2d.getWidth(), bounds2d.getHeight())));
    }

    // AreaUtils.crunchAreas(shapes, null);

    this.proximityMarkers = shapes;

    return shapes;
  }
}
