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
package org.diylc.core.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.diylc.core.AllowAllValidator;
import org.diylc.core.IPropertyValidator;

/**
 * Used to annotate editable component property. Editable property XYZ must have both getter and
 * setter named <code>getXYZ</code> and <code>setXYZ</code> respectively and this annotation must be
 * placed on the getter method. <br>
 * <br>
 * In the example below, {@code category} is the property we want to edit and
 * {@code getCategories()} provides a list of available values for category.
 * 
 * <pre>
 * <code>
 * &#64;EditableProperty(additionalOptions = EditableProperty.DYNAMIC_LIST
 *   + "getCategories")
 * public String getCategory() {
 *   return category;
 * }
 * 
 * public String[] getCategories() {
 *   return categories;
 * }
 * </code>
 * </pre>
 * 
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface EditableProperty {

  /**
   * Returns property name. If left blank property name will be extracted from the getter name, e.g.
   * getter <code>getXYZ</code> will induce property name <code>XYZ</code>. <br>
   * Default value is blank.
   * 
   * @return
   */
  String name() default "";

  /**
   * If true, application may set default value for this property. <br>
   * Default value is true.
   * 
   * @return
   */
  boolean defaultable() default true;

  /**
   * Class of validator to use to
   * 
   * @return
   */
  Class<? extends IPropertyValidator> validatorClass() default AllowAllValidator.class;

  /**
   * Optional parameter in case we want to force an order of properties.
   * 
   * @return
   */
  int sortOrder() default 100;
}
