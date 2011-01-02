package com.diyfever.diylc.common;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Area;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.JPopupMenu.Separator;

import com.diyfever.diylc.model.ComponentLayer;
import com.diyfever.diylc.model.IComponentInstance;
import com.diyfever.diylc.model.IComponentType;
import com.diyfever.diylc.model.Project;
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
	 * Returns all available {@link IComponentType}s classified by category.
	 * Result is a {@link Map} between category name to a {@link List} of all
	 * {@link IComponentType}s that share that category name.
	 * 
	 * @return
	 */
	Map<String, List<IComponentType>> getComponentTypes();

	/**
	 * Draws project on the provided {@link Graphics2D}.
	 * 
	 * @param g2d
	 */
	void draw(Graphics2D g2d, EnumSet<DrawOption> drawProperties);

	/**
	 * Injects a custom GUI panels provided by the plug-in and desired position
	 * in the window. Application will layout plug-in panels accordingly. <br>
	 * Valid positions are:
	 * <ul>
	 * <li> {@link SwingConstants#TOP}</li>
	 * <li> {@link SwingConstants#BOTTOM}</li>
	 * <li> {@link SwingConstants#LEFT}</li>
	 * <li> {@link SwingConstants#RIGHT}</li>
	 * </ul>
	 * 
	 * Center position is reserved for the main canvas panel and cannot be used.
	 * 
	 * @param component
	 * @param position
	 * @throws BadPositionException
	 *             in case invalid position is specified
	 */
	void injectGUIComponent(JComponent component, int position) throws BadPositionException;

	/**
	 * Injects a custom menu action into application's main menu. If
	 * <code>action</code> is set to null {@link Separator} will be added. If
	 * the specified menu does not exist it will be automatically created.
	 * 
	 * @param action
	 *            {@link Action} to inser
	 * @param menuName
	 *            name of the menu to insert into
	 */
	public void injectMenuAction(Action action, String menuName);

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
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 */
	void mouseClicked(Point point, boolean ctrlDown, boolean shiftDown, boolean altDown);

	void mouseMoved(Point point);

	/**
	 * Returns the current {@link ComponentSelection}.
	 * 
	 * @return
	 */
	ComponentSelection getSelectedComponents();

	/**
	 * Returns the {@link Area} occupied by the component.
	 * 
	 * @return
	 */
	Area getComponentArea(IComponentInstance component);

	/**
	 * Notification that drag has been started from the specified point.
	 * 
	 * Note: point coordinates are scaled for zoom factor.
	 * 
	 * @param point
	 */
	void dragStarted(Point point);

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
	 * @param preferredPoint
	 */
	void addComponents(List<IComponentInstance> components, Point preferredPoint);

	/**
	 * Checks if a certain layer is locked for editing.
	 * 
	 * @param layer
	 * @return
	 */
	boolean isLayerLocked(ComponentLayer layer);

	/**
	 * Changes layer's locked state.
	 * 
	 * @param layer
	 * @param locked
	 */
	void setLayerLocked(ComponentLayer layer, boolean locked);

	/**
	 * Checks if a certain layer is visible.
	 * 
	 * @param layer
	 * @return
	 */
	boolean isLayerVisible(ComponentLayer layer);

	/**
	 * Changes layer visibility.
	 * 
	 * @param layer
	 * @param visible
	 */
	void setLayerVisible(ComponentLayer layer, boolean visible);

	/**
	 * Sets default value for the specified property name for currently selected
	 * component types.
	 * 
	 * @param propertyName
	 *            display name for property
	 * @param value
	 *            new default value, must not be null
	 */
	void setDefaultPropertyValue(String propertyName, Object value);

	/**
	 * Returns a list of properties that are mutual for all the selected
	 * components. Resulting list may be empty if selected components do not
	 * have mutual properties or can be null if the selection is empty.
	 * 
	 * @return
	 */
	List<PropertyWrapper> getMutualSelectionProperties();

	/**
	 * Applies specified properties to all the selected components. If some of
	 * the properties are not applicable to some of the selected components an
	 * exception will be thrown.
	 * 
	 * @param properties
	 * @throws Exception
	 */
	void applyPropertiesToSelection(List<PropertyWrapper> properties) throws Exception;

	// void setCursorIcon(Icon icon);

	/**
	 * Sets the new component slot. Specified component type will be used to
	 * instantiate new component.
	 */
	void setNewComponentSlot(IComponentType componentType);
}
