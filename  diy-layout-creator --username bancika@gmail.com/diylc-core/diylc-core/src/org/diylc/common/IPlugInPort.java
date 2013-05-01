package org.diylc.common;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.appframework.simplemq.IMessageListener;
import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.appframework.update.VersionNumber;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.Theme;

/**
 * Interface for communication between plug-ins and the application. Plug-ins
 * may acquire information or make changes through this interface. Application
 * events are dispatched to plug-ins through {@link MessageDispatcher}
 * 
 * @author Branislav Stojkovic
 * 
 * @see IPlugIn
 * @see MessageDispatcher
 * @see IMessageListener
 * @see EventType
 */
public interface IPlugInPort extends ISelectionProcessor, IMouseProcessor, IKeyProcessor, ITemplateProcessor {

	public static final String ANTI_ALIASING_KEY = "antiAliasing";
	public static final String HI_QUALITY_RENDER_KEY = "hiQualityRender";
	public static final String EXPORT_GRID_KEY = "exportGrid";
	public static final String STICKY_POINTS_KEY = "stickyPoints";
	public static final String METRIC_KEY = "metric";
	public static final String SNAP_TO_GRID_KEY = "snapToGrid";
	public static final String AUTO_PADS_KEY = "autoCreatePads";
	public static final String CONTINUOUS_CREATION_KEY = "continuousCreation";
	public static final String AUTO_EDIT_KEY = "autoEdit";
	public static final String ABNORMAL_EXIT_KEY = "abnormalExit";
	public static final String WHEEL_ZOOM_KEY = "wheelZoom";
	public static final String OUTLINE_KEY = "outline";
	public static final String THEME_KEY = "theme";
	public static final String RECENT_COMPONENTS_KEY = "recentComponents";

	public static final int DND_TOGGLE_STICKY = 0x1;
	public static final int DND_TOGGLE_SNAP = 0x40000000;
	
    /**
     * Mouse button constants.
     */ 
    public static final int NOBUTTON = 0;
    public static final int BUTTON1 = 1;
    public static final int BUTTON2 = 2;
    public static final int BUTTON3 = 3;

	/**
	 * Returns size of the canvas that takes project dimensions into account as
	 * well as zoom level. Each dimension is calculated as the product of the
	 * actual size and the number of pixels per unit. If <code>useZoom</code> is
	 * set to true, the result is scaled by zoom factor.
	 * 
	 * @param useZoom
	 * 
	 * @return canvas dimensions
	 */
	Dimension getCanvasDimensions(boolean useZoom);

	/**
	 * Returns an instance of {@link Cursor} that should be used at the
	 * specified location.
	 * 
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 * @return cursor
	 */
	Cursor getCursorAt(Point point);

	/**
	 * Returns an instance of currently loaded project.
	 * 
	 * @return
	 */
	Project getCurrentProject();

	/**
	 * Loads specified {@link Project}.
	 * 
	 * @param project
	 * @param freshStart
	 */
	void loadProject(Project project, boolean freshStart);

	/**
	 * Creates a new project.
	 */
	void createNewProject();

	/**
	 * Loads a project from the specified file.
	 * 
	 * @param fileName
	 */
	void loadProjectFromFile(String fileName);

	/**
	 * Saves the current project into the specified file.
	 * 
	 * @param fileName
	 * @param isBackup
	 */
	void saveProjectToFile(String fileName, boolean isBackup);

	/**
	 * @return the current file name.
	 */
	String getCurrentFileName();

	/**
	 * @return true if the current project is modified.
	 */
	boolean isProjectModified();

	/**
	 * Shows a user dialog if there are changes to confirm that it's safe to
	 * proceed.
	 * 
	 * @return true, if file actions (new, open, close) can be taken
	 */
	boolean allowFileAction();

	/**
	 * Returns all available {@link ComponentType}s classified by category.
	 * Result is a {@link Map} between category name to a {@link List} of all
	 * {@link ComponentType}s that share that category name.
	 * 
	 * @return
	 */
	Map<String, List<ComponentType>> getComponentTypes();

