/*
 * 
 * DIY Layout Creator (DIYLC).
 * Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.components.micro;

import org.diylc.components.micro.ArduinoNano.NanoVersion;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.junit.Assert;
import org.junit.Test;

public class ArduinoNanoTest {

  @Test
  public void testPinoutPerVersion() {
    ArduinoNano classic = new ArduinoNano();
    Assert.assertEquals(NanoVersion.CLASSIC, classic.getVersion());
    Assert.assertEquals(36, classic.getControlPointCount());

    // JP1 down the left, JP2 down the right, then the 2x3 ICSP block
    Assert.assertEquals("D1 (TX)", classic.getControlPointNodeName(0));
    Assert.assertEquals("D12", classic.getControlPointNodeName(14));
    Assert.assertEquals("VIN", classic.getControlPointNodeName(15));
    Assert.assertEquals("RST2", classic.getControlPointNodeName(17));
    Assert.assertEquals("5V", classic.getControlPointNodeName(18));
    Assert.assertEquals("AREF", classic.getControlPointNodeName(27));
    Assert.assertEquals("D13", classic.getControlPointNodeName(29));
    Assert.assertEquals("MISO", classic.getControlPointNodeName(30));

    // the silkscreen prints what is on the board: both grounds and both resets are printed alike
    Assert.assertEquals("GND1", classic.getControlPointNodeName(3));
    Assert.assertEquals("GND2", classic.getControlPointNodeName(16));
    Assert.assertEquals("GND", classic.getSilkPinLabel(3));
    Assert.assertEquals("GND", classic.getSilkPinLabel(16));
    Assert.assertEquals("RST", classic.getSilkPinLabel(2));
    Assert.assertEquals("RST", classic.getSilkPinLabel(17));

    // the 2x3 block belongs to the classic alone; the later boards use that end of the PCB for the
    // radio module or the USB bridge, and the R4 adds a point at the centre of its Qwiic socket
    for (NanoVersion version : NanoVersion.values()) {
      Assert.assertEquals(version + " pin count", expectedPinCount(version),
          version(version).getControlPointCount());
    }

    // the classic leaves the bus functions off A4 / A5, every later board annotates them, and the
    // Nano ESP32 carries the Espressif GPIO number as well
    Assert.assertEquals("A5", classic.getControlPointNodeName(21));
    Assert.assertEquals("A5 (SCL)", version(NanoVersion.EVERY).getControlPointNodeName(21));
    Assert.assertEquals("A4 (~, SDA/GPIO11)",
        version(NanoVersion.NANO_ESP32).getControlPointNodeName(22));

    // the pad the classic repeats RESET on carries the boot mode selector on the later boards
    Assert.assertEquals("REC", version(NanoVersion.NANO_RP2040_CONNECT).getSilkPinLabel(17));
    Assert.assertEquals("BOOT", version(NanoVersion.NANO_R4).getSilkPinLabel(17));
    Assert.assertEquals("B1", version(NanoVersion.NANO_ESP32).getSilkPinLabel(17));
    Assert.assertEquals("B0", version(NanoVersion.NANO_ESP32).getSilkPinLabel(27));

    // the R4 reverses JP1 and doubles D4 / D5 as the CAN pins
    ArduinoNano r4 = version(NanoVersion.NANO_R4);
    Assert.assertEquals("D4 (CAN TX)", r4.getControlPointNodeName(6));
    Assert.assertEquals("A0 (DAC)", r4.getControlPointNodeName(26));
    Assert.assertEquals("QWIIC", r4.getControlPointNodeName(30));
  }

  @Test
  public void testGeometryIsVersionIndependent() {
    ArduinoNano classic = new ArduinoNano();

    MakerBoardTestSupport.assertRow(classic, 0, 14);
    MakerBoardTestSupport.assertRow(classic, 15, 29);
    MakerBoardTestSupport.assertRowSpacing(classic, 0, 15, new Size(0.60d, SizeUnit.in));
    MakerBoardTestSupport.assertBoardSize(classic, new Size(0.73d, SizeUnit.in),
        new Size(1.70d, SizeUnit.in));
    Assert.assertEquals("Margin above the first pin",
        new Size(0.15d, SizeUnit.in).convertToPixels(),
        classic.getControlPoint(0).getY() - classic.getBodyShape().getBounds2D().getY(), 0.1);

    // every Nano keeps the same outline and the same 2x15 pin field; anything past it is
    // board-specific
    for (NanoVersion version : NanoVersion.values()) {
      MakerBoardTestSupport.assertSharedFootprint(classic, version(version), 30);
    }
  }

  @Test
  public void testEveryVersionDraws() {
    for (NanoVersion version : NanoVersion.values()) {
      ArduinoNano nano = version(version);
      for (boolean headers : new boolean[] {false, true}) {
        nano.setHeaders(headers);
        MakerBoardTestSupport.assertDrawsCleanly(nano);
      }
    }
  }

  private static ArduinoNano version(NanoVersion version) {
    ArduinoNano nano = new ArduinoNano();
    nano.setVersion(version);
    return nano;
  }

  /** 36 for the classic with its ICSP block, 31 for the R4 with its Qwiic point, 30 otherwise. */
  private static int expectedPinCount(NanoVersion version) {
    switch (version) {
      case CLASSIC:
        return 36;
      case NANO_R4:
        return 31;
      default:
        return 30;
    }
  }
}
