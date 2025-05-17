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
 * 
 */
package org.diylc.swing.plugins.analyze;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.*;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.IDynamicSubmenuHandler;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.actions.analyze.HighlightConnectedAreasAction;
import org.diylc.swing.loadline.LoadlineEditorFrame;
import org.diylc.swing.plugins.file.ProjectDrawingProvider;
import org.diylc.swing.plugins.file.TraceMaskDrawingProvider;
import org.diylc.utils.IconLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * Entry point class for Analyze toors.
 * 
 * @author Branislav Stojkovic
 */
public class AnalyzeMenuPlugin implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(AnalyzeMenuPlugin.class);

  private static final String ANALYZE_TITLE = "Analyze";
  private static final String EXPORT_TITLE = "Export";

  private ISwingUI swingUI;
  private IPlugInPort plugInPort;

  private HighlightConnectedAreasAction highlightConnectedAreasAction;

  public AnalyzeMenuPlugin(ISwingUI swingUI) {
    super();
    this.swingUI = swingUI;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;

    ActionFactory actionFactory = ActionFactory.getInstance();
    List<Action> menuActions = new ArrayList<>();

    // Add all menu actions to the list
    menuActions.add(actionFactory.createAiAnalyzeAction(plugInPort, swingUI));
    menuActions.add(actionFactory.createCheckProximityAction(plugInPort, swingUI));
    menuActions.add(actionFactory.createCompareAction(plugInPort, swingUI));
    menuActions.add(actionFactory.createBomAction(plugInPort));
    menuActions.add(actionFactory.createGenerateNetlistAction(plugInPort, swingUI, false));
    menuActions.add(actionFactory.createGenerateNetlistAction(plugInPort, swingUI, true));
    highlightConnectedAreasAction =
        actionFactory.createHighlightConnectedAreasAction(plugInPort);
    menuActions.add(highlightConnectedAreasAction);

    List<INetlistAnalyzer> summarizers = plugInPort.getNetlistAnalyzers();
    if (summarizers != null) {
      for (INetlistAnalyzer summarizer : summarizers) {
        if (summarizer.getSwitchPreference().contains(NetlistSwitchPreference.WITH)) {
          menuActions.add(actionFactory.createSummarizeNetlistAction(plugInPort, swingUI, summarizer, true));
        }
        if (summarizer.getSwitchPreference().contains(NetlistSwitchPreference.WITHOUT)) {
          menuActions.add(actionFactory.createSummarizeNetlistAction(plugInPort, swingUI, summarizer, false));
        }
      }
    }

    // Sort actions by name
    menuActions.sort((a1, a2) -> {
      String name1 = (String) a1.getValue(Action.NAME);
      String name2 = (String) a2.getValue(Action.NAME);
      return name1.compareTo(name2);
    });

    // Inject sorted actions
    for (Action action : menuActions) {
      swingUI.injectMenuAction(action, ANALYZE_TITLE);
    }
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.STATUS_MESSAGE_CHANGED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    if (eventType == EventType.STATUS_MESSAGE_CHANGED) {
      highlightConnectedAreasAction.refreshState();;
    }
  }
}
