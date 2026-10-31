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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Covers the shapes of response the server sends back from a prompt.
 *
 * @author Branislav Stojkovic
 */
public class ChatbotResponseTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  public void declinedEditKeepsTheMessageForTheUser() throws Exception {
    ChatbotResponse response = MAPPER.readValue("{\"editScript\": {\"explanation\": "
        + "\"Circuit too large\", \"operations\": []}, \"message\": \"Your circuit contains too "
        + "many components to process within your current plan.\"}", ChatbotResponse.class);

    assertEquals("Your circuit contains too many components to process within your current plan.",
        response.getString());
    assertTrue(response.getEditScript().getOperations().isEmpty());
  }

  @Test
  public void replaceCarriesItsPinMapEvenWithNumbersForValues() throws Exception {
    AiEditScript script = MAPPER.readValue("{\"operations\": [{\"action\": \"replace\", "
        + "\"componentName\": \"PU1\", \"componentType\": \"guitar.HumbuckerPickup\", "
        + "\"pinMap\": {\"0\": \"1\", \"1\": 3}}]}", AiEditScript.class);

    assertEquals(Map.of("0", "1", "1", "3"), script.getOperations().get(0).getPinMap());
  }

  @Test
  public void plainAnswerIsReadFromTheStringField() throws Exception {
    ChatbotResponse response =
        MAPPER.readValue("{\"string\": \"Ohm's law relates...\"}", ChatbotResponse.class);

    assertEquals("Ohm's law relates...", response.getString());
  }
}
