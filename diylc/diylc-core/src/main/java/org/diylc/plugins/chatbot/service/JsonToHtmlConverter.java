package org.diylc.plugins.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonToHtmlConverter {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Convert the given JSON string into HTML.
   *
   * @param json the input JSON
   * @return an HTML document as a String
   * @throws IOException if JSON parsing fails
   */
  public static String convertToHtml(String json) throws IOException {
    JsonNode root = MAPPER.readTree(json);
    StringBuilder html = new StringBuilder();

    html.append("<!DOCTYPE html>\n<html>\n<head>\n")
        .append("  <meta charset=\"UTF-8\">\n")
        .append("  <title>Validation Report</title>\n")
        .append("  <style>\n")
        .append("    body { font-family: Arial, sans-serif; }\n")
        .append("    h2 { color: #444; }\n")
        .append("    ul { list-style: disc; margin-left: 20px; }\n")
        .append("    li { margin-bottom: 5px; }\n")
        .append("  </style>\n")
        .append("</head>\n<body>\n");

    // Process each section
    processSection(root, "critical", html, "#c00");
    processSection(root, "warnings", html, "#e65c00");
    processSection(root, "optimizations", html, "#0066c0");

    html.append("</body>\n</html>");
    return html.toString();
  }

  private static void processSection(JsonNode root, String field, StringBuilder html, String color) {
    JsonNode array = root.path(field);
    if (array.isArray() && array.size() > 0) {
      html.append(String.format("<h2 style=\"color:%s;\">%s</h2>\n", color, capitalize(field)));
      html.append("<ul>\n");
      for (JsonNode item : array) {
        String message = escapeHtml(item.path("message").asText(""));
        if (item.hasNonNull("componentId") && !item.path("componentId").asText().isEmpty()) {
          String id = escapeHtml(item.path("componentId").asText());
          html.append("  <li><strong>")
              .append(id)
              .append("</strong>: ")
              .append(message)
              .append("</li>\n");
        } else {
          html.append("  <li>")
              .append(message)
              .append("</li>\n");
        }
      }
      html.append("</ul>\n");
    }
  }

  private static String capitalize(String text) {
    if (text == null || text.isEmpty()) return text;
    return Character.toUpperCase(text.charAt(0)) + text.substring(1);
  }

  /**
   * Very basic HTML escapement; for more robust use Apache Commons StringEscapeUtils.escapeHtml4().
   */
  private static String escapeHtml(String s) {
    return s.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }

  // Example usage:
  public static void main(String[] args) throws IOException {
    String json = "{\n" +
        "  \"critical\": [{\"message\":\"Value too low\"}],\n" +
        "  \"warnings\": [{\"componentId\":\"C3\",\"message\":\"Consider higher tolerance\"}],\n" +
        "  \"optimizations\": [{\"componentId\":\"L2\",\"message\":\"Could use 0805 package\"}]\n" +
        "}";
    String html = convertToHtml(json);
    System.out.println(html);
  }
}
