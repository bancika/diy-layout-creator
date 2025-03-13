package org.diylc.swing.actions;

import java.awt.event.ActionEvent;
import java.awt.geom.Area;
import java.util.Arrays;
import java.util.List;
import javax.swing.AbstractAction;

import org.diylc.swingframework.ButtonDialog;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.annotations.PositiveNonZeroMeasureValidator;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.lang.LangUtil;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.gui.editor.PropertyEditorDialog;
import org.diylc.utils.IconLoader;

public class CheckProximityAction extends AbstractAction implements Cloneable {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;
  private Size threshold;

  public CheckProximityAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Check Trace Proximity");
    putValue(AbstractAction.SMALL_ICON, IconLoader.TraceProximity.getIcon());
    this.threshold = new Size(0.5d, SizeUnit.mm);
  }

  public Size getThreshold() {
    return threshold;
  }

  public void setThreshold(Size threshold) {
    this.threshold = threshold;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    PropertyWrapper wrapper = new PropertyWrapper("Threshold", Size.class, "getThreshold",
        "setThreshold", false, new PositiveNonZeroMeasureValidator(), 0);
    try {
      wrapper.readFrom(CheckProximityAction.this);
    } catch (Exception e1) {
      ActionFactory.LOG.error("Error reading proximity threshold", e1);
    }

    List<PropertyWrapper> properties = Arrays.asList(wrapper);
    PropertyEditorDialog editor = DialogFactory.getInstance().createPropertyEditorDialog(
        properties, (String) CheckProximityAction.this.getValue(NAME), true);
    editor.setVisible(true);
    if (ButtonDialog.OK.equals(editor.getSelectedButtonCaption())) {
      try {
        wrapper.writeTo(CheckProximityAction.this);
      } catch (Exception e1) {
        ActionFactory.LOG.error("Error writing proximity threshold", e1);
      }

      swingUI.executeBackgroundTask(new ITask<List<Area>>() {

        @Override
        public List<Area> doInBackground() throws Exception {
          return plugInPort.checkContinuityAreaProximity(threshold);
        }

        @Override
        public void failed(Exception e) {
          swingUI.showMessage(e.getMessage(),
              LangUtil.translate((String) CheckProximityAction.this.getValue(NAME)),
              ISwingUI.INFORMATION_MESSAGE);
        }

        @Override
        public void complete(List<Area> res) {
          if (res == null || res.size() == 0)
            swingUI.showMessage(LangUtil.translate("No proximity issues detected."),
                LangUtil.translate((String) CheckProximityAction.this.getValue(NAME)),
                ISwingUI.INFORMATION_MESSAGE);
          else
            swingUI
                .showMessage(
                    String.format(
                        LangUtil.translate(
                            "%s potential proximity issue(s) detected and marked red."),
                        res.size()),
                    LangUtil.translate((String) CheckProximityAction.this.getValue(NAME)),
                    ISwingUI.WARNING_MESSAGE);
        }
      }, true);
    }
  }

  @Override
  public Object clone() throws CloneNotSupportedException {
    return new CheckProximityAction(plugInPort, swingUI);
  }
}