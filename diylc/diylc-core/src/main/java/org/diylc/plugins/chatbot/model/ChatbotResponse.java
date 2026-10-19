package org.diylc.plugins.chatbot.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatbotResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonAlias("message")
  private String string;
  private AiEditScript editScript;
  // set on the client, never sent by the server: identifies the state of the project the model
  // was shown, so that an edit script is only applied to the project it was written against
  @JsonIgnore
  private String projectFingerprint;

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

  @JsonIgnore
  public String getProjectFingerprint() {
    return projectFingerprint;
  }

  @JsonIgnore
  public void setProjectFingerprint(String projectFingerprint) {
    this.projectFingerprint = projectFingerprint;
  }
}
