package org.diylc.plugins.chatbot.model;

import java.util.List;
import java.util.Set;

/**
 * @param connectedTerminalGroups terminals the switch ties together in this position, each group
 *        holding every terminal of one connection
 */
public record AiSwitchPosition(String name, List<Set<Integer>> connectedTerminalGroups) {
}
