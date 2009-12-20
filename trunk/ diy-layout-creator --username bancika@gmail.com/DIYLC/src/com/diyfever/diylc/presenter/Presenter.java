package com.diyfever.diylc.presenter;

import java.awt.BasicStroke;
import java.awt.Color;
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

	private static final VersionNumber CURRENT_VERSION = new VersionNumber(0,
			0, 0);
	private static final String DEFAULTS_KEY = "defaults";

	private double zoomLevel = 1;
	private Map<IComponentInstance, Area> componentAreaMap;
	private Project currentProject;
	private Map<String, List<IComponentType>> componentTypes;
	private List<IPlugIn> plugIns;
	private ComponentSelection selectedComponents;
	private Cloner cloner;

	private Rectangle selectionRect;

	private final IView view;

	private MessageDispatcher<EventType> messageDispatcher;

	// Layers
	private Set<ComponentLayer> lockedLayers;
	private Set<ComponentLayer> visibleLayers;

	// D&D
	private boolean dragInProgress = false;
	private Point dragStartPoint = null;

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
		LOG.debug(String.format("installPlugin(%s)", plugIn.getClass()
				.getSimpleName()));
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
		// Scale point to remove zoom factor.
		Point2D scaledPoint = new Point2D.Double(point.getX() / zoomLevel,
				point.getY() / zoomLevel);
		for (Map.Entry<IComponentInstance, Area> entry : componentAreaMap
				.entrySet()) {
			if (entry.getValue().contains(scaledPoint)) {
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
		messageDispatcher.dispatchMessage(EventType.PROJECT_LOADED, project,
				freshStart);
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public Map<String, List<IComponentType>> getComponentTypes() {
		if (componentTypes == null) {
			LOG.info("Loading component types.");
			componentTypes = new HashMap<String, List<IComponentType>>();
			List<Class<?>> componentTypeClasses = JarScanner.getInstance()
					.scanFolder("library/", IComponentType.class);
			for (Class<?> clazz : componentTypeClasses) {
				try {
					IComponentType componentType = (IComponentType) clazz
							.newInstance();
					List<IComponentType> nestedList;
					if (componentTypes.containsKey(componentType.getCategory())) {
						nestedList = componentTypes.get(componentType
								.getCategory());
					} else {
						nestedList = new ArrayList<IComponentType>();
						componentTypes.put(componentType.getCategory(),
								nestedList);
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
	public void draw(Graphics2D g2d, EnumSet<DrawOption> drawOptions) {
		if (currentProject == null) {
			return;
		}
		G2DWrapper g2dWrapper = new G2DWrapper(g2d);

		if (drawOptions.contains(DrawOption.ANTIALIASING)) {
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
		}

		AffineTransform initialTx = g2d.getTransform();
		Dimension d = getCanvasDimensions(drawOptions
				.contains(DrawOption.ZOOM));

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

		if ((drawOptions.contains(DrawOption.ZOOM))
				&& (Math.abs(1.0 - zoomLevel) > 1e-4)) {
			g2dWrapper.scale(zoomLevel, zoomLevel);
		}

		g2dWrapper.resetTx();

		List<IComponentInstance> components = getCurrentProject()
				.getComponents();
		componentAreaMap.clear();
		if (components != null) {
			for (IComponentInstance component : components) {
				g2dWrapper.startedDrawingComponent();
				ComponentState state = (selectedComponents.contains(component) && drawOptions
						.contains(DrawOption.SELECTION)) ? ComponentState.SELECTED
						: ComponentState.NORMAL;
				component.draw(g2dWrapper, state);
				componentAreaMap.put(component, g2dWrapper
						.finishedDrawingComponent());

				// Draw control points
				if (drawOptions.contains(DrawOption.CONTROL_POINTS)) {
					List<ControlPointWrapper> controlPoints = ComponentProcessor
							.getInstance().extractControlPoints(
									component.getClass());
					for (ControlPointWrapper point : controlPoints) {
						try {
							point.readFrom(component);
							if (point.getVisibilityPolicy().equals(
									VisibilityPolicy.ALWAYS)
									|| ((point.getVisibilityPolicy()
											.equals(VisibilityPolicy.WHEN_SELECTED)) && (getSelectedComponents()
											.contains(component)))) {
								g2d.setColor(Color.blue);
								g2d.setStroke(new BasicStroke(2));
								g2d.drawOval(point.getValue().x - 2, point
										.getValue().y - 2, 4, 4);
								// g2d.fillOval(point.getValue().x - 2, point
								// .getValue().y - 2, 5, 5);
							}
						} catch (Exception e) {
							LOG
									.error("Could not obtain control points for component of type "
											+ component.getClass().getName());
						}
					}
				}
			}
		}

		g2d.setTransform(initialTx);

		// At the end draw selection rectangle.
		if (drawOptions.contains(DrawOption.SELECTION)
				&& (selectionRect != null)) {
			g2d.setColor(Color.white);
			g2d.drawRect(selectionRect.x, selectionRect.y, selectionRect.width,
					selectionRect.height);
			g2d.setColor(Color.black);
			g2d.setStroke(Constants.dashedStroke);
			g2d.drawRect(selectionRect.x, selectionRect.y, selectionRect.width,
					selectionRect.height);
		}
	}

	@Override
	public void injectGUIComponent(JComponent component, int position)
			throws BadPositionException {
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

	private List<IComponentInstance> findComponentsAt(Point point) {
		List<IComponentInstance> components = new ArrayList<IComponentInstance>();
		for (Map.Entry<IComponentInstance, Area> entry : componentAreaMap
				.entrySet()) {
			if (entry.getValue().contains(point.x / zoomLevel,
					point.y / zoomLevel)) {
				components.add(entry.getKey());
			}
		}
		return components;
	}

	@Override
	public void pointClickedOn(Point point, boolean ctrlDown,
			boolean shiftDown, boolean altDown) {
		LOG.debug(String.format("pointClickedOn(%s, %s, %s, %s)", point,
				ctrlDown, shiftDown, altDown));
		if (componentSlot != null) {
			try {
				instantiateComponent(componentSlot.getComponentInstanceClass(),
						point);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			setNewComponentSlot(null);
		} else {
			List<IComponentInstance> components = findComponentsAt(point);
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
			messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
					selectedComponents);
			messageDispatcher.dispatchMessage(EventType.REPAINT);
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
		dragStartPoint = point;
		List<IComponentInstance> components = findComponentsAt(point);
		if (components.isEmpty()) {
			selectedComponents.clear();
			messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
					selectedComponents);
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		} else {
			IComponentInstance component = components.get(0);
			if (!selectedComponents.contains(component)) {
				selectedComponents.clear();
				selectedComponents.add(component);
				messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
						selectedComponents);
				messageDispatcher.dispatchMessage(EventType.REPAINT);
			}
		}
	}

	@Override
	public boolean dragOver(Point point) {
		if (selectedComponents.isEmpty()) {
			this.selectionRect = Utils.createRectangle(point, dragStartPoint);
			messageDispatcher.dispatchMessage(EventType.SELECTION_RECT_CHANGED,
					selectionRect);
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		}
		return true;
	}

	@Override
	public void dragEnded(Point point) {
		LOG.debug(String.format("dragEnded(%s)", point));
		if (!dragInProgress) {
			return;
		}
		if (selectedComponents.isEmpty()) {
			// If there's no selection finalize selectionRect and see which
			// components intersect with it.
			if (point != null) {
				this.selectionRect = Utils.createRectangle(point,
						dragStartPoint);
			}
			selectedComponents.clear();
			for (IComponentInstance component : currentProject.getComponents()) {
				Area area = componentAreaMap.get(component);
				if ((area != null)
						&& area.intersects(selectionRect.x / zoomLevel,
								selectionRect.y / zoomLevel,
								selectionRect.width / zoomLevel,
								selectionRect.height / zoomLevel)) {
					selectedComponents.add(component);
				}
			}
			selectionRect = null;
			messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
					selectedComponents);
		} else {
			Project oldProject = cloner.deepClone(currentProject);

			// If there are components selected translate their control points.
			int dx = (int) ((point.x - dragStartPoint.x) / zoomLevel);
			int dy = (int) ((point.y - dragStartPoint.y) / zoomLevel);
			for (IComponentInstance component : selectedComponents) {
				List<ControlPointWrapper> controlPoints = ComponentProcessor
						.getInstance().extractControlPoints(
								component.getClass());
				for (ControlPointWrapper controlPoint : controlPoints) {
					try {
						controlPoint.readFrom(component);
						if (controlPoint.isEditable()) {
							controlPoint.getValue().translate(dx, dy);
						}
						controlPoint.writeTo(component);
					} catch (Exception e) {
						LOG.error("Could not translate control points: "
								+ e.getMessage());
					}
				}
			}
			if (!oldProject.equals(currentProject)) {
				messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED,
						oldProject, cloner.deepClone(currentProject), "Move");
			}
		}
		messageDispatcher.dispatchMessage(EventType.REPAINT);
		dragInProgress = false;
	}

	@Override
	public void addComponents(List<IComponentInstance> components,
			Point preferredPoint) {
		LOG.debug(String.format("addComponents(%s)", components));
		Project oldProject = cloner.deepClone(currentProject);
		currentProject.getComponents().addAll(components);
		messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED,
				oldProject, cloner.deepClone(currentProject), "Add");
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
		LOG.debug(String.format("setDefaultPropertyValue(%s, %s)",
				propertyName, value));
		Map<String, Object> defaultMap = (Map<String, Object>) ConfigurationManager
				.getInstance().getConfigurationItem(DEFAULTS_KEY);
		if (defaultMap == null) {
			defaultMap = new HashMap<String, Object>();
			ConfigurationManager.getInstance().setConfigurationItem(
					DEFAULTS_KEY, defaultMap);
		}
		for (IComponentInstance component : selectedComponents) {
			String className = component.getClass().getName();
			LOG.debug("Default property value set for " + className + ":"
					+ propertyName);
			defaultMap.put(className + ":" + propertyName, value);
		}
	}

	@SuppressWarnings("unchecked")
	private void instantiateComponent(
			Class<? extends IComponentInstance> componentClass, Point point)
			throws Exception {
		LOG.info("Instatiating component of type: " + componentClass.getName());

		Project oldProject = cloner.deepClone(currentProject);

		// Instantiate the component.
		IComponentInstance component = componentClass.newInstance();
		// Add it to the project.
		currentProject.getComponents().add(component);

		// Extract control points.
		List<ControlPointWrapper> controlPoints = ComponentProcessor
				.getInstance().extractControlPoints(componentClass);
		// Translate them to the desired location.
		if (point != null) {
			for (ControlPointWrapper controlPoint : controlPoints) {
				controlPoint.readFrom(component);
				controlPoint.getValue().translate(point.x, point.y);
				controlPoint.writeTo(component);
			}
		}

		// Extract properties.
		List<PropertyWrapper> properties = ComponentProcessor.getInstance()
				.extractProperties(componentClass);
		// Override with default values if available.
		for (PropertyWrapper property : properties) {
			Object defaultValue = null;
			Map<String, Object> defaultMap = (Map<String, Object>) ConfigurationManager
					.getInstance().getConfigurationItem(DEFAULTS_KEY);
			if (defaultMap != null) {
				defaultValue = defaultMap.get(componentClass.getName() + ":"
						+ property.getName());
			}
			if (defaultValue != null) {
				property.setValue(cloner.deepClone(defaultValue));
				property.writeTo(component);
			}
		}

		// Notify the listeners.
		if (!oldProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED,
					oldProject, cloner.deepClone(currentProject), "Create");
		}
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public List<PropertyWrapper> getMutualSelectionProperties() {
		return ComponentProcessor.getInstance().getMutualSelectionProperties(
				selectedComponents);
	}

	@Override
	public void applyPropertiesToSelection(List<PropertyWrapper> properties)
			throws Exception {
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
				messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED,
						oldProject, cloner.deepClone(currentProject), "Edit");
			}
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		}
	}

	@Override
	public void setNewComponentSlot(IComponentType componentType) {
		LOG.debug(String.format("setNewComponentSlot(%s)",
				componentType == null ? null : componentType.getName()));
		this.componentSlot = componentType;
		selectedComponents.clear();
		messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED);
		messageDispatcher
				.dispatchMessage(EventType.SLOT_CHANGED, componentSlot);
	}
}
