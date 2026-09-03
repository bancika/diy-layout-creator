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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.plugins.canvas.CanvasPlugin;

/**
 * Adds an Excel-style tab strip below the canvas with two tabs, <b>Layout</b> and <b>Schematic</b>.
 * Selecting the Schematic tab regenerates the schematic from the current layout and swaps the canvas
 * area for a read-only {@link SchematicPanel}; selecting Layout swaps it back.
 *
 * <p>
 * Editing schematic symbols in place (drag / rotate / mirror with shared undo) via a restricted
 * {@code Presenter} is a planned follow-up; for now the schematic tab is a viewer with zoom, refresh
 * and export.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class SchematicTabPlugin implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(SchematicTabPlugin.class);

  private final ISwingUI swingUI;
  private final CanvasPlugin canvasPlugin;
  private IPlugInPort plugInPort;

  private final SchematicPanel schematicPanel = new SchematicPanel();
  private final List<JButton> schematicTools = new ArrayList<JButton>();
  private JScrollPane schematicScroll;
  private JComponent canvasScroll;

  public SchematicTabPlugin(ISwingUI swingUI, CanvasPlugin canvasPlugin) {
    this.swingUI = swingUI;
    this.canvasPlugin = canvasPlugin;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    this.canvasScroll = canvasPlugin.getCanvasScrollComponent();

    schematicScroll = new JScrollPane(schematicPanel);
    schematicScroll.getVerticalScrollBar().setUnitIncrement(16);
    schematicScroll.getHorizontalScrollBar().setUnitIncrement(16);
    schematicScroll.setVisible(false);

    try {
      swingUI.injectGUIComponent(schematicScroll, SwingConstants.CENTER, false, null);
      swingUI.injectGUIComponent(buildTabBar(), SwingConstants.CENTER, false, null);
    } catch (BadPositionException e) {
      LOG.error("Could not install schematic tab", e);
    }
  }

  private JComponent buildTabBar() {
    JToolBar bar = new JToolBar();
    bar.setFloatable(false);
    bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("controlShadow")));
    bar.setAlignmentX(Component.LEFT_ALIGNMENT);

    ButtonGroup group = new ButtonGroup();
    JToggleButton layoutTab = makeTab("Layout", true, e -> showLayout());
    JToggleButton schematicTab = makeTab("Schematic", false, e -> showSchematic());
    group.add(layoutTab);
    group.add(schematicTab);
    bar.add(layoutTab);
    bar.add(schematicTab);

    bar.add(Box.createHorizontalStrut(16));
    schematicTools.add(addTool(bar, "Refresh", e -> refreshSchematic(true)));
    schematicTools.add(addTool(bar, "Zoom −", e -> schematicPanel.setZoom(schematicPanel.getZoom() / 1.25d)));
    schematicTools.add(addTool(bar, "Zoom +", e -> schematicPanel.setZoom(schematicPanel.getZoom() * 1.25d)));
    schematicTools.add(addTool(bar, "Reset Zoom", e -> schematicPanel.setZoom(1d)));
    setToolsEnabled(false);

    Dimension pref = bar.getPreferredSize();
    bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));
    bar.setMinimumSize(new Dimension(0, pref.height));
    return bar;
  }

  private static JToggleButton makeTab(String label, boolean selected, ActionListener listener) {
    JToggleButton button = new JToggleButton(label, selected);
    button.setFocusPainted(false);
    button.setFont(button.getFont().deriveFont(Font.PLAIN));
    button.addActionListener(listener);
    return button;
  }

  private static JButton addTool(JToolBar bar, String label, ActionListener listener) {
    JButton button = new JButton(new AbstractAction(label) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        listener.actionPerformed(e);
      }
    });
    button.setFocusPainted(false);
    bar.add(button);
    return button;
  }

  private void setToolsEnabled(boolean enabled) {
    for (JButton button : schematicTools) {
      button.setEnabled(enabled);
    }
  }

  private void showLayout() {
    schematicScroll.setVisible(false);
    canvasScroll.setVisible(true);
    setToolsEnabled(false);
    relayout();
  }

  private void showSchematic() {
    refreshSchematic(false);
    canvasScroll.setVisible(false);
    schematicScroll.setVisible(true);
    setToolsEnabled(true);
    relayout();
  }

  private void refreshSchematic(boolean reportErrors) {
    try {
      schematicPanel.refresh(plugInPort.getCurrentProject(), plugInPort.getContinuityAreas());
    } catch (Exception e) {
      LOG.error("Could not build schematic view", e);
      if (reportErrors) {
        swingUI.showMessage("Could not build the schematic view: " + e.getMessage(), "Schematic View",
            ISwingUI.ERROR_MESSAGE);
      }
    }
  }

  private void relayout() {
    JComponent parent = (JComponent) canvasScroll.getParent();
    if (parent != null) {
      parent.revalidate();
      parent.repaint();
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
