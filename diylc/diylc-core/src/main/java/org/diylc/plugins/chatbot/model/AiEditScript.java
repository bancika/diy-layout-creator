package org.diylc.plugins.chatbot.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents a set of operations to modify the circuit, returned by the AI.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiEditScript {

  private String explanation;
  private List<AiEditOperation> operations;

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }

  public List<AiEditOperation> getOperations() {
    return operations;
  }

  public void setOperations(List<AiEditOperation> operations) {
    this.operations = operations;
  }
}
