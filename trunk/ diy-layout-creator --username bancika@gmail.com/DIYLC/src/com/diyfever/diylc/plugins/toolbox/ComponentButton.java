package com.diyfever.diylc.plugins.toolbox;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.diyfever.diylc.common.IPlugInPort;
import com.diyfever.diylc.model.IComponentType;

/**
 * {@link JButton} that displays component type icon and instantiates the
 * component when clicked.
 * 
 * @author Branislav Stojkovic
 */
class ComponentButton extends JButton {

	private static final long serialVersionUID = 1L;

	private IPlugInPort plugInPort;
	private IComponentType componentType;

	public ComponentButton(final IPlugInPort plugInPort, final IComponentType componentType) {
		super(componentType.getIcon());
		this.plugInPort = plugInPort;
		this.componentType = componentType;

		setToolTipText("<html><b>" + componentType.getName() + "</b><br><br>"
				+ componentType.getDescription() + "</html>");
		// initializeDnD();
		addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// plugInPort.setCursorIcon(componentType.getIcon());
				plugInPort.setNewComponentSlot(componentType);
				// try {
				// plugInPort.instantiateComponent(componentType
				// .getComponentInstanceClass(), null);
				// } catch (Exception e1) {
				// e1.printStackTrace();
				// }
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
		DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(
				this,
				DnDConstants.ACTION_MOVE,
				new ToolboxGestureListener(plugInPort, componentType.getComponentInstanceClass()
						.getName()));
	}
}
