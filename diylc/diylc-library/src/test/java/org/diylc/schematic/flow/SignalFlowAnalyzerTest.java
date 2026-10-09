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
package org.diylc.schematic.flow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.diylc.components.guitar.SingleCoilPickup;
import org.diylc.components.modules.LM2596BuckConverter;
import org.diylc.components.passive.Resistor;
import org.diylc.components.semiconductors.BJTSymbol;
import org.diylc.components.semiconductors.TransistorPinout;
import org.diylc.components.semiconductors.TransistorTO92;
import org.diylc.components.tube.Jumbo4PinTubeSocket;
import org.diylc.components.tube.TriodeSymbol;
import org.diylc.components.tube.TubeSocket;
import org.diylc.core.IDIYComponent;
import org.junit.Test;

public class SignalFlowAnalyzerTest {

  private static final TransistorPinoutFlowRule PINOUT_RULE = new TransistorPinoutFlowRule();

  @Test
  public void bjtPinoutMapsBaseToInputAndCollectorToOutput() {
    FlowHints hints = evaluatePinout(TransistorPinout.BJT_EBC);

    assertNotNull(hints);
    assertEquals(FlowRole.NEUTRAL, hints.getRole(0)); // emitter
    assertEquals(FlowRole.INPUT, hints.getRole(1)); // base
    assertEquals(FlowRole.OUTPUT, hints.getRole(2)); // collector
  }

  @Test
  public void pinoutIsReadInTheDeclaredLegOrder() {
    FlowHints hints = evaluatePinout(TransistorPinout.BJT_CBE);

    assertNotNull(hints);
    assertEquals(FlowRole.OUTPUT, hints.getRole(0)); // collector
    assertEquals(FlowRole.INPUT, hints.getRole(1)); // base
    assertEquals(FlowRole.NEUTRAL, hints.getRole(2)); // emitter
  }

  @Test
  public void fetGateIsAnInputAndDrainIsAnOutput() {
    FlowHints hints = evaluatePinout(TransistorPinout.JFET_GSD);

    assertNotNull(hints);
    assertEquals(FlowRole.INPUT, hints.getRole(0)); // gate
    assertEquals(FlowRole.NEUTRAL, hints.getRole(1)); // source
    assertEquals(FlowRole.OUTPUT, hints.getRole(2)); // drain
  }

  /**
   * G is the gate of a FET but the ground leg of a regulator. Reading it the same way in both cases
   * would turn every regulator around, so this is the case most worth pinning down.
   */
  @Test
  public void regulatorGroundIsNotTreatedAsAGate() {
    FlowHints hints = evaluatePinout(TransistorPinout.REGULATOR_IGO);

    assertNotNull(hints);
    assertEquals(FlowRole.INPUT, hints.getRole(0));
    assertEquals(FlowRole.NEUTRAL, hints.getRole(1)); // ground, not an input
    assertEquals(FlowRole.OUTPUT, hints.getRole(2));
  }

  @Test
  public void aTransistorWithNoPinoutSaysNothing() {
    TransistorTO92 transistor = new TransistorTO92();
    transistor.setPinout(null);

    assertNull(PINOUT_RULE.evaluate(transistor, null));
  }

  @Test
  public void tubeSymbolGridIsInputAndPlateIsOutput() {
    FlowHints hints = new TubeSymbolFlowRule().evaluate(new TriodeSymbol(), null);

    assertNotNull(hints);
    assertEquals(FlowRole.INPUT, hints.getRole(0)); // control grid
    assertEquals(FlowRole.OUTPUT, hints.getRole(1)); // plate
    assertEquals(FlowRole.NEUTRAL, hints.getRole(2)); // cathode
  }

  @Test
  public void aTubeSocketWithNumberedPinsSaysNothing() {
    // the stock electrode labels are just pin numbers, which carry no electrode meaning
    assertNull(new TubeElectrodeFlowRule().evaluate(new TubeSocket(), null));
  }

