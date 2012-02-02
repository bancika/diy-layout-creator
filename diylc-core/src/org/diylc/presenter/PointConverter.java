package org.diylc.presenter;

import java.awt.Point;

import org.diylc.utils.Constants;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Serializes {@link Point} objects by converting coordinates from pixels to
 * inches, thus avoiding the resolution to affect point placement.
 * 
 * @author Branislav Stojkovic
 */
public class PointConverter implements Converter {

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext context) {
		Point point = (Point) object;
		writer.addAttribute("x", Double.toString(1d * point.x / Constants.PIXELS_PER_INCH));
		writer.addAttribute("y", Double.toString(1d * point.y / Constants.PIXELS_PER_INCH));
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		double x = Double.parseDouble(reader.getAttribute("x"));
		double y = Double.parseDouble(reader.getAttribute("y"));
		return new Point((int) Math.round(x * Constants.PIXELS_PER_INCH), (int) Math.round(y
				* Constants.PIXELS_PER_INCH));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean canConvert(Class clazz) {
		return Point.class.isAssignableFrom(clazz);
	}
}
