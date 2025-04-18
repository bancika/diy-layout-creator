package org.diylc.plugins.chatbot.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record AiProject(Map<String, String> metadata,
                        List<String> tags,
                        List<AiComponent> components,
                        Map<String, Set<String>> nets,
                        List<AiSwitch> switches) {
}
