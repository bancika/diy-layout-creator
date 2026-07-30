/*
 *
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.components.guitar.pickup;

/**
 * A neutral, physical-side label for a coil within a multi-coil pickup (e.g. a humbucker).
 * Deliberately does not say "screw"/"slug" or "north"/"south" &mdash; those are separate,
 * independent facts ({@code polePieceType}, {@code magneticPolarity}) that must not be assumed
 * from which side a coil happens to be wound on.
 */
public enum CoilSide {
  SIDE_A, SIDE_B
}
