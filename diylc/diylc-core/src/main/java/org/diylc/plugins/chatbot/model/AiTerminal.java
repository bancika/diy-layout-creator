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

/**
 * @param connectedTo only on connectivity components: the terminals of other components this one
 *        physically touches, e.g. "Bridge.2". Other wires are left out when the end has a net,
 *        which already says where they lead.
 * @param net only on wire ends: the net the end is on, when it reaches one
 */
public record AiTerminal(int id, String label, AiPoint pos, List<String> connectedTo, String net) {
}
