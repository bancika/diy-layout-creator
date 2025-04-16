package org.diylc.plugins.chatbot.model;

import java.util.List;
import java.util.Set;

public record AiSwitchPosition(String name, List<Set<Integer>> connectedTerminalGroups) {
}
