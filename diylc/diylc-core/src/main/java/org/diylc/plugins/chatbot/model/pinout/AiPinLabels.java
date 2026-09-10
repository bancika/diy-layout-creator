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
package org.diylc.plugins.chatbot.model.pinout;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * How the terminals of a component type are labelled, when the labels follow a rule and do not have
 * to be listed one by one.
 *
 * @author Branislav Stojkovic
 */
public enum AiPinLabels {

  /** Numbered one to N in index order, which is what the model assumes when told nothing. */
  SEQUENTIAL("sequential"),

  /** No terminal is a named node, so they can only be referred to by index. */
  NONE("none"),

  /** The labels follow no rule and are listed individually. */
  CUSTOM("custom");

  private final String text;

  private AiPinLabels(String text) {
    this.text = text;
  }

  @JsonValue
  public String getText() {
    return text;
  }
}
