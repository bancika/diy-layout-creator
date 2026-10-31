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
package org.diylc.plugins.chatbot.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import org.diylc.common.ComponentType;
import org.diylc.common.PropertyWrapper;
import org.diylc.core.IDIYComponent;
import org.diylc.core.ISwitch;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.annotations.PinoutDriver;
import org.diylc.plugins.chatbot.model.pinout.AiPin;
import org.diylc.plugins.chatbot.model.pinout.AiPinLabels;
import org.diylc.plugins.chatbot.model.pinout.AiPinout;
import org.diylc.plugins.chatbot.model.pinout.AiPinoutExample;
import org.diylc.plugins.chatbot.model.pinout.AiPinoutVariant;
import org.diylc.plugins.chatbot.model.pinout.AiSwitching;
import org.diylc.plugins.chatbot.model.pinout.AiSwitchingPosition;
import org.diylc.plugins.chatbot.model.pinout.AiTerminals;
import org.diylc.presenter.ComponentProcessor;

/**
 * Works out which terminals a component type exposes, and which of its editable properties change
 * them, by instantiating the component and observing it rather than by reading any declaration.
 * <p>
 * Only sticky control points are reported, matching {@code AiProjectBuilder}, so the indices here
 * are the same indices the model sees for components already on the canvas and the same ones it
 * sends back in a terminal reference.
 *
 * @author Branislav Stojkovic
 */
public class PinoutAnalyzer {

  private static final Logger LOG = Logger.getLogger(PinoutAnalyzer.class);

  /** Upper bound on variants emitted for one component type, to keep the catalog readable. */
  private static final int MAX_VARIANTS = 64;

  /** How many values to try for an integer property that has no enumerable domain. */
  private static final int INT_SAMPLE_COUNT = 3;

  /** Upper bound on the combinations sampled while fitting a terminal count formula. */
  private static final int MAX_NUMERIC_SAMPLES = 32;

  /** How many of those samples are written out as evidence that the formula holds. */
  private static final int MAX_SAMPLE_ROWS = 4;

  /**
   * Upper bound on the switch positions written out in full. A DIP switch reports two to the power
   * of its switch count, which runs into the thousands and says nothing a reader cannot infer.
   */
  private static final int MAX_SWITCH_POSITIONS = 24;

  /** Accessors tried on an enum constant that names a count without spelling it as a number. */
  private static final String[] COUNT_ACCESSORS = new String[] {"getValue", "getCount"};

  private PinoutAnalyzer() {}

  /**
   * @param type component type to analyze
   * @param processor processor used to extract editable properties
   * @return the terminals of the type, or null when it exposes none at all, which is the case for
   *         boards, labels and other purely visual components
   */
  public static AiPinout analyze(ComponentType type, ComponentProcessor processor) {
    return analyze(type.getInstanceClass(), processor);
  }

  /**
   * @param clazz component class to analyze
   * @param processor processor used to extract editable properties
   * @return the terminals of the class, or null when it exposes none at all
   */
  public static AiPinout analyze(Class<? extends IDIYComponent<?>> clazz,
      ComponentProcessor processor) {
    IDIYComponent<?> base = instantiate(clazz);
    if (base == null) {
      return null;
    }
    List<AiPin> basePins = readPins(base);
    if (basePins == null || basePins.isEmpty()) {
      return null;
    }

    List<Driver> drivers = findDrivers(clazz, base, basePins, processor);
    if (drivers.isEmpty()) {
      return AiPinout.fixed(describe(base));
    }

    // A switch is described by its switching table as much as by its terminals, and that table
    // does not follow a formula, so every driver of a switch is enumerated.
    boolean collapseNumeric = !(base instanceof ISwitch);
    List<Driver> numeric = new ArrayList<Driver>();
    List<Driver> finite = new ArrayList<Driver>();
    for (Driver driver : drivers) {
      if (collapseNumeric && driver.isNumeric()) {
        numeric.add(driver);
      } else {
        finite.add(driver);
      }
    }

    List<Driver> heldAtDefault = trimToCap(finite);

    List<AiPinoutVariant> variants = buildVariants(clazz, finite, numeric);
    if (variants == null) {
      // No formula fits the observed counts, so fall back to enumerating everything.
      finite.addAll(numeric);
      numeric.clear();
      heldAtDefault.addAll(trimToCap(finite));
      variants = buildVariants(clazz, finite, numeric);
    }
    if (variants == null || variants.isEmpty()) {
      return AiPinout.fixed(describe(base));
    }

    List<String> driverNames = names(drivers);
    List<String> heldNames = heldAtDefault.isEmpty() ? null : names(heldAtDefault);
    if (variants.size() == 1 && variants.getFirst().when() == null) {
      return AiPinout.uniform(driverNames, heldNames, variants.getFirst().terminals());
    }
    return AiPinout.varying(driverNames, heldNames, variants);
  }

