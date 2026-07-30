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
 * Whole-pickup electrical specification (i.e. as measured across the final output leads, coils
 * already combined per the manufacturer's standard wiring). Storage/display only in v1 - not
 * used by circuit analysis or rendering.
 *
 * <p>Plain final class rather than a record - see {@link Measurement} for why.
 */
public final class ElectricalSpec implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Measurement seriesDcResistance;
  private final Measurement seriesInductance;
  private final Measurement parasiticCapacitance;
  private final Measurement outputMillivolts;

  public ElectricalSpec(Measurement seriesDcResistance, Measurement seriesInductance,
      Measurement parasiticCapacitance, Measurement outputMillivolts) {
    this.seriesDcResistance = seriesDcResistance;
    this.seriesInductance = seriesInductance;
    this.parasiticCapacitance = parasiticCapacitance;
    this.outputMillivolts = outputMillivolts;
  }

  public Measurement seriesDcResistance() {
    return seriesDcResistance;
  }

  public Measurement seriesInductance() {
    return seriesInductance;
  }

  public Measurement parasiticCapacitance() {
    return parasiticCapacitance;
  }

  public Measurement outputMillivolts() {
    return outputMillivolts;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ElectricalSpec other)) {
      return false;
    }
    return Objects.equals(seriesDcResistance, other.seriesDcResistance)
        && Objects.equals(seriesInductance, other.seriesInductance)
        && Objects.equals(parasiticCapacitance, other.parasiticCapacitance)
        && Objects.equals(outputMillivolts, other.outputMillivolts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(seriesDcResistance, seriesInductance, parasiticCapacitance, outputMillivolts);
  }

  @Override
  public String toString() {
    return "ElectricalSpec[seriesDcResistance=" + seriesDcResistance + ", seriesInductance=" + seriesInductance
        + ", parasiticCapacitance=" + parasiticCapacitance + ", outputMillivolts=" + outputMillivolts + "]";
  }
}
