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
package org.diylc.swing.plugins.toolbox;

import java.util.EnumSet;

import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.plugins.config.ConfigPlugin;
import org.diylc.swing.plugins.statusbar.StatusBar;


public class ToolBox implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(StatusBar.class);

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  private ComponentTabbedPane componentTabbedPane;

  public ToolBox(ISwingUI swingUI) {
    this.swingUI = swingUI;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    try {
      swingUI.injectGUIComponent(getComponentTabbedPane(), SwingConstants.TOP);
    } catch (BadPositionException e) {
      LOG.error("Could not install the toolbox", e);
    }
    ConfigurationManager.getInstance().addConfigListener(ConfigPlugin.COMPONENT_BROWSER, new IConfigListener() {

      @Override
      public void valueChanged(String key, Object value) {
        getComponentTabbedPane().setVisible(
            ConfigPlugin.COMPONENT_BROWSER.equals(key) && ConfigPlugin.TABBED_TOOLBAR.equals(value));
      }
    });

    getComponentTabbedPane().setVisible(
        ConfigurationManager.getInstance().readString(ConfigPlugin.COMPONENT_BROWSER, ConfigPlugin.SEARCHABLE_TREE)
            .equals(ConfigPlugin.TABBED_TOOLBAR));
  }

  public ComponentTabbedPane getComponentTabbedPane() {
    if (componentTabbedPane == null) {
      componentTabbedPane = new ComponentTabbedPane(plugInPort);
    }
    return componentTabbedPane;
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    // TODO Auto-generated method stub

  }
}
