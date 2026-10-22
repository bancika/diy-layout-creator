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
package org.diylc.plugins.chatbot.service;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.IProjectEditor;
import org.diylc.common.Percentage;
import org.diylc.common.PropertyWrapper;
import org.diylc.components.AbstractCurvedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.netlist.Node;
import org.diylc.plugins.chatbot.model.AiEditOperation;
import org.diylc.plugins.chatbot.model.AiEditScript;
import org.diylc.presenter.CalcUtils;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.DatasheetService;
import org.diylc.presenter.InstantiationManager;

/**
 * Executes an AiEditScript against a Project.
 * <p>
 * Uses a two-pass approach:
 * <ul>
 *   <li><b>Pass 1</b>: Remove, Modify, Replace and Add (non-wires), in the order the script lists them. Sets properties and determines final pin geometry.</li>
 *   <li><b>Pass 2</b>: Add Wires. Resolves terminal references to the final pixel coordinates of the components from Pass 1.</li>
 * </ul>
 */
public class AiEditScriptEditor implements IProjectEditor {

  private static final Logger LOG = Logger.getLogger(AiEditScriptEditor.class);

  // the one property a replace carries over unasked, so the new part faces the way the old one did
  private static final String ORIENTATION = "Orientation";

  private final AiEditScript editScript;
  private final List<String> warnings = new ArrayList<>();
  private final InstantiationManager instantiationManager = new InstantiationManager();

  public AiEditScriptEditor(AiEditScript editScript) {
    this.editScript = editScript;
  }

  @Override
  public String getEditAction() {
    return "AI Edit: " + (editScript.getExplanation() != null ? editScript.getExplanation() : "Circuit modified");
  }
  
  public List<String> getWarnings() {
    return warnings;
  }

