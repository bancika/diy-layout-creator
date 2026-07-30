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

/**
 * A single problem found while loading/validating a pickup library file. Carries enough context
 * (source file, pickup id, field path) to be logged usefully and, per the task's requirements,
 * never aborts the overall load - offending entries/fields are simply skipped.
 *
 * @param source human-readable origin of the problem, e.g. {@code "classpath:pickups/pickups.json"}
 *        or an absolute user file path.
 * @param pickupId the {@code id} of the pickup entry the problem was found in, or {@code null} if
 *        the problem is not tied to a specific entry (e.g. a top-level file parsing error).
 * @param fieldPath a dotted/indexed path to the offending field, e.g. {@code "coils[0].dcResistance.unit"},
 *        or {@code null} if not applicable.
 * @param message human-readable description of the problem.
 */
public record PickupLoadIssue(String source, String pickupId, String fieldPath, String message) {

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(source);
    if (pickupId != null) {
      sb.append(" [pickup=").append(pickupId).append(']');
    }
    if (fieldPath != null) {
      sb.append(" [field=").append(fieldPath).append(']');
    }
    sb.append(": ").append(message);
    return sb.toString();
  }
}
