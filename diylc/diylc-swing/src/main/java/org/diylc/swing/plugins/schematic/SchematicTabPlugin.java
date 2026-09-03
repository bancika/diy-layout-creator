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
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.IConfigurationManager;
import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.SchematicView;
import org.diylc.presenter.ContinuityArea;
import org.diylc.presenter.Presenter;
import org.diylc.schematic.SchematicSynchronizer;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.plugins.canvas.CanvasPlugin;

/**
 * Adds an Excel-style tab strip below the canvas with two tabs, <b>Layout</b> and <b>Schematic</b>.
 *
 * <p>
 * The schematic tab reuses the regular canvas UI: its own {@link Presenter} + {@link CanvasPlugin}
 * (the same {@code RulerScrollPane}, rulers, scroll bars and zoom-scroll) render a wrapper
 * {@link Project} backed by the layout's {@link SchematicView}. Switching to the Schematic tab
 * regenerates the schematic automatically ({@link SchematicSynchronizer}) and shows the schematic
 * canvas; switching to Layout shows the layout canvas again.
 * </p>
 *
 * <p>
 * The schematic canvas is not restricted yet, so nothing prevents selecting or nudging symbols; a
 * dedicated restricted mode (no add / delete / paste, shared undo) is a planned follow-up.
 * </p>
 *
 * @author Branislav Stojkovic
 */
public class SchematicTabPlugin implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(SchematicTabPlugin.class);

  private final ISwingUI swingUI;
  private final IConfigurationManager<?> configManager;
  private final CanvasPlugin layoutCanvasPlugin;

  private IPlugInPort plugInPort;
  private Presenter schematicPresenter;
  private CanvasPlugin schematicCanvasPlugin;
  private JComponent layoutScroll;
  private JComponent schematicScroll;
  private JToggleButton schematicTab;

  public SchematicTabPlugin(ISwingUI swingUI, IConfigurationManager<?> configManager,
      CanvasPlugin layoutCanvasPlugin) {
    this.swingUI = swingUI;
    this.configManager = configManager;
    this.layoutCanvasPlugin = layoutCanvasPlugin;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    this.layoutScroll = layoutCanvasPlugin.getCanvasScrollComponent();

    // A second presenter + canvas plugin gives the schematic the exact same rendering, scrolling
    // and zoom behaviour as the layout canvas.
    this.schematicPresenter = new Presenter(swingUI, configManager, false);
    this.schematicCanvasPlugin = new CanvasPlugin(swingUI, configManager);
    this.schematicPresenter.installPlugin(() -> schematicCanvasPlugin);
    this.schematicScroll = schematicCanvasPlugin.getCanvasScrollComponent();
    this.schematicScroll.setVisible(false);

    try {
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
    schematicTab = makeTab("Schematic", false, e -> showSchematic());
    group.add(layoutTab);
    group.add(schematicTab);
    bar.add(layoutTab);
    bar.add(schematicTab);

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

  private void showLayout() {
    schematicScroll.setVisible(false);
    layoutScroll.setVisible(true);
    relayout();
  }

  private void showSchematic() {
    Project wrapper;
    try {
      wrapper = buildSchematicProject();
    } catch (Exception e) {
      LOG.error("Could not build schematic view", e);
      swingUI.showMessage("Could not build the schematic view: " + e.getMessage(), "Schematic View",
          ISwingUI.ERROR_MESSAGE);
      return;
    }

    schematicPresenter.loadProject(wrapper, true, null);
    layoutScroll.setVisible(false);
    schematicScroll.setVisible(true);
    relayout();
    SwingUtilities.invokeLater(() -> {
      schematicCanvasPlugin.scrollToCenterAndShowContents();
      relayout();
    });
  }

  /** Regenerates the schematic from the current layout and wraps it in a renderable project. */
  private Project buildSchematicProject() {
    Project layoutProject = plugInPort.getCurrentProject();
    java.util.List<ContinuityArea> areas = plugInPort.getContinuityAreas();
    new SchematicSynchronizer().synchronize(layoutProject,
        areas == null ? new java.util.ArrayList<>() : areas);

    SchematicView view = layoutProject.getOrCreateSchematicView();
    Project wrapper = new Project();
    wrapper.setTitle(layoutProject.getTitle() + " — Schematic");
    wrapper.setAuthor(layoutProject.getAuthor());
    wrapper.setWidth(view.getWidth());
    wrapper.setHeight(view.getHeight());
    wrapper.setGridSpacing(view.getGridSpacing());
    wrapper.setFont(layoutProject.getFont());
    wrapper.getComponents().addAll(view.getComponents());
    // Wires are auto-routed and must never be touched by the user: lock the whole WIRING layer so
    // they cannot be selected, dragged or detached from symbols. Symbols live on the COMPONENT
    // layer and stay fully movable.
    wrapper.getLockedLayers().add(IDIYComponent.WIRING);
    return wrapper;
  }

  private void relayout() {
    JComponent parent = layoutScroll.getParent() instanceof JComponent
        ? (JComponent) layoutScroll.getParent()
        : null;
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