  @Override
  public Set<IDIYComponent<?>> edit(Project project, Set<IDIYComponent<?>> selection) {
    if (editScript.getOperations() == null) {
      return selection;
    }

    Set<IDIYComponent<?>> newSelection = new HashSet<>();
    Set<IDIYComponent<?>> removed = new HashSet<>();
    Map<String, IDIYComponent<?>> componentByName = new HashMap<>();

    // the script names components by the ids they were exported with, W3#a1b2 for a repeated name
    for (Map.Entry<IDIYComponent<?>, String> entry
        : AiProjectBuilder.assignIds(project.getComponents()).entrySet()) {
      componentByName.put(entry.getValue(), entry.getKey());
    }

    ComponentProcessor processor = ComponentProcessor.getInstance();
    
    // Pass 1 keeps the order of the script, so that an add followed by a modify of the same part,
    // or a remove followed by an add that reuses the name, does what the model wrote
    List<AiEditOperation> pass1Ops = new ArrayList<>();
    List<AiEditOperation> pass2Ops = new ArrayList<>();
    List<AiEditOperation> zOrderOps = new ArrayList<>();

    for (AiEditOperation op : editScript.getOperations()) {
      if ("send_behind".equalsIgnoreCase(op.getAction()) || "bring_in_front".equalsIgnoreCase(op.getAction())) {
        zOrderOps.add(op);
      } else if ("add".equalsIgnoreCase(op.getAction()) && op.getFromTerminal() != null && op.getToTerminal() != null) {
        pass2Ops.add(op);
      } else {
        pass1Ops.add(op);
      }
    }

    // ==========================================
    // PASS 1: Remove, Modify, and Add Non-Wires
    // ==========================================
    for (AiEditOperation op : pass1Ops) {
      try {
        if ("remove".equalsIgnoreCase(op.getAction())) {
          IDIYComponent<?> target = findExactComponent(op.getComponentName(), componentByName);
          if (target != null) {
            removeComponent(target, project, componentByName, newSelection, removed);
          } else {
            warnings.add("Cannot remove component '" + op.getComponentName() + "' (not found).");
          }
        } 
        else if ("modify".equalsIgnoreCase(op.getAction())) {
          IDIYComponent<?> target = findExactComponent(op.getComponentName(), componentByName);
          if (target != null) {
            applyProperties(target, op.getProperties(), processor);
            if (op.getDatasheetModel() != null && !op.getDatasheetModel().isEmpty()) {
              applyDatasheetModel(target, op.getDatasheetModel());
            }
            newSelection.add(target);
          } else {
            warnings.add("Cannot modify component '" + op.getComponentName() + "' (not found).");
          }
        }
        else if ("replace".equalsIgnoreCase(op.getAction())) {
          replace(op, project, processor, componentByName, newSelection, removed);
        }
        else if ("add".equalsIgnoreCase(op.getAction())) {
          ComponentType ct = findComponentType(processor, op.getComponentType());
          if (ct == null) {
            warnings.add("Cannot add '" + op.getComponentName() + "' (unknown type: " + op.getComponentType() + " - consider updating DIYLC).");
            continue;
          }
          
          IDIYComponent<?> comp = instantiate(ct, op.getComponentName(), project);

          // Apply properties first so geometry (e.g. pin count, orientation) is computed correctly
          applyProperties(comp, op.getProperties(), processor);
          
          // Apply datasheet model if specified
          if (op.getDatasheetModel() != null && !op.getDatasheetModel().isEmpty()) {
            applyDatasheetModel(comp, op.getDatasheetModel());
          }
          
          // Position the component
          if (ct.getCreationMethod() == CreationMethod.SINGLE_CLICK) {
            if (op.getPosition() != null) {
              moveTo(comp, op.getPosition().toPixels(project.getGridSpacing()));
            } else {
              warnings.add("Component '" + op.getComponentName() + "' is missing 'position' for SINGLE_CLICK placement.");
            }
          } else { // POINT_BY_POINT
            if (op.getFromPos() != null && op.getToPos() != null) {
              Point2D from = op.getFromPos().toPixels(project.getGridSpacing());
              Point2D to = op.getToPos().toPixels(project.getGridSpacing());
              comp.stretchBetween(from, to);
            } else {
              warnings.add("Component '" + op.getComponentName() + "' is missing 'fromPos'/'toPos' for POINT_BY_POINT placement.");
            }
          }
          
          project.getComponents().add(comp);
          componentByName.put(comp.getName(), comp);
          newSelection.add(comp);
        }
      } catch (Exception e) {
        LOG.error("Failed to execute Pass 1 operation: " + op, e);
        warnings.add("Error executing " + op.getAction() + " on " + op.getComponentName() + ": " + e.getMessage());
      }
    }

    // ==========================================
    // PASS 2: Add Wires / Connectors
    // ==========================================
    for (AiEditOperation op : pass2Ops) {
      try {
        ComponentType ct = findComponentType(processor, op.getComponentType());
        if (ct == null) {
          warnings.add("Cannot add wire '" + op.getComponentName() + "' (unknown type: " + op.getComponentType() + ").");
          continue;
        }
        
        Point2D startPoint = resolveTerminal(op.getFromTerminal(), componentByName);
        Point2D endPoint = resolveTerminal(op.getToTerminal(), componentByName);
        
        if (startPoint == null || endPoint == null) {
          warnings.add("Cannot add wire '" + op.getComponentName() + "' (could not resolve terminal references).");
          continue;
        }

        IDIYComponent<?> wire = instantiate(ct, op.getComponentName(), project);
        applyProperties(wire, op.getProperties(), processor);
        // shapes the wire the way it comes out when drawn by hand: a curve, a staircase or a line
        wire.stretchBetween(startPoint, endPoint);

        project.getComponents().add(wire);
        componentByName.put(wire.getName(), wire);
        newSelection.add(wire);
        
      } catch (Exception e) {
        LOG.error("Failed to execute Pass 2 operation: " + op, e);
        warnings.add("Error executing " + op.getAction() + " on wire " + op.getComponentName() + ": " + e.getMessage());
      }
    }

    // ==========================================
    // PASS 3: Z-Order Operations
    // ==========================================
    for (AiEditOperation op : zOrderOps) {
      try {
        IDIYComponent<?> target = findComponentByName(op.getComponentName(), componentByName);
        IDIYComponent<?> ref = findComponentByName(op.getReferenceComponent(), componentByName);
        
        if (target == null) {
          warnings.add("Cannot change Z-order: component '" + op.getComponentName() + "' not found.");
          continue;
        }
        if (ref == null) {
          warnings.add("Cannot change Z-order: reference component '" + op.getReferenceComponent() + "' not found.");
          continue;
        }
        if (target.equals(ref)) {
          continue;
        }

        List<IDIYComponent<?>> comps = project.getComponents();
        if (!comps.contains(target) || !comps.contains(ref)) {
          continue;
        }

        comps.remove(target);
        int refIndex = comps.indexOf(ref);
        
        if ("send_behind".equalsIgnoreCase(op.getAction())) {
          comps.add(refIndex, target);
        } else if ("bring_in_front".equalsIgnoreCase(op.getAction())) {
          comps.add(refIndex + 1, target);
        }
        newSelection.add(target);
      } catch (Exception e) {
        LOG.error("Failed to execute Pass 3 operation: " + op, e);
        warnings.add("Error executing " + op.getAction() + " on " + op.getComponentName() + ": " + e.getMessage());
      }
    }

    if (!newSelection.isEmpty()) {
      return newSelection;
    }
    // a script that only removes leaves nothing new to select, and the old selection may still
    // hold the components it just took out of the project
    Set<IDIYComponent<?>> remaining = new HashSet<>(selection);
    remaining.removeAll(removed);
    return remaining;
  }

