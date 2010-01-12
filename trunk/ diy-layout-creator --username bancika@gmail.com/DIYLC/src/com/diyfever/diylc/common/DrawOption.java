package com.diyfever.diylc.common;

import java.awt.Graphics;

/**
 * Enumerates all options that can be used when drawing a project.
 * 
 * @see IPlugInPort#draw(java.awt.Graphics2D, java.util.EnumSet)
 * 
 * @author Branislav Stojkovic
 */
public enum DrawOption {

	/**
	 * Selection rectangle will be drawn when needed and selected components may
	 * be rendered differently.
	 */
	SELECTION,
	/**
	 * Selected zoom level will be applied to scale the {@link Graphics} before
	 * drawing.
	 */
	ZOOM,
	/**
	 * Grid lines are drawn.
	 */
	GRID,
	/**
	 * Control points are drawn.
	 */
	CONTROL_POINTS,
	/**
	 * Anti-aliasing is used when drawing.
	 */
	ANTIALIASING;
}
