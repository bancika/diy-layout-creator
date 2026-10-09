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

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The terminals a component type exposes for one fixed combination of the properties that
 * determine them. Exactly one of the three descriptions is present, so build one through the
 * factory methods rather than the canonical constructor.
 *
 * @author Branislav Stojkovic
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiTerminals(Integer pinCount, List<AiPin> pins, String pinCountExpression,
    AiPinLabels labels, Map<String, Integer> pinCountSamples, AiPinoutExample example,
    AiSwitching switching) {

  /** Terminals whose labels follow a rule, so a count and that rule say everything. */
  public static AiTerminals counted(int pinCount, AiPinLabels labels, AiSwitching switching) {
    return new AiTerminals(pinCount, null, null, labels, null, null, switching);
  }

  /** Terminals that have to be listed, because their labels follow no rule. */
  public static AiTerminals listed(List<AiPin> pins, AiSwitching switching) {
    return new AiTerminals(null, pins, null, null, null, null, switching);
  }

  /** Terminals whose count follows a formula over one or more numeric properties. */
  public static AiTerminals computed(String expression, AiPinLabels labels,
      Map<String, Integer> samples, AiPinoutExample example) {
    return new AiTerminals(null, null, expression, labels, samples, example, null);
  }
}