  /**
   * Creates a component the way the toolbox does, with the defaults the user saved for its type,
   * but under the name the script gave it, because later operations refer to it by that name.
   */
  private IDIYComponent<?> instantiate(ComponentType ct, String name, Project project)
      throws Exception {
    IDIYComponent<?> comp = ct.getInstanceClass().getDeclaredConstructor().newInstance();
    comp.createdIn(project);
    instantiationManager.fillWithDefaultProperties(comp, null);
    if (name == null || name.isBlank()) {
      name = instantiationManager.createUniqueName(ct, project.getComponents());
    }
    comp.setName(name);
    return comp;
  }

  /**
   * Swaps a component for one of another type and carries the wire ends that sit on its terminals
   * over to the new ones, so the wiring keeps its routing and colours. A wire with an end on a
   * terminal the pin map leaves out has nowhere to go and is removed.
   */
  private void replace(AiEditOperation op, Project project, ComponentProcessor processor,
      Map<String, IDIYComponent<?>> componentByName, Set<IDIYComponent<?>> newSelection,
      Set<IDIYComponent<?>> removed) throws Exception {
    IDIYComponent<?> old = findExactComponent(op.getComponentName(), componentByName);
    if (old == null) {
      warnings.add("Cannot replace component '" + op.getComponentName() + "' (not found).");
      return;
    }
    ComponentType ct = findComponentType(processor, op.getComponentType());
    if (ct == null) {
      warnings.add("Cannot replace '" + op.getComponentName() + "' (unknown type: "
          + op.getComponentType() + " - consider updating DIYLC).");
      return;
    }

    // collected before anything moves, while the wire ends still sit on the old terminals
    Set<Node> wireEnds = AiProjectBuilder.getWireEnds(project);
    List<AttachedEnd> attachedEnds = new ArrayList<>();
    for (IDIYComponent<?> c : project.getComponents()) {
      if (c == old) {
        continue;
      }
      for (int i = 0; i < c.getControlPointCount(); i++) {
        int pin = findStuckPin(c, i, old);
        if (pin < 0) {
          continue;
        }
        if (wireEnds.contains(new Node(c, i))) {
          attachedEnds.add(new AttachedEnd(c, i, pin));
        } else {
          warnings.add("'" + c.getName() + "' touches " + op.getComponentName() + "." + pin
              + " directly and was left where it is.");
        }
      }
    }

    IDIYComponent<?> replacement = instantiate(ct, old.getName(), project);
    if (op.getProperties() == null || op.getProperties().keySet().stream()
        .noneMatch(ORIENTATION::equalsIgnoreCase)) {
      copyProperty(ORIENTATION, old, replacement, processor);
    }
    applyProperties(replacement, op.getProperties(), processor);
    if (op.getDatasheetModel() != null && !op.getDatasheetModel().isEmpty()) {
      applyDatasheetModel(replacement, op.getDatasheetModel());
    }
    moveTo(replacement, old.getControlPoint(0));

    List<IDIYComponent<?>> components = project.getComponents();
    components.set(components.indexOf(old), replacement);
    // the old id is kept, so later operations in the script reach the new component by it
    componentByName.replaceAll((id, c) -> c == old ? replacement : c);
    newSelection.remove(old);
    removed.add(old);
    newSelection.add(replacement);

    Map<Integer, Integer> pinMap = op.getPinMap() == null || op.getPinMap().isEmpty()
        ? matchPinsByLabel(old, replacement) : parsePinMap(op, replacement);

    // a wire goes as soon as one of its ends is unmapped, even if the other one could be moved
    Map<IDIYComponent<?>, Integer> unmappedWires = new LinkedHashMap<>();
    for (AttachedEnd end : attachedEnds) {
      if (!pinMap.containsKey(end.oldPin())) {
        unmappedWires.putIfAbsent(end.wire(), end.oldPin());
      }
    }
    for (Map.Entry<IDIYComponent<?>, Integer> entry : unmappedWires.entrySet()) {
      removeComponent(entry.getKey(), project, componentByName, newSelection, removed);
      warnings.add("Removed '" + entry.getKey().getName() + "', attached to "
          + op.getComponentName() + "." + entry.getValue()
          + ", which has no counterpart on the new component.");
    }
    for (AttachedEnd end : attachedEnds) {
      if (!unmappedWires.containsKey(end.wire())) {
        moveWireEnd(end.wire(), end.pointIndex(),
            replacement.getControlPoint(pinMap.get(end.oldPin())));
      }
    }
  }

