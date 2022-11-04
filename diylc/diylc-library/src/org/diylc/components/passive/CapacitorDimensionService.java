/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
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
 * 
 */
package org.diylc.components.passive;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.diylc.core.measures.Capacitance;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.core.measures.Voltage;
import org.diylc.presenter.DatasheetService;

public class CapacitorDimensionService {

  private static final double TOLERANCE = 10d;

  private static CapacitorDimensionService instance;

  private static NumberFormat format = new DecimalFormat("##.###");

  private Map<String, CapacitorDimensions> cache = new HashMap<String, CapacitorDimensions>();

  public static CapacitorDimensionService getInstance() {
    if (instance == null) {
      instance = new CapacitorDimensionService();
    }
    return instance;
  }

  public CapacitorDimensions lookup(Class<?> clazz, String key, Voltage voltage, Capacitance value) {
    return lookup(clazz, key, voltage, value, null);
  }

  public CapacitorDimensions lookup(Class<?> clazz, String key, Voltage voltage, Capacitance value,
      Boolean polarized) {

    String lookupKey = key + "|" + format.format(voltage.getNormalizedValue()) + " VDC"
        + (Optional.ofNullable(polarized).map(p -> p ? "" : " NP").orElse(""));

    String cacheKey = clazz.getName() + lookupKey + "|" + format.format(value.getNormalizedValue());

    return cache.computeIfAbsent(cacheKey, k -> {
      String[] lookup = DatasheetService.getInstance().lookup(clazz, TOLERANCE, lookupKey,
          value.getNormalizedValue());

      if (lookup != null) {
        Size length = new Size(Double.parseDouble(lookup[3]), SizeUnit.mm);
        Size diameter = new Size(Double.parseDouble(lookup[4]), SizeUnit.mm);
        return new CapacitorDimensions(length, diameter);
      }
      return null;
    });
  }
  
  public static class CapacitorDimensions {
    private Size length;
    private Size diameter;
    private Size height;
    
    public CapacitorDimensions(Size length, Size diameter, Size height) {
      super();
      this.length = length;
      this.diameter = diameter;
      this.height = height;
    }

    public CapacitorDimensions(Size length, Size diameter) {
      super();
      this.length = length;
      this.diameter = diameter;
    }

    public Size getLength() {
      return length;
    }

    public Size getDiameter() {
      return diameter;
    }

    public Size getHeight() {
      return height;
    }
  }
}
