package org.diylc.plugins.chatbot.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a single modification operation on the circuit.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiEditOperation {

  private String action; // "add", "remove", "modify"
  private String componentType; // Catalog name, e.g., "Resistor"
  private String componentName; // e.g., "R1"
  private Map<String, String> properties;
  
  // For SINGLE_CLICK components
  private AiGridPosition position;
  
  // For POINT_BY_POINT non-wires
  private AiGridPosition fromPos;
  private AiGridPosition toPos;
  
  // For POINT_BY_POINT wires and connectors
  private String fromTerminal; // e.g., "R1.0"
  private String toTerminal; // e.g., "C1.1"

  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
  
  public String getComponentType() { return componentType; }
  public void setComponentType(String componentType) { this.componentType = componentType; }
  
  public String getComponentName() { return componentName; }
  public void setComponentName(String componentName) { this.componentName = componentName; }
  
  public Map<String, String> getProperties() { return properties; }
  public void setProperties(Map<String, String> properties) { this.properties = properties; }
  
  public AiGridPosition getPosition() { return position; }
  public void setPosition(AiGridPosition position) { this.position = position; }
  
  public AiGridPosition getFromPos() { return fromPos; }
  public void setFromPos(AiGridPosition fromPos) { this.fromPos = fromPos; }
  
  public AiGridPosition getToPos() { return toPos; }
  public void setToPos(AiGridPosition toPos) { this.toPos = toPos; }
  
  public String getFromTerminal() { return fromTerminal; }
  public void setFromTerminal(String fromTerminal) { this.fromTerminal = fromTerminal; }
  
  public String getToTerminal() { return toTerminal; }
  public void setToTerminal(String toTerminal) { this.toTerminal = toTerminal; }
}
