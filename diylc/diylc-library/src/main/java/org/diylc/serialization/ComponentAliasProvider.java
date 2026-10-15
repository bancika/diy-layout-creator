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
package org.diylc.serialization;

import com.thoughtworks.xstream.XStream;

import org.diylc.components.AbstractLeadedComponent;

public class ComponentAliasProvider implements ISerializationAliasProvider {

  @Override
  public void registerAliases(XStream xStream) {
    // the enum shipped misspelled and its class name is saved wherever the value is held as an
    // Object (variants, defaults in config.xml); configs from before the package alias carry the
    // full name. The alias registered last is also the one written, so files stay readable by
    // older builds
    xStream.alias("org.diylc.components.AbstractLeadedComponent$LabelOriantation",
        AbstractLeadedComponent.LabelOrientation.class);
    xStream.alias("diylc.AbstractLeadedComponent$LabelOriantation",
        AbstractLeadedComponent.LabelOrientation.class);
  }
}
