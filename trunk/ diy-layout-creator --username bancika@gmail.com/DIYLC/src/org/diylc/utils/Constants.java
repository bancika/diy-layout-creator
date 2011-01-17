package org.diylc.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;

public class Constants {

	public static final int PIXELS_PER_INCH = 150;//Toolkit.getDefaultToolkit().getScreenResolution();

	public static Color CANVAS_COLOR = Color.white;
	public static Color GRID_COLOR = new Color(240, 240, 240);
	public static Color CONTROL_POINT_COLOR = Color.black;
	public static Color SELECTED_CONTROL_POINT_COLOR = Color.blue;

	public static Font LABEL_FONT = new Font("Tahoma", Font.PLAIN, 12);

	public static final Stroke BASIC_STROKE = new BasicStroke();
	public static final Stroke DASHED_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 2.0f, new float[] { 2.0f }, 0.0f);
}
