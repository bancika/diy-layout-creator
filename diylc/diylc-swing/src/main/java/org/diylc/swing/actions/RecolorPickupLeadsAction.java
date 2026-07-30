package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import javax.swing.AbstractAction;

import org.diylc.common.IPlugInPort;
import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.core.IDIYComponent;
import org.diylc.editor.RecolorPickupLeadsEditor;
import org.diylc.swing.ActionFactory;

/**
 * Right-click action shown (in the "Guitar Pickups" submenu) for a selected pickup: recolors any
 * already-attached {@code HookupWire} leads to match the lead colours the pickup's currently
 * applied {@code PickupDefinition} specifies, via {@link RecolorPickupLeadsEditor}. Needed
 * because switching a pickup to a different manufacturer/model deliberately never recolours
 * leads that already exist on the diagram - this is the explicit, on-demand way to do that
 * afterwards.
 */
public class RecolorPickupLeadsAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private final IPlugInPort plugInPort;

  public RecolorPickupLeadsAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Recolor Wires to Pickup Colors");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Collection<IDIYComponent<?>> selection = plugInPort.getSelectedComponents();
    if (selection.size() != 1) {
      return;
    }
    IDIYComponent<?> component = selection.iterator().next();
    if (!(component instanceof AbstractGuitarPickup pickup)) {
      return;
    }
    ActionFactory.LOG.info("Recolor Wires to Pickup Colors triggered");
    plugInPort.applyEditor(new RecolorPickupLeadsEditor(pickup));
  }
}