  // ----------------------------------------------------------------- driver discovery

  private static List<Driver> findDrivers(Class<? extends IDIYComponent<?>> clazz,
      IDIYComponent<?> base, List<AiPin> basePins, ComponentProcessor processor) {
    List<Driver> drivers = new ArrayList<Driver>();
    for (PropertyWrapper property : processor.extractProperties(clazz)) {
      if (property.isReadOnly() || property.getDynamicPropertySource() != null) {
        continue;
      }
      PinoutDriver mode = readMode(property, base);
      if (mode == PinoutDriver.EXCLUDE) {
        continue;
      }
      List<Object> values = domain(property, base);
      if (values.isEmpty()) {
        continue;
      }
      if (mode != PinoutDriver.FORCE && !changesTerminals(clazz, property, values, basePins)) {
        continue;
      }
      drivers.add(new Driver(property, values));
    }
    // extractProperties follows Class.getMethods(), whose order the JVM does not promise, so sort
    // to keep the generated catalog stable from one run to the next
    drivers.sort(Comparator.comparing(Driver::name));
    return drivers;
  }

  /**
   * Sets the property to every value in its domain in turn, each time on a fresh instance, and
   * reports whether any of them moved the terminals. Values whose setter rejects them are ignored.
   */
  private static boolean changesTerminals(Class<? extends IDIYComponent<?>> clazz,
      PropertyWrapper property, List<Object> values, List<AiPin> basePins) {
    for (Object value : values) {
      IDIYComponent<?> instance = instantiate(clazz);
      if (instance == null || !apply(instance, property, value)) {
        continue;
      }
      List<AiPin> pins = readPins(instance);
      if (pins != null && !pins.equals(basePins)) {
        return true;
      }
    }
    return false;
  }

  private static PinoutDriver readMode(PropertyWrapper property, IDIYComponent<?> base) {
    try {
      property.readFrom(base);
      Method getter = property.getGetter();
      EditableProperty annotation = getter.getAnnotation(EditableProperty.class);
      return annotation == null ? PinoutDriver.AUTO : annotation.pinoutDriver();
    } catch (Exception e) {
      return PinoutDriver.AUTO;
    }
  }

  /**
   * @return values worth trying for the property, empty when its type has no domain we can walk
   */
  private static List<Object> domain(PropertyWrapper property, IDIYComponent<?> base) {
    List<Object> values = new ArrayList<Object>();
    Class<?> type = property.getType();
    if (type.isEnum()) {
      for (Object constant : type.getEnumConstants()) {
        values.add(constant);
      }
    } else if (type == Boolean.class || type == boolean.class) {
      values.add(Boolean.FALSE);
      values.add(Boolean.TRUE);
    } else if (type == Integer.class || type == int.class) {
      int start = 1;
      try {
        property.readFrom(base);
        if (property.getValue() instanceof Integer current) {
          start = current;
        }
      } catch (Exception e) {
        // fall back to the arbitrary start
      }
      for (int i = 0; i < INT_SAMPLE_COUNT; i++) {
        values.add(start + i);
      }
    }
    return values;
  }

