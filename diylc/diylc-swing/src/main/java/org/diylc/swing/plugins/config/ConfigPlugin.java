/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2025 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.swing.plugins.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.ConfigurationManager;
import org.diylc.appframework.miscutils.IConfigListener;
import org.diylc.components.autocreate.SolderPadAutoCreator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;

import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.Theme;
import org.diylc.utils.FlagLoader;
import org.diylc.lang.LangUtil;
import org.diylc.lang.Language;
import org.diylc.swing.ActionFactory;
import org.diylc.swing.ISwingUI;
import org.diylc.utils.IconLoader;
import org.diylc.utils.ResourceUtils;

/**
 * Controls configuration menu.
 * 
 * @author Branislav Stojkovic
 */
public class ConfigPlugin implements IPlugIn {

  private static final Logger LOG = Logger.getLogger(ConfigPlugin.class);

  private static final String CONFIG_MENU = "Config";
  private static final String THEME_MENU = "Theme";
  private static final String RULER_MENU = "Ruler Inch Subdivision...";
  private static final String SNAP_MENU = "Snap To";
  private static final String COMPONENT_BROWSER_MENU = "Toolbox";
  public static final String COMPONENT_BROWSER = "componentBrowser";
  public static final String PROJECT_EXPLORER = "projectExplorer";
  public static final String CHATBOT = "chatbot";
  public static final String SEARCHABLE_TREE = "Searchable Tree";
  public static final String TABBED_TOOLBAR = "Tabbed Toolbar";
  private static final String LANGUAGE_MENU = LangUtil.translate("Language");
  
  private static final String LANG_RESTART = LangUtil.translate("Language selection will be applied after the application is restarted.");
  public static final String THEMES_DIR = "themes";

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
        ActionFactory.getInstance().createConfigAction(plugInPort, "Auto-Convert Units",
            IPlugInPort.AUTO_UNIT_CONVERSION_KEY, true), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Auto-Edit Mode", IPlugInPort.AUTO_EDIT_KEY, true),
        CONFIG_MENU);    
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Continuous Creation",
            IPlugInPort.CONTINUOUS_CREATION_KEY, false), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Enable Cache Boost",
            IPlugInPort.CACHING_ENABLED_KEY, true), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Export Grid", IPlugInPort.EXPORT_GRID_KEY, false),
        CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Extra Working Area", IPlugInPort.EXTRA_SPACE_KEY, true),
        CONFIG_MENU);
