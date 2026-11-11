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
package org.diylc.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.diylc.common.PCBLayer;
import org.diylc.components.misc.Label;
import org.diylc.core.IBoardSideComponent;
import org.diylc.core.IDIYComponent;
import org.diylc.core.gerber.GerberLayer;
import org.diylc.core.gerber.IGerberComponentSimple;
import org.diylc.utils.ReflectionUtils;
import org.junit.Test;

public class BoardSideComponentsTest {

  @Test
  public void testBoardSideComponentsStartOnTop() throws Exception {
    for (IDIYComponent<?> component : createBoardSideComponents()) {
      assertEquals(component.getClass().getName(), PCBLayer._2,
          ((IBoardSideComponent) component).getBoardSide());
    }
  }

  @Test
  public void testBoardSideCanBeChanged() throws Exception {
    for (IDIYComponent<?> component : createBoardSideComponents()) {
      component.getClass().getMethod("setBoardSide", PCBLayer.class).invoke(component, PCBLayer._1);
      assertEquals(component.getClass().getName(), PCBLayer._1,
          ((IBoardSideComponent) component).getBoardSide());
    }
  }

  // a part that writes its silkscreen to the top layer can sit on either side; Label is text rather
  // than a mounted part, and PCBText has a layer of its own
  @Test
  public void testEveryTopSilkscreenPartHasABoardSide() throws Exception {
    for (Class<?> type : ReflectionUtils.getIndexedClasses(IDIYComponent.class)) {
      if (Modifier.isAbstract(type.getModifiers())
          || !IGerberComponentSimple.class.isAssignableFrom(type) || type == Label.class) {
        continue;
      }
      IGerberComponentSimple component =
          (IGerberComponentSimple) type.getDeclaredConstructor().newInstance();
      if (component.getGerberLayer() == GerberLayer.SilkscreenTop) {
        assertTrue(type.getName(), component instanceof IBoardSideComponent);
      }
    }
  }

  private static List<IDIYComponent<?>> createBoardSideComponents() throws Exception {
    List<IDIYComponent<?>> components = new ArrayList<IDIYComponent<?>>();
    for (Class<?> type : ReflectionUtils.getIndexedClasses(IDIYComponent.class)) {
      if (IBoardSideComponent.class.isAssignableFrom(type)
          && !Modifier.isAbstract(type.getModifiers())) {
        components.add((IDIYComponent<?>) type.getDeclaredConstructor().newInstance());
      }
    }
    assertTrue(components.size() > 0);
    return components;
  }
}
