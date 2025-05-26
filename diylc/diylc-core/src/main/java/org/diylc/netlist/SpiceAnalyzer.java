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
package org.diylc.netlist;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.diylc.common.INetlistAnalyzer;
import org.diylc.common.NetlistSwitchPreference;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.ComponentProcessor;

public class SpiceAnalyzer extends AbstractNetlistAnalyzer implements INetlistAnalyzer {

  public SpiceAnalyzer() {
  }

  @Override
  public String getName() {
    return "Generate Spice Netlist";
  }
  
  @Override
  public String getShortName() {
    return "spice";
  }

  @Override
  public String getIconName() {
    return "JarBeanInto";
  }
  
  @Override
  public String getFontName() {   
    return "Courier New";
  }

  @Override
  public Set<NetlistSwitchPreference> getSwitchPreference() {
    return EnumSet.allOf(NetlistSwitchPreference.class);
  }
  
  protected Summary summarize(Netlist netlist) throws TreeException {
    return new Summary(netlist, SpiceSumarizer.summarize(netlist, true));
  }
}
