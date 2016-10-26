package org.diylc.swing.plugins.cloud.view.browser;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

import org.apache.log4j.Logger;
import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.images.IconLoader;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.presenter.PagingProvider;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.file.FileFilterEnum;

public class ResultsScrollPanel extends JScrollPane {

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(ResultsScrollPanel.class);

  private JPanel resultsPanel;
  /**
   * This label goes at the end of the page. When it gets rendered we know that we need to request
   * another page.
   */
  private JLabel loadMoreLabel;

  private ISwingUI mainUI;
  private CloudBrowserFrame cloudUI;

  private IPlugInPort plugInPort;

  private int currentLocation;
  /**
   * This flag tells us whether the loadMoreLabel should invoke a new data pull or not.
   */
  private boolean armed;
  private PagingProvider provider;

  private Icon spinnerIcon = IconLoader.Spinning.getIcon();

  public ResultsScrollPanel(ISwingUI mainUI, CloudBrowserFrame cloudUI, IPlugInPort plugInPort, PagingProvider provider) {
    super();
    this.mainUI = mainUI;
    this.cloudUI = cloudUI;
    this.plugInPort = plugInPort;
    this.provider = provider;

    this.getVerticalScrollBar().setUnitIncrement(16);
    this.setBorder(null);
    setViewportView(getResultsPanel());
  }

  public void clearPrevious() {
    currentLocation = 0;
    getResultsPanel().removeAll();
    armed = false;
  }

  public void startSearch(List<ProjectEntity> projects) {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.gridx = 0;
    gbc.gridy = 10000;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    gbc.weighty = 100;
    gbc.gridwidth = 3;
    resultsPanel.add(getLoadMoreLabel(), gbc);

    addData(projects);
  }

  public void showNoMatches() {
    JLabel label = new JLabel("<html><font size='5'>No projects match the search criteria.</font></html>");
    label.setHorizontalAlignment(SwingConstants.CENTER);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(2, 2, 2, 2);
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    gbc.weighty = Integer.MAX_VALUE;
    getResultsPanel().add(label, gbc);
    getLoadMoreLabel().setText("");
    getLoadMoreLabel().setIcon(null);
  }

  public void addData(List<ProjectEntity> projects) {
    if (projects == null || projects.isEmpty() && currentLocation == 0) {
      showNoMatches();
    } else {
      LOG.info("Adding " + projects.size() + " projects to display.");
      for (int i = 0; i < projects.size(); i++) {
        addProjectToDisplay(projects.get(i));
        this.currentLocation++;
      }
      armed = true;
      if (provider.hasMoreData()) {
        getLoadMoreLabel().setText("Querying the cloud for more results...");
        getLoadMoreLabel().setIcon(spinnerIcon);
      } else {
        getLoadMoreLabel().setText("No more results.");
        getLoadMoreLabel().setIcon(null);
      }
    }
  }

  private JComponent addProjectToDisplay(final ProjectEntity project) {
    JLabel thumbnailLabel = new JLabel(loadImage(project.getThumbnailUrl()));
    JLabel nameLabel = new JLabel("<html><b>" + project.getName() + "</b></html>");
    nameLabel.setFont(nameLabel.getFont().deriveFont(12f));

    // JLabel descriptionArea = new JLabel("<html><p align='justify'>" + project.getDescription() +
    // "</p></html>");
    JTextArea descriptionArea = new JTextArea(project.getDescription().replace("<br>", "\n"));
    descriptionArea.setEditable(false);
    descriptionArea.setFont(thumbnailLabel.getFont());
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);
    descriptionArea.setFocusable(false);
    descriptionArea.setRequestFocusEnabled(false);
    DefaultCaret caret = (DefaultCaret) descriptionArea.getCaret();
    caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
    descriptionArea.setEnabled(false);
    // descriptionArea.setDisabledTextColor(descriptionArea.getSelectionColor());

    JLabel categoryLabel = new JLabel("<html>Category: <b>" + project.getCategory() + "</b></html>");
    JLabel authorLabel = new JLabel("<html>Author: <b>" + project.getOwner() + "</b></html>");
    JLabel updatedLabel = new JLabel("<html>Last updated: <b>" + project.getUpdated() + "</b></html>");
    JButton downloadButton = new JButton(IconLoader.CloudDownload.getIcon());

