package org.diylc.plugins.chatbot.model;

import java.util.List;

/**
 * @param connectedTo only on connectivity components: the terminals of other components this one
 *        physically touches, e.g. "Bridge.2". Other wires are left out when the end has a net,
 *        which already says where they lead.
 * @param net only on wire ends: the net the end is on, when it reaches one
 */
public record AiTerminal(int id, String label, AiPoint pos, List<String> connectedTo, String net) {
}
