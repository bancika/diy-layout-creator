package org.diylc.swing.actions.edit;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.diylc.clipboard.ComponentTransferableFactory;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ActionFactory;
import org.diylc.utils.IconLoader;

public class CopyAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private Clipboard clipboard;
  private ClipboardOwner clipboardOwner;

  public CopyAction(IPlugInPort plugInPort, Clipboard clipboard, ClipboardOwner clipboardOwner) {
    super();
    this.plugInPort = plugInPort;
    this.clipboard = clipboard;
    this.clipboardOwner = clipboardOwner;
    putValue(AbstractAction.NAME, "Copy");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    putValue(AbstractAction.SMALL_ICON, IconLoader.Copy.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Copy triggered");
    clipboard.setContents(ComponentTransferableFactory.getInstance()
        .build(plugInPort.getSelectedComponents(), plugInPort.getCurrentProject().getGroups()),
        clipboardOwner);
  }
}