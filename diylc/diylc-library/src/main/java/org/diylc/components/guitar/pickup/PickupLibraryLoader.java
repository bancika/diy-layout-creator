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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Parses and validates a single pickup library JSON document (see
 * {@code diylc-library/src/main/resources/pickups/pickup-schema.json} for the documented shape).
 *
 * <p>Design goals mandated by the task:
 * <ul>
 * <li>all fields are optional except minimal identity ({@code id}, {@code manufacturer},
 * {@code model}) and {@code format};</li>
 * <li>unknown JSON fields are ignored (this class reads fields it knows about from a generic
 * {@link JsonNode} tree rather than doing strict POJO binding, so anything else is simply never
 * looked at);</li>
 * <li>a malformed entry/field never stops the rest of the file (or other files) from loading -
 * problems are collected as {@link PickupLoadIssue}s with file/id/field-path/message and the
 * offending entry (or just the offending optional field) is skipped.</li>
 * </ul>
 *
 * <p>This class is stateless; each call to {@link #parse(String, String)} is independent.
 */
public class PickupLibraryLoader {

  public static final int SUPPORTED_SCHEMA_VERSION = 1;

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /** Result of parsing one JSON document: whatever valid pickups were found, plus any issues. */
  public record ParsedFile(Integer schemaVersion, String libraryId, List<PickupDefinition> pickups,
      List<PickupLoadIssue> issues) {
  }

  /**
   * @param json raw JSON text of a pickup library file.
   * @param source human-readable origin of the file, used in {@link PickupLoadIssue#source()}.
   * @return whatever could be validly parsed, plus a list of issues encountered. Never throws for
   *         malformed content - a top-level parse failure results in an empty pickup list and a
   *         single issue describing the problem.
   */
  public ParsedFile parse(String json, String source) {
    List<PickupLoadIssue> issues = new ArrayList<>();
    JsonNode root;
    try {
      root = MAPPER.readTree(json);
    } catch (Exception e) {
      issues.add(new PickupLoadIssue(source, null, null, "could not parse JSON: " + e.getMessage()));
      return new ParsedFile(null, null, List.of(), issues);
    }
    if (root == null || root.isMissingNode() || root.isNull()) {
      issues.add(new PickupLoadIssue(source, null, null, "file is empty"));
      return new ParsedFile(null, null, List.of(), issues);
    }

    Integer schemaVersion = intField(root, "schemaVersion", "schemaVersion", issues, source, null);
    if (schemaVersion == null) {
      issues.add(new PickupLoadIssue(source, null, "schemaVersion", "missing or not an integer"));
    } else if (schemaVersion > SUPPORTED_SCHEMA_VERSION) {
      issues.add(new PickupLoadIssue(source, null, "schemaVersion",
          "schemaVersion " + schemaVersion + " is newer than supported version "
              + SUPPORTED_SCHEMA_VERSION + "; some fields may be ignored"));
    }

    String libraryId = textField(root, "libraryId");

    List<PickupDefinition> pickups = new ArrayList<>();
    JsonNode pickupsNode = field(root, "pickups");
    if (pickupsNode == null) {
      issues.add(new PickupLoadIssue(source, null, "pickups", "missing 'pickups' array"));
      return new ParsedFile(schemaVersion, libraryId, pickups, issues);
    }
    if (!pickupsNode.isArray()) {
      issues.add(new PickupLoadIssue(source, null, "pickups", "expected an array"));
      return new ParsedFile(schemaVersion, libraryId, pickups, issues);
    }

    for (int i = 0; i < pickupsNode.size(); i++) {
      JsonNode entry = pickupsNode.get(i);
      String path = "pickups[" + i + "]";
      PickupDefinition definition = parsePickup(entry, path, source, issues);
      if (definition != null) {
        pickups.add(definition);
      }
    }

    return new ParsedFile(schemaVersion, libraryId, pickups, issues);
  }

  private PickupDefinition parsePickup(JsonNode node, String path, String source,
      List<PickupLoadIssue> issues) {
    if (node == null || !node.isObject()) {
      issues.add(new PickupLoadIssue(source, null, path, "expected an object"));
      return null;
    }

    String id = textField(node, "id");
    String manufacturer = textField(node, "manufacturer");
    String model = textField(node, "model");
    PickupFormat format = enumField(PickupFormat.class, node, "format", path + ".format", issues, source, id);

    boolean missingIdentity = false;
    if (isBlank(id)) {
      issues.add(new PickupLoadIssue(source, id, path + ".id", "is required"));
      missingIdentity = true;
    }
    if (isBlank(manufacturer)) {
      issues.add(new PickupLoadIssue(source, id, path + ".manufacturer", "is required"));
      missingIdentity = true;
    }
    if (isBlank(model)) {
      issues.add(new PickupLoadIssue(source, id, path + ".model", "is required"));
      missingIdentity = true;
    }
    if (format == null) {
      // enumField already logged an issue if the value was present-but-invalid; log a distinct
      // one here only if the field was absent altogether.
      if (field(node, "format") == null) {
        issues.add(new PickupLoadIssue(source, id, path + ".format", "is required"));
      }
      missingIdentity = true;
    }
    if (missingIdentity) {
      return null;
    }

    Integer definitionVersion = intField(node, "definitionVersion", path + ".definitionVersion", issues, source, id);
    String variant = textField(node, "variant");
    Boolean active = boolField(node, "active", path + ".active", issues, source, id);
    Integer stringCount = intField(node, "stringCount", path + ".stringCount", issues, source, id);

    List<CoilDefinition> coils = new ArrayList<>();
    JsonNode coilsNode = field(node, "coils");
    if (coilsNode != null && coilsNode.isArray()) {
      for (int i = 0; i < coilsNode.size(); i++) {
        CoilDefinition coil =
            parseCoil(coilsNode.get(i), path + ".coils[" + i + "]", source, id, issues);
        if (coil != null) {
          coils.add(coil);
        }
      }
    }

    List<TerminalDefinition> terminals = new ArrayList<>();
    JsonNode terminalsNode = field(node, "terminals");
    if (terminalsNode != null && terminalsNode.isArray()) {
      for (int i = 0; i < terminalsNode.size(); i++) {
        TerminalDefinition terminal =
            parseTerminal(terminalsNode.get(i), path + ".terminals[" + i + "]", source, id, issues);
        if (terminal != null) {
          terminals.add(terminal);
        }
      }
    }

    ElectricalSpec electrical = parseElectrical(field(node, "electrical"), path + ".electrical", source, id, issues);
    MagnetSpec magnet = parseMagnet(field(node, "magnet"), source, id);
    PhysicalSpec physical = parsePhysical(field(node, "physical"), path + ".physical", source, id, issues);

    List<SourceReference> sources = new ArrayList<>();
    JsonNode sourcesNode = field(node, "sources");
    if (sourcesNode != null && sourcesNode.isArray()) {
      for (int i = 0; i < sourcesNode.size(); i++) {
        SourceReference ref =
            parseSource(sourcesNode.get(i), path + ".sources[" + i + "]", source, id, issues);
        if (ref != null) {
          sources.add(ref);
        }
      }
    }

    try {
      return new PickupDefinition(id, definitionVersion, manufacturer, model, variant, format, active,
          stringCount, coils, terminals, electrical, magnet, physical, sources);
    } catch (Exception e) {
      issues.add(new PickupLoadIssue(source, id, path, "could not build definition: " + e.getMessage()));
      return null;
    }
  }

  private CoilDefinition parseCoil(JsonNode node, String path, String source, String pickupId,
      List<PickupLoadIssue> issues) {
    if (node == null || !node.isObject()) {
      issues.add(new PickupLoadIssue(source, pickupId, path, "expected an object"));
      return null;
    }
    String id = textField(node, "id");
    if (isBlank(id)) {
      issues.add(new PickupLoadIssue(source, pickupId, path + ".id", "is required; coil skipped"));
      return null;
    }
    CoilSide localSide = enumField(CoilSide.class, node, "localSide", path + ".localSide", issues, source, pickupId);
    CoilPolePieceType polePieceType =
        enumField(CoilPolePieceType.class, node, "polePieceType", path + ".polePieceType", issues, source, pickupId);
    MagneticPolarity magneticPolarity = enumField(MagneticPolarity.class, node, "magneticPolarity",
        path + ".magneticPolarity", issues, source, pickupId);
    WindingDirection windingDirection = enumField(WindingDirection.class, node, "windingDirection",
        path + ".windingDirection", issues, source, pickupId);
    Measurement dcResistance = measurementField(node, "dcResistance", path, issues, source, pickupId);
    Measurement inductance = measurementField(node, "inductance", path, issues, source, pickupId);
    return new CoilDefinition(id, localSide, polePieceType, magneticPolarity, windingDirection, dcResistance,
        inductance);
  }

  private TerminalDefinition parseTerminal(JsonNode node, String path, String source, String pickupId,
      List<PickupLoadIssue> issues) {
    if (node == null || !node.isObject()) {
      issues.add(new PickupLoadIssue(source, pickupId, path, "expected an object"));
      return null;
    }
    String id = textField(node, "id");
    if (isBlank(id)) {
      issues.add(new PickupLoadIssue(source, pickupId, path + ".id", "is required; terminal skipped"));
      return null;
    }
    String coilId = textField(node, "coilId");
    TerminalRole role = enumField(TerminalRole.class, node, "role", path + ".role", issues, source, pickupId);
    LeadSpec lead = parseLead(field(node, "lead"), path + ".lead", source, pickupId, issues);
    return new TerminalDefinition(id, coilId, role, lead);
  }

  private LeadSpec parseLead(JsonNode node, String path, String source, String pickupId,
      List<PickupLoadIssue> issues) {
    if (node == null) {
      return null;
    }
    LeadType type = enumField(LeadType.class, node, "type", path + ".type", issues, source, pickupId);
    String displayName = textField(node, "displayName");
    String baseColour = validateHexColor(textField(node, "baseColour"), path + ".baseColour", source, pickupId, issues);
    String stripeColour =
        validateHexColor(textField(node, "stripeColour"), path + ".stripeColour", source, pickupId, issues);
    return new LeadSpec(type == null ? LeadType.UNKNOWN : type, displayName, baseColour, stripeColour);
  }

  private String validateHexColor(String value, String path, String source, String pickupId,
      List<PickupLoadIssue> issues) {
    if (value == null) {
      return null;
    }
    try {
      LeadSpec.parseHexColor(value);
      return value;
    } catch (Exception e) {
      issues.add(new PickupLoadIssue(source, pickupId, path, "not a valid '#RRGGBB' colour: '" + value + "'"));
      return null;
    }
  }

  private ElectricalSpec parseElectrical(JsonNode node, String path, String source, String pickupId,
      List<PickupLoadIssue> issues) {
    if (node == null) {
      return null;
    }
    Measurement seriesDcResistance = measurementField(node, "seriesDcResistance", path, issues, source, pickupId);
    Measurement seriesInductance = measurementField(node, "seriesInductance", path, issues, source, pickupId);
    Measurement parasiticCapacitance = measurementField(node, "parasiticCapacitance", path, issues, source, pickupId);
    Measurement outputMillivolts = measurementField(node, "outputMillivolts", path, issues, source, pickupId);
    return new ElectricalSpec(seriesDcResistance, seriesInductance, parasiticCapacitance, outputMillivolts);
  }

  private MagnetSpec parseMagnet(JsonNode node, String source, String pickupId) {
    if (node == null) {
      return null;
    }
    return new MagnetSpec(textField(node, "material"), textField(node, "grade"));
  }

  private PhysicalSpec parsePhysical(JsonNode node, String path, String source, String pickupId,
      List<PickupLoadIssue> issues) {
    if (node == null) {
      return null;
    }
    Measurement width = measurementField(node, "width", path, issues, source, pickupId);
    Measurement length = measurementField(node, "length", path, issues, source, pickupId);
    Measurement depth = measurementField(node, "depth", path, issues, source, pickupId);
    Measurement poleSpacing = measurementField(node, "poleSpacing", path, issues, source, pickupId);
    MountingLeg mountingLeg =
        enumField(MountingLeg.class, node, "mountingLeg", path + ".mountingLeg", issues, source, pickupId);
    return new PhysicalSpec(width, length, depth, poleSpacing, mountingLeg);
  }

  private SourceReference parseSource(JsonNode node, String path, String source, String pickupId,
      List<PickupLoadIssue> issues) {
    if (node == null || !node.isObject()) {
      issues.add(new PickupLoadIssue(source, pickupId, path, "expected an object"));
      return null;
    }
    SourceType type = enumField(SourceType.class, node, "type", path + ".type", issues, source, pickupId);
    String reference = textField(node, "reference");
    return new SourceReference(type == null ? SourceType.OTHER : type, reference);
  }

  // ---- generic JsonNode helpers -------------------------------------------------------------

  private static JsonNode field(JsonNode node, String name) {
    if (node == null) {
      return null;
    }
    JsonNode f = node.get(name);
    return (f == null || f.isNull() || f.isMissingNode()) ? null : f;
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private static String textField(JsonNode node, String name) {
    JsonNode f = field(node, name);
    return f == null ? null : f.asText();
  }

  private static Integer intField(JsonNode node, String name, String path, List<PickupLoadIssue> issues,
      String source, String pickupId) {
    JsonNode f = field(node, name);
    if (f == null) {
      return null;
    }
    if (!f.isNumber()) {
      issues.add(new PickupLoadIssue(source, pickupId, path, "expected an integer but got: " + f));
      return null;
    }
    return f.asInt();
  }

  private static Boolean boolField(JsonNode node, String name, String path, List<PickupLoadIssue> issues,
      String source, String pickupId) {
    JsonNode f = field(node, name);
    if (f == null) {
      return null;
    }
    if (!f.isBoolean()) {
      issues.add(new PickupLoadIssue(source, pickupId, path, "expected a boolean but got: " + f));
      return null;
    }
    return f.asBoolean();
  }

  private static <E extends Enum<E>> E enumField(Class<E> type, JsonNode node, String name, String path,
      List<PickupLoadIssue> issues, String source, String pickupId) {
    String text = textField(node, name);
    if (text == null) {
      return null;
    }
    try {
      return Enum.valueOf(type, text.trim());
    } catch (IllegalArgumentException e) {
      issues.add(new PickupLoadIssue(source, pickupId, path,
          "unknown value '" + text + "' for " + type.getSimpleName()));
      return null;
    }
  }

  private static Measurement measurementField(JsonNode parent, String name, String parentPath,
      List<PickupLoadIssue> issues, String source, String pickupId) {
    JsonNode node = field(parent, name);
    if (node == null) {
      return null;
    }
    String path = parentPath + "." + name;
    JsonNode valueNode = field(node, "value");
    if (valueNode == null || !valueNode.isNumber()) {
      issues.add(new PickupLoadIssue(source, pickupId, path + ".value", "must be a numeric value"));
      return null;
    }
    String unitText = textField(node, "unit");
    if (unitText == null) {
      issues.add(new PickupLoadIssue(source, pickupId, path + ".unit", "is required when '" + name + "' is present"));
      return null;
    }
    MeasurementUnit unit;
    try {
      unit = MeasurementUnit.valueOf(unitText.trim());
    } catch (IllegalArgumentException e) {
      issues.add(new PickupLoadIssue(source, pickupId, path + ".unit", "unknown unit '" + unitText + "'"));
      return null;
    }
    return new Measurement(valueNode.asDouble(), unit);
  }
}
