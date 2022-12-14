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

import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.clipboard.ComponentTransferable;
import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.common.Orientation;
import org.diylc.common.OrientationHV;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.Angle;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.Template;
import org.diylc.core.annotations.KeywordPolicy;
import org.diylc.core.measures.Size;

/**
 * Manages component instantiation.
 * 
 * @author Branislav Stojkovic
 */
public class InstantiationManager {

  private static final Logger LOG = Logger.getLogger(InstantiationManager.class);

  public static int MAX_RECENT_COMPONENTS = 16;

  private ComponentType componentTypeSlot;
  private Template template;
  private String[] model;
  private List<IDIYComponent<?>> componentSlot;
  private Point2D firstControlPoint;
  private Point2D potentialControlPoint;

  public static final ComponentType clipboardType = new ComponentType("Clipboard contents",
      "Components from the clipboard", CreationMethod.SINGLE_CLICK, "Multi", "", "", null, null, 0, false, null,
      false, null, KeywordPolicy.NEVER_SHOW, null, false, null, 0);
  public static final ComponentType blockType = new ComponentType("Building block",
	      "Components from the building block", CreationMethod.SINGLE_CLICK, "Multi", "", "", null, null, 0, false, null,
	      false, null, KeywordPolicy.NEVER_SHOW, null, false, null, 0);

  public InstantiationManager() {}

  public ComponentType getComponentTypeSlot() {
    return componentTypeSlot;
  }

  public Template getTemplate() {
    return template;
  }
  
  public String[] getModel() {
    return model;
  }

  public List<IDIYComponent<?>> getComponentSlot() {
    return componentSlot;
  }

  public Point2D getFirstControlPoint() {
    return firstControlPoint;
  }

  public Point2D getPotentialControlPoint() {
    return potentialControlPoint;
  }
  
  public void setPotentialControlPoint(Point2D potentialControlPoint) {
	this.potentialControlPoint = potentialControlPoint;
}

  public void setComponentTypeSlot(ComponentType componentTypeSlot, Template template, String[] model, 
      Project currentProject, boolean forceInstatiate)
      throws Exception {
    this.componentTypeSlot = componentTypeSlot;
    this.template = template;
    this.model = model;
    if (componentTypeSlot == null) {
      this.componentSlot = null;
    } else {
      switch (componentTypeSlot.getCreationMethod()) {
        case POINT_BY_POINT:
          this.componentSlot = forceInstatiate ? instantiateComponent(componentTypeSlot, new Point2D.Double(0, 0), currentProject) : null;
          break;
        case SINGLE_CLICK:
          this.componentSlot = instantiateComponent(componentTypeSlot, new Point2D.Double(0, 0), currentProject);
          break;
      }
    }
    this.firstControlPoint = null;
    this.potentialControlPoint = null;
  }

  public void instatiatePointByPoint(Point2D scaledPoint, Project currentProject) throws Exception {
    firstControlPoint = scaledPoint;
    componentSlot = instantiateComponent(componentTypeSlot, firstControlPoint, currentProject);

    // Set the other control point to the same location, we'll
    // move it later when mouse moves.
    componentSlot.get(0).setControlPoint(firstControlPoint, 0);
    componentSlot.get(0).setControlPoint(firstControlPoint, 1);
  }

  /**
   * Updates component in the slot with the new second control point.
   * 
   * @param scaledPoint
   * @return true, if any change is made
   */
  public boolean updatePointByPoint(Point2D scaledPoint) {
    boolean changeMade = !scaledPoint.equals(potentialControlPoint);
    potentialControlPoint = scaledPoint;
    if (componentSlot != null && !componentSlot.isEmpty()) {
      componentSlot.get(0).setControlPoint(scaledPoint, 1);
    }
    return changeMade;
  }