  // ----------------------------------------------------------------- variant assembly

  /**
   * @return one variant per combination of the finite drivers, or null when a numeric driver was
   *         offered but no formula fits the counts it produces
   */
  private static List<AiPinoutVariant> buildVariants(Class<? extends IDIYComponent<?>> clazz,
      List<Driver> finite, List<Driver> numeric) {
    List<AiPinoutVariant> variants = new ArrayList<AiPinoutVariant>();
    for (Combination combination : combinations(finite)) {
      IDIYComponent<?> instance = instantiate(clazz);
      if (instance == null || !combination.applyTo(instance)) {
        continue;
      }
      AiTerminals terminals =
          numeric.isEmpty() ? describe(instance) : describeComputed(clazz, combination, numeric);
      if (terminals == null) {
        return null;
      }
      variants.add(new AiPinoutVariant(finite.isEmpty() ? null : combination.toWhen(), terminals));
    }
    return variants;
  }

  /** Describes an instance whose terminals are fully determined. */
  private static AiTerminals describe(IDIYComponent<?> instance) {
    List<AiPin> pins = readPins(instance);
    if (pins == null || pins.isEmpty()) {
      return null;
    }
    AiSwitching switching = readSwitching(instance);
    AiPinLabels labels = labelRule(pins);
    return labels == AiPinLabels.CUSTOM ? AiTerminals.listed(pins, switching)
        : AiTerminals.counted(pins.size(), labels, switching);
  }

  /**
   * Describes terminals whose count follows one or more numeric properties, by sampling those
   * properties and fitting a formula to what comes back.
   *
   * @return the description, or null when no formula fits
   */
  private static AiTerminals describeComputed(Class<? extends IDIYComponent<?>> clazz,
      Combination finiteCombination, List<Driver> numeric) {
    List<Sample> samples = collectSamples(clazz, finiteCombination, numeric);
    if (samples.size() < 2) {
      return null;
    }
    String expression = fit(numeric, samples);
    if (expression == null) {
      return null;
    }

    AiPinLabels labels = samples.getFirst().labels();
    for (Sample sample : samples) {
      if (sample.labels() != labels) {
        labels = AiPinLabels.CUSTOM;
        break;
      }
    }
    AiPinoutExample example = null;
    if (labels == AiPinLabels.CUSTOM) {
      Sample first = samples.getFirst();
      example = new AiPinoutExample(first.combination().toWhen(), first.pins());
    }
    return AiTerminals.computed(expression, labels, sampleTable(samples), example);
  }

  private static List<Sample> collectSamples(Class<? extends IDIYComponent<?>> clazz,
      Combination finiteCombination, List<Driver> numeric) {
    List<Sample> samples = new ArrayList<Sample>();
    for (Combination combination : combinations(trimDomains(numeric))) {
      int[] numbers = combination.numbers();
      if (numbers == null) {
        continue;
      }
      IDIYComponent<?> instance = instantiate(clazz);
      if (instance == null || !finiteCombination.applyTo(instance)
          || !combination.applyTo(instance)) {
        continue;
      }
      List<AiPin> pins = readPins(instance);
      if (pins == null || pins.isEmpty()) {
        continue;
      }
      samples.add(new Sample(combination, numbers, pins, labelRule(pins)));
    }
    return samples;
  }

  // ----------------------------------------------------------------- formula fitting

  /**
   * Fits the observed counts to {@code a * n + b} for one numeric driver, or to a product or a sum
   * for two of them, and verifies the fit against every sample before accepting it.
   *
   * @return the formula, or null when nothing fits exactly
   */
  private static String fit(List<Driver> numeric, List<Sample> samples) {
    if (numeric.size() == 1) {
      return fitLinear(numeric.getFirst(), samples);
    }
    if (numeric.size() == 2) {
      String product = fitProduct(numeric, samples);
      return product != null ? product : fitSum(numeric, samples);
    }
    return null;
  }

