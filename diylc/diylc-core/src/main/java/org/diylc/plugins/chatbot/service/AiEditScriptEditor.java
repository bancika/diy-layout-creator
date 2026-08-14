package org.diylc.plugins.chatbot.service;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import org.apache.log4j.Logger;
import org.diylc.common.ComponentType;
import org.diylc.common.IProjectEditor;
import org.diylc.common.Percentage;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.measures.AbstractMeasure;
import org.diylc.core.IDatasheetSupport;
import org.diylc.core.measures.Capacitance;
import org.diylc.plugins.chatbot.model.AiEditOperation;
import org.diylc.plugins.chatbot.model.AiEditScript;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.DatasheetService;

/**
 * Executes an AiEditScript against a Project.
 * <p>
 * Uses a two-pass approach:
 * <ul>
 *   <li><b>Pass 1</b>: Remove, Modify, and Add (non-wires). Sets properties and determines final pin geometry.</li>
 *   <li><b>Pass 2</b>: Add Wires. Resolves terminal references to the final pixel coordinates of the components from Pass 1.</li>
 * </ul>
 */
public class AiEditScriptEditor implements IProjectEditor {

  private static final Logger LOG = Logger.getLogger(AiEditScriptEditor.class);

  private final AiEditScript editScript;
  private final List<String> warnings = new ArrayList<>();

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
    Map<String, IDIYComponent<?>> componentByName = new HashMap<>();

    // Populate initial lookup map
    for (IDIYComponent<?> comp : project.getComponents()) {
      componentByName.put(comp.getName(), comp);
    }

    ComponentProcessor processor = ComponentProcessor.getInstance();
    
    // Split operations into two passes and sort Pass 1 into remove → modify → add order
    List<AiEditOperation> removeOps = new ArrayList<>();
    List<AiEditOperation> modifyOps = new ArrayList<>();
    List<AiEditOperation> addOps = new ArrayList<>();
    List<AiEditOperation> pass2Ops = new ArrayList<>();
    List<AiEditOperation> zOrderOps = new ArrayList<>();

    for (AiEditOperation op : editScript.getOperations()) {
      if ("send_behind".equalsIgnoreCase(op.getAction()) || "bring_in_front".equalsIgnoreCase(op.getAction())) {
        zOrderOps.add(op);
      } else if ("add".equalsIgnoreCase(op.getAction()) && op.getFromTerminal() != null && op.getToTerminal() != null) {
        pass2Ops.add(op);
      } else if ("remove".equalsIgnoreCase(op.getAction())) {
        removeOps.add(op);
      } else if ("modify".equalsIgnoreCase(op.getAction())) {
        modifyOps.add(op);
      } else {
        addOps.add(op);
      }
    }

    List<AiEditOperation> pass1Ops = new ArrayList<>();
    pass1Ops.addAll(removeOps);
    pass1Ops.addAll(modifyOps);
    pass1Ops.addAll(addOps);

