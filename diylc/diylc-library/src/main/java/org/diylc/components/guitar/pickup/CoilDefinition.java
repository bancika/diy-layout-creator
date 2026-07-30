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
 * One coil of a pickup, as it would be found loose on a workbench. {@code id} is a local
 * identifier used by {@link TerminalDefinition#coilId()}, unique within the owning
 * {@link PickupDefinition} only (not globally).
 *
 * <p>Plain final class rather than a record - see {@link Measurement} for why.
 */
public final class CoilDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String id;
  private final CoilSide localSide;
  private final CoilPolePieceType polePieceType;
  private final MagneticPolarity magneticPolarity;
  private final WindingDirection windingDirection;
  private final Measurement dcResistance;
  private final Measurement inductance;

  public CoilDefinition(String id, CoilSide localSide, CoilPolePieceType polePieceType,
      MagneticPolarity magneticPolarity, WindingDirection windingDirection, Measurement dcResistance,
      Measurement inductance) {
    this.id = id;
    this.localSide = localSide;
    this.polePieceType = polePieceType;
    this.magneticPolarity = magneticPolarity;
    this.windingDirection = windingDirection;
    this.dcResistance = dcResistance;
    this.inductance = inductance;
  }

  public String id() {
    return id;
  }

  public CoilSide localSide() {
    return localSide;
  }

  public CoilPolePieceType polePieceType() {
    return polePieceType;
  }

  public MagneticPolarity magneticPolarity() {
    return magneticPolarity;
  }

  public WindingDirection windingDirection() {
    return windingDirection;
  }

  public Measurement dcResistance() {
    return dcResistance;
  }

  public Measurement inductance() {
    return inductance;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CoilDefinition other)) {
      return false;
    }
    return Objects.equals(id, other.id) && localSide == other.localSide && polePieceType == other.polePieceType
        && magneticPolarity == other.magneticPolarity && windingDirection == other.windingDirection
        && Objects.equals(dcResistance, other.dcResistance) && Objects.equals(inductance, other.inductance);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, localSide, polePieceType, magneticPolarity, windingDirection, dcResistance, inductance);
  }

  @Override
  public String toString() {
    return "CoilDefinition[id=" + id + ", localSide=" + localSide + ", polePieceType=" + polePieceType
        + ", magneticPolarity=" + magneticPolarity + ", windingDirection=" + windingDirection + ", dcResistance="
        + dcResistance + ", inductance=" + inductance + "]";
  }
}
