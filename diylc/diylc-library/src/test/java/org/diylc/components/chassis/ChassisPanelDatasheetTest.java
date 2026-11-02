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
package org.diylc.components.chassis;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.diylc.components.chassis.ChassisPanel.Sides;
import org.diylc.core.IDIYComponent;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.DatasheetService;
import org.diylc.serialization.ProjectFileManager;
import org.diylc.utils.BomEntry;
import org.diylc.utils.BomMaker;
import org.junit.Assert;
import org.junit.Test;

public class ChassisPanelDatasheetTest {

  private static final double TOLERANCE = 1e-6;

  private static String[] row(String model) {
    return DatasheetService.getInstance().loadDatasheet(ChassisPanel.class).stream()
        .filter(row -> row[1].equals(model)).findFirst().get();
  }

  private static double mm(double value) {
    return new Size(value, SizeUnit.mm).convertToPixels();
  }

  private static void assertRect(Rectangle2D expected, Rectangle2D actual) {
    Assert.assertEquals(expected.getX(), actual.getX(), TOLERANCE);
    Assert.assertEquals(expected.getY(), actual.getY(), TOLERANCE);
    Assert.assertEquals(expected.getWidth(), actual.getWidth(), TOLERANCE);
    Assert.assertEquals(expected.getHeight(), actual.getHeight(), TOLERANCE);
  }

  @Test
  public void everyRowApplies() {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(ChassisPanel.class);
    Assert.assertNotNull(datasheet);
    Assert.assertFalse(datasheet.isEmpty());
    for (String[] row : datasheet) {
      Assert.assertEquals(String.join(",", row), 5, row.length);
      ChassisPanel enclosure = new ChassisPanel(row);
      Assert.assertEquals(row[1], enclosure.getValue());
      Assert.assertEquals(row[1], Size.parseSize(row[4]), enclosure.getHeight());
      Point2D first = enclosure.getControlPoint(0);
      Point2D second = enclosure.getControlPoint(1);
      Assert.assertEquals(row[1], Size.parseSize(row[2]).convertToPixels(),
          second.getX() - first.getX(), TOLERANCE);
      Assert.assertEquals(row[1], Size.parseSize(row[3]).convertToPixels(),
          second.getY() - first.getY(), TOLERANCE);
    }
  }

  @Test
  public void aNewEnclosureIsA125BWithAllSides() {
    ChassisPanel enclosure = new ChassisPanel();
    ChassisPanel fromDatasheet = new ChassisPanel(row("125B"));

    Assert.assertEquals(fromDatasheet.getValue(), enclosure.getValue());
    Assert.assertEquals(fromDatasheet.getHeight(), enclosure.getHeight());
    Assert.assertEquals(fromDatasheet.getEdgeRadius(), enclosure.getEdgeRadius());
    Assert.assertEquals(fromDatasheet.getControlPoint(1), enclosure.getControlPoint(1));
    Assert.assertEquals(Sides.ALL, enclosure.getSides());
    Assert.assertEquals(4, enclosure.getSidePanels().length);
  }

  @Test
  public void pedalSizedBoxesAreUprightAndBiggerOnesHorizontal() {
    for (String model : new String[] {"125B", "1590A", "1590B", "1590BB", "1550B"}) {
      Point2D second = new ChassisPanel(row(model)).getControlPoint(1);
      Assert.assertTrue(model, second.getY() > second.getX());
    }
    for (String model : new String[] {"1590XX", "1590DD", "1441-10", "1444-32"}) {
      Point2D second = new ChassisPanel(row(model)).getControlPoint(1);
      Assert.assertTrue(model, second.getX() > second.getY());
    }
  }

  @Test
  public void modelNamesAreUnique() {
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(ChassisPanel.class);
    Assert.assertEquals(datasheet.size(),
        datasheet.stream().map(row -> row[1]).distinct().count());
  }

  @Test
  public void modelLeavesTheRadiusAlone() {
    ChassisPanel enclosure = new ChassisPanel();
    enclosure.setEdgeRadius(new Size(3d, SizeUnit.mm));

    enclosure.applyModel(row("1590BB"));

    Assert.assertEquals(new Size(3d, SizeUnit.mm), enclosure.getEdgeRadius());
  }

  @Test
  public void inchRowsKeepTheirUnit() {
    ChassisPanel enclosure = new ChassisPanel(row("1444-32"));
    Assert.assertEquals(new Size(3d, SizeUnit.in), enclosure.getHeight());
    Point2D first = enclosure.getControlPoint(0);
    Point2D second = enclosure.getControlPoint(1);
    Assert.assertEquals(new Size(17d, SizeUnit.in).convertToPixels(),
        second.getX() - first.getX(), TOLERANCE);
    Assert.assertEquals(new Size(10d, SizeUnit.in).convertToPixels(),
        second.getY() - first.getY(), TOLERANCE);
  }

  @Test
  public void sidesUnfoldAroundTheTopPanel() {
    ChassisPanel enclosure = new ChassisPanel(row("1590BB"));

    Rectangle2D[] panels = enclosure.getSidePanels();

    Assert.assertEquals(4, panels.length);
    assertRect(new Rectangle2D.Double(0, -mm(30), mm(94), mm(30)), panels[0]);
    assertRect(new Rectangle2D.Double(0, mm(119.5), mm(94), mm(30)), panels[1]);
    assertRect(new Rectangle2D.Double(-mm(30), 0, mm(30), mm(119.5)), panels[2]);
    assertRect(new Rectangle2D.Double(mm(94), 0, mm(30), mm(119.5)), panels[3]);
  }

