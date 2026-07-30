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

import java.awt.Color;
import java.io.Serializable;
import java.util.Objects;

/**
 * Describes the physical lead wire for one terminal: its construction ({@link #type()}), a
 * human-friendly name (e.g. "Red") and, optionally, its colour so that it can be used by
 * "Add Flexible Leads" and shown in the library dialog. {@code stripeColour} is accepted for
 * forward compatibility but is not rendered in v1.
 *
 * <p>Plain final class rather than a record - see {@link Measurement} for why.
 */
public final class LeadSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  private final LeadType type;
  private final String displayName;
  private final String baseColour;
  private final String stripeColour;

  public LeadSpec(LeadType type, String displayName, String baseColour, String stripeColour) {
    this.type = type;
    this.displayName = displayName;
    this.baseColour = baseColour;
    this.stripeColour = stripeColour;
  }

  public LeadType type() {
    return type;
  }

  public String displayName() {
    return displayName;
  }

  public String baseColour() {
    return baseColour;
  }

  public String stripeColour() {
    return stripeColour;
  }

  /**
   * @return the parsed AWT colour for {@link #baseColour()}, or {@code null} if none is set.
   * @throws IllegalArgumentException if {@link #baseColour()} is set but not a valid
   *         {@code #RRGGBB} hex colour.
   */
  public Color toAwtColor() {
    return parseHexColor(baseColour);
  }

  static Color parseHexColor(String hex) {
    if (hex == null || hex.isBlank()) {
      return null;
    }
    return Color.decode(hex.trim());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LeadSpec other)) {
      return false;
    }
    return type == other.type && Objects.equals(displayName, other.displayName)
        && Objects.equals(baseColour, other.baseColour) && Objects.equals(stripeColour, other.stripeColour);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, displayName, baseColour, stripeColour);
  }

  @Override
  public String toString() {
    return "LeadSpec[type=" + type + ", displayName=" + displayName + ", baseColour=" + baseColour
        + ", stripeColour=" + stripeColour + "]";
  }
}
