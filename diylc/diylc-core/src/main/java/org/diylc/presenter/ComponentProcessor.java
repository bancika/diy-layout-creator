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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.apache.log4j.Logger;

import org.diylc.common.ComponentType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;

import org.diylc.core.IDynamicPropertySource;
import org.diylc.core.IPropertyValidator;
import org.diylc.core.annotations.*;

import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.KeywordPolicy;

import org.diylc.lang.LangUtil;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import static org.reflections.scanners.Scanners.TypesAnnotated;

/**
 * Utility class with component processing methods.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentProcessor {

  private static final Logger LOG = Logger.getLogger(ComponentProcessor.class);

  private static final boolean LOG_COMPONENT_TYPE_DETAILS = false;

  private static ComponentProcessor instance;

  private Map<String, List<PropertyWrapper>> propertyCache;
  private Map<String, IPropertyValidator> propertyValidatorCache;
  private Map<String, ComponentType> componentTypeMap;
  private Map<String, IComponentTransformer> componentTransformerMap;
  private Map<String, List<ComponentType>> componentTypes;

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
    boolean cacheDrawing;
    IComponentTransformer transformer;
    KeywordPolicy keywordPolicy;
    String keywordTag;
    List<String[]> datasheet = null;
    int datasheetCreationStepCount = 0;
    
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
      cacheDrawing = annotation.enableCache();
      if (annotation.enableDatasheet()) {
        datasheet = DatasheetService.getInstance().loadDatasheet(clazz);
        datasheetCreationStepCount = annotation.datasheetCreationStepCount();
      }
    } else { // default
    	return null;
    }
    icon = null;
    // Draw component icon.
    try {
      IDIYComponent<?> componentInstance = (IDIYComponent<?>) clazz.getDeclaredConstructor().newInstance();
      Image image =
          new BufferedImage(Presenter.ICON_SIZE, Presenter.ICON_SIZE, java.awt.image.BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2d = (Graphics2D) image.getGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
          RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
          RenderingHints.VALUE_FRACTIONALMETRICS_ON);
      // g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
      // RenderingHints.VALUE_STROKE_PURE);
      componentInstance.drawIcon(g2d, Presenter.ICON_SIZE, Presenter.ICON_SIZE);
      icon = new ImageIcon(image);
    } catch (Exception e) {
      LOG.error("Error drawing component icon", e);
    }
    ComponentType componentType =
        new ComponentType(name, description, creationMethod, category, namePrefix, author, icon, clazz, zOrder,
            flexibleZOrder, bomPolicy, autoEdit, transformer, keywordPolicy, keywordTag, cacheDrawing, datasheet, 
            datasheetCreationStepCount);
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
            
            String setterName = null;
            try {
              Method setter = clazz.getMethod("set" + getter.getName().substring(3), getter.getReturnType());
              setterName = setter.getName();
            } catch (NoSuchMethodException e) {
              LOG.debug("No matching setter found for " + clazz.getName() + "." + getter.getName() + ". Skipping...");
            }
            
            LangUtil.translate(name); // temporary, just to make sure we catch it in the dict
            if (getter.getReturnType().isEnum()) {
              for(Object o : getter.getReturnType().getEnumConstants()) {
                LangUtil.translate(o.toString());
              }
            }

            IDynamicPropertySource dynamicPropertySource = null;
            if (getter.isAnnotationPresent(DynamicEditableProperty.class)) {
              DynamicEditableProperty dynamicAnnotation = getter.getAnnotation(DynamicEditableProperty.class);
              dynamicPropertySource = getDynamicPropertySource(dynamicAnnotation.source());
            }
            
            PropertyWrapper property =
                new PropertyWrapper(name, getter.getReturnType(), getter.getName(), setterName,
                    annotation.defaultable(), validator, annotation.sortOrder(), dynamicPropertySource);
            properties.add(property);
          }       
      }
    }

    propertyCache.put(clazz.getName(), properties);
    return cloneProperties(properties);
  }

  private static String formatPropertyType(Class<?> type) {
    return type == null ? null : type.getSimpleName();
  }

  private static List<String> getPossibleValues(Class<?> type) {
    if (type == null || !type.isEnum())
      return null;
    return Arrays.stream(type.getEnumConstants())
        .map(Object::toString)
        .collect(Collectors.toList());
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

  public Map<String, List<ComponentType>> getComponentTypes() {
    if (componentTypes == null) {
      LOG.info("Loading component types.");
      componentTypes = new HashMap<String, List<ComponentType>>();
      Reflections reflections = new Reflections(
          new ConfigurationBuilder()
              .forPackage("org.diylc")
              .filterInputsBy(new FilterBuilder().includePackage("org.diylc.components"))
              .setScanners(TypesAnnotated));
      Set<Class<?>> componentTypeClasses = null;
      try {
        componentTypeClasses = reflections.getTypesAnnotatedWith(ComponentDescriptor.class, false);
        List<Map<String, Object>> componentSnapshots = LOG_COMPONENT_TYPE_DETAILS ? new ArrayList<>() : null;

        for (Class<?> clazz : componentTypeClasses) {
          if (!Modifier.isAbstract(clazz.getModifiers()) && IDIYComponent.class.isAssignableFrom(clazz)) {
            ComponentType componentType =
                ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) clazz);
            if (componentType == null)
              continue;

            // just to store in the cache
            ComponentProcessor.getInstance().extractProperties(clazz);

            List<ComponentType> nestedList;
            if (componentTypes.containsKey(componentType.getCategory())) {
              nestedList = componentTypes.get(componentType.getCategory());
            } else {
              nestedList = new ArrayList<ComponentType>();
              componentTypes.put(componentType.getCategory(), nestedList);
            }
            nestedList.add(componentType);

            if (LOG_COMPONENT_TYPE_DETAILS) {
              try {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("name", componentType.getName());
                snapshot.put("description", componentType.getDescription());
                snapshot.put("creationMethod", componentType.getCreationMethod());
                snapshot.put("category", componentType.getCategory());
                List<PropertyWrapper> props = propertyCache.get(clazz.getName());
                snapshot.put("properties", props != null ? props.stream()
                    .map(p -> {
                      Map<String, Object> prop = new LinkedHashMap<>();
                      prop.put("name", p.getName());
                      prop.put("type", formatPropertyType(p.getType()));
                      List<String> possibleValues = getPossibleValues(p.getType());
                      if (possibleValues != null) {
                        prop.put("possibleValues", possibleValues);
                      }
                      return prop;
                    })
                    .collect(Collectors.toList()) : Collections.emptyList());
                componentSnapshots.add(snapshot);
              } catch (Exception e) {
                LOG.warn("Failed to log component type details for " + clazz.getName(), e);
              }
            }
          }
        }

        if (LOG_COMPONENT_TYPE_DETAILS && componentSnapshots != null && !componentSnapshots.isEmpty()) {
          try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            LOG.info("Component types: " + mapper.writeValueAsString(componentSnapshots));
          } catch (Exception e) {
            LOG.warn("Failed to log component types JSON", e);
          }
        }

        for (Map.Entry<String, List<ComponentType>> e : componentTypes.entrySet()) {
          LOG.debug(e.getKey() + ": " + e.getValue());
        }
      } catch (Exception e) {
        LOG.error("Error loading component types", e);
      }
    }
    return componentTypes;
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
      validator = clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      LOG.error("Could not instantiate validator for " + clazz.getName(), e);
      return null;
    }
    propertyValidatorCache.put(clazz.getName(), validator);
    return validator;
  }

  private IDynamicPropertySource getDynamicPropertySource(Class<? extends IDynamicPropertySource> clazz) {
    IDynamicPropertySource source;
    try {
      source = clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      LOG.error("Could not instantiate source for " + clazz.getName(), e);
      return null;
    }
    return source;
  }

  private IComponentTransformer getComponentTransformer(Class<? extends IComponentTransformer> clazz) {
    if (clazz == null)
      return null;
    if (componentTransformerMap.containsKey(clazz.getName())) {
      return componentTransformerMap.get(clazz.getName());
    }
    IComponentTransformer transformer;
    try {
      transformer = clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      LOG.error("Could not instantiate validator for " + clazz.getName(), e);
      return null;
    }
    componentTransformerMap.put(clazz.getName(), transformer);
    return transformer;
  }
  
  public static boolean hasStickyPoint(IDIYComponent<?> c) {
    for (int i = 0; i < c.getControlPointCount(); i++)
      if (c.isControlPointSticky(0))
        return true;
    return false;
  }
  
  private static int touchSensitivity = 5;
  
  public static boolean componentPointsTouch(IDIYComponent<?> c1, IDIYComponent<?> c2) {
    for (int i = 0; i < c1.getControlPointCount(); i++)
      for (int j = 0; j < c2.getControlPointCount(); j++)
        if (c1.isControlPointSticky(i) && c2.isControlPointSticky(j) && c1.getControlPoint(i).distance(c2.getControlPoint(j)) < touchSensitivity)
          return true;
    return false;
  }

  // Add this method for testing purposes
  protected void setComponentTypeForTest(Class<? extends IDIYComponent<?>> componentClass, ComponentType componentType) {
    componentTypeMap.put(componentClass.getName(), componentType);
  }
}
