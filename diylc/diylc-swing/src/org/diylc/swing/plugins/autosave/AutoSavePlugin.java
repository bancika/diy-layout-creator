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
package org.diylc.swing.plugins.autosave;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.diylc.appframework.miscutils.Utils;
import org.diylc.common.EventType;
import org.diylc.common.IPlugIn;
import org.diylc.common.IPlugInPort;
import org.diylc.core.IView;

public class AutoSavePlugin implements IPlugIn {

  private static final String AUTO_SAVE_PATH = Utils.getUserDataDirectory("diylc") + "autoSave";
  
  private static final Logger LOG = Logger.getLogger(AutoSavePlugin.class);

  protected static final long autoSaveFrequencyMs = 60 * 1000;
  protected static final int maxTotalSizeMb = 64;

  private ExecutorService executor;

  private IPlugInPort plugInPort;
  private long lastSave = 0;

  public AutoSavePlugin(IView view) {
    executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void connect(IPlugInPort plugInPort) {
    this.plugInPort = plugInPort;
    // create the directory if needed
    File dir = new File(AUTO_SAVE_PATH);
    if (!dir.exists())
      dir.mkdirs();
  }

  @Override
  public EnumSet<EventType> getSubscribedEventTypes() {
    return EnumSet.of(EventType.PROJECT_MODIFIED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    if (eventType == EventType.PROJECT_MODIFIED) {
      if (System.currentTimeMillis() - lastSave > autoSaveFrequencyMs) {
        executor.execute(new Runnable() {

          @Override
          public void run() {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            lastSave = System.currentTimeMillis();
            String fileName = generateBackupFileName(plugInPort.getCurrentFileName());            
            plugInPort.saveProjectToFile(fileName, true);
            cleanupExtra();
          }
        });
      }
    }
  }
  
  private String generateBackupFileName(String baseFileName) {
    if (baseFileName == null)
      baseFileName = "Untitled";
    File file = new File(baseFileName);
    String name = file.getName();
    
    // remove extension
    if (name.toLowerCase().endsWith(".diy"))
      name = name.substring(name.length() - 4);
    
    // append date and time
    Date date = new Date(); 
    DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss");
    file = new File(AUTO_SAVE_PATH + File.separator + name + "." + dateFormat.format(date) + ".diy");
    // make sure that it doesn't already exist
    int i = 2;
    while (file.exists()) {
      file = new File(AUTO_SAVE_PATH + File.separator + name + "." + dateFormat.format(date) + "-" + i + ".diy");
      i++;
    }
    return file.getAbsolutePath();
  }
  
  private void cleanupExtra() {
    File[] files = new File(AUTO_SAVE_PATH).listFiles();
    // sort files by date
    Arrays.sort(files, new Comparator<File>() {

      @Override
      public int compare(File o1, File o2) {       
        return new Long(o1.lastModified()).compareTo(o2.lastModified());
      }      
    });
    long totalSize = 0;
    long maxTotalSize = maxTotalSizeMb * 1024 * 1024;
    for (File f : files)
      totalSize += f.length();
    int i = 0;
    while (i < files.length && totalSize > maxTotalSize) {
      totalSize -= files[i].length();
      LOG.info("Maximum backup size exceeded. Deleteting old backup file: " + files[i].getName());
      files[i].delete();
      i++;
    }
  }
}
