package org.diylc.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.Toolkit;

import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;

public class Constants {

	public static final int PIXELS_PER_INCH = Toolkit.getDefaultToolkit().getScreenResolution();
	public static Size GRID_SIZE = new Size(0.1d, SizeUnit.in);
	public static final int GRID = GRID_SIZE.convertToPixels();

	public static Color CANVAS_COLOR = Color.white;
	public static Color GRID_COLOR = new Color(240, 240, 240);
	public static Color CONTROL_POINT_COLOR = Color.blue;

	public static Font LABEL_FONT = new Font("Tahoma", Font.BOLD, 11);

	public static final Stroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_MITER, 2.0f, new float[] { 2.0f }, 0.0f);
}
