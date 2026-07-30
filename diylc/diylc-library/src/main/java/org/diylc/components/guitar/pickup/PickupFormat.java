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
 * Intrinsic physical/electrical "shape" of a pickup, independent of how it is installed or wired
 * in a particular diagram. Each value maps to exactly one existing DIYLC pickup component class
 * (see {@code PickupComponentFactory}); visual sub-variants (Strat/Tele, PAF/Mini/Filtertron,
 * rail/stacked single coils, etc.) are represented by the existing per-component "type"
 * properties rather than by additional enum values here.
 *
 * <p>Enumerated from the existing classes under {@code org.diylc.components.guitar}:
 * <ul>
 * <li>{@code SingleCoilPickup} (Stratocaster/Telecaster, incl. rail/stacked pole piece
 * variants) -&gt; {@link #SINGLE_COIL}</li>
 * <li>{@code HumbuckerPickup} (PAF/Mini/Filtertron) -&gt; {@link #HUMBUCKER}</li>
 * <li>{@code P90Pickup} (Dog Ear/Soap Bar) -&gt; {@link #P90}</li>
 * <li>{@code JazzBassPickup} -&gt; {@link #JAZZ_BASS}</li>
 * <li>{@code PBassPickup} -&gt; {@link #PRECISION_BASS}</li>
 * <li>{@code StingrayBassPickup} -&gt; {@link #STINGRAY_BASS}</li>
 * </ul>
 */
public enum PickupFormat {
  SINGLE_COIL, HUMBUCKER, P90, JAZZ_BASS, PRECISION_BASS, STINGRAY_BASS
}
