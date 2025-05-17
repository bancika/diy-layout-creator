package org.diylc.plugins.compare.model;

public record ConnectionDiff(
    String fromComponent,
    String fromNodeName,
    String toComponent,
    String toNodeName,
    boolean presentInCurrent
) {
}
