/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
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
