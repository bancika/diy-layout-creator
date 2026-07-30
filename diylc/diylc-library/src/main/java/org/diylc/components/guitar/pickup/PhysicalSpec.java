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
 * Physical dimensions of the pickup as a standalone object. Storage/display only in v1 - not
 * used to drive rendering (see task non-goals).
 *
 * <p>Plain final class rather than a record - see {@link Measurement} for why.
 */
public final class PhysicalSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Measurement width;
  private final Measurement length;
  private final Measurement depth;
  private final Measurement poleSpacing;
  private final MountingLeg mountingLeg;

  public PhysicalSpec(Measurement width, Measurement length, Measurement depth, Measurement poleSpacing,
      MountingLeg mountingLeg) {
    this.width = width;
    this.length = length;
    this.depth = depth;
    this.poleSpacing = poleSpacing;
    this.mountingLeg = mountingLeg;
  }

  public Measurement width() {
    return width;
  }

  public Measurement length() {
    return length;
  }

  public Measurement depth() {
    return depth;
  }

  public Measurement poleSpacing() {
    return poleSpacing;
  }

  public MountingLeg mountingLeg() {
    return mountingLeg;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PhysicalSpec other)) {
      return false;
    }
    return Objects.equals(width, other.width) && Objects.equals(length, other.length)
        && Objects.equals(depth, other.depth) && Objects.equals(poleSpacing, other.poleSpacing)
        && mountingLeg == other.mountingLeg;
  }

  @Override
  public int hashCode() {
    return Objects.hash(width, length, depth, poleSpacing, mountingLeg);
  }

  @Override
  public String toString() {
    return "PhysicalSpec[width=" + width + ", length=" + length + ", depth=" + depth + ", poleSpacing=" + poleSpacing
        + ", mountingLeg=" + mountingLeg + "]";
  }
}
