package org.diylc.common;

public interface IKeyProcessor {

	public static final int VK_LEFT = 0x25;
	public static final int VK_UP = 0x26;
	public static final int VK_RIGHT = 0x27;
	public static final int VK_DOWN = 0x28;

	/**
	 * Notifies the presenter that a key hes been pressed on the canvas.
	 * 
	 * @param key
	 * @param ctrlDown
	 * @param shiftDown
	 * @param altDown
	 * 
	 * @return true if the event should be consumed
	 */
	boolean keyPressed(int key, boolean ctrlDown, boolean shiftDown,
			boolean altDown);
}
