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
package org.diylc.swing.plugins.chatbot;

import org.apache.log4j.Logger;
import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.plugins.chatbot.model.SubscriptionEntity;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.plugins.config.ConfigPlugin;

import javax.swing.*;
import java.util.EnumSet;

public class ChatbotPlugin implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(ChatbotPlugin.class);

  private IPlugInPort plugInPort;
  private ISwingUI swingUI;
  private ChatbotPane chatbotPane;

  public ChatbotPlugin(ISwingUI swingUI) {
    super();
    this.swingUI = swingUI;
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.PROJECT_SAVED, EventType.PROJECT_LOADED,
        EventType.CLOUD_LOGGED_IN, EventType.CLOUD_LOGGED_OUT);
  }

  @SuppressWarnings("unchecked")
  @Override
  public void processMessage(EventType eventType, Object... params) {
    switch (eventType) {
      case PROJECT_SAVED:
      case PROJECT_LOADED:
      case CLOUD_LOGGED_IN:
      case CLOUD_LOGGED_OUT:
        SubscriptionEntity subscriptionInfo = null;
        if (params.length > 0 && params[0] instanceof SubscriptionEntity) {
          subscriptionInfo = (SubscriptionEntity) params[0];
        }
        getChatbotPane().refreshChat(subscriptionInfo);
        break;
      default:
        break;
    }
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    try {
      this.swingUI.injectGUIComponent(getChatbotPane(), SwingConstants.RIGHT, true,
          ConfigPlugin.CHATBOT);
    } catch (BadPositionException e) {
      LOG.error("Could not install project explorer", e);
    }
  }

  public ChatbotPane getChatbotPane() {
    if (chatbotPane == null) {
      chatbotPane = new ChatbotPane(swingUI, plugInPort);
    }
    return chatbotPane;
  }
}