  private void removeComponent(IDIYComponent<?> target, Project project,
      Map<String, IDIYComponent<?>> componentByName, Set<IDIYComponent<?>> newSelection,
      Set<IDIYComponent<?>> removed) {
    // by entry, not name: the key may be a W3#a1b2 id while the name is W3
    componentByName.values().removeIf(c -> c == target);
    project.getComponents().remove(target);
    newSelection.remove(target);
    removed.add(target);
  }

  /**
   * Moves a component as a whole, so that its first control point lands on the target.
   */
  private static void moveTo(IDIYComponent<?> comp, Point2D target) {
    Point2D anchor = comp.getControlPoint(0);
    double dx = target.getX() - anchor.getX();
    double dy = target.getY() - anchor.getY();
    for (int i = 0; i < comp.getControlPointCount(); i++) {
      Point2D p = comp.getControlPoint(i);
      comp.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
    }
  }

  /**
   * @return the terminal of the component that the given control point is stuck to, or -1
   */
  private static int findStuckPin(IDIYComponent<?> c, int index, IDIYComponent<?> component) {
    for (int pin = 0; pin < component.getControlPointCount(); pin++) {
      if (CalcUtils.areControlPointsStuck(c, index, component, pin)) {
        return pin;
      }
    }
    return -1;
  }

  private Map<Integer, Integer> parsePinMap(AiEditOperation op, IDIYComponent<?> replacement) {
    Map<Integer, Integer> pinMap = new HashMap<>();
    for (Map.Entry<String, String> entry : op.getPinMap().entrySet()) {
      int oldPin = parseIndex(entry.getKey());
      int newPin = parseIndex(entry.getValue());
      if (oldPin < 0) {
        warnings.add("Ignoring pinMap entry '" + entry.getKey() + "' of "
            + op.getComponentName() + " (not a terminal index).");
      } else if (newPin < 0 || newPin >= replacement.getControlPointCount()
          || !replacement.isControlPointSticky(newPin)) {
        warnings.add("pinMap of " + op.getComponentName() + " maps terminal " + oldPin + " to '"
            + entry.getValue() + "', which the new component does not have.");
      } else {
        pinMap.put(oldPin, newPin);
      }
    }
    return pinMap;
  }

