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
package org.diylc.presenter;

import org.diylc.appframework.miscutils.IConfigurationManager;

import org.diylc.common.IPlugInPort;
import org.diylc.core.measures.ValueNotation;

/**
 * Keeps {@link ValueNotation#getCurrent()} in sync with the value notation the user picked in the
 * configuration, and redraws the project when it changes. The notation is global, so only the
 * main window binds it; secondary presenters (previews, thumbnails, batch conversion, regression
 * runs) run on in-memory configurations and must not reset it.
 *
 * @author Branislav Stojkovic
 */
public class ValueNotationBinder {

  public static void bind(IConfigurationManager<?> configManager, Runnable refresh) {
    apply(configManager.readString(IPlugInPort.VALUE_NOTATION_KEY,
        IPlugInPort.VALUE_NOTATION_DEFAULT));
    configManager.addConfigListener(IPlugInPort.VALUE_NOTATION_KEY, (key, value) -> {
      apply(value == null ? null : value.toString());
      // cached component images still carry the labels drawn in the previous notation
      DrawingCache.Instance.clear();
      refresh.run();
    });
  }

  static void apply(String configValue) {
    ValueNotation.setCurrent(IPlugInPort.VALUE_NOTATION_RKM.equalsIgnoreCase(configValue)
        ? ValueNotation.RKM : ValueNotation.DECIMAL);
  }
}
