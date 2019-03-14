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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.DnDConstants;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.JarScanner;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.appframework.update.Version;
import org.diylc.appframework.update.VersionNumber;
import org.diylc.common.BuildingBlockPackage;
import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.IComponentFiler;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IKeyProcessor;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.common.VariantPackage;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IContinuity;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.Theme;
import org.diylc.core.annotations.IAutoCreator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Node;
import org.diylc.netlist.Position;
import org.diylc.netlist.SwitchSetup;
import org.diylc.utils.Constants;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * The main presenter class, contains core app logic and drawing routines.
 * 
 * @author Branislav Stojkovic
 */
public class Presenter implements IPlugInPort {

  private static final Logger LOG = Logger.getLogger(Presenter.class);

  public static VersionNumber CURRENT_VERSION = new VersionNumber(3, 0, 0);
  public static List<Version> RECENT_VERSIONS = null;
  // Read the latest version from the local update.xml file
  static {
    try {
      BufferedInputStream in = new BufferedInputStream(new FileInputStream("update.xml"));
      XStream xStream = new XStream(new DomDriver());
      @SuppressWarnings("unchecked")
      List<Version> allVersions = (List<Version>) xStream.fromXML(in);
      CURRENT_VERSION = allVersions.get(allVersions.size() - 1).getVersionNumber();
      LOG.info("Current DIYLC version: " + CURRENT_VERSION);
      RECENT_VERSIONS = allVersions.subList(allVersions.size() - 10, allVersions.size());
      Collections.sort(RECENT_VERSIONS, new Comparator<Version>() {

        @Override
        public int compare(Version o1, Version o2) {         
          return -o1.getVersionNumber().compareTo(o2.getVersionNumber());
        }        
      });
      in.close();      
    } catch (IOException e) {
      LOG.error("Could not find version number, using default", e);
    }
  }
  public static final String DEFAULTS_KEY_PREFIX = "default.";
  
  private static Map<String, List<Template>> defaultVariantMap = null;
  static {
    try {
      BufferedInputStream in = new BufferedInputStream(new FileInputStream("variants.xml"));
      XStream xStream = new XStream(new DomDriver());
      @SuppressWarnings("unchecked")
      Map<String, List<Template>> map = (Map<String, List<Template>>) xStream.fromXML(in);
      defaultVariantMap = new TreeMap<String, List<Template>>(String.CASE_INSENSITIVE_ORDER);
      defaultVariantMap.putAll(map);
      in.close();
      LOG.info(String.format("Loaded default variants for %d components", defaultVariantMap == null ? 0 : defaultVariantMap.size()));
    } catch (IOException e) {
      LOG.error("Could not load default variants", e);
    }
  }

  public static final List<IDIYComponent<?>> EMPTY_SELECTION = Collections.emptyList();

  public static final int ICON_SIZE = 32;

  private static final int MAX_RECENT_FILES = 20;

  private Project currentProject;
  private Map<String, List<ComponentType>> componentTypes;
  /**
   * {@link List} of {@link IAutoCreator} objects that are capable of creating more components
   * automatically when a component is created, e.g. Solder Pads.
   */
  private List<IAutoCreator> autoCreators;
  // Maps component class names to ComponentType objects.
  private List<IPlugIn> plugIns;

  private Set<IDIYComponent<?>> selectedComponents;
  // Maps components that have at least one dragged point to set of indices
  // that designate which of their control points are being dragged.
  private Map<IDIYComponent<?>, Set<Integer>> controlPointMap;
  private Set<IDIYComponent<?>> lockedComponents;

  // Utilities
  // private Cloner cloner;
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
  private Point previousScaledPoint;

  public Presenter(IView view) {
    super();
    this.view = view;
    plugIns = new ArrayList<IPlugIn>();
    messageDispatcher = new MessageDispatcher<EventType>(true);
    selectedComponents = new HashSet<IDIYComponent<?>>();
    lockedComponents = new HashSet<IDIYComponent<?>>();
    currentProject = new Project();
    // cloner = new Cloner();
    drawingManager = new DrawingManager(messageDispatcher);
    projectFileManager = new ProjectFileManager(messageDispatcher);
    instantiationManager = new InstantiationManager();

    // lockedLayers = EnumSet.noneOf(ComponentLayer.class);
    // visibleLayers = EnumSet.allOf(ComponentLayer.class);
    upgradeVariants();
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
    return new Double[] {0.25d, 0.3333d, 0.5d, 0.6667d, 0.75d, 1d, 1.25d, 1.5d, 2d, 2.5d, 3d};
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
    if (ConfigurationManager.getInstance().readBoolean(HIGHLIGHT_CONTINUITY_AREA, false))
      return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    if (instantiationManager.getComponentTypeSlot() == null) {
      // Scale point to remove zoom factor.
      Point2D scaledPoint = scalePoint(point);
      if (controlPointMap != null && !controlPointMap.isEmpty()) {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      }
      for (IDIYComponent<?> component : currentProject.getComponents()) {
        if (!isComponentLocked(component) && isComponentVisible(component)
            && !ConfigurationManager.getInstance().readBoolean(HIGHLIGHT_CONTINUITY_AREA, false)) {
          ComponentArea area = drawingManager.getComponentArea(component);
          if (area != null && area.getOutlineArea() != null && area.getOutlineArea().contains(scaledPoint)) {
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
          }
        }
      }
    }
    return Cursor.getDefaultCursor();
  }