  private static String fitLinear(Driver driver, List<Sample> samples) {
    Sample first = samples.getFirst();
    for (Sample sample : samples) {
      int step = sample.numberAt(0) - first.numberAt(0);
      if (step == 0) {
        continue;
      }
      int rise = sample.pinCount() - first.pinCount();
      if (rise % step != 0) {
        return null;
      }
      int factor = rise / step;
      int offset = first.pinCount() - factor * first.numberAt(0);
      for (Sample check : samples) {
        if (factor * check.numberAt(0) + offset != check.pinCount()) {
          return null;
        }
      }
      return linear(factor, driver.name(), offset);
    }
    return null;
  }

  private static String fitProduct(List<Driver> numeric, List<Sample> samples) {
    Integer factor = null;
    for (Sample sample : samples) {
      int product = sample.numberAt(0) * sample.numberAt(1);
      if (product == 0 || sample.pinCount() % product != 0) {
        return null;
      }
      int candidate = sample.pinCount() / product;
      if (factor == null) {
        factor = candidate;
      } else if (factor != candidate) {
        return null;
      }
    }
    if (factor == null) {
      return null;
    }
    String body = numeric.getFirst().name() + " * " + numeric.get(1).name();
    return factor == 1 ? body : factor + " * " + body;
  }

  private static String fitSum(List<Driver> numeric, List<Sample> samples) {
    // Solve a * n1 + b * n2 + c from samples that vary one number at a time, then verify.
    Sample origin = samples.getFirst();
    Integer alongFirst = solveStep(samples, origin, 0, 1);
    if (alongFirst == null) {
      return null;
    }
    Integer alongSecond = solveStep(samples, origin, 1, 0);
    if (alongSecond == null) {
      return null;
    }
    int offset = origin.pinCount() - alongFirst * origin.numberAt(0) - alongSecond
        * origin.numberAt(1);
    for (Sample sample : samples) {
      if (alongFirst * sample.numberAt(0) + alongSecond * sample.numberAt(1) + offset != sample
          .pinCount()) {
        return null;
      }
    }
    return linear(alongFirst, numeric.getFirst().name(), 0) + " + "
        + linear(alongSecond, numeric.get(1).name(), offset);
  }

  /**
   * @param moving index of the number allowed to differ from the origin
   * @param fixed index of the number that must match the origin
   * @return how much the count rises per unit of the moving number, null when no sample isolates it
   */
  private static Integer solveStep(List<Sample> samples, Sample origin, int moving, int fixed) {
    for (Sample sample : samples) {
      int step = sample.numberAt(moving) - origin.numberAt(moving);
      if (step == 0 || sample.numberAt(fixed) != origin.numberAt(fixed)) {
        continue;
      }
      int rise = sample.pinCount() - origin.pinCount();
      return rise % step == 0 ? rise / step : null;
    }
    return null;
  }

  private static String linear(int factor, String name, int offset) {
    StringBuilder builder = new StringBuilder();
    builder.append(factor == 1 ? name : factor + " * " + name);
    if (offset > 0) {
      builder.append(" + ").append(offset);
    } else if (offset < 0) {
      builder.append(" - ").append(-offset);
    }
    return builder.toString();
  }

  /**
   * A few worked values, spread across the samples, showing that the formula holds. The full set of
   * legal values is already published as the property's possible values, so it is not repeated.
   */
  private static Map<String, Integer> sampleTable(List<Sample> samples) {
    int step = Math.max(1, (int) Math.ceil((double) samples.size() / MAX_SAMPLE_ROWS));
    Map<String, Integer> table = new LinkedHashMap<String, Integer>();
    for (int i = 0; i < samples.size(); i += step) {
      Sample sample = samples.get(i);
      table.put(String.join(", ", sample.combination().texts()), sample.pinCount());
    }
    return table;
  }

