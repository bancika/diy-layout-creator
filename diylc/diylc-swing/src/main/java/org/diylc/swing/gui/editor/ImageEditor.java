/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 */
package org.diylc.swing.gui.editor;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.JButton;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.annotations.BinaryType;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class ImageEditor extends JButton {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(ImageEditor.class);

  private static final String title = "Click to load image file";

  private BinaryType binaryType;

  public ImageEditor(final PropertyWrapper property, BinaryType binaryType) {
    super(property.isUnique() ? title : ("(multi value) " + title));
    this.binaryType = binaryType;
    if (property.isReadOnly())
      setEnabled(false);
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(java.awt.event.ActionEvent e) {
        File file = DialogFactory.getInstance().showOpenDialog(getFileFilterEnum().getFilter(),
            null, getFileFilterEnum().getExtensions()[0], null);
        if (file != null) {
          property.setChanged(true);
          FileInputStream fis;
          try {
            fis = new FileInputStream(file);
            byte[] byteArray = IOUtils.toByteArray(fis);            
            property.setValue(byteArray);
            fis.close();
          } catch (Exception e1) {
            LOG.error("Error loading image", e1);
          }
        }
      }
    });
  }
  
  private FileFilterEnum getFileFilterEnum() {
    if (binaryType == BinaryType.SVG) {
      return FileFilterEnum.SVG;
    }
    return FileFilterEnum.IMAGES;
  }

  // @Override
  // public void setBackground(Color bg) {
  // if (bg.getRed() < 127 || bg.getBlue() < 127 || bg.getGreen() < 127) {
  // setForeground(Color.white);
  // } else {
  // setForeground(Color.black);
  // }
  // super.setBackground(bg);
  // }
}