  private static int parseIndex(String value) {
    if (value == null) {
      return -1;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  /**
   * Pairs the terminals of two components by their names, for a replace that came without a pin
   * map. Terminals without a name are left unpaired.
   */
  private static Map<Integer, Integer> matchPinsByLabel(IDIYComponent<?> old,
      IDIYComponent<?> replacement) {
    Map<Integer, Integer> pinMap = new HashMap<>();
    for (int oldPin = 0; oldPin < old.getControlPointCount(); oldPin++) {
      String label = getPinLabel(old, oldPin);
      if (label == null) {
        continue;
      }
      for (int newPin = 0; newPin < replacement.getControlPointCount(); newPin++) {
        if (label.equals(getPinLabel(replacement, newPin))) {
          pinMap.put(oldPin, newPin);
          break;
        }
      }
    }
    return pinMap;
  }

  private static String getPinLabel(IDIYComponent<?> component, int index) {
    if (!component.isControlPointSticky(index)) {
      return null;
    }
    String name = component.getControlPointNodeName(index);
    return name == null || name.isBlank() ? null : name.trim().toLowerCase();
  }

  /**
   * Moves one end of a wire and leaves the rest of it alone. On a curve, the handle next to that
   * end moves along with it, so the curve leaves the terminal at the same angle as before.
   */
  private static void moveWireEnd(IDIYComponent<?> wire, int index, Point2D target) {
    Point2D end = wire.getControlPoint(index);
    double dx = target.getX() - end.getX();
    double dy = target.getY() - end.getY();
    if (wire instanceof AbstractCurvedComponent) {
      int last = wire.getControlPointCount() - 1;
      int handle = index == 0 ? 1 : last - 1;
      // a three point curve shares its only handle between both ends, so neither end owns it
      if (handle != last - handle && !wire.isControlPointSticky(handle)) {
        Point2D p = wire.getControlPoint(handle);
        wire.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), handle);
      }
    }
    // the end is set last: a smooth curve realigns its other handles around the point that was
    // set last, unless that point is an end
    wire.setControlPoint(new Point2D.Double(target.getX(), target.getY()), index);
  }

  private void applyProperties(IDIYComponent<?> comp, Map<String, String> properties, ComponentProcessor processor) {
    if (properties == null || properties.isEmpty()) return;

    List<PropertyWrapper> compProps = processor.extractProperties(comp.getClass());
    for (Map.Entry<String, String> entry : properties.entrySet()) {
      String propName = entry.getKey();
      String propValue = entry.getValue();

      for (PropertyWrapper pw : compProps) {
        if (pw.getName().equalsIgnoreCase(propName) && !pw.isReadOnly()) {
          try {
            pw.readFrom(comp);
            Object parsedValue = parsePropertyValue(pw.getType(), propValue);
            pw.setValue(parsedValue);
            pw.writeTo(comp);
          } catch (Exception e) {
            LOG.warn("Failed to set property " + propName + " to " + propValue + " on " + comp.getName(), e);
            warnings.add("Could not set property '" + propName + "' on " + comp.getName() + ": " + e.getMessage());
          }
          break;
        }
      }
    }
  }

  /**
   * Carries a property over to a component of another type, if both have it and it holds the same
   * kind of value on both.
   */
  private void copyProperty(String name, IDIYComponent<?> from, IDIYComponent<?> to,
      ComponentProcessor processor) throws Exception {
    PropertyWrapper source = findProperty(name, from, processor);
    PropertyWrapper target = findProperty(name, to, processor);
    if (source == null || target == null || target.isReadOnly()
        || source.getType() != target.getType()) {
      return;
    }
    source.readFrom(from);
    target.setValue(source.getValue());
    target.writeTo(to);
  }