    downloadButton.setBorderPainted(false);
    downloadButton.setContentAreaFilled(false);
    downloadButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    downloadButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        final File file =
            DialogFactory.getInstance().showSaveDialog((JFrame) cloudUI, FileFilterEnum.DIY.getFilter(),
                new File(project.getName() + ".diy"), FileFilterEnum.DIY.getExtensions()[0], null);
        if (file != null) {
          cloudUI.executeBackgroundTask(new ITask<Void>() {

            @Override
            public Void doInBackground() throws Exception {
              LOG.debug("Downloading project to " + file.getAbsolutePath());
              URL website = new URL(project.getDownloadUrl());
              ReadableByteChannel rbc = Channels.newChannel(website.openStream());
              FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
              fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
              fos.close();
              return null;
            }

            @Override
            public void complete(Void result) {
              if (cloudUI.showConfirmDialog("Project downloaded to " + file.getAbsolutePath()
                  + ".\nDo you want to open it?", "Cloud", ISwingUI.YES_NO_OPTION, ISwingUI.INFORMATION_MESSAGE) == IView.YES_OPTION) {
                if (!plugInPort.allowFileAction()) {
                  return;
                }

                mainUI.executeBackgroundTask(new ITask<Void>() {

                  @Override
                  public Void doInBackground() throws Exception {
                    LOG.debug("Opening from " + file.getAbsolutePath());
                    plugInPort.loadProjectFromFile(file.getAbsolutePath());
                    return null;
                  }

                  @Override
                  public void complete(Void result) {
                    mainUI.bringToFocus();
                  }

                  @Override
                  public void failed(Exception e) {
                    mainUI.showMessage("Could not open file. Detailed message is in the logs", "Error",
                        ISwingUI.ERROR_MESSAGE);
                  }
                });
              }
            }

            @Override
            public void failed(Exception e) {
              cloudUI.showMessage("Could not save to file. Detailed message is in the logs", "Error",
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
    gbc.gridy = currentLocation * 6;
    gbc.fill = GridBagConstraints.NONE;
    gbc.gridheight = 5;
    gbc.weightx = 0;
    getResultsPanel().add(thumbnailLabel, gbc);

    gbc.gridheight = 1;
    gbc.gridx = 1;
    gbc.weightx = 1;
    gbc.insets = new Insets(2, 6, 2, 2);
    getResultsPanel().add(nameLabel, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    gbc.weighty = 1;
    gbc.weightx = 1;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.insets = new Insets(2, 6, 2, 2);
    getResultsPanel().add(descriptionArea, gbc);

    gbc.gridy++;
    gbc.gridx = 1;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.NONE;
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

    return nameLabel;
  }

  private JLabel getLoadMoreLabel() {
    if (loadMoreLabel == null) {
      loadMoreLabel = new JLabel("Loading more data...") {

        private static final long serialVersionUID = 1L;

        @Override
        public void paint(Graphics g) {
          super.paint(g);
          if (armed && provider.hasMoreData()) {
            // disarm immediately so we don't trigger successive requests to the provider
            armed = false;
            SwingWorker<List<ProjectEntity>, Void> worker = new SwingWorker<List<ProjectEntity>, Void>() {

              @Override
              protected List<ProjectEntity> doInBackground() throws Exception {
                return provider.requestMoreData();
              }

              @Override
              protected void done() {
                try {
                  List<ProjectEntity> newResults = get();
                  addData(newResults);
                } catch (Exception e) {
                  cloudUI.showMessage("Search failed! Detailed message is in the logs. Please report to the author.",
                      "Search Failed", IView.ERROR_MESSAGE);
                }
              }
            };
            worker.execute();
          }
        }
      };
      loadMoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
      loadMoreLabel.setFont(loadMoreLabel.getFont().deriveFont(10f));
    }
    return loadMoreLabel;
  }

  private JPanel getResultsPanel() {
    if (resultsPanel == null) {
      resultsPanel = new JPanel(new GridBagLayout());
      resultsPanel.setBackground(Color.white);
    }
    return resultsPanel;
  }

  private Icon loadImage(String imageFile) {
    try {
      BufferedImage img = ImageIO.read(new File(imageFile));
      return new ImageIcon(img);
    } catch (Exception e) {
      return IconLoader.MissingImage.getIcon();
    }
  }
}
