package org.diylc.presenter;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.diylc.common.BadPositionException;
import org.diylc.common.ComponentSelection;
import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.IComponentFiler;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.SizeUnit;
import org.diylc.gui.IView;
import org.diylc.utils.Constants;

import com.diyfever.gui.miscutils.ConfigurationManager;
import com.diyfever.gui.miscutils.JarScanner;
import com.diyfever.gui.miscutils.Utils;
import com.diyfever.gui.simplemq.MessageDispatcher;
import com.diyfever.gui.update.VersionNumber;
import com.rits.cloning.Cloner;

/**
 * The main presenter class, contains core app logic and drawing routines.
 * 
 * @author Branislav Stojkovic
 */
public class Presenter implements IPlugInPort {

	private static final Logger LOG = Logger.getLogger(Presenter.class);

	public static final VersionNumber CURRENT_VERSION = new VersionNumber(3, 0, 1);
	public static final String DEFAULTS_KEY_PREFIX = "default.";
	public static final String METRIC_KEY = "metric";

	public static final int ICON_SIZE = 32;

	private Project currentProject;
	private Map<String, List<ComponentType>> componentTypes;
	// Maps component class names to ComponentType objects.
	private Map<String, ComponentType> componentTypeMap;
	private List<IPlugIn> plugIns;

	private ComponentSelection selectedComponents;
	// Maps components that have at least one dragged point to set of indices
	// that designate which of their control points are being dragged.
	private Map<IDIYComponent<?>, Set<Integer>> controlPointMap;

	// Utilities
	private Cloner cloner;
	private DrawingManager drawingManager;
	private ProjectFileManager projectFileManager;
	private InstantiationManager instantiationManager;

	private Rectangle selectionRect;

	private final IView view;

	private MessageDispatcher<EventType> messageDispatcher;

	// Layers
	// private Set<ComponentLayer> lockedLayers;
	// private Set<ComponentLayer> visibleLayers;

	// D&D
	private boolean dragInProgress = false;
	// Previous mouse location, not scaled for zoom factor.
	private Point previousDragPoint = null;
	private Project preDragProject = null;

	private boolean snapToGrid = true;

	public Presenter(IView view) {
		super();
		this.view = view;
		plugIns = new ArrayList<IPlugIn>();
		messageDispatcher = new MessageDispatcher<EventType>();
		selectedComponents = new ComponentSelection();
		currentProject = new Project();
		cloner = new Cloner();
		drawingManager = new DrawingManager(messageDispatcher);
		projectFileManager = new ProjectFileManager(messageDispatcher);
		instantiationManager = new InstantiationManager();

		// lockedLayers = EnumSet.noneOf(ComponentLayer.class);
		// visibleLayers = EnumSet.allOf(ComponentLayer.class);
	}

