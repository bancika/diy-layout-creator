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

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.ComponentType;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.presenter.ComponentProcessor;

/**
 * Sweeps every registered maker component across the cross product of its enum properties.
 * <p>
 * The per-class tests pin down the figures each variant was built from, and
 * {@link MakerComponentsTest} covers discovery, icons and rotation. Both of those exercise the
 * default variant only: a component is built with its no-argument constructor and never asked for
 * another version. The variants are where the geometry actually differs, and a release makes
 * control-point counts and positions permanent, so this walks all of them.
 * <p>
 * The properties are swept as a cross product rather than one at a time because a variant is
 * usually a pair - a panel size and an interface, a controller and a glass - and only the
 * combination decides the pin count.
 *
 * @author Branislav Stojkovic
 */
public class MakerVariantSweepTest {

  private static final String[] PACKAGES =
      {"org.diylc.components.displays", "org.diylc.components.micro"};

  /**
   * Orientation is swept inside the drawing check instead of joining the cross product: it moves
   * the control points but cannot change how many there are or what they are called, so folding it
   * in would only report the same defect once per rotation.
   */
  private static final String ORIENTATION = "Orientation";

  private static final Project PROJECT = new Project();

  private static final IDrawingObserver OBSERVER = new IDrawingObserver() {

    @Override
    public void stopTracking() {}

    @Override
    public void startTracking() {}

    @Override
    public void startTrackingContinuityArea(boolean positive) {}

    @Override
    public void stopTrackingContinuityArea() {}

    @Override
    public boolean isTrackingContinuityArea() {
      return false;
    }

    @Override
    public void setContinuityMarker(String marker) {}
  };

  @BeforeClass
  public static void setUp() {
    try {
      ConfigurationManager.getInstance().initialize("diylc");
    } catch (Exception ignored) {
    }
  }

  /**
   * Problems are collected rather than asserted one at a time, so a run reports every bad variant
   * at once instead of stopping at the first.
   */
  @Test
  public void everyVariantHasSoundControlPointsAndDraws() throws Exception {
    List<String> problems = new ArrayList<String>();
    int swept = 0;

    for (Class<? extends IDIYComponent<?>> clazz : makerComponentClasses()) {
      List<PropertyWrapper> enums = new ArrayList<PropertyWrapper>();
      PropertyWrapper orientation = null;
      for (PropertyWrapper property : ComponentProcessor.getInstance().extractProperties(clazz)) {
        if (!property.getType().isEnum()) {
          continue;
        }
        if (ORIENTATION.equals(property.getName())) {
          orientation = property;
        } else {
          enums.add(property);
        }
      }

      int combinations = 1;
      for (PropertyWrapper property : enums) {
        combinations *= property.getType().getEnumConstants().length;
      }

      for (int index = 0; index < combinations; index++) {
        IDIYComponent<?> component = clazz.getDeclaredConstructor().newInstance();
        StringBuilder where = new StringBuilder(clazz.getSimpleName());
        int remaining = index;
        boolean complete = true;

        for (PropertyWrapper property : enums) {
          Object[] values = property.getType().getEnumConstants();
          Object value = values[remaining % values.length];
          remaining /= values.length;
          where.append(" / ").append(property.getName()).append("=").append(value);

          PropertyWrapper applied = (PropertyWrapper) property.clone();
          applied.setValue(value);
          try {
            applied.writeTo(component);
          } catch (Exception e) {
            problems.add(where + ": will not apply -- " + e.getClass().getSimpleName());
            complete = false;
            break;
          }
        }

        if (!complete) {
          continue;
        }
        swept++;

        checkNodeNamesAreUnique(component, where.toString(), problems);
        checkControlPointsAreDistinct(component, where.toString(), problems);
        checkDrawsInEveryOrientation(component, orientation, where.toString(), problems);
      }
    }

    Assert.assertTrue("No maker components were discovered", swept > 0);
    Assert.assertTrue(swept + " variants swept, " + problems.size() + " problem(s):\n"
        + String.join("\n", problems), problems.isEmpty());
  }

  private static void checkNodeNamesAreUnique(IDIYComponent<?> component, String where,
      List<String> problems) {
    Set<String> seen = new HashSet<String>();
    for (int i = 0; i < component.getControlPointCount(); i++) {
      String name = component.getControlPointNodeName(i);
      // A null name is a point that carries no node, which is not a collision.
      if (name != null && !seen.add(name)) {
        problems.add(where + ": duplicate node name \"" + name + "\" at point " + i);
      }
    }
  }

  private static void checkControlPointsAreDistinct(IDIYComponent<?> component, String where,
      List<String> problems) {
    int count = component.getControlPointCount();
    for (int i = 0; i < count; i++) {
      Point2D first = component.getControlPoint(i);
      for (int j = i + 1; j < count; j++) {
        if (first.distance(component.getControlPoint(j)) <= 0.001) {
          problems.add(where + ": control points " + i + " and " + j + " coincide");
        }
      }
    }
  }

  private static void checkDrawsInEveryOrientation(IDIYComponent<?> component,
      PropertyWrapper orientation, String where, List<String> problems)
      throws CloneNotSupportedException {
    Object[] orientations =
        orientation == null ? new Object[] {null} : orientation.getType().getEnumConstants();

    for (Object value : orientations) {
      String at = where;
      if (value != null) {
        at = where + " / " + orientation.getName() + "=" + value;
        PropertyWrapper applied = (PropertyWrapper) orientation.clone();
        applied.setValue(value);
        try {
          applied.writeTo(component);
        } catch (Exception e) {
          problems.add(at + ": will not apply -- " + e.getClass().getSimpleName());
          continue;
        }
      }

      BufferedImage image = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = image.createGraphics();
      // Translating the canvas rather than moving control point 0 keeps the component's own
      // geometry intact: a drag sets every control point, so moving only the first leaves the rest
      // stale and the component draws from a mix of old and new positions.
      g2d.translate(300, 300);
      try {
        component.draw(g2d, ComponentState.NORMAL, false, PROJECT, OBSERVER);
        component.draw(g2d, ComponentState.SELECTED, false, PROJECT, OBSERVER);
        component.draw(g2d, ComponentState.NORMAL, true, PROJECT, OBSERVER);
        component.drawIcon(g2d, 32, 32);
      } catch (Exception e) {
        problems.add(at + ": draw threw " + e.getClass().getSimpleName() + " -- " + e.getMessage());
      } finally {
        g2d.dispose();
      }
    }
  }

  private static List<Class<? extends IDIYComponent<?>>> makerComponentClasses() {
    List<Class<? extends IDIYComponent<?>>> classes =
        new ArrayList<Class<? extends IDIYComponent<?>>>();
    for (List<ComponentType> category : ComponentProcessor.getInstance().getComponentTypes()
        .values()) {
      for (ComponentType type : category) {
        String name = type.getInstanceClass().getName();
        for (String prefix : PACKAGES) {
          if (name.startsWith(prefix + ".")) {
            classes.add(type.getInstanceClass());
          }
        }
      }
    }
    // Discovery order is not stable, and a failure message naming variants reads better sorted.
    Collections.sort(classes, new Comparator<Class<? extends IDIYComponent<?>>>() {

      @Override
      public int compare(Class<? extends IDIYComponent<?>> a, Class<? extends IDIYComponent<?>> b) {
        return a.getName().compareTo(b.getName());
      }
    });
    return classes;
  }
}
