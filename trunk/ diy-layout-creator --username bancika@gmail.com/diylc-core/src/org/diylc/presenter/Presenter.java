package org.diylc.presenter;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.JarScanner;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.appframework.update.VersionNumber;
import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.IComponentFiler;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.components.connectivity.SolderPad;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

import com.rits.cloning.Cloner;

/**
 * The main presenter class, contains core app logic and drawing routines.
 * 
 * @author Branislav Stojkovic
 */
public class Presenter implements IPlugInPort {

	private static final Logger LOG = Logger.getLogger(Presenter.class);

	public static final VersionNumber CURRENT_VERSION = new VersionNumber(3, 6, 0);
	public static final String DEFAULTS_KEY_PREFIX = "default.";

	public static final List<IDIYComponent<?>> EMPTY_SELECTION = Collections.emptyList();

	public static final int ICON_SIZE = 32;

	private Project currentProject;
	private Map<String, List<ComponentType>> componentTypes;
	// Maps component class names to ComponentType objects.
	private List<IPlugIn> plugIns;

	private List<IDIYComponent<?>> selectedComponents;
	// Maps components that have at least one dragged point to set of indices
	// that designate which of their control points are being dragged.
	private Map<IDIYComponent<?>, Set<Integer>> controlPointMap;
	private Set<IDIYComponent<?>> lockedComponents;

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
	private int dragAction;

	public Presenter(IView view) {
		super();
		this.view = view;
		plugIns = new ArrayList<IPlugIn>();
		messageDispatcher = new MessageDispatcher<EventType>(true);
		selectedComponents = new ArrayList<IDIYComponent<?>>();
		lockedComponents = new HashSet<IDIYComponent<?>>();
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
			if (controlPointMap != null && !controlPointMap.isEmpty()) {
				return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
			}
			for (IDIYComponent<?> component : currentProject.getComponents()) {
				if (!isComponentLocked(component)) {
					Area area = drawingManager.getComponentArea(component);
					if (area != null && area.contains(scaledPoint)) {
						return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
					}
				}
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
		drawingManager.clearComponentAreaMap();
		updateSelection(EMPTY_SELECTION);
		messageDispatcher.dispatchMessage(EventType.PROJECT_LOADED, project, freshStart);
		messageDispatcher.dispatchMessage(EventType.REPAINT);
		messageDispatcher.dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject
				.getLockedLayers());
	}

	@Override
	public void createNewProject() {
		LOG.info("createNewFile()");
		try {
			Project project = new Project();
			instantiationManager.fillWithDefaultProperties(project);
			loadProject(project, true);
			projectFileManager.startNewFile();
		} catch (Exception e) {
			LOG.error("Could not create new file", e);
			view.showMessage("Could not create a new file. Check the log for details.", "Error",
					IView.ERROR_MESSAGE);
		}
	}

	@Override
	public void loadProjectFromFile(String fileName) {
		LOG.info(String.format("loadProjectFromFile(%s)", fileName));
		try {
			List<String> warnings = new ArrayList<String>();
			Project project = (Project) projectFileManager.deserializeProjectFromFile(fileName,
					warnings);
			loadProject(project, true);
			projectFileManager.fireFileStatusChanged();
			if (!warnings.isEmpty()) {
				StringBuilder builder = new StringBuilder(
						"<html>File was opened, but there were some issues with it:<br><br>");
				for (String warning : warnings) {
					builder.append(warning);
					builder.append("<br>");
				}
				builder.append("</html");
				view.showMessage(builder.toString(), "Warning", IView.WARNING_MESSAGE);
			}
		} catch (Exception ex) {
			LOG.error("Could not load file", ex);
			view.showMessage("Could not open file " + fileName + ". Check the log for details.",
					"Error", IView.ERROR_MESSAGE);
		}
	}

	@Override
	public boolean allowFileAction() {
		if (projectFileManager.isModified()) {
			int response = view.showConfirmDialog(
					"There are unsaved changes. Are you sure you want to abandon these changes?",
					"Warning", IView.YES_NO_OPTION, IView.WARNING_MESSAGE);
			return response == IView.YES_OPTION;
		}
		return true;
	}

