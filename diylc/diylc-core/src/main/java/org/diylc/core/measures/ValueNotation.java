/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.core.measures;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Notation used to write resistance, capacitance, inductance and voltage values: plain decimal
 * ("4.7K", "0.22uF", "6.3V") or RKM, where the unit symbol takes the place of the decimal point
 * ("4K7", "0u22", "6V3"). IEC 60062 defines it for resistance and capacitance; inductance and
 * voltage follow common practice. Parsing accepts both regardless of the selected notation.
 *
 * @author Branislav Stojkovic
 */
public enum ValueNotation {

  DECIMAL, RKM;

  private static final DecimalFormat RKM_FORMAT =
      new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.US));

  private static final Pattern RKM_PATTERN = Pattern.compile("^(-?)(\\d*)([^\\d.\\-]+)(\\d+)([^\\d.]*)$");

  // read on every repaint through toString(), so it is kept here rather than looked up in the
  // configuration
  private static volatile ValueNotation current = DECIMAL;

  public static ValueNotation getCurrent() {
    return current;
  }

  public static void setCurrent(ValueNotation notation) {
    current = notation == null ? DECIMAL : notation;
  }

  /**
   * Writes a value in RKM notation, e.g. 4.7 with "K" becomes "4K7" and 100 with "R" becomes
   * "100R". The value is kept in the unit it is stored in and never renormalized.
   *
   * @param value value expressed in the unit
   * @param rkmSymbol symbol that stands in for the decimal point
   * @return RKM code
   */
  public static String formatRkm(double value, String rkmSymbol) {
    String text;
    synchronized (RKM_FORMAT) {
      text = RKM_FORMAT.format(value);
    }
    int dot = text.indexOf('.');
    if (dot < 0) {
      return text + rkmSymbol;
    }
    return text.substring(0, dot) + rkmSymbol + text.substring(dot + 1);
  }

  /**
   * Parses a value written either in decimal notation with an optional unit ("4.7", "4.7k",
   * "0.22 uF") or in RKM notation ("4K7", "R22", "2n2F").
   *
   * @param text text to parse
   * @param unitResolver maps the unit text to a unit, returning null when it is not recognized
   * @return parsed value; its unit is null when the text carries no unit
   * @throws IllegalArgumentException when the text cannot be parsed
   */
  public static <U extends Unit> ParsedMeasure<U> parse(String text,
      Function<String, U> unitResolver) {
    if (text == null) {
      throw new IllegalArgumentException("Nothing to parse");
    }
    String value = text.replace("*", "").replace(",", ".").replaceAll("\\s", "");
    if (value.isEmpty()) {
      throw new IllegalArgumentException("Nothing to parse");
    }

    int unitStart = value.length();
    while (unitStart > 0 && !Character.isDigit(value.charAt(unitStart - 1))
        && value.charAt(unitStart - 1) != '.') {
      unitStart--;
    }
    String number = value.substring(0, unitStart);
    String unitText = value.substring(unitStart);
    U unit = unitText.isEmpty() ? null : unitResolver.apply(unitText);
    if (unitText.isEmpty() || unit != null) {
      try {
        return new ParsedMeasure<U>(Double.parseDouble(number), unit);
      } catch (NumberFormatException e) {
        // not a decimal number, try RKM below
      }
    }

    Matcher matcher = RKM_PATTERN.matcher(value);
    if (matcher.matches()) {
      unit = unitResolver.apply(matcher.group(3) + matcher.group(5));
      if (unit != null) {
        String integerPart = matcher.group(2).isEmpty() ? "0" : matcher.group(2);
        double parsed = Double.parseDouble(integerPart + "." + matcher.group(4));
        return new ParsedMeasure<U>(matcher.group(1).isEmpty() ? parsed : -parsed, unit);
      }
    }
    throw new IllegalArgumentException("Could not parse value: " + text);
  }

  public record ParsedMeasure<U extends Unit>(double value, U unit) {
  }
}
