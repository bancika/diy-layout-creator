package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swing.ISwingUI;

public class CloudBrowserFrame extends JFrame {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(CloudBrowserFrame.class);

  private JPanel mainPanel;
  private JTabbedPane tabbedPane;
  private JPanel dashboardPanel;
  private JPanel searchPanel;

  private SearchHeaderPanel searchHeaderPanel;
  private ResultsScrollPanel resultsScrollPane;

  private IPlugInPort plugInPort;
  private CloudPresenter cloudPresenter;

  private List<ProjectEntity> currentResults;

  // search criteria
  private String searchFor;
  private String category;
  private String sort;
  private int pageNumber;
  private int itemsPerPage = 10;

  private ISwingUI swingUI;

  public CloudBrowserFrame(ISwingUI swingUI, IPlugInPort plugInPort, CloudPresenter cloudPresenter) {
    super("Search The Cloud");
    this.swingUI = swingUI;
    this.setIconImage(IconLoader.Cloud.getImage());
    this.setPreferredSize(new Dimension(700, 640));
    this.plugInPort = plugInPort;
    this.cloudPresenter = cloudPresenter;

    setContentPane(getSearchPanel());
    this.pack();
    this.setLocationRelativeTo(swingUI.getOwnerFrame());
    this.setGlassPane(new CloudGlassPane());

    JRootPane rootPane = SwingUtilities.getRootPane(getSearchHeaderPanel().getGoButton());
    rootPane.setDefaultButton(getSearchHeaderPanel().getGoButton());
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
      tabbedPane.addTab("Search For Projects", IconLoader.Find.getIcon(), getSearchPanel());
      tabbedPane.setSelectedIndex(1);
      tabbedPane.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          if (getTabbedPane().getSelectedIndex() == 1)
            getSearchHeaderPanel().setFocus();
        }
      });
    }
    return tabbedPane;
  }

  private JPanel getDashboardPanel() {
    if (dashboardPanel == null) {
      dashboardPanel = new JPanel();
    }
    return dashboardPanel;
  }

  private JPanel getSearchPanel() {
    if (searchPanel == null) {
      searchPanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      searchPanel.setBackground(Color.white);
      gbc.anchor = GridBagConstraints.NORTH;
      gbc.insets = new Insets(2, 2, 2, 2);
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;
      searchPanel.add(getSearchHeaderPanel(), gbc);

      gbc.gridy = 1;
      gbc.weighty = 1;
      gbc.fill = GridBagConstraints.BOTH;
      searchPanel.add(getResultsScrollPane(), gbc);
    }
    return searchPanel;
  }

  private SearchHeaderPanel getSearchHeaderPanel() {
    if (searchHeaderPanel == null) {
      searchHeaderPanel = new SearchHeaderPanel(cloudPresenter);
      searchHeaderPanel.getGoButton().addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          CloudBrowserFrame.this.pageNumber = 1;
          CloudBrowserFrame.this.searchFor = getSearchHeaderPanel().getSearchText();
          CloudBrowserFrame.this.category = getSearchHeaderPanel().getCategory();
          CloudBrowserFrame.this.sort = getSearchHeaderPanel().getSorting();
          getResultsScrollPane().startOver();
          search();
        }
      });
    }
    return searchHeaderPanel;
  }

  private ResultsScrollPanel getResultsScrollPane() {
    if (resultsScrollPane == null) {
      resultsScrollPane = new ResultsScrollPanel(swingUI, this, plugInPort, new ILazyProvider<ProjectEntity>() {

        @Override
        public void requestMoreData() {
          CloudBrowserFrame.this.pageNumber++;
          CloudBrowserFrame.this.search();
        }

        @Override
        public boolean hasMoreData() {
          return currentResults.size() == itemsPerPage;
        }
      });
    }
    return resultsScrollPane;
  }

  private void search() {
    executeBackgroundTask(new ITask<List<ProjectEntity>>() {

      @Override
      public List<ProjectEntity> doInBackground() throws Exception {
        return cloudPresenter.search(searchFor, category, sort, pageNumber, itemsPerPage);
      }

      @Override
      public void failed(Exception e) {
        showMessage("Search failed! Detailed message is in the logs. Please report to the author.", "Search Failed",
            IView.ERROR_MESSAGE);
      }

      @Override
      public void complete(List<ProjectEntity> result) {
        CloudBrowserFrame.this.currentResults = result;
        getResultsScrollPane().addData(result);
      }
    });
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
        getGlassPane().setVisible(false);
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
      }
    };
    worker.execute();
  }

  public void showMessage(String message, String title, int messageType) {
    JOptionPane.showMessageDialog(this, message, title, messageType);
  }

  public int showConfirmDialog(String message, String title, int optionType, int messageType) {
    return JOptionPane.showConfirmDialog(this, message, title, optionType, messageType);
  }
}
