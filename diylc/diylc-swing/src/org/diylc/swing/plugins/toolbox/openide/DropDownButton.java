package org.diylc.swing.plugins.toolbox.openide;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

class DropDownButton extends JButton {

  private static final long serialVersionUID = 1L;

  @SuppressWarnings("unused")
  private boolean mouseInButton = false;
  private boolean mouseInArrowArea = false;
  private Map<String, Icon> regIcons = new HashMap<String, Icon>(5);
  private Map<String, Icon> arrowIcons = new HashMap<String, Icon>(5);
  private static final String ICON_NORMAL = "normal";
  private static final String ICON_PRESSED = "pressed";
  private static final String ICON_ROLLOVER = "rollover";
  private static final String ICON_ROLLOVER_SELECTED = "rolloverSelected";
  private static final String ICON_SELECTED = "selected";
  private static final String ICON_DISABLED = "disabled";
  private static final String ICON_DISABLED_SELECTED = "disabledSelected";
  private static final String ICON_ROLLOVER_LINE = "rolloverLine";
  private static final String ICON_ROLLOVER_SELECTED_LINE = "rolloverSelectedLine";
  private PopupMenuListener menuListener;

  public DropDownButton(Icon icon, JPopupMenu popup) {
    Parameters.notNull((CharSequence) "icon", (Object) icon);
    assert (null != icon);
    this.putClientProperty("dropDownMenu", popup);
    this.setIcon(icon);
    this.setDisabledIcon(ImageUtilities.createDisabledIcon((Icon) icon));
    this.resetIcons();
    this.addPropertyChangeListener("dropDownMenu", new PropertyChangeListener() {

      @Override
      public void propertyChange(PropertyChangeEvent e) {
        DropDownButton.this.resetIcons();
      }
    });
    this.addMouseMotionListener(new MouseMotionAdapter() {

      @Override
      public void mouseMoved(MouseEvent e) {
        if (null != DropDownButton.this.getPopupMenu()) {
          DropDownButton.this.mouseInArrowArea = DropDownButton.this.isInArrowArea(e.getPoint());
          DropDownButton.this.updateRollover(DropDownButton.this._getRolloverIcon(),
              DropDownButton.this._getRolloverSelectedIcon());
        }
      }
    });
    this.addMouseListener(new MouseAdapter() {
      private boolean popupMenuOperation;

      @Override
      public void mousePressed(MouseEvent e) {
        this.popupMenuOperation = false;
        JPopupMenu menu = DropDownButton.this.getPopupMenu();
        if (menu != null && DropDownButton.this.getModel() instanceof Model) {
          Model model = (Model) DropDownButton.this.getModel();
          if (!model._isPressed()) {
            if (DropDownButton.this.isInArrowArea(e.getPoint()) && menu.getComponentCount() > 0) {
              model._press();
              menu.addPopupMenuListener(DropDownButton.this.getMenuListener());
              menu.show(DropDownButton.this, 0, DropDownButton.this.getHeight());
              this.popupMenuOperation = true;
            }
          } else {
            model._release();
            menu.removePopupMenuListener(DropDownButton.this.getMenuListener());
            this.popupMenuOperation = true;
          }
        }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (this.popupMenuOperation) {
          this.popupMenuOperation = false;
          e.consume();
        }
      }

      @Override
      public void mouseEntered(MouseEvent e) {
        DropDownButton.this.mouseInButton = true;
        if (DropDownButton.this.hasPopupMenu()) {
          DropDownButton.this.mouseInArrowArea = DropDownButton.this.isInArrowArea(e.getPoint());
          DropDownButton.this.updateRollover(DropDownButton.this._getRolloverIcon(),
              DropDownButton.this._getRolloverSelectedIcon());
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        DropDownButton.this.mouseInButton = false;
        DropDownButton.this.mouseInArrowArea = false;
        if (DropDownButton.this.hasPopupMenu()) {
          DropDownButton.this.updateRollover(DropDownButton.this._getRolloverIcon(),
              DropDownButton.this._getRolloverSelectedIcon());
        }
      }
    });
    this.setModel(new Model());
  }

  private PopupMenuListener getMenuListener() {
    if (null == this.menuListener) {
      this.menuListener = new PopupMenuListener() {

        @Override
        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
          JPopupMenu menu;
          if (DropDownButton.this.getModel() instanceof Model) {
            ((Model) DropDownButton.this.getModel())._release();
          }
          if (null != (menu = DropDownButton.this.getPopupMenu())) {
            menu.removePopupMenuListener(this);
          }
        }

        @Override
        public void popupMenuCanceled(PopupMenuEvent e) {}
      };
    }
    return this.menuListener;
  }

