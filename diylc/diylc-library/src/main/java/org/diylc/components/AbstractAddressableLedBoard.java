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
package org.diylc.components;

import java.awt.Color;

import org.diylc.core.annotations.EditableProperty;

/**
 * Base for the boards built out of addressable 5050 LEDs -- the rings, the jewel, the stick, the
 * strip, the matrix and the bare breakout.
 *
 * <p>Every one of them is sold fitted with any of the packages {@link LedType} lists, on the same
 * PCB with the same footprint, so the package is a property of the board rather than a board of
 * its own. What it changes is what the LEDs can emit, which is the palette here, and what the BOM
 * has to ask for, which each board folds into its own variant label.
 *
 * @author Branislav Stojkovic
 */
public abstract class AbstractAddressableLedBoard extends AbstractMakerBoard {

  private static final long serialVersionUID = 1L;

  protected LedType ledType = LedType.RGB;

  // Rebuilt only when the count or the package changes, because draw() runs on every repaint.
  private transient Color[] ledColors;

  @EditableProperty(name = "LED Type")
  public LedType getLedType() {
    if (ledType == null) {
      ledType = LedType.RGB;
    }
    return ledType;
  }

  public void setLedType(LedType ledType) {
    this.ledType = ledType;
    this.ledColors = null;
    invalidateCache();
  }

  /**
   * The colours {@code count} LEDs are drawn lit with. These parts are drawn lit because unlit
   * they are a dark board with white specks on it, which reads as no particular component; running
   * the palette around the part also says at a glance which package it is fitted with, a WWA one
   * having no colour to show.
   */
  protected Color[] getLedColors(int count) {
    if (ledColors == null || ledColors.length != count) {
      ledColors = buildLedGradient(count, getLedType().getGradient());
    }
    return ledColors;
  }
}
