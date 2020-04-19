/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.plugins.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public enum FileFilterEnum {

  PNG("PNG Images (*.png)", "png"), PDF("PDF Files (*.pdf)", "pdf"), DIY("DIY Project Files (*.diy)", "diy"), EXCEL(
      "Excel Workbooks (*.xls)", "xls"), CSV("Comma Separated Files (*.csv)", "csv"), HTML("HTML Files (*.html)",
      "html"), IMAGES("Image Files (*.png, *.jpg, *.gif)", "png", "jpg", "gif"), XML("XML Files (*.xml)", "xml"), TEST("Test Files (*.tst)", "tst");

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
