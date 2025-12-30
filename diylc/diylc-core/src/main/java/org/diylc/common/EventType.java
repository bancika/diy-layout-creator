/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.common;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

import org.diylc.core.Project;

/**
 * Enumerates all possible events between {@link IPlugInPort} and {@link IPlugIn}. Some events
 * overlap so be careful which ones you subscribe for and how you process them.
 * 
 * @author Branislav Stojkovic
 */
public enum EventType {

  /**
   * Called when zoom level changes. Typically only one parameter of type {@link Double} is passed
   * with new zoom level.
   */
  ZOOM_CHANGED,
  /**
   * Called when a new project is loaded. New {@link Project} is the first parameter. Boolean flag
   * is the second parameter and it's true when new project is loaded, false when the same project
   * has been either reloaded or loaded with undo/redo operations.
   */
  PROJECT_LOADED,
  /**
   * Called when the current project is saved to a file. The first parameter is the current file name.
   */
  PROJECT_SAVED,
  /**
   * Called when either current file name or modified flag are changed. The first parameter is the
   * current file name, the second one is a boolean modified flag.
   */
  FILE_STATUS_CHANGED,
  /**
   * Called when component selection is changed. New list of components and control point map is attached as a
   * parameter.
   */
  SELECTION_CHANGED,
  /**
   * Called when selection size changes, together with a {@link Point2D} object containing selection
   * size (x = width, y = height). Size is expressed in the default measure (cm for metric, inches
   * for imperial).
   */
  REPAINT,
  /**
   * Called when new component slot has been changed. The only parameter is {@link ComponentType}
   * and may be null.
   */
  SLOT_CHANGED,
  /**
   * Called when the current project has been modified. Two instances of {@link Project} are passed
   * as parameters, one before and one after the change. The third parameter is a string containing
   * change description.
   */
  PROJECT_MODIFIED,
  /**
   * Called when control points under the cursor are changed. A single parameter is passed, an
   * instance of <code>Map<IDIYComponent<?>, Set<Integer>></code> containing all the components and
   * indices of their control points that are under the cursor.
   */
  AVAILABLE_CTRL_POINTS_CHANGED,
  /**
   * Called when mouse cursor is moved. Point object is passed, containing the current cursor
   * location (not taking zoom into account) in pixels and two {@link Point2D} objects with coordinates in
   * inches and mm
   */
  MOUSE_MOVED,
  /**
   * Called when a layer status is changed. The first parameter is a set of indices of locked
   * layers. All layers not included are considered unlocked.
   */
  LAYER_STATE_CHANGED,
  /**
   * Called when a layer visibility is changed. The first parameter is a set of indices of hidden
   * layers. All layers not included are considered visible.
   */
  LAYER_VISIBILITY_CHANGED,
  /**
   * Called to update the status message. New status message is passed as a first parameter.
   */
  STATUS_MESSAGE_CHANGED,
  /**
   * Signals the listeners to scroll to selection.
   */
  SCROLL_TO,
  /**
   * Signals that the Cloud account is logged in.
   */
  CLOUD_LOGGED_IN,
  /**
   * Signals that the Cloud account is logged out.
   */
  CLOUD_LOGGED_OUT;
}
