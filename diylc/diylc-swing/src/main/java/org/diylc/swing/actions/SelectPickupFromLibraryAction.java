package org.diylc.swing.actions;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.diylc.common.IPlugInPort;
import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.components.guitar.pickup.PickupDefinition;
import org.diylc.components.guitar.pickup.PickupReplacementService;
import org.diylc.components.guitar.pickup.PickupReplacementService.ReplacementPlan;
import org.diylc.components.guitar.pickup.PickupReplacementService.UnmatchedConnection;
import org.diylc.core.IDIYComponent;
import org.diylc.editor.ApplyPickupDefinitionEditor;
import org.diylc.editor.PlacePickupFromLibraryEditor;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.gui.PickupLibraryDialog;
import org.diylc.utils.IconLoader;

/**
 * "Guitar Pickups" submenu action, with two modes depending on the current selection at the time
 * it is invoked:
 * <ul>
 * <li><b>Replace</b> - exactly one pickup is selected (the menu was reached by right-clicking an
 * existing pickup): opens {@link PickupLibraryDialog} pre-filled from that pickup and, if
 * applied, runs {@link ApplyPickupDefinitionEditor} as one undoable operation.</li>
 * <li><b>Place</b> - otherwise (the menu was reached by right-clicking the empty canvas/base
 * layer): opens the same dialog with no pre-fill and, if applied, places a brand new pickup of
 * the chosen definition's format at {@link #setPlacementPoint}'s last-set location via
 * {@link PlacePickupFromLibraryEditor}, as one undoable operation.</li>
 * </ul>
 *
 * <p>For the replace case, a cross-type replacement is first analysed with
 * {@link PickupReplacementService#plan}. If that reports any currently-connected terminals that
 * cannot be matched to the new pickup, the user is shown a confirmation describing them,
 * <b>defaulting to Cancel</b> - the replacement (and the disconnection of those leads) only goes
 * ahead on explicit confirmation. Same-type updates and cross-type replacements with nothing at
 * risk proceed immediately, with no prompt (seamless, per the task). The place case never needs
 * this check - there is nothing to disconnect when placing a brand new component.
 */
public class SelectPickupFromLibraryAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private final IPlugInPort plugInPort;
  private final Component owner;
  private final PickupReplacementService replacementService;
  private Point placementPoint;

  public SelectPickupFromLibraryAction(IPlugInPort plugInPort, Component owner) {
    this(plugInPort, owner, new PickupReplacementService());
  }

  public SelectPickupFromLibraryAction(IPlugInPort plugInPort, Component owner,
      PickupReplacementService replacementService) {
    super();
    this.plugInPort = plugInPort;
    this.owner = owner;
    this.replacementService = replacementService;
    putValue(AbstractAction.NAME, "Select Pickup from Library...");
    putValue(AbstractAction.SMALL_ICON, IconLoader.ComponentReplace.getIcon());
  }

  /**
   * Records where (in raw, on-screen canvas coordinates) a subsequent "place" invocation should
   * put the new pickup. Called by {@code ComponentPopupMenu} every time the popup menu is shown,
   * from the same coordinates used to position the menu itself.
   */
  public void setPlacementPoint(Point placementPoint) {
    this.placementPoint = placementPoint;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Collection<IDIYComponent<?>> selection = plugInPort.getSelectedComponents();
    if (selection.size() == 1 && selection.iterator().next() instanceof AbstractGuitarPickup pickup) {
      replaceSelectedPickup(pickup);
    } else {
      placeNewPickup();
    }
  }

  private void replaceSelectedPickup(AbstractGuitarPickup pickup) {
    ActionFactory.LOG.info("Select Pickup from Library (replace) triggered");
    PickupDefinition chosen = PickupLibraryDialog.showDialog(owner, pickup);
    if (chosen == null) {
      return;
    }

    ReplacementPlan plan = replacementService.plan(plugInPort.getCurrentProject(), pickup, chosen);
    if (plan.requiresConfirmation() && !confirmUnmatchedConnections(plan)) {
      return;
    }

    plugInPort.applyEditor(new ApplyPickupDefinitionEditor(chosen));
  }

  private void placeNewPickup() {
    ActionFactory.LOG.info("Select Pickup from Library (place) triggered");
    if (placementPoint == null) {
      return;
    }
    PickupDefinition chosen = PickupLibraryDialog.showDialog(owner, null);
    if (chosen == null) {
      return;
    }
    Point2D location = plugInPort.getModelPoint(placementPoint);
    plugInPort.applyEditor(new PlacePickupFromLibraryEditor(chosen, location));
  }

  /** @return {@code true} only if the user explicitly confirmed; defaults to Cancel otherwise. */
  private boolean confirmUnmatchedConnections(ReplacementPlan plan) {
    StringBuilder message = new StringBuilder(
        "<html>Replacing this pickup will disconnect the following wired terminal(s), which have<br>"
            + "no equivalent on the new pickup:<ul>");
    for (UnmatchedConnection unmatched : plan.unmatchedConnections()) {
      message.append("<li>").append(unmatched.terminalName()).append("</li>");
    }
    message.append("</ul>All other matching connections will be preserved. Replace anyway?</html>");

    Object[] options = {"Replace Anyway", "Cancel"};
    int result = JOptionPane.showOptionDialog(owner, message.toString(), "Unmatched Connections",
        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);
    return result == 0;
  }
}
