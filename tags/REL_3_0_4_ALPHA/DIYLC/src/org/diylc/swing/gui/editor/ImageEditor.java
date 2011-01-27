package org.diylc.swing.gui.editor;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.diylc.common.PropertyWrapper;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class ImageEditor extends JButton {

	private static final long serialVersionUID = 1L;

	public ImageEditor(final PropertyWrapper property) {
		super("Click to load file");
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				File file = DialogFactory.getInstance().showOpenDialog(
						FileFilterEnum.IMAGES.getFilter(), null,
						FileFilterEnum.IMAGES.getExtensions()[0], null);
				if (file != null) {
					ImageIcon image = new ImageIcon(file.getAbsolutePath());
					property.setValue(image);
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
