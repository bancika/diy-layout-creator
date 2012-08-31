package org.diylc.swing.plugins.file;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.diylc.common.DrawOption;
import org.diylc.common.IPlugInPort;
import org.diylc.common.PCBLayer;
import org.diylc.core.IDIYComponent;
import org.diylc.presenter.PCBLayerFiler;
import org.diylc.swingframework.IDrawingProvider;

/**
 * {@link IDrawingProvider} implementation that uses {@link IPlugInPort} to draw
 * a project onto the canvas.
 * 
 * @author Branislav Stojkovic
 */
public class TraceMaskDrawingProvider implements IDrawingProvider {

	private IPlugInPort plugInPort;

	public TraceMaskDrawingProvider(IPlugInPort plugInPort) {
		super();
		this.plugInPort = plugInPort;
	}

	@Override
	public Dimension getSize() {
		return plugInPort.getCanvasDimensions(false);
	}

	@Override
	public void draw(int page, Graphics g) {
		plugInPort.draw((Graphics2D) g, EnumSet.of(DrawOption.ANTIALIASING),
				new PCBLayerFiler(getUsedLayers()[page]));
	}

	@Override
	public int getPageCount() {
		return getUsedLayers().length;
	}

	private PCBLayer[] getUsedLayers() {
		Set<PCBLayer> layers = EnumSet.noneOf(PCBLayer.class);
		for (IDIYComponent<?> c : plugInPort.getCurrentProject()
				.getComponents()) {
			Class<?> clazz = c.getClass();
			try {
				Method m = clazz.getMethod("getLayer");
				PCBLayer l = (PCBLayer) m.invoke(c);
				layers.add(l);
			} catch (Exception e) {
			}
		}
		List<PCBLayer> sorted = new ArrayList<PCBLayer>(layers);
		Collections.sort(sorted);
		return sorted.toArray(new PCBLayer[] {});
	}
}
