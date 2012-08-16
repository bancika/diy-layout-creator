package org.diylc.swing.plugins.toolbox;

import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IDIYComponent;

/**
 * {@link JButton} that displays component type icon and instantiates the
 * component when clicked.
 * 
 * @author Branislav Stojkovic
 */
class ComponentButton extends JButton {

	private static final long serialVersionUID = 1L;

	public static int MARGIN = 2;

	private IPlugInPort plugInPort;
	private ComponentType componentType;

	public ComponentButton(final IPlugInPort plugInPort, final ComponentType componentType) {
		super(componentType.getIcon());
		this.plugInPort = plugInPort;
		this.componentType = componentType;

		setMargin(new Insets(MARGIN, MARGIN, MARGIN, MARGIN));

		setToolTipText("<html><b>" + componentType.getName() + "</b><br>"
				+ componentType.getDescription() + "<br>Author: " + componentType.getAuthor()
				+ "<br><br>Double click to select all components of this type"
				+ "</html>");
		// initializeDnD();
		addMouseListener(new MouseAdapter() {
			
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					plugInPort.setNewComponentTypeSlot(componentType);	
				} else {
					List<IDIYComponent<?>> components = plugInPort.getCurrentProject().getComponents();
					List<IDIYComponent<?>> newSelection = new ArrayList<IDIYComponent<?>>();
					for (IDIYComponent<?> component : components) {
						if (componentType.getInstanceClass().equals(component.getClass())) {
							newSelection.add(component);
						}
					}
					plugInPort.updateSelection(newSelection);
					plugInPort.setNewComponentTypeSlot(null);
					plugInPort.refresh();
				}				
			}
		});
//		addActionListener(new ActionListener() {
//
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				// plugInPort.setCursorIcon(componentType.getIcon());
//				plugInPort.setNewComponentTypeSlot(componentType);
//				// try {
//				// plugInPort.instantiateComponent(componentType
//				// .getComponentInstanceClass(), null);
//				// } catch (Exception e1) {
//				// e1.printStackTrace();
//				// }
//			}
//		});
		addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					plugInPort.deleteSelectedComponents();
				}
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
	}

	private void initializeDnD() {
		// Initialize drag source recognizer.
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(this,
				DnDConstants.ACTION_MOVE,
				new ToolboxGestureListener(plugInPort, componentType.getInstanceClass().getName()));
	}
}
