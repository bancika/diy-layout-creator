package org.diylc.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.Toolkit;

public class Constants {

	public static final int PIXELS_PER_INCH = 150;
	public static final double PIXEL_SIZE = 1d * PIXELS_PER_INCH
			/ Toolkit.getDefaultToolkit().getScreenResolution();

	public static Color CANVAS_COLOR = Color.white;

	public static Font LABEL_FONT = new Font("Tahoma", Font.PLAIN, 12);

	public static final Stroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 10f, new float[] { 2.0f }, 0.0f);
}
