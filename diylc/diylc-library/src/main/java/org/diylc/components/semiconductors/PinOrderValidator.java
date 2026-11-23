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
package org.diylc.components.semiconductors;

import org.diylc.core.IPropertyValidator;
import org.diylc.core.ValidationException;

public class PinOrderValidator implements IPropertyValidator {

  private static final String MESSAGE =
      "must list each of R, G, B and the common lead (C, K or A) exactly once, e.g. RCGB.";

  @Override
  public void validate(Object owner, Object value) throws ValidationException {
    if (value == null) {
      return;
    }
    String order = value.toString().trim().toUpperCase();
    if (order.length() != 4) {
      throw new ValidationException(MESSAGE);
    }
    int r = 0;
    int g = 0;
    int b = 0;
    int common = 0;
    for (char c : order.toCharArray()) {
      if (c == 'R') {
        r++;
      } else if (c == 'G') {
        g++;
      } else if (c == 'B') {
        b++;
      } else if (c == 'C' || c == 'K' || c == 'A') {
        common++;
      } else {
        throw new ValidationException(MESSAGE);
      }
    }
    if (r != 1 || g != 1 || b != 1 || common != 1) {
      throw new ValidationException(MESSAGE);
    }
  }
}
