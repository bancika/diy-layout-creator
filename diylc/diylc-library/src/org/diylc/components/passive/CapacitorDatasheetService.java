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

import java.awt.Color;
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

public class CapacitorDatasheetService {

  private static final double TOLERANCE = 10d;

  private static CapacitorDatasheetService instance;

  private static NumberFormat format = new DecimalFormat("##.###");

  private Map<String, CapacitorDatasheet> cache = new HashMap<String, CapacitorDatasheet>();

  public static CapacitorDatasheetService getInstance() {
    if (instance == null) {
      instance = new CapacitorDatasheetService();
    }
    return instance;
  }

  public CapacitorDatasheet lookup(Class<?> clazz, String key, Voltage voltage, Capacitance value) {
    return lookup(clazz, key, voltage, value, null);
  }

  public CapacitorDatasheet lookup(Class<?> clazz, String key, Voltage voltage, Capacitance value,
      Boolean polarized) {

    String lookupKey = key + "|" + format.format(voltage.getNormalizedValue()) + " VDC"
        + (Optional.ofNullable(polarized).map(p -> p ? "" : " NP").orElse(""));

    String cacheKey = clazz.getName() + lookupKey + "|" + format.format(value.getNormalizedValue());
    
    boolean isRadial = clazz.getName().contains("Radial");

    return cache.computeIfAbsent(cacheKey, k -> {
      String[] lookup = DatasheetService.getInstance().lookup(clazz, TOLERANCE, lookupKey,
          value.getNormalizedValue());

      if (lookup != null) {
        return parseCapacitorDatasheet(isRadial, lookup);
      }
      return null;
    });
  }

  public static CapacitorDatasheet parseCapacitorDatasheet(boolean isRadial, String[] lookup) {
    Size length = new Size(Double.parseDouble(lookup[3]), SizeUnit.mm);
    Size width = new Size(Double.parseDouble(lookup[4]), SizeUnit.mm);
    
    int i = 5;
    Size leadSpacing = null;
    if (isRadial && lookup[5].length() > 0) {
      leadSpacing = new Size(Double.parseDouble(lookup[i]), SizeUnit.mm);
      i++;
    }
    
    Color bodyColor = null;
    if (lookup.length > i && lookup[i].length() == 7)
      bodyColor = Color.decode(lookup[i]);
    i++;
    Color borderColor = null;
    if (lookup.length > i && lookup[i].length() == 7)
      borderColor = Color.decode(lookup[i]);
    i++;
    Color labelColor = null;
    if (lookup.length > i && lookup[i].length() == 7)
      labelColor = Color.decode(lookup[i]);
    i++;
    Color markerColor = null;
    if (lookup.length > i && lookup[i].length() == 7)
      markerColor = Color.decode(lookup[i]);
    i++;
    Color tickColor = null;
    if (lookup.length > i && lookup[i].length() == 7)
      tickColor = Color.decode(lookup[i]);
    
    return new CapacitorDatasheet(length, width, leadSpacing, bodyColor, borderColor, labelColor, markerColor, tickColor);
  }
  
  public static class CapacitorDatasheet {
    private Size length;
    private Size width;
    private Color bodyColor;
    private Color borderColor;
    private Color labelColor;
    private Color markerColor;
    private Color tickColor;
    private Size leadSpacing;     
    
    public CapacitorDatasheet(Size length, Size width, Size leadSpacing, Color bodyColor,
        Color borderColor, Color labelColor, Color markerColor, Color tickColor) {
      super();
      this.length = length;
      this.width = width;
      this.leadSpacing = leadSpacing;
      this.bodyColor = bodyColor;
      this.borderColor = borderColor;
      this.labelColor = labelColor;
      this.markerColor = markerColor;
      this.tickColor = tickColor;
    }

    public CapacitorDatasheet(Size length, Size diameter) {
      super();
      this.length = length;
      this.width = diameter;
    }

    public Size getLength() {
      return length;
    }

    public Size getWidth() {
      return width;
    }
    
    public Size getLeadSpacing() {
      return leadSpacing;
    }

    public Color getBodyColor() {
      return bodyColor;
    }

    public Color getBorderColor() {
      return borderColor;
    }

    public Color getLabelColor() {
      return labelColor;
    }

    public Color getMarkerColor() {
      return markerColor;
    }

    public Color getTickColor() {
      return tickColor;
    }       
  }
}
