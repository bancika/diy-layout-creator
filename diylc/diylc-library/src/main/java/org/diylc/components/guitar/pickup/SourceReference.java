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
import java.util.Objects;

/**
 * Provenance note for a definition or a specific field within it.
 *
 * <p>Plain final class rather than a record - see {@link Measurement} for why.
 */
public final class SourceReference implements Serializable {

  private static final long serialVersionUID = 1L;

  private final SourceType type;
  private final String reference;

  public SourceReference(SourceType type, String reference) {
    this.type = type;
    this.reference = reference;
  }

  public SourceType type() {
    return type;
  }

  public String reference() {
    return reference;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SourceReference other)) {
      return false;
    }
    return type == other.type && Objects.equals(reference, other.reference);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, reference);
  }

  @Override
  public String toString() {
    return "SourceReference[type=" + type + ", reference=" + reference + "]";
  }
}