  @Override
  public Dimension getCanvasDimensions(boolean useZoom, boolean includeExtraSpace) {
    return drawingManager.getCanvasDimensions(currentProject, useZoom ? drawingManager.getZoomLevel() : 1/ Constants.PIXEL_SIZE, includeExtraSpace);
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
    drawingManager.clearContinuityArea();
    updateSelection(EMPTY_SELECTION);
    messageDispatcher.dispatchMessage(EventType.PROJECT_LOADED, project, freshStart);
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    messageDispatcher.dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject.getLockedLayers());
    messageDispatcher.dispatchMessage(EventType.LAYER_VISIBILITY_CHANGED, currentProject.getHiddenLayers());
  }

  @Override
  public void createNewProject() {
    LOG.info("createNewFile()");
    try {
      Project project = new Project();
      instantiationManager.fillWithDefaultProperties(project, null);
      loadProject(project, true);
      projectFileManager.startNewFile();
    } catch (Exception e) {
      LOG.error("Could not create new file", e);
      view.showMessage("Could not create a new file. Check the log for details.", "Error", IView.ERROR_MESSAGE);
    }
  }

  @Override
  public void loadProjectFromFile(String fileName) {
    LOG.info(String.format("loadProjectFromFile(%s)", fileName));
    List<String> warnings = null;
    try {
      warnings = new ArrayList<String>();
      Project project = (Project) projectFileManager.deserializeProjectFromFile(fileName, warnings);      
      loadProject(project, true);
      projectFileManager.fireFileStatusChanged();
      if (!warnings.isEmpty()) {
        StringBuilder builder = new StringBuilder("<html>File was opened, but there were some issues with it:<br><br>");
        for (String warning : warnings) {
          builder.append(warning);
          builder.append("<br>");
        }
        builder.append("</html");
        view.showMessage(builder.toString(), "Warning", IView.WARNING_MESSAGE);
      }
      addToRecentFiles(fileName);
    } catch (Exception ex) {
      LOG.error("Could not load file", ex);
      String errorMessage = "Could not open file " + fileName + ". Check the log for details.";
      if (warnings != null && !warnings.isEmpty()) {
        errorMessage += " Possible reasons are:\n\n";
        for (String warn : warnings) {
          errorMessage += warn;
          errorMessage += "\n";
        }
      }
      view.showMessage(errorMessage, "Error", IView.ERROR_MESSAGE);
    }
  }

  @SuppressWarnings("unchecked")
  private void addToRecentFiles(String fileName) {
    List<String> recentFiles = (List<String>) ConfigurationManager.getInstance().readObject(RECENT_FILES_KEY, null);
    if (recentFiles == null)
      recentFiles = new ArrayList<String>();
    recentFiles.remove(fileName);
    recentFiles.add(0, fileName);
    while (recentFiles.size() > MAX_RECENT_FILES)
      recentFiles.remove(recentFiles.size() - 1);
    ConfigurationManager.getInstance().writeValue(RECENT_FILES_KEY, recentFiles);
  }

  @Override
  public boolean allowFileAction() {
    if (projectFileManager.isModified()) {
      int response =
          view.showConfirmDialog("There are unsaved changes. Would you like to save them?", "Warning",
              IView.YES_NO_CANCEL_OPTION, IView.WARNING_MESSAGE);
      if (response == IView.YES_OPTION) {
        if (this.getCurrentFileName() == null) {
          File file = view.promptFileSave();
          if (file == null) {
            return false;
          }
          saveProjectToFile(file.getAbsolutePath(), false);
        } else {
          saveProjectToFile(this.getCurrentFileName(), false);
        }
      }
      return response != IView.CANCEL_OPTION;
    }
    return true;
  }

  @Override
  public void saveProjectToFile(String fileName, boolean isBackup) {
    LOG.info(String.format("saveProjectToFile(%s)", fileName));
    try {
      currentProject.setFileVersion(CURRENT_VERSION);
      projectFileManager.serializeProjectToFile(currentProject, fileName, isBackup);
      if (!isBackup)
        addToRecentFiles(fileName);
    } catch (Exception ex) {
      LOG.error("Could not save file", ex);
      if (!isBackup) {
        view.showMessage("Could not save file " + fileName + ". Check the log for details.", "Error",
            IView.ERROR_MESSAGE);
      }
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
      Set<Class<?>> componentTypeClasses = null;
      try {
        componentTypeClasses = Utils.getClasses("org.diylc.components");
        List<Class<?>> additionalComponentTypeClasses = JarScanner.getInstance().scanFolder("library", IDIYComponent.class);
        if (additionalComponentTypeClasses != null)
          componentTypeClasses.addAll(additionalComponentTypeClasses);

        for (Class<?> clazz : componentTypeClasses) {
          if (!Modifier.isAbstract(clazz.getModifiers()) && IDIYComponent.class.isAssignableFrom(clazz)) {
            ComponentType componentType =
                ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) clazz);
            if (componentType == null)
            	continue;
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

        for (Map.Entry<String, List<ComponentType>> e : componentTypes.entrySet()) {
          LOG.debug(e.getKey() + ": " + e.getValue());
        }
      } catch (Exception e) {
        LOG.error("Error loading component types", e);
      }
    }
    return componentTypes;
  }

  public List<IAutoCreator> getAutoCreators() {
    if (autoCreators == null) {
      autoCreators = new ArrayList<IAutoCreator>();
      Set<Class<?>> classes = null;
      try {
        classes = Utils.getClasses("org.diylc.components.autocreate");
        for (Class<?> clazz : classes) {
          if (IAutoCreator.class.isAssignableFrom(clazz)) {
            autoCreators.add((IAutoCreator) clazz.newInstance());
            LOG.debug("Loaded auto-creator: " + clazz.getName());
          }
        }
      } catch (Exception e) {
        LOG.error("Error loading auto-creator types", e);
      }
    }
    return autoCreators;
  }

  @SuppressWarnings({"unchecked"})
  private boolean isComponentVisible(IDIYComponent<?> component) {
    ComponentType componentType =
        ComponentProcessor.getInstance().extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass());
    return !currentProject.getHiddenLayers().contains((int) Math.round(componentType.getZOrder()));
  }

  @Override
  public void draw(Graphics2D g2d, Set<DrawOption> drawOptions, final IComponentFiler filter, Double externalZoom) {
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

    // Concatenate the specified filter with our own filter that removes hidden layers
    IComponentFiler newFiler = new IComponentFiler() {

      @Override
      public boolean testComponent(IDIYComponent<?> component) {
        return (filter == null || filter.testComponent(component)) && isComponentVisible(component);
      }
    };

    // Don't draw the component in the slot if both control points
    // match.
    List<IDIYComponent<?>> componentSlotToDraw;
    if (instantiationManager.getFirstControlPoint() != null && instantiationManager.getPotentialControlPoint() != null
        && instantiationManager.getFirstControlPoint().equals(instantiationManager.getPotentialControlPoint())) {
      componentSlotToDraw = null;
    } else {
      componentSlotToDraw = instantiationManager.getComponentSlot();
    }
    List<IDIYComponent<?>> failedComponents =
        drawingManager
            .drawProject(
                g2d,
                currentProject,
                drawOptions,
                newFiler,
                selectionRect,
                selectedComponents,
                getLockedComponents(),
                groupedComponents,
                Arrays.asList(instantiationManager.getFirstControlPoint(),
                    instantiationManager.getPotentialControlPoint()), 
                componentSlotToDraw,
                dragInProgress,
                externalZoom);
    List<String> failedComponentNames = new ArrayList<String>();
    for (IDIYComponent<?> component : failedComponents) {
      failedComponentNames.add(component.getName());
    }
    Collections.sort(failedComponentNames);
    if (!failedComponentNames.isEmpty()) {
      messageDispatcher.dispatchMessage(EventType.STATUS_MESSAGE_CHANGED,
          "<html><font color='red'>Failed to draw components: " + Utils.toCommaString(failedComponentNames)
              + "</font></html>");
    } else {
      messageDispatcher.dispatchMessage(EventType.STATUS_MESSAGE_CHANGED, "");
    }
  }

  /**
   * Finds all components whose areas include the specified {@link Point}. Point is <b>not</b>
   * scaled by the zoom factor. Components that belong to locked layers are ignored.
   * 
   * @return
   */
  public List<IDIYComponent<?>> findComponentsAtScaled(Point point) {
    List<IDIYComponent<?>> components = drawingManager.findComponentsAt(point, currentProject);
    Iterator<IDIYComponent<?>> iterator = components.iterator();
    while (iterator.hasNext()) {
      IDIYComponent<?> component = iterator.next();
      if (isComponentLocked(component) || !isComponentVisible(component)) {
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
  public void mouseClicked(Point point, int button, boolean ctrlDown, boolean shiftDown, boolean altDown, int clickCount) {
    LOG.debug(String.format("mouseClicked(%s, %s, %s, %s, %s)", point, button, ctrlDown, shiftDown, altDown));
    Point scaledPoint = scalePoint(point);
    if (clickCount >= 2) {
      editSelection();
    } else {
      if (instantiationManager.getComponentTypeSlot() != null) {
        // Try to rotate the component on right click while creating.
        if (button != IPlugInPort.BUTTON1) {
          instantiationManager.tryToRotateComponentSlot();
          messageDispatcher.dispatchMessage(EventType.REPAINT);
          return;
        }
        // Keep the reference to component type for later.
        ComponentType componentTypeSlot = instantiationManager.getComponentTypeSlot();
        Template template = instantiationManager.getTemplate();
        Project oldProject = currentProject.clone();
        switch (componentTypeSlot.getCreationMethod()) {
          case SINGLE_CLICK:
            try {
              if (isSnapToGrid()) {
                CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
              }
              List<IDIYComponent<?>> componentSlot = instantiationManager.getComponentSlot();
              List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
              for (IDIYComponent<?> component : componentSlot) {
                currentProject.getComponents().add(component);
                newSelection.add(component);
              }
              // group components if there's more than one, e.g. building blocks, but not clipboard contents
              if (componentSlot.size() > 1 && !componentTypeSlot.getName().toLowerCase().contains("clipboard")) {
                this.currentProject.getGroups().add(new HashSet<IDIYComponent<?>>(componentSlot));
              }
              // Select the new component
              // messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
              // selectedComponents);
              // messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
              // calculateSelectionDimension());
              messageDispatcher.dispatchMessage(EventType.REPAINT);
              updateSelection(newSelection);
            } catch (Exception e) {
              LOG.error("Error instatiating component of type: " + componentTypeSlot.getInstanceClass().getName(), e);
            }

            if (componentTypeSlot.isAutoEdit()
                && ConfigurationManager.getInstance().readBoolean(IPlugInPort.AUTO_EDIT_KEY, false)) {
              editSelection();
            }
            if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.CONTINUOUS_CREATION_KEY, false)) {
              setNewComponentTypeSlot(componentTypeSlot, template, false);
            } else {
              setNewComponentTypeSlot(null, null, false);
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
                instantiationManager.instatiatePointByPoint(scaledPoint, currentProject);
              } catch (Exception e) {
                view.showMessage("Could not create component. Check log for details.", "Error", IView.ERROR_MESSAGE);
                LOG.error("Could not create component", e);
              }
              messageDispatcher.dispatchMessage(EventType.SLOT_CHANGED, componentTypeSlot,
                  instantiationManager.getFirstControlPoint());
              messageDispatcher.dispatchMessage(EventType.REPAINT);
            } else {
              // On the second click, add the component to the
              // project.
              addPendingComponentsToProject(scaledPoint, componentTypeSlot, template);
            }
            break;
          default:
            LOG.error("Unknown creation method: " + componentTypeSlot.getCreationMethod());
        }
        // Notify the listeners.
        if (!oldProject.equals(currentProject)) {
          messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Add "
              + componentTypeSlot.getName());
          drawingManager.clearContinuityArea();
          projectFileManager.notifyFileChange();
        }
      } else if (ConfigurationManager.getInstance().readBoolean(HIGHLIGHT_CONTINUITY_AREA, false)) {
        drawingManager.findContinuityAreaAtPoint(currentProject, scaledPoint);
        messageDispatcher.dispatchMessage(EventType.REPAINT);
      } else {
        List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(selectedComponents);
        List<IDIYComponent<?>> components = findComponentsAtScaled(scaledPoint);
        // If there's nothing under mouse cursor deselect all.
        if (components.isEmpty()) {
          if (!ctrlDown) {
            newSelection.clear();
          }
        } else {
          IDIYComponent<?> topComponent = components.get(0);
          // If ctrl is pressed just toggle the component under mouse
          // cursor.
          if (ctrlDown) {
            if (newSelection.contains(topComponent)) {
                newSelection.removeAll(findAllGroupedComponents(topComponent));
            } else {
              newSelection.addAll(findAllGroupedComponents(topComponent));
            }
          } else {
            // Otherwise just select that one component.
            if (button == BUTTON1 || (button == BUTTON3 && newSelection.size() == 1)
                || !newSelection.contains(topComponent)) {
              newSelection.clear();
            }

            newSelection.addAll(findAllGroupedComponents(topComponent));
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

  private void addPendingComponentsToProject(Point scaledPoint, ComponentType componentTypeSlot, Template template) {
    List<IDIYComponent<?>> componentSlot = instantiationManager.getComponentSlot();
    Point firstPoint = componentSlot.get(0).getControlPoint(0);
    // don't allow to create component with the same points
    if (scaledPoint == null || scaledPoint.equals(firstPoint))
      return;
//    componentSlot.get(0).setControlPoint(scaledPoint, 1);
    List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
    for (IDIYComponent<?> component : componentSlot) {
      addComponent(component, true);
      // Select the new component if it's not locked and invisible.
      if (!isComponentLocked(component) && isComponentVisible(component)) {
        newSelection.add(component);
      }
    }

    updateSelection(newSelection);
    messageDispatcher.dispatchMessage(EventType.REPAINT);

    if (componentTypeSlot.isAutoEdit()
        && ConfigurationManager.getInstance().readBoolean(IPlugInPort.AUTO_EDIT_KEY, false)) {
      editSelection();
    }
    if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.CONTINUOUS_CREATION_KEY, false)) {
      setNewComponentTypeSlot(componentTypeSlot, template, false);
    } else {
      setNewComponentTypeSlot(null, null, false);
    }
  }

  @Override
  public boolean keyPressed(int key, boolean ctrlDown, boolean shiftDown, boolean altDown) {
    if (key != VK_DOWN && key != VK_LEFT && key != VK_UP && key != VK_RIGHT && key != IKeyProcessor.VK_H
        && key != IKeyProcessor.VK_V) {
      return false;
    }
    LOG.debug(String.format("keyPressed(%s, %s, %s, %s)", key, ctrlDown, shiftDown, altDown));
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap = new HashMap<IDIYComponent<?>, Set<Integer>>();
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
    if (controlPointMap.isEmpty()) {
      return false;
    }

    boolean snapToGrid = ConfigurationManager.getInstance().readBoolean(IPlugInPort.SNAP_TO_GRID_KEY, true);
    if (shiftDown) {
      snapToGrid = !snapToGrid;
    }

    if (altDown) {
      Project oldProject = null;
      if (key == IKeyProcessor.VK_RIGHT) {
        oldProject = currentProject.clone();
        rotateComponents(this.selectedComponents, 1, snapToGrid);
      } else if (key == IKeyProcessor.VK_LEFT) {
        oldProject = currentProject.clone();
        rotateComponents(this.selectedComponents, -1, snapToGrid);
      } else if (key == IKeyProcessor.VK_H) {
        oldProject = currentProject.clone();
        mirrorComponents(this.selectedComponents, IComponentTransformer.HORIZONTAL, snapToGrid);
      } else if (key == IKeyProcessor.VK_V) {
        oldProject = currentProject.clone();
        mirrorComponents(this.selectedComponents, IComponentTransformer.VERTICAL, snapToGrid);
      } else
        return false;
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(),
          "Rotate Selection");
      messageDispatcher.dispatchMessage(EventType.REPAINT);
      drawingManager.clearContinuityArea();
      return true;
    }

    // Expand control points to include all stuck components.
    boolean sticky = ConfigurationManager.getInstance().readBoolean(IPlugInPort.STICKY_POINTS_KEY, true);
    if (ctrlDown) {
      sticky = !sticky;
    }

    if (sticky) {
      includeStuckComponents(controlPointMap);
    }

    int d;
    if (snapToGrid) {
      d = (int) currentProject.getGridSpacing().convertToPixels();
    } else {
      d = 1;
    }
    int dx = 0;
    int dy = 0;
    switch (key) {
      case IKeyProcessor.VK_DOWN:
        dy = d;
        break;
      case IKeyProcessor.VK_LEFT:
        dx = -d;
        break;
      case IKeyProcessor.VK_UP:
        dy = -d;
        break;
      case IKeyProcessor.VK_RIGHT:
        dx = d;
        break;
      default:
        return false;
    }

    Project oldProject = currentProject.clone();
    moveComponents(controlPointMap, dx, dy, snapToGrid);
    messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Move Selection");
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    return true;
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
          view.showMessage("Error occured while editing selection. Check the log for details.", "Error",
              JOptionPane.ERROR_MESSAGE);
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

  @SuppressWarnings("unchecked")
  @Override
  public void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
    if (point == null)
      return;

    if (shiftDown) {
      dragAction = IPlugInPort.DND_TOGGLE_SNAP;
    } else {
      dragAction = 0;
    }

    Map<IDIYComponent<?>, Set<Integer>> components = new HashMap<IDIYComponent<?>, Set<Integer>>();
    this.previousScaledPoint = scalePoint(point);
    if (instantiationManager.getComponentTypeSlot() != null) {
      if (isSnapToGrid()) {
        CalcUtils.snapPointToGrid(previousScaledPoint, currentProject.getGridSpacing());
      }
      boolean refresh = false;
      switch (instantiationManager.getComponentTypeSlot().getCreationMethod()) {
        case POINT_BY_POINT:
          refresh = instantiationManager.updatePointByPoint(previousScaledPoint);
          break;
        case SINGLE_CLICK:
          refresh =
              instantiationManager.updateSingleClick(previousScaledPoint, isSnapToGrid(),
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
        ComponentType componentType =
            ComponentProcessor.getInstance().extractComponentTypeFrom(
                (Class<? extends IDIYComponent<?>>) component.getClass());
        for (int pointIndex = 0; pointIndex < component.getControlPointCount(); pointIndex++) {
          Point controlPoint = component.getControlPoint(pointIndex);
          // Only consider selected components that are not grouped.
          if (selectedComponents.contains(component) && componentType.isStretchable()
              && findAllGroupedComponents(component).size() == 1) {
            try {
              if (previousScaledPoint.distance(controlPoint) < DrawingManager.CONTROL_POINT_SIZE) {
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
              LOG.warn("Error reading control point for component of type: " + component.getClass().getName());
            }
          }
        }
      }
    }
    
    Point2D inPoint = new Point2D.Double(1.0d * previousScaledPoint.x / Constants.PIXELS_PER_INCH, 1.0d * previousScaledPoint.y / Constants.PIXELS_PER_INCH);
    Point2D mmPoint = new Point2D.Double(inPoint.getX() * SizeUnit.in.getFactor() / SizeUnit.cm.getFactor() * 10d, inPoint.getY() * SizeUnit.in.getFactor() / SizeUnit.cm.getFactor() * 10d);

    messageDispatcher.dispatchMessage(EventType.MOUSE_MOVED, previousScaledPoint, inPoint, mmPoint);

    if (!components.equals(controlPointMap)) {
      controlPointMap = components;
      messageDispatcher.dispatchMessage(EventType.AVAILABLE_CTRL_POINTS_CHANGED,
          new HashMap<IDIYComponent<?>, Set<Integer>>(components));
    }
  }

  @Override
  public Collection<IDIYComponent<?>> getSelectedComponents() {
    List<IDIYComponent<?>> selection = new ArrayList<IDIYComponent<?>>(selectedComponents);
    Collections.sort(selection, ComparatorFactory.getInstance().getComponentProjectZOrderComparator(currentProject));
    return selection;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void selectAll(int layer) {
    LOG.info("selectAll()");
    List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(currentProject.getComponents());
    newSelection.removeAll(getLockedComponents());
    if (layer > 0) {
      Iterator<IDIYComponent<?>> i = newSelection.iterator();
      while (i.hasNext()) {
        IDIYComponent<?> c = i.next();
        ComponentType type =
            ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());
        if ((int) type.getZOrder() != layer)
          i.remove();
      }
    }
    updateSelection(newSelection);
    // messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
    // selectedComponents);
    // messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
    // calculateSelectionDimension());
    messageDispatcher.dispatchMessage(EventType.REPAINT);
  }

  @Override
  public Rectangle2D getSelectionBounds(boolean applyZoom) {
    if (selectedComponents == null || selectedComponents.isEmpty())
      return null;

    int minX = Integer.MAX_VALUE;
	int maxX = Integer.MIN_VALUE;
	int minY = Integer.MAX_VALUE;
	int maxY = Integer.MIN_VALUE;
	for (IDIYComponent<?> c : selectedComponents) {
		ComponentArea compArea = drawingManager.getComponentArea(c);
		if (compArea != null && compArea.getOutlineArea() != null) {
			Rectangle rect = compArea.getOutlineArea().getBounds();
			if (rect.x < minX)
				minX = rect.x;
			if (rect.x + rect.width > maxX)
				maxX = rect.x + rect.width;
			if (rect.y < minY)
				minY = rect.y;
			if (rect.y + rect.height > maxY)
				maxY = rect.y + rect.height;
		} else if (currentProject.getComponents().contains(c))
			LOG.debug("Area is null for " + c.getName() + " of type "
					+ c.getClass().getName());
	}
	
	if (ConfigurationManager.getInstance().readBoolean(EXTRA_SPACE_KEY, true)) {
	  double extraSpace = drawingManager.getExtraSpace(currentProject);
	  minX += extraSpace;
	  maxX += extraSpace;
	  minY += extraSpace;
	  maxY += extraSpace;
	}
	
	if (drawingManager.getZoomLevel() != 1 && applyZoom) {
		minX *= drawingManager.getZoomLevel();
		maxX *= drawingManager.getZoomLevel();
		minY *= drawingManager.getZoomLevel();
		maxY *= drawingManager.getZoomLevel();
	}
	return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
  }

  @Override
  public void nudgeSelection(Size xOffset, Size yOffset, boolean includeStuckComponents) {
    if (selectedComponents == null || selectedComponents.isEmpty())
      return;

    LOG.debug(String.format("nudgeSelection(%s, %s, %s)", xOffset, yOffset, includeStuckComponents));
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap = new HashMap<IDIYComponent<?>, Set<Integer>>();
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
    if (controlPointMap.isEmpty()) {
      return;
    }

    if (includeStuckComponents) {
      includeStuckComponents(controlPointMap);
    }

    int dx = (int) xOffset.convertToPixels();
    int dy = (int) yOffset.convertToPixels();

    Project oldProject = currentProject.clone();
    moveComponents(controlPointMap, dx, dy, false);
    messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Move Selection");
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    drawingManager.clearContinuityArea();
  }

  @Override
  public VersionNumber getCurrentVersionNumber() {
    return CURRENT_VERSION;
  }
  
  @Override
  public List<Version> getRecentUpdates() {
    return RECENT_VERSIONS;
  }

  @Override
  public void dragStarted(Point point, int dragAction, boolean forceSelectionRect) {
    LOG.debug(String.format("dragStarted(%s, %s)", point, dragAction));
    if (instantiationManager.getComponentTypeSlot() != null) {
      LOG.debug("Cannot start drag because a new component is being created.");
      mouseClicked(point, IPlugInPort.BUTTON1, dragAction == DnDConstants.ACTION_COPY,
          dragAction == DnDConstants.ACTION_LINK, dragAction == DnDConstants.ACTION_MOVE, 1);
      return;
    }
    if (ConfigurationManager.getInstance().readBoolean(HIGHLIGHT_CONTINUITY_AREA, false)) {
      LOG.debug("Cannot start drag in hightlight continuity mode.");
      return;
    }
    this.dragInProgress = true;
    this.dragAction = dragAction;
    this.preDragProject = currentProject.clone();
    Point scaledPoint = scalePoint(point);
    this.previousDragPoint = scaledPoint;
    List<IDIYComponent<?>> components = forceSelectionRect ? null : findComponentsAtScaled(scaledPoint);
    if (!this.controlPointMap.isEmpty()) {
      // If we're dragging control points reset selection.
      updateSelection(new ArrayList<IDIYComponent<?>>(this.controlPointMap.keySet()));
      // messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
      // selectedComponents);
      // messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
      // calculateSelectionDimension());
      messageDispatcher.dispatchMessage(EventType.REPAINT);
    } else if (components == null || components.isEmpty()) {
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
      boolean sticky = ConfigurationManager.getInstance().readBoolean(IPlugInPort.STICKY_POINTS_KEY, true);
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
   * Finds any components that are stuck to one of the components already in the map.
   * 
   * @param controlPointMap
   */
  @SuppressWarnings("unchecked")
  private void includeStuckComponents(Map<IDIYComponent<?>, Set<Integer>> controlPointMap) {
    int oldSize = controlPointMap.size();
    LOG.trace("Expanding selected component map");
    for (IDIYComponent<?> component : currentProject.getComponents()) {
      ComponentType componentType =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());

      // Check if there's a control point in the current selection
      // that matches with one of its control points.
      for (int i = 0; i < component.getControlPointCount(); i++) {
        // Do not process a control point if it's already in the map and
        // if it's locked.
        if ((!controlPointMap.containsKey(component) || !controlPointMap.get(component).contains(i))
            && !isComponentLocked(component) && isComponentVisible(component)) {
          if (component.isControlPointSticky(i)) {
            boolean componentMatches = false;
            for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
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
    boolean snapToGrid = ConfigurationManager.getInstance().readBoolean(IPlugInPort.SNAP_TO_GRID_KEY, true);
    if (this.dragAction == IPlugInPort.DND_TOGGLE_SNAP)
      snapToGrid = !snapToGrid;
    return snapToGrid;
  }

  @Override
  public boolean dragOver(Point point) {
    if (point == null || ConfigurationManager.getInstance().readBoolean(HIGHLIGHT_CONTINUITY_AREA, false)) {
      return false;
    }
    Point scaledPoint = scalePoint(point);
    if (!controlPointMap.isEmpty()) {
      // We're dragging control point(s).
      int dx = (scaledPoint.x - previousDragPoint.x);
      int dy = (scaledPoint.y - previousDragPoint.y);

      Point actualD = moveComponents(this.controlPointMap, dx, dy, isSnapToGrid());
      if (actualD == null)
        return true;

      previousDragPoint.translate(actualD.x, actualD.y);
    } else if (selectedComponents.isEmpty() && instantiationManager.getComponentTypeSlot() == null && previousDragPoint != null) {
      // If there's no selection, the only thing to do is update the
      // selection rectangle and refresh.
      Rectangle oldSelectionRect = selectionRect == null ? null : new Rectangle(selectionRect);
      this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
      if (selectionRect.equals(oldSelectionRect)) {
        return true;
      }
      // messageDispatcher.dispatchMessage(EventType.SELECTION_RECT_CHANGED,
      // selectionRect);
    } else if (instantiationManager.getComponentSlot() != null) {
      this.previousScaledPoint = scalePoint(point);
      instantiationManager.updateSingleClick(previousScaledPoint, isSnapToGrid(),
          currentProject.getGridSpacing());
    }
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    return true;
  }

  private Point moveComponents(Map<IDIYComponent<?>, Set<Integer>> controlPointMap, int dx, int dy, boolean snapToGrid) {
    // After we make the transfer and snap to grid, calculate actual dx
    // and dy. We'll use them to translate the previous drag point.
    int actualDx = 0;
    int actualDy = 0;
    // For each component, do a simulation of the move to see if any of
    // them will overlap or go out of bounds.
    
    boolean useExtraSpace = ConfigurationManager.getInstance().readBoolean(EXTRA_SPACE_KEY, true);
    Dimension d = drawingManager.getCanvasDimensions(currentProject, 1d, useExtraSpace);    
    double extraSpace = useExtraSpace ? drawingManager.getExtraSpace(currentProject) : 0;

    if (controlPointMap.size() == 1) {
      Map.Entry<IDIYComponent<?>, Set<Integer>> entry = controlPointMap.entrySet().iterator().next();

      Point firstPoint = entry.getKey().getControlPoint(entry.getValue().toArray(new Integer[] {})[0]);
      Point testPoint = new Point(firstPoint);
      testPoint.translate(dx, dy);
      if (snapToGrid) {
        CalcUtils.snapPointToGrid(testPoint, currentProject.getGridSpacing());
      }

      actualDx = testPoint.x - firstPoint.x;
      actualDy = testPoint.y - firstPoint.y;
    } else if (snapToGrid) {
      actualDx = CalcUtils.roundToGrid(dx, currentProject.getGridSpacing());
      actualDy = CalcUtils.roundToGrid(dy, currentProject.getGridSpacing());
    } else {
      actualDx = dx;
      actualDy = dy;
    }

    if (actualDx == 0 && actualDy == 0) {
      // Nothing to move.
      return null;
    }

    // Validate if moving can be done.
    for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
      IDIYComponent<?> component = entry.getKey();
      Point[] controlPoints = new Point[component.getControlPointCount()];
      for (int index = 0; index < component.getControlPointCount(); index++) {
        controlPoints[index] = new Point(component.getControlPoint(index));
        // When the first point is moved, calculate how much it
        // actually moved after snapping.
        if (entry.getValue().contains(index)) {
          controlPoints[index].translate(actualDx, actualDy);
          controlPoints[index].translate((int)extraSpace, (int)extraSpace);
          if (controlPoints[index].x < 0 || controlPoints[index].y < 0 || controlPoints[index].x > d.width
              || controlPoints[index].y > d.height) {
            // At least one control point went out of bounds.
            return null;
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
          if (controlPoints[i] != null && controlPoints[j] != null && controlPoints[i].equals(controlPoints[j])) {
            // Control points collision detected, cannot make
            // this move.
            return null;
          }
        }
      }
    }

    // Update all points to new location.
    for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
      IDIYComponent<?> c = entry.getKey();
      drawingManager.invalidateComponent(c);
      for (Integer index : entry.getValue()) {
        Point p = new Point(c.getControlPoint(index));
        p.translate(actualDx, actualDy);
        c.setControlPoint(p, index);
      }
    }
    return new Point(actualDx, actualDy);
  }

  @Override
  public void rotateSelection(int direction) {
    if (!selectedComponents.isEmpty()) {
      LOG.trace("Rotating selected components");
      Project oldProject = currentProject.clone();
      rotateComponents(this.selectedComponents, direction, isSnapToGrid());
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(),
          "Rotate Selection");
      drawingManager.clearContinuityArea();
      messageDispatcher.dispatchMessage(EventType.REPAINT);
    }
  }

  /**
   * 
   * @param direction 1 for clockwise, -1 for counter-clockwise
   */
  @SuppressWarnings("unchecked")
  private void rotateComponents(Collection<IDIYComponent<?>> components, int direction, boolean snapToGrid) {
    Point center = getCenterOf(components, snapToGrid);

    boolean canRotate = true;
    for (IDIYComponent<?> component : selectedComponents) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      if (type.getTransformer() == null || !type.getTransformer().canRotate(component)) {
        canRotate = false;
        break;
      }
    }

    if (!canRotate)
      if (view.showConfirmDialog("Selection contains components that cannot be rotated. Do you want to exclude them?",
          "Mirror Selection", IView.YES_NO_OPTION, IView.QUESTION_MESSAGE) != IView.YES_OPTION)
        return;

    for (IDIYComponent<?> component : selectedComponents) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      if (type.getTransformer() != null && type.getTransformer().canRotate(component)) {
        drawingManager.invalidateComponent(component);
        type.getTransformer().rotate(component, center, direction);
      }
    }

    // AffineTransform rotate = AffineTransform.getRotateInstance(Math.PI / 2 * direction, center.x,
    // center.y);
    //
    // // Update all points to new location.
    // for (IDIYComponent<?> component : components) {
    // drawingManager.invalidateComponent(component);
    // ComponentType type =
    // ComponentProcessor.getInstance().extractComponentTypeFrom(
    // (Class<? extends IDIYComponent<?>>) component.getClass());
    // if (type.isRotatable()) {
    // for (int index = 0; index < component.getControlPointCount(); index++) {
    // Point p = new Point(component.getControlPoint(index));
    // rotate.transform(p, p);
    // component.setControlPoint(p, index);
    // }
    // // If component has orientation, change it too
    // List<PropertyWrapper> newProperties =
    // ComponentProcessor.getInstance().extractProperties(component.getClass());
    // for (PropertyWrapper property : newProperties) {
    // if (property.getType() == Orientation.class) {
    // try {
    // property.readFrom(component);
    // Orientation orientation = (Orientation) property.getValue();
    // Orientation[] values = Orientation.values();
    // int newIndex = orientation.ordinal() + direction;
    // if (newIndex < 0)
    // newIndex = values.length - 1;
    // else if (newIndex >= values.length)
    // newIndex = 0;
    // property.setValue(values[newIndex]);
    // property.writeTo(component);
    // } catch (Exception e) {
    // LOG.error("Could not change component orientation for " + component.getName(), e);
    // }
    // } else if (property.getType() == OrientationHV.class) {
    // try {
    // property.readFrom(component);
    // OrientationHV orientation = (OrientationHV) property.getValue();
    // property.setValue(OrientationHV.values()[1 - orientation.ordinal()]);
    // property.writeTo(component);
    // } catch (Exception e) {
    // LOG.error("Could not change component orientation for " + component.getName(), e);
    // }
    // }
    // }
    // } else {
    // // Non-rotatable
    // Point componentCenter = getCenterOf(Arrays.asList(new IDIYComponent<?>[] {component}),
    // false);
    // Point rotatedComponentCenter = new Point();
    // rotate.transform(componentCenter, rotatedComponentCenter);
    // for (int index = 0; index < component.getControlPointCount(); index++) {
    // Point p = new Point(component.getControlPoint(index));
    // p.translate(rotatedComponentCenter.x - componentCenter.x, rotatedComponentCenter.y -
    // componentCenter.y);
    // component.setControlPoint(p, index);
    // }
    // }
    // }
  }

  @Override
  public void mirrorSelection(int direction) {
    if (!selectedComponents.isEmpty()) {
      LOG.trace("Mirroring selected components");
      Project oldProject = currentProject.clone();

      mirrorComponents(selectedComponents, direction, isSnapToGrid());

      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(),
          "Mirror Selection");
      messageDispatcher.dispatchMessage(EventType.REPAINT);
      drawingManager.clearContinuityArea();
    }
  }

  @SuppressWarnings("unchecked")
  private void mirrorComponents(Collection<IDIYComponent<?>> components, int direction, boolean snapToGrid) {
    Point center = getCenterOf(components, snapToGrid);

    boolean canMirror = true;
    boolean changesCircuit = false;
    for (IDIYComponent<?> component : components) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      if (type.getTransformer() == null || !type.getTransformer().canMirror(component)) {
        canMirror = false;
        break;
      }
      if (type.getTransformer() != null && type.getTransformer().mirroringChangesCircuit())
        changesCircuit = true;
    }

    if (!canMirror)
      if (view.showConfirmDialog("Selection contains components that cannot be mirrored. Do you want to exclude them?",
          "Mirror Selection", IView.YES_NO_OPTION, IView.QUESTION_MESSAGE) != IView.YES_OPTION)
        return;

    if (changesCircuit)
      if (view.showConfirmDialog("Mirroring operation will change the circuit. Do you want to continue?",
          "Mirror Selection", IView.YES_NO_OPTION, IView.QUESTION_MESSAGE) != IView.YES_OPTION)
        return;

    for (IDIYComponent<?> component : components) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      drawingManager.invalidateComponent(component);
      if (type.getTransformer() != null && type.getTransformer().canMirror(component)) {
        type.getTransformer().mirror(component, center, direction);
      }
    }
  }

  private Point getCenterOf(Collection<IDIYComponent<?>> components, boolean snapToGrid) {
    // Determine center of rotation
    int minX = Integer.MAX_VALUE;
    int minY = Integer.MAX_VALUE;
    int maxX = Integer.MIN_VALUE;
    int maxY = Integer.MIN_VALUE;
    for (IDIYComponent<?> component : components) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point p = component.getControlPoint(i);
        if (minX > p.x) {
          minX = p.x;
        }
        if (maxX < p.x) {
          maxX = p.x;
        }
        if (minY > p.y) {
          minY = p.y;
        }
        if (maxY < p.y) {
          maxY = p.y;
        }
      }
    }
    int centerX = (maxX + minX) / 2;
    int centerY = (maxY + minY) / 2;

    if (snapToGrid) {
      CalcUtils.roundToGrid(centerX, this.currentProject.getGridSpacing());
      CalcUtils.roundToGrid(centerY, this.currentProject.getGridSpacing());
    }

    return new Point(centerX, centerY);
  }

  @Override
  public void dragEnded(Point point) {
    LOG.debug(String.format("dragEnded(%s)", point));
    
    Point scaledPoint = scalePoint(point);
    
    if (!dragInProgress && instantiationManager.getComponentSlot() == null) {      
      return;
    }    
      
    if (selectedComponents.isEmpty()) {
      // If there's no selection finalize selectionRect and see which
      // components intersect with it.
      if (scaledPoint != null) {
        this.selectionRect = Utils.createRectangle(scaledPoint, previousDragPoint);
      }
      List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
      if (!ConfigurationManager.getInstance().readBoolean(HIGHLIGHT_CONTINUITY_AREA, false))
        for (IDIYComponent<?> component : currentProject.getComponents()) {
          if (!isComponentLocked(component) && isComponentVisible(component)) {
            ComponentArea area = drawingManager.getComponentArea(component);
            if ((area != null && area.getOutlineArea() != null) && (selectionRect != null)
                && area.getOutlineArea().intersects(selectionRect)) {
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
    } else if (instantiationManager.getComponentSlot() != null) {
      preDragProject = currentProject.clone();
      addPendingComponentsToProject(scaledPoint, instantiationManager.getComponentTypeSlot(), null);
    } else {
      updateSelection(selectedComponents);
    }
    // There is selection, so we need to finalize the drag&drop
    // operation.

    if (!preDragProject.equals(currentProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, preDragProject, currentProject.clone(), "Drag");
      drawingManager.clearContinuityArea();
      projectFileManager.notifyFileChange();
    }
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    dragInProgress = false;
  }

  @Override
  public void pasteComponents(Collection<IDIYComponent<?>> components, boolean autoGroup) {
    LOG.info(String.format("pasteComponents(%s, %s)", components, autoGroup));
    instantiationManager.pasteComponents(components, this.previousScaledPoint, isSnapToGrid(),
        currentProject.getGridSpacing(), autoGroup);
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    messageDispatcher.dispatchMessage(EventType.SLOT_CHANGED, instantiationManager.getComponentTypeSlot(),
        instantiationManager.getFirstControlPoint());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void duplicateSelection() {
    LOG.info("duplicateSelection()");
    if (selectedComponents.isEmpty()) {
      LOG.debug("Nothing to duplicate");
      return;
    }
    Project oldProject = currentProject.clone();
    Set<IDIYComponent<?>> newSelection = new HashSet<IDIYComponent<?>>();

    int grid = (int) currentProject.getGridSpacing().convertToPixels();
    for (IDIYComponent<?> component : this.selectedComponents) {
      try {
        IDIYComponent<?> cloned = component.clone();
        ComponentType componentType =
            ComponentProcessor.getInstance().extractComponentTypeFrom(
                (Class<? extends IDIYComponent<?>>) cloned.getClass());
        cloned.setName(instantiationManager.createUniqueName(componentType, currentProject.getComponents()));
        newSelection.add(cloned);
        for (int i = 0; i < component.getControlPointCount(); i++) {
          Point p = component.getControlPoint(i);
          Point newPoint = new Point(p.x + grid, p.y + grid);
          cloned.setControlPoint(newPoint, i);
        }
        currentProject.getComponents().add(cloned);
      } catch (Exception e) {
      }
    }

    updateSelection(newSelection);

    messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Duplicate");
    drawingManager.clearContinuityArea();
    projectFileManager.notifyFileChange();
    messageDispatcher.dispatchMessage(EventType.REPAINT);
  }

  @Override
  public void deleteSelectedComponents() {
    LOG.info("deleteSelectedComponents()");
    if (selectedComponents.isEmpty()) {
      LOG.debug("Nothing to delete");
      return;
    }
    Project oldProject = currentProject.clone();
    // Remove selected components from any groups.
    ungroupComponents(selectedComponents);
    // Remove from area map.
    for (IDIYComponent<?> component : selectedComponents) {
      drawingManager.invalidateComponent(component);
    }
    currentProject.getComponents().removeAll(selectedComponents);
    messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Delete");
    drawingManager.clearContinuityArea();
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
      ConfigurationManager.getInstance().writeValue(DEFAULTS_KEY_PREFIX + className + ":" + propertyName, value);
    }
  }

  @Override
  public void setDefaultPropertyValue(Class<?> clazz, String propertyName, Object value) {
    LOG.info(String.format("setProjectDefaultPropertyValue(%s, %s, %s)", clazz.getName(), propertyName, value));
    LOG.debug("Default property value set for " + Project.class.getName() + ":" + propertyName);
    ConfigurationManager.getInstance().writeValue(DEFAULTS_KEY_PREFIX + clazz.getName() + ":" + propertyName, value);
  }

  @Override
  public void setMetric(boolean isMetric) {
    ConfigurationManager.getInstance().writeValue(Presenter.METRIC_KEY, isMetric);
  }

  @Override
  public void groupSelectedComponents() {
    LOG.info("groupSelectedComponents()");
    Project oldProject = currentProject.clone();
    // First remove the selected components from other groups.
    ungroupComponents(selectedComponents);
    // Then group them together.
    currentProject.getGroups().add(new HashSet<IDIYComponent<?>>(selectedComponents));
    // Notify the listeners.
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    if (!oldProject.equals(currentProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Group");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void ungroupSelectedComponents() {
    LOG.info("ungroupSelectedComponents()");
    Project oldProject = currentProject.clone();
    ungroupComponents(selectedComponents);
    // Notify the listeners.
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    if (!oldProject.equals(currentProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Ungroup");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void setLayerLocked(int layerZOrder, boolean locked) {
    LOG.info(String.format("setLayerLocked(%s, %s)", layerZOrder, locked));
    Project oldProject = currentProject.clone();
    if (locked) {
      currentProject.getLockedLayers().add(layerZOrder);
    } else {
      currentProject.getLockedLayers().remove(layerZOrder);
    }
    updateSelection(EMPTY_SELECTION);
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    messageDispatcher.dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject.getLockedLayers());
    if (!oldProject.equals(currentProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), locked ? "Lock Layer" : "Unlock Layer");
      projectFileManager.notifyFileChange();
    }
  }

  @Override
  public void setLayerVisibility(int layerZOrder, boolean visible) {
    LOG.info(String.format("setLayerVisibility(%s, %s)", layerZOrder, visible));
    Project oldProject = currentProject.clone();
    if (visible) {
      currentProject.getHiddenLayers().remove(layerZOrder);
    } else {
      currentProject.getHiddenLayers().add(layerZOrder);
    }
    updateSelection(EMPTY_SELECTION);
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    messageDispatcher.dispatchMessage(EventType.LAYER_VISIBILITY_CHANGED, currentProject.getHiddenLayers());
    if (!oldProject.equals(currentProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), visible ? "Show Layer" : "Hide Layer");
      projectFileManager.notifyFileChange();
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void sendSelectionToBack() {
    LOG.info("sendSelectionToBack()");
    int forceConfirmation = -1;
    Project oldProject = currentProject.clone();

    // sort the selection in the reversed Z-order to preserve the order after moving to the back
    List<IDIYComponent<?>> selection = new ArrayList<IDIYComponent<?>>(selectedComponents);
    Collections.sort(selection, new Comparator<IDIYComponent<?>>() {

      @Override
      public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
        return new Integer(currentProject.getComponents().indexOf(o2)).compareTo(currentProject.getComponents()
            .indexOf(o1));
      }
    });

    for (IDIYComponent<?> component : selection) {
      ComponentType componentType =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      int index = currentProject.getComponents().indexOf(component);
      if (index < 0) {
        LOG.warn("Component not found in the project: " + component.getName());
      } else
        while (index > 0) {
          IDIYComponent<?> componentBefore = currentProject.getComponents().get(index - 1);
          if (!selectedComponents.contains(componentBefore)) {
            ComponentType componentBeforeType =
                ComponentProcessor.getInstance().extractComponentTypeFrom(
                    (Class<? extends IDIYComponent<?>>) componentBefore.getClass());
            if (!componentType.isFlexibleZOrder()
                && Math.round(componentBeforeType.getZOrder()) < Math.round(componentType.getZOrder())
                && forceConfirmation != IView.YES_OPTION
                && (forceConfirmation =
                    this.view
                        .showConfirmDialog(
                            "Selected component(s) have reached the bottom of their layer. Do you want to force the selection to the back?",
                            "Send Selection to Back", IView.YES_NO_OPTION, IView.QUESTION_MESSAGE)) != IView.YES_OPTION)
              break;
          }
          Collections.swap(currentProject.getComponents(), index, index - 1);
          index--;
        }
    }
    if (!oldProject.equals(currentProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(), "Send to Back");
      projectFileManager.notifyFileChange();
      messageDispatcher.dispatchMessage(EventType.REPAINT);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void bringSelectionToFront() {
    LOG.info("bringSelectionToFront()");
    int forceConfirmation = -1;
    Project oldProject = currentProject.clone();

    // sort the selection in Z-order
    List<IDIYComponent<?>> selection = new ArrayList<IDIYComponent<?>>(selectedComponents);
    Collections.sort(selection, new Comparator<IDIYComponent<?>>() {

      @Override
      public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
        return new Integer(currentProject.getComponents().indexOf(o1)).compareTo(currentProject.getComponents()
            .indexOf(o2));
      }
    });

    for (IDIYComponent<?> component : selection) {
      ComponentType componentType =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      int index = currentProject.getComponents().indexOf(component);
      if (index < 0) {
        LOG.warn("Component not found in the project: " + component.getName());
      } else
        while (index < currentProject.getComponents().size() - 1) {
          IDIYComponent<?> componentAfter = currentProject.getComponents().get(index + 1);
          if (!selectedComponents.contains(componentAfter)) {
            ComponentType componentAfterType =
                ComponentProcessor.getInstance().extractComponentTypeFrom(
                    (Class<? extends IDIYComponent<?>>) componentAfter.getClass());
            if (!componentType.isFlexibleZOrder()
                && Math.round(componentAfterType.getZOrder()) > Math.round(componentType.getZOrder())
                && forceConfirmation != IView.YES_OPTION
                && (forceConfirmation =
                    this.view
                        .showConfirmDialog(
                            "Selected component(s) have reached the top of their layer. Do you want to force the selection to the top?",
                            "Bring Selection to Front", IView.YES_NO_OPTION, IView.QUESTION_MESSAGE)) != IView.YES_OPTION)
              break;
          }
          Collections.swap(currentProject.getComponents(), index, index + 1);
          index++;
        }
    }
    if (!oldProject.equals(currentProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(),
          "Bring to Front");
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

  @SuppressWarnings("unchecked")
  @Override
  public void renumberSelectedComponents(final boolean xAxisFirst) {
    LOG.info("renumberSelectedComponents(" + xAxisFirst + ")");
    if (getSelectedComponents().isEmpty()) {
      return;
    }
    Project oldProject = currentProject.clone();
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
      component.setName(instantiationManager.createUniqueName(ComponentProcessor.getInstance()
          .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass()), currentProject
          .getComponents()));
    }

    messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(),
        "Renumber selection");
    projectFileManager.notifyFileChange();
    messageDispatcher.dispatchMessage(EventType.REPAINT);
  }

  public void updateSelection(Collection<IDIYComponent<?>> newSelection) {
    this.selectedComponents = new HashSet<IDIYComponent<?>>(newSelection);
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
    messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED, selectedComponents, controlPointMap.keySet());
  }

  @SuppressWarnings("unchecked")
  @Override
  public void expandSelection(ExpansionMode expansionMode) {
    LOG.info(String.format("expandSelection(%s)", expansionMode));
    List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(this.selectedComponents);
    // Find control points of all selected components and all types
    Set<String> selectedNamePrefixes = new HashSet<String>();
    if (expansionMode == ExpansionMode.SAME_TYPE) {
      for (IDIYComponent<?> component : getSelectedComponents()) {
        selectedNamePrefixes.add(ComponentProcessor.getInstance()
            .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass()).getNamePrefix());
      }
    }
    // Now try to find components that intersect with at least one component
    // in the pool.
    for (IDIYComponent<?> component : getCurrentProject().getComponents()) {
      // Skip already selected components or ones that cannot be stuck to
      // other components.
      ComponentArea area = drawingManager.getComponentArea(component);
      if (newSelection.contains(component) || !component.isControlPointSticky(0) || area == null
          || area.getOutlineArea() == null)
        continue;
      boolean matches = false;
      for (IDIYComponent<?> selectedComponent : this.selectedComponents) {
        ComponentArea selectedArea = drawingManager.getComponentArea(selectedComponent);
        if (selectedArea == null || selectedArea.getOutlineArea() == null)
          continue;
        Area intersection = new Area(area.getOutlineArea());
        intersection.intersect(selectedArea.getOutlineArea());
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
                .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass()).getNamePrefix())) {
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
   * Removes all the groups that contain at least one of the specified components.
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
   * Finds all components that are grouped with the specified component. This should be called any
   * time components are added or removed from the selection.
   * 
   * @param component
   * @return set of all components that belong to the same group with the specified component. At
   *         the minimum, set contains that single component.
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
  public Point2D[] calculateSelectionDimension() {
    if (selectedComponents.isEmpty()) {
      return null;
    }
    Rectangle2D rect = getSelectionBounds(false);

    double width = rect.getWidth();
    double height = rect.getHeight();

    width /= Constants.PIXELS_PER_INCH;
    height /= Constants.PIXELS_PER_INCH;

    Point2D inSize = new Point2D.Double(width, height);

    width *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();
    height *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();

    Point2D cmSize = new Point2D.Double(width, height);

    return new Point2D[] {inSize, cmSize};
  }

  // @Override
  // public Rectangle2D getSelectedAreaRect() {
  // if (selectedComponents.isEmpty()) {
  // return null;
  // }
  // Area area = new Area();
  // for (IDIYComponent<?> component : selectedComponents) {
  // Area componentArea = drawingManager.getComponentArea(component);
  // if (componentArea != null) {
  // area.add(componentArea);
  // } else {
  // LOG.warn("No area found for: " + component.getName());
  // }
  // }
  // return area.getBounds2D();
  // }

  /**
   * Adds a component to the project taking z-order into account.
   * 
   * @param component
   */
  @SuppressWarnings("unchecked")
  private void addComponent(IDIYComponent<?> component, boolean alowAutoCreate) {
    int index = currentProject.getComponents().size();
    while (index > 0
        && ComponentProcessor.getInstance()
            .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass()).getZOrder() < ComponentProcessor
            .getInstance()
            .extractComponentTypeFrom(
                (Class<? extends IDIYComponent<?>>) currentProject.getComponents().get(index - 1).getClass())
            .getZOrder()) {
      index--;
    }
    if (index < currentProject.getComponents().size()) {
      currentProject.getComponents().add(index, component);
    } else {
      currentProject.getComponents().add(component);
    }

    // Check if we should auto-create something.
    for (IAutoCreator creator : this.getAutoCreators()) {
      List<IDIYComponent<?>> newComponents = creator.createIfNeeded(component);
      if (newComponents != null) {
        for (IDIYComponent<?> c : newComponents)
          addComponent(c, false);
      }
    }
  }

  @Override
  public List<PropertyWrapper> getMutualSelectionProperties() {
    try {
      return ComponentProcessor.getInstance().getMutualSelectionProperties(selectedComponents);
    } catch (Exception e) {
      LOG.error("Could not get mutual selection properties", e);
      return null;
    }
  }

  private void applyPropertiesToSelection(List<PropertyWrapper> properties) {
    LOG.debug(String.format("applyPropertiesToSelection(%s)", properties));
    Project oldProject = currentProject.clone();
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
      view.showMessage("Could not apply changes to the selection. Check the log for details.", "Error",
          IView.ERROR_MESSAGE);
    } finally {
      // Notify the listeners.
      if (!oldProject.equals(currentProject)) {
        messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(),
            "Edit Selection");
        drawingManager.clearContinuityArea();
        projectFileManager.notifyFileChange();
      }
      messageDispatcher.dispatchMessage(EventType.REPAINT);
    }
  }

  @Override
  public List<PropertyWrapper> getProperties(Object object) {
    List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(object.getClass());
    try {
      for (PropertyWrapper property : properties) {
        property.readFrom(object);
      }
    } catch (Exception e) {
      LOG.error("Could not get object properties", e);
      return null;
    }
    Collections.sort(properties, ComparatorFactory.getInstance().getDefaultPropertyComparator());
    return properties;
  }

  @Override
  public void applyProperties(Object obj, List<PropertyWrapper> properties) {
    LOG.debug(String.format("applyProperties(%s, %s)", obj, properties));
    Project oldProject = currentProject.clone();
    try {
      for (PropertyWrapper property : properties) {
        property.writeTo(obj);
      }
    } catch (Exception e) {
      LOG.error("Could not apply properties", e);
      view.showMessage("Could not apply changes. Check the log for details.", "Error", IView.ERROR_MESSAGE);
    } finally {
      // Notify the listeners.
      if (!oldProject.equals(currentProject)) {
        messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(),
            "Edit Project");
        drawingManager.clearContinuityArea();
        projectFileManager.notifyFileChange();
      }
      drawingManager.fireZoomChanged();
    }
  }

  @Override
  public ComponentType getNewComponentTypeSlot() {
    return instantiationManager.getComponentTypeSlot();
  }

  @Override
  public void setNewComponentTypeSlot(ComponentType componentType, Template template, boolean forceInstatiate) {
    LOG.info(String.format("setNewComponentSlot(%s)", componentType == null ? null : componentType.getName()));
    if (componentType != null && componentType.getInstanceClass() == null) {
      LOG.info("Cannot set new component type slot for type " + componentType.getName());
      setNewComponentTypeSlot(null, null, false);
      return;
    }
    
//    if (componentType == null) {
//      controlPointMap.clear();
//      updateSelection(EMPTY_SELECTION);
//    }

    // try to find a default template if none is provided
    if (componentType != null && template == null) {
      String defaultTemplate = getDefaultVariant(componentType);
      List<Template> templates = getVariantsFor(componentType);
      if (templates != null && defaultTemplate != null)
        for (Template t : templates) {
          if (t.getName().equals(defaultTemplate)) {
            template = t;
            break;
          }
        }
    }

    try {
      instantiationManager.setComponentTypeSlot(componentType, template, currentProject, forceInstatiate);
      
      if (forceInstatiate)
        updateSelection(instantiationManager.getComponentSlot());
      else if (componentType != null)
        updateSelection(EMPTY_SELECTION);
      
      messageDispatcher.dispatchMessage(EventType.REPAINT);
      // messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
      // selectedComponents);
      // messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
      // calculateSelectionDimension());
      messageDispatcher.dispatchMessage(EventType.SLOT_CHANGED, instantiationManager.getComponentTypeSlot(),
          instantiationManager.getFirstControlPoint(), forceInstatiate);
    } catch (Exception e) {
      LOG.error("Could not set component type slot", e);
      view.showMessage("Could not set component type slot. Check log for details.", "Error", IView.ERROR_MESSAGE);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void saveSelectedComponentAsVariant(String variantName) {
    LOG.info(String.format("saveSelectedComponentAsVariant(%s)", variantName));
    if (selectedComponents.size() != 1) {
      throw new RuntimeException("Can only save a single component as a variant at once.");
    }
    IDIYComponent<?> component = selectedComponents.iterator().next();
    ComponentType type =
        ComponentProcessor.getInstance().extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass());
    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) ConfigurationManager.getInstance().readObject(TEMPLATES_KEY, null);
    if (variantMap == null) {
      variantMap = new HashMap<String, List<Template>>();
    }
    String key = type.getInstanceClass().getCanonicalName();
    List<Template> variants = variantMap.get(key);
    if (variants == null) {
      variants = new ArrayList<Template>();
      variantMap.put(key, variants);
    }
    List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(component.getClass());
    Map<String, Object> values = new HashMap<String, Object>();
    for (PropertyWrapper property : properties) {
      if (property.getName().equalsIgnoreCase("name")) {
        continue;
      }
      try {
        property.readFrom(component);
        values.put(property.getName(), property.getValue());
      } catch (Exception e) {
      }
    }
    List<Point> points = new ArrayList<Point>();

    for (int i = 0; i < component.getControlPointCount(); i++) {
      Point p = new Point(component.getControlPoint(i));
      points.add(p);
    }
    int x = points.iterator().next().x;
    int y = points.iterator().next().y;
    for (Point point : points) {
      point.translate(-x, -y);
    }

    Template template = new Template(variantName, values, points);
    boolean exists = false;
    for (Template t : variants) {
      if (t.getName().equalsIgnoreCase(variantName)) {
        exists = true;
        break;
      }
    }

    if (exists) {
      int result =
          view.showConfirmDialog("A variant with that name already exists. Overwrite?", "Save as Variant",
              IView.YES_NO_OPTION, IView.WARNING_MESSAGE);
      if (result != IView.YES_OPTION) {
        return;
      }
      // Delete the existing variant
      Iterator<Template> i = variants.iterator();
      while (i.hasNext()) {
        Template t = i.next();
        if (t.getName().equalsIgnoreCase(variantName)) {
          i.remove();
        }
      }
    }

    variants.add(template);

    if (System.getProperty("org.diylc.WriteStaticVariants", "false").equalsIgnoreCase("true"))
    {
      if (defaultVariantMap == null)
        defaultVariantMap = new HashMap<String, List<Template>>();
      // unify default and user-variants
      for(Map.Entry<String, List<Template>> entry : variantMap.entrySet()) {
        if (defaultVariantMap.containsKey(entry.getKey())) {
          defaultVariantMap.get(entry.getKey()).addAll(entry.getValue());
        } else {
          defaultVariantMap.put(entry.getKey(), entry.getValue());
        }        
      }
      try {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("variants.xml"));
        XStream xStream = new XStream(new DomDriver());
        xStream.toXML(defaultVariantMap, out);        
        out.close();
        // no more user variants
        ConfigurationManager.getInstance().writeValue(TEMPLATES_KEY, null);
        LOG.info("Saved default variants");
      } catch (IOException e) {
        LOG.error("Could not save default variants", e);
      }
    } else {
      ConfigurationManager.getInstance().writeValue(TEMPLATES_KEY, variantMap);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Template> getVariantsFor(ComponentType type) {
    Map<String, List<Template>> lookupMap = new TreeMap<String, List<Template>>(String.CASE_INSENSITIVE_ORDER);
    
    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) ConfigurationManager.getInstance().readObject(TEMPLATES_KEY, null);
    if (variantMap != null)
      lookupMap.putAll(variantMap);
    
    // try by class name and then by old category.type format
    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();
    
    List<Template> variants = new ArrayList<Template>();
    if (variantMap != null) {
      List<Template> userVariants = variantMap.get(key1);
      if (userVariants != null && !userVariants.isEmpty())
        variants.addAll(userVariants);
      userVariants = variantMap.get(key2);
      if (userVariants != null && !userVariants.isEmpty())
        variants.addAll(userVariants);
    }
    if (defaultVariantMap != null) {
      List<Template> defaultVariants = defaultVariantMap.get(key1);        
      if (defaultVariants != null && !defaultVariants.isEmpty())
        variants.addAll(defaultVariants);
      defaultVariants = defaultVariantMap.get(key2);
      if (defaultVariants != null && !defaultVariants.isEmpty())
        variants.addAll(defaultVariants);
    }
    Collections.sort(variants, new Comparator<Template>() {

      @Override
      public int compare(Template o1, Template o2) {       
        return o1.getName().compareTo(o2.getName());
      }      
    });
    return variants;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Template> getVariantsForSelection() {
    if (this.selectedComponents.isEmpty()) {
      throw new RuntimeException("No components selected");
    }
    ComponentType selectedType = null;
    Iterator<IDIYComponent<?>> iterator = this.selectedComponents.iterator();
    while (iterator.hasNext()) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) iterator.next().getClass());
      if (selectedType == null)
        selectedType = type;
      else if (selectedType.getInstanceClass() != type.getInstanceClass())
        return null;
    }
    return getVariantsFor(selectedType);
  }

  @Override
  public void applyVariantToSelection(Template template) {
    LOG.debug(String.format("applyTemplateToSelection(%s)", template.getName()));

    Project oldProject = currentProject.clone();

    for (IDIYComponent<?> component : this.selectedComponents) {
      try {
        drawingManager.invalidateComponent(component);
        // this.instantiationManager.loadComponentShapeFromTemplate(component, template);
        this.instantiationManager.fillWithDefaultProperties(component, template);
      } catch (Exception e) {
        LOG.warn("Could not apply templates to " + component.getName(), e);
      }
    }

    // Notify the listeners.
    if (!oldProject.equals(currentProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject.clone(),
          "Edit Selection");
      drawingManager.clearContinuityArea();
      projectFileManager.notifyFileChange();
    }
    messageDispatcher.dispatchMessage(EventType.REPAINT);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void deleteVariant(ComponentType type, String templateName) {
    LOG.debug(String.format("deleteTemplate(%s, %s)", type, templateName));
    Map<String, List<Template>> templateMap =
        (Map<String, List<Template>>) ConfigurationManager.getInstance().readObject(TEMPLATES_KEY, null);
    if (templateMap != null) {
      // try by class name and then by old category.type format
      String key1 = type.getInstanceClass().getCanonicalName();
      String key2 = type.getCategory() + "." + type.getName();
      
      List<Template> templates = templateMap.get(key1);
      if (templates != null) {
        Iterator<Template> i = templates.iterator();
        while (i.hasNext()) {
          Template t = i.next();
          if (t.getName().equalsIgnoreCase(templateName)) {
            i.remove();
          }
        }
      }
      templates = templateMap.get(key2);
      if (templates != null) {
        Iterator<Template> i = templates.iterator();
        while (i.hasNext()) {
          Template t = i.next();
          if (t.getName().equalsIgnoreCase(templateName)) {
            i.remove();
          }
        }
      }
    }
    ConfigurationManager.getInstance().writeValue(TEMPLATES_KEY, templateMap);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setDefaultVariant(ComponentType type, String templateName) {
    LOG.debug(String.format("setTemplateDefault(%s, %s)", type, templateName));
    Map<String, String> defaultTemplateMap =
        (Map<String, String>) ConfigurationManager.getInstance().readObject(DEFAULT_TEMPLATES_KEY, null);
    if (defaultTemplateMap == null)
      defaultTemplateMap = new HashMap<String, String>();
    
    // try by class name and then by old category.type format
    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();
    
    if (templateName.equals(defaultTemplateMap.get(key1)) || templateName.equals(defaultTemplateMap.get(key2))) {
      defaultTemplateMap.remove(key1);
      defaultTemplateMap.remove(key2);
    }
    else {
      // get rid of legacy key
      defaultTemplateMap.remove(key2);
      defaultTemplateMap.put(key1, templateName);
    }
    ConfigurationManager.getInstance().writeValue(DEFAULT_TEMPLATES_KEY, defaultTemplateMap);      
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public String getDefaultVariant(ComponentType type) {
    Map<String, String> defaultTemplateMap =
        (Map<String, String>) ConfigurationManager.getInstance().readObject(DEFAULT_TEMPLATES_KEY, null);
    if (defaultTemplateMap == null)
      return null;
    
    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();
    
    if (defaultTemplateMap.containsKey(key1))
      return defaultTemplateMap.get(key1);
    
    return defaultTemplateMap.get(key2);
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

  @SuppressWarnings("unchecked")
  private boolean isComponentLocked(IDIYComponent<?> component) {
    ComponentType componentType =
        ComponentProcessor.getInstance().extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass());
    return currentProject.getLockedLayers().contains((int) Math.round(componentType.getZOrder()));
  }

  /**
   * Scales point from display base to actual base.
   * 
   * @param point
   * @return
   */
  private Point scalePoint(Point point) {
    Point p = point == null ? null : new Point((int) (point.x / drawingManager.getZoomLevel()),
        (int) (point.y / drawingManager.getZoomLevel()));
        
    if (p != null && ConfigurationManager.getInstance().readBoolean(EXTRA_SPACE_KEY, true)) {
      double extraSpace = drawingManager.getExtraSpace(currentProject);
      p.translate((int)(-extraSpace), (int)(-extraSpace));
    }
    return p;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void saveSelectionAsBlock(String blockName) {
    LOG.debug(String.format("saveSelectionAsBlock(%s)", blockName));
    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getInstance().readObject(BLOCKS_KEY, null);
    if (blocks == null)
      blocks = new HashMap<String, List<IDIYComponent<?>>>();
    List<IDIYComponent<?>> blockComponents = new ArrayList<IDIYComponent<?>>(this.selectedComponents);
    Collections.sort(blockComponents, new Comparator<IDIYComponent<?>>() {

      @Override
      public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
        return new Integer(currentProject.getComponents().indexOf(o1)).compareTo(currentProject.getComponents().indexOf(o2));
      }});
    blocks.put(blockName, blockComponents);
    ConfigurationManager.getInstance().writeValue(BLOCKS_KEY, blocks);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void loadBlock(String blockName) throws InvalidBlockException {
    LOG.debug(String.format("loadBlock(%s)", blockName));
    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getInstance().readObject(BLOCKS_KEY, null);
    if (blocks != null) {
      Collection<IDIYComponent<?>> components = blocks.get(blockName);
      if (components == null)
        throw new InvalidBlockException();
      // clear potential control point every time!
      instantiationManager.setPotentialControlPoint(null);
      // clone components
      List<IDIYComponent<?>> clones = new ArrayList<IDIYComponent<?>>();
      List<IDIYComponent<?>> testComponents = new ArrayList<IDIYComponent<?>>(currentProject.getComponents());
      for (IDIYComponent<?> c : components)
        try {
          IDIYComponent<?> clone = c.clone();
          clone.setName(instantiationManager.createUniqueName(ComponentProcessor.getInstance()
              .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) clone.getClass()), testComponents));
          testComponents.add(clone);
          clones.add(clone);
        } catch (CloneNotSupportedException e) {
          LOG.error("Could not clone component: " + c);
        }
      // paste them to the project
      pasteComponents(clones, true);
    } else
      throw new InvalidBlockException();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void deleteBlock(String blockName) {
    LOG.debug(String.format("deleteBlock(%s)", blockName));
    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getInstance().readObject(BLOCKS_KEY, null);
    if (blocks != null) {
      blocks.remove(blockName);
      ConfigurationManager.getInstance().writeValue(BLOCKS_KEY, blocks);
    }
  }
  
  @Override
  public double getExtraSpace() {
    if (!ConfigurationManager.getInstance().readBoolean(EXTRA_SPACE_KEY, true))
      return 0;
    
    double extraSpace = drawingManager.getExtraSpace(currentProject);
    boolean metric = ConfigurationManager.getInstance().readBoolean(Presenter.METRIC_KEY, true);    

    extraSpace /= Constants.PIXELS_PER_INCH;
    
    if (metric)
       extraSpace *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();
    
    return extraSpace;
  }

  @SuppressWarnings("unchecked")
  @Override
  public int importVariants(String fileName) throws IOException {
    LOG.debug(String.format("importVariants(%s)", fileName));
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName));
    XStream xStream = new XStream(new DomDriver());
    
    VariantPackage pkg = (VariantPackage) xStream.fromXML(in);
    
    in.close();
    
    if (pkg == null || pkg.getVariants().isEmpty())
      return 0;
    
    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) ConfigurationManager.getInstance().readObject(TEMPLATES_KEY, null);
    if (variantMap == null) {
      variantMap = new HashMap<String, List<Template>>();
    }
    
    for (Map.Entry<String, List<Template>> entry : pkg.getVariants().entrySet()) {
      List<Template> templates;      
      templates = variantMap.get(entry.getKey());
      if (templates == null)
      {
        templates = new ArrayList<Template>();
        variantMap.put(entry.getKey(), templates);
      }
      for (Template t : entry.getValue()) {
        templates.add(new Template(t.getName() + " [" + pkg.getOwner() + "]", t.getValues(), t.getPoints()));      
      }
    }
    
    ConfigurationManager.getInstance().writeValue(TEMPLATES_KEY, variantMap);
        
    LOG.info(String.format("Loaded variants for %d components", pkg.getVariants().size()));
    
    return pkg.getVariants().size();
  }

  @SuppressWarnings("unchecked")
  @Override
  public int importBlocks(String fileName) throws IOException {
    LOG.debug(String.format("importBlocks(%s)", fileName));
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName));
    XStream xStream = new XStream(new DomDriver());
    
    BuildingBlockPackage pkg = (BuildingBlockPackage) xStream.fromXML(in);
    
    in.close();
    
    if (pkg == null || pkg.getBlocks().isEmpty())
      return 0;
    
    Map<String, List<IDIYComponent<?>>> blocks =
        (Map<String, List<IDIYComponent<?>>>) ConfigurationManager.getInstance().readObject(BLOCKS_KEY, null);
    if (blocks == null) {
      blocks = new HashMap<String, List<IDIYComponent<?>>>();
    }
    
    for (Map.Entry<String, List<IDIYComponent<?>>> entry : pkg.getBlocks().entrySet()) {
      blocks.put(entry.getKey() + " [" + pkg.getOwner() + "]", entry.getValue());
    }
    
    ConfigurationManager.getInstance().writeValue(BLOCKS_KEY, blocks);
        
    LOG.info(String.format("Loaded building blocks for %d components", pkg.getBlocks().size()));
    
    return pkg.getBlocks().size();
  }
  
  private static boolean upgradedVariants = false;
  
  @SuppressWarnings("unchecked")
  private synchronized void upgradeVariants() {
    if (upgradedVariants)
      return;
    
    upgradedVariants = true;
    
    LOG.info("Checking if variants need to be updated");
    Map<String, List<Template>> lookupMap = new TreeMap<String, List<Template>>(String.CASE_INSENSITIVE_ORDER);    
    Map<String, List<Template>> variantMap =
        (Map<String, List<Template>>) ConfigurationManager.getInstance().readObject(TEMPLATES_KEY, null);
    
    if (variantMap == null)
      return;
    
    Map<String, ComponentType> typeMap = new TreeMap<String, ComponentType>(String.CASE_INSENSITIVE_ORDER);
    
    Map<String, List<ComponentType>> componentTypes = getComponentTypes();
    for (Map.Entry<String, List<ComponentType>> entry : componentTypes.entrySet())
      for (ComponentType type : entry.getValue()) {
        typeMap.put(type.getInstanceClass().getCanonicalName(), type);
        typeMap.put(type.getCategory() + "." + type.getName(), type);
        if (type.getCategory().contains("Electro-Mechanical"))
          typeMap.put(type.getCategory().replace("Electro-Mechanical", "Electromechanical") + "." + type.getName(), type);
      }
    
    Map<String, List<Template>> newVariantMap = new HashMap<String, List<Template>>();
    
    lookupMap.putAll(variantMap);
    
    for (Map.Entry<String, List<Template>> entry : variantMap.entrySet()) {
      if (typeMap.containsKey(entry.getKey())) {
        newVariantMap.put(typeMap.get(entry.getKey()).getInstanceClass().getCanonicalName(), entry.getValue()); // great, nothing to upgrade
      } else {
        LOG.warn("Could not upgrade variants for: " + entry.getKey());
      }
    }
    
    ConfigurationManager.getInstance().writeValue(TEMPLATES_KEY, newVariantMap);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public List<Netlist> extractNetlists() {    
    Map<Netlist, Netlist> result = new HashMap<Netlist, Netlist>();
    List<Node> nodes = new ArrayList<Node>();
    
    List<ISwitch> switches = new ArrayList<ISwitch>();
    for (IDIYComponent<?> c : currentProject.getComponents()) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());
      
      // extract nodes
      if (!(c instanceof IContinuity)) {
        for (int i = 0; i < c.getControlPointCount(); i++) {
          String nodeName = c.getControlPointNodeName(i);
          if (nodeName != null) {
            nodes.add(new Node(c, i));
          }
        }
      }
      
      // extract switches
      if (ISwitch.class.isAssignableFrom(type.getInstanceClass()))
        switches.add((ISwitch)c);
    }
    
    // save us the trouble
    if (nodes.isEmpty())
      return null;
    
    // if there are no switches, make one with 1 position so we get 1 result back
    if (switches.isEmpty())
      switches.add(new ISwitch() {
        
        @Override
        public String getPositionName(int position) {
          return "Default";
        }
        
        @Override
        public int getPositionCount() {
          return 1;
        }
        
        @Override
        public boolean arePointsConnected(int index1, int index2, int position) {
          return false;
        }
      });
    
    // construct all possible combinations
    int[] positions = new int[switches.size()];
    for (int i = 0; i < switches.size(); i++)
      positions[i] = 0;
    
    // grab continuity areas
    List<Area> continuity = drawingManager.getContinuityAreas(currentProject);
    
    int i = switches.size() - 1;
    while (i >= 0) {
      // process the current combination
      Map<ISwitch, Integer> switchPositions = new HashMap<ISwitch, Integer>();
      List<Position> posList = new ArrayList<Position>();
      for (int j = 0; j < positions.length; j++) {
        switchPositions.put(switches.get(j), positions[j]);
        posList.add(new Position(switches.get(j), positions[j]));
      }
      List<Connection> connections = getConnections(switchPositions);  
      Netlist graph = constructNetlist(nodes, connections, continuity);
      
      // merge graphs that are effectively the same
      if (result.containsKey(graph)) {
        result.get(graph).getSwitchSetup().add(new SwitchSetup(posList));
      } else {        
        graph.getSwitchSetup().add(new SwitchSetup(posList));
        result.put(graph, graph);
      }
      
      // find the next combination if possible
      if (positions[i] < switches.get(i).getPositionCount() - 1) {
        positions[i]++;
      } else {
        while (i >= 0 && positions[i] == switches.get(i).getPositionCount() - 1)
          i--;
        if (i >= 0) {
          positions[i]++;
          for (int j = i + 1; j < positions.length; j++)
            positions[j] = 0;
          i = switches.size() - 1;
        }        
      }
    }
    
    // sort everything alphabetically
    List<Netlist> netlists = new ArrayList<Netlist>(result.keySet());
    Collections.sort(netlists);    

    return netlists;
  }
  
  private Netlist constructNetlist(List<Node> nodes, List<Connection> connections, List<Area> continuityAreas) {
    Netlist netlist = new Netlist();
    
//    debugging code    
//    StringBuilder sb = new StringBuilder();
//    sb.append("Nodes:").append("\n");
//    for (Node n : nodes) {
//      sb.append(n.toString()).append("\n");
//    }
//    sb.append("Connections:").append("\n");
//    for (Line2D n : connections) {
//      sb.append(n.getP1()).append(":").append(n.getP2()).append("\n");
//    }
//    LOG.debug(sb.toString());

    double t = DrawingManager.CONTROL_POINT_SIZE;
    for (int i = 0; i < nodes.size() - 1; i++)
      for (int j = i + 1; j < nodes.size(); j++) {
        Node node1 = nodes.get(i);
        Node node2 = nodes.get(j);
        Point2D point1 = node1.getComponent().getControlPoint(node1.getPointIndex());
        Point2D point2 = node2.getComponent().getControlPoint(node2.getPointIndex());
        
        String commonPoint1 = node1.getComponent().getCommonPointName(node1.getPointIndex());
        String commonPoint2 = node2.getComponent().getCommonPointName(node2.getPointIndex());
        
        // try both directions
        if (point1.distance(point2) < t || 
            checkGraphConnection(point1, point2, connections, continuityAreas, new boolean[connections.size()]) || 
            checkGraphConnection(point2, point1, connections, continuityAreas, new boolean[connections.size()]) ||
            (commonPoint1 != null && commonPoint1.equalsIgnoreCase(commonPoint2))) {
          boolean added = false;
          // add to an existing vertex if possible
          for (Group g : netlist.getGroups())
            if (g.getNodes().contains(node1)) {
              g.getNodes().add(node2);
              added = true;
            } else if (g.getNodes().contains(node2)) {
              g.getNodes().add(node1);
              added = true;
            }
          if (!added)
            netlist.getGroups().add(new Group(node1, node2));
        }        
      }
    
    // merge overlapping groups if needed
    boolean reduce = true;
    while (reduce) {
      reduce = false;
      List<Group> groups = netlist.getSortedGroups();
      Iterator<Group> i = groups.iterator();
      while (i.hasNext()) {
        Group g1 = i.next();
        for (Group g2 : groups) {
          if (g1 != g2 && !Collections.disjoint(g1.getNodes(), g2.getNodes())) {
            i.remove();
            g2.getNodes().addAll(g1.getNodes());            
            reduce = true;
            break;
          }
        }
      }
      if (reduce) {
        netlist.getGroups().clear();
        netlist.getGroups().addAll(groups);
      }
    }
    
    Collections.sort(netlist.getSwitchSetup());   
    
    return netlist;
  }
  
  private boolean checkGraphConnection(Point2D point1, Point2D point2, List<Connection> connections,
      List<Area> continuityAreas, boolean[] visited) {
    double t = DrawingManager.CONTROL_POINT_SIZE;
    
    if (point1.distance(point2) < t)
      return true;
    
    for (Area a : continuityAreas) {
      if (a.contains(point1) && a.contains(point2))
        return true;
    }
    
    for (int i = 0; i < connections.size(); i++) {
      if (visited[i])
        continue;
      
      Connection c = connections.get(i);
      if (point1.distance(c.getP1()) < t) {
        visited[i] = true;
        if (checkGraphConnection(c.getP2(), point2, connections, continuityAreas, visited))          
          return true;        
      }
      if (point1.distance(c.getP2()) < t) {
        visited[i] = true;
        if (checkGraphConnection(c.getP1(), point2, connections, continuityAreas, visited))          
          return true;        
      }
    }
    
    return false;
  }

  @SuppressWarnings("unchecked")
  private List<Connection> getConnections(Map<ISwitch, Integer> switchPositions) {
    Set<Connection> connections = new HashSet<Connection>();
    for (IDIYComponent<?> c : currentProject.getComponents()) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());
      // handle direct connections
      if (c instanceof IContinuity) {
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (((IContinuity)c).arePointsConnected(i, j))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j)));
      }
      // handle switches
      if (ISwitch.class.isAssignableFrom(type.getInstanceClass()) && switchPositions.containsKey(c)) {
        int position = switchPositions.get(c);
        ISwitch s = (ISwitch)c;
        for (int i = 0; i < c.getControlPointCount() - 1; i++)
          for (int j = i + 1; j < c.getControlPointCount(); j++)
            if (s.arePointsConnected(i, j, position))
              connections.add(new Connection(c.getControlPoint(i), c.getControlPoint(j)));
      }
    }
    
    drawingManager.expandConnections(connections);
    
    return new ArrayList<Connection>(connections);
  }  
  
  @Override
  public List<INetlistAnalyzer> getNetlistAnalyzers() {
    Set<Class<?>> classes;
    try {
      classes = Utils.getClasses("org.diylc.netlist");
      List<INetlistAnalyzer> result = new ArrayList<INetlistAnalyzer>();
   
      for (Class<?> clazz : classes) {
        if (!Modifier.isAbstract(clazz.getModifiers()) && INetlistAnalyzer.class.isAssignableFrom(clazz)) {
          result.add((INetlistAnalyzer) clazz.newInstance());
        }
      }
      
      Collections.sort(result, new Comparator<INetlistAnalyzer>() {

        @Override
        public int compare(INetlistAnalyzer o1, INetlistAnalyzer o2) {
          return o1.getName().compareToIgnoreCase(o2.getName());
        }        
      });
      
      return result;
    } catch (Exception e) {
      LOG.error("Could not load INetlistSummarizer implementations", e);
      return null;
    }
  }
}
