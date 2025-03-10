package org.diylc.swing.actions.edit;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.swingframework.ButtonDialog;

import org.diylc.common.IPlugInPort;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.measures.Nudge;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.swing.images.IconLoader;

public class NudgeAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;

  public NudgeAction(IPlugInPort plugInPort) {
    super();
    this.plugInPort = plugInPort;
    putValue(AbstractAction.NAME, "Nudge");
    putValue(AbstractAction.SMALL_ICON, IconLoader.FitToSize.getIcon());
    putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q,
        Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    ActionFactory.LOG.info("Nudge triggered");
    Nudge n = new Nudge();
    boolean metric = ConfigurationManager.getInstance().readBoolean(Presenter.METRIC_KEY, true);
    if (metric) {
      n.setxOffset(new Size(0d, SizeUnit.mm));
      n.setyOffset(new Size(0d, SizeUnit.mm));
    } else {
      n.setxOffset(new Size(0d, SizeUnit.in));
      n.setyOffset(new Size(0d, SizeUnit.in));
    }
    List<PropertyWrapper> properties = plugInPort.getProperties(n);
    PropertyEditorDialog editor = DialogFactory.getInstance()
        .createPropertyEditorDialog(properties, "Nudge Selection", false);
    editor.setVisible(true);
    if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
      plugInPort.applyProperties(n, properties);
      plugInPort.nudgeSelection(n.getxOffset(), n.getyOffset(), n.getAffectStuckComponents());
    }
  }
}