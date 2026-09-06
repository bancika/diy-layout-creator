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
package org.diylc.schematic;

import org.diylc.components.passive.PotentiometerSymbol;
import org.diylc.core.IDIYComponent;

/**
 * Maps physical potentiometers to a {@link PotentiometerSymbol}. The
 * three package legs are mapped to the symbol legs in index order; if a particular package uses a
 * different pin-out the user can rotate/relabel the resulting symbol.
 */
public class PotentiometerSchematicFactory extends AbstractSimpleSchematicFactory {

  @Override
  protected IDIYComponent<?> createSymbol() {
    return new PotentiometerSymbol();
  }

  @Override
  protected int electricalPinCount() {
    return 3;
  }
}
