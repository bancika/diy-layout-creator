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
  private AiPoint position;
  
  // For POINT_BY_POINT non-wires
  private AiPoint fromPos;
  private AiPoint toPos;
  
  // For POINT_BY_POINT wires and connectors
  private String fromTerminal; // e.g., "R1.0"
  private String toTerminal; // e.g., "C1.1"

  // For z-order operations
  private String referenceComponent; // e.g., "Board1"

  public String getAction() { return action; }
  public void setAction(String action) { this.action = action; }
  
  public String getComponentType() { return componentType; }
  public void setComponentType(String componentType) { this.componentType = componentType; }
  
  public String getComponentName() { return componentName; }
  public void setComponentName(String componentName) { this.componentName = componentName; }
  
  public Map<String, String> getProperties() { return properties; }
  public void setProperties(Map<String, String> properties) { this.properties = properties; }
  
  public AiPoint getPosition() { return position; }
  public void setPosition(AiPoint position) { this.position = position; }
  
  public AiPoint getFromPos() { return fromPos; }
  public void setFromPos(AiPoint fromPos) { this.fromPos = fromPos; }
  
  public AiPoint getToPos() { return toPos; }
  public void setToPos(AiPoint toPos) { this.toPos = toPos; }
  
  public String getFromTerminal() { return fromTerminal; }
  public void setFromTerminal(String fromTerminal) { this.fromTerminal = fromTerminal; }
  
  public String getToTerminal() { return toTerminal; }
  public void setToTerminal(String toTerminal) { this.toTerminal = toTerminal; }

  public String getReferenceComponent() { return referenceComponent; }
  public void setReferenceComponent(String referenceComponent) { this.referenceComponent = referenceComponent; }
}