  @Test
  public void spacingSetsTheSidesApart() {
    ChassisPanel enclosure = new ChassisPanel(row("1590BB"));
    enclosure.setSpacing(new Size(2d, SizeUnit.mm));

    Rectangle2D[] panels = enclosure.getSidePanels();

    assertRect(new Rectangle2D.Double(0, -mm(32), mm(94), mm(30)), panels[0]);
    assertRect(new Rectangle2D.Double(0, mm(121.5), mm(94), mm(30)), panels[1]);
    assertRect(new Rectangle2D.Double(-mm(32), 0, mm(30), mm(119.5)), panels[2]);
    assertRect(new Rectangle2D.Double(mm(96), 0, mm(30), mm(119.5)), panels[3]);
  }

  @Test
  public void frontAndBackOnly() {
    ChassisPanel enclosure = new ChassisPanel(row("1590BB"));
    enclosure.setSides(Sides.FRONT_AND_BACK);

    Rectangle2D[] panels = enclosure.getSidePanels();

    Assert.assertEquals(2, panels.length);
    assertRect(new Rectangle2D.Double(0, -mm(30), mm(94), mm(30)), panels[0]);
    assertRect(new Rectangle2D.Double(0, mm(119.5), mm(94), mm(30)), panels[1]);
  }

  @Test
  public void noSidesKeepsTheModelButDrawsOnePanel() {
    ChassisPanel enclosure = new ChassisPanel(row("1590BB"));
    enclosure.setSides(Sides.NONE);

    Assert.assertEquals(0, enclosure.getSidePanels().length);
    Assert.assertEquals(new Size(30d, SizeUnit.mm), enclosure.getHeight());
  }

  @Test
  public void sidesFollowTheTopPanelWhenItIsStretched() {
    ChassisPanel enclosure = new ChassisPanel(row("1590BB"));
    enclosure.setControlPoint(new Point2D.Double(mm(200), mm(100)), 1);

    Rectangle2D[] panels = enclosure.getSidePanels();

    assertRect(new Rectangle2D.Double(0, mm(100), mm(200), mm(30)), panels[1]);
    assertRect(new Rectangle2D.Double(mm(200), 0, mm(30), mm(100)), panels[3]);
  }

  @Test
  public void withoutHeightItIsASinglePanel() {
    ChassisPanel enclosure = new ChassisPanel();
    enclosure.setHeight(new Size(0d, SizeUnit.mm));
    Assert.assertEquals(0, enclosure.getSidePanels().length);

    enclosure.setHeight(null);
    Assert.assertEquals(0, enclosure.getSidePanels().length);
  }

  @Test
  public void panelFromAnOldFileIsASinglePanel() {
    // a panel from before the datasheet: no model, a radius of 0 and a free-form size
    ChassisPanel original = new ChassisPanel();
    original.setValue("");
    original.setEdgeRadius(new Size(0d, SizeUnit.mm));
    original.setControlPoint(new Point2D.Double(20, 30), 1);
    String xml = ProjectFileManager.xStreamSerializer.toXML(original)
        .replaceAll("\\s*<height[^>]*/>", "").replaceAll("\\s*<height>.*?</height>", "")
        .replaceAll("\\s*<sides>[^<]*</sides>", "")
        .replaceAll("\\s*<spacing[^>]*/>", "").replaceAll("\\s*<spacing>.*?</spacing>", "");
    Assert.assertFalse(xml, xml.contains("<height"));
    Assert.assertFalse(xml, xml.contains("<sides>"));
    Assert.assertFalse(xml, xml.contains("<spacing"));

    ChassisPanel reloaded = (ChassisPanel) ProjectFileManager.xStreamSerializer.fromXML(xml);

    Assert.assertNull(reloaded.getHeight());
    Assert.assertEquals(Sides.ALL, reloaded.getSides());
    Assert.assertEquals(new Size(0d, SizeUnit.mm), reloaded.getSpacing());
    Assert.assertEquals(0, reloaded.getSidePanels().length);
    Assert.assertEquals("", reloaded.getValueForDisplay());
    Assert.assertEquals(new Size(0d, SizeUnit.mm), reloaded.getEdgeRadius());
    Assert.assertEquals(new Point2D.Double(20, 30), reloaded.getControlPoint(1));
  }

  @Test
  public void bomListsEnclosuresByModel() {
    List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>();
    components.add(new ChassisPanel(row("1590BB")));
    components.add(new ChassisPanel(row("1590BB")));
    components.add(new ChassisPanel(row("1590B")));

    List<BomEntry> bom = BomMaker.getInstance().createBom(components);

    Assert.assertEquals(2, bom.size());
    BomEntry bb = bom.stream().filter(e -> "1590BB".equals(e.getValue())).findFirst().get();
    Assert.assertEquals("Enclosure", bb.getType());
    Assert.assertEquals(Integer.valueOf(2), bb.getQuantity());
  }
}
