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
package org.diylc.presenter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IPropertyValidator;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;

/**
 * Utility class with component processing methods.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentProcessor {

  private static final Logger LOG = Logger.getLogger(ComponentProcessor.class);

  private static ComponentProcessor instance;

  private Map<String, List<PropertyWrapper>> propertyCache;
  private Map<String, IPropertyValidator> propertyValidatorCache;
  private Map<String, ComponentType> componentTypeMap;
  private Map<String, IComponentTransformer> componentTransformerMap;

  public static ComponentProcessor getInstance() {
    if (instance == null) {
      instance = new ComponentProcessor();
    }
    return instance;
  }

  private ComponentProcessor() {
    super();
    this.propertyCache = new HashMap<String, List<PropertyWrapper>>();
    this.componentTypeMap = new HashMap<String, ComponentType>();
    this.propertyValidatorCache = new HashMap<String, IPropertyValidator>();
    this.componentTransformerMap = new HashMap<String, IComponentTransformer>();
  }

  public ComponentType extractComponentTypeFrom(Class<? extends IDIYComponent<?>> clazz) {
    if (componentTypeMap.containsKey(clazz.getName())) {
      return componentTypeMap.get(clazz.getName());
    }
    String name;
    String description;
    CreationMethod creationMethod;
    String category;
    String namePrefix;
    String author;
    Icon icon;
    double zOrder;
    boolean flexibleZOrder;
    BomPolicy bomPolicy;
    boolean autoEdit;
    IComponentTransformer transformer;
    KeywordPolicy keywordPolicy;
    String keywordTag;
    if (clazz.isAnnotationPresent(ComponentDescriptor.class)) {
      ComponentDescriptor annotation = clazz.getAnnotation(ComponentDescriptor.class);
      name = annotation.name();
      description = annotation.description();
      creationMethod = annotation.creationMethod();
      category = annotation.category();
      namePrefix = annotation.instanceNamePrefix();
      author = annotation.author();
      zOrder = annotation.zOrder();
      flexibleZOrder = annotation.flexibleZOrder();
      bomPolicy = annotation.bomPolicy();
      autoEdit = annotation.autoEdit();
      transformer = getComponentTransformer(annotation.transformer());
      keywordPolicy = annotation.keywordPolicy();
      keywordTag = annotation.keywordTag();
    } else { // default
    	return null;
    }
    icon = null;
    // Draw component icon.
    try {
      IDIYComponent<?> componentInstance = (IDIYComponent<?>) clazz.newInstance();
      Image image =
          new BufferedImage(Presenter.ICON_SIZE, Presenter.ICON_SIZE, java.awt.image.BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = (Graphics2D) image.getGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      // g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
      // RenderingHints.VALUE_STROKE_PURE);
      componentInstance.drawIcon(g2d, Presenter.ICON_SIZE, Presenter.ICON_SIZE);
      icon = new ImageIcon(image);
    } catch (Exception e) {
      LOG.error("Error drawing component icon", e);
    }
    ComponentType componentType =
        new ComponentType(name, description, creationMethod, category, namePrefix, author, icon, clazz, zOrder,
            flexibleZOrder, bomPolicy, autoEdit, transformer, keywordPolicy, keywordTag);
    componentTypeMap.put(clazz.getName(), componentType);
    return componentType;
  }

  /**
   * Extracts all editable properties from the component class.
   * 
   * @param clazz
   * @return
   */
  public List<PropertyWrapper> extractProperties(Class<?> clazz) {
    if (propertyCache.containsKey(clazz.getName())) {
      return cloneProperties(propertyCache.get(clazz.getName()));
    }
    List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();
    for (Method getter : clazz.getMethods()) {
      if (getter.getName().startsWith("get")) {
        try {
          if (getter.isAnnotationPresent(EditableProperty.class) && !getter.isAnnotationPresent(Deprecated.class)
              // since Java 8 in generic classes we get properties of both the generic type and Object
              // added this condition to skip Object property that we do not need
              && getter.getReturnType() != Object.class) {
            EditableProperty annotation = getter.getAnnotation(EditableProperty.class);
            String name;
            if (annotation.name().equals("")) {
              name = getter.getName().substring(3);
            } else {
              name = annotation.name();
            }
            IPropertyValidator validator = getPropertyValidator(annotation.validatorClass());
            Method setter = clazz.getMethod("set" + getter.getName().substring(3), getter.getReturnType());
            PropertyWrapper property =
                new PropertyWrapper(name, getter.getReturnType(), getter.getName(), setter.getName(),
                    annotation.defaultable(), validator, annotation.sortOrder());
            properties.add(property);
          }
        } catch (NoSuchMethodException e) {
          LOG.debug("No matching setter found for \"" + getter.getName() + "\". Skipping...");
        }
      }
    }

    propertyCache.put(clazz.getName(), properties);
    return cloneProperties(properties);
  }

  private List<PropertyWrapper> cloneProperties(List<PropertyWrapper> properties) {
    List<PropertyWrapper> result = new ArrayList<PropertyWrapper>(properties.size());
    for (PropertyWrapper propertyWrapper : properties) {
      try {
        result.add((PropertyWrapper) propertyWrapper.clone());
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
    return result;
  }

  /**
   * Returns properties that have the same value for all the selected components.
   * 
   * @param selectedComponents
   * @return
   */
  public List<PropertyWrapper> getMutualSelectionProperties(Collection<IDIYComponent<?>> selectedComponents)
      throws Exception {
    if (selectedComponents.isEmpty()) {
      return null;
    }
    List<PropertyWrapper> properties = new ArrayList<PropertyWrapper>();

    List<IDIYComponent<?>> selectedList = new ArrayList<IDIYComponent<?>>(selectedComponents);

    IDIYComponent<?> firstComponent = selectedList.get(0);
    properties.addAll(extractProperties(firstComponent.getClass()));
    // Initialize values
    for (PropertyWrapper property : properties) {
      property.readFrom(firstComponent);
    }
    for (int i = 1; i < selectedComponents.size(); i++) {
      IDIYComponent<?> component = selectedList.get(i);
      List<PropertyWrapper> newProperties = extractProperties(component.getClass());
      for (PropertyWrapper property : newProperties) {
        property.readFrom(component);
      }
      properties.retainAll(newProperties);
      // Try to find matching properties in old and new lists and see if
      // their values match.
      for (PropertyWrapper oldProperty : properties) {
        if (newProperties.contains(oldProperty)) {
          PropertyWrapper newProperty = newProperties.get(newProperties.indexOf(oldProperty));
          if (newProperty.getValue() != null && newProperty.getValue() != null) {
            if (!newProperty.getValue().equals(oldProperty.getValue()))
              // Values don't match, so the property is not unique
              // valued.
              oldProperty.setUnique(false);
          } else if ((newProperty.getValue() == null && oldProperty.getValue() != null)
              || (newProperty.getValue() != null && oldProperty.getValue() == null)) {
            oldProperty.setUnique(false);
          }
        }
      }
    }
    Collections.sort(properties, ComparatorFactory.getInstance().getDefaultPropertyComparator());
    return properties;
  }

  private IPropertyValidator getPropertyValidator(Class<? extends IPropertyValidator> clazz) {
    if (propertyValidatorCache.containsKey(clazz.getName())) {
      return propertyValidatorCache.get(clazz.getName());
    }
    IPropertyValidator validator;
    try {
      validator = clazz.newInstance();
    } catch (Exception e) {
      LOG.error("Could not instantiate validator for " + clazz.getName(), e);
      return null;
    }
    propertyValidatorCache.put(clazz.getName(), validator);
    return validator;
  }

  private IComponentTransformer getComponentTransformer(Class<? extends IComponentTransformer> clazz) {
    if (clazz == null)
      return null;
    if (componentTransformerMap.containsKey(clazz.getName())) {
      return componentTransformerMap.get(clazz.getName());
    }
    IComponentTransformer transformer;
    try {
      transformer = clazz.newInstance();
    } catch (Exception e) {
      LOG.error("Could not instantiate validator for " + clazz.getName(), e);
      return null;
    }
    componentTransformerMap.put(clazz.getName(), transformer);
    return transformer;
  }
}
