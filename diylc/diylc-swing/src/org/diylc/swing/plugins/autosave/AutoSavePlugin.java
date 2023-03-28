/*
 * 
 * DIY Layout Creator (DIYLC). Copyright (c) 2009-2018 held jointly by the individual authors.
 * 
 * This file is part of DIYLC.
 * 
 * DIYLC is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * DIYLC is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DIYLC. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.diylc.swing.plugins.autosave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
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

  private static final String AUTO_SAVE_PATH = Utils.getUserDataDirectory("diylc") + "backup";

  private static final Logger LOG = Logger.getLogger(AutoSavePlugin.class);

  public static long BACKUP_FREQ_MS = 60 * 1000;
  public static int MAX_TOTAL_SIZE_MB = 64;

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
    return EnumSet.of(EventType.PROJECT_MODIFIED, EventType.PROJECT_LOADED);
  }

  @Override
  public void processMessage(EventType eventType, Object... params) {
    if (eventType == EventType.PROJECT_MODIFIED) {
      if (System.currentTimeMillis() - lastSave > BACKUP_FREQ_MS) {
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
    } else if (eventType == EventType.PROJECT_LOADED) {
      String fileName = (String) params[2];
      if (fileName != null) {
        String backupName = generateBackupFileName(fileName);
        try {
          copyFileUsingStream(new File(fileName), new File(backupName));
          LOG.info("Copied loaded file to " + backupName);
        } catch (IOException e) {
          LOG.error("Could not copy the loaded file to backup", e);
        }
      }
    }
  }

  private static void copyFileUsingStream(File source, File dest) throws IOException {
    InputStream is = null;
    OutputStream os = null;
    try {
      is = new FileInputStream(source);
      os = new FileOutputStream(dest);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = is.read(buffer)) > 0) {
        os.write(buffer, 0, length);
      }
    } finally {
      is.close();
      os.close();
    }
  }

  private String generateBackupFileName(String baseFileName) {
    if (baseFileName == null)
      baseFileName = "Untitled";
    File file = new File(baseFileName);
    String name = file.getName();

    // remove extension
    if (name.toLowerCase().endsWith(".diy"))
      name = name.substring(0, name.length() - 4);

    // append date and time
    String timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .substring(0, 19)
        .replace(':', '-');
    file = new File(AUTO_SAVE_PATH + File.separator + name + "." + timestamp + ".diy");
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
    long maxTotalSize = MAX_TOTAL_SIZE_MB * 1024 * 1024;
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
