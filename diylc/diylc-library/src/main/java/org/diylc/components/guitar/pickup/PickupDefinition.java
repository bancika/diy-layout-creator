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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Immutable intrinsic catalogue data for a single pickup, describing it as it would sit loose on
 * a workbench: identity, format, coils, terminals and optional specification data. Deliberately
 * excludes any installed/wiring/derived state (see task's "Core design rule").
 *
 * <p>Only {@link #id()}, {@link #manufacturer()}, {@link #model()} and {@link #format()} are
 * required; every other field may be {@code null} or empty.
 *
 * <p>Implemented as a plain final class rather than a Java record: this type is embedded inside
 * placed pickup components (via {@link PickupDefinitionSnapshot}) and serialised via XStream's
 * default reflection-based converter, which mutates final fields directly through
 * {@code sun.misc.Unsafe}. The JVM specifically forbids obtaining field offsets on {@code record}
 * classes, so a record here (or on any of its nested types) would throw
 * {@code UnsupportedOperationException} the moment a project containing an applied pickup
 * definition was loaded. List fields are copied into plain {@link ArrayList}s for the same
 * reason: neither {@code List.of()}/{@code List.copyOf()} (JDK compact immutable lists) nor
 * {@code Collections.unmodifiableList()} have a dedicated XStream converter, so XStream falls
 * back to Java's built-in object serialization for them, which is blocked by the Java Platform
 * Module System (`java.base` does not open `java.util` to application code).
 */
public final class PickupDefinition implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String id;
  private final Integer definitionVersion;
  private final String manufacturer;
  private final String model;
  private final String variant;
  private final PickupFormat format;
  private final Boolean active;
  private final Integer stringCount;
  private final List<CoilDefinition> coils;
  private final List<TerminalDefinition> terminals;
  private final ElectricalSpec electrical;
  private final MagnetSpec magnet;
  private final PhysicalSpec physical;
  private final List<SourceReference> sources;

  public PickupDefinition(String id, Integer definitionVersion, String manufacturer, String model, String variant,
      PickupFormat format, Boolean active, Integer stringCount, List<CoilDefinition> coils,
      List<TerminalDefinition> terminals, ElectricalSpec electrical, MagnetSpec magnet, PhysicalSpec physical,
      List<SourceReference> sources) {
    Objects.requireNonNull(id, "id is required");
    Objects.requireNonNull(manufacturer, "manufacturer is required");
    Objects.requireNonNull(model, "model is required");
    Objects.requireNonNull(format, "format is required");
    this.id = id;
    this.definitionVersion = definitionVersion;
    this.manufacturer = manufacturer;
    this.model = model;
    this.variant = variant;
    this.format = format;
    this.active = active;
    this.stringCount = stringCount;
    this.coils = plainCopy(coils);
    this.terminals = plainCopy(terminals);
    this.electrical = electrical;
    this.magnet = magnet;
    this.physical = physical;
    this.sources = plainCopy(sources);
  }

  private static <T> List<T> plainCopy(List<T> source) {
    return source == null ? new ArrayList<>() : new ArrayList<>(source);
  }

  public String id() {
    return id;
  }

  public Integer definitionVersion() {
    return definitionVersion;
  }

  public String manufacturer() {
    return manufacturer;
  }

  public String model() {
    return model;
  }

  public String variant() {
    return variant;
  }

  public PickupFormat format() {
    return format;
  }

  public Boolean active() {
    return active;
  }

  public Integer stringCount() {
    return stringCount;
  }

  public List<CoilDefinition> coils() {
    return coils;
  }

  public List<TerminalDefinition> terminals() {
    return terminals;
  }

  public ElectricalSpec electrical() {
    return electrical;
  }

  public MagnetSpec magnet() {
    return magnet;
  }

  public PhysicalSpec physical() {
    return physical;
  }

  public List<SourceReference> sources() {
    return sources;
  }

  /** @return a short label combining manufacturer, model and variant, for UI display. */
  public String getDisplayName() {
    StringBuilder sb = new StringBuilder();
    sb.append(manufacturer).append(' ').append(model);
    if (variant != null && !variant.isBlank()) {
      sb.append(" (").append(variant).append(')');
    }
    return sb.toString();
  }

  /** @return the coil with the given local id, or {@code null} if not found. */
  public CoilDefinition findCoil(String coilId) {
    if (coilId == null) {
      return null;
    }
    for (CoilDefinition coil : coils) {
      if (coilId.equals(coil.id())) {
        return coil;
      }
    }
    return null;
  }

  /** @return the terminal with the given id, or {@code null} if not found. */
  public TerminalDefinition findTerminal(String terminalId) {
    if (terminalId == null) {
      return null;
    }
    for (TerminalDefinition terminal : terminals) {
      if (terminalId.equals(terminal.id())) {
        return terminal;
      }
    }
    return null;
  }

  /**
   * @param polarity the magnetic polarity to look for.
   * @return the single coil in this definition that has the given polarity, or {@code null} if
   *         no coil has it or more than one coil claims it (ambiguous - never guess which one is
   *         meant in that case).
   */
  public CoilDefinition findUniqueCoilByPolarity(MagneticPolarity polarity) {
    CoilDefinition match = null;
    for (CoilDefinition coil : coils) {
      if (coil.magneticPolarity() == polarity) {
        if (match != null) {
          return null;
        }
        match = coil;
      }
    }
    return match;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PickupDefinition other)) {
      return false;
    }
    return Objects.equals(id, other.id) && Objects.equals(definitionVersion, other.definitionVersion)
        && Objects.equals(manufacturer, other.manufacturer) && Objects.equals(model, other.model)
        && Objects.equals(variant, other.variant) && format == other.format && Objects.equals(active, other.active)
        && Objects.equals(stringCount, other.stringCount) && Objects.equals(coils, other.coils)
        && Objects.equals(terminals, other.terminals) && Objects.equals(electrical, other.electrical)
        && Objects.equals(magnet, other.magnet) && Objects.equals(physical, other.physical)
        && Objects.equals(sources, other.sources);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, definitionVersion, manufacturer, model, variant, format, active, stringCount, coils,
        terminals, electrical, magnet, physical, sources);
  }

  @Override
  public String toString() {
    return "PickupDefinition[id=" + id + ", manufacturer=" + manufacturer + ", model=" + model + ", variant="
        + variant + ", format=" + format + "]";
  }
}