  /** Trims numeric domains so that fitting stays cheap on components driven by two properties. */
  private static List<Driver> trimDomains(List<Driver> numeric) {
    if (count(numeric) <= MAX_NUMERIC_SAMPLES) {
      return numeric;
    }
    List<Driver> trimmed = new ArrayList<Driver>();
    for (Driver driver : numeric) {
      List<Object> values = driver.values();
      trimmed.add(new Driver(driver.property(), List.of(values.getFirst(),
          values.get(values.size() / 2), values.getLast())));
    }
    return trimmed;
  }

  // ----------------------------------------------------------------- switches

  private static AiSwitching readSwitching(IDIYComponent<?> instance) {
    if (!(instance instanceof ISwitch component)) {
      return null;
    }
    try {
      int positionCount = component.getPositionCount();
      if (positionCount <= 0) {
        return null;
      }
      if (positionCount > MAX_SWITCH_POSITIONS) {
        return new AiSwitching(positionCount, null);
      }
      int pointCount = instance.getControlPointCount();
      List<AiSwitchingPosition> positions = new ArrayList<AiSwitchingPosition>();
      for (int position = 0; position < positionCount; position++) {
        List<List<Integer>> connected = new ArrayList<List<Integer>>();
        for (int i = 0; i < pointCount - 1; i++) {
          for (int j = i + 1; j < pointCount; j++) {
            if (component.arePointsConnected(i, j, position)) {
              connected.add(List.of(i, j));
            }
          }
        }
        positions.add(new AiSwitchingPosition(component.getPositionName(position), connected));
      }
      return new AiSwitching(positionCount, positions);
    } catch (Exception e) {
      LOG.debug("Could not read switching table from " + instance.getClass().getName(), e);
      return null;
    }
  }

  // ----------------------------------------------------------------- terminals

  private static List<AiPin> readPins(IDIYComponent<?> instance) {
    try {
      List<AiPin> pins = new ArrayList<AiPin>();
      for (int i = 0; i < instance.getControlPointCount(); i++) {
        if (instance.isControlPointSticky(i)) {
          pins.add(new AiPin(i, instance.getControlPointNodeName(i)));
        }
      }
      return pins;
    } catch (Exception e) {
      LOG.debug("Could not read control points from " + instance.getClass().getName(), e);
      return null;
    }
  }

  private static AiPinLabels labelRule(List<AiPin> pins) {
    boolean sequential = true;
    boolean unnamed = true;
    for (int i = 0; i < pins.size(); i++) {
      AiPin pin = pins.get(i);
      if (pin.index() != i) {
        return AiPinLabels.CUSTOM;
      }
      if (!Integer.toString(i + 1).equals(pin.label())) {
        sequential = false;
      }
      if (pin.label() != null) {
        unnamed = false;
      }
    }
    if (sequential) {
      return AiPinLabels.SEQUENTIAL;
    }
    return unnamed ? AiPinLabels.NONE : AiPinLabels.CUSTOM;
  }

  // ----------------------------------------------------------------- plumbing

  private static IDIYComponent<?> instantiate(Class<? extends IDIYComponent<?>> clazz) {
    try {
      return clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      LOG.debug("Could not instantiate " + clazz.getName(), e);
      return null;
    }
  }

