package org.diylc.plugins.chatbot.model;

import java.util.List;

public record AiComponent(String id, String type, String value, List<AiTerminal> terminals) {
}
