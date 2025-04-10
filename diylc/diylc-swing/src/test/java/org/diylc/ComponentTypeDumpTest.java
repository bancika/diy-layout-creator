package org.diylc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.diylc.common.ComponentType;
import org.diylc.presenter.ComponentProcessor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComponentTypeDumpTest {

//  @Test
  public void dumpComponentTypesToKnowledgeBaseSQLFile() {
    Map<String, List<ComponentType>> componentTypes =
        ComponentProcessor.getInstance().getComponentTypes();

    List<DIYLCComponentType> components = componentTypes.values().stream()
        .flatMap(x -> x.stream())
        .map(x -> new DIYLCComponentType(x.getName(), x.getDescription(), x.getCategory(),
            ComponentProcessor.getInstance().extractProperties(x.getInstanceClass()).stream()
                .map(y -> new DIYLCProperty(y.getName(), formatType(y.getType())))
                .toList()))
        .toList();

    try (PrintWriter writer = new PrintWriter(new File("/users/branislavstojkovic/downloads/test.sql"))) {
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);

      for (DIYLCComponentType component : components) {
        String section = "Component Type: " + component.getName();
        String escapedSection = section.replace("'", "''");
        
        // First delete any existing record for this component type
        String deleteSql = String.format(
            "DELETE FROM diylc_knowledge_base WHERE section = '%s';\n",
            escapedSection
        );
        writer.write(deleteSql);
        
        // Then insert the new record
        String content = mapper.writeValueAsString(component);
        content = content.replace("'", "''");
        
        String insertSql = String.format(
            "INSERT INTO diylc_knowledge_base (section, content) VALUES ('%s', '%s');\n",
            escapedSection,
            content
        );
        
        writer.write(insertSql);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to write SQL file", e);
    }
  }

  private String formatType(Class<?> type) {
    if (type.isEnum()) {
      String enumValues = Arrays.stream(type.getEnumConstants())
          .map(Object::toString)
          .collect(Collectors.joining(","));
      return type.getName() + "(" + enumValues + ")";
    }
    return type.getName();
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  static class DIYLCProperty {
    private String name;
    private String type;

    public DIYLCProperty(String name, String type) {
      this.name = name;
      this.type = type;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private static class DIYLCComponentType {
    private String name;
    private String description;
    private String category;
    private List<DIYLCProperty> properties;

    public DIYLCComponentType(String name, String description, String category,
        List<DIYLCProperty> properties) {
      this.name = name;
      this.description = description;
      this.category = category;
      this.properties = properties;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public List<DIYLCProperty> getProperties() { return properties; }
    public void setProperties(List<DIYLCProperty> properties) { this.properties = properties; }
  }
}
