package org.diylc.swing.plugins.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.EnumSet;

import org.apache.log4j.Logger;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
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

	private ISwingUI swingUI;

	public ConfigPlugin(ISwingUI swingUI) {
		super();
		this.swingUI = swingUI;
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		swingUI.injectMenuAction(ActionFactory.getInstance().createConfigAction(plugInPort,
				"Anti-Aliasing", IPlugInPort.ANTI_ALIASING_KEY, true), CONFIG_MENU);
		swingUI.injectMenuAction(ActionFactory.getInstance().createConfigAction(plugInPort,
				"Hi-Quality Rendering", IPlugInPort.HI_QUALITY_RENDER_KEY, false), CONFIG_MENU);
		swingUI.injectMenuAction(ActionFactory.getInstance().createConfigAction(plugInPort,
				"Sticky Points", IPlugInPort.STICKY_POINTS_KEY, true), CONFIG_MENU);
		swingUI.injectMenuAction(ActionFactory.getInstance().createConfigAction(plugInPort,
				"Export Grid", IPlugInPort.EXPORT_GRID_KEY, false), CONFIG_MENU);
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
						swingUI.injectMenuAction(ActionFactory.getInstance().createThemeAction(
								plugInPort, theme), THEME_MENU);
					} catch (Exception e) {
						LOG.error("Could not load theme file " + file.getName(), e);
					}
				}
			}
		}
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.noneOf(EventType.class);
	}

	@Override
	public void processMessage(EventType eventType, Object... params) {
	}
}
