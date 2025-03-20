package org.diylc.swing.actions.edit;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;

import org.diylc.swingframework.ButtonDialog;

import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.Project;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.utils.IconLoader;

public class EditProjectAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public EditProjectAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Edit Project Settings");
    putValue(AbstractAction.SMALL_ICON, IconLoader.DocumentEdit.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Edit Project triggered");
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