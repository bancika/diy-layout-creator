package org.diylc.swing.plugins.toolbox;

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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;
import org.diylc.core.Template;
import org.diylc.images.IconLoader;
import org.openide.awt.DropDownButtonFactory;

/**
 * {@link JButton} that displays component type icon and instantiates the
 * component when clicked.
 * 
 * @author Branislav Stojkovic
 */
class ComponentButtonFactory {

	private static final long serialVersionUID = 1L;

	public static int MARGIN = 4;

	public static JButton create(final IPlugInPort plugInPort,
			final ComponentType componentType, final JPopupMenu menu) {
		JButton button = DropDownButtonFactory.createDropDownButton(
				componentType.getIcon(), menu);

		button.setBorder(BorderFactory.createEmptyBorder(MARGIN, MARGIN,
				MARGIN, MARGIN));

		// mainButton.setBorder(BorderFactory.createEmptyBorder());
		// dropDownButton.setBorder(BorderFactory.createEmptyBorder());
		// setBorder(BorderFactory.createLineBorder(Color.gray));

		button.setToolTipText("<html><b>" + componentType.getName()
				+ "</b><br>" + componentType.getDescription() + "<br>Author: "
				+ componentType.getAuthor()
				+ "<br><br>Right click to select all components of this type"
				+ "</html>");
		// initializeDnD();
		button.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				plugInPort.setNewComponentTypeSlot(componentType, null);
			}
		});

		button.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					List<IDIYComponent<?>> components = plugInPort
							.getCurrentProject().getComponents();
					List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
					for (IDIYComponent<?> component : components) {
						if (componentType.getInstanceClass().equals(
								component.getClass())) {
							newSelection.add(component);
						}
					}
					plugInPort.updateSelection(newSelection);
					plugInPort.setNewComponentTypeSlot(null, null);
					plugInPort.refresh();
				}
			}
		});
		// addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// // plugInPort.setCursorIcon(componentType.getIcon());
		// plugInPort.setNewComponentTypeSlot(componentType);
		// // try {
		// // plugInPort.instantiateComponent(componentType
		// // .getComponentInstanceClass(), null);
		// // } catch (Exception e1) {
		// // e1.printStackTrace();
		// // }
		// }
		// });
		button.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				plugInPort.keyPressed(e.getKeyCode(), e.isControlDown(), e
						.isShiftDown(), e.isAltDown());
			}
		});
		// addKeyListener(new KeyAdapter() {
		//
		// @Override
		// public void keyPressed(KeyEvent e) {
		// if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		// plugInPort.setNewComponentSlot(null);
		// }
		// }
		// });
		return button;
	}

	// private void initializeDnD() {
	// // Initialize drag source recognizer.
	// DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
	// this,
	// DnDConstants.ACTION_MOVE,
	// new ToolboxGestureListener(plugInPort, componentType
	// .getInstanceClass().getName()));
	// }

	public static JMenuItem createTemplateItem(final IPlugInPort plugInPort,
			final Template template, final ComponentType componentType) {
		final JMenuItem item = new JMenuItem(template.getName()) {

			private static final long serialVersionUID = 1L;

			// Customize item size to fit the delete button
			public java.awt.Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				return new Dimension(d.width + 18, d.height);
			}
		};
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				plugInPort.setNewComponentTypeSlot(componentType, template);
			}
		});
		JLabel label = new JLabel(IconLoader.Garbage.getIcon());
		label.setToolTipText("Delete template");
		label.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// Hide the menu
				Container c = item.getParent();
				if (c != null && c instanceof JPopupMenu) {
					JPopupMenu m = (JPopupMenu) c;
					m.setVisible(false);
				}
				int result = JOptionPane.showConfirmDialog(SwingUtilities
						.getRoot(item),
						"Are you sure you want to delete template \""
								+ template.getName() + "\"", "Delete",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}
				plugInPort.deleteTemplate(componentType.getCategory(),
						componentType.getName(), template.getName());
				e.consume();
			}
		});
		item.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		item.add(label);
		return item;
	}
}
