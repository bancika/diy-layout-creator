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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * One component type as listed in the AI catalog index, which the model reads to decide which
 * types it needs before it is given their full catalog entries. {@code notes} carries only the
 * notes meant for that decision, and is left out when there are none.
 *
 * @author Branislav Stojkovic
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiCatalogIndexEntry(String name, String className, List<String> notes) {
}
