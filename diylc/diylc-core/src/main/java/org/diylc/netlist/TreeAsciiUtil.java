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
    
    if (elementLines == null || elementLines.isEmpty()) {
      return new ArrayList<>();
    }
    
    if (elementLines.size() == 1) {
      return new ArrayList<>(elementLines.get(0));
    }

    // Calculate maximum line length for each element
    List<Integer> lineLengths = elementLines.stream()
            .map(element -> element.stream()
                    .map(TreeAsciiUtil::htmlLength)
                    .max(Integer::compare)
                    .orElse(0))
            .collect(Collectors.toList());
    int maxLineLength = Collections.max(lineLengths);
    
    // Calculate total height and center positions
    List<Integer> elementHeights = elementLines.stream()
            .map(List::size)
            .collect(Collectors.toList());
    
    // Calculate total height including spacing lines
    int totalHeight = elementHeights.stream().mapToInt(Integer::intValue).sum() + (elementLines.size() - 1);
    int centerLine = totalHeight / 2;
    
    List<String> results = new ArrayList<>();
    int lineCounter = 0;
    
    // Process each element
    for (int i = 0; i < elementLines.size(); i++) {
        List<String> element = elementLines.get(i);
        int elementHeight = element.size();
        int elementCenter = elementHeight / 2;
        
        // Calculate the first and last line numbers for the entire parallel group
        int firstLineOfGroup = 0;
        int lastLineOfGroup = totalHeight - 1;
        
        // Process each line of the element
        for (int j = 0; j < elementHeight; j++) {
            StringBuilder sb = new StringBuilder();
            String line = element.get(j);
            int lineLength = htmlLength(line);
            
            // Calculate padding to center this element's content
            int leftPadding = (maxLineLength - lineLength) / 2;
            int rightPadding = maxLineLength - lineLength - leftPadding;
            
            // Start character
            if (lineCounter == centerLine) {
                sb.append(BOXH);
            } else {
                sb.append(NBSP);
            }
            
            // Left connection
            if (j == elementCenter) {
                if (i == 0) {
                    // First element in parallel group
                    sb.append(BOXDR); // Top-left corner (┌)
                } else if (i == elementLines.size() - 1) {
                    // Last element in parallel group
                    sb.append(BOXUR); // Bottom-left corner (└)
                } else {
                    sb.append(BOXVR); // T-connection (├) for middle elements
                }
            } else {
                if (j < elementCenter && i == 0) {
                    sb.append(NBSP); // No connection above first element
                } else if (j > elementCenter && i == elementLines.size() - 1) {
                    sb.append(NBSP); // No connection below last element
                } else if (lineCounter == centerLine) {
                    sb.append(BOXVL); // Horizontal connection
                } else {
                    sb.append(BOXV); // Vertical line
                }
            }
            
            // Left padding
            if (j == elementCenter) {
                sb.append(generateString(BOXH, leftPadding));
            } else {
                sb.append(generateString(NBSP, leftPadding));
            }
            
            // Element content
            sb.append(line);
            
            // Right padding
            if (j == elementCenter) {
                sb.append(generateString(BOXH, rightPadding));
            } else {
                sb.append(generateString(NBSP, rightPadding));
            }
            
            // Right connection
            if (j == elementCenter) {
                if (i == 0) {
                    // First element in parallel group
                    sb.append(BOXDL); // Top-right corner (┐)
                } else if (i == elementLines.size() - 1) {
                    // Last element in parallel group
                    sb.append(BOXUL); // Bottom-right corner (┘)
                } else {
                    sb.append(BOXVL); // T-connection (┤) for middle elements
                }
            } else {
                if (j < elementCenter && i == 0) {
                    sb.append(NBSP); // No connection above first element
                } else if (j > elementCenter && i == elementLines.size() - 1) {
                    sb.append(NBSP); // No connection below last element
                } else if (lineCounter == centerLine) {
                    sb.append(BOXVR); // Horizontal connection
                } else {
                    sb.append(BOXV); // Vertical line
                }
            }
            
            // End character
            if (lineCounter == centerLine) {
                sb.append(BOXH);
            } else {
                sb.append(NBSP);
            }
            
            results.add(sb.toString());
            lineCounter++;
        }
        
        // Add a separator line between elements (except after the last one)
        if (i < elementLines.size() - 1) {
            StringBuilder separatorLine = new StringBuilder();
            
            // Start character
            if (lineCounter == centerLine) {
                separatorLine.append(BOXH);
            } else {
                separatorLine.append(NBSP);
            }
            
            // Left connection
            if (lineCounter == centerLine) {
                separatorLine.append(BOXVL);
                separatorLine.append(generateString(NBSP, maxLineLength));
                separatorLine.append(BOXVR);
            } else {
                separatorLine.append(BOXV);
                separatorLine.append(generateString(NBSP, maxLineLength));
                separatorLine.append(BOXV);
            }
            
            // End character
            if (lineCounter == centerLine) {
                separatorLine.append(BOXH);
            } else {
                separatorLine.append(NBSP);
            }
            
            results.add(separatorLine.toString());
            lineCounter++;
        }
    }
    
    return results;
  }

  public static List<String> concatenateMultiLineSerial(String separator, List<List<String>> elementLines) {
    if (elementLines == null || elementLines.isEmpty()) {
      return new ArrayList<>();
    }
    
    if (elementLines.size() == 1) {
      return new ArrayList<>(elementLines.get(0));
    }

    List<Integer> lineCounts = elementLines.stream()
        .map(List::size)
        .collect(Collectors.toList());
    
    // Detect if all elements have the same height
    boolean uniformHeight = lineCounts.stream().distinct().count() == 1;
    
    String blankSeparator = generateString(NBSP, htmlLength(separator));
    int maxLineCount = Collections.max(lineCounts);
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
        List<String> element = elementLines.get(i);
        int elementHeight = lineCounts.get(i);
        int elementCenter = elementHeight / 2;
        int elementWidth = lineLengths.get(i);
        
        // Determine if this line aligns with the element
        boolean lineAligned = !(line < centerLine - elementCenter || 
                               line > centerLine - elementCenter + elementHeight - 1);
        
        if (!lineAligned) {
          // Line doesn't align - fill with spaces
          sb.append(generateString(NBSP, elementWidth));
        } else {
          // Line aligns - show content
          int elementLine = line - (centerLine - elementCenter);
          if (elementLine < element.size()) {
            String content = element.get(elementLine);
            sb.append(content);
            
            // Pad if needed
            int contentLength = htmlLength(content);
            if (contentLength < elementWidth) {
              sb.append(generateString(NBSP, elementWidth - contentLength));
            }
          } else {
            sb.append(generateString(NBSP, elementWidth));
          }
        }
        
        // Add separator after each element except the last
        if (i < elementLines.size() - 1) {
            // Only add separator on the center line
            if (line == centerLine) {
                // Check if the current element ends with a box character
                String currentLine = sb.toString();
                if (currentLine.endsWith(BOXDL) || currentLine.endsWith(BOXUL)) {
                    // If it ends with a box character, just add a single dash
                    sb.append(BOXH);
                } else {
                    // Otherwise add normal separator
                    sb.append(separator);
                }
            } else {
                // Don't add any separator on non-center lines
                sb.append(NBSP);
            }
        }
      }
      
      results.add(sb.toString());
    }
    
    return results;
  }

  public static String generateString(String seed, int n) {
    if (n <= 0) return "";
    return String.join("", Collections.nCopies(n, seed));
  }

  public static int htmlLength(String s) {
    if (s == null) return 0;
    return s.replaceAll("\\&.*?\\;", "x").length();
  }
}
