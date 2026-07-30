package org.diylc.editor;

import java.util.HashSet;
import java.util.Set;

import org.diylc.common.IProjectEditor;
import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.components.guitar.pickup.PickupComponentFactory;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupDefinitionApplier;
import org.diylc.components.guitar.pickup.PickupReplacementService;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;

/**
 * Applies a {@link PickupDefinition} chosen from the pickup library dialog to every selected
 * pickup component. Run through {@code IPlugInPort#applyEditor(IProjectEditor)} like
 * {@code FlexibleLeadsEditor}, so it participates in undo/redo the same way every other editor
 * action does - no changes to the presenter/undo machinery were needed.
 *
 * <p>Two cases, per the task:
 * <ul>
 * <li><b>Same component type</b> - the selected pickup's component class already matches the
 * definition's format: updated in place via {@link PickupDefinitionApplier}.</li>
 * <li><b>Different component type</b> - handled by {@link PickupReplacementService}, which
 * builds a replacement component, preserves everything the task requires (name, anchor,
 * orientation, list position, label styling, alpha, group membership) and remaps only the
 * terminals that match by semantic name, leaving unmatched ones safely detached.</li>
 * </ul>
 *
 * <p>This class performs the mutation unconditionally once invoked - it is the caller's
 * responsibility (see {@code SelectPickupFromLibraryAction}) to have already checked
 * {@link PickupReplacementService.ReplacementPlan#requiresConfirmation()} and obtained explicit
 * user confirmation before triggering this editor for a cross-type replacement with unmatched
 * connected terminals.
 */
public class ApplyPickupDefinitionEditor implements IProjectEditor {

  private final PickupDefinition definition;
  private final PickupReplacementService replacementService;

  public ApplyPickupDefinitionEditor(PickupDefinition definition) {
    this(definition, new PickupReplacementService());
  }

  public ApplyPickupDefinitionEditor(PickupDefinition definition, PickupReplacementService replacementService) {
    this.definition = definition;
    this.replacementService = replacementService;
  }

  @Override
  public Set<IDIYComponent<?>> edit(Project project, Set<IDIYComponent<?>> selection) {
    Set<IDIYComponent<?>> newSelection = new HashSet<>();
    for (IDIYComponent<?> component : selection) {
      if (component instanceof AbstractGuitarPickup pickup) {
        if (PickupComponentFactory.isSameType(pickup, definition.format())) {
          PickupDefinitionApplier.applyFields(pickup, definition);
          newSelection.add(pickup);
        } else {
          newSelection.add(replacementService.replace(project, pickup, definition));
        }
      } else {
        newSelection.add(component);
      }
    }
    return newSelection;
  }

  @Override
  public String getEditAction() {
    return "Select Pickup from Library";
  }
}
