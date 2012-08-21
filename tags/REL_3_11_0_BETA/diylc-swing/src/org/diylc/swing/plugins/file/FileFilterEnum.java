package org.diylc.swing.plugins.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public enum FileFilterEnum {

	PNG("PNG Images (*.png)", "png"), PDF("PDF Files (*.pdf)", "pdf"), DIY(
			"DIY Project Files (*.diy)", "diy"), EXCEL("Excel Workbooks (*.xls)", "xls"), CSV(
			"Comma Separated Files (*.csv)", "csv"), HTML("HTML Files (*.html)", "html"), IMAGES(
			"Image Files (*.png, *.jpg, *.gif)", "png", "jpg", "gif");

	FileFilter filter;
	String[] extensions;

	private FileFilterEnum(final String description, final String... extensions) {
		this.extensions = extensions;
		filter = new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				String fileExt = f.getName();
				fileExt = fileExt.substring(fileExt.lastIndexOf('.') + 1).toLowerCase();
				for (String ext : extensions) {
					if (ext.equals(fileExt)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
	}

	public FileFilter getFilter() {
		return filter;
	}

	public String[] getExtensions() {
		return extensions;
	}
}
