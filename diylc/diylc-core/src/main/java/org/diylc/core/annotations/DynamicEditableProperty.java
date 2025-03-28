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
package org.diylc.core.annotations;

import org.diylc.core.IDynamicPropertySource;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/***
 * Used for properties having dynamic set of possible values. Only to be used in addition to
 * EditableProperty annotation.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicEditableProperty {

  /**
   * Optional parameter for dynamic values
   *
   * @return
   */
  Class<? extends IDynamicPropertySource> source() ;
}
