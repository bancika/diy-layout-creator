package org.diylc.swing.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Summary;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.images.IconLoader;
import org.diylc.swingframework.TextDialog;

public class SummarizeNetlistAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;
  private INetlistAnalyzer summarizer;

  public SummarizeNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI,
      INetlistAnalyzer summarizer) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    this.summarizer = summarizer;
    putValue(AbstractAction.NAME, summarizer.getName());
    putValue(AbstractAction.SMALL_ICON,
        Enum.valueOf(IconLoader.class, summarizer.getIconName()).getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    swingUI.executeBackgroundTask(new ITask<List<Summary>>() {

      @Override
      public List<Summary> doInBackground() throws Exception {
        List<Netlist> netlists = plugInPort.extractNetlists(true);
        if (netlists == null || netlists.isEmpty()) {
          throw new Exception("The generated netlist is empty, nothing to show.");
        }

        return summarizer.summarize(netlists, null);
      }

      @Override
      public void failed(Exception e) {
        swingUI.showMessage(e.getMessage(), summarizer.getName(), ISwingUI.INFORMATION_MESSAGE);
      }

      @Override
      public void complete(List<Summary> res) {
        if (res == null) {
          swingUI.showMessage("The generated summary is empty, nothing to show.",
              summarizer.getName(), ISwingUI.INFORMATION_MESSAGE);
          return;
        }
        StringBuilder sb = new StringBuilder("<html>");

        for (Summary summary : res) {
          sb.append("<p style=\"font-family: ").append(summarizer.getFontName())
              .append("; font-size: 9px\">");

          if (res.size() > 1)
            sb.append("<b>Switch configuration: ").append(summary.getNetlist().getSwitchSetup())
                .append("</b><br><br>");

          sb.append(summary.getSummary());

          sb.append("</p><br>");

          if (res.size() > 1)
            sb.append("<hr>");
        }
        sb.append("</html>");
        new TextDialog(swingUI.getOwnerFrame().getRootPane(), sb.toString(), summarizer.getName(),
            new Dimension(800, 600)).setVisible(true);
      }
    }, true);
  }
}