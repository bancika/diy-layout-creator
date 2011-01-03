package com.diyfever.diylc.presenter;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import com.diyfever.diylc.common.BadPositionException;
import com.diyfever.diylc.common.ComponentSelection;
import com.diyfever.diylc.common.ControlPointWrapper;
import com.diyfever.diylc.common.DrawOption;
import com.diyfever.diylc.common.EventType;
import com.diyfever.diylc.common.IPlugIn;
import com.diyfever.diylc.common.IPlugInPort;
import com.diyfever.diylc.common.PropertyWrapper;
import com.diyfever.diylc.gui.IView;
import com.diyfever.diylc.model.ComponentLayer;
import com.diyfever.diylc.model.ComponentState;
import com.diyfever.diylc.model.IComponentInstance;
import com.diyfever.diylc.model.IComponentType;
import com.diyfever.diylc.model.Project;
import com.diyfever.diylc.model.VisibilityPolicy;
import com.diyfever.diylc.utils.Constants;
import com.diyfever.gui.miscutils.ConfigurationManager;
import com.diyfever.gui.miscutils.JarScanner;
import com.diyfever.gui.miscutils.Utils;
import com.diyfever.gui.simplemq.MessageDispatcher;
import com.diyfever.gui.update.VersionNumber;
import com.rits.cloning.Cloner;

/**
 * The main presenter class.
 * 
 * @author Branislav Stojkovic
 */
public class Presenter implements IPlugInPort {

	private static final Logger LOG = Logger.getLogger(Presenter.class);

	private static final VersionNumber CURRENT_VERSION = new VersionNumber(0, 0, 0);
	private static final String DEFAULTS_KEY = "defaults";
	private static final int CONTROL_POINT_SENSITIVITY = 4;

	private double zoomLevel = 1;
	private Map<IComponentInstance, Area> componentAreaMap;
	private Project currentProject;
	private Map<String, List<IComponentType>> componentTypes;
	private List<IPlugIn> plugIns;

	private ComponentSelection selectedComponents;
	// List of component names that have at least one of their control points
	// under the last recorded mouse position.
	private Map<IComponentInstance, ControlPointWrapper> componentsUnderCursor;

	private Cloner cloner;

	private Rectangle selectionRect;

	private final IView view;

	private MessageDispatcher<EventType> messageDispatcher;

	// Layers
	private Set<ComponentLayer> lockedLayers;
	private Set<ComponentLayer> visibleLayers;

	// D&D
	private boolean dragInProgress = false;
	// Previous mouse location, not scaled for zoom factor.
	private Point previousDragPoint = null;
	private Project preDragProject = null;

	private IComponentType componentSlot;

	public Presenter(IView view) {
		super();
		this.view = view;
		componentAreaMap = new HashMap<IComponentInstance, Area>();
		plugIns = new ArrayList<IPlugIn>();
		messageDispatcher = new MessageDispatcher<EventType>();
		selectedComponents = new ComponentSelection();
		currentProject = new Project();
		cloner = new Cloner();

		lockedLayers = EnumSet.noneOf(ComponentLayer.class);
		visibleLayers = EnumSet.allOf(ComponentLayer.class);
	}

	public void installPlugin(IPlugIn plugIn) {
		LOG.debug(String.format("installPlugin(%s)", plugIn.getClass().getSimpleName()));
		plugIns.add(plugIn);
		plugIn.connect(this);
		messageDispatcher.registerListener(plugIn);
	}

	public void dispose() {
		for (IPlugIn plugIn : plugIns) {
			messageDispatcher.unregisterListener(plugIn);
		}
	}

	// IPlugInPort

	@Override
	public double getZoomLevel() {
		return zoomLevel;
	}

