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
package org.diylc.swing.gui.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;

import org.apache.log4j.Logger;
import org.diylc.common.PropertyWrapper;
import org.diylc.components.misc.LoadlineEntity;
<<<<<<< HEAD
import org.diylc.core.annotations.ByteArrayProperty;
=======
>>>>>>> refs/heads/master
import org.diylc.core.annotations.DynamicList;
import org.diylc.core.annotations.MultiLineText;
import org.diylc.core.measures.AbstractMeasure;

/**
 * Based on {@link PropertyWrapper#getType()}, creates an appropriate {@link Component} that can
 * edit that type.
 * 
 * @author Branislav Stojkovic
 */
public class FieldEditorFactory {

  private static final Logger LOG = Logger.getLogger(FieldEditorFactory.class);

  public static Component createFieldEditor(PropertyWrapper property) {
    try {
      if (property.getType().equals(String.class) && property.getGetter().isAnnotationPresent(DynamicList.class)) {
        StringListEditor editor = new StringListEditor(property);
        return editor;
      }
    } catch (Exception e) {
      LOG.error("Could not determine if a function is annotated with DynamicList", e);
    }
    try {
      if (property.getType().equals(String.class) && property.getGetter().isAnnotationPresent(MultiLineText.class)) {
        MultiLineStringEditor editor = new MultiLineStringEditor(property);
        return editor;
      }
    } catch (Exception e) {
      LOG.error("Could not determine if a function is annotated with MultiLineText", e);
    }
    if (property.getType().equals(String.class)) {
      StringEditor editor = new StringEditor(property);
      return editor;
    }
    if (property.getType().equals(Color.class)) {
      ColorEditor editor = new ColorEditor(property);
      return editor;
    }
    if (AbstractMeasure.class.isAssignableFrom(property.getType())) {
      MeasureEditor editor = new MeasureEditor(property);
      return editor;
    }
    if (AbstractMeasure[].class.isAssignableFrom(property.getType())) {
        MeasureArrayEditor editor = new MeasureArrayEditor(property);
        return editor;
      }
    if (byte[].class.isAssignableFrom(property.getType())) {
      ByteArrayProperty annotation = null;
      try {
        property.getGetter().isAnnotationPresent(ByteArrayProperty.class);
        annotation = property.getGetter().getAnnotation(ByteArrayProperty.class);   
      } catch (SecurityException e) {
        LOG.error("Error while reading ByteArrayProperty annotation", e);
      } catch (NoSuchMethodException e) {
        LOG.error("Error while reading ByteArrayProperty annotation", e);
      }         
      ImageEditor editor = new ImageEditor(property, annotation == null ? null : annotation.binaryType());
      return editor;
    }
    if (property.getType().isEnum()) {
      EnumEditor editor = new EnumEditor(property);
      return editor;
    }
    if (Byte.class.isAssignableFrom(property.getType()) || byte.class.isAssignableFrom(property.getType())) {
      ByteEditor editor = new ByteEditor(property);
      return editor;
    }
    if (Boolean.class.isAssignableFrom(property.getType()) || boolean.class.isAssignableFrom(property.getType())) {
      BooleanEditor editor = new BooleanEditor(property);
      return editor;
    }
    if (Font.class.isAssignableFrom(property.getType())) {
      FontEditor editor = new FontEditor(property);
      return editor;
    }
    if (Integer.class.isAssignableFrom(property.getType()) || int.class.isAssignableFrom(property.getType())) {
      IntEditor editor = new IntEditor(property);
      return editor;
    }
    if (LoadlineEntity.class.isAssignableFrom(property.getType())) {
      LoadlineEditor editor = new LoadlineEditor(property);
      return editor;
    }
    LOG.error("Unrecognized parameter type: " + property.getType().getName());
    return new JLabel("Unrecognized");
  }
}
