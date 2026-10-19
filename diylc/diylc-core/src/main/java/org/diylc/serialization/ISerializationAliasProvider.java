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

import org.diylc.core.annotations.Indexed;

/**
 * Registers XStream aliases for classes and fields that were renamed after they shipped, so that
 * the old names keep resolving. Implementations live next to the classes they alias and are
 * applied to every {@link XStream} instance prepared by {@link ProjectFileManager#configure(XStream)}.
 * 
 * @author Branislav Stojkovic
 */
@Indexed
public interface ISerializationAliasProvider {

  /**
   * Registers aliases on the provided {@link XStream} instance.
   * 
   * @param xStream
   */
  void registerAliases(XStream xStream);
}
