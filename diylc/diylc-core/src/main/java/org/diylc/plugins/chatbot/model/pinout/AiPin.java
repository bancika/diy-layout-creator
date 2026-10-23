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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One terminal of a component type, identified by the control point index the model uses to refer
 * to it. The label is the node name the component reports, and is null when the component does not
 * name that point.
 *
 * @author Branislav Stojkovic
 */
public record AiPin(@JsonProperty("i") int index, String label) {
}
