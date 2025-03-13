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
package org.diylc.common;

public interface IKeyProcessor {

  public static final int VK_LEFT = 0x25;
  public static final int VK_UP = 0x26;
  public static final int VK_RIGHT = 0x27;
  public static final int VK_DOWN = 0x28;
  public static final int VK_H = 0x48;
  public static final int VK_V = 0x56;

  /**
   * Notifies the presenter that a key hes been pressed on the canvas.
   * 
   * @param key
   * @param ctrlDown
   * @param shiftDown
   * @param altDown
   * 
   * @return true if the event should be consumed
   */
  boolean keyPressed(int key, boolean ctrlDown, boolean shiftDown, boolean altDown);
}
