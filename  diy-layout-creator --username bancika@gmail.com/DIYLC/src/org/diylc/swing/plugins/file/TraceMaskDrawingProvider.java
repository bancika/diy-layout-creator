package org.diylc.swing.plugins.file;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EnumSet;

import org.diylc.common.DrawOption;
import org.diylc.common.IComponentFiler;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.ComponentZOrderFiler;

import com.diyfever.gui.IDrawingProvider;

/**
 * {@link IDrawingProvider} implementation that uses {@link IPlugInPort} to draw
 * a project onto the canvas.
 * 
 * @author Branislav Stojkovic
 */
public class TraceMaskDrawingProvider implements IDrawingProvider {

	private IPlugInPort plugInPort;
	private IComponentFiler filter;

	public TraceMaskDrawingProvider(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
		this.filter = new ComponentZOrderFiler(IDIYComponent.ABOVE_BOARD);
	}

	@Override
	public void draw(Graphics g) {
		plugInPort.draw((Graphics2D) g, EnumSet.of(DrawOption.ANTIALIASING),
				this.filter);
	}

	@Override
	public Dimension getSize() {
		return plugInPort.getCanvasDimensions(false);
	}
}
