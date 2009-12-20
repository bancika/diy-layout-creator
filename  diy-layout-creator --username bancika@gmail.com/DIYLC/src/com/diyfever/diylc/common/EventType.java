package com.diyfever.diylc.common;

import java.awt.Rectangle;

import com.diyfever.diylc.model.IComponentType;
import com.diyfever.diylc.model.Project;

/**
 * Enumerates all possible events between {@link IPlugInPort} and
 * {@link IPlugIn}. Some events overlap so be careful which ones you subscribe
 * for and how you process them.
 * 
 * @author Branislav Stojkovic
 */
public enum EventType {

	/**
	 * Called when zoom level changes. Typically only one parameter of type
	 * {@link Double} is passed with new zoom level.
	 */
	ZOOM_CHANGED,
	/**
	 * Called when a new project is loaded. New {@link Project} is the first
	 * parameter. Boolean flag is the second parameter and it's true when new
	 * project is loaded, false when the same project has been either reloaded
	 * or loaded with undo/redo operations.
	 */
	PROJECT_LOADED,
	/**
	 * Called when selection rectangle is changed. Object of type
	 * {@link Rectangle} is the only parameter and it contains the new selection
	 * rectangle.
	 */
	SELECTION_RECT_CHANGED,
	/**
	 * Called when component selection is changed. New
	 * {@link ComponentSelection} is attached as a parameter.
	 */
	SELECTION_CHANGED,
	/**
	 * Called when display needs to be repainted. No parameters are passed.
	 */
	REPAINT,
	/**
	 * Called when new component slot has been changed. The only parameter is
	 * {@link IComponentType} and may be null.
	 */
	SLOT_CHANGED,
	/**
	 * Called when the current project has been modified. Two instances of
	 * {@link Project} are passed as parameters, one before and one after the
	 * change. The third parameter is a string containing change description.
	 */
	PROJECT_MODIFIED;
}
