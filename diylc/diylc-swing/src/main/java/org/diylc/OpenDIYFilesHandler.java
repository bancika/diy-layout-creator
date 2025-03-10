package org.diylc;

import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.diylc.common.IPlugInPort;

public class OpenDIYFilesHandler implements OpenFilesHandler {
  
  private static final Logger LOG = Logger.getLogger(DIYLCStarter.class);
  
  private final IPlugInPort presenter;

  public OpenDIYFilesHandler(IPlugInPort presenter) {
    super();
    this.presenter = presenter;
  }

  @Override
  public void openFiles(OpenFilesEvent e) {
    
    List<File> files = e.getFiles();
    if (files != null && files.size() > 0) {
        String filePath = files.get(0).getAbsolutePath();
        if (presenter.allowFileAction()) {
            presenter.loadProjectFromFile(filePath);
        }
    }
    LOG.info("Finished setting up open file handler for Mac.");
  }

}
