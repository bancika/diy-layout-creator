/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

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
package org.diylc.common;

import java.util.List;

import org.diylc.netlist.Netlist;

/**
 * Interface for {@link Netlist} related operations.
 * 
 * @author Branislav Stojkovic
 */
public interface INetlistProcessor {

  /**
   * Finds all {@link Netlist}s for each switch position combination. 
   * 
   * @param includeSwitches
   * @return
   */
  List<Netlist> extractNetlists(boolean includeSwitches);
  
  /**
   * Finds all available {@link INetlistAnalyzer} implementations.
   * 
   * @return
   */
  List<INetlistAnalyzer> getNetlistAnalyzers();
  
  
}
