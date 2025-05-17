package org.diylc.plugins.compare.model;

public record ComponentDiff(
    String componentName,
    boolean presentInCurrent
) {
}
