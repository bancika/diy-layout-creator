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
package org.diylc.plugins.chatbot.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.junit.Test;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.ISwitch;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PinoutDriver;
import org.diylc.plugins.chatbot.model.pinout.AiPin;
import org.diylc.plugins.chatbot.model.pinout.AiPinLabels;
import org.diylc.plugins.chatbot.model.pinout.AiPinout;
import org.diylc.plugins.chatbot.model.pinout.AiPinoutVariant;
import org.diylc.plugins.chatbot.model.pinout.AiSwitching;
import org.diylc.plugins.chatbot.model.pinout.AiSwitchingPosition;
import org.diylc.plugins.chatbot.model.pinout.AiTerminals;
import org.diylc.presenter.ComponentProcessor;

public class PinoutAnalyzerTest {

  private AiPinout analyze(Class<? extends IDIYComponent<?>> clazz) {
    return PinoutAnalyzer.analyze(clazz, ComponentProcessor.getInstance());
  }

  @Test
  public void fixedTerminalsAreReportedAsACountAndARule() {
    AiPinout pinout = analyze(TwoLeads.class);

    assertNull("nothing drives the terminals", pinout.drivers());
    assertNull(pinout.variants());
    assertEquals(Integer.valueOf(2), pinout.terminals().pinCount());
    assertEquals(AiPinLabels.SEQUENTIAL, pinout.terminals().labels());
  }

  @Test
  public void labelAnchorIsNotATerminal() {
    AiPinout pinout = analyze(TwoLeadsWithLabelAnchor.class);

    assertEquals("the third, non-sticky point is not a terminal", Integer.valueOf(2),
        pinout.terminals().pinCount());
  }

  @Test
  public void componentWithoutStickyPointsHasNoPinout() {
    assertNull(analyze(Board.class));
  }

  @Test
  public void terminalsThatDoNotStartAtZeroAreListedExplicitly() {
    List<AiPin> pins = analyze(OffsetTerminals.class).terminals().pins();

    assertEquals(2, pins.size());
    assertEquals(1, pins.get(0).index());
    assertEquals(2, pins.get(1).index());
  }

  @Test
  public void unnamedTerminalsAreReportedAsSuch() {
    assertEquals(AiPinLabels.NONE, analyze(UnnamedTerminals.class).terminals().labels());
  }

  @Test
  public void enumPropertyProducesOneVariantPerValue() {
    AiPinout pinout = analyze(SelectableLugCount.class);

    assertEquals(List.of("Lugs"), pinout.drivers());
    List<AiPinoutVariant> variants = pinout.variants();
    assertEquals(2, variants.size());
    assertEquals("TWO", variants.get(0).when().get("Lugs"));
    assertEquals(Integer.valueOf(2), variants.get(0).terminals().pinCount());
    assertEquals("THREE", variants.get(1).when().get("Lugs"));
    assertEquals(Integer.valueOf(3), variants.get(1).terminals().pinCount());
  }

  @Test
  public void booleanPropertyThatRenamesTerminalsIsADriver() {
    AiPinout pinout = analyze(Polarized.class);

    assertEquals(List.of("Polarized"), pinout.drivers());
    List<AiPinoutVariant> variants = pinout.variants();
    assertEquals(2, variants.size());
    assertEquals("false", variants.get(0).when().get("Polarized"));
    assertEquals(AiPinLabels.SEQUENTIAL, variants.get(0).terminals().labels());
    assertEquals("true", variants.get(1).when().get("Polarized"));
    List<AiPin> pins = variants.get(1).terminals().pins();
    assertEquals("+", pins.get(0).label());
    assertEquals("-", pins.get(1).label());
  }

  @Test
  public void twoDriversAreCombined() {
    AiPinout pinout = analyze(TwoDrivers.class);

    assertEquals(List.of("Lugs", "Polarized"), pinout.drivers());
    assertEquals(4, pinout.variants().size());
  }

