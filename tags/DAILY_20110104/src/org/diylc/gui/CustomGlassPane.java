package org.diylc.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.Icon;
import javax.swing.JComponent;

public class CustomGlassPane extends JComponent {

	private static final long serialVersionUID = 1L;
	private Icon icon;

	public CustomGlassPane() {
		super();
		addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				if (icon != null) {
					repaint();
				}
			}
		});
	}

	public void setCursorIcon(Icon icon) {
		this.icon = icon;
	}

	@Override
	public void paint(Graphics g) {
		Point cursor = getMousePosition();
		if ((icon != null) && (cursor != null)) {
			Graphics2D g2d = (Graphics2D) g;
			icon.paintIcon(this, g2d, cursor.x + 10, cursor.y + 10);
		}
	}
}
