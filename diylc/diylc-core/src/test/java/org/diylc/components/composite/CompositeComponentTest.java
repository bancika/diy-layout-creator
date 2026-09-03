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
package org.diylc.components.composite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.UUID;

import org.diylc.common.Percentage;
import org.diylc.core.IDIYComponent;
import org.diylc.netlist.Node;
import org.diylc.serialization.ProjectFileManager;
import org.diylc.testcomponents.TwoPointTestComponent;
import org.junit.Test;

/**
 * Exercises the delegation, rigidity, cloning and equality behavior described in
 * {@code docs/plans/composite-building-blocks.md}.
 */
public class CompositeComponentTest {

  private CompositeComponent twoChildComposite() {
    CompositeComponent composite = new CompositeComponent();
    composite.setName("BLK1");
    composite.initBlockName("Test Block");
    composite.getChildComponents().add(new TwoPointTestComponent("R1",
        new Point2D.Double(0, 0), new Point2D.Double(10, 0)));
    composite.getChildComponents().add(new TwoPointTestComponent("R2",
        new Point2D.Double(20, 0), new Point2D.Double(30, 0)));
    return composite;
  }

  @Test
  public void controlPointCountIsSumOverChildren() {
    CompositeComponent composite = twoChildComposite();
    assertEquals(4, composite.getControlPointCount());
  }

  @Test
  public void controlPointsMapToTheRightChildAndLocalIndex() {
    CompositeComponent composite = twoChildComposite();

    assertEquals(new Point2D.Double(0, 0), composite.getControlPoint(0));
    assertEquals(new Point2D.Double(10, 0), composite.getControlPoint(1));
    assertEquals(new Point2D.Double(20, 0), composite.getControlPoint(2));
    assertEquals(new Point2D.Double(30, 0), composite.getControlPoint(3));
  }

  @Test
  public void setControlPointDelegatesToTheOwningChild() {
    CompositeComponent composite = twoChildComposite();

    composite.setControlPoint(new Point2D.Double(99, 99), 2);

    assertEquals(new Point2D.Double(99, 99), composite.getControlPoint(2));
    // the sibling child's points must be untouched
    assertEquals(new Point2D.Double(30, 0), composite.getControlPoint(3));
    TwoPointTestComponent r2 = (TwoPointTestComponent) composite.getChildComponents().get(1);
    assertEquals(new Point2D.Double(99, 99), r2.getControlPoint(0));
  }

  @Test
  public void stickinessIsDelegatedPerChildPoint() {
    CompositeComponent composite = twoChildComposite();
    ((TwoPointTestComponent) composite.getChildComponents().get(0)).setSticky(1, false);

    assertTrue(composite.isControlPointSticky(0));
    assertFalse(composite.isControlPointSticky(1));
    assertTrue(composite.isControlPointSticky(2));
    assertTrue(composite.isControlPointSticky(3));
  }

  @Test
  public void isAlwaysRigid() {
    CompositeComponent composite = twoChildComposite();
    for (int i = 0; i < composite.getControlPointCount(); i++)
      assertFalse("point " + i + " should not move freely", composite.canPointMoveFreely(i));
  }

  @Test
  public void nodeNamingProducesHierarchicalNetlistLabels() {
    // Design decision D3: Node.toString() should read "<composite>.<child>.<local point name>"
    CompositeComponent composite = twoChildComposite();

    Node r1FirstPoint = new Node(composite, 0);
    Node r2FirstPoint = new Node(composite, 2);
    Node r2SecondPoint = new Node(composite, 3);

    assertEquals("BLK1.R1.1", r1FirstPoint.toString());
    assertEquals("BLK1.R2.1", r2FirstPoint.toString());
    // local point index must reset per child, not keep growing with the flat composite index
    assertEquals("BLK1.R2.2", r2SecondPoint.toString());
  }