  private static PropertyWrapper findProperty(String name, IDIYComponent<?> comp,
      ComponentProcessor processor) {
    for (PropertyWrapper pw : processor.extractProperties(comp.getClass())) {
      if (pw.getName().equalsIgnoreCase(name)) {
        return pw;
      }
    }
    return null;
  }

  private void applyDatasheetModel(IDIYComponent<?> comp, String datasheetModel) {
    if (!(comp instanceof IDatasheetSupport)) {
      warnings.add("Component '" + comp.getName() + "' does not take datasheet models.");
      return;
    }
    DatasheetService datasheetService = DatasheetService.getInstance();
    List<String[]> rows = datasheetService.findSeries(comp.getClass(), datasheetModel.trim());
    if (rows.isEmpty()) {
      warnings.add("No datasheet model '" + datasheetModel + "' found for " + comp.getName() + ".");
      return;
    }
    String[] model = datasheetService.findModel(comp, rows);
    if (model == null) {
      warnings.add("Datasheet model '" + datasheetModel + "' has no part with the value "
          + comp.getValue() + " of " + comp.getName() + ".");
      return;
    }
    ((IDatasheetSupport) comp).applyModel(model);
  }

  private Object parsePropertyValue(Class<?> type, String value) throws Exception {
    if (type == String.class) return value;
    if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
    if (type == Integer.class || type == int.class) return Integer.parseInt(value);
    if (type == Double.class || type == double.class) return Double.parseDouble(value);
    if (type == Byte.class || type == byte.class) return Byte.parseByte(value);
    if (type == Long.class || type == long.class) return Long.parseLong(value);
    
    if (type == Color.class) {
      // Support hex (#RRGGBB), and common color names
      if (value.startsWith("#")) {
        return Color.decode(value);
      }
      try {
        java.lang.reflect.Field field = Color.class.getField(value.toLowerCase());
        return (Color) field.get(null);
      } catch (NoSuchFieldException e) {
        return Color.decode(value);
      }
    }
    
    if (type.isEnum()) {
      for (Object enumConstant : type.getEnumConstants()) {
        if (enumConstant.toString().equalsIgnoreCase(value) || ((Enum<?>) enumConstant).name().equalsIgnoreCase(value)) {
          return enumConstant;
        }
      }
      throw new IllegalArgumentException("Unknown enum value: " + value);
    }
    
    if (Percentage.class.isAssignableFrom(type)) {
      String cleanVal = value.replace("%", "").trim();
      return new Percentage(Integer.parseInt(cleanVal));
    }
    
    if (AbstractMeasure.class.isAssignableFrom(type)) {
      for (Method m : type.getMethods()) {
        if (Modifier.isStatic(m.getModifiers()) 
            && m.getName().startsWith("parse")
            && m.getParameterCount() == 1 
            && m.getParameterTypes()[0] == String.class) {
          return m.invoke(null, value);
        }
      }
    }
    throw new IllegalArgumentException("Unsupported property type: " + type.getName());
  }

  private ComponentType findComponentType(ComponentProcessor processor, String typeName) {
    if (typeName == null) return null;
    String cleanTypeName = typeName.replace("org.diylc.components.", "");
    for (Map.Entry<String, List<ComponentType>> entry : processor.getComponentTypes().entrySet()) {
      for (ComponentType ct : entry.getValue()) {
        String canonical = ct.getInstanceClass().getCanonicalName();
        String cleanCanonical = canonical.replace("org.diylc.components.", "");
        if (cleanCanonical.equalsIgnoreCase(cleanTypeName) || canonical.equalsIgnoreCase(typeName)) {
          return ct;
        }
        // Fallback for backwards compatibility with old scripts that might use the display name
        if (ct.getName().equalsIgnoreCase(typeName)) {
          return ct;
        }
      }
    }
    return null;
  }

