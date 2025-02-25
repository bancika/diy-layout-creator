/*
 * 
l * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.appframework.miscutils.JarScanner;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.appframework.update.Version;
import org.diylc.appframework.update.VersionNumber;
import org.diylc.clipboard.ComponentTransferable;
import org.diylc.common.ComponentType;
import org.diylc.common.DrawOption;
import org.diylc.common.EventType;
import org.diylc.common.IComponentFilter;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IKeyProcessor;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.IProjectEditor;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.ExpansionMode;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.IView;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.Theme;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.IAutoCreator;
import org.diylc.core.gerber.GerberExporter;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.lang.LangUtil;
import org.diylc.netlist.INetlistParser;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistAnalyzer;
import org.diylc.netlist.NetlistBuilder;
import org.diylc.netlist.NetlistException;
import org.diylc.serialization.ProjectFileManager;
import org.diylc.test.DIYTest;
import org.diylc.test.Snapshot;
import org.diylc.utils.Constants;
import org.diylc.utils.ReflectionUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

/**
 * The main presenter class, contains core app logic and drawing routines.
 * 
 * @author Branislav Stojkovic
 */
public class Presenter implements IPlugInPort, IConfigListener {

  private static final String REACHED_BOTTOM =
      LangUtil.translate("Selected component(s) have reached the bottom of their layer. Do you want to force the selection to the back?");
  private static final String SEND_SELECTION_TO_BACK = LangUtil.translate("Send Selection to Back");
  private static final String SAVE_AS_VARIANT = LangUtil.translate("Save as Variant");
  private static final String VARIANT_EXISTS = LangUtil.translate("A variant with that name already exists. Overwrite?");
  private static final String ROTATE_CHANGE =
      LangUtil.translate("Selection contains components that cannot be rotated. Do you want to exclude them?");
  private static final String MIRRORING_CHANGE =
      LangUtil.translate("Mirroring operation will change the circuit. Do you want to continue?");
  private static final String MIRROR_SELECTION = LangUtil.translate("Mirror Selection");
  private static final String ROTATE_SELECTION = LangUtil.translate("Rotate Selection");
  private static final String CANNOT_MIRROR =
      LangUtil.translate("Selection contains components that cannot be mirrored. Do you want to exclude them?");
  private static final String BRING_SELECTION_TO_FRONT = LangUtil.translate("Bring Selection to Front");
  private static final String REACHED_TOP =
      LangUtil.translate("Selected component(s) have reached the top of their layer. Do you want to force the selection to the top?");
  private static final String WARNING = LangUtil.translate("Warning");
  private static final String UNSAVED_CHANGES = LangUtil.translate("There are unsaved changes. Would you like to save them?");
  private static final String ERROR_SLOT = LangUtil.translate("Could not set component type slot. Check log for details.");
  private static final String ERROR_SAVE = LangUtil.translate("Could not save the project to file. Check the log for details.");
  private static final String ERROR_CREATE = LangUtil.translate("Could not create component. Check log for details.");
  private static final String ERROR_EDIT = LangUtil.translate("Error occurred while editing selection. Check the log for details.");
  private static final String ERROR_NEW = LangUtil.translate("Could not create a new file. Check the log for details.");
  private static final String ERROR = LangUtil.translate("Error");
  private static final String APPLY_ERROR = LangUtil.translate("Could not apply changes. Check the log for details.");

  private static final Logger LOG = Logger.getLogger(Presenter.class);

  public static VersionNumber CURRENT_VERSION = new VersionNumber(4, 0, 0);
  public static List<Version> RECENT_VERSIONS = null;
  // Read the latest version from the local update.xml file
	static {
		try {
			URL resource = Presenter.class.getResource("update.xml");
			if (resource != null) {
				BufferedInputStream in = new BufferedInputStream(resource.openStream());
				XStream xStream = new XStream(new DomDriver());
				xStream.addPermission(AnyTypePermission.ANY);
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
			}
		} catch (Exception e) {
			LOG.error("Could not find version number, using default", e);
		}
	}
  public static final String DEFAULTS_KEY_PREFIX = "default.";

  public static final List<IDIYComponent<?>> EMPTY_SELECTION = Collections.emptyList();

  public static final int ICON_SIZE = 32;

  private static final int MAX_RECENT_FILES = 20;  

  private Project currentProject;
  private Map<String, List<ComponentType>> componentTypes;

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
  private VariantManager variantManager;
  private BuildingBlockManager buildingBlockManager;

  private Rectangle selectionRect;

  private final IView view;
  private IConfigurationManager<?> configManager;

  private MessageDispatcher<EventType> messageDispatcher;

  // Layers
  // private Set<ComponentLayer> lockedLayers;
  // private Set<ComponentLayer> visibleLayers;

  // D&D
  private boolean dragInProgress = false;
  // Previous mouse location, not scaled for zoom factor.
  private Point2D previousDragPoint = null;
  private Project preDragProject = null;
  private int dragAction;
  private Point2D previousScaledPoint;
  
  private DIYTest test = null;
  
  public Presenter(IView view, IConfigurationManager<?> configManager) {
    this(view, configManager, false);
  }

  public Presenter(IView view, IConfigurationManager<?> configManager, boolean importVariantsAndBlocks) {
    super();
    this.view = view;
    this.configManager = configManager;
    plugIns = new ArrayList<IPlugIn>();
    messageDispatcher = new MessageDispatcher<EventType>(true);
    selectedComponents = new HashSet<IDIYComponent<?>>();
    lockedComponents = new HashSet<IDIYComponent<?>>();
    currentProject = new Project();
    // cloner = new Cloner();
    drawingManager = new DrawingManager(messageDispatcher, configManager);
    projectFileManager = new ProjectFileManager(messageDispatcher);
    instantiationManager = new InstantiationManager();
    variantManager = new VariantManager(configManager, projectFileManager.getXStream());
    buildingBlockManager = new BuildingBlockManager(configManager, projectFileManager.getXStream(), instantiationManager);

    // lockedLayers = EnumSet.noneOf(ComponentLayer.class);
    // visibleLayers = EnumSet.allOf(ComponentLayer.class);
    if (importVariantsAndBlocks) {
      variantManager.upgradeVariants(getComponentTypes());
      variantManager.importDefaultVariants();
      buildingBlockManager.importDefaultBlocks();
    }
    
    this.configManager.addConfigListener(HIGHLIGHT_CONTINUITY_AREA, this);
  }

  public void installPlugin(Supplier<IPlugIn> plugInSupplier) {
    try {
      IPlugIn plugIn = plugInSupplier.get();
      LOG.info(String.format("installPlugin(%s)", plugIn.getClass().getSimpleName()));
      plugIns.add(plugIn);
      plugIn.connect(this);
      messageDispatcher.registerListener(plugIn);
    } catch (Exception e) {
      LOG.error("Error while installing plugin", e);
    }
  }

  public void dispose() {
    for (IPlugIn plugIn : plugIns) {
      messageDispatcher.unregisterListener(plugIn);
    }
  }

  // IPlugInPort

  @Override
  public java.lang.Double[] getAvailableZoomLevels() {
    return new java.lang.Double[] {0.25d, 0.3333d, 0.5d, 0.6667d, 0.75d, 1d, 1.25d, 1.5d, 2d, 2.5d, 3d};
  }

  @Override
  public double getZoomLevel() {
    return drawingManager.getZoomLevel();
  }

  @Override
  public void setZoomLevel(double zoomLevel) {
    if (drawingManager.getZoomLevel() == zoomLevel) {
      return;
    }
//    LOG.info(String.format("setZoomLevel(%s)", zoomLevel));
    drawingManager.setZoomLevel(zoomLevel);
  }

