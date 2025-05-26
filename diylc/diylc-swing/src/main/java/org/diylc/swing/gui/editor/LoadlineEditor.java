/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.swing.gui.editor;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import org.apache.log4j.Logger;
import org.diylc.components.misc.LoadlineEntity;

import com.thoughtworks.xstream.XStream;

import org.diylc.common.PropertyWrapper;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.loadline.LoadlineEditorFrame;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class LoadlineEditor extends JButton {

  private static final long serialVersionUID = 1L;
  
  private static final Logger LOG = Logger.getLogger(LoadlineEditor.class);

  private static final String title = " (click to load from a file)";
  
  public LoadlineEditor(final PropertyWrapper property) {
    super();

    LoadlineEntity loadline = (LoadlineEntity) property.getValue();
    
    String name = (loadline == null ? "<empty>" : loadline.getName()) + " ";
    
    setText(property.isUnique() ? (name + title ): ("(multi value) " + title));
    setOpaque(true);
    setHorizontalAlignment(SwingConstants.CENTER);
    setBorder(BorderFactory.createEtchedBorder());
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent arg0) {
        final File file = DialogFactory.getInstance().showOpenDialog(FileFilterEnum.CRV.getFilter(), null, 
            FileFilterEnum.CRV.getExtensions()[0], null);
        if (file != null) {
          try {
            XStream xStream = LoadlineEditorFrame.loadlineXStream();

            FileInputStream fis = new FileInputStream(file);
            Reader reader = new InputStreamReader(fis, "UTF-8");
            LoadlineEntity loadline = (LoadlineEntity) xStream.fromXML(reader);
            fis.close();
            property.setValue(loadline);
            property.setChanged(true);
            
            String name = (loadline == null ? "<empty>" : loadline.getName()) + " ";            
            setText(property.isUnique() ? (name + title ): ("(multi value) " + title));
          } catch (Exception ex) {
            LOG.error("Error loading loadline file", ex);
          }
        }        
      }
    });
    
    if (property.isReadOnly()) {
     setEnabled(false);
    }
  }
}