  private void updateRollover(Icon rollover, Icon rolloverSelected) {
    super.setRolloverIcon(rollover);
    super.setRolloverSelectedIcon(rolloverSelected);
  }

  private void resetIcons() {
    Icon icon = this.regIcons.get(ICON_NORMAL);
    if (null != icon) {
      this.setIcon(icon);
    }
    if (null != (icon = this.regIcons.get(ICON_PRESSED))) {
      this.setPressedIcon(icon);
    }
    if (null != (icon = this.regIcons.get(ICON_ROLLOVER))) {
      this.setRolloverIcon(icon);
    }
    if (null != (icon = this.regIcons.get(ICON_ROLLOVER_SELECTED))) {
      this.setRolloverSelectedIcon(icon);
    }
    if (null != (icon = this.regIcons.get(ICON_SELECTED))) {
      this.setSelectedIcon(icon);
    }
    if (null != (icon = this.regIcons.get(ICON_DISABLED))) {
      this.setDisabledIcon(icon);
    }
    if (null != (icon = this.regIcons.get(ICON_DISABLED_SELECTED))) {
      this.setDisabledSelectedIcon(icon);
    }
  }

  private Icon _getRolloverIcon() {
    Icon icon = null;
    icon = this.arrowIcons.get(this.mouseInArrowArea ? ICON_ROLLOVER : ICON_ROLLOVER_LINE);
    if (null == icon) {
      Icon orig = this.regIcons.get(ICON_ROLLOVER);
      if (null == orig) {
        orig = this.regIcons.get(ICON_NORMAL);
      }
      icon = new IconWithArrow(orig, !this.mouseInArrowArea);
      this.arrowIcons.put(this.mouseInArrowArea ? ICON_ROLLOVER : ICON_ROLLOVER_LINE, icon);
    }
    return icon;
  }

  private Icon _getRolloverSelectedIcon() {
    Icon icon = null;
    icon = this.arrowIcons.get(this.mouseInArrowArea ? ICON_ROLLOVER_SELECTED : ICON_ROLLOVER_SELECTED_LINE);
    if (null == icon) {
      Icon orig = this.regIcons.get(ICON_ROLLOVER_SELECTED);
      if (null == orig) {
        orig = this.regIcons.get(ICON_ROLLOVER);
      }
      if (null == orig) {
        orig = this.regIcons.get(ICON_NORMAL);
      }
      icon = new IconWithArrow(orig, !this.mouseInArrowArea);
      this.arrowIcons.put(this.mouseInArrowArea ? ICON_ROLLOVER_SELECTED : ICON_ROLLOVER_SELECTED_LINE, icon);
    }
    return icon;
  }

  JPopupMenu getPopupMenu() {
    Object menu = this.getClientProperty("dropDownMenu");
    if (menu instanceof JPopupMenu) {
      return (JPopupMenu) menu;
    }
    return null;
  }

  boolean hasPopupMenu() {
    return null != this.getPopupMenu();
  }

  private boolean isInArrowArea(Point p) {
    return p.getLocation().x >= this.getWidth() - IconWithArrow.getArrowAreaWidth() - this.getInsets().right;
  }

