package org.diylc.swing.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;

import org.apache.log4j.Logger;
import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.presenter.Presenter;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.plugins.canvas.CanvasPlugin;
import org.diylc.swing.plugins.config.ConfigPlugin;
import org.diylc.swing.plugins.edit.EditMenuPlugin;
import org.diylc.swing.plugins.file.FileMenuPlugin;
import org.diylc.swing.plugins.help.HelpMenuPlugin;
import org.diylc.swing.plugins.layers.LayersMenuPlugin;
import org.diylc.swing.plugins.statusbar.StatusBar;
import org.diylc.swing.plugins.toolbox.ToolBox;

public class MainFrame extends JFrame implements ISwingUI {

	private static final Logger LOG = Logger.getLogger(MainFrame.class);

	private static final long serialVersionUID = 1L;

	private JPanel centerPanel;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel topPanel;
	private JPanel bottomPanel;

	private Presenter presenter;

	private JMenuBar mainMenuBar;
	private Map<String, JMenu> menuMap;
	private Map<String, ButtonGroup> buttonGroupMap;

	private CanvasPlugin canvasPlugin;

	public MainFrame() {
		super("DIYLC 3");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));
		createBasePanels();
		menuMap = new HashMap<String, JMenu>();
		buttonGroupMap = new HashMap<String, ButtonGroup>();
		setIconImages(Arrays.asList(IconLoader.IconSmall.getImage(), IconLoader.IconMedium
				.getImage(), IconLoader.IconLarge.getImage()));
		DialogFactory.getInstance().initialize(this);

		this.presenter = new Presenter(this);

		presenter.installPlugin(new ToolBox(this));
		presenter.installPlugin(new FileMenuPlugin(this));
		presenter.installPlugin(new EditMenuPlugin(this));
		presenter.installPlugin(new ConfigPlugin(this));
		presenter.installPlugin(new LayersMenuPlugin(this));
		// presenter.installPlugin(new OnlineManager());
		presenter.installPlugin(new HelpMenuPlugin(this));

		presenter.installPlugin(new StatusBar(this));
		canvasPlugin = new CanvasPlugin(this);
		presenter.installPlugin(canvasPlugin);
		presenter.installPlugin(new FramePlugin());

		presenter.createNewProject();

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				if (presenter.allowFileAction()) {
					dispose();
					presenter.dispose();
					System.exit(0);
				}
			}

			@Override
			public void windowClosing(WindowEvent e) {
				if (presenter.allowFileAction()) {
					dispose();
					presenter.dispose();
					System.exit(0);
				}
			}
		});

		// setGlassPane(new CustomGlassPane());
		// getGlassPane().setVisible(true);
	}

	public Presenter getPresenter() {
		return presenter;
	}

	@Override
	public void setVisible(boolean b) {
		super.setVisible(b);
		// TODO: hack to prevent painting issues in the scroll bar rulers. Find
		// a better fix if possible.
		Timer timer = new Timer(500, new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				canvasPlugin.refresh();
			}

		});
		timer.setRepeats(false);
		timer.start();
		// if (b) {
		// SwingUtilities.invokeLater(new Runnable() {
		// @Override
		// public void run() {
		//					
		// }
		// });
		// }
	}

	private void createBasePanels() {
		Container c = new Container();
		c.setLayout(new BorderLayout());

		centerPanel = new JPanel(new BorderLayout());
		c.add(centerPanel, BorderLayout.CENTER);

		topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		c.add(topPanel, BorderLayout.NORTH);

		leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
		c.add(leftPanel, BorderLayout.WEST);

		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		c.add(bottomPanel, BorderLayout.SOUTH);

		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.X_AXIS));
		c.add(rightPanel, BorderLayout.EAST);

		setContentPane(c);
	}

	private JMenuBar getMainMenuBar() {
		if (mainMenuBar == null) {
			mainMenuBar = new JMenuBar();
			setJMenuBar(mainMenuBar);
		}
		return mainMenuBar;
	}

	// IView

	@Override
	public void showMessage(String message, String title, int messageType) {
		JOptionPane.showMessageDialog(this, message, title, messageType);
	}

	@Override
	public int showConfirmDialog(String message, String title, int optionType, int messageType) {
		return JOptionPane.showConfirmDialog(this, message, title, optionType, messageType);
	}

	private JMenu findOrCreateMenu(String menuName) {
		JMenu menu;
		if (menuMap.containsKey(menuName)) {
			menu = menuMap.get(menuName);
		} else {
			menu = new JMenu(menuName);
			menuMap.put(menuName, menu);
			getMainMenuBar().add(menu);
		}
		return menu;
	}

	@Override
	public void injectGUIComponent(JComponent component, int position) throws BadPositionException {
		LOG.info(String.format("injectGUIComponent(%s, %s)", component.getClass().getName(),
				position));
		switch (position) {
		case SwingConstants.TOP:
			topPanel.add(component);
			break;
		case SwingConstants.LEFT:
			leftPanel.add(component);
			break;
		case SwingConstants.BOTTOM:
			bottomPanel.add(component);
			break;
		case SwingConstants.RIGHT:
			rightPanel.add(component);
			break;
		case SwingConstants.CENTER:
			centerPanel.add(component, BorderLayout.CENTER);
			break;
		default:
			throw new BadPositionException();
		}
		pack();
	}

	@Override
	public void injectMenuAction(Action action, String menuName) {
		LOG.info(String.format("injectMenuAction(%s, %s)", action == null ? "Separator" : action
				.getValue(Action.NAME), menuName));
		JMenu menu = findOrCreateMenu(menuName);
		if (action == null) {
			menu.addSeparator();
		} else {
			Boolean isCheckBox = (Boolean) action.getValue(IView.CHECK_BOX_MENU_ITEM);
			String groupName = (String) action.getValue(IView.RADIO_BUTTON_GROUP_KEY);
			if (isCheckBox != null && isCheckBox) {
				menu.add(new JCheckBoxMenuItem(action));
			} else if (groupName != null) {
				ButtonGroup group;
				if (buttonGroupMap.containsKey(groupName)) {
					group = buttonGroupMap.get(groupName);
				} else {
					group = new ButtonGroup();
					buttonGroupMap.put(groupName, group);
				}
				JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
				group.add(item);
				menu.add(item);
			} else {
				menu.add(action);
			}
		}
	}

	@Override
	public void injectSubmenu(String name, Icon icon, String parentMenuName) {
		LOG.info(String.format("injectSubmenu(%s, icon, %s)", name, parentMenuName));
		JMenu menu = findOrCreateMenu(parentMenuName);
		JMenu submenu = new JMenu(name);
		submenu.setIcon(icon);
		menu.add(submenu);
		menuMap.put(name, submenu);
	}

	@Override
	public <T extends Object> void executeBackgroundTask(final ITask<T> task) {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		SwingWorker<T, Void> worker = new SwingWorker<T, Void>() {

			@Override
			protected T doInBackground() throws Exception {
				return task.doInBackground();
			}

			@Override
			protected void done() {
				try {
					T result = get();
					task.complete(result);
				} catch (Exception e) {
					LOG.error("Task failed", e);
					task.failed(e);
				}
				setCursor(Cursor.getDefaultCursor());
			}
		};
		worker.execute();
	}

	class FramePlugin implements IPlugIn {

		private IPlugInPort plugInPort;

		@Override
		public void connect(IPlugInPort plugInPort) {
			this.plugInPort = plugInPort;
		}

		@Override
		public EnumSet<EventType> getSubscribedEventTypes() {
			return EnumSet.of(EventType.FILE_STATUS_CHANGED);
		}

		@Override
		public void processMessage(EventType eventType, Object... params) {
			if (eventType == EventType.FILE_STATUS_CHANGED) {
				String fileName = (String) params[0];
				if (fileName == null) {
					fileName = "Untitled";
				}
				String modified = (Boolean) params[1] ? " (modified)" : "";
				setTitle(String.format("DIYLC %s beta - %s %s", plugInPort
						.getCurrentVersionNumber(), fileName, modified));
			}
		}
	}

}
