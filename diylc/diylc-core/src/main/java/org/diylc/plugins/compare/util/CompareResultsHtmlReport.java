/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.plugins.compare.util;

import javax.swing.JLabel;
import org.diylc.plugins.compare.model.CompareResults;
import org.diylc.plugins.compare.model.ConnectionDiff;
import org.diylc.plugins.compare.model.ComponentDiff;

import java.util.List;
import java.util.stream.Collectors;

public class CompareResultsHtmlReport {

    public static String generateReport(CompareResults results) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html>\n");
        html.append("<head>\n");
        html.append("    <title>Circuit Comparison Results</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: ").append(new JLabel().getFont().getName()).append(", sans-serif; margin: 10px; color: black; font-size: 12px; }\n");
        html.append("        .result { font-size: 14px; margin-bottom: 8px; }\n");
        html.append("        .note { font-size: 11px; color: #666; margin-top: 4px; }\n");
        html.append("        .section { margin: 6px 0; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin: 4px 0; }\n");
        html.append("        th, td { padding: 4px; text-align: left; border: 1px solid #ddd; font-size: 11px; }\n");
        html.append("        th { background-color: #f5f5f5; }\n");
        html.append("        tr:nth-child(even) { background-color: #f9f9f9; }\n");
        html.append("        h2 { font-size: 12px; margin-top: 6px; margin-bottom: 4px; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");

        // Main result
        html.append("    <p class=\"result\"><strong>The circuits ").append(results.matches() ? "match" : "do not match").append(".</strong></p>\n");
        if (!results.matches()) {
            html.append("    <p class=\"note\">Note: The circuits might still be electronically equivalent. Differences could be due to different component labeling or placement. Please review the differences below.</p>\n");
        }

        if (!results.matches()) {
            // Component differences section
            if (!results.componentDiffs().isEmpty()) {
                html.append("    <div class=\"section\">\n");
                html.append("        <h2>Component Differences</h2>\n");
                html.append("        <table>\n");
                html.append("            <tr>\n");
                html.append("                <th>Component</th>\n");
                html.append("                <th>Status</th>\n");
                html.append("            </tr>\n");

                List<ComponentDiff> missingComponents = results.componentDiffs().stream()
                    .filter(ComponentDiff::presentInCurrent)
                    .collect(Collectors.toList());
                List<ComponentDiff> extraComponents = results.componentDiffs().stream()
                    .filter(diff -> !diff.presentInCurrent())
                    .collect(Collectors.toList());

                for (ComponentDiff diff : missingComponents) {
                    html.append("            <tr>\n");
                    html.append("                <td>").append(diff.componentName()).append("</td>\n");
                    html.append("                <td>Missing in target circuit</td>\n");
                    html.append("            </tr>\n");
                }

                for (ComponentDiff diff : extraComponents) {
                    html.append("            <tr>\n");
                    html.append("                <td>").append(diff.componentName()).append("</td>\n");
                    html.append("                <td>Extra in target circuit</td>\n");
                    html.append("            </tr>\n");
                }

                html.append("        </table>\n");
                html.append("    </div>\n");
            }

            // Connection differences section
            if (!results.connectionDiffs().isEmpty()) {
                html.append("    <div class=\"section\">\n");
                html.append("        <h2>Connection Differences</h2>\n");
                html.append("        <table>\n");
                html.append("            <tr>\n");
                html.append("                <th>From Component</th>\n");
                html.append("                <th>From Node</th>\n");
                html.append("                <th>To Component</th>\n");
                html.append("                <th>To Node</th>\n");
                html.append("                <th>Status</th>\n");
                html.append("            </tr>\n");

                List<ConnectionDiff> missingConnections = results.connectionDiffs().stream()
                    .filter(ConnectionDiff::presentInCurrent)
                    .collect(Collectors.toList());
                List<ConnectionDiff> extraConnections = results.connectionDiffs().stream()
                    .filter(diff -> !diff.presentInCurrent())
                    .collect(Collectors.toList());

                for (ConnectionDiff diff : missingConnections) {
                    html.append("            <tr>\n");
                    html.append("                <td>").append(diff.fromComponent()).append("</td>\n");
                    html.append("                <td>").append(diff.fromNodeName()).append("</td>\n");
                    html.append("                <td>").append(diff.toComponent()).append("</td>\n");
                    html.append("                <td>").append(diff.toNodeName()).append("</td>\n");
                    html.append("                <td>Missing in target circuit</td>\n");
                    html.append("            </tr>\n");
                }

                for (ConnectionDiff diff : extraConnections) {
                    html.append("            <tr>\n");
                    html.append("                <td>").append(diff.fromComponent()).append("</td>\n");
                    html.append("                <td>").append(diff.fromNodeName()).append("</td>\n");
                    html.append("                <td>").append(diff.toComponent()).append("</td>\n");
                    html.append("                <td>").append(diff.toNodeName()).append("</td>\n");
                    html.append("                <td>Extra in target circuit</td>\n");
                    html.append("            </tr>\n");
                }

                html.append("        </table>\n");
                html.append("    </div>\n");
            }
        }

        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }
} 
