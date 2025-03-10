package org.diylc.swing.actions;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JLabel;

import org.diylc.swingframework.TextDialog;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.netlist.Group;
import org.diylc.netlist.Netlist;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.images.IconLoader;

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
        StringBuilder sb = new StringBuilder("<html>");

        for (Netlist netlist : res) {
          sb.append("<p style=\"font-family: " + new JLabel().getFont().getName()
              + "; font-size: 9px\">");
          if (includeSwitches) {
            sb.append("<b>Switch configuration: ").append(netlist.getSwitchSetup())
                .append("</b><br><br>");
          }
          sb.append("Connected node groups:<br><br>");
          for (Group v : netlist.getSortedGroups()) {
            sb.append("&nbsp;&nbsp;").append(v.getSortedNodes()).append("<br>");
          }
          sb.append("</p><br><hr>");
        }
        sb.append("</html>");
        new TextDialog(swingUI.getOwnerFrame().getRootPane(), sb.toString(), "DIYLC Netlist",
            new Dimension(800, 600)).setVisible(true);
      }
    }, true);
  }
}