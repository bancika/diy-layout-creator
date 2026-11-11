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
package org.diylc.core;

import org.diylc.common.PCBLayer;

/**
 * Interface for components on one side of a board, such as a resistor, an IC or a trace. The side
 * says where the part sits physically; it is separate from {@link ILayeredComponent}, which drives
 * continuity, because the leads of a through-hole part connect to copper on both sides whichever
 * side the part is mounted on. Copper implements it through {@link IPCBLayerComponent}, whose layer
 * is its side and also drives its continuity.
 * 
 * @author Branislav Stojkovic
 */
public interface IBoardSideComponent {

  /**
   * @return {@link PCBLayer#_2} for the top of the board or {@link PCBLayer#_1} for the bottom,
   *         never an inner layer or null
   */
  PCBLayer getBoardSide();

  /**
   * Sets the side of the board the component is mounted on. The component is not mirrored.
   * 
   * @param boardSide {@link PCBLayer#_2} for the top or {@link PCBLayer#_1} for the bottom
   */
  void setBoardSide(PCBLayer boardSide);

  /**
   * @return {@link PCBLayer#_1} for a component mounted on the bottom of the board, and
   *         {@link PCBLayer#_2} for any other, including components that have no side
   */
  static PCBLayer sideOf(IDIYComponent<?> component) {
    return component instanceof IBoardSideComponent sided && sided.getBoardSide() == PCBLayer._1
        ? PCBLayer._1 : PCBLayer._2;
  }
}