  @Test
  public void integerPropertyBecomesAFormula() {
    AiPinout pinout = analyze(CountedTerminals.class);

    assertEquals(List.of("Terminals"), pinout.drivers());
    AiTerminals terminals = pinout.terminals();
    assertEquals("2 * Terminals", terminals.pinCountExpression());
    assertEquals(AiPinLabels.SEQUENTIAL, terminals.labels());
    assertEquals(Integer.valueOf(8), terminals.pinCountSamples().get("4"));
  }

  @Test
  public void excludedPropertyIsNotADriver() {
    AiPinout pinout = analyze(ExcludedDriver.class);

    assertNull(pinout.drivers());
    assertEquals(Integer.valueOf(2), pinout.terminals().pinCount());
  }

  @Test
  public void forcedPropertyIsADriverEvenWhenItChangesNothing() {
    AiPinout pinout = analyze(ForcedDriver.class);

    assertEquals(List.of("Lugs"), pinout.drivers());
    assertEquals(2, pinout.variants().size());
  }

  @Test
  public void propertyWhoseSetterRejectsValuesIsSurvivable() {
    AiPinout pinout = analyze(PickyDriver.class);

    assertEquals(List.of("Lugs"), pinout.drivers());
    // FOUR is rejected by the setter, so only the two values that take effect are described
    assertEquals(2, pinout.variants().size());
  }

  @Test
  public void switchCarriesItsSwitchingTable() {
    AiSwitching switching = analyze(SimpleSwitch.class).terminals().switching();

    assertEquals(2, switching.positionCount());
    List<AiSwitchingPosition> positions = switching.positions();
    assertEquals("OFF", positions.get(0).name());
    assertTrue(positions.get(0).connected().isEmpty());
    assertEquals(List.of(List.of(0, 1)), positions.get(1).connected());
  }

  // ----------------------------------------------------------------- stubs

  public abstract static class Stub extends AbstractComponent<String> {

    private static final long serialVersionUID = 1L;

    private String value;

    @Override
    public String getValue() {
      return value;
    }

    @Override
    public void setValue(String value) {
      this.value = value;
    }

    @Override
    public Point2D getControlPoint(int index) {
      return new Point2D.Double(index, 0);
    }

    @Override
    public void setControlPoint(Point2D point, int index) {}

    @Override
    public boolean isControlPointSticky(int index) {
      return true;
    }

    @Override
    public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
      return VisibilityPolicy.NEVER;
    }

