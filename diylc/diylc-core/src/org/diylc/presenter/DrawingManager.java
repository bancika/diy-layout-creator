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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.GridType;
import org.diylc.common.IComponentFiler;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ObjectCache;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.utils.Constants;

/**
 * Utility that deals with painting {@link Project} on the {@link Graphics2D} and keeps areas taken
 * by each drawn component.
 * 
 * @author Branislav Stojkovic
 */
public class DrawingManager {

  private static final Logger LOG = Logger.getLogger(DrawingManager.class);

  public static int CONTROL_POINT_SIZE = 7;

  public static final String ZOOM_KEY = "zoom";

  public static String DEBUG_COMPONENT_AREAS = "org.diylc.debugComponentAreas";
  public static String DEBUG_CONTINUITY_AREAS = "org.diylc.debugContinuityAreas";

  public static Color CONTROL_POINT_COLOR = Color.blue;
  public static Color SELECTED_CONTROL_POINT_COLOR = Color.green;

  private Theme theme = (Theme) ConfigurationManager.getInstance().readObject(IPlugInPort.THEME_KEY,
      Constants.DEFAULT_THEME);

  // Keeps Area object of each drawn component.
  private Map<IDIYComponent<?>, ComponentArea> componentAreaMap;
  // Maps components to the last state they are drawn in. Also, used to
  // determine which components are invalidated when they are not in the map.
  private Map<IDIYComponent<?>, ComponentState> lastDrawnStateMap;

  private Area continuityArea;

  private Composite slotComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
  private Composite lockedComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
  private List<IDIYComponent<?>> failedComponents = new ArrayList<IDIYComponent<?>>();

  private double zoomLevel = 1d;// ConfigurationManager.getInstance().readDouble(ZOOM_KEY,
  // 1d);

  private MessageDispatcher<EventType> messageDispatcher;

  private boolean debugComponentAreas;
  private boolean debugContinuityAreas;

