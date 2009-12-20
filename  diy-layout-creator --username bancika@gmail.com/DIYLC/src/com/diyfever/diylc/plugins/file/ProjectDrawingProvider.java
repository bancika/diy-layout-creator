package com.diyfever.diylc.plugins.file;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EnumSet;

import com.diyfever.diylc.common.DrawOption;
import com.diyfever.diylc.common.IPlugInPort;
import com.diyfever.gui.IDrawingProvider;

/**
 * {@link IDrawingProvider} implementation that uses {@link IPlugInPort} to draw
 * a project onto the canvas.
 * 
 * @author Branislav Stojkovic
 */
public class ProjectDrawingProvider implements IDrawingProvider {

	IPlugInPort plugInPort;

	public ProjectDrawingProvider(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
	}

	@Override
	public void draw(Graphics g) {
		plugInPort.draw((Graphics2D) g, EnumSet.of(DrawOption.ZOOM,
				DrawOption.ANTIALIASING));
	}

	@Override
	public Dimension getSize() {
		return plugInPort.getCanvasDimensions(true);
	}
}