	@Override
	public void saveProjectToFile(String fileName, boolean isBackup) {
		LOG.info(String.format("saveProjectToFile(%s)", fileName));
		try {
			currentProject.setFileVersion(CURRENT_VERSION);
			projectFileManager.serializeProjectToFile(currentProject, fileName, isBackup);
		} catch (Exception ex) {
			LOG.error("Could not save file", ex);
			view.showMessage("Could not save file " + fileName + ". Check the log for details.",
					"Error", IView.ERROR_MESSAGE);
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
			List<Class<?>> componentTypeClasses = JarScanner.getInstance().scanFolder("library/",
					IDIYComponent.class);
			for (Class<?> clazz : componentTypeClasses) {
				if (!Modifier.isAbstract(clazz.getModifiers())) {
					ComponentType componentType = ComponentProcessor.getInstance()
							.extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) clazz);
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
		// Don't draw the component in the slot if both control points
		// match.
		IDIYComponent<?> componentSlotToDraw;
		if (instantiationManager.getFirstControlPoint() != null
				&& instantiationManager.getPotentialControlPoint() != null
				&& instantiationManager.getFirstControlPoint().equals(
						instantiationManager.getPotentialControlPoint())) {
			componentSlotToDraw = null;
		} else {
			componentSlotToDraw = instantiationManager.getComponentSlot();
		}
		List<IDIYComponent<?>> failedComponents = drawingManager.drawProject(g2d, currentProject,
				drawOptions, filter, selectionRect, selectedComponents, getLockedComponents(),
				groupedComponents, Arrays.asList(instantiationManager.getFirstControlPoint(),
						instantiationManager.getPotentialControlPoint()), componentSlotToDraw,
				dragInProgress);
		List<String> failedComponentNames = new ArrayList<String>();
		for (IDIYComponent<?> component : failedComponents) {
			failedComponentNames.add(component.getName());
		}
		Collections.sort(failedComponentNames);
		if (!failedComponentNames.isEmpty()) {
			messageDispatcher.dispatchMessage(EventType.STATUS_MESSAGE_CHANGED,
					"<html><font color='red'>Failed to draw components: "
							+ Utils.toCommaString(failedComponentNames) + "</font></html>");
		} else {
			messageDispatcher.dispatchMessage(EventType.STATUS_MESSAGE_CHANGED, "");
		}
	}

	/**
	 * Finds all components whose areas include the specified {@link Point}.
	 * Point is <b>not</b> scaled by the zoom factor. Components that belong to
	 * locked layers are ignored.
	 * 
	 * @return
	 */
	public List<IDIYComponent<?>> findComponentsAtScaled(Point point) {
		List<IDIYComponent<?>> components = drawingManager.findComponentsAt(point, currentProject);
		Iterator<IDIYComponent<?>> iterator = components.iterator();
		while (iterator.hasNext()) {
			if (isComponentLocked(iterator.next())) {
				iterator.remove();
			}
		}
		return components;
	}

	@Override
	public List<IDIYComponent<?>> findComponentsAt(Point point) {
		Point scaledPoint = scalePoint(point);
		List<IDIYComponent<?>> components = findComponentsAtScaled(scaledPoint);
		return components;
	}

	@Override
	public void mouseClicked(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown,
			int clickCount) {
		LOG.debug(String
				.format("mouseClicked(%s, %s, %s, %s)", point, ctrlDown, shiftDown, altDown));
		Point scaledPoint = scalePoint(point);
		if (clickCount >= 2) {
			editSelection();
		} else {
			if (instantiationManager.getComponentTypeSlot() != null) {
				// Keep the reference to component type for later.
				ComponentType componentTypeSlot = instantiationManager.getComponentTypeSlot();
				Project oldProject = cloner.deepClone(currentProject);
				switch (componentTypeSlot.getCreationMethod()) {
				case SINGLE_CLICK:
					try {
						if (isSnapToGrid()) {
							CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
						}
						IDIYComponent<?> component = instantiationManager.instantiateComponent(
								componentTypeSlot, scaledPoint, currentProject);
						addComponent(component, componentTypeSlot, true);
						// Select the new component
						// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
						// selectedComponents);
						// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
						// calculateSelectionDimension());
						messageDispatcher.dispatchMessage(EventType.REPAINT);
						List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
						newSelection.add(component);
						updateSelection(newSelection);
					} catch (Exception e) {
						LOG.error("Error instatiating component of type: "
								+ componentTypeSlot.getInstanceClass().getName(), e);
					}

					if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.AUTO_EDIT_KEY,
							false)) {
						editSelection();
					}
					if (ConfigurationManager.getInstance().readBoolean(
							IPlugInPort.CONTINUOUS_CREATION_KEY, false)) {
						setNewComponentTypeSlot(componentTypeSlot);
					} else {
						setNewComponentTypeSlot(null);
					}
					break;
				case POINT_BY_POINT:
					// First click is just to set the controlPointSlot and
					// componentSlot.
					if (isSnapToGrid()) {
						CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
					}
					if (instantiationManager.getComponentSlot() == null) {
						try {
							instantiationManager
									.instatiatePointByPoint(scaledPoint, currentProject);
						} catch (Exception e) {
							view.showMessage("Could not create component. Check log for details.",
									"Error", IView.ERROR_MESSAGE);
							LOG.error("Could not create component", e);
						}
						messageDispatcher.dispatchMessage(EventType.SLOT_CHANGED,
								componentTypeSlot, instantiationManager.getFirstControlPoint());
						messageDispatcher.dispatchMessage(EventType.REPAINT);
					} else {
						// On the second click, add the component to the
						// project.
						IDIYComponent<?> componentSlot = instantiationManager.getComponentSlot();
						componentSlot.setControlPoint(scaledPoint, 1);
						addComponent(componentSlot, componentTypeSlot, true);
						// Select the new component if it's not locked.
						List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
						if (!isComponentLocked(componentSlot)) {
							newSelection.add(componentSlot);
						}
						updateSelection(newSelection);
						messageDispatcher.dispatchMessage(EventType.REPAINT);

						if (ConfigurationManager.getInstance().readBoolean(
								IPlugInPort.AUTO_EDIT_KEY, false)) {
							editSelection();
						}
						if (ConfigurationManager.getInstance().readBoolean(
								IPlugInPort.CONTINUOUS_CREATION_KEY, false)) {
							setNewComponentTypeSlot(componentTypeSlot);
						} else {
							setNewComponentTypeSlot(null);
						}
					}
					break;
				default:
					LOG.error("Unknown creation method: " + componentTypeSlot.getCreationMethod());
				}
				// Notify the listeners.
				if (!oldProject.equals(currentProject)) {
					messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject,
							cloner.deepClone(currentProject), "Add " + componentTypeSlot.getName());
					projectFileManager.notifyFileChange();
				}
			} else {
				List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(
						selectedComponents);
				List<IDIYComponent<?>> components = findComponentsAtScaled(scaledPoint);
				// If there's nothing under mouse cursor deselect all.
				if (components.isEmpty()) {
					if (!ctrlDown) {
						newSelection.clear();
					}
				} else {
					IDIYComponent<?> component = components.get(0);
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
	}

	@Override
	public void editSelection() {
		List<PropertyWrapper> properties = getMutualSelectionProperties();
		if (properties != null && !properties.isEmpty()) {
			Set<PropertyWrapper> defaultedProperties = new HashSet<PropertyWrapper>();
			boolean edited = view.editProperties(properties, defaultedProperties);
			if (edited) {
				try {
					applyPropertiesToSelection(properties);
				} catch (Exception e1) {
					view.showMessage(
							"Error occured while editing selection. Check the log for details.",
							"Error", JOptionPane.ERROR_MESSAGE);
					LOG.error("Error applying properties", e1);
				}
				// Save default values.
				for (PropertyWrapper property : defaultedProperties) {
					if (property.getValue() != null) {
						setSelectionDefaultPropertyValue(property.getName(), property.getValue());
					}
				}
			}
		}
	}

	@Override
	public void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
		Map<IDIYComponent<?>, Set<Integer>> components = new HashMap<IDIYComponent<?>, Set<Integer>>();
		Point scaledPoint = scalePoint(point);
		if (instantiationManager.getComponentTypeSlot() != null) {
			if (isSnapToGrid()) {
				CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
			}
			boolean refresh = false;
			switch (instantiationManager.getComponentTypeSlot().getCreationMethod()) {
			case POINT_BY_POINT:
				refresh = instantiationManager.updatePointByPoint(scaledPoint);
				break;
			case SINGLE_CLICK:
				refresh = instantiationManager.updateSingleClick(scaledPoint, isSnapToGrid(),
						currentProject.getGridSpacing());
				break;
			}
			if (refresh) {
				messageDispatcher.dispatchMessage(EventType.REPAINT);
			}
		} else {
			// Go backwards so we take the highest z-order components first.
			for (int i = currentProject.getComponents().size() - 1; i >= 0; i--) {
				IDIYComponent<?> component = currentProject.getComponents().get(i);
				ComponentType componentType = ComponentProcessor.getInstance()
						.extractComponentTypeFrom(
								(Class<? extends IDIYComponent<?>>) component.getClass());
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
	public List<IDIYComponent<?>> getSelectedComponents() {
		return selectedComponents;
	}

	@Override
	public void selectAll() {
		LOG.info("selectAll()");
		List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(currentProject
				.getComponents());
		newSelection.removeAll(getLockedComponents());
		updateSelection(newSelection);
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
	public void dragStarted(Point point, int dragAction) {
		LOG.debug(String.format("dragStarted(%s, %s)", point, dragAction));
		if (instantiationManager.getComponentTypeSlot() != null) {
			LOG.debug("Cannot start drag because a new component is being created.");
			mouseClicked(point, dragAction == DnDConstants.ACTION_COPY,
					dragAction == DnDConstants.ACTION_LINK, dragAction == DnDConstants.ACTION_MOVE,
					1);
			return;
		}
		this.dragInProgress = true;
		this.dragAction = dragAction;
		preDragProject = cloner.deepClone(currentProject);
		Point scaledPoint = scalePoint(point);
		this.previousDragPoint = scaledPoint;
		List<IDIYComponent<?>> components = findComponentsAtScaled(scaledPoint);
		if (!this.controlPointMap.isEmpty()) {
			// If we're dragging control points reset selection.
			updateSelection(new ArrayList<IDIYComponent<?>>(this.controlPointMap.keySet()));
			// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
			// selectedComponents);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
			// calculateSelectionDimension());
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		} else if (components.isEmpty()) {
			// If there are no components are under the cursor, reset selection.
			updateSelection(EMPTY_SELECTION);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
			// selectedComponents);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
			// calculateSelectionDimension());
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		} else {
			// Take the last component, i.e. the top order component.
			IDIYComponent<?> component = components.get(0);
			// If the component under the cursor is not already selected, make
			// it into the only selected component.
			if (!selectedComponents.contains(component)) {
				updateSelection(new ArrayList<IDIYComponent<?>>(findAllGroupedComponents(component)));
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
					this.controlPointMap.put(c, pointIndices);
				}
			}
			// Expand control points to include all stuck components.
			boolean sticky = ConfigurationManager.getInstance().readBoolean(
					IPlugInPort.STICKY_POINTS_KEY, true);
			if (this.dragAction == IPlugInPort.DND_TOGGLE_STICKY) {
				sticky = !sticky;
			}
			if (sticky) {
				includeStuckComponents(controlPointMap);
			}
		}
	}

	@Override
	public void dragActionChanged(int dragAction) {
		LOG.debug("dragActionChanged(" + dragAction + ")");
		this.dragAction = dragAction;
	}

	/**
	 * Finds any components that are stuck to one of the components already in
	 * the map.
	 * 
	 * @param controlPointMap
	 */
	private void includeStuckComponents(Map<IDIYComponent<?>, Set<Integer>> controlPointMap) {
		int oldSize = controlPointMap.size();
		LOG.trace("Expanding selected component map");
		for (IDIYComponent<?> component : currentProject.getComponents()) {
			ComponentType componentType = ComponentProcessor.getInstance()
					.extractComponentTypeFrom(
							(Class<? extends IDIYComponent<?>>) component.getClass());

			// Check if there's a control point in the current selection
			// that matches with one of its control points.
			for (int i = 0; i < component.getControlPointCount(); i++) {
				// Do not process a control point if it's already in the map and
				// if it's locked.
				if ((!controlPointMap.containsKey(component) || !controlPointMap.get(component)
						.contains(i))
						&& !isComponentLocked(component)) {
					if (component.isControlPointSticky(i)) {
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
									// If they are close enough we can consider
									// them matched.
									if (firstPoint.distance(secondPoint) < DrawingManager.CONTROL_POINT_SIZE) {
										componentMatches = true;
										break;
									}
								}
							}
						}
						if (componentMatches) {
							LOG.trace("Including component: " + component);
							Set<Integer> indices = new HashSet<Integer>();
							// For stretchable components just add the
							// matching component. Otherwise, add all control
							// points.
							if (componentType.isStretchable()) {
								indices.add(i);
							} else {
								for (int k = 0; k < component.getControlPointCount(); k++) {
									indices.add(k);
								}
							}
							if (controlPointMap.containsKey(component)) {
								controlPointMap.get(component).addAll(indices);
							} else {
								controlPointMap.put(component, indices);
							}
						}
					}
				}
			}
		}
		int newSize = controlPointMap.size();
		// As long as we're adding new components, do another iteration.
		if (newSize > oldSize) {
			LOG.trace("Component count changed, trying one more time.");
			includeStuckComponents(controlPointMap);
		} else {
			LOG.trace("Component count didn't change, done with expanding.");
		}
	}

