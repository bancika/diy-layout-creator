package org.diylc.plugins.chatbot.model;

import java.util.List;

/**
 * @param connectedTo only on connectivity components: the terminals of other components this one
 *        physically touches, e.g. "Bridge.2"
 */
public record AiTerminal(int id, String label, AiPoint pos, List<String> connectedTo) {
}