  @Test
  public void cloneDeepCopiesChildrenSoInstancesAreIndependent() throws Exception {
    CompositeComponent original = twoChildComposite();

    IDIYComponent<?> clone = original.clone();

    ((CompositeComponent) clone).setControlPoint(new Point2D.Double(-1, -1), 0);

    assertEquals(new Point2D.Double(0, 0), original.getControlPoint(0));
    assertEquals(new Point2D.Double(-1, -1), clone.getControlPoint(0));
  }

  @Test
  public void cloneKeepsBothItsOwnAndChildrenIds() throws Exception {
    // Matches AbstractComponent.clone()'s contract of preserving identity by default - a caller
    // that needs a genuinely new component (e.g. BuildingBlockManager placing a fresh instance)
    // reassigns ids itself, exactly as it does for any other component's clone().
    CompositeComponent original = twoChildComposite();
    UUID originalChildId = original.getChildComponents().get(0).getId();

    CompositeComponent clone = (CompositeComponent) original.clone();

    assertEquals(original.getId(), clone.getId());
    assertEquals(originalChildId, clone.getChildComponents().get(0).getId());
  }

  @Test
  public void equalsToComparesStructureNotObjectIdentity() throws Exception {
    CompositeComponent original = twoChildComposite();
    CompositeComponent clone = (CompositeComponent) original.clone();

    // structurally identical, distinct child objects - must still compare equal
    assertTrue(original.equalsTo(clone));

    clone.setControlPoint(new Point2D.Double(-1, -1), 0);
    assertFalse(original.equalsTo(clone));
  }

  @Test
  public void equalsToRejectsDifferentBlockName() {
    CompositeComponent a = twoChildComposite();
    CompositeComponent b = twoChildComposite();
    b.initBlockName("Different Block");

    assertFalse(a.equalsTo(b));
  }

  @Test
  public void defaultsToFullyOpaque() {
    assertEquals(new Percentage(100), twoChildComposite().getAlpha());
  }

  @Test
  public void cloneCopiesAlpha() throws Exception {
    CompositeComponent original = twoChildComposite();
    original.setAlpha(new Percentage(40));

    CompositeComponent clone = (CompositeComponent) original.clone();

    assertEquals(new Percentage(40), clone.getAlpha());
  }

  @Test
  public void equalsToRejectsDifferentAlpha() throws Exception {
    CompositeComponent original = twoChildComposite();
    CompositeComponent clone = (CompositeComponent) original.clone();
    assertTrue(original.equalsTo(clone));

    clone.setAlpha(new Percentage(50));
    assertFalse(original.equalsTo(clone));
  }

  @Test
  public void survivesAnXStreamRoundTripAndRebuildsItsIndexLazily() {
    CompositeComponent original = twoChildComposite();

    String xml = ProjectFileManager.xStreamSerializer.toXML(original);
    CompositeComponent reloaded = (CompositeComponent) ProjectFileManager.xStreamSerializer.fromXML(xml);

    // the transient child-index arrays are never serialized - this call exercises the lazy
    // rebuild in buildIndexIfNeeded() on an instance XStream constructed without going through
    // the no-arg constructor
    assertEquals(original.getControlPointCount(), reloaded.getControlPointCount());
    assertEquals(original.getName(), reloaded.getName());
    assertEquals(original.getBlockName(), reloaded.getBlockName());
    for (int i = 0; i < original.getControlPointCount(); i++)
      assertEquals(original.getControlPoint(i), reloaded.getControlPoint(i));
    assertEquals("BLK1.R2.2", new Node(reloaded, 3).toString());
  }

  @Test
  public void drawIconDoesNotTouchChildren() {
    // ComponentProcessor draws the palette icon on a default-constructed, childless instance
    CompositeComponent blank = new CompositeComponent();
    BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = image.createGraphics();

    blank.drawIcon(g2d, 32, 32);

    g2d.dispose();
    // no exception means components list (empty) was never dereferenced as if populated
    assertNotNull(blank.getChildComponents());
    assertTrue(blank.getChildComponents().isEmpty());
  }
}