	private boolean isSnapToGrid() {
		boolean snapToGrid = ConfigurationManager.getInstance().readBoolean(
				IPlugInPort.SNAP_TO_GRID_KEY, true);
		if (this.dragAction == IPlugInPort.DND_TOGGLE_SNAP)
			snapToGrid = !snapToGrid;
		return snapToGrid;
	}

	@Override
	public boolean dragOver(Point point) {
		if (point == null) {
			return false;
		}
		Point scaledPoint = scalePoint(point);
		if (!controlPointMap.isEmpty()) {
			// We're dragging control point(s).
			int dx = (scaledPoint.x - previousDragPoint.x);
			int dy = (scaledPoint.y - previousDragPoint.y);
			// After we make the transfer and snap to grid, calculate actual dx
			// and dy. We'll use them to translate the previous drag point.
			int actualDx = 0;
			int actualDy = 0;

			// For each component, do a simulation of the move to see if any of
			// them will overlap or go out of bounds.
			int width = (int) currentProject.getWidth().convertToPixels();
			int height = (int) currentProject.getHeight().convertToPixels();
			boolean isFirst = true;
			for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
				IDIYComponent<?> component = entry.getKey();
				Point[] controlPoints = new Point[component.getControlPointCount()];
				for (int index = 0; index < component.getControlPointCount(); index++) {
					controlPoints[index] = new Point(component.getControlPoint(index));
					// When the first point is moved, calculate how much it
					// actually moved after snapping.
					if (entry.getValue().contains(index)) {
						if (isFirst) {
							isFirst = false;
							Point testPoint = new Point(controlPoints[index]);
							testPoint.translate(dx, dy);
							if (isSnapToGrid()) {
								CalcUtils.snapPointToGrid(testPoint, currentProject
										.getGridSpacing());
							}
							actualDx = testPoint.x - component.getControlPoint(index).x;
							actualDy = testPoint.y - component.getControlPoint(index).y;
							if (actualDx == 0 && actualDy == 0) {
								// Nothing to move.
								return true;
							}
						}
						controlPoints[index].translate(actualDx, actualDy);
						if (controlPoints[index].x < 0 || controlPoints[index].y < 0
								|| controlPoints[index].x > width
								|| controlPoints[index].y > height) {
							// At least one control point went out of bounds.
							return true;
						}
					}
					// For control points that may overlap, just write null,
					// we'll ignore them later.
					if (component.canControlPointOverlap(index)) {
						controlPoints[index] = null;
					}
				}

				for (int i = 0; i < controlPoints.length - 1; i++) {
					for (int j = i + 1; j < controlPoints.length; j++) {
						if (controlPoints[i] != null && controlPoints[j] != null
								&& controlPoints[i].equals(controlPoints[j])) {
							// Control points collision detected, cannot make
							// this move.
							return true;
						}
					}
				}
			}

			// Update all points.
			for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
				IDIYComponent<?> c = entry.getKey();
				drawingManager.invalidateComponent(c);
				for (Integer index : entry.getValue()) {
					Point p = new Point(c.getControlPoint(index));
					p.translate(actualDx, actualDy);
					c.setControlPoint(p, index);
				}
			}
			previousDragPoint.translate(actualDx, actualDy);
		} else if (selectedComponents.isEmpty()
				&& instantiationManager.getComponentTypeSlot() == null) {
			// If there's no selection, the only thing to do is update the
			// selection rectangle and refresh.
			Rectangle oldSelectionRect = selectionRect == null ? null
					: new Rectangle(selectionRect);
			this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
			if (selectionRect.equals(oldSelectionRect)) {
				return true;
			}
			// messageDispatcher.dispatchMessage(EventType.SELECTION_RECT_CHANGED,
			// selectionRect);
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
			List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
			for (IDIYComponent<?> component : currentProject.getComponents()) {
				if (!isComponentLocked(component)) {
					Area area = drawingManager.getComponentArea(component);
					if ((area != null) && (selectionRect != null) && area.intersects(selectionRect)) {
						newSelection.addAll(findAllGroupedComponents(component));
					}
				}
			}
			selectionRect = null;
			updateSelection(newSelection);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
			// selectedComponents);
			// messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
			// calculateSelectionDimension());
		} else {
			updateSelection(selectedComponents);
		}
		// There is selection, so we need to finalize the drag&drop
		// operation.

		if (!preDragProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, preDragProject, cloner
					.deepClone(currentProject), "Drag");
			projectFileManager.notifyFileChange();
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
				point.translate((int) currentProject.getGridSpacing().convertToPixels(),
						(int) currentProject.getGridSpacing().convertToPixels());
				component.setControlPoint(point, i);
			}
			component.setName(instantiationManager.createUniqueName(ComponentProcessor
					.getInstance().extractComponentTypeFrom(
							(Class<? extends IDIYComponent<?>>) component.getClass()),
					currentProject));
			addComponent(component, ComponentProcessor.getInstance().extractComponentTypeFrom(
					(Class<? extends IDIYComponent<?>>) component.getClass()), false);
		}
		messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
				.deepClone(currentProject), "Add");
		projectFileManager.notifyFileChange();
		updateSelection(new ArrayList<IDIYComponent<?>>(components));
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
		updateSelection(EMPTY_SELECTION);
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
					.deepClone(currentProject), "Ungroup");
			projectFileManager.notifyFileChange();
		}
	}

	@Override
	public void setLayerLocked(int layerZOrder, boolean locked) {
		LOG.info(String.format("setLayerLocked(%s, %s)", layerZOrder, locked));
		if (locked) {
			currentProject.getLockedLayers().add(layerZOrder);
		} else {
			currentProject.getLockedLayers().remove(layerZOrder);
		}
		updateSelection(EMPTY_SELECTION);
		messageDispatcher.dispatchMessage(EventType.REPAINT);
		messageDispatcher.dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject
				.getLockedLayers());
	}

	@Override
	public void sendSelectionToBack() {
		LOG.info("sendSelectionToBack()");
		Project oldProject = cloner.deepClone(currentProject);
		for (IDIYComponent<?> component : selectedComponents) {
			ComponentType componentType = ComponentProcessor.getInstance()
					.extractComponentTypeFrom(
							(Class<? extends IDIYComponent<?>>) component.getClass());
			int index = currentProject.getComponents().indexOf(component);
			if (index < 0) {
				LOG.warn("Component not found in the project: " + component.getName());
			} else if (index > 0) {
				IDIYComponent<?> componentBefore = currentProject.getComponents().get(index - 1);
				ComponentType componentBeforeType = ComponentProcessor.getInstance()
						.extractComponentTypeFrom(
								(Class<? extends IDIYComponent<?>>) componentBefore.getClass());
				Collections.swap(currentProject.getComponents(), index, index - 1);
			}
		}
		if (!oldProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
					.deepClone(currentProject), "Send to Back");
			projectFileManager.notifyFileChange();
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		}
	}

	@Override
	public void bringSelectionToFront() {
		LOG.info("bringSelectionToFront()");
		Project oldProject = cloner.deepClone(currentProject);
		for (IDIYComponent<?> component : selectedComponents) {
			ComponentType componentType = ComponentProcessor.getInstance()
					.extractComponentTypeFrom(
							(Class<? extends IDIYComponent<?>>) component.getClass());
			int index = currentProject.getComponents().indexOf(component);
			if (index < 0) {
				LOG.warn("Component not found in the project: " + component.getName());
			} else if (index < currentProject.getComponents().size() - 1) {
				IDIYComponent<?> componentAfter = currentProject.getComponents().get(index + 1);
				ComponentType componentAfterType = ComponentProcessor.getInstance()
						.extractComponentTypeFrom(
								(Class<? extends IDIYComponent<?>>) componentAfter.getClass());
				Collections.swap(currentProject.getComponents(), index, index + 1);
			}
		}
		if (!oldProject.equals(currentProject)) {
			messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
					.deepClone(currentProject), "Bring to Front");
			projectFileManager.notifyFileChange();
			messageDispatcher.dispatchMessage(EventType.REPAINT);
		}
	}

	@Override
	public void refresh() {
		LOG.info("refresh()");
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	@Override
	public Theme getSelectedTheme() {
		return drawingManager.getTheme();
	}

	@Override
	public void setSelectedTheme(Theme theme) {
		drawingManager.setTheme(theme);
	}

	@Override
	public void renumberSelectedComponents(final boolean xAxisFirst) {
		LOG.info("renumberSelectedComponents(" + xAxisFirst + ")");
		if (getSelectedComponents().isEmpty()) {
			return;
		}
		Project oldProject = cloner.deepClone(currentProject);
		List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>(getSelectedComponents());
		// Sort components by their location.
		Collections.sort(components, new Comparator<IDIYComponent<?>>() {

			@Override
			public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
				int sumX1 = 0;
				int sumY1 = 0;
				int sumX2 = 0;
				int sumY2 = 0;
				for (int i = 0; i < o1.getControlPointCount(); i++) {
					sumX1 += o1.getControlPoint(i).getX();
					sumY1 += o1.getControlPoint(i).getY();
				}
				for (int i = 0; i < o2.getControlPointCount(); i++) {
					sumX2 += o2.getControlPoint(i).getX();
					sumY2 += o2.getControlPoint(i).getY();
				}
				sumX1 /= o1.getControlPointCount();
				sumY1 /= o1.getControlPointCount();
				sumX2 /= o2.getControlPointCount();
				sumY2 /= o2.getControlPointCount();

				if (xAxisFirst) {
					if (sumY1 < sumY2) {
						return -1;
					} else if (sumY1 > sumY2) {
						return 1;
					} else {
						if (sumX1 < sumX2) {
							return -1;
						} else if (sumX1 > sumX2) {
							return 1;
						}
					}
				} else {
					if (sumX1 < sumX2) {
						return -1;
					} else if (sumX1 > sumX2) {
						return 1;
					} else {
						if (sumY1 < sumY2) {
							return -1;
						} else if (sumY1 > sumY2) {
							return 1;
						}
					}
				}
				return 0;
			}
		});
		// Clear names.
		for (IDIYComponent<?> component : components) {
			component.setName("");
		}
		// Assign new ones.
		for (IDIYComponent<?> component : components) {
			component.setName(instantiationManager.createUniqueName(ComponentProcessor
					.getInstance().extractComponentTypeFrom(
							(Class<? extends IDIYComponent<?>>) component.getClass()),
					currentProject));
		}

		messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
				.deepClone(currentProject), "Renumber selection");
		projectFileManager.notifyFileChange();
		messageDispatcher.dispatchMessage(EventType.REPAINT);
	}

	public void updateSelection(List<IDIYComponent<?>> newSelection) {
		this.selectedComponents = newSelection;
		Map<IDIYComponent<?>, Set<Integer>> controlPointMap = new HashMap<IDIYComponent<?>, Set<Integer>>();
		for (IDIYComponent<?> component : selectedComponents) {
			Set<Integer> indices = new HashSet<Integer>();
			for (int i = 0; i < component.getControlPointCount(); i++) {
				indices.add(i);
			}
			controlPointMap.put(component, indices);
		}
		if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.STICKY_POINTS_KEY, true)) {
			includeStuckComponents(controlPointMap);
		}
		messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED, selectedComponents,
				controlPointMap.keySet());
	}

	@Override
	public void expandSelection(ExpansionMode expansionMode) {
		List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(
				this.selectedComponents);
		// Find control points of all selected components and all types
		Set<String> selectedNamePrefixes = new HashSet<String>();
		if (expansionMode == ExpansionMode.SAME_TYPE) {
			for (IDIYComponent<?> component : getSelectedComponents()) {
				selectedNamePrefixes.add(ComponentProcessor.getInstance().extractComponentTypeFrom(
						(Class<? extends IDIYComponent<?>>) component.getClass()).getNamePrefix());
			}
		}
		// Now try to find components that intersect with at least one component
		// in the pool.
		for (IDIYComponent<?> component : getCurrentProject().getComponents()) {
			// Skip already selected components or ones that cannot be stuck to
			// other components.
			Area area = drawingManager.getComponentArea(component);
			if (newSelection.contains(component) || !component.isControlPointSticky(0)
					|| area == null)
				continue;
			boolean matches = false;
			for (IDIYComponent<?> selectedComponent : this.selectedComponents) {
				Area selectedArea = drawingManager.getComponentArea(selectedComponent);
				if (selectedArea == null)
					continue;
				Area intersection = new Area(area);
				intersection.intersect(selectedArea);
				if (!intersection.isEmpty()) {
					matches = true;
					break;
				}
			}

			if (matches) {
				switch (expansionMode) {
				case ALL:
				case IMMEDIATE:
					newSelection.add(component);
					break;
				case SAME_TYPE:
					if (selectedNamePrefixes.contains(ComponentProcessor.getInstance()
							.extractComponentTypeFrom(
									(Class<? extends IDIYComponent<?>>) component.getClass())
							.getNamePrefix())) {
						newSelection.add(component);
					}
					break;
				}
			}
		}

		int oldSize = this.getSelectedComponents().size();
		updateSelection(newSelection);
		// Go deeper if possible.
		if (newSelection.size() > oldSize && expansionMode != ExpansionMode.IMMEDIATE) {
			expandSelection(expansionMode);
		}
		messageDispatcher.dispatchMessage(EventType.REPAINT);
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

	@Override
	public Point2D calculateSelectionDimension() {
		if (selectedComponents.isEmpty()) {
			return null;
		}
		boolean metric = ConfigurationManager.getInstance().readBoolean(METRIC_KEY, true);
		Area area = new Area();
		for (IDIYComponent<?> component : selectedComponents) {
			Area componentArea = drawingManager.getComponentArea(component);
			if (componentArea != null) {
				area.add(componentArea);
			} else {
				LOG.warn("No area found for: " + component.getName());
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
	private void addComponent(IDIYComponent<?> component, ComponentType componentType,
			boolean canCreatePads) {
		int index = 0;
		while (index < currentProject.getComponents().size()
				&& componentType.getZOrder() >= ComponentProcessor.getInstance()
						.extractComponentTypeFrom(
								(Class<? extends IDIYComponent<?>>) currentProject.getComponents()
										.get(index).getClass()).getZOrder()) {
			index++;
		}
		if (index < currentProject.getComponents().size()) {
			currentProject.getComponents().add(index, component);
		} else {
			currentProject.getComponents().add(component);
		}
		if (canCreatePads
				&& ConfigurationManager.getInstance().readBoolean(IPlugInPort.AUTO_PADS_KEY, false)
				&& !(component instanceof SolderPad)) {
			ComponentType padType = ComponentProcessor.getInstance().extractComponentTypeFrom(
					SolderPad.class);
			for (int i = 0; i < component.getControlPointCount(); i++) {
				if (component.isControlPointSticky(i)) {
					try {
						IDIYComponent<?> pad = instantiationManager.instantiateComponent(padType,
								component.getControlPoint(i), currentProject);
						pad.setControlPoint(component.getControlPoint(i), 0);
						addComponent(pad, padType, false);
					} catch (Exception e) {
						LOG.warn("Could not auto-create solder pad", e);
					}
					// SolderPad pad = new SolderPad();
					// pad.setControlPoint(component.getControlPoint(i), 0);
					// addComponent(pad,
					// ComponentProcessor.getInstance().extractComponentTypeFrom(
					// SolderPad.class), false);
				}
			}
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

	private void applyPropertiesToSelection(List<PropertyWrapper> properties) {
		LOG.debug(String.format("applyPropertiesToSelection(%s)", properties));
		Project oldProject = cloner.deepClone(currentProject);
		try {
			for (IDIYComponent<?> component : selectedComponents) {
				drawingManager.invalidateComponent(component);
				for (PropertyWrapper property : properties) {
					if (property.isChanged()) {
						property.writeTo(component);
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Could not apply selection properties", e);
			view.showMessage(
					"Could not apply changes to the selection. Check the log for details.",
					"Error", IView.ERROR_MESSAGE);
		} finally {
			// Notify the listeners.
			if (!oldProject.equals(currentProject)) {
				messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, cloner
						.deepClone(currentProject), "Edit Selection");
				projectFileManager.notifyFileChange();
			}
			messageDispatcher.dispatchMessage(EventType.REPAINT);
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
					"Error", IView.ERROR_MESSAGE);
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
			if (componentType != null) {
				updateSelection(EMPTY_SELECTION);
			}
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
					IView.ERROR_MESSAGE);
		}
	}

	private Set<IDIYComponent<?>> getLockedComponents() {
		lockedComponents.clear();
		for (IDIYComponent<?> component : currentProject.getComponents()) {
			if (isComponentLocked(component)) {
				lockedComponents.add(component);
			}
		}
		return lockedComponents;
	}

	private boolean isComponentLocked(IDIYComponent<?> component) {
		ComponentType componentType = ComponentProcessor.getInstance().extractComponentTypeFrom(
				(Class<? extends IDIYComponent<?>>) component.getClass());
		return currentProject.getLockedLayers().contains(
				(int) Math.round(componentType.getZOrder()));
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
