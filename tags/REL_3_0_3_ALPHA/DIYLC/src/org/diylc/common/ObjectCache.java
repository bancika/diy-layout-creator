package org.diylc.common;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for object that may be reused, such as {@link Stroke} or
 * {@link AlphaComposite}.
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

	private ObjectCache() {
	}

	private Map<Integer, Stroke> basicStrokeMap = new HashMap<Integer, Stroke>();

	public Stroke fetchBasicStroke(int width) {
		if (basicStrokeMap.containsKey(width)) {
			return basicStrokeMap.get(width);
		}
		Stroke stroke = new BasicStroke(width);
		basicStrokeMap.put(width, stroke);
		return stroke;
	}
}
