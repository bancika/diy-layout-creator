package org.diylc.components.guitar.pickup;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Confirms {@link Measurement#toString()} renders a proper ohm sign (and other unit symbols),
 * and that whole numbers print cleanly (no trailing ".0"). The unit symbols are built from
 * numeric character codes rather than literal Unicode text (see {@link MeasurementUnit}), since
 * this project's source encoding is ISO-8859-1, not UTF-8. This test does the same (using
 * {@code (char) 0x03A9}/{@code (char) 0x00B5} rather than literal characters in this source
 * file) so it stays correct regardless of encoding, and guards against that ever regressing into
 * mangled/garbled output.
 */
public class MeasurementTest {

  private static final char OMEGA = (char) 0x03A9;
  private static final char MICRO = (char) 0x00B5;

  @Test
  public void testOhmSymbolRendersCorrectly() {
    String kiloohm = new Measurement(8.4, MeasurementUnit.KILOOHM).toString();
    assertEquals("8.4 k" + OMEGA, kiloohm);
    assertEquals(OMEGA, kiloohm.charAt(kiloohm.length() - 1));

    assertEquals("1 " + OMEGA, new Measurement(1, MeasurementUnit.OHM).toString());
    assertEquals("2.2 M" + OMEGA, new Measurement(2.2, MeasurementUnit.MEGAOHM).toString());
  }

  @Test
  public void testMicroSignRendersCorrectly() {
    assertEquals("10 " + MICRO + "F", new Measurement(10, MeasurementUnit.MICROFARAD).toString());
    assertEquals("100 " + MICRO + "H", new Measurement(100, MeasurementUnit.MICROHENRY).toString());
  }

  @Test
  public void testWholeNumbersPrintWithoutTrailingZero() {
    assertEquals("6 mm", new Measurement(6.0, MeasurementUnit.MILLIMETRE).toString());
    assertEquals("36.5 mm", new Measurement(36.5, MeasurementUnit.MILLIMETRE).toString());
  }

  @Test
  public void testOtherUnitsRenderPlainAsciiSymbols() {
    assertEquals("110 pF", new Measurement(110, MeasurementUnit.PICOFARAD).toString());
    assertEquals("3.2 H", new Measurement(3.2, MeasurementUnit.HENRY).toString());
    assertEquals("9 V", new Measurement(9, MeasurementUnit.VOLT).toString());
  }
}