  @Override
  public Cursor getCursorAt(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
    // Only change the cursor if we're not making a new component.
    if (configManager.readBoolean(HIGHLIGHT_CONTINUITY_AREA, false) || altDown)
      return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    if (instantiationManager.getComponentTypeSlot() == null) {
      // Scale point to remove zoom factor.
      Point2D scaledPoint = scalePoint(point);
      if (controlPointMap != null && !controlPointMap.isEmpty()) {
        return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
      }
      for (IDIYComponent<?> component : currentProject.getComponents()) {
        if (!isComponentLocked(component) && isComponentVisible(component)
            && !configManager.readBoolean(HIGHLIGHT_CONTINUITY_AREA, false)) {
          ComponentArea area = drawingManager.getComponentArea(component);
          if (area != null && area.getOutlineArea() != null && scaledPoint != null && area.getOutlineArea().contains(scaledPoint)) {
            return Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
          }
        }
      }
    }
    return Cursor.getDefaultCursor();
  }

  @Override
  public Dimension getCanvasDimensions(boolean useZoom, boolean includeExtraSpace) {
    return drawingManager.getCanvasDimensions(currentProject, useZoom ? drawingManager.getZoomLevel()
        : 1 / Constants.PIXEL_SIZE, includeExtraSpace);
  }

  @Override
  public Project getCurrentProject() {
    return currentProject;
  }

