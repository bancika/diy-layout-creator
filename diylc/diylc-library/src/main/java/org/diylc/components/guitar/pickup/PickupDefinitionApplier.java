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

import org.diylc.components.guitar.AbstractGuitarPickup;
import org.diylc.components.guitar.AbstractGuitarPickup.Polarity;
import org.diylc.components.guitar.AbstractSingleOrHumbuckerPickup;
import org.diylc.components.guitar.HumbuckerPickup;
import org.diylc.components.guitar.SingleCoilPickup;

/**
 * Populates a pickup component's own fields from an applied {@link PickupDefinition}. Used both
 * for a same-type, in-place update and, after a cross-type replacement has placed a new
 * component, to give that new component the same metadata a same-type update would have applied.
 *
 * <p>Fields applied, per the task's "same component type" rules:
 * <ul>
 * <li>definition id + snapshot (always);</li>
 * <li>model/value label (always, set to the definition's display name);</li>
 * <li>pole-piece metadata, for the two component classes that actually expose a settable
 * pole-piece property ({@link HumbuckerPickup#setCoilType1}/{@code setCoilType2} and
 * {@link SingleCoilPickup#setPolePieceType}) - and only when the mapping from the library's
 * {@link CoilPolePieceType} to the component's own enum is unambiguous;</li>
 * <li>magnetic polarity, for any {@link AbstractSingleOrHumbuckerPickup} - but only while it is
 * already in 2-terminal (non-humbucking) mode, and only North/South, never automatically
 * switching a pickup into or out of 4-terminal humbucking mode, since that would change the
 * diagram's circuit topology (see task compatibility requirements).
 * </ul>
 * Every mapping below is deliberately conservative: whenever the source metadata is missing or
 * ambiguous (e.g. no single coil has a given polarity, or the pole-piece type has no equivalent
 * on this component), the existing property is left untouched rather than guessed at. Unrelated
 * user styling (body/pole/bobbin colours, etc.) is never touched here.
 */
public final class PickupDefinitionApplier {

  private PickupDefinitionApplier() {}

  public static void applyFields(AbstractGuitarPickup pickup, PickupDefinition definition) {
    pickup.setPickupDefinitionSnapshot(new PickupDefinitionSnapshot(definition));
    pickup.setValue(definition.getDisplayName());

    if (pickup instanceof HumbuckerPickup humbucker) {
      applyHumbuckerPolePieces(humbucker, definition);
    } else if (pickup instanceof SingleCoilPickup singleCoil) {
      applySingleCoilPolePieces(singleCoil, definition);
    }

    if (pickup instanceof AbstractSingleOrHumbuckerPickup twoOrFourLeadPickup) {
      applyPolarityIfSafe(twoOrFourLeadPickup, definition);
    }
  }

  private static void applyHumbuckerPolePieces(HumbuckerPickup humbucker, PickupDefinition definition) {
    CoilDefinition northCoil = definition.findUniqueCoilByPolarity(MagneticPolarity.NORTH);
    CoilDefinition southCoil = definition.findUniqueCoilByPolarity(MagneticPolarity.SOUTH);

    HumbuckerPickup.PolePieceType coilType1 = toHumbuckerPolePieceType(northCoil);
    if (coilType1 != null) {
      humbucker.setCoilType1(coilType1);
    }
    HumbuckerPickup.PolePieceType coilType2 = toHumbuckerPolePieceType(southCoil);
    if (coilType2 != null) {
      humbucker.setCoilType2(coilType2);
    }
  }

  private static HumbuckerPickup.PolePieceType toHumbuckerPolePieceType(CoilDefinition coil) {
    if (coil == null || coil.polePieceType() == null) {
      return null;
    }
    return switch (coil.polePieceType()) {
      case SLUG, ROD -> HumbuckerPickup.PolePieceType.Rods;
      case SCREW -> HumbuckerPickup.PolePieceType.Screws;
      case RAIL -> HumbuckerPickup.PolePieceType.Rail;
      // BLADE and UNKNOWN have no faithful equivalent on HumbuckerPickup - leave unchanged
      // rather than guess.
      default -> null;
    };
  }

  private static void applySingleCoilPolePieces(SingleCoilPickup singleCoil, PickupDefinition definition) {
    // A single coil's pole-piece property describes the one physical coil it has; with zero or
    // several coils in the definition there's no reliable single value to apply.
    if (definition.coils().size() != 1) {
      return;
    }
    SingleCoilPickup.PolePieceType type = toSingleCoilPolePieceType(definition.coils().get(0));
    if (type != null) {
      singleCoil.setPolePieceType(type);
    }
  }

  private static SingleCoilPickup.PolePieceType toSingleCoilPolePieceType(CoilDefinition coil) {
    if (coil == null || coil.polePieceType() == null) {
      return null;
    }
    return switch (coil.polePieceType()) {
      case SLUG, ROD -> SingleCoilPickup.PolePieceType.Rods;
      case RAIL -> SingleCoilPickup.PolePieceType.Rail;
      // SCREW and BLADE have no faithful equivalent on SingleCoilPickup - leave unchanged.
      default -> null;
    };
  }

  private static void applyPolarityIfSafe(AbstractSingleOrHumbuckerPickup pickup, PickupDefinition definition) {
    // Never automatically switch a pickup into or out of 4-terminal humbucking mode: that would
    // change the number of active control points/leads, i.e. the diagram's circuit topology,
    // which applying a definition must never do.
    if (pickup.getPolarity() == Polarity.Humbucking) {
      return;
    }
    // Only a definition describing exactly one coil maps unambiguously onto this 2-terminal
    // pickup's single polarity property.
    if (definition.coils().size() != 1) {
      return;
    }
    MagneticPolarity polarity = definition.coils().get(0).magneticPolarity();
    if (polarity == MagneticPolarity.NORTH) {
      pickup.setPolarity(Polarity.North);
    } else if (polarity == MagneticPolarity.SOUTH) {
      pickup.setPolarity(Polarity.South);
    }
    // UNKNOWN (or null): leave the pickup's existing polarity exactly as it is.
  }
}
