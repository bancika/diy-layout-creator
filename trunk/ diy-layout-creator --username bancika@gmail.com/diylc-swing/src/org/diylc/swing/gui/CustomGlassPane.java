package org.diylc.swing.gui;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

public class CustomGlassPane extends JPanel implements MouseListener,
		KeyListener {

	public static final CustomGlassPane GLASS_PANE = new CustomGlassPane();
	
	private static final long serialVersionUID = -5344758920442881290L;

	public CustomGlassPane() {
		addKeyListener(this);
		addMouseListener(this);
		setCursor(new Cursor(Cursor.WAIT_CURSOR));
		setOpaque(false);
	}

	@Override
	public void mouseClicked(final MouseEvent pArg0) {
	}

	@Override
	public void mouseEntered(final MouseEvent pArg0) {
	}

	@Override
	public void mouseExited(final MouseEvent pArg0) {
	}

	@Override
	public void mousePressed(final MouseEvent pArg0) {
	}

	@Override
	public void mouseReleased(final MouseEvent pArg0) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}
}
