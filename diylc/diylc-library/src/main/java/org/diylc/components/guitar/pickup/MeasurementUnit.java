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
 * Unit for a {@link Measurement}. These are purely for storage/display in v1 - they are
 * intentionally not wired into circuit analysis or rendering (see task non-goals).
 *
 * <p>Each unit carries a compact display symbol (e.g. kilohm, using the Greek capital letter
 * Omega as the ohm sign) used by {@link Measurement#toString()} and, in turn, by the library
 * dialog's results table and details panel.
 *
 * <p>Symbols are built from numeric character codes ({@code (char) 0x03A9} for Omega,
 * {@code (char) 0x00B5} for the micro sign) rather than written as literal Unicode characters in
 * this source file, because this project's {@code project.build.sourceEncoding} is
 * {@code ISO-8859-1}, not UTF-8: a literal multi-byte UTF-8 character saved in this file would be
 * misread by the compiler under that encoding. Numeric character codes are plain ASCII in the
 * source and are therefore unaffected by source encoding.
 */
public enum MeasurementUnit {
  OHM(omegaSign()),
  KILOOHM("k" + omegaSign()),
  MEGAOHM("M" + omegaSign()),
  HENRY("H"),
  MILLIHENRY("mH"),
  MICROHENRY(microSign() + "H"),
  PICOFARAD("pF"),
  NANOFARAD("nF"),
  MICROFARAD(microSign() + "F"),
  MILLIMETRE("mm"),
  CENTIMETRE("cm"),
  INCH("in"),
  MILLIVOLT("mV"),
  VOLT("V");

  private final String symbol;

  MeasurementUnit(String symbol) {
    this.symbol = symbol;
  }

  private static String omegaSign() {
    return String.valueOf((char) 0x03A9);
  }

  private static String microSign() {
    return String.valueOf((char) 0x00B5);
  }

  /** @return the compact display symbol for this unit, e.g. kilohm symbol for {@link #KILOOHM}. */
  public String getSymbol() {
    return symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
}