	public void installPlugin(IPlugIn plugIn) {
		LOG.info(String.format("installPlugin(%s)", plugIn.getClass().getSimpleName()));
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
	public Double[] getAvailableZoomLevels() {
		return new Double[] { 0.25d, 0.3333d, 0.5d, 0.6667d, 0.75d, 1d, 1.25d, 1.5d, 2d };
	}

	@Override
	public double getZoomLevel() {
		return drawingManager.getZoomLevel();
	}

	@Override
	public void setZoomLevel(double zoomLevel) {
		LOG.info(String.format("setZoomLevel(%s)", zoomLevel));
		if (drawingManager.getZoomLevel() == zoomLevel) {
			return;
		}
		drawingManager.setZoomLevel(zoomLevel);
	}

	@Override
	public Cursor getCursorAt(Point point) {
		// Only change the cursor if we're not making a new component.
		if (instantiationManager.getComponentTypeSlot() == null) {
			// Scale point to remove zoom factor.
			Point2D scaledPoint = scalePoint(point);
			if (drawingManager.isCursorOverArea(scaledPoint)) {
				return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			}
			if (controlPointMap != null && !controlPointMap.isEmpty()) {
				return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			}
		}
		return Cursor.getDefaultCursor();
	}

	@Override
	public Dimension getCanvasDimensions(boolean useZoom) {
		return drawingManager.getCanvasDimensions(currentProject, drawingManager.getZoomLevel(),
				useZoom);
	}

	@Override
	public Project getCurrentProject() {
		return currentProject;
	}

	@Override
	public void loadProject(Project project, boolean freshStart) {
		LOG.info(String.format("loadProject(%s, %s)", project.getTitle(), freshStart));
		this.currentProject = project;
		updateSelection(ComponentSelection.EMPTY_SELECTION);
		messageDispatcher.dispatchMessage(EventType.PROJECT_LOADED, project, freshStart);
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public void createNewProject() {
		LOG.info("createNewFile()");
		try {
			Project project = new Project();
			instantiationManager.fillWithDefaultProperties(project);
			loadProject(project, true);
			projectFileManager.fireFileStatusChanged();
		} catch (Exception e) {
			LOG.error("Could not create new file", e);
			view.showMessage("Could not create a new file. Check the log for details.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void loadProjectFromFile(String fileName) {
		LOG.info(String.format("loadProjectFromFile(%s)", fileName));
		try {
			Project project = (Project) projectFileManager.deserializeProjectFromFile(fileName);
			loadProject(project, true);
			projectFileManager.fireFileStatusChanged();
		} catch (Exception ex) {
			LOG.error("Could not load file", ex);
			view.showMessage("Could not open file " + fileName + ". Check the log for details.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public boolean allowFileAction() {
		if (projectFileManager.isModified()) {
			int response = view.showConfirmDialog(
					"There are unsaved changes. Are you sure you want to abandon these changes?",
					"Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			return response == JOptionPane.YES_OPTION;
		}
		return true;
	}

	@Override
	public void saveProjectToFile(String fileName) {
		LOG.info(String.format("saveProjectToFile(%s)", fileName));
		try {
			projectFileManager.serializeProjectToFile(currentProject, fileName);
		} catch (Exception ex) {
			LOG.error("Could not save file", ex);
			view.showMessage("Could not save file " + fileName + ". Check the log for details.",
					"Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public String getCurrentFileName() {
		return projectFileManager.getCurrentFileName();
	}

	@Override
	public boolean isProjectModified() {
		return projectFileManager.isModified();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, List<ComponentType>> getComponentTypes() {
		if (componentTypes == null) {
			LOG.info("Loading component types.");
			componentTypes = new HashMap<String, List<ComponentType>>();
			componentTypeMap = new HashMap<String, ComponentType>();
			List<Class<?>> componentTypeClasses = JarScanner.getInstance().scanFolder("library/",
					IDIYComponent.class);
			for (Class<?> clazz : componentTypeClasses) {
				if (!Modifier.isAbstract(clazz.getModifiers())) {
					ComponentType componentType = ComponentProcessor.getInstance()
							.createComponentTypeFrom((Class<? extends IDIYComponent<?>>) clazz);
					componentTypeMap.put(componentType.getInstanceClass().getName(), componentType);
					List<ComponentType> nestedList;
					if (componentTypes.containsKey(componentType.getCategory())) {
						nestedList = componentTypes.get(componentType.getCategory());
					} else {
						nestedList = new ArrayList<ComponentType>();
						componentTypes.put(componentType.getCategory(), nestedList);
					}
					nestedList.add(componentType);
				}
			}
		}
		return componentTypes;
	}

	@Override
	public void draw(Graphics2D g2d, Set<DrawOption> drawOptions, IComponentFiler filter) {
		if (currentProject == null) {
			return;
		}
		Set<IDIYComponent<?>> groupedComponents = new HashSet<IDIYComponent<?>>();
		for (IDIYComponent<?> component : currentProject.getComponents()) {
			// Only try to draw control points of ungrouped components.
			if (findAllGroupedComponents(component).size() > 1) {
				groupedComponents.add(component);
			}
		}
		// Don't draw the component in the slot if both control points match.
		IDIYComponent<?> componentSlotToDraw;
		if (instantiationManager.getFirstControlPoint() != null
				&& instantiationManager.getPotentialControlPoint() != null
				&& instantiationManager.getFirstControlPoint().equals(
						instantiationManager.getPotentialControlPoint())) {
			componentSlotToDraw = null;
		} else {
			componentSlotToDraw = instantiationManager.getComponentSlot();
		}
		drawingManager.drawProject(g2d, currentProject, drawOptions, filter, selectionRect,
				selectedComponents, groupedComponents, Arrays.asList(instantiationManager
						.getFirstControlPoint(), instantiationManager.getPotentialControlPoint()),
				componentSlotToDraw, dragInProgress);
	}

	@Override
	public void injectGUIComponent(JComponent component, int position) throws BadPositionException {
		LOG.info(String.format("injectGUIComponent(%s, %s)", component.getClass().getName(),
				position));
		view.addComponent(component, position);
	}

	@Override
	public void injectMenuAction(Action action, String menuName) {
		LOG.info(String.format("injectMenuAction(%s, %s)", action == null ? "Separator" : action
				.getValue(Action.NAME), menuName));
		view.addMenuAction(action, menuName);
	}

	@Override
	public void injectSubmenu(String name, Icon icon, String parentMenuName) {
		LOG.info(String.format("injectSubmenu(%s, icon, %s)", name, parentMenuName));
		view.addSubmenu(name, icon, parentMenuName);
	}

	/**
	 * Finds all components whose areas include the specified {@link Point}.
	 * Point is <b>not</b> scaled by the zoom factor.
	 * 
	 * @return
	 */
	private List<IDIYComponent<?>> findComponentsAt(Point point) {
		return drawingManager.findComponentsAt(point, currentProject);
	}

	@Override
	public void mouseClicked(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
		LOG.debug(String
				.format("mouseClicked(%s, %s, %s, %s)", point, ctrlDown, shiftDown, altDown));
		Point scaledPoint = scalePoint(point);
		if (instantiationManager.getComponentTypeSlot() != null) {
			// Keep the reference to component type for later.
			ComponentType componentTypeSlot = instantiationManager.getComponentTypeSlot();
			Project oldProject = cloner.deepClone(currentProject);
			switch (componentTypeSlot.getCreationMethod()) {
			case SINGLE_CLICK:
				try {
					if (snapToGrid) {
						CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
					}
					IDIYComponent<?> component = instantiationManager.instantiateComponent(
							componentTypeSlot, scaledPoint, currentProject);
					addComponent(component, componentTypeSlot);
					// Select the new component
					updateSelection(ComponentSelection.of(component));
					// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
					// selectedComponents);
					// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
					// calculateSelectionDimension());
					messageDispatcher.dispatchMessage(EventType.REPAINT);
				} catch (Exception e) {
					LOG.error("Error instatiating component of type: "
							+ componentTypeSlot.getInstanceClass().getName(), e);
				}
				setNewComponentTypeSlot(null);
				break;
			case POINT_BY_POINT:
				// First click is just to set the controlPointSlot and
				// componentSlot.
				if (snapToGrid) {
					CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
				}
				if (instantiationManager.getComponentSlot() == null) {
					try {
						instantiationManager.instatiatePointByPoint(scaledPoint, currentProject);
					} catch (Exception e) {
						view.showMessage("Could not create component. Check log for details.",
								"Error", JOptionPane.ERROR_MESSAGE);
						LOG.error("Could not create component", e);
					}
					messageDispatcher.dispatchMessage(EventType.SLOT_CHANGED, componentTypeSlot,
							instantiationManager.getFirstControlPoint());
					messageDispatcher.dispatchMessage(EventType.REPAINT);
				} else {
					// On the second click, add the component to the project.
					IDIYComponent<?> componentSlot = instantiationManager.getComponentSlot();
					componentSlot.setControlPoint(scaledPoint, 1);
					addComponent(componentSlot, componentTypeSlot);
					// Select the new component
					updateSelection(ComponentSelection.of(componentSlot));
					messageDispatcher.dispatchMessage(EventType.REPAINT);
					setNewComponentTypeSlot(null);
				}
				break;
			default:
				LOG.error("Unknown creation method: " + componentTypeSlot.getCreationMethod());
			}
			// Notify the listeners.
			if (!oldProject.equals(currentProject)) {
				messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
						.deepClone(currentProject), "Add " + componentTypeSlot.getName());
				projectFileManager.notifyFileChange();
			}
		} else {
			ComponentSelection newSelection = new ComponentSelection(selectedComponents);
			List<IDIYComponent<?>> components = findComponentsAt(scaledPoint);
			// If there's nothing under mouse cursor deselect all.
			if (components.isEmpty()) {
				newSelection.clear();
			} else {
				IDIYComponent<?> component = components.get(components.size() - 1);
				// If ctrl is pressed just toggle the component under mouse
				// cursor.
				if (ctrlDown) {
					if (newSelection.contains(component)) {
						newSelection.removeAll(findAllGroupedComponents(component));
					} else {
						newSelection.addAll(findAllGroupedComponents(component));
					}
				} else {
					// Otherwise just select that one component.
					newSelection.clear();
					newSelection.addAll(findAllGroupedComponents(component));
				}
			}
			updateSelection(newSelection);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
			// selectedComponents);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
			// calculateSelectionDimension());
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		}
	}

	@Override
	public void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
		Map<IDIYComponent<?>, Set<Integer>> components = new HashMap<IDIYComponent<?>, Set<Integer>>();
		Point scaledPoint = scalePoint(point);
		if (instantiationManager.getComponentTypeSlot() != null) {
			if (snapToGrid) {
				CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
			}
			switch (instantiationManager.getComponentTypeSlot().getCreationMethod()) {
			case POINT_BY_POINT:
				instantiationManager.updatePointByPoint(scaledPoint);
				break;
			case SINGLE_CLICK:
				instantiationManager.updateSingleClick(scaledPoint, snapToGrid, currentProject
						.getGridSpacing());
				break;
			}
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		} else {
			// Go backwards so we take the highest z-order components first.
			for (int i = currentProject.getComponents().size() - 1; i >= 0; i--) {
				IDIYComponent<?> component = currentProject.getComponents().get(i);
				ComponentType componentType = componentTypeMap.get(component.getClass().getName());
				for (int pointIndex = 0; pointIndex < component.getControlPointCount(); pointIndex++) {
					Point controlPoint = component.getControlPoint(pointIndex);
					// Only consider selected components that are not grouped.
					if (selectedComponents.contains(component) && componentType.isStretchable()
							&& findAllGroupedComponents(component).size() == 1) {
						try {
							if (scaledPoint.distance(controlPoint) < DrawingManager.CONTROL_POINT_SIZE) {
								Set<Integer> indices = new HashSet<Integer>();
								if (componentType.isStretchable()) {
									indices.add(pointIndex);
								} else {
									for (int j = 0; j < component.getControlPointCount(); j++) {
										indices.add(j);
									}
								}
								components.put(component, indices);
								break;
							}
						} catch (Exception e) {
							LOG.warn("Error reading control point for component of type: "
									+ component.getClass().getName());
						}
					}
				}
				// // If CTRL is pressed, we only care about the top most
				// component.
				// if (altDown && components.size() > 0) {
				// break;
				// }
			}
		}

		messageDispatcher.dispatchMessage(EventType.MOUSE_MOVED, scaledPoint);

		if (!components.equals(controlPointMap)) {
			controlPointMap = components;
			messageDispatcher.dispatchMessage(EventType.AVAILABLE_CTRL_POINTS_CHANGED,
					new HashMap<IDIYComponent<?>, Set<Integer>>(components));
		}
	}

	@Override
	public ComponentSelection getSelectedComponents() {
		return new ComponentSelection(selectedComponents);
	}

	@Override
	public void selectAll() {
		LOG.info("selectAll()");
		updateSelection(new ComponentSelection(currentProject.getComponents()));
		// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
		// selectedComponents);
		// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
		// calculateSelectionDimension());
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public VersionNumber getCurrentVersionNumber() {
		return CURRENT_VERSION;
	}

	@Override
	public void dragStarted(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
		LOG.debug(String.format("dragStarted(%s)", point));
		dragInProgress = true;
		preDragProject = cloner.deepClone(currentProject);
		Point scaledPoint = scalePoint(point);
		previousDragPoint = scaledPoint;
		List<IDIYComponent<?>> components = findComponentsAt(scaledPoint);
		if (!controlPointMap.isEmpty()) {
			// If we're dragging control points reset selection.
			updateSelection(new ComponentSelection(controlPointMap.keySet()));
			// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
			// selectedComponents);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
			// calculateSelectionDimension());
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		} else if (components.isEmpty()) {
			// If there are no components are under the cursor, reset selection.
			updateSelection(ComponentSelection.EMPTY_SELECTION);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
			// selectedComponents);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
			// calculateSelectionDimension());
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		} else {
			// Take the last component, i.e. the top order component.
			IDIYComponent<?> component = components.get(components.size() - 1);
			// If the component under the cursor is not already selected, make
			// it into the only selected component.
			if (!selectedComponents.contains(component)) {
				updateSelection(new ComponentSelection(findAllGroupedComponents(component)));
				// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
				// selectedComponents);
				// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
				// calculateSelectionDimension());
				messageDispatcher.dispatchMessage(EventType.REPAINT);
			}
			// If there aren't any control points, try to add all the selected
			// components with all their control points. That will allow the
			// user to drag the whole components.
			for (IDIYComponent<?> c : selectedComponents) {
				Set<Integer> pointIndices = new HashSet<Integer>();
				if (c.getControlPointCount() > 0) {
					for (int i = 0; i < c.getControlPointCount(); i++) {
						pointIndices.add(i);
					}
					controlPointMap.put(c, pointIndices);
				}
			}
			// Expand control points to include all stuck components.
			if (!ctrlDown) {
				includeStuckComponents(controlPointMap);
			}
		}
	}

	/**
	 * Finds any components that are stuck to one of the components already in
	 * the map.
	 * 
	 * @param controlPointMap
	 */
	private void includeStuckComponents(Map<IDIYComponent<?>, Set<Integer>> controlPointMap) {
		int oldSize = controlPointMap.size();
		LOG.debug("Expanding selected component map");
		for (IDIYComponent<?> component : currentProject.getComponents()) {
			// Do not process a component if it's already in the map.
			ComponentType componentType = componentTypeMap.get(component.getClass().getName());

			// Check if there's a control point in the current selection
			// that matches with one of its control points.
			for (int i = 0; i < component.getControlPointCount(); i++) {
				if (controlPointMap.containsKey(component)) {
					break;
				}
				if (!controlPointMap.containsKey(component) && component.isControlPointSticky(i)) {
					boolean componentMatches = false;
					for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap
							.entrySet()) {
						if (componentMatches) {
							break;
						}
						for (Integer j : entry.getValue()) {
							Point firstPoint = component.getControlPoint(i);
							if (entry.getKey().isControlPointSticky(j)) {
								Point secondPoint = entry.getKey().getControlPoint(j);
								// If they are close enough we can consider them
								// matched.
								if (firstPoint.distance(secondPoint) < DrawingManager.CONTROL_POINT_SIZE) {
									componentMatches = true;
									break;
								}
							}
						}
					}
					if (componentMatches) {
						LOG.debug("Including component: " + component);
						Set<Integer> indices = new HashSet<Integer>();
						// For stretchable components just add the
						// matching component.
						// Otherwise, add all control points.
						if (componentType.isStretchable()) {
							indices.add(i);
						} else {
							for (int k = 0; k < component.getControlPointCount(); k++) {
								indices.add(k);
							}
						}
						controlPointMap.put(component, indices);
					}
				}
			}
		}
		int newSize = controlPointMap.size();
		// As long as we're adding new components, do another iteration.
		if (newSize > oldSize) {
			LOG.debug("Component count changed, trying one more time.");
			includeStuckComponents(controlPointMap);
		} else {
			LOG.debug("Component count didn't change, done with expanding.");
		}
	}

	@Override
	public boolean dragOver(Point point) {
		if (point == null) {
			return false;
		}
		Point scaledPoint = scalePoint(point);
		boolean repaint = false;
		if (!controlPointMap.isEmpty()) {
			// We're dragging control point(s).
			int dx = (scaledPoint.x - previousDragPoint.x);
			int dy = (scaledPoint.y - previousDragPoint.y);
			if (snapToGrid) {
				dx = CalcUtils.roundToGrid(dx, currentProject.getGridSpacing());
				dy = CalcUtils.roundToGrid(dy, currentProject.getGridSpacing());
			}
			// Only repaint if there's an actual change.
			repaint = dx != 0 || dy != 0;

			if (repaint) {
				// Do a simulation of the move to see if any of them will
				// collide.
				for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
					IDIYComponent<?> c = entry.getKey();
					Point[] controlPoints = new Point[c.getControlPointCount()];
					for (int i = 0; i < controlPoints.length; i++) {
						controlPoints[i] = new Point(c.getControlPoint(i));
						if (entry.getValue().contains(i)) {
							controlPoints[i].translate(dx, dy);
						}
					}
					for (int i = 0; i < controlPoints.length - 1; i++) {
						for (int j = i + 1; j < controlPoints.length; j++) {
							if (controlPoints[i].equals(controlPoints[j])) {
								LOG
										.error("Control points collision detected, cannot make this move.");
								return true;
							}
						}
					}
				}

				previousDragPoint.translate(dx, dy);

				// Update all points.
				for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
					IDIYComponent<?> c = entry.getKey();
					drawingManager.invalidateComponent(c);
					for (Integer index : entry.getValue()) {
						Point p = new Point(c.getControlPoint(index));
						p.translate(dx, dy);
						c.setControlPoint(p, index);
					}
				}
			}
		} else if (selectedComponents.isEmpty()) {
			// If there's no selection, the only thing to do is update the
			// selection rectangle and refresh.
			Rectangle oldSelectionRect = selectionRect == null ? null
					: new Rectangle(selectionRect);
			this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
			repaint = !selectionRect.equals(oldSelectionRect);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_RECT_CHANGED,
			// selectionRect);
		}
		if (repaint) {
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		}
		messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
				calculateSelectionDimension());
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
			ComponentSelection newSelection = new ComponentSelection();
			for (IDIYComponent<?> component : currentProject.getComponents()) {
				Area area = drawingManager.getComponentArea(component);
				if ((area != null) && (selectionRect != null) && area.intersects(selectionRect)) {
					newSelection.addAll(findAllGroupedComponents(component));
				}
			}
			selectionRect = null;
			updateSelection(newSelection);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
			// selectedComponents);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
			// calculateSelectionDimension());
		}
		// There is selection, so we need to finalize the drag&drop
		// operation.

		if (!preDragProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, preDragProject, cloner
					.deepClone(currentProject), "Drag");
			projectFileManager.notifyFileChange();
			fireSelectionSizeChanged();
		}
		messageDispatcher.dispatchMessage(EventType.REPAINT);
		dragInProgress = false;
	}

	@Override
	public void pasteComponents(List<IDIYComponent<?>> components) {
		LOG.info(String.format("addComponents(%s)", components));
		Project oldProject = cloner.deepClone(currentProject);
		for (IDIYComponent<?> component : components) {
			for (int i = 0; i < component.getControlPointCount(); i++) {
				Point point = new Point(component.getControlPoint(i));
				point.translate(currentProject.getGridSpacing().convertToPixels(), currentProject
						.getGridSpacing().convertToPixels());
				component.setControlPoint(point, i);
			}
			addComponent(component, componentTypeMap.get(component.getClass().getName()));
		}
		messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
				.deepClone(currentProject), "Add");
		projectFileManager.notifyFileChange();
		updateSelection(new ComponentSelection(components));
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public void deleteSelectedComponents() {
		LOG.info("deleteSelectedComponents()");
		if (selectedComponents.isEmpty()) {
			LOG.debug("Nothing to delete");
			return;
		}
		Project oldProject = cloner.deepClone(currentProject);
		// Remove selected components from any groups.
		ungroupComponents(selectedComponents);
		// Remove from area map.
		for (IDIYComponent<?> component : selectedComponents) {
			drawingManager.invalidateComponent(component);
		}
		currentProject.getComponents().removeAll(selectedComponents);
		messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
				.deepClone(currentProject), "Delete");
		projectFileManager.notifyFileChange();
		updateSelection(ComponentSelection.EMPTY_SELECTION);
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public void setSelectionDefaultPropertyValue(String propertyName, Object value) {
		LOG.info(String.format("setSelectionDefaultPropertyValue(%s, %s)", propertyName, value));
		for (IDIYComponent<?> component : selectedComponents) {
			String className = component.getClass().getName();
			LOG.debug("Default property value set for " + className + ":" + propertyName);
			ConfigurationManager.getInstance().writeValue(
					DEFAULTS_KEY_PREFIX + className + ":" + propertyName, value);
		}
	}

	@Override
	public void setProjectDefaultPropertyValue(String propertyName, Object value) {
		LOG.info(String.format("setProjectDefaultPropertyValue(%s, %s)", propertyName, value));
		LOG.debug("Default property value set for " + Project.class.getName() + ":" + propertyName);
		ConfigurationManager.getInstance().writeValue(
				DEFAULTS_KEY_PREFIX + Project.class.getName() + ":" + propertyName, value);
	}

	@Override
	public void setMetric(boolean isMetric) {
		ConfigurationManager.getInstance().writeValue(Presenter.METRIC_KEY, isMetric);
		messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
				calculateSelectionDimension());
	}

	@Override
	public void groupSelectedComponents() {
		LOG.info("groupSelectedComponents()");
		Project oldProject = cloner.deepClone(currentProject);
		// First remove the selected components from other groups.
		ungroupComponents(selectedComponents);
		// Then group them together.
		currentProject.getGroups().add(new HashSet<IDIYComponent<?>>(selectedComponents));
		// Notify the listeners.
		messageDispatcher.dispatchMessage(EventType.REPAINT);
		if (!oldProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
					.deepClone(currentProject), "Group");
			projectFileManager.notifyFileChange();
		}
	}

	@Override
	public void ungroupSelectedComponents() {
		LOG.info("ungroupSelectedComponents()");
		Project oldProject = cloner.deepClone(currentProject);
		ungroupComponents(selectedComponents);
		// Notify the listeners.
		messageDispatcher.dispatchMessage(EventType.REPAINT);
		if (!oldProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
					.deepClone(currentProject), "Group");
			projectFileManager.notifyFileChange();
		}
	}

