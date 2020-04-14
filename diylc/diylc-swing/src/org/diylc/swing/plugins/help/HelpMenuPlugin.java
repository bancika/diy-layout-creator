/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2018 held jointly by the individual authors.

    This file is part of DIYLC.

    DIYLC is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    DIYLC is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with DIYLC.  If not, see <http://www.gnu.org/licenses/>.

*/
package org.diylc.swing.plugins.help;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.EnumSet;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.appframework.update.UpdateChecker;
import org.diylc.appframework.update.Version;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swingframework.AboutDialog;
import org.diylc.swingframework.LinkLabel;
import org.diylc.swingframework.update.UpdateDialog;

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
  private ISwingUI swingUI;

  public HelpMenuPlugin(ISwingUI swingUI) {
    this.swingUI = swingUI;
    swingUI.injectMenuAction(new NavigateURLAction("User Manual", IconLoader.Manual.getIcon(), MANUAL_URL), HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("FAQ", IconLoader.Faq.getIcon(), FAQ_URL), HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("Component API", IconLoader.CoffeebeanEdit.getIcon(), COMPONENT_URL),
        HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("Plugin API", IconLoader.ApplicationEdit.getIcon(), PLUGIN_URL), HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("Submit a Bug", IconLoader.Bug.getIcon(), BUG_URL), HELP_TITLE);
    swingUI.injectMenuAction(new NavigateFolderAction("Access User Files", IconLoader.User.getIcon(), System.getProperty("user.home") + "/.diylc"), HELP_TITLE);
    swingUI.injectMenuAction(null, HELP_TITLE);
    swingUI.injectMenuAction(new RecentUpdatesAction(), HELP_TITLE);
    swingUI.injectMenuAction(null, HELP_TITLE);
    swingUI.injectMenuAction(new NavigateURLAction("Make a Donation", IconLoader.Donate.getIcon(), DONATE_URL), HELP_TITLE);    
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


  private static final String LICENSE_TEXT = "<p>This program is free software: you can redistribute it and/or modify" +
    " it under the terms of the GNU General Public License as published by" +
    " the Free Software Foundation, either version 3 of the License, or" +
    " (at your option) any later version.</p>" +
    "<p>This program is distributed in the hope that it will be useful," +
    " but WITHOUT ANY WARRANTY; without even the implied warranty of" +
    " MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" +
    " GNU General Public License for more details.</p>" +
    "<p>You should have received a copy of the GNU General Public License" +
    " along with this program.  If not, see <a href=\"https://www.gnu.org/licenses\">https://www.gnu.org/licenses</a>.</p>";
  private AboutDialog getAboutDialog() {
    if (aboutDialog == null) {
      aboutDialog =
          DialogFactory.getInstance().createAboutDialog("DIY Layout Creator", IconLoader.IconLarge.getIcon(),
              plugInPort.getCurrentVersionNumber().toString(), "Branislav Stojkovic",
              "github.com/bancika/diy-layout-creator", "bancika@gmail.com", LICENSE_TEXT);
      aboutDialog.setSize(aboutDialog.getSize().width + 30, aboutDialog.getSize().height + 200);
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
  
  class RecentUpdatesAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    public RecentUpdatesAction() {
      super();
      putValue(AbstractAction.NAME, "Recent Updates");
      putValue(AbstractAction.SMALL_ICON, IconLoader.ScrollInformation.getIcon());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      List<Version> updates = plugInPort.getRecentUpdates();
      if (updates == null)
        swingUI.showMessage("Version history is not available.", "Information", IView.INFORMATION_MESSAGE);
      else {
        String html = UpdateChecker.createUpdateHTML(updates);
        UpdateDialog updateDialog = new UpdateDialog(swingUI.getOwnerFrame().getRootPane(), html, (String)null);
        updateDialog.setVisible(true);
      }
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
  
  class NavigateFolderAction extends AbstractAction {

    private static final long serialVersionUID = 1L;

    private String url;

    public NavigateFolderAction(String name, Icon icon, String url) {
      super();
      this.url = url;
      putValue(AbstractAction.NAME, name);
      putValue(AbstractAction.SMALL_ICON, icon);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        Desktop.getDesktop().open(new File((url)));
      } catch (Exception e1) {
        Logger.getLogger(LinkLabel.class).error("Could not launch desktop", e1);
      }
    }
  }
}
