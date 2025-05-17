package org.diylc.plugins.compare.util;

import org.diylc.plugins.compare.model.CompareResults;
import org.diylc.plugins.compare.model.ConnectionDiff;
import org.diylc.plugins.compare.model.ComponentDiff;

public class CompareResultsHtmlReport {

    public static String generateReport(CompareResults results) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; color: black; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 10px; }\n");
        html.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        html.append("th { background-color: #f2f2f2; }\n");
        html.append(".result { font-size: 18px; }\n");
        html.append("h2 { font-size: 14px; margin-top: 20px; }\n");
        html.append("</style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Start with bold results
        if (results.matches()) {
            html.append("<p class=\"result\"><strong>The circuits match exactly.</strong></p>\n");
        } else {
            html.append("<p class=\"result\"><strong>The circuits do not match.</strong></p>\n");
        }

        if (!results.matches()) {
            if (!results.componentDiffs().isEmpty()) {
                html.append("<h2>Component Differences</h2>\n");
                html.append("<table>\n");
                html.append("<tr><th>Component</th><th>Status</th></tr>\n");
                
                for (ComponentDiff diff : results.componentDiffs()) {
                    html.append("<tr>\n");
                    html.append("<td>").append(diff.componentName()).append("</td>\n");
                    html.append("<td>");
                    html.append(diff.presentInCurrent() ? "Missing in target circuit" : "Extra in target circuit");
                    html.append("</td>\n");
                    html.append("</tr>\n");
                }
                
                html.append("</table>\n");
            }

            if (!results.connectionDiffs().isEmpty()) {
                html.append("<h2>Connection Differences</h2>\n");
                html.append("<table>\n");
                html.append("<tr><th>From Component</th><th>From Node</th><th>To Component</th><th>To Node</th><th>Status</th></tr>\n");
                
                for (ConnectionDiff diff : results.connectionDiffs()) {
                    html.append("<tr>\n");
                    html.append("<td>").append(diff.fromComponent()).append("</td>\n");
                    html.append("<td>").append(diff.fromNodeName()).append("</td>\n");
                    html.append("<td>").append(diff.toComponent()).append("</td>\n");
                    html.append("<td>").append(diff.toNodeName()).append("</td>\n");
                    html.append("<td>");
                    html.append(diff.presentInCurrent() ? "Missing in target circuit" : "Extra in target circuit");
                    html.append("</td>\n");
                    html.append("</tr>\n");
                }
                
                html.append("</table>\n");
            }
        }

        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }
} 