package org.diylc.components.misc;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org.diylc.components.AbstractComponent;
import org.diylc.core.ComponentState;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Project;
import org.diylc.core.annotations.ComponentDescriptor;
import org.diylc.core.annotations.EditableProperty;
import org.diylc.core.measures.Size;
import org.diylc.core.measures.SizeUnit;
import org.diylc.plugins.file.BomEntry;
import org.diylc.plugins.file.BomMaker;
import org.diylc.utils.Constants;

@ComponentDescriptor(name = "Bill of materials", author = "Branislav Stojkovic", category = "Misc", description = "", instanceNamePrefix = "BOM", zOrder = IDIYComponent.ABOVE_COMPONENT, stretchable = false)
public class BOM extends AbstractComponent<Size> {

	public static Size DEFAULT_SIZE = new Size(10d, SizeUnit.cm);
	public static Size SPACING = Constants.GRID_SIZE;
	public static Color COLOR = Color.black;

	private static final long serialVersionUID = 1L;
	private Size size = DEFAULT_SIZE;

	private Point point = new Point(0, 0);

	@Override
	public void draw(Graphics2D g2d, ComponentState componentState, Project project) {
		List<BomEntry> bom = BomMaker.getInstance().createBom(project.getComponents());
		// Cleanup entries that do not have a value set.
		Iterator<BomEntry> iterator = bom.iterator();
		while (iterator.hasNext()) {
			BomEntry entry = iterator.next();
			Object value = entry.getValue();
			if (value == null || value.toString().trim().isEmpty()
					|| entry.getName().toLowerCase().contains("bom")) {
				iterator.remove();
			}
		}
		g2d.setFont(Constants.LABEL_FONT);
		g2d.setColor(componentState == ComponentState.DRAGGING
				|| componentState == ComponentState.SELECTED ? SELECTION_COLOR : COLOR);
		// Determine maximum name length and maximum value length to calculate
		// number of columns.
		FontMetrics fontMetrics = g2d.getFontMetrics();
		int maxNameWidth = 0;
		int maxValueWidth = 0;
		int maxHeight = 0;
		for (BomEntry entry : bom) {
			Rectangle2D stringBounds = fontMetrics.getStringBounds(entry.getName(), g2d);
			if (stringBounds.getWidth() > maxNameWidth) {
				maxNameWidth = (int) stringBounds.getWidth();
			}
			if (stringBounds.getHeight() > maxHeight) {
				maxHeight = (int) stringBounds.getHeight();
			}
			stringBounds = fontMetrics.getStringBounds(entry.getValue().toString(), g2d);
			if (stringBounds.getWidth() > maxValueWidth) {
				maxValueWidth = (int) stringBounds.getWidth();
			}
			if (stringBounds.getHeight() > maxHeight) {
				maxHeight = (int) stringBounds.getHeight();
			}
		}
		// Calculate maximum entry size.
		int maxEntrySize = maxNameWidth + maxValueWidth + 2 * SPACING.convertToPixels();
		int columnCount = size.convertToPixels() / maxEntrySize;
		int columnWidth = size.convertToPixels() / columnCount;
		int entriesPerColumn = (int) Math.ceil(1.d * bom.size() / columnCount);
		if (entriesPerColumn == 0) {
			return;
		}
		for (int i = 0; i < bom.size(); i++) {
			int columnIndex = i / entriesPerColumn;
			int rowIndex = i % entriesPerColumn;
			int x = point.x + columnIndex * columnWidth;
			int y = point.y + rowIndex * maxHeight;
			g2d.drawString(bom.get(i).getName(), x, y);
			x += maxNameWidth + SPACING.convertToPixels();
			g2d.drawString(bom.get(i).getValue().toString(), x, y);
		}
	}

	@Override
	public void drawIcon(Graphics2D g2d, int width, int height) {
		// TODO Auto-generated method stub

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
	public void setControlPoint(Point point, int index) {
		this.point.setLocation(point);
	}

	@EditableProperty(name = "Width")
	@Override
	public Size getValue() {
		return size;
	}

	@Override
	public void setValue(Size value) {
		this.size = value;
	}
}
