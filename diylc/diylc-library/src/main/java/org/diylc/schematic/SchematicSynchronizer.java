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
package org.diylc.schematic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.diylc.components.schematic.SchematicWire;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.SchematicView;
import org.diylc.netlist.Netlist;
import org.diylc.presenter.ContinuityArea;
import org.diylc.schematic.SchematicBuilder.SymbolEntry;

/**
 * Incrementally reconciles an already generated {@link SchematicView} with the current state of the
 * layout {@link Project}. It is meant to run silently whenever the user switches to the schematic
 * tab.
 *
 * <ul>
 * <li>Components added to the layout get fresh symbols placed in a free grid cell.</li>
 * <li>Components removed from the layout lose their symbols (and the wires attached to them).</li>
 * <li>Symbols that survive keep their position, rotation and mirroring; only name and value are
 * refreshed.</li>
 * <li>All wires are regenerated from the current netlist.</li>
 * </ul>
 *
 * @author Branislav Stojkovic
 */
public class SchematicSynchronizer {

  private static final Logger LOG = Logger.getLogger(SchematicSynchronizer.class);

  private final SchematicBuilder builder = new SchematicBuilder();

  /**
   * Synchronizes the schematic view. If the project has no schematic view yet, or it is empty, a
   * full {@link SchematicBuilder#build} is performed instead.
   */
  public void synchronize(Project project, List<ContinuityArea> continuityAreas) {
    SchematicView view = project.getSchematicView();
    if (view == null || !view.isGenerated()) {
      builder.build(project, continuityAreas == null ? new ArrayList<ContinuityArea>() : continuityAreas);
      return;
    }

    Map<UUID, List<UUID>> map = view.getPhysicalToSchematicMap();
    Map<UUID, IDIYComponent<?>> symbolsById = new HashMap<UUID, IDIYComponent<?>>();
    for (IDIYComponent<?> component : view.getComponents()) {
      if (!(component instanceof SchematicWire)) {
        symbolsById.put(component.getId(), component);
      }
    }

    Map<UUID, IDIYComponent<?>> layoutById = new LinkedHashMap<UUID, IDIYComponent<?>>();
    for (IDIYComponent<?> component : project.getComponents()) {
      layoutById.put(component.getId(), component);
    }

    List<IDIYComponent<?>> keptSymbols = new ArrayList<IDIYComponent<?>>();
    Map<UUID, List<SymbolEntry>> entriesByPhysicalId = new LinkedHashMap<UUID, List<SymbolEntry>>();
    Map<UUID, List<UUID>> newMap = new LinkedHashMap<UUID, List<UUID>>();

    int occupiedSlots = map.size();

    for (Map.Entry<UUID, IDIYComponent<?>> layoutEntry : layoutById.entrySet()) {
      UUID physicalId = layoutEntry.getKey();
      IDIYComponent<?> physical = layoutEntry.getValue();
      if (!SchematicBuilder.isEligible(physical)) {
        continue;
      }

      List<SymbolEntry> freshEntries = builder.createSymbols(physical);
      if (freshEntries.isEmpty()) {
        continue;
      }

      List<UUID> existingIds = map.get(physicalId);
      List<SymbolEntry> resolvedEntries = new ArrayList<SymbolEntry>();
      List<UUID> resolvedIds = new ArrayList<UUID>();

      if (existingIds != null) {
        // component already present: keep positioned symbols, refresh labels, reuse fresh pin maps
        for (int i = 0; i < freshEntries.size(); i++) {
          SymbolEntry fresh = freshEntries.get(i);
          IDIYComponent<?> existing =
              i < existingIds.size() ? symbolsById.get(existingIds.get(i)) : null;
          if (existing != null) {
            copyLabels(fresh.symbol, existing);
            resolvedEntries.add(new SymbolEntry(existing, fresh.pinMapping));
            resolvedIds.add(existing.getId());
          } else {
            resolvedEntries.add(fresh);
            resolvedIds.add(fresh.symbol.getId());
          }
        }
        // place any brand new sub-symbols (e.g. section count grew) that had no counterpart
        if (resolvedEntries.size() > existingIds.size()) {
          SchematicBuilder.placeAtGridSlot(
              resolvedEntries.subList(existingIds.size(), resolvedEntries.size()), occupiedSlots++,
              occupiedSlots + 1);
        }
      } else {
        // new component
        resolvedEntries.addAll(freshEntries);
        for (SymbolEntry e : freshEntries) {
          resolvedIds.add(e.symbol.getId());
        }
        SchematicBuilder.placeAtGridSlot(freshEntries, occupiedSlots++, occupiedSlots + 1);
      }

      entriesByPhysicalId.put(physicalId, resolvedEntries);
      newMap.put(physicalId, resolvedIds);
      for (SymbolEntry e : resolvedEntries) {
        keptSymbols.add(e.symbol);
      }
    }

    // anything left in the old map that we did not resolve corresponds to a deleted component
    Set<UUID> removed = new HashSet<UUID>(map.keySet());
    removed.removeAll(newMap.keySet());
    if (!removed.isEmpty()) {
      LOG.debug("Removing schematic symbols for " + removed.size() + " deleted component(s)");
    }

    Netlist netlist = builder.extractNetlist(project,
        continuityAreas == null ? new ArrayList<ContinuityArea>() : continuityAreas);
    List<SchematicWire> wires = builder.createWires(netlist, entriesByPhysicalId);

    List<IDIYComponent<?>> result = new ArrayList<IDIYComponent<?>>(keptSymbols);
    result.addAll(wires);
    result.sort(java.util.Comparator.comparingDouble(SchematicBuilder::zOrderOf));

    view.getComponents().clear();
    view.getComponents().addAll(result);
    view.setPhysicalToSchematicMap(newMap);
    SchematicBuilder.resizeCanvasToFit(view);
  }

  private static void copyLabels(IDIYComponent<?> from, IDIYComponent<?> to) {
    to.setName(from.getName());
    try {
      Object value = from.getValue();
      @SuppressWarnings("unchecked")
      IDIYComponent<Object> target = (IDIYComponent<Object>) to;
      target.setValue(value);
    } catch (Exception e) {
      // value types differ between fresh and existing symbol; leave the existing value untouched
    }
  }

  public static void synchronize(Project project, Collection<ContinuityArea> continuityAreas,
      boolean unused) {
    new SchematicSynchronizer().synchronize(project,
        continuityAreas == null ? new ArrayList<ContinuityArea>()
            : new ArrayList<ContinuityArea>(continuityAreas));
  }
}
