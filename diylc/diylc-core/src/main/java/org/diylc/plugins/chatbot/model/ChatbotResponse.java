package org.diylc.plugins.chatbot.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatbotResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private String string;
  private AiEditScript editScript;

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public AiEditScript getEditScript() {
    return editScript;
  }

  public void setEditScript(AiEditScript editScript) {
    this.editScript = editScript;
  }
}
