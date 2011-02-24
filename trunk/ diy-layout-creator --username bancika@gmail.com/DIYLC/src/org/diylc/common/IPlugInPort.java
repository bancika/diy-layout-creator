package org.diylc.common;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Theme;
import org.diylc.swing.plugins.edit.ComponentTransferable;

import com.diyfever.gui.simplemq.IMessageListener;
import com.diyfever.gui.simplemq.MessageDispatcher;
import com.diyfever.gui.update.VersionNumber;

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
public interface IPlugInPort {

	public static final String ANTI_ALIASING_KEY = "antiAliasing";
	public static final String HI_QUALITY_RENDER_KEY = "hiQualityRender";
	public static final String EXPORT_GRID_KEY = "exportGrid";
	public static final String STICKY_POINTS_KEY = "stickyPoints";
	public static final String METRIC_KEY = "metric";
	public static final String SNAP_TO_GRID_KEY = "snapToGrid";
	public static final String AUTO_PADS_KEY = "autoCreatePads";
	public static final String CONTINUOUS_CREATION_KEY = "continuousCreation";
	public static final String AUTO_EDIT_KEY = "autoEdit";

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
	 */
	void saveProjectToFile(String fileName);

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
	 * Notifies the presenter that mouse is clicked.
	 * 
	 * Note: point coordinates are display based, i.e. scaled for zoom factor.
	 * 
	 * @param point
	 * @param ctrlDown
	 * @param shiftDown
	 * @param altDown
	 * @param clickCount
	 */
	void mouseClicked(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown, int clickCount);

	/**
	 * Notifies the presenter that mouse is moved.
	 * 
	 * Note: point coordinates are display based, i.e. scaled for zoom factor.
	 * 
	 * @param point
	 * @param ctrlDown
	 * @param shiftDown
	 * @param altDown
	 */
	void mouseMoved(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown);

	/**
	 * Returns the current {@link ComponentTransferable}.
	 * 
	 * @return
	 */
	List<IDIYComponent<?>> getSelectedComponents();

	/**
	 * Selects all components in the project.
	 */
	void selectAll();

	/**
	 * Notification that drag has been started from the specified point.
	 * 
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 * @param ctrlDown
	 * @param shiftDown
	 * @param altDown
	 */
	void dragStarted(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown);

	/**
	 * Checks if it's possible to drop over the specified point.
	 * 
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 * @return
	 */
	boolean dragOver(Point point);

	/**
	 * Notification that drag has been ended in the specified point.
	 * 
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 */
	void dragEnded(Point point);

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

	/**
	 * Applies specified properties to all the selected components. If some of
	 * the properties are not applicable to some of the selected components an
	 * exception will be thrown.
	 * 
	 * @param properties
	 */
	void applyPropertiesToSelection(List<PropertyWrapper> properties);

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
	 * Sets the new component slot. Specified component type will be used to
	 * instantiate new component.
	 */
	void setNewComponentTypeSlot(ComponentType componentType);

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
}
