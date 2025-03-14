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
 */
package org.diylc.swing.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.swingframework.ButtonDialog;

import org.diylc.common.IPlugInPort;
import org.diylc.utils.IconLoader;

public class InfoDialog extends ButtonDialog {

  private static final long serialVersionUID = 1L;

  private String message;
  private String tipKey;

  public InfoDialog(JFrame owner, String tipKey) {
    super(owner, "Usage Tip", new String[] {"OK", "Dismiss"});
    this.tipKey = tipKey;
    int index = Arrays.asList(tipKeys).indexOf(tipKey);
    this.message = messages[index];   

    setMinimumSize(new Dimension(240, 40));

    layoutGui();
    setLocationRelativeTo(owner);
  }

  @Override
  protected JComponent getMainComponent() {
    JLabel label = new JLabel("<html>" + message + "</html>");
    label.setIcon(IconLoader.Help.getIcon());
    label.setBackground(Color.white);
    label.setOpaque(true);
    label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray), BorderFactory.createEmptyBorder(16, 16, 16, 16)));
    label.setIconTextGap(8);
    return label;
  }

  @Override
  public void setVisible(boolean b) {
    if (!b && getSelectedButtonCaption() == "Dismiss")
      ConfigurationManager.getInstance().writeValue(tipKey + ".dismissed", true);

    super.setVisible(b);
  }

  private static String[] tipKeys = new String[] {IPlugInPort.HIGHLIGHT_CONTINUITY_AREA};
  private static String[] messages =
      new String[] {"Click on a component to highlight continuity area directly or indirectly connected to it.<br>In this mode components cannot be moved or edited. In order to continue editing,<br>this mode needs to be switched OFF."};
}
