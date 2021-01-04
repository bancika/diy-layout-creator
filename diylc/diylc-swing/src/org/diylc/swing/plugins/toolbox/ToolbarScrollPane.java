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
package org.diylc.swing.plugins.toolbox;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicArrowButton;

import org.apache.log4j.Logger;

public class ToolbarScrollPane extends JPanel {

  private static final long serialVersionUID = 1L;
  
  private static final Logger LOG = Logger.getLogger(ToolbarScrollPane.class);

  private Component view;
  private BasicArrowButton west;
  private BasicArrowButton east;

  public ToolbarScrollPane(Component view) {
    this.view = view;
    setOpaque(false);
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder());

    try {
      JScrollPane scrollPane = new JScrollPane(view);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);    
  
      JScrollBar horizontal = scrollPane.getHorizontalScrollBar();
  
      west = new BasicArrowButton(BasicArrowButton.WEST);
      west.setAction(new ActionMapAction("", horizontal, "negativeUnitIncrement", 120));
      west.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Button.shadow")));
      add(west, BorderLayout.WEST);
  
      east = new BasicArrowButton(BasicArrowButton.EAST);
      east.setAction(new ActionMapAction("", horizontal, "positiveUnitIncrement", 120));
      east.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Button.shadow")));
      add(east, BorderLayout.EAST);
  
      addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
          refreshScrollButtonVisibility();
        }
      });
      
     // LOG.info("Creating scrollable toolbar");
      add(scrollPane);
    } catch (Exception e) {
      LOG.info("Creating non-scrollable toolbar, since scroll bar initialization failed");
      add(view);
    }
  }

  private void refreshScrollButtonVisibility() {
    boolean needsScroll = getWidth() < view.getWidth();
    west.setVisible(needsScroll);
    east.setVisible(needsScroll);
  }
}
