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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A hand-written note about a component type that is published in the AI component catalog: how
 * the component is normally sized and placed, which defaults need changing, or anything else the
 * model cannot work out from the editable properties alone.
 * <p>
 * May be put on an abstract base class as well as on a concrete one. A component type gets the
 * notes of every annotated class in its hierarchy, those of the most general class first. The
 * annotation may be repeated to give a class several notes, which keep the order they are written in.
 * <p>
 * Every note goes into the full catalog. Notes with {@link Scope#SELECTION} also go into the catalog
 * index, which is all the model sees when it decides which component types it needs.
 *
 * @author Branislav Stojkovic
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(AiNotes.class)
public @interface AiNote {

  String value();

  Scope scope() default Scope.USAGE;

  enum Scope {
    /** Helps decide whether to use the component type at all, e.g. when to prefer another one. */
    SELECTION,
    /** Helps use the component type once chosen: placement, sizing, which properties to set. */
    USAGE
  }
}
