package org.diylc.swing.actions.file;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.Project;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.swing.images.IconLoader;
import org.diylc.swingframework.ButtonDialog;

public class NewAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public NewAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "New");
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentPlain.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("NewAction triggered");
    if (!plugInPort.allowFileAction()) {
      return;
    }
    plugInPort.createNewProject();
    List<PropertyWrapper> properties = plugInPort.getProperties(plugInPort.getCurrentProject());
    PropertyEditorDialog editor =
        DialogFactory.getInstance().createPropertyEditorDialog(properties, "Edit Project", true);
    editor.setVisible(true);
    if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
      plugInPort.applyProperties(plugInPort.getCurrentProject(), properties);
    }
    // Save default values.
    for (PropertyWrapper property : editor.getDefaultedProperties()) {
      if (property.getValue() != null) {
        plugInPort.setDefaultPropertyValue(Project.class, property.getName(),
            property.getValue());
      }
    }
  }
}