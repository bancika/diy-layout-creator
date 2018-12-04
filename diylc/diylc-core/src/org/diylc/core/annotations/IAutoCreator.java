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

import java.util.List;

import org.diylc.core.IDIYComponent;

/**
 * Interface for creating components automatically each time a component is created by the user.
 * 
 * @author Branislav Stojkovic
 */
public interface IAutoCreator {

  /**
   * @param lastAdded the component created by the user
   * @return {@link List} of {@link IDIYComponent}s that will be automatically created as a result.
   *         Empty {@link List} or {@code null} are valid responses for cases when no components
   *         should be auto-created.
   */
  public List<IDIYComponent<?>> createIfNeeded(IDIYComponent<?> lastAdded);
}
