package org.diylc.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Toolkit;

import org.diylc.core.Theme;

public class Constants {

	public static final int PIXELS_PER_INCH = 200;
	public static final double PIXEL_SIZE = 1d * PIXELS_PER_INCH
			/ Toolkit.getDefaultToolkit().getScreenResolution();

	public static Color CANVAS_COLOR = Color.white;

	public static final Stroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10f, new float[] { 2.0f }, 0.0f);

	public static final Theme DEFAULT_THEME = new Theme("Light", Constants.CANVAS_COLOR, new Color(
			240, 240, 240), Color.black);

	public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);
	
	public static final Color MULTI_VALUE_COLOR = Color.yellow;
}
