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
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.service.SearchSession;
import org.diylc.swing.ISimpleView;
import org.diylc.swing.ISwingUI;
import org.diylc.utils.IconLoader;
import org.diylc.utils.Pair;

/**
 * {@link JFrame} for searching the projects on the cloud.
 * 
 * @author Branislav Stojkovic
 */
public class CloudBrowserFrame extends JFrame implements ISimpleView {

  private static final String TITLE = "Search The Cloud";

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(CloudBrowserFrame.class);

  private JPanel mainPanel;
  private JTabbedPane tabbedPane;
  private JPanel dashboardPanel;
  private JPanel searchPanel;

  private SearchHeaderPanel searchHeaderPanel;
  private ResultsScrollPanel resultsScrollPane;

  private IPlugInPort plugInPort;
  private SearchSession searchSession;

  private ISwingUI swingUI;

  public CloudBrowserFrame(ISwingUI swingUI, IPlugInPort plugInPort) {
    super(TITLE);
    this.swingUI = swingUI;
    this.setIconImage(IconLoader.Cloud.getImage());
    this.setPreferredSize(new Dimension(700, 640));
    this.plugInPort = plugInPort;
    this.searchSession = new SearchSession(plugInPort);

    setContentPane(getSearchPanel());
    this.pack();
    this.setLocationRelativeTo(swingUI.getOwnerFrame());
    this.setGlassPane(SimpleCloudGlassPane.GLASS_PANE);

    JRootPane rootPane = SwingUtilities.getRootPane(getSearchHeaderPanel().getGoButton());
    rootPane.setDefaultButton(getSearchHeaderPanel().getGoButton());
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
    if (b && !getSearchHeaderPanel().isInitialized()) {
      executeBackgroundTask(new ITask<Pair<String[], String[]>>() {

        @Override
        public Pair<String[], String[]> doInBackground() throws Exception {
          return new Pair<String[], String[]>(
              plugInPort.getCloudService().getCategories(),
              plugInPort.getCloudService().getSortings());
        }

        @Override
        public void failed(Exception e) {
          LOG.error("Could not fetch categories and sortings from the cloud", e);
          showMessage("Could not fetch categories and sortings from the cloud", "Cloud Error", IView.ERROR_MESSAGE);
        }

        @Override
        public void complete(Pair<String[], String[]> result) {
          getSearchHeaderPanel().initializeLists(result.getFirst(), result.getSecond());
        }
      });
    }
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
      searchHeaderPanel = new SearchHeaderPanel();
      searchHeaderPanel.getGoButton().addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          search();
        }
      });
    }
    return searchHeaderPanel;
  }

  private ResultsScrollPanel getResultsScrollPane() {
    if (resultsScrollPane == null) {
      resultsScrollPane = new ResultsScrollPanel(swingUI, this, plugInPort, searchSession, false);
    }
    return resultsScrollPane;
  }

  private void search() {
    getResultsScrollPane().clearPrevious();
    executeBackgroundTask(new ITask<List<ProjectEntity>>() {

      @Override
      public List<ProjectEntity> doInBackground() throws Exception {
        return searchSession.startSession(getSearchHeaderPanel().getSearchText(),
            getSearchHeaderPanel().getCategory(), getSearchHeaderPanel().getSorting());
      }

      @Override
      public void failed(Exception e) {
        showMessage("Search failed! Detailed message is in the logs. Please report to the author.", "Search Failed",
            IView.ERROR_MESSAGE);
      }

      @Override
      public void complete(List<ProjectEntity> result) {        
        getResultsScrollPane().startSearch(result);
      }
    });
  }

  @Override
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

  @Override
  public void showMessage(String message, String title, int messageType) {
    JOptionPane.showMessageDialog(this, message, title, messageType);
  }

  @Override
  public int showConfirmDialog(String message, String title, int optionType, int messageType) {
    return JOptionPane.showConfirmDialog(this, message, title, optionType, messageType);
  }
  
  @Override
  public JFrame getOwnerFrame() {
    return this;
  }
}
