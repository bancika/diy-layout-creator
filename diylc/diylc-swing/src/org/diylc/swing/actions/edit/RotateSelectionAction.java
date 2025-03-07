package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.images.IconLoader;

public class RotateSelectionAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private int direction;

  public RotateSelectionAction(IPlugInPort plugInPort, int direction) {
    super();
    this.plugInPort = plugInPort;
    this.direction = direction;
    if (direction > 0) {
      putValue(AbstractAction.NAME, "Rotate Clockwise");
      putValue(AbstractAction.SMALL_ICON, IconLoader.RotateCW.getIcon());
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
    } else {
      putValue(AbstractAction.NAME, "Rotate Counterclockwise");
      putValue(AbstractAction.SMALL_ICON, IconLoader.RotateCCW.getIcon());
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Rotate Selection triggered: " + direction);
    plugInPort.rotateSelection(direction);
  }
}