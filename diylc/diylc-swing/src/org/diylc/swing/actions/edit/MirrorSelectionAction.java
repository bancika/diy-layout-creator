package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.images.IconLoader;

public class MirrorSelectionAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private int direction;

  public MirrorSelectionAction(IPlugInPort plugInPort, int direction) {
    super();
    this.plugInPort = plugInPort;
    this.direction = direction;
    if (direction == IComponentTransformer.HORIZONTAL) {
      putValue(AbstractAction.NAME, "Mirror Horizontally");
      putValue(AbstractAction.SMALL_ICON, IconLoader.FlipHorizontal.getIcon());
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.ALT_MASK));
    } else {
      putValue(AbstractAction.NAME, "Mirror Vertically");
      putValue(AbstractAction.SMALL_ICON, IconLoader.FlipVertical.getIcon());
      putValue(AbstractAction.ACCELERATOR_KEY,
          KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.ALT_MASK));
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Mirror Selection triggered: " + direction);
    plugInPort.mirrorSelection(direction);
  }
}