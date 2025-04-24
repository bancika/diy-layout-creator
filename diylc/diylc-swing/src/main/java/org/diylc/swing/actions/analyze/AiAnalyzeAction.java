package org.diylc.swing.actions.analyze;

import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Summary;
import org.diylc.swing.ISwingUI;
import org.diylc.swingframework.TextDialog;
import org.diylc.utils.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class AiAnalyzeAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  public AiAnalyzeAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "AI Circuit Analyzer");
//    putValue(AbstractAction.SMALL_ICON,
//        Enum.valueOf(IconLoader.class, summarizer.getIconName()).getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    swingUI.executeBackgroundTask(new ITask<String>() {

      @Override
      public String doInBackground() throws Exception {
        return plugInPort.getChatbotService().analyzeCircuit();
      }

      @Override
      public void failed(Exception e) {
        swingUI.showMessage(e.getMessage(), "AI Circuit Analyzer", ISwingUI.ERROR_MESSAGE);
      }

      @Override
      public void complete(String res) {
        if (res == null || res.isEmpty()) {
          swingUI.showMessage("The generated analysis is empty, nothing to show.",
              "AI Circuit Analyzer", ISwingUI.INFORMATION_MESSAGE);
          return;
        }
        new TextDialog(swingUI.getOwnerFrame().getRootPane(), res, "AI Circuit Analyzer",
            new Dimension(800, 600)).setVisible(true);
      }
    }, true);
  }
}
