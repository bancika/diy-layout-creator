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
 * A single lead-out terminal of the pickup, identified semantically (e.g. {@code "coilA.start"}
 * or {@code "shield"}) rather than by wire colour. {@code coilId} references
 * {@link CoilDefinition#id()} and is {@code null} for terminals that are not part of any coil
 * winding (e.g. a shield/ground lead).
 *
 * <p>Plain final class rather than a record - see {@link Measurement} for why.
 */
public final class TerminalDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String id;
  private final String coilId;
  private final TerminalRole role;
  private final LeadSpec lead;

  public TerminalDefinition(String id, String coilId, TerminalRole role, LeadSpec lead) {
    this.id = id;
    this.coilId = coilId;
    this.role = role;
    this.lead = lead;
  }

  public String id() {
    return id;
  }

  public String coilId() {
    return coilId;
  }

  public TerminalRole role() {
    return role;
  }

  public LeadSpec lead() {
    return lead;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TerminalDefinition other)) {
      return false;
    }
    return Objects.equals(id, other.id) && Objects.equals(coilId, other.coilId) && role == other.role
        && Objects.equals(lead, other.lead);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, coilId, role, lead);
  }

  @Override
  public String toString() {
    return "TerminalDefinition[id=" + id + ", coilId=" + coilId + ", role=" + role + ", lead=" + lead + "]";
  }
}
