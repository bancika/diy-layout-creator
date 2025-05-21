package org.diylc.swing.actions.analyze;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.plugins.compare.model.CompareResults;
import org.diylc.plugins.compare.util.CompareResultsHtmlReport;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.TextDialog;
import org.diylc.utils.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class CompareAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  public CompareAction(IPlugInPort plugInPort, ISwingUI swingUI) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    putValue(AbstractAction.NAME, "Compare With...");
    putValue(AbstractAction.SMALL_ICON, IconLoader.Tables.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    final File file = DialogFactory.getInstance().showOpenDialog(FileFilterEnum.DIY.getFilter(),
        null, FileFilterEnum.DIY.getExtensions()[0], null);
    if (file == null) {
      return;
    }

    swingUI.executeBackgroundTask(new ITask<CompareResults>() {

      @Override
      public CompareResults doInBackground() throws Exception {
        return plugInPort.getCompareService().compareWith(file);
      }

      @Override
      public void failed(Exception e) {
        swingUI.showMessage(e.getMessage(), "Circuit Comparison", ISwingUI.ERROR_MESSAGE);
      }

      @Override
      public void complete(CompareResults res) {
        String htmlReport = CompareResultsHtmlReport.generateReport(res);
        new TextDialog(swingUI.getOwnerFrame().getRootPane(), htmlReport, "Circuit Comparison",
            new Dimension(800, 600)).setVisible(true);
      }
    }, true);
  }
}
