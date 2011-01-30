package org.diylc.swing.gui.editor;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.diylc.common.PropertyWrapper;


public class ColorEditor extends JLabel {

	private static final long serialVersionUID = 1L;

	public ColorEditor(final PropertyWrapper property) {
		super("Click to edit");
		setOpaque(true);
		setHorizontalAlignment(SwingConstants.CENTER);
		setBorder(BorderFactory.createEtchedBorder());
		setBackground((Color) property.getValue());
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				Color newColor = JColorChooser.showDialog(ColorEditor.this, "Choose Color",
						getBackground());
				if (newColor != null) {
					property.setValue(newColor);
					setBackground(newColor);
				}
			}
		});
	}
	
	@Override
	public void setBackground(Color bg) {
		if (bg.getRed() < 127 || bg.getBlue() < 127 || bg.getGreen() < 127) {
			setForeground(Color.white);
		} else {
			setForeground(Color.black);
		}
		super.setBackground(bg);
	}
}
