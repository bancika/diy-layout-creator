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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.diylc.utils.ReflectionUtils;

/**
 * Marks a type whose concrete implementations are discovered at runtime. The
 * {@code diylc-processor} annotation processor lists them under {@code META-INF/services} when
 * the implementing module is compiled, and {@link ReflectionUtils#getIndexedClasses(Class)} reads
 * that list back, so nothing has to scan the classpath. An implementation is picked up just by
 * implementing the type; there is no registry to update.
 *
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface Indexed {

}