	/**
	 * Updates the selection with the specified {@link ComponentSelection}.
	 * Also, updates control point map with all components that are stuck to the
	 * newly selected components.
	 * 
	 * @param newSelection
	 */
	private void updateSelection(ComponentSelection newSelection) {
		this.selectedComponents = newSelection;
		Map<IDIYComponent<?>, Set<Integer>> controlPointMap = new HashMap<IDIYComponent<?>, Set<Integer>>();
		for (IDIYComponent<?> component : selectedComponents) {
			Set<Integer> indices = new HashSet<Integer>();
			for (int i = 0; i < component.getControlPointCount(); i++) {
				indices.add(i);
			}
			controlPointMap.put(component, indices);
		}
		includeStuckComponents(controlPointMap);
		messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED, selectedComponents,
				controlPointMap.keySet());
		fireSelectionSizeChanged();
	}

	private void fireSelectionSizeChanged() {
		messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
				calculateSelectionDimension());
	}

	/**
	 * Removes all the groups that contain at least one of the specified
	 * components.
	 * 
	 * @param components
	 */
	private void ungroupComponents(Collection<IDIYComponent<?>> components) {
		Iterator<Set<IDIYComponent<?>>> groupIterator = currentProject.getGroups().iterator();
		while (groupIterator.hasNext()) {
			Set<IDIYComponent<?>> group = groupIterator.next();
			group.removeAll(components);
			if (group.isEmpty()) {
				groupIterator.remove();
			}
		}
	}

	/**
	 * Finds all components that are grouped with the specified component. This
	 * should be called any time components are added or removed from the
	 * selection.
	 * 
	 * @param component
	 * @return set of all components that belong to the same group with the
	 *         specified component. At the minimum, set contains that single
	 *         component.
	 */
	private Set<IDIYComponent<?>> findAllGroupedComponents(IDIYComponent<?> component) {
		Set<IDIYComponent<?>> components = new HashSet<IDIYComponent<?>>();
		components.add(component);
		for (Set<IDIYComponent<?>> group : currentProject.getGroups()) {
			if (group.contains(component)) {
				components.addAll(group);
				break;
			}
		}
		return components;
	}

	/**
	 * @return selection size expressed in either inches or centimeters, based
	 *         on the user preference.
	 */
	private Point2D calculateSelectionDimension() {
		if (selectedComponents.isEmpty()) {
			return null;
		}
		boolean metric = ConfigurationManager.getInstance().readBoolean(METRIC_KEY, true);
		Area area = new Area();
		for (IDIYComponent<?> component : selectedComponents) {
			Area componentArea = drawingManager.getComponentArea(component);
			if (componentArea != null) {
				area.add(componentArea);
			}
		}
		double width = area.getBounds2D().getWidth();
		double height = area.getBounds2D().getHeight();
		width /= Constants.PIXELS_PER_INCH;
		height /= Constants.PIXELS_PER_INCH;
		if (metric) {
			width *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();
			height *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();
		}
		Point2D dimension = new Point2D.Double(width, height);
		return dimension;
	}

	/**
	 * Adds a component to the project taking z-order into account.
	 * 
	 * @param component
	 * @param componentType
	 */
	private void addComponent(IDIYComponent<?> component, ComponentType componentType) {
		int index = 0;
		while (index < currentProject.getComponents().size()
				&& componentType.getZOrder() >= componentTypeMap.get(
						currentProject.getComponents().get(index).getClass().getName()).getZOrder()) {
			index++;
		}
		if (index < currentProject.getComponents().size()) {
			currentProject.getComponents().add(index, component);
		} else {
			currentProject.getComponents().add(component);
		}
	}

	@Override
	public List<PropertyWrapper> getMutualSelectionProperties() {
		try {
			return ComponentProcessor.getInstance()
					.getMutualSelectionProperties(selectedComponents);
		} catch (Exception e) {
			LOG.error("Could not get mutual selection properties", e);
			return null;
		}
	}

	@Override
	public void applyPropertiesToSelection(List<PropertyWrapper> properties) {
		LOG.debug(String.format("applyPropertiesToSelection(%s)", properties));
		Project oldProject = cloner.deepClone(currentProject);
		try {
			for (IDIYComponent<?> component : selectedComponents) {
				drawingManager.invalidateComponent(component);
				for (PropertyWrapper property : properties) {
					property.writeTo(component);
				}
			}
		} catch (Exception e) {
			LOG.error("Could not apply selection properties", e);
			view.showMessage(
					"Could not apply changes to the selection. Check the log for details.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			// Notify the listeners.
			if (!oldProject.equals(currentProject)) {
				messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
						.deepClone(currentProject), "Edit Selection");
				projectFileManager.notifyFileChange();
			}
			messageDispatcher.dispatchMessage(EventType.REPAINT);
			messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
					calculateSelectionDimension());
		}
	}

	@Override
	public List<PropertyWrapper> getProjectProperties() {
		List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(
				Project.class);
		try {
			for (PropertyWrapper property : properties) {
				property.readFrom(currentProject);
			}
		} catch (Exception e) {
			LOG.error("Could not get project properties", e);
			return null;
		}
		Collections.sort(properties, ComparatorFactory.getInstance().getPropertyNameComparator());
		return properties;
	}

	@Override
	public void applyPropertiesToProject(List<PropertyWrapper> properties) {
		LOG.debug(String.format("applyPropertiesToProject(%s)", properties));
		Project oldProject = cloner.deepClone(currentProject);
		try {
			for (PropertyWrapper property : properties) {
				property.writeTo(currentProject);
			}
		} catch (Exception e) {
			LOG.error("Could not apply project properties", e);
			view.showMessage("Could not apply changes to the project. Check the log for details.",
					"Error", JOptionPane.ERROR_MESSAGE);
		} finally {
			// Notify the listeners.
			if (!oldProject.equals(currentProject)) {
				messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
						.deepClone(currentProject), "Edit Project");
				projectFileManager.notifyFileChange();
			}
			drawingManager.fireZoomChanged();
		}
	}

	@Override
	public void setNewComponentTypeSlot(ComponentType componentType) {
		LOG.info(String.format("setNewComponentSlot(%s)", componentType == null ? null
				: componentType.getName()));
		try {
			instantiationManager.setComponentTypeSlot(componentType, currentProject);
			updateSelection(ComponentSelection.EMPTY_SELECTION);
			messageDispatcher.dispatchMessage(EventType.REPAINT);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
			// selectedComponents);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
			// calculateSelectionDimension());
			messageDispatcher.dispatchMessage(EventType.SLOT_CHANGED, instantiationManager
					.getComponentTypeSlot(), instantiationManager.getFirstControlPoint());
		} catch (Exception e) {
			LOG.error("Could not set component type slot", e);
			view.showMessage("Could not set component type slot. Check log for details.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Scales point from display base to actual base.
	 * 
	 * @param point
	 * @return
	 */
	private Point scalePoint(Point point) {
		return point == null ? null : new Point((int) (point.x / drawingManager.getZoomLevel()),
				(int) (point.y / drawingManager.getZoomLevel()));
	}
}
