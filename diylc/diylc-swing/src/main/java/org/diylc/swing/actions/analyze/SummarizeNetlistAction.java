/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.actions.analyze;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;

import org.diylc.swingframework.TextDialog;

import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.netlist.Netlist;
import org.diylc.netlist.Summary;
import org.diylc.netlist.NetlistSummaryHtmlReport;
import org.diylc.swing.ISwingUI;
import org.diylc.utils.IconLoader;

public class SummarizeNetlistAction extends AbstractAction {

  private static final long serialVersionUID = 1L;

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;
  private INetlistAnalyzer summarizer;
  private final boolean includeSwitches;

  public SummarizeNetlistAction(IPlugInPort plugInPort, ISwingUI swingUI,
      INetlistAnalyzer summarizer, boolean includeSwitches) {
    super();
    this.plugInPort = plugInPort;
    this.swingUI = swingUI;
    this.summarizer = summarizer;
    this.includeSwitches = includeSwitches;
    String name = summarizer.getName();
    if (summarizer.getSwitchPreference().size() > 1) {
      name += includeSwitches ?
          " (incl. Switches)" : " (excl. Switches)";
    }
    putValue(AbstractAction.NAME, name);
    putValue(AbstractAction.SMALL_ICON,
        Enum.valueOf(IconLoader.class, summarizer.getIconName()).getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    swingUI.executeBackgroundTask(new ITask<List<Summary>>() {

      @Override
      public List<Summary> doInBackground() throws Exception {
        List<Netlist> netlists = plugInPort.extractNetlists(includeSwitches);
        if (netlists == null || netlists.isEmpty()) {
          throw new Exception("The generated netlist is empty, nothing to show.");
        }

        return summarizer.summarize(netlists);
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
        
        String html = NetlistSummaryHtmlReport.generateHtml(res, summarizer.getFontName());
        new TextDialog(swingUI.getOwnerFrame().getRootPane(), html, summarizer.getName(),
            new Dimension(800, 600)).setVisible(true);
      }
    }, true);
  }
}
