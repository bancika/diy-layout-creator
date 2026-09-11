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
package org.diylc.netlist;

import org.junit.Assert;
import org.junit.Test;

public class NodeNameTest {

  @Test
  public void testSanitizeNodeName() {
    // The annotation in parentheses is stripped, the pin name is kept
    Assert.assertEquals("D11", Node.sanitizeNodeName("D11 (~, MOSI)"));
    Assert.assertEquals("A5", Node.sanitizeNodeName("A5 (SCL)"));
    Assert.assertEquals("D3", Node.sanitizeNodeName("D3 (~)"));
    Assert.assertEquals("VIN", Node.sanitizeNodeName("VIN (3.6-5.5V)"));
    Assert.assertEquals("GPIO2/SDA", Node.sanitizeNodeName("GPIO2/SDA (Pin 3)"));

    // Names without an annotation are untouched
    Assert.assertEquals("VIN", Node.sanitizeNodeName("VIN"));
    Assert.assertEquals("Tip", Node.sanitizeNodeName("Tip"));
    Assert.assertEquals("3.3V", Node.sanitizeNodeName("3.3V"));

    // Trailing disambiguators stay: these mark genuinely different pins
    Assert.assertEquals("GND_1", Node.sanitizeNodeName("GND_1"));
    Assert.assertEquals("5V_ICSP", Node.sanitizeNodeName("5V_ICSP"));

    // Degenerate input never leaves a pin nameless
    Assert.assertEquals("(only annotation)", Node.sanitizeNodeName("(only annotation)"));
    Assert.assertNull(Node.sanitizeNodeName(null));
  }
}
