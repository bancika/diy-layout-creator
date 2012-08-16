package org.diylc.components.misc;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.IDrawingObserver;
import org.diylc.core.Project;
import org.diylc.core.VisibilityPolicy;
import org.diylc.core.annotations.BomPolicy;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;

@ComponentDescriptor(name = "Label", author = "Branislav Stojkovic", category = "Misc", description = "User defined label", instanceNamePrefix = "L", zOrder = IDIYComponent.TEXT, stretchable = false, bomPolicy = BomPolicy.NEVER_SHOW)
public class Label extends AbstractComponent<Void> {

	public static String DEFAULT_TEXT = "Double click to edit text";

	private static final long serialVersionUID = 1L;

	private Point point = new Point(0, 0);
	private String text = DEFAULT_TEXT;
	private Font font = LABEL_FONT;
	private Color color = LABEL_COLOR;
	private boolean center = true;

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, boolean outlineMode,
			Project project, IDrawingObserver drawingObserver) {
		g2d.setColor(componentState == ComponentState.SELECTED ? LABEL_COLOR_SELECTED : color);
		g2d.setFont(font);
		FontMetrics fontMetrics = g2d.getFontMetrics();
		Rectangle2D rect = fontMetrics.getStringBounds(text, g2d);

		int textHeight = (int) rect.getHeight();
		int textWidth = (int) rect.getWidth();

		int x;
		int y;
		if (center) {
			// Center text horizontally and vertically
			x = point.x - textWidth / 2;
			y = point.y - textHeight / 2 + fontMetrics.getAscent();
		} else {
			x = point.x;
			y = point.y - textHeight;
		}

		g2d.drawString(text, x, y);
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		g2d.setColor(LABEL_COLOR);
		g2d.setFont(LABEL_FONT.deriveFont(13f * width / 32).deriveFont(Font.PLAIN));

		FontMetrics fontMetrics = g2d.getFontMetrics();
		Rectangle2D rect = fontMetrics.getStringBounds("Abc", g2d);

		int textHeight = (int) (rect.getHeight());
		int textWidth = (int) (rect.getWidth());

		// Center text horizontally and vertically.
		int x = (width - textWidth) / 2 + 1;
		int y = (height - textHeight) / 2 + fontMetrics.getAscent();

		g2d.drawString("Abc", x, y);
	}

	@EditableProperty
	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	// Bold and italic fields are named to be alphabetically after Font. This is
	// important!

	@EditableProperty(name = "Font Bold")
	public boolean getBold() {
		return font.isBold();
	}

	public void setBold(boolean bold) {
		if (bold) {
			if (font.isItalic()) {
				font = font.deriveFont(Font.BOLD + Font.ITALIC);
			} else {
				font = font.deriveFont(Font.BOLD);
			}
		} else {
			if (font.isItalic()) {
				font = font.deriveFont(Font.ITALIC);
			} else {
				font = font.deriveFont(Font.PLAIN);
			}
		}
	}

	@EditableProperty(name = "Font Italic")
	public boolean getItalic() {
		return font.isItalic();
	}

	public void setItalic(boolean italic) {
		if (italic) {
			if (font.isBold()) {
				font = font.deriveFont(Font.BOLD + Font.ITALIC);
			} else {
				font = font.deriveFont(Font.ITALIC);
			}
		} else {
			if (font.isBold()) {
				font = font.deriveFont(Font.BOLD);
			} else {
				font = font.deriveFont(Font.PLAIN);
			}
		}
	}

	@EditableProperty(name = "Font Size")
	public int getFontSize() {
		return font.getSize();
	}

	public void setFontSize(int size) {
		font = font.deriveFont((float) size);
	}

	@Override
	public int getControlPointCount() {
		return 1;
	}

	@Override
	public Point getControlPoint(int index) {
		return point;
	}

	@Override
	public boolean isControlPointSticky(int index) {
		return false;
	}

	@Override
	public VisibilityPolicy getControlPointVisibilityPolicy(int index) {
		return VisibilityPolicy.NEVER;
	}

	@Override
	public void setControlPoint(Point point, int index) {
		this.point.setLocation(point);
	}

	@EditableProperty(defaultable = false)
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@EditableProperty
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean getCenter() {
		return center;
	}

	public void setCenter(boolean center) {
		this.center = center;
	}

	@Override
	public String getName() {
		return super.getName();
	}

	@Override
	public Void getValue() {
		return null;
	}

	@Override
	public void setValue(Void value) {
	}
}
