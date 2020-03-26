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

import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

/***
 * Panel that has a clickable line that can used to collapse and expand the contents of the panel. 
 * 
 * @author bancika
 */
public class CollapsiblePanel extends JPanel {
  
  private static final long serialVersionUID = 1L;
  
  private static int thickness = 4;
  
  private MatteBorderEx border;
  
  private Color defaultColor = Color.lightGray;
  private Color highlightColor = Color.decode("#66CCFF");

  public CollapsiblePanel(int position) {
      border = new MatteBorderEx(position == SwingConstants.BOTTOM ? thickness : 0, 
          position == SwingConstants.RIGHT ? thickness : 0, 
          position == SwingConstants.TOP ? thickness : 0, 
          position == SwingConstants.LEFT ? thickness : 0, defaultColor);
      setToolTipText("Click to collapse");
      setBorder(border);
      BorderLayout borderLayout = new BorderLayout();
      setLayout(borderLayout);
      addMouseListener(mouseListener);
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
      return r;
  }

  @Override
  public Component add(String name, Component comp) {
      comp.addComponentListener(contentComponentListener);
      Component r = super.add(name, comp);
      updateBorderTitle();
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
  }

  @Override
  public void add(Component comp, Object constraints, int index) {
      comp.addComponentListener(contentComponentListener);
      super.add(comp, constraints, index);
      updateBorderTitle();
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
      toggleVisibility(hasInvisibleComponent());
  }

  protected void toggleVisibility(boolean visible) {
      for (Component c : getComponents()) {
          c.setVisible(visible);
      }
      updateBorderTitle();
  }

  protected void updateBorderTitle() {

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