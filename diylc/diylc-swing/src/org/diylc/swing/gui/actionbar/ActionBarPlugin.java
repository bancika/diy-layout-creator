/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.swing.gui.actionbar;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.diylc.common.EventType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;

/**
 * Mini toolbar with common actions
 * 
 * @author Branislav Stojkovic
 */
public class ActionBarPlugin implements IPlugIn {

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;

  private JPanel actionPanel;
  private ActionToolbar contextActionToolbar;
  private ConfigToolbar configToolbar;

  public ActionBarPlugin(ISwingUI swingUI) {
    this.swingUI = swingUI;
  }

  public JPanel getActionPanel() {
    if (actionPanel == null) {
      actionPanel = new JPanel();
      actionPanel.setOpaque(false);
      actionPanel.setLayout(new GridBagLayout());
      
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.weightx = 1;
      
      actionPanel.add(new JPanel(), gbc);
      
      gbc.gridx = 1;
      gbc.weightx = 0;      
      actionPanel.add(getConfigToolbar(), gbc);
      
      gbc.gridx = 2;
      // add some space to separate the two toolbars
      actionPanel.add(new JLabel() {
        
        private static final long serialVersionUID = 1L;

        @Override
        public Dimension getPreferredSize() {
          return new Dimension(16, 16);
        }
      }, gbc);
      
      gbc.gridx = 3;
      actionPanel.add(getContextActionToolbar(), gbc);
      
      actionPanel.setBorder(BorderFactory.createEmptyBorder());
    }
    return actionPanel;
  }
  
  public ConfigToolbar getConfigToolbar() {
    if (configToolbar == null) {
      configToolbar = new ConfigToolbar();
      configToolbar.add("Continuous Creation", IPlugInPort.CONTINUOUS_CREATION_KEY, IconLoader.Elements.getIcon(), false);
      configToolbar.add("Highlight Connected Areas", IPlugInPort.HIGHLIGHT_CONTINUITY_AREA, IconLoader.LaserPointer.getIcon(), false);
      configToolbar.add("Sticky Points", IPlugInPort.STICKY_POINTS_KEY, IconLoader.GraphNodes.getIcon(), true);
    }
    return configToolbar;
  }
  
  public ActionToolbar getContextActionToolbar() {
    if (contextActionToolbar == null) {
      contextActionToolbar = new ActionToolbar();
      contextActionToolbar.add(ActionFactory.getInstance().createRotateSelectionAction(plugInPort, 1));
      contextActionToolbar.add(ActionFactory.getInstance().createRotateSelectionAction(plugInPort, -1));
      contextActionToolbar.addSpacer();
      contextActionToolbar.add(ActionFactory.getInstance().createMirrorSelectionAction(plugInPort, IComponentTransformer.HORIZONTAL));
      contextActionToolbar.add(ActionFactory.getInstance().createMirrorSelectionAction(plugInPort, IComponentTransformer.VERTICAL));
      contextActionToolbar.addSpacer();
      contextActionToolbar.add(ActionFactory.getInstance().createNudgeAction(plugInPort));
      contextActionToolbar.addSpacer();
      contextActionToolbar.add(ActionFactory.getInstance().createSendToBackAction(plugInPort));
      contextActionToolbar.add(ActionFactory.getInstance().createBringToFrontAction(plugInPort));
      contextActionToolbar.addSpacer();
      contextActionToolbar.add(ActionFactory.getInstance().createGroupAction(plugInPort));
      contextActionToolbar.add(ActionFactory.getInstance().createUngroupAction(plugInPort));
    }
    return contextActionToolbar;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    swingUI.injectMenuComponent(getActionPanel());
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.SELECTION_CHANGED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    if (eventType != EventType.SELECTION_CHANGED)
      return;
    boolean enabled = !plugInPort.getSelectedComponents().isEmpty();
    getContextActionToolbar().setEnabled(enabled);
  }
}