	@Override
	public void setZoomLevel(double zoomLevel) {
		LOG.debug(String.format("setZoomLevel(%s)", zoomLevel));
		this.zoomLevel = zoomLevel;
		messageDispatcher.dispatchMessage(EventType.ZOOM_CHANGED, zoomLevel);
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public Cursor getCursorAt(Point point) {
		// Only change the cursor if we're not making a new component.
		if (componentSlot == null) {
			// Scale point to remove zoom factor.
			Point2D scaledPoint = scalePoint(point);
			for (Map.Entry<IComponentInstance, Area> entry : componentAreaMap.entrySet()) {
				if (entry.getValue().contains(scaledPoint)) {
					return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				}
			}
			if (componentsUnderCursor != null && !componentsUnderCursor.isEmpty()) {
				return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			}
		}
		return Cursor.getDefaultCursor();
	}

	@Override
	public Dimension getCanvasDimensions(boolean useZoom) {
		double width = currentProject.getWidth().convertToPixels();
		int height = currentProject.getHeight().convertToPixels();
		if (useZoom) {
			width *= zoomLevel;
			height *= zoomLevel;
		}
		return new Dimension((int) width, (int) height);
	}

	@Override
	public Project getCurrentProject() {
		return currentProject;
	}

	@Override
	public void loadProject(Project project, boolean freshStart) {
		LOG.info("Loading project: " + project.getTitle());
		this.currentProject = project;
		selectedComponents.clear();
		messageDispatcher.dispatchMessage(EventType.PROJECT_LOADED, project, freshStart);
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public Map<String, List<IComponentType>> getComponentTypes() {
		if (componentTypes == null) {
			LOG.info("Loading component types.");
			componentTypes = new HashMap<String, List<IComponentType>>();
			List<Class<?>> componentTypeClasses = JarScanner.getInstance().scanFolder("library/",
					IComponentType.class);
			for (Class<?> clazz : componentTypeClasses) {
				try {
					IComponentType componentType = (IComponentType) clazz.newInstance();
					List<IComponentType> nestedList;
					if (componentTypes.containsKey(componentType.getCategory())) {
						nestedList = componentTypes.get(componentType.getCategory());
					} else {
						nestedList = new ArrayList<IComponentType>();
						componentTypes.put(componentType.getCategory(), nestedList);
					}
					nestedList.add(componentType);
				} catch (InstantiationException e) {
					LOG.warn("Could not instantiate: " + clazz.getName());
				} catch (IllegalAccessException e) {
					LOG.warn("Could not access: " + clazz.getName());
				}
			}
		}
		return componentTypes;
	}

	@Override
	public void draw(Graphics2D g2d, Set<DrawOption> drawOptions) {
		if (currentProject == null) {
			return;
		}
		G2DWrapper g2dWrapper = new G2DWrapper(g2d);

		if (drawOptions.contains(DrawOption.ANTIALIASING)) {
			g2d
					.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
		}

		AffineTransform initialTx = g2d.getTransform();
		Dimension d = getCanvasDimensions(drawOptions.contains(DrawOption.ZOOM));

		g2dWrapper.setColor(Color.white);
		g2dWrapper.fillRect(0, 0, d.width, d.height);

		if (drawOptions.contains(DrawOption.GRID)) {
			double zoomStep = Constants.GRID * zoomLevel;
			// Point2D p = new Point2D.Double(step, 0);
			// g2d.getTransform().transform(p, p);
			// System.out.println(p);

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

		Composite mainComposite = g2d.getComposite();
		Composite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

		g2dWrapper.resetTx();

		List<IComponentInstance> components = getCurrentProject().getComponents();
		componentAreaMap.clear();
		if (components != null) {
			for (IComponentInstance component : components) {
				g2dWrapper.startedDrawingComponent();
				ComponentState state = ComponentState.NORMAL;
				if (drawOptions.contains(DrawOption.SELECTION)
						&& selectedComponents.contains(component)) {
					if (dragInProgress) {
						state = ComponentState.DRAGGING;
					} else {
						state = ComponentState.SELECTED;
					}
				}
				// If the component is being dragged, draw it in a separate
				// composite.
				if (state == ComponentState.DRAGGING) {
					g2dWrapper.setComposite(alphaComposite);
				}
				// Draw the component through the g2dWrapper.
				component.draw(g2dWrapper, state);
				// Restore the composite if needed.
				if (state == ComponentState.DRAGGING) {
					g2dWrapper.setComposite(mainComposite);
				}
				componentAreaMap.put(component, g2dWrapper.finishedDrawingComponent());

				// Draw control points
				if (drawOptions.contains(DrawOption.CONTROL_POINTS)) {
					List<ControlPointWrapper> controlPoints = ComponentProcessor.getInstance()
							.extractControlPoints(component.getClass());
					for (ControlPointWrapper controlPoint : controlPoints) {
						try {
							controlPoint.readFrom(component);
							if (shouldShowControlPoint(controlPoint, component)) {
								g2d.setColor(Color.blue);
								g2d.setStroke(new BasicStroke(2));
								g2d.drawOval(controlPoint.getValue().x - 2,
										controlPoint.getValue().y - 2, 4, 4);
								// g2d.fillOval(point.getValue().x - 2, point
								// .getValue().y - 2, 5, 5);
							}
						} catch (Exception e) {
							LOG.error("Could not obtain control points for component of type "
									+ component.getClass().getName());
						}
					}
				}
			}
		}

		g2d.setTransform(initialTx);
		if ((drawOptions.contains(DrawOption.ZOOM)) && (Math.abs(1.0 - zoomLevel) > 1e-4)) {
			g2d.scale(zoomLevel, zoomLevel);
		}

		// At the end draw selection rectangle.
		if (drawOptions.contains(DrawOption.SELECTION) && (selectionRect != null)) {
			g2d.setColor(Color.white);
			g2d.draw(selectionRect);
			g2d.setColor(Color.black);
			g2d.setStroke(Constants.dashedStroke);
			g2d.draw(selectionRect);
		}

		// g2d.setColor(Color.red);
		// for (Area area : componentAreaMap.values()) {
		// g2d.draw(area);
		// }
	}

	@Override
	public void injectGUIComponent(JComponent component, int position) throws BadPositionException {
		view.addComponent(component, position);
	}

	@Override
	public void injectMenuAction(Action action, String menuName) {
		view.addMenuAction(action, menuName);
	}

	// @Override
	// public void setCursorIcon(Icon icon) {
	// view.setCursorIcon(icon);
	// }

	/**
	 * Finds all components whose areas include the specified {@link Point}.
	 * Point is <b>not</b> scaled by the zoom factor.
	 * 
	 * @return
	 */
	private List<IComponentInstance> findComponentsAt(Point point) {
		List<IComponentInstance> components = new ArrayList<IComponentInstance>();
		for (Map.Entry<IComponentInstance, Area> entry : componentAreaMap.entrySet()) {
			if (entry.getValue().contains(point)) {
				components.add(entry.getKey());
			}
		}
		return components;
	}

	@Override
	public void mouseClicked(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
		LOG.debug(String
				.format("mouseClicked(%s, %s, %s, %s)", point, ctrlDown, shiftDown, altDown));
		Point scaledPoint = scalePoint(point);
		if (componentSlot != null) {
			try {
				instantiateComponent(componentSlot.getComponentInstanceClass(), scaledPoint);
			} catch (Exception e) {
				LOG.error("Error instatiating component of type: "
						+ componentSlot.getComponentInstanceClass().getName());
			}
			setNewComponentSlot(null);
		} else {
			List<IComponentInstance> components = findComponentsAt(scaledPoint);
			// If there's nothing under mouse cursor deselect all.
			if (components.isEmpty()) {
				selectedComponents.clear();
			} else {
				IComponentInstance component = components.get(0);
				// If ctrl is pressed just toggle the component under mouse
				// cursor.
				if (ctrlDown) {
					if (selectedComponents.contains(component)) {
						selectedComponents.remove(component);
					} else {
						selectedComponents.add(component);
					}
				} else {
					// Otherwise just select that one component.
					selectedComponents.clear();
					selectedComponents.add(component);
				}
			}
			messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED, selectedComponents);
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		}
	}

	@Override
	public void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
		Map<IComponentInstance, ControlPointWrapper> components = new HashMap<IComponentInstance, ControlPointWrapper>();
		Point scaledPoint = scalePoint(point);
		for (IComponentInstance component : currentProject.getComponents()) {
			List<ControlPointWrapper> currentPoints = ComponentProcessor.getInstance()
					.extractControlPoints(component.getClass());
			for (ControlPointWrapper controlPoint : currentPoints) {
				if (shouldShowControlPoint(controlPoint, component)) {
					try {
						controlPoint.readFrom(component);
						if (scaledPoint.distance(controlPoint.getValue()) < CONTROL_POINT_SENSITIVITY) {
							components.put(component, controlPoint);
							break;
						}
					} catch (Exception e) {
						LOG.warn("Error reading control point for component of type: "
								+ component.getClass().getName());
					}
				}
			}
			// If CTRL is pressed, we only care about the top most component.
			if (altDown && components.size() > 0) {
				break;
			}
		}
		if (!components.equals(componentsUnderCursor)) {
			componentsUnderCursor = components;
			messageDispatcher.dispatchMessage(EventType.AVAILABLE_CTRL_POINTS_CHANGED, components);
		}
	}

	@Override
	public ComponentSelection getSelectedComponents() {
		return new ComponentSelection(selectedComponents);
	}

	@Override
	public Area getComponentArea(IComponentInstance component) {
		return componentAreaMap.get(component);
	}

	@Override
	public VersionNumber getCurrentVersionNumber() {
		return CURRENT_VERSION;
	}

	@Override
	public void dragStarted(Point point) {
		LOG.debug(String.format("dragStarted(%s)", point));
		dragInProgress = true;
		preDragProject = cloner.deepClone(currentProject);
		Point scaledPoint = scalePoint(point);
		previousDragPoint = scaledPoint;
		List<IComponentInstance> components = findComponentsAt(scaledPoint);
		if (!componentsUnderCursor.isEmpty()) {
			// If there are control points under the cursor, drag them.
		} else if (components.isEmpty()) {
			// If no components are under the cursor, reset selection.
			selectedComponents.clear();
			messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED, selectedComponents);
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		} else {
			IComponentInstance component = components.get(0);
			// If the component under the cursor is not already selected, make
			// it into the only selected component.
			if (!selectedComponents.contains(component)) {
				selectedComponents.clear();
				selectedComponents.add(component);
				messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED, selectedComponents);
				messageDispatcher.dispatchMessage(EventType.REPAINT);
			}
		}
	}

	@Override
	public boolean dragOver(Point point) {
		Point scaledPoint = scalePoint(point);
		if (!componentsUnderCursor.isEmpty()) {
			// We're dragging control point(s).
			// int dx = (int) ((point.x - dragStartPoint.x) / zoomLevel);
			// int dy = (int) ((point.y - dragStartPoint.y) / zoomLevel);
			//			
			IComponentInstance firstComponent = componentsUnderCursor.keySet().iterator().next();
			ControlPointWrapper controlPoint = componentsUnderCursor.get(firstComponent);
			// Re-read the value just in case.
			try {
				controlPoint.readFrom(firstComponent);
			} catch (Exception e) {
				e.printStackTrace();
			}
			int x = (int) (Math
					.round((controlPoint.getValue().x + scaledPoint.x - previousDragPoint.x)
							/ Constants.GRID) * Constants.GRID);
			int y = (int) (Math
					.round((controlPoint.getValue().y + scaledPoint.y - previousDragPoint.y)
							/ Constants.GRID) * Constants.GRID);
			previousDragPoint.setLocation(x, y);

			for (Entry<IComponentInstance, ControlPointWrapper> entry : componentsUnderCursor
					.entrySet()) {
				try {
					controlPoint = entry.getValue();
					IComponentInstance component = entry.getKey();
					controlPoint.readFrom(component);
					if (controlPoint.isEditable()) {
						controlPoint.getValue().setLocation(x, y);
					}
					controlPoint.writeTo(component);
				} catch (Exception e) {

				}
			}
			// dragStartPoint = point;
		} else if (selectedComponents.isEmpty()) {
			// If there's no selection, the only thing to do is update the
			// selection rectangle and refresh.
			this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_RECT_CHANGED,
			// selectionRect);
		} else {
			// If there are components selected translate their control points.
			translateSelectedComponents(previousDragPoint, scaledPoint);
			// dragStartPoint = point;
		}
		messageDispatcher.dispatchMessage(EventType.REPAINT);
		return true;
	}

	@Override
	public void dragEnded(Point point) {
		LOG.debug(String.format("dragEnded(%s)", point));
		if (!dragInProgress) {
			return;
		}
		Point scaledPoint = scalePoint(point);
		if (selectedComponents.isEmpty()) {
			// If there's no selection finalize selectionRect and see which
			// components intersect with it.
			if (scaledPoint != null) {
				this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
			}
			selectedComponents.clear();
			for (IComponentInstance component : currentProject.getComponents()) {
				Area area = componentAreaMap.get(component);
				if ((area != null) && area.intersects(selectionRect)) {
					selectedComponents.add(component);
				}
			}
			selectionRect = null;
			messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED, selectedComponents);
		}
		// There is selection, so we need to finalize the drag&drop
		// operation.

		if (!preDragProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, preDragProject, cloner
					.deepClone(currentProject), "Move");
		}
		messageDispatcher.dispatchMessage(EventType.REPAINT);
		dragInProgress = false;
	}

	private void translateSelectedComponents(Point fromPoint, Point toPoint) {
		if (toPoint == null) {
			LOG.debug("Drag ended outside the drawing area.");
			return;
		}
		int dx = (int) (Math.round((toPoint.x - fromPoint.x) / zoomLevel / Constants.GRID) * Constants.GRID);
		int dy = (int) (Math.round((toPoint.y - fromPoint.y) / zoomLevel / Constants.GRID) * Constants.GRID);
		fromPoint.translate(dx, dy);
		for (IComponentInstance component : selectedComponents) {
			List<ControlPointWrapper> controlPoints = ComponentProcessor.getInstance()
					.extractControlPoints(component.getClass());
			for (ControlPointWrapper controlPoint : controlPoints) {
				try {
					controlPoint.readFrom(component);
					if (controlPoint.isEditable()) {
						translateControlPoint(controlPoint, dx, dy);
					}
					controlPoint.writeTo(component);
				} catch (Exception e) {
					LOG.error("Could not translate control points: " + e.getMessage());
				}
			}
		}
	}

	private void translateControlPoint(ControlPointWrapper controlPoint, int dx, int dy) {
		int x = controlPoint.getValue().x + dx;
		int y = controlPoint.getValue().y + dy;
		controlPoint.getValue().setLocation(x, y);
	}

	@Override
	public void addComponents(List<IComponentInstance> components, Point preferredPoint) {
		LOG.debug(String.format("addComponents(%s)", components));
		Project oldProject = cloner.deepClone(currentProject);
		currentProject.getComponents().addAll(components);
		messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
				.deepClone(currentProject), "Add");
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	public boolean isLayerLocked(ComponentLayer layer) {
		return lockedLayers.contains(layer);
	}

	public void setLayerLocked(ComponentLayer layer, boolean locked) {
		LOG.debug(String.format("setLayerLocked(%s, %s)", layer, locked));
		if (locked) {
			lockedLayers.add(layer);
		} else {
			lockedLayers.remove(layer);
		}
	}

	public boolean isLayerVisible(ComponentLayer layer) {
		return visibleLayers.contains(layer);
	}

	public void setLayerVisible(ComponentLayer layer, boolean visible) {
		LOG.debug(String.format("setLayerVisible(%s, %s)", layer, visible));
		if (visible) {
			visibleLayers.add(layer);
		} else {
			visibleLayers.remove(layer);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	// @Override
	// public Object getDefaultPropertyValue(
	// Class<? extends IComponentInstance> componentClass,
	// String propertyName) {
	// return configuration.getDefaultPropertyValues().get(
	// componentClass.getName() + ":" + propertyName);
	// }
	public void setDefaultPropertyValue(String propertyName, Object value) {
		LOG.debug(String.format("setDefaultPropertyValue(%s, %s)", propertyName, value));
		Map<String, Object> defaultMap = (Map<String, Object>) ConfigurationManager.getInstance()
				.getConfigurationItem(DEFAULTS_KEY);
		if (defaultMap == null) {
			defaultMap = new HashMap<String, Object>();
			ConfigurationManager.getInstance().setConfigurationItem(DEFAULTS_KEY, defaultMap);
		}
		for (IComponentInstance component : selectedComponents) {
			String className = component.getClass().getName();
			LOG.debug("Default property value set for " + className + ":" + propertyName);
			defaultMap.put(className + ":" + propertyName, value);
		}
	}

	@SuppressWarnings("unchecked")
	private void instantiateComponent(Class<? extends IComponentInstance> componentClass,
			Point point) throws Exception {
		LOG.info("Instatiating component of type: " + componentClass.getName());

		Project oldProject = cloner.deepClone(currentProject);

		// Instantiate the component.
		IComponentInstance component = componentClass.newInstance();
		// Add it to the project.
		currentProject.getComponents().add(component);

		// Extract control points.
		List<ControlPointWrapper> controlPoints = ComponentProcessor.getInstance()
				.extractControlPoints(componentClass);
		// Translate them to the desired location.
		if (point != null) {
			for (ControlPointWrapper controlPoint : controlPoints) {
				controlPoint.readFrom(component);
				int x = controlPoint.getValue().x + point.x;
				int y = controlPoint.getValue().y + point.y;
				x = (int) (Math.round(x / Constants.GRID) * Constants.GRID);
				y = (int) (Math.round(y / Constants.GRID) * Constants.GRID);
				controlPoint.getValue().setLocation(x, y);
				controlPoint.writeTo(component);
			}
		}

		// Extract properties.
		List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(
				componentClass);
		// Override with default values if available.
		for (PropertyWrapper property : properties) {
			Object defaultValue = null;
			Map<String, Object> defaultMap = (Map<String, Object>) ConfigurationManager
					.getInstance().getConfigurationItem(DEFAULTS_KEY);
			if (defaultMap != null) {
				defaultValue = defaultMap.get(componentClass.getName() + ":" + property.getName());
			}
			if (defaultValue != null) {
				property.setValue(cloner.deepClone(defaultValue));
				property.writeTo(component);
			}
		}

		// Notify the listeners.
		if (!oldProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
					.deepClone(currentProject), "Create");
		}
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public List<PropertyWrapper> getMutualSelectionProperties() {
		return ComponentProcessor.getInstance().getMutualSelectionProperties(selectedComponents);
	}

	@Override
	public void applyPropertiesToSelection(List<PropertyWrapper> properties) throws Exception {
		LOG.debug(String.format("applyPropertiesToSelection(%s)", properties));
		Project oldProject = cloner.deepClone(currentProject);
		try {
			for (IComponentInstance component : selectedComponents) {
				for (PropertyWrapper property : properties) {
					property.writeTo(component);
				}
			}
		} finally {
			// Notify the listeners.
			if (!oldProject.equals(currentProject)) {
				messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
						.deepClone(currentProject), "Edit");
			}
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		}
	}

	@Override
	public void setNewComponentSlot(IComponentType componentType) {
		LOG.debug(String.format("setNewComponentSlot(%s)", componentType == null ? null
				: componentType.getName()));
		this.componentSlot = componentType;
		selectedComponents.clear();
		messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED);
		messageDispatcher.dispatchMessage(EventType.SLOT_CHANGED, componentSlot);
	}

	/**
	 * Scales point from display base to actual base.
	 * 
	 * @param point
	 * @return
	 */
	private Point scalePoint(Point point) {
		return new Point((int) (point.x / zoomLevel), (int) (point.y / zoomLevel));
	}

	private boolean shouldShowControlPoint(ControlPointWrapper controlPoint,
			IComponentInstance component) {
		return controlPoint.getVisibilityPolicy().equals(VisibilityPolicy.ALWAYS)
				|| ((controlPoint.getVisibilityPolicy().equals(VisibilityPolicy.WHEN_SELECTED)) && (getSelectedComponents()
						.contains(component)));
	}
}
