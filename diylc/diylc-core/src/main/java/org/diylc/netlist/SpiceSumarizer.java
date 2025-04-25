package org.diylc.netlist;

import org.diylc.common.ComponentType;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.ComponentProcessor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.diylc.netlist.AbstractNetlistAnalyzer.find;

public class SpiceSumarizer {

  public static String summarize(Netlist netlist, boolean useHTML) throws TreeException {

    // grab all components that are in the netlist
    List<Group> groups = netlist.getSortedGroups();

    List<IDIYComponent<?>> allComponents = groups.stream()
        .flatMap(x -> x.getNodes().stream()
            .map(y -> y.getComponent()))
        .distinct()
        .collect(Collectors.toList());

    int unconnectedIndex = groups.size();

    int maxLen = 0;
    for (IDIYComponent<?> c : allComponents) {
      if (c.getName().length() > maxLen)
        maxLen = c.getName().length();
    }

    List<String> lines = new ArrayList<String>();

    for (IDIYComponent<?> c : allComponents) {
      StringBuilder sb = new StringBuilder();
      // change the prefix to match spice convention if needed
      String name = c.getName();
      String prefix = null;
      if (c instanceof ISpiceMapper)
        prefix = ((ISpiceMapper)c).getPrefix();
      if (prefix != null && !name.toLowerCase().startsWith(prefix.toLowerCase()))
        name = prefix + name;

      sb.append(fill(name, (int) (Math.ceil(maxLen / 5.0) * 5), useHTML));
      sb.append(" ");
      List<Integer> nodeIndices = new ArrayList<Integer>();

      // find node indices for each control point
      for (int i = 0; i < c.getControlPointCount(); i++) {
        // skip non-sticky points
        if (c.getControlPointNodeName(i) == null)
          continue;

        int pointIndex = i;

        // remap if needed
        if (c instanceof ISpiceMapper)
          pointIndex = ((ISpiceMapper)c).mapToSpiceNode(pointIndex);

        int nodeIndex = find(new Node(c, pointIndex), groups);
        if (nodeIndex < 0)
          nodeIndex = unconnectedIndex++;

        // 1-based convention
        nodeIndex++;

        nodeIndices.add(nodeIndex);
      }

      // output to spice
      for (Integer nodeIndex : nodeIndices) {
        sb.append(fill(formatSpiceNode(nodeIndex), 5, useHTML));
        sb.append(" ");
      }

      outputComponentValue(c, sb);

      outputComment(c, sb);
      if (useHTML)
        sb.append("<br>");
      lines.add(sb.toString());
    }

    return lines
        .stream()
        .sorted()
        .collect(Collectors.joining("\n"));
  }

  private static void outputComment(IDIYComponent<?> c, StringBuilder sb) {
    ComponentType componentType = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());

    String comment = componentType.getName() + " [" + componentType.getCategory() + "]";
    if (c instanceof ISpiceMapper) {
      String spiceComment = ((ISpiceMapper) c).getComment();
      if (spiceComment != null)
        comment = spiceComment + " " + comment;
    }
    sb.append(" ; ").append(comment);
  }

  private static void outputComponentValue(IDIYComponent<?> c, StringBuilder sb) {
    ComponentType componentType = ComponentProcessor.getInstance()
        .extractComponentTypeFrom((Class<? extends IDIYComponent<?>>) c.getClass());

    if (c.getValue() == null || c.getValue().toString().trim().isEmpty()) {
      //      if (
      //          Void.class.equals(((ParameterizedType) c.getClass()
      //              .getGenericSuperclass()).getActualTypeArguments()[0])) {
      //        return typeName;
      //      }
      String typeName = componentType.getName();
      if (typeName.contains(" ")) {
        sb.append("\"").append(typeName).append("\"");
      } else {
        sb.append(typeName);
      }
    } else {
      if (c.getValue().toString().contains(" ")) {
        sb.append("\"").append(c.getValue()).append("\"");
      } else {
        sb.append(c.getValue());
      }
    }
  }

  private static String formatSpiceNode(int i) {
    return String.format("N%03d" , i);
  }

  private static String fill(String source, int desiredLength, boolean useHTML) {
    String res = source;
    for (int i = 0; i < desiredLength - source.length(); i++)
      res += useHTML ? "&nbsp;" : " ";
    return res;
  }
}
