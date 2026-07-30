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
 * Magnet material/grade, free-text since grades and naming vary widely between makers.
 *
 * <p>Plain final class rather than a record - see {@link Measurement} for why.
 */
public final class MagnetSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String material;
  private final String grade;

  public MagnetSpec(String material, String grade) {
    this.material = material;
    this.grade = grade;
  }

  public String material() {
    return material;
  }

  public String grade() {
    return grade;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MagnetSpec other)) {
      return false;
    }
    return Objects.equals(material, other.material) && Objects.equals(grade, other.grade);
  }

  @Override
  public int hashCode() {
    return Objects.hash(material, grade);
  }

  @Override
  public String toString() {
    return "MagnetSpec[material=" + material + ", grade=" + grade + "]";
  }
}