    // ==========================================
    // PASS 1: Remove, Modify, and Add Non-Wires
    // ==========================================
    for (AiEditOperation op : pass1Ops) {
      try {
        if ("remove".equalsIgnoreCase(op.getAction())) {
          IDIYComponent<?> target = findComponentByName(op.getComponentName(), componentByName);
          if (target != null) {
            componentByName.remove(target.getName());
            project.getComponents().remove(target);
            newSelection.remove(target);
          } else {
            warnings.add("Cannot remove component '" + op.getComponentName() + "' (not found).");
          }
        } 
        else if ("modify".equalsIgnoreCase(op.getAction())) {
          IDIYComponent<?> target = findComponentByName(op.getComponentName(), componentByName);
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
        else if ("add".equalsIgnoreCase(op.getAction())) {
          ComponentType ct = findComponentType(processor, op.getComponentType());
          if (ct == null) {
            warnings.add("Cannot add '" + op.getComponentName() + "' (unknown type: " + op.getComponentType() + " - consider updating DIYLC).");
            continue;
          }
          
          IDIYComponent<?> comp = ct.getInstanceClass().getDeclaredConstructor().newInstance();
          comp.setName(op.getComponentName());
          comp.createdIn(project);
          
          // Apply properties first so geometry (e.g. pin count, orientation) is computed correctly
          applyProperties(comp, op.getProperties(), processor);
          
          // Apply datasheet model if specified
          if (op.getDatasheetModel() != null && !op.getDatasheetModel().isEmpty()) {
            applyDatasheetModel(comp, op.getDatasheetModel());
          }
          
          // Position the component
          if (ct.getCreationMethod() == CreationMethod.SINGLE_CLICK) {
            if (op.getPosition() != null) {
              Point2D targetPixels = op.getPosition().toPixels(project.getGridSpacing());
              Point2D currentAnchor = comp.getControlPoint(0);
              double dx = targetPixels.getX() - currentAnchor.getX();
              double dy = targetPixels.getY() - currentAnchor.getY();
              
              for (int i = 0; i < comp.getControlPointCount(); i++) {
                Point2D p = comp.getControlPoint(i);
                comp.setControlPoint(new Point2D.Double(p.getX() + dx, p.getY() + dy), i);
              }
            } else {
              warnings.add("Component '" + op.getComponentName() + "' is missing 'position' for SINGLE_CLICK placement.");
            }
          } else { // POINT_BY_POINT
            if (op.getFromPos() != null && op.getToPos() != null) {
              Point2D from = op.getFromPos().toPixels(project.getGridSpacing());
              Point2D to = op.getToPos().toPixels(project.getGridSpacing());
              comp.setControlPoint(from, 0);
              comp.setControlPoint(to, comp.getControlPointCount() - 1);
              
              // For components with >2 control points, place intermediate points at center
              if (comp.getControlPointCount() > 2) {
                double cx = (from.getX() + to.getX()) / 2;
                double cy = (from.getY() + to.getY()) / 2;
                for (int i = 1; i < comp.getControlPointCount() - 1; i++) {
                  comp.setControlPoint(new Point2D.Double(cx, cy), i);
                }
              }
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

        IDIYComponent<?> wire = ct.getInstanceClass().getDeclaredConstructor().newInstance();
        wire.setName(op.getComponentName());
        applyProperties(wire, op.getProperties(), processor);
        
        int pCount = wire.getControlPointCount();
        wire.setControlPoint(startPoint, 0);
        wire.setControlPoint(endPoint, pCount - 1);
        
        // For wires with >2 control points (like HookupWire), distribute intermediate points linearly
        if (pCount > 2) {
          double dx = (endPoint.getX() - startPoint.getX()) / (pCount - 1);
          double dy = (endPoint.getY() - startPoint.getY()) / (pCount - 1);
          for (int i = 1; i < pCount - 1; i++) {
            wire.setControlPoint(new Point2D.Double(startPoint.getX() + dx * i, startPoint.getY() + dy * i), i);
          }
        }
        
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

    return newSelection.isEmpty() ? selection : newSelection;
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

  private void applyDatasheetModel(IDIYComponent<?> comp, String datasheetModel) {
    if (!(comp instanceof IDatasheetSupport) || datasheetModel == null || datasheetModel.trim().isEmpty()) {
      return;
    }
    List<String[]> datasheet = DatasheetService.getInstance().loadDatasheet(comp.getClass());
    if (datasheet == null || datasheet.isEmpty()) {
      return;
    }

    String targetModel = datasheetModel.trim();
    List<String[]> modelCandidates = new ArrayList<>();
    for (String[] row : datasheet) {
      if (row.length > 0 && row[0].trim().equalsIgnoreCase(targetModel)) {
        modelCandidates.add(row);
      }
    }

    if (modelCandidates.isEmpty()) {
      for (String[] row : datasheet) {
        if (row.length > 0 && row[0].trim().toLowerCase().contains(targetModel.toLowerCase())) {
          modelCandidates.add(row);
        }
      }
    }

    if (modelCandidates.isEmpty()) {
      LOG.warn("No datasheet model found matching '" + datasheetModel + "' for " + comp.getName());
      return;
    }

    if (modelCandidates.size() == 1) {
      try {
        ((IDatasheetSupport) comp).applyModel(modelCandidates.get(0));
      } catch (Exception e) {
        LOG.warn("Failed to apply datasheet model " + datasheetModel + " to " + comp.getName(), e);
      }
      return;
    }

    String[] bestMatch = modelCandidates.get(0);
    Object valObj = comp.getValue();
    
    Double compCapValue = null;
    if (valObj instanceof Capacitance) {
      compCapValue = ((Capacitance) valObj).getNormalizedValue();
    } else if (valObj != null) {
      try {
        compCapValue = Capacitance.parseCapacitance(valObj.toString()).getNormalizedValue();
      } catch (Exception ignored) {}
    }

    for (String[] row : modelCandidates) {
      if (row.length >= 3 && compCapValue != null) {
        try {
          String[] capParts = row[2].split(" ");
          Capacitance rowCap = Capacitance.parseCapacitance(capParts[0] + capParts[1]);
          if (rowCap.getNormalizedValue() != null && Math.abs(rowCap.getNormalizedValue() - compCapValue) / compCapValue < 0.05) {
            bestMatch = row;
            break;
          }
        } catch (Exception ignored) {}
      }
    }

    try {
      ((IDatasheetSupport) comp).applyModel(bestMatch);
    } catch (Exception e) {
      LOG.warn("Failed to apply datasheet model " + datasheetModel + " to " + comp.getName(), e);
    }
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
    
    // First, try to match by node name
    for (int i = 0; i < comp.getControlPointCount(); i++) {
      String nodeName = comp.getControlPointNodeName(i);
      if (pinStr.equalsIgnoreCase(nodeName)) {
        resolvedIndex = i;
        break;
      }
    }
    
    // Fallback: try parsing as index
    if (resolvedIndex == -1) {
      try {
        int pinIndex = Integer.parseInt(pinStr);
        if (pinIndex >= 0 && pinIndex < comp.getControlPointCount()) {
          resolvedIndex = pinIndex;
        }
      } catch (NumberFormatException e) {
        // Ignored
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
}
