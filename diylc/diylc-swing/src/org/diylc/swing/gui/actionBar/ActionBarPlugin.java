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
package org.diylc.swing.gui.actionBar;

import java.awt.BorderLayout;
import java.util.EnumSet;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.diylc.common.EventType;
import org.diylc.common.IComponentTransformer;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
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
  private MiniToolbar miniToolbar;

  public ActionBarPlugin(ISwingUI swingUI) {
    this.swingUI = swingUI;
  }

  public JPanel getActionPanel() {
    if (actionPanel == null) {
      actionPanel = new JPanel();
      actionPanel.setOpaque(false);
      actionPanel.setLayout(new BorderLayout());
      actionPanel.add(getMiniToolbar(), BorderLayout.EAST);
      actionPanel.setBorder(BorderFactory.createEmptyBorder());
    }
    return actionPanel;
  }
  
  public MiniToolbar getMiniToolbar() {
    if (miniToolbar == null) {
      miniToolbar = new MiniToolbar();
      miniToolbar.add(ActionFactory.getInstance().createRotateSelectionAction(plugInPort, 1));
      miniToolbar.add(ActionFactory.getInstance().createRotateSelectionAction(plugInPort, -1));
      miniToolbar.addSpacer();
      miniToolbar.add(ActionFactory.getInstance().createMirrorSelectionAction(plugInPort, IComponentTransformer.HORIZONTAL));
      miniToolbar.add(ActionFactory.getInstance().createMirrorSelectionAction(plugInPort, IComponentTransformer.VERTICAL));
      miniToolbar.addSpacer();
      miniToolbar.add(ActionFactory.getInstance().createNudgeAction(plugInPort));
      miniToolbar.addSpacer();
      miniToolbar.add(ActionFactory.getInstance().createSendToBackAction(plugInPort));
      miniToolbar.add(ActionFactory.getInstance().createBringToFrontAction(plugInPort));
      miniToolbar.addSpacer();
      miniToolbar.add(ActionFactory.getInstance().createGroupAction(plugInPort));
      miniToolbar.add(ActionFactory.getInstance().createUngroupAction(plugInPort));
    }
    return miniToolbar;
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
    getMiniToolbar().setEnabled(enabled);
  }
}
