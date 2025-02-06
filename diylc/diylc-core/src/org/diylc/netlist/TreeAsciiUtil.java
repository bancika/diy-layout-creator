package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TreeAsciiUtil {

  public static List<String> concatenateMultiLineParallel(List<List<String>> elementLines) {

    List<Integer> lineCounts = elementLines.stream()
            .map(lines -> lines.size())
            .collect(Collectors.toList());
    List<Integer> lineLengths = elementLines.stream()
            .map(element -> element.stream()
                    .map(TreeAsciiUtil::htmlLength)
                    .max(Integer::compare)
                    .orElse(0))
            .collect(Collectors.toList());
    int maxLineLength = Collections.max(lineLengths);
    int targetLineCount = lineCounts.stream()
            .mapToInt(Integer::intValue)
            .sum() + elementLines.size() - 1;
    int centerLine = targetLineCount / 2;

    List<String> results = new ArrayList<String>();
    int overallLineCount = 0;
    for (int i = 0; i < elementLines.size(); i++) {
      List<String> lines = elementLines.get(i);

      int lineLength = lineLengths.get(i);

      String leftBuffer = "";
      String rightBuffer = "";
      if (lineLength < maxLineLength) {
        leftBuffer = generateString(" ", (maxLineLength - lineLength) / 2);
        rightBuffer = generateString(" ", (maxLineLength - lineLength) - (maxLineLength - lineLength) / 2);
      }

      String leftBufferCentral = "";
      String rightBufferCentral = "";
      if (lineLength < maxLineLength) {
        leftBufferCentral = generateString("&boxh;", (maxLineLength - lineLength) / 2);
        rightBufferCentral = generateString("&boxh;", (maxLineLength - lineLength) - (maxLineLength - lineLength) / 2);
      }

      int elementCenterLine = lines.size() / 2;

      for (int j = 0; j < lines.size(); j++) {
        String line = lines.get(j);

        StringBuilder sb = new StringBuilder();
        if ((j < elementCenterLine && i == 0) || (j > elementCenterLine && i == elementLines.size() - 1)) {
          sb.append("&nbsp;");
        } else if (j == elementCenterLine && i == 0) {
          sb.append("&boxdr;");
        } else if (i == elementLines.size() - 1 && j == elementCenterLine) {
          sb.append("&boxur;");
        } else if (overallLineCount == centerLine) {
          if (j == elementCenterLine && i > 0 && i < elementLines.size() - 1) {
            sb.append("&boxvh;");
          } else {
            sb.append("&boxvl;");
          }
        } else {
          sb.append("&boxv;");
        }

        if (j == elementCenterLine) {
          sb.append(leftBufferCentral);
        } else {
          sb.append(leftBuffer);
        }
        sb.append(line);
        if (j == elementCenterLine) {
          sb.append(rightBufferCentral);
        } else {
          sb.append(rightBuffer);
        }

        if ((j < elementCenterLine && i == 0) || (j > elementCenterLine && i == elementLines.size() - 1)) {
          sb.append("&nbsp;");
        } else if (j == elementCenterLine && i == 0) {
          sb.append("&boxdl;");
        } else if (i == elementLines.size() - 1 && j == elementCenterLine) {
          sb.append("&boxul;");
        } else if (overallLineCount == centerLine) {
          if (j == elementCenterLine && i > 0 && i < elementLines.size() - 1) {
            sb.append("&boxvh;");
          } else {
            sb.append("&boxvr;");
          }
        } else {
          sb.append("&boxv;");
        }
        results.add(sb.toString());
        overallLineCount++;
      }


      if (i < elementLines.size() - 1) {
        String leftEnding;
        String rightEnding;
        if (overallLineCount == centerLine) {
          leftEnding = "&boxvl;";
          rightEnding = "&boxvr;";
        } else {
          leftEnding = "&boxv;";
          rightEnding = "&boxv;";
        }
        String emptyLine = leftEnding + generateString("&nbsp;", maxLineLength) + rightEnding;
        results.add(emptyLine);
        overallLineCount++;
      }
    }

    return results;
  }

  public static List<String> concatenateMultiLineSerial(String separator, List<List<String>> elementLines) {
    List<Integer> lineCounts = elementLines.stream()
        .map(lines -> lines.size())
        .collect(Collectors.toList());
    String blankSeparator = generateString("&nbsp;", htmlLength(separator));
    int maxLineCount = lineCounts.stream().mapToInt(Integer::intValue).max().orElse(0);
    int centerLine = maxLineCount / 2;
    List<Integer> lineLengths = elementLines.stream()
        .map(element -> element.stream()
            .map(TreeAsciiUtil::htmlLength)
            .max(Integer::compare)
            .orElse(0))
        .collect(Collectors.toList());
    List<String> results = new ArrayList<>();
    for (int line = 0; line < maxLineCount; line++) {
      
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < elementLines.size(); i++) {
        int elementLineCount = lineCounts.get(i);
        int elementCenterLine = elementLineCount / 2;
        int elementLineLength = lineLengths.get(i);
        if (line < centerLine - elementCenterLine
            || line > centerLine - elementCenterLine + elementLineCount) {
          sb.append(generateString("&nbsp;", elementLineLength));
        } else {
          int lineIndex = line + elementCenterLine - centerLine;
          List<String> element = elementLines.get(i);
          String elementLine;
          if (lineIndex < element.size()) {
            elementLine = element.get(lineIndex);
          } else {
            elementLine = generateString("&nbsp;", elementLineLength);
          }
          sb.append(elementLine);
        }
        if (i < elementLines.size() - 1) {
          if (line == centerLine) {
            sb.append(separator);
          } else {
            sb.append(blankSeparator);
          }
        }
      }
      results.add(sb.toString());
    }
    return results;
  }

  public static String generateString(String seed, int n) {
    return String.join("", Collections.nCopies(n, seed));
  }

  public static int htmlLength(String s) {
    return s.replaceAll("\\&.*?\\;", "x").length();
  }
}
