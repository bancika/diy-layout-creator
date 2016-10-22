package org.diylc.swing.plugins.cloud.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.diylc.common.IPlugInPort;
import org.diylc.images.IconLoader;
import org.diylc.plugins.cloud.presenter.CloudPresenter;

public class CloudBrowserFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel mainPanel;
	private JTabbedPane tabbedPane;
	private JPanel dashboardPanel;
	private JPanel browsePanel;

	private IPlugInPort plugInPort;
	private CloudPresenter cloudPresenter;

	public CloudBrowserFrame(JFrame owner, IPlugInPort plugInPort,
			CloudPresenter cloudPresenter) {
		super("DIY Cloud Browser");
		this.setIconImage(IconLoader.Cloud.getImage());
		this.setPreferredSize(new Dimension(800, 600));
		this.plugInPort = plugInPort;
		this.cloudPresenter = cloudPresenter;

		setContentPane(getMainPanel());
		this.pack();
		this.setLocationRelativeTo(owner);
	}

	public JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTHWEST;
			gbc.insets = new Insets(2, 2, 2, 2);

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1;
			gbc.weighty = 1;
			mainPanel.add(getTabbedPane(), gbc);
		}
		return mainPanel;
	}

	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane();
			tabbedPane.addTab("Dashboard", IconLoader.Dashboard.getIcon(), getDashboardPanel());
			tabbedPane.addTab("Search For Projects", IconLoader.Find.getIcon(), getBrowsePanel());
		}
		return tabbedPane;
	}

	private JPanel getDashboardPanel() {
		if (dashboardPanel == null) {
			dashboardPanel = new JPanel();
		}
		return dashboardPanel;
	}

	private JPanel getBrowsePanel() {
		if (browsePanel == null) {
			browsePanel = new JPanel();
		}
		return browsePanel;
	}
}