  @SuppressWarnings("unchecked")
  public void pasteComponents(ComponentTransferable componentTransferable, Point2D scaledPoint, boolean snapToGrid,
      Size gridSpacing, boolean autoGroup, Project currentProject, boolean assignNewNames) {	  
    // Adjust location of components so they are centered under the mouse
    // cursor
    double minX = Integer.MAX_VALUE;
    double maxX = Integer.MIN_VALUE;
    double minY = Integer.MAX_VALUE;
    double maxY = Integer.MIN_VALUE;
    
    Set<String> existingNames = new HashSet<String>();
    for (IDIYComponent<?> c : currentProject.getComponents())
      existingNames.add(c.getName());
    
    List<IDIYComponent<?>> allComponents = new ArrayList<IDIYComponent<?>>(currentProject.getComponents());
    
    List<IDIYComponent<?>> components = componentTransferable.getComponents();
    
    for (IDIYComponent<?> component : components) {
      // assign a new name if it already exists in the project
      if (assignNewNames && existingNames.contains(component.getName())) {
        ComponentType componentType =
            ComponentProcessor.getInstance().extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) component.getClass());
        String newName = createUniqueName(componentType, allComponents);
        existingNames.add(newName);
        component.setName(newName);
        allComponents.add(component);
      }
      
