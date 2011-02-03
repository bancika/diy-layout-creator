package org.diylc.presenter;

import java.awt.Point;

import org.diylc.core.measures.Size;

public class CalcUtils {

	/**
	 * Rounds the number to the closest grid line.
	 * 
	 * @param x
	 * @return
	 */
	public static int roundToGrid(int x, Size gridSpacing) {
		double grid = gridSpacing.convertToPixels();
		return (int) (Math.round(1f * x / grid) * grid);
	}

	public static void snapPointToGrid(Point point, Size gridSpacing) {
		int x = roundToGrid(point.x, gridSpacing);
		int y = roundToGrid(point.y, gridSpacing);
		point.setLocation(x, y);
	}
}
