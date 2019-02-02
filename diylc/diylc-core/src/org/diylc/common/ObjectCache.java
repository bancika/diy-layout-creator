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
 */
package org.diylc.common;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for object that may be reused, such as {@link Stroke} or {@link AlphaComposite}.
 * 
 * @author Branislav Stojkovic
 */
public class ObjectCache {

  private static ObjectCache instance;

  public static ObjectCache getInstance() {
    if (instance == null) {
      instance = new ObjectCache();
    }
    return instance;
  }

  private ObjectCache() {}

  private Map<Float, Stroke> basicStrokeMap = new HashMap<Float, Stroke>();
  private Map<Float, Stroke> zoomableStrokeMap = new HashMap<Float, Stroke>();
  private Map<String, Stroke> dashStrokeMap = new HashMap<String, Stroke>();

  public Stroke fetchBasicStroke(float width) {
    if (basicStrokeMap.containsKey(width)) {
      return basicStrokeMap.get(width);
    }
    Stroke stroke = new BasicStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    basicStrokeMap.put(width, stroke);
    return stroke;
  }
  
  public Stroke fetchZoomableStroke(float width) {
    if (zoomableStrokeMap.containsKey(width)) {
      return zoomableStrokeMap.get(width);
    }
    Stroke stroke = new ZoomableStroke(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    zoomableStrokeMap.put(width, stroke);
    return stroke;
  }

  public Stroke fetchStroke(float width, float[] dash, float phase, int cap) {
    String key = width + "|" + Arrays.toString(dash) + "|" + phase + "|" + phase;
    if (dashStrokeMap.containsKey(key)) {
      return dashStrokeMap.get(key);
    }
    Stroke stroke = new BasicStroke(width, cap, BasicStroke.JOIN_ROUND, 0, dash, phase);
    dashStrokeMap.put(key, stroke);
    return stroke;
  }
}
