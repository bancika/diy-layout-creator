package org.diylc.swing.actions.file;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class ExitAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public ExitAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Exit");
    putValue(AbstractAction.SMALL_ICON, IconLoader.Exit.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("ExitAction triggered");
    if (plugInPort.allowFileAction()) {
      System.exit(0);
    }
  }
}