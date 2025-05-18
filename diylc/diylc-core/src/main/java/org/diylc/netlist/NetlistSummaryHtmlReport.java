package org.diylc.netlist;

import org.diylc.netlist.Netlist;
import org.diylc.netlist.Summary;

import java.util.List;

public class NetlistSummaryHtmlReport {

    public static String generateHtml(List<Summary> summaries, String fontName) {
        if (summaries == null || summaries.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("<html><head><style>");
        sb.append("body { font-family: ").append(fontName)
            .append(", sans-serif; color: black; margin: 10px; }");
        sb.append("h2 { font-size: 12px; margin-top: 10px; margin-bottom: 0px; }");
        sb.append("p { margin: 0; }");
        sb.append("hr { margin-top: 10px; margin-bottom: 0; }");
        sb.append("</style></head><body>");

        for (Summary summary : summaries) {
            if (summaries.size() > 1) {
                sb.append("<h2>Switch configuration: ").append(summary.getNetlist().getSwitchSetup())
                    .append("</h2>");
            }

            sb.append(summary.getSummaryHtml());

            if (summaries.size() > 1) {
                sb.append("<br><hr>");
            }
        }
        sb.append("</body></html>");

        return sb.toString();
    }
}
