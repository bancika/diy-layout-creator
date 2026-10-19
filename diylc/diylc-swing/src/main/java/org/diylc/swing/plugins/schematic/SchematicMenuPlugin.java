/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.swing.plugins.schematic;

import java.awt.event.ActionEvent;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ISwingUI;

/**
 * Adds the "Schematic View..." entry to the Analyze menu. It generates (or incrementally
 * synchronizes) the schematic representation of the current layout and shows it in a
 * {@link SchematicViewFrame}.
 *
 * @author Branislav Stojkovic
 */
public class SchematicMenuPlugin implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(SchematicMenuPlugin.class);

  private static final String ANALYZE_TITLE = "Analyze";

  private final ISwingUI swingUI;
  private IPlugInPort plugInPort;

  public SchematicMenuPlugin(ISwingUI swingUI) {
    this.swingUI = swingUI;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    swingUI.injectMenuAction(new AbstractAction("Schematic View...") {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        openSchematicView();
      }
    }, ANALYZE_TITLE);
  }

  private void openSchematicView() {
    try {
      SchematicViewFrame frame = SchematicViewFrame.getInstance();
      frame.showFor(plugInPort.getCurrentProject(), plugInPort.getContinuityAreas());
    } catch (Exception ex) {
      LOG.error("Could not open schematic view", ex);
      SwingUtilities.invokeLater(() -> swingUI.showMessage(
          "Could not generate the schematic view: " + ex.getMessage(), "Schematic View",
          ISwingUI.ERROR_MESSAGE));
    }
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.noneOf(EventType.class);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    // no-op
  }
}
