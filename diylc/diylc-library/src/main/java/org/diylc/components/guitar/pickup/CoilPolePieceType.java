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
 * Pole piece construction of a single coil within a pickup definition. Named distinctly from the
 * per-component {@code PolePieceType} enums already declared on {@code HumbuckerPickup} and
 * {@code SingleCoilPickup} to avoid clashing with those unrelated, rendering-focused enums.
 */
public enum CoilPolePieceType {
  SLUG, SCREW, ROD, RAIL, BLADE, UNKNOWN
}