  @Override
  public void setIcon(Icon icon) {
    assert (null != icon);
    Icon arrow = this.updateIcons(icon, ICON_NORMAL);
    this.arrowIcons.remove(ICON_ROLLOVER_LINE);
    this.arrowIcons.remove(ICON_ROLLOVER_SELECTED_LINE);
    this.arrowIcons.remove(ICON_ROLLOVER);
    this.arrowIcons.remove(ICON_ROLLOVER_SELECTED);
    super.setIcon(this.hasPopupMenu() ? arrow : icon);
  }

  private Icon updateIcons(Icon orig, String iconType) {
    ImageIcon arrow = null;
    if (null == orig) {
      this.regIcons.remove(iconType);
      this.arrowIcons.remove(iconType);
    } else {
      this.regIcons.put(iconType, orig);
      arrow = new ImageIcon(ImageUtilities.icon2Image((Icon) new IconWithArrow(orig, false)));
      this.arrowIcons.put(iconType, arrow);
    }
    return arrow;
  }

  @Override
  public void setPressedIcon(Icon icon) {
    Icon arrow = this.updateIcons(icon, ICON_PRESSED);
    super.setPressedIcon(this.hasPopupMenu() ? arrow : icon);
  }

  @Override
  public void setSelectedIcon(Icon icon) {
    Icon arrow = this.updateIcons(icon, ICON_SELECTED);
    super.setSelectedIcon(this.hasPopupMenu() ? arrow : icon);
  }

  @Override
  public void setRolloverIcon(Icon icon) {
    Icon arrow = this.updateIcons(icon, ICON_ROLLOVER);
    this.arrowIcons.remove(ICON_ROLLOVER_LINE);
    this.arrowIcons.remove(ICON_ROLLOVER_SELECTED_LINE);
    super.setRolloverIcon(this.hasPopupMenu() ? arrow : icon);
  }

  @Override
  public void setRolloverSelectedIcon(Icon icon) {
    Icon arrow = this.updateIcons(icon, ICON_ROLLOVER_SELECTED);
    this.arrowIcons.remove(ICON_ROLLOVER_SELECTED_LINE);
    super.setRolloverSelectedIcon(this.hasPopupMenu() ? arrow : icon);
  }

  @Override
  public void setDisabledIcon(Icon icon) {
    Icon arrow = this.updateIcons(icon, ICON_DISABLED);
    super.setDisabledIcon(this.hasPopupMenu() ? arrow : icon);
  }

  @Override
  public void setDisabledSelectedIcon(Icon icon) {
    Icon arrow = this.updateIcons(icon, ICON_DISABLED_SELECTED);
    super.setDisabledSelectedIcon(this.hasPopupMenu() ? arrow : icon);
  }

  @Override
  public void setText(String text) {}

  @Override
  public String getText() {
    return null;
  }

  private class Model extends DefaultButtonModel {

    private static final long serialVersionUID = 1L;

    private boolean _pressed;

    private Model() {
      this._pressed = false;
    }

    @Override
    public void setPressed(boolean b) {
      if (DropDownButton.this.mouseInArrowArea || this._pressed) {
        return;
      }
      super.setPressed(b);
    }

    public void _press() {
      if (this.isPressed() || !this.isEnabled()) {
        return;
      }
      this.stateMask |= 5;
      this.fireStateChanged();
      this._pressed = true;
    }

    public void _release() {
      this._pressed = false;
      DropDownButton.this.mouseInArrowArea = false;
      this.setArmed(false);
      this.setPressed(false);
      this.setRollover(false);
      this.setSelected(false);
    }

    public boolean _isPressed() {
      return this._pressed;
    }

    @Override
    protected void fireStateChanged() {
      if (this._pressed) {
        return;
      }
      super.fireStateChanged();
    }

    @Override
    public void setArmed(boolean b) {
      if (this._pressed) {
        return;
      }
      super.setArmed(b);
    }

    @Override
    public void setEnabled(boolean b) {
      if (this._pressed) {
        return;
      }
      super.setEnabled(b);
    }

    @Override
    public void setSelected(boolean b) {
      if (this._pressed) {
        return;
      }
      super.setSelected(b);
    }

    @Override
    public void setRollover(boolean b) {
      if (this._pressed) {
        return;
      }
      super.setRollover(b);
    }
  }

}