//    swingUI.injectMenuAction(
//        ActionFactory.getInstance().createConfigAction(plugInPort, "Hardware Acceleration", IPlugInPort.HARDWARE_ACCELERATION, false),
//        CONFIG_MENU);
    swingUI.injectMenuAction(
            ActionFactory.getInstance().createConfigAction(plugInPort, "High DPI Rendering",
                    IPlugInPort.HIGH_DPI_RENDERING, false), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Hi-Quality Rendering",
            IPlugInPort.HI_QUALITY_RENDER_KEY, false), CONFIG_MENU);
    
    try {
      List<Language> languages = LangUtil.getAvailableLanguages(); 
      if (languages != null && !languages.isEmpty()) {
        swingUI.injectSubmenu(LANGUAGE_MENU, IconLoader.Earth.getIcon(), CONFIG_MENU);
        for(Language language : languages) {
          Icon icon = null;
          if (language.getProperties().containsKey("icon"))
            icon = FlagLoader.getIcon(language.getProperties().get("icon"));
          swingUI.injectMenuAction(                              
              ActionFactory.getInstance().createToggleAction(language.getName(), IPlugInPort.LANGUAGE, LANGUAGE_MENU, IPlugInPort.LANGUAGE_DEFAULT, 
                  icon),
              LANGUAGE_MENU);       
        }
      }
    } catch (Exception e) {
      LOG.error("Error while setting up language menu", e);
    }
   
    
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Mouse Wheel Zoom", IPlugInPort.WHEEL_ZOOM_KEY,
            false), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Outline Mode", IPlugInPort.OUTLINE_KEY, false),
        CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Renumber On Paste", IPlugInPort.RENUMBER_ON_PASTE_KEY, true),
        CONFIG_MENU);
    
    swingUI.injectSubmenu(RULER_MENU, IconLoader.TapeMeasure.getIcon(), CONFIG_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createToggleAction(IPlugInPort.RULER_IN_SUBDIVISION_10, IPlugInPort.RULER_IN_SUBDIVISION_KEY, RULER_MENU, IPlugInPort.RULER_IN_SUBDIVISION_DEFAULT),
        RULER_MENU); 
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createToggleAction(IPlugInPort.RULER_IN_SUBDIVISION_2, IPlugInPort.RULER_IN_SUBDIVISION_KEY, RULER_MENU, IPlugInPort.RULER_IN_SUBDIVISION_DEFAULT),
        RULER_MENU);

    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Show AI Assistant",
            ConfigPlugin.CHATBOT, true), CONFIG_MENU);
    
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Show Project Explorer",
            ConfigPlugin.PROJECT_EXPLORER, false), CONFIG_MENU);
    
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Show Rulers", IPlugInPort.SHOW_RULERS_KEY, true),
        CONFIG_MENU);
    
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Show Resize Dimensions",
            IPlugInPort.SHOW_RESIZE_DIMENSIONS_TOOLTIP_KEY, true), CONFIG_MENU);
    
    swingUI.injectSubmenu(SNAP_MENU, IconLoader.GraphEdgeDirected.getIcon(), CONFIG_MENU);    
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createToggleAction(IPlugInPort.SNAP_TO_NONE, IPlugInPort.SNAP_TO_KEY, SNAP_MENU, IPlugInPort.SNAP_TO_DEFAULT),
        SNAP_MENU);    
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createToggleAction(IPlugInPort.SNAP_TO_GRID, IPlugInPort.SNAP_TO_KEY, SNAP_MENU, IPlugInPort.SNAP_TO_DEFAULT),
        SNAP_MENU);
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createToggleAction(IPlugInPort.SNAP_TO_COMPONENTS, IPlugInPort.SNAP_TO_KEY, SNAP_MENU, IPlugInPort.SNAP_TO_DEFAULT),
        SNAP_MENU);  
    
    swingUI.injectMenuAction(
        ActionFactory.getInstance().createConfigAction(plugInPort, "Show Grid", IPlugInPort.SHOW_GRID_KEY, true),
        CONFIG_MENU);
    
    swingUI.injectMenuAction(
        ActionFactory.getInstance()
            .createConfigAction(plugInPort, "Sticky Points", IPlugInPort.STICKY_POINTS_KEY, true), CONFIG_MENU);      

    Map<String, String> themeContents = ResourceUtils.getResourceContents(THEMES_DIR);
    if (!themeContents.isEmpty()) {
      XStream xStream = new XStream(new DomDriver());
      xStream.addPermission(AnyTypePermission.ANY);
      swingUI.injectSubmenu(THEME_MENU, IconLoader.Pens.getIcon(), CONFIG_MENU);
      themeContents.entrySet().stream()
          .sorted(Map.Entry.comparingByKey())
          .forEach(entry -> {
            if (entry.getKey().toLowerCase().endsWith(".xml")) {
              try {
                Theme theme = (Theme) xStream.fromXML(entry.getValue());
                LOG.debug("Found theme: " + theme.getName());
                swingUI.injectMenuAction(ActionFactory.getInstance().createThemeAction(plugInPort, theme), THEME_MENU);
              } catch (Exception e) {
                LOG.error("Could not load theme file " + entry.getKey(), e);
              }
            }
          });
    }

    swingUI.injectSubmenu(COMPONENT_BROWSER_MENU, IconLoader.Hammer.getIcon(), CONFIG_MENU);

    swingUI.injectMenuAction(ActionFactory.getInstance().createComponentBrowserAction(SEARCHABLE_TREE),
        COMPONENT_BROWSER_MENU);
    swingUI.injectMenuAction(ActionFactory.getInstance().createComponentBrowserAction(TABBED_TOOLBAR),
        COMPONENT_BROWSER_MENU);
    
    // notify the user that language selection is not immediate
    ConfigurationManager.getInstance().addConfigListener(IPlugInPort.LANGUAGE, new IConfigListener() {
      
      @Override
      public void valueChanged(String key, Object value) {
        swingUI.showMessage(LANG_RESTART, LANGUAGE_MENU, ISwingUI.INFORMATION_MESSAGE);
      }
    });
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.noneOf(EventType.class);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {}
}