    @Override
    public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
        Project project, IDrawingObserver drawingObserver) {}

    @Override
    public void drawIcon(Graphics2D g2d, int width, int height) {}
  }

  public static class TwoLeads extends Stub {

    private static final long serialVersionUID = 1L;

    @Override
    public int getControlPointCount() {
      return 2;
    }
  }

  public static class TwoLeadsWithLabelAnchor extends TwoLeads {

    private static final long serialVersionUID = 1L;

    @Override
    public int getControlPointCount() {
      return 3;
    }

    @Override
    public boolean isControlPointSticky(int index) {
      return index < 2;
    }
  }

  public static class Board extends Stub {

    private static final long serialVersionUID = 1L;

    @Override
    public int getControlPointCount() {
      return 2;
    }

    @Override
    public boolean isControlPointSticky(int index) {
      return false;
    }
  }

  public static class OffsetTerminals extends Stub {

    private static final long serialVersionUID = 1L;

    @Override
    public int getControlPointCount() {
      return 3;
    }

    @Override
    public boolean isControlPointSticky(int index) {
      return index > 0;
    }
  }

  public static class UnnamedTerminals extends TwoLeads {

    private static final long serialVersionUID = 1L;

    @Override
    public String getControlPointNodeName(int index) {
      return null;
    }
  }

  public static class SelectableLugCount extends Stub {

    private static final long serialVersionUID = 1L;

    private LugCount lugs = LugCount.TWO;

    @EditableProperty(name = "Lugs")
    public LugCount getLugs() {
      return lugs;
    }

    public void setLugs(LugCount lugs) {
      this.lugs = lugs;
    }

    @Override
    public int getControlPointCount() {
      return lugs == LugCount.TWO ? 2 : 3;
    }

    public enum LugCount {
      TWO, THREE
    }
  }

  public static class Polarized extends TwoLeads {

    private static final long serialVersionUID = 1L;

    private boolean polarized;

    @EditableProperty(name = "Polarized")
    public boolean getPolarized() {
      return polarized;
    }

    public void setPolarized(boolean polarized) {
      this.polarized = polarized;
    }

    @Override
    public String getControlPointNodeName(int index) {
      if (polarized) {
        return index == 0 ? "+" : "-";
      }
      return super.getControlPointNodeName(index);
    }
  }

  public static class TwoDrivers extends SelectableLugCount {

    private static final long serialVersionUID = 1L;

    private boolean polarized;

    @EditableProperty(name = "Polarized")
    public boolean getPolarized() {
      return polarized;
    }

    public void setPolarized(boolean polarized) {
      this.polarized = polarized;
    }

    @Override
    public String getControlPointNodeName(int index) {
      if (polarized) {
        return index == 0 ? "+" : "-";
      }
      return super.getControlPointNodeName(index);
    }
  }

  public static class CountedTerminals extends Stub {

    private static final long serialVersionUID = 1L;

    private int terminals = 4;

    @EditableProperty(name = "Terminals")
    public int getTerminals() {
      return terminals;
    }

    public void setTerminals(int terminals) {
      this.terminals = terminals;
    }

    @Override
    public int getControlPointCount() {
      return terminals * 2;
    }
  }

  public static class ExcludedDriver extends TwoLeads {

    private static final long serialVersionUID = 1L;

    private boolean extraLug;

    @EditableProperty(name = "Extra Lug", pinoutDriver = PinoutDriver.EXCLUDE)
    public boolean getExtraLug() {
      return extraLug;
    }

    public void setExtraLug(boolean extraLug) {
      this.extraLug = extraLug;
    }

    @Override
    public int getControlPointCount() {
      return extraLug ? 3 : 2;
    }
  }

  public static class ForcedDriver extends TwoLeads {

    private static final long serialVersionUID = 1L;

    private LugStyle lugs = LugStyle.SOLDER;

    @EditableProperty(name = "Lugs", pinoutDriver = PinoutDriver.FORCE)
    public LugStyle getLugs() {
      return lugs;
    }

    public void setLugs(LugStyle lugs) {
      this.lugs = lugs;
    }

    public enum LugStyle {
      SOLDER, SCREW
    }
  }

  public static class PickyDriver extends Stub {

    private static final long serialVersionUID = 1L;

    private LugCount lugs = LugCount.TWO;

    @EditableProperty(name = "Lugs")
    public LugCount getLugs() {
      return lugs;
    }

    public void setLugs(LugCount lugs) {
      if (lugs == LugCount.FOUR) {
        throw new IllegalArgumentException("four lugs are not available on this stub");
      }
      this.lugs = lugs;
    }

    @Override
    public int getControlPointCount() {
      return lugs == LugCount.TWO ? 2 : 3;
    }

    public enum LugCount {
      TWO, THREE, FOUR
    }
  }

  public static class SimpleSwitch extends TwoLeads implements ISwitch {

    private static final long serialVersionUID = 1L;

    private Integer selectedPosition;

    @Override
    public int getPositionCount() {
      return 2;
    }

    @Override
    public String getPositionName(int position) {
      return position == 0 ? "OFF" : "ON";
    }

    @Override
    public boolean arePointsConnected(int index1, int index2, int position) {
      return position == 1;
    }

    @Override
    public void setSelectedPosition(Integer selectedPosition) {
      this.selectedPosition = selectedPosition;
    }

    @Override
    public Integer getSelectedPosition() {
      return selectedPosition;
    }
  }
}
