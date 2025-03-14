package org.diylc.presenter;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.diylc.common.DrawOption;
import org.diylc.common.IComponentFilter;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Theme;

public interface DrawingService {
    // Core drawing methods
    List<IDIYComponent<?>> drawProject(Graphics2D g2d, Project project,
                                       Set<DrawOption> drawOptions, IComponentFilter filter, Rectangle selectionRect,
                                       Collection<IDIYComponent<?>> selectedComponents, Set<IDIYComponent<?>> lockedComponents,
                                       Set<IDIYComponent<?>> groupedComponents, List<Point2D> controlPointSlot,
                                       List<IDIYComponent<?>> componentSlot, boolean dragInProgress, Double externalZoom,
                                       Double scaleFactor, Rectangle2D visibleRect);
    
    // Component area handling
    ComponentArea getComponentArea(IDIYComponent<?> component);
    void clearComponentAreaMap();
    List<IDIYComponent<?>> findComponentsAt(Point2D point, Project project);
    void invalidateComponent(IDIYComponent<?> component);
    
    // Continuity area handling  
    void clearContinuityArea();
    List<ContinuityArea> getContinuityAreas();
    List<Area> getContinuityAreaProximity(float threshold);
    void findContinuityAreaAtPoint(Point2D point);
    
    // Theme handling
    Theme getTheme();
    void setTheme(Theme theme);
    
    // Zoom control
    double getZoomLevel();
    void setZoomLevel(double zoomLevel);
    void fireZoomChanged();

    // Canvas dimensions
    Dimension getCanvasDimensions(Project project, Double zoomLevel,
                                  boolean includeExtraSpace);
    double getExtraSpace(Project project);
} 