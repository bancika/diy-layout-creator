package org.diylc.plugins.chatbot.model;

import java.util.List;
import java.util.Map;

public record AiComponent(String id, String type, Map<String, Object> properties, List<AiTerminal> terminals) {
}
