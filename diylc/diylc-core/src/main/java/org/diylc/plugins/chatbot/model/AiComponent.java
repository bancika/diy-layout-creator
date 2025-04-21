package org.diylc.plugins.chatbot.model;

import java.util.List;

public record AiComponent(String id, String type, String value, AiPoint fromPos, AiPoint toPos, AiPoint pos, List<AiTerminal> terminals) {
}
