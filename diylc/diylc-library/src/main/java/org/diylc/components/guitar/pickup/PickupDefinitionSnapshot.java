/*
 *
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 *
 * This file is part of DIYLC.
 *
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.components.guitar.pickup;

import java.io.Serializable;

/**
 * A frozen, self-contained copy of a {@link PickupDefinition} that is stored directly on a
 * placed pickup component. It is what gets serialised into the {@code .diy} project file, so
 * that the project remains fully portable and stable even if:
 * <ul>
 * <li>the project is opened on another computer without the same user library;</li>
 * <li>a user library file is later removed or renamed;</li>
 * <li>a built-in definition is changed or removed in a later DIYLC release.</li>
 * </ul>
 * The snapshot, not the live {@link PickupLibrary}, is authoritative for a placed component.
 * The referenced {@link #definitionId()} is kept only for display and possible future
 * "refresh from library" functionality; the library is never consulted automatically to update
 * an already-placed pickup.
 */
public final class PickupDefinitionSnapshot implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String definitionId;
  private final PickupDefinition definition;

  /**
   * XStream (used for project persistence) instantiates objects reflectively without calling a
   * constructor, but keeping a no-arg constructor here matches the rest of the codebase's
   * component/model classes and keeps this class usable from generic tooling if ever needed.
   */
  public PickupDefinitionSnapshot() {
    this.definitionId = null;
    this.definition = null;
  }

  public PickupDefinitionSnapshot(PickupDefinition definition) {
    this.definition = definition;
    this.definitionId = definition == null ? null : definition.id();
  }

  /** @return the id of the {@link PickupDefinition} this snapshot was captured from. */
  public String getDefinitionId() {
    return definitionId;
  }

  /** @return the frozen definition data itself, authoritative for the placed component. */
  public PickupDefinition getDefinition() {
    return definition;
  }
}