  @Override
  public void loadProject(Project project, boolean freshStart, String filename) {
    LOG.info(String.format("loadProject(%s, %s)", project.getTitle(), freshStart));
    this.currentProject = project;
    drawingManager.clearComponentAreaMap();
    drawingManager.clearContinuityArea();
    DrawingCache.Instance.clear();
    updateSelection(EMPTY_SELECTION);
    messageDispatcher.dispatchMessage(EventType.PROJECT_LOADED, project, freshStart, filename);
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
      loadProject(project, true, null);
      projectFileManager.startNewFile();      
    } catch (Exception e) {
      LOG.error("Could not create new file", e);
      view.showMessage(ERROR_NEW, ERROR, IView.ERROR_MESSAGE);
    }
  }

  @Override
  public void loadProjectFromFile(String fileName) {
    LOG.info(String.format("loadProjectFromFile(%s)", fileName));
    List<String> warnings = null;
    try {
      warnings = new ArrayList<String>();
      Project project = (Project) projectFileManager.deserializeProjectFromFile(fileName, warnings);
      loadProject(project, true, fileName);
      projectFileManager.fireFileStatusChanged();
      if (!warnings.isEmpty()) {
        StringBuilder builder = new StringBuilder("<html>File was opened, but there were some issues with it:<br><br>");
        for (String warning : warnings) {
          builder.append(warning);
          builder.append("<br>");
        }
        builder.append("</html");
        view.showMessage(builder.toString(), WARNING, IView.WARNING_MESSAGE);
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
      view.showMessage(errorMessage, ERROR, IView.ERROR_MESSAGE);
    }
  }

  @SuppressWarnings("unchecked")
  private void addToRecentFiles(String fileName) {
    List<String> recentFiles = (List<String>) configManager.readObject(RECENT_FILES_KEY, null);
    if (recentFiles == null)
      recentFiles = new ArrayList<String>();
    recentFiles.remove(fileName);
    recentFiles.add(0, fileName);
    while (recentFiles.size() > MAX_RECENT_FILES)
      recentFiles.remove(recentFiles.size() - 1);
    configManager.writeValue(RECENT_FILES_KEY, recentFiles);
  }

  @Override
  public boolean allowFileAction() {
    if (projectFileManager.isModified()) {
      int response =
          view.showConfirmDialog(UNSAVED_CHANGES, WARNING,
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
      return response != IView.CANCEL_OPTION && response >= 0;
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
        view.showMessage(ERROR_SAVE, ERROR,
            IView.ERROR_MESSAGE);
      }
    }
  }
  
  @Override
  public void exportToGerber(String fileNameBase, Graphics2D g2d) {
    GerberExporter.exportGerber(fileNameBase, currentProject, view, g2d, getCurrentVersionNumber().toString());    
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
        File libraryFile = new File("library");
        if (libraryFile.exists() && libraryFile.isDirectory() && !Utils.isMac()) {
          LOG.info("Loading additional library JARs");
          try {
            List<Class<?>> additionalComponentTypeClasses =
                JarScanner.getInstance().scanFolder("library", IDIYComponent.class);
            if (additionalComponentTypeClasses != null)
              componentTypeClasses.addAll(additionalComponentTypeClasses);
          } catch (Exception e) {
            LOG.warn("Could not find additional type classes", e);
          }
        }

        for (Class<?> clazz : componentTypeClasses) {
          if (!Modifier.isAbstract(clazz.getModifiers()) && IDIYComponent.class.isAssignableFrom(clazz)) {
            ComponentType componentType =
                ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) clazz);
            if (componentType == null)
              continue;
            
            // just to store in the cache
            ComponentProcessor.getInstance().extractProperties(clazz);
            
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

  @SuppressWarnings({"unchecked"})
  private boolean isComponentVisible(IDIYComponent<?> component) {
    ComponentType componentType =
        ComponentProcessor.getInstance().extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass());
    // for internal types
    if (componentType == null)
      return true;
    return !currentProject.getHiddenLayers().contains((int) Math.round(componentType.getZOrder()));
  }

  @Override
  public void draw(Graphics2D g2d, Set<DrawOption> drawOptions, final IComponentFilter filter, java.lang.Double externalZoom, Rectangle2D visibleRect) {
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
    IComponentFilter newFiler = new IComponentFilter() {

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
                    instantiationManager.getPotentialControlPoint()), componentSlotToDraw, dragInProgress, externalZoom,
                visibleRect);
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
  public List<IDIYComponent<?>> findComponentsAtScaled(Point2D point, boolean includeLocked) {
    List<IDIYComponent<?>> components = drawingManager.findComponentsAt(point, currentProject);
    Iterator<IDIYComponent<?>> iterator = components.iterator();
    while (iterator.hasNext()) {
      IDIYComponent<?> component = iterator.next();
      if ((includeLocked ? isComponentLayerLocked(component) : isComponentLocked(component)) || !isComponentVisible(component)) {
        iterator.remove();
      }
    }
    return components;
  }

  @Override
  public List<IDIYComponent<?>> findComponentsAt(Point2D point, boolean includeLocked) {
    Point2D scaledPoint = scalePoint(point);
    List<IDIYComponent<?>> components = findComponentsAtScaled(scaledPoint, includeLocked);
    return components;
  }

  @Override
  public void mouseClicked(Point point, int button, boolean ctrlDown, boolean shiftDown, boolean altDown, int clickCount) {
    LOG.debug(String.format("mouseClicked(%s, %s, %s, %s, %s)", point, button, ctrlDown, shiftDown, altDown));

    // record a test step if needed
    if (test != null) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("point", point);
      params.put("button", button);
      params.put("ctrlDown", ctrlDown);
      params.put("shiftDown", shiftDown);
      params.put("altDown", altDown);
      params.put("clickCount", clickCount);
      test.addStep(DIYTest.MOUSE_CLICKED, params);
    }    
      
    Point2D scaledPoint = scalePoint(point);
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
        String[] model = instantiationManager.getModel();
        Project oldProject = currentProject.clone();
        switch (componentTypeSlot.getCreationMethod()) {
          case SINGLE_CLICK:
            try {
              if (isSnapToGrid()) {
                CalcUtils.snapPointToGrid(scaledPoint, currentProject.getGridSpacing());
              }
              List<IDIYComponent<?>> componentSlot = instantiationManager.getComponentSlot();
              List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(componentSlot);
              addComponents(componentSlot);
              // group components if there's more than one, e.g. building blocks, but not clipboard
              // contents
              if (componentSlot.size() > 1 && !componentTypeSlot.getName().toLowerCase().contains("clipboard")) {
                this.currentProject.getGroups().add(new HashSet<IDIYComponent<?>>(componentSlot));
              }
              
              notifyProjectModifiedIfNeeded(oldProject, "Add " + componentTypeSlot.getName(), true, true);

              updateSelection(newSelection);
            } catch (Exception e) {
              LOG.error("Error instatiating component of type: " + componentTypeSlot.getInstanceClass().getName(), e);
            }

            if (componentTypeSlot.isAutoEdit()
                && configManager.readBoolean(IPlugInPort.AUTO_EDIT_KEY, false)) {
              editSelection();
            }
            if (configManager.readBoolean(IPlugInPort.CONTINUOUS_CREATION_KEY, false)) {
              setNewComponentTypeSlot(componentTypeSlot, template, model, false);
            } else {
              setNewComponentTypeSlot(null, null, null, false);
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
                view.showMessage(ERROR_CREATE, ERROR, IView.ERROR_MESSAGE);
                LOG.error("Could not create component", e);
              }
              
              notifyProjectModifiedIfNeeded(oldProject, "Add " + componentTypeSlot.getName(), true, true);
              
              messageDispatcher.dispatchMessage(EventType.SLOT_CHANGED, componentTypeSlot,
                  instantiationManager.getFirstControlPoint());
            } else {
              // On the second click, add the component to the
              // project.
              addPendingComponentsToProject(scaledPoint, componentTypeSlot, template, model, oldProject);
            }
            break;
          default:
            LOG.error("Unknown creation method: " + componentTypeSlot.getCreationMethod());
        }
      } else if (configManager.readBoolean(HIGHLIGHT_CONTINUITY_AREA, false) || altDown) {
        drawingManager.findContinuityAreaAtPoint(scaledPoint);
        messageDispatcher.dispatchMessage(EventType.REPAINT);
      } else {
        List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(selectedComponents);
        List<IDIYComponent<?>> components = findComponentsAtScaled(scaledPoint, false);
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
        drawingManager.clearContinuityArea();
        updateSelection(newSelection);
        // messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED,
        // selectedComponents);
        // messageDispatcher.dispatchMessage(EventType.SELECTION_SIZE_CHANGED,
        // calculateSelectionDimension());
        messageDispatcher.dispatchMessage(EventType.REPAINT);
      }
    }
  }

  private void notifyProjectModifiedIfNeeded(Project oldProject, String action, boolean clearContinuityArea, boolean repaint) {
    // Notify the listeners.
    if (oldProject == null || !currentProject.equals(oldProject)) {
      messageDispatcher.dispatchMessage(EventType.PROJECT_MODIFIED, oldProject, currentProject, action);
      if (clearContinuityArea) {
        drawingManager.clearContinuityArea();
      }
      projectFileManager.notifyFileChange();
      if (repaint) {
        messageDispatcher.dispatchMessage(EventType.REPAINT);
      }
    }
  }

  private void addPendingComponentsToProject(Point2D scaledPoint, ComponentType componentTypeSlot, Template template, String[] model,
      Project oldProject) {
    List<IDIYComponent<?>> componentSlot = instantiationManager.getComponentSlot();
    Point2D firstPoint = componentSlot.get(0).getControlPoint(0);
    // don't allow to create component with the same points
    if (scaledPoint == null || scaledPoint.equals(firstPoint))
      return;
    // componentSlot.get(0).setControlPoint(scaledPoint, 1);
    List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
    for (IDIYComponent<?> component : componentSlot) {
      addComponent(component, true);
      // Select the new component if it's not locked and invisible.
      if (!isComponentLocked(component) && isComponentVisible(component)) {
        newSelection.add(component);
      }
    }

    notifyProjectModifiedIfNeeded(oldProject, "Add " + componentTypeSlot.getName(), true, true);
    
    updateSelection(newSelection);
    messageDispatcher.dispatchMessage(EventType.REPAINT);

    if (componentTypeSlot.isAutoEdit()
        && configManager.readBoolean(IPlugInPort.AUTO_EDIT_KEY, false)) {
      editSelection();
    }
    if (configManager.readBoolean(IPlugInPort.CONTINUOUS_CREATION_KEY, false)) {
      setNewComponentTypeSlot(componentTypeSlot, template, model, false);
    } else {
      setNewComponentTypeSlot(null, null, null, false);
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

    String snapTo = configManager.readString(IPlugInPort.SNAP_TO_KEY, IPlugInPort.SNAP_TO_DEFAULT);
    if (shiftDown) {
        snapTo = IPlugInPort.SNAP_TO_NONE;
    }
    boolean snapToGrid = snapTo.equalsIgnoreCase(IPlugInPort.SNAP_TO_GRID);
    boolean snapToObjects = snapTo.equalsIgnoreCase(IPlugInPort.SNAP_TO_COMPONENTS);

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
      
      notifyProjectModifiedIfNeeded(oldProject, "Rotate Selection", true, true);
     
      return true;
    }

    // Expand control points to include all stuck components.
    boolean sticky = configManager.readBoolean(IPlugInPort.STICKY_POINTS_KEY, true);
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
    moveComponents(controlPointMap, dx, dy, snapToGrid, snapToObjects);
    notifyProjectModifiedIfNeeded(oldProject, "Move Selection", true, true);
    return true;
  }

  @Override
  public void editSelection() {
    List<PropertyWrapper> properties = getMutualSelectionProperties();
    if (properties != null && !properties.isEmpty()) {
      Set<PropertyWrapper> defaultedProperties = new HashSet<PropertyWrapper>();
      boolean edited = view.editProperties(properties, defaultedProperties, LangUtil.translate("Edit Selection"));
      if (edited) {
        try {
          applyPropertiesToSelection(properties);
        } catch (Exception e1) {
          view.showMessage(ERROR_EDIT, ERROR,
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

  @Override
  public void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown) {
    if (point == null)
      return;
    
    // record a test step if needed
    if (test != null) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("point", point);
      params.put("ctrlDown", ctrlDown);
      params.put("shiftDown", shiftDown);
      params.put("altDown", altDown);
      test.addStep(DIYTest.MOUSE_MOVED, params);
    }      

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
      } else if (isSnapToObjects()) {
        CalcUtils.snapPointToObjects(previousScaledPoint, currentProject.getGridSpacing(), null, currentProject.getComponents());
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
        for (int pointIndex = 0; pointIndex < component.getControlPointCount(); pointIndex++) {
          Point2D controlPoint = component.getControlPoint(pointIndex);
          // Only consider selected components that are not grouped.
          if (selectedComponents.contains(component) && component.canPointMoveFreely(pointIndex)
              && findAllGroupedComponents(component).size() == 1) {
            try {
              if (previousScaledPoint.distance(controlPoint) < DrawingManager.CONTROL_POINT_SIZE && 
                  component.getControlPointVisibilityPolicy(pointIndex) != VisibilityPolicy.NEVER) {
                Set<Integer> indices = new HashSet<Integer>();
                indices.add(pointIndex);
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

    Point2D inPoint =
        new Point2D.Double(1.0d * previousScaledPoint.getX() / Constants.PIXELS_PER_INCH, 1.0d * previousScaledPoint.getY()
            / Constants.PIXELS_PER_INCH);
    Point2D mmPoint =
        new Point2D.Double(inPoint.getX() * SizeUnit.in.getFactor() / SizeUnit.cm.getFactor() * 10d, inPoint.getY()
            * SizeUnit.in.getFactor() / SizeUnit.cm.getFactor() * 10d);

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
        LOG.debug("Area is null for " + c.getName() + " of type " + c.getClass().getName());
    }

    if (configManager.readBoolean(EXTRA_SPACE_KEY, true)) {
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
    moveComponents(controlPointMap, dx, dy, false, false);
    notifyProjectModifiedIfNeeded(oldProject, "Move Selection", true, true);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void selectMatching(String criteria) {
    Set<IDIYComponent<?>> matching = new HashSet<IDIYComponent<?>>();
    String regex = ".*" + criteria.toLowerCase() + ".*";
    for (IDIYComponent<?> c : currentProject.getComponents()) {
      ComponentType type = ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());
      if ((c.getName() != null && c.getName().toLowerCase().matches(regex)) || 
          (c.getValueForDisplay() != null && c.getValueForDisplay().toLowerCase().matches(regex)) ||
          (type != null && type.getName().toLowerCase().matches(regex)))
        matching.add(c);
    }
    updateSelection(matching);    
    messageDispatcher.dispatchMessage(EventType.REPAINT, true);
    if (matching.size() > 0)
      messageDispatcher.dispatchMessage(EventType.SCROLL_TO, getSelectionBounds(true));
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
    
    // record a test step if needed
    if (test != null) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("point", point);
      params.put("dragAction", dragAction);
      params.put("forceSelectionRect", forceSelectionRect);
      test.addStep(DIYTest.DRAG_START, params);
    }    
    
    if (instantiationManager.getComponentTypeSlot() != null) {
      LOG.debug("Cannot start drag because a new component is being created.");
      mouseClicked(point, IPlugInPort.BUTTON1, dragAction == DnDConstants.ACTION_COPY,
          dragAction == DnDConstants.ACTION_LINK, dragAction == DnDConstants.ACTION_MOVE, 1);
      return;
    }
    if (configManager.readBoolean(HIGHLIGHT_CONTINUITY_AREA, false)) {
      LOG.debug("Cannot start drag in hightlight continuity mode.");
      return;
    }
    this.dragInProgress = true;
    this.dragAction = dragAction;
    this.preDragProject = currentProject.clone();
    Point2D scaledPoint = scalePoint(point);
    this.previousDragPoint = scaledPoint;
    List<IDIYComponent<?>> components = forceSelectionRect ? null : findComponentsAtScaled(scaledPoint, false);
    if (controlPointMap != null && !this.controlPointMap.isEmpty()) {
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
      boolean sticky = configManager.readBoolean(IPlugInPort.STICKY_POINTS_KEY, true);
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
  private void includeStuckComponents(Map<IDIYComponent<?>, Set<Integer>> controlPointMap) {
    int oldSize = controlPointMap.size();
    LOG.trace("Expanding selected component map");
    for (IDIYComponent<?> component : currentProject.getComponents()) {
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
                Point2D firstPoint = component.getControlPoint(i);
                if (entry.getKey().isControlPointSticky(j)) {
                  Point2D secondPoint = entry.getKey().getControlPoint(j);
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
              if (component.canPointMoveFreely(i)) {
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
    String snapTo = configManager.readString(IPlugInPort.SNAP_TO_KEY, IPlugInPort.SNAP_TO_DEFAULT);    
    if (this.dragAction == IPlugInPort.DND_TOGGLE_SNAP)
      return false;
    return snapTo.equalsIgnoreCase(IPlugInPort.SNAP_TO_GRID);
  }
  
  private boolean isSnapToObjects() {
    String snapTo = configManager.readString(IPlugInPort.SNAP_TO_KEY, IPlugInPort.SNAP_TO_DEFAULT);    
    if (this.dragAction == IPlugInPort.DND_TOGGLE_SNAP)
      return false;
    return snapTo.equalsIgnoreCase(IPlugInPort.SNAP_TO_COMPONENTS);
  }

  @Override
  public boolean dragOver(Point point) {
    // record a test step if needed
    if (test != null) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("point", point);
      test.addStep(DIYTest.DRAG_OVER, params);
    }    
    
    if (point == null || configManager.readBoolean(HIGHLIGHT_CONTINUITY_AREA, false)) {
      return false;
    }
    Point2D scaledPoint = scalePoint(point);
    if (controlPointMap != null && !controlPointMap.isEmpty()) {
      // We're dragging control point(s).
      int dx = (int) (scaledPoint.getX() - previousDragPoint.getX());
      int dy = (int) (scaledPoint.getY() - previousDragPoint.getY());

      Point2D actualD = moveComponents(this.controlPointMap, dx, dy, isSnapToGrid(), isSnapToObjects());
      if (actualD == null)
        return true;

      previousDragPoint.setLocation(previousDragPoint.getX() + actualD.getX(), previousDragPoint.getY() + actualD.getY());
    } else if (selectedComponents.isEmpty() && instantiationManager.getComponentTypeSlot() == null
        && previousDragPoint != null) {
      // If there's no selection, the only thing to do is update the
      // selection rectangle and refresh.
      Rectangle oldSelectionRect = selectionRect == null ? null : new Rectangle(selectionRect);
      this.selectionRect = Utils.createRectangle(new Point((int)scaledPoint.getX(), (int)scaledPoint.getY()), 
          new Point((int)previousDragPoint.getX(), (int)previousDragPoint.getY()));
      if (selectionRect.equals(oldSelectionRect)) {
        return true;
      }
      // messageDispatcher.dispatchMessage(EventType.SELECTION_RECT_CHANGED,
      // selectionRect);
    } else if (instantiationManager.getComponentSlot() != null) {
      this.previousScaledPoint = scalePoint(point);
      instantiationManager.updateSingleClick(previousScaledPoint, isSnapToGrid(), currentProject.getGridSpacing());
    }
    messageDispatcher.dispatchMessage(EventType.REPAINT);
    return true;
  }

  private Point2D moveComponents(Map<IDIYComponent<?>, Set<Integer>> controlPointMap, int dx, int dy, boolean snapToGrid, boolean snapToObjects) {
    // After we make the transfer and snap to grid, calculate actual dx
    // and dy. We'll use them to translate the previous drag point.
    double actualDx = 0;
    double actualDy = 0;
    // For each component, do a simulation of the move to see if any of
    // them will overlap or go out of bounds.

    boolean useExtraSpace = configManager.readBoolean(EXTRA_SPACE_KEY, true);
    Dimension d = drawingManager.getCanvasDimensions(currentProject, 1d, useExtraSpace);
    double extraSpace = useExtraSpace ? drawingManager.getExtraSpace(currentProject) : 0;
    
    boolean isRigid = true;
    
    List<Point2D> points = new ArrayList<Point2D>();    
    for (Map.Entry<IDIYComponent<?>, Set<Integer>> entry : controlPointMap.entrySet()) {
      for (Integer i : entry.getValue())
      {
        Point2D p = entry.getKey().getControlPoint(i);
        if (entry.getKey().canPointMoveFreely(i))
          isRigid = false;
        points.add(p);
      }
    }

    Point2D firstPoint = points.iterator().next();
    if (points.size() == 1 || points.stream().allMatch((x) -> x.equals(firstPoint)) || (controlPointMap.size() == 1 && isRigid)) {    
      double avgX = firstPoint.getX();//points.stream().mapToDouble((x) -> x.getX()).average().getAsDouble();
      double avgY = firstPoint.getY();//points.stream().mapToDouble((x) -> x.getY()).average().getAsDouble();      
      
      Point2D testPoint = new Point2D.Double(avgX + dx, avgY + dy);      
      if (snapToGrid) {
        CalcUtils.snapPointToGrid(testPoint, currentProject.getGridSpacing());
      } else if (snapToObjects && controlPointMap.size() == 1) {
        CalcUtils.snapPointToObjects(testPoint, currentProject.getGridSpacing(), controlPointMap.entrySet().iterator().next().getKey(), currentProject.getComponents());
      }

      actualDx = testPoint.getX() - avgX;
      actualDy = testPoint.getY() - avgY;
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
      // check if a component already has overlapping points because of a previous error, in that case skip it
      boolean skip = false;
      IDIYComponent<?> component = entry.getKey();
      for (int i = 0; i < component.getControlPointCount() - 1 && !skip; i++)
        for (int j = i + 1; j < component.getControlPointCount() && !skip; j++)
          if (CalcUtils.pointsMatch(component.getControlPoint(i), component.getControlPoint(j), DrawingManager.CONTROL_POINT_SIZE / 2.0))
            skip = true;
      
      if (skip)
        continue;
            
      Point2D[] controlPoints = new Point2D[component.getControlPointCount()];
      for (int index = 0; index < component.getControlPointCount(); index++) {
        Point2D p = component.getControlPoint(index);
        controlPoints[index] = new Point2D.Double(p.getX(), p.getY());
        // When the first point is moved, calculate how much it
        // actually moved after snapping.
        if (entry.getValue().contains(index)) {
          controlPoints[index].setLocation(controlPoints[index].getX() + actualDx + extraSpace, controlPoints[index].getY() + actualDy + extraSpace);          
          if (controlPoints[index].getX() < 0 || controlPoints[index].getY() < 0 || controlPoints[index].getX() > d.width
              || controlPoints[index].getY() > d.height) {
            // At least one control point went out of bounds.
            return null;
          }
        } else {
          // for non-affected points just add extra space so we are comparing apples with apples later
          controlPoints[index].setLocation(controlPoints[index].getX() + extraSpace, controlPoints[index].getY() + extraSpace);    
        }
        // For control points that may overlap, just write null,
        // we'll ignore them later.
        if (component.canControlPointOverlap(index)) {
          controlPoints[index] = null;
        }
      }

      for (int i = 0; i < controlPoints.length - 1; i++) {
        for (int j = i + 1; j < controlPoints.length; j++) {
          if (controlPoints[i] != null && controlPoints[j] != null && 
              CalcUtils.pointsMatch(controlPoints[i], controlPoints[j], DrawingManager.CONTROL_POINT_SIZE / 2.0)) {
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
      for (Integer index : entry.getValue()) {
        Point2D oldP = c.getControlPoint(index);
        Point2D p = new Point2D.Double(oldP.getX() + actualDx, oldP.getY() + actualDy);        
        c.setControlPoint(p, index);
      }
    }
    
    // Remove from area map.    
    drawingManager.clearComponentAreaMap();
    drawingManager.clearContinuityArea();
    
    return new Point2D.Double(actualDx, actualDy);
  }

  @Override
  public void rotateSelection(int direction) {
    if (!selectedComponents.isEmpty()) {
      LOG.trace("Rotating selected components");
      Project oldProject = currentProject.clone();
      rotateComponents(this.selectedComponents, direction, isSnapToGrid());
      
      notifyProjectModifiedIfNeeded(oldProject,  "Rotate Selection", true, true);
    }
  }

  /**
   * 
   * @param direction 1 for clockwise, -1 for counter-clockwise
   */
  @SuppressWarnings("unchecked")
  private void rotateComponents(Collection<IDIYComponent<?>> components, int direction, boolean snapToGrid) {
    Point2D center = getCenterOf(components, snapToGrid);

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
      if (view.showConfirmDialog(ROTATE_CHANGE,
          ROTATE_SELECTION, IView.YES_NO_OPTION, IView.QUESTION_MESSAGE) != IView.YES_OPTION)
        return;

    for (IDIYComponent<?> component : selectedComponents) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());
      if (type.getTransformer() != null && type.getTransformer().canRotate(component)) {        
        type.getTransformer().rotate(component, center, direction);
      }
    }
    
    // Remove from area map.    
    drawingManager.clearComponentAreaMap();
    drawingManager.clearContinuityArea();
  }

  @Override
  public void mirrorSelection(int direction) {
    if (!selectedComponents.isEmpty()) {
      LOG.trace("Mirroring selected components");
      Project oldProject = currentProject.clone();

      mirrorComponents(selectedComponents, direction, isSnapToGrid());

      notifyProjectModifiedIfNeeded(oldProject, "Mirror Selection", true, true);
    }
  }

  @SuppressWarnings("unchecked")
  private void mirrorComponents(Collection<IDIYComponent<?>> components, int direction, boolean snapToGrid) {
    Point2D center = getCenterOf(components, snapToGrid);

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
      if (view.showConfirmDialog(CANNOT_MIRROR,
          MIRROR_SELECTION, IView.YES_NO_OPTION, IView.QUESTION_MESSAGE) != IView.YES_OPTION)
        return;

    if (changesCircuit)
      if (view.showConfirmDialog(MIRRORING_CHANGE,
          MIRROR_SELECTION, IView.YES_NO_OPTION, IView.QUESTION_MESSAGE) != IView.YES_OPTION)
        return;

    for (IDIYComponent<?> component : components) {
      ComponentType type =
          ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass());      
      if (type.getTransformer() != null && type.getTransformer().canMirror(component)) {
        type.getTransformer().mirror(component, center, direction);
      }
    }
    
    // Remove from area map.    
    drawingManager.clearComponentAreaMap();
    drawingManager.clearContinuityArea();
  }

  private Point2D getCenterOf(Collection<IDIYComponent<?>> components, boolean snapToGrid) {
    // Determine center of rotation
    double minX = Integer.MAX_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double maxY = Integer.MIN_VALUE;
    for (IDIYComponent<?> component : components) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point2D p = component.getControlPoint(i);
        if (minX > p.getX()) {
          minX = p.getX();
        }
        if (maxX < p.getX()) {
          maxX = p.getX();
        }
        if (minY > p.getY()) {
          minY = p.getY();
        }
        if (maxY < p.getY()) {
          maxY = p.getY();
        }
      }
    }
    double centerX = (maxX + minX) / 2;
    double centerY = (maxY + minY) / 2;

    if (snapToGrid) {
      CalcUtils.roundToGrid(centerX, this.currentProject.getGridSpacing());
      CalcUtils.roundToGrid(centerY, this.currentProject.getGridSpacing());
    }

    return new Point2D.Double(centerX, centerY);
  }

  @Override
  public void dragEnded(Point point) {
    LOG.debug(String.format("dragEnded(%s)", point));
    
    // record a test step if needed
    if (test != null) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("point", point);
      params.put("dragAction", dragAction);
      test.addStep(DIYTest.DRAG_END, params);
    }

    Point2D scaledPoint = scalePoint(point);

    if (!dragInProgress && instantiationManager.getComponentSlot() == null) {
      return;
    }

    if (selectedComponents.isEmpty()) {
      // If there's no selection finalize selectionRect and see which
      // components intersect with it.
      if (scaledPoint != null && previousDragPoint != null) {
        this.selectionRect = Utils.createRectangle(new Point((int)scaledPoint.getX(), (int)scaledPoint.getY()), 
            new Point((int)previousDragPoint.getX(), (int)previousDragPoint.getY()));;
      }
      List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
      if (!configManager.readBoolean(HIGHLIGHT_CONTINUITY_AREA, false))
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
      addPendingComponentsToProject(scaledPoint, instantiationManager.getComponentTypeSlot(), null, null, preDragProject);
    } else {
      updateSelection(selectedComponents);
    }
    // There is selection, so we need to finalize the drag&drop
    // operation.
    dragInProgress = false;
  }

  @Override
  public void pasteComponents(ComponentTransferable componentTransferable, boolean autoGroup, boolean assignNewNames) {
    LOG.info(String.format("pasteComponents(%s, %s, %s)", componentTransferable, autoGroup, assignNewNames));
    instantiationManager.pasteComponents(componentTransferable, this.previousScaledPoint, isSnapToGrid(),
        currentProject.getGridSpacing(), autoGroup, this.currentProject, assignNewNames);
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
          Point2D p = component.getControlPoint(i);
          Point2D newPoint = new Point2D.Double(p.getX() + grid, p.getY() + grid);
          cloned.setControlPoint(newPoint, i);
        }
        currentProject.getComponents().add(cloned);
      } catch (Exception e) {
      }
    }

    notifyProjectModifiedIfNeeded(oldProject, "Duplicate", true, true);
    updateSelection(newSelection);
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
    drawingManager.clearComponentAreaMap();
    drawingManager.clearContinuityArea();
    
    currentProject.getComponents().removeAll(selectedComponents);
    DrawingCache.Instance.invalidate(selectedComponents);
    
    notifyProjectModifiedIfNeeded(oldProject, "Delete", true, true);
    updateSelection(EMPTY_SELECTION);
  }

  @Override
  public void setSelectionDefaultPropertyValue(String propertyName, Object value) {
    LOG.info(String.format("setSelectionDefaultPropertyValue(%s, %s)", propertyName, value));
    for (IDIYComponent<?> component : selectedComponents) {
      String className = component.getClass().getName();
      LOG.debug("Default property value set for " + className + ":" + propertyName);
      configManager.writeValue(DEFAULTS_KEY_PREFIX + className + ":" + propertyName, value);
    }
  }

  @Override
  public void setDefaultPropertyValue(Class<?> clazz, String propertyName, Object value) {
    LOG.info(String.format("setProjectDefaultPropertyValue(%s, %s, %s)", clazz.getName(), propertyName, value));
    LOG.debug("Default property value set for " + Project.class.getName() + ":" + propertyName);
    configManager.writeValue(DEFAULTS_KEY_PREFIX + clazz.getName() + ":" + propertyName, value);
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
    notifyProjectModifiedIfNeeded(oldProject, "Group", false, true);
  }

  @Override
  public void ungroupSelectedComponents() {
    LOG.info("ungroupSelectedComponents()");
    Project oldProject = currentProject.clone();
    ungroupComponents(selectedComponents);
    // Notify the listeners.
    notifyProjectModifiedIfNeeded(oldProject, "Ungroup", false, true);
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
    messageDispatcher.dispatchMessage(EventType.LAYER_STATE_CHANGED, currentProject.getLockedLayers());
    
    notifyProjectModifiedIfNeeded(oldProject, locked ? "Lock Layer" : "Unlock Layer", false, true);
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
    messageDispatcher.dispatchMessage(EventType.LAYER_VISIBILITY_CHANGED, currentProject.getHiddenLayers());
    
    notifyProjectModifiedIfNeeded(oldProject,  visible ? "Show Layer" : "Hide Layer", false, true);
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
        return Integer.valueOf(currentProject.getComponents().indexOf(o2)).compareTo(currentProject.getComponents()
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
                            REACHED_BOTTOM,
                            SEND_SELECTION_TO_BACK, IView.YES_NO_OPTION, IView.QUESTION_MESSAGE)) != IView.YES_OPTION)
              break;
          }
          Collections.swap(currentProject.getComponents(), index, index - 1);
          index--;
        }
    }
    notifyProjectModifiedIfNeeded(oldProject, "Send to Back", false, true);
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
        return Integer.valueOf(currentProject.getComponents().indexOf(o1)).compareTo(currentProject.getComponents()
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
                            REACHED_TOP,
                            BRING_SELECTION_TO_FRONT, IView.YES_NO_OPTION, IView.QUESTION_MESSAGE)) != IView.YES_OPTION)
              break;
          }
          Collections.swap(currentProject.getComponents(), index, index + 1);
          index++;
        }
    }
    notifyProjectModifiedIfNeeded(oldProject, "Bring to Front", false, true);
  }
  
  @Override
  public void moveSelectionToZIndex(int zIndex) {
    LOG.info(String.format("moveSelectionToZIndex(%d)", zIndex));
    Project oldProject = currentProject.clone();

    // sort the selection in Z-order
    List<IDIYComponent<?>> selection = new ArrayList<IDIYComponent<?>>(selectedComponents);
    Collections.sort(selection, new Comparator<IDIYComponent<?>>() {

      @Override
      public int compare(IDIYComponent<?> o1, IDIYComponent<?> o2) {
        return Integer.valueOf(currentProject.getComponents().indexOf(o1))
            .compareTo(currentProject.getComponents().indexOf(o2));
      }
    });

    for (IDIYComponent<?> component : selection) {
      int index = currentProject.getComponents().indexOf(component);
      if (index < 0) {
        LOG.warn("Component not found in the project: " + component.getName());
      } else {
        if (index < zIndex)
          zIndex--;

        currentProject.getComponents().remove(index);
        currentProject.getComponents().add(zIndex, component);
      }
    }
    notifyProjectModifiedIfNeeded(oldProject, "Change Z-order", false, true);
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

    notifyProjectModifiedIfNeeded(oldProject,  "Renumber selection", false, true);
  }

  public void setSelection(Collection<IDIYComponent<?>> newSelection, boolean panToSelection) {
    this.updateSelection(newSelection);
    messageDispatcher.dispatchMessage(EventType.REPAINT, panToSelection);
  }
 
  private void updateSelection(Collection<IDIYComponent<?>> newSelection) {
    this.selectedComponents = new HashSet<IDIYComponent<?>>(newSelection);
    Map<IDIYComponent<?>, Set<Integer>> controlPointMap = new HashMap<IDIYComponent<?>, Set<Integer>>();
    for (IDIYComponent<?> component : selectedComponents) {
      Set<Integer> indices = new HashSet<Integer>();
      for (int i = 0; i < component.getControlPointCount(); i++) {
        indices.add(i);
      }
      controlPointMap.put(component, indices);
    }
    if (configManager.readBoolean(IPlugInPort.STICKY_POINTS_KEY, true)) {
      includeStuckComponents(controlPointMap);
    }
    messageDispatcher.dispatchMessage(EventType.SELECTION_CHANGED, selectedComponents, controlPointMap.keySet());    
  }
  
  private static boolean useNetlistForExpandSelection = false;

  @SuppressWarnings("unchecked")
  @Override
  public void expandSelection(ExpansionMode expansionMode) {
    LOG.info(String.format("expandSelection(%s)", expansionMode));    
    List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>(this.selectedComponents);
    // experimental mode using netlist to expand selection. Works fine but doesn't include any connectivity components 
    // like traces and wires, so it's not ideal
    if (useNetlistForExpandSelection) {
      List<Netlist> netlists = null;
      try {
        netlists = extractNetlists(false);
      } catch (NetlistException e1) {
        // should never happen
        LOG.warn("Unexpected netlist exception", e1);
      }
      
      if (netlists == null)
        return;
      
      List<Set<IDIYComponent<?>>> allGroups = NetlistAnalyzer.extractComponentGroups(netlists);
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
        // no need to consider it, it's already in the selection
        if (newSelection.contains(component))
          continue;
        // construct a list of component groups that contain the current component
        List<Set<IDIYComponent<?>>> componentGroups = new ArrayList<Set<IDIYComponent<?>>>();
        for (Set<IDIYComponent<?>> e : allGroups)
          if (e.contains(component))
            componentGroups.add(e);
        if (componentGroups.isEmpty())
          continue;
        // Skip already selected components or ones that cannot be stuck to
        // other components.
        boolean matches = false;
        outer: for (IDIYComponent<?> selectedComponent : this.selectedComponents) {
          // try to find the selectedComponent in one of the groups
          for (Set<IDIYComponent<?>> s : componentGroups)
            if (s.contains(selectedComponent)) {
              matches = true;
              break outer;
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
    } else {
      Set<String> selectedNamePrefixes = new HashSet<String>();
      if (expansionMode == ExpansionMode.SAME_TYPE) {
        // find prefixes of all selected components to bundle similar component types together
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
        if (newSelection.contains(component) || !(ComponentProcessor.hasStickyPoint(component) || (area != null
            && area.getContinuityPositiveAreas() == null)))
          continue;
        boolean matches = false;
        for (IDIYComponent<?> selectedComponent : this.selectedComponents) {
          ComponentArea selectedArea = drawingManager.getComponentArea(selectedComponent);
          if (ComponentProcessor.componentPointsTouch(component, selectedComponent)) {
            matches = true;
            break;
          }
          if (selectedArea == null || selectedArea.getContinuityPositiveAreas() == null)
            continue;
          
          // do a rough check, if the outlines don't intersect there's no chance of a match
          // so don't waste time doing the precise check
          if (area.getOutlineArea() != null && selectedArea.getOutlineArea() != null && 
              !area.getOutlineArea().getBounds().intersects(selectedArea.getOutlineArea().getBounds()))
            continue;          
          
          // create a unified continuity area for both components
          Area totalArea = new Area();
          for (Area a : area.getContinuityPositiveAreas())
            totalArea.add(a);
          if (area.getContinuityNegativeAreas() != null)
            for(Area a : area.getContinuityNegativeAreas())
              totalArea.subtract(a);
          
          Area totalSelectedArea = new Area();
          for (Area a : selectedArea.getContinuityPositiveAreas())
            totalSelectedArea.add(a);
          if (selectedArea.getContinuityNegativeAreas() != null)
            for(Area a : selectedArea.getContinuityNegativeAreas())
              totalSelectedArea.subtract(a);
          
          // now check the intersection, if there's something we have a match
          Area intersection = new Area(totalArea);
          intersection.intersect(totalSelectedArea);
          if (!intersection.isEmpty()) {
            matches = true;
            break;
          }
          
          // check if one of the sticky points of one components falls on the continuity area of the other
          for (int i = 0; i < component.getControlPointCount(); i++)
            if (component.isControlPointSticky(i) && totalSelectedArea.contains(component.getControlPoint(i))) {
              matches = true;
              break;
            }
          for (int i = 0; i < selectedComponent.getControlPointCount(); i++)
            if (selectedComponent.isControlPointSticky(i) && totalArea.contains(selectedComponent.getControlPoint(i))) {
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

  /**
   * Adds a component to the project taking z-order into account.
   * 
   * @param component
   */
  @SuppressWarnings("unchecked")
  private void addComponent(IDIYComponent<?> component, boolean allowAutoCreate) {
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

    if (allowAutoCreate) {
      // Check if we should auto-create something.
      for (IAutoCreator creator : ReflectionUtils.getAutoCreators()) {
        List<IDIYComponent<?>> newComponents = creator.createIfNeeded(component);
        if (newComponents != null) {
          for (IDIYComponent<?> c : newComponents)
            addComponent(c, false);
        }
      }
    }
  }
  
  /**
   * Adds components to the project taking z-order into account. It places the first component according to the z-index rules 
   * and then places all the others ensuring they are on top of the first one
   * 
   * @param components
   */
  @SuppressWarnings("unchecked")
  private void addComponents(List<IDIYComponent<?>> components) {
    if (components.isEmpty())
      return;
    int maxIndex = 0;
    for (IDIYComponent<?> component : components) {
      int index = currentProject.getComponents().size();
      while (index > maxIndex && ComponentProcessor.getInstance()
          .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass())
          .getZOrder() < ComponentProcessor.getInstance()
              .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) currentProject
                  .getComponents().get(index - 1).getClass())
              .getZOrder()) {
        index--;
      }
      
      if (index < currentProject.getComponents().size()) {
        currentProject.getComponents().add(index, component);
        if (index > maxIndex) {
          maxIndex = index;
        }
      } else {
        currentProject.getComponents().add(component);
        maxIndex = currentProject.getComponents().size();
      }
    }

    for (IDIYComponent<?> component : components) {
      // Check if we should auto-create something.
      for (IAutoCreator creator : ReflectionUtils.getAutoCreators()) {
        List<IDIYComponent<?>> newComponents = creator.createIfNeeded(component);
        if (newComponents != null) {
          for (IDIYComponent<?> c : newComponents)
            addComponent(c, false);
        }
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
      view.showMessage("Could not apply changes to the selection. Check the log for details.", ERROR,
          IView.ERROR_MESSAGE);
    } finally {
      notifyProjectModifiedIfNeeded(oldProject, "Edit Selection", true, true);
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
      view.showMessage(APPLY_ERROR, ERROR, IView.ERROR_MESSAGE);
    } finally {
      notifyProjectModifiedIfNeeded(oldProject, "Edit Project", true, true);
      drawingManager.fireZoomChanged();
    }
  }
  
  @Override
  public void applyEditor(IProjectEditor editor) {
    LOG.debug(String.format("applyEditor(%s)", editor.getEditAction()));
    Project oldProject = currentProject.clone();
    try {
      Set<IDIYComponent<?>> newSelection = editor.edit(currentProject, selectedComponents);
      if (newSelection != null)      
        updateSelection(newSelection);      
    } catch (Exception e) {
      LOG.error("Could not apply editor", e);
      view.showMessage("Could not apply " + editor.getEditAction() + ". Check the log for details.", ERROR, IView.ERROR_MESSAGE);
    } finally {
      notifyProjectModifiedIfNeeded(oldProject, editor.getEditAction(), true, true);
      drawingManager.fireZoomChanged();
    }
  }
  
  @Override
  public void applyModelToSelection(String[] model) {
    Project oldProject = currentProject.clone();
    try {
      selectedComponents.forEach(component -> {
        ((IDatasheetSupport) component).applyModel(model);
      });
    } catch (Exception e) {
      LOG.warn("Could not apply datasheet to component", e);
      view.showMessage(APPLY_ERROR, ERROR, IView.ERROR_MESSAGE);
    } finally {
      notifyProjectModifiedIfNeeded(oldProject, "Edit Project", true, true);
      drawingManager.fireZoomChanged();
    }
  }

  @Override
  public ComponentType getNewComponentTypeSlot() {
    return instantiationManager.getComponentTypeSlot();
  }

  @Override
  public void setNewComponentTypeSlot(ComponentType componentType, Template template, String[] model, boolean forceInstatiate) {
    LOG.info(String.format("setNewComponentSlot(%s)", componentType == null ? null : componentType.getName()));    
    
    // record a test step if needed
    if (test != null) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("componentType", componentType);
      params.put("template", template);
      params.put("forceInstatiate", forceInstatiate);
      test.addStep(DIYTest.SET_COMPONENT_SLOT, params);
    }    
    
    if (componentType != null && componentType.getInstanceClass() == null) {
      LOG.info("Cannot set new component type slot for type " + componentType.getName());
      setNewComponentTypeSlot(null, null, null, false);
      return;
    }

    // if (componentType == null) {
    // controlPointMap.clear();
    // updateSelection(EMPTY_SELECTION);
    // }

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
      instantiationManager.setComponentTypeSlot(componentType, template, model, currentProject, forceInstatiate);

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
      view.showMessage(ERROR_SLOT, ERROR, IView.ERROR_MESSAGE);
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
        (Map<String, List<Template>>) configManager.readObject(TEMPLATES_KEY, null);
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
//      if (property.getName().equalsIgnoreCase("name")) {
//        continue;
//      }
      try {
        property.readFrom(component);
        values.put(property.getName(), property.getValue());
      } catch (Exception e) {
      }
    }
    List<Point2D> points = new ArrayList<Point2D>();

    for (int i = 0; i < component.getControlPointCount(); i++) {
      Point2D oldP = component.getControlPoint(i);
      Point2D p = new Point2D.Double(oldP.getX(), oldP.getY());
      points.add(p);
    }
    double x = points.get(0).getX();
    double y = points.get(0).getY();
    for (Point2D point : points) {
      point.setLocation(point.getX() - x, point.getY() - y);
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
          view.showConfirmDialog(VARIANT_EXISTS, SAVE_AS_VARIANT,
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

    if (System.getProperty("org.diylc.WriteStaticVariants", "false").equalsIgnoreCase("true")) {
      Map<String, List<Template>>  defaultVariantMap = new HashMap<String, List<Template>>();
      // unify default and user-variants
      for (Map.Entry<String, List<Template>> entry : variantMap.entrySet()) {
        if (defaultVariantMap.containsKey(entry.getKey())) {
          defaultVariantMap.get(entry.getKey()).addAll(entry.getValue());
        } else {
          defaultVariantMap.put(entry.getKey(), entry.getValue());
        }
      }
      try {
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("variants.xml"));
        XStream xStream = new XStream(new DomDriver());
        xStream.addPermission(AnyTypePermission.ANY);
        ProjectFileManager.xStreamSerializer.toXML(defaultVariantMap, out);
        out.close();
        // no more user variants
        configManager.writeValue(TEMPLATES_KEY, null);
        LOG.info("Saved default variants");
      } catch (IOException e) {
        LOG.error("Could not save default variants", e);
      }
    } else {
      configManager.writeValue(TEMPLATES_KEY, variantMap);
    }
  }
  
  @Override
  public List<Template> getVariantsFor(ComponentType type) {
    return variantManager.getVariantsFor(type);
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
  
  @SuppressWarnings("unchecked")
  @Override
  public Collection<ComponentType> getSelectedComponentTypes() {
    return this.getSelectedComponents().stream()
        .map(component -> ComponentProcessor.getInstance().extractComponentTypeFrom(
              (Class<? extends IDIYComponent<?>>) component.getClass()))
        .distinct()
        .collect(Collectors.toList());
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

    notifyProjectModifiedIfNeeded(oldProject, "Edit Selection", true, true);
  }
  
  @Override
  public void deleteVariant(ComponentType type, String templateName) {
    variantManager.deleteVariant(type, templateName);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void setDefaultVariant(ComponentType type, String templateName) {
    LOG.debug(String.format("setTemplateDefault(%s, %s)", type, templateName));
    Map<String, String> defaultTemplateMap =
        (Map<String, String>) configManager.readObject(DEFAULT_TEMPLATES_KEY, null);
    if (defaultTemplateMap == null)
      defaultTemplateMap = new HashMap<String, String>();

    // try by class name and then by old category.type format
    String key1 = type.getInstanceClass().getCanonicalName();
    String key2 = type.getCategory() + "." + type.getName();

    if (templateName.equals(defaultTemplateMap.get(key1)) || templateName.equals(defaultTemplateMap.get(key2))) {
      defaultTemplateMap.remove(key1);
      defaultTemplateMap.remove(key2);
    } else {
      // get rid of legacy key
      defaultTemplateMap.remove(key2);
      defaultTemplateMap.put(key1, templateName);
    }
    configManager.writeValue(DEFAULT_TEMPLATES_KEY, defaultTemplateMap);
  }

  @Override
  public String getDefaultVariant(ComponentType type) {
    return variantManager.getDefaultVariant(type);
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
  
  @Override
  public void lockComponent(IDIYComponent<?> c, boolean locked) {
    LOG.debug(String.format("lockComponent(%s, %s)", c.getName(), locked));
    Project oldProject = currentProject.clone();
    try {
      if (locked) {
        currentProject.getLockedComponents().add(c);
        selectedComponents.remove(c);
      } else {
        currentProject.getLockedComponents().remove(c);        
      }
    } finally {
      notifyProjectModifiedIfNeeded(oldProject, locked ? "Lock Component" : "Unlock Component", false, true);

      if (locked)
        updateSelection(selectedComponents);      
    }
  }

  @SuppressWarnings("unchecked")
  private boolean isComponentLocked(IDIYComponent<?> component) {
    ComponentType componentType =
        ComponentProcessor.getInstance().extractComponentTypeFrom(
            (Class<? extends IDIYComponent<?>>) component.getClass());
    // for internal-use components
    if (componentType == null)
      return false;
    return currentProject.getLockedLayers().contains((int) Math.round(componentType.getZOrder())) || currentProject.getLockedComponents().contains(component);
  }
  
  @SuppressWarnings("unchecked")
  private boolean isComponentLayerLocked(IDIYComponent<?> component) {
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
  private Point2D scalePoint(Point2D point) {
    Point2D p =
        point == null ? null : new Point2D.Double(point.getX() / drawingManager.getZoomLevel(),
            point.getY() / drawingManager.getZoomLevel());

    if (p != null && configManager.readBoolean(EXTRA_SPACE_KEY, true)) {
      double extraSpace = drawingManager.getExtraSpace(currentProject);
      p.setLocation(p.getX() - extraSpace, p.getY() - extraSpace);      
    }
    return p;
  }
  
  @Override
  public void saveSelectionAsBlock(String blockName) {
    buildingBlockManager.saveSelectionAsBlock(blockName, this.getSelectedComponents(), this.currentProject.getComponents());
  }

  @Override
  public void loadBlock(String blockName) throws InvalidBlockException { 
    List<IDIYComponent<?>> components = buildingBlockManager.loadBlock(blockName, currentProject.getComponents());
    pasteComponents(new ComponentTransferable(components), true, true);    
  }

  @Override
  public void deleteBlock(String blockName) {
    buildingBlockManager.deleteBlock(blockName);
  }

  @Override
  public double getExtraSpace() {
    if (!configManager.readBoolean(EXTRA_SPACE_KEY, true))
      return 0;

    double extraSpace = drawingManager.getExtraSpace(currentProject);
    boolean metric = configManager.readBoolean(Presenter.METRIC_KEY, true);

    extraSpace /= Constants.PIXELS_PER_INCH;

    if (metric)
      extraSpace *= SizeUnit.in.getFactor() / SizeUnit.cm.getFactor();

    return extraSpace;
  }

  @Override
  public int importVariants(String fileName) throws IOException {
    return variantManager.importVariants(fileName);
  }
  
  @Override
  public int importBlocks(String fileName) throws IOException {
    return buildingBlockManager.importBlocks(fileName);
  }

  @Override
  public List<Netlist> extractNetlists(boolean includeSwitches) throws NetlistException {
    List<ContinuityArea> continuityAreas = drawingManager.getContinuityAreas();
    
    return NetlistBuilder.extractNetlists(includeSwitches, currentProject, continuityAreas);
  }

  @Override
  public List<INetlistAnalyzer> getNetlistAnalyzers() {
    Set<Class<?>> classes;
    try {
      classes = Utils.getClasses("org.diylc.netlist");
      List<INetlistAnalyzer> result = new ArrayList<INetlistAnalyzer>();

      for (Class<?> clazz : classes) {
        if (!Modifier.isAbstract(clazz.getModifiers()) && INetlistAnalyzer.class.isAssignableFrom(clazz)) {
          result.add((INetlistAnalyzer) clazz.getDeclaredConstructor().newInstance());
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
  
  @Override
  public List<INetlistParser> getNetlistParserDefinitions() {
    return ReflectionUtils.getNetlistParserDefinitions();
  }
  
  // Test stuff
  
  @Override
  public void startRecording(String name) {
    test = new DIYTest(name);
  }
  
  @Override
  public DIYTest stopRecording() {
    DIYTest res = test;
    test = null;
    return res;
  }
  
  @Override
  public void addValidation(Map<String, Object> params) {   
    if (test != null)
      test.addStep(DIYTest.VALIDATION, params);
  }
  
  @Override
  public Snapshot createTestSnapshot() {
    Map<IDIYComponent<?>, ComponentArea> areas = new HashMap<IDIYComponent<?>, ComponentArea>();
    for (IDIYComponent<?> c : currentProject.getComponents()) {
      ComponentArea componentArea = drawingManager.getComponentArea(c);
      areas.put(c, componentArea);
    }
    return new Snapshot(currentProject.clone(), new HashSet<IDIYComponent<?>>(selectedComponents), areas, 
        new HashSet<ContinuityArea>(drawingManager.getContinuityAreas()));
  }
  
  @Override
  public List<Area> checkContinuityAreaProximity(Size threshold) {
    return drawingManager.getContinuityAreaProximity((float)threshold.convertToPixels());    
  }
  
  // IConfigListener

  @Override
  public void valueChanged(String configKey, Object value) {
    if (HIGHLIGHT_CONTINUITY_AREA.equalsIgnoreCase(configKey)) {
      drawingManager.clearContinuityArea();
      if (Boolean.TRUE.equals(value)) {
        drawingManager.clearComponentAreaMap();
      }
      messageDispatcher.dispatchMessage(EventType.REPAINT);
    }
  }
}
