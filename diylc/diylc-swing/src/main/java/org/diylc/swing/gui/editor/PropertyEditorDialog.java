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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.apache.log4j.Logger;
import org.diylc.swingframework.ButtonDialog;

import org.diylc.common.PropertyWrapper;
import org.diylc.core.ValidationException;
import org.diylc.lang.LangUtil;

public class PropertyEditorDialog extends ButtonDialog { 

  private static final Logger LOG = Logger.getLogger(PropertyEditorDialog.class);

  private static final long serialVersionUID = 1L;
  
  private static final String ERROR_TITLE = LangUtil.translate("Error");
  private static final String INPUT_ERROR = LangUtil.translate("Input error for \"%s\": %s");

  private static final String DEFAULT_BOX_TOOLTIP_TEXT =
      LangUtil.translate("If this box is checked application will use the current value as a<br>default when creating new components of the same type");
  private static final String DEFAULT_BOX_TOOLTIP =
      "<html>"
      + DEFAULT_BOX_TOOLTIP_TEXT
      + "</html>";

  private List<PropertyWrapper> properties;
  private Set<PropertyWrapper> defaultedProperties;
  private Component componentToFocus = null;
  private boolean saveDefaults;

  public PropertyEditorDialog(JFrame owner, List<PropertyWrapper> properties, String title,
      boolean saveDefaults) {
    super(owner, title, new String[] {ButtonDialog.OK, ButtonDialog.CANCEL});
    this.saveDefaults = saveDefaults;

    LOG.debug("Creating property editor for: " + properties);

    this.properties = properties;
    this.defaultedProperties = new HashSet<PropertyWrapper>();

    setMinimumSize(new Dimension(240, 40));

    layoutGui();
    setLocationRelativeTo(owner);
  }

  @Override
  protected boolean validateInput(String button) {
    if (button.equals(ButtonDialog.OK)) {
      Object testObject = null;
      if (properties.size() > 0) {
        if (properties.get(0).getOwnerObject() instanceof Cloneable) {
          try {
            Object object = ((Cloneable) properties.get(0).getOwnerObject());
            Method method = object.getClass().getMethod("clone");
            method.setAccessible(true);
            testObject = method.invoke(object);
            for (PropertyWrapper property : properties) {
              property.writeTo(testObject);
            }
          } catch (Exception e) {
            LOG.warn("Could not clone test object", e);
            testObject = properties.get(0).getOwnerObject();
          }
        } else {
          testObject = properties.get(0).getOwnerObject();
        }
      }
      for (PropertyWrapper property : properties) {
        try {
          property.getValidator().validate(testObject, property.getValue());
        } catch (ValidationException ve) {
          JOptionPane.showMessageDialog(PropertyEditorDialog.this,
              String.format(INPUT_ERROR, property.getName(), ve.getMessage()), ERROR_TITLE,
              JOptionPane.ERROR_MESSAGE);
          return false;
        }
      }
    }
    return true;
  }

  private JPanel createEditorFields() {
    JPanel editorPanel = new JPanel(new GridBagLayout());
    editorPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridy = 0;

    gbc.anchor = GridBagConstraints.FIRST_LINE_START;

    for (PropertyWrapper property : properties) {
      gbc.gridx = 0;
      gbc.fill = GridBagConstraints.NONE;
      gbc.weightx = 0;
      gbc.insets = new Insets(4, 2, 2, 2);
      gbc.anchor = GridBagConstraints.WEST;
      editorPanel.add(new JLabel(LangUtil.translate(property.getName()) + ": "), gbc);

      gbc.gridx = 1;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;
      gbc.insets = new Insets(2, 2, 2, 2);

      try {
        Component editor = FieldEditorFactory.createFieldEditor(property);
        editor.addKeyListener(new KeyAdapter() {
  
          @Override
          public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
              getButton(OK).doClick();
            } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
              getButton(CANCEL).doClick();
            }
          }
        });
        editorPanel.add(editor, gbc);

        if (property.isDefaultable()) {
          gbc.gridx = 2;
          gbc.fill = GridBagConstraints.NONE;
          gbc.weightx = 0;
  
          if (saveDefaults && !property.isReadOnly()) {
            editorPanel.add(createDefaultCheckBox(property), gbc);
          }
        }
  
        if (property.getName().equalsIgnoreCase("value")) {
          componentToFocus = editor;
        }
  
        // Make value field focused
        try {
          if (componentToFocus == null
              && property.getGetter().getName().equalsIgnoreCase("getValue")) {
            componentToFocus = editor;
          }
        } catch (Exception e1) {
          LOG.warn("Could not determine editor component to focus", e1);
        }
  
        gbc.gridy++;
      } catch (Exception e) {
        LOG.error("Error creating editor for " + property.getName(), e);
      }
    }

    return editorPanel;
  }

  private JCheckBox createDefaultCheckBox(final PropertyWrapper property) {
    final JCheckBox checkBox = new JCheckBox();
    checkBox.setToolTipText(DEFAULT_BOX_TOOLTIP);
    checkBox.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (checkBox.isSelected()) {
          defaultedProperties.add(property);
        } else {
          defaultedProperties.remove(property);
        }
      }
    });
    return checkBox;
  }

  public Set<PropertyWrapper> getDefaultedProperties() {
    return defaultedProperties;
  }

  @Override
  public void setVisible(boolean b) {
    if (b && componentToFocus != null) {
      componentToFocus.requestFocusInWindow();
      componentToFocus = null;
    }
    super.setVisible(b);
  }

  @Override
  protected JComponent getMainComponent() {
    return createEditorFields();
  }
}
