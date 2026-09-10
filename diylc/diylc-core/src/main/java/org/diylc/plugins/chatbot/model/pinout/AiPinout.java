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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * What terminals a component type exposes, as published in the AI component catalog.
 * <p>
 * When no editable property changes the terminals, {@code terminals} describes them outright and
 * there are no drivers. Otherwise {@code drivers} names the properties that change them and
 * {@code variants} works through the combinations, except where a single description covers them
 * all because the count follows a formula.
 *
 * @author Branislav Stojkovic
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiPinout(List<String> drivers, List<String> driversHeldAtDefault,
    @JsonUnwrapped AiTerminals terminals, List<AiPinoutVariant> variants) {

  /** Terminals that nothing the user can edit moves. */
  public static AiPinout fixed(AiTerminals terminals) {
    return new AiPinout(null, null, terminals, null);
  }

  /** One description covering every value of the drivers, because the count follows a formula. */
  public static AiPinout uniform(List<String> drivers, List<String> heldAtDefault,
      AiTerminals terminals) {
    return new AiPinout(drivers, heldAtDefault, terminals, null);
  }

  /** One description per combination of the drivers. */
  public static AiPinout varying(List<String> drivers, List<String> heldAtDefault,
      List<AiPinoutVariant> variants) {
    return new AiPinout(drivers, heldAtDefault, null, variants);
  }
}
