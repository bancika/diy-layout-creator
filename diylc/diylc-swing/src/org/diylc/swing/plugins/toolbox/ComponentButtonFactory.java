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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.ComponentType;
import org.diylc.common.IBlockProcessor.InvalidBlockException;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Template;
import org.diylc.swing.images.IconLoader;
import org.diylc.swingframework.openide.DropDownButtonFactory;
import com.google.common.collect.Lists;

/**
 * Factory that creates {@link JButton}s which display component type icons and instantiates the
 * component when clicked.
 * 
 * @author Branislav Stojkovic
 */
public class ComponentButtonFactory {

  private static final int MAX_MENU_ITEMS = 30;

  private static final Logger LOG = Logger.getLogger(ComponentButtonFactory.class);

  public static int MARGIN = 3;

  public static JButton create(final IPlugInPort plugInPort, final ComponentType componentType,
      final JPopupMenu menu) {
    JButton button = DropDownButtonFactory.createDropDownButton(componentType.getIcon(), menu);

    button.setBorder(BorderFactory.createEmptyBorder(MARGIN + 1, MARGIN + 1, MARGIN, MARGIN));

    button.setToolTipText("<html><b>" + componentType.getName() + "</b><br>"
        + componentType.getDescription() + "<br>Author: " + componentType.getAuthor()
        + "<br><br>Right click to select all components of this type" + "</html>");
    button.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        plugInPort.setNewComponentTypeSlot(componentType, null, null, false);
      }
    });

    button.addMouseListener(new MouseAdapter() {

      @Override
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
          List<IDIYComponent<?>> components = plugInPort.getCurrentProject().getComponents();
          List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
          for (IDIYComponent<?> component : components) {
            if (componentType.getInstanceClass().equals(component.getClass())) {
              newSelection.add(component);
            }
          }
          // Ctrl appends selection
          if (Utils.isMac() ? e.isControlDown() : e.isMetaDown()) {
            newSelection.addAll(plugInPort.getSelectedComponents());
          }
          plugInPort.updateSelection(newSelection);
          plugInPort.setNewComponentTypeSlot(null, null, null, false);
          plugInPort.refresh();
        }
      }
    });

    button.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        plugInPort.keyPressed(e.getKeyCode(), Utils.isMac() ? e.isControlDown() : e.isMetaDown(),
            e.isShiftDown(), e.isAltDown());
      }
    });
    return button;
  }

  private static final Pattern contributedPattern = Pattern.compile("^(.*)\\[(.*)\\]");

  public static JMenuItem createVariantItem(final IPlugInPort plugInPort, final Template variant,
      final ComponentType componentType) {

    String display = variant.getName();

    Matcher match = contributedPattern.matcher(display);
    if (match.find()) {
      String name = match.group(1);
      String owner = match.group(2);
      display = "<html>" + name + "<font color='gray'>[" + owner + "]</font></html>";
    }

    final JMenuItem item = new JMenuItem(display) {

      private static final long serialVersionUID = 1L;

      // Customize item size to fit the delete button
      public java.awt.Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 32, d.height);
      }
    };
    item.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        plugInPort.setNewComponentTypeSlot(componentType, variant, null, false);
      }
    });

    String defaultVariant = plugInPort.getDefaultVariant(componentType);

    JLabel label =
        new JLabel(variant.getName().equals(defaultVariant) ? IconLoader.PinGreen.getIcon()
            : IconLoader.PinGrey.getIcon());
    label.setToolTipText(variant.getName().equals(defaultVariant) ? "Remove default variant"
        : "Set default variant");
    label.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        // Hide the menu
        Container c = item.getParent();
        if (c != null && c instanceof JPopupMenu) {
          JPopupMenu m = (JPopupMenu) c;
          m.setVisible(false);
        }
        plugInPort.setDefaultVariant(componentType, variant.getName());
        e.consume();
      }
    });
    Border margin = new EmptyBorder(4, 0, 0, 0);
    label.setBorder(margin);
    item.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    item.add(label);

    label = new JLabel(IconLoader.Garbage.getIcon());
    label.setToolTipText("Delete variant");
    label.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {
        // Hide the menu
        Container c = item.getParent();
        if (c != null && c instanceof JPopupMenu) {
          JPopupMenu m = (JPopupMenu) c;
          m.setVisible(false);
        }
        int result = JOptionPane.showConfirmDialog(SwingUtilities.getRoot(item),
            "Are you sure you want to delete variant \"" + variant.getName() + "\"", "Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result != JOptionPane.YES_OPTION) {
          return;
        }
        plugInPort.deleteVariant(componentType, variant.getName());
        e.consume();
      }
    });
    margin = new EmptyBorder(4, 2, 0, 0);
    label.setBorder(margin);
    item.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    item.add(label);
    return item;
  }

  public static List<Component> createDatasheetItems(final IPlugInPort plugInPort,
      final ComponentType componentType, List<String> path, Consumer<String[]> modelConsumer) {
    int depth = path.size();
    List<String[]> datasheet = componentType.getDatasheet();
    if (datasheet == null) {
      return null;
    }
    Stream<String[]> stream = datasheet.stream();
    for (int i = 0; i < depth; i++) {
      final int finalI = i;
      stream = stream.filter(line -> line[finalI].equals(path.get(finalI)));
    }
    List<String[]> filteredDatasheet = stream.collect(Collectors.toList());

    return filteredDatasheet.stream().map(x -> x[depth]).distinct().map(value -> {

      if (depth < componentType.getDatasheetCreationStepCount() - 1) {
        final JMenu submenu = new JMenu(value);

        submenu.add(new JMenuItem("Loading..."));

        submenu.addMenuListener(new MenuListener() {

          @Override
          public void menuSelected(MenuEvent e) {
            submenu.removeAll();
            List<String> newPath = new ArrayList<String>(path);
            newPath.add(value);
            List<Component> childItems = createDatasheetItems(plugInPort, componentType, newPath, modelConsumer);
            if (childItems.size() > MAX_MENU_ITEMS) {
              List<List<Component>> partitions = Lists.partition(childItems, MAX_MENU_ITEMS);
              for (List<Component> partition : partitions) {
                String label = null;
                Component first = partition.get(0);
                if (first instanceof JMenu) {
                  label = ((JMenu) first).getText();
                  if (partition.size() > 1) {
                    label += " - " + ((JMenu) partition.get(partition.size() - 1)).getText();
                  }
                } else if (first instanceof JMenuItem) {
                  label = ((JMenuItem) first).getText();
                  if (partition.size() > 1) {
                    label += " - " + ((JMenuItem) partition.get(partition.size() - 1)).getText();
                  }
                }
                JMenu partitionMenu = new JMenu(label);
                for (Component childItem : partition) {
                  partitionMenu.add(childItem);
                }
                submenu.add(partitionMenu);
              }
            } else {
              for (Component childItem : childItems) {
                submenu.add(childItem);
              }
            }
          }

          @Override
          public void menuDeselected(MenuEvent e) {}

          @Override
          public void menuCanceled(MenuEvent e) {}
        });

        return (Component) submenu;
      }

      JMenuItem item = new JMenuItem(value);

      item.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          Optional<String[]> findFirst =
              filteredDatasheet.stream().filter(line -> line[depth].equals(value)).findFirst();

          if (findFirst.isPresent()) {
            modelConsumer.accept(findFirst.get());            
          } else {
            LOG.error("Could not find datasheet item for: " + value);
          }
        }
      });

      return (Component) item;
    }).collect(Collectors.toList());
  }

  public static JButton createBuildingBlockButton(final IPlugInPort plugInPort,
      final String blockName, final JPopupMenu menu) {
    JButton button = DropDownButtonFactory.createDropDownButton(blockName,
        IconLoader.ComponentLarge.getIcon(), menu);

    button.setVerticalTextPosition(SwingConstants.CENTER);
    button.setHorizontalTextPosition(SwingConstants.LEFT);

    button.setBorder(BorderFactory.createEmptyBorder(MARGIN + 1, MARGIN + 1, MARGIN, MARGIN));
    button.setToolTipText("<html><b>" + blockName + "</b><br> building block</html>");

    button.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          plugInPort.loadBlock(blockName);
        } catch (InvalidBlockException e1) {
          LOG.error("Error loading building block", e1);
        }
      }
    });

    button.addKeyListener(new KeyAdapter() {

      @Override
      public void keyPressed(KeyEvent e) {
        plugInPort.keyPressed(e.getKeyCode(), Utils.isMac() ? e.isControlDown() : e.isMetaDown(),
            e.isShiftDown(), e.isAltDown());
      }
    });
    return button;
  }
}
