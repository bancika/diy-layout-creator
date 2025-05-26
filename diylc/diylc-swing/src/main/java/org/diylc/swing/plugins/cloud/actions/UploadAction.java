/*

    DIY Layout Creator (DIYLC).
    Copyright (c) 2009-2025 held jointly by the individual authors.

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
package org.diylc.swing.plugins.cloud.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import org.apache.log4j.Logger;
import org.diylc.common.ITask;
import org.diylc.core.IView;
import org.diylc.plugins.cloud.service.CloudException;
import org.diylc.swing.ISwingUI;
import org.diylc.swing.gui.DialogFactory;
import org.diylc.swing.plugins.cloud.CloudPlugIn;
import org.diylc.swing.plugins.cloud.view.UploadDialog;
import org.diylc.swing.plugins.file.FileFilterEnum;
import org.diylc.swingframework.ButtonDialog;
import org.diylc.utils.IconLoader;

public class UploadAction extends AbstractAction {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Logger.getLogger(UploadAction.class);
  private final CloudPlugIn cloudPlugIn;

  public UploadAction(CloudPlugIn cloudPlugIn) {
    super();
    this.cloudPlugIn = cloudPlugIn;
    putValue(AbstractAction.NAME, "Upload A Project");
    putValue(AbstractAction.SMALL_ICON, IconLoader.CloudUp.getIcon());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    LOG.info("UploadAction triggered");

    final File[] files =
        DialogFactory.getInstance().showOpenMultiDialog(FileFilterEnum.DIY.getFilter(), null,
            FileFilterEnum.DIY.getExtensions()[0], null, cloudPlugIn.getSwingUI().getOwnerFrame());
    if (files != null && files.length > 0) {
      List<ITask<String[]>> tasks = new ArrayList<ITask<String[]>>();
      final ListIterator<ITask<String[]>> taskIterator = tasks.listIterator();

      for (final File file : files) {
        taskIterator.add(new ITask<String[]>() {
          @Override
          public String[] doInBackground() throws Exception {
            LOG.debug("Uploading from " + file.getAbsolutePath());
            cloudPlugIn.getThumbnailPresenter().loadProjectFromFile(file.getAbsolutePath());
            return cloudPlugIn.getCloudService().getCategories();
          }

          @Override
          public void complete(final String[] result) {
            final UploadDialog dialog =
                DialogFactory.getInstance().createUploadDialog(cloudPlugIn.getSwingUI().getOwnerFrame(), 
                    cloudPlugIn.getThumbnailPresenter(), result, false);
            dialog.setVisible(true);
            if (ButtonDialog.OK.equals(dialog.getSelectedButtonCaption())) {
              try {
                final File thumbnailFile = File.createTempFile("upload-thumbnail", ".png");
                if (ImageIO.write(cloudPlugIn.getThumbnailGenerator().getThumbnail(), "png", thumbnailFile)) {
                  cloudPlugIn.getSwingUI().executeBackgroundTask(new ITask<Void>() {
                    @Override
                    public Void doInBackground() throws Exception {
                      cloudPlugIn.getCloudService().uploadProject(dialog.getName(), dialog.getCategory(), dialog
                          .getDescription(), dialog.getKeywords(), cloudPlugIn.getPlugInPort().getCurrentVersionNumber().toString(),
                          thumbnailFile, file, null);
                      return null;
                    }

                    @Override
                    public void failed(Exception e) {
                      cloudPlugIn.getSwingUI().showMessage(e.getMessage(), "Upload Error", IView.ERROR_MESSAGE);
                    }

                    @Override
                    public void complete(Void result) {
                      cloudPlugIn.getSwingUI().showMessage(
                          "The project has been uploaded to the cloud successfully. Thank you for your contribution!",
                          "Upload Success", IView.INFORMATION_MESSAGE);

                      synchronized (taskIterator) {
                        if (taskIterator.hasPrevious())
                          cloudPlugIn.getSwingUI().executeBackgroundTask(taskIterator.previous(), true);
                      }
                    }
                  }, true);
                } else {
                  cloudPlugIn.getSwingUI().showMessage("Could not prepare temporary files to be uploaded to the cloud.",
                      "Upload Error", IView.ERROR_MESSAGE);
                }
              } catch (Exception e) {
                cloudPlugIn.getSwingUI().showMessage(e.getMessage(), "Upload Error", IView.ERROR_MESSAGE);
              }
            }
          }

          @Override
          public void failed(Exception e) {
            cloudPlugIn.getSwingUI().showMessage("Could not open file. " + e.getMessage(), "Error", ISwingUI.ERROR_MESSAGE);
          }
        });
      }        

      synchronized (taskIterator) {
        if (taskIterator.hasPrevious())
          cloudPlugIn.getSwingUI().executeBackgroundTask(taskIterator.previous(), true);
      }
    }
  }
} 
