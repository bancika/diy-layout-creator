package org.diylc.common;

import org.diylc.appframework.simplemq.MessageDispatcher;
import org.diylc.plugins.chatbot.service.ChatbotService;
import org.diylc.plugins.cloud.service.CloudService;

public interface IServiceManager {

  CloudService getCloudService();

  ChatbotService getChatbotService();

  MessageDispatcher<EventType> getMessageDispatcher();
}
