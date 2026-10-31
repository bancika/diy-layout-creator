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

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.presenter.ComponentProcessor;
import org.diylc.presenter.InstantiationManager;

/**
 * What the operations of one edit script share: the project they change, the components they can
 * name, what the edit leaves selected and what it has taken out. Also resolves the component names
 * and terminal references the script uses.
 *
 * @author Branislav Stojkovic
 */
class AiEditContext {

  private final Project project;
  private final List<String> warnings;
  private final ComponentProcessor processor = ComponentProcessor.getInstance();
  private final InstantiationManager instantiationManager = new InstantiationManager();
  private final AiPropertyApplier propertyApplier;
  private final Map<String, IDIYComponent<?>> componentByName = new HashMap<>();
  private final Set<IDIYComponent<?>> newSelection = new HashSet<>();
  private final Set<IDIYComponent<?>> removed = new HashSet<>();

  AiEditContext(Project project, List<String> warnings) {
    this.project = project;
    this.warnings = warnings;
    this.propertyApplier = new AiPropertyApplier(processor, warnings);
    // the script names components by the ids they were exported with, W3#a1b2 for a repeated name
    for (Map.Entry<IDIYComponent<?>, String> entry
        : AiProjectBuilder.assignIds(project.getComponents()).entrySet()) {
      componentByName.put(entry.getValue(), entry.getKey());
    }
  }

  Project getProject() {
    return project;
  }

  AiPropertyApplier getPropertyApplier() {
    return propertyApplier;
  }

  Set<IDIYComponent<?>> getNewSelection() {
    return newSelection;
  }

  Set<IDIYComponent<?>> getRemoved() {
    return removed;
  }

  void warn(String warning) {
    warnings.add(warning);
  }

  void select(IDIYComponent<?> component) {
    newSelection.add(component);
  }

  /**
   * Creates a component the way the toolbox does, with the defaults the user saved for its type,
   * but under the name the script gave it, because later operations refer to it by that name.
   */
  IDIYComponent<?> instantiate(ComponentType ct, String name) throws Exception {
    IDIYComponent<?> comp = ct.getInstanceClass().getDeclaredConstructor().newInstance();
    comp.createdIn(project);
    instantiationManager.fillWithDefaultProperties(comp, null);
    if (name == null || name.isBlank()) {
      name = instantiationManager.createUniqueName(ct, project.getComponents());
    }
    comp.setName(name);
    return comp;
  }

  void addComponent(IDIYComponent<?> comp) {
    project.getComponents().add(comp);
    componentByName.put(comp.getName(), comp);
    newSelection.add(comp);
  }

  void removeComponent(IDIYComponent<?> target) {
    // by entry, not name: the key may be a W3#a1b2 id while the name is W3
    componentByName.values().removeIf(c -> c == target);
    project.getComponents().remove(target);
    newSelection.remove(target);
    removed.add(target);
  }

  /**
   * Puts a component in the place of another, in the z-order as well as under the other one's id.
   */
  void substitute(IDIYComponent<?> old, IDIYComponent<?> replacement) {
    List<IDIYComponent<?>> components = project.getComponents();
    components.set(components.indexOf(old), replacement);
    // the old id is kept, so later operations in the script reach the new component by it
    componentByName.replaceAll((id, c) -> c == old ? replacement : c);
    newSelection.remove(old);
    removed.add(old);
    newSelection.add(replacement);
  }

  ComponentType findComponentType(String typeName) {
    if (typeName == null) {
      return null;
    }
    String cleanTypeName = typeName.replace("org.diylc.components.", "");
    for (Map.Entry<String, List<ComponentType>> entry : processor.getComponentTypes().entrySet()) {
      for (ComponentType ct : entry.getValue()) {
        String canonical = ct.getInstanceClass().getCanonicalName();
        String cleanCanonical = canonical.replace("org.diylc.components.", "");
        if (cleanCanonical.equalsIgnoreCase(cleanTypeName)
            || canonical.equalsIgnoreCase(typeName)) {
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
  IDIYComponent<?> findExactComponent(String searchName) {
    if (searchName == null) {
      return null;
    }
    String name = searchName.trim();
    if (componentByName.containsKey(name)) {
      return componentByName.get(name);
    }
    IDIYComponent<?> match = null;
    for (Map.Entry<String, IDIYComponent<?>> entry : componentByName.entrySet()) {
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
  IDIYComponent<?> findComponentByName(String searchName) {
    if (searchName == null) {
      return null;
    }
    String name = searchName.trim();

    // 1. Exact match
    if (componentByName.containsKey(name)) {
      return componentByName.get(name);
    }

    // 2. Case-insensitive match
    for (Map.Entry<String, IDIYComponent<?>> entry : componentByName.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(name)) {
        return entry.getValue();
      }
    }

    // 3. Match ignoring spaces/underscores/hyphens
    String normalizedSearch = name.replaceAll("[\\s_\\-]+", "").toLowerCase();
    for (Map.Entry<String, IDIYComponent<?>> entry : componentByName.entrySet()) {
      String normalizedKey = entry.getKey().replaceAll("[\\s_\\-]+", "").toLowerCase();
      if (normalizedKey.equals(normalizedSearch)) {
        return entry.getValue();
      }
    }

    // 4. Prefix match (e.g. "Neck" matching "Neck Pickup" or "Neck P")
    List<IDIYComponent<?>> prefixMatches = new ArrayList<>();
    for (Map.Entry<String, IDIYComponent<?>> entry : componentByName.entrySet()) {
      if (entry.getKey().toLowerCase().startsWith(name.toLowerCase())) {
        prefixMatches.add(entry.getValue());
      }
    }
    if (prefixMatches.size() == 1) {
      return prefixMatches.get(0);
    }

    return null;
  }

  Point2D resolveTerminal(String terminalRef) {
    if (terminalRef == null) {
      return null;
    }
    int lastDot = terminalRef.lastIndexOf('.');
    if (lastDot == -1) {
      warnings.add("Invalid terminal reference format: " + terminalRef
          + " (expected 'CompName.PinIndex')");
      return null;
    }
    String compName = terminalRef.substring(0, lastDot);
    String pinStr = terminalRef.substring(lastDot + 1);

    IDIYComponent<?> comp = findComponentByName(compName);
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

  static int parseIndex(String value) {
    if (value == null) {
      return -1;
    }
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return -1;
    }
  }
}
