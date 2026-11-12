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
package org.diylc.presenter;

import static org.junit.Assert.assertEquals;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import org.diylc.common.PCBLayer;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IPCBLayerComponent;
import org.diylc.presenter.DrawingManagerTest.MountedComponent;
import org.diylc.testcomponents.MockDIYComponent;
import org.junit.Test;

public class MirrorPlacementTest {

  @Test
  public void testPlacesOnBottom() {
    MountedComponent part = new MountedComponent(new Rectangle2D.Double(0, 0, 10, 10), PCBLayer._2);
    LayeredComponent copper = new LayeredComponent();
    MockDIYComponent other = new MockDIYComponent();

    MirrorPlacement.placeOnBottom(Arrays.<IDIYComponent<?>>asList(part, copper, other));

    assertEquals(PCBLayer._1, part.getBoardSide());
    assertEquals(PCBLayer._1, copper.getLayer());
  }

  public static class LayeredComponent extends MockDIYComponent implements IPCBLayerComponent {

    private static final long serialVersionUID = 1L;

    private PCBLayer layer = PCBLayer._2;

    @Override
    public PCBLayer getLayer() {
      return layer;
    }

    @Override
    public void setLayer(PCBLayer layer) {
      this.layer = layer;
    }
  }
}
