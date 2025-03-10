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

import java.awt.Dimension;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;

import org.diylc.common.IPlugInPort;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.plugins.cloud.model.ProjectEntity;
import org.diylc.plugins.cloud.presenter.CloudPresenter;
import org.diylc.swing.ISimpleView;
import org.diylc.swing.ISwingUI;
import org.diylc.utils.IconLoader;

public class UploadManagerFrame extends JFrame implements ISimpleView {

  private static final String TITLE = "Manage My Uploads";

  private static final long serialVersionUID = 1L;

  private static final Logger LOG = Logger.getLogger(UploadManagerFrame.class);

  private ResultsScrollPanel resultsScrollPane;

  private IPlugInPort plugInPort;

  private ISwingUI swingUI;

  public UploadManagerFrame(ISwingUI swingUI, IPlugInPort plugInPort) {
    super(TITLE);
    this.swingUI = swingUI;
    this.setIconImage(IconLoader.CloudGear.getImage());
    this.setPreferredSize(new Dimension(700, 640));
    this.plugInPort = plugInPort;

    setContentPane(getResultsScrollPane());
    this.pack();
    this.setLocationRelativeTo(swingUI.getOwnerFrame());
    this.setGlassPane(SimpleCloudGlassPane.GLASS_PANE);

    search();
  }

  private ResultsScrollPanel getResultsScrollPane() {
    if (resultsScrollPane == null) {
      resultsScrollPane = new ResultsScrollPanel(swingUI, this, plugInPort, null, true);
    }
    return resultsScrollPane;
  }

  private void search() {
    getResultsScrollPane().clearPrevious();
    executeBackgroundTask(new ITask<List<ProjectEntity>>() {

      @Override
      public List<ProjectEntity> doInBackground() throws Exception {
        return CloudPresenter.Instance.fetchUserUploads(null);
      }

      @Override
      public void failed(Exception e) {
        showMessage("Search failed! Detailed message is in the logs. Please report to the author.", "Search Failed",
            IView.ERROR_MESSAGE);
      }

      @Override
      public void complete(List<ProjectEntity> result) {
//        setTitle(TITLE + " - " + result.size() + " Uploads Found");
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
