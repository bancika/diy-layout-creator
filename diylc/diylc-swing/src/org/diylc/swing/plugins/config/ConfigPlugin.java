package org.diylc.swing.plugins.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.components.autocreate.SolderPadAutoCreator;
import org.diylc.core.Theme;
import org.diylc.images.IconLoader;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Controls configuration menu.
 * 
 * @author Branislav Stojkovic
 */
public class ConfigPlugin implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(ConfigPlugin.class);

  private static final String CONFIG_MENU = "Config";
  private static final String THEME_MENU = "Theme";
  private static final String COMPONENT_BROWSER_MENU = "Toolbox";
  public static final String COMPONENT_BROWSER = "componentBrowser";
  public static final String SEARCHABLE_TREE = "Searchable Tree";
  public static final String TABBED_TOOLBAR = "Tabbed Toolbar";

  private ISwingUI swingUI;

  public ConfigPlugin(ISwingUI swingUI) {
    super();
    this.swingUI = swingUI;
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    swingUI.injectMenuAction(
        ActionFactory.getInstance()
            .createConfigAction(plugInPort, "Anti-Aliasing", IPlugInPort.ANTI_ALIASING_KEY, true), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Auto-Create Pads",
            SolderPadAutoCreator.AUTO_PADS_KEY, false), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Auto-Edit Mode", IPlugInPort.AUTO_EDIT_KEY, true),
        CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Continuous Creation",
            IPlugInPort.CONTINUOUS_CREATION_KEY, false), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Show Rulers", IPlugInPort.SHOW_RULERS_KEY, true),
        CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Show Grid", IPlugInPort.SHOW_GRID_KEY, true),
        CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Export Grid", IPlugInPort.EXPORT_GRID_KEY, false),
        CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Hi-Quality Rendering",
            IPlugInPort.HI_QUALITY_RENDER_KEY, false), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Mouse Wheel Zoom", IPlugInPort.WHEEL_ZOOM_KEY,
            false), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Outline Mode", IPlugInPort.OUTLINE_KEY, false),
        CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Snap to Grid", IPlugInPort.SNAP_TO_GRID_KEY, true),
        CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance()
            .createConfigAction(plugInPort, "Sticky Points", IPlugInPort.STICKY_POINTS_KEY, true), CONFIG_MENU);

    File themeDir = new File("themes");
    if (themeDir.exists()) {
      XStream xStream = new XStream(new DomDriver());
      swingUI.injectSubmenu(THEME_MENU, IconLoader.Pens.getIcon(), CONFIG_MENU);
      for (File file : themeDir.listFiles()) {
        if (file.getName().toLowerCase().endsWith(".xml")) {
          try {
            InputStream in = new FileInputStream(file);
            Theme theme = (Theme) xStream.fromXML(in);
            LOG.debug("Found theme: " + theme.getName());
            swingUI.injectMenuAction(ActionFactory.getInstance().createThemeAction(plugInPort, theme), THEME_MENU);
          } catch (Exception e) {
            LOG.error("Could not load theme file " + file.getName(), e);
          }
        }
      }
    }

    swingUI.injectSubmenu(COMPONENT_BROWSER_MENU, IconLoader.Hammer.getIcon(), CONFIG_MENU);

    swingUI.injectMenuAction(ActionFactory.getInstance().createComponentBrowserAction(SEARCHABLE_TREE),
        COMPONENT_BROWSER_MENU);
    swingUI.injectMenuAction(ActionFactory.getInstance().createComponentBrowserAction(TABBED_TOOLBAR),
        COMPONENT_BROWSER_MENU);
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.noneOf(EventType.class);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}
}
