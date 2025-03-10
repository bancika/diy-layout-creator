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

/**
 * Tells the system how keywords should be extracted from a component.
 * 
 * @see ComponentDescriptor
 * 
 * @author Branislav Stojkovic
 */
public enum KeywordPolicy {
  /**
   * The component is represented with its type.
   */
  SHOW_TYPE_NAME,
  /**
   * The component is represented with its value.
   */
  SHOW_VALUE,
  /**
   * The component is not important enough and will not be shown in the keywords.
   */
  NEVER_SHOW,
  /**
   * The component is represented with the value of {@link ComponentDescriptor#keywordTag()}
   */
  SHOW_TAG,
  /**
   * The component is represented with both the value of {@link ComponentDescriptor#keywordTag()}
   * and the actual component value
   */
  SHOW_TAG_AND_VALUE,
}