  public DrawingManager(MessageDispatcher<EventType> messageDispatcher) {
    super();
    this.messageDispatcher = messageDispatcher;
    componentAreaMap = new HashMap<IDIYComponent<?>, ComponentArea>();
    lastDrawnStateMap = new HashMap<IDIYComponent<?>, ComponentState>();
    String debugComponentAreasStr = System.getProperty(DEBUG_COMPONENT_AREAS);
    debugComponentAreas = debugComponentAreasStr != null && debugComponentAreasStr.equalsIgnoreCase("true");

    String debugContinuityAreasStr = System.getProperty(DEBUG_CONTINUITY_AREAS);
    debugContinuityAreas = debugContinuityAreasStr != null && debugContinuityAreasStr.equalsIgnoreCase("true");
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
   * @return
   */
  public List<IDIYComponent<?>> drawProject(Graphics2D g2d, Project project, Set<DrawOption> drawOptions,
      IComponentFiler filter, Rectangle selectionRect, Collection<IDIYComponent<?>> selectedComponents,
      Set<IDIYComponent<?>> lockedComponents, Set<IDIYComponent<?>> groupedComponents, List<Point> controlPointSlot,
      List<IDIYComponent<?>> componentSlot, boolean dragInProgress) {
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

    G2DWrapper g2dWrapper = new G2DWrapper(g2d, zoom);

    if (drawOptions.contains(DrawOption.ANTIALIASING)) {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    } else {
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
      g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }
    if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.HI_QUALITY_RENDER_KEY, false)) {
      g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      // g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
      // RenderingHints.VALUE_STROKE_PURE);
    } else {
      g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
      g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
      g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    // AffineTransform initialTx = g2d.getTransform();
    Dimension d = getCanvasDimensions(project, zoom, true);

    g2dWrapper.setColor(theme.getBgColor());
    g2dWrapper.fillRect(0, 0, d.width, d.height);
    g2d.clip(new Rectangle(new Point(0, 0), d));

    GridType gridType = GridType.LINES;
    if (drawOptions.contains(DrawOption.GRID) && gridType != GridType.NONE) {
      double zoomStep = project.getGridSpacing().convertToPixels() * zoom;
      if (gridType == GridType.CROSSHAIR) {
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {
            (float) zoomStep / 2, (float) zoomStep / 2}, (float) zoomStep / 4));
      } else if (gridType == GridType.DOT) {
        g2d.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {1f,
            (float) zoomStep - 1}, 0f));
      }

      g2dWrapper.setColor(theme.getGridColor());
      for (double i = zoomStep; i < d.width; i += zoomStep) {
        g2dWrapper.drawLine((int) i, 0, (int) i, d.height - 1);
      }
      for (double j = zoomStep; j < d.height; j += zoomStep) {
        g2dWrapper.drawLine(0, (int) j, d.width - 1, (int) j);
      }
    }

    if (Math.abs(1.0 - zoom) > 1e-4) {
      g2dWrapper.scale(zoom, zoom);
    }

    // Composite mainComposite = g2d.getComposite();
    // Composite alphaComposite =
    // AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    // g2dWrapper.resetTx();

    // componentAreaMap.clear();
    for (IDIYComponent<?> component : project.getComponents()) {
      // Do not draw the component if it's filtered out.
      if (filter != null && !filter.testComponent(component)) {
        continue;
      }
      ComponentState state = ComponentState.NORMAL;
      if (drawOptions.contains(DrawOption.SELECTION) && selectedComponents.contains(component)) {
        if (dragInProgress) {
          state = ComponentState.DRAGGING;
        } else {
          state = ComponentState.SELECTED;
        }
      }
      // Do not track the area if component is not invalidated and was
      // drawn in the same state.
      boolean trackArea = lastDrawnStateMap.get(component) != state;

      synchronized (g2d) {
        g2dWrapper.startedDrawingComponent();
        if (!trackArea) {
          g2dWrapper.stopTracking();
        }
        // Draw locked components in a new composite.
        if (lockedComponents.contains(component)) {
          g2d.setComposite(lockedComposite);
        }
        // Draw the component through the g2dWrapper.
        try {
          component.draw(g2dWrapper, state, drawOptions.contains(DrawOption.OUTLINE_MODE), project, g2dWrapper);
        } catch (Exception e) {
          LOG.error("Error drawing " + component.getName(), e);
          failedComponents.add(component);
        }
        ComponentArea area = g2dWrapper.finishedDrawingComponent();
        if (trackArea && area != null && !area.getOutlineArea().isEmpty()) {
          componentAreaMap.put(component, area);
          lastDrawnStateMap.put(component, state);
        }
      }
    }

    // Draw control points.
    if (drawOptions.contains(DrawOption.CONTROL_POINTS)) {
      // Draw unselected points first to make sure they are below.
      if (dragInProgress || drawOptions.contains(DrawOption.OUTLINE_MODE)) {
        for (IDIYComponent<?> component : project.getComponents()) {
          for (int i = 0; i < component.getControlPointCount(); i++) {
            VisibilityPolicy visibilityPolicy = component.getControlPointVisibilityPolicy(i);
            if ((groupedComponents.contains(component)
                && (visibilityPolicy == VisibilityPolicy.ALWAYS || (selectedComponents.contains(component) && visibilityPolicy == VisibilityPolicy.WHEN_SELECTED)) || (!groupedComponents
                .contains(component) && !selectedComponents.contains(component) && component
                  .getControlPointVisibilityPolicy(i) == VisibilityPolicy.ALWAYS))) {
              g2dWrapper.setColor(CONTROL_POINT_COLOR);
              Point controlPoint = component.getControlPoint(i);
              int pointSize = CONTROL_POINT_SIZE - 2;
              g2dWrapper.fillOval(controlPoint.x - pointSize / 2, controlPoint.y - pointSize / 2, pointSize, pointSize);
            }
          }
        }
      }
      // Then draw the selected ones.
      for (IDIYComponent<?> component : selectedComponents) {
        for (int i = 0; i < component.getControlPointCount(); i++) {
          if (!groupedComponents.contains(component)
              && (component.getControlPointVisibilityPolicy(i) == VisibilityPolicy.WHEN_SELECTED || component
                  .getControlPointVisibilityPolicy(i) == VisibilityPolicy.ALWAYS)) {

            Point controlPoint = component.getControlPoint(i);
            int pointSize = CONTROL_POINT_SIZE;

            g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR.darker());
            g2dWrapper.fillOval(controlPoint.x - pointSize / 2, controlPoint.y - pointSize / 2, pointSize, pointSize);
            g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR);
            g2dWrapper.fillOval(controlPoint.x - CONTROL_POINT_SIZE / 2 + 1, controlPoint.y - CONTROL_POINT_SIZE / 2
                + 1, CONTROL_POINT_SIZE - 2, CONTROL_POINT_SIZE - 2);
          }
        }
      }
    }

    // Draw component slot in a separate composite.
    if (componentSlot != null) {
      g2dWrapper.startedDrawingComponent();
      g2dWrapper.setComposite(slotComposite);
      for (IDIYComponent<?> component : componentSlot) {
        try {

          component.draw(g2dWrapper, ComponentState.NORMAL, drawOptions.contains(DrawOption.OUTLINE_MODE), project,
              g2dWrapper);

        } catch (Exception e) {
          LOG.error("Error drawing " + component.getName(), e);
          failedComponents.add(component);
        }
      }
      g2dWrapper.finishedDrawingComponent();
    }

    // Draw control points of the component in the slot.
    if (controlPointSlot != null) {
      for (Point point : controlPointSlot) {
        if (point != null) {
          g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR.darker());
          g2dWrapper.fillOval(point.x - CONTROL_POINT_SIZE / 2, point.y - CONTROL_POINT_SIZE / 2, CONTROL_POINT_SIZE,
              CONTROL_POINT_SIZE);
          g2dWrapper.setColor(SELECTED_CONTROL_POINT_COLOR);
          g2dWrapper.fillOval(point.x - CONTROL_POINT_SIZE / 2 + 1, point.y - CONTROL_POINT_SIZE / 2 + 1,
              CONTROL_POINT_SIZE - 2, CONTROL_POINT_SIZE - 2);
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
    
    if (continuityArea != null && (ConfigurationManager.getInstance().readBoolean(IPlugInPort.HIGHLIGHT_CONTINUITY_AREA, false)))
    {
      Composite oldComposite = g2d.getComposite();
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
      g2d.setColor(Color.green);
      g2d.fill(continuityArea);
      g2d.setComposite(oldComposite);
    }

    return failedComponents;
  }

  public double getZoomLevel() {
    return zoomLevel;
  }

  public void setZoomLevel(double zoomLevel) {
    this.zoomLevel = zoomLevel;
    fireZoomChanged();
    // ConfigurationManager.getInstance().writeValue(ZOOM_KEY, zoomLevel);
  }

  public void invalidateComponent(IDIYComponent<?> component) {
    componentAreaMap.remove(component);
    lastDrawnStateMap.remove(component);
  }

  public ComponentArea getComponentArea(IDIYComponent<?> component) {
    return componentAreaMap.get(component);
  }

  public void clearComponentAreaMap() {
    componentAreaMap.clear();
    lastDrawnStateMap.clear();
  }
  
  public void clearContinuityArea() {
    this.continuityArea = null;
  }

  public List<IDIYComponent<?>> findComponentsAt(Point point, Project project) {
    List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>();
    for (int i = 0; i < project.getComponents().size(); i++) {
      ComponentArea area = componentAreaMap.get(project.getComponents().get(i));
      if (area != null && area.getOutlineArea().contains(point)) {
        components.add(0, project.getComponents().get(i));
      }
    }
    return components;
  }

  public Dimension getCanvasDimensions(Project project, Double zoomLevel, boolean useZoom) {
    double width = project.getWidth().convertToPixels();
    double height = project.getHeight().convertToPixels();
    if (useZoom) {
      width *= zoomLevel;
      height *= zoomLevel;
    } else {
      width /= Constants.PIXEL_SIZE;
      height /= Constants.PIXEL_SIZE;
    }
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
    ConfigurationManager.getInstance().writeValue(IPlugInPort.THEME_KEY, theme);
    messageDispatcher.dispatchMessage(EventType.REPAINT);
  }

  @SuppressWarnings("unchecked")
  public void findContinuityAreaAtPoint(Project project, Point p) {
    // Find all individual continuity areas for all components
    List<Area> preliminaryAreas = new ArrayList<Area>();
    List<Boolean> checkBreakout = new ArrayList<Boolean>();
    List<Connection> connections = new ArrayList<Connection>();
    for (IDIYComponent<?> c : project.getComponents()) {
      ComponentArea a = getComponentArea(c);

      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());
      if (type.isContinuity()) {
        connections.add(new Connection(c.getControlPoint(0), c.getControlPoint(c.getControlPointCount() - 1)));
      }

      if (a == null || a.getOutlineArea() == null)
        continue;
      if (a.getContinuityPositiveAreas() != null)
        for (Area a1 : a.getContinuityPositiveAreas()) {
          preliminaryAreas.add(a1);
          checkBreakout.add(false);
        }
      if (a.getContinuityNegativeAreas() != null) {
        for (Area na : a.getContinuityNegativeAreas())
          for (int i = 0; i < preliminaryAreas.size(); i++) {
            Area a1 = preliminaryAreas.get(i);
            if (a1.intersects(na.getBounds2D())) {
              a1.subtract(na);
              checkBreakout.set(i, true);
            }
          }
      }
    }

    // Check if we need to break some areas out in case they are interrupted
    List<Area> areas = new ArrayList<Area>();
    for (int i = 0; i < preliminaryAreas.size(); i++) {
      Area a = preliminaryAreas.get(i);
      if (checkBreakout.get(i))
        areas.addAll(tryBreakout(a));
      else
        areas.add(a);
    }

    crunchAreas(areas, connections);

    for (Area a : areas) {
      if (a.contains(p)) {
        continuityArea = a;
        return;
      }
    }

    continuityArea = null;
  }

  private boolean crunchAreas(List<Area> areas, List<Connection> connections) {
    boolean isChanged = false;

    List<Area> newAreas = new ArrayList<Area>();
    List<Boolean> consumed = new ArrayList<Boolean>();
    for (int i = 0; i < areas.size(); i++) {
      consumed.add(false);
    }
    for (int i = 0; i < areas.size(); i++) {
      for (int j = i + 1; j < areas.size(); j++) {
        if (consumed.get(j))
          continue;
        Area a1 = areas.get(i);
        Area a2 = areas.get(j);
        Area intersection = new Area(a1);
        intersection.intersect(a2);
        // if the two areas intersect, make a union and consume the second area
        if (!intersection.isEmpty()) {
          a1.add(a2);
          consumed.set(j, true);
        } else { // maybe there's a connection between them
          for (Connection p : connections) {
            if ((a1.contains(p.pointA) && a2.contains(p.pointB)) || (a1.contains(p.pointB) && a2.contains(p.pointA))) {
              a1.add(a2);
              consumed.set(j, true);
              break;
            }
          }
        }
      }
    }
    for (int i = 0; i < areas.size(); i++)
      if (!consumed.get(i))
        newAreas.add(areas.get(i));
      else
        isChanged = true;

    if (isChanged) {
      areas.clear();
      areas.addAll(newAreas);
      crunchAreas(areas, connections);
    }

    return isChanged;
  }

  private List<Area> tryBreakout(Area a) {
    List<Area> toReturn = new ArrayList<Area>();
    Path2D p = null;
    PathIterator pathIterator = a.getPathIterator(null);
    while (!pathIterator.isDone()) {
      double[] coord = new double[6];
      int type = pathIterator.currentSegment(coord);
      switch (type) {
        case PathIterator.SEG_MOVETO:
          if (p != null) {
            Area partArea = new Area(p);
            toReturn.add(partArea);
          }
          p = new Path2D.Double();
          p.moveTo(coord[0], coord[1]);
          break;
        case PathIterator.SEG_LINETO:
          p.lineTo(coord[0], coord[1]);
          break;
        case PathIterator.SEG_CUBICTO:
          p.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[5]);
          break;
        case PathIterator.SEG_QUADTO:
          p.quadTo(coord[0], coord[1], coord[2], coord[3]);
          break;
      }
      pathIterator.next();
    }
    if (p != null) {
      Area partArea = new Area(p);
      toReturn.add(partArea);
    }

    return toReturn;
  }

  class Connection {

    private Point pointA;
    private Point pointB;

    public Connection(Point pointA, Point pointB) {
      super();
      this.pointA = pointA;
      this.pointB = pointB;
    }

    public Point getPointA() {
      return pointA;
    }

    public Point getPointB() {
      return pointB;
    }
  }
}
