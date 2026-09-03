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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.diylc.core.Project;
import org.diylc.presenter.ContinuityArea;

/**
 * Stand-alone window that hosts a {@link SchematicPanel}. Opened from the Analyze menu; there is at
 * most one instance, reused on subsequent invocations.
 *
 * @author Branislav Stojkovic
 */
public class SchematicViewFrame extends JFrame {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(SchematicViewFrame.class);

  private static SchematicViewFrame instance;

  private final SchematicPanel panel;
  private Project layoutProject;

  private SchematicViewFrame() {
    super("Schematic View");
    setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
    this.panel = new SchematicPanel();

    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(16);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(buildToolBar(), BorderLayout.NORTH);
    getContentPane().add(scrollPane, BorderLayout.CENTER);
    setPreferredSize(new Dimension(1000, 720));
    pack();
    setLocationRelativeTo(null);
  }

  public static SchematicViewFrame getInstance() {
    if (instance == null) {
      instance = new SchematicViewFrame();
    }
    return instance;
  }

  private JToolBar buildToolBar() {
    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(action("Refresh", e -> doRefresh()));
    toolBar.addSeparator();
    toolBar.add(action("Zoom -", e -> panel.setZoom(panel.getZoom() / 1.25d)));
    toolBar.add(action("Zoom +", e -> panel.setZoom(panel.getZoom() * 1.25d)));
    toolBar.add(action("Reset Zoom", e -> panel.setZoom(1d)));
    toolBar.addSeparator();
    toolBar.add(action("Export PNG...", e -> doExport()));
    return toolBar;
  }

  private static JButton action(String label, java.util.function.Consumer<ActionEvent> handler) {
    return new JButton(new AbstractAction(label) {
      private static final long serialVersionUID = 1L;

      @Override
      public void actionPerformed(ActionEvent e) {
        handler.accept(e);
      }
    });
  }

  /**
   * Points the window at a layout project and (re)generates the schematic.
   */
  public void showFor(Project layoutProject, List<ContinuityArea> continuityAreas) {
    this.layoutProject = layoutProject;
    this.continuityAreas = continuityAreas;
    panel.refresh(layoutProject, continuityAreas);
    setVisible(true);
    toFront();
  }

  private List<ContinuityArea> continuityAreas;

  private void doRefresh() {
    if (layoutProject != null) {
      panel.refresh(layoutProject, continuityAreas);
    }
  }

  private void doExport() {
    if (!panel.hasContent()) {
      JOptionPane.showMessageDialog(this, "Nothing to export.", "Schematic View",
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(new File("schematic.png"));
    if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
      return;
    }
    File file = chooser.getSelectedFile();
    if (!file.getName().toLowerCase().endsWith(".png")) {
      file = new File(file.getParentFile(), file.getName() + ".png");
    }
    try {
      ImageIO.write(panel.renderToImage(), "png", file);
    } catch (Exception ex) {
      LOG.error("Could not export schematic image", ex);
      JOptionPane.showMessageDialog(this, "Could not export image: " + ex.getMessage(),
          "Schematic View", JOptionPane.ERROR_MESSAGE);
    }
  }
}
