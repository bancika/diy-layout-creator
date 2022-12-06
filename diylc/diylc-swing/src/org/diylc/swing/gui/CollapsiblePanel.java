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
package org.diylc.swing.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;

/***
 * Panel that has a clickable line that can used to collapse and expand the contents of the panel.
 * 
 * @author bancika
 */
public class CollapsiblePanel extends JPanel {

  private static final long serialVersionUID = 1L;

  private static int BORDER_THICKNESS = 4;

  private MatteBorderEx border;
  private String visibilityConfigKey;

  private Color defaultColor = Color.lightGray;
  private Color highlightColor = Color.decode("#66CCFF");

  /**
   * 
   * @param position designates the direction in which the panel should collapse
   */
  public CollapsiblePanel(int position, String visibilityConfigKey) {
    this.visibilityConfigKey = visibilityConfigKey;
    border = new MatteBorderEx(position == SwingConstants.BOTTOM ? BORDER_THICKNESS : 0,
        position == SwingConstants.RIGHT ? BORDER_THICKNESS : 0,
        position == SwingConstants.TOP ? BORDER_THICKNESS : 0,
        position == SwingConstants.LEFT ? BORDER_THICKNESS : 0, defaultColor);
    setToolTipText("Click to collapse");
    setBorder(border);
    BorderLayout borderLayout = new BorderLayout();
    setLayout(borderLayout);
    addMouseListener(mouseListener);

    ConfigurationManager.getInstance().addConfigListener(visibilityConfigKey,
        new IConfigListener() {

          @Override
          public void valueChanged(String key, Object value) {
            toggleVisibility(Boolean.TRUE.equals(value));
          }
        });
  }

  MouseListener mouseListener = new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
      toggleVisibility();
    }

    public void mouseEntered(MouseEvent e) {
      border.setMatteColor(highlightColor);
    };

    public void mouseExited(MouseEvent e) {
      border.setMatteColor(defaultColor);
    };
  };

  public Color getDefaultColor() {
    return defaultColor;
  }

  public void setDefaultColor(Color defaultColor) {
    this.defaultColor = defaultColor;
  }

  public Color getHighlightColor() {
    return highlightColor;
  }

  public void setHighlightColor(Color highlightColor) {
    this.highlightColor = highlightColor;
  }

  ComponentListener contentComponentListener = new ComponentAdapter() {
    @Override
    public void componentShown(ComponentEvent e) {
      updateBorderTitle();
    }

    @Override
    public void componentHidden(ComponentEvent e) {
      updateBorderTitle();
    }
  };

  @Override
  public Component add(Component comp) {
    comp.addComponentListener(contentComponentListener);
    Component r = super.add(comp);
    updateBorderTitle();
    if (!ConfigurationManager.getInstance().readBoolean(visibilityConfigKey, true)) {
      comp.setVisible(false);
    }
    return r;
  }

  @Override
  public Component add(String name, Component comp) {
    comp.addComponentListener(contentComponentListener);
    Component r = super.add(name, comp);
    updateBorderTitle();
    if (!ConfigurationManager.getInstance().readBoolean(visibilityConfigKey, true)) {
      comp.setVisible(false);
    }
    return r;
  }

  @Override
  public Component add(Component comp, int index) {
    comp.addComponentListener(contentComponentListener);
    Component r = super.add(comp, index);
    updateBorderTitle();
    return r;
  }

  @Override
  public void add(Component comp, Object constraints) {
    comp.addComponentListener(contentComponentListener);
    super.add(comp, constraints);
    updateBorderTitle();
    if (!ConfigurationManager.getInstance().readBoolean(visibilityConfigKey, true)) {
      comp.setVisible(false);
    }
  }

  @Override
  public void add(Component comp, Object constraints, int index) {
    comp.addComponentListener(contentComponentListener);
    super.add(comp, constraints, index);
    updateBorderTitle();
    if (!ConfigurationManager.getInstance().readBoolean(visibilityConfigKey, true)) {
      comp.setVisible(false);
    }
  }

  @Override
  public void remove(int index) {
    Component comp = getComponent(index);
    comp.removeComponentListener(contentComponentListener);
    super.remove(index);
  }

  @Override
  public void remove(Component comp) {
    comp.removeComponentListener(contentComponentListener);
    super.remove(comp);
  }

  @Override
  public void removeAll() {
    for (Component c : getComponents()) {
      c.removeComponentListener(contentComponentListener);
    }
    super.removeAll();
  }

  protected void toggleVisibility() {
    boolean invisible = hasInvisibleComponent();
    ConfigurationManager.getInstance().writeValue(visibilityConfigKey, invisible);
    toggleVisibility(invisible);
  }

  protected void toggleVisibility(boolean visible) {
    for (Component c : getComponents()) {
      c.setVisible(visible);
    }
    updateBorderTitle();
  }

  protected void updateBorderTitle() {
    boolean invisible = hasInvisibleComponent();
    Optional<String> componentName = Arrays.stream(getComponents())
        .map(x -> x.getName())
        .filter(x -> x != null)
        .findFirst();
    setToolTipText("Click to " + (invisible ? "expand" : "collapse") + (componentName.isPresent() ? " " + componentName.get() : ""));
  }

  protected final boolean hasInvisibleComponent() {
    for (Component c : getComponents()) {
      if (!c.isVisible()) {
        return true;
      }
    }
    return false;
  }

  class MatteBorderEx extends MatteBorder {

    public MatteBorderEx(int top, int left, int bottom, int right, Color matteColor) {
      super(top, left, bottom, right, matteColor);
    }

    private static final long serialVersionUID = 1L;

    public void setMatteColor(Color color) {
      this.color = color;
      repaint();
    }
  }
}