  private static boolean apply(IDIYComponent<?> instance, PropertyWrapper property, Object value) {
    try {
      property.setValue(value);
      property.writeTo(instance);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /** Walks the cartesian product of the driver domains, in driver order. */
  private static List<Combination> combinations(List<Driver> drivers) {
    List<List<Object>> valueLists = new ArrayList<List<Object>>();
    valueLists.add(new ArrayList<Object>());
    for (Driver driver : drivers) {
      List<List<Object>> expanded = new ArrayList<List<Object>>();
      for (List<Object> prefix : valueLists) {
        for (Object value : driver.values()) {
          List<Object> combination = new ArrayList<Object>(prefix);
          combination.add(value);
          expanded.add(combination);
        }
      }
      valueLists = expanded;
    }
    List<Combination> combinations = new ArrayList<Combination>();
    for (List<Object> values : valueLists) {
      combinations.add(new Combination(drivers, values));
    }
    return combinations;
  }

  /**
   * Drops drivers, smallest domain first, until the number of combinations fits the cap. The
   * dropped ones stay at their default value and are reported to the reader.
   */
  private static List<Driver> trimToCap(List<Driver> finite) {
    List<Driver> held = new ArrayList<Driver>();
    while (count(finite) > MAX_VARIANTS && finite.size() > 1) {
      Driver smallest = finite.getFirst();
      for (Driver driver : finite) {
        if (driver.values().size() < smallest.values().size()) {
          smallest = driver;
        }
      }
      finite.remove(smallest);
      held.add(smallest);
    }
    return held;
  }

  private static long count(List<Driver> drivers) {
    long total = 1;
    for (Driver driver : drivers) {
      total *= driver.values().size();
    }
    return total;
  }

  private static List<String> names(List<Driver> drivers) {
    List<String> names = new ArrayList<String>();
    for (Driver driver : drivers) {
      names.add(driver.name());
    }
    return names;
  }

  /** Renders a property value the way the model is expected to send it back. */
  private static String text(Object value) {
    if (value == null) {
      return null;
    }
    return value.getClass().isEnum() ? ((Enum<?>) value).name() : value.toString();
  }

  /**
   * @return the number a value stands for, for enums that name a count either through their string
   *         form or through a getter, null when the value is not numeric
   */
  private static Integer numberOf(Object value) {
    if (value instanceof Integer number) {
      return number;
    }
    if (value == null || !value.getClass().isEnum()) {
      return null;
    }
    try {
      return Integer.valueOf(value.toString().trim());
    } catch (NumberFormatException e) {
      // not spelled as a number, try the usual accessors
    }
    for (String accessor : COUNT_ACCESSORS) {
      try {
        Object result = value.getClass().getMethod(accessor).invoke(value);
        if (result instanceof Integer number) {
          return number;
        }
      } catch (Exception e) {
        // try the next one
      }
    }
    return null;
  }

  /** An editable property that moves the terminals, together with the values worth trying. */
  private record Driver(PropertyWrapper property, List<Object> values) {

    String name() {
      return property.getName();
    }

    /** True when every value in the domain stands for a number the count can be fitted against. */
    boolean isNumeric() {
      for (Object value : values) {
        if (numberOf(value) == null) {
          return false;
        }
      }
      return !values.isEmpty();
    }
  }

  /** One value for each of a list of drivers, in driver order. */
  private record Combination(List<Driver> drivers, List<Object> values) {

    boolean applyTo(IDIYComponent<?> instance) {
      for (int i = 0; i < drivers.size(); i++) {
        if (!apply(instance, drivers.get(i).property(), values.get(i))) {
          return false;
        }
      }
      return true;
    }

    Map<String, String> toWhen() {
      Map<String, String> when = new LinkedHashMap<String, String>();
      for (int i = 0; i < drivers.size(); i++) {
        when.put(drivers.get(i).name(), text(values.get(i)));
      }
      return when;
    }

    List<String> texts() {
      List<String> texts = new ArrayList<String>();
      for (Object value : values) {
        texts.add(text(value));
      }
      return texts;
    }

    /** @return the numbers these values stand for, null when any of them stands for none */
    int[] numbers() {
      int[] numbers = new int[values.size()];
      for (int i = 0; i < values.size(); i++) {
        Integer number = numberOf(values.get(i));
        if (number == null) {
          return null;
        }
        numbers[i] = number;
      }
      return numbers;
    }
  }

  /** What one combination of the numeric drivers produced, as evidence for the formula. */
  private record Sample(Combination combination, int[] numbers, List<AiPin> pins,
      AiPinLabels labels) {

    int numberAt(int index) {
      return numbers[index];
    }

    int pinCount() {
      return pins.size();
    }
  }
}
