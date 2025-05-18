package org.diylc.swing.actions.analyze;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JLabel;

import org.diylc.swingframework.TextDialog;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.NetlistHtmlReport;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.utils.IconLoader;

public class GenerateNetlistAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;
  private boolean includeSwitches;

  public GenerateNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI,
      boolean includeSwitches) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    this.includeSwitches = includeSwitches;
    if (includeSwitches) {
      putValue(AbstractAction.NAME, "Generate DIYLC Netlist (incl. Switches)");
    } else {
      putValue(AbstractAction.NAME, "Generate DIYLC Netlist (excl. Switches)");
    }
    putValue(AbstractAction.SMALL_ICON, IconLoader.Web.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    swingUI.executeBackgroundTask(new ITask<List<Netlist>>() {

      @Override
      public List<Netlist> doInBackground() throws Exception {
        ActionFactory.LOG.info("Generating netlist, includeSwitches = " + includeSwitches + "...");
        return plugInPort.extractNetlists(includeSwitches);
      }

      @Override
      public void failed(Exception e) {
        swingUI.showMessage("Failed to generate the netlist: " + e.getMessage(), "DIYLC Netlist",
            ISwingUI.INFORMATION_MESSAGE);
      }

      @Override
      public void complete(List<Netlist> res) {
        ActionFactory.LOG.info("Finished generating netlist");
        if (res == null) {
          swingUI.showMessage("The generated netlist is empty, nothing to show.", "DIYLC Netlist",
              ISwingUI.INFORMATION_MESSAGE);
          return;
        }
        
        String html = NetlistHtmlReport.generateHtml(res, new JLabel().getFont().getName());
        new TextDialog(swingUI.getOwnerFrame().getRootPane(), html, "DIYLC Netlist",
            new Dimension(800, 600)).setVisible(true);
      }
    }, true);
  }
}
