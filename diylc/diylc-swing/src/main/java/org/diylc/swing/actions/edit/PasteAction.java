package org.diylc.swing.actions.edit;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.clipboard.ComponentTransferable;
import org.diylc.common.IPlugInPort;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class PasteAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private Clipboard clipboard;

  public PasteAction(IPlugInPort plugInPort, Clipboard clipboard) {
    super();
    this.plugInPort = plugInPort;
    this.clipboard = clipboard;
    putValue(AbstractAction.NAME, "Paste");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
    putValue(AbstractAction.SMALL_ICON, IconLoader.Paste.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Paste triggered");
    try {
      ComponentTransferable componentTransferable =
          (ComponentTransferable) clipboard.getData(ComponentTransferable.listFlavor);
      plugInPort.pasteComponents(componentTransferable, false,
          ConfigurationManager.getInstance().readBoolean(Presenter.RENUMBER_ON_PASTE_KEY, true));
    } catch (Exception ex) {
      ActionFactory.LOG.error("Coule not paste.", ex);
    }
  }
}
