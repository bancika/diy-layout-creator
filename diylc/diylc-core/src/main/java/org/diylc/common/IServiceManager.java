package org.diylc.common;

import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.plugins.chatbot.service.ChatbotService;
import org.diylc.plugins.cloud.service.CloudService;
import org.diylc.presenter.DrawingManager;

public interface IServiceManager {

  CloudService getCloudService();

  ChatbotService getChatbotService();

  MessageDispatcher<EventType> getMessageDispatcher();

  DrawingManager getDrawingManager();
}
