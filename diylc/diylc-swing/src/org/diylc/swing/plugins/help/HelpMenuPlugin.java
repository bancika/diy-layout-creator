package org.diylc.swing.plugins.help;

import java.awt.event.ActionEvent;
import java.util.EnumSet;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swingframework.AboutDialog;
import org.diylc.swingframework.LinkLabel;

/**
 * Entry point class for help-related utilities.
 * 
 * @author Branislav Stojkovic
 */
public class HelpMenuPlugin implements IPlugIn {

  private static final String HELP_TITLE = "Help";

  public static String MANUAL_URL = "https://github.com/bancika/diy-layout-creator/blob/wiki/Manual.md";
  public static String FAQ_URL = "https://github.com/bancika/diy-layout-creator/blob/wiki/FAQ.md";
  public static String COMPONENT_URL = "https://github.com/bancika/diy-layout-creator/blob/wiki/ComponentAPI.md";
  public static String PLUGIN_URL = "https://github.com/bancika/diy-layout-creator/blob/wiki/PluginAPI.md";
  public static String BUG_URL = "https://github.com/bancika/diy-layout-creator/issues";
  public static String DONATE_URL = "http://diy-fever.com/donate";

  private IPlugInPort plugInPort;
  private AboutDialog aboutDialog;

  public HelpMenuPlugin(ISwingUI swingUI) {
    swingUI.injectMenuAction(new NavigateURLAction("User Manual", IconLoader.Manual.getIcon(), MANUAL_URL), HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("FAQ", IconLoader.Faq.getIcon(), FAQ_URL), HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("Component API", IconLoader.Component.getIcon(), COMPONENT_URL),
        HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("Plugin API", IconLoader.Plugin.getIcon(), PLUGIN_URL), HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("Submit a Bug", IconLoader.Bug.getIcon(), BUG_URL), HELP_TITLE);
    swingUI.injectMenuAction(null, HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("Donate", IconLoader.Donate.getIcon(), DONATE_URL), HELP_TITLE);
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
  public void processMessage(EventType eventType, Object... params) {}

  private AboutDialog getAboutDialog() {
    if (aboutDialog == null) {
      aboutDialog =
          DialogFactory.getInstance().createAboutDialog("DIY Layout Creator", IconLoader.IconLarge.getIcon(),
              plugInPort.getCurrentVersionNumber().toString(), "Branislav Stojkovic",
              "github.com/bancika/diy-layout-creator", "bancika@gmail.com", "");
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
