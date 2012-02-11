package org.diylc.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import org.diylc.common.HorizontalAlignment;
import org.diylc.common.VerticalAlignment;
import org.diylc.core.IDIYComponent;
import org.diylc.core.annotations.EditableProperty;

/**
 * Abstract implementation of {@link IDIYComponent} that contains component name
 * and toString.
 * 
 * @author Branislav Stojkovic
 * 
 * @param <T>
 */
public abstract class AbstractComponent<T> implements IDIYComponent<T> {

	private static final long serialVersionUID = 1L;

	protected String name = "";

	public static Color SELECTION_COLOR = Color.red;
	public static Color LABEL_COLOR = Color.black;
	public static Color LABEL_COLOR_SELECTED = Color.red;
	public static Font LABEL_FONT = new Font("Tahoma", Font.PLAIN, 14);

	@EditableProperty(defaultable = false)
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean canControlPointOverlap(int index) {
		return false;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Returns the closest odd number, i.e. x when x is odd, or x + 1 when x is
	 * even.
	 * 
	 * @param x
	 * @return
	 */
	protected int getClosestOdd(double x) {
		return ((int) x / 2) * 2 + 1;
	}

	/**
	 * @param clip
	 * @return true if none of the control points lie in the clip rectangle.
	 */
	protected boolean checkPointsClipped(Shape clip) {
		for (int i = 0; i < getControlPointCount(); i++) {
			if (clip.contains(getControlPoint(i))) {
				return false;
			}
		}
		return true;
	}

	protected void drawCenteredText(Graphics2D g2d, String text, int x, int y,
			HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
		FontMetrics fontMetrics = g2d.getFontMetrics();
		Rectangle stringBounds = fontMetrics.getStringBounds(text, g2d).getBounds();

		Font font = g2d.getFont();
		FontRenderContext renderContext = g2d.getFontRenderContext();
		GlyphVector glyphVector = font.createGlyphVector(renderContext, text);
		Rectangle visualBounds = glyphVector.getVisualBounds().getBounds();

		int textX = 0;
		switch (horizontalAlignment) {
		case CENTER:
			textX = x - stringBounds.width / 2;
			break;
		case LEFT:
			textX = x;
			break;
		case RIGHT:
			textX = x - stringBounds.width;
			break;
		}

		int textY = 0;
		switch (verticalAlignment) {
		case TOP:
			textY = y + stringBounds.height;
			break;
		case CENTER:
			textY = y - visualBounds.height / 2 - visualBounds.y;
			break;
		case BOTTOM:
			textY = y - visualBounds.y;
			break;
		}

		g2d.drawString(text, textX, textY);
	}
}
