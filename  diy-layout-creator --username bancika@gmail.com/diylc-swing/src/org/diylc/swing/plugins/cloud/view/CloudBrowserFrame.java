package org.diylc.swing.plugins.cloud.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.presenter.CloudException;
import org.diylc.plugins.cloud.presenter.CloudPresenter;

public class CloudBrowserFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = Logger.getLogger(CloudBrowserFrame.class);

	private JPanel mainPanel;
	private JTabbedPane tabbedPane;
	private JPanel dashboardPanel;
	private JPanel browsePanel;

	private JPanel searchPanel;
	private JTextField searchField;
	private JComboBox categoryBox;
	private JComboBox sortBox;
	private JButton goButton;

	private IPlugInPort plugInPort;
	private CloudPresenter cloudPresenter;

	private int pageNumber;
	private int itemsPerPage = 10;

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
			tabbedPane.addTab("Dashboard", IconLoader.Dashboard.getIcon(),
					getDashboardPanel());
			tabbedPane.addTab("Search For Projects", IconLoader.Find.getIcon(),
					getBrowsePanel());
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
			browsePanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.NORTH;
			gbc.insets = new Insets(2, 2, 2, 2);
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.weightx = 1;
			browsePanel.add(getSearchPanel(), gbc);

			gbc.gridy = 1;
			gbc.weighty = 1;
			gbc.fill = GridBagConstraints.BOTH;
			browsePanel.add(new JPanel(), gbc);
		}
		return browsePanel;
	}

	private JPanel getSearchPanel() {
		if (searchPanel == null) {
			searchPanel = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.LINE_START;
			gbc.insets = new Insets(2, 2, 2, 2);

			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 0;
			searchPanel.add(new JLabel("Search For:"), gbc);

			gbc.gridx = 1;
			gbc.weightx = 1;
			searchPanel.add(getSearchField(), gbc);

			gbc.gridx = 2;
			gbc.weightx = 0;
			searchPanel.add(new JLabel("Filter By Category:"), gbc);

			gbc.gridx = 3;
			gbc.weightx = 1;
			searchPanel.add(getCategoryBox(), gbc);

			gbc.gridx = 4;
			gbc.weightx = 0;
			searchPanel.add(new JLabel("Sort By:"), gbc);

			gbc.gridx = 5;
			searchPanel.add(getSortBox(), gbc);

			gbc.gridx = 6;
			searchPanel.add(getGoButton(), gbc);
		}
		return searchPanel;
	}

	private JTextField getSearchField() {
		if (searchField == null) {
			searchField = new JTextField();
		}
		return searchField;
	}

	private JComboBox getCategoryBox() {
		if (categoryBox == null) {
			try {
				categoryBox = new JComboBox(cloudPresenter.getCategories());
			} catch (CloudException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return categoryBox;
	}

	private JComboBox getSortBox() {
		if (sortBox == null) {
			sortBox = new JComboBox();
		}
		return sortBox;
	}

	public JButton getGoButton() {
		if (goButton == null) {
			goButton = new JButton("Go", IconLoader.DataFind.getIcon());
			goButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					CloudBrowserFrame.this.pageNumber = 1;

					executeBackgroundTask(new ITask<List<ProjectEntity>>() {

						@Override
						public List<ProjectEntity> doInBackground()
								throws Exception {
							return cloudPresenter
									.search(getSearchField().getText(),
											getCategoryBox().getSelectedItem() == null ? ""
													: getCategoryBox()
															.getSelectedItem()
															.toString(),
											getSortBox().getSelectedItem() == null ? ""
													: getSortBox()
															.getSelectedItem()
															.toString(),
											CloudBrowserFrame.this.pageNumber,
											CloudBrowserFrame.this.itemsPerPage);
						}

						@Override
						public void failed(Exception e) {
							showMessage(
									"Search failed. Error: " + e.getMessage(),
									"Search Failed", IView.ERROR_MESSAGE);
						}

						@Override
						public void complete(List<ProjectEntity> result) {
							// TODO Auto-generated method stub

						}
					});
				}
			});
		}
		return goButton;
	}

	public <T extends Object> void executeBackgroundTask(final ITask<T> task) {
		getGlassPane().setVisible(true);
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
				} catch (ExecutionException e) {
					LOG.error("Background task execution failed", e);
					task.failed(e);
				} catch (InterruptedException e) {
					LOG.error("Background task execution interrupted", e);
					task.failed(e);
				}
				getGlassPane().setVisible(false);
			}
		};
		worker.execute();
	}

	public void showMessage(String message, String title, int messageType) {
		JOptionPane.showMessageDialog(this, message, title, messageType);
	}
}
