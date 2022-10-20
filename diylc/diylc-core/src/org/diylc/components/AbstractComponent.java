/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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

import java.awt.Color;
import java.awt.Font;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;

/**
 * Abstract implementation of {@link IDIYComponent} that contains component name and toString.
 * 
 * IMPORTANT: to improve performance, all fields except for <code>Point</code> and
 * <code>Point</code> arrays should be immutable. Failing to comply with this can result in annoying
 * and hard to trace bugs.
 * 
 * @author Branislav Stojkovic
 * 
 * @param <T>
 */
public abstract class AbstractComponent<T> implements IDIYComponent<T> {

  private static final long serialVersionUID = 1L;

  protected String name = "";

  public static Color SELECTION_COLOR = Color.red;
  public static Color LABEL_COLOR = Color.black;
  public static Color LABEL_COLOR_SELECTED = Color.red;
  public static Font LABEL_FONT = new Font("Square721 BT", Font.PLAIN, 14);
  public static Color METAL_COLOR = Color.decode("#759DAF");
  public static Color LIGHT_METAL_COLOR = Color.decode("#EEEEEE");
  public static Color COPPER_COLOR = Color.decode("#DA8A67");
  public static Color GOLD_COLOR = Color.decode("#DAA520");
  public static Color FR4_COLOR = Color.decode("#6B9B6B");
  public static Color FR4_LIGHT_COLOR = Color.decode("#BABE71");
  public static Color PHENOLIC_COLOR = Color.decode("#F8EBB3");
  public static Color PHENOLIC_DARK_COLOR = Color.decode("#CD8500");
  
  @EditableProperty(defaultable = false)
  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean canControlPointOverlap(int index) {
    return false;
  }

  @Override
  public String toString() {
    return name;
  }

  @Override
  public String getValueForDisplay() {
    return getValue() == null ? "" : getValue().toString();
  }

  /**
   * Returns the closest odd number, i.e. x when x is odd, or x + 1 when x is even.
   * 
   * @param x
   * @return
   */
  protected int getClosestOdd(double x) {
    return ((int) x / 2) * 2 + 1;
  }
  
  /**
   * Returns the closest odd number, i.e. x when x is odd, or x + 1 when x is even.
   * 
   * @param x
   * @return
   */
  protected int getClosestOdd(Size s) {
    double x = s.convertToPixels();
    return ((int) x / 2) * 2 + 1;
  }

  /**
   * Returns darker color if possible, or lighter if it's already dark
   * 
   * @param color
   * @return
   */
  protected Color darkerOrLighter(Color color) {
    float[] hsb = new float[3];
    Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
    return new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2] > 0.5 ? hsb[2] - 0.25f : hsb[2] + 0.25f));
  }

  /**
   * @param clip
   * @return true if none of the control points lie in the clip rectangle.
   */
  protected boolean checkPointsClipped(Shape clip) {
    for (int i = 0; i < getControlPointCount(); i++) {
      if (clip.contains(getControlPoint(i))) {
        return false;
      }
    }
    double minX = Integer.MAX_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double maxY = Integer.MIN_VALUE;
    for (int i = 0; i < getControlPointCount(); i++) {
      Point2D p = getControlPoint(i);
      if (minX > p.getX()) {
        minX = p.getX();
      }
      if (maxX < p.getX()) {
        maxX = p.getX();
      }
      if (minY > p.getY()) {
        minY = p.getY();
      }
      if (maxY < p.getY()) {
        maxY = p.getY();
      }
    }
    Rectangle2D rect = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    return !clip.intersects(rect);
  }

  @SuppressWarnings("unchecked")
  public IDIYComponent<T> clone() throws CloneNotSupportedException {
    try {
      // Instantiate object of the same type
      AbstractComponent<T> newInstance = (AbstractComponent<T>) this.getClass().getConstructors()[0].newInstance();
      Class<?> clazz = this.getClass();
      while (AbstractComponent.class.isAssignableFrom(clazz)) {
        Field[] fields = clazz.getDeclaredFields();
        clazz = clazz.getSuperclass();
        // fields = this.getClass().getDeclaredFields();
        // Copy over all non-static, non-final fields that are declared
        // in AbstractComponent or one of it's child classes
        for (Field field : fields) {
          if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
            field.setAccessible(true);
            Object value = field.get(this);

            // Deep copy point arrays.
            // TODO: something nicer
            if (value != null && value.getClass().isArray()
                && value.getClass().getComponentType().isAssignableFrom(Point2D.class)) {
              Object newArray = Array.newInstance(value.getClass().getComponentType(), Array.getLength(value));
              for (int i = 0; i < Array.getLength(value); i++) {
                Point2D p = (Point2D) Array.get(value, i);
                Array.set(newArray, i, new Point2D.Double(p.getX(), p.getY()));
              }
              value = newArray;
            }
            // Deep copy points.
            // TODO: something nicer
            if (value != null && value instanceof Point2D) {
              Point2D p = (Point2D) value;
              value = new Point2D.Double(p.getX(), p.getY());
            }

            field.set(newInstance, value);
          }
        }
      }
      return newInstance;
    } catch (Exception e) {
      throw new CloneNotSupportedException("Could not clone the component. Reason: " + e.getMessage());
    }
  }

  @Override
  public boolean equalsTo(IDIYComponent<?> other) {
    if (other == null)
      return false;
    if (!other.getClass().equals(this.getClass()))
      return false;
    Class<?> clazz = this.getClass();
    while (AbstractComponent.class.isAssignableFrom(clazz)) {
      Field[] fields = clazz.getDeclaredFields();
      clazz = clazz.getSuperclass();
      // fields = this.getClass().getDeclaredFields();
      // Copy over all non-static, non-final and non-transient fields that are declared
      // in
      // AbstractComponent or one of it's child classes
      for (Field field : fields) {
        if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) &&
            !Modifier.isTransient(field.getModifiers())) {
          field.setAccessible(true);
          try {
            Object value = field.get(this);
            Object otherValue = field.get(other);
            if (!compareObjects(value, otherValue))
              return false;
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    return true;
  }

  private boolean compareObjects(Object o1, Object o2) {
    if (o1 == null && o2 == null)
      return true;
    if (o1 == null || o2 == null)
      return false;
    if (o1.getClass().isArray()) {
      if (o1.getClass().getComponentType() == byte.class)
        return Arrays.equals((byte[]) o1, (byte[]) o2);
      if (o1.getClass().getComponentType() == double.class)
        return Arrays.equals((double[]) o1, (double[]) o2);
      if (o1.getClass().getComponentType() == int.class)
        return Arrays.equals((int[]) o1, (int[]) o2);      
      return Arrays.equals((Object[]) o1, (Object[]) o2);
    }
    return o1.equals(o2);
  }
  
  @Override
  public String getControlPointNodeName(int index) {
    return Integer.toString(index + 1);
  }
  
  @Override
  public String getInternalLinkName(int index1, int index2) {   
    return null;
  }
  
  @Override
  public String[] getSectionNames(int pointIndex) {   
    return null;
  }
  
  @Override
  public String getCommonPointName(int pointIndex) {   
    return null;
  }
  
  @Override
  public boolean canPointMoveFreely(int pointIndex) {   
    return true;
  }
  
  @Override
  public Rectangle2D getCachingBounds() {
    return null;
  }
  
  @Override
  public void createdIn(Project project) {
    // TODO Auto-generated method stub
  }
}
