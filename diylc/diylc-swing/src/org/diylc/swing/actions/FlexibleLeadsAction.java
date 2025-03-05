package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.diylc.common.IPlugInPort;
import org.diylc.editor.FlexibleLeadsEditor;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.images.IconLoader;

public class FlexibleLeadsAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public FlexibleLeadsAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Add Flexible Leads");
    putValue(AbstractAction.SMALL_ICON, IconLoader.FlexibleLeads.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Add Flexible Leads triggered");
    plugInPort.applyEditor(new FlexibleLeadsEditor());
  }
}