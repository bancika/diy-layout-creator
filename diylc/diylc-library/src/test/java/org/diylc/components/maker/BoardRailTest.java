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
package org.diylc.components.maker;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Node;
import org.diylc.components.displays.WS2812BStrip;
import org.diylc.components.micro.ArduinoUno;
import org.diylc.components.micro.ESP32DevKit;
import org.diylc.components.micro.ESP8266NodeMCU;
import org.diylc.components.micro.RaspberryPi;
import org.diylc.components.micro.RaspberryPiPico;
import org.diylc.components.micro.Teensy;

/**
 * Pins a board joins inside itself, as the netlist decides them through
 * {@link Node#railName(String)}.
 * <p>
 * The negative cases carry the weight here. An underscore does three unrelated jobs across these
 * pinouts - it disambiguates a repeated rail, it namespaces a connector's signals, and it qualifies
 * a separate net - so a rule keyed on the text before the underscore would short an Ethernet or USB
 * header together. Each of these was produced by a sweep of every board and variant.
 *
 * @author Branislav Stojkovic
 */
public class BoardRailTest {

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  @Test
  public void repeatedRailsAreOneNode() throws Exception {
    ESP32DevKit esp = new ESP32DevKit();
    assertJoined(esp, "GND_1", "GND_2");

    RaspberryPi pi = new RaspberryPi();
    assertJoined(pi, "GND (Pin 6)", "GND (Pin 9)");
    assertJoined(pi, "5V (Pin 2)", "5V (Pin 4)");
    assertJoined(pi, "3.3V (Pin 1)", "3.3V (Pin 17)");

    RaspberryPiPico pico = new RaspberryPiPico();
    assertJoined(pico, "GND_1", "GND_8");

    WS2812BStrip strip = new WS2812BStrip();
    assertJoined(strip, "GND_1", "GND_2");
    assertJoined(strip, "+5V_1", "+5V_2");
  }

  @Test
  public void annotatedRailsAreOneNode() throws Exception {
    Teensy teensy = new Teensy();
    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_1);
    assertJoined(teensy, "GND_1", "GND_4 (Mid)");
    assertJoined(teensy, "3.3V_1", "3.3V_2 (250mA)");
  }

  @Test
  public void connectorSignalsSharingAPrefixAreNotJoined() throws Exception {
    Teensy teensy = new Teensy();
    teensy.setVersion(Teensy.TeensyVersion.Teensy_4_1);
    assertSeparate(teensy, "ETH_TX-", "ETH_RX+");
    assertSeparate(teensy, "ETH_TX-", "ETH_GND");
    assertSeparate(teensy, "USB_D-", "USB_D+");
    assertSeparate(teensy, "USB_5V", "USB_GND1");
  }

  @Test
  public void qualifiedNamesAreSeparateNets() throws Exception {
    ArduinoUno uno = new ArduinoUno();
    uno.setVersion(ArduinoUno.ArduinoUnoVersion.REV3);
    // the USB bridge has its own ICSP header; it is not the main MCU's SPI
    assertSeparate(uno, "MISO", "MISO_16U2");
    assertSeparate(uno, "SCK", "SCK_16U2");

    RaspberryPiPico pico = new RaspberryPiPico();
    // the regulator's enable input is not its output
    assertSeparate(pico, "3V3_EN", "3V3 (OUT)");
  }

  @Test
  public void absentConnectionsAreNotANet() throws Exception {
    ESP32DevKit esp = new ESP32DevKit();
    esp.setVersion(ESP32DevKit.DevKitVersion.ESP32_C6_DevKitC_1);
    assertSeparate(esp, "NC_1", "NC_2");

    ESP8266NodeMCU nodeMcu = new ESP8266NodeMCU();
    assertSeparate(nodeMcu, "RSV_1", "RSV_2");
  }

  @Test
  public void signalPinsAreNeverJoined() throws Exception {
    RaspberryPiPico pico = new RaspberryPiPico();
    assertSeparate(pico, "GP0", "GP1");
    assertSeparate(pico, "GP0", "GP28");
  }

  private static void assertJoined(IDIYComponent<?> board, String first, String second) {
    Assert.assertTrue(board.getClass().getSimpleName() + ": \"" + first + "\" and \"" + second
        + "\" should be one node",
        sameRail(board, indexOf(board, first), indexOf(board, second)));
  }

  private static void assertSeparate(IDIYComponent<?> board, String first, String second) {
    Assert.assertFalse(board.getClass().getSimpleName() + ": \"" + first + "\" and \"" + second
        + "\" should stay separate nodes",
        sameRail(board, indexOf(board, first), indexOf(board, second)));
  }

  private static boolean sameRail(IDIYComponent<?> board, int index1, int index2) {
    String rail = Node.railName(board.getControlPointNodeName(index1));
    return rail != null && rail.equals(Node.railName(board.getControlPointNodeName(index2)));
  }

  private static int indexOf(IDIYComponent<?> board, String nodeName) {
    for (int i = 0; i < board.getControlPointCount(); i++) {
      if (nodeName.equals(board.getControlPointNodeName(i))) {
        return i;
      }
    }
    throw new IllegalArgumentException(
        board.getClass().getSimpleName() + " has no pin named \"" + nodeName + "\"");
  }
}
