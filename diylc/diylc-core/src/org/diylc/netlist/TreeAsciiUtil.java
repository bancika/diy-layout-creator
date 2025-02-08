package org.diylc.netlist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TreeAsciiUtil {

  public static final String BOXVR = "&#x251C;";
  public static final String BOXUL = "&#x2518;";
  public static final String BOXDL = "&#x2510;";
  public static final String BOXH = "&#x2500;";
  public static final String NBSP = "&nbsp;";
  public static final String BOXDR = "&#x250C;";
  public static final String BOXUR = "&#x2514;";
  public static final String BOXVH = "&#x253C;";
  public static final String BOXVL = "&#x2524;";
  public static final String BOXV = "&#x2502;";

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
        leftBufferCentral = generateString(BOXH, (maxLineLength - lineLength) / 2);
        rightBufferCentral = generateString(BOXH, (maxLineLength - lineLength) - (maxLineLength - lineLength) / 2);
      }
      
      int elementCenterLine = lines.size() / 2;

      for (int j = 0; j < lines.size(); j++) {
        String line = lines.get(j);

        StringBuilder sb = new StringBuilder();
        
        if (overallLineCount == centerLine) {
          sb.append(BOXH);
        } else {
          sb.append(NBSP);
        }
        
        if ((j < elementCenterLine && i == 0) || (j > elementCenterLine && i == elementLines.size() - 1)) {
          sb.append(NBSP);
        } else if (j == elementCenterLine && i == 0) {
          sb.append(BOXDR);
        } else if (i == elementLines.size() - 1 && j == elementCenterLine) {
          sb.append(BOXUR);
        } else if (overallLineCount == centerLine) {
          if (j == elementCenterLine && i > 0 && i < elementLines.size() - 1) {
            sb.append(BOXVH);
          } else {
            sb.append(BOXVL);
          }
        } else {
          if (j == elementCenterLine && i > 0 && i < elementLines.size() - 1) {
            sb.append(BOXVR);
          } else {
            sb.append(BOXV);
          }
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
          sb.append(NBSP);
        } else if (j == elementCenterLine && i == 0) {
          sb.append(BOXDL);
        } else if (i == elementLines.size() - 1 && j == elementCenterLine) {
          sb.append(BOXUL);
        } else if (overallLineCount == centerLine) {
          if (j == elementCenterLine && i > 0 && i < elementLines.size() - 1) {
            sb.append(BOXVH);
          } else {
            sb.append(BOXVR);
          }
        } else {
          if (j == elementCenterLine && i > 0 && i < elementLines.size() - 1) {
            sb.append(BOXVL);
          } else {
            sb.append(BOXV);
          }
        }
        
        if (overallLineCount == centerLine) {
          sb.append(BOXH);
        } else {
          sb.append(NBSP);
        }
        
        results.add(sb.toString());
        overallLineCount++;
      }


      if (i < elementLines.size() - 1) {
        String leftEnding;
        String rightEnding;
        if (overallLineCount == centerLine) {
          leftEnding = BOXH + BOXVL;
          rightEnding = BOXVR + BOXH;
        } else {
          leftEnding = NBSP + BOXV;
          rightEnding = BOXV + NBSP;
        }
        String emptyLine = leftEnding + generateString(NBSP, maxLineLength) + rightEnding;
        results.add(emptyLine);
        overallLineCount++;
      }
    }
    
    List<Integer> collect = results.stream().map(x -> htmlLength(x)).collect(Collectors.toList());

    return results;
  }

  public static List<String> concatenateMultiLineSerial(String separator, List<List<String>> elementLines) {
    List<Integer> lineCounts = elementLines.stream()
        .map(lines -> lines.size())
        .collect(Collectors.toList());
    String blankSeparator = generateString(NBSP, htmlLength(separator));
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
          sb.append(generateString(NBSP, elementLineLength));
        } else {
          int lineIndex = line + elementCenterLine - centerLine;
          List<String> element = elementLines.get(i);
          String elementLine;
          if (lineIndex < element.size()) {
            elementLine = element.get(lineIndex);
          } else {
            elementLine = generateString(NBSP, elementLineLength);
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
