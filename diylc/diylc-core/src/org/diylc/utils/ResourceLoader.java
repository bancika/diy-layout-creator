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
 * 
 */
package org.diylc.utils;

import java.io.File;

import org.apache.log4j.Logger;

public class ResourceLoader {
  
  private static final Logger LOG = Logger.getLogger(ResourceLoader.class);

  public static File[] getFiles(String relativePath) {
    String bundlePath;
    try {
      bundlePath = com.apple.eio.FileManager.getPathToApplicationBundle();
      //LOG.debug("Detected OSX bundle path: " + bundlePath);
    } catch (Exception e) {
      bundlePath = null; // on non-OSX platforms
    }
    try {
      String absolutePath;
      if (bundlePath == null)
        absolutePath = relativePath;
      else
        absolutePath = bundlePath + File.separator + "Contents" + File.separator + "Resources" + File.separator + relativePath;
      LOG.debug("Looking for files in: " + absolutePath);
      File absoluteFile = new File(absolutePath);
      return absoluteFile.listFiles();
    } catch (Exception e) {
      LOG.error("Error while looking for files in: " + relativePath, e);
      return null;
    }
  }
  
  public static File getFile(String relativePath) {
    String bundlePath;
    try {
      bundlePath = com.apple.eio.FileManager.getPathToApplicationBundle();
      //LOG.debug("Detected OSX bundle path: " + bundlePath);
    } catch (Exception e) {
      bundlePath = null; // on non-OSX platforms
    }
    try {
      String absolutePath;
      if (bundlePath == null)
        absolutePath = relativePath;
      else
        absolutePath = bundlePath + File.separator + "Contents" + File.separator + "Resources" + File.separator + relativePath;
      File absoluteFile = new File(absolutePath);
      return absoluteFile;
    } catch (Exception e) {
      LOG.error("Error while finding file: " + relativePath, e);
      return null;
    }
  }
}
