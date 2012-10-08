package org.diylc.components.semiconductors;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.common.IPlugInPort;
import org.diylc.components.AbstractLeadedComponent;
import org.diylc.core.CreationMethod;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Theme;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Diode (plastic)", author = "Branislav Stojkovic", category = "Semiconductors", creationMethod = CreationMethod.POINT_BY_POINT, instanceNamePrefix = "D", description = "Plastic diode, like most rectifier, zener, schottky, etc.", zOrder = IDIYComponent.COMPONENT)
public class DiodePlastic extends AbstractLeadedComponent<String> {

	private static final long serialVersionUID = 1L;

	public static Size DEFAULT_WIDTH = new Size(1d / 4, SizeUnit.in);
	public static Size DEFAULT_HEIGHT = new Size(1d / 8, SizeUnit.in);
	public static Size MARKER_WIDTH = new Size(1d, SizeUnit.mm);
	public static Color BODY_COLOR = Color.darkGray;
	public static Color MARKER_COLOR = Color.decode("#DDDDDD");
	public static Color LABEL_COLOR = Color.white;
	public static Color BORDER_COLOR = BODY_COLOR.darker();

	private String value = "";
	private Color markerColor = MARKER_COLOR;		

	public DiodePlastic() {
		super();
		this.labelColor = LABEL_COLOR;
		this.bodyColor = BODY_COLOR;
		this.borderColor = BORDER_COLOR;
	}

	@Override
	protected boolean supportsStandingMode() {
		return true;
	}

	@Override
	public Color getStandingBodyColor() {
		return getFlipStanding() ? getBodyColor() : getMarkerColor();
	}

	@EditableProperty(name = "Reverse (standing)")
	public boolean getFlipStanding() {
		return super.getFlipStanding();
	}

	@EditableProperty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.rotate(-Math.PI / 4, width / 2, height / 2);
		g2d.setColor(LEAD_COLOR);
		g2d.drawLine(0, height / 2, width, height / 2);
		g2d.setColor(BODY_COLOR);
		g2d.fillRect(6, height / 2 - 3, width - 12, 6);
		g2d.setColor(MARKER_COLOR);
		int markerWidth = 4 * width / 32;
		g2d.fillRect(width - 6 - markerWidth, height / 2 - 3, markerWidth, 6);
		g2d.setColor(BORDER_COLOR);
		g2d.drawRect(6, height / 2 - 3, width - 12, 6);
	}

	@Override
	protected Size getDefaultWidth() {
		return DEFAULT_HEIGHT;
	}

	@Override
	protected Size getDefaultLength() {
		return DEFAULT_WIDTH;
	}

	@Override
	protected Shape getBodyShape() {
		return new Rectangle2D.Double(0f, 0f, getLength().convertToPixels(),
				getClosestOdd(getWidth().convertToPixels()));
	}

	@Override
	protected void decorateComponentBody(Graphics2D g2d, boolean outlineMode) {
		Color finalMarkerColor;
		if (outlineMode) {
			Theme theme = (Theme) ConfigurationManager.getInstance()
					.readObject(IPlugInPort.THEME_KEY, Constants.DEFAULT_THEME);
			finalMarkerColor = theme.getOutlineColor();
		} else {
			finalMarkerColor = markerColor;
		}
		g2d.setColor(finalMarkerColor);
		int width = (int) getLength().convertToPixels();
		int markerWidth = (int) MARKER_WIDTH.convertToPixels();
		g2d.fillRect(width - markerWidth, 0, markerWidth,
				getClosestOdd(getWidth().convertToPixels()));
	}

	@EditableProperty(name = "Marker")
	public Color getMarkerColor() {
		return markerColor;
	}

	public void setMarkerColor(Color markerColor) {
		this.markerColor = markerColor;
	}
}