      for (int i = 0; i < component.getControlPointCount(); i++) {        
        Point2D p = component.getControlPoint(i);
        if (p.getX() > maxX) {
          maxX = p.getX();
        }
        if (p.getX() < minX) {
          minX = p.getX();
        }
        if (p.getY() > maxY) {
          maxY = p.getY();
        }
        if (p.getY() < minY) {
          minY = p.getY();
        }
      }
    }
    double x = minX;
    double y = minY;
    if (snapToGrid) {
      x = CalcUtils.roundToGrid(x, gridSpacing);
      x = CalcUtils.roundToGrid(x, gridSpacing);
    }
    for (IDIYComponent<?> component : components) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point2D p = component.getControlPoint(i);
        Point2D newP = new Point2D.Double(p.getX() - x, p.getY() - y);        
        component.setControlPoint(newP, i);
      }
    }

    // Update component slot
    this.componentSlot = new ArrayList<IDIYComponent<?>>(components);

    // Update the component type slot so the app knows that something's
    // being instantiated.
    this.componentTypeSlot = autoGroup ? blockType : clipboardType;

    if (snapToGrid) {
      scaledPoint = new Point2D.Double(scaledPoint.getX(), scaledPoint.getY());
      CalcUtils.snapPointToGrid(scaledPoint, gridSpacing);
    }
    // Update the location according to mouse location
    updateSingleClick(scaledPoint, snapToGrid, gridSpacing);
    
    // copy group information if available
    if (componentTransferable.getGroups() != null) {
      currentProject.getGroups().addAll(componentTransferable.getGroups());
    }
  }
  
  /**
   * Updates location of component slot based on the new mouse location.
   * 
   * @param scaledPoint
   * @param snapToGrid
   * @param gridSpacing
   * @return true if we need to refresh the canvas
   */
  public boolean updateSingleClick(Point2D scaledPoint, boolean snapToGrid, Size gridSpacing) {
    if (potentialControlPoint == null) {
      potentialControlPoint = new Point2D.Double(0, 0);
    }
    if (scaledPoint == null) {
      scaledPoint = new Point2D.Double(0, 0);
    }
    double dx = scaledPoint.getX() - potentialControlPoint.getX();
    double dy = scaledPoint.getY() - potentialControlPoint.getY();
    if (snapToGrid) {
      dx = CalcUtils.roundToGrid(dx, gridSpacing);
      dy = CalcUtils.roundToGrid(dy, gridSpacing);
    }
    // Only repaint if there's an actual change.
    if (dx == 0 && dy == 0) {
      return false;
    }
    potentialControlPoint.setLocation(potentialControlPoint.getX() + dx, potentialControlPoint.getY() + dy);
    if (componentSlot == null) {
      LOG.error("Component slot should not be null!");
    } else {
      Point2D p = new Point2D.Double();
      for (IDIYComponent<?> component : componentSlot) {
        for (int i = 0; i < component.getControlPointCount(); i++) {
          Point2D oldP = component.getControlPoint(i);
          p.setLocation(oldP.getX() + dx, oldP.getY() + dy);          
          component.setControlPoint(p, i);
        }
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  private List<IDIYComponent<?>> instantiateComponent(ComponentType componentType, Point2D point,
      Project currentProject) throws InstantiationException, IllegalAccessException, NoSuchMethodException, 
          SecurityException, IllegalArgumentException, InvocationTargetException {
    LOG.info("Instatiating component of type: " + componentType.getInstanceClass().getName());

    // Instantiate the component.
    IDIYComponent<?> component;
    if (model == null) {
      component = componentType.getInstanceClass().newInstance();
    } else {
      Constructor<? extends IDIYComponent<?>> constructor = componentType.getInstanceClass().getConstructor(String[].class);
      component = constructor.newInstance((Object)model);
    }
    component.createdIn(currentProject);

    // Translate them to the desired location.
    if (point != null) {
      for (int j = 0; j < component.getControlPointCount(); j++) {
        Point2D p = component.getControlPoint(j);
        Point2D controlPoint = new Point2D.Double(p.getX() + point.getX(), p.getY() + point.getY());        
        // snapPointToGrid(controlPoint);
        component.setControlPoint(controlPoint, j);
      }
    }

    loadComponentShapeFromTemplate(component, template);   

    if (model == null) {
      // do not fill with defaults if creating by a model
      fillWithDefaultProperties(component, template);
    }
    
    // preserve component from the template if RENUMBER_ON_PASTE_KEY is OFF
    if (template == null || ConfigurationManager.getInstance().readBoolean(IPlugInPort.RENUMBER_ON_PASTE_KEY, true)) {
      component.setName(createUniqueName(componentType, currentProject.getComponents()));
    }

    // Write to recent components
    List<String> recentComponentTypes =
        (List<String>) ConfigurationManager.getInstance().readObject(IPlugInPort.RECENT_COMPONENTS_KEY,
            new ArrayList<ComponentType>());
    String className = componentType.getInstanceClass().getName();
    if (recentComponentTypes.size() == 0 || !recentComponentTypes.get(0).equals(className)) {
      // Remove if it's already somewhere in the list.
      recentComponentTypes.remove(className);
      // Add to the end of the list.
      recentComponentTypes.add(0, className);
      // Trim the list if necessary.
      if (recentComponentTypes.size() > MAX_RECENT_COMPONENTS) {
        recentComponentTypes.remove(recentComponentTypes.size() - 1);
      }
      ConfigurationManager.getInstance().writeValue(IPlugInPort.RECENT_COMPONENTS_KEY, recentComponentTypes);
    }

    List<IDIYComponent<?>> list = new ArrayList<IDIYComponent<?>>();
    list.add(component);
    return list;
  }


  /**
   * * Creates a unique component name for the specified type taking existing components into
   * account.
   * 
   * @param componentType
   * @param currentProject
   * @param additionalComponents
   * @return
   */
  public String createUniqueName(ComponentType componentType, List<IDIYComponent<?>> components) {
    boolean exists = true;
    String[] takenNames = new String[components.size()];
    for (int j = 0; j < components.size(); j++) {
      takenNames[j] = components.get(j).getName();
    }
    Arrays.sort(takenNames);
    int i = 0;
    while (exists) {
      i++;
      String name = componentType.getNamePrefix() + i;
      exists = false;
      if (Arrays.binarySearch(takenNames, name) >= 0) {
        exists = true;
      }
    }
    return componentType.getNamePrefix() + i;
  }

  /**
   * Finds any properties that have default values and injects default values. Typically it should
   * be used for {@link IDIYComponent} and {@link Project} objects.
   * 
   * @param object
   * @param template
   * @throws IllegalArgumentException
   * @throws IllegalAccessException
   * @throws InvocationTargetException
   * @throws NoSuchMethodException
   * @throws SecurityException
   */
  public void fillWithDefaultProperties(Object object, Template template) {
    // Extract properties.
    List<PropertyWrapper> properties = ComponentProcessor.getInstance().extractProperties(object.getClass());
    Map<String, PropertyWrapper> propertyCache = new HashMap<String, PropertyWrapper>();
    // Override with default values if available.
    for (PropertyWrapper property : properties) {
      propertyCache.put(property.getName(), property);
      Object defaultValue =
          ConfigurationManager.getInstance().readObject(
              Presenter.DEFAULTS_KEY_PREFIX + object.getClass().getName() + ":" + property.getName(), null);
      if (defaultValue != null) {
        property.setValue(defaultValue);
        try {
          property.writeTo(object);
        } catch (Exception e) {
          LOG.error("Could not write property " + property.getName(), e);
        }
      }
    }
    if (template != null) {
      for (Map.Entry<String, Object> pair : template.getValues().entrySet()) {
        PropertyWrapper property = propertyCache.get(pair.getKey());
        if (property == null) {
          LOG.warn("Cannot find property " + pair.getKey());
        } else {
          LOG.debug("Filling value from template for " + pair.getKey());
          property.setValue(pair.getValue());
          try {
            property.writeTo(object);
          } catch (Exception e) {
            LOG.error("Could not write property " + property.getName(), e);
          }
        }
      }
    }
  }

  /**
   * Uses stored control points from the template to shape component.
   * 
   * @param component
   * @param template
   */
  public void loadComponentShapeFromTemplate(IDIYComponent<?> component, Template template) {
    if (template != null && template.getPoints() != null
        && template.getPoints().size() >= component.getControlPointCount()) {
      for (int i = 0; i < component.getControlPointCount(); i++) {
        Point2D oldP = component.getControlPoint(0);
        Point2D p = new Point2D.Double(oldP.getX() + template.getPoints().get(i).getX(), oldP.getY() + template.getPoints().get(i).getY());        
        component.setControlPoint(p, i);
      }
    }
  }

  public void tryToRotateComponentSlot() {
    if (this.componentSlot == null) {
      LOG.debug("Component slot is empty, cannot rotate");
      return;
    }
    List<PropertyWrapper> properties =
        ComponentProcessor.getInstance().extractProperties(this.componentTypeSlot.getInstanceClass());
    PropertyWrapper angleProperty = null;
    for (PropertyWrapper propertyWrapper : properties) {
      if (propertyWrapper.getType().getName().equals(Orientation.class.getName())
          || propertyWrapper.getType().getName().equals(OrientationHV.class.getName())
          || propertyWrapper.getName().equalsIgnoreCase("angle")) {
        angleProperty = propertyWrapper;
        break;
      }
    }
    if (angleProperty == null) {
      LOG.debug("Component in the slot does not have a property of type Orientation, cannot rotate");
      return;
    }
    try {
      for (IDIYComponent<?> component : this.componentSlot) {
        angleProperty.readFrom(component);
        Object value = angleProperty.getValue();
        if (value instanceof Orientation) {
          angleProperty.setValue(Orientation.values()[(((Orientation) value).ordinal() + 1)
              % Orientation.values().length]);
        } else if (value instanceof OrientationHV) {
          angleProperty.setValue(OrientationHV.values()[(((OrientationHV) value).ordinal() + 1)
              % OrientationHV.values().length]);
        } else if (value instanceof Angle) {
          Angle angle = (Angle) angleProperty.getValue();
          Angle newAngle = angle.rotate(1);
          angleProperty.setValue(newAngle);
        }
        angleProperty.writeTo(component);
      }
    } catch (Exception e) {
      LOG.warn("Error trying to rotate the component", e);
    }
  }
}
