package org.diylc.swing.plugins.file;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.EnumSet;

import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;

import com.diyfever.gui.IDrawingProvider;
import com.diyfever.gui.miscutils.ConfigurationManager;

/**
 * {@link IDrawingProvider} implementation that uses {@link IPlugInPort} to draw
 * a project onto the canvas.
 * 
 * @author Branislav Stojkovic
 */
public class ProjectDrawingProvider implements IDrawingProvider {

	private IPlugInPort plugInPort;
	private boolean useZoom;

	public ProjectDrawingProvider(IPlugInPort plugInPort, boolean useZoom) {
		super();
		this.plugInPort = plugInPort;
		this.useZoom = useZoom;
	}

	@Override
	public void draw(Graphics g) {
		EnumSet<DrawOption> options = EnumSet.of(DrawOption.ANTIALIASING);
		if (useZoom) {
			options.add(DrawOption.ZOOM);
		}
		if (ConfigurationManager.getInstance().readBoolean(IPlugInPort.EXPORT_GRID_KEY, false)) {
			options.add(DrawOption.GRID);
		}
		plugInPort.draw((Graphics2D) g, options, null);
	}

	@Override
	public Dimension getSize() {
		return plugInPort.getCanvasDimensions(useZoom);
	}
}
