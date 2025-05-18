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
import java.util.Objects;

public class ChatMessageEntity implements Serializable {

  private String prompt;
  private String response;
  private String createdOn;

  public ChatMessageEntity(String prompt, String response, String createdOn) {
    this.prompt = prompt;
    this.response = response;
    this.createdOn = createdOn;
  }

  public String getPrompt() {
    return prompt;
  }

  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public String getResponse() {
    return response;
  }

  public void setResponse(String response) {
    this.response = response;
  }

  public String getCreatedOn() {
    return createdOn;
  }

  public void setCreatedOn(String createdOn) {
    this.createdOn = createdOn;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass())
      return false;
    ChatMessageEntity that = (ChatMessageEntity) o;
    return Objects.equals(prompt, that.prompt) && Objects.equals(response,
        that.response) && Objects.equals(createdOn, that.createdOn);
  }

  @Override
  public int hashCode() {
    return Objects.hash(prompt, response, createdOn);
  }
}
