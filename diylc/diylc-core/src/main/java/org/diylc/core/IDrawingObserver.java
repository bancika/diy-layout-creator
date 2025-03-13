/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.core;

/**
 * Interface for tracking component drawing process. Components may use it to stop or restart
 * tracking. Anything drawn while tracking is stopped will not be added to the mouse hot-spot area.
 * 
 * @author Branislav Stojkovic
 */
public interface IDrawingObserver {

  /**
   * Notifies the observer that it should start tracking the drawing process.
   */
  void stopTracking();

  /**
   * Notifies the observer that it should stop tracking the drawing process.
   */
  void startTracking();
  
  /**   
   * Notifies the observer that it should start tracking conductive continuity areas.
   * 
   * @param positive designates if the area should be additive or subtractive
   */
  void startTrackingContinuityArea(boolean positive);
  
  /**
   * Notifies the observer that it should stop tracking conductive continuity areas.
   */
  void stopTrackingContinuityArea();  
  
  /**
   * Checks if we are tracking continuity ares.
   * 
   * @return
   */
  boolean isTrackingContinuityArea();
  
  /**
   * Allows us to connect multiple disjoint continuity areas by specifying the same continuity markers.
   * By default it is set to null and does not connect disjoint areas.
   * 
   * @param marker
   */
  void setContinuityMarker(String marker);
}
