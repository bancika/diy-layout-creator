package org.diylc.plugins.toolbox;

import java.awt.Insets;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

import org.diylc.common.ComponentType;
import org.diylc.common.IPlugInPort;

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
				+ "</html>");
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
