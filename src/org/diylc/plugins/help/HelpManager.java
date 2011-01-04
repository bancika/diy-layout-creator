package org.diylc.plugins.help;

import java.awt.event.ActionEvent;
import java.util.EnumSet;

import javax.swing.AbstractAction;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.gui.DialogFactory;
import org.diylc.images.IconLoader;

import com.diyfever.gui.AboutDialog;

/**
 * Entry point class for help-related utilities.
 * 
 * @author Branislav Stojkovic
 */
public class HelpManager implements IPlugIn {

	private static final String HELP_TITLE = "Help";

	private IPlugInPort plugInPort;
	private AboutDialog aboutDialog;

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;

		plugInPort.injectMenuAction(new AboutAction(), HELP_TITLE);
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return null;
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
	}

	private AboutDialog getAboutDialog() {
		if (aboutDialog == null) {
			aboutDialog = DialogFactory.getInstance().createAboutDialog("DIY Layout Creator 4",
					IconLoader.BlackBoard.getIcon(),
					plugInPort.getCurrentVersionNumber().toString(), "Bane Stojkovic",
					"diy-fever.com", "bancika@gmail.com", "");
		}
		return aboutDialog;
	}

	class AboutAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		public AboutAction() {
			super();
			putValue(AbstractAction.NAME, "About");
			putValue(AbstractAction.SMALL_ICON, IconLoader.About.getIcon());
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			getAboutDialog().setVisible(true);
		}
	}
}
