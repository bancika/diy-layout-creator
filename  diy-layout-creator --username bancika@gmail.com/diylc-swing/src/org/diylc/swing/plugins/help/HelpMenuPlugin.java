package org.diylc.swing.plugins.help;

import java.awt.event.ActionEvent;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swingframework.AboutDialog;
import org.diylc.swingframework.LinkLabel;
import org.diylc.swingframework.miscutils.Utils;

/**
 * Entry point class for help-related utilities.
 * 
 * @author Branislav Stojkovic
 */
public class HelpMenuPlugin implements IPlugIn {

	private static final String HELP_TITLE = "Help";

	public static String MANUAL_URL = "http://code.google.com/p/diy-layout-creator/wiki/Manual";
	public static String FAQ_URL = "http://code.google.com/p/diy-layout-creator/wiki/FAQ";
	public static String COMPONENT_URL = "http://code.google.com/p/diy-layout-creator/wiki/ComponentAPI";
	public static String PLUGIN_URL = "http://code.google.com/p/diy-layout-creator/wiki/PluginAPI";
	public static String BUG_URL = "http://code.google.com/p/diy-layout-creator/issues/entry";
	public static String DONATE_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=25161";

	private IPlugInPort plugInPort;
	private AboutDialog aboutDialog;

	public HelpMenuPlugin(ISwingUI swingUI) {
		swingUI.injectMenuAction(new NavigateURLAction("User Manual", IconLoader.Manual.getIcon(),
				MANUAL_URL), HELP_TITLE);
		swingUI.injectMenuAction(new NavigateURLAction("FAQ", IconLoader.Faq.getIcon(), FAQ_URL),
				HELP_TITLE);
		swingUI.injectMenuAction(new NavigateURLAction("Component API", IconLoader.Component
				.getIcon(), COMPONENT_URL), HELP_TITLE);
		swingUI.injectMenuAction(new NavigateURLAction("Plugin API", IconLoader.Plugin.getIcon(),
				PLUGIN_URL), HELP_TITLE);
		swingUI.injectMenuAction(new NavigateURLAction("Submit a Bug", IconLoader.Bug.getIcon(),
				BUG_URL), HELP_TITLE);
		swingUI.injectMenuAction(null, HELP_TITLE);
		swingUI.injectMenuAction(new NavigateURLAction("Donate", IconLoader.Donate.getIcon(),
				DONATE_URL), HELP_TITLE);
		swingUI.injectMenuAction(new AboutAction(), HELP_TITLE);
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
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
			aboutDialog = DialogFactory.getInstance().createAboutDialog("DIY Layout Creator",
					IconLoader.IconLarge.getIcon(),
					plugInPort.getCurrentVersionNumber().toString(), "Branislav Stojkovic",
					"diylc.org", "bancika@gmail.com", "");
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

	class NavigateURLAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private String url;

		public NavigateURLAction(String name, Icon icon, String url) {
			super();
			this.url = url;
			putValue(AbstractAction.NAME, name);
			putValue(AbstractAction.SMALL_ICON, icon);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Utils.openURL(url);
			} catch (Exception e1) {
				Logger.getLogger(LinkLabel.class).error("Could not launch default browser", e1);
			}
		}
	}
}