  @Test
  public void aTubeSocketWithNamedElectrodesGivesDirection() {
    Jumbo4PinTubeSocket socket = new Jumbo4PinTubeSocket();

    FlowHints hints = new TubeElectrodeFlowRule().evaluate(socket, null);

    assertNotNull(hints);
    assertTrue(hints.hasDirection());
    assertRoleOfNamedPin(socket, hints, "G", FlowRole.INPUT);
    assertRoleOfNamedPin(socket, hints, "P", FlowRole.OUTPUT);
    assertRoleOfNamedPin(socket, hints, "F", FlowRole.NEUTRAL); // filament
  }

  @Test
  public void pickupTerminalsAreOutputsBecauseTheSignalStartsThere() {
    SingleCoilPickup pickup = new SingleCoilPickup();

    FlowHints hints = new PickupFlowRule().evaluate(pickup, null);

    assertNotNull(hints);
    assertTrue(hints.hasDirection());
    for (int i = 0; i < pickup.getControlPointCount(); i++) {
      FlowRole expected = pickup.isControlPointSticky(i) ? FlowRole.OUTPUT : FlowRole.NEUTRAL;
      assertEquals("point " + i, expected, hints.getRole(i));
    }
  }

  @Test
  public void namedPinsGiveDirectionOnModulesThatLabelThem() {
    LM2596BuckConverter module = new LM2596BuckConverter();

    FlowHints hints = new NamedPinFlowRule().evaluate(module, null);

    assertNotNull(hints);
    assertRoleOfNamedPin(module, hints, "IN+", FlowRole.INPUT);
    assertRoleOfNamedPin(module, hints, "IN-", FlowRole.INPUT);
    assertRoleOfNamedPin(module, hints, "OUT+", FlowRole.OUTPUT);
    assertRoleOfNamedPin(module, hints, "OUT-", FlowRole.OUTPUT);
  }

  @Test
  public void drawnTransistorSymbolsFallBackToTheirGeometry() {
    FlowHints hints = new BjtSymbolFlowRule().evaluate(new BJTSymbol(), null);

    assertNotNull(hints);
    assertEquals(FlowRole.INPUT, hints.getRole(0)); // base, the lone pin on the left
    assertEquals(FlowRole.OUTPUT, hints.getRole(1)); // collector
    assertEquals(FlowRole.NEUTRAL, hints.getRole(2)); // emitter
  }

  @Test
  public void passivePartsGetNoDirectionAtAll() {
    assertNull(new SignalFlowAnalyzer().analyze(new Resistor()));
  }

  @Test
  public void rulesThatReadRealDataComeBeforeRulesThatGuess() {
    List<ISignalFlowRule> rules = SignalFlowAnalyzer.getRules();

    assertTrue("the pin-out rule must outrank the pin-name rule",
        indexOf(rules, TransistorPinoutFlowRule.class) < indexOf(rules, NamedPinFlowRule.class));
    assertTrue("named electrodes must outrank bare geometry",
        indexOf(rules, TubeElectrodeFlowRule.class) < indexOf(rules, TubeSymbolFlowRule.class));
  }

  private static FlowHints evaluatePinout(TransistorPinout pinout) {
    TransistorTO92 transistor = new TransistorTO92();
    transistor.setPinout(pinout);
    return PINOUT_RULE.evaluate(transistor, null);
  }

  /** Finds the pins the component reports under {@code pinName} and checks the role each was given. */
  private static void assertRoleOfNamedPin(IDIYComponent<?> component, FlowHints hints,
      String pinName, FlowRole expected) {
    boolean found = false;
    for (int i = 0; i < component.getControlPointCount(); i++) {
      if (pinName.equals(component.getControlPointNodeName(i))) {
        assertEquals("pin " + pinName + " at index " + i, expected, hints.getRole(i));
        found = true;
      }
    }
    assertTrue("expected a pin named " + pinName, found);
  }

  private static int indexOf(List<ISignalFlowRule> rules, Class<? extends ISignalFlowRule> type) {
    for (int i = 0; i < rules.size(); i++) {
      if (type.isInstance(rules.get(i))) {
        return i;
      }
    }
    return -1;
  }
}
