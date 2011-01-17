package org.diylc.presenter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.common.ComponentSelection;
import org.diylc.common.DrawOption;
import org.diylc.common.IComponentFiler;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.utils.Constants;

/**
 * Utility that deals with painting {@link Project} on the {@link Graphics2D}
 * and keeps areas taken by each drawn component.
 * 
 * @author Branislav Stojkovic
 */
public class ProjectPainter {

	public static final int CONTROL_POINT_SIZE = 7;
	public static boolean ENABLE_ANTIALIASING = true;
	public static boolean DEBUG_COMPONENT_AREAS = false;

	// Keeps Area object of each drawn component.
	private Map<IDIYComponent<?>, Area> componentAreaMap;
	// Maps components to the last state they are drawn in. Also, used to
	// determine which components are invalidated when they are not in the map.
	private Map<IDIYComponent<?>, ComponentState> lastDrawnStateMap;

	public ProjectPainter() {
		super();
		componentAreaMap = new HashMap<IDIYComponent<?>, Area>();
		lastDrawnStateMap = new HashMap<IDIYComponent<?>, ComponentState>();
	}

	public void drawProject(Graphics2D g2d, Project project, Set<DrawOption> drawOptions,
			IComponentFiler filter, Rectangle selectionRect, ComponentSelection selectedComponents,
			Set<IDIYComponent<?>> groupedComponents, List<Point> controlPointSlot,
			boolean dragInProgress, double zoomLevel) {
		if (project == null) {
			return;
		}
		G2DWrapper g2dWrapper = new G2DWrapper(g2d);

		if (drawOptions.contains(DrawOption.ANTIALIASING) && ENABLE_ANTIALIASING) {
			g2d
					.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		}

		// AffineTransform initialTx = g2d.getTransform();
		Dimension d = getCanvasDimensions(project, zoomLevel, drawOptions.contains(DrawOption.ZOOM));

		g2dWrapper.setColor(Constants.CANVAS_COLOR);
		g2dWrapper.fillRect(0, 0, d.width, d.height);

		if (drawOptions.contains(DrawOption.GRID)) {
			double zoomStep = project.getGridSpacing().convertToPixels() * zoomLevel;

			g2dWrapper.setColor(Constants.GRID_COLOR);
			for (double i = zoomStep; i < d.width; i += zoomStep) {
				g2dWrapper.drawLine((int) i, 0, (int) i, d.height - 1);
			}
			for (double j = zoomStep; j < d.height; j += zoomStep) {
				g2dWrapper.drawLine(0, (int) j, d.width - 1, (int) j);
			}
		}

		if ((drawOptions.contains(DrawOption.ZOOM)) && (Math.abs(1.0 - zoomLevel) > 1e-4)) {
			g2dWrapper.scale(zoomLevel, zoomLevel);
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
			if (drawOptions.contains(DrawOption.SELECTION)
					&& selectedComponents.contains(component)) {
				if (dragInProgress) {
					state = ComponentState.DRAGGING;
				} else {
					state = ComponentState.SELECTED;
				}
			}
			// Do not track the area if component is not invalidated and was
			// drawn in the same state.
			boolean trackArea = lastDrawnStateMap.get(component) != state;
			g2dWrapper.startedDrawingComponent();
			if (!trackArea) {
				g2dWrapper.stopTracking();
			}
			// Draw the component through the g2dWrapper.
			component.draw(g2dWrapper, state, project, g2dWrapper);
			Area area = g2dWrapper.finishedDrawingComponent();
			if (trackArea) {
				componentAreaMap.put(component, area);
				lastDrawnStateMap.put(component, state);
			}
		}

		// Draw control points of the component in the slot.
		if (controlPointSlot != null) {
			for (Point point : controlPointSlot) {
				if (point != null) {
					g2dWrapper.setColor(Constants.SELECTED_CONTROL_POINT_COLOR);
					g2dWrapper.fillOval(point.x - CONTROL_POINT_SIZE / 2, point.y
							- CONTROL_POINT_SIZE / 2, CONTROL_POINT_SIZE, CONTROL_POINT_SIZE);
				}
			}
		}

		// Draw control points.
		if (drawOptions.contains(DrawOption.CONTROL_POINTS)) {
			// Draw unselected points first to make sure they are below.
			for (IDIYComponent<?> component : project.getComponents()) {
				for (int i = 0; i < component.getControlPointCount(); i++) {
					VisibilityPolicy visibilityPolicy = component
							.getControlPointVisibilityPolicy(i);
					if ((groupedComponents.contains(component)
							&& (visibilityPolicy == VisibilityPolicy.ALWAYS || (selectedComponents
									.contains(component) && visibilityPolicy == VisibilityPolicy.WHEN_SELECTED)) || (!groupedComponents
							.contains(component)
							&& !selectedComponents.contains(component) && component
							.getControlPointVisibilityPolicy(i) == VisibilityPolicy.ALWAYS))) {
						g2dWrapper.setColor(Constants.CONTROL_POINT_COLOR);

						Point controlPoint = component.getControlPoint(i);
						int pointSize = CONTROL_POINT_SIZE - 2;
						g2dWrapper.fillOval(controlPoint.x - pointSize / 2, controlPoint.y
								- pointSize / 2, pointSize, pointSize);
					}
				}
			}
			// Then draw the selected ones.
			for (IDIYComponent<?> component : selectedComponents) {
				for (int i = 0; i < component.getControlPointCount(); i++) {
					if (!groupedComponents.contains(component)
							&& (component.getControlPointVisibilityPolicy(i) == VisibilityPolicy.WHEN_SELECTED || component
									.getControlPointVisibilityPolicy(i) == VisibilityPolicy.ALWAYS)) {
						g2dWrapper.setColor(Constants.SELECTED_CONTROL_POINT_COLOR);

						Point controlPoint = component.getControlPoint(i);
						int pointSize = CONTROL_POINT_SIZE;
						g2dWrapper.fillOval(controlPoint.x - pointSize / 2, controlPoint.y
								- pointSize / 2, pointSize, pointSize);
					}
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
		if (DEBUG_COMPONENT_AREAS) {
			g2d.setStroke(new BasicStroke());
			g2d.setColor(Color.red);
			for (Area area : componentAreaMap.values()) {
				g2d.draw(area);
			}
		}
	}

	public void invalidateComponent(IDIYComponent<?> component) {
		lastDrawnStateMap.remove(component);
	}

	public Area getComponentArea(IDIYComponent<?> component) {
		return componentAreaMap.get(component);
	}

	public boolean isCursorOverArea(Point2D cursorPosition) {
		for (Map.Entry<IDIYComponent<?>, Area> entry : componentAreaMap.entrySet()) {
			if (entry.getValue().contains(cursorPosition)) {
				return true;
			}
		}
		return false;
	}

	public List<IDIYComponent<?>> findComponentsAt(Point point, Project project) {
		List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>();
		for (Map.Entry<IDIYComponent<?>, Area> entry : componentAreaMap.entrySet()) {
			if (entry.getValue().contains(point)) {
				components.add(entry.getKey());
			}
		}
		// Sort by z-order.
		Collections.sort(components, ComparatorFactory.getInstance().getComponentZOrderComparator(
				project.getComponents()));
		return components;
	}

	public Dimension getCanvasDimensions(Project project, Double zoomLevel, boolean useZoom) {
		double width = project.getWidth().convertToPixels();
		int height = project.getHeight().convertToPixels();
		if (useZoom) {
			width *= zoomLevel;
			height *= zoomLevel;
		}
		return new Dimension((int) width, (int) height);
	}
}
