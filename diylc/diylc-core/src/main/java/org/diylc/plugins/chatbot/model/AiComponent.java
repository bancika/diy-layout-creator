package org.diylc.plugins.chatbot.model;

import java.util.List;
import java.util.Map;

/**
 * @param properties current values of the editable properties
 */
public record AiComponent(String id, String type, String value, Map<String, String> properties,
    List<AiTerminal> terminals) {
}