  /**
   * Looks a component up for an operation that removes or changes it, where acting on the wrong
   * component is worse than reporting that none was found. Only an exact name, or a name that
   * differs in case alone and matches a single component, is accepted.
   */
  private IDIYComponent<?> findExactComponent(String searchName,
      Map<String, IDIYComponent<?>> map) {
    if (searchName == null) {
      return null;
    }
    String name = searchName.trim();
    if (map.containsKey(name)) {
      return map.get(name);
    }
    IDIYComponent<?> match = null;
    for (Map.Entry<String, IDIYComponent<?>> entry : map.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(name)) {
        if (match != null) {
          return null;
        }
        match = entry.getValue();
      }
    }
    return match;
  }

  /**
   * Looks a component up for a reference that only adds to the project, such as a wire terminal,
   * where a near miss in the name is worth forgiving.
   */
  private IDIYComponent<?> findComponentByName(String searchName, Map<String, IDIYComponent<?>> map) {
    if (searchName == null) return null;
    String name = searchName.trim();

    // 1. Exact match
    if (map.containsKey(name)) {
      return map.get(name);
    }

    // 2. Case-insensitive match
    for (Map.Entry<String, IDIYComponent<?>> entry : map.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(name)) {
        return entry.getValue();
      }
    }

    // 3. Match ignoring spaces/underscores/hyphens
    String normalizedSearch = name.replaceAll("[\\s_\\-]+", "").toLowerCase();
    for (Map.Entry<String, IDIYComponent<?>> entry : map.entrySet()) {
      String normalizedKey = entry.getKey().replaceAll("[\\s_\\-]+", "").toLowerCase();
      if (normalizedKey.equals(normalizedSearch)) {
        return entry.getValue();
      }
    }

    // 4. Prefix match (e.g. "Neck" matching "Neck Pickup" or "Neck P")
    List<IDIYComponent<?>> prefixMatches = new ArrayList<>();
    for (Map.Entry<String, IDIYComponent<?>> entry : map.entrySet()) {
      if (entry.getKey().toLowerCase().startsWith(name.toLowerCase())) {
        prefixMatches.add(entry.getValue());
      }
    }
    if (prefixMatches.size() == 1) {
      return prefixMatches.get(0);
    }

    return null;
  }

  private Point2D resolveTerminal(String terminalRef, Map<String, IDIYComponent<?>> componentByName) {
    if (terminalRef == null) return null;
    int lastDot = terminalRef.lastIndexOf('.');
    if (lastDot == -1) {
      warnings.add("Invalid terminal reference format: " + terminalRef + " (expected 'CompName.PinIndex')");
      return null;
    }
    String compName = terminalRef.substring(0, lastDot);
    String pinStr = terminalRef.substring(lastDot + 1);
    
    IDIYComponent<?> comp = findComponentByName(compName, componentByName);
    if (comp == null) {
      warnings.add("Terminal reference points to unknown component: " + compName);
      return null;
    }
    
    int resolvedIndex = -1;

    // A control point index is what both the project description and the component catalog give
    // the model, so a numeric reference is an index and nothing else. Node names must not be tried
    // first: they are usually the one-based lug numbers, which made "R1.1" land on the resistor's
    // first lead and left the second lead unreachable.
    try {
      int pinIndex = Integer.parseInt(pinStr.trim());
      if (pinIndex >= 0 && pinIndex < comp.getControlPointCount()) {
        resolvedIndex = pinIndex;
      }
    } catch (NumberFormatException e) {
      // not an index, so it must be a node name
    }

    // Fallback: match by node name, which covers references written as "Tip" or "+"
    if (resolvedIndex == -1) {
      for (int i = 0; i < comp.getControlPointCount(); i++) {
        String nodeName = comp.getControlPointNodeName(i);
        if (pinStr.equalsIgnoreCase(nodeName)) {
          resolvedIndex = i;
          break;
        }
      }
    }

    if (resolvedIndex == -1) {
      warnings.add("Terminal reference pin not found (name or index): " + terminalRef);
      return null;
    }

    // Return a copy to avoid accidentally moving the source component's pin
    Point2D p = comp.getControlPoint(resolvedIndex);
    return new Point2D.Double(p.getX(), p.getY());
  }

  /**
   * A wire end found on a terminal of a component that is about to be replaced.
   */
  private record AttachedEnd(IDIYComponent<?> wire, int pointIndex, int oldPin) {
  }
}
