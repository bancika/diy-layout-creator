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
package org.diylc.swing.plugins.explorer;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;

import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.plugins.config.ConfigPlugin;

public class ExplorerPlugin implements IPlugIn {
  
  private static final Logger LOG = Logger.getLogger(ExplorerPlugin.class);

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;
  private ExplorerPane explorerPane;
  private JComponent canvasPanel;

  public ExplorerPlugin(ISwingUI swingUI, JComponent canvasPanel) {
    super();
    this.swingUI = swingUI;
    this.canvasPanel = canvasPanel;
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.FILE_STATUS_CHANGED, EventType.PROJECT_LOADED, EventType.PROJECT_MODIFIED, EventType.SELECTION_CHANGED);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void processMessage(EventType eventType, Object... params) {
    switch (eventType) {
      case PROJECT_MODIFIED:
        getExplorerPane().setComponents(((Project)params[1]).getComponents(), plugInPort.getSelectedComponents());
        break;
      case PROJECT_LOADED:
        getExplorerPane().setComponents(((Project)params[0]).getComponents(), plugInPort.getSelectedComponents());
        break;
      case SELECTION_CHANGED:
        getExplorerPane().setSelection((Set<IDIYComponent<?>>)params[0]);
      default:
        break;
    }
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    try {
      this.swingUI.injectGUIComponent(getExplorerPane(), SwingConstants.RIGHT, true, ConfigPlugin.PROJECT_EXPLORER);
    } catch (BadPositionException e) {
      LOG.error("Could not install project explorer", e);
    }
    
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

      @Override
      public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getKeyChar() == 'x' && e.getModifiersEx() == 0
            && ConfigurationManager.getInstance()
                .readBoolean(ConfigPlugin.PROJECT_EXPLORER, true)) {
          explorerPane.getSearchField().requestFocusInWindow();
          return true;
        }
        return false;
      }
    });
  }
  
  public ExplorerPane getExplorerPane() {
    if (explorerPane == null) {
      explorerPane = new ExplorerPane(swingUI, plugInPort);
    }
    return explorerPane;
  }
}
