package com.diyfever.diylc.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.diyfever.diylc.common.BadPositionException;
import com.diyfever.diylc.model.Project;
import com.diyfever.diylc.plugins.canvas.CanvasPlugin;
import com.diyfever.diylc.plugins.clipboard.ClipboardManager;
import com.diyfever.diylc.plugins.file.FileManager;
import com.diyfever.diylc.plugins.help.HelpManager;
import com.diyfever.diylc.plugins.online.OnlineManager;
import com.diyfever.diylc.plugins.statusbar.StatusBar;
import com.diyfever.diylc.plugins.toolbox.ToolBox;
import com.diyfever.diylc.presenter.Presenter;

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

	public MainFrame() {
		super("DIYLC4");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));
		createBasePanels();
		menuMap = new HashMap<String, JMenu>();
		DialogFactory.getInstance().initialize(this);

		this.presenter = new Presenter(this);

		setTitle(getTitle() + " " + presenter.getCurrentVersionNumber());

		presenter.installPlugin(new ToolBox());
		presenter.installPlugin(new FileManager());
		presenter.installPlugin(new ClipboardManager());
		presenter.installPlugin(new OnlineManager());
		presenter.installPlugin(new HelpManager());

		presenter.installPlugin(new StatusBar());
		presenter.installPlugin(new CanvasPlugin());

		Project project = new Project();
//		project.getComponents().add(new MockComponentInstance());
		presenter.loadProject(project, true);

		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosed(WindowEvent e) {
				presenter.dispose();
			}

			@Override
			public void windowClosing(WindowEvent e) {
				presenter.dispose();
			}
		});

		// setGlassPane(new CustomGlassPane());
		// getGlassPane().setVisible(true);
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
		JMenu menu;
		if (menuMap.containsKey(menuName)) {
			menu = menuMap.get(menuName);
		} else {
			menu = new JMenu(menuName);
			menuMap.put(menuName, menu);
			getMainMenuBar().add(menu);
		}
		if (action == null) {
			menu.addSeparator();
		} else {
			menu.add(action);
		}
	}

	@Override
	public void setCursorIcon(Icon icon) {
		// TODO Auto-generated method stub

	}
}
