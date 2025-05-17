package org.diylc.plugins.compare.model;

import java.util.List;

public record CompareResults(boolean matches, List<ConnectionDiff> connectionDiffs, List<ComponentDiff> componentDiffs) {
}
