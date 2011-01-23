package org.diylc.gui.components;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class OverwritePromptFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1L;
	private String defaultExtension;

	public void approveSelection() {
		File lSelectedFile;
		if (getSelectedFile().getAbsolutePath().contains(".")) {
			lSelectedFile = getSelectedFile();
		} else {
			lSelectedFile = new File(getSelectedFile().getAbsoluteFile() + "." + defaultExtension);
		}

		if (lSelectedFile != null && lSelectedFile.exists()) {
			int lResponse = JOptionPane.showConfirmDialog(this, lSelectedFile.getAbsolutePath()
					+ " already exists.\nDo you " + "want to replace it?", "Warning",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

			if (lResponse != JOptionPane.YES_OPTION) {
				return;
			}
		}
		super.approveSelection();
	}

	public void setFileFilter(FileFilter filter, String defaultExtension) {
		this.defaultExtension = defaultExtension;
		super.setFileFilter(filter);
	}
}
