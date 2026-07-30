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

import java.io.Serializable;
import java.util.Objects;

/**
 * A simple {@code value}/{@code unit} pair, e.g. "8.4 kOhm" or "36.5 mm". Deliberately not tied
 * to DIYLC's internal {@code org.diylc.core.measures.*} classes used for circuit analysis - this
 * is catalogue metadata for storage/display only (see task non-goals: not used in analysis).
 *
 * <p>Implemented as a plain final class rather than a Java record: this type is embedded inside
 * placed pickup components and serialised via XStream's reflection-based converter, which mutates
 * final fields directly via {@code sun.misc.Unsafe} - the JVM specifically forbids that for
 * {@code record} classes, so a record here would silently fail to deserialize from a project
 * file.
 */
public final class Measurement implements Serializable {

  private static final long serialVersionUID = 1L;

  private final double value;
  private final MeasurementUnit unit;

  public Measurement(double value, MeasurementUnit unit) {
    this.value = value;
    this.unit = unit;
  }

  public double value() {
    return value;
  }

  public MeasurementUnit unit() {
    return unit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Measurement other)) {
      return false;
    }
    return Double.compare(value, other.value) == 0 && unit == other.unit;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, unit);
  }

  @Override
  public String toString() {
    return formatValue(value) + " " + unit.getSymbol();
  }

  /** Formats a whole number without a trailing ".0" (e.g. "6" rather than "6.0"). */
  private static String formatValue(double value) {
    if (!Double.isInfinite(value) && !Double.isNaN(value) && value == Math.rint(value)
        && Math.abs(value) < 1_000_000_000L) {
      return String.valueOf((long) value);
    }
    return String.valueOf(value);
  }
}