	/**
	 * Draws project on the provided {@link Graphics2D}. If the provided filter
	 * is not null, it will be used to filter the components that are shown.
	 * 
	 * @param g2d
	 * @param drawOptions
	 *            specific drawing options
	 * @param filter
	 */
	void draw(Graphics2D g2d, Set<DrawOption> drawOptions, IComponentFiler filter);

	Double[] getAvailableZoomLevels();

	/**
	 * Returns current zoom level where <code>zoomLevel = 1.0d</code> means
	 * 100%.
	 * 
	 * @return current zoom level
	 */
	double getZoomLevel();

	/**
	 * Changes current zoom level where <code>zoomLevel = 1.0d</code> means 100%
	 * 
	 * @param zoomLevel
	 *            new zoom leve
	 */
	void setZoomLevel(double zoomLevel);

	/**
	 * Returns current version number.
	 * 
	 * @return
	 */
	VersionNumber getCurrentVersionNumber();

	/**
	 * Adds a list of components to the project.
	 * 
	 * @param components
	 */
	void pasteComponents(List<IDIYComponent<?>> components);

	/**
	 * Deletes all the selected components from the project.
	 */
	void deleteSelectedComponents();

	/**
	 * Sets default value for the specified property name for currently selected
	 * component types.
	 * 
	 * @param propertyName
	 *            display name for property
	 * @param value
	 *            new default value, must not be null
	 */
	void setSelectionDefaultPropertyValue(String propertyName, Object value);

	/**
	 * Sets default value for the specified property name for projects.
	 * 
	 * @param propertyName
	 *            display name for property
	 * @param value
	 *            new default value, must not be null
	 */
	void setProjectDefaultPropertyValue(String propertyName, Object value);

	/**
	 * @return a list of properties that are mutual for all the selected
	 *         components. Resulting list may be empty if selected components do
	 *         not have mutual properties or can be null if the selection is
	 *         empty.
	 */
	List<PropertyWrapper> getMutualSelectionProperties();

	void editSelection();

	/**
	 * @return a list of editable properties of the current project.
	 */
	List<PropertyWrapper> getProjectProperties();

	/**
	 * Applies specified properties to the current project.
	 * 
	 * @param properties
	 */
	void applyPropertiesToProject(List<PropertyWrapper> properties);
	
	/**
	 * Gets the current new component slot.
	 */
	ComponentType getNewComponentTypeSlot();

	/**
	 * Sets the new component slot. Specified component type will be used to
	 * instantiate new component.
	 * 
	 * @param componentType
	 * @param template
	 */
	void setNewComponentTypeSlot(ComponentType componentType, Template template);

	/**
	 * Changes default size notation, true for metric, false for imperial.
	 * 
	 * @param isMetric
	 */
	void setMetric(boolean isMetric);

	/**
	 * Groups all selected components.
	 */
	void groupSelectedComponents();

	/**
	 * Ungroups all selected components.
	 */
	void ungroupSelectedComponents();

	/**
	 * Locks or unlocks the specified layer. All components within +- 0.5 range
	 * will be affected by the change as well.
	 * 
	 * @param layerZOrder
	 * @param locked
	 */
	void setLayerLocked(int layerZOrder, boolean locked);

	/**
	 * @return selection size expressed in either inches or centimeters, based
	 *         on the user preference.
	 */
	Point2D calculateSelectionDimension();
	
//	/**
//	 * @return the smallest rectangle that encloses the selection
//	 */
//	Rectangle2D getSelectedAreaRect();

	/**
	 * Sends each of the selected components one step back.
	 */
	void sendSelectionToBack();

	/**
	 * Brings each of the selected components one step to front.
	 */
	void bringSelectionToFront();

	/**
	 * Causes the display to refresh.
	 */
	void refresh();

	/**
	 * @return currently selected theme.
	 */
	Theme getSelectedTheme();

	/**
	 * Changes the current theme.
	 * 
	 * @param theme
	 */
	void setSelectedTheme(Theme theme);

	/**
	 * Renumbers all the selected components.
	 * 
	 * @param xAxisFirst
	 */
	void renumberSelectedComponents(boolean xAxisFirst);
}
