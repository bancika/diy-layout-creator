package org.diylc.swing.plugins.toolbox.openide;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPopupMenu;

public final class DropDownButtonFactory {
	public static final String PROP_DROP_DOWN_MENU = "dropDownMenu";

	public static JButton createDropDownButton(Icon icon,
			JPopupMenu dropDownMenu) {
		return new DropDownButton(icon, dropDownMenu);
	}
}
