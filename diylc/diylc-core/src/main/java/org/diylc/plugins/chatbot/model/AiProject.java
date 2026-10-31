package org.diylc.plugins.chatbot.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @param selection ids of the components the user has selected, which a request such as "change
 *        this resistor" refers to
 * @param components in z-order, the last one is drawn on top
 */
public record AiProject(Map<String, String> metadata,
                        List<String> tags,
                        List<String> selection,
                        List<AiComponent> components,
                        Map<String, Set<String>> nets,
                        List<AiSwitch> switches) {
}
