package org.diylc.swing.plugins.cloud.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.presenter.CloudException;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.CustomGlassPane;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;

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

  private JScrollPane resultsScrollPane;
  private JPanel resultsPanel;

  private IPlugInPort plugInPort;
  private CloudPresenter cloudPresenter;

  // search criteria
  private String searchFor;
  private String category;
  private String sort;
  private int pageNumber;
  private int itemsPerPage = 10;

  public CloudBrowserFrame(JFrame owner, IPlugInPort plugInPort, CloudPresenter cloudPresenter) {
    super("DIY Cloud Browser");
    this.setIconImage(IconLoader.Cloud.getImage());
    this.setPreferredSize(new Dimension(800, 600));
    this.plugInPort = plugInPort;
    this.cloudPresenter = cloudPresenter;

    setContentPane(getMainPanel());
    this.pack();
    this.setLocationRelativeTo(owner);
    this.setGlassPane(new CustomGlassPane());
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
      tabbedPane.addChangeListener(new ChangeListener() {

        @Override
        public void stateChanged(ChangeEvent e) {
          if (getTabbedPane().getSelectedIndex() == 1)
            getSearchField().requestFocus();
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

  private JPanel getBrowsePanel() {
    if (browsePanel == null) {
      browsePanel = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      browsePanel.setBackground(Color.white);
      gbc.anchor = GridBagConstraints.NORTH;
      gbc.insets = new Insets(2, 2, 2, 2);
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.weightx = 1;
      browsePanel.add(getSearchPanel(), gbc);

      // gbc.gridy = 1;
      // gbc.fill = GridBagConstraints.HORIZONTAL;
      // browsePanel.add(new JSeparator(), gbc);

      gbc.gridy = 1;
      gbc.weighty = 1;
      gbc.fill = GridBagConstraints.BOTH;
      browsePanel.add(getResultsScrollPane(), gbc);
    }
    return browsePanel;
  }

  private JPanel getSearchPanel() {
    if (searchPanel == null) {
      searchPanel = new JPanel(new GridBagLayout());
      searchPanel.setBackground(Color.white);
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
      gbc.weightx = 0.3;
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
      searchField = new JTextField(60);
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
      try {
        sortBox = new JComboBox(cloudPresenter.getSortings());
      } catch (CloudException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return sortBox;
  }

  private JScrollPane getResultsScrollPane() {
    if (resultsScrollPane == null) {
      resultsScrollPane = new JScrollPane(getResultsPanel());
      resultsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
      resultsScrollPane.setBorder(null);
    }
    return resultsScrollPane;
  }

  private JPanel getResultsPanel() {
    if (resultsPanel == null) {
      resultsPanel = new JPanel(new GridBagLayout());
      resultsPanel.setBackground(Color.white);
    }
    return resultsPanel;
  }

  public JButton getGoButton() {
    if (goButton == null) {
      goButton = new JButton("Go", IconLoader.DataFind.getIcon());
      goButton.addActionListener(new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
          CloudBrowserFrame.this.pageNumber = 1;
          CloudBrowserFrame.this.searchFor = getSearchField().getText();
          CloudBrowserFrame.this.category =
              getCategoryBox().getSelectedItem() == null ? "" : getCategoryBox().getSelectedItem().toString();
          CloudBrowserFrame.this.sort =
              getSortBox().getSelectedItem() == null ? "" : getSortBox().getSelectedItem().toString();

          search();
        }
      });
    }
    return goButton;
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
        getResultsPanel().removeAll();
        int count = 0;
        for (Iterator<ProjectEntity> i = result.iterator(); i.hasNext();) {
          ProjectEntity project = i.next();
          addProjectToDisplay(project, count++);
        }
        if (count == 0) {
          showNoMatches();
          count++;
        }
        addNavigationPanel(count);
      }
    });
  }

  private void showNoMatches() {
    JLabel label = new JLabel("<html><font size='5' color='red'>No projects match the search criteria.</font></html>");
    label.setHorizontalAlignment(SwingConstants.CENTER);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    gbc.weighty = Integer.MAX_VALUE;
    getResultsPanel().add(label, gbc);
  }

  private void addProjectToDisplay(final ProjectEntity project, int location) {
    JLabel thumbnailLabel = new JLabel(downloadImage(project.getThumbnailUrl()));
    JLabel nameLabel = new JLabel("<html><b>" + project.getName() + "</b></html>");
    nameLabel.setFont(nameLabel.getFont().deriveFont(12f));
    JLabel descriptionLabel = new JLabel("<html>" + project.getDescription() + "</html>");
    JLabel categoryLabel = new JLabel("<html>Category: <b>" + project.getCategory() + "</b></html>");
    JLabel authorLabel = new JLabel("<html>Author: <b>" + project.getOwner() + "</b></html>");
    JLabel updatedLabel = new JLabel("<html>Last updated: <b>" + project.getUpdated() + "</b></html>");
    JButton downloadButton = new JButton(IconLoader.CloudDownload.getIcon());

    downloadButton.setBorderPainted(false);
    // downloadButton.setFocusPainted(false);
    downloadButton.setContentAreaFilled(false);
    downloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    downloadButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        final File file =
            DialogFactory.getInstance().showSaveDialog(CloudBrowserFrame.this, FileFilterEnum.DIY.getFilter(),
                new File(project.getName() + ".diy"), FileFilterEnum.DIY.getExtensions()[0], null);
        if (file != null) {
          CloudBrowserFrame.this.executeBackgroundTask(new ITask<Void>() {

            @Override
            public Void doInBackground() throws Exception {
              LOG.debug("Downloading project to to " + file.getAbsolutePath());
              URL website = new URL(project.getDownloadUrl());
              ReadableByteChannel rbc = Channels.newChannel(website.openStream());
              FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
              fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
              fos.close();
              return null;
            }

            @Override
            public void complete(Void result) {
              if (CloudBrowserFrame.this.showConfirmDialog("Project downloaded to " + file.getAbsolutePath()
                  + ".\nDo you want to open it?", "Cloud", ISwingUI.YES_NO_OPTION, ISwingUI.INFORMATION_MESSAGE) == IView.YES_OPTION) {
                if (!plugInPort.allowFileAction()) {
                  return;
                }

                CloudBrowserFrame.this.executeBackgroundTask(new ITask<Void>() {

                  @Override
                  public Void doInBackground() throws Exception {
                    LOG.debug("Opening from " + file.getAbsolutePath());
                    plugInPort.loadProjectFromFile(file.getAbsolutePath());
                    return null;
                  }

                  @Override
                  public void complete(Void result) {}

                  @Override
                  public void failed(Exception e) {
                    CloudBrowserFrame.this.showMessage("Could not open file. Detailed message is in the logs", "Error",
                        ISwingUI.ERROR_MESSAGE);
                  }
                });
              }
            }

            @Override
            public void failed(Exception e) {
              CloudBrowserFrame.this.showMessage("Could not save to file. Detailed message is in the logs", "Error",
                  ISwingUI.ERROR_MESSAGE);
            }
          });
        }
      }
    });

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridx = 0;
    gbc.gridy = location * 6;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridheight = 5;
    gbc.weightx = 0;
    getResultsPanel().add(thumbnailLabel, gbc);

    gbc.gridheight = 1;
    gbc.gridx = 1;
    gbc.weightx = 1;
    gbc.insets = new Insets(2, 6, 2, 2);
    getResultsPanel().add(nameLabel, gbc);
    //
    // gbc.gridx = 2;
    // gbc.anchor = GridBagConstraints.NORTHEAST;
    // gbc.insets = new Insets(2, 2, 2, 8);
    // getResultsPanel().add(authorLabel, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    gbc.weighty = 1;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(2, 6, 2, 2);
    getResultsPanel().add(descriptionLabel, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    gbc.weighty = 0;
    getResultsPanel().add(categoryLabel, gbc);

    gbc.gridx = 2;
    gbc.gridheight = 3;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.anchor = GridBagConstraints.SOUTHEAST;
    getResultsPanel().add(downloadButton, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    gbc.gridheight = 1;
    gbc.insets = new Insets(2, 6, 2, 2);
    gbc.anchor = GridBagConstraints.NORTHWEST;
    getResultsPanel().add(authorLabel, gbc);

    gbc.gridy++;
    getResultsPanel().add(updatedLabel, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 3;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    gbc.insets = new Insets(2, 2, 2, 2);
    getResultsPanel().add(new JSeparator(), gbc);
  }

  private void addNavigationPanel(int location) {
    JPanel navigationPanel = new JPanel(new GridBagLayout());
    navigationPanel.setBackground(Color.white);

    JButton prevPageButton = new JButton(IconLoader.NavLeftBlue.getIcon());
    prevPageButton.setBorderPainted(false);
    prevPageButton.setContentAreaFilled(false);
    prevPageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    prevPageButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        if (CloudBrowserFrame.this.pageNumber > 1) {
          CloudBrowserFrame.this.pageNumber--;
          CloudBrowserFrame.this.search();
        }
      }
    });

    JButton nextPageButton = new JButton(IconLoader.NavRightBlue.getIcon());
    nextPageButton.setBorderPainted(false);
    nextPageButton.setContentAreaFilled(false);
    nextPageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    nextPageButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        CloudBrowserFrame.this.pageNumber++;
        CloudBrowserFrame.this.search();
      }
    });

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.SOUTHWEST;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridx = 0;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    navigationPanel.add(prevPageButton, gbc);

    gbc.gridx++;
    gbc.weightx = 1;
    navigationPanel.add(new JLabel(), gbc);

    gbc.gridx++;
    gbc.weightx = 0;
    navigationPanel.add(nextPageButton, gbc);

    gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridx = 0;
    gbc.gridy = location * 6;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weighty = 100;
    gbc.gridwidth = 3;
    getResultsPanel().add(navigationPanel, gbc);
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
          getGlassPane().setVisible(false);
        } catch (ExecutionException e) {
          LOG.error("Background task execution failed", e);
          getGlassPane().setVisible(false);
          task.failed(e);
        } catch (InterruptedException e) {
          getGlassPane().setVisible(false);
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

  private Icon downloadImage(String imageUrl) {
    Image image = null;
    try {
      URL url = new URL(imageUrl);
      image = ImageIO.read(url);
    } catch (IOException e) {
    }
    return new ImageIcon(image);
  }
}
