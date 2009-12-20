package com.diyfever.diylc.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.Toolkit;

public class Constants {

	public static final float GRID = Toolkit.getDefaultToolkit()
			.getScreenResolution() / 10f;

	public static final Color GRID_COLOR = new Color(240, 240, 240);

	public static final Stroke dashedStroke = new BasicStroke(1.0f,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 2.0f,
			new float[] { 2.0f }, 0.0f);
}
