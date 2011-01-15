package org.diylc.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import org.diylc.common.BadPositionException;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.plugins.canvas.CanvasPlugin;
import org.diylc.plugins.clipboard.ClipboardManager;
import org.diylc.plugins.file.FileManager;
import org.diylc.plugins.help.HelpManager;
import org.diylc.plugins.statusbar.StatusBar;
import org.diylc.plugins.toolbox.ToolBox;
import org.diylc.presenter.Presenter;

public class MainFrame extends JFrame implements IView {

	private static final long serialVersionUID = 1L;

	private JPanel centerPanel;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel topPanel;
	private JPanel bottomPanel;

	private Presenter presenter;

	private JMenuBar mainMenuBar;
	private Map<String, JMenu> menuMap;

	private CanvasPlugin canvasPlugin;

	public MainFrame() {
		super("DIYLC 3");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));
		createBasePanels();
		menuMap = new HashMap<String, JMenu>();
		DialogFactory.getInstance().initialize(this);

		this.presenter = new Presenter(this);

		presenter.installPlugin(new ToolBox());
		presenter.installPlugin(new FileManager());
		presenter.installPlugin(new ClipboardManager());
		// presenter.installPlugin(new OnlineManager());
		presenter.installPlugin(new HelpManager());

		presenter.installPlugin(new StatusBar());
		canvasPlugin = new CanvasPlugin();
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
	public void addComponent(JComponent component, int position) throws BadPositionException {
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
	public void addMenuAction(Action action, String menuName) {
		JMenu menu = findOrCreateMenu(menuName);
		if (action == null) {
			menu.addSeparator();
		} else {
			menu.add(action);
		}
	}

	@Override
	public void addSubmenu(String name, Icon icon, String parentMenuName) {
		JMenu menu = findOrCreateMenu(parentMenuName);
		JMenu submenu = new JMenu(name);
		submenu.setIcon(icon);
		menu.add(submenu);
		menuMap.put(name, submenu);
	}

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
				String fileName = plugInPort.getCurrentFileName() == null ? "Untitled" : plugInPort
						.getCurrentFileName();
				String modified = plugInPort.isProjectModified() ? " (modified)" : "";
				setTitle(String.format("DIYLC 3.%s - %s %s", plugInPort.getCurrentVersionNumber(),
						fileName, modified));
			}
		}

	}
}
